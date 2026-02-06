package com.limingz.mymod.renderer.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * 螺旋链渲染助手类
 * 生成和缓存螺旋结构的几何体数据。
 * 懒加载，仅在首次使用时生成几何体。
 */
public class SpiralStrand {
    // 几何体缓存存储
    // 存储格式: [x1, y1, z1, x2, y2, z2, ...]
    private static float[] SOLID_STRAND_CACHE;  // 实线链缓存
    private static float[] DOTTED_STRAND_CACHE; // 虚线链缓存

    // 几何参数配置
    public static final float CACHE_LENGTH = 2.0f;          // 螺旋总长度 (Y轴)
    private static final int CACHE_SEGMENTS = 128;          // 纵向分段数 (决定螺旋长的细分)
    private static final float CACHE_RADIUS = 0.04f;        // 螺旋主体围绕中心轴旋转的半径
    private static final float CACHE_TWIST_DENSITY = 12.0f;  // 扭曲密度 (决定螺旋缠绕的圈数)
    private static final float CACHE_TUBE_RADIUS = 0.01f;   // 单根管子的粗细 (截面圆半径)
    private static final int CACHE_SIDES = 12;              // 管体截面精度 (圆柱侧面数)

    /**
     * 初始化缓存
     * 如果缓存为空，则计算并生成几何体数据。
     */
    public static void initCache() {
        if (SOLID_STRAND_CACHE != null) return;
        SOLID_STRAND_CACHE = generateStrandGeometry(false); // 生成实线几何
        DOTTED_STRAND_CACHE = generateStrandGeometry(true);  // 生成虚线几何
    }

    /**
     * 渲染实线螺旋
     * @param poseStack 变换矩阵栈
     * @param consumer 顶点消费者
     * @param r 红色分量 (0-255)
     * @param g 绿色分量 (0-255)
     * @param b 蓝色分量 (0-255)
     * @param a Alpha透明度 (0-255)
     */
    public static void renderSolid(PoseStack poseStack, VertexConsumer consumer, int r, int g, int b, int a) {
        initCache();
        drawCachedStrand(consumer, poseStack, SOLID_STRAND_CACHE, r, g, b, a);
    }

    /**
     * 渲染虚线螺旋
     * @param poseStack 变换矩阵栈
     * @param consumer 顶点消费者
     * @param r 红色分量 (0-255)
     * @param g 绿色分量 (0-255)
     * @param b 蓝色分量 (0-255)
     * @param a Alpha透明度 (0-255)
     */
    public static void renderDotted(PoseStack poseStack, VertexConsumer consumer, int r, int g, int b, int a) {
        initCache();
        drawCachedStrand(consumer, poseStack, DOTTED_STRAND_CACHE, r, g, b, a);
    }

    /**
     * 绘制缓存的几何体
     * 直接将预计算的顶点数据推送到渲染管线，应用当前的 Pose 变换。
     */
    private static void drawCachedStrand(VertexConsumer consumer, PoseStack poseStack, float[] cache, int r, int g, int b, int a) {
        Matrix4f pose = poseStack.last().pose();
        // 遍历缓存数组，每3个float构成一个顶点(x, y, z)
        for (int i = 0; i < cache.length; i += 3) {
            float x = cache[i];
            float y = cache[i+1];
            float z = cache[i+2];
            // 提交顶点，仅包含位置和颜色信息 (无光照和UV)
            consumer.vertex(pose, x, y, z)
                    .color(r, g, b, a)
                    .endVertex();
        }
    }

