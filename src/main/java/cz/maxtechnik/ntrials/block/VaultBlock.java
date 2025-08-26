package cz.maxtechnik.ntrials.block;

import cz.maxtechnik.ntrials.block.entity.VaultBlockEntity;
import cz.maxtechnik.ntrials.init.NTrialsModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.item.Items;
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
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VaultBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty OMINOUS=BooleanProperty.create("ominous");
    public static final EnumProperty<VaultState>STATE=EnumProperty.create("vault_state",VaultState.class);
    public VaultBlock(){
        super(Properties.of().sound(SoundType.METAL).strength(-1,3600000).noOcclusion().mapColor(MapColor.COLOR_BLACK).isRedstoneConductor((bs,br,bp)->false).noLootTable());
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(OMINOUS,false).setValue(STATE,VaultState.INACTIVE));
    }
    public enum VaultState implements net.minecraft.util.StringRepresentable{
        INACTIVE("inactive"),
        EJECTING("ejecting"),
        ACTIVE("active"),
        UNLOCKING("unlocking");
        private final String name;
        VaultState(String name){
            this.name=name;
        }
        @Override
        public @NotNull String getSerializedName(){
            return this.name;
        }
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
        builder.add(OMINOUS,STATE,FACING);
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context){
        return this.defaultBlockState().setValue(FACING,context.getHorizontalDirection().getOpposite());
    }
    public @NotNull BlockState rotate(BlockState state,Rotation rot){
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }
    public @NotNull BlockState mirror(BlockState state,Mirror mirrorIn){
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }
    @Override
    public int getLightBlock(@NotNull BlockState state,@NotNull BlockGetter worldIn,@NotNull BlockPos pos){
        return 0;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack heldItem = player.getItemInHand(hand);

        // Zkontroluje, zda má blok block entity a zda hráč již otevřel tento vault
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof VaultBlockEntity vaultEntity) {
                if (vaultEntity.hasPlayerOpened(player.getUUID())) {
                    // Hráč již otevřel tento vault
                    player.displayClientMessage(Component.literal("Tento vault už jsi otevřel!"), true);
                    return InteractionResult.FAIL;
                }
            }
        }
        //normal
        if (!state.getValue(OMINOUS)) {
            if (heldItem.getItem() == NTrialsModItems.TRIAL_KEY.get()) {
                if (!level.isClientSide) {
                    // Označí hráče jako toho, kdo otevřel vault
                    BlockEntity blockEntity = level.getBlockEntity(pos);
                    if (blockEntity instanceof VaultBlockEntity vaultEntity) {
                        vaultEntity.addPlayerWhoOpened(player.getUUID());
                    }

                    // Získání LootTable (např. desert pyramid chest)
                    ResourceLocation lootTableId = ResourceLocation.fromNamespaceAndPath("ntrials", "vaults/normal");
                    LootTable lootTable = level.getServer().getLootData().getLootTable(lootTableId);

                    // Kontext – kdo otevřel, kde, atd.
                    LootParams.Builder params = new LootParams.Builder((ServerLevel) level)
                            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                            .withParameter(LootContextParams.THIS_ENTITY, player)
                            .withLuck(player.getLuck());

                    // Vygenerování dropů
                    List<ItemStack> loot = lootTable.getRandomItems(params.create(LootContextParamSets.CHEST));

                    BlockState newState = state.setValue(STATE, VaultState.EJECTING);
                    level.setBlock(pos, newState, Block.UPDATE_ALL);

                    // Vyhození itemů do světa
                    if (!player.getAbilities().instabuild) {
                        heldItem.shrink(1);
                    }

                    for (ItemStack stack : loot) {
                        ItemEntity drop = new ItemEntity(
                                level,
                                pos.getX() + 0.5,
                                pos.getY() + 1,
                                pos.getZ() + 0.5,
                                stack
                        );
                        level.addFreshEntity(drop);
                    }
                }

                return InteractionResult.SUCCESS;
            }

        //ominous
        } else {
            if (heldItem.getItem() == NTrialsModItems.OMINOUS_TRIAL_KEY.get()) {
                if (!level.isClientSide) {
                    // Označí hráče jako toho, kdo otevřel vault
                    BlockEntity blockEntity = level.getBlockEntity(pos);
                    if (blockEntity instanceof VaultBlockEntity vaultEntity) {
                        vaultEntity.addPlayerWhoOpened(player.getUUID());
                    }

                    // Získání LootTable (např. desert pyramid chest)
                    ResourceLocation lootTableId = ResourceLocation.fromNamespaceAndPath("ntrials", "vaults/onimous");
                    LootTable lootTable = level.getServer().getLootData().getLootTable(lootTableId);

                    // Kontext – kdo otevřel, kde, atd.
                    LootParams.Builder params = new LootParams.Builder((ServerLevel) level)
                            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                            .withParameter(LootContextParams.THIS_ENTITY, player)
                            .withLuck(player.getLuck());

                    // Vygenerování dropů
                    List<ItemStack> loot = lootTable.getRandomItems(params.create(LootContextParamSets.CHEST));

                    // Vyhození itemů do světa
                    BlockState newState = state.setValue(STATE, VaultState.EJECTING);
                    level.setBlock(pos, newState, Block.UPDATE_ALL);

                    if (!player.getAbilities().instabuild) {
                        heldItem.shrink(1);
                    }

                    for (ItemStack stack : loot) {
                        ItemEntity drop = new ItemEntity(
                                level,
                                pos.getX() + 0.5,
                                pos.getY() + 1,
                                pos.getZ() + 0.5,
                                stack
                        );
                        level.addFreshEntity(drop);
                    }
                }

                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, cz.maxtechnik.ntrials.init.NTrialsModBlockEntities.VAULT_BLOCK_ENTITY.get(), VaultBlock::serverTick);
    }

    private static void serverTick(Level level, BlockPos pos, BlockState state, VaultBlockEntity vaultEntity) {

        checkNearbyPlayers(level, pos, vaultEntity,state);

    }

    private static void checkNearbyPlayers(Level level, BlockPos pos, VaultBlockEntity vaultEntity, BlockState state) {
        // Zkontroluje všechny hráče v okolí 5 bloků
        double range = 5.0;
        net.minecraft.world.phys.AABB searchArea = new net.minecraft.world.phys.AABB(
            pos.getX() - range, pos.getY() - range, pos.getZ() - range,
            pos.getX() + range, pos.getY() + range, pos.getZ() + range
        );

        List<Player> players = level.getEntitiesOfClass(Player.class, searchArea);
        long currentTime = System.currentTimeMillis();
        int playerCount = players.size();
        if (playerCount == 0) {
            // Žádní hráči v okolí, nastaví vault na INACTIVE
            BlockState newState = state.setValue(STATE, VaultState.INACTIVE);
            level.setBlock(pos, newState, Block.UPDATE_ALL);
            return;
        }

        for (Player nearbyPlayer : players) {
            // Pokud hráč ještě neotevřel vault a měla by se zobrazit zpráva
            if (!vaultEntity.hasPlayerOpened(nearbyPlayer.getUUID()) {
                BlockState newState = state.setValue(STATE, VaultState.ACTIVE);
                level.setBlock(pos, newState, Block.UPDATE_ALL);

            }
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VaultBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
