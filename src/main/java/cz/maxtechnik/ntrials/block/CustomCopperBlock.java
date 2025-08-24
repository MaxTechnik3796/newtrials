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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import cz.maxtechnik.ntrials.NTrialsModEvents;
import net.minecraft.core.particles.ParticleTypes;

public class CustomCopperBlock extends Block implements WeatheringCopper {
    private final WeatherState level;

    public CustomCopperBlock(WeatherState level, BlockBehaviour.Properties props) {
        super(props);
        this.level = level;
    }

    @Override
    public WeatherState getAge() {
        return this.level;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        // Blok môže oxidovať iba ak nie je na najvyššom stupni oxidácie (OXIDIZED)
        return this.getAge() != WeatherState.OXIDIZED;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack itemInHand = player.getItemInHand(hand);

        // Honeycomb interakcia - waxovanie (výmena za waxed verziu)
        if (itemInHand.is(Items.HONEYCOMB)) {
            Block waxedBlock = NTrialsModEvents.WAXING_MAP.get(this);
            if (waxedBlock != null) {
                if (!level.isClientSide) {
                    level.setBlock(pos, waxedBlock.defaultBlockState(), 3);
                    level.playSound(null, pos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1.0F, 1.0F);
                    if (level instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.WAX_ON, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 10, 0.2, 0.2, 0.2, 0.01);
                    }
                    if (!player.isCreative()) {
                        itemInHand.shrink(1);
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        // Sekera interakcia - scraping (čištění oxidace o jeden stupeň zpět)
        if (itemInHand.getItem() instanceof AxeItem) {
            Block scrapedBlock = NTrialsModEvents.SCRAPING_MAP.get(this);
            if (scrapedBlock != null) { // Null znamená že je to první fáze (nelze čistit dál)
                if (!level.isClientSide) {
                    level.setBlock(pos, scrapedBlock.defaultBlockState(), 3);
                    level.playSound(null, pos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0F, 1.0F);
                    // Poškodenie nástroja
                    itemInHand.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
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
        // Vanilla oxidácia má pravděpodobnosť približne 1/17.6 na každý random tick
        if (random.nextFloat() < 0.05688889f) {
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
                level.setBlockAndUpdate(pos, nextBlock.defaultBlockState());
            }
        }
    }
}