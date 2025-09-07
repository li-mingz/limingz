package com.limingz.mymod.command;

import com.limingz.mymod.util.BlockReplaceInLoadingChunk;
import com.limingz.mymod.util.RegionUtil;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.common.Mod;

import static com.limingz.mymod.Main.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EndingCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ending_by_4").executes((context) -> {
//            // 先删除，再插入
//            // 删除
//            ForgeSQLiteSubmitAndDeleteEvent.deleteFromSQLite();
//            // 插入
//            ForgeSQLiteSubmitAndDeleteEvent.submitToSQLite();
//            sqLiteBatchTraversal.reset();
//            sqLiteBatchTraversal.startTraversal();// 在命令执行方法中
            context.getSource().sendSuccess(
                    () -> Component.literal(String.format("[%s] [调试] [4结局事件] 开始替换方块", MODID)),
                false // 是否广播到日志
            );
            context.getSource().sendSuccess(
                    () -> Component.literal(BlockReplaceInLoadingChunk.replaceBlock()),
                    false // 是否广播到日志
            );
            context.getSource().sendSuccess(
                    () -> Component.literal(RegionUtil.traversalAllRegionFiles()),
                    false // 是否广播到日志
            );
            return 0;
        }));

    }
}
