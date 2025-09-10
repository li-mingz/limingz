package com.limingz.mymod.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.*;

/*
 * @since 1.9.0
 * Duplicate of ModelPart class which is not final
 *
 * 借鉴自 "灾变" mod
 * https://github.com/lender544/Lionfish-API/blob/master/src/main/java/com/github/L_Ender/lionfishapi/client/model/tools/BasicModelPart.java
 *
 * 注释为 ai+人工编写
 *
 * 核心功能：
 * 1. 定义模型部件的几何结构（立方体）
 * 2. 管理子部件层级关系
 * 3. 处理旋转/平移/缩放变换
 * 4. 纹理映射和渲染
 *
 */
@OnlyIn(Dist.CLIENT)
public class BasicModelPart {
    // 纹理尺寸
    public float textureWidth = 32.0F;
    public float textureHeight = 32.0F;
    // 纹理偏移量
    public int textureOffsetX;
    public int textureOffsetY;
    // 旋转中心点（局部坐标系）
    public float rotationPointX;
    public float rotationPointY;
    public float rotationPointZ;
    // 欧拉旋转角度（弧度制）
    public float rotateAngleX;
    public float rotateAngleY;
    public float rotateAngleZ;
    // 缩放系数
    public float xScale = 1.0F;
    public float yScale = 1.0F;
    public float zScale = 1.0F;
    // 镜像标志（翻转UV）(可以用来实现类似原版双开门的效果）
    public boolean mirror;
    // 是否可见
    public boolean showModel = true;
    // 存储当前部件的几何立方体
    private final ObjectList<ModelBox> cubeList = new ObjectArrayList<>();
    // 存储子部件
    private final ObjectList<BasicModelPart> childModels = new ObjectArrayList<>();

//    public BasicModelPart(BasicEntityModel model) {
//        this.setTextureSize(model.textureWidth, model.textureHeight);
//    }
//
//    public BasicModelPart(BasicEntityModel model, int texOffX, int texOffY) {
//        this(model.textureWidth, model.textureHeight, texOffX, texOffY);
//    }
    /**
     * 构造函数
     * @param textureWidthIn  纹理宽度
     * @param textureHeightIn 纹理高度
     * @param textureOffsetXIn 纹理X偏移
     * @param textureOffsetYIn 纹理Y偏移
     */
    public BasicModelPart(int textureWidthIn, int textureHeightIn, int textureOffsetXIn, int textureOffsetYIn) {
        this.setTextureSize(textureWidthIn, textureHeightIn);
        this.setTextureOffset(textureOffsetXIn, textureOffsetYIn);
    }

    /**
     * 无参数构造函数
     * <p>使用默认纹理大小</p>
     * <p>纹理偏移未初始化</p>
     */
    private BasicModelPart() {
    }

    /**
     * 复制传入实例的部分参数
     * <p>仅复制 旋转中心点，欧拉旋转角度，缩放系数</p>
     * @return 复制的新实例
     */
    public BasicModelPart getModelAngleCopy() {
        BasicModelPart BasicModelPart = new BasicModelPart();
        BasicModelPart.copyModelAngles(this);
        return BasicModelPart;
    }


    /**
     * 设置当前实例的部分参数为传入实例的参数
     * <p>仅设置 旋转中心点，欧拉旋转角度，缩放系数</p>
     */
    public void copyModelAngles(BasicModelPart BasicModelPartIn) {
        this.rotateAngleX = BasicModelPartIn.rotateAngleX;
        this.rotateAngleY = BasicModelPartIn.rotateAngleY;
        this.rotateAngleZ = BasicModelPartIn.rotateAngleZ;
        this.rotationPointX = BasicModelPartIn.rotationPointX;
        this.rotationPointY = BasicModelPartIn.rotationPointY;
        this.rotationPointZ = BasicModelPartIn.rotationPointZ;
        this.xScale = BasicModelPartIn.xScale;
        this.yScale = BasicModelPartIn.yScale;
        this.zScale = BasicModelPartIn.zScale;
    }

    /**
     * 为当前实例添加子部件
     */
    public void addChild(BasicModelPart renderer) {
        this.childModels.add(renderer);
    }

