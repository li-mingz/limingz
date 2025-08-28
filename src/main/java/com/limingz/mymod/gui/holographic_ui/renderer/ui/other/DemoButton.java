package com.limingz.mymod.gui.holographic_ui.renderer.ui.other;

import com.limingz.mymod.gui.holographic_ui.renderer.ui.system.Button;

public class DemoButton extends Button {
    public DemoButton(String id, float x, float y, float width, float height) {
        super(id, x, y, width, height);
    }

    @Override
    // 隐藏状态下仍可接收点击事件
    public boolean handleClick(double mouseX, double mouseY) {
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
}
