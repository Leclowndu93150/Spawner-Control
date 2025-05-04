package com.leclowndu93150.spawnercontrol.data;

import com.leclowndu93150.spawnercontrol.SpawnerControl;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

public class SpawnerControlBlockStateProvider extends BlockStateProvider {

    public SpawnerControlBlockStateProvider(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, SpawnerControl.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
         
        ModelFile leverOn = models().getExistingFile(new ResourceLocation("minecraft:block/lever_on"));
        ModelFile leverOff = models().getExistingFile(new ResourceLocation("minecraft:block/lever"));

         
        var togglerBlock = SpawnerControl.SPAWNER_TOGGLER_BLOCK.get();

         
        getVariantBuilder(togglerBlock)
                .forAllStates(state -> {
                    boolean powered = state.getValue(BlockStateProperties.POWERED);
                    AttachFace face = state.getValue(BlockStateProperties.ATTACH_FACE);
                    int rotationX = 0;
                    int rotationY = 0;

                    switch (face) {
                        case CEILING:
                            rotationX = 180;
                            break;
                        case FLOOR:
                            break;
                        case WALL:
                            rotationX = 90;
                            break;
                    }

                    switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
                        case NORTH:
                            if (face == AttachFace.WALL) {
                                rotationY = 0;
                            } else {
                                rotationY = 0;
                            }
                            break;
                        case EAST:
                            if (face == AttachFace.WALL) {
                                rotationY = 90;
                            } else {
                                rotationY = 90;
                            }
                            break;
                        case SOUTH:
                            if (face == AttachFace.WALL) {
                                rotationY = 180;
                            } else {
                                rotationY = 180;
                            }
                            break;
                        case WEST:
                            if (face == AttachFace.WALL) {
                                rotationY = 270;
                            } else {
                                rotationY = 270;
                            }
                            break;
                    }

                    return ConfiguredModel.builder()
                            .modelFile(powered ? leverOn : leverOff)
                            .rotationX(rotationX)
                            .rotationY(rotationY)
                            .build();
                });

         
        itemModels().withExistingParent("item/" + SpawnerControl.SPAWNER_TOGGLER_BLOCK.getId().getPath(),
                new ResourceLocation("minecraft:item/lever"));

         
        itemModels().withExistingParent("item/spawner_disabler", "item/generated")
                .texture("layer0", modLoc("item/spawner_disabler"));

        itemModels().withExistingParent("item/spawner_breaker", "item/generated")
                .texture("layer0", modLoc("item/spawner_breaker"));

        itemModels().withExistingParent("item/spawner_toggler", "item/generated")
                .texture("layer0", modLoc("item/spawner_toggler"));
    }
}