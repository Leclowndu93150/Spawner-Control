package com.leclowndu93150.spawnercontrol.data;

import com.leclowndu93150.spawnercontrol.SpawnerControl;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(modid = SpawnerControl.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SpawnerControlDataGenerator {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(new SpawnerControlBlockStateProvider(generator, existingFileHelper));
    }
}
