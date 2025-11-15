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
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import cz.maxtechnik.ntrials.init.NTrialsModMobEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.entity.AreaEffectCloud;

import java.util.*;
@SuppressWarnings("deprecation")
public class TrialSpawnerBlockEntity extends BlockEntity {
    
    private static final int COOLDOWN_OMINOUS_RESET_TICK = 10;
    private static final int LOOT_DROP_INTERVAL = 10;
    
    private static final List<MobEffect> OMNIOUS_EFFECTS = List.of(
        MobEffects.REGENERATION, MobEffects.BLINDNESS, MobEffects.POISON, MobEffects.MOVEMENT_SLOWDOWN,
        MobEffects.CONFUSION, MobEffects.WEAKNESS, MobEffects.MOVEMENT_SPEED, MobEffects.DAMAGE_BOOST
    );
    
    private static final String[] ARMOR_HELMETS = {"minecraft:iron_helmet", "minecraft:golden_helmet", "minecraft:diamond_helmet"};
    private static final String[] ARMOR_CHESTPLATES = {"minecraft:iron_chestplate", "minecraft:golden_chestplate", "minecraft:diamond_chestplate"};
    private static final String[] ARMOR_LEGGINGS = {"minecraft:iron_leggings", "minecraft:golden_leggings", "minecraft:diamond_leggings"};
    private static final String[] ARMOR_BOOTS = {"minecraft:iron_boots", "minecraft:golden_boots", "minecraft:diamond_boots"};
    private static final String[] WEAPONS = {"minecraft:stone_sword", "minecraft:iron_sword", "minecraft:diamond_sword", "minecraft:iron_axe", "minecraft:diamond_axe"};
    
    private static final int DEFAULT_BASE_MOBS_PER_WAVE = 2;
    private static final int DEFAULT_BASE_MAX_WAVES = 2;
    private static final int BREEZE_BASE_MOBS_PER_WAVE = 1;
    private static final int BREEZE_BASE_MAX_WAVES = 1;
    private static final int BREEZE_MOBS_ADDED_PER_PLAYER = 1;
    private static final int BREEZE_WAVES_ADDED_PER_PLAYER = 2;
    
    private final Set<UUID> detectedPlayers = new HashSet<>();
    private final Set<UUID> spawnedEntities = new HashSet<>();
    
    private int cooldownTime = 0;
    private boolean isOminous = false;
    private int tickCount = 0;
    private int clientTickCount = 0;
    private EntityType<?> spawnEntity = null;
    private boolean hasBeenSynced = false;
    private boolean shouldResetOminousOnCooldownEnd = false;
    
    private transient boolean wasOminous = false;
    private transient cz.maxtechnik.ntrials.block.TrialSpawnerBlock.TrialSpawnerState lastState = null;
    
    private int maxWaves = 2;
    private int mobsPerWave = 3;
    private int currentTrialMobsPerWave = 2;
    private int maxWavesCount = 2;
    private int playersCount = 0;
    private int currentWave = 0;
    private int currentWaveMobs = 0;
    private boolean trialActive = false;
    
    private String normalLootTable = "ntrials:chests/spawner";
    private String ominousLootTable = "ntrials:chests/spawner_ominous";
    
    private boolean isLootAnimating = false;
    private int lootAnimationTick = 0;
    private List<ItemStack> pendingLootItems = new ArrayList<>();
    private int currentLootDropIndex = 0;
    
    private int completeTrialTimer = -1;
    private int startTrialTimer = -1;
    
    public TrialSpawnerBlockEntity(BlockPos pos, BlockState blockState) {
        super(NTrialsModBlockEntities.TRIAL_SPAWNER_BLOCK_ENTITY.get(), pos, blockState);
    }
    
