package com.limingz.mymod.capability.chunkdata;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.chunk.LevelChunk;

import static com.limingz.mymod.Main.MODID;

public class ChunkData {
    // 区块是否为富营养的
    private Boolean is_nutritious;
    // 该区块实例
    private LevelChunk levelChunk;

    public ChunkData(LevelChunk levelChunk) {
        this.is_nutritious = false;
        this.levelChunk = levelChunk;
    }
    public ChunkData(LevelChunk levelChunk, Boolean is_nutritious) {
        this.is_nutritious = is_nutritious;
        this.levelChunk = levelChunk;
    }

    public Boolean get_nutritious() {
        return is_nutritious;
    }

    public void set_nutritious(Boolean is_nutritious) {
        this.is_nutritious = is_nutritious;
        // 标记为保存
        this.levelChunk.setUnsaved(true);
    }

    public void saveNBTData(CompoundTag compoundTag) {
        compoundTag.putBoolean(MODID+":is_nutritious", this.is_nutritious);
    }

    public void loadNBTData(CompoundTag compoundTag) {
        this.is_nutritious = compoundTag.getBoolean(MODID+":is_nutritious");
    }

}
