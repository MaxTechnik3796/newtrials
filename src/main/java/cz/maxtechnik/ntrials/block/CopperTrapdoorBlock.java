package cz.maxtechnik.ntrials.block;

import cz.maxtechnik.ntrials.NTrialsModEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
@SuppressWarnings("deprecation")
public class CopperTrapdoorBlock extends TrapDoorBlock implements WeatheringCopper{
	private final WeatherState level;
	public CopperTrapdoorBlock(WeatherState level,BlockBehaviour.Properties props){
		super(props,BlockSetType.OAK);
		this.level=level;
	}
	@Override
	public int getLightBlock(@NotNull BlockState state,@NotNull BlockGetter worldIn,@NotNull BlockPos pos){
		return 0;
	}
	@Override
	public @NotNull WeatherState getAge(){
		return this.level;
	}
	@Override
	public boolean isRandomlyTicking(@NotNull BlockState state){
		// Blok môže oxidovať iba ak nie je na najvyššom stupni oxidácie (OXIDIZED)
		return this.getAge()!=WeatherState.OXIDIZED;
	}
	@Override
	public @NotNull InteractionResult use(@NotNull BlockState state,@NotNull Level level,@NotNull BlockPos pos,Player player,@NotNull InteractionHand hand,@NotNull BlockHitResult hit){
		ItemStack itemInHand=player.getItemInHand(hand);
		// Honeycomb interakcia - waxovanie (výmena za waxed verziu)
		if(itemInHand.is(Items.HONEYCOMB)){
			Block waxedBlock=NTrialsModEvents.WAXING_MAP.get(this);
			if(waxedBlock!=null){
				if(!level.isClientSide){
					BlockState stateAtPos=level.getBlockState(pos);
					// Zachováváme orientáciu a stav trapdoor
					BlockState nextState=waxedBlock.defaultBlockState()
							.setValue(FACING,stateAtPos.getValue(FACING))
							.setValue(OPEN,stateAtPos.getValue(OPEN))
							.setValue(HALF,stateAtPos.getValue(HALF))
							.setValue(POWERED,stateAtPos.getValue(POWERED))
							.setValue(WATERLOGGED,stateAtPos.getValue(WATERLOGGED));
					level.setBlock(pos,nextState,3);
					level.playSound(null,pos,SoundEvents.HONEYCOMB_WAX_ON,SoundSource.BLOCKS,1f,1f);
					if(level instanceof ServerLevel serverLevel){
						for(int i=0;i<20;i++){
							double x=pos.getX()-0.2+level.random.nextDouble()*1.4;
							double y=pos.getY()-0.2+level.random.nextDouble()*1.4;
							double z=pos.getZ()-0.2+level.random.nextDouble()*1.4;
							serverLevel.sendParticles(ParticleTypes.WAX_ON,x,y,z,1,0,0,0,0.05);
						}
					}
					if(!player.isCreative()){
						itemInHand.shrink(1);
					}
				}
				return InteractionResult.sidedSuccess(level.isClientSide);
			}
		}
		//axe scraping
		if(itemInHand.getItem() instanceof AxeItem){
			Block scrapedBlock=NTrialsModEvents.SCRAPING_MAP.get(this);
			if(scrapedBlock!=null){ // Null znamená že je to první fáze (nelze čistit dál)
				if(!level.isClientSide){
					BlockState stateAtPos=level.getBlockState(pos);
					// Zachováváme orientáciu a stav trapdoor
					BlockState nextState=scrapedBlock.defaultBlockState()
							.setValue(FACING,stateAtPos.getValue(FACING))
							.setValue(OPEN,stateAtPos.getValue(OPEN))
							.setValue(HALF,stateAtPos.getValue(HALF))
							.setValue(POWERED,stateAtPos.getValue(POWERED))
							.setValue(WATERLOGGED,stateAtPos.getValue(WATERLOGGED));
					level.setBlock(pos,nextState,3);
					level.playSound(null,pos,SoundEvents.AXE_SCRAPE,SoundSource.BLOCKS,1.0f,1.0f);
					if(level instanceof ServerLevel serverLevel){
						for(int i=0;i<20;i++){
							double x=pos.getX()-0.2+level.random.nextDouble()*1.4;
							double y=pos.getY()-0.2+level.random.nextDouble()*1.4;
							double z=pos.getZ()-0.2+level.random.nextDouble()*1.4;
							serverLevel.sendParticles(ParticleTypes.SCRAPE,x,y,z,1,0,0,0,0.05);
						}
					}
					// Poškodenie nástroja
					itemInHand.hurtAndBreak(1,player,(p)->p.broadcastBreakEvent(hand));
				}
				return InteractionResult.sidedSuccess(level.isClientSide);
			}
		}
		return super.use(state,level,pos,player,hand,hit);
	}
	@Override
	public void randomTick(@NotNull BlockState state,@NotNull ServerLevel serverLevel,@NotNull BlockPos pos,@NotNull RandomSource random){
		// Používame vanilla Minecraft logiku pro oxidáciu
		this.changeOverTime(state,serverLevel,pos,random);
	}
	// Implementujeme vlastní changeOverTime metódu s vanilla logikou
	private void changeOverTime(BlockState state,ServerLevel level,BlockPos pos,RandomSource random){
		// Vanilla oxidácia má pravděpodobnosť približne 1/17.6 na každý random tick
		if(random.nextFloat()<0.05688889f){
			this.tryOxidize(state,level,pos,random);
		}
	}
	private void tryOxidize(BlockState state,ServerLevel level,BlockPos pos,RandomSource random){
		int nearbyOxidizedBlocks=0;
		// Kontrolujeme 4x4x4 oblasť okolo bloku (vanilla logika)
		for(BlockPos nearbyPos: BlockPos.betweenClosed(pos.offset(-2,-2,-2),pos.offset(2,2,2))){
			if(nearbyPos.distManhattan(pos)<=4){
				BlockState nearbyState=level.getBlockState(nearbyPos);
				Block nearbyBlock=nearbyState.getBlock();
				// Počítame oxidované bloky v okolí
				if(nearbyBlock instanceof WeatheringCopper copper){
					WeatherState nearbyAge=copper.getAge();
					if(nearbyAge==WeatherState.OXIDIZED){
						nearbyOxidizedBlocks++;
					}
				}
			}
		}
		// Výpočet šance na oxidáciu na základe okolia (vanilla logika)
		float oxidationChance=(nearbyOxidizedBlocks+1)/64f;
		if(random.nextFloat()<oxidationChance){
			Block nextBlock=NTrialsModEvents.OXIDATION_LEVEL_INCREASES.get(this);
			if(nextBlock!=null){
				BlockState nextState=nextBlock.defaultBlockState();
				// Zachováváme orientáciu a stav trapdoor
				nextState=nextState.setValue(FACING,state.getValue(FACING));
				nextState=nextState.setValue(OPEN,state.getValue(OPEN));
				nextState=nextState.setValue(HALF,state.getValue(HALF));
				nextState=nextState.setValue(POWERED,state.getValue(POWERED));
				nextState=nextState.setValue(WATERLOGGED,state.getValue(WATERLOGGED));
				// Aktualizujeme blok na nový oxidovaný stav
				level.setBlockAndUpdate(pos,nextState);
			}
		}
	}
}
