package com.limingz.mymod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SmallDoorBlock extends HorizontalDirectionalBlock {
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    protected static final VoxelShape Z_SHAPE = Block.box(0.0D, 0.0D, 7.0D, 16.0D, 16.0D, 9.0D);
    protected static final VoxelShape X_SHAPE = Block.box(7.0D, 0.0D, 0.0D, 9.0D, 16.0D, 16.0D);
    protected static final VoxelShape Z_COLLISION_SHAPE = Block.box(0.0D, 0.0D, 7.0D, 16.0D, 24.0D, 9.0D);
    protected static final VoxelShape X_COLLISION_SHAPE = Block.box(7.0D, 0.0D, 0.0D, 9.0D, 24.0D, 16.0D);
    protected static final VoxelShape EAST_SHAPE_OPEN = Shapes.or(Block.box(8.0D, 0.0D, 0.0D, 16.0D, 16.0D, 2.0D), Block.box(8.0D, 0.0D, 14.0D, 16.0D, 16.0D, 16.0D));
    protected static final VoxelShape WEST_SHAPE_OPEN = Shapes.or(Block.box(0.0D, 0.0D, 0.0D, 8.0D, 16.0D, 2.0D), Block.box(0.0D, 0.0D, 14.0D, 8.0D, 16.0D, 16.0D));
    protected static final VoxelShape SOUTH_SHAPE_OPEN = Shapes.or(Block.box(0.0D, 0.0D, 8.0D, 2.0D, 16.0D, 16.0D), Block.box(14.0D, 0.0D, 8.0D, 16.0D, 16.0D, 16.0D));
    protected static final VoxelShape NORTH_SHAPE_OPEN = Shapes.or(Block.box(0.0D, 0.0D, 0.0D, 2.0D, 16.0D, 8.0D), Block.box(14.0D, 0.0D, 0.0D, 16.0D, 16.0D, 8.0D));
    protected static final VoxelShape EAST_COLLISION_SHAPE_OPEN = Shapes.or(Block.box(8.0D, 0.0D, 0.0D, 16.0D, 24.0D, 2.0D), Block.box(8.0D, 0.0D, 14.0D, 16.0D, 24.0D, 16.0D));
    protected static final VoxelShape WEST_COLLISION_SHAPE_OPEN = Shapes.or(Block.box(0.0D, 0.0D, 0.0D, 8.0D, 24.0D, 2.0D), Block.box(0.0D, 0.0D, 14.0D, 8.0D, 24.0D, 16.0D));
    protected static final VoxelShape SOUTH_COLLISION_SHAPE_OPEN = Shapes.or(Block.box(0.0D, 0.0D, 8.0D, 2.0D, 24.0D, 16.0D), Block.box(14.0D, 0.0D, 8.0D, 16.0D, 24.0D, 16.0D));
    protected static final VoxelShape NORTH_COLLISION_SHAPE_OPEN = Shapes.or(Block.box(0.0D, 0.0D, 0.0D, 2.0D, 24.0D, 8.0D), Block.box(14.0D, 0.0D, 0.0D, 16.0D, 24.0D, 8.0D));

    public SmallDoorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(OPEN, Boolean.FALSE).setValue(FACING, Direction.EAST));
    }

    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        var isOpen = blockState.getValue(OPEN);
        var facing = blockState.getValue(FACING);
        if (isOpen) {
            return switch (facing) {
                case NORTH -> NORTH_SHAPE_OPEN;
                case SOUTH -> SOUTH_SHAPE_OPEN;
                case WEST -> WEST_SHAPE_OPEN;
                default -> EAST_SHAPE_OPEN;
            };
        } else {
            return facing.getAxis() == Direction.Axis.Z ? Z_SHAPE : X_SHAPE;
        }
    }

    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        var isOpen = blockState.getValue(OPEN);
        var facing = blockState.getValue(FACING);
        if (isOpen) {
            return switch (facing) {
                case NORTH -> NORTH_COLLISION_SHAPE_OPEN;
                case SOUTH -> SOUTH_COLLISION_SHAPE_OPEN;
                case WEST -> WEST_COLLISION_SHAPE_OPEN;
                default -> EAST_COLLISION_SHAPE_OPEN;
            };
        } else {
            return facing.getAxis() == Direction.Axis.Z ? Z_COLLISION_SHAPE : X_COLLISION_SHAPE;
        }
    }

    public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
        return blockState.getValue(OPEN);
    }


    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        Direction direction = blockPlaceContext.getHorizontalDirection();
        return this.defaultBlockState().setValue(FACING, direction).setValue(OPEN, Boolean.FALSE);
    }


    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (blockState.getValue(OPEN)) {
            blockState = blockState.setValue(OPEN, Boolean.FALSE);
            level.setBlock(blockPos, blockState, 10);
        } else {
            Direction blockHitDirection = blockHitResult.getDirection();
            if (blockState.getValue(FACING) == blockHitDirection.getOpposite()) {
                blockState = blockState.setValue(OPEN, Boolean.TRUE);
                level.setBlock(blockPos, blockState, 10);
            } else {
                if (!level.isClientSide) {
                    player.sendSystemMessage(Component.literal("大门不能从这一侧打开"));
                }
            }
        }

        boolean flag = blockState.getValue(OPEN);
        level.gameEvent(player, flag ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, blockPos);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_53389_) {
        p_53389_.add(FACING, OPEN);
    }
}
