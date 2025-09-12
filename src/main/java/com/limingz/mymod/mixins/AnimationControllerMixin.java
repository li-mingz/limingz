package com.limingz.mymod.mixins;

import com.limingz.mymod.mixins_access.AnimationControllerAccess;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationController;

@Mixin(AnimationController.class)
public abstract class AnimationControllerMixin<T extends GeoAnimatable> implements AnimationControllerAccess {
    @Shadow(remap = false) protected double tickOffset;
    // 是否需要跳转
    @Unique
    private Boolean needJumpTick = false;
    @Unique
    private double targetJumpTick = 0;
    @Inject(method = "adjustTick", at = {
            @At("HEAD"),
            @At(value = "FIELD", target = "Lsoftware/bernie/geckolib/core/animation/AnimationController;shouldResetTick:Z",
            opcode = Opcodes.PUTFIELD, // 赋值操作码
            ordinal = 0)}, // 第几次出现
            remap = false)
    protected void adjustTick(double tick, CallbackInfoReturnable<Double> cir) {
        if(needJumpTick){
            this.tickOffset = tick - targetJumpTick;
            needJumpTick = false;
//            // 如果不在Running或非重置状态下就重复执行
//            if(getAnimationState() == AnimationController.State.RUNNING && !shouldResetTick) needJumpTick = false;
        }
    }
    public double getTickOffset(){
        return tickOffset;
    };
    public void setAnimationTick(Double targetTick){
        needJumpTick = true;
        targetJumpTick = targetTick;
    }
}
