package com.limingz.mymod.register;

import com.limingz.mymod.loot.CropsModifier;
import com.mojang.serialization.Codec;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.limingz.mymod.Main.MODID;

public class LootModifierRegister {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MODID);

    public static final RegistryObject<Codec<CropsModifier>> CROPS_MODIFIER =
            LOOT_MODIFIERS.register("crops_modifier", () -> CropsModifier.CODEC);
}
