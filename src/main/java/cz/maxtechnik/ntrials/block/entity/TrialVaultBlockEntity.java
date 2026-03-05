package cz.maxtechnik.ntrials.block.entity;

import cz.maxtechnik.ntrials.NTrialsModCommonConfig;
import cz.maxtechnik.ntrials.block.TrialVaultBlock;
import cz.maxtechnik.ntrials.init.basic.NTrialsModSounds;
import cz.maxtechnik.ntrials.init.other.NTrialsModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TrialVaultBlockEntity extends BlockEntity {
    private final Set<UUID> playersWhoOpened = new HashSet<>();
    private int animationTick = 0;
    private boolean isAnimating = false;
    private List<ItemStack> pendingLoot = new ArrayList<>();
    private int lootDropIndex = 0;

    private List<ItemStack> displayItems = new ArrayList<>();
    private int currentDisplayItemIndex = 0;
    private int displayItemSwitchTick = 0;
    private float itemRotation = 0.0f;

    private String vaultTag = "";
    private String lootTable = "";

    public TrialVaultBlockEntity(BlockPos pos, BlockState state) {
        super(NTrialsModBlockEntities.VAULT_BLOCK_ENTITY.get(), pos, state);
    }

    public boolean hasPlayerOpened(UUID uuid) { return playersWhoOpened.contains(uuid); }
    public void addPlayerWhoOpened(UUID uuid) { playersWhoOpened.add(uuid); setChanged(); }
    public String getVaultTag() { return vaultTag; }
    public void setVaultTag(String tag) { this.vaultTag = tag; setChanged(); }
    public String getLootTable() { return lootTable; }
    public void setLootTable(String table) { this.lootTable = table; setChanged(); }

    public ItemStack getCurrentDisplayItem() {
        if (displayItems.isEmpty()) return ItemStack.EMPTY;
        return displayItems.get(currentDisplayItemIndex % displayItems.size());
    }

    public float getItemRotation() { return itemRotation; }
    public boolean hasDisplayItems() { return !displayItems.isEmpty(); }

    public void startAnimation(List<ItemStack> loot) {
        this.isAnimating = true;
        this.animationTick = 0;
        this.pendingLoot = new ArrayList<>(loot);
        this.lootDropIndex = 0;
        setChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TrialVaultBlockEntity entity) {
        if (level.isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) level;
        long time = level.getGameTime();
        TrialVaultBlock.VaultState vaultState = state.getValue(TrialVaultBlock.STATE);

        if (time % 10 == 0) addParticles(serverLevel, pos, ParticleTypes.SMOKE, 0.8);

        if (vaultState == TrialVaultBlock.VaultState.ACTIVE) {
            if (time % 20 == 0) {
                var particle = (state.getValue(TrialVaultBlock.TYPE) == TrialVaultBlock.VaultType.OMINOUS)
                        ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME;
                addParticles(serverLevel, pos, particle, 0.2);
            }
            if (!entity.hasDisplayItems()) entity.generateDisplayItems(serverLevel, pos, state);
            entity.tickDisplayItem(level, pos, state);
        } else if (vaultState != TrialVaultBlock.VaultState.UNLOCKING && vaultState != TrialVaultBlock.VaultState.EJECTING) {
            if (entity.hasDisplayItems()) {
                entity.displayItems.clear();
                entity.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
        }

        if (entity.isAnimating) entity.handleVaultAnimation(level, pos, state);
        else if (time % 10 == 0) entity.checkNearbyPlayers(level, pos, state);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, TrialVaultBlockEntity entity) {
        if (state.getValue(TrialVaultBlock.STATE) == TrialVaultBlock.VaultState.ACTIVE)
            entity.tickDisplayItem(level, pos, state);
    }

    private void tickDisplayItem(Level level, BlockPos pos, BlockState state) {
        if (displayItems.isEmpty()) return;
        itemRotation = (itemRotation + NTrialsModCommonConfig.vaultItemRotationSpeed) % 360.0f;
        if (++displayItemSwitchTick >= NTrialsModCommonConfig.vaultDisplayItemSwitchInterval) {
            displayItemSwitchTick = 0;
            currentDisplayItemIndex = (currentDisplayItemIndex + 1) % displayItems.size();
            if (level != null && !level.isClientSide)
                level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    private void generateDisplayItems(ServerLevel level, BlockPos pos, BlockState state) {
        String path = this.lootTable.isEmpty()
                ? ((TrialVaultBlock) state.getBlock()).getDefaultLootTable(state)
                : this.lootTable;
        try {
            LootTable table = level.getServer().getLootData().getLootTable(ResourceLocation.parse(path));
            this.displayItems = table.getRandomItems(new LootParams.Builder(level)
                    .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                    .create(LootContextParamSets.CHEST));
            setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        } catch (Exception ignored) {}
    }

    private static void addParticles(ServerLevel sl, BlockPos pos,
                                     net.minecraft.core.particles.ParticleOptions particle, double yOffset) {
        sl.sendParticles(particle, pos.getX() + 0.5, pos.getY() + yOffset,
                pos.getZ() + 0.5, 2, 0.2, 0.1, 0.2, 0.02);
    }

    private void handleVaultAnimation(Level level, BlockPos pos, BlockState state) {
        animationTick++;
        TrialVaultBlock.VaultState currentState = state.getValue(TrialVaultBlock.STATE);

        if (animationTick == NTrialsModCommonConfig.vaultUnlockingDuration
                && currentState == TrialVaultBlock.VaultState.UNLOCKING) {
            level.playSound(null, pos, NTrialsModSounds.BLOCK_VAULT_OPEN_SHUTTER.get(),
                    SoundSource.BLOCKS, 1.0f, 1.0f);
            level.setBlock(pos, state.setValue(TrialVaultBlock.STATE, TrialVaultBlock.VaultState.EJECTING),
                    Block.UPDATE_ALL);
        } else if (currentState == TrialVaultBlock.VaultState.EJECTING) {
            if ((animationTick - NTrialsModCommonConfig.vaultUnlockingDuration)
                    / NTrialsModCommonConfig.vaultEjectInterval > lootDropIndex
                    && lootDropIndex < pendingLoot.size()) {
                ItemEntity drop = new ItemEntity(level,
                        pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                        pendingLoot.get(lootDropIndex).copy());
                drop.setDeltaMovement(0.0, 0.2, 0.0);
                level.addFreshEntity(drop);
                level.playSound(null, pos, NTrialsModSounds.BLOCK_VAULT_EJECT_ITEM.get(),
                        SoundSource.BLOCKS, 1.0f, 1.0f);
                lootDropIndex++;
            }
            if (lootDropIndex >= pendingLoot.size()
                    && animationTick >= (NTrialsModCommonConfig.vaultUnlockingDuration
                    + pendingLoot.size() * NTrialsModCommonConfig.vaultEjectInterval + NTrialsModCommonConfig.vaultCloseDelay)) {
                this.isAnimating = false;
                level.setBlock(pos, state.setValue(TrialVaultBlock.STATE, TrialVaultBlock.VaultState.INACTIVE),
                        Block.UPDATE_ALL);
                level.playSound(null, pos, NTrialsModSounds.BLOCK_VAULT_CLOSE_SHUTTER.get(),
                        SoundSource.BLOCKS, 1.0f, 1.0f);
            }
        }
    }

    private void checkNearbyPlayers(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;
        boolean hasUnopenedPlayer = level.getEntitiesOfClass(Player.class, new AABB(pos).inflate(4.5))
                .stream().anyMatch(p -> !hasPlayerOpened(p.getUUID()));
        TrialVaultBlock.VaultState targetState = hasUnopenedPlayer
                ? TrialVaultBlock.VaultState.ACTIVE
                : TrialVaultBlock.VaultState.INACTIVE;

        if (state.getValue(TrialVaultBlock.STATE) != targetState) {
            BlockState newState = state.setValue(TrialVaultBlock.STATE, targetState);
            level.setBlock(pos, newState, Block.UPDATE_ALL);
            level.sendBlockUpdated(pos, state, newState, 3);
            level.playSound(null, pos,
                    targetState == TrialVaultBlock.VaultState.ACTIVE
                            ? NTrialsModSounds.BLOCK_VAULT_ACTIVATE.get()
                            : NTrialsModSounds.BLOCK_VAULT_DEACTIVATE.get(),
                    SoundSource.BLOCKS, 1.0f, 1.0f);
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        ListTag playersTag = new ListTag();
        for (UUID uuid : playersWhoOpened) playersTag.add(StringTag.valueOf(uuid.toString()));
        tag.put("PlayersWhoOpened", playersTag);
        tag.putString("VaultTag", vaultTag);
        tag.putString("LootTable", lootTable);
        ListTag displayItemsTag = new ListTag();
        for (ItemStack stack : displayItems) {
            CompoundTag itemTag = new CompoundTag();
            stack.save(itemTag);
            displayItemsTag.add(itemTag);
        }
        tag.put("DisplayItems", displayItemsTag);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        playersWhoOpened.clear();

        ListTag playersTag = tag.getList("PlayersWhoOpened", 8);
        for (int i = 0; i < playersTag.size(); i++) {
            try { playersWhoOpened.add(UUID.fromString(playersTag.getString(i))); }
            catch (Exception ignored) {}
        }

        this.vaultTag = tag.getString("VaultTag");
        this.lootTable = tag.getString("LootTable");

        this.displayItems.clear();
        ListTag displayItemsTag = tag.getList("DisplayItems", 10);
        for (int i = 0; i < displayItemsTag.size(); i++)
            displayItems.add(ItemStack.of(displayItemsTag.getCompound(i)));

        if (tag.contains("CurrentDisplayItemIndex"))
            this.currentDisplayItemIndex = tag.getInt("CurrentDisplayItemIndex");
        if (tag.contains("ItemRotation"))
            this.itemRotation = tag.getFloat("ItemRotation");
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        load(tag);
    }

    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener>
    getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }
}
