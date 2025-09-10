package com.limingz.mymod.register;

import com.limingz.mymod.Main;
import com.limingz.mymod.block.entity.DemoBlockEntity;
import com.limingz.mymod.block.entity.DeskBlockEntity;
import com.limingz.mymod.block.entity.TestDoorBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockEntityRegister {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Main.MODID);

    public static final RegistryObject<BlockEntityType<DemoBlockEntity>> demo_block_entity =
            BLOCK_ENTITY.register("demo_block", () ->
                    BlockEntityType.Builder.of(DemoBlockEntity::new, BlockRegister.demo_block.get()).build(null));
    public static final RegistryObject<BlockEntityType<DeskBlockEntity>> desk_block_entity = BLOCK_ENTITY.register("desk_block",
            () -> BlockEntityType.Builder.of(DeskBlockEntity::new, BlockRegister.desk_block.get()).build(null));
    public static final RegistryObject<BlockEntityType<TestDoorBlockEntity>> test_door_block_entity = BLOCK_ENTITY.register("test_door_block",
            () -> BlockEntityType.Builder.of(TestDoorBlockEntity::new, BlockRegister.test_door_block.get()).build(null));
}
