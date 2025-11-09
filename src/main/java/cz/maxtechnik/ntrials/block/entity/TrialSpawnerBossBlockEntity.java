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
    private static final int SPAWN_DELAY = 60;
    private static final int LOOT_DROP_INTERVAL = 10;
    private static final int COMPLETE_TRIAL_DELAY = 20;
    private static final int BASE_BOSS_HP = 60;
    private static final double HP_MULTIPLIER_PER_PLAYER = 1.2;
    
    private boolean isActivated = false;
    private boolean isKeyActivated = false;
    private int spawnTimer = -1;
    private UUID spawnedBossUUID = null;
    private Set<UUID> playersInRange = new HashSet<>();
    private int bossHP = BASE_BOSS_HP;
    
    private boolean isLootAnimating = false;
    private int lootAnimationTick = 0;
    private List<ItemStack> pendingLootItems = new ArrayList<>();
    private int currentLootDropIndex = 0;
    
    private int completeTrialTimer = -1;
    private int clientTickCount = 0;
    private transient boolean wasActivated = false;
    
    public TrialSpawnerBossBlockEntity(BlockPos pos, BlockState blockState) {
        super(NTrialsModBlockEntities.TRIAL_SPAWNER_BOSS_BLOCK_ENTITY.get(), pos, blockState);
    }
    
    // Hlavní tick metoda - spravuje spawn, loot a boss
    public void tick() {
        if (level == null || level.isClientSide) return;
        
        if (level.getGameTime() % 40 == 0) {
            updatePlayersInRange();
            checkAndActivateByProximity();
        }
        
        if (isKeyActivated && spawnTimer > 0 && --spawnTimer == 0) spawnBreezeBoss();
        if (completeTrialTimer > 0 && --completeTrialTimer == 0) generateLootReward();
        if (isLootAnimating) tickLootAnimation();
        
        if (isKeyActivated && spawnedBossUUID != null && level instanceof ServerLevel serverLevel) {
            net.minecraft.world.entity.Entity boss = serverLevel.getEntity(spawnedBossUUID);
            if (boss == null || !boss.isAlive()) handleBossKilled();
        }
        
        if (level.getGameTime() % 20 == 0) updateBlockState();
    }
    
    // Aktualizuje seznam hráčů v dosahu
    private void updatePlayersInRange() {
        if (level == null) return;
        double range = 20.0;
        AABB searchArea = new AABB(getBlockPos().getX() - range, getBlockPos().getY() - range, getBlockPos().getZ() - range,
            getBlockPos().getX() + range, getBlockPos().getY() + range, getBlockPos().getZ() + range);
        playersInRange.clear();
        for (Player player : level.getEntitiesOfClass(Player.class, searchArea)) playersInRange.add(player.getUUID());
    }
    
    // Kontroluje aktivaci podle blízkosti hráčů
    private void checkAndActivateByProximity() {
        if (level == null || level.isClientSide) return;
        double range = 8.0;
        AABB searchArea = new AABB(getBlockPos().getX() - range, getBlockPos().getY() - range, getBlockPos().getZ() - range,
            getBlockPos().getX() + range, getBlockPos().getY() + range, getBlockPos().getZ() + range);
        boolean hasPlayers = !level.getEntitiesOfClass(Player.class, searchArea).isEmpty();
        
        if (hasPlayers && !isActivated) {
            isActivated = true;
            setChanged();
            BlockState currentState = level.getBlockState(getBlockPos());
            if (currentState.getBlock() instanceof TrialSpawnerBossBlock) {
                level.setBlock(getBlockPos(), currentState.setValue(TrialSpawnerBossBlock.STATE, TrialSpawnerBlock.TrialSpawnerState.ACTIVE), Block.UPDATE_ALL);
            }
        } else if (!hasPlayers && isActivated && !isKeyActivated) {
            isActivated = false;
            setChanged();
            BlockState currentState = level.getBlockState(getBlockPos());
            if (currentState.getBlock() instanceof TrialSpawnerBossBlock) {
                level.setBlock(getBlockPos(), currentState.setValue(TrialSpawnerBossBlock.STATE, TrialSpawnerBlock.TrialSpawnerState.INACTIVE), Block.UPDATE_ALL);
            }
        }
    }
    
    // Aktivuje spawner pomocí klíče
    public void activateWithKey(Player player) {
        if (!isKeyActivated && level != null && !level.isClientSide && isActivated) {
            isKeyActivated = true;
            spawnTimer = SPAWN_DELAY;
            setChanged();
            BlockState currentState = level.getBlockState(getBlockPos());
            if (currentState.getBlock() instanceof TrialSpawnerBossBlock) {
                level.setBlock(getBlockPos(), currentState.setValue(TrialSpawnerBossBlock.STATE, TrialSpawnerBlock.TrialSpawnerState.ACTIVE), Block.UPDATE_ALL);
            }
            level.playSound(null, getBlockPos(), NTrialsModSounds.BLOCK_TRIAL_SPAWNER_DETECT_PLAYER.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
            spawnKeyActivationParticles();
        }
    }
    
    // Spawnuje částice při aktivaci klíčem
    private void spawnKeyActivationParticles() {
        if (level == null || level.isClientSide || !(level instanceof ServerLevel serverLevel)) return;
        BlockPos center = getBlockPos();
        double centerX = center.getX() + 0.5, centerY = center.getY() + 0.5, centerZ = center.getZ() + 0.5;
        for (int i = 0; i < 15; i++) {
            double angle = level.random.nextDouble() * 2 * Math.PI;
            double verticalAngle = (level.random.nextDouble() - 0.5) * Math.PI * 0.5;
            double speed = 0.1 + level.random.nextDouble() * 0.1;
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME, centerX, centerY, centerZ, 1,
                Math.cos(angle) * Math.cos(verticalAngle) * speed, Math.sin(verticalAngle) * speed, Math.sin(angle) * Math.cos(verticalAngle) * speed, 0.0);
        }
    }
    
    // Spawnuje breeze bossa
    private void spawnBreezeBoss() {
        if (level == null || level.isClientSide) return;
        int playerCount = playersInRange.size();
        bossHP = BASE_BOSS_HP;
        for (int i = 1; i < playerCount; i++) bossHP = (int) (bossHP * HP_MULTIPLIER_PER_PLAYER);
        
        BlockPos spawnPos = findSpawnPosition();
        if (spawnPos != null) {
            cz.maxtechnik.ntrials.entity.BreezeBossEntity boss = new cz.maxtechnik.ntrials.entity.BreezeBossEntity(NTrialsModEntityTypes.BREEZE_BOSS.get(), level);
            boss.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
            var healthAttribute = boss.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
            if (healthAttribute != null) {
                healthAttribute.setBaseValue(bossHP);
                boss.setHealth(boss.getMaxHealth());
            }
            if (level instanceof ServerLevel serverLevel) {
                @SuppressWarnings({"deprecation", "unused"})
                var ignored = boss.finalizeSpawn(serverLevel, level.getCurrentDifficultyAt(spawnPos), net.minecraft.world.entity.MobSpawnType.SPAWNER, null, null);
            }
            level.addFreshEntity(boss);
            spawnedBossUUID = boss.getUUID();
            level.playSound(null, getBlockPos(), NTrialsModSounds.BLOCK_TRIAL_SPAWNER_SPAWN.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
            setChanged();
        }
    }
    
    // Najde validní pozici pro spawn
    private BlockPos findSpawnPosition() {
        if (level == null) return null;
        for (int attempts = 0; attempts < 10; attempts++) {
            int x = getBlockPos().getX() + (level.random.nextInt(7) - 3);
            int z = getBlockPos().getZ() + (level.random.nextInt(7) - 3);
            int y = getBlockPos().getY();
            for (int yOffset = -1; yOffset <= 2; yOffset++) {
                BlockPos pos = new BlockPos(x, y + yOffset, z);
                if (isValidSpawnPosition(pos)) return pos;
            }
        }
        return getBlockPos().above();
    }
    
    // Zkontroluje zda je pozice validní pro spawn
    private boolean isValidSpawnPosition(BlockPos pos) {
        if (level == null) return false;
        @SuppressWarnings("deprecation")
        boolean isSolid = level.getBlockState(pos.below()).isSolid();
        return level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir() && isSolid;
    }
    
    // Zpracuje zabití bossa
    private void handleBossKilled() {
        if (level == null || level.isClientSide) return;
        completeTrialTimer = COMPLETE_TRIAL_DELAY;
        spawnedBossUUID = null;
        setChanged();
    }
    
    // Vygeneruje loot podle počtu hráčů
    private void generateLootReward() {
        if (level == null || level.isClientSide) return;
        List<ItemStack> lootItems = new ArrayList<>();
        if (level instanceof ServerLevel serverLevel) {
            for (UUID playerUUID : playersInRange) {
                Player player = serverLevel.getPlayerByUUID(playerUUID);
                if (player != null) lootItems.add(new ItemStack(NTrialsModItems.BOSS_TRIAL_KEY.get()));
            }
        }
        if (!lootItems.isEmpty()) startLootAnimation(lootItems);
        else convertToRegularSpawner();
    }
    
    // Spustí animaci vypadávání lootu
    private void startLootAnimation(List<ItemStack> lootItems) {
        this.isLootAnimating = true;
        this.lootAnimationTick = 0;
        this.currentLootDropIndex = 0;
        this.pendingLootItems = new ArrayList<>(lootItems);
        setChanged();
        if (level != null) {
            level.playSound(null, getBlockPos(), NTrialsModSounds.BLOCK_TRIAL_SPAWNER_OPEN_SHUTTER.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
            level.playSound(null, getBlockPos(), NTrialsModSounds.BLOCK_TRIAL_SPAWNER_SPAWN_ITEM_BEGIN.get(), SoundSource.BLOCKS, 0.8f, 1.0f);
            if (!level.isClientSide()) level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }
    
    // Tick animace lootu - vypadává postupně
    private void tickLootAnimation() {
        if (!isLootAnimating || pendingLootItems.isEmpty()) { stopLootAnimation(); return; }
        lootAnimationTick++;
        if (lootAnimationTick % LOOT_DROP_INTERVAL == 0 && currentLootDropIndex < pendingLootItems.size()) dropNextLootItem();
        if (currentLootDropIndex >= pendingLootItems.size()) stopLootAnimation();
    }
    
    // Vypustí další item z lootu
    private void dropNextLootItem() {
        if (level == null || level.isClientSide() || currentLootDropIndex >= pendingLootItems.size()) return;
        ItemStack itemToDrop = pendingLootItems.get(currentLootDropIndex);
        level.playSound(null, getBlockPos(), NTrialsModSounds.BLOCK_TRIAL_SPAWNER_EJECT_ITEM.get(), SoundSource.BLOCKS, 0.7f, 1.0f + (level.random.nextFloat() - 0.5f) * 0.4f);
        double offsetX = (level.random.nextDouble() - 0.5) * 0.8;
        double offsetZ = (level.random.nextDouble() - 0.5) * 0.8;
        double offsetY = 0.2 + level.random.nextDouble() * 0.3;
        net.minecraft.world.entity.item.ItemEntity itemEntity = new net.minecraft.world.entity.item.ItemEntity(level,
            getBlockPos().getX() + 0.5 + offsetX, getBlockPos().getY() + 1.0 + offsetY, getBlockPos().getZ() + 0.5 + offsetZ, itemToDrop.copy());
        itemEntity.setDeltaMovement(0.0, 0.15, 0.0);
        level.addFreshEntity(itemEntity);
        currentLootDropIndex++;
        setChanged();
    }
    
    // Zastaví animaci lootu
    private void stopLootAnimation() {
        this.isLootAnimating = false;
        this.lootAnimationTick = 0;
        this.currentLootDropIndex = 0;
        this.pendingLootItems.clear();
        setChanged();
        if (level != null) {
            level.playSound(null, getBlockPos(), NTrialsModSounds.BLOCK_TRIAL_SPAWNER_CLOSE_SHUTTER.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
            if (!level.isClientSide()) level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
        convertToRegularSpawner();
    }
    
    // Převede spawner na běžný trial spawner s breeze
    private void convertToRegularSpawner() {
        if (level == null || level.isClientSide) return;
        BlockPos pos = getBlockPos();
        BlockState newState = cz.maxtechnik.ntrials.init.NTrialsModBlocks.TRIAL_SPAWNER.get().defaultBlockState()
            .setValue(TrialSpawnerBlock.STATE, TrialSpawnerBlock.TrialSpawnerState.INACTIVE)
            .setValue(TrialSpawnerBlock.OMINOUS, false);
        level.setBlock(pos, newState, Block.UPDATE_ALL);
        BlockEntity newBlockEntity = level.getBlockEntity(pos);
        if (newBlockEntity instanceof TrialSpawnerBlockEntity trialSpawner) {
            trialSpawner.setSpawnEntity(NTrialsModEntityTypes.BREEZE.get());
        }
    }
    
    // Aktualizuje stav bloku podle aktuálního stavu
    private void updateBlockState() {
        if (level == null || level.isClientSide()) return;
        BlockState currentState = level.getBlockState(getBlockPos());
        if (!(currentState.getBlock() instanceof TrialSpawnerBossBlock)) return;
        
        TrialSpawnerBlock.TrialSpawnerState newState;
        if (this.completeTrialTimer > 0 || this.isLootAnimating || (this.pendingLootItems != null && !this.pendingLootItems.isEmpty())) {
            newState = TrialSpawnerBlock.TrialSpawnerState.EJECTING_REWARD;
        } else if (this.isKeyActivated && spawnedBossUUID != null) {
            newState = TrialSpawnerBlock.TrialSpawnerState.ACTIVE;
        } else if (this.isActivated || this.isKeyActivated) {
            newState = TrialSpawnerBlock.TrialSpawnerState.ACTIVE;
        } else {
            newState = TrialSpawnerBlock.TrialSpawnerState.INACTIVE;
        }
        
        if (currentState.getValue(TrialSpawnerBossBlock.STATE) != newState) {
            level.setBlock(getBlockPos(), currentState.setValue(TrialSpawnerBossBlock.STATE, newState), 3);
        }
    }
    
    // Uloží data do NBT
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
        if (spawnedBossUUID != null) tag.putUUID("SpawnedBossUUID", spawnedBossUUID);
        net.minecraft.nbt.ListTag lootItemsTag = new net.minecraft.nbt.ListTag();
        for (ItemStack item : pendingLootItems) {
            CompoundTag itemTag = new CompoundTag();
            item.save(itemTag);
            lootItemsTag.add(itemTag);
        }
        tag.put("PendingLootItems", lootItemsTag);
    }
    
    // Načte data z NBT
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
        if (tag.hasUUID("SpawnedBossUUID")) spawnedBossUUID = tag.getUUID("SpawnedBossUUID");
        this.pendingLootItems.clear();
        net.minecraft.nbt.ListTag lootItemsTag = tag.getList("PendingLootItems", 10);
        for (int i = 0; i < lootItemsTag.size(); i++) this.pendingLootItems.add(ItemStack.of(lootItemsTag.getCompound(i)));
    }
    
    // Zkontroluje zda je spawner aktivován
    public boolean isActivated() { return isActivated; }
    
    // Zkontroluje zda je spawner aktivován klíčem
    public boolean isKeyActivated() { return isKeyActivated; }
    
    // Client-side tick - zpracovává částice
    public void clientTick() {
        this.clientTickCount++;
        if (level == null || !level.isClientSide) return;
        BlockState state = level.getBlockState(worldPosition);
        if (!(state.getBlock() instanceof TrialSpawnerBossBlock)) return;
        
        TrialSpawnerBlock.TrialSpawnerState spawnerState = state.getValue(TrialSpawnerBossBlock.STATE);
        boolean isCurrentlyActive = spawnerState == TrialSpawnerBlock.TrialSpawnerState.ACTIVE;
        
        if (isCurrentlyActive && !wasActivated) wasActivated = true;
        else if (!isCurrentlyActive) wasActivated = false;
        
        if (spawnerState == TrialSpawnerBlock.TrialSpawnerState.ACTIVE && clientTickCount % 10 == 0) {
            level.addParticle(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
                worldPosition.getX() + 0.3 + level.random.nextDouble() * 0.4,
                worldPosition.getY() + 0.5 + level.random.nextDouble() * 0.4,
                worldPosition.getZ() + 0.3 + level.random.nextDouble() * 0.4, 0.0, 0.05, 0.0);
        }
    }
    
    // Vrátí počet client ticků
    public int getClientTickCount() { return clientTickCount; }
}
