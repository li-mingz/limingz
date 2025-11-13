package com.limingz.mymod.util;

//import net.minecraft.client.Minecraft;
import software.bernie.geckolib.util.RenderUtils;

public class PauseTick {
//    private static double lastPauseTick = 0; // 暂停时第一个tick
//    private static double chunkTick = 0; // 暂停的时间
//    private static Boolean isPaused = false;
    public static double getTick(){
//        Minecraft mc = Minecraft.getInstance();
//        if(mc.isPaused()){
//            if(!isPaused) {
//                isPaused = true;
//                lastPauseTick = RenderUtils.getCurrentTick() - chunkTick;
//            }
//            return lastPauseTick;
//        } else {
//            if(isPaused) {
//                isPaused = false;
//                chunkTick = RenderUtils.getCurrentTick() - lastPauseTick;
//            }
//            return RenderUtils.getCurrentTick() - chunkTick;
//        }
        // 直接返回
        return RenderUtils.getCurrentTick();
    }
}
