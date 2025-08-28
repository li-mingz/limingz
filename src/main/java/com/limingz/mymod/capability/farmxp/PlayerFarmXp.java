package com.limingz.mymod.capability.farmxp;

import net.minecraft.nbt.CompoundTag;

public class PlayerFarmXp {
    private int xp;

    public PlayerFarmXp() {
        this.xp = 0;
    }

    public PlayerFarmXp(int xp) {
        this.xp = xp;
    }

    public int getXp() {
        return this.xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public void increase() {
        this.xp++;
    }

    public void increase(int i) {
        this.xp += i;
    }

    public boolean decrease() {
        if (this.xp >= 1) {
            this.xp--;
            return true;
        }
        return false;
    }

    public boolean decrease(int d) {
        if (this.xp >= d) {
            this.xp -= d;
            return true;
        }
        return false;
    }

    public void saveNBTData(CompoundTag compoundTag) {
        compoundTag.putInt("farm_xp", this.xp);
    }

    public void loadNBTData(CompoundTag compoundTag) {
        this.xp = compoundTag.getInt("farm_xp");
    }
}
