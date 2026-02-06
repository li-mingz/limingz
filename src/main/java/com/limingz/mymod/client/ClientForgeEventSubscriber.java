package com.limingz.mymod.client;

import com.limingz.mymod.Main;
import com.limingz.mymod.command.ClientOverlayCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Main.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientForgeEventSubscriber {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (KeyInit.TOGGLE_OVERLAY.consumeClick()) {
            boolean newState = !ChunkOverlayManager.isEnabled();
            ChunkOverlayManager.setEnabled(newState);

            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                if (newState) {
                    ChunkPos current = mc.player.chunkPosition();
                    ChunkOverlayManager.setAnchorChunk(current);
                    mc.player.displayClientMessage(Component.literal("Chunk Overlay Enabled at " + current), true);
                } else {
                    mc.player.displayClientMessage(Component.literal("Chunk Overlay Disabled"), true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        Main.LOGGER.info("Registering client commands...");
        ClientOverlayCommand.register(event.getDispatcher());
    }
}
