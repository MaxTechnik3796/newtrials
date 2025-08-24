package cz.maxtechnik.ntrials.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import cz.maxtechnik.ntrials.NTrialsModEvents;

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
    public void randomTick(BlockState state, ServerLevel serverLevel, BlockPos pos, RandomSource random) {
        // Používame vanilla Minecraft logiku pro oxidáciu
        this.changeOverTime(state, serverLevel, pos, random);
    }

    // Implementujeme vlastní changeOverTime metódu s vanilla logikou
    private void changeOverTime(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
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
                level.setBlockAndUpdate(pos, nextBlock.defaultBlockState());
            }
        }
    }
}