package com.limingz.mymod.gui.holographic_ui.util;

import com.limingz.mymod.gui.holographic_ui.renderer.ui.system.AnimatedPng;
import com.limingz.mymod.util.PauseTick;
import net.minecraft.nbt.CompoundTag;

// 单个AnimatedPng组件的独立状态
public class PngState {
    public boolean show; // 是否渲染

    public PngState() {
        this.show = true;
    }
    /**
     * 设置渲染状态
     * @param showState 是否渲染
     */
    public void setShowState(boolean showState) {
        this.show = showState;
    }
}