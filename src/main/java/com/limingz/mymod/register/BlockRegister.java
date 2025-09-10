package com.limingz.mymod.register;

import com.limingz.mymod.Main;
import com.limingz.mymod.block.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockRegister {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Main.MODID);

    public static final RegistryObject<Block> myblock = BLOCKS.register("myblock", () -> new Block(BlockBehaviour.Properties.of().strength(3.0f).sound(SoundType.STONE)));
    public static final RegistryObject<Block> small_door = BLOCKS.register("small_door_block", () -> new SmallDoorBlock(BlockBehaviour.Properties.of().strength(1.0f)));
    public static final RegistryObject<Block> desk_block = BLOCKS.register("desk_block", () -> new DeskBlock(BlockBehaviour.Properties.of().strength(1.0f)));
    public static final RegistryObject<Block> demo_block = BLOCKS.register("demo_block",
            () -> new DemoBlock(BlockBehaviour.Properties.of().strength(3.0f).sound(SoundType.STONE)));
    public static final RegistryObject<Block> test_door_block = BLOCKS.register("test_door_block",
            () -> new TestDoorBlock(BlockBehaviour.Properties.of().noCollission()));
}
