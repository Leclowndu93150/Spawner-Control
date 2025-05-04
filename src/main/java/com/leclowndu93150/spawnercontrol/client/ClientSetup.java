package com.leclowndu93150.spawnercontrol.client;

import com.leclowndu93150.spawnercontrol.SpawnerControl;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = SpawnerControl.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
             // BlockEntityRenderers.register(BlockEntityType.MOB_SPAWNER, CustomSpawnerRenderer::new);
        });
    }
}
