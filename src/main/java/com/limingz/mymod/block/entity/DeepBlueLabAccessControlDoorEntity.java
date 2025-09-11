package com.limingz.mymod.block.entity;

import com.limingz.mymod.mixins.AnimationControllerAccess;
import com.limingz.mymod.register.BlockEntityRegister;
import com.limingz.mymod.util.PauseTick;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class DeepBlueLabAccessControlDoorEntity extends BlockEntity implements GeoBlockEntity {
    protected static final RawAnimation OPEN_ANIM = RawAnimation.begin().thenPlay("animation.deep_blue_lab_access_control_door.opening");

    protected static final RawAnimation CLOSE_ANIM = RawAnimation.begin().thenPlay("animation.deep_blue_lab_access_control_door.closing");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    // 门的状态
    private enum DoorState {
        OPENED,
        CLOSED,
        OPENING,
        CLOSING
    }
    private DoorState doorState = DoorState.OPENING;
    // 当前动画的帧数
    private double animationTick = 0;

    public DeepBlueLabAccessControlDoorEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntityRegister.deep_blue_lab_access_control_door_entity.get(), pPos, pBlockState);
    }

    // 切换门的状态
    public void toggleDoor() {
        if (doorState == DoorState.CLOSED || doorState == DoorState.CLOSING) {
            // 如果是关门状态或正在关门，改为开门
            doorState = DoorState.OPENING;
        } else if (doorState == DoorState.OPENED || doorState == DoorState.OPENING) {
            // 如果是开门状态或正在开门，改为关门
            doorState = DoorState.CLOSING;
        }
        // 同步数据到客户端
        setChanged();
        if (level != null) {
            // 立即同步状态
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL_IMMEDIATE);
        }

    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        AnimationController<DeepBlueLabAccessControlDoorEntity> animationController = new AnimationController<>(this, "door_controller", 0, this::doorAnimController);
        controllers.add(animationController);
    }
    protected PlayState doorAnimController(final AnimationState<DeepBlueLabAccessControlDoorEntity> state) {

        AnimationController<DeepBlueLabAccessControlDoorEntity> controller = state.getController();

        // 通过门的状态决定播放动画
        if(doorState == DoorState.OPENING){
            controller.setAnimation(OPEN_ANIM);
        } else if(doorState == DoorState.CLOSING){
            controller.setAnimation(CLOSE_ANIM);
        } else if(doorState == DoorState.OPENED){
            controller.setAnimation(OPEN_ANIM);
        } else if(doorState == DoorState.CLOSED){
            controller.setAnimation(CLOSE_ANIM);
        }

        // 动画加载完后再获取
        if (controller.getCurrentAnimation() != null) {
            // 记录当前进度
            double animationLength = controller.getCurrentAnimation().animation().length();
            animationTick = state.getAnimationTick();
            // 更新动画状态
            if (doorState == DoorState.OPENING || doorState == DoorState.CLOSING) {
                // 检查动画是否完成
                if (doorState == DoorState.OPENING && animationTick >= animationLength) {
                    doorState = DoorState.OPENED;
                } else if(doorState == DoorState.CLOSING && animationTick >= animationLength) {
                    doorState = DoorState.CLOSED;
                }
            }
        }

        return PlayState.CONTINUE;
    }

    // 数据同步
    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        super.onDataPacket(net, pkt);
        if (pkt.getTag() != null) {
            load(pkt.getTag()); // 客户端接收数据时更新
        }
    }

    // 保存/加载状态
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("doorState", doorState.name());
        tag.putDouble("animationTick", animationTick);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        doorState = DoorState.valueOf(tag.getString("doorState"));
        animationTick = tag.getDouble("animationTick");
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        load(tag); // 客户端加载同步的数据
    }
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public double getTick(Object blockEntity) {
        return PauseTick.pauseTick;
    }
}
