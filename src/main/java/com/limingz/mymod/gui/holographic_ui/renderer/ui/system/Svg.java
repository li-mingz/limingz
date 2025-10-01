package com.limingz.mymod.gui.holographic_ui.renderer.ui.system;

import com.limingz.mymod.Main;
import com.limingz.mymod.util.batik.SVGTextureManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

public class Svg extends UIComponent{

    public Svg(String id, float x, float y, float width, float height) {
        super(id, x, y, width, height);
    }
    // SVG资源路径（需放在 mod资源包/resources/modid/svg/ 下）
    private static final ResourceLocation SVG_LOCATION = ResourceLocation.fromNamespaceAndPath(Main.MODID, "svg/wiki_into/data-frame-1.svg");

    @Override
    public void render(MultiBufferSource bufferSource, PoseStack poseStack, int combinedOverlay, float x, float y, BlockEntity blockEntity) {
        super.render(bufferSource, poseStack, combinedOverlay, x, y, blockEntity);
        // 1. 获取SVG纹理ID
        ResourceLocation textureId = SVGTextureManager.getOrCreateTexture(SVG_LOCATION, (int) width, (int) height);

        // 2. 准备渲染矩阵（平移、旋转、缩放）
        poseStack.pushPose();

        // 3. 获取顶点消费者（使用带透明通道的渲染类型）
        VertexConsumer vertexConsumer = bufferSource.getBuffer(
                RenderType.entityTranslucent(textureId) // 支持透明的渲染类型
        );

        // 4. 绘制一个四边形（矩形）来展示SVG纹理
        // 四边形的四个顶点坐标（本地坐标，以原点为中心）
        float halfW = width / 2.0f / 16.0f; // 转换为方块单位（1格=16像素）
        float halfH = height / 2.0f / 16.0f;

        // 顶点顺序：左上、右上、右下、左下（确保UV坐标对应）
        // 顶点1：左上
        poseStack.pushPose();
        vertexConsumer.vertex(poseStack.last().pose(), -halfW, halfH, 0)
                .color(1.0f, 1.0f, 1.0f, 1.0f) // 白色不透明度100%
                .uv(0.0f, 0.0f) // 纹理左上角UV
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light) // 光照信息
                .normal(0, 1, 0)
                .endVertex();

        // 顶点2：右上
        vertexConsumer.vertex(poseStack.last().pose(), halfW, halfH, 0)
                .color(1.0f, 1.0f, 1.0f, 1.0f)
                .uv(1.0f, 0.0f) // 纹理右上角UV
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(0, 1, 0)
                .endVertex();

        // 顶点3：右下
        vertexConsumer.vertex(poseStack.last().pose(), halfW, -halfH, 0)
                .color(1.0f, 1.0f, 1.0f, 1.0f)
                .uv(1.0f, 1.0f) // 纹理右下角UV
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(0, 1, 0)
                .endVertex();

        // 顶点4：左下
        vertexConsumer.vertex(poseStack.last().pose(), -halfW, -halfH, 0)
                .color(1.0f, 1.0f, 1.0f, 1.0f)
                .uv(0.0f, 1.0f) // 纹理左下角UV
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(0, 1, 0)
                .endVertex();
        poseStack.popPose();

        poseStack.popPose();
    }
}
