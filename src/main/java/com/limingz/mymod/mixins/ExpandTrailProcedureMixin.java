package com.limingz.mymod.mixins;

import com.limingz.mymod.util.SQLiteTempData;
import net.mcreator.caerulaarbor.procedures.ExpandTrailProcedure;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExpandTrailProcedure.class)
public class ExpandTrailProcedureMixin {

    // 当ExpandTrailProcedure调用destroyBlock前注入
    @Inject(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/LevelAccessor;destroyBlock(Lnet/minecraft/core/BlockPos;Z)Z"
            )
    )
    private static void execute(LevelAccessor world, double x, double y, double z, BlockState blockstate, CallbackInfo ci) {
        // 服务端执行
        if(!world.isClientSide()) {
            SQLiteTempData.sqliteDeleteQueue.add((int)x);
            SQLiteTempData.sqliteDeleteQueue.add((int)y);
            SQLiteTempData.sqliteDeleteQueue.add((int)z);
            SQLiteTempData.sqliteDeleteQueue.add(blockstate.getBlock().getDescriptionId());
        }
    }
}
