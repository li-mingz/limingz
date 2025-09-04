package com.limingz.mymod.util;

import com.limingz.mymod.event.server.ForgeMinecraftServerEvent;
import net.minecraft.nbt.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.storage.LevelResource;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.limingz.mymod.Main.MODID;

public class RegionUtil {
    // 获取世界存储目录
    private static File worldDir = ForgeMinecraftServerEvent.getMinecraftServer().getWorldPath(LevelResource.ROOT).toFile();
    // 获取区域文件目录
    private static File regionDir = new File(worldDir, "region");
    // 遍历区块计数
    private static int traversalChunkCount = 0;
    // 写入区块计数
    private static int writeChunkCount = 0;

    /**
     * 遍历所有区域文件
     */
    public static String traversalAllRegionFiles() {
        traversalChunkCount = 0;
        writeChunkCount = 0;
        long startTime = System.nanoTime();  // 记录开始时间
        File[] regionFiles = regionDir.listFiles(file -> file.getName().endsWith(".mca"));
        System.out.printf("[mymod] 在 %s 中发现 %d 个区域文件%n",
                regionDir.getAbsolutePath(), regionFiles.length);

        for (File regionFile : regionFiles) {
            System.out.printf("[mymod] 处理区域文件: %s%n", regionFile.getName());
            traversalAllChunksAtRegion(regionFile);
        }
        long durationNanos = System.nanoTime() - startTime;
        double durationMillis = durationNanos / 1_000_000.0;
        System.out.printf("[mymod] 遍历区域文件操作耗时: %.2f ms%n", durationMillis);
        return String.format("[%s] [调试] [4结局事件] 共遍历 %,d 个区块，写入 %,d 个区块，耗时: %.2f ms", MODID, traversalChunkCount, writeChunkCount, durationMillis);
    }
    /*
     * 遍历指定区域内文件的所有有效区块
     */
    public static void traversalAllChunksAtRegion(File regionFile){
        // 根据区域文件名计算区域坐标
        int[] regionPos = getRegionPosInRegionFile(regionFile);
        int regionX = regionPos[0];
        int regionZ = regionPos[1];
        List<ChunkPos> chunkPosList = getChunksInRegion(regionX, regionZ);
        // 使用RegionFile访问区域文件
        // 读取NBT数据
        try (RegionFile region = new RegionFile(regionFile.toPath(), regionDir.toPath(), true)) {
            for (int chunk_i=0;chunk_i<chunkPosList.size();chunk_i++){
                // 创建区块位置对象
                ChunkPos chunkPos = chunkPosList.get(chunk_i);
                // 区块不存在则跳过
                if(!region.hasChunk(chunkPos)) continue;
                // 是否需要修改文件
                Boolean need_write = false;
                CompoundTag nbt = null;
                try(DataInputStream dis = region.getChunkDataInputStream(chunkPos)){
                    if (dis != null) {
                        nbt = NbtIo.read(dis);
                        // 处理NBT数据
                        ListTag sections = nbt.getList("sections", Tag.TAG_COMPOUND);

                        for (int sections_i = 0; sections_i < sections.size(); sections_i++) {
                            CompoundTag section = sections.getCompound(sections_i);
                            // 跳过没有方块状态的子区块（如空区块）
                            if (!section.contains("block_states")) continue;
                            CompoundTag blockStates = section.getCompound("block_states");
                            ListTag palette = blockStates.getList("palette", Tag.TAG_COMPOUND);
                            // 检测调色板是否有目标方块
                            Boolean has_block = false;
                            for (int i = 0; i < palette.size(); i++) {
                                String tmp_name = palette.getCompound(i).getString("Name");
                                if(BlockReplaceList.replace_name_Map.containsKey(tmp_name)){
                                    has_block = true;
                                }
                            }
                            // 跳过没有目标方块的子区块
                            if(!has_block) continue;
                            // 创建新调色板
                            ListTag new_palette = new ListTag();
                            // new_palette可能被打乱，需要重新建立映射关系
                            Map<Integer, Integer> new_index = new HashMap();
                            Map<CompoundTag, Integer> temp_index = new HashMap();  // 新，目标对应的索引
                            for (int i = 0; i < palette.size(); i++) {
                                String tmp_name = palette.getCompound(i).getString("Name");
                                // 查询是否为替换目标
                                if(!BlockReplaceList.replace_name_Map.containsKey(tmp_name)){
                                    // 不是替换目标则直接添加
                                    CompoundTag compoundTag = palette.getCompound(i);
                                    // 确保没有重复
                                    if(!new_palette.contains(compoundTag)){
                                        new_palette.add(compoundTag);
                                        temp_index.put(compoundTag, new_palette.size()-1);
                                        new_index.put(i, new_palette.size()-1);
                                    } else {
                                        // 重复则添加重复的索引
                                        new_index.put(i, temp_index.get(compoundTag));
                                    };
                                    continue;
                                }
                                // 为替换目标
                                CompoundTag newBlock = new CompoundTag();
                                // 查询是否需要继承Properties
                                if(BlockReplaceList.replace_properties_Map.containsKey(tmp_name)){
                                    // 获取目标方块的Properties
                                    CompoundTag tmp_properties = palette.getCompound(i).getCompound("Properties");
                                    CompoundTag newProperties = new CompoundTag();
                                    // 遍历要继承的Properties
                                    BlockReplaceList.replace_properties_Map.get(tmp_name).forEach((properties_name) -> {
                                        // 继承原方块的目标Properties
                                        newProperties.putString(properties_name, tmp_properties.getString(properties_name));
                                    });
                                    // 保存替换后的Properties
                                    newBlock.put("Properties", newProperties);
                                }
                                // 保存替换后的名称
                                newBlock.putString("Name", BlockReplaceList.replace_name_Map.get(tmp_name));
                                // 确保没有重复
                                if(!new_palette.contains(newBlock)){
                                    new_palette.add(newBlock);
                                    temp_index.put(newBlock, new_palette.size()-1);
                                    new_index.put(i, new_palette.size()-1);
                                } else {
                                    // 重复则添加重复的索引
                                    new_index.put(i, temp_index.get(newBlock));
                                }
                            }
                            // 获取存储数据
                            long[] data = blockStates.getLongArray("data");
                            // 没有数据的子区块只需更改调色板
                            if (data.length==0) {
                                blockStates.put("palette", new_palette);  // 更新调色板
                                section.put("block_states", blockStates); // 更新section
                                sections.set(sections_i, section); // 更新sections
                                need_write = true;
                                continue;
                            };

                            // 是否需要使用新索引
                            // 检测新调色板大小是否有变动（只可能会更小，不会更大）
                            if(palette.size() == new_palette.size()){
                                // 不需要使用新索引,只需更改调色板
                                blockStates.put("palette", new_palette);  // 更新调色板
                                section.put("block_states", blockStates); // 更新section
                                sections.set(sections_i, section); // 更新sections
                                need_write = true;
                                continue;
                            }

                            // 计算存储位数
                            int old_b = Math.max(log2(palette.size()), 4);
                            int new_b = Math.max(log2(new_palette.size()), 4);
                            // 计算一个长整数（64位整数）能存储多少个元素
                            int old_u = 64/old_b;
                            // 计算位掩码
                            long old_mask = (1L << old_b) - 1L;
                            // 如果修改后的调色板存储位数无需变动
                            if(old_b == new_b){
                                // 计算索引开始变动的最小索引
                                AtomicInteger min_change_index = new AtomicInteger(Integer.MAX_VALUE);
                                new_index.forEach((oldIdx, newIdx) -> {
                                    if (!oldIdx.equals(newIdx)) { // 仅当索引变化时
                                        min_change_index.set(Math.min(min_change_index.get(),Math.min(oldIdx, newIdx)));
                                    }
                                });
                                // 只需修改索引
                                for(int i=0;i<4096;i++){
                                    int l = (int)getPalette(data, i, old_b, old_u, old_mask);
                                    // 仅修改变动的索引, 提高效率
                                    if(l >= min_change_index.get()){
                                        setPalette(data, i, old_b, old_u, old_mask, new_index.get(l));
                                    }
                                }
                                blockStates.putLongArray("data", data); // 更新数据
                                // 如果修改后的调色板存储位数需要变动, 则重新构造数据
                            } else {
                                // 计算新数组长度
                                int new_u = 64/new_b;
                                long new_mask = (1L << new_b) - 1L;
                                int newLength = (int) Math.ceil(4096.0 / new_u);
                                long[] newData = new long[newLength];

                                for (int i = 0; i < 4096; i++) {
                                    long oldIndex = getPalette(data, i, old_b, old_u, old_mask);
                                    int newIndex = new_index.get((int) oldIndex);
                                    setPalette(newData, i, new_b, new_u, new_mask, newIndex);
                                }
                                blockStates.putLongArray("data", newData); // 更新数据
                            }
                            blockStates.put("palette", new_palette);  // 更新调色板
                            section.put("block_states", blockStates); // 更新section
                            sections.set(sections_i, section); // 更新sections
                            need_write = true;
                        }
                    }
                }
                if (need_write){
                    try (DataOutputStream dos = region.getChunkDataOutputStream(chunkPos)) {
                        NbtIo.write(nbt, dos);
                    }
                    writeChunkCount++;
                }
                traversalChunkCount++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * 根据区域坐标计算该区域内的所有区块坐标
     *
     * @param regionX 区域X坐标
     * @param regionZ 区域Z坐标
     * @return 区块坐标列表 (ChunkPos)
     */
    public static List<ChunkPos> getChunksInRegion(int regionX, int regionZ) {
        List<ChunkPos> chunks = new ArrayList<>();

        for (int localZ = 0; localZ < 32; localZ++) {
            for (int localX = 0; localX < 32; localX++) {
                // 计算全局区块坐标
                int chunkX = regionX * 32 + localX;
                int chunkZ = regionZ * 32 + localZ;

                chunks.add(new ChunkPos(chunkX, chunkZ));
            }
        }

        return chunks;
    }

    /**
     * 解析区域文件名中的区域坐标
     */
    private static int[] getRegionPosInRegionFile(File file) {

        // 从区域文件名解析区域坐标
        String fileName = file.getName();
        String[] parts = fileName.split("\\.");

        if (parts.length >= 3) {
            int regionX = Integer.parseInt(parts[1]);
            int regionZ = Integer.parseInt(parts[2]);
            return new int[]{regionX, regionZ};
        }
        return new int[]{};
    }
    // 使用序号i，读取存储位数为b（长整数能存储元素的数量为u）调色板数据
    private static long getPalette(long[] data, int i, int b, int u, long mask) {
        return (data[i / u] >> ((i % u) * b)) & mask;
    }
    private static void setPalette(long[] data, int i, int b, int u, long mask, long value) {
        int index = i / u;           // 计算数据在数组中的位置
        int off = (i % u) * b;       // 计算元素在长整数内的位偏移
        long clearMask = ~(mask << off); // 创建用于清除目标位的掩码
        // 先清除目标位，然后设置新的值
        data[index] = (data[index] & clearMask) | ((value & mask) << off);
    }
    private static int log2(int x) {
        return 32 - Integer.numberOfLeadingZeros(x - 1);
    }
}
