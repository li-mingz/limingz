package com.limingz.mymod.gui.holographic_ui.event;

import com.limingz.mymod.block.entity.DemoBlockEntity;
import com.limingz.mymod.gui.holographic_ui.config.UIConfig;
import com.limingz.mymod.gui.holographic_ui.renderer.blockentity.DemoBlockEntityRenderer;
import com.limingz.mymod.gui.holographic_ui.util.BlockEntityUtils;
import com.limingz.mymod.gui.holographic_ui.util.RaycastingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

import java.util.List;

import static com.limingz.mymod.Main.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEventListener {
    private static final int MAX_DISTANCE = 5;

    @SubscribeEvent
    public static void onMouseClick(InputEvent.InteractionKeyMappingTriggered event) {
        if (event.getKeyMapping() != Minecraft.getInstance().options.keyUse) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        Vec3 eyePos = mc.player.getEyePosition();
        Vec3 viewVec = mc.player.getViewVector(1.0F);

        List<DemoBlockEntity> entities = BlockEntityUtils.getBlockEntitiesNearPlayerByType(
                mc.player, MAX_DISTANCE, DemoBlockEntity.class);

        DemoBlockEntity closestHit = null;
        double closestDistance = Double.MAX_VALUE;

        for (DemoBlockEntity be : entities) {
            BlockEntityRenderDispatcher dispatcher = mc.getBlockEntityRenderDispatcher();
            // 通过方块实体实例获取渲染器
            DemoBlockEntityRenderer blockentityrenderer = (DemoBlockEntityRenderer) dispatcher.getRenderer(be);
            Vec3 quadCenter = Vec3.atCenterOf(be.getBlockPos()).add(blockentityrenderer.demoScreen.getTranslate()[0]-0.5f, blockentityrenderer.demoScreen.getTranslate()[1]-0.5f, blockentityrenderer.demoScreen.getTranslate()[2]-0.5f);
            Vector3f normal = new Vector3f(0, 1, 0);
            normal.rotateX((float) Math.toRadians(UIConfig.BG_ROTATION));

            Vec3 hitPos = RaycastingHelper.calculateIntersection(eyePos, viewVec, quadCenter,
                    new Vec3(normal.x(), normal.y(), normal.z()), MAX_DISTANCE);

            if(hitPos != null) {
                float[] hitPos2 = RaycastingHelper.isPointInQuad(hitPos, quadCenter, normal, blockentityrenderer.demoScreen);
                if (hitPos != null && hitPos2 != null) {
//                    spawnParticlesAtPosition(mc, hitPos);
                    double distance = hitPos.distanceTo(eyePos);
                    if (distance < closestDistance) {
                        closestDistance = distance;
                        closestHit = be;
                    }
                }
            }


        }

        if (closestHit != null) {
            // PacketHandler.sendToServer(new UIClickPacket(closestHit.getBlockPos()));
            event.setCanceled(true);
        }
    }

    private static void spawnParticlesAtPosition(Minecraft mc, Vec3 position) {
        if (mc.level == null) return;
        mc.level.addParticle(ParticleTypes.WAX_ON, position.x(), position.y(), position.z(), 0, 0, 0);
    }

}