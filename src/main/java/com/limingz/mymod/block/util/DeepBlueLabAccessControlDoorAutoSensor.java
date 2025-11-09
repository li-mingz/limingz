package com.limingz.mymod.block.util;

import com.limingz.mymod.block.entity.DeepBlueLabAccessControlDoorEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

/**
 * 深蓝实验室大门门自动感应
 */
public class DeepBlueLabAccessControlDoorAutoSensor {
    public static final double DETECT_RADIUS = 3.5D; // 玩家检测距离（格）
    public static final int OPEN_DELAY = 20; // 开门延迟（20 tick = 1 秒）
    public static final int CLOSE_DELAY = 20; // 关门延迟（40 tick = 2 秒）
    public static final int SYNC_DELAY = 2; // 同步延迟

    public static final boolean FORCE_SHOW_PARTICLE = true; // 强制显示调试粒子
    public static final int PARTICLE_INTERVAL = 2; // 粒子生成间隔（减少卡顿）
    public static final int EDGE_POINT_COUNT = 8; // 每条边的粒子数量（边界清晰度）

    private final DeepBlueLabAccessControlDoorEntity doorEntity; // 关联的门实体
    private int openDelayTimer = 0; // 开门延迟计时器
    private int closeDelayTimer = 0; // 关门延迟计时器
    private int particleTimer = 0; // 粒子生成计时器（控制间隔）

    public DeepBlueLabAccessControlDoorAutoSensor(DeepBlueLabAccessControlDoorEntity doorEntity) {
        this.doorEntity = doorEntity;
    }

    public void handleTick() {
        Level level = doorEntity.getLevel();
        BlockPos pos = doorEntity.getBlockPos();

        if (level == null || pos == null) return;

        // 客户端仅处理调试粒子渲染
        if (level.isClientSide()) {
            spawnDebugParticles();
            return;
        }

        // 服务端处理玩家检测和门状态切换
        if (level instanceof ServerLevel serverLevel) {
            boolean hasPlayerNearby = detectNearbyPlayers(serverLevel);
            updateDoorState(hasPlayerNearby);
        }
    }

    // 玩家检测
    private boolean detectNearbyPlayers(ServerLevel serverLevel) {
        BlockPos pos = doorEntity.getBlockPos();
        // 以门中心为原点，构建检测范围 AABB
        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 3D;  // 门中心
        double centerZ = pos.getZ() + 0.5D;

        AABB detectArea = new AABB(
                centerX - DETECT_RADIUS,
                centerY - DETECT_RADIUS,
                centerZ - DETECT_RADIUS,
                centerX + DETECT_RADIUS + 1,
                centerY + DETECT_RADIUS + 1,
                centerZ + DETECT_RADIUS + 1
        );

        // 过滤存活、非旁观者的玩家
        List<Player> nearbyPlayers = serverLevel.getEntitiesOfClass(
                Player.class,
                detectArea,
                player -> !player.isSpectator() && player.isAlive()
        );

        return !nearbyPlayers.isEmpty();
    }

    // 服务端：门状态更新逻辑
    private void updateDoorState(boolean hasPlayerNearby) {
        DeepBlueLabAccessControlDoorEntity.DoorState currentState = doorEntity.getDoorState();
        if (hasPlayerNearby) {
            // 玩家在范围内：准备开门

            closeDelayTimer = 0; // 重置关门延迟
            if (currentState == DeepBlueLabAccessControlDoorEntity.DoorState.OPENING ||
                    currentState == DeepBlueLabAccessControlDoorEntity.DoorState.OPENED) {
                // 开门状态则跳过
                return;
            }

            openDelayTimer++;
            if (openDelayTimer == OPEN_DELAY) {
                // 同步客户端动画帧
                doorEntity.sendNearbyPacketGetClientPacket();
            }
            if (openDelayTimer >= OPEN_DELAY+SYNC_DELAY) {
                // 开门
                doorEntity.openDoor();
            }
        } else {
            // 玩家不在范围内：准备关门
            openDelayTimer = 0; // 重置开门延迟
            if (currentState == DeepBlueLabAccessControlDoorEntity.DoorState.CLOSING ||
                    currentState == DeepBlueLabAccessControlDoorEntity.DoorState.CLOSED) {
                // 关门状态则跳过
                return;
            }
            closeDelayTimer++;
            if (closeDelayTimer == CLOSE_DELAY) {
                // 同步客户端动画帧
                doorEntity.sendNearbyPacketGetClientPacket();
            }
            if (closeDelayTimer >= CLOSE_DELAY+SYNC_DELAY) {
                // 关门
                doorEntity.closeDoor();
            }
        }
    }

