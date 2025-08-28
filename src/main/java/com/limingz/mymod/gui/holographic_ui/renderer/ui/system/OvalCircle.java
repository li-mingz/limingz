package com.limingz.mymod.gui.holographic_ui.renderer.ui.system;

import com.limingz.mymod.gui.holographic_ui.renderer.rendertype.PositionColorRenderType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Matrix4f;

public class OvalCircle extends Circle{
    protected double radiusZ; // 椭圆Z轴半径

    public OvalCircle(String id, float x, float y, int segments, float radiusX, float radiusZ, float lineWidth, int color) {
        super(id, x, y, segments, radiusX, lineWidth, color);
        this.radiusZ = radiusZ;
    }

    @Override
    public void render(MultiBufferSource bufferSource, PoseStack poseStack, int overlay, float x, float y, BlockEntity blockEntity) {
        poseStack.pushPose();
        poseStack.translate(this.x + x, 0 ,this.y + y);
        poseStack.mulPose(Axis.XP.rotationDegrees(x_rotationDegrees));
        poseStack.mulPose(Axis.YP.rotationDegrees(y_rotationDegrees));
        poseStack.mulPose(Axis.ZP.rotationDegrees(z_rotationDegrees));

        VertexConsumer vertexConsumer = bufferSource.getBuffer(PositionColorRenderType.renderType_no_uv_light_normal);
        Matrix4f matrix = poseStack.last().pose();

        float angleStep = (float) (2 * Math.PI / segments);
        OvalRender(vertexConsumer, matrix, overlay, angleStep);
        poseStack.popPose();
    }

    protected void OvalRender(VertexConsumer vertexConsumer, Matrix4f matrix, int overlay, float angleStep){
        for (int i = 0; i <= segments; i++) {
            float angle = i * angleStep;
            float nextAngle = (i + 1) * angleStep;

            // 计算外椭圆上的点
            float x1 = (float) (radius * Mth.cos(angle));
            float z1 = (float) (radiusZ * Mth.sin(angle));
            float x2 = (float) (radius * Mth.cos(nextAngle));
            float z2 = (float) (radiusZ * Mth.sin(nextAngle));

            // 计算内椭圆上的点（考虑线宽）
            float innerRadiusX = (float) Math.max(0, radius - lineWidth);
            float innerRadiusZ = (float) Math.max(0, radiusZ - lineWidth);
            float x3 = innerRadiusX * Mth.cos(nextAngle);
            float z3 = innerRadiusZ * Mth.sin(nextAngle);
            float x4 = innerRadiusX * Mth.cos(angle);
            float z4 = innerRadiusZ * Mth.sin(angle);

            // 绘制四边形（梯形带）
            UIRender.addVertex_by_position_color_overlay(vertexConsumer, matrix, x1, 0, z1, color, overlay);
            UIRender.addVertex_by_position_color_overlay(vertexConsumer, matrix, x2, 0, z2, color, overlay);
            UIRender.addVertex_by_position_color_overlay(vertexConsumer, matrix, x3, 0, z3, color, overlay);
            UIRender.addVertex_by_position_color_overlay(vertexConsumer, matrix, x4, 0, z4, color, overlay);
        }
    }
}