package com.limingz.mymod.gui.holographic_ui.renderer.ui.system;

import com.limingz.mymod.Main;
import com.limingz.mymod.gui.holographic_ui.interfaces.AnimatedPngHolder;
import com.limingz.mymod.gui.holographic_ui.util.PngState;
import com.limingz.mymod.util.pacture.PNGTextureManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Map;

public class PNG extends UIComponent{

    // 资源路径
    private ResourceLocation PNG_LOCATION;
    public PNG(String id, float x, float y, float width, float height, String path) {
        super(id, x, y, width, height);
        PNG_LOCATION =  ResourceLocation.fromNamespaceAndPath(Main.MODID, path);
    }
    @Override
    public void render(MultiBufferSource bufferSource, PoseStack poseStack, int combinedOverlay, float x, float y, BlockEntity blockEntity) {
        super.render(bufferSource, poseStack, combinedOverlay, x, y, blockEntity);

        Map<String, PngState> pngStateMap = ((AnimatedPngHolder)blockEntity).getPngState();
        if(pngStateMap.containsKey(this.id)){
            PngState pngState = pngStateMap.get(this.id);
            // 跳过不渲染的
            if(!pngState.show) return;
        }

        // 获取SVG纹理
        ResourceLocation textureId = PNGTextureManager.getOrCreateTexture(PNG_LOCATION);

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

    public void setPngLocation(ResourceLocation pngLocation) {
        PNG_LOCATION = pngLocation;
    }
}
