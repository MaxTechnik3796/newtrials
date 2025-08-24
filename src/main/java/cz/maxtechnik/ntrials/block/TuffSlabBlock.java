package cz.maxtechnik.ntrials.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;

public class TuffSlabBlock extends SlabBlock{
    public TuffSlabBlock(){
        super(Properties.of().sound(SoundType.TUFF).strength(1.5f,6f).requiresCorrectToolForDrops().mapColor(MapColor.COLOR_GRAY));
    }
    @Override
    public int getLightBlock(@NotNull BlockState state,@NotNull BlockGetter worldIn,@NotNull BlockPos pos){
        return 0;
    }
}
