package cz.maxtechnik.ntrials.block;

import cz.maxtechnik.ntrials.block.entity.TrialSpawnerBlockEntity;
import cz.maxtechnik.ntrials.init.NTrialsModBlockEntities;
import cz.maxtechnik.ntrials.network.NetworkHandler;
import cz.maxtechnik.ntrials.network.TrialSpawnerSyncPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
@SuppressWarnings("deprecation")
public class TrialSpawnerBlock extends BaseEntityBlock{
    public static final BooleanProperty OMINOUS=BooleanProperty.create("ominous");
    public static final EnumProperty<TrialSpawnerState>STATE=EnumProperty.create("trial_spawner_state",TrialSpawnerState.class);

    public TrialSpawnerBlock(){
        super(Properties.of().sound(SoundType.METAL).strength(2F,999999999F).noOcclusion().mapColor(MapColor.COLOR_BLACK).isRedstoneConductor((bs,br,bp)->false).noLootTable().pushReaction(PushReaction.BLOCK));
        this.registerDefaultState(this.stateDefinition.any().setValue(OMINOUS,false).setValue(STATE,TrialSpawnerState.INACTIVE));
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (itemStack.getItem() instanceof SpawnEggItem spawnEggItem) {
            if (!level.isClientSide) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof TrialSpawnerBlockEntity trialSpawner) {
                    EntityType<?> entityType = spawnEggItem.getType(itemStack.getTag());
                    trialSpawner.setSpawnEntity(entityType);

                    // Synchronize to all players in range
                    if (level instanceof ServerLevel serverLevel) {
                        TrialSpawnerSyncPacket packet = new TrialSpawnerSyncPacket(pos, entityType);
                        NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() ->
                            serverLevel.getChunkAt(pos)), packet);
                    }

                    if (!player.isCreative()) {
                        itemStack.shrink(1);
                    }

                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new TrialSpawnerBlockEntity(pos, state);
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
        WAITING_FOR_REWARD_EJECTION("waiting_for_reward_ejection"),
        ACTIVE("active"),
        WAITING_FOR_PLAYERS("waiting_for_players");
        private final String name;
        TrialSpawnerState(String name){
            this.name=name;
        }
        @Override
        public @NotNull String getSerializedName(){
            return this.name;
        }
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
        builder.add(OMINOUS,STATE);
    }
    @Override
    public int getLightBlock(@NotNull BlockState state,@NotNull BlockGetter worldIn,@NotNull BlockPos pos){
        return 0;
    }
}
