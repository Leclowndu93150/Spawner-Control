package com.leclowndu93150.spawnercontrol.events;

import com.leclowndu93150.spawnercontrol.SpawnerControl;
import com.leclowndu93150.spawnercontrol.items.SpawnerBreakerItem;
import com.leclowndu93150.spawnercontrol.util.SpawnerHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Iterator;

@Mod.EventBusSubscriber(modid = SpawnerControl.MODID)
public class SpawnerEventHandler {

    /**
     * Handle spawner placement to preserve entity type
     */
    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        BlockState placedState = event.getPlacedBlock();
        Level level = (Level) event.getWorld();
        BlockPos pos = event.getPos();
        Entity entity = event.getEntity();

        if (placedState.getBlock() == Blocks.SPAWNER && entity instanceof Player player) {
            ItemStack heldItem = getSpawnerItemFromPlayer(player);

            if (heldItem != null && heldItem.getItem() == Blocks.SPAWNER.asItem() && heldItem.hasTag()) {
                level.getServer().tell(new net.minecraft.server.TickTask(0, () -> {
                    BlockEntity blockEntity = level.getBlockEntity(pos);
                    if (blockEntity instanceof SpawnerBlockEntity spawnerEntity) {
                        CompoundTag itemTag = heldItem.getTag();

                        if (itemTag != null && itemTag.contains("BlockEntityTag")) {
                            CompoundTag blockEntityTag = itemTag.getCompound("BlockEntityTag");

                            applySpawnerData(spawnerEntity, blockEntityTag, level, pos);
                        }
                    }
                }));
            }
        }
    }

    /**
     * Prevent breaking spawners with normal tools
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        BlockState state = event.getState();
        Player player = event.getPlayer();

        if (state.getBlock() == Blocks.SPAWNER) {
            ItemStack heldItem = player.getMainHandItem();
            boolean isUsingBreakerItem = heldItem.getItem() instanceof SpawnerBreakerItem;

            if (!isUsingBreakerItem && !player.getAbilities().instabuild) {
                event.setCanceled(true);
            }
        }
    }


    /**
     * Prevent spawners from being destroyed by explosions
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onExplosion(ExplosionEvent.Detonate event) {
        Explosion explosion = event.getExplosion();
        Level level = event.getWorld();

        Iterator<BlockPos> iterator = explosion.getToBlow().iterator();
        while (iterator.hasNext()) {
            BlockPos pos = iterator.next();
            if (level.getBlockState(pos).getBlock() == Blocks.SPAWNER) {
                iterator.remove();
            }
        }
    }

    /**
     * Prevent normal left-click mining of spawners
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Level level = event.getWorld();
        BlockPos pos = event.getPos();
        Player player = event.getPlayer();

        if (level.getBlockState(pos).getBlock() == Blocks.SPAWNER) {
            ItemStack heldItem = player.getMainHandItem();
            boolean isUsingBreakerItem = heldItem.getItem() instanceof SpawnerBreakerItem;

            if (!isUsingBreakerItem && !player.getAbilities().instabuild) {
                event.setCanceled(true);
                event.setUseBlock(Event.Result.DENY);
                event.setUseItem(Event.Result.DENY);
            }
        }
    }

    /**
     * Handle right-click interactions with spawners
     * Only allow interaction with our mod's items
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getWorld();
        BlockPos pos = event.getPos();
        Player player = event.getPlayer();
        ItemStack heldItem = player.getMainHandItem();

        if (level.getBlockState(pos).getBlock() == Blocks.SPAWNER) {
            boolean isUsingModItem = heldItem.getItem() == SpawnerControl.SPAWNER_DISABLER.get() ||
                    heldItem.getItem() == SpawnerControl.SPAWNER_BREAKER.get();

            if (!isUsingModItem && !player.getAbilities().instabuild) {
                event.setCanceled(true);
                event.setUseBlock(Event.Result.DENY);
                event.setUseItem(Event.Result.DENY);
            }
        }
    }

    /**
     * Helper method to get the spawner item from a player
     */
    private static ItemStack getSpawnerItemFromPlayer(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() == Blocks.SPAWNER.asItem()) {
            return mainHand;
        }

        ItemStack offHand = player.getOffhandItem();
        if (offHand.getItem() == Blocks.SPAWNER.asItem()) {
            return offHand;
        }

        return null;
    }

    /**
     * Helper method to apply spawner data from NBT
     */
    private static void applySpawnerData(SpawnerBlockEntity spawnerEntity, CompoundTag blockEntityTag, Level level, BlockPos pos) {
        try {
            spawnerEntity.load(blockEntityTag);

            CompoundTag tileData = spawnerEntity.getTileData();

            if (blockEntityTag.contains(SpawnerHelper.TAG_PERMANENTLY_DISABLED)) {
                tileData.putBoolean(SpawnerHelper.TAG_PERMANENTLY_DISABLED,
                        blockEntityTag.getBoolean(SpawnerHelper.TAG_PERMANENTLY_DISABLED));
            }

            if (blockEntityTag.contains(SpawnerHelper.TAG_TEMPORARILY_DISABLED)) {
                tileData.putBoolean(SpawnerHelper.TAG_TEMPORARILY_DISABLED,
                        blockEntityTag.getBoolean(SpawnerHelper.TAG_TEMPORARILY_DISABLED));
            }

            if (blockEntityTag.contains(SpawnerHelper.TAG_DISABLED_UNTIL)) {
                tileData.putLong(SpawnerHelper.TAG_DISABLED_UNTIL,
                        blockEntityTag.getLong(SpawnerHelper.TAG_DISABLED_UNTIL));
            }

            spawnerEntity.setChanged();
            level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
        } catch (Exception e) {
            System.out.println("Failed to apply spawner data: " + e.getMessage());
        }
    }
}