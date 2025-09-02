package com.limingz.mymod.util.sqlite;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteBatchTraversal {
    private static int batchSize;
    private static int last_x_pos;         // 起始x坐标
    private static int last_y_pos;         // 起始y坐标
    public static void traversal() {
        // 初始化参数
        batchSize = 1024;       // 每批处理组数
        last_x_pos = Integer.MIN_VALUE;         // 起始x坐标
        last_y_pos = Integer.MIN_VALUE;         // 起始y坐标
        boolean hasMoreData = true;  // 数据结束标志

        try (Connection conn = SQLiteUtil.getConnection()) {

            long startTime = System.nanoTime();  // 记录开始时间
            // 分批处理循环
            while (hasMoreData) {
                hasMoreData = batchSearch(conn);
            }

            long durationNanos = System.nanoTime() - startTime;
            double durationMillis = durationNanos / 1_000_000.0;
            System.out.printf("[SQLite] 遍历操作耗时: %.2f ms%n", durationMillis);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static boolean batchSearch(Connection conn) throws SQLException {
        String sql = String.format(
                "SELECT * FROM block_pos " +
                        "WHERE (chunk_x, chunk_y) > (%d, %d) " +
                        "ORDER BY chunk_x ASC, chunk_y ASC " +
                        "LIMIT %d",
                last_x_pos, last_y_pos, batchSize
        );

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            int count = 0;  // 当前批次遍历计数

            while (rs.next()) {
                // 处理数据
                int x = rs.getInt("x");
                int y = rs.getInt("y");
                int z = rs.getInt("z");
                String name = rs.getString("name");
                int chunk_x = rs.getInt("chunk_x");
                int chunk_y = rs.getInt("chunk_y");
                processData(x, y, z, name, chunk_x, chunk_y); // 数据处理
                count++;
                last_x_pos = chunk_x;  // 更新为当前最大x坐标
                last_y_pos = chunk_y;  // 更新为当前最大x坐标
            }
            // 判断终止条件
            if (count < batchSize) return false; // 返回的数据量小于预设大小时终止循环
            return true;
        }
    }
    // 数据处理
    private static void processData(int x, int y, int z, String name, int chunk_x, int chunk_y) {
        System.out.println("[SQLite] 遍历: X=" + x + ", Y=" + y + ", Z=" + z + ", Name=" + name + ", Chunk_X=" + chunk_x + ", Chunk_Y=" + chunk_y);
    }
}
