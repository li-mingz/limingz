package com.limingz.mymod.event.server;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.limingz.mymod.Main.MODID;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = MODID)
public class ForgeMinecraftServerEvent {
    private static MinecraftServer minecraftServer = null;
    @SubscribeEvent
    public static void getMinecraftServer(ServerStartingEvent event) {
        if (minecraftServer == null) { // 确保不重复加载
            minecraftServer = event.getServer();
        }
    }
    @SubscribeEvent
    public static void getMinecraftServer(ServerStoppingEvent event) {
        if (minecraftServer != null) { // 确保不重复加载
            minecraftServer = null;
        }
    }
    public static MinecraftServer getMinecraftServer() {
        return minecraftServer;
    }

}
