package cz.maxtechnik.ntrials.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import cz.maxtechnik.ntrials.NTrialsModEvents;

public class CustomCopperBlock extends Block implements WeatheringCopper {
    private final WeatherState level;

    // Přidáváme BlockState property pro waxování
    public static final BooleanProperty WAXED = BooleanProperty.create("waxed");

    public CustomCopperBlock(WeatherState level, BlockBehaviour.Properties props) {
        super(props);
        this.level = level;
        // Nastavujeme default hodnotu waxed na false
        this.registerDefaultState(this.stateDefinition.any().setValue(WAXED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WAXED);
    }

    @Override
    public WeatherState getAge() {
        return this.level;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        // Blok môže oxidovať iba ak nie je na najvyššom stupni oxidácie (OXIDIZED) A nie je waxovaný
        return this.getAge() != WeatherState.OXIDIZED && !state.getValue(WAXED);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack itemInHand = player.getItemInHand(hand);

        // Honeycomb interakcia - waxovanie
        if (itemInHand.is(Items.HONEYCOMB) && !state.getValue(WAXED)) {
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(WAXED, true), 3);
                level.playSound(null, pos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1.0F, 1.0F);
                if (!player.isCreative()) {
                    itemInHand.shrink(1);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        // Sekera interakcia - unwaxovanie
        if (itemInHand.getItem() instanceof AxeItem && state.getValue(WAXED)) {
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(WAXED, false), 3);
                level.playSound(null, pos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0F, 1.0F);
                // Poškodenie nástroja
                itemInHand.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return super.use(state, level, pos, player, hand, hit);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel serverLevel, BlockPos pos, RandomSource random) {
        // Používame vanilla Minecraft logiku pro oxidáciu
        this.changeOverTime(state, serverLevel, pos, random);
    }

    // Implementujeme vlastní changeOverTime metódu s vanilla logikou
    private void changeOverTime(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Kontrola či blok nie je waxovaný - ak je, oxidácia sa nepokračuje
        if (state.getValue(WAXED)) {
            return;
        }

        // Vanilla oxidácia má 1/16 šancu na každý random tick
        if (random.nextFloat() < 0.05688889f) { // 1/17.6 približne ako vanilla
            this.tryOxidize(state, level, pos, random);
        }
    }

    private void tryOxidize(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int nearbyOxidizedBlocks = 0;

        // Kontrolujeme 4x4x4 oblasť okolo bloku (vanilla logika)
        for (BlockPos nearbyPos : BlockPos.betweenClosed(pos.offset(-2, -2, -2), pos.offset(2, 2, 2))) {
            if (nearbyPos.distManhattan(pos) <= 4) {
                BlockState nearbyState = level.getBlockState(nearbyPos);
                Block nearbyBlock = nearbyState.getBlock();

                // Počítame oxidované bloky v okolí
                if (nearbyBlock instanceof WeatheringCopper copper) {
                    WeatherState nearbyAge = copper.getAge();
                    if (nearbyAge == WeatherState.OXIDIZED) {
                        nearbyOxidizedBlocks++;
                    }
                }
            }
        }

        // Výpočet šance na oxidáciu na základe okolia (vanilla logika)
        float oxidationChance = (nearbyOxidizedBlocks + 1) / 64.0f;

        if (random.nextFloat() < oxidationChance) {
            Block nextBlock = NTrialsModEvents.OXIDATION_LEVEL_INCREASES.get(this);
            if (nextBlock != null) {
                // Zachováváme waxed stav pri oxidácii
                BlockState newState = nextBlock.defaultBlockState();
                if (nextBlock instanceof CustomCopperBlock) {
                    newState = newState.setValue(WAXED, state.getValue(WAXED));
                }
                level.setBlockAndUpdate(pos, newState);
            }
        }
    }
}