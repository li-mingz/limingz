package com.limingz.mymod.item;

import com.limingz.mymod.register.BlockRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class TicketItem extends Item {
    public TicketItem(Properties p_41383_) {
        super(p_41383_);
    }

    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        BlockState blockstate = level.getBlockState(blockpos);
        Block block = blockstate.getBlock();
        if (block == BlockRegister.myblock.get()) {
            Player player = context.getPlayer();
            player.addItem(new ItemStack(Items.COOKED_COD));
            var itemstack = context.getItemInHand();
            itemstack.shrink(1);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return super.useOn(context);
    }
}
