package com.limingz.mymod.util;

import com.limingz.mymod.event.server.ForgeMinecraftServerEvent;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.mcreator.caerulaarbor.init.CaerulaArborModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class BlockReplacer {
    private static LongSet base_chunk_set;  // 原本已加载的区块
    private static LongSet chunk_set; // 当前加载的区块
    private static ServerLevel serverLevel;
    private static int[] lastLoadedChunk;  // 上次加载的区块
    private static long long_lastLoadedChunk;  // 上次加载的区块(long数据格式)
    public static void init(){
        serverLevel = ForgeMinecraftServerEvent.getMinecraftServer().getLevel(Level.OVERWORLD);
        base_chunk_set = serverLevel.getForcedChunks();
        chunk_set = new LongOpenHashSet(base_chunk_set);
        lastLoadedChunk = null;
    }
    public static void processData(int x, int y, int z, String name, int chunk_x, int chunk_y) {
        long chunk = ChunkPos.asLong(chunk_x, chunk_y);
        // 区块未加载时加载该区块
        if(!chunk_set.contains(chunk)){
            chunk_set.add(chunk);
            serverLevel.setChunkForced(chunk_x, chunk_y, true);
            lastLoadedChunk = new int[]{chunk_x, chunk_y};
            long_lastLoadedChunk = chunk;
        };
        // 区块内方块替换完毕后卸载该区块, 原本已经加载的区块不用卸载
        if(lastLoadedChunk != null && long_lastLoadedChunk != chunk && !base_chunk_set.contains(long_lastLoadedChunk)){
            serverLevel.setChunkForced(lastLoadedChunk[0], lastLoadedChunk[1], false);
            chunk_set.remove(long_lastLoadedChunk);
        }
        BlockPos blockPos = new BlockPos(x, y, z);
        // 检测该位置方块是否正确
        if(serverLevel.getBlockState(blockPos).getBlock().getDescriptionId().equals(name)){
            serverLevel.setBlock(blockPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            System.out.println("[SQLite] 有效遍历: X=" + x + ", Y=" + y + ", Z=" + z + ", Name=" + name + ", Chunk_X=" + chunk_x + ", Chunk_Y=" + chunk_y);
        }

    }
    public static void endProcessData(){
        // 卸载最后一个区块
        // 原本已经加载的区块不用卸载
        if(lastLoadedChunk != null && !base_chunk_set.contains(long_lastLoadedChunk)){
            serverLevel.setChunkForced(lastLoadedChunk[0], lastLoadedChunk[1], false);
        }
        base_chunk_set = null;
        chunk_set = null;
        serverLevel = null;
        lastLoadedChunk  = null;
    }
}
