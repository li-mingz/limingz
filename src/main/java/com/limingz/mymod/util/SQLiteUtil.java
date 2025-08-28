package com.limingz.mymod.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.limingz.mymod.event.server.ForgeMinecraftServerEvent;
import net.minecraft.world.level.storage.LevelResource;

import static com.limingz.mymod.Main.MODID;

public class SQLiteUtil {
    // 相对于存档路径的位置
    private static final String dbFilePathBySave = "db/user.db";
    private static String dbFilePath = null;
    // 是否初始化过
    private static Boolean isInitialized = false;
    public static Boolean initSQLite() {
        // 获取当前存档目录
        if (ForgeMinecraftServerEvent.getMinecraftServer() != null){
            Path tempPath = ForgeMinecraftServerEvent.getMinecraftServer().getWorldPath(LevelResource.ROOT).resolve("data").resolve(MODID).normalize();
            try {
                Files.createDirectories(tempPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            dbFilePath = String.valueOf(tempPath.resolve(dbFilePathBySave).toAbsolutePath());
            isInitialized = true;
            return true;
        }
        return false;
    }
    public static Connection getConnection(){
        if(!isInitialized) return null;
        System.out.println(dbFilePath);
        try {
            Class.forName("org.sqlite.JDBC"); // 在连接前调用
            return DriverManager.getConnection("jdbc:sqlite:"+dbFilePath);
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void UnInitSQLite() {
        dbFilePath = null;
        isInitialized = false;
    }
}
