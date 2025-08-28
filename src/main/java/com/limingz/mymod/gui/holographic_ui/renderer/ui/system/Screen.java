package com.limingz.mymod.gui.holographic_ui.renderer.ui.system;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.entity.BlockEntity;

public class Screen extends UIComponent{
    private float[] translate = {0,0,0};
    private float x_rotationDegrees = 0;
    private float y_rotationDegrees = 0;
    private float z_rotationDegrees = 0;
    public Screen(String id, float x, float y, float width, float height) {
        super(id, x, y, width, height);
    }

    @Override
    public void renderAll(MultiBufferSource bufferSource, PoseStack poseStack, int combinedOverlay, BlockEntity blockEntity){
        if(!visible) return;
        poseStack.pushPose();
        poseStack.translate(translate[0], translate[1], translate[2]);
        poseStack.mulPose(Axis.XP.rotationDegrees(x_rotationDegrees));
        poseStack.mulPose(Axis.YP.rotationDegrees(y_rotationDegrees));
        poseStack.mulPose(Axis.ZP.rotationDegrees(z_rotationDegrees));
        for (int i = 0; i <= children.size() - 1; i++) {
            poseStack.translate(0, 0.001f, 0);  // z序
            children.get(i).renderAll(bufferSource, poseStack, combinedOverlay, x, y, blockEntity);
        }
        poseStack.popPose();
    }

    @Override
    public void renderAll(MultiBufferSource bufferSource, PoseStack poseStack, int combinedOverlay, float x, float y, BlockEntity blockEntity){
        if(!visible) return;
        poseStack.pushPose();
        poseStack.translate(translate[0], translate[1], translate[2]);
        poseStack.mulPose(Axis.XP.rotationDegrees(x_rotationDegrees));
        for (int i = 0; i <= children.size() - 1; i++) {
            poseStack.translate(0, 0.001f, 0);  // z序
            children.get(i).renderAll(bufferSource, poseStack, combinedOverlay, x, y, blockEntity);
        }
        poseStack.popPose();
    }


    public float[] getTranslate() {
        return translate;
    }

    public void setTranslate(float x,float y, float z) {
        this.translate = new float[]{x, y, z};
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

