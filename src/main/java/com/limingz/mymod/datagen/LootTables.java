package com.limingz.mymod.datagen;

import com.limingz.mymod.register.BlockRegister;
import net.minecraft.data.loot.packs.VanillaBlockLoot;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

public class LootTables extends VanillaBlockLoot {
    @Override
    protected Iterable<Block> getKnownBlocks() {
        return BlockRegister.BLOCKS.getEntries().stream()
                .filter(block -> block == BlockRegister.small_door)
                .flatMap(RegistryObject::stream)
                ::iterator;
    }

    public void generate() {
        dropSelf(BlockRegister.small_door.get());
    }
}
