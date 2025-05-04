package com.leclowndu93150.spawnercontrol.mixin;

import com.leclowndu93150.spawnercontrol.SpawnerControl;
import com.leclowndu93150.spawnercontrol.util.SpawnerHelper;
import com.leclowndu93150.spawnercontrol.util.TogglerState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.InclusiveRange;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Mixin(BaseSpawner.class)
public class BaseSpawnerMixin {

    @Shadow private int spawnDelay;


    private static final Map<BlockPos, TogglerState> TOGGLER_CACHE = new HashMap<>();
    private static final int CHECK_INTERVAL_TICKS = 20;  
    private static long lastGlobalCheckTime = 0;
    
    private static final String PROCESSED_KEY = "SpawnerControlProcessed";
    private static boolean isProcessing = false;

    @Inject(method = "serverTick", at = @At("HEAD"), cancellable = true)
    private void onServerTick(ServerLevel level, BlockPos pos, CallbackInfo ci) {
        if (level == null) return;

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof SpawnerBlockEntity) {
             
            boolean shouldBeDisabled = checkAndUpdateTogglerState(level, pos);

             
            CompoundTag tag = blockEntity.getTileData();
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

             
            if (shouldBeDisabled) {
                ci.cancel();
            }
        }
    }

    /*
     * Checks for adjacent toggler blocks and updates the spawner state accordingly.
     * Returns true if the spawner should be disabled.
     */
    private boolean checkAndUpdateTogglerState(Level level, BlockPos pos) {
         
        long currentTime = System.currentTimeMillis();
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) return false;

        CompoundTag tileData = blockEntity.getTileData();

         
        TogglerState cachedState = TOGGLER_CACHE.get(pos);
        if (cachedState != null && !cachedState.isExpired()) {
             
            if (cachedState.hasToggler) {
                boolean isCurrentlyDisabled = tileData.getBoolean(SpawnerHelper.TAG_PERMANENTLY_DISABLED);
                boolean shouldBeDisabled = cachedState.togglerPowered;

                 
                if (isCurrentlyDisabled != shouldBeDisabled) {
                    tileData.putBoolean(SpawnerHelper.TAG_PERMANENTLY_DISABLED, shouldBeDisabled);
                    blockEntity.setChanged();
                    level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
                }

                return shouldBeDisabled;
            } else if (tileData.getBoolean(SpawnerHelper.TAG_PERMANENTLY_DISABLED)) {
                 
                tileData.putBoolean(SpawnerHelper.TAG_PERMANENTLY_DISABLED, false);
                blockEntity.setChanged();
                level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
                return false;
            }
            return tileData.getBoolean(SpawnerHelper.TAG_PERMANENTLY_DISABLED);
        }

         
        if (currentTime - lastGlobalCheckTime < 1000) {
            return tileData.getBoolean(SpawnerHelper.TAG_PERMANENTLY_DISABLED);
        }

        lastGlobalCheckTime = currentTime;

         
        Block togglerBlock = SpawnerControl.SPAWNER_TOGGLER_BLOCK.get();
        boolean foundToggler = false;
        boolean togglerPowered = false;

         
        for (Direction direction : Direction.values()) {
            BlockPos checkPos = pos.relative(direction);
            BlockState checkState = level.getBlockState(checkPos);
            Block checkBlock = checkState.getBlock();

             
            if (checkBlock == togglerBlock) {
                foundToggler = true;

                 
                if (checkState.hasProperty(BlockStateProperties.POWERED)) {
                    togglerPowered = checkState.getValue(BlockStateProperties.POWERED);
                    break;  
                }
            }
        }

         
        TOGGLER_CACHE.put(pos, new TogglerState(foundToggler, togglerPowered));

         
        if (foundToggler) {
            boolean isCurrentlyDisabled = tileData.getBoolean(SpawnerHelper.TAG_PERMANENTLY_DISABLED);

             
            if (isCurrentlyDisabled != togglerPowered) {
                tileData.putBoolean(SpawnerHelper.TAG_PERMANENTLY_DISABLED, togglerPowered);
                blockEntity.setChanged();
                level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
            }

            return togglerPowered;
        } else if (tileData.getBoolean(SpawnerHelper.TAG_PERMANENTLY_DISABLED)) {
             
            tileData.putBoolean(SpawnerHelper.TAG_PERMANENTLY_DISABLED, false);
            blockEntity.setChanged();
            level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
            return false;
        }

         
        return tileData.getBoolean(SpawnerHelper.TAG_PERMANENTLY_DISABLED);
    }

     
    @Redirect(method = "serverTick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/SpawnData$CustomSpawnRules;blockLightLimit()Lnet/minecraft/util/InclusiveRange;"))
    private InclusiveRange<Integer> modifyBlockLightLimit(SpawnData.CustomSpawnRules customSpawnRules) {
         
        return new InclusiveRange<>(0, 15);
    }

    @Redirect(method = "serverTick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/SpawnData$CustomSpawnRules;skyLightLimit()Lnet/minecraft/util/InclusiveRange;"))
    private InclusiveRange<Integer> modifySkyLightLimit(SpawnData.CustomSpawnRules customSpawnRules) {
         
        return new InclusiveRange<>(0, 15);
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