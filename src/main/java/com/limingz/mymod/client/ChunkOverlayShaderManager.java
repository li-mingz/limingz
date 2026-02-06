package com.limingz.mymod.client;

import net.minecraft.client.renderer.ShaderInstance;

public class ChunkOverlayShaderManager {
    private static ShaderInstance hologramShader;

    public static ShaderInstance getHologramShader() {
        return hologramShader;
    }

    public static void setHologramShader(ShaderInstance shader) {
        hologramShader = shader;
    }
}
