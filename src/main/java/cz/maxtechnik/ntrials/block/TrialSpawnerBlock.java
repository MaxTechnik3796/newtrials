package cz.maxtechnik.ntrials.block;

import cz.maxtechnik.ntrials.block.entity.TrialSpawnerBlockEntity;
import cz.maxtechnik.ntrials.init.NTrialsModBlockEntities;
import cz.maxtechnik.ntrials.network.NetworkHandler;
import cz.maxtechnik.ntrials.network.TrialSpawnerSyncPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import cz.maxtechnik.ntrials.init.NTrialsModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class TrialSpawnerBlock extends BaseEntityBlock {
    public static final BooleanProperty OMINOUS = BooleanProperty.create("ominous");
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");
    public static final EnumProperty<TrialSpawnerState> STATE = EnumProperty.create("trial_spawner_state", TrialSpawnerState.class);

    public TrialSpawnerBlock() {
        super(Properties.of().sound(SoundType.METAL).strength(20F, 999999999F).noOcclusion().mapColor(MapColor.COLOR_BLACK).isRedstoneConductor((bs, br, bp) -> false).noLootTable().pushReaction(PushReaction.BLOCK));
        this.registerDefaultState(this.stateDefinition.any().setValue(OMINOUS, false).setValue(POWERED, false).setValue(STATE, TrialSpawnerState.INACTIVE));
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        ItemStack itemStack = player.getItemInHand(hand);
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (blockEntity instanceof TrialSpawnerBlockEntity trialSpawner) {
            // 1. Logika pro Spawn Egg (zachována)
            if (itemStack.getItem() instanceof SpawnEggItem spawnEggItem) {
                // Only allow spawn-egg changes when player is in Creative (instabuild)
                if (!level.isClientSide) {
                    if (!player.getAbilities().instabuild) {
                        player.displayClientMessage(Component.literal("You must be in Creative to change the spawner with a spawn egg."), true);
                        level.playSound(null, pos, cz.maxtechnik.ntrials.init.NTrialsModSounds.BLOCK_VAULT_REJECT_REWARDED_PLAYER.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
                        return InteractionResult.FAIL;
                    }

                    EntityType<?> entityType = spawnEggItem.getType(itemStack.getTag());
                    trialSpawner.setSpawnEntity(entityType);

                    if (level instanceof ServerLevel serverLevel) {
                        TrialSpawnerSyncPacket packet = new TrialSpawnerSyncPacket(pos, entityType);
                        NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() ->
                                serverLevel.getChunkAt(pos)), packet);
                    }

                    // creative players don't consume the egg; non-creative case is prevented above
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.sidedSuccess(true);
            }

            // 2. Logika pro klíče - umožní nastavit tag, který pak bude potřeba pro tagované klíče
            if (itemStack.getItem() == NTrialsModItems.TRIAL_KEY.get() || itemStack.getItem() == NTrialsModItems.OMINOUS_TRIAL_KEY.get() || itemStack.getItem() == NTrialsModItems.BOSS_TRIAL_KEY.get()) {
                if (!level.isClientSide) {
                    CompoundTag itemTag = itemStack.getTag();
                    String keyTag = itemTag != null ? itemTag.getString("vault_tag") : "";
                    trialSpawner.setVaultTag(keyTag);
                    trialSpawner.setChanged();
                    player.displayClientMessage(Component.literal("Spawner tag set."), true);
                    if (!player.isCreative()) itemStack.shrink(1);
                }
                return InteractionResult.SUCCESS;
            }

            // 3. Logika pro zobrazení cooldownu (NOVÉ)
            if (trialSpawner.getCooldownTime() > 0) {
                if (!level.isClientSide) {
                    int totalSeconds = trialSpawner.getCooldownTime() / 20;
                    long minutes = (totalSeconds % 3600) / 60;
                    long seconds = totalSeconds % 60;

                    String timeString = String.format("%02d:%02d", minutes, seconds);
                    // True = action bar (text nad inventářem), False = chat
                    player.displayClientMessage(Component.literal("Cooldown " + timeString), true);
                }
                // Zabrání další interakci, ale nepřehraje zvuk bloku (jen máchnutí rukou)
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new TrialSpawnerBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) {
            CompoundTag tag = stack.getTagElement("BlockEntityTag");
            if (tag == null) tag = stack.getTag();
            if (tag != null) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof TrialSpawnerBlockEntity spawnerEntity) {
                    // Accept both "SpawnEntity" and "spawn_entity" naming
                    String entityName = null;
                    if (tag.contains("SpawnEntity")) entityName = tag.getString("SpawnEntity");
                    else if (tag.contains("spawn_entity")) entityName = tag.getString("spawn_entity");
                    if (entityName != null && !entityName.isEmpty()) {
                                            if (tag.contains("vault_tag")) spawnerEntity.setVaultTag(tag.getString("vault_tag"));
                        try {
                            ResourceLocation rl = ResourceLocation.parse(entityName);
                            EntityType<?> type = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getValue(rl);
                            if (type != null) spawnerEntity.setSpawnEntity(type);
                        } catch (Exception ignored) {}
                    }

                    // Accept loot tables using various key names
                    if (tag.contains("NormalLootTable")) spawnerEntity.setNormalLootTable(tag.getString("NormalLootTable"));
                    if (tag.contains("normal_loot_table")) spawnerEntity.setNormalLootTable(tag.getString("normal_loot_table"));
                    if (tag.contains("OminousLootTable")) spawnerEntity.setOminousLootTable(tag.getString("OminousLootTable"));
                    if (tag.contains("ominous_loot_table")) spawnerEntity.setOminousLootTable(tag.getString("ominous_loot_table"));
                }
            }
        }
        // Initialize redstone state when block is placed
        if (!level.isClientSide) {
            BlockState blockState = level.getBlockState(pos);
            boolean isPowered = level.hasNeighborSignal(pos);
            if (isPowered != blockState.getValue(POWERED)) {
                level.setBlock(pos, blockState.setValue(POWERED, isPowered), 3);
            }
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof TrialSpawnerBlockEntity spawnerEntity) {
                spawnerEntity.setRedstoneEnabled(!isPowered);
            }
        }
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, NTrialsModBlockEntities.TRIAL_SPAWNER_BLOCK_ENTITY.get(),
                level.isClientSide()
                        ? (level1, pos, state1, blockEntity) -> blockEntity.clientTick()
                        : (level1, pos, state1, blockEntity) -> blockEntity.tick());
    }

    public enum TrialSpawnerState implements net.minecraft.util.StringRepresentable {
        INACTIVE("inactive"),
        COOLDOWN("cooldown"),
        EJECTING_REWARD("ejecting_reward"),
        ACTIVE("active"),
        WAITING_FOR_PLAYERS("waiting_for_players");
        private final String name;

        TrialSpawnerState(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return this.name;
        }
    }

    @Override
    public void neighborChanged(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Block blockIn, @NotNull BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            boolean isPowered = level.hasNeighborSignal(pos);
            if (isPowered != state.getValue(POWERED)) {
                level.setBlock(pos, state.setValue(POWERED, isPowered), 3);
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof TrialSpawnerBlockEntity spawnerEntity) {
                    spawnerEntity.setRedstoneEnabled(!isPowered);
                }
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OMINOUS, POWERED, STATE);
    }

    @Override
    public int getLightBlock(@NotNull BlockState state, @NotNull BlockGetter worldIn, @NotNull BlockPos pos) {
        return 0;
    }
}