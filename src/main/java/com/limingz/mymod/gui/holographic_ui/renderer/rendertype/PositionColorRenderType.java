package com.limingz.mymod.gui.holographic_ui.renderer.rendertype;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

public class PositionColorRenderType {
    private static final RenderType.CompositeState rendertype$compositestate =
            RenderType.CompositeState.builder()
                    .setShaderState(RenderTypeValue.POSITION_COLOR_SHADER)
                    .setTransparencyState(RenderTypeValue.TRANSLUCENT_TRANSPARENCY)
                    .setCullState(RenderTypeValue.NO_CULL)
                    .setOverlayState(RenderTypeValue.OVERLAY)
                    .createCompositeState(false);
    public static final RenderType renderType_no_uv_light_normal = RenderType.create("entity_translucent_no_uv_light_normal", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256, true, true, rendertype$compositestate);
}
