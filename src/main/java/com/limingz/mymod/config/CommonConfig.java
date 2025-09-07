package com.limingz.mymod.config;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.limingz.mymod.Main.MODID;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = MODID)
public class CommonConfig {
    public static Set<Block> block_usexp_set, block_getxp_set;
    public static int xp_increase, xp_decrease;
    private static ForgeConfigSpec.Builder BUIDER = new ForgeConfigSpec.Builder();
    private static ForgeConfigSpec.IntValue XPINCREASE = BUIDER.defineInRange("xp_increase", 1, 0, Integer.MAX_VALUE);
    private static ForgeConfigSpec.IntValue XPDECREASE = BUIDER.defineInRange("xp_decrease", 1, 0, Integer.MAX_VALUE);
    private static ForgeConfigSpec.ConfigValue<List<? extends String>> BLOCK_USE_XP_LIST = BUIDER.defineListAllowEmpty("block_use_xp_list", List.of(), CommonConfig::allow);
    private static ForgeConfigSpec.ConfigValue<List<? extends String>> BLOCK_GET_XP_LIST = BUIDER.defineListAllowEmpty("block_get_xp_list", List.of(), CommonConfig::allow);
    public static ForgeConfigSpec SPEC = BUIDER.build();
    
    private static boolean allow(Object obj) {
        var loc = ResourceLocation.tryParse((String) obj);
        return loc != null && ForgeRegistries.BLOCKS.containsKey(loc);
    }

    public static void getConfig() {
        xp_increase = XPINCREASE.get();
        xp_decrease = XPDECREASE.get();
            block_usexp_set = BLOCK_USE_XP_LIST.get().stream()
                    .map(ResourceLocation::tryParse)
                    .map(ForgeRegistries.BLOCKS::getValue)
                    .collect(Collectors.toSet());
            block_getxp_set = BLOCK_GET_XP_LIST.get().stream()
                    .map(ResourceLocation::tryParse)
                    .map(ForgeRegistries.BLOCKS::getValue)
                    .collect(Collectors.toSet());
    }

    @SubscribeEvent
    public static void onSetup(FMLCommonSetupEvent event) {
        getConfig();
    }

    @SubscribeEvent
    public static void onLoad(ModConfigEvent.Reloading event) {
        getConfig();
    }
}
