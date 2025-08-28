package com.limingz.mymod.task;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * 高效的已加载区块方块替换器
 * 用于测试目的，只替换已加载区块中的方块
 */
public class LoadedChunkBlockReplacer {
    private final Level level;
    private final Block targetBlock;
    private final BlockState replacement;
    private final List<ChunkPos> chunksToProcess;
    private int currentChunkIndex = 0;
    private boolean isActive = false;
    private boolean isComplete = false;

    // 用于性能统计
    private int totalBlocksReplaced = 0;
    private long startTime = 0;

    public LoadedChunkBlockReplacer(Level level, Block targetBlock, BlockState replacement) {
        this.level = level;
        this.targetBlock = targetBlock;
        this.replacement = replacement;
        this.chunksToProcess = new ArrayList<>();

        // 收集所有已加载的区块
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel) level;

            // 使用更兼容的方法获取已加载的区块
            // 遍历所有可能的区块坐标，检查是否已加载
            int maxRadius = 8; // 假设最大加载半径为8个区块（128格）

            // 获取玩家位置作为中心点（如果没有玩家，使用世界出生点）
            BlockPos centerPos = serverLevel.getSharedSpawnPos();

            // 计算区块坐标范围
            int centerChunkX = centerPos.getX() >> 4;
            int centerChunkZ = centerPos.getZ() >> 4;

