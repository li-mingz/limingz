package com.limingz.mymod.util.batik;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SVGTextureManager {
    private static final Map<ResourceLocation, ResourceLocation> TEXTURE_CACHE = new HashMap<>();

    public static ResourceLocation getOrCreateTexture(ResourceLocation svgLocation, int width, int height) {
        if (TEXTURE_CACHE.containsKey(svgLocation)) {
            return TEXTURE_CACHE.get(svgLocation);
        }

        try {
            ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
            Optional<Resource> optionalResource = resourceManager.getResource(svgLocation);
            Resource svgResource = optionalResource.orElseThrow(() ->
                    new RuntimeException("SVG resource not found: " + svgLocation)
            );

            // 修正：使用 open() 方法获取输入流（需处理 IOException）
            try (InputStream inputStream = svgResource.open()) {
                BufferedImage bufferedImage = parseSVG(inputStream, width, height);
//                File outputfile = new File("saved_image.png");
//                ImageIO.write(bufferedImage, "png", outputfile);
//                System.out.println("图像已保存至 "+outputfile.getAbsolutePath());
                // 转换为 NativeImage
                NativeImage nativeImage = new NativeImage(width, height, true);
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        nativeImage.setPixelRGBA(x, y, bufferedImage.getRGB(x, y));
                    }
                }

                // 创建并注册动态纹理
                DynamicTexture dynamicTexture = new DynamicTexture(nativeImage);
                ResourceLocation textureId = Minecraft.getInstance().getTextureManager()
                        .register("svg/" + svgLocation.getPath(), dynamicTexture);
//                TextureSaver.saveTextureAsImage(textureId, "C:/aaa", "aba");
                TEXTURE_CACHE.put(svgLocation, textureId);
                return textureId;
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to open SVG input stream: " + svgLocation, e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SVG texture: " + svgLocation, e);
        }
    }

    private static BufferedImage parseSVG(InputStream inputStream, int width, int height) throws Exception {
        class CustomTranscoder extends ImageTranscoder {
            private BufferedImage image;

            @Override
            public BufferedImage createImage(int w, int h) {
                return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            }

            @Override
            public void writeImage(BufferedImage img, TranscoderOutput out) {
                this.image = img;
            }

            public BufferedImage getImage() {
                return image;
            }
        }

        CustomTranscoder transcoder = new CustomTranscoder();
        transcoder.addTranscodingHint(ImageTranscoder.KEY_WIDTH, (float) width);
        transcoder.addTranscodingHint(ImageTranscoder.KEY_HEIGHT, (float) height);
        transcoder.transcode(new TranscoderInput(inputStream), null);
        return transcoder.getImage();
    }

    public static void clearCache() {
        TEXTURE_CACHE.clear();
    }
}