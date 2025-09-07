package com.limingz.mymod.datagen;

import com.limingz.mymod.loot.CropsModifier;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;

public class GlobalLootModifier extends GlobalLootModifierProvider {
    public GlobalLootModifier(PackOutput output, String modid) {
        super(output, modid);
    }

    @Override
    protected void start() {
        add("crops_modifier", new CropsModifier(
                new LootItemCondition[]{
                }
        ));
    }
}