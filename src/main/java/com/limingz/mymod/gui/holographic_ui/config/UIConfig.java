package com.limingz.mymod.gui.holographic_ui.config;

public class UIConfig {
    // 背景板参数
    public static final float BG_SIZE = Math.max(1.76f, 1.03f);
    public static final float BG_SCALE = 1f / BG_SIZE;
    public static final float BG_X = 1.76f * BG_SCALE;
    public static final float BG_Y = 1.03f * BG_SCALE;

    public static final float BG_HEIGHT = 1.3f;
    public static final float BG_ROTATION = 45.0f;

    // 按钮参数
//    public static final float BUTTON_HALF_SIZE = BUTTON_SIZE / 2;
    public static final float BUTTON_WIDTH = (float) (BG_X * 0.178);
    public static final float BUTTON_HEIGHT = (float) (BG_Y * 0.384);
    public static final float TEXT_SCALE = 0.02f;              // 文字缩放大小
    public static final int TEXT_COLOR = 0x8fbdf1;      // 文字颜色


    // 按钮位置
    public static final float[] BUTTON1_XY = {0.139f * BG_X, 0.228f * BG_Y};
    public static final float[] BUTTON2_XY = {0.317f * BG_X, 0.228f * BG_Y};
    public static final float[] BUTTON3_XY = {0.495f * BG_X, 0.228f * BG_Y};
    public static final float[] BUTTON4_XY = {0.673f * BG_X, 0.228f * BG_Y};

}