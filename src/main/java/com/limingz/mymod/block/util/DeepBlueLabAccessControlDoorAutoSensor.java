package com.limingz.mymod.block.util;

import com.limingz.mymod.block.DeepBlueLabAccessControlDoor;
import com.limingz.mymod.block.entity.DeepBlueLabAccessControlDoorEntity;
import com.limingz.mymod.register.ModSoundRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;

/**
 * 深蓝实验室大门门自动感应
 */
public class DeepBlueLabAccessControlDoorAutoSensor {
    public static final int OPEN_DELAY = 20; // 开门延迟（20 tick = 1 秒）
    public static final int CLOSE_DELAY = 20; // 关门延迟（20 tick = 1 秒）


    public static final boolean FORCE_SHOW_PARTICLE = true; // 强制显示调试粒子
    public static final int PARTICLE_INTERVAL = 2; // 粒子生成间隔（减少卡顿）
    public static final int EDGE_POINT_COUNT = 8; // 每条边的粒子数量（边界清晰度）

    private final DeepBlueLabAccessControlDoorEntity doorEntity; // 关联的门实体
    private final AABB doorOpenDetectionArea;  // 开门检测区域
    private final AABB doorOpenSoundDetectionArea;  // 开门音效检测区域

    private int openDelayTimer = 0; // 开门延迟计时器
    private int closeDelayTimer = 0; // 关门延迟计时器
    private int particleTimer = 0; // 粒子生成计时器（控制间隔）

    // ==================== 门前方判断区域配置（核心修改）====================
    public static final double DOOR_FRONT_AREA_WIDTH = 5.0D; // 区域宽度（格）- 覆盖门左右各0.5格
    public static final double DOOR_FRONT_AREA_DEPTH = 3.0D; // 区域深度（格）- 门前方2格内
    public static final double DOOR_FRONT_AREA_HEIGHT = 6.0D; // 区域高度（格）- 从门底部向上3格（覆盖玩家身高）
    public static final DustParticleOptions FRONT_AREA_PARTICLE = new DustParticleOptions(new Vector3f(0.0F, 1.0F, 0.0F), 1.0F); // 前方区域粒子（绿色）
    public static final DustParticleOptions DETECTION_AREA_PARTICLE = new DustParticleOptions(new Vector3f(1.0F, 0.0F, 0.0F), 1.0F); // 区域粒子

    public DeepBlueLabAccessControlDoorAutoSensor(DeepBlueLabAccessControlDoorEntity doorEntity) {
        BlockPos blockPos = doorEntity.getBlockPos();
        Direction doorFacing = doorEntity.getBlockState().getValue(DeepBlueLabAccessControlDoor.FACING);
        this.doorEntity = doorEntity;
        this.doorOpenDetectionArea = calculateOpenDoorAreaAABB(blockPos, doorFacing);
        this.doorOpenSoundDetectionArea = calculateFrontAreaAABB(blockPos, doorFacing);
    }

    public void handleTick() {
        Level level = doorEntity.getLevel();
        BlockPos pos = doorEntity.getBlockPos();

        if (level == null || pos == null) return;

        // 客户端仅处理调试粒子渲染
        if (level.isClientSide()) {
            particleTimer++;
            spawnDetectionAreaDebugParticles(doorOpenDetectionArea, DETECTION_AREA_PARTICLE);
            spawnDetectionAreaDebugParticles(doorOpenSoundDetectionArea, FRONT_AREA_PARTICLE); // 渲染门前方判断区域
            return;
        }

        // 服务端处理玩家检测和门状态切换
        if (level instanceof ServerLevel serverLevel) {
            // 获取距离门最近的玩家
            Optional<Player> players = detectNearbyPlayers(serverLevel);

            updateDoorState(players);
        }
    }

    // 玩家检测，获取最近的玩家
    private Optional<Player> detectNearbyPlayers(ServerLevel serverLevel) {
        BlockPos pos = doorEntity.getBlockPos();
        // 以门中心为原点，构建检测范围 AABB
        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 3D;  // 门中心
        double centerZ = pos.getZ() + 0.5D;


        // 过滤存活、非旁观者的玩家
        List<Player> nearbyPlayers = serverLevel.getEntitiesOfClass(
                Player.class,
                doorOpenDetectionArea,
                player -> !player.isSpectator() && player.isAlive()
        );

        if (nearbyPlayers.isEmpty()) {
            return Optional.empty();
        }

        // 找到距离门最近的玩家
        Player closestPlayer = null;
        double closestDistanceSq = Double.MAX_VALUE;
        for (Player player : nearbyPlayers) {
            double distanceSq = player.distanceToSqr(centerX, centerY, centerZ);
            if (distanceSq < closestDistanceSq) {
                closestDistanceSq = distanceSq;
                closestPlayer = player;
            }
        }

        return Optional.ofNullable(closestPlayer);
    }

