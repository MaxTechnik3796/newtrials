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
    }
}
