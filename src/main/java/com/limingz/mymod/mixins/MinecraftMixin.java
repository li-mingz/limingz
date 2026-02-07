package com.limingz.mymod.mixins;

import com.limingz.mymod.client.ChunkOverlayManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ProgressScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void cancelReceivingLevelScreen(Screen screen, CallbackInfo ci) {
        if (ChunkOverlayManager.isSeamlessTransitioning() && screen != null) {
            String screenName = screen.getClass().getSimpleName();
            System.out.println("[Debug] setScreen called with: " + screenName);

            // Cancel ReceivingLevelScreen (Dirt BG) and LevelLoadingScreen (Joining World percent)
            if (screen instanceof ReceivingLevelScreen || screen instanceof ProgressScreen || screenName.contains("LevelLoadingScreen") || screenName.contains("LoadingOverlay")) {
                System.out.println("[Debug] Cancelling " + screenName + " due to Seamless Transition.");
                ci.cancel();
            }
        }
    }
}
