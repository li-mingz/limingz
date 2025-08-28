package com.limingz.mymod.datagen;

import com.limingz.mymod.Main;
import net.minecraft.data.loot.packs.VanillaBlockLoot;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

public class LootTables extends VanillaBlockLoot {
    @Override
    protected Iterable<Block> getKnownBlocks() {
        return Main.BLOCKS.getEntries().stream()
                .filter(block -> block == Main.small_door)
                .flatMap(RegistryObject::stream)
                ::iterator;
    }

    public void generate() {
        dropSelf(Main.small_door.get());
    }
}
