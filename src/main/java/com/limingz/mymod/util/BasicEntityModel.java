package com.limingz.mymod.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.function.Function;

/**
 *
 * 借鉴自 "灾变" mod
 * https://github.com/lender544/Lionfish-API/blob/master/src/main/java/com/github/L_Ender/lionfishapi/client/model/tools/BasicEntityModel.java
 */
public abstract class BasicEntityModel<T extends Entity> extends EntityModel<T> {
    public int textureWidth = 32;
    public int textureHeight = 32;

    protected BasicEntityModel() {
        // 默认使用无剔除的渲染类型
        this(RenderType::entityCutoutNoCull);
    }

    protected BasicEntityModel(Function<ResourceLocation, RenderType> function) {
        // 调用父类构造器，指定渲染类型
        super(function);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha) {
        this.parts().forEach((basicModelPart) -> {
            basicModelPart.render(poseStack, vertexConsumer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
        });
    }
    // 返回模型所有部件的集合（如头、身体、四肢）
    public abstract Iterable<BasicModelPart> parts();

    /**
     * 实体动画相关
     * @param pEntity 目标实体实例
     * @param pLimbSwing 肢体摆动时间计数器（用于行走动画）
     * @param pLimbSwingAmount 肢体摆动幅度（0~1）
     * @param pAgeInTicks 实体存在时间（刻）
     * @param pNetHeadYaw 头部水平旋转角度（弧度）
     * @param pHeadPitch 头部垂直旋转角度（弧度）
     */
    @Override
    public abstract void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch);

    @Override
    public void prepareMobModel(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick) {
    }
}