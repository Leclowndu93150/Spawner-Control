package com.leclowndu93150.spawnercontrol.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class SpawnerHelper {
    public static final String TAG_PERMANENTLY_DISABLED = "SpawnerControlPermanentlyDisabled";
    public static final String TAG_TEMPORARILY_DISABLED = "SpawnerControlTemporarilyDisabled";
    public static final String TAG_DISABLED_UNTIL = "SpawnerControlDisabledUntil";

    public static boolean isSpawner(Level level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() == Blocks.SPAWNER;
    }

    public static ItemEntity createItemEntity(Level level, BlockPos pos, ItemStack stack) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        ItemEntity itemEntity = new ItemEntity(level, x, y, z, stack);
        itemEntity.setDefaultPickUpDelay();
        return itemEntity;
    }
}