package com.limingz.mymod.block.entity;

import com.limingz.mymod.Main;
import com.limingz.mymod.register.BlockEntityRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class DemoBlockEntity extends BlockEntity {

    @Override
    public AABB getRenderBoundingBox() {
        BlockPos pos = this.getBlockPos();
        return new AABB(
                pos.getX() - 1, pos.getY(), pos.getZ() - 1,
                pos.getX() + 2, pos.getY() + 2, pos.getZ() + 2
        );
    }

    public DemoBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegister.demo_block_entity.get(), pos, state);
    }

    public void serverTick() {
        // 服务端逻辑
    }

    public void clientTick() {
        // 客户端逻辑
    }
}