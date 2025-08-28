package com.limingz.mymod.gui.holographic_ui.font;

import com.limingz.mymod.gui.holographic_ui.renderer.rendertype.HolographicRenderType;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NoCuLLFontBufferSource implements MultiBufferSource {
    private final BufferSource bufferSource;

    public NoCuLLFontBufferSource(BufferSource bufferSource) {
        this.bufferSource = bufferSource;
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {

        Pattern pattern = Pattern.compile("Optional\\[(.*?)?\\]");
        Matcher matcher = pattern.matcher(renderType.toString());
        if (matcher.find()) {
            return bufferSource.getBuffer(HolographicRenderType.getRenderType(matcher.group(1)));
        }
        return bufferSource.getBuffer(renderType);
    }


}