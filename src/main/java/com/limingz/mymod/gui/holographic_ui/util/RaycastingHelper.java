package com.limingz.mymod.gui.holographic_ui.util;

import com.limingz.mymod.gui.holographic_ui.config.UIConfig;
import com.limingz.mymod.gui.holographic_ui.renderer.ui.system.UIComponent;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class RaycastingHelper {
    public static Vec3 calculateIntersection(Vec3 rayOrigin, Vec3 rayDirection,
                                             Vec3 planePoint, Vec3 planeNormal,
                                             double maxDistance) {
        double denominator = planeNormal.dot(rayDirection);
        if (Math.abs(denominator) < 1e-6) return null;

        Vec3 p0MinusO = planePoint.subtract(rayOrigin);
        double t = planeNormal.dot(p0MinusO) / denominator;
        if (t < 0 || t > maxDistance) return null;

        return rayOrigin.add(rayDirection.scale(t));
    }

    public static float[] isPointInQuad(Vec3 point, Vec3 quadCenter,
                                        Vector3f normal, UIComponent uiComponent) {
        Vec3 offset = point.subtract(quadCenter);

        Vector3f axisX = new Vector3f(1, 0, 0);
        Vector3f axisZ = new Vector3f(0, 0, 1);
        axisX.rotateX((float) Math.toRadians(UIConfig.BG_ROTATION));
        axisZ.rotateX((float) Math.toRadians(UIConfig.BG_ROTATION));

        Vector3f axisY = new Vector3f(normal).cross(axisX).normalize();
        axisX = new Vector3f(axisY).cross(normal).normalize();

        float u = (float) offset.dot(new Vec3(axisX.x(), axisX.y(), axisX.z()));
        float v = -(float) offset.dot(new Vec3(axisY.x(), axisY.y(), axisY.z()));

        if(uiComponent.handleClick(u, v)) return new float[]{u, v};

        return null;
    }
}