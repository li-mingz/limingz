package com.limingz.mymod.renderer.blockentity;


import com.limingz.mymod.block.entity.DeepBlueLabAccessControlDoorEntity;
import com.limingz.mymod.gui.holographic_ui.event.ClientForgeTickEvent;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static com.limingz.mymod.Main.MODID;

@OnlyIn(Dist.CLIENT)
public class DeepBlueLabAccessControlDoorRenderer implements BlockEntityRenderer<DeepBlueLabAccessControlDoorEntity> {
    public DeepBlueLabAccessControlDoorRenderer(BlockEntityRendererProvider.Context context) {}
    private double renderTick = 0; // 渲染计数时间
    float rotation = 0; // 旋转角度
    // 纹理位置
    public static final Material ROTATING_TEXTURE = new Material(TextureAtlas.LOCATION_BLOCKS,
            ResourceLocation.fromNamespaceAndPath(MODID, "block/demo_block"));

    @Override
    public void render(DeepBlueLabAccessControlDoorEntity entity, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        // 每 tick变一次
        if(renderTick!= ClientForgeTickEvent.updateTick){
            renderTick = ClientForgeTickEvent.updateTick;
            // 随时间旋转（每秒30度）
            rotation += 30;
            if(rotation >= 360) rotation-=360;
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        }

        // 获取方块纹理
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(ROTATING_TEXTURE.texture());

        // 设置渲染位置为中心
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        // 移回原点
        poseStack.translate(-0.5, -0.5, -0.5);

        // 创建顶点消费者
        VertexConsumer vertexBuilder = buffer.getBuffer(RenderType.solid());

        // 绘制方块六个面
        renderCube(poseStack.last(), vertexBuilder, sprite, packedLight, packedOverlay);

        poseStack.popPose();
    }

    // 绘制立方体
    private void renderCube(PoseStack.Pose matrix, VertexConsumer builder,
                            TextureAtlasSprite sprite, int light, int overlay) {
        Matrix4f matrix4f = matrix.pose();
        Matrix3f normal = matrix.normal();

        // 面的法线向量
        Vector3f[] normals = {
                new Vector3f(0, 1, 0), // 上
                new Vector3f(0, -1, 0), // 下
                new Vector3f(0, 0, -1), // 北
                new Vector3f(0, 0, 1),  // 南
                new Vector3f(-1, 0, 0), // 西
                new Vector3f(1, 0, 0)   // 东
        };

        // 顶点坐标（完整方块）
        float minX = 0;
        float minY = 0;
        float minZ = 0;
        float maxX = 1;
        float maxY = 1;
        float maxZ = 1;

        // 纹理坐标
        float uMin = sprite.getU0();
        float uMax = sprite.getU1();
        float vMin = sprite.getV0();
        float vMax = sprite.getV1();

        // 绘制每个面
        for (Vector3f normalVec : normals) {
            normalVec.mul(normal);
            renderFace(matrix4f, builder, minX, minY, minZ, maxX, maxY, maxZ,
                    normalVec, uMin, uMax, vMin, vMax, light, overlay);
        }
    }

