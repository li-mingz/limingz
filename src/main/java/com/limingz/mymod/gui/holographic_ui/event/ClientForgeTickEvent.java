package com.limingz.mymod.gui.holographic_ui.event;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.limingz.mymod.Main.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeTickEvent {
    public static double updateTick = 0; // 每 Tick增加的计数器
    @SubscribeEvent
    public static void ClientTickEvent(TickEvent event){
        updateTick++;
    }
}
