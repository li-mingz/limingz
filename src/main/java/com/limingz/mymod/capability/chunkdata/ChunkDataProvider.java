package com.limingz.mymod.capability.chunkdata;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChunkDataProvider implements ICapabilityProvider, INBTSerializable {
    public static final Capability<ChunkData> CHUNK_DATA_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });
    private ChunkData chunkData;
    private final LazyOptional<ChunkData> lazyOptional = LazyOptional.of(() -> this.chunkData);

    public ChunkDataProvider(LevelChunk levelChunk) {
        chunkData = new ChunkData(levelChunk);
    }

    @Override
    public Tag serializeNBT() {
        var tag = new CompoundTag();
        chunkData.saveNBTData(tag);
        return tag;
    }

    @Override
    public void deserializeNBT(Tag nbt) {
        chunkData.loadNBTData((CompoundTag) nbt);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return getCapability(cap);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
        if (cap == CHUNK_DATA_CAPABILITY) {
            return lazyOptional.cast();
        }
        return LazyOptional.empty();
    }
}
