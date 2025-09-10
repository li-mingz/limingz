package com.limingz.mymod.renderer.blockentity;

import com.limingz.mymod.block.entity.DeepBlueLabAccessControlDoorEntity;
import com.limingz.mymod.gui.holographic_ui.event.ClientForgeTickEvent;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static com.limingz.mymod.Main.MODID;

@OnlyIn(Dist.CLIENT)
public class DeepBlueLabAccessControlDoorRenderer implements BlockEntityRenderer<DeepBlueLabAccessControlDoorEntity> {

    private static class TexturedQuad {
        private final PositionTextureVertex[] vertices;
        private final Vector3f normal;

        public TexturedQuad(PositionTextureVertex[] vertices, Vector3f normal) {
            this.vertices = vertices;
            this.normal = normal;
        }
    }

    private static class PositionTextureVertex {
        private final Vector3f position;
        private final float u;
        private final float v;

        public PositionTextureVertex(Vector3f position, float u, float v) {
            this.position = position;
            this.u = u;
            this.v = v;
        }
    }

    private final TexturedQuad[] quads;
    private double renderTick = 0;
    private float rotation = 0;

    // 使用正确的纹理资源路径
    private final ResourceLocation textureLocation = ResourceLocation.fromNamespaceAndPath(MODID, "textures/block/smooth_stone.png");

    public DeepBlueLabAccessControlDoorRenderer(BlockEntityRendererProvider.Context context) {
        // 创建立方体顶点（单位：1/16方块）
        PositionTextureVertex[] vertices = new PositionTextureVertex[] {
                new PositionTextureVertex(new Vector3f(0, 0, 0), 0, 0),
                new PositionTextureVertex(new Vector3f(16, 0, 0), 16, 0),
                new PositionTextureVertex(new Vector3f(16, 16, 0), 16, 16),
                new PositionTextureVertex(new Vector3f(0, 16, 0), 0, 16),
                new PositionTextureVertex(new Vector3f(0, 0, 16), 0, 0),
                new PositionTextureVertex(new Vector3f(16, 0, 16), 16, 0),
                new PositionTextureVertex(new Vector3f(16, 16, 16), 16, 16),
                new PositionTextureVertex(new Vector3f(0, 16, 16), 0, 16)
        };

        // 定义立方体的6个面及其法线
        quads = new TexturedQuad[] {
                // 底面 (y=0)
                new TexturedQuad(new PositionTextureVertex[]{
                        vertices[0], vertices[1], vertices[2], vertices[3]
                }, new Vector3f(0, -1, 0)),

                // 顶面 (y=16)
                new TexturedQuad(new PositionTextureVertex[]{
                        vertices[7], vertices[6], vertices[5], vertices[4]
                }, new Vector3f(0, 1, 0)),

                // 西面 (z=0)
                new TexturedQuad(new PositionTextureVertex[]{
                        vertices[0], vertices[3], vertices[7], vertices[4]
                }, new Vector3f(0, 0, -1)),

                // 东面 (z=16)
                new TexturedQuad(new PositionTextureVertex[]{
                        vertices[1], vertices[5], vertices[6], vertices[2]
                }, new Vector3f(0, 0, 1)),

                // 北面 (x=0)
                new TexturedQuad(new PositionTextureVertex[]{
                        vertices[0], vertices[4], vertices[5], vertices[1]
                }, new Vector3f(-1, 0, 0)),

                // 南面 (x=16)
                new TexturedQuad(new PositionTextureVertex[]{
                        vertices[3], vertices[2], vertices[6], vertices[7]
                }, new Vector3f(1, 0, 0))
        };
    }

    @Override
    public void render(DeepBlueLabAccessControlDoorEntity entity, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        // 更新时间计数器
        if (renderTick != ClientForgeTickEvent.updateTick) {
            renderTick = ClientForgeTickEvent.updateTick;
            rotation += 2;
            if (rotation >= 360) rotation -= 360;
        }

        // 获取纹理精灵 - 添加调试信息
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(textureLocation);

        // 调试：检查纹理是否加载
        if (sprite.contents().name().toString().contains("missing")) {
            System.err.println("纹理加载失败: " + textureLocation);
            // 临时使用红色纹理作为错误指示
            sprite = Minecraft.getInstance()
                    .getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                    .apply(ResourceLocation.tryParse("block/red_concrete"));
        }

        // 设置渲染位置为中心
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.translate(-0.5, -0.5, -0.5);

        // 获取变换矩阵
        Matrix4f poseMatrix = poseStack.last().pose();
        Matrix3f normalMatrix = poseStack.last().normal();

        // 创建顶点消费者 - 使用半透明渲染类型
        VertexConsumer vertexBuilder = buffer.getBuffer(RenderType.entityTranslucent(textureLocation));

        // 渲染所有面
        for (TexturedQuad quad : quads) {
            renderQuad(poseMatrix, normalMatrix, vertexBuilder, quad, sprite, LightTexture.pack(15,15), packedOverlay);
        }

        poseStack.popPose();
    }

    private void renderQuad(Matrix4f poseMatrix, Matrix3f normalMatrix, VertexConsumer builder,
                            TexturedQuad quad, TextureAtlasSprite sprite, int light, int overlay) {
        // 计算变换后的法线
        Vector3f transformedNormal = new Vector3f(quad.normal);
        transformedNormal.mul(normalMatrix);

        // 渲染四个顶点 - 确保正确的顶点顺序
        for (int i = 0; i < 4; i++) {
            PositionTextureVertex vertex = quad.vertices[i];

            // 应用模型变换
            Vector4f pos = new Vector4f(
                    vertex.position.x() / 16.0f,
                    vertex.position.y() / 16.0f,
                    vertex.position.z() / 16.0f,
                    1.0f
            );
            pos.mul(poseMatrix);

            // 计算UV坐标 - 使用顶点UV映射到精灵
            float u = sprite.getU(vertex.u);
            float v = sprite.getV(vertex.v);

            // 添加顶点数据
            builder.vertex(pos.x(), pos.y(), pos.z())
                    .color(1.0f, 1.0f, 1.0f, 1.0f) // 白色（使用纹理颜色）
                    .uv(u, v)
                    .overlayCoords(overlay)
                    .uv2(light)
                    .normal(transformedNormal.x(), transformedNormal.y(), transformedNormal.z())
                    .endVertex();
        }
    }
}