    /**
     * 设置当前实例的纹理偏移量
     * @param x 纹理X偏移
     * @param y 纹理Y偏移
     * @return 当前实例
     */
    public BasicModelPart setTextureOffset(int x, int y) {
        this.textureOffsetX = x;
        this.textureOffsetY = y;
        return this;
    }

    /**
     * 添加立方体到当前实例
     * <p>并设置当前实例的纹理偏移量为指定参数</p>
     * @param x      基准X坐标
     * @param y      基准Y坐标
     * @param z      基准Z坐标
     * @param width  宽度(x)
     * @param height 高度(y)
     * @param depth  深度(z)
     * @param delta  膨胀值（xyz）
     *               在立方体的每个面上向外或向内扩展的额外尺寸，
     *               为正时向外扩大，为负时向内收缩，
     *               用于避免深度冲突或者改善接缝
     * @param texX 纹理X偏移
     * @param texY 纹理Y偏移
     * @return 当前实例
     */
    public BasicModelPart addBox(String partName, float x, float y, float z, int width, int height, int depth, float delta, int texX, int texY) {
        this.setTextureOffset(texX, texY);
        this.addBox(this.textureOffsetX, this.textureOffsetY, x, y, z, (float)width, (float)height, (float)depth, delta, delta, delta, this.mirror, false);
        return this;
    }

    /**
     * 添加立方体到当前实例
     * @param x      基准X坐标
     * @param y      基准Y坐标
     * @param z      基准Z坐标
     * @param width  宽度(x)
     * @param height 高度(y)
     * @param depth  深度(z)
     * @return 当前实例
     */
    public BasicModelPart addBox(float x, float y, float z, float width, float height, float depth) {
        this.addBox(this.textureOffsetX, this.textureOffsetY, x, y, z, width, height, depth, 0.0F, 0.0F, 0.0F, this.mirror, false);
        return this;
    }

    /**
     * 添加立方体到当前实例
     * @param x      基准X坐标
     * @param y      基准Y坐标
     * @param z      基准Z坐标
     * @param width  宽度(x)
     * @param height 高度(y)
     * @param depth  深度(z)
     * @param mirrorIn 是否翻转纹理
     * @return 当前实例
     */
    public BasicModelPart addBox(float x, float y, float z, float width, float height, float depth, boolean mirrorIn) {
        this.addBox(this.textureOffsetX, this.textureOffsetY, x, y, z, width, height, depth, 0.0F, 0.0F, 0.0F, mirrorIn, false);
        return this;
    }

    /**
     * 添加立方体到当前实例
     * @param x      基准X坐标
     * @param y      基准Y坐标
     * @param z      基准Z坐标
     * @param width  宽度(x)
     * @param height 高度(y)
     * @param depth  深度(z)
     * @param delta  膨胀值（xyz）
     *               在立方体的每个面上向外或向内扩展的额外尺寸，
     *               为正时向外扩大，为负时向内收缩，
     *               用于避免深度冲突或者改善接缝
     */
    public void addBox(float x, float y, float z, float width, float height, float depth, float delta) {
        this.addBox(this.textureOffsetX, this.textureOffsetY, x, y, z, width, height, depth, delta, delta, delta, this.mirror, false);
    }

    /**
     * 添加立方体到当前实例
     * @param x      基准X坐标
     * @param y      基准Y坐标
     * @param z      基准Z坐标
     * @param width  宽度(x)
     * @param height 高度(y)
     * @param depth  深度(z)
     * @param deltaX  膨胀值（x）
     *               在立方体的每个面上向外或向内扩展的额外尺寸，
     *               为正时向外扩大，为负时向内收缩，
     *               用于避免深度冲突或者改善接缝
     * @param deltaY  膨胀值（y）
     * @param deltaZ  膨胀值（z）
     */
    public void addBox(float x, float y, float z, float width, float height, float depth, float deltaX, float deltaY, float deltaZ) {
        this.addBox(this.textureOffsetX, this.textureOffsetY, x, y, z, width, height, depth, deltaX, deltaY, deltaZ, this.mirror, false);
    }