    // Hlavní tick metoda - spouští se každý tick a řídí všechny procesy spawneru
    public void tick() {
        this.tickCount++;
        
        if (this.cooldownTime > 0) {
            this.cooldownTime--;
            if (this.cooldownTime == COOLDOWN_OMINOUS_RESET_TICK && this.shouldResetOminousOnCooldownEnd) {
                assert level != null;
                BlockState currentState = level.getBlockState(getBlockPos());
                if (currentState.getValue(cz.maxtechnik.ntrials.block.TrialSpawnerBlock.OMINOUS)) {
                    level.setBlock(getBlockPos(), currentState.setValue(cz.maxtechnik.ntrials.block.TrialSpawnerBlock.OMINOUS, false), 3);
                    this.setOminous(false);
                    this.shouldResetOminousOnCooldownEnd = false;
                    updateBlockState();
                }
            }
        }
        
        if (startTrialTimer > 0 && --startTrialTimer == 0) spawnWave();
        if (completeTrialTimer > 0 && --completeTrialTimer == 0) {
            trialActive = false;
            currentWave = 0;
            currentWaveMobs = 0;
            spawnedEntities.clear();
            generateLootReward();
            setCooldownTime(36000);
        }
        
        if (isLootAnimating) tickLootAnimation();
        
        if (this.tickCount % 100 == 0) {
            assert level != null;
            if (!level.isClientSide() && hasSpawnEntity() && !trialActive && cooldownTime == 0) {
                boolean isOminousBlock = level.getBlockState(getBlockPos()).getValue(cz.maxtechnik.ntrials.block.TrialSpawnerBlock.OMINOUS);
                level.playSound(null, getBlockPos(), isOminousBlock ? NTrialsModSounds.BLOCK_TRIAL_SPAWNER_AMBIENT_OMINOUS.get() : NTrialsModSounds.BLOCK_TRIAL_SPAWNER_AMBIENT.get(),
                        SoundSource.BLOCKS, isOminousBlock ? 0.8f : 0.6f, 1.0f);
            }
        }
        
        if (this.tickCount % 40 == 0) checkAndManageTrial();
        if (!hasBeenSynced && this.spawnEntity != null && this.tickCount % 200 == 0) {
            syncToClients();
            hasBeenSynced = true;
        }
        assert level != null;
        if (!level.isClientSide() && isOminous() && trialActive && this.tickCount % 600 == 0) spawnRandomOminousEffect();
    }
    
    // Client-side tick - zpracovává částice a vizuální efekty
    public void clientTick() {
        this.clientTickCount++;
        if (level == null || !level.isClientSide) return;
        
        BlockState state = level.getBlockState(worldPosition);
        if (!(state.getBlock() instanceof cz.maxtechnik.ntrials.block.TrialSpawnerBlock)) return;
        
        boolean ominous = state.getValue(cz.maxtechnik.ntrials.block.TrialSpawnerBlock.OMINOUS);
        cz.maxtechnik.ntrials.block.TrialSpawnerBlock.TrialSpawnerState spawnerState = state.getValue(cz.maxtechnik.ntrials.block.TrialSpawnerBlock.STATE);
        
        if (ominous && !wasOminous) {
            BlockPos pos = worldPosition;
            for (int i = 0; i < 20; i++) {
                double x = pos.getX() + (level.random.nextDouble() - 0.5) * 6;
                double y = pos.getY() + level.random.nextDouble() * 3;
                double z = pos.getZ() + (level.random.nextDouble() - 0.5) * 6;
                double dist = Math.sqrt(Math.pow(x - pos.getX(), 2) + Math.pow(y - pos.getY(), 2) + Math.pow(z - pos.getZ(), 2));
                if (dist <= 3.0) level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, (level.random.nextDouble() - 0.5) * 0.1, level.random.nextDouble() * 0.2, (level.random.nextDouble() - 0.5) * 0.1);
            }
            wasOminous = true;
        } else if (!ominous) wasOminous = false;
        
        if (lastState != spawnerState && (spawnerState == cz.maxtechnik.ntrials.block.TrialSpawnerBlock.TrialSpawnerState.WAITING_FOR_PLAYERS || spawnerState == cz.maxtechnik.ntrials.block.TrialSpawnerBlock.TrialSpawnerState.ACTIVE)) {
            spawnActivationParticles(ominous);
        }
        lastState = spawnerState;
        
