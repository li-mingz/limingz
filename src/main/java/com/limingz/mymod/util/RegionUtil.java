package com.limingz.mymod.util;

import com.limingz.mymod.event.server.ForgeMinecraftServerEvent;
import net.minecraft.nbt.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.storage.LevelResource;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RegionUtil {
    // 获取世界存储目录
    private static File worldDir = ForgeMinecraftServerEvent.getMinecraftServer().getWorldPath(LevelResource.ROOT).toFile();
    // 获取区域文件目录
    private static File regionDir = new File(worldDir, "region");

    public static void main(){
        int chunkX = 1;
        int chunkZ = 1;
        // 根据区块坐标计算区域文件名
        int regionX = chunkX >> 5; // 除以32
        int regionZ = chunkZ >> 5;
        // 是否需要修改文件
        Boolean need_write = false;
        File regionFile = new File(regionDir, "r." + regionX + "." + regionZ + ".mca");
        // 创建区块位置对象
        ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);

        // 使用RegionFile访问区域文件
        // 读取NBT数据
        try (RegionFile region = new RegionFile(regionFile.toPath(), regionDir.toPath(), true);
             DataInputStream dis = region.getChunkDataInputStream(chunkPos)) {

            long startTime = System.nanoTime();  // 记录开始时间
            if (dis != null) {
                CompoundTag nbt = NbtIo.read(dis);
                // 处理NBT数据
                ListTag sections = nbt.getList("sections", Tag.TAG_COMPOUND);

                for (int sections_i = 0; sections_i < sections.size(); sections_i++) {
                    CompoundTag section = sections.getCompound(sections_i);
                    // 跳过没有方块状态的子区块（如空区块）
                    if (!section.contains("block_states")) continue;
                    CompoundTag blockStates = section.getCompound("block_states");
                    ListTag palette = blockStates.getList("palette", Tag.TAG_COMPOUND);
                    // 检测调色板是否有目标方块
                    String[] paletteNames = new String[palette.size()];
                    Boolean has_block = false;
                    for (int i = 0; i < palette.size(); i++) {
                        String tmp_name = palette.getCompound(i).getString("Name");
                        if(BlockReplaceList.replace_name_Map.containsKey(tmp_name)){
                            paletteNames[i] = tmp_name;
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
                                temp_index.put(compoundTag, new_palette.size());
                                new_index.put(i, new_palette.size());
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
                            temp_index.put(newBlock, new_palette.size());
                            new_index.put(i, new_palette.size());
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
                    if(palette.size() != new_palette.size()){
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
                        // 只需修改索引
                        for(int i=0;i<4096;i++){
                            int l = (int)getPalette(data, i, old_b, old_u, old_mask);
                            String paletteName = paletteNames[l];
                            // 当遍历到有目标方块的坐标时
                            if(paletteName != null){

                            }
                        }
                    }

                }
            }
            long durationNanos = System.nanoTime() - startTime;
            double durationMillis = durationNanos / 1_000_000.0;
            System.out.printf("[mymod] 遍历操作耗时: %.2f ms%n", durationMillis);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    // 使用序号i，读取存储位数为b（长整数能存储元素的数量为u）调色板数据
    private static long getPalette(long[] data, int i, int b, int u, long mask) {
        return (data[i / u] >> ((i % u) * b)) & mask;
    }
    private static int log2(int x) {
        return 32 - Integer.numberOfLeadingZeros(x - 1);
    }
}
