package com.limingz.mymod.network.packet.playertoserver;

import com.limingz.mymod.block.entity.DeepBlueLabAccessControlDoorEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientToServerDoorTickPacket {
    private final BlockPos blockPos;
    private final Double animationTick;
    private final DeepBlueLabAccessControlDoorEntity.DoorState doorState;

    public ClientToServerDoorTickPacket(BlockPos blockPos, Double animationTick, DeepBlueLabAccessControlDoorEntity.DoorState doorState) {
        this.blockPos = blockPos;
        this.animationTick = animationTick;
        this.doorState = doorState;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
        buf.writeDouble(animationTick);
        buf.writeEnum(doorState);
    }

    public static ClientToServerDoorTickPacket decode(FriendlyByteBuf buf) {
        return new ClientToServerDoorTickPacket(buf.readBlockPos(), buf.readDouble(), buf.readEnum(DeepBlueLabAccessControlDoorEntity.DoorState.class));
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerLevel level = (ServerLevel) ctx.get().getSender().level();
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if(blockEntity instanceof DeepBlueLabAccessControlDoorEntity doorEntity){
                doorEntity.setAnimationTick(animationTick);
                doorEntity.setDoorState(doorState);
                doorEntity.setChanged();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
