package com.limingz.mymod.block.entity;

import com.limingz.mymod.gui.holographic_ui.interfaces.AnimatedPngHolder;
import com.limingz.mymod.gui.holographic_ui.util.AnimatedPngState;
import com.limingz.mymod.mixins_access.AnimationControllerAccess;
import com.limingz.mymod.network.Channel;
import com.limingz.mymod.network.packet.playertoserver.DoorTickPacket;
import com.limingz.mymod.register.BlockEntityRegister;
import com.limingz.mymod.util.PauseTick;
import net.minecraft.client.Minecraft;
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

import java.util.HashMap;
import java.util.Map;

public class DeepBlueLabAccessControlDoorEntity extends BlockEntity implements GeoBlockEntity, AnimatedPngHolder {
    // 存储每个组件的独立状态（key：组件id，value：状态）
    private final Map<String, AnimatedPngState> componentStates = new HashMap<>();
    private AnimatedPngState aside_closeAnimatedPng = new AnimatedPngState();
    private AnimatedPngState aside_openAnimatedPng = new AnimatedPngState();
    private AnimatedPngState aside_ro_openAnimatedPng = new AnimatedPngState();
    private AnimatedPngState iconAnimatedPng = new AnimatedPngState();
    private AnimatedPngState iconCloseAnimatedPng = new AnimatedPngState();
    private AnimatedPngState centerAnimatedPng = new AnimatedPngState();


    protected static final RawAnimation OPEN_AND_CLOSE_ANIM = RawAnimation.begin().thenPlay("animation.deep_blue_lab_access_control_door.open_and_close");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // 门的状态
    private enum DoorState {
        OPENED,
        CLOSED,
        OPENING,
        CLOSING
    }
    private DoorState doorState = DoorState.CLOSED;
    // 当前动画的帧数
    private double animationTick = 0;
    // 当前动画的长度
    private double animationLength = 0;
    // 是否需要加载动画的帧数
    private Boolean needLoadTick = false;

    public DeepBlueLabAccessControlDoorEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntityRegister.deep_blue_lab_access_control_door_entity.get(), pPos, pBlockState);
        // 初始化AnimatedPng状态
        iconAnimatedPng.setPlayMode(AnimatedPngState.PlayMode.PLAY_ONCE);
        iconCloseAnimatedPng.setPlayMode(AnimatedPngState.PlayMode.PLAY_ONCE);
        aside_openAnimatedPng.setPlayMode(AnimatedPngState.PlayMode.PLAY_ONCE);
        aside_ro_openAnimatedPng.setPlayMode(AnimatedPngState.PlayMode.PLAY_ONCE);

        iconAnimatedPng.setShowState(false);
        aside_closeAnimatedPng.setShowState(false);
        aside_ro_openAnimatedPng.setShowState(false);

        componentStates.put("aside_closeAnimatedPng", aside_closeAnimatedPng);
        componentStates.put("aside_openAnimatedPng", aside_openAnimatedPng);
        componentStates.put("aside_ro_openAnimatedPng", aside_ro_openAnimatedPng);
        componentStates.put("iconAnimatedPng", iconAnimatedPng);
        componentStates.put("iconCloseAnimatedPng", iconCloseAnimatedPng);
        componentStates.put("centerAnimatedPng", centerAnimatedPng);
    }

    @Override
    public Map<String, AnimatedPngState> getAnimatedState() {
        return componentStates;
    }

    // 切换门的状态
    public void toggleDoor() {
        if (doorState == DoorState.CLOSED || doorState == DoorState.CLOSING) {
            // 如果是关门状态或正在关门，改为开门
            doorState = DoorState.OPENING;
            iconAnimatedPng.setShowState(true);
            iconAnimatedPng.resetAnimation();
            iconCloseAnimatedPng.setShowState(false);
        } else if (doorState == DoorState.OPENED || doorState == DoorState.OPENING) {
            // 如果是开门状态或正在开门，改为关门
            doorState = DoorState.CLOSING;
            iconCloseAnimatedPng.setShowState(true);
            iconCloseAnimatedPng.resetAnimation();
            iconAnimatedPng.setShowState(false);
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
        AnimationControllerAccess animationControllerAccess = (AnimationControllerAccess) controller;
        Minecraft mc = Minecraft.getInstance();
        controller.setAnimation(OPEN_AND_CLOSE_ANIM);
        // 动画加载完后再获取
        if (controller.getCurrentAnimation() != null) {
            // 记录当前进度
            animationLength = controller.getCurrentAnimation().animation().length();
            double offsetTick = animationControllerAccess.getTickOffset();
            if(needLoadTick){
                // 加载动画帧
                animationControllerAccess.setAnimationTick(animationTick);
                needLoadTick = false;
            } else if(!mc.isPaused()) {
                // 加载时不需要计算动画帧
                // 暂停时不更新动画帧
                // 计算当前动画帧
                animationTick = Math.min(state.getAnimationTick()-offsetTick, animationLength) ;
            } else {
                // 暂停时强制设置动画帧,防止串动画
                animationControllerAccess.setAnimationTick(animationTick);
            }
            // 更新动画状态,仅非状态状态更新
            // 暂停时会串动画，也不知道为什么
            if ((doorState == DoorState.OPENING || doorState == DoorState.CLOSING)&&!mc.isPaused()) {
                // 检查动画是否完成
                if (doorState == DoorState.OPENING && animationTick >= animationLength/2) {
                    doorState = DoorState.OPENED;
                    // 同步开门状态
                    Channel.INSTANCE.sendToServer(new DoorTickPacket(worldPosition, animationLength/2));
                } else if(doorState == DoorState.CLOSING && animationTick >= animationLength) {
                    doorState = DoorState.CLOSED;
                    // 同步关门状态
                    Channel.INSTANCE.sendToServer(new DoorTickPacket(worldPosition, animationLength));
                }
            }
        }
        if(doorState == DoorState.CLOSED){
            animationTick = animationLength;
            animationControllerAccess.setAnimationTick(animationTick);
        } else if(doorState == DoorState.OPENED){
            animationTick = animationLength/2;
            animationControllerAccess.setAnimationTick(animationTick);
        }
        return PlayState.CONTINUE;
    }



    public Double getAnimationTick() {
        return animationTick;
    }
    public void setAnimationTick(double animationTick) {
        this.animationTick = animationTick;
    }

    public double getAnimationLength() {
        return animationLength;
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

        // 保存所有AnimatedPngState
        CompoundTag componentsTag = new CompoundTag();
        for (Map.Entry<String, AnimatedPngState> entry : componentStates.entrySet()) {
            String componentId = entry.getKey();
            AnimatedPngState state = entry.getValue();
            componentsTag.put(componentId, state.saveToTag()); // 每个组件状态存入子标签
        }
        tag.put("componentStates", componentsTag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        doorState = DoorState.valueOf(tag.getString("doorState"));
        animationTick = tag.getDouble("animationTick");
        needLoadTick = true;

        // 加载所有AnimatedPngState
        CompoundTag componentsTag = tag.getCompound("componentStates");
        componentStates.clear(); // 先清空现有数据
        for (String componentId : componentsTag.getAllKeys()) {
            CompoundTag stateTag = componentsTag.getCompound(componentId);
            AnimatedPngState state = new AnimatedPngState();
            state.loadFromTag(stateTag); // 从标签恢复状态
            componentStates.put(componentId, state);
        }
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
        return PauseTick.getTick();
    }

}
