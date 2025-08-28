package com.limingz.mymod.renderer.blockentity;

import com.limingz.mymod.block.entity.DeskBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

public class DeskBlockEntityRenderer implements BlockEntityRenderer<DeskBlockEntity> {
    public DeskBlockEntityRenderer(BlockEntityRendererProvider.Context pContext) {
    }

    @Override
    public void render(DeskBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        pBlockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
            var itemstack = iItemHandler.getStackInSlot(0);
            if (!itemstack.isEmpty()) {
                var item_renderer = Minecraft.getInstance().getItemRenderer();
                pPoseStack.pushPose();
                pPoseStack.scale(0.5f, 0.5f, 0.5f);
                pPoseStack.translate(1, 3, 1);
                item_renderer.renderStatic(itemstack, ItemDisplayContext.FIXED, LightTexture.FULL_BRIGHT, pPackedOverlay, pPoseStack, pBuffer, Minecraft.getInstance().level, 0);
                pPoseStack.popPose();
            }
        });
    }
}
