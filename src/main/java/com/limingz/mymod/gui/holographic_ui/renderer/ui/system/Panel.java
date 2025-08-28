package com.limingz.mymod.gui.holographic_ui.renderer.ui.system;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

public class Panel extends UIComponent{
    protected ResourceLocation backgroundTexture = ResourceLocation.fromNamespaceAndPath("minecraft", "default/0");

    public Panel(String id, float x, float y, float width, float height) {
        super(id, x, y, width, height);
    }


    public void setBackgroundTexture(ResourceLocation backgroundTexture) {
        this.backgroundTexture = backgroundTexture;
    }

    @Override
    public void render(MultiBufferSource bufferSource, PoseStack poseStack, int combinedOverlay, float x, float y, BlockEntity blockEntity) {
        super.render(bufferSource, poseStack, combinedOverlay, x, y, blockEntity);
//        UIRender.renderBackground(bufferSource, poseStack,  this.x + x, this.y + y, width, height, light, combinedOverlay, backgroundTexture);
    }
}
