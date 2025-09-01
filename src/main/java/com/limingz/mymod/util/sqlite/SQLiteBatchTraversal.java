package com.limingz.mymod.util.sqlite;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteBatchTraversal {
    public static void traversal() {
        // 配置参数
        int batchSize = 1024;       // 每批处理行数
        long lastMaxId = -1;         // 起始ID (从0开始)
        boolean hasMoreData = true;  // 数据结束标志

        try (Connection conn = SQLiteUtil.getConnection()) {

            long startTime = System.nanoTime();  // 记录开始时间
            // 分批处理循环
            while (hasMoreData) {
                String sql = String.format(
                        "SELECT * FROM block_pos WHERE id > %d ORDER BY id ASC LIMIT %d",
                        lastMaxId, batchSize
                );

                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {

                    int count = 0;  // 当前批次遍历计数

                    while (rs.next()) {
                        // 处理数据
                        long id = rs.getLong("id");
                        int x = rs.getInt("x");
                        int y = rs.getInt("y");
                        int z = rs.getInt("z");
                        String name = rs.getString("name");
                        processData(id, x, y, z, name); // 数据处理
                        count++;
                        lastMaxId = id;  // 更新为当前最大ID
                    }
                    // 判断终止条件
                    if (count < batchSize) {
                        hasMoreData = false;     // 返回的数据量小于预设大小时终止循环
                    }
                }
            }

            long durationNanos = System.nanoTime() - startTime;
            double durationMillis = durationNanos / 1_000_000.0;
            System.out.printf("[SQLite] 遍历操作耗时: %.2f ms%n", durationMillis);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    // 数据处理
    private static void processData(long id, int x, int y, int z, String name) {
        System.out.println("[SQLite] 遍历: ID=" + id + ", X=" + x + ", Y=" + y + ", Z=" + z + ", Name=" + name);
    }
}
