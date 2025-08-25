package cz.maxtechnik.ntrials.block;

import cz.maxtechnik.ntrials.NTrialsModEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import org.jetbrains.annotations.NotNull;


public class CopperTrapdoorBlock extends TrapDoorBlock implements WeatheringCopper{
    private final WeatherState level;

    public CopperTrapdoorBlock(WeatherState level, BlockBehaviour.Properties props){
        super(props,BlockSetType.OAK);
        this.level=level;
    }
    //@Override
    //public int getLightBlock(@NotNull BlockState state,@NotNull BlockGetter worldIn,@NotNull BlockPos pos){
    //    return 0;
    //}

    @Override
    public @NotNull WeatherState getAge(){
        return this.level;
    }

    @Override
    public boolean isRandomlyTicking(@NotNull BlockState state){
        // Blok môže oxidovať iba ak nie je na najvyššom stupni oxidácie (OXIDIZED)
        return this.getAge()!= WeatherState.OXIDIZED;
    }

    @Override
    public void randomTick(@NotNull BlockState state,@NotNull ServerLevel serverLevel,@NotNull BlockPos pos,@NotNull RandomSource random){
        // Používame vanilla Minecraft logiku pro oxidáciu
        this.changeOverTime(state,serverLevel,pos,random);
    }
    // Implementujeme vlastní changeOverTime metódu s vanilla logikou
    private void changeOverTime(BlockState state,ServerLevel level,BlockPos pos,RandomSource random){
        // Vanilla oxidácia má pravděpodobnosť približne 1/17.6 na každý random tick
        if(random.nextFloat()< 0.05688889f){
            this.tryOxidize(state,level,pos,random);
        }
    }
    private void tryOxidize(BlockState state,ServerLevel level,BlockPos pos,RandomSource random){
        int nearbyOxidizedBlocks=0;
        // Kontrolujeme 4x4x4 oblasť okolo bloku (vanilla logika)
        for(BlockPos nearbyPos:BlockPos.betweenClosed(pos.offset(-2,-2,-2),pos.offset(2,2,2))){
            if(nearbyPos.distManhattan(pos)<=4){
                BlockState nearbyState=level.getBlockState(nearbyPos);
                Block nearbyBlock=nearbyState.getBlock();
                // Počítame oxidované bloky v okolí
                if(nearbyBlock instanceof WeatheringCopper copper){
                    WeatherState nearbyAge=copper.getAge();
                    if(nearbyAge==WeatherState.OXIDIZED){
                        nearbyOxidizedBlocks++;
                    }
                }
            }
        }
        // Výpočet šance na oxidáciu na základe okolia (vanilla logika)
        float oxidationChance=(nearbyOxidizedBlocks+1)/64f;
        if(random.nextFloat()<oxidationChance){
            Block nextBlock=NTrialsModEvents.OXIDATION_LEVEL_INCREASES.get(this);
            if(nextBlock!=null){
                BlockState nextState=nextBlock.defaultBlockState();
                // Zachováváme orientáciu a stav trapdoor
                nextState=nextState.setValue(FACING,state.getValue(FACING));
                nextState=nextState.setValue(OPEN,state.getValue(OPEN));
                nextState=nextState.setValue(HALF,state.getValue(HALF));
                nextState=nextState.setValue(POWERED,state.getValue(POWERED));
                nextState=nextState.setValue(WATERLOGGED,state.getValue(WATERLOGGED));
                // Aktualizujeme blok na nový oxidovaný stav
                level.setBlockAndUpdate(pos,nextState);
            }
        }
    }

}
