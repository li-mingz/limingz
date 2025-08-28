package com.limingz.mymod.gui.holographic_ui.renderer.rendertype;

import net.minecraft.client.renderer.RenderStateShard;

import java.util.OptionalDouble;

public class MyLineStateShard extends RenderStateShard {
    public MyLineStateShard(String p_110161_, Runnable p_110162_, Runnable p_110163_) {
        super(p_110161_, p_110162_, p_110163_);
    }
    public static LineStateShard getLineStateShard(double num){
        return new LineStateShard(OptionalDouble.of(num));
    }
}
