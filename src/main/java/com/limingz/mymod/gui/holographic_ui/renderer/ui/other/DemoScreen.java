package com.limingz.mymod.gui.holographic_ui.renderer.ui.other;

import com.limingz.mymod.gui.holographic_ui.config.UIConfig;
import com.limingz.mymod.gui.holographic_ui.renderer.ui.system.*;
import net.minecraft.resources.ResourceLocation;

import static com.limingz.mymod.Main.MODID;

public class DemoScreen extends Screen {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/screen/background.png");
    private static final ResourceLocation BUTTON_TEXTURE1 =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/screen/button1.png");
    private static final ResourceLocation BUTTON_TEXTURE2 =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/screen/button2.png");
    private static final ResourceLocation BUTTON_TEXTURE3 =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/screen/button3.png");
    private static final ResourceLocation BUTTON_TEXTURE4 =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/screen/button4.png");
    private static final ResourceLocation LINE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/screen/circle.png");
    public Panel panel;
    private LabelCenter labelCenter1;
    private LabelCenter labelCenter2;
    private LabelCenter labelCenter3;
    private LabelCenter labelCenter4;
    private DemoButton button1;
    private DemoButton button2;
    private DemoButton button3;
    private DemoButton button4;
    private LineByPoint lineByPoint;
    private DottedOvalCircle dottedOvalCircle;
    private OvalCircle ovalCircle;
    private Circle circle;

