package com.limingz.mymod.gui.holographic_ui.util;

import com.limingz.mymod.util.PauseTick;
import net.minecraft.nbt.CompoundTag;

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
    public boolean show; // 是否渲染

    public void setCurrentFrameIndex(int currentFrameIndex) {
        this.currentFrameIndex = currentFrameIndex;
    }

    public AnimatedPngState() {
        this.playMode = PlayMode.LOOP; // 默认循环播放
        this.lastFrameTick = PauseTick.getTick();
        this.currentFrameIndex = 0;
        this.direction = 1;
        this.isPlaying = true;
        this.show = true;
    }




    /**
     * 设置渲染状态
     * @param showState 是否渲染
     */
    public void setShowState(boolean showState) {
        this.show = showState;
    }

    /**
     * 设置播放方向
     * @param direction 1为正向播放, -1为反向播放
     */
    public void setDirection(int direction) {
        this.direction = direction > 0 ? 1 : -1;
    }

    /**
     * 获取当前播放方向
     * @return 1为正向, -1为反向
     */
    public int getDirection() {
        return direction;
    }

    /**
     * 设置当前播放帧
     */
    public void setFrame(int frame) {
        currentFrameIndex = frame;
    }

    /**
     * 将当前播放帧设置为起始帧
     */
    public void playStartFrame() {
        currentFrameIndex = 0;
    }

    /**
     * 将当前播放帧设置为终止帧
     */
    public void playEndFrame() {
        // -1 代表让render处理
        currentFrameIndex = -1;
    }

    /**
     * 设置播放模式
     */
    public void setPlayMode(PlayMode playMode) {
        this.playMode = playMode;
    }

    /**
     * 重置动画（从当前方向的起始帧重新开始）
     */
    public void resetAnimation() {
        if (direction == 1) {
            currentFrameIndex = 0;
        } else {
            // -1 代表让render处理
            currentFrameIndex = -1;
        }
        lastFrameTick = PauseTick.getTick();
        isPlaying = true;
    }

    /**
     * 设置上一帧渲染的时间，用于跳帧
     */
    public void setLastFrameTick(double time) {
        lastFrameTick = time;
    }

    // 序列化
    public CompoundTag saveToTag() {
        CompoundTag tag = new CompoundTag();
        // 存储枚举（用名字字符串）
        tag.putString("playMode", playMode.name());
        // 存储基本类型
        tag.putDouble("lastFrameTick", lastFrameTick);
        tag.putInt("currentFrameIndex", currentFrameIndex);
        tag.putInt("direction", direction);
        tag.putBoolean("isPlaying", isPlaying);
        tag.putBoolean("show", show);
        return tag;
    }

    // 反序列化
    public void loadFromTag(CompoundTag tag) {
        // 读取枚举
        this.playMode = tag.contains("playMode")
                ? PlayMode.valueOf(tag.getString("playMode"))
                : PlayMode.LOOP;
        // 读取基本类型
        this.lastFrameTick = tag.getDouble("lastFrameTick");
        this.currentFrameIndex = tag.getInt("currentFrameIndex");
        this.direction = tag.getInt("direction");
        if (this.direction == 0) this.direction = 1; // 防止方向为0的无效值
        this.isPlaying = tag.getBoolean("isPlaying");
        this.show = tag.getBoolean("show");
    }
}