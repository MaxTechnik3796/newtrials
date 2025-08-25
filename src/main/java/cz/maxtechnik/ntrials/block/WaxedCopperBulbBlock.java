package cz.maxtechnik.ntrials.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

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
}
