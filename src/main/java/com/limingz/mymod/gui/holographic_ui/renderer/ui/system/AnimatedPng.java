package com.limingz.mymod.gui.holographic_ui.renderer.ui.system;

import com.limingz.mymod.Main;
import com.limingz.mymod.util.PauseTick;
import com.limingz.mymod.util.pacture.PNGTextureManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import software.bernie.geckolib.util.RenderUtils;

public class AnimatedPng extends UIComponent {
    private ResourceLocation pngLocation;
    private String path;
    private int num = 0;
    private int maxNum;
    private final int fps; // 目标帧率
    private final double frameInterval; // 每帧持续的刻数 = 20.0 / fps（1秒=20刻）
    private double lastFrameTick; // 上一次切换帧的刻数

    public AnimatedPng(String id, float x, float y, float width, float height, String path, int maxNum, int fps) {
        super(id, x, y, width, height);
        this.path = path;
        this.maxNum = maxNum-1;
        this.fps = fps;
        this.frameInterval = 20.0 / fps; // 计算每帧持续的刻数（1秒=20刻）
        this.lastFrameTick = PauseTick.getTick(); // 初始时间设为当前刻数
        // 初始化图片位置（暂时为空，后续动态更新）
        pngLocation = ResourceLocation.fromNamespaceAndPath(Main.MODID, "");
    }
    @Override
    public void render(MultiBufferSource bufferSource, PoseStack poseStack, int combinedOverlay, float x, float y, BlockEntity blockEntity) {
        super.render(bufferSource, poseStack, combinedOverlay, x, y, blockEntity);
        // 获取当前游戏刻数
        double currentTick = PauseTick.getTick();
        double timeElapsed = currentTick - lastFrameTick; // 时间差（刻）

        // 判断是否需要切换帧（支持累积刻数差）
        if (timeElapsed >= frameInterval) {
            int framesToAdvance = (int) (timeElapsed / frameInterval); // 需前进的帧数
            num = (num + framesToAdvance) % maxNum; // 循环切换帧
            lastFrameTick += framesToAdvance * frameInterval; // 更新上一帧刻数
        }


        // 格式化：不足5位前面补0，超过5位则保留原数字
        String intResult = String.format("%05d", num);
        pngLocation = ResourceLocation.fromNamespaceAndPath(Main.MODID, path+intResult+".png");
        // 获取SVG纹理
        ResourceLocation textureId = PNGTextureManager.getOrCreateTexture(pngLocation);

        poseStack.pushPose();
        // 获取顶点消费者
        VertexConsumer vertexConsumer = bufferSource.getBuffer(
                RenderType.entityTranslucent(textureId) // 支持透明的渲染类型
        );

        // 绘制矩形
        float halfW = width / 2.0f;
        float halfH = height / 2.0f;

        UIRender.renderVerticalRectangle(vertexConsumer, poseStack, -halfW, halfW, halfH, -halfH, light, combinedOverlay);
        poseStack.popPose();
    }
}
