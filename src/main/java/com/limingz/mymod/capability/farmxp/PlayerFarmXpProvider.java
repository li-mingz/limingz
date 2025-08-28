package com.limingz.mymod.capability.farmxp;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerFarmXpProvider implements ICapabilityProvider, INBTSerializable {
    public static final Capability<PlayerFarmXp> PLAYER_FARM_XP_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static int farm_xp_client = 0;
    private PlayerFarmXp playerFarmXp = new PlayerFarmXp();
    private final LazyOptional<PlayerFarmXp> lazyOptional = LazyOptional.of(() -> this.playerFarmXp);

    public PlayerFarmXpProvider() {
        playerFarmXp = new PlayerFarmXp();
    }

    @Override
    public Tag serializeNBT() {
        var tag = new CompoundTag();
        playerFarmXp.saveNBTData(tag);
        return tag;
    }

    @Override
    public void deserializeNBT(Tag nbt) {
        playerFarmXp.loadNBTData((CompoundTag) nbt);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return getCapability(cap);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
        if (cap == PLAYER_FARM_XP_CAPABILITY) {
            return lazyOptional.cast();
        }
        return lazyOptional.empty();
    }
}
