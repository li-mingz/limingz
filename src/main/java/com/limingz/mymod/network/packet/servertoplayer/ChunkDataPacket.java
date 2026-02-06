package com.limingz.mymod.network.packet.servertoplayer;

import com.limingz.mymod.client.ChunkOverlayManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ChunkDataPacket {
    private final ChunkPos center;
    private final Map<BlockPos, Integer> data;
    // Map storing packed light data: Key is BlockPos, Value is (SkyLight << 4 | BlockLight)
    private final Map<BlockPos, Byte> lightData;
    private final int packetIndex;
    private final int totalPackets;

    public ChunkDataPacket(ChunkPos center, Map<BlockPos, Integer> data, Map<BlockPos, Byte> lightData, int packetIndex, int totalPackets) {
        this.center = center;
        this.data = data;
        this.lightData = lightData;
        this.packetIndex = packetIndex;
        this.totalPackets = totalPackets;
    }

    public ChunkDataPacket(FriendlyByteBuf buf) {
        System.out.println("[Debug] Client decoding ChunkDataPacket header...");
        this.center = buf.readChunkPos();
        this.packetIndex = buf.readVarInt();
        this.totalPackets = buf.readVarInt();

        // 1. Read Palette
        int paletteSize = buf.readVarInt();
        System.out.println("[Debug] Client decoding palette size: " + paletteSize);
        List<Integer> palette = new ArrayList<>(paletteSize);
        for (int i = 0; i < paletteSize; i++) {
            palette.add(buf.readVarInt());
        }

        // 2. Read Blocks and Light
        int blockSize = buf.readVarInt();
        System.out.println("[Debug] Client decoding block count: " + blockSize);
        this.data = new HashMap<>(blockSize);
        this.lightData = new HashMap<>(blockSize);

        for (int i = 0; i < blockSize; i++) {
            // Read relative coords
            byte dx = buf.readByte();
            int y = buf.readShort();
            byte dz = buf.readByte();
            int paletteIndex = buf.readVarInt();
            byte packedLight = buf.readByte(); // Read light

            int stateId = palette.get(paletteIndex);

            // Keep as relative position
            BlockPos pos = new BlockPos(dx, y, dz);
            data.put(pos, stateId);
            lightData.put(pos, packedLight);
        }
        System.out.println("[Debug] Client finished decoding ChunkDataPacket. Part: " + (packetIndex + 1) + "/" + totalPackets);
    }

    public void encode(FriendlyByteBuf buf) {
        System.out.println("[Debug] Server encoding ChunkDataPacket for " + center + " with " + data.size() + " blocks. Part: " + (packetIndex + 1) + "/" + totalPackets);
        buf.writeChunkPos(center);
        buf.writeVarInt(packetIndex);
        buf.writeVarInt(totalPackets);

        // 1. Build Palette
        List<Integer> palette = new ArrayList<>();
        Map<Integer, Integer> stateToPaletteIndex = new HashMap<>();

        for (Integer stateId : data.values()) {
            if (!stateToPaletteIndex.containsKey(stateId)) {
                stateToPaletteIndex.put(stateId, palette.size());
                palette.add(stateId);
            }
        }

        System.out.println("[Debug] Server built palette with " + palette.size() + " entries.");

        // Write Palette
        buf.writeVarInt(palette.size());
        for (Integer id : palette) {
            buf.writeVarInt(id);
        }

        // 2. Write Blocks
        buf.writeVarInt(data.size());

        // DO NOT use center offset. Map keys are already relative from RequestChunkCapturePacket.
        // long centerX = center.x * 16L;
        // long centerZ = center.z * 16L;

        for (Map.Entry<BlockPos, Integer> entry : data.entrySet()) {
            BlockPos pos = entry.getKey();
            int stateId = entry.getValue();

            // Encode light: get from light map or default
            byte packedLight = lightData.getOrDefault(pos, (byte)0);

            // Pos is already relative
            int dx = pos.getX();
            int dz = pos.getZ();
            int dy = pos.getY();

            // Write Relative Coords
            buf.writeByte(dx);
            buf.writeShort(dy);
            buf.writeByte(dz);

            // Write Palette Index
            buf.writeVarInt(stateToPaletteIndex.get(stateId));

            // Write Light
            buf.writeByte(packedLight);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            System.out.println("[Debug] Client handling ChunkDataPacket used blocks: " + data.size());

            Map<BlockPos, BlockState> resolved = new HashMap<>();
            for (Map.Entry<BlockPos, Integer> entry : data.entrySet()) {
                resolved.put(entry.getKey(), Block.stateById(entry.getValue()));
            }
            ChunkOverlayManager.loadFromPacket(center, resolved, lightData, packetIndex, totalPackets);

            if (net.minecraft.client.Minecraft.getInstance().player != null) {
                 // Only show message on first packet or periodically to avoid spam
                 if (packetIndex == 0 || packetIndex == totalPackets - 1) {
                     // Log to console instead of spamming chat
                     System.out.println("Received packet part " + (packetIndex + 1) + "/" + totalPackets + " with " + data.size() + " blocks.");
                 }
            }
        });
        context.get().setPacketHandled(true);
    }
}
