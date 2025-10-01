package com.limingz.mymod.block.entity.client;

import com.limingz.mymod.block.entity.DeepBlueLabAccessControlDoorEntity;
import com.limingz.mymod.gui.holographic_ui.config.UIConfig;
import com.limingz.mymod.gui.holographic_ui.renderer.ui.other.DemoScreen;
import com.limingz.mymod.gui.holographic_ui.renderer.ui.system.Svg;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class DeepBlueLabAccessControlDoorRenderer extends GeoBlockRenderer<DeepBlueLabAccessControlDoorEntity> {
    private Svg svg;
    public DeepBlueLabAccessControlDoorRenderer(BlockEntityRendererProvider.Context context) {
        super(new DeepBlueLabAccessControlDoorModel());
        svg = new Svg("Svg", 0, 0, 1920, 1080);
    }

    @Override
    public RenderType getRenderType(DeepBlueLabAccessControlDoorEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }

    @Override
    public void actuallyRender(PoseStack poseStack, DeepBlueLabAccessControlDoorEntity animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        svg.renderAll(bufferSource, poseStack, packedOverlay, animatable);;
    }
}
