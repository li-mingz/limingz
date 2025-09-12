package com.limingz.mymod.network;

import com.limingz.mymod.network.packet.playertoserver.DoorTickPacket;
import com.limingz.mymod.network.packet.servertoplayer.FarmXpPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import static com.limingz.mymod.Main.MODID;

public class Channel {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    private static int id = 0;

    public static void register() {
        INSTANCE.messageBuilder(FarmXpPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(FarmXpPacket::new)
                .encoder(FarmXpPacket::encode)
                .consumerMainThread(FarmXpPacket::handle)
                .add();
        INSTANCE.messageBuilder(DoorTickPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(DoorTickPacket::decode)
                .encoder(DoorTickPacket::encode)
                .consumerMainThread(DoorTickPacket::handle)
                .add();
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
