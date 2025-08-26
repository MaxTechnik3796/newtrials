package cz.maxtechnik.ntrials.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.NotNull;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;


//main class

public class WaxedCopperBulbBlock extends Block{
    public static final BooleanProperty LIT=BooleanProperty.create("lit");
    public static final BooleanProperty POWERED=BooleanProperty.create("powered");
    public WaxedCopperBulbBlock(Properties props){
        super(props);

        this.registerDefaultState(this.stateDefinition.any().setValue(LIT,false).setValue(POWERED,false));
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>builder){
        builder.add(LIT);
        builder.add(POWERED);
    }
    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return state.getValue(LIT) ? 15 : 0;
    }



    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        if (state.getValue(LIT)) {
            return 15;
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
                BlockState new_state = state;
                new_state = new_state.setValue(POWERED, powered);
                if (powered) {
                    if (state.getValue(LIT)) {
                        new_state = new_state.setValue(LIT, false);
                    } else {
                        new_state = new_state.setValue(LIT, true);
                    }
                }
                level.setBlock(pos, new_state, 3);
            }
        }
    }
}
