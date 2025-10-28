package com.limingz.mymod.gui.holographic_ui.renderer.ui.system;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

public abstract class UIComponent {
    protected float x; // 组件左上角 X 坐标
    protected float y; // 组件左上角 Y 坐标
    protected float width;
    protected float height;
    protected boolean visible = true; // 是否可见
    protected boolean canClick = true; // 是否可被点击
    protected int light = LightTexture.pack(15,15);
    private UIComponent parent; // 父组件引用
    protected String id; // 唯一标识符
    protected final List<UIComponent> children = new ArrayList<>(); // 子类列表

    public UIComponent(String id, float x, float y, float width, float height) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    public UIComponent(String id, float x, float y) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    public void addChild(UIComponent uiComponent){
        children.add(uiComponent);
        uiComponent.parent = this;
    }

    public void render(MultiBufferSource bufferSource, PoseStack poseStack, int combinedOverlay, float x, float y, BlockEntity blockEntity){
        if(!visible) return;
    };
    protected void onMouseClick(double mouseX, double mouseY){
        System.out.println(
                id+" 被点击, 控件坐标: "+mouseX+", "+mouseY+" 全局坐标: "+(getGlobalX()+mouseX)+", "+(getGlobalY()+mouseY));
    };

    // 渲染自己及子控件
    public void renderAll(MultiBufferSource bufferSource, PoseStack poseStack, int combinedOverlay, BlockEntity blockEntity){
        if(!visible) return;
        poseStack.pushPose();
        render(bufferSource, poseStack, combinedOverlay, x, y, blockEntity);
        for (int i = 0; i <= children.size() - 1; i++) {
            poseStack.translate(0, 0.001f, 0);  // z序
            children.get(i).renderAll(bufferSource, poseStack, combinedOverlay, x, y, blockEntity);
        }
        poseStack.popPose();
    }

    public void renderAll(MultiBufferSource bufferSource, PoseStack poseStack, int combinedOverlay, float x, float y, BlockEntity blockEntity){
        if(!visible) return;
        poseStack.pushPose();
        render(bufferSource, poseStack, combinedOverlay, x, y, blockEntity);
        for (int i = 0; i <= children.size() - 1; i++) {
            poseStack.translate(0, 0.001f, 0);  // z序
            children.get(i).renderAll(bufferSource, poseStack, combinedOverlay, x, y, blockEntity);
        }
        poseStack.popPose();
    }

    // 事件处理
    public boolean handleClick(double mouseX, double mouseY) {
        if (!visible) return false;
        if (!canClick) return false;
        float globalX = getGlobalX();
        float globalY = getGlobalY();

        if (mouseX >= globalX && mouseX <= globalX + width &&
                mouseY >= globalY && mouseY <= globalY + height) {

            // 逆向遍历（后添加的组件优先）
            for (int i = children.size() - 1; i >= 0; i--) {
                if (children.get(i).handleClick(mouseX, mouseY)) {
                    return true;
                }
            }
            onMouseClick(mouseX - globalX, mouseY - globalY);
            return true;
        }
        return false;
    }
    public void setLight(int light) {
        this.light = light;
    }

    public void setCanClick(boolean canClick) {
        this.canClick = canClick;
    }


    // Getter/Setter
    public String getId() { return id; }
    public float getX() { return x; }
    public void setX(float x) { this.x = x; }
    public float getY() { return y; }
    public void setY(float y) { this.y = y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
    public boolean getVisible() { return visible; }

    // 获取相全局坐标
    public float getGlobalX() {
        return (parent != null) ? x + parent.getGlobalX() : x;
    }

    public float getGlobalY() {
        return (parent != null) ? y + parent.getGlobalY() : y;
    }
}