package com.limingz.mymod.gui.holographic_ui.renderer.ui.system;

import com.limingz.mymod.Main;
import com.limingz.mymod.gui.holographic_ui.interfaces.AnimatedPngHolder;
import com.limingz.mymod.gui.holographic_ui.util.AnimatedPngState;
import com.limingz.mymod.util.PauseTick;
import com.limingz.mymod.util.pacture.PNGTextureManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.*;

public class AnimatedPng extends UIComponent {
    private final String folderPath;
    private final int fps;
    private final double frameInterval;
    private List<ResourceLocation> frameLocations = new ArrayList<>();
//    private double lastFrameTick;
//    private int currentFrameIndex = 0;
//    private int direction = 1; // 1: 正向播放, -1: 反向播放
//    private boolean isPlaying = true; // 动画是否正在播放

    public AnimatedPng(String id, float x, float y, float width, float height, String folderPath, int fps) {
        super(id, x, y, width, height);
        this.id = id;
        this.folderPath = folderPath;
        this.fps = fps;
        this.frameInterval = 20.0 / fps;
        loadFrames();
    }

    /**
     * 加载文件夹中的所有帧并按文件名升序排序
     */
    private void loadFrames() {
        frameLocations.clear();
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();

        // 遍历指定文件夹下的所有PNG文件
        resourceManager.listResources(folderPath, location ->
                location.getPath().toLowerCase().endsWith(".png")
        ).forEach((resourceLocation, resource) -> {
            frameLocations.add(resourceLocation);
        });

        // 按文件名升序排序
        Collections.sort(frameLocations, Comparator.comparing(
                loc -> loc.getPath().substring(loc.getPath().lastIndexOf("/") + 1)
        ));

        Main.LOGGER.info("加载 {} 个帧 从 {}", frameLocations.size(), folderPath);
    }

    @Override
    public void render(MultiBufferSource bufferSource, PoseStack poseStack, int combinedOverlay, float x, float y, BlockEntity blockEntity) {
        super.render(bufferSource, poseStack, combinedOverlay, x, y, blockEntity);

        AnimatedPngHolder animatedPngHolderBlockEntity = (AnimatedPngHolder) blockEntity;

        // 没有帧资源则不渲染
        if (frameLocations.isEmpty()) {
            return;
        }
        Map<String, AnimatedPngState> animatedPngStateMap = animatedPngHolderBlockEntity.getAnimatedState();
        AnimatedPngState animatedPngState = animatedPngStateMap.get(this.id);
        double currentTick = PauseTick.getTick();
        double timeElapsed = currentTick - animatedPngState.lastFrameTick;
        if(animatedPngState.currentFrameIndex == -1){
            // -1 代表设置为最后一帧
            animatedPngState.currentFrameIndex = frameLocations.size()-1;
        }

        // 计算需要推进的帧数(仅播放时)
        if (animatedPngState.isPlaying && timeElapsed >= frameInterval) {
            int framesToAdvance = (int) (timeElapsed / frameInterval);
            int frameCount = frameLocations.size();

            // 根据播放方向更新帧索引
            animatedPngState.currentFrameIndex += animatedPngState.direction * framesToAdvance;

            // 处理循环逻辑（确保索引在有效范围内循环）
            int newFrameIndex = (animatedPngState.currentFrameIndex % frameCount + frameCount) % frameCount;

            // 根据播放模式处理帧索引
            if (animatedPngState.playMode == AnimatedPngState.PlayMode.PLAY_ONCE) {
                // 正向播放
                if (animatedPngState.direction == 1 && newFrameIndex < animatedPngState.currentFrameIndex) {
                    animatedPngState.currentFrameIndex = frameLocations.size()-1;
                    animatedPngState.isPlaying = false; // 停止播放
                    // 触发回调函数
                    animatedPngHolderBlockEntity.executeOnPlayOnceFinishedCallback(this.id, this);
                // 反向播放
                } else if(animatedPngState.direction == -1 && newFrameIndex > animatedPngState.currentFrameIndex) {
                    animatedPngState.currentFrameIndex = 0;
                    animatedPngState.isPlaying = false; // 停止播放
                    // 触发回调函数
                    animatedPngHolderBlockEntity.executeOnPlayOnceFinishedCallback(this.id, this);
                } else {
                    animatedPngState.currentFrameIndex = newFrameIndex;
                }
            } else { // 循环播放模式
                animatedPngState.currentFrameIndex = newFrameIndex;
            }

            animatedPngState.lastFrameTick += framesToAdvance * frameInterval;
        }
        // 不渲染则跳过渲染环节
        if (!animatedPngState.show) return;
        // 获取当前帧的纹理
        ResourceLocation currentFrame = frameLocations.get(animatedPngState.currentFrameIndex);
        ResourceLocation textureId = PNGTextureManager.getOrCreateTexture(currentFrame);

        // 绘制当前帧
        poseStack.pushPose();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(textureId));

        float halfW = width / 2.0f;
        float halfH = height / 2.0f;
        UIRender.renderVerticalRectangle(vertexConsumer, poseStack, -halfW, halfW, halfH, -halfH, light, combinedOverlay);

        poseStack.popPose();
    }
}