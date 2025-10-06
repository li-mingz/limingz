package com.limingz.mymod.util.batik;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture; // 正确的父类
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.platform.NativeImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

@OnlyIn(Dist.CLIENT)
public class TextureSaver {
    public static boolean saveTextureAsImage(ResourceLocation textureId, String outputDir, String fileName) {
        try {
            Minecraft minecraft = Minecraft.getInstance();
            AbstractTexture abstractTexture = minecraft.getTextureManager().getTexture(textureId);

            if (!(abstractTexture instanceof DynamicTexture)) {
                System.err.println("Error: Texture is not a DynamicTexture (type: " + abstractTexture.getClass().getSimpleName() + ")");
                return false;
            }

            DynamicTexture dynamicTexture = (DynamicTexture) abstractTexture;
            NativeImage nativeImage = dynamicTexture.getPixels(); // 获取 NativeImage

            if (nativeImage == null) {
                System.err.println("Error: NativeImage is null for texture: " + textureId);
                return false;
            }

            // 关键修正：使用 getPixelsRGBA() 直接获取 ARGB 格式的像素数组
            if (nativeImage.format() != NativeImage.Format.RGBA) {
                System.err.println("Error: Texture is not RGBA format, cannot save as image");
                return false;
            }
            int[] pixels = nativeImage.getPixelsRGBA();
            int width = nativeImage.getWidth();
            int height = nativeImage.getHeight();

            // 直接创建 BufferedImage（无需格式转换，getPixelsRGBA() 已返回 ARGB）
            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            bufferedImage.setRGB(0, 0, width, height, pixels, 0, width);

            // 保存图片
            File dir = Paths.get(outputDir).toFile();
            if (!dir.exists() && !dir.mkdirs()) {
                System.err.println("Failed to create directory: " + outputDir);
                return false;
            }

            File outputFile = new File(dir, fileName + ".png");
            ImageIO.write(bufferedImage, "png", outputFile);
            System.out.println("Texture saved to: " + outputFile.getAbsolutePath());
            return true;

        } catch (IOException e) {
            System.err.println("IO error saving texture: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("Error saving texture: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}