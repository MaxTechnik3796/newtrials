package cz.maxtechnik.ntrials.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;

public class TuffBlock extends Block{
    public TuffBlock(){
        super(Properties.of().sound(SoundType.TUFF).strength(1.5f,6f).requiresCorrectToolForDrops().mapColor(MapColor.COLOR_GRAY));
    }
    @Override
    public boolean propagatesSkylightDown(@NotNull BlockState state,@NotNull BlockGetter reader,@NotNull BlockPos pos){
        return true;
    }
    @Override
    public int getLightBlock(@NotNull BlockState state,@NotNull BlockGetter worldIn,@NotNull BlockPos pos){
        return 0;
    }
}
