package com.limingz.mymod.mixins;

import net.mcreator.caerulaarbor.block.SeaTrailGrownBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SeaTrailGrownBlock.class)
public abstract class SeaTrailGrownBlockMixin extends Block implements SimpleWaterloggedBlock, BonemealableBlock{
    public SeaTrailGrownBlockMixin(Properties pProperties) {
        super(pProperties);
    }
//    @Inject(method = "onPlace", at = @At("HEAD"))
//    public void onPlace(BlockState blockstate, Level world, BlockPos pos, BlockState oldState, boolean moving, CallbackInfo ci) {
//        System.out.println("放置溟痕");
//    }
}
