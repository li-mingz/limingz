package com.limingz.mymod.register;

import com.limingz.mymod.Main;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSoundRegister {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Main.MODID);

    public static final RegistryObject<SoundEvent> DEEP_BLUE_LAB_ACCESS_CONTROL_DOOR_OPEN =
            registerSoundEvent("block.deep_blue_lab_access_control_door.open");

    private static RegistryObject<SoundEvent> registerSoundEvent(String soundName) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Main.MODID, soundName);
        // 注册 SoundEvent 实例
        return SOUND_EVENTS.register(soundName, () -> SoundEvent.createVariableRangeEvent(id));
    }
}
