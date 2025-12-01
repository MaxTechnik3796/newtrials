package cz.maxtechnik.ntrials.block;

import cz.maxtechnik.ntrials.block.entity.TrialSpawnerBossBlockEntity;
import cz.maxtechnik.ntrials.init.NTrialsModBlockEntities;
import cz.maxtechnik.ntrials.init.NTrialsModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class TrialSpawnerBossBlock extends BaseEntityBlock {
    public static final EnumProperty<TrialSpawnerBlock.TrialSpawnerState> STATE = EnumProperty.create("trial_spawner_state", TrialSpawnerBlock.TrialSpawnerState.class);

    public TrialSpawnerBossBlock() {
        super(Properties.of().sound(SoundType.METAL).strength(45F, 999999999F).noOcclusion().mapColor(MapColor.COLOR_BLACK).isRedstoneConductor((bs, br, bp) -> false).noLootTable().pushReaction(PushReaction.BLOCK));
        this.registerDefaultState(this.stateDefinition.any().setValue(STATE, TrialSpawnerBlock.TrialSpawnerState.INACTIVE));
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        ItemStack itemStack = player.getItemInHand(hand);
        boolean isHoldingKey = itemStack.getItem() == cz.maxtechnik.ntrials.init.NTrialsModItems.OMINOUS_TRIAL_KEY.get();

        // Allow changing mob with spawn egg only if player is in creative (instabuild) — otherwise spawner must be set via BlockEntityTag (/give)
        if (itemStack.getItem() instanceof SpawnEggItem spawnEggItem) {
            if (!level.isClientSide) {
                if (!player.getAbilities().instabuild) {
                    // Not allowed for survival players
                    player.displayClientMessage(Component.literal("You must be in Creative to change a boss spawner with a spawn egg."), true);
                    level.playSound(null, pos, NTrialsModSounds.BLOCK_VAULT_REJECT_REWARDED_PLAYER.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
                    return InteractionResult.FAIL;
                }
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof TrialSpawnerBossBlockEntity bossSpawner) {
                    EntityType<?> entityType = spawnEggItem.getType(itemStack.getTag());
                    bossSpawner.setBossMobType(entityType);
                    bossSpawner.setChanged();
                    player.displayClientMessage(Component.literal("Boss mob set from spawn egg (Creative)."), true);
                }
            }
            return InteractionResult.SUCCESS;
        }

        if (!level.isClientSide()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof TrialSpawnerBossBlockEntity bossSpawner) {
                // If the placed item has BlockEntityTag or top-level NBT with settings, apply them on use
                // (We still accept block NBT when using the item in the world; for placement we handle setPlacedBy below.)
                // Support for setting key tag via BlockEntityTag or item NBT (for /give)
                CompoundTag tag = itemStack.getTagElement("BlockEntityTag");
                if (tag == null) tag = itemStack.getTag();
                if (tag != null) {
                    if (tag.contains("BossMobType")) {
                        try {
                            String mobId = tag.getString("BossMobType");
                            EntityType<?> type = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.parse(mobId));
                            if (type != null) bossSpawner.setBossMobType(type);
                        } catch (Exception ignored) {}
                    }
                    if (tag.contains("key_tag")) {
                        bossSpawner.setKeyTag(tag.getString("key_tag"));
                    }
                }
    // Add keyTag field and getter/setter
    // (This is a new field for supporting tagged keys)

                // 1. KONTROLA COOLDOWNU
                if (bossSpawner.getCooldownTimer() > 0) {
                    if (bossSpawner.hasPlayerReceivedReward(player.getUUID())) {
                        // Hráč už má odměnu + je cooldown
                        player.displayClientMessage(Component.literal("Cooldown: (" + formatTime(bossSpawner.getCooldownTimer()) + "). Boss already defeated. Find another Trial Chamber."), true);
                    } else {
                        // Hráč ještě nemá odměnu, ale musí čekat na cooldown
                        player.displayClientMessage(Component.literal("Cooldown: " + formatTime(bossSpawner.getCooldownTimer())), true);
                    }
                    level.playSound(null, pos, NTrialsModSounds.BLOCK_VAULT_REJECT_REWARDED_PLAYER.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
                    return InteractionResult.FAIL;
                }

                // 2. KONTROLA ODMĚNY (pokud není cooldown)
                if (bossSpawner.hasPlayerReceivedReward(player.getUUID())) {
                    player.displayClientMessage(Component.literal("Boss already defeated. Find another Trial Chamber."), true);
                    level.playSound(null, pos, NTrialsModSounds.BLOCK_VAULT_REJECT_REWARDED_PLAYER.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
                    return InteractionResult.FAIL;
                }

                // 3. LOGIKA INTERAKCE S KLÍČEM VS BEZ KLÍČE
                if (isHoldingKey) {
                    // --- Hráč DRŽÍ KLÍČ ---
                    if (bossSpawner.isActivated() && !bossSpawner.isKeyActivated()) {
                        bossSpawner.activateWithKey();
                        if (!player.getAbilities().instabuild) itemStack.shrink(1);
                        return InteractionResult.SUCCESS;
                    }
                    // Pokud hráč drží klíč, ale spawner nejde aktivovat (špatný stav),
                    // vrátíme FAIL, ale nevypisujeme zprávu ani nehrajeme zvuk.

                } else {
                    // --- Hráč NEDRŽÍ KLÍČ ---
                    // Pouze v tomto případě vypíšeme, že potřebuje klíč a přehrajeme zvuk.
                    player.displayClientMessage(Component.literal("Need Ominous Key"), true);
                    level.playSound(null, pos, NTrialsModSounds.BLOCK_VAULT_REJECT_REWARDED_PLAYER.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
                }
                return InteractionResult.FAIL;
            }
        }

        return InteractionResult.FAIL;
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new TrialSpawnerBossBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) {
            CompoundTag tag = stack.getTagElement("BlockEntityTag");
            if (tag == null) tag = stack.getTag();
            if (tag != null) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof TrialSpawnerBossBlockEntity spawnerEntity) {
                    if (tag.contains("BossMobType")) {
                        try {
                            String mobId = tag.getString("BossMobType");
                            EntityType<?> type = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.parse(mobId));
                            if (type != null) spawnerEntity.setBossMobType(type);
                        } catch (Exception ignored) {}
                    }
                    if (tag.contains("key_tag")) spawnerEntity.setKeyTag(tag.getString("key_tag"));
                }
            }
        }
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, NTrialsModBlockEntities.TRIAL_SPAWNER_BOSS_BLOCK_ENTITY.get(),
                level.isClientSide ? (level1, pos, state1, blockEntity) -> blockEntity.clientTick()
                        : (level1, pos, state1, blockEntity) -> blockEntity.tick());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STATE);
    }

    @Override
    public int getLightBlock(@NotNull BlockState state, @NotNull BlockGetter worldIn, @NotNull BlockPos pos) {
        return 0;
    }

    // Pomocná metoda pro formátování času
    private String formatTime(int ticks) {
        int seconds = ticks / 20;
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;

        if (minutes > 0) {
            return String.format("%d min %d s", minutes, remainingSeconds);
        } else {
            return String.format("%d s", remainingSeconds);
        }
    }
}