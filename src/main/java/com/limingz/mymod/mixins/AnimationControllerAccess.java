package com.limingz.mymod.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationController;

@Mixin(AnimationController.class)
public interface AnimationControllerAccess<T extends GeoAnimatable>{
    @Accessor(remap = false)
    double getTickOffset();
}
