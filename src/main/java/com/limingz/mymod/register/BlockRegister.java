package com.limingz.mymod.register;

import com.limingz.mymod.Main;
import com.limingz.mymod.block.DeepBlueLabAccessControlDoorBlock;
import com.limingz.mymod.block.DemoBlock;
import com.limingz.mymod.block.DeskBlock;
import com.limingz.mymod.block.SmallDoorBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockRegister {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Main.MODID);
    // Creates a new Block with the id "examplemod:example_block", combining the namespace and path
    public static final RegistryObject<Block> myblock = BLOCKS.register("myblock", () -> new Block(BlockBehaviour.Properties.of().strength(3.0f).sound(SoundType.STONE)));
    public static final RegistryObject<Block> deep_blue_lab_access_control_door = BLOCKS.register("deep_blue_lab_access_control_door", () -> new DeepBlueLabAccessControlDoorBlock(BlockBehaviour.Properties.of().strength(1.0f)));
    public static final RegistryObject<Block> small_door = BLOCKS.register("small_door_block", () -> new SmallDoorBlock(BlockBehaviour.Properties.of().strength(1.0f)));
    public static final RegistryObject<Block> desk_block = BLOCKS.register("desk_block", () -> new DeskBlock(BlockBehaviour.Properties.of().strength(1.0f)));
    public static final RegistryObject<Block> demo_block = BLOCKS.register("demo_block",
            () -> new DemoBlock(BlockBehaviour.Properties.of().strength(3.0f).sound(SoundType.STONE)));
}
