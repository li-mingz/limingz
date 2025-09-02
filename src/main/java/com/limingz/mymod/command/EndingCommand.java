package com.limingz.mymod.command;

import com.limingz.mymod.util.sqlite.SQLiteBatchTraversal;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.level.block.Blocks;

public class EndingCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ending_by_4").executes((context) -> {
//            // 创建替换器
//            LoadedChunkBlockReplacer replacer = new LoadedChunkBlockReplacer(
//                    context.getSource().getLevel(),
//                    Blocks.STONE,
//                    Blocks.DIAMOND_BLOCK.defaultBlockState()
//            );
//
//            // 启动替换
//            replacer.start();
            SQLiteBatchTraversal.traversal();
            return 0;
        }));

    }
}