    public DemoScreen(String id, float x, float y, float width, float height) {
        super(id, x, y, width, height);

        setX_rotationDegrees(UIConfig.BG_ROTATION);
        setTranslate(0, (float) (UIConfig.BG_HEIGHT + height/2.0f*Math.sin(getX_rotationDegrees())), (float) ((1.0f - height*Math.cos(getX_rotationDegrees()))/2.0f));

        panel = new Panel("主窗口", 0, 0, width, height);
        panel.setBackgroundTexture(TEXTURE);
        labelCenter1 = new LabelCenter("文本1", 0, 0, "GROW");
        labelCenter1.setX(0.47f);
        labelCenter1.setY(-0.2f);
        labelCenter1.setScale(UIConfig.TEXT_SCALE);
        labelCenter1.setColor(UIConfig.TEXT_COLOR);
        labelCenter1.setVisible(false);
        panel.addChild(labelCenter1);
        labelCenter2 = new LabelCenter("文本2", 0, 0, "SUBSISTING");
        labelCenter2.setX(0.47f);
        labelCenter2.setY(-0.2f);
        labelCenter2.setScale(UIConfig.TEXT_SCALE);
        labelCenter2.setColor(UIConfig.TEXT_COLOR);
        labelCenter2.setVisible(false);
        panel.addChild(labelCenter2);
        labelCenter3 = new LabelCenter("文本3", 0, 0, "BREED");
        labelCenter3.setX(0.47f);
        labelCenter3.setY(-0.2f);
        labelCenter3.setScale(UIConfig.TEXT_SCALE);
        labelCenter3.setColor(UIConfig.TEXT_COLOR);
        labelCenter3.setVisible(false);
        panel.addChild(labelCenter3);
        labelCenter4 = new LabelCenter("文本4", 0, 0, "MIGRATION");
        labelCenter4.setX(0.47f);
        labelCenter4.setY(-0.2f);
        labelCenter4.setScale(UIConfig.TEXT_SCALE);
        labelCenter4.setColor(UIConfig.TEXT_COLOR);
        labelCenter4.setVisible(false);
        panel.addChild(labelCenter4);
        lineByPoint = new LineByPoint("框1", 0, 0, panel.getWidth(), panel.getHeight(), 0.0025f, 0xFF8fbdf1, LINE_TEXTURE);
        panel.addChild(lineByPoint);
        dottedOvalCircle = new DottedOvalCircle("椭圆1", 0.5f, 0.25f, 300, 0.173f*1.75f,0.173f, 0.0025f, 0xFF8fbdf1);
        panel.addChild(dottedOvalCircle);
        ovalCircle = new OvalCircle("椭圆2", 0.5f, 0.25f, 300, 0.143f*1.75f,0.143f*1.1f, 0.005f, 0xFF8fbdf1);
        panel.addChild(ovalCircle);
//        circle = new LightCircle("圆1", 0.4985f, 0.27f, 60, 0.173f, 0.005f, 0xFF5791CA);
        circle = new LightCircle("圆1", 0.5f, 0.25f, 60, 0.173f, 0.005f, 0xFF5791CA);
        circle.setX_rotationDegrees(25.0f);
        panel.addChild(circle);
        button1 = new DemoButton("按钮1", UIConfig.BUTTON1_XY[0], UIConfig.BUTTON1_XY[1], UIConfig.BUTTON_WIDTH,  UIConfig.BUTTON_HEIGHT){
            @Override
            protected void onMouseClick(double mouseX, double mouseY) {
                super.onMouseClick(mouseX, mouseY);
                if(labelCenter1.getVisible()){
                    labelCenter1.setVisible(false);
                    button1.setVisible(false);
                } else {
                    labelCenter1.setVisible(true);
                    labelCenter2.setVisible(false);
                    labelCenter3.setVisible(false);
                    labelCenter4.setVisible(false);
                    button1.setVisible(true);
                    button2.setVisible(false);
                    button3.setVisible(false);
                    button4.setVisible(false);
                }
            }
        };
        button1.setBackgroundTexture(BUTTON_TEXTURE1);
        button1.setVisible(false);
        panel.addChild(button1);
        button2 = new DemoButton("按钮2", UIConfig.BUTTON2_XY[0], UIConfig.BUTTON2_XY[1], UIConfig.BUTTON_WIDTH,  UIConfig.BUTTON_HEIGHT){
            @Override
            protected void onMouseClick(double mouseX, double mouseY) {
                super.onMouseClick(mouseX, mouseY);
                if(labelCenter2.getVisible()){
                    labelCenter2.setVisible(false);
                    button2.setVisible(false);
                } else {
                    labelCenter1.setVisible(false);
                    labelCenter2.setVisible(true);
                    labelCenter3.setVisible(false);
                    labelCenter4.setVisible(false);
                    button1.setVisible(false);
                    button2.setVisible(true);
                    button3.setVisible(false);
                    button4.setVisible(false);
                }
            }
        };;
        button2.setBackgroundTexture(BUTTON_TEXTURE2);
        button2.setVisible(false);
        panel.addChild(button2);
        button3 = new DemoButton("按钮3", UIConfig.BUTTON3_XY[0], UIConfig.BUTTON3_XY[1], UIConfig.BUTTON_WIDTH,  UIConfig.BUTTON_HEIGHT){
            @Override
            protected void onMouseClick(double mouseX, double mouseY) {
                super.onMouseClick(mouseX, mouseY);
                if(labelCenter3.getVisible()){
                    labelCenter3.setVisible(false);
                    button3.setVisible(false);
                } else {
                    labelCenter1.setVisible(false);
                    labelCenter2.setVisible(false);
                    labelCenter3.setVisible(true);
                    labelCenter4.setVisible(false);
                    button1.setVisible(false);
                    button2.setVisible(false);
                    button3.setVisible(true);
                    button4.setVisible(false);
                }
            }
        };;
        button3.setBackgroundTexture(BUTTON_TEXTURE3);
        button3.setVisible(false);
        panel.addChild(button3);
        button4 = new DemoButton("按钮4", UIConfig.BUTTON4_XY[0], UIConfig.BUTTON4_XY[1], UIConfig.BUTTON_WIDTH,  UIConfig.BUTTON_HEIGHT){
            @Override
            protected void onMouseClick(double mouseX, double mouseY) {
                super.onMouseClick(mouseX, mouseY);
                if(labelCenter4.getVisible()){
                    labelCenter4.setVisible(false);
                    button4.setVisible(false);
                } else {
                    labelCenter1.setVisible(false);
                    labelCenter2.setVisible(false);
                    labelCenter3.setVisible(false);
                    labelCenter4.setVisible(true);
                    button1.setVisible(false);
                    button2.setVisible(false);
                    button3.setVisible(false);
                    button4.setVisible(true);
                }
            }
        };;
        button4.setBackgroundTexture(BUTTON_TEXTURE4);
        button4.setVisible(false);
        panel.addChild(button4);
        this.addChild(panel);
    }
}
