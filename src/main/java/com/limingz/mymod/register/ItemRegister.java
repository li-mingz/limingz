package com.limingz.mymod.register;

import com.limingz.mymod.Main;
import com.limingz.mymod.item.TicketItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemRegister {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Main.MODID);
    public static final RegistryObject<Item> demo_block_item = ITEMS.register("demo_block", () -> new BlockItem(BlockRegister.demo_block.get(), new Item.Properties()));
    public static final RegistryObject<Item> my_block_item = ITEMS.register("myblock", () -> new BlockItem(BlockRegister.myblock.get(), new Item.Properties()));
    public static final RegistryObject<Item> small_door_item = ITEMS.register("small_door_block", () -> new BlockItem(BlockRegister.small_door.get(), new Item.Properties()));
    public static final RegistryObject<Item> desk_block_item = ITEMS.register("desk_block", () -> new BlockItem(BlockRegister.desk_block.get(), new Item.Properties()));
    public static final RegistryObject<Item> ticket = ITEMS.register("ticket", () -> new TicketItem(new Item.Properties()));
}
