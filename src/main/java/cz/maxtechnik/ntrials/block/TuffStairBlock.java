package cz.maxtechnik.ntrials.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;
@SuppressWarnings("deprecation")
public class TuffStairBlock extends StairBlock{
	@SuppressWarnings("deprecation")
	public TuffStairBlock(){
		super(Blocks.AIR.defaultBlockState(),Properties.of().sound(SoundType.TUFF).strength(1.5f,6).requiresCorrectToolForDrops().mapColor(MapColor.COLOR_GRAY));
	}
	@Override
	public float getExplosionResistance(){
		return 6f;
	}
	@Override
	public int getLightBlock(@NotNull BlockState state,@NotNull BlockGetter worldIn,@NotNull BlockPos pos){
		return 0;
	}
}
