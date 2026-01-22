package cz.maxtechnik.ntrials.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import cz.maxtechnik.ntrials.block.entity.TrialSpawnerBossBlockEntity;
import cz.maxtechnik.ntrials.init.other.NTrialsModEntityTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class TrialSpawnerBossBlockEntityRenderer implements BlockEntityRenderer<TrialSpawnerBossBlockEntity> {
    private final EntityRenderDispatcher entityRenderer;
    private static final Map<EntityType<?>, Entity> CACHED_ENTITIES = new HashMap<>();

    public TrialSpawnerBossBlockEntityRenderer() {
        this.entityRenderer = Minecraft.getInstance().getEntityRenderDispatcher();
    }

    @Override
    public void render(@NotNull TrialSpawnerBossBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Level level = blockEntity.getLevel();
        if (level == null) {
            return;
        }


        // Display the correct mob model (if bossMobType is Breeze Boss, show regular Breeze)
        EntityType<?> entityType = blockEntity.getBossMobType();
        if (entityType == NTrialsModEntityTypes.BREEZE_BOSS.get()) {
            entityType = NTrialsModEntityTypes.BREEZE.get();
        }

        // Get or create cached entity for this type
        Entity entity = getOrCreateEntity(entityType, level);
        if (entity == null) {
            return;
        }

        poseStack.pushPose();

        // Position the entity higher and more centered
        poseStack.translate(0.5, 0.25, 0.5);

        // Much faster and more visible rotation
        float time = (float) blockEntity.getClientTickCount() + partialTick;
        float rotation = time * 16.0F; // Faster rotation
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        float scale = 0.3F;
        poseStack.scale(scale, scale, scale);

        // Set entity properties for proper rendering
        entity.setYRot(0.0F);
        entity.yRotO = 0.0F;
        entity.setXRot(0.0F);
        entity.xRotO = 0.0F;

        // Make sure entity is visible
        entity.setInvisible(false);

        // Render the entity with full brightness
        try {
            this.entityRenderer.render(entity, 0.0, 0.0, 0.0, 0.0F, partialTick, poseStack, bufferSource, 15728880); // Full brightness
        } catch (Exception e) {
            // If rendering fails, remove from cache and skip
            CACHED_ENTITIES.remove(entityType);
        }

        poseStack.popPose();
    }

    private Entity getOrCreateEntity(EntityType<?> entityType, Level level) {
        // Use cached entity if available
        Entity entity = CACHED_ENTITIES.get(entityType);
        if (entity != null && entity.level() == level) {
            return entity;
        }

        // Create new entity and cache it
        try {
            entity = entityType.create(level);
            if (entity != null) {
                // Initialize entity for rendering
                entity.tickCount = 0;
                CACHED_ENTITIES.put(entityType, entity);
            }
            return entity;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public int getViewDistance() {
        return 80;
    }
}

