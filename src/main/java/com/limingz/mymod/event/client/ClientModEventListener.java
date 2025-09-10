package com.limingz.mymod.event.client;

import com.limingz.mymod.block.entity.client.TestDoorBlockRenderer;
import com.limingz.mymod.gui.holographic_ui.renderer.blockentity.DemoBlockEntityRenderer;
import com.limingz.mymod.gui.screen.DeskBlockContainerScreen;
import com.limingz.mymod.register.BlockEntityRegister;
import com.limingz.mymod.register.MenuRegister;
import com.limingz.mymod.renderer.blockentity.DeskBlockEntityRenderer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static com.limingz.mymod.Main.*;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEventListener {
    @SubscribeEvent
    public static void registerBlockEntityRenderer(EntityRenderersEvent.RegisterRenderers event){
        event.registerBlockEntityRenderer(BlockEntityRegister.desk_block_entity.get(), DeskBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegister.demo_block_entity.get(), DemoBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegister.test_door_block_entity.get(), TestDoorBlockRenderer::new);
    }
    @SubscribeEvent
    public static void registerScreen(FMLClientSetupEvent event){
        event.enqueueWork(() -> MenuScreens.register(MenuRegister.desk_block_container_menu.get(), DeskBlockContainerScreen::new));
    }
}
