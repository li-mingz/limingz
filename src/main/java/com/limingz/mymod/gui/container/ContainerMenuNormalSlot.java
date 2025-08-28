package com.limingz.mymod.gui.container;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public abstract class ContainerMenuNormalSlot extends ContainerMenu{
    private final int slotCount, slotNormal, slotNormalCont;
    public static final int PLAYER_INVENTORY_COUNT = 27;
    public static final int PLAYER_HOTBAR = 9;
    protected ContainerMenuNormalSlot(@Nullable MenuType<?> pMenuType, int pContainerId, BlockPos pos, Block targetBlock, int slotCount, int slotNormal, int slotNormalCont) {
        super(pMenuType, pContainerId, pos, targetBlock);
        this.slotCount = slotCount;
        this.slotNormal = slotNormal;
        this.slotNormalCont = slotNormalCont;
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        var itemstack = ItemStack.EMPTY;
        var slot = this.slots.get(pIndex);
        if (slot.hasItem()){
            var stack = slot.getItem();
            itemstack = stack.copy();
            if (pIndex < slotCount && !this.moveItemStackTo(stack, slotCount, this.slots.size(), true)) return ItemStack.EMPTY;
            else if(!this.moveItemStackTo(stack, slotNormal, slotNormalCont, true)) return ItemStack.EMPTY;
            if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
            else slot.setChanged();
            if (stack.getCount() == itemstack.getCount()) return ItemStack.EMPTY;
            slot.onTake(pPlayer, stack);
        }
        return itemstack;
    }
}
