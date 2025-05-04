package com.leclowndu93150.spawnercontrol.render;

import com.leclowndu93150.spawnercontrol.util.SpawnerHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CustomSpawnerRenderer implements BlockEntityRenderer<SpawnerBlockEntity> {
    private static final Material NORMAL_CAGE_TEXTURE =
            new Material(TextureAtlas.LOCATION_BLOCKS,
                    new ResourceLocation("minecraft:block/spawner"));

    private static final Material DISABLED_CAGE_TEXTURE =
            new Material(TextureAtlas.LOCATION_BLOCKS,
                    new ResourceLocation("spawnercontrol:block/disabled_spawner"));

    private static final Material TEMPORARY_DISABLED_CAGE_TEXTURE =
            new Material(TextureAtlas.LOCATION_BLOCKS,
                    new ResourceLocation("spawnercontrol:block/temp_disabled_spawner"));

    public CustomSpawnerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(SpawnerBlockEntity spawnerEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);

        CompoundTag tileData = spawnerEntity.getTileData();
        boolean isPermanentlyDisabled = tileData.getBoolean(SpawnerHelper.TAG_PERMANENTLY_DISABLED);
        boolean isTemporarilyDisabled = tileData.getBoolean(SpawnerHelper.TAG_TEMPORARILY_DISABLED);
        long disabledUntil = tileData.getLong(SpawnerHelper.TAG_DISABLED_UNTIL);
        boolean isStillTemporarilyDisabled = isTemporarilyDisabled && System.currentTimeMillis() < disabledUntil;

         
        Material cageTexture;
        if (isPermanentlyDisabled) {
            cageTexture = DISABLED_CAGE_TEXTURE;
        } else if (isStillTemporarilyDisabled) {
            cageTexture = TEMPORARY_DISABLED_CAGE_TEXTURE;
        } else {
            cageTexture = NORMAL_CAGE_TEXTURE;
        }

         
        VertexConsumer vertexConsumer = cageTexture.buffer(buffer, RenderType::entitySolid);
        renderCage(poseStack, vertexConsumer, combinedLight, combinedOverlay);

         
        BaseSpawner baseSpawner = spawnerEntity.getSpawner();
        Entity displayEntity = baseSpawner.getOrCreateDisplayEntity(spawnerEntity.getLevel());

        if (displayEntity != null) {
            float scale = 0.53125F;
            float maxSize = Math.max(displayEntity.getBbWidth(), displayEntity.getBbHeight());
            if (maxSize > 1.0F) {
                scale /= maxSize;
            }

            poseStack.translate(0.0F, 0.4F, 0.0F);

             
            if (!isPermanentlyDisabled && !isStillTemporarilyDisabled) {
                poseStack.mulPose(Vector3f.YP.rotationDegrees(
                        (float) (Mth.lerp(partialTick, baseSpawner.getoSpin(), baseSpawner.getSpin()) * 10.0F)));
            }

            poseStack.translate(0.0F, -0.2F, 0.0F);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(-30.0F));
            poseStack.scale(scale, scale, scale);

            Minecraft.getInstance().getEntityRenderDispatcher().render(
                    displayEntity, 0.0F, 0.0F, 0.0F, 0.0F, partialTick,
                    poseStack, buffer, combinedLight);
        }

        poseStack.popPose();
    }

    /**
     * Renders the cage part of the spawner
     */
    private void renderCage(PoseStack poseStack, VertexConsumer vertexConsumer, int combinedLight, int combinedOverlay) {
        Matrix4f matrix = poseStack.last().pose();
        float size = 0.4375F;  

         
        renderQuad(matrix, vertexConsumer, -size, size, -size, size, size, size, size, size, -size, -size, size, -size, 0, 1, 0, combinedLight, combinedOverlay);

         
        renderQuad(matrix, vertexConsumer, -size, -size, size, size, -size, size, size, -size, -size, -size, -size, -size, 0, -1, 0, combinedLight, combinedOverlay);

         
        renderQuad(matrix, vertexConsumer, -size, -size, -size, size, -size, -size, size, size, -size, -size, size, -size, 0, 0, -1, combinedLight, combinedOverlay);

         
        renderQuad(matrix, vertexConsumer, -size, size, size, size, size, size, size, -size, size, -size, -size, size, 0, 0, 1, combinedLight, combinedOverlay);

         
        renderQuad(matrix, vertexConsumer, -size, -size, size, -size, -size, -size, -size, size, -size, -size, size, size, -1, 0, 0, combinedLight, combinedOverlay);

         
        renderQuad(matrix, vertexConsumer, size, size, size, size, size, -size, size, -size, -size, size, -size, size, 1, 0, 0, combinedLight, combinedOverlay);
    }

    /**
     * Helper method to render a quad
     */
    private void renderQuad(Matrix4f matrix, VertexConsumer vertexConsumer,
                            float x1, float y1, float z1,
                            float x2, float y2, float z2,
                            float x3, float y3, float z3,
                            float x4, float y4, float z4,
                            float normalX, float normalY, float normalZ,
                            int combinedLight, int combinedOverlay) {
        vertexConsumer.vertex(matrix, x1, y1, z1).color(255, 255, 255, 255)
                .uv(0, 0).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normalX, normalY, normalZ).endVertex();
        vertexConsumer.vertex(matrix, x2, y2, z2).color(255, 255, 255, 255)
                .uv(1, 0).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normalX, normalY, normalZ).endVertex();
        vertexConsumer.vertex(matrix, x3, y3, z3).color(255, 255, 255, 255)
                .uv(1, 1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normalX, normalY, normalZ).endVertex();
        vertexConsumer.vertex(matrix, x4, y4, z4).color(255, 255, 255, 255)
                .uv(0, 1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normalX, normalY, normalZ).endVertex();
    }
}

