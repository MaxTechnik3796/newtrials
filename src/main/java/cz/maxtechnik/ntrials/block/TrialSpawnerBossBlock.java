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
        super(Properties.of().sound(SoundType.METAL).strength(45F,999999999F).noOcclusion().mapColor(MapColor.COLOR_BLACK).isRedstoneConductor((bs, br, bp) -> false).noLootTable().pushReaction(PushReaction.BLOCK));
        this.registerDefaultState(this.stateDefinition.any().setValue(STATE, TrialSpawnerBlock.TrialSpawnerState.INACTIVE));
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        ItemStack itemStack = player.getItemInHand(hand);
        boolean isHoldingKey = itemStack.getItem() == cz.maxtechnik.ntrials.init.NTrialsModItems.OMINOUS_TRIAL_KEY.get();

        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof TrialSpawnerBossBlockEntity bossSpawner) {

                // Kontrola cooldownu - platí pro všechny, má přednost
                if (bossSpawner.getCooldownTimer() > 0) {
                    player.displayClientMessage(Component.literal("Trial Spawner is cooling down. Please wait: " + formatTime(bossSpawner.getCooldownTimer())), true);
                    level.playSound(null, pos, NTrialsModSounds.BLOCK_VAULT_REJECT_REWARDED_PLAYER.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
                    return InteractionResult.FAIL;
                }

                // Kontrola, zda hráč již obdržel odměnu (trvalá kontrola - platí pro všechny kliknutí)
                if (bossSpawner.hasPlayerReceivedReward(player.getUUID())) {
                    player.displayClientMessage(Component.literal("Boss already defeated. Find another Trial Chamber."), true);
                    level.playSound(null, pos, NTrialsModSounds.BLOCK_VAULT_REJECT_REWARDED_PLAYER.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
                    return InteractionResult.FAIL;
                }

                if (isHoldingKey) {
                    if (bossSpawner.isActivated() && !bossSpawner.isKeyActivated()) {
                        bossSpawner.activateWithKey();
                        if (!player.getAbilities().instabuild) itemStack.shrink(1);
                        return InteractionResult.SUCCESS;
                    }
                }
            }
        }

        // Zpráva o neúspěchu pouze při kliknutí rukou (bez klíče)
        if (!level.isClientSide && !isHoldingKey) {
            player.displayClientMessage(Component.literal("Need Ominous Key"), true);
            level.playSound(null, pos, NTrialsModSounds.BLOCK_VAULT_REJECT_REWARDED_PLAYER.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
        }
        return InteractionResult.FAIL;
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) { return new TrialSpawnerBossBlockEntity(pos, state); }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) { return RenderShape.MODEL; }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, NTrialsModBlockEntities.TRIAL_SPAWNER_BOSS_BLOCK_ENTITY.get(),
                level.isClientSide ? (level1, pos, state1, blockEntity) -> ((TrialSpawnerBossBlockEntity) blockEntity).clientTick()
                        : (level1, pos, state1, blockEntity) -> ((TrialSpawnerBossBlockEntity) blockEntity).tick());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(STATE); }

    @Override
    public int getLightBlock(@NotNull BlockState state, @NotNull BlockGetter worldIn, @NotNull BlockPos pos) { return 0; }

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