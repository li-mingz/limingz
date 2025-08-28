package com.limingz.mymod.gui.holographic_ui.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UIClickPacket {
    private final BlockPos pos;

    public UIClickPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(UIClickPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
    }

    public static UIClickPacket decode(FriendlyByteBuf buf) {
        return new UIClickPacket(buf.readBlockPos());
    }

    public static void handle(UIClickPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerHandler.handleUIClick(ctx.get().getSender(), msg.pos);
        });
        ctx.get().setPacketHandled(true);
    }
}