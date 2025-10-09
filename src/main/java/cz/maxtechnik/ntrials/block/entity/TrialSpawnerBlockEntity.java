package cz.maxtechnik.ntrials.block.entity;

import cz.maxtechnik.ntrials.init.NTrialsModBlockEntities;
import cz.maxtechnik.ntrials.init.NTrialsModSounds;
import cz.maxtechnik.ntrials.network.NetworkHandler;
import cz.maxtechnik.ntrials.network.TrialSpawnerSyncPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random; // For client-side randomness if needed, but level.random is available
import cz.maxtechnik.ntrials.init.NTrialsModMobEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.entity.AreaEffectCloud;
import java.util.ArrayList;
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

    // Client-side only for detecting ominous state change
    private transient boolean wasOminous = false;

    // Trial spawner wave settings
    private int maxWaves = 2; // Default number of waves
    private int mobsPerWave = 3; // Default mobs per wave
    private int currentTrialMobsPerWave = 2; // Actual mobs per wave for current trial (scaled by player count)
    private int maxWavesCount = 2;
	private int playersCount = 0;
	private int currentWave = 0; // Current wave number (0 = not started)
    private int currentWaveMobs = 0; // Number of alive mobs in current wave
    private boolean trialActive = false; // Whether trial is currently active
    private long lastPlayerCheckTime = 0; // Last time we checked for players

    // Loot table settings
    private String normalLootTable = "minecraft:chests/desert_pyramid"; // Default loot table for normal state
    private String ominousLootTable = "minecraft:chests/village/village_plains_house"; // Default loot table for ominous state

    // Ominous effects list
    private static final List<MobEffect> OMNIOUS_EFFECTS = List.of(
        MobEffects.REGENERATION,
        MobEffects.BLINDNESS,
        MobEffects.POISON,
        MobEffects.MOVEMENT_SLOWDOWN,
        MobEffects.CONFUSION,
        MobEffects.WEAKNESS,
        MobEffects.MOVEMENT_SPEED,
			MobEffects.DAMAGE_BOOST
    );

    // Loot animation system
    private boolean isLootAnimating = false;
    private int lootAnimationTick = 0;
    private List<ItemStack> pendingLootItems = new ArrayList<>();
    private int currentLootDropIndex = 0;
    private static final int LOOT_DROP_INTERVAL = 10; // Ticks between each item drop (0.5 seconds)

    public TrialSpawnerBlockEntity(BlockPos pos, BlockState blockState) {
        super(NTrialsModBlockEntities.TRIAL_SPAWNER_BLOCK_ENTITY.get(), pos, blockState);
    }

    public void tick() {
        this.tickCount++;

        if (this.cooldownTime > 0) {
            this.cooldownTime--;
        }

        // Handle loot animation
        if (isLootAnimating) {
            tickLootAnimation();
        }

        // Play ambient sounds periodically (every 3 seconds when waiting for players)
        if (this.tickCount % 60 == 0 && !level.isClientSide() && hasSpawnEntity() && !trialActive && cooldownTime == 0) {
            BlockState currentState = level.getBlockState(getBlockPos());
            boolean isOminousBlock = currentState.getValue(cz.maxtechnik.ntrials.block.TrialSpawnerBlock.OMINOUS);

            if (isOminousBlock) {
                level.playSound(null, getBlockPos(), NTrialsModSounds.BLOCK_TRIAL_SPAWNER_AMBIENT_OMINOUS.get(),
                    SoundSource.BLOCKS, 0.8f, 1.0f);
            } else {
                level.playSound(null, getBlockPos(), NTrialsModSounds.BLOCK_TRIAL_SPAWNER_AMBIENT.get(),
                    SoundSource.BLOCKS, 0.6f, 1.0f);
            }
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

        // Spawn random ominous effects during active waves
        if (!level.isClientSide() && isOminous() && trialActive && this.tickCount % 300 == 0) {
            spawnRandomOminousEffect();
        }
    }

    public void clientTick() {
        this.clientTickCount++;

        if (level != null && level.isClientSide) {
            BlockState state = level.getBlockState(worldPosition);
            if (state.getBlock() instanceof cz.maxtechnik.ntrials.block.TrialSpawnerBlock spawnerBlock) {
                boolean ominous = state.getValue(cz.maxtechnik.ntrials.block.TrialSpawnerBlock.OMINOUS);
                cz.maxtechnik.ntrials.block.TrialSpawnerBlock.TrialSpawnerState spawnerState = state.getValue(cz.maxtechnik.ntrials.block.TrialSpawnerBlock.STATE);

                // Detect ominous activation and spawn particles
                if (ominous && !wasOminous) {
                    // Spawn ominous activation particles within 3-block range
                    BlockPos pos = worldPosition;
                    double radius = 3.0;
                    int numParticles = 50;
                    for (int i = 0; i < numParticles; i++) {
                        double x = pos.getX() + (level.random.nextDouble() - 0.5) * 2 * radius;
                        double y = pos.getY() + level.random.nextDouble() * radius;
                        double z = pos.getZ() + (level.random.nextDouble() - 0.5) * 2 * radius;
                        
                        // Ensure within radius
                        while (Math.sqrt(Math.pow(x - pos.getX(), 2) + Math.pow(y - pos.getY(), 2) + Math.pow(z - pos.getZ(), 2)) > radius) {
                            x = pos.getX() + (level.random.nextDouble() - 0.5) * 2 * radius;
                            y = pos.getY() + level.random.nextDouble() * radius;
                            z = pos.getZ() + (level.random.nextDouble() - 0.5) * 2 * radius;
                        }
                        
                        double vx = (level.random.nextDouble() - 0.5) * 0.1;
                        double vy = level.random.nextDouble() * 0.2;
                        double vz = (level.random.nextDouble() - 0.5) * 0.1;
                        level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, vx, vy, vz);
                    }
                    wasOminous = true;
                } else if (!ominous) {
                    wasOminous = false;
                }

                // Ambient particles
                if (spawnerState == cz.maxtechnik.ntrials.block.TrialSpawnerBlock.TrialSpawnerState.WAITING_FOR_PLAYERS ||
                    spawnerState == cz.maxtechnik.ntrials.block.TrialSpawnerBlock.TrialSpawnerState.ACTIVE) {
                    if (clientTickCount % 5 == 0) {
                        ParticleOptions particle = ominous ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME;
                        double x = worldPosition.getX() + 0.3 + level.random.nextDouble() * 0.4;
                        double y = worldPosition.getY() + 0.5 + level.random.nextDouble() * 0.4;
                        double z = worldPosition.getZ() + 0.3 + level.random.nextDouble() * 0.4;
                        level.addParticle(particle, x, y, z, 0.0, 0.05, 0.0);
                    }
                }
            }
        }
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

        // Load loot tables
        if (tag.contains("NormalLootTable")) {
            this.normalLootTable = tag.getString("NormalLootTable");
        }
        if (tag.contains("OminousLootTable")) {
            this.ominousLootTable = tag.getString("OminousLootTable");
        }

        // Load loot animation data
        this.isLootAnimating = tag.getBoolean("IsLootAnimating");
        this.lootAnimationTick = tag.getInt("LootAnimationTick");
        this.currentLootDropIndex = tag.getInt("CurrentLootDropIndex");

        // Load pending loot items
        if (tag.contains("PendingLootItems")) {
            this.pendingLootItems.clear();
            ListTag lootItemsTag = tag.getList("PendingLootItems", 10); // 10 = CompoundTag
            for (int i = 0; i < lootItemsTag.size(); i++) {
                CompoundTag itemTag = lootItemsTag.getCompound(i);
                ItemStack item = ItemStack.of(itemTag);
                this.pendingLootItems.add(item);
            }
        }

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

        // Save loot tables
        tag.putString("NormalLootTable", this.normalLootTable);
        tag.putString("OminousLootTable", this.ominousLootTable);

        // Save loot animation data
        tag.putBoolean("IsLootAnimating", this.isLootAnimating);
        tag.putInt("LootAnimationTick", this.lootAnimationTick);
        tag.putInt("CurrentLootDropIndex", this.currentLootDropIndex);

        // Save pending loot items
        ListTag lootItemsTag = new ListTag();
        for (ItemStack item : this.pendingLootItems) {
            CompoundTag itemTag = new CompoundTag();
            item.save(itemTag);
            lootItemsTag.add(itemTag);
        }
        tag.put("PendingLootItems", lootItemsTag);

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

    public void setNormalLootTable(String lootTable) {
        this.normalLootTable = lootTable;
        setChanged();
    }

    public void setOminousLootTable(String lootTable) {
        this.ominousLootTable = lootTable;
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


        // Check for players in range
        double range = 14.0;
        net.minecraft.world.phys.AABB searchArea = new net.minecraft.world.phys.AABB(
                getBlockPos().getX() - range, getBlockPos().getY() - range, getBlockPos().getZ() - range,
                getBlockPos().getX() + range, getBlockPos().getY() + range, getBlockPos().getZ() + range
        );

        List<net.minecraft.world.entity.player.Player> players = level.getEntitiesOfClass(
                net.minecraft.world.entity.player.Player.class, searchArea);

        // FOR TESTING IN SINGLE PLAYER - Include entities with "player" tag as fake players
        // Comment out or remove this section when not needed for testing
		/*
        List<net.minecraft.world.entity.Entity> entitiesWithPlayerTag = level.getEntitiesOfClass(
                net.minecraft.world.entity.Entity.class, searchArea,
                entity -> entity.getTags().contains("player"));
        int fakePlayerCount = entitiesWithPlayerTag.size();
		*/
        int fakePlayerCount = 0; // Set to 0 when not testing, or uncomment above section for testing

        int playerCount = players.size() + fakePlayerCount;

        // Check if any player has Bad Omen effect and activate ominous mode
        boolean hasSurvivalPlayer = false;
        boolean hasPlayerWithBadOmen = false;
        for (net.minecraft.world.entity.player.Player player : players) {
            if (player instanceof ServerPlayer serverPlayer && serverPlayer.gameMode.getGameModeForPlayer() == GameType.SURVIVAL) {
                hasSurvivalPlayer = true;
                if (player.hasEffect(MobEffects.BAD_OMEN)) {
                    player.removeEffect(MobEffects.BAD_OMEN);
                    MobEffectInstance trialOmenEffect = new MobEffectInstance(NTrialsModMobEffects.TRIAL_OMEN.get(),36000,0);
                    player.addEffect(trialOmenEffect);
                }

                if (player.hasEffect(NTrialsModMobEffects.TRIAL_OMEN.get())) {
                    hasPlayerWithBadOmen = true;
                }
            }
        }

        // Set ominous state on the block if any player had Bad Omen
		BlockState currentState_now = level.getBlockState(getBlockPos());
        if (hasPlayerWithBadOmen && !currentState_now.getValue(cz.maxtechnik.ntrials.block.TrialSpawnerBlock.OMINOUS)) {
            BlockState currentState = level.getBlockState(getBlockPos());
            if (!currentState.getValue(cz.maxtechnik.ntrials.block.TrialSpawnerBlock.OMINOUS)) {
                level.setBlock(getBlockPos(),
                    currentState.setValue(cz.maxtechnik.ntrials.block.TrialSpawnerBlock.OMINOUS, true), 3);
                this.setOminous(true);
                System.out.println("Trial Spawner at " + getBlockPos() + " switched to ominous mode due to Bad Omen");

                // Play ominous activation sound
                level.playSound(null, getBlockPos(), NTrialsModSounds.BLOCK_TRIAL_SPAWNER_OMINOUS_ACTIVATE.get(),
                    SoundSource.BLOCKS, 1.0f, 1.0f);

                // Ominous particles now handled client-side in clientTick()
            }

            // If spawner is on cooldown but player has Bad Omen, cancel cooldown and start trial immediately
            if (this.cooldownTime > 0) {
                this.cooldownTime = 0;
                System.out.println("Bad Omen overrides cooldown - cancelling cooldown and starting ominous trial immediately");
            }
        }

		if (this.cooldownTime > 0) {
			return;
		}

        boolean hasPlayers = playerCount > 0;

        // Detect player sound when first entering range
        if (hasSurvivalPlayer && !trialActive && currentWave == 0) {
            // Play detect player sound
            level.playSound(null, getBlockPos(), NTrialsModSounds.BLOCK_TRIAL_SPAWNER_DETECT_PLAYER.get(),
                SoundSource.BLOCKS, 1.0f, 1.0f);
            // Start trial
            startTrial();
        } else if (!hasPlayers && trialActive && spawnedEntities.isEmpty()) {
            // Stop trial only if no players AND no spawned entities remain
            stopTrial();
        } else if (trialActive && hasPlayers) {
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

        if (this.isLootAnimating) {
			newState = cz.maxtechnik.ntrials.block.TrialSpawnerBlock.TrialSpawnerState.EJECTING_REWARD;
        } else if (this.cooldownTime > 0) {
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
        spawnedEntities.clear();

        // Scan for players in 16 block radius to scale mob count
        updateMobCountBasedOnPlayers();

        spawnWave();
    }

    private void updateMobCountBasedOnPlayers() {
        if (level == null || level.isClientSide()) {
            return;
        }

        // Scan 16 blocks around spawner for players
        double scanRange = 16.0;
        net.minecraft.world.phys.AABB scanArea = new net.minecraft.world.phys.AABB(
                getBlockPos().getX() - scanRange, getBlockPos().getY() - scanRange, getBlockPos().getZ() - scanRange,
                getBlockPos().getX() + scanRange, getBlockPos().getY() + scanRange, getBlockPos().getZ() + scanRange
        );

        List<net.minecraft.world.entity.player.Player> playersInRange = level.getEntitiesOfClass(
                net.minecraft.world.entity.player.Player.class, scanArea);

        // FOR TESTING IN SINGLE PLAYER - Include entities with "player" tag as fake players
        // Comment out or remove this section when not needed for testing
        /*
        List<net.minecraft.world.entity.Entity> entitiesWithPlayerTag = level.getEntitiesOfClass(
                net.minecraft.world.entity.Entity.class, scanArea,
                entity -> entity.getTags().contains("player"));
        int fakePlayerCount = entitiesWithPlayerTag.size();
        */
        int fakePlayerCount = 0; // Set to 0 when not testing, or uncomment above section for testing

        int playerCount = playersInRange.size() + fakePlayerCount;

        // Calculate scaled mob count (default 3 mobs per wave * player count)
        int baseMobsPerWave = 2; // Default value
        int scaledMobsPerWave = baseMobsPerWave + playerCount;
  int maxWavesCount = maxWaves + playerCount;
        // Update mobs per wave for this trial
        this.currentTrialMobsPerWave = scaledMobsPerWave;
		this.maxWavesCount = maxWavesCount;
		this.playersCount = playerCount;

        System.out.println("Found " + playerCount + " players in 16 block radius. Scaling mobs per wave from " +
                          baseMobsPerWave + " to " + scaledMobsPerWave);
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

        // Check if block state is ominous
        BlockState currentState = level.getBlockState(getBlockPos());
        boolean isOminousBlock = currentState.getValue(cz.maxtechnik.ntrials.block.TrialSpawnerBlock.OMINOUS);

        System.out.println("Spawning wave " + currentWave + "/" + maxWavesCount + " with " + currentTrialMobsPerWave + " mobs at " + getBlockPos() + " (Ominous: " + isOminousBlock + ")");

        // Play spawn sound at the beginning of wave
        level.playSound(null, getBlockPos(), NTrialsModSounds.BLOCK_TRIAL_SPAWNER_SPAWN.get(),
            SoundSource.BLOCKS, 1.0f, 1.0f);

        for (int i = 0; i < currentTrialMobsPerWave; i++) {
            // Find spawn position around the spawner
            BlockPos spawnPos = findSpawnPosition();
            if (spawnPos != null) {
                net.minecraft.world.entity.Entity entity = spawnEntity.create(level);
                if (entity != null) {
                    entity.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);

                    // Make sure it's a mob and set it up properly
                    if (entity instanceof net.minecraft.world.entity.Mob mob) {
                        // If block state ominous is true, give random equipment and make stronger
                        if (isOminousBlock) {
                            equipOminousMob(mob);
                        }
                        mob.finalizeSpawn((net.minecraft.server.level.ServerLevel) level,
                                level.getCurrentDifficultyAt(spawnPos),
                                net.minecraft.world.entity.MobSpawnType.SPAWNER,
                                null, null);
                    }

                    level.addFreshEntity(entity);
                    spawnedEntities.add(entity.getUUID());

                    spawnSpawnParticles(getBlockPos(), isOminousBlock);

                    System.out.println("Spawned " + entity.getType().getDescriptionId() + " at " + spawnPos +
                        (isOminousBlock ? " [OMINOUS]" : ""));
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
        // Remove dead entities and count alive
        spawnedEntities.removeIf(uuid -> {
            net.minecraft.world.entity.Entity entity = ((net.minecraft.server.level.ServerLevel) level).getEntity(uuid);
            return entity == null || !entity.isAlive();
        });
        int alive = spawnedEntities.size();
        if (alive == 0) {
            if (currentWave >= maxWavesCount) {
                completeTrial();
            }
            return;
        }
        int target = currentTrialMobsPerWave;
        int needed = target - alive;
        if (needed > 0) {
            int projectedWave = currentWave + 1;
            BlockState currentState = level.getBlockState(getBlockPos());
            boolean isOminousBlock = currentState.getValue(cz.maxtechnik.ntrials.block.TrialSpawnerBlock.OMINOUS);
            if (projectedWave <= maxWavesCount) {
                // Safe to refill, increment wave by 1 for this batch
                currentWave = projectedWave;
                // Play spawn sound for refill batch
                level.playSound(null, getBlockPos(), NTrialsModSounds.BLOCK_TRIAL_SPAWNER_SPAWN.get(),
                    SoundSource.BLOCKS, 1.0f, 1.0f);
                for (int i = 0; i < needed; i++) {
                    BlockPos spawnPos = findSpawnPosition();
                    if (spawnPos != null) {
                        net.minecraft.world.entity.Entity entity = spawnEntity.create(level);
                        if (entity != null) {
                            entity.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
                            if (entity instanceof net.minecraft.world.entity.Mob mob) {
                                if (isOminousBlock) {
                                    equipOminousMob(mob);
                                }
                                mob.finalizeSpawn((net.minecraft.server.level.ServerLevel) level,
                                    level.getCurrentDifficultyAt(spawnPos),
                                    net.minecraft.world.entity.MobSpawnType.SPAWNER,
                                    null, null);
                            }
                            level.addFreshEntity(entity);
                            spawnedEntities.add(entity.getUUID());

                            spawnSpawnParticles(getBlockPos(), isOminousBlock);

                            System.out.println("Refilled " + entity.getType().getDescriptionId() + " at " + spawnPos +
                                (isOminousBlock ? " [OMINOUS]" : ""));
                        }
                    }
                }
                System.out.println("Refilled " + needed + " mobs (batch). Current wave: " + currentWave + "/" + maxWavesCount);
            } else {
                // Final phase: no refill, no wave increment
                System.out.println("Final phase: " + needed + " mobs killed, no refill. Wave remains " + currentWave + "/" + maxWavesCount);
            }
        }
    }

    private void completeTrial() {
        System.out.println("Trial completed at " + getBlockPos());
        trialActive = false;
        currentWave = 0;
        currentWaveMobs = 0;
        spawnedEntities.clear();

        // Generate and drop loot
        generateLootReward();

        // Set cooldown
        setCooldownTime(36000);
    }

    private void generateLootReward() {
        if (level == null || level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        // Determine which loot table to use based on ominous state
        BlockState currentState = level.getBlockState(getBlockPos());
        boolean isOminousBlock = currentState.getValue(cz.maxtechnik.ntrials.block.TrialSpawnerBlock.OMINOUS);

        String lootTableId = isOminousBlock ? ominousLootTable : normalLootTable;
        ResourceLocation lootTableLocation = new ResourceLocation(lootTableId);

        LootTable lootTable = serverLevel.getServer().getLootData().getLootTable(lootTableLocation);

        if (lootTable == LootTable.EMPTY) {
            System.out.println("Warning: Loot table " + lootTableId + " not found, using default");
            lootTableLocation = new ResourceLocation("minecraft:chests/desert_pyramid");
            lootTable = serverLevel.getServer().getLootData().getLootTable(lootTableLocation);
        }

        // Create loot context
        LootParams.Builder lootParamsBuilder = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.ORIGIN, getBlockPos().getCenter());

        LootParams lootParams = lootParamsBuilder.create(LootContextParamSets.CHEST);

        // Generate all loot items based on player count
        int lootMultiplier = Math.max(1, playersCount);
        List<ItemStack> allLootItems = new ArrayList<>();

        System.out.println("Generating loot from table " + lootTableId + " with multiplier " + lootMultiplier +
                          " (Ominous: " + isOminousBlock + ")");

        for (int i = 0; i < lootMultiplier; i++) {
            List<ItemStack> lootItems = lootTable.getRandomItems(lootParams);
            for (ItemStack item : lootItems) {
                if (!item.isEmpty()) {
                    allLootItems.add(item.copy());
                }
            }
        }

        // Start animated loot drop instead of dropping all at once
        if (!allLootItems.isEmpty()) {
            startLootAnimation(allLootItems);
        }
    }

    // Loot animation methods
    private void startLootAnimation(List<ItemStack> lootItems) {
        this.isLootAnimating = true;
        this.lootAnimationTick = 0;
        this.currentLootDropIndex = 0;
        this.pendingLootItems = new ArrayList<>(lootItems);
        setChanged();

        // Play open shutter sound when starting loot animation
        level.playSound(null, getBlockPos(), NTrialsModSounds.BLOCK_TRIAL_SPAWNER_OPEN_SHUTTER.get(),
            SoundSource.BLOCKS, 1.0f, 1.0f);

        // Play spawn item begin sound to indicate loot is about to drop
        level.playSound(null, getBlockPos(), NTrialsModSounds.BLOCK_TRIAL_SPAWNER_SPAWN_ITEM_BEGIN.get(),
            SoundSource.BLOCKS, 0.8f, 1.0f);

        // Sync animation start to clients
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }

        System.out.println("Started loot animation with " + lootItems.size() + " items");
    }

    private void tickLootAnimation() {
        if (!isLootAnimating || pendingLootItems.isEmpty()) {
            stopLootAnimation();
            return;
        }

        lootAnimationTick++;

        // Drop next item every LOOT_DROP_INTERVAL ticks
        if (lootAnimationTick % LOOT_DROP_INTERVAL == 0 && currentLootDropIndex < pendingLootItems.size()) {
            dropNextLootItem();
        }

        // Stop animation when all items are dropped
        if (currentLootDropIndex >= pendingLootItems.size()) {
            stopLootAnimation();
        }
    }

    private void dropNextLootItem() {
        if (level == null || level.isClientSide() || currentLootDropIndex >= pendingLootItems.size()) {
            return;
        }

        ItemStack itemToDrop = pendingLootItems.get(currentLootDropIndex);

        // Play eject item sound for each item drop
        level.playSound(null, getBlockPos(), NTrialsModSounds.BLOCK_TRIAL_SPAWNER_EJECT_ITEM.get(),
            SoundSource.BLOCKS, 0.7f, 1.0f + (level.random.nextFloat() - 0.5f) * 0.4f); // Random pitch variation

        // Add some randomness to drop position for visual effect
        double offsetX = (level.random.nextDouble() - 0.5) * 0.8; // -0.4 to +0.4
        double offsetZ = (level.random.nextDouble() - 0.5) * 0.8; // -0.4 to +0.4
        double offsetY = 0.2 + level.random.nextDouble() * 0.3; // 0.2 to 0.5

        // Drop item with slight upward velocity for visual effect
        net.minecraft.world.entity.item.ItemEntity itemEntity = new net.minecraft.world.entity.item.ItemEntity(
                level,
                getBlockPos().getX() + 0.5 + offsetX,
                getBlockPos().getY() + 1.0 + offsetY,
                getBlockPos().getZ() + 0.5 + offsetZ,
                itemToDrop.copy()
        );

        // Add upward motion
        itemEntity.setDeltaMovement(0.0, 0.15, 0.0);

        level.addFreshEntity(itemEntity);

        currentLootDropIndex++;
        setChanged();

        System.out.println("Dropped loot item " + currentLootDropIndex + "/" + pendingLootItems.size() +
                          ": " + itemToDrop.getDisplayName().getString());
    }

    private void stopLootAnimation() {
        this.isLootAnimating = false;
        this.lootAnimationTick = 0;
        this.currentLootDropIndex = 0;
        this.pendingLootItems.clear();
        setChanged();

        // Play close shutter sound when loot animation ends
        level.playSound(null, getBlockPos(), NTrialsModSounds.BLOCK_TRIAL_SPAWNER_CLOSE_SHUTTER.get(),
            SoundSource.BLOCKS, 1.0f, 1.0f);

        // Sync animation end to clients
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }

        // After loot animation completes, set block to appropriate state
        // If we have cooldown time, it will go to COOLDOWN state
        // Otherwise it will go to appropriate state based on current conditions
        updateBlockState();

        System.out.println("Stopped loot animation");
    }

    // Getters for animation state (for client rendering)
    public boolean isLootAnimating() {
        return isLootAnimating;
    }

    public int getLootAnimationTick() {
        return lootAnimationTick;
    }

    public int getCurrentLootDropIndex() {
        return currentLootDropIndex;
    }

    public int getTotalLootItems() {
        return pendingLootItems.size();
    }

    // Synchronization methods for client-server communication
    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        this.saveAdditional(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        this.load(tag);
    }

    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
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

    private void equipOminousMob(net.minecraft.world.entity.Mob mob) {
        if (level == null || level.random == null) {
            return;
        }

        // Make mob stronger
        // Increase health by 50%
        float currentHealth = mob.getMaxHealth();
        mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH)
            .setBaseValue(currentHealth * 1.5f);
        mob.setHealth(mob.getMaxHealth());

        // Increase attack damage by 25%
        if (mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE) != null) {
            double currentDamage = mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE).getBaseValue();
            mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)
                .setBaseValue(currentDamage * 1.25);
        }

        // Random armor pieces and weapons
        net.minecraft.util.RandomSource random = level.random;

        String[] trimPatterns = {"coast", "dune", "eye", "host", "raiser", "rib", "sentry", "shaper", "silence", "snout", "spire", "tide", "ward", "wayfinder", "wild"};

        // Armor materials (different tiers)
        net.minecraft.world.item.Item[] helmets = {

            net.minecraft.world.item.Items.IRON_HELMET,
            net.minecraft.world.item.Items.GOLDEN_HELMET,
            net.minecraft.world.item.Items.DIAMOND_HELMET

        };

        net.minecraft.world.item.Item[] chestplates = {

            net.minecraft.world.item.Items.IRON_CHESTPLATE,
            net.minecraft.world.item.Items.GOLDEN_CHESTPLATE,
            net.minecraft.world.item.Items.DIAMOND_CHESTPLATE

        };

        net.minecraft.world.item.Item[] leggings = {

            net.minecraft.world.item.Items.IRON_LEGGINGS,
            net.minecraft.world.item.Items.GOLDEN_LEGGINGS,
            net.minecraft.world.item.Items.DIAMOND_LEGGINGS

        };

        net.minecraft.world.item.Item[] boots = {

            net.minecraft.world.item.Items.IRON_BOOTS,
            net.minecraft.world.item.Items.GOLDEN_BOOTS,
            net.minecraft.world.item.Items.DIAMOND_BOOTS

        };

        net.minecraft.world.item.Item[] weapons = {
            net.minecraft.world.item.Items.STONE_SWORD,
            net.minecraft.world.item.Items.IRON_SWORD,
            net.minecraft.world.item.Items.DIAMOND_SWORD,
            net.minecraft.world.item.Items.IRON_AXE,
            net.minecraft.world.item.Items.DIAMOND_AXE
        };

        // Randomly equip armor pieces (30% chance for each piece)
        if (random.nextFloat() < 0.3f) {
            net.minecraft.world.item.Item chosenHelmet = helmets[random.nextInt(helmets.length)];
            net.minecraft.world.item.ItemStack helmet = new net.minecraft.world.item.ItemStack(chosenHelmet);
            // Apply trim
            String pattern = trimPatterns[random.nextInt(trimPatterns.length)];
            String material;
            if (chosenHelmet == net.minecraft.world.item.Items.IRON_HELMET) {
                material = "trim_material:iron";
            } else if (chosenHelmet == net.minecraft.world.item.Items.GOLDEN_HELMET) {
                material = "trim_material:gold";
            } else {
                material = "trim_material:diamond";
            }
            net.minecraft.nbt.CompoundTag trimTag = new net.minecraft.nbt.CompoundTag();
            trimTag.putString("pattern", "trim_pattern:" + pattern);
            trimTag.putString("material", material);
            helmet.getOrCreateTag().put("Trim", trimTag);
            mob.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, helmet);
            mob.setDropChance(net.minecraft.world.entity.EquipmentSlot.HEAD, 0.0f);
        }

        if (random.nextFloat() < 0.3f) {
            net.minecraft.world.item.Item chosenChestplate = chestplates[random.nextInt(chestplates.length)];
            net.minecraft.world.item.ItemStack chestplate = new net.minecraft.world.item.ItemStack(chosenChestplate);
            // Apply trim
            String pattern = trimPatterns[random.nextInt(trimPatterns.length)];
            String material;
            if (chosenChestplate == net.minecraft.world.item.Items.IRON_CHESTPLATE) {
                material = "trim_material:iron";
            } else if (chosenChestplate == net.minecraft.world.item.Items.GOLDEN_CHESTPLATE) {
                material = "trim_material:gold";
            } else {
                material = "trim_material:diamond";
            }
            net.minecraft.nbt.CompoundTag trimTag = new net.minecraft.nbt.CompoundTag();
            trimTag.putString("pattern", "trim_pattern:" + pattern);
            trimTag.putString("material", material);
            chestplate.getOrCreateTag().put("Trim", trimTag);
            mob.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, chestplate);
            mob.setDropChance(net.minecraft.world.entity.EquipmentSlot.CHEST, 0.0f);
        }

        if (random.nextFloat() < 0.3f) {
            net.minecraft.world.item.Item chosenLeggings = leggings[random.nextInt(leggings.length)];
            net.minecraft.world.item.ItemStack legging = new net.minecraft.world.item.ItemStack(chosenLeggings);
            // Apply trim
            String pattern = trimPatterns[random.nextInt(trimPatterns.length)];
            String material;
            if (chosenLeggings == net.minecraft.world.item.Items.IRON_LEGGINGS) {
                material = "trim_material:iron";
            } else if (chosenLeggings == net.minecraft.world.item.Items.GOLDEN_LEGGINGS) {
                material = "trim_material:gold";
            } else {
                material = "trim_material:diamond";
            }
            net.minecraft.nbt.CompoundTag trimTag = new net.minecraft.nbt.CompoundTag();
            trimTag.putString("pattern", "trim_pattern:" + pattern);
            trimTag.putString("material", material);
            legging.getOrCreateTag().put("Trim", trimTag);
            mob.setItemSlot(net.minecraft.world.entity.EquipmentSlot.LEGS, legging);
            mob.setDropChance(net.minecraft.world.entity.EquipmentSlot.LEGS, 0.0f);
        }

        if (random.nextFloat() < 0.3f) {
            net.minecraft.world.item.Item chosenBoots = boots[random.nextInt(boots.length)];
            net.minecraft.world.item.ItemStack boot = new net.minecraft.world.item.ItemStack(chosenBoots);
            // Apply trim
            String pattern = trimPatterns[random.nextInt(trimPatterns.length)];
            String material;
            if (chosenBoots == net.minecraft.world.item.Items.IRON_BOOTS) {
                material = "trim_material:iron";
            } else if (chosenBoots == net.minecraft.world.item.Items.GOLDEN_BOOTS) {
                material = "trim_material:gold";
            } else {
                material = "trim_material:diamond";
            }
            net.minecraft.nbt.CompoundTag trimTag = new net.minecraft.nbt.CompoundTag();
            trimTag.putString("pattern", "trim_pattern:" + pattern);
            trimTag.putString("material", material);
            boot.getOrCreateTag().put("Trim", trimTag);
            mob.setItemSlot(net.minecraft.world.entity.EquipmentSlot.FEET, boot);
            mob.setDropChance(net.minecraft.world.entity.EquipmentSlot.FEET, 0.0f);
        }

        // Randomly give weapons (50% chance)
        if (random.nextFloat() < 0.5f) {
            net.minecraft.world.item.ItemStack weapon = new net.minecraft.world.item.ItemStack(
                weapons[random.nextInt(weapons.length)]);
            mob.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, weapon);
            mob.setDropChance(net.minecraft.world.entity.EquipmentSlot.MAINHAND, 0.0f);
        }

        // Add some enchantments randomly
        addRandomEnchantments(mob);

        System.out.println("Equipped ominous mob with random gear");
    }

    private void addRandomEnchantments(net.minecraft.world.entity.Mob mob) {
        if (level == null || level.random == null) {
            return;
        }

        net.minecraft.util.RandomSource random = level.random;

        // 40% chance to add enchantments
        if (random.nextFloat() < 0.4f) {
            net.minecraft.world.entity.EquipmentSlot[] slots = {
                net.minecraft.world.entity.EquipmentSlot.HEAD,
                net.minecraft.world.entity.EquipmentSlot.CHEST,
                net.minecraft.world.entity.EquipmentSlot.LEGS,
                net.minecraft.world.entity.EquipmentSlot.FEET,
                net.minecraft.world.entity.EquipmentSlot.MAINHAND
            };

            for (net.minecraft.world.entity.EquipmentSlot slot : slots) {
                net.minecraft.world.item.ItemStack item = mob.getItemBySlot(slot);
                if (!item.isEmpty() && random.nextFloat() < 0.3f) {
                    if (slot == net.minecraft.world.entity.EquipmentSlot.MAINHAND) {
                        // Weapon enchantments
                        if (random.nextBoolean()) {
                            item.enchant(net.minecraft.world.item.enchantment.Enchantments.SHARPNESS,
                                random.nextInt(3) + 1);
                        }
                        if (random.nextBoolean()) {
                            item.enchant(net.minecraft.world.item.enchantment.Enchantments.KNOCKBACK,
                                random.nextInt(2) + 1);
                        }
                    } else {
                        // Armor enchantments
                        if (random.nextBoolean()) {
                            item.enchant(net.minecraft.world.item.enchantment.Enchantments.ALL_DAMAGE_PROTECTION,
                                random.nextInt(3) + 1);
                        }
                        if (random.nextBoolean()) {
                            item.enchant(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING,
                                random.nextInt(2) + 1);
                        }
                    }
                    mob.setItemSlot(slot, item);
                }
            }
        }
    }

    private void spawnSpawnParticles(BlockPos center, boolean isOminous) {
        if (level == null) return;

        double radius = 2.0;
        int numParticles = 20;
        ParticleOptions particle = isOminous ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME;

        for (int i = 0; i < numParticles; i++) {
            double x = center.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 2 * radius;
            double y = center.getY() + 0.5 + (level.random.nextDouble() - 0.5) * 2 * radius;
            double z = center.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 2 * radius;
            
            // Ensure within radius
            double cx = center.getX() + 0.5;
            double cy = center.getY() + 0.5;
            double cz = center.getZ() + 0.5;
            while (Math.sqrt(Math.pow(x - cx, 2) + Math.pow(y - cy, 2) + Math.pow(z - cz, 2)) > radius) {
                x = center.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 2 * radius;
                y = center.getY() + 0.5 + (level.random.nextDouble() - 0.5) * 2 * radius;
                z = center.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 2 * radius;
            }
            
            double vx = (level.random.nextDouble() - 0.5) * 0.05;
            double vy = level.random.nextDouble() * 0.1;
            double vz = (level.random.nextDouble() - 0.5) * 0.05;
            level.addParticle(particle, x, y, z, vx, vy, vz);
        }
    }

    private void spawnRandomOminousEffect() {
        if (level == null || OMNIOUS_EFFECTS.isEmpty()) return;

        MobEffect selectedEffect = OMNIOUS_EFFECTS.get(level.random.nextInt(OMNIOUS_EFFECTS.size()));
        BlockPos effectPos = findEffectPosition();
        if (effectPos == null) return;

        AreaEffectCloud cloud = new AreaEffectCloud(EntityType.AREA_EFFECT_CLOUD, level);
        cloud.setPos(effectPos.getX() + 0.5, effectPos.getY() + 0.5, effectPos.getZ() + 0.5);
        cloud.setRadius(3.0f);
        cloud.setDuration(200); // 10 seconds
        cloud.addEffect(new MobEffectInstance(selectedEffect, 200, 0)); // Apply for 10 seconds, amplifier 0
        cloud.setParticle(ParticleTypes.ENTITY_EFFECT);

        level.addFreshEntity(cloud);
        System.out.println("Spawned ominous area effect: " + ForgeRegistries.MOB_EFFECTS.getKey(selectedEffect) + " at " + effectPos);
    }

    private BlockPos findEffectPosition() {
        double radius = 10.0;
        for (int attempts = 0; attempts < 20; attempts++) {
            double dx = (level.random.nextDouble() - 0.5) * 2 * radius;
            double dz = (level.random.nextDouble() - 0.5) * 2 * radius;
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist > radius) continue; // Ensure within horizontal radius

            int x = getBlockPos().getX() + (int) dx;
            int z = getBlockPos().getZ() + (int) dz;
            int y = getBlockPos().getY();

            // Check a few blocks up and down for space
            for (int yOffset = -2; yOffset <= 3; yOffset++) {
                BlockPos pos = new BlockPos(x, y + yOffset, z);
                if (level.getBlockState(pos).isAir() &&
                    level.getBlockState(pos.above()).isAir() &&
                    level.getBlockState(pos.below()).isSolid()) { // Solid below
                    return pos;
                }
            }
        }
        return null; // No valid position found
    }
}
