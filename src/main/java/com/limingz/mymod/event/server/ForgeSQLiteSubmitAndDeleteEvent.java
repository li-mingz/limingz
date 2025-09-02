package com.limingz.mymod.event.server;

import com.limingz.mymod.util.sqlite.SQLiteTempData;
import com.limingz.mymod.util.sqlite.SQLiteUtil;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ForgeSQLiteSubmitAndDeleteEvent {

    @SubscribeEvent
    public static void onServerSave(LevelEvent.Save event) {
        // 仅在服务端保存
        if (event.getLevel().isClientSide()) return;
        // 先删除，再插入
        // 删除
        deleteFromSQLite();
        // 插入
        submitToSQLite();
    }

    public static void submitToSQLite() {
        // 队列为空则返回
        if (SQLiteTempData.sqliteAddQueue.isEmpty()) return;

        long startTime = System.nanoTime();  // 记录开始时间

        try (Connection conn = SQLiteUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT OR IGNORE INTO block_pos (x, y, z, name, chunk_x, chunk_y) VALUES (?,?,?,?,?,?)")) {

            conn.setAutoCommit(false);
            int batchSize = 0;

            // 批量插入
            while (!SQLiteTempData.sqliteAddQueue.isEmpty()) {
                Object x = SQLiteTempData.sqliteAddQueue.poll();
                Object y = SQLiteTempData.sqliteAddQueue.poll();
                Object z = SQLiteTempData.sqliteAddQueue.poll();
                Object name = SQLiteTempData.sqliteAddQueue.poll();
                Object chunk_x = SQLiteTempData.sqliteAddQueue.poll();
                Object chunk_y = SQLiteTempData.sqliteAddQueue.poll();

                if (x == null || y == null || z == null || name == null || chunk_x == null || chunk_y == null) break;

                pstmt.setInt(1, (Integer) x);
                pstmt.setInt(2, (Integer) y);
                pstmt.setInt(3, (Integer) z);
                pstmt.setString(4, (String) name);
                pstmt.setInt(5, (Integer) chunk_x);
                pstmt.setInt(6, (Integer) chunk_y);
                pstmt.addBatch(); // 批量
                batchSize++;

                // 每100条执行一次批量
                if (batchSize % 100 == 0) pstmt.executeBatch();
            }
            pstmt.executeBatch(); // 提交剩余数据
            conn.commit();

            long durationNanos = System.nanoTime() - startTime;
            double durationMillis = durationNanos / 1_000_000.0;
            System.out.printf("[SQLite] 插入操作耗时: %.2f ms%n", durationMillis);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteFromSQLite() {
        // 队列为空则返回
        if (SQLiteTempData.sqliteDeleteQueue.isEmpty()) return;

        long startTime = System.nanoTime();  // 记录开始时间

        try (Connection conn = SQLiteUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "DELETE FROM block_pos WHERE x=? AND y=? AND z=? AND name=? AND chunk_x=? AND chunk_y=?")) {

            conn.setAutoCommit(false);  // 关闭自动提交
            int batchSize = 0;

            while (!SQLiteTempData.sqliteDeleteQueue.isEmpty()) {
                Object x = SQLiteTempData.sqliteDeleteQueue.poll();
                Object y = SQLiteTempData.sqliteDeleteQueue.poll();
                Object z = SQLiteTempData.sqliteDeleteQueue.poll();
                Object name = SQLiteTempData.sqliteDeleteQueue.poll();
                Object chunk_x = SQLiteTempData.sqliteDeleteQueue.poll();
                Object chunk_y = SQLiteTempData.sqliteDeleteQueue.poll();

                if (x == null || y == null || z == null || name == null || chunk_x == null || chunk_y == null) break;

                // 绑定删除条件
                pstmt.setInt(1, (Integer) x);
                pstmt.setInt(2, (Integer) y);
                pstmt.setInt(3, (Integer) z);
                pstmt.setString(4, (String) name);
                pstmt.setInt(5, (Integer) chunk_x);
                pstmt.setInt(6, (Integer) chunk_y);
                pstmt.addBatch();  // 加入批量操作
                batchSize++;

                // 每100条执行一次批量删除
                if (batchSize % 100 == 0) {
                    pstmt.executeBatch();  // 执行当前批次
                }
            }
            pstmt.executeBatch();  // 提交剩余数据
            conn.commit();         // 提交事务

            long durationNanos = System.nanoTime() - startTime;
            double durationMillis = durationNanos / 1_000_000.0;
            System.out.printf("[SQLite] 删除操作耗时: %.2f ms%n", durationMillis);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



}