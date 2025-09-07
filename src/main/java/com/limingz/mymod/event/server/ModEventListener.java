package com.limingz.mymod.event.server;

import com.limingz.mymod.capability.chunkdata.ChunkDataProvider;
import com.limingz.mymod.capability.farmxp.PlayerFarmXpProvider;
import com.limingz.mymod.gui.overlay.FarmXpOverlay;
import com.limingz.mymod.network.Channel;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import static com.limingz.mymod.Main.*;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = MODID)
public class ModEventListener {
    @SubscribeEvent
    public static void addCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(myblock);
        }
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ticket);
        }
    }

    @SubscribeEvent
    public static void registerCapability(RegisterCapabilitiesEvent event) {
        event.register(PlayerFarmXpProvider.class);
        event.register(ChunkDataProvider.class);
    }

    @SubscribeEvent
    public static void registerOverlay(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "farm_xp_overlay", FarmXpOverlay.FARM_XP_OVERLAY);
    }

    @SubscribeEvent
    public static void registerChannel(FMLCommonSetupEvent event) {
        Channel.register();
    }
}
