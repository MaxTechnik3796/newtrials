package cz.maxtechnik.ntrials.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class VaultBlockEntity extends BlockEntity {
    private final Set<UUID> playersWhoOpened = new HashSet<>();
    private final Map<UUID, Long> lastMessageTime = new HashMap<>();

    // Animace vault bloku
    private int animationTick = 0;
    private boolean isAnimating = false;
    private List<ItemStack> pendingLoot = new ArrayList<>();
    private int lootDropIndex = 0;

    // Rotující itemy pro zobrazení
    private List<ItemStack> displayItems = new ArrayList<>();
    private int currentDisplayItemIndex = 0;
    private int displayItemSwitchTick = 0;
    private float itemRotation = 0.0f;

    public VaultBlockEntity(BlockPos pos, BlockState blockState) {
        super(cz.maxtechnik.ntrials.init.NTrialsModBlockEntities.VAULT_BLOCK_ENTITY.get(), pos, blockState);
    }

    public boolean hasPlayerOpened(UUID playerUuid) {
        return playersWhoOpened.contains(playerUuid);
    }

    public void addPlayerWhoOpened(UUID playerUuid) {
        playersWhoOpened.add(playerUuid);
        setChanged();
    }

    public boolean shouldShowMessage(UUID playerUuid, long currentTime) {
        // Zobrazí zprávu pouze jednou za 5 sekund (5000ms)
        Long lastTime = lastMessageTime.get(playerUuid);
        if (lastTime == null || currentTime - lastTime > 5000) {
            lastMessageTime.put(playerUuid, currentTime);
            return true;
        }
        return false;
    }

    // Animace metody
    public void startAnimation(List<ItemStack> loot) {
        this.isAnimating = true;
        this.animationTick = 0;
        this.pendingLoot = new ArrayList<>(loot);
        this.lootDropIndex = 0;
        setChanged();
    }

    public void tickAnimation() {
        if (isAnimating) {
            animationTick++;
        }
    }

    public int getAnimationTick() {
        return animationTick;
    }

    public boolean isAnimating() {
        return isAnimating;
    }

    public List<ItemStack> getPendingLoot() {
        return pendingLoot;
    }

    public int getLootDropIndex() {
        return lootDropIndex;
    }

    public void incrementLootDropIndex() {
        lootDropIndex++;
        setChanged();
    }

    public void stopAnimation() {
        this.isAnimating = false;
        this.animationTick = 0;
        this.pendingLoot.clear();
        this.lootDropIndex = 0;
        setChanged();
    }

    // Metody pro rotující zobrazované itemy
    public void setDisplayItems(List<ItemStack> items) {
        System.out.println("DEBUG: VaultBlockEntity.setDisplayItems volána s " + items.size() + " itemy");
        this.displayItems = new ArrayList<>(items);
        this.currentDisplayItemIndex = 0;
        this.displayItemSwitchTick = 0;
        setChanged();
        // Synchronizace na client
        if (level != null && !level.isClientSide) {
            System.out.println("DEBUG: Posílám update packet na client");
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public void clearDisplayItems() {
        System.out.println("DEBUG: VaultBlockEntity.clearDisplayItems volána");
        this.displayItems.clear();
        this.currentDisplayItemIndex = 0;
        this.displayItemSwitchTick = 0;
        setChanged();
        // Synchronizace na client
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public void tickDisplayItem() {
        if (!displayItems.isEmpty()) {
            // Rotace itemu
            itemRotation += 2.0f; // 2 stupně za tick
            if (itemRotation >= 360.0f) {
                itemRotation = 0.0f;
            }

            // Střídání itemů každých 10 ticků (0.5 sekundy)
            displayItemSwitchTick++;
            if (displayItemSwitchTick >= 10) {
                displayItemSwitchTick = 0;
                int oldIndex = currentDisplayItemIndex;
                currentDisplayItemIndex = (currentDisplayItemIndex + 1) % displayItems.size();

                // Synchronizace změny indexu na client (pouze když se index skutečně změní)
                if (oldIndex != currentDisplayItemIndex && level != null && !level.isClientSide) {
                    level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                }
            }
        }
    }

    public ItemStack getCurrentDisplayItem() {
        if (displayItems.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return displayItems.get(currentDisplayItemIndex);
    }

    public float getItemRotation() {
        return itemRotation;
    }

    public boolean hasDisplayItems() {
        return !displayItems.isEmpty();
    }

    // Synchronizace dat mezi serverem a clientem
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

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        ListTag playersTag = new ListTag();
        for (UUID uuid : playersWhoOpened) {
            playersTag.add(StringTag.valueOf(uuid.toString()));
        }
        tag.put("PlayersWhoOpened", playersTag);

        // Uložení animace
        tag.putBoolean("IsAnimating", isAnimating);
        tag.putInt("AnimationTick", animationTick);
        tag.putInt("LootDropIndex", lootDropIndex);

        // Uložení zobrazovaných itemů
        ListTag displayItemsTag = new ListTag();
        for (ItemStack stack : displayItems) {
            CompoundTag itemTag = new CompoundTag();
            stack.save(itemTag);
            displayItemsTag.add(itemTag);
        }
        tag.put("DisplayItems", displayItemsTag);
        tag.putInt("CurrentDisplayItemIndex", currentDisplayItemIndex);
        tag.putFloat("ItemRotation", itemRotation);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        playersWhoOpened.clear();
        ListTag playersTag = tag.getList("PlayersWhoOpened", 8); // 8 = StringTag
        for (int i = 0; i < playersTag.size(); i++) {
            try {
                UUID uuid = UUID.fromString(playersTag.getString(i));
                playersWhoOpened.add(uuid);
            } catch (IllegalArgumentException e) {
                // Invalid UUID, skip
            }
        }

        // Načtení animace
        this.isAnimating = tag.getBoolean("IsAnimating");
        this.animationTick = tag.getInt("AnimationTick");
        this.lootDropIndex = tag.getInt("LootDropIndex");

        // Načtení zobrazovaných itemů
        this.displayItems.clear();
        ListTag displayItemsTag = tag.getList("DisplayItems", 10); // 10 = CompoundTag
        for (int i = 0; i < displayItemsTag.size(); i++) {
            CompoundTag itemTag = displayItemsTag.getCompound(i);
            ItemStack stack = ItemStack.of(itemTag);
            displayItems.add(stack);
        }
        this.currentDisplayItemIndex = tag.getInt("CurrentDisplayItemIndex");
        this.itemRotation = tag.getFloat("ItemRotation");
    }
}
