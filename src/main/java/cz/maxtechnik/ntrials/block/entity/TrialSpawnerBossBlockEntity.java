package cz.maxtechnik.ntrials.block.entity;

import cz.maxtechnik.ntrials.block.TrialSpawnerBossBlock;
import cz.maxtechnik.ntrials.block.TrialSpawnerBlock;
import cz.maxtechnik.ntrials.init.NTrialsModBlockEntities;
import cz.maxtechnik.ntrials.init.NTrialsModEntityTypes;
import cz.maxtechnik.ntrials.init.NTrialsModItems;
import cz.maxtechnik.ntrials.init.NTrialsModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TrialSpawnerBossBlockEntity extends BlockEntity {
    private boolean isActivated = false; // Activated by player proximity
    private boolean isKeyActivated = false; // Activated by key click
    private int spawnTimer = -1;
    private static final int SPAWN_DELAY = 60; // 60 ticks = 3 seconds
    private UUID spawnedBossUUID = null;
    private Set<UUID> playersInRange = new HashSet<>();
    private int bossHP = 60; // Base HP for 1 player

    // Loot animation system
    private boolean isLootAnimating = false;
    private int lootAnimationTick = 0;
    private List<ItemStack> pendingLootItems = new ArrayList<>();
    private int currentLootDropIndex = 0;
    private static final int LOOT_DROP_INTERVAL = 10; // Ticks between each item drop (0.5 seconds)

    private int completeTrialTimer = -1; // Timer for delay before loot generation
    private static final int COMPLETE_TRIAL_DELAY = 20; // 20 ticks delay

    // Client-side only
    private int clientTickCount = 0;
    private transient boolean wasActivated = false;

    public TrialSpawnerBossBlockEntity(BlockPos pos, BlockState blockState) {
        super(NTrialsModBlockEntities.TRIAL_SPAWNER_BOSS_BLOCK_ENTITY.get(), pos, blockState);
    }

    public void tick() {
        if (level == null || level.isClientSide) {
            return;
        }

        // Update players in range (20 blocks)
        updatePlayersInRange();

        // Check for nearby players to activate spawner (but don't spawn yet)
        checkAndActivateByProximity();

        // Handle spawn timer (only if key was activated)
        if (isKeyActivated && spawnTimer > 0) {
            spawnTimer--;
            if (spawnTimer == 0) {
                spawnBreezeBoss();
            }
        }

        // Handle completion timer
        if (completeTrialTimer > 0) {
            completeTrialTimer--;
            if (completeTrialTimer == 0) {
                // Timer finished, now generate loot
                generateLootReward();
            }
        }

        // Handle loot animation
        if (isLootAnimating) {
            tickLootAnimation();
        }

        // Check if boss is still alive
        if (isKeyActivated && spawnedBossUUID != null) {
            if (level instanceof ServerLevel serverLevel) {
                net.minecraft.world.entity.Entity boss = serverLevel.getEntity(spawnedBossUUID);
                if (boss == null || !boss.isAlive()) {
                    // Boss was killed
                    handleBossKilled();
                }
            }
        }

        // Update block state
        updateBlockState();
    }

    private void updatePlayersInRange() {
        if (level == null) {
            return;
        }

        double range = 20.0;
        AABB searchArea = new AABB(
                getBlockPos().getX() - range, getBlockPos().getY() - range, getBlockPos().getZ() - range,
                getBlockPos().getX() + range, getBlockPos().getY() + range, getBlockPos().getZ() + range
        );

        List<Player> players = level.getEntitiesOfClass(Player.class, searchArea);
        playersInRange.clear();
        for (Player player : players) {
            playersInRange.add(player.getUUID());
        }
    }

    private void checkAndActivateByProximity() {
        if (level == null || level.isClientSide) {
            return;
        }

        // Check for players in range (8 blocks)
        double range = 8.0;
        AABB searchArea = new AABB(
                getBlockPos().getX() - range, getBlockPos().getY() - range, getBlockPos().getZ() - range,
                getBlockPos().getX() + range, getBlockPos().getY() + range, getBlockPos().getZ() + range
        );

        List<Player> players = level.getEntitiesOfClass(Player.class, searchArea);
        boolean hasPlayers = !players.isEmpty();

        // Activate spawner when player comes near (but don't spawn yet, no sound)
        if (hasPlayers && !isActivated) {
            isActivated = true;
            setChanged();

            // Update block state to active (waiting for key)
            BlockState currentState = level.getBlockState(getBlockPos());
            if (currentState.getBlock() instanceof TrialSpawnerBossBlock) {
                level.setBlock(getBlockPos(),
                        currentState.setValue(TrialSpawnerBossBlock.STATE, TrialSpawnerBlock.TrialSpawnerState.ACTIVE),
                        Block.UPDATE_ALL);
            }
        } else if (!hasPlayers && isActivated && !isKeyActivated) {
            // Deactivate if no players and not key activated
            isActivated = false;
            setChanged();

            BlockState currentState = level.getBlockState(getBlockPos());
            if (currentState.getBlock() instanceof TrialSpawnerBossBlock) {
                level.setBlock(getBlockPos(),
                        currentState.setValue(TrialSpawnerBossBlock.STATE, TrialSpawnerBlock.TrialSpawnerState.INACTIVE),
                        Block.UPDATE_ALL);
            }
        }
    }

    public void activateWithKey(Player player) {
        if (!isKeyActivated && level != null && !level.isClientSide && isActivated) {
            isKeyActivated = true;
            spawnTimer = SPAWN_DELAY;
            setChanged();

            // Update block state to active (already active, but now key activated)
            BlockState currentState = level.getBlockState(getBlockPos());
            if (currentState.getBlock() instanceof TrialSpawnerBossBlock) {
                level.setBlock(getBlockPos(),
                        currentState.setValue(TrialSpawnerBossBlock.STATE, TrialSpawnerBlock.TrialSpawnerState.ACTIVE),
                        Block.UPDATE_ALL);
            }

            // Play activation sound only when key is inserted
            level.playSound(null, getBlockPos(), NTrialsModSounds.BLOCK_TRIAL_SPAWNER_DETECT_PLAYER.get(),
                    SoundSource.BLOCKS, 1.0f, 1.0f);

            // Spawn activation particles server-side (will be synced to clients)
            spawnKeyActivationParticles();
        }
    }

    private void spawnKeyActivationParticles() {
        if (level == null || level.isClientSide) {
            return;
        }

        BlockPos center = getBlockPos();
        double centerX = center.getX() + 0.5;
        double centerY = center.getY() + 0.5;
        double centerZ = center.getZ() + 0.5;
        
        int numParticles = 30;
        
        for (int i = 0; i < numParticles; i++) {
            // Random direction from center
            double angle = level.random.nextDouble() * 2 * Math.PI; // Horizontal angle
            double verticalAngle = (level.random.nextDouble() - 0.5) * Math.PI * 0.5; // Vertical angle (-45 to 45 degrees)
            
            // Calculate direction vector
            double dirX = Math.cos(angle) * Math.cos(verticalAngle);
            double dirY = Math.sin(verticalAngle);
            double dirZ = Math.sin(angle) * Math.cos(verticalAngle);
            
            // Spawn particle at center, moving outward to 1 block distance
            double speed = 0.1 + level.random.nextDouble() * 0.1; // Random speed
            
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME, 
                        centerX, centerY, centerZ, 1, dirX * speed, dirY * speed, dirZ * speed, 0.0);
            }
        }
    }

    private void spawnBreezeBoss() {
        if (level == null || level.isClientSide) {
            return;
        }

        // Calculate HP based on player count
        int playerCount = playersInRange.size();
        bossHP = 60; // Base HP
        for (int i = 1; i < playerCount; i++) {
            bossHP = (int) (bossHP * 1.2); // Multiply by 1.2x for each additional player
        }

        // Find spawn position
        BlockPos spawnPos = findSpawnPosition();
        if (spawnPos != null) {
            cz.maxtechnik.ntrials.entity.BreezeBossEntity boss = new cz.maxtechnik.ntrials.entity.BreezeBossEntity(
                    NTrialsModEntityTypes.BREEZE_BOSS.get(), level);
            boss.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
            
            // Set HP based on player count
            var healthAttribute = boss.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
            if (healthAttribute != null) {
                healthAttribute.setBaseValue(bossHP);
                boss.setHealth(boss.getMaxHealth());
            }

            if (level instanceof ServerLevel serverLevel) {
                @SuppressWarnings({"deprecation", "unused"})
                var ignored = boss.finalizeSpawn(serverLevel,
                        level.getCurrentDifficultyAt(spawnPos),
                        net.minecraft.world.entity.MobSpawnType.SPAWNER,
                        null, null);
            }

            level.addFreshEntity(boss);
            spawnedBossUUID = boss.getUUID();

            // Play spawn sound
            level.playSound(null, getBlockPos(), NTrialsModSounds.BLOCK_TRIAL_SPAWNER_SPAWN.get(),
                    SoundSource.BLOCKS, 1.0f, 1.0f);

            setChanged();
        }
    }

    private BlockPos findSpawnPosition() {
        if (level == null) {
            return null;
        }

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
        @SuppressWarnings("deprecation")
        boolean isSolid = level.getBlockState(pos.below()).isSolid();
        return level.getBlockState(pos).isAir() &&
                level.getBlockState(pos.above()).isAir() &&
                isSolid;
    }

    private void handleBossKilled() {
        if (level == null || level.isClientSide) {
            return;
        }

        // Start completion timer (like regular spawner)
        completeTrialTimer = COMPLETE_TRIAL_DELAY;
        spawnedBossUUID = null;
        setChanged();
    }

    private void generateLootReward() {
        if (level == null || level.isClientSide) {
            return;
        }

        // Create list of boss keys for each player in range (20 blocks)
        List<ItemStack> lootItems = new ArrayList<>();
        for (UUID playerUUID : playersInRange) {
            if (level instanceof ServerLevel serverLevel) {
                Player player = serverLevel.getPlayerByUUID(playerUUID);
                if (player != null) {
                    ItemStack bossKey = new ItemStack(NTrialsModItems.BOSS_TRIAL_KEY.get());
                    lootItems.add(bossKey);
                }
            }
        }

        // Start animated loot drop instead of dropping all at once
        if (!lootItems.isEmpty()) {
            startLootAnimation(lootItems);
        } else {
            // No players in range, just convert spawner
            convertToRegularSpawner();
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
        if (level != null) {
            level.playSound(null, getBlockPos(), NTrialsModSounds.BLOCK_TRIAL_SPAWNER_OPEN_SHUTTER.get(),
                    SoundSource.BLOCKS, 1.0f, 1.0f);

            // Play spawn item begin sound to indicate loot is about to drop
            level.playSound(null, getBlockPos(), NTrialsModSounds.BLOCK_TRIAL_SPAWNER_SPAWN_ITEM_BEGIN.get(),
                    SoundSource.BLOCKS, 0.8f, 1.0f);

            // Sync animation start to clients
            if (!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
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
    }

    private void stopLootAnimation() {
        this.isLootAnimating = false;
        this.lootAnimationTick = 0;
        this.currentLootDropIndex = 0;
        this.pendingLootItems.clear();
        setChanged();

        // Play close shutter sound when loot animation ends
        if (level != null) {
            level.playSound(null, getBlockPos(), NTrialsModSounds.BLOCK_TRIAL_SPAWNER_CLOSE_SHUTTER.get(),
                    SoundSource.BLOCKS, 1.0f, 1.0f);

            // Sync animation end to clients
            if (!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }

        // Convert spawner to regular trial spawner with breeze
        convertToRegularSpawner();
    }

    private void convertToRegularSpawner() {
        if (level == null || level.isClientSide) {
            return;
        }

        BlockPos pos = getBlockPos();
        
        // Replace with regular trial spawner
        BlockState newState = cz.maxtechnik.ntrials.init.NTrialsModBlocks.TRIAL_SPAWNER.get().defaultBlockState()
                .setValue(TrialSpawnerBlock.STATE, TrialSpawnerBlock.TrialSpawnerState.INACTIVE)
                .setValue(TrialSpawnerBlock.OMINOUS, false);
        
        level.setBlock(pos, newState, Block.UPDATE_ALL);

        // Set breeze entity in the new spawner
        BlockEntity newBlockEntity = level.getBlockEntity(pos);
        if (newBlockEntity instanceof TrialSpawnerBlockEntity trialSpawner) {
            trialSpawner.setSpawnEntity(NTrialsModEntityTypes.BREEZE.get());
        }
    }

    private void updateBlockState() {
        if (level == null || level.isClientSide()) {
            return;
        }

        BlockState currentState = level.getBlockState(getBlockPos());
        if (!(currentState.getBlock() instanceof TrialSpawnerBossBlock)) {
            return;
        }

        TrialSpawnerBlock.TrialSpawnerState newState;

        if (this.completeTrialTimer > 0 || this.isLootAnimating || (this.pendingLootItems != null && !this.pendingLootItems.isEmpty())) {
            newState = TrialSpawnerBlock.TrialSpawnerState.EJECTING_REWARD;
        } else if (this.isKeyActivated && spawnedBossUUID != null) {
            newState = TrialSpawnerBlock.TrialSpawnerState.ACTIVE;
        } else if (this.isActivated || this.isKeyActivated) {
            newState = TrialSpawnerBlock.TrialSpawnerState.ACTIVE; // Active waiting for key or after key activation
        } else {
            newState = TrialSpawnerBlock.TrialSpawnerState.INACTIVE;
        }

        if (currentState.getValue(TrialSpawnerBossBlock.STATE) != newState) {
            level.setBlock(getBlockPos(), currentState.setValue(TrialSpawnerBossBlock.STATE, newState), 3);
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("IsActivated", isActivated);
        tag.putBoolean("IsKeyActivated", isKeyActivated);
        tag.putInt("SpawnTimer", spawnTimer);
        tag.putInt("BossHP", bossHP);
        tag.putInt("CompleteTrialTimer", completeTrialTimer);
        tag.putBoolean("IsLootAnimating", isLootAnimating);
        tag.putInt("LootAnimationTick", lootAnimationTick);
        tag.putInt("CurrentLootDropIndex", currentLootDropIndex);
        if (spawnedBossUUID != null) {
            tag.putUUID("SpawnedBossUUID", spawnedBossUUID);
        }

        // Save pending loot items
        net.minecraft.nbt.ListTag lootItemsTag = new net.minecraft.nbt.ListTag();
        for (ItemStack item : pendingLootItems) {
            CompoundTag itemTag = new CompoundTag();
            item.save(itemTag);
            lootItemsTag.add(itemTag);
        }
        tag.put("PendingLootItems", lootItemsTag);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        isActivated = tag.getBoolean("IsActivated");
        isKeyActivated = tag.getBoolean("IsKeyActivated");
        spawnTimer = tag.getInt("SpawnTimer");
        bossHP = tag.getInt("BossHP");
        completeTrialTimer = tag.getInt("CompleteTrialTimer");
        isLootAnimating = tag.getBoolean("IsLootAnimating");
        lootAnimationTick = tag.getInt("LootAnimationTick");
        currentLootDropIndex = tag.getInt("CurrentLootDropIndex");
        if (tag.hasUUID("SpawnedBossUUID")) {
            spawnedBossUUID = tag.getUUID("SpawnedBossUUID");
        }

        // Load pending loot items
        this.pendingLootItems.clear();
        net.minecraft.nbt.ListTag lootItemsTag = tag.getList("PendingLootItems", 10); // 10 = CompoundTag
        for (int i = 0; i < lootItemsTag.size(); i++) {
            CompoundTag itemTag = lootItemsTag.getCompound(i);
            ItemStack stack = ItemStack.of(itemTag);
            this.pendingLootItems.add(stack);
        }
    }

    public boolean isActivated() {
        return isActivated;
    }

    public boolean isKeyActivated() {
        return isKeyActivated;
    }

    public void clientTick() {
        this.clientTickCount++;

        if (level != null && level.isClientSide) {
            BlockState state = level.getBlockState(worldPosition);
            if (state.getBlock() instanceof TrialSpawnerBossBlock) {
                TrialSpawnerBlock.TrialSpawnerState spawnerState = state.getValue(TrialSpawnerBossBlock.STATE);
                boolean isCurrentlyActive = spawnerState == TrialSpawnerBlock.TrialSpawnerState.ACTIVE;

                // Detect activation (proximity) - no particles, just track state
                if (isCurrentlyActive && !wasActivated) {
                    wasActivated = true;
                } else if (!isCurrentlyActive) {
                    wasActivated = false;
                }

                // Ambient particles (like ominous version)
                if (spawnerState == TrialSpawnerBlock.TrialSpawnerState.ACTIVE) {
                    if (clientTickCount % 5 == 0) {
                        net.minecraft.core.particles.ParticleOptions particle = net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME;
                        double x = worldPosition.getX() + 0.3 + level.random.nextDouble() * 0.4;
                        double y = worldPosition.getY() + 0.5 + level.random.nextDouble() * 0.4;
                        double z = worldPosition.getZ() + 0.3 + level.random.nextDouble() * 0.4;
                        level.addParticle(particle, x, y, z, 0.0, 0.05, 0.0);
                    }
                }
            }
        }
    }


    public int getClientTickCount() {
        return clientTickCount;
    }
}

