package com.limingz.mymod.block.entity;

import com.limingz.mymod.register.BlockEntityRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DeskBlockEntity extends BlockEntity {
    private static final String TAG_NAME = "Item";
    private final ItemStackHandler items = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    };
    private final LazyOptional<IItemHandler> iItemHandler = LazyOptional.of(() -> items);

    public DeskBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntityRegister.desk_block_entity.get(), pPos, pBlockState);
    }

    public ItemStackHandler getItems() {
        return items;

    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        loaddata(pTag);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return iItemHandler.cast();
        } else {
            return super.getCapability(cap, side);
        }
    }

    private void savedata(CompoundTag tag) {
        tag.put(TAG_NAME, items.serializeNBT());
    }

    private void loaddata(CompoundTag tag) {
        if (tag.contains(TAG_NAME)) {
            items.deserializeNBT(tag.getCompound(TAG_NAME));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        savedata(tag);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        savedata(tag);
        return tag;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            handleUpdateTag(tag);
        }
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        if (tag != null) {
            loaddata(tag);
        }
    }

    public void serverTick() {
        //TODO:方块实体每刻逻辑(服务端)
    }

    public void clientTick() {
        //TODO:方块实体每刻逻辑(客户端)
    }
}
