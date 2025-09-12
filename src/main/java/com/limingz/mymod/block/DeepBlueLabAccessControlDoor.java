package com.limingz.mymod.block;

import com.limingz.mymod.block.entity.DeepBlueLabAccessControlDoorEntity;
import com.limingz.mymod.network.Channel;
import com.limingz.mymod.network.packet.playertoserver.DoorTickPacket;
import com.limingz.mymod.register.BlockEntityRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class DeepBlueLabAccessControlDoor extends BaseEntityBlock {

    public DeepBlueLabAccessControlDoor(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return BlockEntityRegister.deep_blue_lab_access_control_door_entity.get().create(pPos, pState);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {

        if (level.getBlockEntity(pos) instanceof DeepBlueLabAccessControlDoorEntity doorEntity) {
            if(!level.isClientSide){
                // 切换门的状态
                doorEntity.toggleDoor();
            } else {
                Channel.INSTANCE.sendToServer(new DoorTickPacket(pos, doorEntity.getAnimationLength()-doorEntity.getAnimationTick()));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
