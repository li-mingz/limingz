package com.limingz.mymod.gui.overlay;

import com.limingz.mymod.capability.farmxp.PlayerFarmXpProvider;
import com.limingz.mymod.config.CommonConfig;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.util.concurrent.atomic.AtomicInteger;

public class FarmXpOverlay {
    public static IGuiOverlay FARM_XP_OVERLAY = ((gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        var font = gui.getFont();
        guiGraphics.drawString(font, "Farm Xp: " + PlayerFarmXpProvider.farm_xp_client, 0, 0, 0xffffff);

        var get_xp_set = CommonConfig.block_getxp_set;
        AtomicInteger index = new AtomicInteger(0);
        get_xp_set.forEach((block) -> {
            int xPos = index.getAndIncrement() * 20; // 每个方块间隔 20 像素
            guiGraphics.renderItem(new ItemStack(block), 0, 100 + xPos);
        });
    });
}
