package com.limingz.mymod.util;

import com.eliotlash.mclib.utils.Interpolations;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import org.joml.Vector3d;
import software.bernie.geckolib.core.animation.EasingType;

import java.util.Comparator;
import java.util.List;

/**
 * Geckolib插值工具类
 */
public class GeckolibInterpolationTool {
    public static class PositionKeyframe {
        private final double time;       // 关键帧时间
        private final Vector3d vector;   // 位置向量（x,y,z）
        private final EasingType easing; // 缓动类型

        /**
         * 构造关键帧
         * 默认线性缓动 EasingType.LINEAR
         */
        public PositionKeyframe(double time, Vector3d vector) {
            this(time, vector, EasingType.LINEAR);
        }

        /**
         * 构造关键帧
         * @param easing 缓动类型
         */
        public PositionKeyframe(double time, Vector3d vector, EasingType easing) {
            this.time = time;
            this.vector = vector;
            this.easing = easing;
        }

        private double getTime() { return time; }
        private Vector3d getVector() { return vector; }
        private EasingType getEasing() { return easing; }
    }


    /*
        插值计算
     */
    public static Vector3d interpolatePosition(List<PositionKeyframe> keyframes, double currentTime) {
        if (keyframes == null || keyframes.isEmpty()) {
            throw new IllegalArgumentException("关键帧列表不能为空");
        }

        // 关键帧按时间升序排序
        List<PositionKeyframe> sortedKeyframes = keyframes.stream()
                .sorted(Comparator.comparingDouble(PositionKeyframe::getTime))
                .toList();

        // 边界处理
        PositionKeyframe first = sortedKeyframes.get(0);
        PositionKeyframe last = sortedKeyframes.get(sortedKeyframes.size() - 1);
        if (currentTime <= first.getTime()) {
            return first.getVector();
        }
        if (currentTime >= last.getTime()) {
            return last.getVector();
        }

        // 查找当前时间对应的关键帧区间
        PositionKeyframe start = null;
        PositionKeyframe end = null;
        for (int i = 0; i < sortedKeyframes.size() - 1; i++) {
            start = sortedKeyframes.get(i);
            end = sortedKeyframes.get(i + 1);
            if (currentTime >= start.getTime() && currentTime <= end.getTime()) {
                break;
            }
        }

        // 计算线性进度
        double duration = end.getTime() - start.getTime();
        double progress = (currentTime - start.getTime()) / duration;
        // Catmull-Rom 插值
        // 曲线必过控制点
        Double2DoubleFunction easingTransformer = end.getEasing().buildTransformer(null);
        double easedProgress = easingTransformer.apply(progress); // 计算缓动进度

        // 插值
        return lerpVector3d(start.getVector(), end.getVector(), easedProgress);
    }

    /*
        Vector3d插值
     */
    private static Vector3d lerpVector3d(Vector3d start, Vector3d end, double progress) {
        double x = Interpolations.lerp(start.x, end.x, progress);
        double y = Interpolations.lerp(start.y, end.y, progress);
        double z = Interpolations.lerp(start.z, end.z, progress);
        return new Vector3d(x, y, z);
    }

    // 测试
    public static void main(String[] args) {
        List<PositionKeyframe> keyframes = List.of(
                new PositionKeyframe(0.0, new Vector3d(0, 0, 0)),
                new PositionKeyframe(15.0, new Vector3d(28, 0, 0), EasingType.EASE_IN_OUT_QUAD),
                new PositionKeyframe(30.0, new Vector3d(0, 0, 0), EasingType.EASE_IN_OUT_QUAD)
        );

        System.out.println(GeckolibInterpolationTool.interpolatePosition(keyframes, 0.0));
    }
}