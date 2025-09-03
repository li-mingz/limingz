package com.limingz.mymod.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockReplaceList {
    // 创建替换映射
    public static Map<String, String> replace_name_Map = new HashMap<>();
    public static Map<String, List<String>> replace_properties_Map = new HashMap();
    static {
//        replace_name_Map.put("minecraft:deepslate", "minecraft:diamond_block");
        replace_name_Map.put("minecraft:rock", "minecraft:tuff");
        replace_name_Map.put("minecraft:air", "minecraft:tuff");
        replace_properties_Map.put("minecraft:deepslate", Arrays.asList("axis"));  // 继承朝向
    }
}
