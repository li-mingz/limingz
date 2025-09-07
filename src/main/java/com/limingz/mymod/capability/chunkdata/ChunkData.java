package com.limingz.mymod.capability.chunkdata;

import net.minecraft.nbt.CompoundTag;

import static com.limingz.mymod.Main.MODID;

public class ChunkData {
    // 区块是否为富营养的
    private Boolean is_nutritious;

    public ChunkData() {
        this.is_nutritious = false;
    }
    public ChunkData(Boolean is_nutritious) {
        this.is_nutritious = is_nutritious;
    }

    public Boolean get_nutritious() {
        return is_nutritious;
    }

    public void set_nutritious(Boolean is_nutritious) {
        this.is_nutritious = is_nutritious;
    }

    public void saveNBTData(CompoundTag compoundTag) {
        compoundTag.putBoolean(MODID+":is_nutritious", this.is_nutritious);
    }

    public void loadNBTData(CompoundTag compoundTag) {
        this.is_nutritious = compoundTag.getBoolean(MODID+":is_nutritious");
    }

}
