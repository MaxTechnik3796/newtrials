package cz.maxtechnik.ntrials.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;

public class CopperBulbBlock extends Block implements WeatheringCopper {
    private final WeatherState level;

    public CopperBulbBlock(WeatherState level, BlockBehaviour.Properties props){
        super(props);
        this.level=level;
    }

    @Override
    public @NotNull WeatherState getAge(){
        return this.level;
    }

    @Override
    public int getLightBlock(@NotNull BlockState state,@NotNull BlockGetter worldIn,@NotNull BlockPos pos){
        return 0;
    }
}
