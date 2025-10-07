package com.limingz.mymod.util.pacture;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PNGTextureManager {
    // 纹理缓存：键为PNG资源位置，值为注册后的纹理位置
    private static final Map<ResourceLocation, ResourceLocation> TEXTURE_CACHE = new HashMap<>();

    /**
     * 获取或创建PNG纹理
     * @param pngLocation PNG资源的位置
     * @return 注册后的纹理资源位置
     */
    public static ResourceLocation getOrCreateTexture(ResourceLocation pngLocation) {
        // 先检查缓存
        if (TEXTURE_CACHE.containsKey(pngLocation)) {
            return TEXTURE_CACHE.get(pngLocation);
        }

        try {
            ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
            Optional<Resource> optionalResource = resourceManager.getResource(pngLocation);
            Resource pngResource = optionalResource.orElseThrow(() ->
                    new RuntimeException("PNG resource not found: " + pngLocation)
            );

            try (InputStream inputStream = pngResource.open()) {
                // 1. 读取PNG为BufferedImage
                BufferedImage bufferedImage = readPngImage(inputStream);

                // 2. 转换为NativeImage（适配Minecraft渲染）
                NativeImage nativeImage = convertToNativeImage(bufferedImage);

                // 3. 创建动态纹理并注册
                DynamicTexture dynamicTexture = new DynamicTexture(nativeImage);
                // 生成纹理ID（使用png路径作为唯一标识）
                ResourceLocation textureId = Minecraft.getInstance().getTextureManager()
                        .register("png/" + pngLocation.getPath().replaceAll("/", "_"), dynamicTexture);

                // 存入缓存
                TEXTURE_CACHE.put(pngLocation, textureId);
                return textureId;
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to open PNG input stream: " + pngLocation, e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load PNG texture: " + pngLocation, e);
        }
    }

    /**
     * 从输入流读取PNG图像
     */
    private static BufferedImage readPngImage(InputStream inputStream) throws IOException {
        BufferedImage image = ImageIO.read(inputStream);
        if (image == null) {
            throw new IOException("Could not read PNG image (ImageIO returned null)");
        }
        // 确保图像格式为ARGB（带alpha通道）
        if (image.getType() != BufferedImage.TYPE_INT_ARGB) {
            BufferedImage argbImage = new BufferedImage(
                    image.getWidth(),
                    image.getHeight(),
                    BufferedImage.TYPE_INT_ARGB
            );
            argbImage.getGraphics().drawImage(image, 0, 0, null);
            image = argbImage;
        }
        return image;
    }

    /**
     * 转换BufferedImage到NativeImage（处理像素格式）
     * Minecraft的NativeImage使用ABGR格式存储像素
     */
    private static NativeImage convertToNativeImage(BufferedImage bufferedImage) {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        // 创建带alpha通道的NativeImage
        NativeImage nativeImage = new NativeImage(width, height, true);

        // 获取BufferedImage的像素数据（ARGB格式）
        int[] argbPixels = ((DataBufferInt) bufferedImage.getRaster().getDataBuffer()).getData();

        // 转换ARGB到ABGR格式（适配NativeImage）
        for (int i = 0; i < argbPixels.length; i++) {
            int argb = argbPixels[i];
            int alpha = (argb >> 24) & 0xFF;
            int red = (argb >> 16) & 0xFF;
            int green = (argb >> 8) & 0xFF;
            int blue = argb & 0xFF;

            // 转换为ABGR格式
            int abgr = (alpha << 24) | (blue << 16) | (green << 8) | red;

            // 计算坐标并设置像素
            int x = i % width;
            int y = i / width;
            nativeImage.setPixelRGBA(x, y, abgr);
        }
        return nativeImage;
    }

    /**
     * 清除纹理缓存（用于资源重载等场景）
     */
    public static void clearCache() {
        TEXTURE_CACHE.clear();
    }
}