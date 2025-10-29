package cz.maxtechnik.ntrials.block;

import cz.maxtechnik.ntrials.block.entity.VaultBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import cz.maxtechnik.ntrials.init.NTrialsModItems;
import cz.maxtechnik.ntrials.init.NTrialsModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
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
import net.minecraft.core.particles.ParticleTypes;
import java.util.List;
import java.util.Objects;

public class VaultBlock extends BaseEntityBlock {
    static String defaultLootNormal="ntrials:chests/reward";
    static String defaultLootOminous="ntrials:chests/reward_ominous";
    public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty OMINOUS=BooleanProperty.create("ominous");
    public static final EnumProperty<VaultState>STATE=EnumProperty.create("vault_state",VaultState.class);
    public VaultBlock(){
        super(Properties.of().sound(SoundType.METAL).strength(-1,3600000).noOcclusion().mapColor(MapColor.COLOR_BLACK).isRedstoneConductor((bs,br,bp)->false).noLootTable().pushReaction(PushReaction.BLOCK));
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
    public @NotNull InteractionResult use(@NotNull BlockState state,Level level,@NotNull BlockPos pos,Player player,@NotNull InteractionHand hand,@NotNull BlockHitResult hit){
        ItemStack heldItem = player.getItemInHand(hand);

        // Zkontroluje, zda má blok block entity a zda hráč již otevřel tento vault
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof VaultBlockEntity vaultEntity) {
                if (vaultEntity.hasPlayerOpened(player.getUUID())) {
                    // Hráč již otevřel tento vault
                    player.displayClientMessage(Component.literal("You Already Opened This Vault."), true);
                    return InteractionResult.FAIL;
                }
            }
        }
        //normal
        if (!state.getValue(OMINOUS)) {
            if (heldItem.getItem() == NTrialsModItems.TRIAL_KEY.get() && state.getValue(STATE) == VaultState.ACTIVE) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof VaultBlockEntity vaultEntity) {
                    String vaultTag = vaultEntity.getVaultTag();
                    CompoundTag itemTag = heldItem.getTag();
                    String keyTag = itemTag != null ? itemTag.getString("vault_tag") : "";
                    if (!((keyTag.isEmpty() && vaultTag.isEmpty()) || keyTag.equals(vaultTag))) {
                        if (!level.isClientSide()) level.playSound(null, pos, NTrialsModSounds.BLOCK_VAULT_INSERT_ITEM_FAIL.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
                        return InteractionResult.PASS;
                    }

                    if (!level.isClientSide) {
                        // Označí hráče jako toho, kdo otevřel vault
                        vaultEntity.addPlayerWhoOpened(player.getUUID());

                        // Získání LootTable
                        String lootPath = vaultEntity.getLootTable().isEmpty() ? defaultLootNormal : vaultEntity.getLootTable();
                        String modId=removeSuffix(lootPath);
                        String path=removePrefix(lootPath);
                        ResourceLocation lootTableId = ResourceLocation.fromNamespaceAndPath(modId,path);
                        LootTable lootTable = Objects.requireNonNull(level.getServer()).getLootData().getLootTable(lootTableId);

                        // Kontext – kdo otevřel, kde, atd.
                        LootParams.Builder params = new LootParams.Builder((ServerLevel) level)
                                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                                .withParameter(LootContextParams.THIS_ENTITY, player)
                                .withLuck(player.getLuck());

                        // Vygenerování dropů
                        List<ItemStack> loot = lootTable.getRandomItems(params.create(LootContextParamSets.CHEST));

                        // Začne animaci - nastaví na UNLOCKING a uloží loot
                        BlockState newState = state.setValue(STATE, VaultState.UNLOCKING);
                        level.setBlock(pos, newState, Block.UPDATE_ALL);

                        vaultEntity.startAnimation(loot);
                    }

                    if (!level.isClientSide()) level.playSound(null, pos, NTrialsModSounds.BLOCK_VAULT_INSERT_ITEM.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
                    // Spotřebuje klíč
                    if (!player.getAbilities().instabuild) {
                        heldItem.shrink(1);
                    }
                }

                return InteractionResult.SUCCESS;
            }

        //ominous
        } else {
            if (heldItem.getItem() == NTrialsModItems.OMINOUS_TRIAL_KEY.get()  && state.getValue(STATE) == VaultState.ACTIVE) {
                if (!level.isClientSide) {
                    // Označí hráče jako toho, kdo otevřel vault
                    BlockEntity blockEntity = level.getBlockEntity(pos);
                    if (blockEntity instanceof VaultBlockEntity vaultEntity) {
                        vaultEntity.addPlayerWhoOpened(player.getUUID());
                        String vaultTag = vaultEntity.getVaultTag();
                        CompoundTag itemTag = heldItem.getTag();
                        String keyTag = itemTag != null ? itemTag.getString("vault_tag") : "";
                        if (!((keyTag.isEmpty() && vaultTag.isEmpty()) || keyTag.equals(vaultTag))) {
                            if (!level.isClientSide()) level.playSound(null, pos, NTrialsModSounds.BLOCK_VAULT_INSERT_ITEM_FAIL.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
                            return InteractionResult.PASS;
                        }

                        // Získání LootTable
                        String lootPath = vaultEntity.getLootTable().isEmpty() ? defaultLootOminous : vaultEntity.getLootTable();
                        String modId=removeSuffix(lootPath);
                        String path=removePrefix(lootPath);
                        ResourceLocation lootTableId = ResourceLocation.fromNamespaceAndPath(modId,path);
                        LootTable lootTable = Objects.requireNonNull(level.getServer()).getLootData().getLootTable(lootTableId);

                        // Kontext – kdo otevřel, kde, atd.
                        LootParams.Builder params = new LootParams.Builder((ServerLevel) level)
                                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                                .withParameter(LootContextParams.THIS_ENTITY, player)
                                .withLuck(player.getLuck());

                        // Vygenerování dropů
                        List<ItemStack> loot = lootTable.getRandomItems(params.create(LootContextParamSets.CHEST));

                        // Začne animaci - nastaví na UNLOCKING a uloží loot
                        BlockState newState = state.setValue(STATE, VaultState.UNLOCKING);
                        level.setBlock(pos, newState, Block.UPDATE_ALL);

                        vaultEntity.startAnimation(loot);
                    }
                    if (!level.isClientSide()) level.playSound(null, pos, NTrialsModSounds.BLOCK_VAULT_INSERT_ITEM.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
                    // Spotřebuje klíč
                    if (!player.getAbilities().instabuild) {
                        heldItem.shrink(1);
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }
        if (!level.isClientSide()) level.playSound(null, pos, NTrialsModSounds.BLOCK_VAULT_INSERT_ITEM_FAIL.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, cz.maxtechnik.ntrials.init.NTrialsModBlockEntities.VAULT_BLOCK_ENTITY.get(),
            level.isClientSide ? VaultBlock::clientTick : VaultBlock::serverTick);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        VaultState vaultState = state.getValue(STATE);
        if (vaultState == VaultState.INACTIVE) {
            return 6;
        } else {
            return 12;
        }
    }



    private static void serverTick(Level level, BlockPos pos, BlockState state, VaultBlockEntity vaultEntity) {

        if (!level.isClientSide && level.getGameTime() % 5 == 0) {
            addSmokeParticles(level, pos);
        }
        if (!level.isClientSide && state.getValue(STATE) == VaultState.ACTIVE && level.getGameTime() % 10 == 0) {
            addFireParticles(level, pos, state);
        }

        // Pokud je vault ACTIVE a ještě nemá zobrazované itemy, vygeneruje je z loot table
        if (state.getValue(STATE) == VaultState.ACTIVE && !vaultEntity.hasDisplayItems()) {
            generateDisplayItems(level, pos, state, vaultEntity);
        }

        // Pokud vault není ACTIVE, vymaže zobrazované itemy
        if (state.getValue(STATE) != VaultState.ACTIVE && vaultEntity.hasDisplayItems()) {
            vaultEntity.clearDisplayItems();
        }

        // Aktualizuje rotaci zobrazovaných itemů pokud je vault ACTIVE
        if (state.getValue(STATE) == VaultState.ACTIVE) {
            vaultEntity.tickDisplayItem();
        }

        // Pokud je vault v animaci, zpracovává animaci
        if (vaultEntity.isAnimating()) {
            handleVaultAnimation(level, pos, state, vaultEntity);
        } else {
            // Běžná kontrola hráčů v okolí
            checkNearbyPlayers(level, pos, vaultEntity, state);
        }

    }

    private static void clientTick(Level level, BlockPos pos, BlockState state, VaultBlockEntity vaultEntity) {
        // Client-side pouze tickuje rotaci zobrazovaných itemů
        if (state.getValue(STATE) == VaultState.ACTIVE) {
            vaultEntity.tickDisplayItem();
        }
    }
    public static String removePrefix(String text){
        int index=text.indexOf(':');
        return(index!=-1)?text.substring(index+1):text;
    }
    public static String removeSuffix(String text){
        int index=text.indexOf(':');
        if (index!=-1){
            return text.substring(0,index);
        }else{
            return"";
        }
    }





    private static void generateDisplayItems(Level level, BlockPos pos, BlockState state, VaultBlockEntity vaultEntity) {
        if (level instanceof ServerLevel serverLevel) {
            // Určí správnou loot table podle toho, zda je vault ominous nebo ne
            String lootPath=vaultEntity.getLootTable().isEmpty() ? defaultLootNormal : vaultEntity.getLootTable();;
            if (state.getValue(OMINOUS)) {
                lootPath = vaultEntity.getLootTable().isEmpty() ? defaultLootOminous : vaultEntity.getLootTable();
            }
            String modId=removeSuffix(lootPath);
            String path=removePrefix(lootPath);
            ResourceLocation lootTableId = ResourceLocation.fromNamespaceAndPath(modId,path);
            System.out.println("DEBUG: Generuji display items pro vault na pozici " + pos + ", loot table: " + lootTableId);

            LootTable lootTable = serverLevel.getServer().getLootData().getLootTable(lootTableId);

            // Vygeneruje vzorky itemů z loot table pro zobrazení
            LootParams.Builder params = new LootParams.Builder(serverLevel)
                    .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                    .withLuck(0.0f);

            List<ItemStack> displayLoot = lootTable.getRandomItems(params.create(LootContextParamSets.CHEST));

            System.out.println("DEBUG: Vygenerováno " + displayLoot.size() + " itemů z loot table");
            for (ItemStack stack : displayLoot) {
                System.out.println("DEBUG: Item: " + stack.getItem().getDescriptionId() + " x" + stack.getCount());
            }

            // Nastaví zobrazované itemy
            vaultEntity.setDisplayItems(displayLoot);
            System.out.println("DEBUG: Display items nastaveny");
        }
    }

    private static void addSmokeParticles(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            RandomSource random = level.random;

            // Generuje 2-3 particles každý tick
            for (int i = 0; i < 2 + random.nextInt(2); i++) {
                double x = pos.getX() + 0.3 + random.nextDouble() * 0.4;
                double y = pos.getY() + 0.8 + random.nextDouble() * 0.3;
                double z = pos.getZ() + 0.3 + random.nextDouble() * 0.4;

                double velocityX = (random.nextDouble() - 0.5) * 0.02;
                double velocityY = random.nextDouble() * 0.05 + 0.02;
                double velocityZ = (random.nextDouble() - 0.5) * 0.02;

                // Pošle particles všem hráčům v okolí
                serverLevel.sendParticles(
                        ParticleTypes.SMOKE,
                        x, y, z,
                        1, // počet particles
                        velocityX, velocityY, velocityZ,
                        0.0 // rychlost
                );
            }
        }
    }

    private static void addFireParticles(Level level, BlockPos pos, BlockState state) {
        if (level instanceof ServerLevel serverLevel) {
            RandomSource random = level.random;

            // Generuje 2-3 particles každý tick
            for (int i = 0; i < 2 + random.nextInt(2); i++) {
                double x = pos.getX() + 0.3 + random.nextDouble() * 0.4;
                double y = pos.getY() + 0.2 + random.nextDouble() * 0.3;
                double z = pos.getZ() + 0.3 + random.nextDouble() * 0.4;

                double velocityX = (random.nextDouble() - 0.5) * 0.02;
                double velocityY = random.nextDouble() * 0.05 + 0.02;
                double velocityZ = (random.nextDouble() - 0.5) * 0.02;

                if (!state.getValue(OMINOUS)) {
                // Pošle particles všem hráčům v okolí
                serverLevel.sendParticles(
                        ParticleTypes.FLAME,
                        x, y, z,
                        1, // počet particles
                        velocityX, velocityY, velocityZ,
                        0.0 // rychlost
                ); } else {
                    serverLevel.sendParticles(
                            ParticleTypes.SOUL_FIRE_FLAME,
                            x, y, z,
                            1, // počet particles
                            velocityX, velocityY, velocityZ,
                            0.0 // rychlost
                    );
                }
            }
        }
    }

    private static void handleVaultAnimation(Level level, BlockPos pos, BlockState state, VaultBlockEntity vaultEntity) {
        vaultEntity.tickAnimation();
        int tick = vaultEntity.getAnimationTick();
        VaultState currentState = state.getValue(STATE);

        // Fáze 1: UNLOCKING (0-10 ticků)
        if (tick == 10 && currentState == VaultState.UNLOCKING) {
            // Po 10 tickách přejde na EJECTING
            if (!level.isClientSide()) level.playSound(null, pos, NTrialsModSounds.BLOCK_VAULT_OPEN_SHUTTER.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
            BlockState newState = state.setValue(STATE, VaultState.EJECTING);
            level.setBlock(pos, newState, Block.UPDATE_ALL);
            return;
        }


        // Fáze 2: EJECTING - postupné dropování každých 20 ticků
        if (currentState == VaultState.EJECTING && tick > 10) {
            // Počítá kolik itemů už bylo vyhozeno
            int dropPhase = (tick - 10) / 20; // První drop na tick 30, pak každých 20 ticků
            List<ItemStack> loot = vaultEntity.getPendingLoot();
            int currentDropIndex = vaultEntity.getLootDropIndex();

            if (dropPhase > currentDropIndex && currentDropIndex < loot.size()) {
                // Vyhodí další item
                ItemStack stack = loot.get(currentDropIndex);
                ItemEntity drop = new ItemEntity(
                    level,
                    pos.getX() + 0.5,
                    pos.getY() + 1.0,
                    pos.getZ() + 0.5,
                    stack.copy()
                );
                if (!level.isClientSide()) level.playSound(null, pos, NTrialsModSounds.BLOCK_VAULT_EJECT_ITEM.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
                drop.setDeltaMovement(0.0, 0.15, 0.0); // X, Y, Z rychlost
                drop.setPickUpDelay(10);
                level.addFreshEntity(drop);
                vaultEntity.incrementLootDropIndex();
            }

            // Pokud byly všechny itemy vyhozeny, ukončí animaci a nastaví na INACTIVE
            if (currentDropIndex >= loot.size()) {
                // Počká 20 ticků (1 sekundu) před zavřením
                if (tick >= (10 + loot.size() * 20 + 20)) {
                    vaultEntity.stopAnimation();
                    BlockState newState = state.setValue(STATE, VaultState.INACTIVE);
                    level.setBlock(pos, newState, Block.UPDATE_ALL);
                    if (!level.isClientSide()) level.playSound(null, pos, NTrialsModSounds.BLOCK_VAULT_CLOSE_SHUTTER.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
                }
            }
        }
    }


    private static void checkNearbyPlayers(Level level, BlockPos pos, VaultBlockEntity vaultEntity, BlockState state) {
        // Zkontroluje všechny hráče v okolí 5 bloků pouze pokud vault není v animaci
        if (vaultEntity.isAnimating()) {
            return; // Během animace nespouští kontrolu hráčů
        }

        double range = 4.0;
        net.minecraft.world.phys.AABB searchArea = new net.minecraft.world.phys.AABB(
            pos.getX() - range, pos.getY() - range, pos.getZ() - range,
            pos.getX() + range, pos.getY() + range, pos.getZ() + range
        );

        List<Player> players = level.getEntitiesOfClass(Player.class, searchArea);
        long currentTime = System.currentTimeMillis();
        int playerCount = players.size();

        if (playerCount == 0) {
            // Žádní hráči v okolí, nastaví vault na INACTIVE
            if (state.getValue(STATE) != VaultState.INACTIVE) {
                BlockState newState = state.setValue(STATE, VaultState.INACTIVE);
                level.setBlock(pos, newState, Block.UPDATE_ALL);
                if (!level.isClientSide()) level.playSound(null, pos, NTrialsModSounds.BLOCK_VAULT_DEACTIVATE.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
            }
            return;
        }

        for (Player nearbyPlayer : players) {
            // Pokud hráč ještě neotevřel vault, zobrazí zprávu a nastaví vault na ACTIVE
            if (!vaultEntity.hasPlayerOpened(nearbyPlayer.getUUID())) {

                if (state.getValue(STATE) != VaultState.ACTIVE) {
                    BlockState newState = state.setValue(STATE, VaultState.ACTIVE);
                    level.setBlock(pos, newState, Block.UPDATE_ALL);
                    if (!level.isClientSide()) level.playSound(null, pos, NTrialsModSounds.BLOCK_VAULT_ACTIVATE.get(), SoundSource.BLOCKS, 1.0f, 1.0f);

                }
                return; // Našli jsme nepřipraveného hráče, nemusíme pokračovat
            }
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new VaultBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && placer instanceof Player player) {
            CompoundTag tag = stack.getTag();
            if (tag != null) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof VaultBlockEntity vaultEntity) {
                    if (tag.contains("vault_tag")) {
                        String vaultTag = tag.getString("vault_tag");
                        vaultEntity.setVaultTag(vaultTag);
                    }
                    if (tag.contains("loot_table")) {
                        String lootTable = tag.getString("loot_table");
                        vaultEntity.setLootTable(lootTable);
                    }
                }
            }
        }
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state){
        return RenderShape.MODEL;
    }
}
