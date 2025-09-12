package com.limingz.mymod.mixins_access;

public interface AnimationControllerAccess {
    default void setAnimationTick(Double targetTick){}
    default double getTickOffset() {
        return 0;
    }
}
