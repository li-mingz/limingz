package com.limingz.mymod.datagen;

import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.GlobalLootModifierProvider;

public class GlobalLootModifier extends GlobalLootModifierProvider {
    public GlobalLootModifier(PackOutput output, String modid) {
        super(output, modid);
    }

    @Override
    protected void start() {
    }
}
