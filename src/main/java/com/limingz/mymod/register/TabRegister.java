package com.limingz.mymod.register;

import com.limingz.mymod.Main;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class TabRegister {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Main.MODID);
    public static final RegistryObject<CreativeModeTab> mytab = CREATIVE_MODE_TABS.register("mytab", () -> CreativeModeTab.builder()
            .title(Component.translatable("mymod"))
            .icon(() -> new ItemStack(ItemRegister.ticket.get()))
            .displayItems((parm, output) -> {
                output.accept(ItemRegister.my_block_item.get());
                output.accept(ItemRegister.ticket.get());
                output.accept(ItemRegister.small_door_item.get());
                output.accept(ItemRegister.deep_blue_lab_access_control_door_item.get());
                output.accept(ItemRegister.desk_block_item.get());
                output.accept(ItemRegister.demo_block_item.get());
            })
            .build());
}