    // ==================== 客户端：调试粒子渲染（仅调试模式）====================
    @OnlyIn(Dist.CLIENT)
    private void spawnDebugParticles() {
        Minecraft mc = Minecraft.getInstance();
        boolean isDebugMode = mc.options.renderDebug;
        // 粒子显示条件：强制显示 或 打开F3调试模式
        if (!FORCE_SHOW_PARTICLE && !isDebugMode) {
            return;
        }

        // 控制粒子生成间隔（避免每tick生成过多）
        particleTimer++;
        if (particleTimer % PARTICLE_INTERVAL != 0) {
            return;
        }

        BlockPos pos = doorEntity.getBlockPos();
        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 3D;
        double centerZ = pos.getZ() + 0.5D;

        // 构建立方体8个顶点（检测范围边界）
        Vec3[] vertices = new Vec3[8];
        vertices[0] = new Vec3(centerX - DETECT_RADIUS, centerY - DETECT_RADIUS, centerZ - DETECT_RADIUS);
        vertices[1] = new Vec3(centerX + DETECT_RADIUS, centerY - DETECT_RADIUS, centerZ - DETECT_RADIUS);
        vertices[2] = new Vec3(centerX + DETECT_RADIUS, centerY + DETECT_RADIUS, centerZ - DETECT_RADIUS);
        vertices[3] = new Vec3(centerX - DETECT_RADIUS, centerY + DETECT_RADIUS, centerZ - DETECT_RADIUS);
        vertices[4] = new Vec3(centerX - DETECT_RADIUS, centerY - DETECT_RADIUS, centerZ + DETECT_RADIUS);
        vertices[5] = new Vec3(centerX + DETECT_RADIUS, centerY - DETECT_RADIUS, centerZ + DETECT_RADIUS);
        vertices[6] = new Vec3(centerX + DETECT_RADIUS, centerY + DETECT_RADIUS, centerZ + DETECT_RADIUS);
        vertices[7] = new Vec3(centerX - DETECT_RADIUS, centerY + DETECT_RADIUS, centerZ + DETECT_RADIUS);

        // 立方体12条边（连接顶点）
        int[][] edges = {
                {0, 1}, {1, 2}, {2, 3}, {3, 0}, // 前面
                {4, 5}, {5, 6}, {6, 7}, {7, 4}, // 后面
                {0, 4}, {1, 5}, {2, 6}, {3, 7}  // 侧面
        };

        // 沿每条边生成粒子
        for (int[] edge : edges) {
            spawnParticlesAlongLine(vertices[edge[0]], vertices[edge[1]]);
        }
    }

    // ==================== 客户端：沿线段均匀生成粒子 ====================
    @OnlyIn(Dist.CLIENT)
    private void spawnParticlesAlongLine(Vec3 start, Vec3 end) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        // 计算线段步长（均匀分布粒子）
        double dx = (end.x - start.x) / EDGE_POINT_COUNT;
        double dy = (end.y - start.y) / EDGE_POINT_COUNT;
        double dz = (end.z - start.z) / EDGE_POINT_COUNT;

        // 生成红色调试粒子（关键修复：使用 DustParticleOptions 替代强制转换）
        // 方案1：使用默认红色粒子（推荐，简洁）
        DustParticleOptions redParticle = DustParticleOptions.REDSTONE;
        // 方案2：自定义颜色和大小（例如：深红色，大小1.0F）
        // DustParticleOptions redParticle = new DustParticleOptions(new Vec3(0.8D, 0.0D, 0.0D), 1.0F);

        for (int i = 0; i <= EDGE_POINT_COUNT; i++) {
            double x = start.x + dx * i;
            double y = start.y + dy * i;
            double z = start.z + dz * i;

            // 修复：传入正确的 ParticleOptions 实例，运动方向设为 0（静止粒子）
            mc.level.addParticle(
                    redParticle, // 正确的粒子选项（包含颜色和大小）
                    x, y, z,
                    0.0D, 0.0D, 0.0D // 粒子运动方向（0=静止，避免飘走）
            );
        }
    }
}