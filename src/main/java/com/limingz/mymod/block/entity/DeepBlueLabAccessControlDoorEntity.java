package com.limingz.mymod.block.entity;

import com.limingz.mymod.register.BlockEntityRegister;
import com.limingz.mymod.util.PauseTick;
import net.minecraft.core.BlockPos;
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
    protected static final RawAnimation OPENING_ANIM = RawAnimation.begin().thenLoop("animation.deep_blue_lab_access_control_door.opening");

//    protected static final RawAnimation OPED_ANIM = RawAnimation.begin().thenPlay("animation.deep_blue_lab_access_control_door.opened");

    protected static final RawAnimation TEST_ANIM = RawAnimation.begin();

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public DeepBlueLabAccessControlDoorEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntityRegister.deep_blue_lab_access_control_door_entity.get(), pPos, pBlockState);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, this::deployAnimController));
    }
    protected PlayState deployAnimController(final AnimationState<DeepBlueLabAccessControlDoorEntity> state) {
        return state.setAndContinue(OPENING_ANIM);
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
