package com.limingz.mymod.command;

import com.limingz.mymod.capability.farmxp.PlayerFarmXpProvider;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class GetFarmXpCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("farmxp").executes((context) -> {
            context.getSource().getPlayer().getCapability(PlayerFarmXpProvider.PLAYER_FARM_XP_CAPABILITY).ifPresent((xp) -> {
                context.getSource().sendSuccess(() -> Component.literal("xp:" + xp.getXp()), false);
            });
            return 0;
        }));

    }
}
