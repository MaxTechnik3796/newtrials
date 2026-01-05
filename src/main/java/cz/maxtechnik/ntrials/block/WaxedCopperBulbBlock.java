package cz.maxtechnik.ntrials.block;

import cz.maxtechnik.ntrials.NTrialsModEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
@SuppressWarnings("deprecation")
public class WaxedCopperBulbBlock extends Block{
	private final WeatheringCopper.WeatherState weatheringLevel;
	public static final BooleanProperty LIT=BooleanProperty.create("lit");
	public static final BooleanProperty POWERED=BooleanProperty.create("powered");
	public WaxedCopperBulbBlock(WeatheringCopper.WeatherState weatheringLevel,Properties props){
		super(props);
		this.weatheringLevel=weatheringLevel;
		this.registerDefaultState(this.stateDefinition.any().setValue(LIT,false).setValue(POWERED,false));
	}
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
		builder.add(LIT);
		builder.add(POWERED);
	}
	@Override
	public boolean hasAnalogOutputSignal(@NotNull BlockState state){
		return true;
	}
	@Override
	public int getAnalogOutputSignal(BlockState state,@NotNull Level level,@NotNull BlockPos pos){
		return state.getValue(LIT)?15:0;
	}
	@Override
	public void onPlace(@NotNull BlockState state,@NotNull Level level,@NotNull BlockPos pos,@NotNull BlockState oldState,boolean isMoving){
		super.onPlace(state,level,pos,oldState,isMoving);
		if(!level.isClientSide){
			boolean powered=level.hasNeighborSignal(pos);
			if(state.getValue(POWERED)!=powered){
				BlockState newState=state.setValue(POWERED,powered);
				if(powered){
					newState=newState.setValue(LIT,!state.getValue(LIT));
				}
				level.setBlock(pos,newState,3);
			}
		}
	}
	// WeatheringCopper interface method
	@Override
	public int getLightEmission(BlockState state,BlockGetter level,BlockPos pos){
		if(state.getValue(LIT)){
			return switch(this.weatheringLevel){
				case UNAFFECTED -> 15;
				case EXPOSED -> 12;
				case WEATHERED -> 8;
				case OXIDIZED -> 4;
			};
		}else{
			return 0;
		}
	}
	@Override
	public void neighborChanged(@NotNull BlockState state,@NotNull Level level,@NotNull BlockPos pos,@NotNull Block neighborBlock,@NotNull BlockPos neighborPos,boolean isMoving){
		super.neighborChanged(state,level,pos,neighborBlock,neighborPos,isMoving);
		if(!level.isClientSide){
			boolean powered=level.hasNeighborSignal(pos);
			if(state.getValue(POWERED)!=powered){
				BlockState new_state=state;
				new_state=new_state.setValue(POWERED,powered);
				if(powered){
					if(state.getValue(LIT)){
						new_state=new_state.setValue(LIT,false);
					}else{
						new_state=new_state.setValue(LIT,true);
					}
				}
				level.setBlock(pos,new_state,3);
			}
		}
	}
	@Override
	public @NotNull InteractionResult use(@NotNull BlockState state,@NotNull Level level,@NotNull BlockPos pos,Player player,@NotNull InteractionHand hand,@NotNull BlockHitResult hit){
		ItemStack itemInHand=player.getItemInHand(hand);
		// Sekera interakcia - unwaxovanie (výmena za non-waxed verziu)
		if(itemInHand.getItem() instanceof AxeItem){
			Block unwaxedBlock=NTrialsModEvents.UNWAXING_MAP.get(this);
			if(unwaxedBlock!=null){
				if(!level.isClientSide){
					BlockState new_state=unwaxedBlock.defaultBlockState();
					new_state=new_state.setValue(LIT,state.getValue(LIT));
					new_state=new_state.setValue(POWERED,state.getValue(POWERED));
					level.setBlock(pos,new_state,3);
					CopperUtil.play(level,pos,SoundEvents.AXE_WAX_OFF);
					CopperUtil.spawnParticles(level,pos,ParticleTypes.WAX_OFF);
					CopperUtil.damageToolIfNotCreative(itemInHand,player,hand);
				}
				return InteractionResult.sidedSuccess(level.isClientSide);
			}
		}
		return super.use(state,level,pos,player,hand,hit);
	}
}
