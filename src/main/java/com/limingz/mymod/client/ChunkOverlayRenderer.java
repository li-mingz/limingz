package com.limingz.mymod.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.Map;

public class ChunkOverlayRenderer {

    private static java.util.concurrent.CompletableFuture<Void> rebuildTask = null;

    private static VertexBuffer vertexBuffer;
    private static long instanceLastDataVersion = -1; // static

    private static long animationStartTime = 0;
    private static boolean isAnimating = false;
    private static double currentRadius = 0.0f; // Default hidden until transaction starts? Or full visible?
    // If we want seamless transition, maybe start at 0. But capture usually means "show me this".
    // User wants: "Transition effect... sweep across... scene changes".
    // This implies scene is initially NOT the overlay.
    // So default radius should be 0.
    private static net.minecraft.world.phys.Vec3 transitionCenter = net.minecraft.world.phys.Vec3.ZERO;
    private static boolean hasTriggeredTeleport = false;
    private static int handoffDelay = 0; // Ticks to wait after arrival before disabling overlay

    @SubscribeEvent
    public void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        if (!ChunkOverlayManager.isEnabled()) {
            if (vertexBuffer != null) {
                vertexBuffer.close();
                vertexBuffer = null;
                instanceLastDataVersion = -1;
            }
            return;
        }

