package com.limingz.mymod.gui.holographic_ui.util;

import com.limingz.mymod.util.PauseTick;

// 单个AnimatedPng组件的独立状态
public class AnimatedPngState {
    // 播放模式
    public enum PlayMode {
        LOOP,       // 循环播放
        PLAY_ONCE   // 单次播放（结束后停在最后一帧）
    }
    public PlayMode playMode; // 播放模式
    public double lastFrameTick; // 上一帧时间
    public int currentFrameIndex; // 当前帧索引
    public int direction; // 1: 正向播放, -1: 反向播放
    public boolean isPlaying; // 是否播放

    public AnimatedPngState() {
        this.playMode = PlayMode.LOOP; // 默认循环播放
        this.lastFrameTick = PauseTick.getTick();
        this.currentFrameIndex = 0;
        this.direction = 1;
        this.isPlaying = true;
    }

    public AnimatedPngState setPlayMode(PlayMode playMode) {
        this.playMode = playMode;
        return this;
    }
}