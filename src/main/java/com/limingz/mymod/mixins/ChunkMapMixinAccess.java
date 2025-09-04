package com.limingz.mymod.mixins;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkMap.class)
public interface ChunkMapMixinAccess{
    @Accessor
    Long2ObjectLinkedOpenHashMap<ChunkHolder> getUpdatingChunkMap();
}
