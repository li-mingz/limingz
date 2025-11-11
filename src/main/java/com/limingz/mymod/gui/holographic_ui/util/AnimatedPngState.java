package com.limingz.mymod.gui.holographic_ui.util;

import com.limingz.mymod.gui.holographic_ui.renderer.ui.system.AnimatedPng;
import com.limingz.mymod.util.PauseTick;

import java.util.LinkedList;

// 单个AnimatedPng组件的独立状态
public class AnimatedPngState {
    // 定义单次播放结束回调接口
    @FunctionalInterface
    public interface OnPlayOnceFinished {
        void onFinished(AnimatedPng animatedPng);
    }
    // 按顺序调用的回调函数列表
    public LinkedList<String> onPlayOnceFinishedExecuteList;

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
        onPlayOnceFinishedExecuteList = new LinkedList<>();
    }

    /**
     * 清空单次播放结束后调用的回调函数
     */
    public void clearOnPlayOnceFinishedExecuteList() {
        onPlayOnceFinishedExecuteList.clear();
    }
    /**
     * 添加单次播放结束后调用的回调函数
     */
    public void addOnPlayOnceFinishedExecuteName(String id) {
        onPlayOnceFinishedExecuteList.add(id);
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
        // 切换到循环动画则取消暂停
        if(playMode == PlayMode.LOOP){
            isPlaying = true;
        }
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


}