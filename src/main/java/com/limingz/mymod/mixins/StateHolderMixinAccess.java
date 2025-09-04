package com.limingz.mymod.mixins;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StateHolder.class)
public interface StateHolderMixinAccess<O, S> {
    @Accessor
    Table<Property<?>, Comparable<?>, S> getNeighbours();
    @Accessor
    ImmutableMap<Property<?>, Comparable<?>> getValues();
}
