package com.limingz.mymod.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public final class KeyInit {
    public static final String KEY_CATEGORY_TUTORIAL = "key.categories.mymod";
    public static final String KEY_TOGGLE_OVERLAY = "key.mymod.toggle_overlay";

    public static final KeyMapping TOGGLE_OVERLAY = new KeyMapping(
            KEY_TOGGLE_OVERLAY,
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            KEY_CATEGORY_TUTORIAL
    );
}
