package com.limingz.mymod;

import com.limingz.mymod.block.DemoBlock;
import com.limingz.mymod.block.DeskBlock;
import com.limingz.mymod.block.SmallDoorBlock;
import com.limingz.mymod.block.entity.DemoBlockEntity;
import com.limingz.mymod.block.entity.DeskBlockEntity;
import com.limingz.mymod.capability.farmxp.PlayerFarmXpProvider;
import com.limingz.mymod.config.CommonConfig;
import com.limingz.mymod.gui.container.DeskBlockContainerMenu;
import com.limingz.mymod.item.TicketItem;
import com.limingz.mymod.register.LootModifierRegister;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Main.MODID)
public class Main {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "mymod";
    // Create a Deferred Register to hold Blocks which will all be registered under the "examplemod" namespace
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    // Creates a new Block with the id "examplemod:example_block", combining the namespace and path
    public static final RegistryObject<Block> myblock = BLOCKS.register("myblock", () -> new Block(BlockBehaviour.Properties.of().strength(3.0f).sound(SoundType.STONE)));
    public static final RegistryObject<Block> small_door = BLOCKS.register("small_door_block", () -> new SmallDoorBlock(BlockBehaviour.Properties.of().strength(1.0f)));
    public static final RegistryObject<Block> desk_block = BLOCKS.register("desk_block", () -> new DeskBlock(BlockBehaviour.Properties.of().strength(1.0f)));
    public static final RegistryObject<Block> demo_block = BLOCKS.register("demo_block",
            () -> new DemoBlock(BlockBehaviour.Properties.of().strength(3.0f).sound(SoundType.STONE)));
    // Create a Deferred Register to hold Items which will all be registered under the "examplemod" namespace
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);

    public static final RegistryObject<BlockEntityType<DemoBlockEntity>> demo_block_entity =
            BLOCK_ENTITY.register("demo_block", () ->
                    BlockEntityType.Builder.of(DemoBlockEntity::new, demo_block.get()).build(null));
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    // Creates a new BlockItem with the id "examplemod:example_block", combining the namespace and path
    public static final RegistryObject<Item> demo_block_item = ITEMS.register("demo_block", () -> new BlockItem(demo_block.get(), new Item.Properties()));
    public static final RegistryObject<Item> my_block_item = ITEMS.register("myblock", () -> new BlockItem(myblock.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<DeskBlockEntity>> desk_block_entity = BLOCK_ENTITY.register("desk_block", () -> BlockEntityType.Builder.of(DeskBlockEntity::new, desk_block.get()).build(null));
    public static final RegistryObject<Item> small_door_item = ITEMS.register("small_door_block", () -> new BlockItem(small_door.get(), new Item.Properties()));
    public static final RegistryObject<Item> desk_block_item = ITEMS.register("desk_block", () -> new BlockItem(desk_block.get(), new Item.Properties()));
    public static final RegistryObject<Item> ticket = ITEMS.register("ticket", () -> new TicketItem(new Item.Properties()));
    public static final DeferredRegister<MenuType<?>> MENU_TYPE = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);
    public static final RegistryObject<MenuType<DeskBlockContainerMenu>> desk_block_container_menu =
            MENU_TYPE.register("desk_block",
                    () -> IForgeMenuType.create(
                            (windowId, inv, data) -> new DeskBlockContainerMenu(inv, windowId, data.readBlockPos())));
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "examplemod" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final RegistryObject<CreativeModeTab> mytab = CREATIVE_MODE_TABS.register("mytab", () -> CreativeModeTab.builder()
            .title(Component.translatable("mymod"))
            .icon(() -> new ItemStack(ticket.get()))
            .displayItems((parm, output) -> {
                output.accept(my_block_item.get());
                output.accept(ticket.get());
                output.accept(small_door_item.get());
                output.accept(desk_block_item.get());
                output.accept(demo_block_item.get());
            })
            .build());
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> GLOBAL_LOOT_MODIFIER = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MODID);
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    public Main(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        context.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        BLOCK_ENTITY.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);
        GLOBAL_LOOT_MODIFIER.register(modEventBus);
        MENU_TYPE.register(modEventBus);
        // 注册战利品修改器
        LootModifierRegister.LOOT_MODIFIERS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, this::attachCapability);

    }

    public void attachCapability(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player player) {
            if (!player.getCapability(PlayerFarmXpProvider.PLAYER_FARM_XP_CAPABILITY).isPresent()) {
                event.addCapability(ResourceLocation.fromNamespaceAndPath(MODID, "farm_xp"), new PlayerFarmXpProvider());
            }
        }
    }

}
