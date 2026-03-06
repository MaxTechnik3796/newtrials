package cz.maxtechnik.ntrials.block;

import cz.maxtechnik.ntrials.NTrialsMod;
import cz.maxtechnik.ntrials.block.entity.TrialVaultBlockEntity;
import cz.maxtechnik.ntrials.init.other.NTrialsModBlockEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import cz.maxtechnik.ntrials.init.basic.NTrialsModItems;
import cz.maxtechnik.ntrials.init.basic.NTrialsModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.sounds.SoundSource;

import java.util.Objects;

@SuppressWarnings("deprecation")
public class TrialVaultBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<VaultType> TYPE = EnumProperty.create("vault_type", VaultType.class);
    public static final EnumProperty<VaultState> STATE = EnumProperty.create("vault_state", VaultState.class);
    // Legacy backward-compat property: old vault blocks stored ominous as boolean.
    // On first tick, ominous=true is converted to vault_type=ominous and ominous is reset to false.
    public static final BooleanProperty OMINOUS = BooleanProperty.create("ominous");

    public TrialVaultBlock() {
        super(Properties.of().sound(SoundType.METAL).strength(20F, 999999999F).noOcclusion()
                .mapColor(MapColor.COLOR_BLACK).isRedstoneConductor((bs, br, bp) -> false)
                .noLootTable().pushReaction(PushReaction.BLOCK));
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(TYPE, VaultType.NORMAL)
                .setValue(STATE, VaultState.INACTIVE)
                .setValue(OMINOUS, false));
    }

    public enum VaultType implements net.minecraft.util.StringRepresentable {
        NORMAL("normal"), OMINOUS("ominous"), BOSS("boss");
        private final String name;
        VaultType(String name) { this.name = name; }
        @Override public @NotNull String getSerializedName() { return this.name; }
    }

    public enum VaultState implements net.minecraft.util.StringRepresentable {
        INACTIVE("inactive"), EJECTING("ejecting"), ACTIVE("active"), UNLOCKING("unlocking");
        private final String name;
        VaultState(String name) { this.name = name; }
        @Override public @NotNull String getSerializedName() { return this.name; }
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(TYPE, STATE, FACING, OMINOUS); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) { return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()); }
    public @NotNull BlockState rotate(BlockState state, Rotation rot) { return state.setValue(FACING, rot.rotate(state.getValue(FACING))); }
    @Override public @NotNull BlockState mirror(BlockState state, Mirror mirror) { return state.rotate(mirror.getRotation(state.getValue(FACING))); }
    @Override public int getLightBlock(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) { return 0; }
    @Override public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(STATE) == VaultState.INACTIVE ? 6 : 12;
    }

    public String getDefaultLootTable(BlockState state) {
        return switch (state.getValue(TYPE)) {
            case NORMAL -> "ntrials:chests/reward";
            case OMINOUS -> "ntrials:chests/reward_ominous";
            case BOSS -> "ntrials:chests/reward_boss";
        };
    }

    private Item getRequiredKey(BlockState state) {
        return switch (state.getValue(TYPE)) {
            case NORMAL -> NTrialsModItems.TRIAL_KEY.get();
            case OMINOUS -> NTrialsModItems.OMINOUS_TRIAL_KEY.get();
            case BOSS -> NTrialsModItems.BOSS_TRIAL_KEY.get();
        };
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, Level level, @NotNull BlockPos pos, Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        ItemStack heldItem = player.getItemInHand(hand);

        if (!level.isClientSide && level.getBlockEntity(pos) instanceof TrialVaultBlockEntity vaultEntity) {
            if (vaultEntity.hasPlayerOpened(player.getUUID())) {
                player.displayClientMessage(Component.literal("You Already Opened This Vault."), true);
                return InteractionResult.FAIL;
            }

            if (heldItem.getItem() == getRequiredKey(state) && state.getValue(STATE) == VaultState.ACTIVE) {
                String keyTag = heldItem.hasTag() ? heldItem.getTag().getString("vault_tag") : "";
                if (!((keyTag.isEmpty() && vaultEntity.getVaultTag().isEmpty()) || keyTag.equals(vaultEntity.getVaultTag()))) {
                    level.playSound(null, pos, NTrialsModSounds.BLOCK_VAULT_INSERT_ITEM_FAIL.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
                    return InteractionResult.PASS;
                }

                vaultEntity.addPlayerWhoOpened(player.getUUID());
                String lootPath = vaultEntity.getLootTable().isEmpty() ? getDefaultLootTable(state) : vaultEntity.getLootTable();
                LootTable lootTable = Objects.requireNonNull(level.getServer()).getLootData().getLootTable(ResourceLocation.parse(lootPath));
                LootParams params = new LootParams.Builder((ServerLevel) level)
                        .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                        .withParameter(LootContextParams.THIS_ENTITY, player)
                        .withLuck(player.getLuck()).create(LootContextParamSets.CHEST);

                level.setBlock(pos, state.setValue(STATE, VaultState.UNLOCKING), Block.UPDATE_ALL);
                vaultEntity.startAnimation(lootTable.getRandomItems(params));

                String adv = switch(state.getValue(TYPE)) {
                    case NORMAL -> "vault";
                    case OMINOUS -> "vault_ominous";
                    case BOSS -> "vault_boss";
                };
                NTrialsMod.adv((ServerPlayer) player, ResourceLocation.fromNamespaceAndPath("ntrials", adv));

                level.playSound(null, pos, NTrialsModSounds.BLOCK_VAULT_INSERT_ITEM.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
                if (!player.getAbilities().instabuild) heldItem.shrink(1);
                return InteractionResult.SUCCESS;
            }
        }

        if (!level.isClientSide())
            level.playSound(null, pos, NTrialsModSounds.BLOCK_VAULT_REJECT_REWARDED_PLAYER.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        return createTickerHelper(type, NTrialsModBlockEntities.VAULT_BLOCK_ENTITY.get(),
                level.isClientSide ? TrialVaultBlockEntity::clientTick : TrialVaultBlockEntity::serverTick);
    }

    @Override
    public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) {
            CompoundTag tag = stack.getTagElement("BlockEntityTag");
            if (tag == null) tag = stack.getTag();
            if (tag != null && level.getBlockEntity(pos) instanceof TrialVaultBlockEntity vaultEntity) {
                if (tag.contains("vault_tag")) vaultEntity.setVaultTag(tag.getString("vault_tag"));
                if (tag.contains("loot_table") && !tag.getString("loot_table").isEmpty()) vaultEntity.setLootTable(tag.getString("loot_table"));
            }
        }
    }

    @Nullable @Override public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) { return new TrialVaultBlockEntity(pos, state); }
    @Override public @NotNull RenderShape getRenderShape(@NotNull BlockState state) { return RenderShape.MODEL; }
}
