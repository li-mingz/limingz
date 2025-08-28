package com.limingz.mymod.gui.holographic_ui.renderer.ui.system;

import com.limingz.mymod.gui.holographic_ui.renderer.rendertype.PositionColorRenderType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Matrix4f;

public class Circle extends UIComponent{
    protected double radius; // 圆环半径
    protected float lineWidth; // 线条宽度
    protected int color; // ARGB 格式
    protected int segments; // 分段数
    protected float x_rotationDegrees = 0;
    protected float y_rotationDegrees = 0;
    protected float z_rotationDegrees = 0;
    // 2π常量
    protected final float PI2 = (2 * (float) Math.PI);

    public Circle(String id, float x, float y, int segments, float radius, float lineWidth, int color) {
        super(id, x, y);
        // 不可被点击
        setCanClick(false);
        this.segments = segments;
        this.radius = radius;
        this.lineWidth = lineWidth;
        this.color = color;
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

        for (int i = 0; i <= segments; i++) {
            float angle1 = i * angleStep;
            float angle2 = (i + 1) * angleStep;

            float x1 = (float) (radius * Mth.cos(angle1));
            float z1 = (float) (radius * Mth.sin(angle1));
            float x2 = (float) (radius * Mth.cos(angle2));
            float z2 = (float) (radius * Mth.sin(angle2));
            float x3 = (float) ((radius-lineWidth) * Mth.cos(angle2));
            float z3 = (float) ((radius-lineWidth) * Mth.sin(angle2));
            float x4 = (float) ((radius-lineWidth) * Mth.cos(angle1));
            float z4 = (float) ((radius-lineWidth) * Mth.sin(angle1));

            UIRender.addVertex_by_position_color_overlay(vertexConsumer, matrix, x4, 0, z4, color, overlay);
            UIRender.addVertex_by_position_color_overlay(vertexConsumer, matrix, x3, 0, z3, color, overlay);
            UIRender.addVertex_by_position_color_overlay(vertexConsumer, matrix, x2, 0, z2, color, overlay);
            UIRender.addVertex_by_position_color_overlay(vertexConsumer, matrix, x1, 0, z1, color, overlay);
        }

        poseStack.popPose();
    }

    public float getX_rotationDegrees() {
        return x_rotationDegrees;
    }

    public void setX_rotationDegrees(float x_rotationDegrees) {
        this.x_rotationDegrees = x_rotationDegrees;
    }

    public float getY_rotationDegrees() {
        return y_rotationDegrees;
    }

    public void setY_rotationDegrees(float y_rotationDegrees) {
        this.y_rotationDegrees = y_rotationDegrees;
    }

    public float getZ_rotationDegrees() {
        return z_rotationDegrees;
    }

    public void setZ_rotationDegrees(float z_rotationDegrees) {
        this.z_rotationDegrees = z_rotationDegrees;
    }
}
