package com.limingz.mymod.gui.holographic_ui.renderer.ui.system;

import com.limingz.mymod.gui.holographic_ui.renderer.rendertype.PositionColorRenderType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class LineByPoint extends UIComponent {
    private int DottedStep = 1;
    private float lineWidth;
    private int color;
    private ResourceLocation texture;
    // 贝塞尔曲线
    private final List<float[]> bezierSegments = new ArrayList<float[]>() {{
        add(new float[]{0, 0, 152, 32, 0, 152, 53, 0.025f, 137, 77, 0.04f, 96});
        add(new float[]{77, 0.04f, 96, 106, 0.075f, 55, 126, 0.075f, 58, 160, 0.075f, 58});
        add(new float[]{0, 0, 152, 32, 0, 152, 61, 0.025f, 145, 84, 0.04f, 114});
        add(new float[]{84, 0.04f, 114, 111, 0.075f, 78, 125, 0.075f, 82, 160, 0.075f, 82});
        add(new float[]{0, 0, 152, 32, 0, 152, 70, 0.025f, 147, 91, 0.05f, 119});
        add(new float[]{91, 0.05f, 119, 107, 0.075f, 100, 123, 0.075f, 100, 160, 0.075f, 100});
        add(new float[]{0, 0, 152, 32, 0, 152, 74, 0.025f, 148, 98, 0.06f, 123});
        add(new float[]{98, 0.06f, 123, 110, 0.075f, 113, 123, 0.075f, 114, 160, 0.075f, 114});
    }};
    private List<float[]> curvePoints;
    // 定义4种坐标变换组合
    private final float[][] transforms = {
            {1.0f, 1.0f},   // 原始
            {-1.0f, 1.0f},  // X轴翻转
            {1.0f, -1.0f},  // Z轴翻转
            {-1.0f, -1.0f}  // X轴和Z轴同时翻转
    };

    private static final int BEZIER_SEGMENTS = 60; // 每段曲线的细分数量

    public LineByPoint(String id, float x, float y, float width, float height, float lineWidth, int color, ResourceLocation texture) {
        super(id, x, y, width, height);
        this.lineWidth = lineWidth/2;
        this.color = color;
        this.texture = texture;
        // 生成贝塞尔曲线点序列
        curvePoints = calculateBezierPoints();
    }

    // 计算贝塞尔曲线上的点
    private List<float[]> calculateBezierPoints() {
        List<float[]> curvePoints = new ArrayList<>();

        for (float[] segment : bezierSegments) {
            // 解析三维控制点 (x,y,z)
            Vector3f p0 = new Vector3f(segment[0], segment[1], segment[2]);
            Vector3f p1 = new Vector3f(segment[3], segment[4], segment[5]);
            Vector3f p2 = new Vector3f(segment[6], segment[7], segment[8]);
            Vector3f p3 = new Vector3f(segment[9], segment[10], segment[11]);

            // 归一化时只处理x和z坐标，y坐标保持不变
            normalizePoint(p0, 565, 333);
            normalizePoint(p1, 565, 333);
            normalizePoint(p2, 565, 333);
            normalizePoint(p3, 565, 333);

            // 计算贝塞尔曲线上的点
            for (int i = 0; i < BEZIER_SEGMENTS; i++) {
                float t = (float) i / BEZIER_SEGMENTS;
                Vector3f point = calculateBezierPoint(t, p0, p1, p2, p3);
                curvePoints.add(new float[]{point.x(), point.y(), point.z()});
            }
        }

        return curvePoints;
    }

    // 贝塞尔曲线计算公式
    private Vector3f calculateBezierPoint(float t, Vector3f p0, Vector3f p1, Vector3f p2, Vector3f p3) {
        float u = 1 - t;
        float uu = u * u;
        float uuu = uu * u;
        float tt = t * t;
        float ttt = tt * t;

        Vector3f point = new Vector3f(p0).mul(uuu);
        point.add(new Vector3f(p1).mul(3 * uu * t));
        point.add(new Vector3f(p2).mul(3 * u * tt));
        point.add(new Vector3f(p3).mul(ttt));

        return point;
    }

    // 归一化
    private void normalizePoint(Vector3f point, float maxX, float maxZ) {
        point.x = point.x() / maxX;
        point.z = point.z() / maxZ;
    }

    @Override
    public void render(MultiBufferSource bufferSource, PoseStack poseStack, int combinedOverlay, float x, float y, BlockEntity blockEntity) {
        super.render(bufferSource, poseStack, combinedOverlay, x, y, blockEntity);
        poseStack.pushPose();
        poseStack.translate(this.x + x, 0, this.y + y);

        VertexConsumer vertexConsumer = bufferSource.getBuffer(PositionColorRenderType.renderType_no_uv_light_normal);
        Matrix4f matrix = poseStack.last().pose();

        // 应用所有变换组合
        for (float[] transform : transforms) {
            float xScale = transform[0];
            float zScale = transform[1];

            // 绘制带宽度的曲线（应用当前变换）
            for (int i = 0; i < curvePoints.size() - 1; i++) {

                float[] start = curvePoints.get(i);
                float[] end = curvePoints.get(i + 1);

                float ex = (xScale < 0 ? width - end[0] * width : end[0] * width);
                float ey = end[1]; // y坐标直接使用不缩放
                float ez = (zScale < 0 ? height - end[2] * height : end[2] * height);

                if(i % (BEZIER_SEGMENTS * 2) == BEZIER_SEGMENTS * 2 - 2){
                    Vector3f normal = new Vector3f(0, 1, 0);
                    vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(texture));
                    float left = ex;
                    float right = (xScale < 0 ? ex-lineWidth*20 : ex+lineWidth*20);
                    float top = (zScale < 0 ? ez+lineWidth*15 : ez-lineWidth*15);
                    float bottom = (zScale < 0 ? ez-lineWidth*5 : ez+lineWidth*5);

                    UIRender.renderRectangle(vertexConsumer, matrix, normal, left, right, top, bottom, ey, color, light, combinedOverlay);
                    vertexConsumer = bufferSource.getBuffer(PositionColorRenderType.renderType_no_uv_light_normal);
                }
                // 贴图后再跳过
                if(i % (DottedStep + 1) != 0) continue;

                // 应用坐标变换
                float sx = (xScale < 0 ? width - start[0] * width : start[0] * width);
                float sy = start[1]; // y坐标直接使用不缩放
                float sz = (zScale < 0 ? height - start[2] * height : start[2] * height);

                // 计算线段方向（在x-z平面）
                float dx = ex - sx;
                float dz = ez - sz;
                float length = (float) Math.sqrt(dx * dx + dz * dz);

                if (length > 0) {
                    // 计算垂直方向（用于线宽，基于x-z平面）
                    float nx = -dz / length * lineWidth;
                    float nz = dx / length * lineWidth;

                    // 绘制线段（四边形带），使用三维坐标
                    // 顶点1：起点左侧
                    UIRender.addVertex_by_position_color_overlay(vertexConsumer, matrix,
                            sx - nx, sy, sz - nz, color, combinedOverlay);
                    // 顶点2：终点左侧
                    UIRender.addVertex_by_position_color_overlay(vertexConsumer, matrix,
                            ex - nx, ey, ez - nz, color, combinedOverlay);
                    // 顶点3：终点右侧
                    UIRender.addVertex_by_position_color_overlay(vertexConsumer, matrix,
                            ex + nx, ey, ez + nz, color, combinedOverlay);
                    // 顶点4：起点右侧
                    UIRender.addVertex_by_position_color_overlay(vertexConsumer, matrix,
                            sx + nx, sy, sz + nz, color, combinedOverlay);
                }
            }
        }
        poseStack.popPose();
    }
}