    /**
     * 添加立方体到当前实例
     * @param x      基准X坐标
     * @param y      基准Y坐标
     * @param z      基准Z坐标
     * @param width  宽度(x)
     * @param height 高度(y)
     * @param depth  深度(z)
     * @param delta  膨胀值（xyz）
     *               在立方体的每个面上向外或向内扩展的额外尺寸，
     *               为正时向外扩大，为负时向内收缩，
     *               用于避免深度冲突或者改善接缝
     * @param mirrorIn 是否翻转纹理
     */
    public void addBox(float x, float y, float z, float width, float height, float depth, float delta, boolean mirrorIn) {
        this.addBox(this.textureOffsetX, this.textureOffsetY, x, y, z, width, height, depth, delta, delta, delta, mirrorIn, false);
    }

    // 私有方法实际处理立方体创建
    private void addBox(int texOffX, int texOffY, float x, float y, float z, float width, float height, float depth, float deltaX, float deltaY, float deltaZ, boolean mirorIn, boolean p_228305_13_) {
        // 创建 ModelBox 对象并存入列表
        this.cubeList.add(new ModelBox(texOffX, texOffY, x, y, z, width, height, depth, deltaX, deltaY, deltaZ, mirorIn, this.textureWidth, this.textureHeight));
    }

    /**
     * 设置当前实例的旋转中心点（局部坐标系）
     * @param rotationPointXIn 旋转中心点X坐标
     * @param rotationPointYIn 旋转中心点Y坐标
     * @param rotationPointZIn 旋转中心点Z坐标
     */
    public void setRotationPoint(float rotationPointXIn, float rotationPointYIn, float rotationPointZIn) {
        this.rotationPointX = rotationPointXIn;
        this.rotationPointY = rotationPointYIn;
        this.rotationPointZ = rotationPointZIn;
    }

