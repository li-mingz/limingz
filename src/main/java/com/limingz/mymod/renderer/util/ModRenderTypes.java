package com.limingz.mymod.renderer.util;

import com.limingz.mymod.Main;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;

public class ModRenderTypes extends RenderType {

    public ModRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    // 自定义 RenderType
    public static final RenderType SPIRAL_STRAND = create(
            Main.MODID + ":spiral_strand", // RenderType 的名称
            DefaultVertexFormat.POSITION_COLOR,                // 顶点格式：位置、颜色 (去掉了光照 LIGHTMAP)
            VertexFormat.Mode.QUADS,                           // 绘制模式：四边形
            256,                                               // 缓冲区大小
            false,                                             // affectsCrumbling
            false,                                             // sortOnUpload
            CompositeState.builder()                // 构建复合状态
                    .setShaderState(new ShaderStateShard(GameRenderer::getPositionColorShader))  // 使用原版 Position Color Shader (替代自定义 Shader)
                    .setTextureState(NO_TEXTURE)               // 【核心修改】不使用纹理
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY) // 半透明
                    .setLightmapState(NO_LIGHTMAP)             // 禁用光照
                    .setOverlayState(NO_OVERLAY)               // 无 Overlay
                    .createCompositeState(false)               // 创建状态
    );
}
