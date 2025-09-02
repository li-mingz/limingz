package com.limingz.mymod.mixins;

import com.limingz.mymod.util.sqlite.SQLiteTempData;
import net.mcreator.caerulaarbor.block.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockBehaviour.class)
public abstract class BlockBehaviourMixin implements FeatureElement {

    @Inject(method = "onPlace", at = @At("HEAD"))
    public void onPlace(BlockState blockstate, Level world, BlockPos pos, BlockState oldState, boolean moving, CallbackInfo ci) {
        // 检查当前实例是否为Block类型且是目标子类
        if ((Object)this instanceof Block block) {
            if(block instanceof SeaTrailGrownBlock||
                    block instanceof SeaTrailInitBlock||
                    block instanceof SeaTrailGrowingBlock||
                    block instanceof SeaTrailStopBlock||
                    block instanceof SeaTrailSolidBlock||
                    block instanceof TrailLogBlock||
                    block instanceof TrailLeaveBlock||
                    block instanceof TrailMushroomBlock) {
                SQLiteTempData.executeAdd(world, pos, block);
            }
        }
    }
    @Inject(method = "onRemove", at = @At("HEAD"))
    public void onRemove(BlockState pState, Level world, BlockPos pos, BlockState pNewState, boolean pMovedByPiston, CallbackInfo ci) {
        // 检查当前实例是否为Block类型且是目标子类
        if ((Object)this instanceof Block block) {
            if(block instanceof SeaTrailGrownBlock||
                    block instanceof SeaTrailInitBlock||
                    block instanceof SeaTrailGrowingBlock||
                    block instanceof SeaTrailStopBlock||
                    block instanceof SeaTrailSolidBlock||
                    block instanceof TrailLogBlock||
                    block instanceof TrailLeaveBlock||
                    block instanceof TrailMushroomBlock) {
                SQLiteTempData.executeDelete(world, pos, block);
            }
        }
    }
}
