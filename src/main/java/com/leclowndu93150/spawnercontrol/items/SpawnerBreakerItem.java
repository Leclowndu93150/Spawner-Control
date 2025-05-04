package com.leclowndu93150.spawnercontrol.items;

import com.leclowndu93150.spawnercontrol.config.SpawnerControlConfig;
import com.leclowndu93150.spawnercontrol.util.SpawnerHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;

import javax.annotation.Nullable;
import java.util.List;

public class SpawnerBreakerItem extends Item {

    public SpawnerBreakerItem(Properties properties) {
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

        if (!SpawnerControlConfig.ALLOW_SPAWNER_COLLECTION.get()) {
            if (player != null) {
                player.sendMessage(new TextComponent("Spawner collection is disabled in the config.").withStyle(ChatFormatting.RED), player.getUUID());
            }
            return InteractionResult.FAIL;
        }

        if (SpawnerHelper.isSpawner(level, pos)) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof SpawnerBlockEntity spawnerEntity) {
                try {
                    BaseSpawner baseSpawner = spawnerEntity.getSpawner();

                    ItemStack spawnerStack = new ItemStack(Blocks.SPAWNER);

                    CompoundTag itemTag = new CompoundTag();
                    CompoundTag blockEntityTag = new CompoundTag();

                     
                    baseSpawner.save(blockEntityTag);

                     
                    CompoundTag tileData = blockEntity.getTileData();
                    if (tileData.contains(SpawnerHelper.TAG_PERMANENTLY_DISABLED)) {
                        blockEntityTag.putBoolean(SpawnerHelper.TAG_PERMANENTLY_DISABLED,
                                tileData.getBoolean(SpawnerHelper.TAG_PERMANENTLY_DISABLED));
                    }

                    if (tileData.contains(SpawnerHelper.TAG_TEMPORARILY_DISABLED)) {
                        blockEntityTag.putBoolean(SpawnerHelper.TAG_TEMPORARILY_DISABLED,
                                tileData.getBoolean(SpawnerHelper.TAG_TEMPORARILY_DISABLED));
                    }

                    if (tileData.contains(SpawnerHelper.TAG_DISABLED_UNTIL)) {
                        blockEntityTag.putLong(SpawnerHelper.TAG_DISABLED_UNTIL,
                                tileData.getLong(SpawnerHelper.TAG_DISABLED_UNTIL));
                    }

                     
                    itemTag.put("BlockEntityTag", blockEntityTag);
                    spawnerStack.setTag(itemTag);

                    String entityId = "minecraft:pig";

                    if (blockEntityTag.contains("SpawnData") && blockEntityTag.getCompound("SpawnData").contains("entity")) {
                        CompoundTag entityData = blockEntityTag.getCompound("SpawnData").getCompound("entity");
                        if (entityData.contains("id")) {
                            entityId = entityData.getString("id");
                        }
                    }

                    String translationKey = "entity." + entityId.replace(":", ".");
                    Component entityName = new TranslatableComponent(translationKey);
                    spawnerStack.setHoverName(new TextComponent("Monster Spawner: ").append(entityName).withStyle(ChatFormatting.GOLD));

                     
                    level.removeBlock(pos, false);
                    level.addFreshEntity(SpawnerHelper.createItemEntity(level, pos, spawnerStack));

                    if (player != null) {
                        ItemStack itemStack = context.getItemInHand();
                        if (!player.getAbilities().instabuild) {
                            itemStack.hurt(1, level.getRandom(), null);
                        }

                        player.sendMessage(new TextComponent("Spawner collected successfully.").withStyle(ChatFormatting.GREEN), player.getUUID());
                    }

                    return InteractionResult.CONSUME;
                } catch (Exception e) {
                    if (player != null) {
                        player.sendMessage(new TextComponent("Error collecting spawner: " + e.getMessage()).withStyle(ChatFormatting.RED), player.getUUID());
                    }
                    return InteractionResult.FAIL;
                }
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(new TextComponent("Breaks a spawner and preserves its data").withStyle(ChatFormatting.GRAY));
        tooltip.add(new TextComponent("Durability: " + (stack.getMaxDamage() - stack.getDamageValue()) + "/" + stack.getMaxDamage()).withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return SpawnerControlConfig.SPAWNER_DROP_ENCHANTED.get() || super.isFoil(stack);
    }
}