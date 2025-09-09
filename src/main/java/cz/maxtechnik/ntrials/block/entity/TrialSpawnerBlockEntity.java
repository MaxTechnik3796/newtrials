package cz.maxtechnik.ntrials.block.entity;

import cz.maxtechnik.ntrials.init.NTrialsModBlockEntities;
import cz.maxtechnik.ntrials.network.NetworkHandler;
import cz.maxtechnik.ntrials.network.TrialSpawnerSyncPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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
        boolean hasPlayerWithBadOmen = false;
        for (net.minecraft.world.entity.player.Player player : players) {
            if (player.hasEffect(net.minecraft.world.effect.MobEffects.BAD_OMEN)) {
                hasPlayerWithBadOmen = true;
                // Remove Bad Omen effect from player when activating ominous mode
                player.removeEffect(net.minecraft.world.effect.MobEffects.BAD_OMEN);
                System.out.println("Player " + player.getName().getString() + " had Bad Omen - activating ominous mode");
            }
        }

        // Set ominous state on the block if any player had Bad Omen
        if (hasPlayerWithBadOmen) {
            BlockState currentState = level.getBlockState(getBlockPos());
            if (!currentState.getValue(cz.maxtechnik.ntrials.block.TrialSpawnerBlock.OMINOUS)) {
                level.setBlock(getBlockPos(),
                    currentState.setValue(cz.maxtechnik.ntrials.block.TrialSpawnerBlock.OMINOUS, true), 3);
                System.out.println("Trial Spawner at " + getBlockPos() + " switched to ominous mode due to Bad Omen");
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

        if (hasPlayers && !trialActive && currentWave == 0) {
            // Start trial
            startTrial();
        } else if (!hasPlayers && trialActive && spawnedEntities.isEmpty()) {
            // Stop trial only if no players AND no spawned entities remain
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
        currentWaveMobs = 0;
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
        int scaledMobsPerWave = baseMobsPerWave + (playerCount*2);
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

        System.out.println("Spawning wave " + currentWave + "/" + maxWaves + " with " + currentTrialMobsPerWave + " mobs at " + getBlockPos() + " (Ominous: " + isOminousBlock + ")");

        for (int i = 0; i < currentTrialMobsPerWave; i++) {
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

                        // If block state ominous is true, give random equipment and make stronger
                        if (isOminousBlock) {
                            equipOminousMob(mob);
                        }
                    }

                    level.addFreshEntity(entity);
                    spawnedEntities.add(entity.getUUID());
                    currentWaveMobs++;

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
        // Count how many spawned entities are still alive
        int aliveCount = 0;
        spawnedEntities.removeIf(uuid -> {
            net.minecraft.world.entity.Entity entity = ((net.minecraft.server.level.ServerLevel) level).getEntity(uuid);
            return entity == null || !entity.isAlive();
        });

        aliveCount = spawnedEntities.size();

        if (aliveCount == 0 && currentWaveMobs > 0) {
            // Wave completed
            if (currentWave < maxWavesCount) {
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

        // Armor materials (different tiers)
        net.minecraft.world.item.Item[] helmets = {
            net.minecraft.world.item.Items.LEATHER_HELMET,
            net.minecraft.world.item.Items.CHAINMAIL_HELMET,
            net.minecraft.world.item.Items.IRON_HELMET,
            net.minecraft.world.item.Items.DIAMOND_HELMET,
            net.minecraft.world.item.Items.NETHERITE_HELMET
        };

        net.minecraft.world.item.Item[] chestplates = {
            net.minecraft.world.item.Items.LEATHER_CHESTPLATE,
            net.minecraft.world.item.Items.CHAINMAIL_CHESTPLATE,
            net.minecraft.world.item.Items.IRON_CHESTPLATE,
            net.minecraft.world.item.Items.DIAMOND_CHESTPLATE,
            net.minecraft.world.item.Items.NETHERITE_CHESTPLATE
        };

        net.minecraft.world.item.Item[] leggings = {
            net.minecraft.world.item.Items.LEATHER_LEGGINGS,
            net.minecraft.world.item.Items.CHAINMAIL_LEGGINGS,
            net.minecraft.world.item.Items.IRON_LEGGINGS,
            net.minecraft.world.item.Items.DIAMOND_LEGGINGS,
            net.minecraft.world.item.Items.NETHERITE_LEGGINGS
        };

        net.minecraft.world.item.Item[] boots = {
            net.minecraft.world.item.Items.LEATHER_BOOTS,
            net.minecraft.world.item.Items.CHAINMAIL_BOOTS,
            net.minecraft.world.item.Items.IRON_BOOTS,
            net.minecraft.world.item.Items.DIAMOND_BOOTS,
            net.minecraft.world.item.Items.NETHERITE_BOOTS
        };

        net.minecraft.world.item.Item[] weapons = {
            net.minecraft.world.item.Items.WOODEN_SWORD,
            net.minecraft.world.item.Items.STONE_SWORD,
            net.minecraft.world.item.Items.IRON_SWORD,
            net.minecraft.world.item.Items.DIAMOND_SWORD,
            net.minecraft.world.item.Items.NETHERITE_SWORD,
            net.minecraft.world.item.Items.IRON_AXE,
            net.minecraft.world.item.Items.DIAMOND_AXE
        };

        // Randomly equip armor pieces (30% chance for each piece)
        if (random.nextFloat() < 0.3f) {
            net.minecraft.world.item.ItemStack helmet = new net.minecraft.world.item.ItemStack(
                helmets[random.nextInt(helmets.length)]);
            mob.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, helmet);
            mob.setDropChance(net.minecraft.world.entity.EquipmentSlot.HEAD, 0.1f);
        }

        if (random.nextFloat() < 0.3f) {
            net.minecraft.world.item.ItemStack chestplate = new net.minecraft.world.item.ItemStack(
                chestplates[random.nextInt(chestplates.length)]);
            mob.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, chestplate);
            mob.setDropChance(net.minecraft.world.entity.EquipmentSlot.CHEST, 0.1f);
        }

        if (random.nextFloat() < 0.3f) {
            net.minecraft.world.item.ItemStack legging = new net.minecraft.world.item.ItemStack(
                leggings[random.nextInt(leggings.length)]);
            mob.setItemSlot(net.minecraft.world.entity.EquipmentSlot.LEGS, legging);
            mob.setDropChance(net.minecraft.world.entity.EquipmentSlot.LEGS, 0.1f);
        }

        if (random.nextFloat() < 0.3f) {
            net.minecraft.world.item.ItemStack boot = new net.minecraft.world.item.ItemStack(
                boots[random.nextInt(boots.length)]);
            mob.setItemSlot(net.minecraft.world.entity.EquipmentSlot.FEET, boot);
            mob.setDropChance(net.minecraft.world.entity.EquipmentSlot.FEET, 0.1f);
        }

        // Randomly give weapons (50% chance)
        if (random.nextFloat() < 0.5f) {
            net.minecraft.world.item.ItemStack weapon = new net.minecraft.world.item.ItemStack(
                weapons[random.nextInt(weapons.length)]);
            mob.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, weapon);
            mob.setDropChance(net.minecraft.world.entity.EquipmentSlot.MAINHAND, 0.15f);
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
}
