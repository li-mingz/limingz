package com.limingz.mymod.mixins;

import com.limingz.mymod.loot.CropsModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(CraftingMenu.class)
public class CraftingMenuMixin {
    @Inject(method = "slotChangedCraftingGrid", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/ResultContainer;setItem(ILnet/minecraft/world/item/ItemStack;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void slotChangedCraftingGrid(AbstractContainerMenu pMenu, Level pLevel, Player pPlayer, CraftingContainer pContainer, ResultContainer pResult,
                                                CallbackInfo ci, ServerPlayer serverplayer, ItemStack itemstack) {
        // 如果存在输出结果
        if(!itemstack.isEmpty()) {
            List<ItemStack> itemStacks = pContainer.getItems();
            float nutritious_value = 0;
            int itemStacks_size = itemStacks.size();
            for(ItemStack temp_itemStack : itemStacks){
                // 检查是否存在自定义标签
                if (CropsModifier.isNutritious(temp_itemStack)){
                    // 获取额外营养值
                    float extra_nutrition = temp_itemStack.getTag().getFloat(CropsModifier.TAG_NUTRITIOUS);
                    // 计算占比
                    nutritious_value += extra_nutrition/itemStacks_size;
                }

            }
            // 如果存在营养值
            if(nutritious_value > 0){
                // 平分产物营养值
                nutritious_value = nutritious_value / itemstack.getCount();
                CompoundTag tag = itemstack.getOrCreateTag();
                // 设置营养值
                tag.putFloat(CropsModifier.TAG_NUTRITIOUS, nutritious_value);
                // 更改物品名称
                MutableComponent oldName = itemstack.getHoverName().copy();
                Style oldStyle = oldName.getStyle();
                if(oldStyle.isEmpty()) oldStyle = oldStyle.applyFormats(ChatFormatting.WHITE);
                MutableComponent newName = Component.literal("富营养的 ").withStyle(ChatFormatting.GREEN)
                        .append(oldName.withStyle(oldStyle));
                itemstack.setHoverName(newName);
            }
        }
    }
}
