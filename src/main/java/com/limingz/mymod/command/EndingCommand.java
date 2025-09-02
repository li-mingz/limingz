package com.limingz.mymod.command;

import com.limingz.mymod.event.server.ForgeSQLiteSubmitAndDeleteEvent;
import com.limingz.mymod.util.sqlite.SQLiteBatchTraversal;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.limingz.mymod.Main.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EndingCommand {
    private static SQLiteBatchTraversal sqLiteBatchTraversal = new SQLiteBatchTraversal();
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
            // 先删除，再插入
            // 删除
            ForgeSQLiteSubmitAndDeleteEvent.deleteFromSQLite();
            // 插入
            ForgeSQLiteSubmitAndDeleteEvent.submitToSQLite();
            sqLiteBatchTraversal.reset();
            sqLiteBatchTraversal.startTraversal();
            return 0;
        }));

    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        sqLiteBatchTraversal.tickTraversal();
    }
}
