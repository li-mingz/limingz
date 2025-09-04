package com.limingz.mymod.gui.holographic_ui.renderer.blockentity;

import com.limingz.mymod.block.entity.DemoBlockEntity;
import com.limingz.mymod.gui.holographic_ui.config.UIConfig;
import com.limingz.mymod.gui.holographic_ui.renderer.ui.other.DemoScreen;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class DemoBlockEntityRenderer implements BlockEntityRenderer<DemoBlockEntity> {
    public DemoScreen demoScreen;
    public DemoBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        demoScreen = new DemoScreen("主屏幕", 0, 0, UIConfig.BG_X, UIConfig.BG_Y);
    }

    @Override
    public void render(DemoBlockEntity blockEntity, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource,
                       int combinedLight, int combinedOverlay) {
        demoScreen.renderAll(bufferSource, poseStack, combinedOverlay, blockEntity);
    }

}