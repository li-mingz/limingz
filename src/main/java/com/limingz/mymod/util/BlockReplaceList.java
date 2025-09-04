package com.limingz.mymod.util;

import net.mcreator.caerulaarbor.CaerulaArborMod;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockReplaceList {
    // 创建替换映射
    public static Map<String, String> replace_name_Map = new HashMap<>();
    public static Map<String, ResourceLocation> replace_name_ResourceLocation_Map = new HashMap<>();
    public static Map<String, List<String>> replace_properties_Map = new HashMap();
    static {
        replace_name_Map.put(CaerulaArborMod.MODID+":sea_trail_init", "minecraft:air");
        replace_name_Map.put(CaerulaArborMod.MODID+":sea_trail_growing", "minecraft:air");
        replace_name_Map.put(CaerulaArborMod.MODID+":sea_trail_grown", "minecraft:air");
        replace_name_Map.put(CaerulaArborMod.MODID+":ocean_ovary", "minecraft:air");
        replace_name_Map.put(CaerulaArborMod.MODID+":red_ovary", "minecraft:air");
        replace_name_Map.put(CaerulaArborMod.MODID+":trail_mushroom", "minecraft:air");
        replace_name_Map.put(CaerulaArborMod.MODID+":sea_trail_stop", "minecraft:air");
        replace_name_Map.put(CaerulaArborMod.MODID+":sea_trail_solid", "minecraft:grass_block");
        replace_name_Map.put(CaerulaArborMod.MODID+":trail_pulse", "minecraft:grass_block");
        replace_name_Map.put(CaerulaArborMod.MODID+":trail_log", "minecraft:oak_log");
        replace_properties_Map.put(CaerulaArborMod.MODID+":trail_log", Arrays.asList("axis"));  // 继承朝向
        replace_name_Map.put(CaerulaArborMod.MODID+":trail_leave", "minecraft:oak_leaves");
        replace_properties_Map.put(CaerulaArborMod.MODID+":trail_leave", Arrays.asList("waterlogged"));  // 继承含水状态
    }
    static {
        replace_name_Map.forEach((name1, name2) -> {
            replace_name_ResourceLocation_Map.put(name1, ResourceLocation.tryParse(name2));
        });
    }
}
