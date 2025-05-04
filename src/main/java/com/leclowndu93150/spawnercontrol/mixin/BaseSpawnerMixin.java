package com.leclowndu93150.spawnercontrol.mixin;

import com.leclowndu93150.spawnercontrol.util.SpawnerHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BaseSpawner.class)
public class BaseSpawnerMixin {

    @Shadow private int spawnDelay;

    private static final String PROCESSED_KEY = "SpawnerControlProcessed";
    private static boolean isProcessing = false;

    @Inject(method = "serverTick", at = @At("HEAD"), cancellable = true)
    private void onServerTick(ServerLevel level, BlockPos pos, CallbackInfo ci) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof SpawnerBlockEntity) {
            CompoundTag tag = blockEntity.getTileData();

             
            if (tag.getBoolean(SpawnerHelper.TAG_PERMANENTLY_DISABLED)) {
                ci.cancel();
                return;
            }

             
            if (tag.getBoolean(SpawnerHelper.TAG_TEMPORARILY_DISABLED)) {
                long disabledUntil = tag.getLong(SpawnerHelper.TAG_DISABLED_UNTIL);
                long currentTime = System.currentTimeMillis();

                if (currentTime < disabledUntil) {
                     
                    ci.cancel();
                } else {
                     
                    tag.putBoolean(SpawnerHelper.TAG_TEMPORARILY_DISABLED, false);
                    blockEntity.setChanged();
                }
            }
        }
    }

    @Inject(method = "isNearPlayer", at = @At("HEAD"), cancellable = true)
    private void onIsNearPlayer(Level level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof SpawnerBlockEntity) {
            CompoundTag tag = blockEntity.getTileData();

             
            if (tag.getBoolean(SpawnerHelper.TAG_PERMANENTLY_DISABLED)) {
                cir.setReturnValue(false);
                return;
            }

             
            if (tag.getBoolean(SpawnerHelper.TAG_TEMPORARILY_DISABLED)) {
                long disabledUntil = tag.getLong(SpawnerHelper.TAG_DISABLED_UNTIL);
                long currentTime = System.currentTimeMillis();

                if (currentTime < disabledUntil) {
                    cir.setReturnValue(false);
                }
            }
        }
    }

    @Inject(method = "load", at = @At("RETURN"))
    private void onLoad(Level level, BlockPos pos, CompoundTag tag, CallbackInfo ci) {
        if (isProcessing || level == null || level.getBlockEntity(pos) == null) {
            return;
        }

        isProcessing = true;
        try {
             
            BlockEntity blockEntity = level.getBlockEntity(pos);
            CompoundTag tileData = blockEntity.getTileData();

             
            if (tag.contains(SpawnerHelper.TAG_PERMANENTLY_DISABLED)) {
                tileData.putBoolean(SpawnerHelper.TAG_PERMANENTLY_DISABLED,
                        tag.getBoolean(SpawnerHelper.TAG_PERMANENTLY_DISABLED));
            }

            if (tag.contains(SpawnerHelper.TAG_TEMPORARILY_DISABLED)) {
                tileData.putBoolean(SpawnerHelper.TAG_TEMPORARILY_DISABLED,
                        tag.getBoolean(SpawnerHelper.TAG_TEMPORARILY_DISABLED));
            }

            if (tag.contains(SpawnerHelper.TAG_DISABLED_UNTIL)) {
                tileData.putLong(SpawnerHelper.TAG_DISABLED_UNTIL,
                        tag.getLong(SpawnerHelper.TAG_DISABLED_UNTIL));
            }

            blockEntity.setChanged();
        } finally {
            isProcessing = false;
        }
    }

    @Inject(method = "save", at = @At("RETURN"))
    private void onSave(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        if (isProcessing) {
            return;
        }

        isProcessing = true;
        try {
            CompoundTag result = cir.getReturnValue();
            BaseSpawner spawner = (BaseSpawner)(Object)this;

             
            BlockEntity blockEntity = spawner.getSpawnerBlockEntity();
            if (blockEntity != null) {
                CompoundTag tileData = blockEntity.getTileData();

                 
                if (tileData.contains(SpawnerHelper.TAG_PERMANENTLY_DISABLED)) {
                    result.putBoolean(SpawnerHelper.TAG_PERMANENTLY_DISABLED,
                            tileData.getBoolean(SpawnerHelper.TAG_PERMANENTLY_DISABLED));
                }

                if (tileData.contains(SpawnerHelper.TAG_TEMPORARILY_DISABLED)) {
                    result.putBoolean(SpawnerHelper.TAG_TEMPORARILY_DISABLED,
                            tileData.getBoolean(SpawnerHelper.TAG_TEMPORARILY_DISABLED));
                }

                if (tileData.contains(SpawnerHelper.TAG_DISABLED_UNTIL)) {
                    result.putLong(SpawnerHelper.TAG_DISABLED_UNTIL,
                            tileData.getLong(SpawnerHelper.TAG_DISABLED_UNTIL));
                }
            }
        } finally {
            isProcessing = false;
        }
    }
}