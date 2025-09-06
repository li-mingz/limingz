package com.limingz.mymod.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import static com.limingz.mymod.Main.MODID;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = MODID)
public class CropsModifier extends LootModifier {
    // 富营养的标签
    public static final String TAG_NUTRITIOUS = "mymod:nutritious_food";
    // 定义 Codec 用于序列化
    public static final Codec<CropsModifier> CODEC = RecordCodecBuilder.create(inst ->
            LootModifier.codecStart(inst).apply(inst, CropsModifier::new)
    );

    public CropsModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        // 获取方块状态
        BlockState blockState = context.getParamOrNull(LootContextParams.BLOCK_STATE);
        // 获取战利品表发生位置
        Vec3 originVector = context.getParamOrNull(LootContextParams.ORIGIN);
        // 没有则直接返回
        if (originVector == null || blockState == null) return generatedLoot;
        // 计算区块坐标
        int chunkX = ((int) Math.floor(originVector.x)) >> 4;
        int chunkZ = ((int) Math.floor(originVector.z)) >> 4;
        for(ItemStack itemStack : generatedLoot){
            // 获取物品的食物属性
            FoodProperties foodProperties = itemStack.getItem().getFoodProperties(itemStack, null);
            // 跳过不可食用的和已经有标签的
            if(foodProperties == null || isNutritious(itemStack)) continue;
            CompoundTag tag = itemStack.getOrCreateTag();
            tag.putFloat(TAG_NUTRITIOUS, 1.0f);
            // 更改物品名称
            MutableComponent oldName = itemStack.getHoverName().copy();
            Style oldStyle = oldName.getStyle();
            if(oldStyle.isEmpty()) oldStyle = oldStyle.applyFormats(ChatFormatting.WHITE);
            MutableComponent newName = Component.literal("富营养的 ").withStyle(ChatFormatting.GREEN)
                    .append(oldName.withStyle(oldStyle));
            itemStack.setHoverName(newName);
        }
        return generatedLoot;
    }

    private static boolean isNutritious(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains(TAG_NUTRITIOUS);
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
    @SubscribeEvent
    public static void onFoodEaten(LivingEntityUseItemEvent.Finish event) {
        // 确保是玩家且是在吃东西
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return; // 仅在服务端执行

        ItemStack foodStack = event.getItem();

        // 检查食物是否拥有自定义标签
        if (!isNutritious(foodStack)) return;
        FoodProperties foodProperties = foodStack.getItem().getFoodProperties();
        if (foodProperties == null) return;

        // 获取原始营养值和饱和度
        int nutrition = foodProperties.getNutrition();
        float saturation = foodProperties.getSaturationModifier();
        float extra_nutrition = foodStack.getTag().getFloat(TAG_NUTRITIOUS);
        int bonusNutrition = Mth.floor(nutrition * extra_nutrition); // 额外营养值
        float bonusSaturation = saturation * extra_nutrition; // 额外饱和度

        // 应用额外饱食效果
        player.getFoodData().eat(bonusNutrition, bonusSaturation);
    }
}