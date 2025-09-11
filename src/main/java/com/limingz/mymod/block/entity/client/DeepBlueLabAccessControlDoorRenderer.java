package com.limingz.mymod.block.entity.client;

import com.limingz.mymod.block.entity.DeepBlueLabAccessControlDoorEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class DeepBlueLabAccessControlDoorRenderer extends GeoBlockRenderer<DeepBlueLabAccessControlDoorEntity> {

    public DeepBlueLabAccessControlDoorRenderer(BlockEntityRendererProvider.Context context) {
        super(new DeepBlueLabAccessControlDoorModel());
    }
    @Override
    public RenderType getRenderType(DeepBlueLabAccessControlDoorEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }
}
