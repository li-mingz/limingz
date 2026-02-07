package com.limingz.mymod.network.packet.playertoserver;

import com.limingz.mymod.client.ChunkOverlayManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestTeleportPacket {
    private final ChunkPos target;
    private final String dimension;
    private final double targetY;

    public RequestTeleportPacket(ChunkPos target, String dimension, double targetY) {
        this.target = target;
        this.dimension = dimension;
        this.targetY = targetY;
    }

    public RequestTeleportPacket(FriendlyByteBuf buf) {
        this.target = buf.readChunkPos();
        if (buf.readBoolean()) {
            this.dimension = buf.readUtf();
        } else {
            this.dimension = null;
        }
        this.targetY = buf.readDouble();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeChunkPos(target);
        if (dimension != null) {
            buf.writeBoolean(true);
            buf.writeUtf(dimension);
        } else {
            buf.writeBoolean(false);
        }
        buf.writeDouble(targetY);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null) return;

            ServerLevel level = player.serverLevel();
            if (this.dimension != null) {
                ResourceLocation dimLoc = ResourceLocation.tryParse(this.dimension);
                if (dimLoc != null) {
                    ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION, dimLoc);
                    ServerLevel targetLevel = player.server.getLevel(dimKey);
                    if (targetLevel != null) {
                        level = targetLevel;
                    }
                }
            }

            // Teleport the player
            // Center of the chunk
            double x = target.x * 16 + 8.0;
            double z = target.z * 16 + 8.0;

            // Use provided targetY if valid ( > -999)
            double y = (targetY > -999) ? targetY : player.getY();

            if (level != player.serverLevel()) {
                player.teleportTo(level, x, y, z, player.getYRot(), player.getXRot());
            } else {
                player.connection.teleport(x, y, z, player.getYRot(), player.getXRot());
            }

            System.out.println("[Debug] Teleported " + player.getName().getString() + " to " + x + ", " + y + ", " + z + " in " + level.dimension().location());
        });
        context.get().setPacketHandled(true);
    }
}
