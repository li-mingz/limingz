package com.limingz.mymod.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class BlockReplaceList {
    // 创建替换映射
    public static Map<String, String> replace_name_Map = new HashMap<>();
    public static Map<String, ResourceLocation> replace_name_ResourceLocation_Map = new HashMap<>();
    public static Map<String, List<String>> replace_properties_Map = new HashMap();
    public static Predicate<BlockState> blockStatePredicate = new Predicate<BlockState>() {
        @Override
        public boolean test(BlockState blockState) {
            return false;
        }
    };
    static {
        replace_name_Map.put("minecraft:deepslate", "minecraft:oak_log");
        replace_properties_Map.put("minecraft:deepslate", Arrays.asList("axis"));  // 继承朝向
    }
    static {
        replace_name_Map.forEach((name1, name2) -> {
            replace_name_ResourceLocation_Map.put(name1, ResourceLocation.tryParse(name2));
        });
    }
}
