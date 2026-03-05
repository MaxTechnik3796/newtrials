package cz.maxtechnik.ntrials.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import cz.maxtechnik.ntrials.block.TrialSpawnerBlock;
import cz.maxtechnik.ntrials.block.entity.TrialSpawnerBlockEntity;
import cz.maxtechnik.ntrials.init.other.NTrialsModEntityTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class TrialSpawnerBlockEntityRenderer implements BlockEntityRenderer<TrialSpawnerBlockEntity> {
    private final EntityRenderDispatcher entityRenderer;
    private static final Map<EntityType<?>, Entity> CACHED_ENTITIES = new HashMap<>();

    public TrialSpawnerBlockEntityRenderer() {
        this.entityRenderer = Minecraft.getInstance().getEntityRenderDispatcher();
    }

    @Override
    public void render(@NotNull TrialSpawnerBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Level level = blockEntity.getLevel();
        if (level == null) return;

        BlockState state = level.getBlockState(blockEntity.getBlockPos());
        if (!(state.getBlock() instanceof TrialSpawnerBlock)) return;

        TrialSpawnerBlock.SpawnerType spawnerType = state.getValue(TrialSpawnerBlock.TYPE);
        EntityType<?> entityType;

        if (spawnerType == TrialSpawnerBlock.SpawnerType.BOSS) {
            // Boss mode: show boss mob type (swap BREEZE_BOSS for BREEZE visual)
            entityType = blockEntity.getBossMobType();
            if (entityType == NTrialsModEntityTypes.BREEZE_BOSS.get()) {
                entityType = NTrialsModEntityTypes.BREEZE.get();
            }
        } else {
            // Normal mode: show spawn entity
            if (!blockEntity.hasSpawnEntity()) return;
            entityType = blockEntity.getSpawnEntity();
        }

        if (entityType == null) return;

        Entity entity = getOrCreateEntity(entityType, level);
        if (entity == null) return;

        poseStack.pushPose();
        poseStack.translate(0.5, 0.25, 0.5);

        float time = (float) blockEntity.getClientTickCount() + partialTick;
        float rotation = time * 16.0F;
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        float scale = 0.3F;
        poseStack.scale(scale, scale, scale);

        entity.setYRot(0.0F);
        entity.yRotO = 0.0F;
        entity.setXRot(0.0F);
        entity.xRotO = 0.0F;
        entity.setInvisible(false);

        try {
            this.entityRenderer.render(entity, 0.0, 0.0, 0.0, 0.0F, partialTick, poseStack, bufferSource, 15728880);
        } catch (Exception e) {
            CACHED_ENTITIES.remove(entityType);
        }

        poseStack.popPose();
    }

    private Entity getOrCreateEntity(EntityType<?> entityType, Level level) {
        Entity entity = CACHED_ENTITIES.get(entityType);
        if (entity != null && entity.level() == level) {
            return entity;
        }

        try {
            entity = entityType.create(level);
            if (entity != null) {
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
