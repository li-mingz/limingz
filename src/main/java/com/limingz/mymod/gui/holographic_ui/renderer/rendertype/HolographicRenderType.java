package com.limingz.mymod.gui.holographic_ui.renderer.rendertype;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.ForgeRenderTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class HolographicRenderType {

    /**
     * 创建一个新的RenderType
     * @param resourceLocation 资源
     * @return RenderType实例
     */
    private static RenderType createNewRenderType(ResourceLocation resourceLocation){
        RenderType.CompositeState holographic_render_type_text_state = RenderType.CompositeState.builder()
                .setShaderState(RenderTypeValue.RENDERTYPE_TEXT_SHADER)
                .setTextureState(new RenderTypeValue.CustomizableTextureState(resourceLocation, () -> ForgeRenderTypes.enableTextTextureLinearFiltering, () -> false))
                .setTransparencyState(RenderTypeValue.TRANSLUCENT_TRANSPARENCY)
                .setCullState(RenderTypeValue.NO_CULL)
                .setLightmapState(RenderTypeValue.LIGHTMAP)
                .createCompositeState(false);
        RenderType holographic_render_type_text = RenderType.create("forge_text_no_cull", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 256, false, true, holographic_render_type_text_state);
        return holographic_render_type_text;
    }

    public static Function<ResourceLocation, RenderType> TEXT_NO_CULL = Util.memoize(HolographicRenderType::createNewRenderType);

    /**
     * 从缓存中获取RenderType, 如果没有则创建一个
     * @param s 路径 例: minecraft:default/0
     * @return RenderType实例
     */
    public static RenderType getRenderType(String s){
        return TEXT_NO_CULL.apply(ResourceLocation.tryParse(s));
    }

}