        if (spawnerState == cz.maxtechnik.ntrials.block.TrialSpawnerBlock.TrialSpawnerState.WAITING_FOR_PLAYERS || spawnerState == cz.maxtechnik.ntrials.block.TrialSpawnerBlock.TrialSpawnerState.ACTIVE) {
            if (clientTickCount % 10 == 0) {
                ParticleOptions particle = ominous ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME;
                level.addParticle(particle, worldPosition.getX() + 0.3 + level.random.nextDouble() * 0.4, worldPosition.getY() + 0.5 + level.random.nextDouble() * 0.4, worldPosition.getZ() + 0.3 + level.random.nextDouble() * 0.4, 0.0, 0.05, 0.0);
            }
        }
    }
    
    // Vytvoří částice při aktivaci spawneru
    private void spawnActivationParticles(boolean ominous) {
        if (level == null) return;
        BlockPos center = worldPosition;
        double centerX = center.getX() + 0.5, centerY = center.getY() + 0.5, centerZ = center.getZ() + 0.5;
        ParticleOptions particle = ominous ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME;
        for (int i = 0; i < 15; i++) {
            double angle = level.random.nextDouble() * 2 * Math.PI;
            double verticalAngle = (level.random.nextDouble() - 0.5) * Math.PI * 0.5;
            double speed = 0.1 + level.random.nextDouble() * 0.1;
            level.addParticle(particle, centerX, centerY, centerZ, Math.cos(angle) * Math.cos(verticalAngle) * speed, Math.sin(verticalAngle) * speed, Math.sin(angle) * Math.cos(verticalAngle) * speed);
        }
    }
    
    // Načte data z NBT při načítání světa
    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        this.cooldownTime = tag.getInt("CooldownTime");
        this.isOminous = tag.getBoolean("Ominous");
        this.tickCount = tag.getInt("TickCount");
        this.shouldResetOminousOnCooldownEnd = tag.getBoolean("ShouldResetOminousOnCooldownEnd");
        this.maxWaves = tag.getInt("MaxWaves");
        if (this.maxWaves <= 0) this.maxWaves = 5;
        this.mobsPerWave = tag.getInt("MobsPerWave");
        if (this.mobsPerWave <= 0) this.mobsPerWave = 3;
        this.currentWave = tag.getInt("CurrentWave");
        this.currentWaveMobs = tag.getInt("CurrentWaveMobs");
        this.trialActive = tag.getBoolean("TrialActive");
        if (tag.contains("NormalLootTable")) this.normalLootTable = tag.getString("NormalLootTable");
        if (tag.contains("OminousLootTable")) this.ominousLootTable = tag.getString("OminousLootTable");
        this.isLootAnimating = tag.getBoolean("IsLootAnimating");
        this.lootAnimationTick = tag.getInt("LootAnimationTick");
        this.currentLootDropIndex = tag.getInt("CurrentLootDropIndex");
        if (tag.contains("PendingLootItems")) {
            this.pendingLootItems.clear();
            ListTag lootItemsTag = tag.getList("PendingLootItems", 10);
            for (int i = 0; i < lootItemsTag.size(); i++) this.pendingLootItems.add(ItemStack.of(lootItemsTag.getCompound(i)));
        }
        if (tag.contains("SpawnEntity")) {
            ResourceLocation entityLocation = ResourceLocation.parse(tag.getString("SpawnEntity"));
            this.spawnEntity = ForgeRegistries.ENTITY_TYPES.getValue(entityLocation);
        } else this.spawnEntity = null;
        if (tag.contains("SpawnedEntities")) {
            this.spawnedEntities.clear();
            CompoundTag spawnedTag = tag.getCompound("SpawnedEntities");
            for (String key : spawnedTag.getAllKeys()) {
                try { this.spawnedEntities.add(UUID.fromString(key)); } catch (IllegalArgumentException ignored) {}
            }
        }
        if (tag.contains("DetectedPlayers")) {
            this.detectedPlayers.clear();
            CompoundTag playersTag = tag.getCompound("DetectedPlayers");
            for (String key : playersTag.getAllKeys()) {
                try { this.detectedPlayers.add(UUID.fromString(key)); } catch (IllegalArgumentException ignored) {}
            }
        }
        if (level != null && !level.isClientSide() && this.spawnEntity != null) syncToClients();
    }
    
    // Uloží data do NBT při ukládání světa
    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("CooldownTime", this.cooldownTime);
        tag.putBoolean("Ominous", this.isOminous);
        tag.putInt("TickCount", this.tickCount);
        tag.putBoolean("ShouldResetOminousOnCooldownEnd", this.shouldResetOminousOnCooldownEnd);
        tag.putInt("MaxWaves", this.maxWaves);
        tag.putInt("MobsPerWave", this.mobsPerWave);
        tag.putInt("CurrentWave", this.currentWave);
        tag.putInt("CurrentWaveMobs", this.currentWaveMobs);
        tag.putBoolean("TrialActive", this.trialActive);
        tag.putString("NormalLootTable", this.normalLootTable);
        tag.putString("OminousLootTable", this.ominousLootTable);
        tag.putBoolean("IsLootAnimating", this.isLootAnimating);
        tag.putInt("LootAnimationTick", this.lootAnimationTick);
        tag.putInt("CurrentLootDropIndex", this.currentLootDropIndex);
        ListTag lootItemsTag = new ListTag();
        for (ItemStack item : this.pendingLootItems) {
            CompoundTag itemTag = new CompoundTag();
            item.save(itemTag);
            lootItemsTag.add(itemTag);
        }
        tag.put("PendingLootItems", lootItemsTag);
        if (this.spawnEntity != null) {
            ResourceLocation entityLocation = ForgeRegistries.ENTITY_TYPES.getKey(this.spawnEntity);
            if (entityLocation != null) tag.putString("SpawnEntity", entityLocation.toString());
        }
        CompoundTag spawnedTag = new CompoundTag();
        for (UUID uuid : this.spawnedEntities) spawnedTag.putBoolean(uuid.toString(), true);
        tag.put("SpawnedEntities", spawnedTag);
        CompoundTag playersTag = new CompoundTag();
        for (UUID uuid : this.detectedPlayers) playersTag.putBoolean(uuid.toString(), true);
        tag.put("DetectedPlayers", playersTag);
    }

    // Nastaví čas cooldownu
    public void setCooldownTime(int cooldownTime) { this.cooldownTime = cooldownTime; setChanged(); }
    
    // Zkontroluje zda je spawner v ominous módu
    public boolean isOminous() { return isOminous; }
    
    // Nastaví ominous stav
    public void setOminous(boolean ominous) { this.isOminous = ominous; setChanged(); }

    // Vrátí počet client ticků
    public int getClientTickCount() { return clientTickCount; }
    
    // Vrátí typ entity která se spawnuje
    @Nullable
    public EntityType<?> getSpawnEntity() { return spawnEntity; }
    
    // Nastaví typ entity která se má spawnovat
    public void setSpawnEntity(@Nullable EntityType<?> spawnEntity) {
        this.spawnEntity = spawnEntity;
        this.hasBeenSynced = false;
        setChanged();
        syncToClients();
    }
    
    // Synchronizuje data s klienty
    private void syncToClients() {
        if (level != null && !level.isClientSide() && level instanceof ServerLevel serverLevel) {
            NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> serverLevel.getChunkAt(getBlockPos())), new TrialSpawnerSyncPacket(getBlockPos(), this.spawnEntity));
        }
    }
    
    // Zkontroluje zda má spawner nastavenou entitu
    public boolean hasSpawnEntity() { return this.spawnEntity != null; }

    // Kontroluje hráče a spravuje trial
    private void checkAndManageTrial() {
        if (level == null || level.isClientSide() || !hasSpawnEntity()) return;
        
        double range = 14.0;
        net.minecraft.world.phys.AABB searchArea = new net.minecraft.world.phys.AABB(
            getBlockPos().getX() - range, getBlockPos().getY() - range, getBlockPos().getZ() - range,
            getBlockPos().getX() + range, getBlockPos().getY() + range, getBlockPos().getZ() + range
        );
        
        List<net.minecraft.world.entity.player.Player> players = level.getEntitiesOfClass(net.minecraft.world.entity.player.Player.class, searchArea);
        int playerCount = players.size();
        
        boolean hasSurvivalPlayer = false, hasPlayerWithBadOmen = false;
        for (net.minecraft.world.entity.player.Player player : players) {
            if (player instanceof ServerPlayer serverPlayer && serverPlayer.gameMode.getGameModeForPlayer() == GameType.SURVIVAL) {
                hasSurvivalPlayer = true;
                if (player.hasEffect(MobEffects.BAD_OMEN)) {
                    player.removeEffect(MobEffects.BAD_OMEN);
                    player.addEffect(new MobEffectInstance(NTrialsModMobEffects.TRIAL_OMEN.get(), 36000, 0));
                }
                if (player.hasEffect(NTrialsModMobEffects.TRIAL_OMEN.get())) hasPlayerWithBadOmen = true;
            }
        }
        
        if (hasPlayerWithBadOmen) {
            BlockState currentState = level.getBlockState(getBlockPos());
            if (!currentState.getValue(cz.maxtechnik.ntrials.block.TrialSpawnerBlock.OMINOUS)) {
                level.setBlock(getBlockPos(), currentState.setValue(cz.maxtechnik.ntrials.block.TrialSpawnerBlock.OMINOUS, true), 3);
                this.setOminous(true);
                level.playSound(null, getBlockPos(), NTrialsModSounds.BLOCK_TRIAL_SPAWNER_OMINOUS_ACTIVATE.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
            }
            if (this.cooldownTime > 0 && !this.shouldResetOminousOnCooldownEnd) {
                this.cooldownTime = 0;
            }
        }
        
        if (this.cooldownTime > 0) return;
        
        if (hasSurvivalPlayer && !trialActive && currentWave == 0) {
            level.playSound(null, getBlockPos(), NTrialsModSounds.BLOCK_TRIAL_SPAWNER_DETECT_PLAYER.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
            startTrial();
        } else if (playerCount == 0 && trialActive && spawnedEntities.isEmpty()) stopTrial();
        else if (trialActive && playerCount > 0) checkWaveCompletion();
        
        updateBlockState();
    }
    
    // Aktualizuje stav bloku podle aktuálního stavu trial
    private void updateBlockState() {
        if (level == null || level.isClientSide()) return;
        BlockState currentState = level.getBlockState(getBlockPos());
        if (!(currentState.getBlock() instanceof cz.maxtechnik.ntrials.block.TrialSpawnerBlock)) return;
        
        cz.maxtechnik.ntrials.block.TrialSpawnerBlock.TrialSpawnerState newState;
        if (this.completeTrialTimer > 0 || this.isLootAnimating || (this.pendingLootItems != null && !this.pendingLootItems.isEmpty())) {
            newState = cz.maxtechnik.ntrials.block.TrialSpawnerBlock.TrialSpawnerState.EJECTING_REWARD;
        } else if (this.cooldownTime > COOLDOWN_OMINOUS_RESET_TICK && this.isOminous) {
            newState = cz.maxtechnik.ntrials.block.TrialSpawnerBlock.TrialSpawnerState.COOLDOWN;
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
    
    // Spustí trial
    private void startTrial() {
        trialActive = true;
        currentWave = 1;
        spawnedEntities.clear();
        updateMobCountBasedOnPlayers();
        startTrialTimer = 20;
    }
    
    // Aktualizuje počet mobů podle počtu hráčů (breeze má speciální hodnoty)
    private void updateMobCountBasedOnPlayers() {
        if (level == null || level.isClientSide()) return;
        double scanRange = 16.0;
        net.minecraft.world.phys.AABB scanArea = new net.minecraft.world.phys.AABB(
            getBlockPos().getX() - scanRange, getBlockPos().getY() - scanRange, getBlockPos().getZ() - scanRange,
            getBlockPos().getX() + scanRange, getBlockPos().getY() + scanRange, getBlockPos().getZ() + scanRange
        );
        int playerCount = level.getEntitiesOfClass(net.minecraft.world.entity.player.Player.class, scanArea).size();
        this.playersCount = playerCount;
        boolean isBreeze = spawnEntity != null && spawnEntity == cz.maxtechnik.ntrials.init.NTrialsModEntityTypes.BREEZE.get();
        if (isBreeze) {
            this.currentTrialMobsPerWave = BREEZE_BASE_MOBS_PER_WAVE + (playerCount * BREEZE_MOBS_ADDED_PER_PLAYER);
            this.maxWavesCount = BREEZE_BASE_MAX_WAVES + (playerCount * BREEZE_WAVES_ADDED_PER_PLAYER);
        } else {
            this.currentTrialMobsPerWave = DEFAULT_BASE_MOBS_PER_WAVE + playerCount;
            this.maxWavesCount = DEFAULT_BASE_MAX_WAVES + playerCount;
        }
    }
    
    // Zastaví trial a vyčistí entity
    private void stopTrial() {
        trialActive = false;
        currentWave = 0;
        currentWaveMobs = 0;
        cleanupSpawnedEntities();
        spawnedEntities.clear();
        updateBlockState();
    }
    
    // Spawnuje vlnu mobů
    private void spawnWave() {
        if (level == null || spawnEntity == null) return;
        BlockState currentState = level.getBlockState(getBlockPos());
        boolean isOminousBlock = currentState.getValue(cz.maxtechnik.ntrials.block.TrialSpawnerBlock.OMINOUS);
        level.playSound(null, getBlockPos(), NTrialsModSounds.BLOCK_TRIAL_SPAWNER_SPAWN.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
        
        for (int i = 0; i < currentTrialMobsPerWave; i++) {
            BlockPos spawnPos = findSpawnPosition();
            net.minecraft.world.entity.Entity entity = spawnEntity.create(level);
            if (entity != null) {
                entity.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
                if (entity instanceof net.minecraft.world.entity.Mob mob) {
                    if (isOminousBlock) equipOminousMob(mob);
                    @SuppressWarnings({"deprecation", "unused"})
                    var ignored = mob.finalizeSpawn((ServerLevel) level, level.getCurrentDifficultyAt(spawnPos), net.minecraft.world.entity.MobSpawnType.SPAWNER, null, null);
                }
                level.addFreshEntity(entity);
                spawnedEntities.add(entity.getUUID());
                spawnSpawnParticles(getBlockPos(), isOminousBlock);
            }
        }
    }
    
    // Najde validní pozici pro spawn
    private BlockPos findSpawnPosition() {
        for (int attempts = 0; attempts < 10; attempts++) {
            assert level != null;
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
        boolean isSolid = level.getBlockState(pos.below()).isSolid();
        return level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir() && isSolid;
    }
    
    // Zkontroluje dokončení vlny a spawnuje další pokud je potřeba
    private void checkWaveCompletion() {
        spawnedEntities.removeIf(uuid -> {
            assert level != null;
            net.minecraft.world.entity.Entity entity = ((net.minecraft.server.level.ServerLevel) level).getEntity(uuid);
            return entity == null || !entity.isAlive();
        });
        int alive = spawnedEntities.size();
        if (alive == 0) {
            if (currentWave >= maxWavesCount) completeTrial();
            else { currentWave++; spawnWave(); }
            return;
        }
        int needed = currentTrialMobsPerWave - alive;
        if (needed > 0 && (currentWave + 1) <= maxWavesCount) {
            currentWave++;
            level.playSound(null, getBlockPos(), NTrialsModSounds.BLOCK_TRIAL_SPAWNER_SPAWN.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
            BlockState currentState = level.getBlockState(getBlockPos());
            boolean isOminousBlock = currentState.getValue(cz.maxtechnik.ntrials.block.TrialSpawnerBlock.OMINOUS);
            for (int i = 0; i < needed; i++) {
                BlockPos spawnPos = findSpawnPosition();
                net.minecraft.world.entity.Entity entity = spawnEntity.create(level);
                if (entity != null) {
                    entity.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
                    if (entity instanceof net.minecraft.world.entity.Mob mob) {
                        if (isOminousBlock) equipOminousMob(mob);
                        @SuppressWarnings({"deprecation", "unused"})
                        var ignored = mob.finalizeSpawn((ServerLevel) level, level.getCurrentDifficultyAt(spawnPos), net.minecraft.world.entity.MobSpawnType.SPAWNER, null, null);
                    }
                    level.addFreshEntity(entity);
                    spawnedEntities.add(entity.getUUID());
                    spawnSpawnParticles(getBlockPos(), isOminousBlock);
                }
            }
        }
    }
    
    // Dokončí trial a spustí timer pro loot
    private void completeTrial() { completeTrialTimer = 20; }
    
    // Vygeneruje loot podle počtu hráčů a ominous stavu
    private void generateLootReward() {
        if (level == null || level.isClientSide() || !(level instanceof ServerLevel serverLevel)) return;
        BlockState currentState = level.getBlockState(getBlockPos());
        boolean isOminousBlock = currentState.getValue(cz.maxtechnik.ntrials.block.TrialSpawnerBlock.OMINOUS);
        if (isOminousBlock) this.shouldResetOminousOnCooldownEnd = true;
        
        String lootTableId = isOminousBlock ? ominousLootTable : normalLootTable;
        ResourceLocation lootTableLocation = ResourceLocation.parse(lootTableId);
        LootTable lootTable = serverLevel.getServer().getLootData().getLootTable(lootTableLocation);
        if (lootTable == LootTable.EMPTY) {
            lootTableLocation = ResourceLocation.parse(normalLootTable);
            lootTable = serverLevel.getServer().getLootData().getLootTable(lootTableLocation);
        }
        
        LootParams lootParams = new LootParams.Builder(serverLevel).withParameter(LootContextParams.ORIGIN, getBlockPos().getCenter()).create(LootContextParamSets.CHEST);
        int lootMultiplier = Math.max(1, playersCount);
        List<ItemStack> allLootItems = new ArrayList<>();
        for (int i = 0; i < lootMultiplier; i++) {
            for (ItemStack item : lootTable.getRandomItems(lootParams)) {
                if (!item.isEmpty()) allLootItems.add(item.copy());
            }
        }
        if (!allLootItems.isEmpty()) startLootAnimation(allLootItems);
    }
    
    // Spustí animaci vypadávání lootu
    private void startLootAnimation(List<ItemStack> lootItems) {
        this.isLootAnimating = true;
        this.lootAnimationTick = 0;
        this.currentLootDropIndex = 0;
        this.pendingLootItems = new ArrayList<>(lootItems);
        setChanged();
        assert level != null;
        level.playSound(null, getBlockPos(), NTrialsModSounds.BLOCK_TRIAL_SPAWNER_OPEN_SHUTTER.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
        level.playSound(null, getBlockPos(), NTrialsModSounds.BLOCK_TRIAL_SPAWNER_SPAWN_ITEM_BEGIN.get(), SoundSource.BLOCKS, 0.8f, 1.0f);
        if (level != null && !level.isClientSide()) level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
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
        net.minecraft.world.entity.item.ItemEntity itemEntity = new net.minecraft.world.entity.item.ItemEntity(level, getBlockPos().getX() + 0.5 + offsetX, getBlockPos().getY() + 1.0 + offsetY, getBlockPos().getZ() + 0.5 + offsetZ, itemToDrop.copy());
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
        assert level != null;
        level.playSound(null, getBlockPos(), NTrialsModSounds.BLOCK_TRIAL_SPAWNER_CLOSE_SHUTTER.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            updateBlockState();
        }
    }

    // Vrátí update tag pro synchronizaci
    @Override
    public @NotNull CompoundTag getUpdateTag() { CompoundTag tag = super.getUpdateTag(); this.saveAdditional(tag); return tag; }
    
    // Zpracuje update tag od klienta
    @Override
    public void handleUpdateTag(CompoundTag tag) { super.handleUpdateTag(tag); this.load(tag); }
    
    // Vrátí update packet pro síťovou synchronizaci
    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }
    
    // Zničí všechny spawnuté entity
    private void cleanupSpawnedEntities() {
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            spawnedEntities.forEach(uuid -> {
                net.minecraft.world.entity.Entity entity = serverLevel.getEntity(uuid);
                if (entity != null) entity.discard();
            });
        }
    }
    
    // Vybaví ominous moba brněním, zbraněmi a enchanty
    private void equipOminousMob(net.minecraft.world.entity.Mob mob) {
        if (level == null) return;
        
        float currentHealth = mob.getMaxHealth();
        Objects.requireNonNull(mob.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(currentHealth * 1.5f);
        mob.setHealth(mob.getMaxHealth());
        
        if (mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE) != null) {
            double currentDamage = Objects.requireNonNull(mob.getAttribute(Attributes.ATTACK_DAMAGE)).getBaseValue();
            Objects.requireNonNull(mob.getAttribute(Attributes.ATTACK_DAMAGE)).setBaseValue(currentDamage * 1.25);
        }
        
        net.minecraft.util.RandomSource random = level.random;
        equipArmorPiece(mob, random, ARMOR_HELMETS, net.minecraft.world.entity.EquipmentSlot.HEAD, 0.3f);
        equipArmorPiece(mob, random, ARMOR_CHESTPLATES, net.minecraft.world.entity.EquipmentSlot.CHEST, 0.3f);
        equipArmorPiece(mob, random, ARMOR_LEGGINGS, net.minecraft.world.entity.EquipmentSlot.LEGS, 0.3f);
        equipArmorPiece(mob, random, ARMOR_BOOTS, net.minecraft.world.entity.EquipmentSlot.FEET, 0.3f);
        equipArmorPiece(mob, random, WEAPONS, net.minecraft.world.entity.EquipmentSlot.MAINHAND, 0.5f);
        
        addRandomEnchantments(mob);
    }
    
    // Pomocná funkce pro vybavení armor kusu
    private void equipArmorPiece(net.minecraft.world.entity.Mob mob, net.minecraft.util.RandomSource random, String[] items, net.minecraft.world.entity.EquipmentSlot slot, float chance) {
        if (random.nextFloat() < chance) {
            net.minecraft.world.item.Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(items[random.nextInt(items.length)]));
            if (item != null) {
                net.minecraft.world.item.ItemStack stack = new net.minecraft.world.item.ItemStack(item);
                mob.setItemSlot(slot, stack);
                mob.setDropChance(slot, 0.0f);
            }
        }
    }
    
    // Přidá náhodné enchanty na vybavení moba
    private void addRandomEnchantments(net.minecraft.world.entity.Mob mob) {
        if (level == null) return;
        net.minecraft.util.RandomSource random = level.random;
        if (random.nextFloat() >= 0.4f) return;
        
        net.minecraft.world.entity.EquipmentSlot[] slots = {
            net.minecraft.world.entity.EquipmentSlot.HEAD, net.minecraft.world.entity.EquipmentSlot.CHEST,
            net.minecraft.world.entity.EquipmentSlot.LEGS, net.minecraft.world.entity.EquipmentSlot.FEET,
            net.minecraft.world.entity.EquipmentSlot.MAINHAND
        };
        
        for (net.minecraft.world.entity.EquipmentSlot slot : slots) {
            net.minecraft.world.item.ItemStack item = mob.getItemBySlot(slot);
            if (!item.isEmpty() && random.nextFloat() < 0.3f) {
                if (slot == net.minecraft.world.entity.EquipmentSlot.MAINHAND) {
                    if (random.nextBoolean()) item.enchant(net.minecraft.world.item.enchantment.Enchantments.SHARPNESS, random.nextInt(3) + 1);
                    if (random.nextBoolean()) item.enchant(net.minecraft.world.item.enchantment.Enchantments.KNOCKBACK, random.nextInt(2) + 1);
                } else {
                    if (random.nextBoolean()) item.enchant(net.minecraft.world.item.enchantment.Enchantments.ALL_DAMAGE_PROTECTION, random.nextInt(3) + 1);
                    if (random.nextBoolean()) item.enchant(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING, random.nextInt(2) + 1);
                }
                mob.setItemSlot(slot, item);
            }
        }
    }
    
    // Spawnuje částice při spawnu moba
    private void spawnSpawnParticles(BlockPos center, boolean isOminous) {
        if (level == null) return;
        ParticleOptions particle = isOminous ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME;
        for (int i = 0; i < 10; i++) {
            double x = center.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 4;
            double y = center.getY() + 0.5 + (level.random.nextDouble() - 0.5) * 4;
            double z = center.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 4;
            double dist = Math.sqrt(Math.pow(x - center.getX() - 0.5, 2) + Math.pow(y - center.getY() - 0.5, 2) + Math.pow(z - center.getZ() - 0.5, 2));
            if (dist <= 2.0) level.addParticle(particle, x, y, z, (level.random.nextDouble() - 0.5) * 0.05, level.random.nextDouble() * 0.1, (level.random.nextDouble() - 0.5) * 0.05);
        }
    }
    
    // Spawnuje náhodný ominous efekt v oblasti
    private void spawnRandomOminousEffect() {
        if (level == null || OMNIOUS_EFFECTS.isEmpty()) return;
        MobEffect selectedEffect = OMNIOUS_EFFECTS.get(level.random.nextInt(OMNIOUS_EFFECTS.size()));
        BlockPos effectPos = findEffectPosition();
        if (effectPos == null) return;
        AreaEffectCloud cloud = new AreaEffectCloud(EntityType.AREA_EFFECT_CLOUD, level);
        cloud.setPos(effectPos.getX() + 0.5, effectPos.getY() + 0.5, effectPos.getZ() + 0.5);
        cloud.setRadius(3.0f);
        cloud.setDuration(200);
        cloud.addEffect(new MobEffectInstance(selectedEffect, 200, 0));
        cloud.setParticle(ParticleTypes.ENTITY_EFFECT);
        level.addFreshEntity(cloud);
    }
    
    // Najde pozici pro spawn ominous efektu
    private BlockPos findEffectPosition() {
        double radius = 10.0;
        for (int attempts = 0; attempts < 20; attempts++) {
            assert level != null;
            double dx = (level.random.nextDouble() - 0.5) * 2 * radius;
            double dz = (level.random.nextDouble() - 0.5) * 2 * radius;
            if (Math.sqrt(dx * dx + dz * dz) > radius) continue;
            int x = getBlockPos().getX() + (int) dx;
            int z = getBlockPos().getZ() + (int) dz;
            int y = getBlockPos().getY();
            for (int yOffset = -2; yOffset <= 3; yOffset++) {
                BlockPos pos = new BlockPos(x, y + yOffset, z);
                boolean isSolid = level.getBlockState(pos.below()).isSolid();
                if (level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir() && isSolid) return pos;
            }
        }
        return null;
    }
}
