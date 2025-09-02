package com.limingz.mymod.util.sqlite;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.concurrent.ConcurrentLinkedQueue;

public class SQLiteTempData {
    public static ConcurrentLinkedQueue sqliteAddQueue = new ConcurrentLinkedQueue();
    public static ConcurrentLinkedQueue sqliteDeleteQueue = new ConcurrentLinkedQueue();

    public static void executeAdd(Level world, BlockPos pos, Block block) {
        // 服务端执行
        if(!world.isClientSide) {
            sqliteAddQueue.add(pos.getX());
            sqliteAddQueue.add(pos.getY());
            sqliteAddQueue.add(pos.getZ());
            sqliteAddQueue.add(block.getDescriptionId());
            sqliteAddQueue.add(pos.getX()>>4);
            sqliteAddQueue.add(pos.getY()>>4);
        }
    }

    public static void executeDelete(LevelAccessor world, int x, int y, int z, BlockState blockstate) {
        // 服务端执行
        if(!world.isClientSide()) {
            sqliteDeleteQueue.add(x);
            sqliteDeleteQueue.add(y);
            sqliteDeleteQueue.add(z);
            sqliteDeleteQueue.add(blockstate.getBlock().getDescriptionId());
            sqliteDeleteQueue.add(x>>4);
            sqliteDeleteQueue.add(y>>4);
        }
    }
}
