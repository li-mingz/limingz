package com.limingz.mymod.util;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import software.bernie.geckolib.util.RenderUtils;

import static com.limingz.mymod.Main.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class PauseTick {
    public static double pauseTick = 0; // 暂停时不计数的tick
    private static double lastPauseTick = 0; // 暂停时第一个tick
    private static double chunkTick = 0; // 暂停的时间
    private static Boolean isPaused = false;
    @SubscribeEvent
    public static void ClientTickEvent(TickEvent event){
        Minecraft mc = Minecraft.getInstance();
        if(mc.isPaused()){
            if(!isPaused) {
                isPaused = true;
                lastPauseTick = RenderUtils.getCurrentTick() - chunkTick;
                pauseTick = lastPauseTick;
            }
        } else {
            if(isPaused) {
                isPaused = false;
                chunkTick = RenderUtils.getCurrentTick() - lastPauseTick;
            }
            pauseTick = RenderUtils.getCurrentTick() - chunkTick;
        }
    }
}
