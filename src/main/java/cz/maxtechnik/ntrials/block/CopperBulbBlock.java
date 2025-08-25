package cz.maxtechnik.ntrials.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.NotNull;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;

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

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        if (state.getValue(LIT)) {
            if (this.level == WeatherState.UNAFFECTED) {
                return 15;
            } else if (this.level == WeatherState.EXPOSED) {
                return 12;
            } else if (this.level == WeatherState.WEATHERED) {
                return 8;
            } else if (this.level == WeatherState.OXIDIZED) {
                return 4;
            } else {
                return 0;
            }
        }
        else {
            return 0;
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, isMoving);
        if (!level.isClientSide) {
            boolean powered = level.hasNeighborSignal(pos);
            if (state.getValue(POWERED) != powered) {
                level.setBlock(pos, state.setValue(POWERED, powered).setValue(LIT, powered), 3);
            }
        }
    }
}
