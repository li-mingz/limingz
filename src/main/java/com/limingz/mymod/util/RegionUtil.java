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

                for (Tag sectionTag : sections) {
                    CompoundTag section = (CompoundTag) sectionTag;
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
                    for (int i = 0; i < palette.size(); i++) {
                        String tmp_name = palette.getCompound(i).getString("Name");
                        // 查询是否为替换目标
                        if(!BlockReplaceList.replace_name_Map.containsKey(tmp_name)){
                            CompoundTag compoundTag = palette.getCompound(i);
                            // 确保没有重复
                            if(!new_palette.contains(compoundTag)){
                                new_palette.add(compoundTag);
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
                            // 遍历要继承的属性
                            BlockReplaceList.replace_properties_Map.get(tmp_name).forEach((properties_name) -> {
                                newProperties.putString(properties_name, tmp_properties.getString(properties_name));
                            });
                            newBlock.put("Properties", newProperties);
                        }
                        newBlock.putString("Name", tmp_name);
                        // 确保没有重复
                        if(!new_palette.contains(newBlock)){
                            new_palette.add(newBlock);
                        };
                    }



                    // 获取存储数据
                    long[] data = blockStates.getLongArray("data");
                    // 跳过没有数据的子区块（如空区块）
                    if (data.length==0) continue;
                    // 计算存储位数
                    int b = Math.max(log2(palette.size()), 4);
                    // 计算一个长整数（64位整数）能存储多少个元素
                    int u = 64/b;
                    // 计算位掩码
                    long mask = (1L << b) - 1L;
                    for(int i=0;i<4096;i++){
                        int l = (int)getPalette(data, i, b, u, mask);
                        String paletteName = paletteNames[l];
                        // 当遍历到有目标方块的坐标时
                        if(paletteName != null){

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
