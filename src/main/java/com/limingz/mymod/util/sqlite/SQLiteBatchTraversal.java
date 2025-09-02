package com.limingz.mymod.util.sqlite;

import com.limingz.mymod.event.server.ForgeMinecraftServerEvent;
import com.limingz.mymod.util.BlockReplacer;
import net.minecraft.world.level.Level;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteBatchTraversal {
    private int batchSize;
    private int last_x_pos;         // 上一次的x坐标
    private int last_y_pos;         // 上一次的y坐标
    private boolean isActive;       // 遍历是否激活
    private boolean hasMoreData;    // 是否还有剩余数据

    // 重置遍历状态
    public void reset() {
        batchSize = 256;           // 每批处理行数
        last_x_pos = Integer.MIN_VALUE;
        last_y_pos = Integer.MIN_VALUE;
        isActive = false;
        hasMoreData = true;
        // 初始化方块替换器
        BlockReplacer.init();
    }

    // 开始遍历
    public void startTraversal() {
        if (!isActive) {
            isActive = true;
            last_x_pos = Integer.MIN_VALUE;
            last_y_pos = Integer.MIN_VALUE;
            hasMoreData = true;
        }
    }

    // 每tick调用此方法处理一批数据
    public void tickTraversal() {
        if (!isActive || !hasMoreData) {
            return;
        }

        try (Connection conn = SQLiteUtil.getConnection()) {
            hasMoreData = batchSearch(conn);
        } catch (SQLException e) {
            e.printStackTrace();
            isActive = false; // 出错时停止遍历
            throw new RuntimeException(e);
        }

        // 如果遍历完成，重置状态
        if (!hasMoreData) {
            isActive = false;
            BlockReplacer.endProcessData();
            // 清空表
            SQLiteUtil.clearSQLite();
        }
    }

    private boolean batchSearch(Connection conn) throws SQLException {
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
                BlockReplacer.processData(x, y, z, name, chunk_x, chunk_y);
                count++;
                last_x_pos = chunk_x;  // 更新为当前最大x坐标
                last_y_pos = chunk_y;  // 更新为当前最大y坐标
            }
            // 返回的数据量小于预设大小时终止循环
            return count >= batchSize;
        }
    }
}