package com.limingz.mymod.block.entity.client;

import com.limingz.mymod.Main;
import com.limingz.mymod.block.entity.TestDoorBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class TestDoorBlockModel extends GeoModel<TestDoorBlockEntity> {
    @Override
    public ResourceLocation getModelResource(TestDoorBlockEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Main.MODID, "geo/testdoor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(TestDoorBlockEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/machines/testdoor.png");
    }

    @Override
    public ResourceLocation getAnimationResource(TestDoorBlockEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Main.MODID, "animations/testdoor.animation.json");
    }
}