    // 绘制单个面
    private void renderFace(Matrix4f matrix, VertexConsumer builder,
                            float minX, float minY, float minZ,
                            float maxX, float maxY, float maxZ,
                            Vector3f normal,
                            float uMin, float uMax, float vMin, float vMax,
                            int light, int overlay) {

        // 根据法线确定哪个面
        if (normal.y() > 0) { // 上面
            builder.vertex(matrix, minX, maxY, maxZ).color(1,1,1,1).uv(uMin, vMax).overlayCoords(overlay).uv2(light).normal(normal.x(), normal.y(), normal.z()).endVertex();
            builder.vertex(matrix, maxX, maxY, maxZ).color(1,1,1,1).uv(uMax, vMax).overlayCoords(overlay).uv2(light).normal(normal.x(), normal.y(), normal.z()).endVertex();
            builder.vertex(matrix, maxX, maxY, minZ).color(1,1,1,1).uv(uMax, vMin).overlayCoords(overlay).uv2(light).normal(normal.x(), normal.y(), normal.z()).endVertex();
            builder.vertex(matrix, minX, maxY, minZ).color(1,1,1,1).uv(uMin, vMin).overlayCoords(overlay).uv2(light).normal(normal.x(), normal.y(), normal.z()).endVertex();
        }
        else if (normal.y() < 0) { // 下面
            builder.vertex(matrix, minX, minY, minZ).color(1,1,1,1).uv(uMin, vMin).overlayCoords(overlay).uv2(light).normal(normal.x(), normal.y(), normal.z()).endVertex();
            builder.vertex(matrix, maxX, minY, minZ).color(1,1,1,1).uv(uMax, vMin).overlayCoords(overlay).uv2(light).normal(normal.x(), normal.y(), normal.z()).endVertex();
            builder.vertex(matrix, maxX, minY, maxZ).color(1,1,1,1).uv(uMax, vMax).overlayCoords(overlay).uv2(light).normal(normal.x(), normal.y(), normal.z()).endVertex();
            builder.vertex(matrix, minX, minY, maxZ).color(1,1,1,1).uv(uMin, vMax).overlayCoords(overlay).uv2(light).normal(normal.x(), normal.y(), normal.z()).endVertex();
        }
        else if (normal.z() > 0) { // 南面
            builder.vertex(matrix, minX, maxY, maxZ).color(1,1,1,1).uv(uMin, vMin).overlayCoords(overlay).uv2(light).normal(normal.x(), normal.y(), normal.z()).endVertex();
            builder.vertex(matrix, maxX, maxY, maxZ).color(1,1,1,1).uv(uMax, vMin).overlayCoords(overlay).uv2(light).normal(normal.x(), normal.y(), normal.z()).endVertex();
            builder.vertex(matrix, maxX, minY, maxZ).color(1,1,1,1).uv(uMax, vMax).overlayCoords(overlay).uv2(light).normal(normal.x(), normal.y(), normal.z()).endVertex();
            builder.vertex(matrix, minX, minY, maxZ).color(1,1,1,1).uv(uMin, vMax).overlayCoords(overlay).uv2(light).normal(normal.x(), normal.y(), normal.z()).endVertex();
        }
        else if (normal.z() < 0) { // 北面
            builder.vertex(matrix, minX, minY, minZ).color(1,1,1,1).uv(uMin, vMax).overlayCoords(overlay).uv2(light).normal(normal.x(), normal.y(), normal.z()).endVertex();
            builder.vertex(matrix, maxX, minY, minZ).color(1,1,1,1).uv(uMax, vMax).overlayCoords(overlay).uv2(light).normal(normal.x(), normal.y(), normal.z()).endVertex();
            builder.vertex(matrix, maxX, maxY, minZ).color(1,1,1,1).uv(uMax, vMin).overlayCoords(overlay).uv2(light).normal(normal.x(), normal.y(), normal.z()).endVertex();
            builder.vertex(matrix, minX, maxY, minZ).color(1,1,1,1).uv(uMin, vMin).overlayCoords(overlay).uv2(light).normal(normal.x(), normal.y(), normal.z()).endVertex();
        }
        else if (normal.x() > 0) { // 东面
            builder.vertex(matrix, maxX, minY, minZ).color(1,1,1,1).uv(uMin, vMax).overlayCoords(overlay).uv2(light).normal(normal.x(), normal.y(), normal.z()).endVertex();
            builder.vertex(matrix, maxX, minY, maxZ).color(1,1,1,1).uv(uMax, vMax).overlayCoords(overlay).uv2(light).normal(normal.x(), normal.y(), normal.z()).endVertex();
            builder.vertex(matrix, maxX, maxY, maxZ).color(1,1,1,1).uv(uMax, vMin).overlayCoords(overlay).uv2(light).normal(normal.x(), normal.y(), normal.z()).endVertex();
            builder.vertex(matrix, maxX, maxY, minZ).color(1,1,1,1).uv(uMin, vMin).overlayCoords(overlay).uv2(light).normal(normal.x(), normal.y(), normal.z()).endVertex();
        }
        else { // 西面
            builder.vertex(matrix, minX, minY, maxZ).color(1,1,1,1).uv(uMin, vMax).overlayCoords(overlay).uv2(light).normal(normal.x(), normal.y(), normal.z()).endVertex();
            builder.vertex(matrix, minX, minY, minZ).color(1,1,1,1).uv(uMax, vMax).overlayCoords(overlay).uv2(light).normal(normal.x(), normal.y(), normal.z()).endVertex();
            builder.vertex(matrix, minX, maxY, minZ).color(1,1,1,1).uv(uMax, vMin).overlayCoords(overlay).uv2(light).normal(normal.x(), normal.y(), normal.z()).endVertex();
            builder.vertex(matrix, minX, maxY, maxZ).color(1,1,1,1).uv(uMin, vMin).overlayCoords(overlay).uv2(light).normal(normal.x(), normal.y(), normal.z()).endVertex();
        }
    }
}