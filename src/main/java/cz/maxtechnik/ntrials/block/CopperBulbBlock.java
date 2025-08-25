package cz.maxtechnik.ntrials.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.NotNull;

public class CopperBulbBlock extends Block implements WeatheringCopper {
    private final WeatherState level;
    public static final BooleanProperty LIT=BooleanProperty.create("lit");
    public static final BooleanProperty POWERED=BooleanProperty.create("powered");
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>builder){
        builder.add(LIT);
        builder.add(POWERED);
    }
    public CopperBulbBlock(WeatherState level, BlockBehaviour.Properties props){
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT,false).setValue(POWERED,false));
        this.level=level;
    }

    @Override
    public @NotNull WeatherState getAge(){
        return this.level;
    }
}
