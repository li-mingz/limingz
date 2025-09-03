package com.limingz.mymod.util;

import com.limingz.mymod.event.server.ForgeMinecraftServerEvent;
import net.minecraft.nbt.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.storage.LevelResource;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;

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
                    // 获取存储数据
                    long[] data = blockStates.getLongArray("data");
                    // 跳过没有数据的子区块（如空区块）
                    if (data.length==0) continue;
                    // 计算存储位数
                    int b = Math.max(log2(palette.size()), 4);
                    // 计算一个长整数（64位整数）能存储多少个元素
                    int u = 64/b;
                    for(int i=0;i<4096;i++){
                        long l = getPalette(data, i, b, u);
                        System.out.println(((CompoundTag)palette.get((int)l)).getString("Name"));
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    // 使用序号i，读取存储位数为b（长整数能存储元素的数量为u）调色板数据
    private static long getPalette(long[] data, int i, int b, int u) {
        return (data[i / u] >> ((i % u) * b)) & ((1L << b) - 1L);
    }
    private static int log2(int x) {
        return 32 - Integer.numberOfLeadingZeros(x - 1);
    }
}
