package com.limingz.mymod.gui.holographic_ui.network;

import com.limingz.mymod.block.entity.DemoBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ServerHandler {
    public static void handleUIClick(ServerPlayer player, BlockPos pos) {
        if (player == null) return;

        Level level = player.level();
        if (!level.isLoaded(pos)) return;

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof DemoBlockEntity demoBE) {
            // 触发服务端逻辑
        }
    }
}