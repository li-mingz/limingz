package com.limingz.mymod.gui.holographic_ui.font;

import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

import static com.limingz.mymod.Main.MODID;

public class FontStyle {
    public final static Style MY_STYLE = Style.EMPTY.withFont(ResourceLocation.fromNamespaceAndPath(MODID, "myfont"));
}
