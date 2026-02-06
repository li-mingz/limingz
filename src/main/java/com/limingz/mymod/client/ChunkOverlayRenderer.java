package com.limingz.mymod.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
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
import org.joml.Matrix4f;

import javax.annotation.Nullable;
import java.util.Map;

public class ChunkOverlayRenderer {

    private VertexBuffer vertexBuffer;
    private long lastDataVersion = -1;

    @SubscribeEvent
    public void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        if (!ChunkOverlayManager.isEnabled()) {
            if (vertexBuffer != null) {
                vertexBuffer.close();
                vertexBuffer = null;
                lastDataVersion = -1;
            }
            return;
        }

        // Check for updates
        long currentVersion = ChunkOverlayManager.getDataVersion();
        if (currentVersion != lastDataVersion) {
            rebuildBuffer();
            lastDataVersion = currentVersion;
        }

        if (vertexBuffer != null) {
            renderOverlay(event);
        }
    }

    private void rebuildBuffer() {
        if (vertexBuffer != null) {
            vertexBuffer.close();
            vertexBuffer = null;
        }

        Map<BlockPos, BlockState> blocks = ChunkOverlayManager.getCapturedBlocks();
        Map<BlockPos, Byte> lights = ChunkOverlayManager.getCapturedLight();
        if (blocks.isEmpty()) return;

        // Create vertex buffer only if we have data to render
        vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();

        // Start building mesh
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);

        Minecraft mc = Minecraft.getInstance();
        BlockRenderDispatcher dispatcher = mc.getBlockRenderer();

        ChunkPos targetChunk = ChunkOverlayManager.getTargetChunk();
        ChunkPos anchorChunk = ChunkOverlayManager.getAnchorChunk();

        if (anchorChunk == null && mc.player != null) {
            anchorChunk = mc.player.chunkPosition();
        }

        // Use a custom view for culling and tinting
        BlockAndTintGetter view = new SnapshotBlockGetter(blocks, lights, mc.level, targetChunk, anchorChunk);
        RandomSource random = RandomSource.create();

        // Redirect all render types to our single builder
        MultiBufferSource fixedSource = type -> builder;
        PoseStack poseStack = new PoseStack();

        for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
            BlockPos pos = entry.getKey();
            BlockState state = entry.getValue();

            poseStack.pushPose();
            // Translate is required because renderBatched does NOT translate the PoseStack.
            // It only uses the pos for logic (culling, model data, etc).
            poseStack.translate(pos.getX(), pos.getY(), pos.getZ());

            // 15728880 is MAX_LIGHT
            // We use renderBatched which does culling
            dispatcher.renderBatched(state, pos, view, poseStack, builder, true, random);

            poseStack.popPose();
        }

        BufferBuilder.RenderedBuffer renderedBuffer = builder.end();
        vertexBuffer.bind();
        vertexBuffer.upload(renderedBuffer);
        VertexBuffer.unbind();
    }

    private void renderOverlay(RenderLevelStageEvent event) {
        ChunkPos targetChunk = ChunkOverlayManager.getTargetChunk();
        ShaderInstance shader = ChunkOverlayShaderManager.getHologramShader();
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

        // Translate to anchor chunk origin, relative to camera
        poseStack.translate(originX - camX, -camY, originZ - camZ);

        // Setup Render System - Use cutout shader to handle transparency (grass, leaves) correctly
        // Solid shader doesn't discard alpha, causing transparent pixels to write to depth buffer and occlude blocks behind.
        RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getRendertypeCutoutShader);
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);

        // Ensure lightmap is active
        mc.gameRenderer.lightTexture().turnOnLightLayer();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);

        // Draw
        vertexBuffer.bind();
        if (RenderSystem.getShader() != null) {
            vertexBuffer.drawWithShader(poseStack.last().pose(), RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
        }
        VertexBuffer.unbind();

        RenderSystem.disableBlend();
        poseStack.popPose();
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
            if (centerChunk == null) return 0xFF5DBB63; // Fallback green

            // Calculate absolute position to query biome color from the current world (SOURCE biomes)
            int absX = centerChunk.x * 16 + pos.getX();
            int absY = pos.getY();
            int absZ = centerChunk.z * 16 + pos.getZ();
            BlockPos worldPos = new BlockPos(absX, absY, absZ);

            try {
                return originalLevel.getBlockTint(worldPos, colorResolver);
            } catch (Exception e) {
                return 0xFF5DBB63;
            }
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

        private BlockPos resolveSourcePos(BlockPos relativePos) {
            if (centerChunk == null) return relativePos;
            int absX = centerChunk.x * 16 + relativePos.getX();
            int absY = relativePos.getY();
            int absZ = centerChunk.z * 16 + relativePos.getZ();
            return new BlockPos(absX, absY, absZ);
        }

        @Override public int getHeight() { return 384; }
        @Override public int getMinBuildHeight() { return -64; }
    }
}
