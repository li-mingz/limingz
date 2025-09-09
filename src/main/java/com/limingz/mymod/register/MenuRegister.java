package com.limingz.mymod.register;

import com.limingz.mymod.Main;
import com.limingz.mymod.gui.container.DeskBlockContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MenuRegister {
    public static final DeferredRegister<MenuType<?>> MENU_TYPE = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Main.MODID);
    public static final RegistryObject<MenuType<DeskBlockContainerMenu>> desk_block_container_menu =
            MENU_TYPE.register("desk_block",
                    () -> IForgeMenuType.create(
                            (windowId, inv, data) -> new DeskBlockContainerMenu(inv, windowId, data.readBlockPos())));

}
