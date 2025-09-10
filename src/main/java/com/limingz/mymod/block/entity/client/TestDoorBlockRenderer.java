package com.limingz.mymod.block.entity.client;

import com.limingz.mymod.block.entity.TestDoorBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class TestDoorBlockRenderer extends GeoBlockRenderer<TestDoorBlockEntity> {
    public TestDoorBlockRenderer(BlockEntityRendererProvider.Context context) {
        super(new TestDoorBlockModel());
    }

    @Override
    public RenderType getRenderType(TestDoorBlockEntity animatable, ResourceLocation texture,
                                    @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }
}
