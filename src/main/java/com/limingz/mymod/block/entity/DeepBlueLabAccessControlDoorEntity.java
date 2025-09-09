package com.limingz.mymod.block.entity;

import com.limingz.mymod.register.BlockEntityRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class DeepBlueLabAccessControlDoorEntity extends BlockEntity {
    public DeepBlueLabAccessControlDoorEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegister.deep_blue_lab_access_control_door_entity.get(), pos, state);
    }
}
