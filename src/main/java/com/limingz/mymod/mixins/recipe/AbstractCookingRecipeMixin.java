package com.limingz.mymod.mixins.recipe;

import com.limingz.mymod.loot.CropsModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(AbstractCookingRecipe.class)
public class AbstractCookingRecipeMixin {
    @Shadow @Final protected ItemStack result;

    @Inject(method = "assemble", at = @At("TAIL"), cancellable = true)
    public void assemble(Container pContainer, RegistryAccess pRegistryAccess, CallbackInfoReturnable<ItemStack> cir) {
        // 熔炉配方只有一个输入
        ItemStack temp_itemStack = pContainer.getItem(0);
        float nutritious_value = 0;
        // 检查是否存在自定义标签
        if (CropsModifier.isNutritious(temp_itemStack)){
            // 获取额外营养值
            nutritious_value = temp_itemStack.getTag().getFloat(CropsModifier.TAG_NUTRITIOUS);
        }
        ItemStack result_itemstack = this.result.copy();
        // 如果存在营养值
        if(nutritious_value > 0){
            // 平分产物营养值
            nutritious_value = nutritious_value / result_itemstack.getCount();
            CompoundTag tag = result_itemstack.getOrCreateTag();
            // 设置营养值
            tag.putFloat(CropsModifier.TAG_NUTRITIOUS, nutritious_value);
            // 更改物品名称
            MutableComponent oldName = result_itemstack.getHoverName().copy();
            Style oldStyle = oldName.getStyle();
            if(oldStyle.isEmpty()) oldStyle = oldStyle.applyFormats(ChatFormatting.WHITE);
            MutableComponent newName = Component.literal("富营养的 ").withStyle(ChatFormatting.GREEN)
                    .append(oldName.withStyle(oldStyle));
            result_itemstack.setHoverName(newName);
            cir.setReturnValue(result_itemstack);
        }
    }
}
