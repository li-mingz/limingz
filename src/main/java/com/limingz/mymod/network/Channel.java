package com.limingz.mymod.network;

import com.limingz.mymod.network.packet.playertoserver.ClientToServerDoorTickPacket;
import com.limingz.mymod.network.packet.servertoplayer.FarmXpPacket;
import com.limingz.mymod.network.packet.servertoplayer.GetClientTickPacket;
import com.limingz.mymod.network.packet.servertoplayer.ServerToClientDoorTickPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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
        INSTANCE.messageBuilder(ClientToServerDoorTickPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(ClientToServerDoorTickPacket::decode)
                .encoder(ClientToServerDoorTickPacket::encode)
                .consumerMainThread(ClientToServerDoorTickPacket::handle)
                .add();
        INSTANCE.messageBuilder(ServerToClientDoorTickPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ServerToClientDoorTickPacket::decode)
                .encoder(ServerToClientDoorTickPacket::encode)
                .consumerMainThread(ServerToClientDoorTickPacket::handle)
                .add();
        INSTANCE.messageBuilder(GetClientTickPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(GetClientTickPacket::decode)
                .encoder(GetClientTickPacket::encode)
                .consumerMainThread(GetClientTickPacket::handle)
                .add();
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
    /**
     * 向指定方块实体附近的玩家发送 S2C 数据包（范围：64格内+同维度，和 sendBlockUpdated 一致）
     * @param message 要发送的数据包（必须是 S2C 方向，比如 FarmXpPacket）
     * @param blockPos 目标方块的坐标（比如你的 BlockEntity 坐标）
     * @param serverLevel 服务端维度（确保同维度筛选）
     */
    public static <MSG> void sendToNearby(MSG message, BlockPos blockPos, ServerLevel serverLevel) {
        PacketDistributor.TargetPoint targetPoint = new PacketDistributor.TargetPoint(
                blockPos.getX() + 0.5,  // 方块中心点 X（避免偏移）
                blockPos.getY() + 0.5,  // 方块中心点 Y
                blockPos.getZ() + 0.5,  // 方块中心点 Z
                64.0D,                  // 半径 64 格（sendBlockUpdated 固定值）
                serverLevel.dimension() // 仅同维度玩家
        );

        // 2. 发送数据包：通过你的通道实例，用 NEAR 分发器筛选玩家
        INSTANCE.send(
                PacketDistributor.NEAR.with(() -> targetPoint),
                message
        );
    }
}
