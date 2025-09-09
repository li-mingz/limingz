package com.limingz.mymod.datagen;

import com.limingz.mymod.Main;
import com.limingz.mymod.register.BlockRegister;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraftforge.common.Tags;

import java.util.function.Consumer;

public class Recipes extends RecipeProvider {
    public Recipes(PackOutput packOutput) {
        super(packOutput);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BlockRegister.small_door.get())
                .pattern("   ")
                .pattern("axa")
                .pattern("axa")
                .define('a', Tags.Items.INGOTS_IRON)
                .define('x', Tags.Items.INGOTS_COPPER)
                .unlockedBy("has_copper", has(Tags.Items.INGOTS_COPPER))
                .save(consumer);
    }
}
