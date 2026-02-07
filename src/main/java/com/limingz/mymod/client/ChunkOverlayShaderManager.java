package com.limingz.mymod.client;

import net.minecraft.client.renderer.ShaderInstance;

public class ChunkOverlayShaderManager {
    private static ShaderInstance hologramShader;
    private static ShaderInstance transitionShader;

    public static ShaderInstance getHologramShader() {
        return hologramShader;
    }

    public static void setHologramShader(ShaderInstance shader) {
        hologramShader = shader;
    }

    public static ShaderInstance getTransitionShader() {
        return transitionShader;
    }

    public static void setTransitionShader(ShaderInstance shader) {
        transitionShader = shader;
    }
}
