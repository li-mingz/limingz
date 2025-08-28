package com.limingz.mymod.network.packet.servertoplayer;

import com.limingz.mymod.capability.farmxp.PlayerFarmXpProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class FarmXpPacket {
    private final int farm_xp;

    public FarmXpPacket(int farm_xp) {
        this.farm_xp = farm_xp;
    }

    public FarmXpPacket(FriendlyByteBuf buf) {
        farm_xp = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(farm_xp);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        var context = supplier.get();
        PlayerFarmXpProvider.farm_xp_client = farm_xp;
        context.setPacketHandled(true);
        return true;
    }
}
