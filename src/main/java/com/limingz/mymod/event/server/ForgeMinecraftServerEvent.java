package com.limingz.mymod.event.server;

import com.limingz.mymod.util.SQLiteUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.limingz.mymod.Main.MODID;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = MODID)
public class ForgeMinecraftServerEvent {
    private static MinecraftServer minecraftServer = null;
    @SubscribeEvent
    public static void getMinecraftServer(LevelEvent.Load event) {
        if (!event.getLevel().isClientSide() && minecraftServer == null) { // 确保在服务端并且不重复加载
            minecraftServer = ((ServerLevel) event.getLevel()).getServer();
            SQLiteUtil.initSQLite();
        }
    }
    @SubscribeEvent
    public static void getMinecraftServer(LevelEvent.Unload event) {
        if (!event.getLevel().isClientSide() && minecraftServer != null) { // 确保在服务端并且不重复加载
            // 退出前保存数据
            // 先删除，再插入
            // 更改数据库
            ForgeSQLiteSubmitAndDeleteEvent.deleteFromSQLite();
            ForgeSQLiteSubmitAndDeleteEvent.submitToSQLite();
            minecraftServer = null;
            SQLiteUtil.UnInitSQLite();
        }
    }
    public static MinecraftServer getMinecraftServer() {
        return minecraftServer;
    }

}
