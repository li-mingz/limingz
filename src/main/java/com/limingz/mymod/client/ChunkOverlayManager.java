package com.limingz.mymod.client;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;

public class ChunkOverlayManager {
    private static boolean enabled = false;
    private static ChunkPos targetChunk = null;
    private static ChunkPos anchorChunk = null;
    private static net.minecraft.resources.ResourceKey<Level> anchorDimension = null;
    private static String targetDimension = null;
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

    private static boolean seamlessTransitioning = false;
    private static double renderYOffset = 0.0;
    private static double targetTeleportY = -1000.0;

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

    public static net.minecraft.resources.ResourceKey<Level> getAnchorDimension() {
        return anchorDimension;
    }

    public static void setAnchorDimension(net.minecraft.resources.ResourceKey<Level> dim) {
        anchorDimension = dim;
    }

    public static String getTargetDimension() {
        return targetDimension;
    }

    public static void setTargetDimension(String dimension) {
        targetDimension = dimension;
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

    public static boolean isSeamlessTransitioning() {
        return seamlessTransitioning;
    }

    public static void setSeamlessTransitioning(boolean seamlessTransitioning) {
        ChunkOverlayManager.seamlessTransitioning = seamlessTransitioning;
    }

    public static double getRenderYOffset() {
        return renderYOffset;
    }

    public static double getTargetTeleportY() {
        return targetTeleportY;
    }

    public static void capture(Level level, ChunkPos center, int r) {
        capturedBlocks.clear();
        capturedLight.clear();
        dataVersion++;
        targetChunk = center;
        radius = r;
        // If local capture, assume current dimension if not set?
        // Actually this capture method is for local (integrated server/client same thread logic usually unused in mod)
        // Adjust if needed.

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

                // PERFORMANCE FIX: Direct assignment instead of deep copy.
                // Copying 2 million entries causes massive lag/freeze.
                capturedBlocks = pendingBlocks;
                capturedLight = pendingLight;

                // Re-init buffers for next time
                pendingBlocks = new HashMap<>();
                pendingLight = new HashMap<>();

                dataVersion++;

                pendingTarget = null;
                receivedPacketsCount = 0;

                if (net.minecraft.client.Minecraft.getInstance().player != null) {
                    net.minecraft.client.Minecraft.getInstance().player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("Capture complete! Preparing visual buffer..."), false);
                }

                calculateSmartOffset();

                // Trigger async build. Animation will start when build finishes.
                ChunkOverlayRenderer.scheduleRebuild();
            }
        }
    }

    private static void calculateSmartOffset() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        int playerY = mc.player.getBlockY();

        // 1. Calculate Current Ground Y
        // Scan downwards from player to find solid ground
        int currentGroundY = playerY;
        boolean foundGround = false;
        BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos(mc.player.getX(), playerY, mc.player.getZ());

        for (int y = playerY; y >= mc.level.getMinBuildHeight(); y--) {
            probe.setY(y - 1); // Check block below
            BlockState state = mc.level.getBlockState(probe);
            if (!state.getCollisionShape(mc.level, probe).isEmpty()) {
                // Found solid ground at y-1. Valid standing spot is y.
                currentGroundY = y;
                foundGround = true;
                break;
            }
        }

        if (!foundGround) {
            // "如果脚下是虚空则为玩家当前高度"
            currentGroundY = playerY;
            System.out.println("[Debug] Current ground scan result: Void. Using Player Y: " + playerY);
        } else {
            System.out.println("[Debug] Current ground scan result: Found ground at " + currentGroundY);
        }

        // 2. Calculate Target Ground Y
        // "随机抽5个点决定高度会导致渲染和实际传送的位置不一致"
        // Fix: Only check the exact center (8, 8), which is where we will teleport.
        // If we teleport to (8, 8), we must align visually with (8, 8).
        int targetX = 8;
        int targetZ = 8;

        int bestTargetY = -1000;

        // Find highest ground at center (8, 8)
        for (int y = 319; y > -64; y--) {
            if (isSolid(targetX, y, targetZ) && !isSolid(targetX, y + 1, targetZ)) {
                // Found surface
                bestTargetY = y + 1; // Standing on y, means feet at y+1
                break;
            }
        }

        if (bestTargetY != -1000) {
            // Visual Offset = Current Ground - Target Ground
            renderYOffset = currentGroundY - bestTargetY;

            // Teleport Target: If player is flying (playerY > currentGroundY), maintain that relative height.
            // "如果玩家在离地状态还要加上离地高度-地面高度"
            // Relative Height = playerY - currentGroundY.
            // New Y = Target Ground + Relative Height.
            targetTeleportY = bestTargetY + (playerY - currentGroundY);

            System.out.println("[Debug] Smart Offset Calculated: TargetGround=" + bestTargetY +
                               ", CurrentGround=" + currentGroundY +
                               ", RelativeHeight=" + (playerY - currentGroundY) +
                               ", RenderOffset=" + renderYOffset +
                               ", TeleportY=" + targetTeleportY);
        } else {
            // Fallback: If no safe spot found in target (Void), align with current ground (flying transition)
            targetTeleportY = currentGroundY;
            renderYOffset = 0; // Or currentGroundY - currentGroundY
            System.out.println("[Debug] No safe spot found in target (Void?), maintaining relative height.");
        }
    }

    private static boolean isSolid(int x, int y, int z) {
        BlockPos p = new BlockPos(x, y, z);
        BlockState s = capturedBlocks.get(p);
        // If not in map, assume Air (or unknown). Default false (not solid).
        if (s == null) return false;
        // In 1.20.1, getMaterial() is removed. Use collision shape check.
        return !s.getCollisionShape(net.minecraft.world.level.EmptyBlockGetter.INSTANCE, BlockPos.ZERO).isEmpty();
    }
}
