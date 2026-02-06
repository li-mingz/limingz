package com.limingz.mymod.network.packet.playertoserver;

import com.limingz.mymod.network.Channel;
import com.limingz.mymod.network.packet.servertoplayer.ChunkDataPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class RequestChunkCapturePacket {
    private final ChunkPos target;
    private final int radius;
    private final String dimension; // Optional: specify dimension. If null, use current.

    public RequestChunkCapturePacket(ChunkPos target, int radius, String dimension) {
        this.target = target;
        this.radius = radius;
        this.dimension = dimension;
    }

    public RequestChunkCapturePacket(FriendlyByteBuf buf) {
        System.out.println("[Debug] Server decoding RequestChunkCapturePacket...");
        this.target = buf.readChunkPos();
        this.radius = buf.readInt();
        if (buf.readBoolean()) {
            this.dimension = buf.readUtf();
        } else {
            this.dimension = null;
        }
        System.out.println("[Debug] Server decoded Request from client: Target=" + target + ", Radius=" + radius);
    }

    public void encode(FriendlyByteBuf buf) {
        System.out.println("[Debug] Client encoding RequestChunkCapturePacket: Target=" + target + ", Radius=" + radius);
        buf.writeChunkPos(target);
        buf.writeInt(radius);
        if (dimension != null) {
            buf.writeBoolean(true);
            buf.writeUtf(dimension);
        } else {
            buf.writeBoolean(false);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null) return;

            // Resolve Level based on dimension ID or use player current level
            ServerLevel level = player.serverLevel();
            if (this.dimension != null) {
                ResourceLocation dimLoc = ResourceLocation.tryParse(this.dimension);
                if (dimLoc != null) {
                    ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION, dimLoc);
                    ServerLevel targetLevel = player.server.getLevel(dimKey);
                    if (targetLevel != null) {
                        level = targetLevel;
                        System.out.println("[Debug] Switching capture target to dimension: " + this.dimension);
                    } else {
                        System.out.println("[Debug] Dimension not found: " + this.dimension + ", using current.");
                    }
                }
            }

            System.out.println("[Debug] Server handling RequestChunkCapture from " + player.getName().getString() + " for " + target + " in " + level.dimension().location());

            // Security check? maybe op only if accessing other dimensions or large radius?
            // For now assume strictly visual and harmless, but large radius causes lag.
            int r = Math.min(this.radius, 2); // Cap at 2 on server side

            Map<BlockPos, Integer> blockData = new HashMap<>(); // Store state IDs
            Map<BlockPos, Byte> lightMap = new HashMap<>(); // Store light data
            int totalBlocks = 0;

            System.out.println("[Debug] Server starting chunk scan... Radius=" + r);

            // Phase 1: Force Load Chunks (including border for lighting context)
            int loadRadius = r + 1;
            for (int x = -loadRadius; x <= loadRadius; x++) {
                for (int z = -loadRadius; z <= loadRadius; z++) {
                    int cx = target.x + x;
                    int cz = target.z + z;
                    // Force load chunk to FULL status ensuring lighting and generation
                    level.getChunkSource().getChunk(cx, cz, net.minecraft.world.level.chunk.ChunkStatus.FULL, true);
                }
            }

            // Phase 2: Capture Data
            for (int x = -r; x <= r; x++) {
                for (int z = -r; z <= r; z++) {
                    int cx = target.x + x;
                    int cz = target.z + z;

                    // This chunk is now guaranteed loaded
                    LevelChunk chunk = level.getChunk(cx, cz);

                    LevelChunkSection[] sections = chunk.getSections();
                    for (int i = 0; i < sections.length; i++) {
                        LevelChunkSection section = sections[i];
                        if (section == null || section.hasOnlyAir()) continue;
                        int sectionY = chunk.getMinBuildHeight() + (i * 16);

                        for (int lx = 0; lx < 16; lx++) {
                            for (int ly = 0; ly < 16; ly++) {
                                for (int lz = 0; lz < 16; lz++) {
                                    BlockState state = section.getBlockState(lx, ly, lz);
                                    if (!state.isAir()) {
                                        int wx = cx * 16 + lx;
                                        int wy = sectionY + ly;
                                        int wz = cz * 16 + lz;

                                        // Relative to center chunk
                                        // RelX = wx - target.x*16
                                        // RelZ = wz - target.z*16
                                        int rx = wx - target.x * 16;
                                        int rz = wz - target.z * 16;
                                        // Relative Y is just Y

                                        BlockPos rel = new BlockPos(rx, wy, rz);
                                        // Use State ID
                                        blockData.put(rel, net.minecraft.world.level.block.Block.getId(state));

                                        // Capture Light
                                        int sky = level.getBrightness(net.minecraft.world.level.LightLayer.SKY, new BlockPos(wx, wy, wz));
                                        int block = level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, new BlockPos(wx, wy, wz));
                                        byte packedLight = (byte) ((sky << 4) | block);
                                        lightMap.put(rel, packedLight);

                                        totalBlocks++;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            System.out.println("[Debug] Server scan complete. Total blocks captured: " + totalBlocks);
            System.out.println("[Debug] Server splitting & sending ChunkDataPacket responses...");

            // Limit per packet to ensure we stay well below 2MB limit.
            // 20000 blocks * 5 bytes = 100KB, very safe.
            final int MAX_BLOCKS_PER_PACKET = 20000;

            java.util.List<Map.Entry<BlockPos, Integer>> allEntries = new java.util.ArrayList<>(blockData.entrySet());
            int total = allEntries.size();
            int totalPackets = (int) Math.ceil((double) total / MAX_BLOCKS_PER_PACKET);

            if (total == 0) {
                 Channel.sendToPlayer(new ChunkDataPacket(target, new HashMap<>(), new HashMap<>(), 0, 1), player);
            } else {
                for (int i = 0; i < total; i += MAX_BLOCKS_PER_PACKET) {
                    int end = Math.min(total, i + MAX_BLOCKS_PER_PACKET);
                    List<Map.Entry<BlockPos, Integer>> subList = allEntries.subList(i, end);

                    Map<BlockPos, Integer> packetData = new HashMap<>();
                    Map<BlockPos, Byte> packetLight = new HashMap<>();

                    for (Map.Entry<BlockPos, Integer> e : subList) {
                        packetData.put(e.getKey(), e.getValue());
                        if (lightMap.containsKey(e.getKey())) {
                            packetLight.put(e.getKey(), lightMap.get(e.getKey()));
                        }
                    }

                    int packetIndex = i / MAX_BLOCKS_PER_PACKET;
                    // System.out.println("[Debug] Sending packet part: " + (packetIndex + 1) + "/" + totalPackets + ", size: " + packetData.size());
                    Channel.sendToPlayer(new ChunkDataPacket(target, packetData, packetLight, packetIndex, totalPackets), player);

                    // Small delay to prevent flooding network queue? (Optional, Netty handles it but ...)
                }
            }

            System.out.println("[Debug] All packets queued.");
        });
        context.get().setPacketHandled(true);
    }
}
