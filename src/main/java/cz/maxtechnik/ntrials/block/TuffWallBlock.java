package cz.maxtechnik.ntrials.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;
@SuppressWarnings("deprecation")
public class TuffWallBlock extends WallBlock{
	public TuffWallBlock(){
		super(BlockBehaviour.Properties.of().sound(SoundType.TUFF).strength(1.5f,6f).requiresCorrectToolForDrops().mapColor(MapColor.COLOR_GRAY).noCollission().isRedstoneConductor((bs,br,bp)->false).forceSolidOn());
	}
	@Override
	public int getLightBlock(@NotNull BlockState state,@NotNull BlockGetter worldIn,@NotNull BlockPos pos){
		return 0;
	}
}
