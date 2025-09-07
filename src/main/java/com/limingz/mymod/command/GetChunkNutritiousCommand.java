package com.limingz.mymod.command;

import com.limingz.mymod.capability.chunkdata.ChunkDataProvider;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

public class GetChunkNutritiousCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("get_chunk_nutritious").executes((context) -> {
            Level level = context.getSource().getLevel();
            // 获取玩家所在区块
            ChunkPos chunkPos = context.getSource().getPlayer().chunkPosition();
            LevelChunk levelChunk = level.getChunk(chunkPos.x, chunkPos.z);
            levelChunk.getCapability(ChunkDataProvider.CHUNK_DATA_CAPABILITY).ifPresent((data) -> {
                context.getSource().sendSuccess(() -> Component.literal("当前区块富营养化情况：" + data.get_nutritious()), false);
            });
            return 0;
        }));
        dispatcher.register(Commands.literal("set_chunk_nutritious")
                .then(Commands.argument("is_nutritious", BoolArgumentType.bool())
                        .executes(context -> {
            boolean is_nutritious = BoolArgumentType.getBool(context, "is_nutritious");
            Level level = context.getSource().getLevel();
            // 获取玩家所在区块
            ChunkPos chunkPos = context.getSource().getPlayer().chunkPosition();
            LevelChunk levelChunk = level.getChunk(chunkPos.x, chunkPos.z);
            levelChunk.getCapability(ChunkDataProvider.CHUNK_DATA_CAPABILITY).ifPresent((data) -> {
                data.set_nutritious(is_nutritious);
                context.getSource().sendSuccess(() -> Component.literal("设置当前区块富营养化：" + is_nutritious), false);
            });
            return 0;
        })));

    }
}
