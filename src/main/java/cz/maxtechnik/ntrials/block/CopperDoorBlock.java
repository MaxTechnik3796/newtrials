package cz.maxtechnik.ntrials.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import cz.maxtechnik.ntrials.NTrialsModEvents;
import org.jetbrains.annotations.NotNull;

public class CopperDoorBlock extends DoorBlock implements WeatheringCopper {
    private final WeatherState level;

    public CopperDoorBlock(WeatherState level, BlockBehaviour.Properties props){
        super(props,BlockSetType.OAK);
        this.level = level;
    }

    @Override
    public @NotNull WeatherState getAge(){
        return this.level;
    }

    @Override
    public boolean isRandomlyTicking(@NotNull BlockState state){
        // Oxidace se spouští POUZE na dolním dílu dveří, ne na horním
        // Tím zabráníme duplicitní oxidaci
        return this.getAge() != WeatherState.OXIDIZED && state.getValue(HALF) == DoubleBlockHalf.LOWER;
    }

    @Override
    public int getLightBlock(@NotNull BlockState state,@NotNull BlockGetter worldIn,@NotNull BlockPos pos){
        return 0;
    }

    @Override
    public void randomTick(@NotNull BlockState state, @NotNull ServerLevel serverLevel, @NotNull BlockPos pos, @NotNull RandomSource random){
        // Používáme vanilla Minecraft logiku pro oxidaci
        this.changeOverTime(state, serverLevel, pos, random);
    }

    // Implementujeme vlastní changeOverTime metodu s vanilla logikou
    private void changeOverTime(BlockState state, ServerLevel level, BlockPos pos, RandomSource random){
        // Vanilla oxidace má pravděpodobnost přibližně 1/17.6 na každý random tick
        if(random.nextFloat() < 0.05688889f){
            this.tryOxidize(state, level, pos, random);
        }
    }

    private void tryOxidize(BlockState state, ServerLevel level, BlockPos pos, RandomSource random){
        // ZAJISTÍME, že oxidace se volá pouze na dolním dílu
        if(state.getValue(HALF) != DoubleBlockHalf.LOWER) {
            return; // Pokud nejsme dolní díl, neděláme nic
        }

        int nearbyOxidizedBlocks = 0;
        // Kontrolujeme 4x4x4 oblast okolo bloku (vanilla logika)
        for(BlockPos nearbyPos : BlockPos.betweenClosed(pos.offset(-2,-2,-2), pos.offset(2,2,2))){
            if(nearbyPos.distManhattan(pos) <= 4){
                BlockState nearbyState = level.getBlockState(nearbyPos);
                Block nearbyBlock = nearbyState.getBlock();
                // Počítáme oxidované bloky v okolí
                if(nearbyBlock instanceof WeatheringCopper copper){
                    WeatherState nearbyAge = copper.getAge();
                    if(nearbyAge == WeatherState.OXIDIZED){
                        nearbyOxidizedBlocks++;
                    }
                }
            }
        }
        // Výpočet šance na oxidaci na základě okolí (vanilla logika)
        float oxidationChance = (nearbyOxidizedBlocks + 1) / 64f;
        if(random.nextFloat() < oxidationChance){
            Block nextBlock = NTrialsModEvents.OXIDATION_LEVEL_INCREASES.get(this);
            if(nextBlock != null){
                // Najdeme horní díl
                BlockPos upperPos = pos.above();
                BlockState upperState = level.getBlockState(upperPos);

                // Zkontrolujeme, že horní díl existuje a je to stejný typ dveří
                if(upperState.getBlock() == this && upperState.getValue(HALF) == DoubleBlockHalf.UPPER){

                    // Připravíme nové stavy pro oba díly (zachováme všechny properties)
                    BlockState newLowerState = nextBlock.defaultBlockState()
                            .setValue(FACING, state.getValue(FACING))
                            .setValue(OPEN, state.getValue(OPEN))
                            .setValue(HINGE, state.getValue(HINGE))
                            .setValue(POWERED, state.getValue(POWERED))
                            .setValue(HALF, DoubleBlockHalf.LOWER);

                    BlockState newUpperState = nextBlock.defaultBlockState()
                            .setValue(FACING, state.getValue(FACING)) // Horní díl má stejný FACING jako dolní
                            .setValue(OPEN, state.getValue(OPEN))
                            .setValue(HINGE, state.getValue(HINGE))
                            .setValue(POWERED, false) // Horní díl nikdy nemá power
                            .setValue(HALF, DoubleBlockHalf.UPPER);

                    // Vyměníme bloky PŘÍMO bez dropu - používáme flag 2 (no drop) + flag 16 (no physics update)
                    level.setBlock(pos, newLowerState, 2 | 16);
                    level.setBlock(upperPos, newUpperState, 2 | 16);

                    // Pošleme update klientům pro oba bloky
                    level.sendBlockUpdated(pos, state, newLowerState, 3);
                    level.sendBlockUpdated(upperPos, upperState, newUpperState, 3);
                }
            }
        }
    }
}
