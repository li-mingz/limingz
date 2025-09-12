package com.limingz.mymod.network.packet.playertoserver;

import com.limingz.mymod.block.entity.DeepBlueLabAccessControlDoorEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class DoorTickPacket {
    private final BlockPos blockPos;
    private final Double animationTick;

    public DoorTickPacket(BlockPos blockPos, Double animationTick) {
        this.blockPos = blockPos;
        this.animationTick = animationTick;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
        buf.writeDouble(animationTick);
    }

    public static DoorTickPacket decode(FriendlyByteBuf buf) {
        return new DoorTickPacket(buf.readBlockPos(), buf.readDouble());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerLevel level = (ServerLevel) ctx.get().getSender().level();
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if(blockEntity instanceof DeepBlueLabAccessControlDoorEntity doorEntity){
                doorEntity.setAnimationTick(animationTick);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
