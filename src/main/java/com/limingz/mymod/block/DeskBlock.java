package com.limingz.mymod.block;

import com.limingz.mymod.block.entity.DeskBlockEntity;
import com.limingz.mymod.gui.menuprovider.DeskBlockContainerMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class DeskBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public DeskBlock(Properties properties) {
        super(properties);
    }

    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return Block.box(0, 1, 0, 16, 16, 16);
    }

    public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
        return true;
    }

    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (!level.isClientSide){
            var blockEntity = level.getBlockEntity(blockPos);
            if(blockEntity instanceof DeskBlockEntity deskBlockEntity){
                NetworkHooks.openScreen((ServerPlayer) player, new DeskBlockContainerMenuProvider(blockPos), deskBlockEntity.getBlockPos());
            } else {
                throw new IllegalStateException("missing");
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new DeskBlockEntity(pPos, pState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        if (pLevel.isClientSide){
            return ((pLevel1, pPos, pState1, pBlockEntity) -> {
                if(pBlockEntity instanceof DeskBlockEntity deskBlockEntity){
                    deskBlockEntity.clientTick();
                }
            });
        }else {
            return ((pLevel1, pPos, pState1, pBlockEntity) -> {
                if(pBlockEntity instanceof DeskBlockEntity deskBlockEntity){
                    deskBlockEntity.serverTick();
                }
            });
        }
    }
}
