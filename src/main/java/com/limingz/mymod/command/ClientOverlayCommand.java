package com.limingz.mymod.command;

import com.limingz.mymod.client.ChunkOverlayManager;
import com.limingz.mymod.client.ChunkOverlayRenderer;
import com.limingz.mymod.network.Channel;
import com.limingz.mymod.network.packet.playertoserver.RequestChunkCapturePacket;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;

public class ClientOverlayCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("overlay")
            .then(Commands.literal("capture")
                .executes(ctx -> capture(ctx.getSource(), null, null))
                .then(Commands.argument("x", IntegerArgumentType.integer())
                    .then(Commands.argument("z", IntegerArgumentType.integer())
                        .executes(ctx -> capture(ctx.getSource(), new ChunkPos(
                            IntegerArgumentType.getInteger(ctx, "x"),
                            IntegerArgumentType.getInteger(ctx, "z")
                        ), null))
                        .then(Commands.argument("dimension", net.minecraft.commands.arguments.ResourceLocationArgument.id())
                            .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(new String[]{"minecraft:overworld", "minecraft:the_nether", "minecraft:the_end"}, builder))
                            .executes(ctx -> capture(ctx.getSource(), new ChunkPos(
                                IntegerArgumentType.getInteger(ctx, "x"),
                                IntegerArgumentType.getInteger(ctx, "z")
                            ), net.minecraft.commands.arguments.ResourceLocationArgument.getId(ctx, "dimension").toString()))
                        )
                    )
                )
            )
            .then(Commands.literal("clear")
                .executes(ctx -> {
                    ChunkOverlayManager.setEnabled(false);
                    return 1;
                })
            )
            .then(Commands.literal("transition")
                .executes(ctx -> {
                    ChunkOverlayRenderer.startAnimation();
                    ctx.getSource().sendSuccess(() -> Component.literal("Starting dimension transition effect..."), false);
                    return 1;
                })
            )
        );
    }

    private static int capture(CommandSourceStack source, ChunkPos pos, String dimension) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return 0;

        ChunkPos target = (pos == null) ? mc.player.chunkPosition() : pos;

        // Sync radius with client render distance
        int viewDistance = mc.options.renderDistance().get();
        ChunkOverlayManager.setRadius(viewDistance);

        // Send packet to server to request data
        Channel.INSTANCE.sendToServer(new RequestChunkCapturePacket(target, ChunkOverlayManager.getRadius(), dimension));

        String dimInfo = (dimension == null) ? "current dimension" : dimension;
        source.sendSuccess(() -> Component.literal("Requesting chunk data for: " + target + " in " + dimInfo), false);

        // Auto-enable when capturing (data will arrive async)
        ChunkOverlayManager.setEnabled(true);
        ChunkOverlayManager.setAnchorChunk(mc.player.chunkPosition());
        ChunkOverlayManager.setAnchorDimension(mc.level.dimension());
        ChunkOverlayManager.setTargetDimension(dimension); // Store invalidation

        // Reset animation state to ensure overlay starts hidden
        ChunkOverlayRenderer.resetState();

        return 1;
    }
}
