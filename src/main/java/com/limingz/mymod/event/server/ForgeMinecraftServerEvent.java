package com.limingz.mymod.event.server;

import com.limingz.mymod.util.RegionUtil;
import com.limingz.mymod.util.sqlite.SQLiteUtil;
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
            SQLiteUtil.initSQLite();
        }
    }
    @SubscribeEvent
    public static void getMinecraftServer(ServerStoppingEvent event) {
        if (minecraftServer != null) { // 确保不重复加载
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
