package cz.maxtechnik.ntrials.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VaultBlockEntity extends BlockEntity {
    private final Set<UUID> playersWhoOpened = new HashSet<>();

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

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        ListTag playersTag = new ListTag();
        for (UUID uuid : playersWhoOpened) {
            playersTag.add(StringTag.valueOf(uuid.toString()));
        }
        tag.put("PlayersWhoOpened", playersTag);
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
    }
}