            for (int dx = -maxRadius; dx <= maxRadius; dx++) {
                for (int dz = -maxRadius; dz <= maxRadius; dz++) {
                    int chunkX = centerChunkX + dx;
                    int chunkZ = centerChunkZ + dz;

                    if (serverLevel.hasChunk(chunkX, chunkZ)) {
                        chunksToProcess.add(new ChunkPos(chunkX, chunkZ));
                    }
                }
            }
        }
    }

    public void start() {
        if (chunksToProcess.isEmpty() || isActive) {
            return;
        }

        isActive = true;
        isComplete = false;
        totalBlocksReplaced = 0;
        startTime = System.currentTimeMillis();

        // 注册到事件总线
        MinecraftForge.EVENT_BUS.register(this);

        System.out.println("开始替换已加载区块中的方块: " + targetBlock.getName().getString() +
                " -> " + replacement.getBlock().getName().getString());
        System.out.println("需要处理的区块数量: " + chunksToProcess.size());
    }

    public void stop() {
        isActive = false;
        MinecraftForge.EVENT_BUS.unregister(this);

        long elapsedTime = System.currentTimeMillis() - startTime;
        System.out.println("替换操作已停止。已替换 " + totalBlocksReplaced + " 个方块，耗时 " + elapsedTime + "ms");
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public int getProgress() {
        if (chunksToProcess.isEmpty()) {
            return 100;
        }
        return (currentChunkIndex * 100) / chunksToProcess.size();
    }

    public int getTotalChunks() {
        return chunksToProcess.size();
    }

    public int getProcessedChunks() {
        return currentChunkIndex;
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (!isActive || event.phase != TickEvent.Phase.END) {
            return;
        }

        // 每帧处理一个区块，避免卡顿
        if (currentChunkIndex >= chunksToProcess.size()) {
            complete();
            return;
        }

        ChunkPos chunkPos = chunksToProcess.get(currentChunkIndex);
        processChunk(chunkPos);
        currentChunkIndex++;

        // 每处理10个区块输出一次进度
        if (currentChunkIndex % 10 == 0 || currentChunkIndex == chunksToProcess.size()) {
            System.out.println("处理进度: " + getProgress() + "% (" + currentChunkIndex + "/" + chunksToProcess.size() + ")");
        }
    }

    private void processChunk(ChunkPos chunkPos) {
        if (!level.hasChunk(chunkPos.x, chunkPos.z)) {
            return; // 区块已卸载，跳过
        }

        LevelChunk chunk = level.getChunk(chunkPos.x, chunkPos.z);
        int chunkBlocksReplaced = 0;

        // 遍历区块内的所有方块位置
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y++) {
                    BlockPos pos = new BlockPos(
                            chunkPos.getMinBlockX() + x,
                            y,
                            chunkPos.getMinBlockZ() + z
                    );

                    BlockState state = chunk.getBlockState(pos);
                    if (state.is(targetBlock)) {
                        level.setBlock(pos, replacement, 3);
                        chunkBlocksReplaced++;
                        totalBlocksReplaced++;
                    }
                }
            }
        }

        // 输出区块处理结果
        if (chunkBlocksReplaced > 0) {
            System.out.println("区块 [" + chunkPos.x + ", " + chunkPos.z + "] 替换了 " + chunkBlocksReplaced + " 个方块");
        }
    }

    private void complete() {
        isActive = false;
        isComplete = true;
        MinecraftForge.EVENT_BUS.unregister(this);

        long elapsedTime = System.currentTimeMillis() - startTime;
        System.out.println("替换完成! 总共替换了 " + totalBlocksReplaced + " 个方块，耗时 " + elapsedTime + "ms");
    }

    /**
     * 使用更高效的方法替换已加载区块中的方块
     */
    public static int replaceInLoadedChunksEfficient(ServerLevel level, Block targetBlock, BlockState replacement) {
        int totalReplaced = 0;
        int maxRadius = 8; // 假设最大加载半径为8个区块

        // 获取中心点
        BlockPos centerPos = level.getSharedSpawnPos();
        int centerChunkX = centerPos.getX() >> 4;
        int centerChunkZ = centerPos.getZ() >> 4;

        // 遍历所有可能的区块
        for (int dx = -maxRadius; dx <= maxRadius; dx++) {
            for (int dz = -maxRadius; dz <= maxRadius; dz++) {
                int chunkX = centerChunkX + dx;
                int chunkZ = centerChunkZ + dz;

                if (level.hasChunk(chunkX, chunkZ)) {
                    LevelChunk chunk = level.getChunk(chunkX, chunkZ);
                    totalReplaced += replaceInChunk(chunk, targetBlock, replacement);
                }
            }
        }

        return totalReplaced;
    }

    /**
     * 在单个区块中替换方块
     */
    private static int replaceInChunk(LevelChunk chunk, Block targetBlock, BlockState replacement) {
        int replaced = 0;
        Level level = chunk.getLevel();

        // 遍历区块的所有Y坐标
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y++) {
                    BlockPos pos = new BlockPos(
                            chunk.getPos().getMinBlockX() + x,
                            y,
                            chunk.getPos().getMinBlockZ() + z
                    );

                    BlockState state = chunk.getBlockState(pos);
                    if (state.is(targetBlock)) {
                        level.setBlock(pos, replacement, 3);
                        replaced++;
                    }
                }
            }
        }

        return replaced;
    }

    /**
     * 使用Forge的ChunkEvent.Load事件跟踪已加载区块
     * 需要在mod主类中注册事件监听器
     */
    public static class ChunkLoadTracker {
        private static final List<ChunkPos> loadedChunks = new ArrayList<>();

        @SubscribeEvent
        public static void onChunkLoad(net.minecraftforge.event.level.ChunkEvent.Load event) {
            if (event.getLevel() instanceof ServerLevel) {
                ChunkPos chunkPos = event.getChunk().getPos();
                if (!loadedChunks.contains(chunkPos)) {
                    loadedChunks.add(chunkPos);
                }
            }
        }

        @SubscribeEvent
        public static void onChunkUnload(net.minecraftforge.event.level.ChunkEvent.Unload event) {
            if (event.getLevel() instanceof ServerLevel) {
                ChunkPos chunkPos = event.getChunk().getPos();
                loadedChunks.remove(chunkPos);
            }
        }

        public static List<ChunkPos> getLoadedChunks() {
            return new ArrayList<>(loadedChunks);
        }

        public static void clear() {
            loadedChunks.clear();
        }
    }

    /**
     * 使用ChunkLoadTracker获取已加载区块的替换器
     */
    public static LoadedChunkBlockReplacer createWithTrackedChunks(Level level, Block targetBlock, BlockState replacement) {
        LoadedChunkBlockReplacer replacer = new LoadedChunkBlockReplacer(level, targetBlock, replacement);
        replacer.chunksToProcess.clear();
        replacer.chunksToProcess.addAll(ChunkLoadTracker.getLoadedChunks());
        return replacer;
    }
}