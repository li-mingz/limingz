package com.limingz.mymod.block;

import com.limingz.mymod.Main;
import com.limingz.mymod.block.entity.DeepBlueLabAccessControlDoorEntity;
import com.limingz.mymod.register.BlockEntityRegister;
import com.limingz.mymod.util.GeckolibInterpolationTool;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import software.bernie.geckolib.core.animation.EasingType;

import java.util.List;


public class DeepBlueLabAccessControlDoor extends BaseEntityBlock{
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final int MAX_XZ_INDEX = 8;
    public static final int MIDDLE_XZ_INDEX = 4;
    public static final int MAX_Y_OFFSET = 5;
    // XZ偏移的映射表(IntegerProperty不允许负值)
    private static final int[] XZ_OFFSET_MAP = new int[]{-4, -3, -2, -1, 0, 1, 2, 3, 4};
    // 占位方块在X/Z方向的相对位置
    public static final IntegerProperty XZ_INDEX = IntegerProperty.create("xz_index", 0, MAX_XZ_INDEX);
    // 占位方块在Y方向的相对位置
    public static final IntegerProperty Y_OFFSET = IntegerProperty.create("y_offset", 0, MAX_Y_OFFSET);
    // 是否是中心方块（负责渲染和逻辑）
    public static final BooleanProperty IS_MAIN = BooleanProperty.create("is_main");
    // 默认状态: 1格
    private static final VoxelShape BASE_COLLISION_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    //无体积
    private static final VoxelShape AIR_SHAPE = Shapes.empty();

    // 关键帧
    public static final List<GeckolibInterpolationTool.PositionKeyframe> KEYFRAMES_LEFT = List.of(
            new GeckolibInterpolationTool.PositionKeyframe(0, new Vector3d(0, 0, 0)),
            new GeckolibInterpolationTool.PositionKeyframe(300, new Vector3d(28, 0, 0), EasingType.EASE_IN_OUT_QUAD),
            new GeckolibInterpolationTool.PositionKeyframe(600, new Vector3d(0, 0, 0), EasingType.EASE_IN_OUT_QUAD)
    );
    public static final List<GeckolibInterpolationTool.PositionKeyframe> KEYFRAMES_RIGHT = List.of(
            new GeckolibInterpolationTool.PositionKeyframe(0, new Vector3d(0, 0, 0)),
            new GeckolibInterpolationTool.PositionKeyframe(300, new Vector3d(-28, 0, 0), EasingType.EASE_IN_OUT_QUAD),
            new GeckolibInterpolationTool.PositionKeyframe(600, new Vector3d(0, 0, 0), EasingType.EASE_IN_OUT_QUAD)
    );



