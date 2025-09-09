package com.limingz.mymod;

import com.limingz.mymod.block.DemoBlock;
import com.limingz.mymod.block.DeskBlock;
import com.limingz.mymod.block.SmallDoorBlock;
import com.limingz.mymod.block.entity.DemoBlockEntity;
import com.limingz.mymod.block.entity.DeskBlockEntity;
import com.limingz.mymod.capability.chunkdata.ChunkDataProvider;
import com.limingz.mymod.capability.farmxp.PlayerFarmXpProvider;
import com.limingz.mymod.config.CommonConfig;
import com.limingz.mymod.config.TagID;
import com.limingz.mymod.gui.container.DeskBlockContainerMenu;
import com.limingz.mymod.item.TicketItem;
import com.limingz.mymod.register.*;
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
import net.minecraft.world.level.chunk.LevelChunk;
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
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> GLOBAL_LOOT_MODIFIER = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MODID);
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    public Main(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        context.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BlockRegister.BLOCKS.register(modEventBus);
        BlockEntityRegister.BLOCK_ENTITY.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ItemRegister.ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        TabRegister.CREATIVE_MODE_TABS.register(modEventBus);
        GLOBAL_LOOT_MODIFIER.register(modEventBus);
        MenuRegister.MENU_TYPE.register(modEventBus);
        // 注册战利品修改器
        LootModifierRegister.LOOT_MODIFIERS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, this::attachCapabilityForEntity);
        MinecraftForge.EVENT_BUS.addGenericListener(LevelChunk.class, this::attachCapabilityForChunk);

    }

    public void attachCapabilityForEntity(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player player) {
            if (!player.getCapability(PlayerFarmXpProvider.PLAYER_FARM_XP_CAPABILITY).isPresent()) {
                event.addCapability(ResourceLocation.fromNamespaceAndPath(MODID, "farm_xp"), new PlayerFarmXpProvider());
            }
        }
    }

    public void attachCapabilityForChunk(AttachCapabilitiesEvent<LevelChunk> event) {
        LevelChunk levelChunk = event.getObject();
        if (levelChunk != null) {
            if (!levelChunk.getCapability(ChunkDataProvider.CHUNK_DATA_CAPABILITY).isPresent()) {
                event.addCapability(ResourceLocation.fromNamespaceAndPath(MODID, TagID.IsNutritiousTagName), new ChunkDataProvider(levelChunk));
            }
        }
    }

}
