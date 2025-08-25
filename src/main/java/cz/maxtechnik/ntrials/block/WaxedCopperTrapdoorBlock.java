package cz.maxtechnik.ntrials.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import org.jetbrains.annotations.NotNull;

public class WaxedCopperTrapdoorBlock extends TrapDoorBlock {
    public WaxedCopperTrapdoorBlock(Properties props){
        super(props,BlockSetType.OAK);
    }
    @Override
    public int getLightBlock(@NotNull BlockState state,@NotNull BlockGetter worldIn,@NotNull BlockPos pos){
        return 0;
    }
}
