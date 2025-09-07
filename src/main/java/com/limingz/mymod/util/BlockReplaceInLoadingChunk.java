package com.limingz.mymod.util;

import com.google.common.collect.ImmutableMap;
import com.limingz.mymod.capability.chunkdata.ChunkDataProvider;
import com.limingz.mymod.config.BlockReplaceList;
import com.limingz.mymod.config.TagID;
import com.limingz.mymod.event.server.ForgeMinecraftServerEvent;
import com.limingz.mymod.mixins.ChunkMapMixinAccess;
import com.limingz.mymod.mixins.StateHolderMixinAccess;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

import static com.limingz.mymod.Main.MODID;

public class BlockReplaceInLoadingChunk {
    // 遍历区块计数
    private static int traversalChunkCount = 0;
    // 写入区块计数
    private static int writeChunkCount = 0;
    // 替换方块计数
    private static int replaceBlockCount = 0;
    public static String replaceBlock(){
        traversalChunkCount = 0;
        writeChunkCount = 0;
        replaceBlockCount = 0;
        long startTime = System.nanoTime();  // 记录开始时间
        ServerLevel serverLevel = ForgeMinecraftServerEvent.getMinecraftServer().getLevel(Level.OVERWORLD);
        ChunkMap chunkMap = serverLevel.getChunkSource().chunkMap;
        Long2ObjectLinkedOpenHashMap<ChunkHolder> updatingChunkMap = ((ChunkMapMixinAccess)chunkMap).getUpdatingChunkMap();
        updatingChunkMap.forEach((key, value) -> {
            ChunkAccess chunkAccess = value.getLastAvailable();
            // 跳过未完全生成的区块
            if (!(chunkAccess instanceof LevelChunk levelChunk)) return;
            LevelChunkSection[] levelChunkSections = chunkAccess.getSections();
            // 是否需要更新该区块
            Boolean needUpdate = false;
            // 遍历子区块
            for (int i=0;i<levelChunkSections.length;i++){
                LevelChunkSection section = levelChunkSections[i];
                // 检查是否有目标方块
                if(section.getStates().maybeHas(blockState -> BlockReplaceList.replace_name_Map.containsKey(ForgeRegistries.BLOCKS.getKey(blockState.getBlock()).toString()))){
                    // 有则遍历该子区块
                    // 子区块替换方块计数
                    int temp_count = 0;
                    // 遍历 x, y, z 坐标（0 到 15）
                    for (int x = 0; x < 16; x++) {
                        for (int y = 0; y < 16; y++) {
                            for (int z = 0; z < 16; z++) {
                                BlockState blockState = section.getBlockState(x, y, z);
                                String block_name = ForgeRegistries.BLOCKS.getKey(blockState.getBlock()).toString();
                                // 替换目标方块
                                if(BlockReplaceList.replace_name_Map.containsKey(block_name)){
                                    // 新方块
                                    BlockState new_blockState = ForgeRegistries.BLOCKS.getValue(
                                            BlockReplaceList.replace_name_ResourceLocation_Map.get(block_name)).defaultBlockState();
                                    // 查询是否需要继承属性
                                    if(BlockReplaceList.replace_properties_Map.containsKey(block_name)){
                                        // 遍历目标属性
                                        for(Property<?> property : blockState.getProperties()){
                                            if(BlockReplaceList.replace_properties_Map.get(block_name).contains(property.getName())){
                                                ImmutableMap<Property<?>, Comparable<?>> pValue = ((StateHolderMixinAccess)new_blockState).getValues();
                                                Comparable<?> comparable = pValue.get(property);
                                                if (comparable == null) {
                                                    throw new IllegalArgumentException("属性不存在，无法设置");
                                                } else if (!comparable.equals(blockState.getValue(property))) {
                                                    // 属性不一致，修改
                                                    new_blockState = (BlockState) ((StateHolderMixinAccess)new_blockState).getNeighbours().get(property, blockState.getValue(property));
                                                }
                                            }
                                        }
                                    }
                                    section.setBlockState(x, y, z, new_blockState);
                                    temp_count++;
                                }
                            }
                        }
                    }
                    // 为有效替换才保存
                    if(temp_count>0){
                        replaceBlockCount+=temp_count;
                        needUpdate = true;
                    }
                }
            }
            if(needUpdate){
                // 修改区块Capability
                levelChunk.getCapability(ChunkDataProvider.CHUNK_DATA_CAPABILITY).ifPresent((data) -> {
                    data.set_nutritious(true);
                });
                chunkAccess.setLightCorrect(false);    // 标记高度图需重新计算
                chunkAccess.setUnsaved(true);
                // 向玩家发送区块更新
                updatePlayersForChunk(serverLevel, levelChunk, chunkAccess.getPos());
                writeChunkCount++;
            }
            traversalChunkCount++;
        });
        long durationNanos = System.nanoTime() - startTime;
        double durationMillis = durationNanos / 1_000_000.0;
        System.out.printf("[mymod] 遍历已加载区块操作耗时: %.2f ms%n", durationMillis);
        return String.format("[%s] [调试] [4结局事件] 在已加载区块中共遍历 %,d 个区块，写入 %,d 个区块，替换 %,d 个方块，耗时: %.2f ms", MODID, traversalChunkCount, writeChunkCount, replaceBlockCount, durationMillis);
    }

    private static void updatePlayersForChunk(ServerLevel level, LevelChunk chunk, ChunkPos chunkPos) {
        List<ServerPlayer> players = level.getChunkSource().chunkMap.getPlayers(chunkPos, false);
        if (players.isEmpty()) return;

        // 1.20.1 使用 ClientboundLevelChunkWithLightPacket 发送区块和光照数据
        ClientboundLevelChunkWithLightPacket packet = new ClientboundLevelChunkWithLightPacket(
                chunk,
                level.getLightEngine(),
                null,  // 子区块掩码 - null 表示所有子区块
                null   // 光照掩码 - null 表示所有光照
        );

        for (ServerPlayer player : players) {
            // 先发送区块卸载包
            player.connection.send(new ClientboundForgetLevelChunkPacket(chunkPos.x, chunkPos.z));

            // 再发送包含更新内容的区块包
            player.connection.send(packet);
        }
    }
}
