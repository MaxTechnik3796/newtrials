package cz.maxtechnik.ntrials.block.entity;

import cz.maxtechnik.ntrials.init.NTrialsModBlockEntities;
import cz.maxtechnik.ntrials.network.NetworkHandler;
import cz.maxtechnik.ntrials.network.TrialSpawnerSyncPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TrialSpawnerBlockEntity extends BlockEntity {
    private final Set<UUID> detectedPlayers = new HashSet<>();
    private final Set<UUID> spawnedEntities = new HashSet<>(); // Track spawned entities for this trial
    private int cooldownTime = 0;
    private boolean isOminous = false;
    private int tickCount = 0;
    private int clientTickCount = 0;
    private EntityType<?> spawnEntity = null;
    private boolean hasBeenSynced = false;

    // Trial spawner wave settings
    private int maxWaves = 5; // Default number of waves
    private int mobsPerWave = 3; // Default mobs per wave
    private int currentWave = 0; // Current wave number (0 = not started)
    private int currentWaveMobs = 0; // Number of alive mobs in current wave
    private boolean trialActive = false; // Whether trial is currently active
    private long lastPlayerCheckTime = 0; // Last time we checked for players

    public TrialSpawnerBlockEntity(BlockPos pos, BlockState blockState) {
        super(NTrialsModBlockEntities.TRIAL_SPAWNER_BLOCK_ENTITY.get(), pos, blockState);
    }

    public void tick() {
        this.tickCount++;

        if (this.cooldownTime > 0) {
            this.cooldownTime--;
        }

        // Check for players and manage trial every second (20 ticks)
        if (this.tickCount % 20 == 0) {
            checkAndManageTrial();
        }

        // Synchronize to clients periodically if needed (every 5 seconds)
        if (!hasBeenSynced && this.spawnEntity != null && this.tickCount % 100 == 0) {
            syncToClients();
            hasBeenSynced = true;
        }
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

        // Load wave settings
        this.maxWaves = tag.getInt("MaxWaves");
        if (this.maxWaves <= 0) this.maxWaves = 5; // Default fallback

        this.mobsPerWave = tag.getInt("MobsPerWave");
        if (this.mobsPerWave <= 0) this.mobsPerWave = 3; // Default fallback

        this.currentWave = tag.getInt("CurrentWave");
        this.currentWaveMobs = tag.getInt("CurrentWaveMobs");
        this.trialActive = tag.getBoolean("TrialActive");

        // Load spawn entity
        if (tag.contains("SpawnEntity")) {
            String entityTypeId = tag.getString("SpawnEntity");
            ResourceLocation entityLocation = new ResourceLocation(entityTypeId);
            this.spawnEntity = ForgeRegistries.ENTITY_TYPES.getValue(entityLocation);
        } else {
            this.spawnEntity = null;
        }

        // Load spawned entities UUIDs
        if (tag.contains("SpawnedEntities")) {
            this.spawnedEntities.clear();
            CompoundTag spawnedTag = tag.getCompound("SpawnedEntities");
            for (String key : spawnedTag.getAllKeys()) {
                try {
                    UUID uuid = UUID.fromString(key);
                    this.spawnedEntities.add(uuid);
                } catch (IllegalArgumentException ignored) {
                    // Invalid UUID, skip
                }
            }
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

        // Synchronize to client when data is loaded (only on server side)
        if (level != null && !level.isClientSide() && this.spawnEntity != null) {
            syncToClients();
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("CooldownTime", this.cooldownTime);
        tag.putBoolean("Ominous", this.isOminous);
        tag.putInt("TickCount", this.tickCount);

        // Save wave settings
        tag.putInt("MaxWaves", this.maxWaves);
        tag.putInt("MobsPerWave", this.mobsPerWave);
        tag.putInt("CurrentWave", this.currentWave);
        tag.putInt("CurrentWaveMobs", this.currentWaveMobs);
        tag.putBoolean("TrialActive", this.trialActive);

        // Save spawn entity
        if (this.spawnEntity != null) {
            ResourceLocation entityLocation = ForgeRegistries.ENTITY_TYPES.getKey(this.spawnEntity);
            if (entityLocation != null) {
                tag.putString("SpawnEntity", entityLocation.toString());
            }
        }

        // Save spawned entities UUIDs
        CompoundTag spawnedTag = new CompoundTag();
        for (UUID uuid : this.spawnedEntities) {
            spawnedTag.putBoolean(uuid.toString(), true);
        }
        tag.put("SpawnedEntities", spawnedTag);

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
        this.hasBeenSynced = false; // Reset sync flag when entity changes
        setChanged();

        // Immediately sync to clients when spawn entity is set
        syncToClients();
    }

    private void syncToClients() {
        if (level != null && !level.isClientSide() && level instanceof ServerLevel serverLevel) {
            TrialSpawnerSyncPacket packet = new TrialSpawnerSyncPacket(getBlockPos(), this.spawnEntity);
            NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() ->
                serverLevel.getChunkAt(getBlockPos())), packet);
        }
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

    // Wave configuration getters and setters
    public int getMaxWaves() {
        return maxWaves;
    }

    public void setMaxWaves(int maxWaves) {
        this.maxWaves = Math.max(1, maxWaves); // Minimum 1 wave
        setChanged();
    }

    public int getMobsPerWave() {
        return mobsPerWave;
    }

    public void setMobsPerWave(int mobsPerWave) {
        this.mobsPerWave = Math.max(1, mobsPerWave); // Minimum 1 mob per wave
        setChanged();
    }

    public int getCurrentWave() {
        return currentWave;
    }

    public boolean isTrialActive() {
        return trialActive;
    }

    public int getSpawnedEntitiesCount() {
        return spawnedEntities.size();
    }

    private void checkAndManageTrial() {
        if (level == null || level.isClientSide() || !hasSpawnEntity()) {
            return;
        }

        // Don't start trial during cooldown
        if (this.cooldownTime > 0) {
            return;
        }

        // Check for players in range
        double range = 8.0;
        net.minecraft.world.phys.AABB searchArea = new net.minecraft.world.phys.AABB(
                getBlockPos().getX() - range, getBlockPos().getY() - range, getBlockPos().getZ() - range,
                getBlockPos().getX() + range, getBlockPos().getY() + range, getBlockPos().getZ() + range
        );

        List<net.minecraft.world.entity.player.Player> players = level.getEntitiesOfClass(
                net.minecraft.world.entity.player.Player.class, searchArea);

        boolean hasPlayers = !players.isEmpty();

        if (hasPlayers && !trialActive && currentWave == 0) {
            // Start trial
            startTrial();
        } else if (!hasPlayers && trialActive) {
            // Stop trial if no players
            stopTrial();
        } else if (trialActive) {
            // Check if current wave is completed
            checkWaveCompletion();
        }

        // Update block state based on trial status
        updateBlockState();
    }

    private void updateBlockState() {
        if (level == null || level.isClientSide()) {
            return;
        }

        BlockState currentState = level.getBlockState(getBlockPos());
        if (!(currentState.getBlock() instanceof cz.maxtechnik.ntrials.block.TrialSpawnerBlock)) {
            return;
        }

        cz.maxtechnik.ntrials.block.TrialSpawnerBlock.TrialSpawnerState newState;

        if (this.cooldownTime > 0) {
            newState = cz.maxtechnik.ntrials.block.TrialSpawnerBlock.TrialSpawnerState.COOLDOWN;
        } else if (this.trialActive) {
            newState = cz.maxtechnik.ntrials.block.TrialSpawnerBlock.TrialSpawnerState.ACTIVE;
        } else if (!hasSpawnEntity()) {
            newState = cz.maxtechnik.ntrials.block.TrialSpawnerBlock.TrialSpawnerState.INACTIVE;
        } else {
            newState = cz.maxtechnik.ntrials.block.TrialSpawnerBlock.TrialSpawnerState.WAITING_FOR_PLAYERS;
        }

        if (currentState.getValue(cz.maxtechnik.ntrials.block.TrialSpawnerBlock.STATE) != newState) {
            level.setBlock(getBlockPos(), currentState.setValue(cz.maxtechnik.ntrials.block.TrialSpawnerBlock.STATE, newState), 3);
        }
    }

    private void startTrial() {
        System.out.println("Starting trial at " + getBlockPos());
        trialActive = true;
        currentWave = 1;
        currentWaveMobs = 0;
        spawnedEntities.clear();
        spawnWave();
    }

    private void stopTrial() {
        System.out.println("Stopping trial at " + getBlockPos());
        trialActive = false;
        currentWave = 0;
        currentWaveMobs = 0;
        // Kill all spawned entities
        cleanupSpawnedEntities();
        spawnedEntities.clear();
    }

    private void spawnWave() {
        if (level == null || spawnEntity == null) {
            return;
        }

        System.out.println("Spawning wave " + currentWave + "/" + maxWaves + " at " + getBlockPos());

        for (int i = 0; i < mobsPerWave; i++) {
            // Find spawn position around the spawner
            BlockPos spawnPos = findSpawnPosition();
            if (spawnPos != null) {
                net.minecraft.world.entity.Entity entity = spawnEntity.create(level);
                if (entity != null) {
                    entity.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);

                    // Make sure it's a mob and set it up properly
                    if (entity instanceof net.minecraft.world.entity.Mob mob) {
                        mob.finalizeSpawn((net.minecraft.server.level.ServerLevel) level,
                                level.getCurrentDifficultyAt(spawnPos),
                                net.minecraft.world.entity.MobSpawnType.SPAWNER,
                                null, null);
                    }

                    level.addFreshEntity(entity);
                    spawnedEntities.add(entity.getUUID());
                    currentWaveMobs++;

                    System.out.println("Spawned " + entity.getType().getDescriptionId() + " at " + spawnPos);
                }
            }
        }
    }

    private BlockPos findSpawnPosition() {
        // Try to find a valid spawn position around the spawner
        for (int attempts = 0; attempts < 10; attempts++) {
            int x = getBlockPos().getX() + (level.random.nextInt(7) - 3); // -3 to +3
            int z = getBlockPos().getZ() + (level.random.nextInt(7) - 3);
            int y = getBlockPos().getY();

            // Check a few blocks up and down
            for (int yOffset = -1; yOffset <= 2; yOffset++) {
                BlockPos pos = new BlockPos(x, y + yOffset, z);
                if (isValidSpawnPosition(pos)) {
                    return pos;
                }
            }
        }

        // Fallback to spawner position
        return getBlockPos().above();
    }

    private boolean isValidSpawnPosition(BlockPos pos) {
        if (level == null) return false;

        // Check if the position and the block above are air/passable
        return level.getBlockState(pos).isAir() &&
                level.getBlockState(pos.above()).isAir() &&
                level.getBlockState(pos.below()).isSolid();
    }

    private void checkWaveCompletion() {
        // Count how many spawned entities are still alive
        int aliveCount = 0;
        spawnedEntities.removeIf(uuid -> {
            net.minecraft.world.entity.Entity entity = ((net.minecraft.server.level.ServerLevel) level).getEntity(uuid);
            return entity == null || !entity.isAlive();
        });

        aliveCount = spawnedEntities.size();

        if (aliveCount == 0 && currentWaveMobs > 0) {
            // Wave completed
            if (currentWave < maxWaves) {
                // Start next wave
                currentWave++;
                currentWaveMobs = 0;
                spawnWave();
            } else {
                // Trial completed
                completeTrial();
            }
        }
    }

    private void completeTrial() {
        System.out.println("Trial completed at " + getBlockPos());
        trialActive = false;
        currentWave = 0;
        currentWaveMobs = 0;
        spawnedEntities.clear();

        // Set cooldown
        setCooldownTime(6000); // 5 minutes cooldown
    }

    private void cleanupSpawnedEntities() {
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            spawnedEntities.forEach(uuid -> {
                net.minecraft.world.entity.Entity entity = serverLevel.getEntity(uuid);
                if (entity != null) {
                    entity.discard();
                }
            });
        }
    }
}
