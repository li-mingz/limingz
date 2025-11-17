package com.limingz.mymod.block;

import com.limingz.mymod.block.entity.DeepBlueLabAccessControlDoorEntity;
import com.limingz.mymod.network.Channel;
import com.limingz.mymod.network.packet.playertoserver.ClientToServerDoorTickPacket;
import com.limingz.mymod.register.BlockEntityRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;


public class DeepBlueLabAccessControlDoor extends BaseEntityBlock{
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

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
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {

        if (level.getBlockEntity(pos) instanceof DeepBlueLabAccessControlDoorEntity doorEntity) {
            if(!level.isClientSide){
                // 切换门的状态
                doorEntity.toggleDoor();
            } else {
                Channel.INSTANCE.sendToServer(new ClientToServerDoorTickPacket(pos, doorEntity.getAnimationLength()-doorEntity.getAnimationTick(), doorEntity.getDoorState()));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

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
