package cz.maxtechnik.ntrials.block.entity;

import cz.maxtechnik.ntrials.init.NTrialsModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TrialSpawnerBlockEntity extends BlockEntity {
    private final Set<UUID> detectedPlayers = new HashSet<>();
    private int cooldownTime = 0;
    private boolean isOminous = false;
    private int tickCount = 0;
    private int clientTickCount = 0; // Client-side tick counter pro animace
    private EntityType<?> spawnEntity = null; // Tag pro uložení entity typu

    public TrialSpawnerBlockEntity(BlockPos pos, BlockState blockState) {
        super(NTrialsModBlockEntities.TRIAL_SPAWNER_BLOCK_ENTITY.get(), pos, blockState);
    }

    public void tick() {
        this.tickCount++;

        if (this.cooldownTime > 0) {
            this.cooldownTime--;
        }

        // Add your trial spawner logic here
        // This is where you would handle spawning, state transitions, etc.
    }

    public void clientTick() {
        this.clientTickCount++;
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        this.cooldownTime = tag.getInt("CooldownTime");
        this.isOminous = tag.getBoolean("Ominous");
        this.tickCount = tag.getInt("TickCount");

        // Load spawn entity
        if (tag.contains("SpawnEntity")) {
            String entityTypeId = tag.getString("SpawnEntity");
            ResourceLocation entityLocation = new ResourceLocation(entityTypeId);
            this.spawnEntity = ForgeRegistries.ENTITY_TYPES.getValue(entityLocation);
        } else {
            this.spawnEntity = null;
        }

        // Load detected players
        if (tag.contains("DetectedPlayers")) {
            this.detectedPlayers.clear();
            CompoundTag playersTag = tag.getCompound("DetectedPlayers");
            for (String key : playersTag.getAllKeys()) {
                try {
                    UUID uuid = UUID.fromString(key);
                    this.detectedPlayers.add(uuid);
                } catch (IllegalArgumentException ignored) {
                    // Invalid UUID, skip
                }
            }
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("CooldownTime", this.cooldownTime);
        tag.putBoolean("Ominous", this.isOminous);
        tag.putInt("TickCount", this.tickCount);

        // Save spawn entity
        if (this.spawnEntity != null) {
            ResourceLocation entityLocation = ForgeRegistries.ENTITY_TYPES.getKey(this.spawnEntity);
            if (entityLocation != null) {
                tag.putString("SpawnEntity", entityLocation.toString());
            }
        }

        // Save detected players
        CompoundTag playersTag = new CompoundTag();
        for (UUID uuid : this.detectedPlayers) {
            playersTag.putBoolean(uuid.toString(), true);
        }
        tag.put("DetectedPlayers", playersTag);
    }

    // Getters and setters
    public Set<UUID> getDetectedPlayers() {
        return detectedPlayers;
    }

    public int getCooldownTime() {
        return cooldownTime;
    }

    public void setCooldownTime(int cooldownTime) {
        this.cooldownTime = cooldownTime;
        setChanged();
    }

    public boolean isOminous() {
        return isOminous;
    }

    public void setOminous(boolean ominous) {
        this.isOminous = ominous;
        setChanged();
    }

    public int getTickCount() {
        return tickCount;
    }

    public int getClientTickCount() {
        return clientTickCount;
    }

    @Nullable
    public EntityType<?> getSpawnEntity() {
        return spawnEntity;
    }

    public void setSpawnEntity(@Nullable EntityType<?> spawnEntity) {
        this.spawnEntity = spawnEntity;
        setChanged();
    }

    public boolean hasSpawnEntity() {
        return this.spawnEntity != null;
    }

    public void addDetectedPlayer(UUID playerId) {
        this.detectedPlayers.add(playerId);
        setChanged();
    }

    public void removeDetectedPlayer(UUID playerId) {
        this.detectedPlayers.remove(playerId);
        setChanged();
    }

    public void clearDetectedPlayers() {
        this.detectedPlayers.clear();
        setChanged();
    }
}
