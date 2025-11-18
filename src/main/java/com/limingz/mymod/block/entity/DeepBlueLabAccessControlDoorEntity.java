package com.limingz.mymod.block.entity;

import com.limingz.mymod.Main;
import com.limingz.mymod.block.DeepBlueLabAccessControlDoor;
import com.limingz.mymod.block.util.DeepBlueLabAccessControlDoorAutoSensor;
import com.limingz.mymod.gui.holographic_ui.interfaces.AnimatedPngHolder;
import com.limingz.mymod.gui.holographic_ui.renderer.ui.system.AnimatedPng;
import com.limingz.mymod.gui.holographic_ui.util.AnimatedPngState;
import com.limingz.mymod.gui.holographic_ui.util.PngState;
import com.limingz.mymod.mixins_access.AnimationControllerAccess;
import com.limingz.mymod.network.Channel;
import com.limingz.mymod.network.packet.servertoplayer.ServerToClientDoorTickPacket;
import com.limingz.mymod.register.BlockEntityRegister;
import com.limingz.mymod.util.PauseTick;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
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


    public AABB SOUTH_AND_NORTH_MAX_BOUNDING_BOX;
    public AABB EAST_AND_WEST_MAX_BOUNDING_BOX;

    // 存储每个组件的独立状态（key：组件id，value：状态）
    private final Map<String, AnimatedPngState> componentStates = new HashMap<>();
    private final Map<String, PngState> pngStates = new HashMap<>();
    private AnimatedPngState aside_closeAnimatedPng = new AnimatedPngState();
    private AnimatedPngState aside_openAnimatedPng = new AnimatedPngState();
    private AnimatedPngState aside_ro_openAnimatedPng = new AnimatedPngState();
    private AnimatedPngState iconAnimatedPng = new AnimatedPngState();
    private AnimatedPngState iconCloseAnimatedPng = new AnimatedPngState();
    private AnimatedPngState centerAnimatedPng = new AnimatedPngState();

    private PngState asidePng = new PngState();
    private PngState aside2Png = new PngState();

    private final DeepBlueLabAccessControlDoorAutoSensor autoSensor;


    // 回调函数实例Map
    public Map<String, AnimatedPngState.OnPlayOnceFinished> onPlayOnceFinishedMap;


    protected static final RawAnimation OPEN_AND_CLOSE_ANIM = RawAnimation.begin().thenPlay("animation.deep_blue_lab_access_control_door.open_and_close");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // 门的状态
    public enum DoorState {
        OPENED,
        CLOSED,
        OPENING,
        CLOSING
    }

    private DoorState doorState = DoorState.CLOSED;
    // 当前动画的帧数
    private double animationTick = 0;
    // 当前动画的长度
    private double animationLength = 600;  // 30 * 20

    // 是否需要加载动画的帧数
    private Boolean needLoadTick = false;

    public DeepBlueLabAccessControlDoorEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntityRegister.deep_blue_lab_access_control_door_entity.get(), pPos, pBlockState);

        SOUTH_AND_NORTH_MAX_BOUNDING_BOX = new AABB(
                pPos.getX()-4.0D, pPos.getY(), pPos.getZ(),
                pPos.getX()+5.0D, pPos.getY()+6.0D, pPos.getZ()+1.0D
        );
        EAST_AND_WEST_MAX_BOUNDING_BOX = new AABB(
                pPos.getX()+0.0D, pPos.getY(), pPos.getZ()-4.0D,
                pPos.getX()+1.0D, pPos.getY()+6.0D, pPos.getZ()+5.0D
        );


        onPlayOnceFinishedMap =  new HashMap<>();

        this.autoSensor = new DeepBlueLabAccessControlDoorAutoSensor(this);

        // 初始化AnimatedPng状态
        iconAnimatedPng.setPlayMode(AnimatedPngState.PlayMode.PLAY_ONCE);
        iconCloseAnimatedPng.setPlayMode(AnimatedPngState.PlayMode.PLAY_ONCE);
        aside_ro_openAnimatedPng.setPlayMode(AnimatedPngState.PlayMode.PLAY_ONCE);

        iconAnimatedPng.setShowState(false);
        aside_openAnimatedPng.setShowState(false);
        aside_closeAnimatedPng.setShowState(false);
        aside_ro_openAnimatedPng.setShowState(false);

        componentStates.put("aside_closeAnimatedPng", aside_closeAnimatedPng);
        componentStates.put("aside_openAnimatedPng", aside_openAnimatedPng);
        componentStates.put("aside_ro_openAnimatedPng", aside_ro_openAnimatedPng);
        componentStates.put("iconAnimatedPng", iconAnimatedPng);
        componentStates.put("iconCloseAnimatedPng", iconCloseAnimatedPng);
        componentStates.put("centerAnimatedPng", centerAnimatedPng);

        pngStates.put("asidePng", asidePng);
        pngStates.put("aside2Png", aside2Png);


        this.onPlayOnceFinishedMap.put("doorOpened", end -> {
            aside_openAnimatedPng.resetAnimation();
            aside_openAnimatedPng.setShowState(true);
            aside_ro_openAnimatedPng.setShowState(false);
        });
        this.onPlayOnceFinishedMap.put("doorClosed", end -> {
            aside_closeAnimatedPng.resetAnimation();
            aside_closeAnimatedPng.setShowState(true);
            aside_ro_openAnimatedPng.setShowState(false);
        });
    }


    /**
     * 执行单次播放结束后调用的回调函数
     */
    public void executeOnPlayOnceFinishedCallback(String targetId, AnimatedPng animatedPng) {
        AnimatedPngState target = componentStates.get(targetId);
        while (!target.onPlayOnceFinishedExecuteList.isEmpty()){
            onPlayOnceFinishedMap.get(target.onPlayOnceFinishedExecuteList.pop()).onFinished(animatedPng);
        }
    }

    private void doorOpeningAnimate(){
        iconAnimatedPng.setShowState(true);
        iconAnimatedPng.resetAnimation();
        iconCloseAnimatedPng.setShowState(false);

        asidePng.setShowState(false);
        aside2Png.setShowState(false);
        aside_closeAnimatedPng.setShowState(false);
        aside_ro_openAnimatedPng.setShowState(true);
        aside_ro_openAnimatedPng.setDirection(1);
        aside_ro_openAnimatedPng.resetAnimation();
        aside_ro_openAnimatedPng.clearOnPlayOnceFinishedExecuteList();
        aside_ro_openAnimatedPng.addOnPlayOnceFinishedExecuteName("doorOpened");
    }
    private void doorClosingAnimate(){
        iconCloseAnimatedPng.setShowState(true);
        iconCloseAnimatedPng.resetAnimation();
        iconAnimatedPng.setShowState(false);

        asidePng.setShowState(false);
        aside2Png.setShowState(false);
        aside_openAnimatedPng.setShowState(false);
        aside_ro_openAnimatedPng.setShowState(true);
        aside_ro_openAnimatedPng.setDirection(-1);
        aside_ro_openAnimatedPng.resetAnimation();
        aside_ro_openAnimatedPng.clearOnPlayOnceFinishedExecuteList();
        aside_ro_openAnimatedPng.addOnPlayOnceFinishedExecuteName("doorClosed");
    }
    private void doorClosedAnimate(){
        iconCloseAnimatedPng.setShowState(true);
        iconCloseAnimatedPng.playEndFrame();
        iconAnimatedPng.setShowState(false);

        asidePng.setShowState(true);
        aside2Png.setShowState(false);
        aside_openAnimatedPng.setShowState(false);
        aside_closeAnimatedPng.setShowState(false);
        aside_ro_openAnimatedPng.setShowState(false);
        aside_ro_openAnimatedPng.clearOnPlayOnceFinishedExecuteList();
    }
    private void doorOpenedAnimate(){
        iconAnimatedPng.setShowState(true);
        iconAnimatedPng.playEndFrame();
        iconCloseAnimatedPng.setShowState(false);

        asidePng.setShowState(false);
        aside2Png.setShowState(true);
        aside_openAnimatedPng.setShowState(false);
        aside_closeAnimatedPng.setShowState(false);
        aside_ro_openAnimatedPng.setShowState(false);
        aside_ro_openAnimatedPng.clearOnPlayOnceFinishedExecuteList();
    }
    public void switchDoorState(){
        if (doorState == DoorState.CLOSING) {
            doorClosingAnimate();
        } else if (doorState == DoorState.OPENING) {
            doorOpeningAnimate();
        } else if (doorState == DoorState.OPENED) {
            doorOpenedAnimate();
        } else {
            doorClosedAnimate();
        }
    }
    public void openDoor(){
        doorState = DoorState.OPENING;
        doorOpeningAnimate();
        // 反转动画进度实现平滑过渡
        animationTick = animationLength - animationTick;
        // 同步数据到客户端
        Channel.sendToNearby(new ServerToClientDoorTickPacket(worldPosition, animationTick, doorState), this.worldPosition, (ServerLevel) this.level);
    }
    public void closeDoor(){
        doorState = DoorState.CLOSING;
        doorClosingAnimate();
        // 反转动画进度实现平滑过渡
        animationTick = animationLength - animationTick;
        // 同步数据到客户端
        Channel.sendToNearby(new ServerToClientDoorTickPacket(worldPosition, animationTick, doorState), this.worldPosition, (ServerLevel) this.level);
    }
    @Override
    public Map<String, AnimatedPngState> getAnimatedState() {
        return componentStates;
    }

    @Override
    public Map<String, PngState> getPngState() {
        return pngStates;
    }


    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        AnimationController<DeepBlueLabAccessControlDoorEntity> animationController = new AnimationController<>(this, "door_controller", 0, this::doorAnimController);
        controllers.add(animationController);
    }
    // 客户端动画
    protected PlayState doorAnimController(final AnimationState<DeepBlueLabAccessControlDoorEntity> state) {
        AnimationController<DeepBlueLabAccessControlDoorEntity> controller = state.getController();
        AnimationControllerAccess animationControllerAccess = (AnimationControllerAccess) controller;
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
            } else {
                // 计算当前动画帧
                animationTick = Math.min(state.getAnimationTick()-offsetTick, animationLength) ;
            }
                if (doorState == DoorState.OPENING || doorState == DoorState.CLOSING) {
                // 检查动画是否完成
                if (doorState == DoorState.OPENING && animationTick >= animationLength/2) {
                    doorState = DoorState.OPENED;
                    switchDoorState();
                } else if(doorState == DoorState.CLOSING && animationTick >= animationLength) {
                    doorState = DoorState.CLOSED;
                    switchDoorState();
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
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        doorState = DoorState.valueOf(tag.getString("doorState"));
        animationTick = tag.getDouble("animationTick");
        needLoadTick = true;
        switchDoorState();
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

    public DoorState getDoorState() {
        return doorState;
    }
    public void setDoorState(DoorState doorState) {
        this.doorState = doorState;
    }

    public void setNeedLoadTick(Boolean needLoadTick) {
        this.needLoadTick = needLoadTick;
    }

    public void clientTick() {
        autoSensor.handleTick();
    }
    public void serverTick() {
        autoSensor.handleTick();
        // 服务端动画帧自增
        if (doorState == DoorState.OPENING || doorState == DoorState.CLOSING) {
            // 检查动画是否完成
            if (doorState == DoorState.OPENING && animationTick >= animationLength/2) {
                doorState = DoorState.OPENED;
                // 同步开门状态
                Channel.sendToNearby(new ServerToClientDoorTickPacket(worldPosition, animationTick, doorState), this.worldPosition, (ServerLevel) this.level);
                switchDoorState();
            } else if(doorState == DoorState.CLOSING && animationTick >= animationLength) {
                doorState = DoorState.CLOSED;
                // 同步关门状态
                Channel.sendToNearby(new ServerToClientDoorTickPacket(worldPosition, animationTick, doorState), this.worldPosition, (ServerLevel) this.level);
                switchDoorState();
            } else {
                animationTick += 1D;
                Main.LOGGER.info(String.valueOf(animationTick));
            }
        }
        if(doorState == DoorState.CLOSED){
            animationTick = animationLength;
        } else if(doorState == DoorState.OPENED){
            animationTick = animationLength/2;
        }
    }


    @Override
    public AABB getRenderBoundingBox() {
        Direction doorFacing = this.getBlockState().getValue(DeepBlueLabAccessControlDoor.FACING);
        return switch (doorFacing) {
            case EAST, WEST -> EAST_AND_WEST_MAX_BOUNDING_BOX;
            case SOUTH, NORTH -> SOUTH_AND_NORTH_MAX_BOUNDING_BOX;
            default -> new AABB(0, 0, 0, 1, 1, 1);
        };
    }

}
