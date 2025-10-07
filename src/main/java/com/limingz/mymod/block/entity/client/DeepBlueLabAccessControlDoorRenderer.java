package com.limingz.mymod.block.entity.client;

import com.limingz.mymod.block.entity.DeepBlueLabAccessControlDoorEntity;
import com.limingz.mymod.gui.holographic_ui.renderer.ui.system.AnimatedPng;
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
    private AnimatedPng animatedPng;
    public DeepBlueLabAccessControlDoorRenderer(BlockEntityRendererProvider.Context context) {
        super(new DeepBlueLabAccessControlDoorModel());
        animatedPng = new AnimatedPng("Png", 0, 0, 1.92f, 1.08f, "png/main/main_", 120, 30);
    }

    @Override
    public RenderType getRenderType(DeepBlueLabAccessControlDoorEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }

    @Override
    public void actuallyRender(PoseStack poseStack, DeepBlueLabAccessControlDoorEntity animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        poseStack.pushPose();
        // 前移0.375格,上移3格
        poseStack.translate(0, 3, 0.375);
        animatedPng.renderAll(bufferSource, poseStack, packedOverlay, animatable);
        poseStack.popPose();
    }
}