    public DeepBlueLabAccessControlDoor(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(XZ_INDEX, MIDDLE_XZ_INDEX)
                .setValue(Y_OFFSET, 0)
                .setValue(IS_MAIN, true));
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING, XZ_INDEX, Y_OFFSET, IS_MAIN);
    }
    // 通过索引获取实际XZ偏移
    private int getXzOffsetByIndex(int xzIndex) {
        return XZ_OFFSET_MAP[xzIndex];
    }

    // 计算占位方块的实际位置（根据朝向偏移）
    private BlockPos getPlaceholderPos(BlockPos centerPos, Direction facing, int xzIndex, int yOffset) {
        int actualXzOffset = getXzOffsetByIndex(xzIndex); // 索引→实际偏移
        return switch (facing) {
            case NORTH, SOUTH -> centerPos.offset(actualXzOffset, yOffset, 0); // 用实际偏移
            case EAST, WEST -> centerPos.offset(0, yOffset, actualXzOffset);
            default -> centerPos;
        };
    }

    // 放置时生成所有占位方块
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos centerPos = context.getClickedPos();
        Direction facing = context.getHorizontalDirection();
        Level level = context.getLevel();

        if (!canPlaceAllPlaceholders(context)) return null;

        // 返回中心状态：XZ_INDEX=4（偏移0）
        return this.defaultBlockState()
                .setValue(FACING, facing)
                .setValue(XZ_INDEX, MIDDLE_XZ_INDEX)
                .setValue(Y_OFFSET, 0)
                .setValue(IS_MAIN, true);
    }
    // 校验所有占位方块位置
    private boolean canPlaceAllPlaceholders(BlockPlaceContext context) {
        BlockPos centerPos = context.getClickedPos();
        Direction facing = context.getHorizontalDirection();
        Level level = context.getLevel();

        for (int xzIndex = 0; xzIndex <= MAX_XZ_INDEX; xzIndex++) {
            for (int y = 0; y <= MAX_Y_OFFSET; y++) {
                BlockPos placeholderPos = getPlaceholderPos(centerPos, facing, xzIndex, y);
                if (placeholderPos.getY() > level.getMaxBuildHeight() - 1) return false;
                if (!level.getBlockState(placeholderPos).canBeReplaced(context)) return false;
            }
        }
        return true;
    }

    // 生成所有占位方块
    @Override
    public void setPlacedBy(Level level, BlockPos centerPos, BlockState centerState, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, centerPos, centerState, placer, stack);
        if (level.isClientSide) return;

        Direction facing = centerState.getValue(FACING);
        // 循环索引 0~9（跳过中心索引4+Y=0）
        for (int xzIndex = 0; xzIndex <= MAX_XZ_INDEX; xzIndex++) {
            for (int y = 0; y <= MAX_Y_OFFSET; y++) {
                if (xzIndex == MIDDLE_XZ_INDEX && y == 0) continue; // 中心索引4→偏移0，跳过已生成的中心方块

                BlockPos placeholderPos = getPlaceholderPos(centerPos, facing, xzIndex, y);
                // 占位方块状态：设置 XZ_INDEX（不是偏移）
                BlockState placeholderState = this.defaultBlockState()
                        .setValue(FACING, facing)
                        .setValue(XZ_INDEX, xzIndex) // 存储索引，不是偏移
                        .setValue(Y_OFFSET, y)
                        .setValue(IS_MAIN, false);
                level.setBlock(placeholderPos, placeholderState, 3);
                level.blockUpdated(placeholderPos, Blocks.AIR);
            }
        }
    }

    @Override
    public boolean hasDynamicShape() {
        return true;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // 找到中心方块的BlockEntity
        BlockPos centerPos = getCenterPos(state, pos);
        DeepBlueLabAccessControlDoorEntity centerEntity = (DeepBlueLabAccessControlDoorEntity) level.getBlockEntity(centerPos);

        if (centerEntity == null) return BASE_COLLISION_SHAPE;
        return getDynamicShape(state, level, pos, centerEntity);
    }

    // 鼠标对准的视觉反馈
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape voxelShape = getCollisionShape(state, level, pos, context);
        return voxelShape;
    }

    // 从任意占位方块找到中心方块（
    private BlockPos getCenterPos(BlockState state, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        int xzIndex = state.getValue(XZ_INDEX); // 获取索引
        int actualXzOffset = getXzOffsetByIndex(xzIndex); // 索引→实际偏移
        int yOffset = state.getValue(Y_OFFSET);

        // 反向偏移：从占位方块位置回到中心
        return switch (facing) {
            case NORTH, SOUTH -> pos.offset(-actualXzOffset, -yOffset, 0);
            case EAST, WEST -> pos.offset(0, -yOffset, -actualXzOffset);
            default -> pos;
        };
    }
    /*
        动态生成门板的VoxelShape(仅门板，不包括墙)
     */
    private VoxelShape getDynamicShape(BlockState state, BlockGetter level, BlockPos pos, DeepBlueLabAccessControlDoorEntity centerEntity) {
        // 计算门的偏移
        Vector3d leftDoorPosition = centerEntity.getLeftDoorPos();
        Vector3d rightDoorPosition = centerEntity.getRightDoorPos();
        double left_x_position = leftDoorPosition.x();
        double right_x_position = rightDoorPosition.x();

        Direction facing = state.getValue(FACING); // 获取门的朝向
        int xz_index = state.getValue(XZ_INDEX); // 获取门方块的横向索引
        int actualXzOffset = getXzOffsetByIndex(xz_index); // 获取横向索引对应的横向偏移

        // 以北朝向为基准(门朝北时方块索引从左往右递增)
//        int leftOrRightOffset;
//        switch (facing){
//            case NORTH -> leftOrRightOffset = actualXzOffset;
//            case SOUTH -> leftOrRightOffset = -actualXzOffset;
//            default -> leftOrRightOffset = actualXzOffset;
//        }

        if(actualXzOffset == 0){
            // 为中间一列
            double leftPosition = Math.max(0.0D, 8.0D - left_x_position);
            double rightPosition = Math.min(16.0D, 8.0D - right_x_position);
            switch (facing){
                case NORTH, SOUTH -> {
                    return Shapes.or(Block.box(0.0D, 0.0D, 5.0D, leftPosition, 16.0D, 11.0D),
                            Block.box(rightPosition, 0.0D, 5.0D, 16.0D, 16.0D, 11.0D));
                }
                case EAST, WEST -> {
                    return Shapes.or(Block.box(5.0D, 0.0D, 0.0D, 11.0D, 16.0D, leftPosition),
                            Block.box(5.0D, 0.0D, rightPosition, 11.0D, 16.0D, 16.0D));
                }
            }
        } else if (actualXzOffset < 0) {
            double rightPosition = Math.min(16.0D,
                    Math.max(0.0D, 16.0D * (Math.abs(actualXzOffset)+1) - left_x_position - 8.0D));
            double leftPosition = Math.min(16.0D,
                    Math.max(0.0D, 16.0D * (Math.abs(actualXzOffset)-2) - left_x_position));
            switch (facing){
                case NORTH, SOUTH -> {
                    return Block.box(leftPosition, 0.0D, 5.0D, rightPosition, 16.0D, 11.0D);
                }
                case EAST, WEST -> {
                    return Block.box(5.0D, 0.0D, leftPosition, 11.0D, 16.0D, rightPosition);
                }
            }
        } else {
            double leftPosition = Math.min(16.0D,
                    Math.max(0.0D, -16.0D * (Math.abs(actualXzOffset)-1) - right_x_position - 8.0D));
            double rightPosition = Math.min(16.0D,
                    Math.max(0.0D, -16.0D * (Math.abs(actualXzOffset)+2) - right_x_position));
            switch (facing){
                case NORTH, SOUTH -> {
                    return Block.box(leftPosition, 0.0D, 5.0D, rightPosition, 16.0D, 11.0D);
                }
                case EAST, WEST -> {
                    return Block.box(5.0D, 0.0D, leftPosition, 11.0D, 16.0D, rightPosition);
                }
            }
        }
        return BASE_COLLISION_SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        // 只有中心方块渲染Geckolib模型，其他隐藏
        return state.getValue(IS_MAIN) ? RenderShape.ENTITYBLOCK_ANIMATED : RenderShape.INVISIBLE;
    }

    // 隐藏占位方块的视觉遮挡（避免挡住中心模型）
    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    // 只有中心方块创建BlockEntity（节省资源）
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(IS_MAIN) ? BlockEntityRegister.deep_blue_lab_access_control_door_entity.get().create(pos, state) : null;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        if (!pState.getValue(IS_MAIN)) return null; // 非中心方块无Ticker
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
    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        super.playerWillDestroy(level, pos, state, player);
        if (level.isClientSide) return;

        // 找到中心方块
        BlockPos centerPos = getCenterPos(state, pos);
        Direction facing = level.getBlockState(centerPos).getValue(FACING);


        // 销毁中心方块的BlockEntity
        BlockEntity centerEntity = level.getBlockEntity(centerPos);
        if (centerEntity != null) {
            centerEntity.setRemoved();
        }

        // 销毁所有占位方块
        for (int xz = 0; xz <= MAX_XZ_INDEX; xz++) {
            for (int y = 0; y <= MAX_Y_OFFSET; y++) {
                BlockPos placeholderPos = getPlaceholderPos(centerPos, facing, xz, y);
                if (level.getBlockState(placeholderPos).is(this)) {
                    level.setBlock(placeholderPos, Blocks.AIR.defaultBlockState(), 35);
                    level.levelEvent(player, 2001, placeholderPos, Block.getId(level.getBlockState(placeholderPos)));
                }
            }
        }
    }
}
