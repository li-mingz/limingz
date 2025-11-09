package com.limingz.mymod.network.packet.servertoplayer;

import com.limingz.mymod.block.entity.DeepBlueLabAccessControlDoorEntity;
import com.limingz.mymod.network.Channel;
import com.limingz.mymod.network.packet.playertoserver.DoorTickPacket;
import com.limingz.mymod.register.BlockEntityRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class GetClientTickPacket {
    private final BlockPos blockPos;

    public GetClientTickPacket(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    public static GetClientTickPacket decode(FriendlyByteBuf buf) {
        return new GetClientTickPacket(buf.readBlockPos());
    }
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        // 线程安全：操作入队到客户端主线程
        context.enqueueWork(() -> {
            // 获取客户端 Minecraft 实例
            Minecraft minecraft = Minecraft.getInstance();
            // 获取客户端世界
            Level clientLevel = minecraft.level;
            if (!clientLevel.isClientSide()) {
                return; // 确保是客户端 Level
            }
            if (!clientLevel.isInWorldBounds(this.blockPos)) {
                return; // 坐标不在世界边界内
            }

            // 获取方块实体
            DeepBlueLabAccessControlDoorEntity deepBlueLabAccessControlDoorEntity = clientLevel.getBlockEntity(
                    this.blockPos,
                    BlockEntityRegister.deep_blue_lab_access_control_door_entity.get()
            ).orElse(null);
            if (deepBlueLabAccessControlDoorEntity != null) {
                // 同步客户端的动画帧
                Channel.INSTANCE.sendToServer(new DoorTickPacket(blockPos, deepBlueLabAccessControlDoorEntity.getAnimationLength()-deepBlueLabAccessControlDoorEntity.getAnimationTick(), deepBlueLabAccessControlDoorEntity.getDoorState()));
            }
        });

        // 标记数据包已处理
        context.setPacketHandled(true);
        return true;
    }
}
