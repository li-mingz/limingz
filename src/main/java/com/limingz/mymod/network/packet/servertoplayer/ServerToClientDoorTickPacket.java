package com.limingz.mymod.network.packet.servertoplayer;

import com.limingz.mymod.block.entity.DeepBlueLabAccessControlDoorEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerToClientDoorTickPacket {
    private final BlockPos blockPos;
    private final Double animationTick;
    private final DeepBlueLabAccessControlDoorEntity.DoorState doorState;

    public ServerToClientDoorTickPacket(BlockPos blockPos, Double animationTick, DeepBlueLabAccessControlDoorEntity.DoorState doorState) {
        this.blockPos = blockPos;
        this.animationTick = animationTick;
        this.doorState = doorState;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
        buf.writeDouble(animationTick);
        buf.writeEnum(doorState);
    }

    public static ServerToClientDoorTickPacket decode(FriendlyByteBuf buf) {
        return new ServerToClientDoorTickPacket(buf.readBlockPos(), buf.readDouble(), buf.readEnum(DeepBlueLabAccessControlDoorEntity.DoorState.class));
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var clientLevel = Minecraft.getInstance().level;
            if (clientLevel == null) return;

            BlockEntity blockEntity = clientLevel.getBlockEntity(blockPos);
            if (blockEntity instanceof DeepBlueLabAccessControlDoorEntity doorEntity) {
                doorEntity.setAnimationTick(animationTick);
                doorEntity.setDoorState(doorState);
                doorEntity.setChanged();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