    /**
     * 生成螺旋几何体数据 (核心逻辑)
     * @param isDotted 是否为虚线模式 (虚线会有间断和额外的封口)
     * @return 包含所有顶点坐标的 float 数组
     */
    private static float[] generateStrandGeometry(boolean isDotted) {
        // 预估顶点数量，避免频繁扩容
        List<Float> vertices = new ArrayList<>(CACHE_SEGMENTS * CACHE_SIDES * 12);

        float dy = CACHE_LENGTH / CACHE_SEGMENTS; // 单个分段的高度
        int period = 5;   // 虚线周期 (每5段为一个循环)
        int drawLen = 2;  // 绘制长度 每个周期内绘制前2段, 即 线段:空 = 2:3

        for (int i = 0; i < CACHE_SEGMENTS; i++) {
            // 判断当前段是否需要绘制 (实线全部绘制，虚线按周期绘制)
            boolean shouldDraw = !isDotted || (i % period < drawLen);
            if (!shouldDraw) continue;

            float y1 = i * dy;
            float y2 = (i + 1) * dy;

            // 计算当前段上下底面的螺旋角度
            float angle1 = y1 * CACHE_TWIST_DENSITY;
            float angle2 = y2 * CACHE_TWIST_DENSITY;

            // 计算螺旋路径中心的 X/Z 坐标 (绕Y轴旋转)
            float cx1 = CACHE_RADIUS * (float)Math.cos(angle1);
            float cz1 = CACHE_RADIUS * (float)Math.sin(angle1);
            float cx2 = CACHE_RADIUS * (float)Math.cos(angle2);
            float cz2 = CACHE_RADIUS * (float)Math.sin(angle2);

            for (int k = 0; k < CACHE_SIDES; k++) {
                // 计算管子截面圆上的角度
                float t1 = (float)k / CACHE_SIDES * 2 * (float)Math.PI;
                float t2 = (float)(k + 1) / CACHE_SIDES * 2 * (float)Math.PI;

                // 计算管体表面的局部偏移
                float c1 = (float)Math.cos(t1) * CACHE_TUBE_RADIUS;
                float s1 = (float)Math.sin(t1) * CACHE_TUBE_RADIUS;
                float c2 = (float)Math.cos(t2) * CACHE_TUBE_RADIUS;
                float s2 = (float)Math.sin(t2) * CACHE_TUBE_RADIUS;

                // 计算侧面四边形的4个顶点坐标 (绝对坐标 = 螺旋中心 + 管体偏移)
                float px1 = cx1 + c1; float pz1 = cz1 + s1; // 下环点1
                float px2 = cx1 + c2; float pz2 = cz1 + s2; // 下环点2
                float px3 = cx2 + c2; float pz3 = cz2 + s2; // 上环点2
                float px4 = cx2 + c1; float pz4 = cz2 + s1; // 上环点1

                // 添加侧面四边形 (顺序: 4->3->2->1 逆时针，确保法线朝外)
                addToList(vertices, px4, y2, pz4);
                addToList(vertices, px3, y2, pz3);
                addToList(vertices, px2, y1, pz2);
                addToList(vertices, px1, y1, pz1);
            }

            // 处理封口逻辑 (Caps)
            // 只有虚线在断开处需要封口，或者实线的首尾需要封口
            boolean needsBottomCap = false;
            boolean needsTopCap = false;
            if (isDotted) {
                // 虚线段开始处：底部封口
                if (i == 0 || (i % period == 0)) needsBottomCap = true;
                // 虚线段结束处：顶部封口
                if (i == CACHE_SEGMENTS - 1 || (i % period == drawLen - 1)) needsTopCap = true;
            } else {
                // 实线仅在最底端和最顶端封口
                if (i == 0) needsBottomCap = true;
                if (i == CACHE_SEGMENTS - 1) needsTopCap = true;
            }

            if (needsBottomCap) addCapToList(vertices, cx1, y1, cz1, true);
            if (needsTopCap) addCapToList(vertices, cx2, y2, cz2, false);
        }

        // 将 List 转为原生 float 数组
        float[] arr = new float[vertices.size()];
        for(int i=0; i<vertices.size(); i++) arr[i] = vertices.get(i);
        return arr;
    }

    /**
     * 添加圆柱封口几何体
     * @param cx 封口中心X
     * @param y 封口Y高度
     * @param cz 封口中心Z
     * @param isBottom 是底部封口还是顶部封口 (决定法线朝向)
     */
    private static void addCapToList(List<Float> list, float cx, float y, float cz, boolean isBottom) {
        for (int k = 0; k < CACHE_SIDES; k++) {
            float t1 = (float)k / CACHE_SIDES * 2 * (float)Math.PI;
            float t2 = (float)(k + 1) / CACHE_SIDES * 2 * (float)Math.PI;

            // 计算圆周上的点
            float px1 = cx + (float)Math.cos(t1) * CACHE_TUBE_RADIUS;
            float pz1 = cz + (float)Math.sin(t1) * CACHE_TUBE_RADIUS;
            float px2 = cx + (float)Math.cos(t2) * CACHE_TUBE_RADIUS;
            float pz2 = cz + (float)Math.sin(t2) * CACHE_TUBE_RADIUS;

            if (isBottom) {
                // 底部封口 (朝向外部/下方): Center -> P1 -> P2
                addToList(list, cx, y, cz);
                addToList(list, px1, y, pz1);
                addToList(list, px2, y, pz2);
                addToList(list, px2, y, pz2); // 作为一个退化四边形提交
            } else {
                // 顶部封口 (朝向外部/上方): Center -> P2 -> P1
                addToList(list, cx, y, cz);
                addToList(list, px2, y, pz2);
                addToList(list, px1, y, pz1);
                addToList(list, px1, y, pz1); // 作为一个退化四边形提交
            }
        }
    }

    private static void addToList(List<Float> list, float x, float y, float z) {
        list.add(x); list.add(y); list.add(z);
    }
}
