package com.limingz.mymod.gui.menuprovider;

import com.limingz.mymod.gui.container.DeskBlockContainerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

public class DeskBlockContainerMenuProvider implements MenuProvider {
    private final BlockPos blockPos;
    public DeskBlockContainerMenuProvider(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.desk_block");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new DeskBlockContainerMenu(pPlayerInventory, pContainerId, blockPos);
    }
}
