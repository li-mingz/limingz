package com.limingz.mymod.client;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import java.util.HashMap;
import java.util.Map;

public class ChunkOverlayManager {
    private static boolean enabled = false;
    private static ChunkPos targetChunk = null;
    private static ChunkPos anchorChunk = null;
    private static int radius = 1; // Default radius lower for testing and safety
    private static Map<BlockPos, BlockState> capturedBlocks = new HashMap<>();
    private static Map<BlockPos, Byte> capturedLight = new HashMap<>();
    private static long dataVersion = 0;

    // Transitory buffer for incoming multi-part packets
    private static ChunkPos pendingTarget = null;
    private static Map<BlockPos, BlockState> pendingBlocks = new HashMap<>();
    private static Map<BlockPos, Byte> pendingLight = new HashMap<>();
    private static int receivedPacketsCount = 0;
    private static int expectedTotalPackets = -1;

    public static long getDataVersion() {
        return dataVersion;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean enabled) {
        ChunkOverlayManager.enabled = enabled;
        if (!enabled) {
            capturedBlocks.clear();
            capturedLight.clear();
            dataVersion++;
        }
    }

    public static ChunkPos getTargetChunk() {
        return targetChunk;
    }

    public static void setTargetChunk(ChunkPos targetChunk) {
        ChunkOverlayManager.targetChunk = targetChunk;
    }

    public static ChunkPos getAnchorChunk() {
        return anchorChunk;
    }

    public static void setAnchorChunk(ChunkPos anchorChunk) {
        ChunkOverlayManager.anchorChunk = anchorChunk;
    }

    public static int getRadius() {
        return radius;
    }

    public static void setRadius(int r) {
        radius = r;
    }

    public static Map<BlockPos, BlockState> getCapturedBlocks() {
        return capturedBlocks;
    }

    public static Map<BlockPos, Byte> getCapturedLight() {
        return capturedLight;
    }

    public static void capture(Level level, ChunkPos center, int r) {
        capturedBlocks.clear();
        capturedLight.clear();
        dataVersion++;
        targetChunk = center;
        radius = r;

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                int cx = center.x + x;
                int cz = center.z + z;

                if (!level.hasChunk(cx, cz)) continue;
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
                                    int wx = cx * 16 + lx; // World X
                                    int wy = sectionY + ly; // World Y
                                    int wz = cz * 16 + lz; // World Z

                                    // Store relative to center chunk origin (center.x*16, 0, center.z*16)
                                    // RelX = wx - center.x*16
                                    // RelZ = wz - center.z*16

                                    BlockPos relativePos = new BlockPos(wx - center.x * 16, wy, wz - center.z * 16);
                                    capturedBlocks.put(relativePos, state);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void loadFromPacket(ChunkPos center, Map<BlockPos, BlockState> data, Map<BlockPos, Byte> lightData, int packetIndex, int totalPackets) {
        // If this is a new transfer (packetIndex 0) or target changed drastically (fail-safe)
        if (packetIndex == 0 || !center.equals(pendingTarget)) {
            pendingTarget = center;
            pendingBlocks.clear();
            pendingLight.clear();
            receivedPacketsCount = 0;
            expectedTotalPackets = totalPackets;
        }

        // Add partial data
        if (center.equals(pendingTarget)) {
            pendingBlocks.putAll(data);
            if (lightData != null) pendingLight.putAll(lightData);
            receivedPacketsCount++;

            System.out.println("[Debug] Assemble packet: " + receivedPacketsCount + "/" + totalPackets);

            // Check if complete
            if (receivedPacketsCount >= totalPackets) {
                targetChunk = pendingTarget;
                capturedBlocks = new HashMap<>(pendingBlocks);
                capturedLight = new HashMap<>(pendingLight);
                dataVersion++;

                pendingTarget = null;
                pendingBlocks.clear();
                pendingLight.clear();
                receivedPacketsCount = 0;

                if (net.minecraft.client.Minecraft.getInstance().player != null) {
                    net.minecraft.client.Minecraft.getInstance().player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("Capture complete! " + capturedBlocks.size() + " blocks."), false);
                }
            }
        }
    }
}
