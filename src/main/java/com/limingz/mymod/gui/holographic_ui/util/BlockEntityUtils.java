package com.limingz.mymod.gui.holographic_ui.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class BlockEntityUtils {

    /**
     * 获取玩家附近所有方块实体
     * @param player 玩家对象
     * @param radius 搜索半径（方块数）
     * @return 方块实体列表
     */
    public static List<BlockEntity> getBlockEntitiesNearPlayer(Player player, int radius) {
        return getBlockEntitiesNearPlayer(player, radius, be -> true);
    }

    /**
     * 获取玩家附近特定类型的方块实体
     * @param player 玩家对象
     * @param radius 搜索半径（方块数）
     * @param blockEntityClass 目标方块实体的类
     * @return 特定类型的方块实体列表
     */
    public static <T extends BlockEntity> List<T> getBlockEntitiesNearPlayerByType(
            Player player, int radius, Class<T> blockEntityClass) {

        List<T> result = new ArrayList<>();
        Level level = player.level();
        BlockPos playerPos = player.blockPosition();

        int minX = playerPos.getX() - radius;
        int minY = Math.max(level.getMinBuildHeight(), playerPos.getY() - radius);
        int minZ = playerPos.getZ() - radius;
        int maxX = playerPos.getX() + radius;
        int maxY = Math.min(level.getMaxBuildHeight(), playerPos.getY() + radius);
        int maxZ = playerPos.getZ() + radius;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockEntity blockEntity = level.getBlockEntity(pos);

                    // 检查方块实体是否非空且为指定类型
                    if (blockEntity != null && blockEntityClass.isInstance(blockEntity)) {
                        result.add(blockEntityClass.cast(blockEntity));
                    }
                }
            }
        }
        return result;
    }

    /**
     * 使用谓词条件获取玩家附近的方块实体
     * @param player 玩家对象
     * @param radius 搜索半径（方块数）
     * @param condition 自定义过滤条件
     * @return 符合条件的方块实体列表
     */
    public static List<BlockEntity> getBlockEntitiesNearPlayer(
            Player player, int radius, Predicate<BlockEntity> condition) {

        List<BlockEntity> result = new ArrayList<>();
        Level level = player.level();
        BlockPos playerPos = player.blockPosition();

        int minX = playerPos.getX() - radius;
        int minY = Math.max(level.getMinBuildHeight(), playerPos.getY() - radius);
        int minZ = playerPos.getZ() - radius;
        int maxX = playerPos.getX() + radius;
        int maxY = Math.min(level.getMaxBuildHeight(), playerPos.getY() + radius);
        int maxZ = playerPos.getZ() + radius;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockEntity blockEntity = level.getBlockEntity(pos);

                    // 应用自定义条件过滤
                    if (blockEntity != null && condition.test(blockEntity)) {
                        result.add(blockEntity);
                    }
                }
            }
        }
        return result;
    }
}