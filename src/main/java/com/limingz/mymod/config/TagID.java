package com.limingz.mymod.config;

import static com.limingz.mymod.Main.MODID;

public class TagID {
    public static String IsNutritiousTagName = "is_nutritious";
    public static String IsNutritiousTagID = getIDFromName(IsNutritiousTagName);

    public static String getIDFromName(String name){
        return MODID+":"+name;
    }

}
