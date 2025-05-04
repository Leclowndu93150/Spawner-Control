package com.leclowndu93150.spawnercontrol.blocks;

import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

public class SpawnerTogglerBlock extends LeverBlock {
    public SpawnerTogglerBlock() {
        super(BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL)
                .strength(50.0F, 1200.0F)   
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops());
    }

    /**
     * Check if this lever is in the ON state
     */
    public static boolean isLeverOn(BlockState state) {
        return state.hasProperty(BlockStateProperties.POWERED) && state.getValue(BlockStateProperties.POWERED);
    }
}