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

public class SpawnerTogglerItem extends Item {

    public SpawnerTogglerItem(Properties properties) {
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
                boolean disabled = tileData.getBoolean(SpawnerHelper.TAG_PERMANENTLY_DISABLED);

                try {
                    if (disabled) {
                         
                        tileData.putBoolean(SpawnerHelper.TAG_PERMANENTLY_DISABLED, false);
                        if (player != null) {
                            player.sendMessage(new TextComponent("Spawner enabled.").withStyle(ChatFormatting.GREEN), player.getUUID());
                        }
                    } else {
                         
                        tileData.putBoolean(SpawnerHelper.TAG_PERMANENTLY_DISABLED, true);
                        if (player != null) {
                            player.sendMessage(new TextComponent("Spawner disabled.").withStyle(ChatFormatting.GREEN), player.getUUID());
                        }
                    }

                     
                    blockEntity.setChanged();
                    level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);

                     
                    if (player != null && !player.getAbilities().instabuild) {
                        ItemStack itemStack = context.getItemInHand();
                        itemStack.hurt(1, level.getRandom(), null);
                    }

                    return InteractionResult.CONSUME;
                } catch (Exception e) {
                    if (player != null) {
                        player.sendMessage(new TextComponent("Error toggling spawner: " + e.getMessage()).withStyle(ChatFormatting.RED), player.getUUID());
                    }
                    return InteractionResult.FAIL;
                }
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(new TextComponent("Toggles a spawner on/off permanently").withStyle(ChatFormatting.GRAY));
        tooltip.add(new TextComponent("Durability: " + (stack.getMaxDamage() - stack.getDamageValue()) + "/" + stack.getMaxDamage()).withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return SpawnerControlConfig.SPAWNER_DROP_ENCHANTED.get() || super.isFoil(stack);
    }
}