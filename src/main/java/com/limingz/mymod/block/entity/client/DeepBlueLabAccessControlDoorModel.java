package com.limingz.mymod.block.entity.client;

import com.limingz.mymod.Main;
import com.limingz.mymod.block.entity.DeepBlueLabAccessControlDoorEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DeepBlueLabAccessControlDoorModel extends GeoModel<DeepBlueLabAccessControlDoorEntity> {
    @Override
    public ResourceLocation getModelResource(DeepBlueLabAccessControlDoorEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Main.MODID, "geo/deep_blue_lab_access_control_door.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DeepBlueLabAccessControlDoorEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/machines/deep_blue_lab_access_control_door.png");
    }

    @Override
    public ResourceLocation getAnimationResource(DeepBlueLabAccessControlDoorEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Main.MODID, "animations/deep_blue_lab_access_control_door.animation.json");
    }
}
