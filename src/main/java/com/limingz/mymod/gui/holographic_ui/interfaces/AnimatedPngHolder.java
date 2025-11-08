package com.limingz.mymod.gui.holographic_ui.interfaces;

import com.limingz.mymod.gui.holographic_ui.renderer.ui.system.AnimatedPng;
import com.limingz.mymod.gui.holographic_ui.util.AnimatedPngState;
import com.limingz.mymod.gui.holographic_ui.util.PngState;

import java.util.Map;

public interface AnimatedPngHolder {
    // 获取指定组件在当前方块中的所有状态
    Map<String, AnimatedPngState> getAnimatedState();
    // 获取指定组件在当前方块中的所有状态
    Map<String, PngState> getPngState();

    /**
     * 执行单次播放结束后调用的回调函数
     */
    void executeOnPlayOnceFinishedCallback(String targetId, AnimatedPng animatedPng);
}
