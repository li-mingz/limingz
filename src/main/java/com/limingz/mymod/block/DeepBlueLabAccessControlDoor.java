package com.limingz.mymod.block;

import com.limingz.mymod.block.entity.DeepBlueLabAccessControlDoorEntity;
import com.limingz.mymod.register.BlockEntityRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;


public class DeepBlueLabAccessControlDoor extends BaseEntityBlock{
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    protected static final VoxelShape SOUTH_AND_NORTH_SHAPE_CLOSE = Shapes.or(
            Block.box(0.0D, 0.0D, 5.0D, 48.0D, 96.0D, 11.0D),
            Block.box(-32.0D, 0.0D, 5.0D, 0.0D, 96.0D, 11.0D)
    );


    public DeepBlueLabAccessControlDoor(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return BlockEntityRegister.deep_blue_lab_access_control_door_entity.get().create(pPos, pState);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        BlockPos blockpos = pContext.getClickedPos();
        Level level = pContext.getLevel();
        if (blockpos.getY() < level.getMaxBuildHeight() - 5 && level.getBlockState(blockpos.above()).canBeReplaced(pContext)) {
            return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection());
        } else {
            return null;
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
    }

    @Override
    public boolean hasDynamicShape() {
        return true;
    }


    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SOUTH_AND_NORTH_SHAPE_CLOSE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SOUTH_AND_NORTH_SHAPE_CLOSE;
    }
    /*
        动态生成门的AABB
     */
//    private VoxelShape getDynamicShape(BlockState state, BlockGetter level, BlockPos pos) {
//        BlockEntity blockEntity = level.getBlockEntity(pos);
//        if (!(blockEntity instanceof DeepBlueLabAccessControlDoorEntity doorEntity)) {
//            return CLOSED_SHAPE; // 无 BlockEntity 时默认关闭形状
//        }
//
//        Direction facing = state.getValue(FACING); // 获取门的朝向
//        DeepBlueLabAccessControlDoorEntity.DoorState doorState = doorEntity.getDoorState();
//        double animationTick = doorEntity.getAnimationTick();
//        double animationHalfLength = doorEntity.getAnimationLength() / 2; // 开门动画长度（0~300）
//
//        switch (doorState) {
//            case OPENED:
//                // 打开状态：薄型碰撞箱（沿朝向轴收缩）
//                return getOpenedShape(facing);
//            case OPENING:
//                // 开门中：根据动画进度平滑过渡（从完整→薄）
//                float openProgress = (float) (animationTick / animationHalfLength);
//                openProgress = Math.min(openProgress, 1.0F); // 防止进度溢出
//                return getTransitionShape(facing, openProgress, true);
//            case CLOSING:
//                // 关门中：根据动画进度平滑过渡（从薄→完整）
//                float closeProgress = (float) ((animationTick - animationHalfLength) / animationHalfLength);
//                closeProgress = Math.min(closeProgress, 1.0F);
//                return getTransitionShape(facing, closeProgress, false);
//            case CLOSED:
//            default:
//                // 关闭状态：完整碰撞箱
//                return CLOSED_SHAPE;
//        }
//    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        if (pLevel.isClientSide){
            return ((pLevel1, pPos, pState1, pBlockEntity) -> {
                if(pBlockEntity instanceof DeepBlueLabAccessControlDoorEntity deepBlueLabAccessControlDoorEntity){
                    deepBlueLabAccessControlDoorEntity.clientTick();
                }
            });
        } else {
            return ((pLevel1, pPos, pState1, pBlockEntity) -> {
                if(pBlockEntity instanceof DeepBlueLabAccessControlDoorEntity deepBlueLabAccessControlDoorEntity){
                    deepBlueLabAccessControlDoorEntity.serverTick();
                }
            });
        }
    }
}
