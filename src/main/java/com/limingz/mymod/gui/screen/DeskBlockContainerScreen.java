package com.limingz.mymod.gui.screen;

import com.limingz.mymod.gui.container.DeskBlockContainerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static com.limingz.mymod.Main.MODID;

public class DeskBlockContainerScreen extends AbstractContainerScreen<DeskBlockContainerMenu> {
    public static final ResourceLocation GUI = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/container/desk_block_gui.png");
    public DeskBlockContainerScreen(DeskBlockContainerMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.inventoryLabelY = this.imageHeight - 110;
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        pGuiGraphics.blit(GUI, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }
}
