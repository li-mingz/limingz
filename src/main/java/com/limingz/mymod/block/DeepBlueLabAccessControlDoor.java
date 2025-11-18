package com.limingz.mymod.block;

import com.limingz.mymod.block.entity.DeepBlueLabAccessControlDoorEntity;
import com.limingz.mymod.register.BlockEntityRegister;
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


public class DeepBlueLabAccessControlDoor extends BaseEntityBlock{
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    // 占位方块在X/Z方向的相对位置
    public static final IntegerProperty XZ_INDEX = IntegerProperty.create("xz_index", 0, 8);
    // 占位方块在Y方向的相对位置
    public static final IntegerProperty Y_OFFSET = IntegerProperty.create("y_offset", 0, 5);
    // 是否是中心方块（负责渲染和逻辑）
    public static final BooleanProperty IS_MAIN = BooleanProperty.create("is_main");
    // XZ偏移的映射表
    private static final int[] XZ_OFFSET_MAP = new int[]{-4, -3, -2, -1, 0, 1, 2, 3, 4};
//    protected static final VoxelShape SOUTH_AND_NORTH_SHAPE_CLOSE = Shapes.or(
//            Block.box(0.0D, 0.0D, 5.0D, 48.0D, 96.0D, 11.0D),
//            Block.box(-32.0D, 0.0D, 5.0D, 0.0D, 96.0D, 11.0D)
//    );
    // 闭合状态：每个占位方块的碰撞体积（1×1×1基础框，拼接成完整门碰撞）
    private static final VoxelShape BASE_COLLISION_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    // 打开状态：每个占位方块的碰撞体积（薄框，不阻挡）
    private static final VoxelShape OPEN_COLLISION_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 2.0D);



    public DeepBlueLabAccessControlDoor(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(XZ_INDEX, 4)
                .setValue(Y_OFFSET, 0)
                .setValue(IS_MAIN, true));
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING, XZ_INDEX, Y_OFFSET, IS_MAIN);
    }
    // 通过索引获取实际XZ偏移（核心映射方法）
    private int getXzOffsetByIndex(int xzIndex) {
        if (xzIndex < 0 || xzIndex >= XZ_OFFSET_MAP.length) {
            return 0; // 异常索引默认返回0偏移
        }
        return XZ_OFFSET_MAP[xzIndex];
    }

//    @Override
//    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
//        BlockPos blockpos = pContext.getClickedPos();
//        Level level = pContext.getLevel();
//        if (blockpos.getY() < level.getMaxBuildHeight() - 5 && level.getBlockState(blockpos.above()).canBeReplaced(pContext)) {
//            return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection());
//        } else {
//            return null;
//        }
//    }

    // 2. 计算占位方块的实际位置（根据朝向偏移）
    private BlockPos getPlaceholderPos(BlockPos centerPos, Direction facing, int xzIndex, int yOffset) {
        int actualXzOffset = getXzOffsetByIndex(xzIndex); // 索引→实际偏移
        return switch (facing) {
            case NORTH, SOUTH -> centerPos.offset(actualXzOffset, yOffset, 0); // 用实际偏移
            case EAST, WEST -> centerPos.offset(0, yOffset, actualXzOffset);
            default -> centerPos;
        };
    }

    // 3. 放置时生成所有占位方块（效仿灾变setPlacedBy）
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos centerPos = context.getClickedPos();
        Direction facing = context.getHorizontalDirection();
        Level level = context.getLevel();

        if (!canPlaceAllPlaceholders(context)) return null;

        // 返回中心状态：XZ_INDEX=4（偏移0）
        return this.defaultBlockState()
                .setValue(FACING, facing)
                .setValue(XZ_INDEX, 4)
                .setValue(Y_OFFSET, 0)
                .setValue(IS_MAIN, true);
    }
    // 1. 校验所有占位方块位置（修改循环范围）
    private boolean canPlaceAllPlaceholders(BlockPlaceContext context) {
        BlockPos centerPos = context.getClickedPos();
        Direction facing = context.getHorizontalDirection();
        Level level = context.getLevel();

        for (int xzIndex = 0; xzIndex <= 8; xzIndex++) {
            for (int y = 0; y <= 5; y++) {
                BlockPos placeholderPos = getPlaceholderPos(centerPos, facing, xzIndex, y);
                if (placeholderPos.getY() > level.getMaxBuildHeight() - 1) return false;
                if (!level.getBlockState(placeholderPos).canBeReplaced(context)) return false;
            }
        }
        return true;
    }

    // 2. 生成所有占位方块（修改循环范围）
    @Override
    public void setPlacedBy(Level level, BlockPos centerPos, BlockState centerState, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, centerPos, centerState, placer, stack);
        if (level.isClientSide) return;

        Direction facing = centerState.getValue(FACING);
        // 循环索引 0~9（跳过中心索引4+Y=0）
        for (int xzIndex = 0; xzIndex <= 8; xzIndex++) {
            for (int y = 0; y <= 5; y++) {
                if (xzIndex == 4 && y == 0) continue; // 中心索引4→偏移0，跳过已生成的中心方块

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
        // 1. 找到中心方块的BlockEntity（获取门的状态）
        BlockPos centerPos = getCenterPos(state, pos);
        DeepBlueLabAccessControlDoorEntity centerEntity = (DeepBlueLabAccessControlDoorEntity) level.getBlockEntity(centerPos);
        if (centerEntity == null) return BASE_COLLISION_SHAPE;

        // 2. 根据门的状态返回碰撞体积
        if (centerEntity.getDoorState() == DeepBlueLabAccessControlDoorEntity.DoorState.OPENED) {
            return OPEN_COLLISION_SHAPE; // 开门：薄框不阻挡
        } else {
            return BASE_COLLISION_SHAPE; // 关门：完整1×1碰撞，拼接成整个门的碰撞框
        }
    }

    // 3. 鼠标对准的视觉反馈（让鼠标能选中所有占位方块，效仿灾变的getShape）
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getCollisionShape(state, level, pos, context);
    }

    // 4. 从任意占位方块找到中心方块（效仿灾变的getBasePos）
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
        for (int xz = 0; xz <= 8; xz++) {
            for (int y = 0; y <= 5; y++) {
                BlockPos placeholderPos = getPlaceholderPos(centerPos, facing, xz, y);
                if (level.getBlockState(placeholderPos).is(this)) {
                    level.setBlock(placeholderPos, Blocks.AIR.defaultBlockState(), 35);
                    level.levelEvent(player, 2001, placeholderPos, Block.getId(level.getBlockState(placeholderPos)));
                }
            }
        }
    }
}
