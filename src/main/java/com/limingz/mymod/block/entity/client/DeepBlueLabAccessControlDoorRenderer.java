package com.limingz.mymod.block.entity.client;

import com.limingz.mymod.block.entity.DeepBlueLabAccessControlDoorEntity;
import com.limingz.mymod.gui.holographic_ui.renderer.ui.system.AnimatedPng;
import com.limingz.mymod.gui.holographic_ui.renderer.ui.system.PNG;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class DeepBlueLabAccessControlDoorRenderer extends GeoBlockRenderer<DeepBlueLabAccessControlDoorEntity> {
    private AnimatedPng aside_closeAnimatedPng;
    private AnimatedPng aside_openAnimatedPng;
    private AnimatedPng aside_ro_openAnimatedPng;
    private AnimatedPng iconAnimatedPng;
    private AnimatedPng iconCloseAnimatedPng;
    private AnimatedPng centerAnimatedPng;
    private PNG png1;
    private PNG png2;
    private PNG asidePng;
    public DeepBlueLabAccessControlDoorRenderer(BlockEntityRendererProvider.Context context) {
        super(new DeepBlueLabAccessControlDoorModel());
        float width = 1.92f*3.75f;
        float height = 1.08f*3.75f;
        png1 = new PNG("otherPng", 0, 0, width, height, "png/deep_blue_lab_access_control_door_ui/other/other_00000.png");
        png2 = new PNG("otherPng2", 0, 0, width, height, "png/deep_blue_lab_access_control_door_ui/other2/other2_00000.png");
        asidePng = new PNG("asidePng", 0, 0, width, height, "png/deep_blue_lab_access_control_door_ui/aside/aside_00000.png");
        aside_closeAnimatedPng = new AnimatedPng("aside_closeAnimatedPng", 0, 0, width, height, "png/deep_blue_lab_access_control_door_ui/aside_close", 30);
        aside_openAnimatedPng = new AnimatedPng("aside_openAnimatedPng", 0, 0, width, height, "png/deep_blue_lab_access_control_door_ui/aside_open", 30);
        aside_ro_openAnimatedPng = new AnimatedPng("aside_ro_openAnimatedPng", 0, 0, width, height, "png/deep_blue_lab_access_control_door_ui/aside_ro_open", 30);
        iconAnimatedPng = new AnimatedPng("iconAnimatedPng", 0, 0, width, height, "png/deep_blue_lab_access_control_door_ui/icon", 30);
        iconCloseAnimatedPng = new AnimatedPng("iconCloseAnimatedPng", 0, 0, width, height, "png/deep_blue_lab_access_control_door_ui/icon_close", 30);
        centerAnimatedPng = new AnimatedPng("centerAnimatedPng", 0, 0, width, height, "png/deep_blue_lab_access_control_door_ui/center", 30);
    }

    @Override
    public RenderType getRenderType(DeepBlueLabAccessControlDoorEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }

    @Override
    public void actuallyRender(PoseStack poseStack, DeepBlueLabAccessControlDoorEntity animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        poseStack.pushPose();
        // 上移3格
        poseStack.translate(0, 3, 0);
        // 前移0.325格
        poseStack.translate(0, 0, 0.325);
        png1.renderAll(bufferSource, poseStack, packedOverlay, animatable);
        // 前移0.1格
        poseStack.translate(0, 0, 0.1);
        asidePng.renderAll(bufferSource, poseStack, packedOverlay, animatable);
        aside_closeAnimatedPng.renderAll(bufferSource, poseStack, packedOverlay, animatable);
        aside_openAnimatedPng.renderAll(bufferSource, poseStack, packedOverlay, animatable);
        aside_ro_openAnimatedPng.renderAll(bufferSource, poseStack, packedOverlay, animatable);
        iconAnimatedPng.renderAll(bufferSource, poseStack, packedOverlay, animatable);
        iconCloseAnimatedPng.renderAll(bufferSource, poseStack, packedOverlay, animatable);
        png2.renderAll(bufferSource, poseStack, packedOverlay, animatable);
        // 前移0.05格
        poseStack.translate(0, 0, 0.05);
        png2.renderAll(bufferSource, poseStack, packedOverlay, animatable);
        // 前移0.05格
        poseStack.translate(0, 0, 0.05);
        png2.renderAll(bufferSource, poseStack, packedOverlay, animatable);
        // 前移0.1格
        poseStack.translate(0, 0, 0.1);
        centerAnimatedPng.renderAll(bufferSource, poseStack, packedOverlay, animatable);
        poseStack.popPose();
    }
}