    /**
     * 渲染当前部件及所有子部件
     * @param matrixStackIn    变换栈
     * @param bufferIn         顶点缓冲区
     * @param packedLightIn    光照数据
     * @param packedOverlayIn  叠加层数据
     */
    public void render(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn) {
        this.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, 1.0F, 1.0F, 1.0F, 1.0F);
    }


    /**
     * 渲染当前部件及所有子部件
     * @param matrixStackIn    变换栈
     * @param bufferIn         顶点缓冲区
     * @param packedLightIn    光照数据
     * @param packedOverlayIn  叠加层数据
     * @param red            颜色通道R
     * @param green          颜色通道G
     * @param blue           颜色通道B
     * @param alpha          透明度
     */
    public void render(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        // 判断是否需要显示
        if (this.showModel) {
            // 当前实例的部件或者子部件不为空时
            if (!this.cubeList.isEmpty() || !this.childModels.isEmpty()) {
                // 保存状态
                matrixStackIn.pushPose();
                // 应用传入的变换
                this.translateAndRotate(matrixStackIn);
                // 渲染当前部件
                this.doRender(matrixStackIn.last(), bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
                // 渲染当前部件的子部件
                for(BasicModelPart BasicModelPart : this.childModels) {
                    BasicModelPart.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
                }

                // 恢复状态
                matrixStackIn.popPose();
            }
        }
    }

    /**
     * 应用旋转/平移/缩放变换到 PoseStack
     */
    public void translateAndRotate(PoseStack poseStack) {
        // 先平移
        poseStack.translate(this.rotationPointX / 16.0F, this.rotationPointY / 16.0F, this.rotationPointZ / 16.0F);
        // 再旋转
        if (this.rotateAngleX != 0.0F || this.rotateAngleY != 0.0F || this.rotateAngleZ != 0.0F) {
            poseStack.mulPose((new Quaternionf()).rotationZYX(this.rotateAngleZ, this.rotateAngleY, this.rotateAngleX));
        }
        // 最后缩放
        if (this.xScale != 1.0F || this.yScale != 1.0F || this.zScale != 1.0F) {
            poseStack.scale(this.xScale, this.yScale, this.zScale);
        }

    }

    /**
     * 实际执行几何体渲染（处理单个立方体的四边形）
     * @param matrixEntryIn   当前变换矩阵
     * @param bufferIn        顶点缓冲区
     * @param packedLightIn    光照数据
     * @param packedOverlayIn  叠加层数据
     * @param red            颜色通道R
     * @param green          颜色通道G
     * @param blue           颜色通道B
     * @param alpha          透明度
     */
    private void doRender(PoseStack.Pose matrixEntryIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        Matrix4f matrix4f = matrixEntryIn.pose();
        Matrix3f matrix3f = matrixEntryIn.normal();
        // 逐个渲染当前部件的各个立方体
        for(ModelBox BasicModelPart$modelbox : this.cubeList) {
            for(TexturedQuad BasicModelPart$texturedquad : BasicModelPart$modelbox.quads) {
                Vector3f vector3f = new Vector3f(BasicModelPart$texturedquad.normal);
                vector3f.mul(matrix3f);
                float f = vector3f.x();
                float f1 = vector3f.y();
                float f2 = vector3f.z();

                for(int i = 0; i < 4; ++i) {
                    PositionTextureVertex BasicModelPart$positiontexturevertex = BasicModelPart$texturedquad.vertexPositions[i];
                    float f3 = BasicModelPart$positiontexturevertex.position.x() / 16.0F;
                    float f4 = BasicModelPart$positiontexturevertex.position.y() / 16.0F;
                    float f5 = BasicModelPart$positiontexturevertex.position.z() / 16.0F;
                    Vector4f vector4f = new Vector4f(f3, f4, f5, 1.0F);
                    vector4f.mul(matrix4f);
                    // 写入缓冲区
                    bufferIn.vertex(vector4f.x(), vector4f.y(), vector4f.z(), red, green, blue, alpha, BasicModelPart$positiontexturevertex.textureU, BasicModelPart$positiontexturevertex.textureV, packedOverlayIn, packedLightIn, f, f1, f2);
                }
            }
        }

    }

    /**
     * 设置当前部件纹理大小
     * @return 当前实例
     */
    public BasicModelPart setTextureSize(int textureWidthIn, int textureHeightIn) {
        this.textureWidth = (float)textureWidthIn;
        this.textureHeight = (float)textureHeightIn;
        return this;
    }

    /**
     * 随机返回一个当前部件的立方体(这有啥用？)
     * @param randomIn 随机数接口
     * @return 立方体
     */
    public ModelBox getRandomCube(RandomSource randomIn) {
        return this.cubeList.size() > 0 ? this.cubeList.get(randomIn.nextInt(this.cubeList.size())) : null;
    }

    // 几何立方体
    @OnlyIn(Dist.CLIENT)
    public static class ModelBox {
        // 立方体的6个面（每个面是一个TexturedQuad）
        private final TexturedQuad[] quads;
        // 立方体边界坐标（膨胀后）
        public final float posX1; // 最小X坐标
        public final float posY1; // 最小Y坐标
        public final float posZ1; // 最小Z坐标
        public final float posX2; // 最大X坐标
        public final float posY2; // 最大Y坐标
        public final float posZ2; // 最大Z坐标

        /**
         * 构造一个立方体模型
         *
         * @param texOffX    纹理起始U偏移（像素）
         * @param texOffY    纹理起始V偏移（像素）
         * @param x          起始X坐标（模型空间）
         * @param y          起始Y坐标
         * @param z          起始Z坐标
         * @param width      立方体宽度（X方向）
         * @param height     立方体高度（Y方向）
         * @param depth      立方体深度（Z方向）
         * @param deltaX     X方向膨胀值
         * @param deltaY     Y方向膨胀值
         * @param deltaZ     Z方向膨胀值
         * @param mirorIn    是否镜像立方体
         * @param texWidth   纹理总宽度（像素）
         * @param texHeight  纹理总高度（像素）
         */
        public ModelBox(int texOffX, int texOffY, float x, float y, float z, float width, float height, float depth, float deltaX, float deltaY, float deltaZ, boolean mirorIn, float texWidth, float texHeight) {

            // 存储原始位置和尺寸（膨胀前）
            this.posX1 = x;
            this.posY1 = y;
            this.posZ1 = z;
            this.posX2 = x + width;
            this.posY2 = y + height;
            this.posZ2 = z + depth;
            // 初始化6个面的数组
            this.quads = new TexturedQuad[6];
            float f = x + width;
            float f1 = y + height;
            float f2 = z + depth;
            // 应用膨胀值
            x = x - deltaX;
            y = y - deltaY;
            z = z - deltaZ;
            f = f + deltaX;
            f1 = f1 + deltaY;
            f2 = f2 + deltaZ;
            // 判断是否反转纹理
            if (mirorIn) {
                float f3 = f;
                f = x;
                x = f3;
            }

            // 创建8个顶点（膨胀后的立方体顶点）
            // 顶点顺序：后下左，后下右，后上右，后上左，前下左，前下右，前上右，前上左
            PositionTextureVertex BasicModelPart$positiontexturevertex7 = new PositionTextureVertex(x, y, z, 0.0F, 0.0F);  // 后下左
            PositionTextureVertex BasicModelPart$positiontexturevertex = new PositionTextureVertex(f, y, z, 0.0F, 8.0F);  // 后下右
            PositionTextureVertex BasicModelPart$positiontexturevertex1 = new PositionTextureVertex(f, f1, z, 8.0F, 8.0F);  // 后上右
            PositionTextureVertex BasicModelPart$positiontexturevertex2 = new PositionTextureVertex(x, f1, z, 8.0F, 0.0F);  // 后上左
            PositionTextureVertex BasicModelPart$positiontexturevertex3 = new PositionTextureVertex(x, y, f2, 0.0F, 0.0F);  // 前下左
            PositionTextureVertex BasicModelPart$positiontexturevertex4 = new PositionTextureVertex(f, y, f2, 0.0F, 8.0F);  // 前下右
            PositionTextureVertex BasicModelPart$positiontexturevertex5 = new PositionTextureVertex(f, f1, f2, 8.0F, 8.0F);  // 前上右
            PositionTextureVertex BasicModelPart$positiontexturevertex6 = new PositionTextureVertex(x, f1, f2, 8.0F, 0.0F);  // 前上左

            // 计算纹理分段（将纹理划分为不同区域用于各面）
            float f4 = (float)texOffX;
            float f5 = (float)texOffX + depth;
            float f6 = (float)texOffX + depth + width;
            float f7 = (float)texOffX + depth + width + width;
            float f8 = (float)texOffX + depth + width + depth;
            float f9 = (float)texOffX + depth + width + depth + width;
            float f10 = (float)texOffY;
            float f11 = (float)texOffY + depth;
            float f12 = (float)texOffY + depth + height;
            /*
             * 创建立方体的6个面（按方向索引）：
             * 0: EAST    (+X)
             * 1: WEST    (-X)
             * 2: DOWN    (-Y)
             * 3: UP      (+Y)
             * 4: NORTH   (-Z)
             * 5: SOUTH   (+Z)
             */
            this.quads[2] = new TexturedQuad(new PositionTextureVertex[]{BasicModelPart$positiontexturevertex4, BasicModelPart$positiontexturevertex3, BasicModelPart$positiontexturevertex7, BasicModelPart$positiontexturevertex}, f5, f10, f6, f11, texWidth, texHeight, mirorIn, Direction.DOWN);
            this.quads[3] = new TexturedQuad(new PositionTextureVertex[]{BasicModelPart$positiontexturevertex1, BasicModelPart$positiontexturevertex2, BasicModelPart$positiontexturevertex6, BasicModelPart$positiontexturevertex5}, f6, f11, f7, f10, texWidth, texHeight, mirorIn, Direction.UP);
            this.quads[1] = new TexturedQuad(new PositionTextureVertex[]{BasicModelPart$positiontexturevertex7, BasicModelPart$positiontexturevertex3, BasicModelPart$positiontexturevertex6, BasicModelPart$positiontexturevertex2}, f4, f11, f5, f12, texWidth, texHeight, mirorIn, Direction.WEST);
            this.quads[4] = new TexturedQuad(new PositionTextureVertex[]{BasicModelPart$positiontexturevertex, BasicModelPart$positiontexturevertex7, BasicModelPart$positiontexturevertex2, BasicModelPart$positiontexturevertex1}, f5, f11, f6, f12, texWidth, texHeight, mirorIn, Direction.NORTH);
            this.quads[0] = new TexturedQuad(new PositionTextureVertex[]{BasicModelPart$positiontexturevertex4, BasicModelPart$positiontexturevertex, BasicModelPart$positiontexturevertex1, BasicModelPart$positiontexturevertex5}, f6, f11, f8, f12, texWidth, texHeight, mirorIn, Direction.EAST);
            this.quads[5] = new TexturedQuad(new PositionTextureVertex[]{BasicModelPart$positiontexturevertex3, BasicModelPart$positiontexturevertex4, BasicModelPart$positiontexturevertex5, BasicModelPart$positiontexturevertex6}, f8, f11, f9, f12, texWidth, texHeight, mirorIn, Direction.SOUTH);
        }
    }

    // 顶点数据
    @OnlyIn(Dist.CLIENT)
    static class PositionTextureVertex {
        public final Vector3f position;  // 顶点位置 (x,y,z)
        public final float textureU;  // U纹理坐标
        public final float textureV;  // V纹理坐标
        /**
        初始化顶点数据
         @param texU 纹理坐标(u) 在BasicModelPart类中仅用作占位符
         @param texV 纹理坐标(v) 在BasicModelPart类中仅用作占位符
         */
        public PositionTextureVertex(float x, float y, float z, float texU, float texV) {
            this(new Vector3f(x, y, z), texU, texV);
        }

        /**
         * 设置顶点数据的纹理坐标到新实例(复制顶点位置)
         * @param texU // U纹理坐标
         * @param texV // V纹理坐标
         * @return 新实例
         */
        public PositionTextureVertex setTextureUV(float texU, float texV) {
            return new PositionTextureVertex(this.position, texU, texV);
        }

        public PositionTextureVertex(Vector3f posIn, float texU, float texV) {
            this.position = posIn;
            this.textureU = texU;
            this.textureV = texV;
        }
    }

    // 纹理四边形
    @OnlyIn(Dist.CLIENT)
    static class TexturedQuad {
        // 四边形的四个顶点（每个顶点包含位置和纹理坐标）
        public final PositionTextureVertex[] vertexPositions;
        // 四边形的法线向量（用于光照计算）
        public final Vector3f normal;

        /**
         * 构造一个纹理四边形。
         *
         * @param positionsIn 四个顶点数组（顺序很重要，通常按逆时针方向，表示正面）
         * @param u1          UV坐标的最小U值（对应纹理区域的左边界）
         * @param v1          UV坐标的最小V值（对应纹理区域的上边界）
         * @param u2          UV坐标的最大U值（对应纹理区域的右边界）
         * @param v2          UV坐标的最大V值（对应纹理区域的下边界）
         * @param texWidth    整个纹理的宽度（用于将像素坐标转换为0-1的UV坐标）
         * @param texHeight   整个纹理的高度（同上）
         * @param mirrorIn    是否镜像（翻转）纹理
         * @param directionIn 这个四边形所面向的方向（用于自动计算法线）
         */
        public TexturedQuad(PositionTextureVertex[] positionsIn, float u1, float v1, float u2, float v2, float texWidth, float texHeight, boolean mirrorIn, Direction directionIn) {
            // 初始化顶点数组
            this.vertexPositions = positionsIn;
            // 计算UV偏移量（防止纹理边缘瑕疵）(当前始终为0,为预留扩展用)
            float f = 0.0F / texWidth;   // U方向微小偏移（通常为1/texWidth）
            float f1 = 0.0F / texHeight; // V方向微小偏移（通常为1/texHeight）
            // 设置每个顶点的UV坐标（将像素坐标转换为0-1范围的UV坐标）
            positionsIn[0] = positionsIn[0].setTextureUV(u2 / texWidth - f, v1 / texHeight + f1);
            positionsIn[1] = positionsIn[1].setTextureUV(u1 / texWidth + f, v1 / texHeight + f1);
            positionsIn[2] = positionsIn[2].setTextureUV(u1 / texWidth + f, v2 / texHeight - f1);
            positionsIn[3] = positionsIn[3].setTextureUV(u2 / texWidth - f, v2 / texHeight - f1);
            // 处理镜像（翻转）纹理
            if (mirrorIn) {
                int i = positionsIn.length;

                // 反转顶点顺序：实现纹理水平翻转
                for(int j = 0; j < i / 2; ++j) {
                    PositionTextureVertex BasicModelPart$positiontexturevertex = positionsIn[j];
                    positionsIn[j] = positionsIn[i - 1 - j];
                    positionsIn[i - 1 - j] = BasicModelPart$positiontexturevertex;
                }
            }

            // 设置法线向量（从方向枚举获取）
            this.normal = directionIn.step();
            // 如果镜像，反转法线X分量
            if (mirrorIn) {
                // 反转X分量（保持Y和Z不变）
                this.normal.mul(-1.0F, 1.0F, 1.0F);
            }

        }
    }
}