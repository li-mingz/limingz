package com.limingz.mymod.gui.container;

import com.limingz.mymod.block.entity.DeskBlockEntity;
import com.limingz.mymod.register.BlockRegister;
import com.limingz.mymod.register.MenuRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.items.SlotItemHandler;

public class DeskBlockContainerMenu extends ContainerMenuNormalSlot {
    public DeskBlockContainerMenu(Inventory inventory, int pContainerId, BlockPos pos) {

        super(MenuRegister.desk_block_container_menu.get(), pContainerId, pos, BlockRegister.desk_block.get(), 1, 0, 1);
        if (inventory.player.level().getBlockEntity(pos) instanceof DeskBlockEntity deskBlockEntity) {
            addSlot(new SlotItemHandler(deskBlockEntity.getItems(), 0, 80, 17));
        }
        addPlayerInventory(inventory, 8, 51);
    }
}
