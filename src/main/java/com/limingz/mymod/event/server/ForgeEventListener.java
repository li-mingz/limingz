package com.limingz.mymod.event.server;

import com.limingz.mymod.capability.farmxp.PlayerFarmXpProvider;
import com.limingz.mymod.command.EndingCommand;
import com.limingz.mymod.command.GetChunkNutritiousCommand;
import com.limingz.mymod.command.GetFarmXpCommand;
import com.limingz.mymod.config.CommonConfig;
import com.limingz.mymod.network.Channel;
import com.limingz.mymod.network.packet.servertoplayer.FarmXpPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.limingz.mymod.Main.MODID;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = MODID)
public class ForgeEventListener {
    @SubscribeEvent
    public static void useXp(BonemealEvent event) {
        var blockstate = event.getBlock();
        var block = blockstate.getBlock();
        if (CommonConfig.block_usexp_set.contains(block) &&
                block instanceof BonemealableBlock bonemealableBlock &&
                bonemealableBlock.isValidBonemealTarget(event.getLevel(), event.getPos(), blockstate, true)) {
            event.getEntity().getCapability(PlayerFarmXpProvider.PLAYER_FARM_XP_CAPABILITY).ifPresent((xp) -> {
                if (!xp.decrease(CommonConfig.xp_decrease)) event.setCanceled(true);
                if (!event.getLevel().isClientSide)
                    Channel.sendToPlayer(new FarmXpPacket(xp.getXp()), (ServerPlayer) event.getEntity());
            });
        }

    }

    @SubscribeEvent
    public static void registerCommand(RegisterCommandsEvent event) {
        GetFarmXpCommand.register(event.getDispatcher());
        EndingCommand.register(event.getDispatcher());
        GetChunkNutritiousCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void getXp(BlockEvent.BreakEvent event) {
        var blockstate = event.getState();
        var block = blockstate.getBlock();
        if (CommonConfig.block_getxp_set.contains(block) &&
                block instanceof BonemealableBlock bonemealableBlock &&
                !bonemealableBlock.isValidBonemealTarget(event.getLevel(), event.getPos(), blockstate, true)) {
            event.getPlayer().getCapability(PlayerFarmXpProvider.PLAYER_FARM_XP_CAPABILITY).ifPresent((xp) -> {
                xp.increase(CommonConfig.xp_increase);
                if (event.getPlayer() instanceof ServerPlayer serverPlayer)
                    Channel.sendToPlayer(new FarmXpPacket(xp.getXp()), serverPlayer);
            });
        }
    }

    @SubscribeEvent
    public static void playerClone(PlayerEvent.Clone event) {
        event.getOriginal().getCapability(PlayerFarmXpProvider.PLAYER_FARM_XP_CAPABILITY).ifPresent((old) -> {
            event.getEntity().getCapability(PlayerFarmXpProvider.PLAYER_FARM_XP_CAPABILITY).ifPresent((xp) -> {
                xp.setXp(old.getXp());
                if (event.getEntity() instanceof ServerPlayer serverPlayer)
                    Channel.sendToPlayer(new FarmXpPacket(xp.getXp()), serverPlayer);
            });
        });
    }

    @SubscribeEvent
    public static void PlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        event.getEntity().getCapability(PlayerFarmXpProvider.PLAYER_FARM_XP_CAPABILITY).ifPresent((xp) -> {
            if (event.getEntity() instanceof ServerPlayer serverPlayer)
                Channel.sendToPlayer(new FarmXpPacket(xp.getXp()), serverPlayer);
        });
    }
}
