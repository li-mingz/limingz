package com.limingz.mymod.gui.holographic_ui.renderer.ui.system;

import com.limingz.mymod.gui.holographic_ui.config.UIConfig;
import com.limingz.mymod.gui.holographic_ui.font.NoCuLLFontBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class UIRender {
    /**
     * 绘制背景
     * @param bufferSource 缓冲构建器, 用来将渲染数据格式化并上传到 OpenGL
     * @param poseStack pose堆栈管理接口
     * @param x 相对于原点的 x 坐标
     * @param y 相对于原点的 y 坐标
     * @param width 宽
     * @param height 高
     * @param light 封装的光照信息
     * @param combinedOverlay 指定当前顶点的覆盖纹理（Overlay Texture）坐标
     *                高 16 位（bits 16-31） 存储 U 坐标（水平方向）
     *                低 16 位（bits 0-15） 存储 V 坐标（垂直方向）
     * @param texture 材质资源
     */
    public static void renderBackground(MultiBufferSource bufferSource, PoseStack poseStack,
                                        float x, float y, float width, float height, int light, int combinedOverlay,
                                        ResourceLocation texture){
        VertexConsumer bgVertexBuilder = bufferSource.getBuffer(RenderType.entityTranslucent(texture));
        Matrix4f matrix = poseStack.last().pose();
        Vector3f normal = new Vector3f(0, 1, 0);
        normal.rotateX((float) Math.toRadians(UIConfig.BG_ROTATION));
        renderRectangle(bgVertexBuilder, matrix, normal, x, width + x, y, height + y,
                1, 1, 1, 1, light, combinedOverlay);
    }


    /**
     * 绘制矩形
     * @param builder 顶点渲染核心接口，用于构建和提交顶点数据到 GPU 进行渲染
     * @param matrix 4x4 变换矩阵的实例，主要用于几何变换（平移/旋转/缩放）和投影计算
     * @param normal 矩形的法线
     * @param left 矩形左侧边界的 x 坐标
     * @param right 矩形右侧侧边界的 x 坐标
     * @param top 矩形顶部边界的 y 坐标
     * @param bottom 矩形底部边界的 y 坐标
     * @param r RGB
     * @param g RGB
     * @param b RGB
     * @param a 透明度
     * @param light 封装的光照信息
     * @param overlay 指定当前顶点的覆盖纹理（Overlay Texture）坐标
     *                高 16 位（bits 16-31） 存储 U 坐标（水平方向）
     *                低 16 位（bits 0-15） 存储 V 坐标（垂直方向）
     */
    public static void renderRectangle(VertexConsumer builder, Matrix4f matrix, Vector3f normal,
                                       float left, float right, float top, float bottom,
                                       float r, float g, float b, float a,
                                       int light, int overlay) {
        addVertex(builder, matrix, normal, left, 0, bottom, 0, 1, r, g, b, a, light, overlay);
        addVertex(builder, matrix, normal, right, 0, bottom, 1, 1, r, g, b, a, light, overlay);
        addVertex(builder, matrix, normal, right, 0, top, 1, 0, r, g, b, a, light, overlay);
        addVertex(builder, matrix, normal, left, 0, top, 0, 0, r, g, b, a, light, overlay);
    }


    /**
     * 绘制矩形
     * @param builder 顶点渲染核心接口，用于构建和提交顶点数据到 GPU 进行渲染
     * @param matrix 4x4 变换矩阵的实例，主要用于几何变换（平移/旋转/缩放）和投影计算
     * @param normal 矩形的法线
     * @param left 矩形左侧边界的 x 坐标
     * @param right 矩形右侧侧边界的 x 坐标
     * @param top 矩形顶部边界的 y 坐标
     * @param bottom 矩形底部边界的 y 坐标
     * @param color 颜色
     * @param light 封装的光照信息
     * @param overlay 指定当前顶点的覆盖纹理（Overlay Texture）坐标
     *                高 16 位（bits 16-31） 存储 U 坐标（水平方向）
     *                低 16 位（bits 0-15） 存储 V 坐标（垂直方向）
     */
    public static void renderRectangle(VertexConsumer builder, Matrix4f matrix, Vector3f normal,
                                       float left, float right, float top, float bottom, float y,
                                       int color,
                                       int light, int overlay) {
        addVertex(builder, matrix, normal, left, y, bottom, 0, 1, color, light, overlay);
        addVertex(builder, matrix, normal, right, y, bottom, 1, 1, color, light, overlay);
        addVertex(builder, matrix, normal, right, y, top, 1, 0, color, light, overlay);
        addVertex(builder, matrix, normal, left, y, top, 0, 0, color, light, overlay);
    }

    /**
     被包装的vertex函数
     */
    public static void addVertex(VertexConsumer builder, Matrix4f matrix, Vector3f normal,
                                 float x, float y, float z, float u, float v,
                                 float r, float g, float b, float a,
                                 int light, int overlay) {
        builder.vertex(matrix, x, y, z)
                .color(r, g, b, a)
                .uv(u, v)
                .overlayCoords(overlay)
                .uv2(light)
                .normal(normal.x(), normal.y(), normal.z())
                .endVertex();
    }

    /**
     被包装的vertex函数
     */
    public static void addVertex(VertexConsumer builder, Matrix4f matrix, Vector3f normal,
                                 float x, float y, float z, float u, float v,
                                 int color,
                                 int light, int overlay) {
        builder.vertex(matrix, x, y, z)
                .color(color)
                .uv(u, v)
                .overlayCoords(overlay)
                .uv2(light)
                .normal(normal.x(), normal.y(), normal.z())
                .endVertex();
    }

    /**
     被包装的vertex函数
     */
    public static void addVertex_by_position_color_overlay(VertexConsumer builder, Matrix4f matrix,
                                 float x, float y, float z,
                                 int color, int overlay) {
        builder.vertex(matrix, x, y, z)
                .color(color)
                .overlayCoords(overlay)
                .endVertex();
    }




    /**
     *
     * @param poseStack pose堆栈管理接口
     * @param bufferSource 缓冲构建器, 用来将渲染数据格式化并上传到 OpenGL
     * @param x 相对于原点的 x 坐标
     * @param y 相对于原点的 y 坐标
     * @param text 文本
     * @param light 光照
     * @param color 文本颜色 16进制
     * @param scale 文本大小
     */
    public static void renderText(MultiBufferSource bufferSource, PoseStack poseStack,
                                  float x, float y, String text, int light, int color, float scale) {
        poseStack.pushPose();
        // 先平移，再旋转，最后调整大小
        poseStack.translate(x, 0, y);
        poseStack.mulPose(Axis.XP.rotationDegrees(90));
        poseStack.scale(scale, scale, scale);
        // 获取Minecraft实例和字体渲染器
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;

        // 渲染文字（使用自定义背部剔除类型）
        font.drawInBatch(
                text,
                0,
                0,
                color,
                false,
                poseStack.last().pose(),
                new NoCuLLFontBufferSource((MultiBufferSource.BufferSource) bufferSource),
                Font.DisplayMode.NORMAL,
                0,
                light
        );
        poseStack.popPose();
    }

    /**
     *
     * @param poseStack pose堆栈管理接口
     * @param bufferSource 缓冲构建器, 用来将渲染数据格式化并上传到 OpenGL
     * @param x 相对于原点的 x 坐标
     * @param y 相对于原点的 y 坐标
     * @param text 文本
     * @param light 光照
     * @param color 文本颜色 16进制
     */
    public static void renderTextCenter(MultiBufferSource bufferSource, PoseStack poseStack,
                                  float x, float y, String text, int light, int color, float scale) {
        poseStack.pushPose();
        // 先平移，再旋转，最后调整大小
        poseStack.translate(x, 0, y);
        poseStack.mulPose(Axis.XP.rotationDegrees(90));
        poseStack.scale(scale, scale, scale);
        // 获取Minecraft实例和字体渲染器
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;

        // 计算文字居中偏移
        float textWidth = font.width(text);
        float xOffset = -textWidth / 2.0f;
        float yOffset = 0;

        // 渲染文字（使用自定义背部剔除类型）
        font.drawInBatch(
                text,
                xOffset,
                yOffset,
                color,
                false,
                poseStack.last().pose(),
                new NoCuLLFontBufferSource((MultiBufferSource.BufferSource) bufferSource),
                Font.DisplayMode.NORMAL,
                0,
                light
        );
        poseStack.popPose();
    }



    /**
     *
     * @param poseStack pose堆栈管理接口
     * @param bufferSource 缓冲构建器, 用来将渲染数据格式化并上传到 OpenGL
     * @param x 相对于原点的 x 坐标
     * @param y 相对于原点的 y 坐标
     * @param text 文本
     * @param light 光照
     * @param color 文本颜色 16进制
     * @param style 文本样式
     */
    public static void renderTextCenterByFont(MultiBufferSource bufferSource, PoseStack poseStack,
                                        float x, float y, String text, int light, int color, float scale, Style style) {
        poseStack.pushPose();
        // 先平移，再旋转，最后调整大小
        poseStack.translate(x, 0, y);
        poseStack.mulPose(Axis.XP.rotationDegrees(90));
        // 海嗣文应翻转180度使用(大部分情况下)
        poseStack.mulPose(Axis.YP.rotationDegrees(180));
        poseStack.scale(scale, scale, scale);
        // 获取Minecraft实例和字体渲染器
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;

        Component component = Component.literal(text).withStyle(style);

        // 计算文字居中偏移
        float textWidth = font.width(text);
        float xOffset = -textWidth / 2.0f;
        float yOffset = 0;

        // 渲染文字（使用自定义背部剔除类型）
        font.drawInBatch(
                component,
                xOffset,
                yOffset,
                color,
                false,
                poseStack.last().pose(),
                new NoCuLLFontBufferSource((MultiBufferSource.BufferSource) bufferSource),
                Font.DisplayMode.NORMAL,
                0,
                light
        );
        poseStack.popPose();
    }
}
