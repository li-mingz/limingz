package com.limingz.mymod.gui.holographic_ui.interfaces;

import com.limingz.mymod.gui.holographic_ui.util.AnimatedPngState;

import java.util.Map;

public interface AnimatedPngHolder {
    // 获取指定组件在当前方块中的所有状态
    Map<String, AnimatedPngState> getAnimatedState();
}
