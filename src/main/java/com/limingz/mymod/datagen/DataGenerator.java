package com.limingz.mymod.datagen;

import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collections;
import java.util.List;

import static com.limingz.mymod.Main.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerator {
    @SubscribeEvent
    public static void generate(GatherDataEvent event) {
        var generator = event.getGenerator();
        var packoutput = generator.getPackOutput();
        var existingFileHelper = event.getExistingFileHelper();
        var lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeServer(), new Tags(packoutput, lookupProvider, MODID, existingFileHelper));
        generator.addProvider(event.includeServer(), new LootTableProvider(
                packoutput, Collections.emptySet(),
                List.of(new LootTableProvider.SubProviderEntry(
                        LootTables::new,
                        LootContextParamSets.BLOCK))));
        generator.addProvider(event.includeServer(), new Recipes(packoutput));
        generator.addProvider(event.includeServer(), new GlobalLootModifier(packoutput, MODID));
    }
}
