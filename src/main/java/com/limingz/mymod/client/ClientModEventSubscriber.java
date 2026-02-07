package com.limingz.mymod.client;

import com.limingz.mymod.Main;
import com.limingz.mymod.command.ClientOverlayCommand;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import java.io.IOException;

@Mod.EventBusSubscriber(modid = Main.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientModEventSubscriber {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new ChunkOverlayRenderer());
    }

    @SubscribeEvent
    public static void onKeyRegister(RegisterKeyMappingsEvent event) {
        event.register(KeyInit.TOGGLE_OVERLAY);
    }

    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(new net.minecraft.client.renderer.ShaderInstance(
                event.getResourceProvider(),
                ResourceLocation.fromNamespaceAndPath(Main.MODID, "hologram"),
                DefaultVertexFormat.BLOCK
        ), shaderInstance -> ChunkOverlayShaderManager.setHologramShader(shaderInstance));
        event.registerShader(new net.minecraft.client.renderer.ShaderInstance(
                event.getResourceProvider(),
                ResourceLocation.fromNamespaceAndPath(Main.MODID, "transition_overlay"),
                DefaultVertexFormat.BLOCK
        ), shaderInstance -> ChunkOverlayShaderManager.setTransitionShader(shaderInstance));
    }
}