        // Auto-disable if player changed dimension
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && ChunkOverlayManager.getAnchorDimension() != null) {
            // If we are seamlessly transitioning, we ALLOW dimension mismatch
            // because we want to hide the Void/Loading with the overlay.
            if (!ChunkOverlayManager.isSeamlessTransitioning()) {
                if (!mc.level.dimension().equals(ChunkOverlayManager.getAnchorDimension())) {
                    ChunkOverlayManager.setEnabled(false);
                    return;
                }
            } else {
                // We ARE seamless transitioning.
                // Check if we arrived in the target dimension and chunks are loaded.
                String targetDimStr = ChunkOverlayManager.getTargetDimension();
                if (targetDimStr != null && mc.level.dimension().location().toString().equals(targetDimStr)) {
                    // We are in target dimension.
                    // Check if chunk is loaded to avoid showing void
                    if (mc.level.getChunkSource().hasChunk(mc.player.chunkPosition().x, mc.player.chunkPosition().z)) {
                        // Check if chunks are actually ready to render?
                        // Just waiting for hasChunk often leaves a 1-second gap where chunks are building (invisible).
                        // Hack: Wait for a few frames/ticks after arrival.

                        if (handoffDelay < 20) { // Wait ~1 second (assuming 20fps logic, or actually render frames)
                            // This is inside onRenderLevel, so it counts Frames, not Ticks.
                            // 20 frames is decent (At 60fps = 0.3s). Maybe wait more?
                            // Let's increment.
                            handoffDelay++;
                            return;
                        }

                        // Arrived, loaded, and waited for meshing!
                        // Disable overlay and flag
                        System.out.println("[Debug] Seamless transition complete. Disabling overlay.");
                        ChunkOverlayManager.setSeamlessTransitioning(false);
                        ChunkOverlayManager.setEnabled(false);
                        handoffDelay = 0;
                        return;
                    }
                }
            }
        }

        // Check for updates
        long currentVersion = ChunkOverlayManager.getDataVersion();
        if (currentVersion != instanceLastDataVersion) {
            // Data changed. Mark as outdated.
            // But we don't rebuild here on main thread anymore to avoid freeze.
            // Rebuild is triggered explicitly by Manager when capture completes.
            // Or if we detect version change here and task is null?
            // If we are here, we might just be waiting for the task to finish.
            // We just update lastDataVersion when we successfully upload?
            // Let's rely on 'scheduleRebuild' being called.
        }

        if (vertexBuffer != null) {
            renderOverlay(event);
        }
    }

    public static void scheduleRebuild() {
        if (rebuildTask != null && !rebuildTask.isDone()) return; // Already baking

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal("Preparing visuals..."), true);
        }

        // Capture data state for the thread
        Map<BlockPos, BlockState> blocks = new java.util.HashMap<>(ChunkOverlayManager.getCapturedBlocks());
        Map<BlockPos, Byte> lights = new java.util.HashMap<>(ChunkOverlayManager.getCapturedLight());
        ChunkPos target = ChunkOverlayManager.getTargetChunk();
        ChunkPos anchor = ChunkOverlayManager.getAnchorChunk();
        net.minecraft.world.level.Level level = mc.level;

        long version = ChunkOverlayManager.getDataVersion();

        rebuildTask = java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                // Background thread
                BufferBuilder builder = new BufferBuilder(2097152); // 2MB buffer init
                builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);

                BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
                BlockAndTintGetter view = new SnapshotBlockGetter(blocks, lights, level, target, anchor);
                RandomSource random = RandomSource.create();

                // PoseStack for offsets
                PoseStack poseStack = new PoseStack();

                for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
                    BlockPos pos = entry.getKey();
                    BlockState state = entry.getValue();
                    poseStack.pushPose();
                    poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
                    dispatcher.renderBatched(state, pos, view, poseStack, builder, true, random);
                    poseStack.popPose();
                }

                BufferBuilder.RenderedBuffer rendered = builder.end();

                // Upload on Main Thread
                Minecraft.getInstance().execute(() -> {
                    uploadBuffer(rendered, version);
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void uploadBuffer(BufferBuilder.RenderedBuffer rendered, long version) {
        if (vertexBuffer != null) {
            vertexBuffer.close();
        }
        vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        vertexBuffer.bind();
        vertexBuffer.upload(rendered);
        VertexBuffer.unbind();

        // Render ready!
        ChunkOverlayRenderer.instanceLastDataVersion = version;

        // Auto-start animation now that mesh is ready
        startAnimation();

        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.displayClientMessage(net.minecraft.network.chat.Component.literal("Ready! Moving..."), true);
        }
    }

    private void renderOverlay(RenderLevelStageEvent event) {
        ChunkPos targetChunk = ChunkOverlayManager.getTargetChunk();
        ShaderInstance shader = ChunkOverlayShaderManager.getTransitionShader();
        ChunkPos anchorChunk = ChunkOverlayManager.getAnchorChunk();

        if (targetChunk == null || shader == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Fallback if anchor not set
        if (anchorChunk == null) {
            anchorChunk = mc.player.chunkPosition();
        }

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();

        double camX = event.getCamera().getPosition().x;
        double camY = event.getCamera().getPosition().y;
        double camZ = event.getCamera().getPosition().z;

        // Use anchor chunk origin
        double originX = anchorChunk.x * 16.0;
        double originZ = anchorChunk.z * 16.0;

        // Apply calculated Y offset to aligning target floor with current view
        double yOffset = ChunkOverlayManager.getRenderYOffset();

        // Translate to anchor chunk origin, relative to camera, plus Y offset
        poseStack.translate(originX - camX, -camY + yOffset, originZ - camZ);

        // Setup Render System
        // If animating, update Radius
        if (isAnimating) {
            long elapsed = System.currentTimeMillis() - animationStartTime;

            // Variable speed: Start slow (0.01) and accelerate to fast (0.1) over 10 seconds
            double v0 = 0.01;
            double v1 = 0.1;
            double duration = 10000.0;

            if (elapsed < duration) {
                // v(t) = v0 + (v1 - v0) * (t / duration)
                // R(t) = Integral v(t) dt = v0*t + 0.5 * (v1 - v0)/duration * t^2
                double t = elapsed;
                currentRadius = v0 * t + 0.5 * (v1 - v0) / duration * t * t;
            } else {
                // After 10s, continue at max speed v1
                // R(duration) = v0*D + 0.5*(v1-v0)*D = D * (v0 + 0.5*v1 - 0.5*v0) = D * 0.5 * (v0 + v1)
                double radiusAtDuration = duration * 0.5 * (v0 + v1);
                double extraTime = elapsed - duration;
                currentRadius = radiusAtDuration + extraTime * v1;
            }

            // Limit radius to capture size to avoid showing void beyond data
            float maxRadius = ChunkOverlayManager.getRadius() * 16.0f;
            if (currentRadius > maxRadius) currentRadius = maxRadius;

            // Trigger Teleport if we have covered enough of the screen
            // Ensures we don't teleport too early or too late.
            float triggerRadius = maxRadius * 0.8f;

            if (!hasTriggeredTeleport && currentRadius > triggerRadius) {
                hasTriggeredTeleport = true;
                ChunkPos target = ChunkOverlayManager.getTargetChunk();
                String dim = ChunkOverlayManager.getTargetDimension();

                System.out.println("[Debug] Triggering teleport to " + target + " dim: " + dim);

                ChunkOverlayManager.setSeamlessTransitioning(true); // Flag for Mixin

                double targetY = ChunkOverlayManager.getTargetTeleportY();

                if (target != null) {
                    com.limingz.mymod.network.Channel.INSTANCE.sendToServer(
                        new com.limingz.mymod.network.packet.playertoserver.RequestTeleportPacket(target, dim, targetY)
                    );
                    if (mc.player != null) {
                        mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal("Teleporting..."), true);
                    }
                }
            }

            // Optionally stop if huge
            if (currentRadius > 2000.0f) {
                // isAnimating = false; // Keep it huge
            }
        }

        if (shader == null) {
             // Fallback to vanilla if our shader failed to load
             RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getRendertypeCutoutShader);
        } else {
             RenderSystem.setShader(() -> shader);
             // Set Uniforms if they exist
             if (shader.getUniform("Radius") != null) shader.getUniform("Radius").set((float)currentRadius);
             if (shader.getUniform("Center") != null) shader.getUniform("Center").set((float)transitionCenter.x, (float)transitionCenter.y, (float)transitionCenter.z);
             if (shader.getUniform("Softness") != null) shader.getUniform("Softness").set(5.0f);
             if (shader.getUniform("ColorModulator") != null) shader.getUniform("ColorModulator").set(1.0f, 1.0f, 1.0f, 1.0f);
        }

        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);

        // Ensure lightmap is active
        mc.gameRenderer.lightTexture().turnOnLightLayer();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);

        // Draw
        vertexBuffer.bind();
        // IMPORTANT: Use the shader we set!
        // RenderSystem.getShader() returns what we set.
        if (RenderSystem.getShader() != null) {
            vertexBuffer.drawWithShader(poseStack.last().pose(), RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
        }
        VertexBuffer.unbind();

        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    public static void startAnimation() {
        isAnimating = true;
        hasTriggeredTeleport = false;
        animationStartTime = System.currentTimeMillis();
        // Reset radius to 0 to start growth
        currentRadius = 0.0f;
        if (Minecraft.getInstance().player != null) {
            // Calculate center relative to anchor
            if (ChunkOverlayManager.getAnchorChunk() != null) {
                ChunkPos anchor = ChunkOverlayManager.getAnchorChunk();
                double ox = anchor.x * 16.0;
                double oz = anchor.z * 16.0;
                net.minecraft.world.phys.Vec3 pPos = Minecraft.getInstance().player.getPosition(1.0f); // partial ticks
                // Center relative to Anchor Origin
                transitionCenter = pPos.subtract(ox, pPos.y, oz);
                // Wait, pPos.y is absolute Y. Anchor Chunk Origin is usually at Y=0?
                // In renderOverlay: poseStack.translate(originX - camX, -camY, originZ - camZ);
                // The geometry is at (pos.x, pos.y, pos.z).
                // Vertices Y = pos.y (relative y depends on section? No, it's absolute Y usually in ChunkDataPacket?
                // In Packet: y = buf.readShort(). It's absolute Y.
                // So vertices are at (x, Y, z) relative to Chunk Origin (x*16, 0, z*16).
                // So Center Y should be player Y.
                transitionCenter = new net.minecraft.world.phys.Vec3(pPos.x - ox, pPos.y, pPos.z - oz);
            }
        }
    }

    public static void resetState() {
        isAnimating = false;
        currentRadius = 0.0f;
        hasTriggeredTeleport = false;
        handoffDelay = 0;
    }

    private static class SnapshotBlockGetter implements BlockAndTintGetter {
        private final Map<BlockPos, BlockState> data;
        private final Map<BlockPos, Byte> lightData;
        private final BlockAndTintGetter originalLevel;
        private final ChunkPos centerChunk;
        private final ChunkPos anchorChunk;

        public SnapshotBlockGetter(Map<BlockPos, BlockState> data, Map<BlockPos, Byte> lightData, BlockAndTintGetter originalLevel, ChunkPos centerChunk, ChunkPos anchorChunk) {
            this.data = data;
            this.lightData = lightData;
            this.originalLevel = originalLevel;
            this.centerChunk = centerChunk;
            this.anchorChunk = anchorChunk;
        }

        @Override
        public BlockState getBlockState(BlockPos pos) {
            return data.getOrDefault(pos, Blocks.AIR.defaultBlockState());
        }

        @Override
        public FluidState getFluidState(BlockPos pos) {
            return Fluids.EMPTY.defaultFluidState();
        }

        @Override
        public float getShade(Direction pDirection, boolean pShade) {
            return originalLevel.getShade(pDirection, pShade);
        }

        @Override
        public LevelLightEngine getLightEngine() {
            return originalLevel.getLightEngine();
        }

        @Override
        public int getBlockTint(BlockPos pos, ColorResolver colorResolver) {
            // Fix: Do NOT query originalLevel for biome colors.
            // originalLevel is the CURRENT world. If we are in the End viewing the Overworld,
            // querying the End for "Grass Color" at pos (x,y,z) returns End-ish colors (or crashes/defaults).
            // Since we don't transfer Biome data in the packets (too heavy), we should return a standard constant color.
            // This ensures standard Green grass/leaves instead of "Current Dimension" tinted ones.

            // Standard Plains/Forest colors:
            // Grass: 0x91BD59
            // Foliage: 0x77AB2F
            // Water: 0x3F76E4

            // We can try to distinguish resolver type, but ColorResolver is an interface.
            // Usually BiomeColors.GRASS_COLOR_RESOLVER etc.
            // Since we can't easily identify the resolver instance without access to BiomeColors static fields efficiently or reflection,
            // we will return a generic robust green that looks good for both.
            // Actually, for Water it might be weird if green.

            // NOTE: The game calls this. Usually:
            // If it's for grass, returns specific green.
            // If it's for water, returns specific blue.
            // But we don't know WHICH resolver is called here easily.
            // However, most tinted blocks are vegetation (Green). Water is usually handled by block model tint, but let's be safe.

            // Best average "Good Looking" tint: 0xFF5DBB63 (Standard Green).
            // For water, this might make it green water (Swamp like).
            // But having Green Water is better than having Purple/Grey Grass when viewing Overworld from End.

            // Optimization: If we want to be fancy, we could check BlockState in calling context? No access here.

            // Let's stick to a vibrant nice green.
            return 0xFF5DBB63;
        }

        @Nullable
        @Override
        public BlockEntity getBlockEntity(BlockPos pos) {
            return null;
        }

        @Override
        public int getBrightness(LightLayer pLightType, BlockPos pBlockPos) {
            // Priority 1: Use captured light data if available
            if (lightData != null && lightData.containsKey(pBlockPos)) {
                byte packed = lightData.get(pBlockPos);
                if (pLightType == LightLayer.BLOCK) {
                    return packed & 0xF;
                } else if (pLightType == LightLayer.SKY) {
                    return (packed >> 4) & 0xF;
                }
            }

            // Priority 2: If we don't have data (it's air/outside capture), return Full Brightness to ensure visibility.
            // Relying on originalLevel causes black faces if the source location is unloaded or dark in the client world.
            // We want the hologram to be clearly visible constantly.
            return 15;
        }

        @Override
        public int getRawBrightness(BlockPos pBlockPos, int pAmount) {
            // Priority 1: Use captured light data
            if (lightData != null && lightData.containsKey(pBlockPos)) {
                byte packed = lightData.get(pBlockPos);
                int blockLight = packed & 0xF;
                int skyLight = (packed >> 4) & 0xF;

                int brightness = skyLight - pAmount;
                if (brightness < 0) brightness = 0;
                return Math.max(blockLight, brightness);
            }

            // Priority 2: Default full brightness for any missing blocks (Air) to light up the faces of our captured blocks.
            return 15;
        }


        @Override public int getHeight() { return 384; }
        @Override public int getMinBuildHeight() { return -64; }
    }
}
