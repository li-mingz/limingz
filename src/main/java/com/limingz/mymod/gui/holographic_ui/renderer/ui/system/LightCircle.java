package com.limingz.mymod.gui.holographic_ui.renderer.ui.system;

import com.limingz.mymod.gui.holographic_ui.event.ClientForgeTickEvent;
import com.limingz.mymod.gui.holographic_ui.renderer.rendertype.PositionColorRenderType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Matrix4f;

public class LightCircle extends Circle{
    private float speed = 0.005f; // 光效旋转速度
    // 定义拖尾长度
    final float TAIL_LENGTH = (float) Math.PI; // 180度拖尾效果
    // 2π常量
    final float PI2 = (2 * (float) Math.PI);
    // 光效方向控制 (false为顺时针，true为逆时针)
    private boolean clockwise = true;
    private float dynamicAngle = 0f;  // 当前光效头部的角度

    private float brightnessIntensity = 0.5f;  // 光效增加的最高亮度
    private double renderTick = 0; // 渲染计数时间

    public LightCircle(String id, float x, float y, int segments, float radius, float lineWidth, int color) {
        super(id, x, y, segments, radius, lineWidth, color);
    }

    // 设置光效方向
    public void setEffectDirection(boolean clockwise) {
        this.clockwise = clockwise;
    }

    // 设置光效增加的最高亮度
    public void setBrightnessIntensity(float brightnessIntensity) {
        this.brightnessIntensity = brightnessIntensity;
    }

    // 设置光效旋转速度
    public void setSpeed(float speed) {
        this.speed = speed;
    }

    @Override
    public void render(MultiBufferSource bufferSource, PoseStack poseStack, int overlay, float x, float y, BlockEntity blockEntity) {
        // 每 tick变一次
        if(renderTick!= ClientForgeTickEvent.updateTick){
            renderTick = ClientForgeTickEvent.updateTick;
            // 根据光效方向调整角度变化
            dynamicAngle = (dynamicAngle + (clockwise ? -speed : speed)) % PI2;
            if (dynamicAngle < 0) dynamicAngle += (float) (2 * Math.PI); // 确保角度在0-2π范围内
        }

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

            // 传入光效方向参数
            // 原始光带亮度因子
            float factor1_1 = calculateTailBrightness(angle1, dynamicAngle, TAIL_LENGTH, clockwise);
            float factor2_1 = calculateTailBrightness(angle2, dynamicAngle, TAIL_LENGTH, clockwise);

            // 计算对称光带角度 (180度偏移)
            float symAngle = dynamicAngle + (float)Math.PI;
            symAngle = symAngle % (2 * (float)Math.PI);
            if (symAngle < 0) symAngle += (2 * (float)Math.PI);

            // 对称光带亮度因子
            float factor1_2 = calculateTailBrightness(angle1, symAngle, TAIL_LENGTH, clockwise);
            float factor2_2 = calculateTailBrightness(angle2, symAngle, TAIL_LENGTH, clockwise);

            // 合并亮度因子（取最大值）
            float factor1 = Math.max(factor1_1, factor1_2);
            float factor2 = Math.max(factor2_1, factor2_2);

            int color1 = applyBrightness(color, factor1);
            int color2 = applyBrightness(color, factor2);

            float x1 = (float) (radius * Mth.cos(angle1));
            float z1 = (float) (radius * Mth.sin(angle1));
            float x2 = (float) (radius * Mth.cos(angle2));
            float z2 = (float) (radius * Mth.sin(angle2));
            float x3 = (float) ((radius-lineWidth) * Mth.cos(angle2));
            float z3 = (float) ((radius-lineWidth) * Mth.sin(angle2));
            float x4 = (float) ((radius-lineWidth) * Mth.cos(angle1));
            float z4 = (float) ((radius-lineWidth) * Mth.sin(angle1));

            UIRender.addVertex_by_position_color_overlay(vertexConsumer, matrix, x4, 0, z4, color1, overlay);
            UIRender.addVertex_by_position_color_overlay(vertexConsumer, matrix, x3, 0, z3, color2, overlay);
            UIRender.addVertex_by_position_color_overlay(vertexConsumer, matrix, x2, 0, z2, color2, overlay);
            UIRender.addVertex_by_position_color_overlay(vertexConsumer, matrix, x1, 0, z1, color1, overlay);
        }

        poseStack.popPose();
    }

    /**
     * 获取当前绘制线段在光效中的位置
     * @param pointAngle  当前绘制线段所在角度
     * @param headAngle  当前光效头部线段所在角度
     * @param tailLength  光效拖尾长度
     * @param clockwise  光效是否逆时针旋转
     * @return 归一化后的位置
     */
    private float calculateTailBrightness(float pointAngle, float headAngle, float tailLength, boolean clockwise) {
        // 根据光效方向调整角度差计算
        float diff;
        if (clockwise) {
            diff = headAngle - pointAngle; // 逆时针旋转时的角度差
        } else {
            diff = pointAngle - headAngle; // 顺时针旋转时的角度差
        }

        // 归一化角度差到0-2π范围
        diff = (diff % PI2 + PI2) % PI2;

        // 反转角度差模拟拖尾方向
        diff = PI2 - diff;

        float position = diff / tailLength;

        if (position > 1.0f || position < 0.0f) {
            return 0.0f;
        }

        return Mth.clamp(1.0f - position, 0.0f, 1.0f);
    }

    /**
     * 获取更亮的输入的颜色
     * @param color 输入的颜色
     * @param factor 输出颜色的亮度(0为初始,1为最亮)
     * @return 更亮的颜色
     */
    private int applyBrightness(int color, float factor) {
        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        int whiteMix = (int) (255 * factor * brightnessIntensity);

        r = Mth.clamp(r + whiteMix, 0, 255);
        g = Mth.clamp(g + whiteMix, 0, 255);
        b = Mth.clamp(b + whiteMix, 0, 255);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
