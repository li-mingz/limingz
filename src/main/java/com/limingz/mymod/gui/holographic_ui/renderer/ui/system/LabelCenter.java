package com.limingz.mymod.gui.holographic_ui.renderer.ui.system;

import com.limingz.mymod.gui.holographic_ui.font.FontStyle;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.entity.BlockEntity;

public class LabelCenter extends UIComponent{
    private String text = "";
    private int color = 0xffffff;
    private float scale = 1.0f;
    public LabelCenter(String id, float x, float y, String text) {
        super(id, x, y);
        // 不可被点击
        setCanClick(false);
        this.text = text;
    }

    @Override
    public void render(MultiBufferSource bufferSource, PoseStack poseStack, int combinedOverlay, float x, float y, BlockEntity blockEntity) {
        super.render(bufferSource, poseStack, combinedOverlay, x, y, blockEntity);
//        UIRender.renderTextCenter(bufferSource, poseStack, this.x + x, this.y + y, text, light, color, scale);
        UIRender.renderTextCenterByFont(bufferSource, poseStack, this.x + x, this.y + y, text, light, color, scale, FontStyle.MY_STYLE);
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }


}