    private boolean isPlayerInDoorFront(Player player) {
        // 判断玩家的碰撞箱是否与前方区域相交
        return player.getBoundingBox().intersects(doorOpenSoundDetectionArea);
    }


    // 播放开门音效
    private void playDoorOpenSound() {
        Level level = doorEntity.getLevel();
        BlockPos doorPos = doorEntity.getBlockPos();

        // 仅在服务端播放（自动同步给周围玩家）
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.playSound(
                    null, // 播放者（null表示全局播放，周围16格内玩家可听）
                    doorPos, // 音效播放位置（门的位置）
                    ModSoundRegister.DEEP_BLUE_LAB_ACCESS_CONTROL_DOOR_OPEN.get(), // 音效
                    SoundSource.BLOCKS, // 声音类别（方块音效）
                    1.0F, // 音量
                    1.0F  // 音调
            );
        }
    }
    // 根据门的位置和朝向，计算前方判断区域的 AABB
    private AABB calculateFrontAreaAABB(BlockPos doorPos, Direction facing) {
        double doorCenterX = doorPos.getX() + 0.5D;
        double doorCenterZ = doorPos.getZ() + 0.5D;

        // 区域基础参数（可自由调整）
        double halfWidth = DOOR_FRONT_AREA_WIDTH / 2.0D; // 半宽（左右对称）
        double depth = DOOR_FRONT_AREA_DEPTH; // 深度（门前方的长度）
        double minY = doorPos.getY(); // 区域底部（与门底部齐平）
        double maxY = doorPos.getY() + DOOR_FRONT_AREA_HEIGHT; // 区域顶部

        // 根据门朝向计算 X/Z 轴边界
        return switch (facing) {
            // 朝东：X轴从门右侧开始，向右延伸 depth 格；Z轴左右对称
            case EAST -> new AABB(
                    doorPos.getX() - depth, // 区域左边界（向左延伸 depth 格）
                    minY,
                    doorCenterZ - halfWidth,
                    doorPos.getX(), // 区域右边界（门的左侧）
                    maxY,
                    doorCenterZ + halfWidth
            );
            // 朝西：X轴从门左侧向左延伸 depth 格；Z轴左右对称
            case WEST -> new AABB(
                    doorPos.getX() + 1.0D, // 区域左边界（门的右侧）
                    minY,
                    doorCenterZ - halfWidth, // 区域后边界
                    doorPos.getX() + 1.0D + depth, // 区域右边界（延伸 depth 格）
                    maxY,
                    doorCenterZ + halfWidth  // 区域前边界
            );
            // 朝南：Z轴从门后侧开始，向后延伸 depth 格；X轴左右对称
            case SOUTH -> new AABB(
                    doorCenterX - halfWidth,
                    minY,
                    doorPos.getZ() - depth, // 区域前边界（向前延伸 depth 格）
                    doorCenterX + halfWidth,
                    maxY,
                    doorPos.getZ() // 区域后边界（门的前侧）
            );
            // 朝北：Z轴从门前侧向前延伸 depth 格；X轴左右对称
            case NORTH -> new AABB(
                    doorCenterX - halfWidth, // 区域左边界
                    minY,
                    doorPos.getZ() + 1.0D, // 区域前边界（门的后侧）
                    doorCenterX + halfWidth, // 区域右边界
                    maxY,
                    doorPos.getZ() + 1.0D + depth // 区域后边界（延伸 depth 格）
            );
            // 垂直朝向（理论上不会触发，留作兼容）
            default -> new AABB(doorPos);
        };
    }

    // 根据门的位置和朝向，计算开门判断区域的 AABB
    private AABB calculateOpenDoorAreaAABB(BlockPos doorPos, Direction facing) {
        double doorCenterX = doorPos.getX() + 0.5D;
        double doorCenterZ = doorPos.getZ() + 0.5D;

        // 区域基础参数（可自由调整）
        double halfWidth = DOOR_FRONT_AREA_WIDTH / 2.0D; // 半宽（左右对称）
        double depth = DOOR_FRONT_AREA_DEPTH; // 深度（门前方的长度）
        double minY = doorPos.getY(); // 区域底部（与门底部齐平）
        double maxY = doorPos.getY() + DOOR_FRONT_AREA_HEIGHT; // 区域顶部

        // 根据门朝向计算 X/Z 轴边界
        return switch (facing) {
            // 朝东：X轴从门右侧开始，向右延伸 depth 格；Z轴左右对称
            case EAST -> new AABB(
                    doorPos.getX() - depth, // 区域左边界（向左延伸 depth 格）
                    minY,
                    doorCenterZ - halfWidth,
                    doorPos.getX() + depth + 1.0D, // 区域右边界（门的左侧）
                    maxY,
                    doorCenterZ + halfWidth
            );
            // 朝西：X轴从门左侧向左延伸 depth 格；Z轴左右对称
            case WEST -> new AABB(
                    doorPos.getX() - depth, // 区域左边界（门的右侧）
                    minY,
                    doorCenterZ - halfWidth, // 区域后边界
                    doorPos.getX() + 1.0D + depth, // 区域右边界（延伸 depth 格）
                    maxY,
                    doorCenterZ + halfWidth  // 区域前边界
            );
            // 朝南：Z轴从门后侧开始，向后延伸 depth 格；X轴左右对称
            case SOUTH -> new AABB(
                    doorCenterX - halfWidth,
                    minY,
                    doorPos.getZ() - depth, // 区域前边界（向前延伸 depth 格）
                    doorCenterX + halfWidth,
                    maxY,
                    doorPos.getZ() + 1.0D + depth // 区域后边界（门的前侧）
            );
            // 朝北：Z轴从门前侧向前延伸 depth 格；X轴左右对称
            case NORTH -> new AABB(
                    doorCenterX - halfWidth, // 区域左边界
                    minY,
                    doorPos.getZ() - depth, // 区域前边界（门的后侧）
                    doorCenterX + halfWidth, // 区域右边界
                    maxY,
                    doorPos.getZ() + 1.0D + depth // 区域后边界（延伸 depth 格）
            );
            // 垂直朝向（理论上不会触发，留作兼容）
            default -> new AABB(doorPos);
        };
    }

    // 服务端：门状态更新逻辑
    private void updateDoorState(Optional<Player> closestPlayerOpt) {
        DeepBlueLabAccessControlDoorEntity.DoorState currentState = doorEntity.getDoorState();
        boolean hasPlayerNearby = closestPlayerOpt.isPresent();
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
                // 播放正面开门音效, 需要完全关闭后打开才触发
                if(isPlayerInDoorFront(closestPlayerOpt.get()) && currentState == DeepBlueLabAccessControlDoorEntity.DoorState.CLOSED){
                    playDoorOpenSound();
                }
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
                // 关门
                doorEntity.closeDoor();
            }
        }
    }

    // ==================== 客户端：新增前方区域调试粒子（绿色）====================
    @OnlyIn(Dist.CLIENT)
    private void spawnDetectionAreaDebugParticles(AABB detectionAreaAABB, DustParticleOptions dustParticleOptions) {
        Minecraft mc = Minecraft.getInstance();
        boolean isDebugMode = mc.options.renderDebug;
        if (!FORCE_SHOW_PARTICLE && !isDebugMode) {
            return;
        }

        if (particleTimer % PARTICLE_INTERVAL != 0) {
            return;
        }

        // 提取 AABB 的8个顶点
        Vec3[] vertices = new Vec3[8];
        vertices[0] = new Vec3(detectionAreaAABB.minX, detectionAreaAABB.minY, detectionAreaAABB.minZ);
        vertices[1] = new Vec3(detectionAreaAABB.maxX, detectionAreaAABB.minY, detectionAreaAABB.minZ);
        vertices[2] = new Vec3(detectionAreaAABB.maxX, detectionAreaAABB.maxY, detectionAreaAABB.minZ);
        vertices[3] = new Vec3(detectionAreaAABB.minX, detectionAreaAABB.maxY, detectionAreaAABB.minZ);
        vertices[4] = new Vec3(detectionAreaAABB.minX, detectionAreaAABB.minY, detectionAreaAABB.maxZ);
        vertices[5] = new Vec3(detectionAreaAABB.maxX, detectionAreaAABB.minY, detectionAreaAABB.maxZ);
        vertices[6] = new Vec3(detectionAreaAABB.maxX, detectionAreaAABB.maxY, detectionAreaAABB.maxZ);
        vertices[7] = new Vec3(detectionAreaAABB.minX, detectionAreaAABB.maxY, detectionAreaAABB.maxZ);

        // 立方体12条边
        int[][] edges = {
                {0, 1}, {1, 2}, {2, 3}, {3, 0}, // 前面
                {4, 5}, {5, 6}, {6, 7}, {7, 4}, // 后面
                {0, 4}, {1, 5}, {2, 6}, {3, 7}  // 侧面
        };

        // 绿色粒子渲染前方判断区域
        for (int[] edge : edges) {
            spawnParticlesAlongLine(vertices[edge[0]], vertices[edge[1]], dustParticleOptions);
        }
    }

    // 通用粒子生成方法（支持自定义粒子颜色）
    @OnlyIn(Dist.CLIENT)
    private void spawnParticlesAlongLine(Vec3 start, Vec3 end, DustParticleOptions particle) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        // 均匀分布粒子
        double dx = (end.x - start.x) / EDGE_POINT_COUNT;
        double dy = (end.y - start.y) / EDGE_POINT_COUNT;
        double dz = (end.z - start.z) / EDGE_POINT_COUNT;

        for (int i = 0; i <= EDGE_POINT_COUNT; i++) {
            double x = start.x + dx * i;
            double y = start.y + dy * i;
            double z = start.z + dz * i;

            mc.level.addParticle(
                    particle,
                    x, y, z,
                    0.0D, 0.0D, 0.0D // 静止粒子
            );
        }
    }
}