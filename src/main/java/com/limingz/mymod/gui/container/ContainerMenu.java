package com.limingz.mymod.gui.container;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public abstract class ContainerMenu extends AbstractContainerMenu {
    private final BlockPos pos;
    private final Block targetBlock;
    protected ContainerMenu(@Nullable MenuType<?> pMenuType, int pContainerId, BlockPos pos, Block targetBlock) {
        super(pMenuType, pContainerId);
        this.pos = pos;
        this.targetBlock = targetBlock;
    }
    public int addSlotLine(Container container, int index, int x, int y, int amount, int dx){
        for(int i = 0; i < amount; i++){
            addSlot(new Slot(container, index++, x, y));
            x += dx;
        }
        return index;
    }

    public int addSlotBox(Container container, int index, int x, int y, int xAmount, int dx, int yAmount, int dy){
        for(int j = 0; j < yAmount; j++){
            index = addSlotLine(container, index, x, y, xAmount, dx);
            y += dy;
        }
        return index;
    }

    public void addPlayerInventory(Container container, int x, int y){
        addSlotBox(container, 9, x, y, 9, 18, 3, 18);
        y += 58;
        addSlotLine(container, 0, x, y, 9, 18);
    }
    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(ContainerLevelAccess.create(pPlayer.level(), pos), pPlayer, targetBlock);
    }
}
