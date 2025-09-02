package com.limingz.mymod.util.sqlite;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;

import com.limingz.mymod.event.server.ForgeMinecraftServerEvent;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.minecraft.world.level.storage.LevelResource;

import static com.limingz.mymod.Main.MODID;

public class SQLiteUtil {
    private static HikariDataSource dataSource;
    // 相对于存档路径的位置
    private static final String dbFilePathBySave = "user.db";
    private static String dbFilePath = null;
    // 是否初始化过
    private static Boolean isInitialized = false;
    static {
        try {
            Class.forName("org.sqlite.JDBC"); // 在连接前调用
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public static Boolean initSQLite() {
        // 获取当前存档目录
        if (ForgeMinecraftServerEvent.getMinecraftServer() != null){
            Path tempPath = ForgeMinecraftServerEvent.getMinecraftServer().getWorldPath(LevelResource.ROOT).resolve("data").resolve(MODID).resolve("db").normalize();
            try {
                Files.createDirectories(tempPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            dbFilePath = String.valueOf(tempPath.resolve(dbFilePathBySave).toAbsolutePath());
            isInitialized = true;
            // 如果数据库不存在则创建数据库
            try(Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath)){} catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            HikariConfig config = getHikariConfig();
            dataSource = new HikariDataSource(config);
            createTable();
            return true;
        }
        return false;
    }

    private static HikariConfig getHikariConfig() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + dbFilePath); // 文件路径
        config.setDriverClassName("org.sqlite.JDBC");
        // 关键优化参数
        config.setMaximumPoolSize(10);   // 连接 = 10
        config.setConnectionTimeout(10000);      // 获取连接超时（毫秒）
        config.setIdleTimeout(600000);           // 空闲连接存活时间
        config.setMaxLifetime(1800000);          // 连接最大生命周期
        config.addDataSourceProperty("cachePrepStmts", true); // 启用预编译语句缓存
        return config;
    }

    private static void createTable(){
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            // 建表（IF NOT EXISTS保障幂等性）
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS block_pos (" +
                            "    id INTEGER PRIMARY KEY," +
                            "    x INT NOT NULL," +
                            "    y INT NOT NULL," +
                            "    z INT NOT NULL," +
                            "    name TEXT," +
                            "    chunk_x INT NOT NULL," +
                            "    chunk_y INT NOT NULL," +
                            "    UNIQUE(x, y, z, name, chunk_x, chunk_y)" +
                            ")"
            );
        } catch (SQLException e) {
            throw new RuntimeException("初始化失败", e);
        }
    }
    public static Connection getConnection(){
        if(!isInitialized) return null;
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void UnInitSQLite() {
        dbFilePath = null;
        isInitialized = false;
        if(dataSource != null) dataSource.close();
        dataSource = null;
    }

}
