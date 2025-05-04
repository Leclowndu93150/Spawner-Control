package com.leclowndu93150.spawnercontrol;

import com.leclowndu93150.spawnercontrol.blocks.SpawnerTogglerBlock;
import com.leclowndu93150.spawnercontrol.config.SpawnerControlConfig;
import com.leclowndu93150.spawnercontrol.events.SpawnerEventHandler;
import com.leclowndu93150.spawnercontrol.items.SpawnerBreakerItem;
import com.leclowndu93150.spawnercontrol.items.SpawnerDisablerItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(SpawnerControl.MODID)
public class SpawnerControl {
    public static final String MODID = "spawnercontrol";

    public static final CreativeModeTab SPAWNER_CONTROL_TAB = new CreativeModeTab(MODID) {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(SpawnerControl.SPAWNER_TOGGLER_BLOCK.get());
        }
    };

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);

    public static final RegistryObject<Block> SPAWNER_TOGGLER_BLOCK = BLOCKS.register("spawner_toggler_block",
            SpawnerTogglerBlock::new);

    public static final RegistryObject<Item> SPAWNER_TOGGLER_BLOCK_ITEM = ITEMS.register("spawner_toggler_block",
            () -> new BlockItem(SPAWNER_TOGGLER_BLOCK.get(), new Item.Properties().tab(SPAWNER_CONTROL_TAB)));

    public static final RegistryObject<Item> SPAWNER_DISABLER = ITEMS.register("spawner_disabler",
            () -> new SpawnerDisablerItem(new Item.Properties().tab(SPAWNER_CONTROL_TAB).stacksTo(16)));

    public static final RegistryObject<Item> SPAWNER_BREAKER = ITEMS.register("spawner_breaker",
            () -> new SpawnerBreakerItem(new Item.Properties().tab(SPAWNER_CONTROL_TAB).stacksTo(1).durability(1)));

    public SpawnerControl() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SpawnerControlConfig.SPEC);
        MinecraftForge.EVENT_BUS.register(SpawnerEventHandler.class);
        MinecraftForge.EVENT_BUS.register(this);
    }
}