package com.leclowndu93150.spawnercontrol.items;

import com.leclowndu93150.spawnercontrol.config.SpawnerControlConfig;
import com.leclowndu93150.spawnercontrol.util.SpawnerHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;

import javax.annotation.Nullable;
import java.util.List;

public class SpawnerDisablerItem extends Item {

    public SpawnerDisablerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (SpawnerHelper.isSpawner(level, pos)) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof SpawnerBlockEntity) {
                CompoundTag tileData = blockEntity.getTileData();

                boolean isTemporarilyDisabled = tileData.getBoolean(SpawnerHelper.TAG_TEMPORARILY_DISABLED);
                long disabledUntil = tileData.getLong(SpawnerHelper.TAG_DISABLED_UNTIL);
                boolean stillDisabled = isTemporarilyDisabled && System.currentTimeMillis() < disabledUntil;

                if (!stillDisabled) {
                     
                     
                    long disableDurationMs = SpawnerControlConfig.DISABLER_DURATION.get() * 50L;
                    long currentTime = System.currentTimeMillis();
                    long disableUntilTime = currentTime + disableDurationMs;

                     
                    tileData.putBoolean(SpawnerHelper.TAG_TEMPORARILY_DISABLED, true);
                    tileData.putLong(SpawnerHelper.TAG_DISABLED_UNTIL, disableUntilTime);

                     
                    blockEntity.setChanged();
                    level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);

                    if (player != null) {
                        ItemStack itemStack = context.getItemInHand();
                        if (!player.getAbilities().instabuild) {
                            itemStack.shrink(1);
                        }

                        int durationMinutes = SpawnerControlConfig.DISABLER_DURATION.get() / 1200;  
                        player.sendMessage(new TextComponent("Spawner disabled for " + durationMinutes + " minutes.").withStyle(ChatFormatting.GREEN), player.getUUID());
                    }

                    return InteractionResult.CONSUME;
                } else {
                    if (player != null) {
                        long remainingMs = disabledUntil - System.currentTimeMillis();
                        int remainingSecs = (int)(remainingMs / 1000);
                        player.sendMessage(new TextComponent("This spawner is already disabled for " + remainingSecs + " more seconds.").withStyle(ChatFormatting.RED), player.getUUID());
                    }

                    return InteractionResult.FAIL;
                }
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int durationMinutes = SpawnerControlConfig.DISABLER_DURATION.get() / 1200;  
        tooltip.add(new TextComponent("Temporarily disables a spawner for " + durationMinutes + " minutes").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}