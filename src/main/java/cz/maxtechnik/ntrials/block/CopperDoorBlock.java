package cz.maxtechnik.ntrials.block;

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
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import cz.maxtechnik.ntrials.NTrialsModEvents;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
@SuppressWarnings("deprecation")
public class CopperDoorBlock extends DoorBlock implements WeatheringCopper{
	private final WeatherState level;
	public CopperDoorBlock(WeatherState level,BlockBehaviour.Properties props){
		super(props,new BlockSetType("copper",true,SoundType.COPPER,SoundEvents.IRON_DOOR_CLOSE,SoundEvents.IRON_DOOR_OPEN,SoundEvents.IRON_TRAPDOOR_CLOSE,SoundEvents.IRON_TRAPDOOR_OPEN,SoundEvents.METAL_PRESSURE_PLATE_CLICK_OFF,SoundEvents.METAL_PRESSURE_PLATE_CLICK_ON,SoundEvents.STONE_BUTTON_CLICK_OFF,SoundEvents.STONE_BUTTON_CLICK_ON));
		this.level=level;
	}
	@Override
	public @NotNull WeatherState getAge(){
		return this.level;
	}
	@Override
	public boolean isRandomlyTicking(@NotNull BlockState state){
		// Oxidace se spouští POUZE na dolním dílu dveří, ne na horním
		// Tím zabráníme duplicitní oxidaci
		return this.getAge()!=WeatherState.OXIDIZED&&state.getValue(HALF)==DoubleBlockHalf.LOWER;
	}
	@Override
	public @NotNull InteractionResult use(@NotNull BlockState state,@NotNull Level level,@NotNull BlockPos pos,Player player,@NotNull InteractionHand hand,@NotNull BlockHitResult hit){
		ItemStack stack=player.getItemInHand(hand);
		// Honeycomb waxing - POUZE když držíme shift
		if(stack.is(Items.HONEYCOMB)){
			Block waxedBlock=NTrialsModEvents.WAXING_MAP.get(this);
			if(waxedBlock!=null){
				if(!level.isClientSide){
					// Inspirované tryOxidize funkcí - zpracování obou dílů dveří současně
					BlockPos otherPos;
					BlockState otherState;
					// Najdeme druhý díl dveří
					if(state.getValue(HALF)==DoubleBlockHalf.LOWER){
						// Jsme dolní díl, druhý díl je nahoře
						otherPos=pos.above();
					}else{
						// Jsme horní díl, druhý díl je dole
						otherPos=pos.below();
					}
					otherState=level.getBlockState(otherPos);
					// Zkontrolujeme, že druhý díl je stejný typ dveří
					if(otherState.getBlock()==this){
						// Připravíme nové stavy pro oba díly (zachováme všechny properties)
						BlockState newState1=waxedBlock.defaultBlockState()
								.setValue(FACING,state.getValue(FACING))
								.setValue(OPEN,state.getValue(OPEN))
								.setValue(HINGE,state.getValue(HINGE))
								.setValue(POWERED,state.getValue(POWERED))
								.setValue(HALF,state.getValue(HALF));
						BlockState newState2=waxedBlock.defaultBlockState()
								.setValue(FACING,otherState.getValue(FACING))
								.setValue(OPEN,otherState.getValue(OPEN))
								.setValue(HINGE,otherState.getValue(HINGE))
								.setValue(POWERED,otherState.getValue(POWERED))
								.setValue(HALF,otherState.getValue(HALF));
						// Vyměníme bloky PŘÍMO bez dropu - používáme flag 2|16 (no drop + no physics update)
						level.setBlock(pos,newState1,2|16);
						level.setBlock(otherPos,newState2,2|16);
						level.sendBlockUpdated(pos,state,newState1,3);
						level.sendBlockUpdated(otherPos,otherState,newState2,3);
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
							stack.shrink(1);
						}
					}
				}
				return InteractionResult.sidedSuccess(level.isClientSide);
			}
		}
		// Axe scraping - POUZE když držíme shift
		if(stack.getItem() instanceof AxeItem){
			Block scrapedBlock=NTrialsModEvents.SCRAPING_MAP.get(this);
			if(scrapedBlock!=null){
				if(!level.isClientSide){
					// Inspirované tryOxidize funkcí - zpracování obou dílů dveří současně
					BlockPos otherPos;
					BlockState otherState;
					// Najdeme druhý díl dveří
					if(state.getValue(HALF)==DoubleBlockHalf.LOWER){
						// Jsme dolní díl, druhý díl je nahoře
						otherPos=pos.above();
					}else{
						// Jsme horní díl, druhý díl je dole
						otherPos=pos.below();
					}
					otherState=level.getBlockState(otherPos);
					// Zkontrolujeme, že druhý díl je stejný typ dveří
					if(otherState.getBlock()==this){
						// Připravíme nové stavy pro oba díly (zachováme všechny properties)
						BlockState newState1=scrapedBlock.defaultBlockState()
								.setValue(FACING,state.getValue(FACING))
								.setValue(OPEN,state.getValue(OPEN))
								.setValue(HINGE,state.getValue(HINGE))
								.setValue(POWERED,state.getValue(POWERED))
								.setValue(HALF,state.getValue(HALF));
						BlockState newState2=scrapedBlock.defaultBlockState()
								.setValue(FACING,otherState.getValue(FACING))
								.setValue(OPEN,otherState.getValue(OPEN))
								.setValue(HINGE,otherState.getValue(HINGE))
								.setValue(POWERED,otherState.getValue(POWERED))
								.setValue(HALF,otherState.getValue(HALF));
						// Vyměníme bloky PŘÍMO bez dropu - používáme flag 2|16 (no drop + no physics update)
						level.setBlock(pos,newState1,2|16);
						level.setBlock(otherPos,newState2,2|16);
						level.sendBlockUpdated(pos,state,newState1,3);
						level.sendBlockUpdated(otherPos,otherState,newState2,3);
						level.playSound(null,pos,SoundEvents.AXE_SCRAPE,SoundSource.BLOCKS,1.0f,1.0f);
						if(level instanceof ServerLevel serverLevel){
							for(int i=0;i<20;i++){
								double x=pos.getX()-0.2+level.random.nextDouble()*1.4;
								double y=pos.getY()-0.2+level.random.nextDouble()*1.4;
								double z=pos.getZ()-0.2+level.random.nextDouble()*1.4;
								serverLevel.sendParticles(ParticleTypes.SCRAPE,x,y,z,1,0,0,0,0.05);
							}
						}
						// Poškození nástroje
						stack.hurtAndBreak(1,player,(p)->p.broadcastBreakEvent(hand));
					}
				}
				return InteractionResult.sidedSuccess(level.isClientSide);
			}
		}
		// Pokud není shift+honeycomb ani shift+axe, použij normální chování dveří
		return super.use(state,level,pos,player,hand,hit);
	}
	@Override
	public int getLightBlock(@NotNull BlockState state,@NotNull BlockGetter worldIn,@NotNull BlockPos pos){
		return 0;
	}
	@Override
	public void randomTick(@NotNull BlockState state,@NotNull ServerLevel serverLevel,@NotNull BlockPos pos,@NotNull RandomSource random){
		// Používáme vanilla Minecraft logiku pro oxidaci
		this.changeOverTime(state,serverLevel,pos,random);
	}
	// Implementujeme vlastní changeOverTime metodu s vanilla logikou
	private void changeOverTime(BlockState state,ServerLevel level,BlockPos pos,RandomSource random){
		// Vanilla oxidace má pravděpodobnost přibližně 1/17.6 na každý random tick
		if(random.nextFloat()<0.05688889f){
			this.tryOxidize(state,level,pos,random);
		}
	}
	private void tryOxidize(BlockState state,ServerLevel level,BlockPos pos,RandomSource random){
		// ZAJISTÍME, že oxidace se volá pouze na dolním dílu
		if(state.getValue(HALF)!=DoubleBlockHalf.LOWER){
			return; // Pokud nejsme dolní díl, neděláme nic
		}
		int nearbyOxidizedBlocks=0;
		// Kontrolujeme 4x4x4 oblast okolo bloku (vanilla logika)
		for(BlockPos nearbyPos: BlockPos.betweenClosed(pos.offset(-2,-2,-2),pos.offset(2,2,2))){
			if(nearbyPos.distManhattan(pos)<=4){
				BlockState nearbyState=level.getBlockState(nearbyPos);
				Block nearbyBlock=nearbyState.getBlock();
				// Počítáme oxidované bloky v okolí
				if(nearbyBlock instanceof WeatheringCopper copper){
					WeatherState nearbyAge=copper.getAge();
					if(nearbyAge==WeatherState.OXIDIZED){
						nearbyOxidizedBlocks++;
					}
				}
			}
		}
		// Výpočet šance na oxidaci na základě okolí (vanilla logika)
		float oxidationChance=(nearbyOxidizedBlocks+1)/64f;
		if(random.nextFloat()<oxidationChance){
			Block nextBlock=NTrialsModEvents.OXIDATION_LEVEL_INCREASES.get(this);
			if(nextBlock!=null){
				// Najdeme horní díl
				BlockPos upperPos=pos.above();
				BlockState upperState=level.getBlockState(upperPos);
				// Zkontrolujeme, že horní díl existuje a je to stejný typ dveří
				if(upperState.getBlock()==this&&upperState.getValue(HALF)==DoubleBlockHalf.UPPER){
					// Připravíme nové stavy pro oba díly (zachováme všechny properties)
					BlockState newLowerState=nextBlock.defaultBlockState()
							.setValue(FACING,state.getValue(FACING))
							.setValue(OPEN,state.getValue(OPEN))
							.setValue(HINGE,state.getValue(HINGE))
							.setValue(POWERED,state.getValue(POWERED))
							.setValue(HALF,DoubleBlockHalf.LOWER);
					BlockState newUpperState=nextBlock.defaultBlockState()
							.setValue(FACING,state.getValue(FACING)) // Horn�� díl má stejný FACING jako dolní
							.setValue(OPEN,state.getValue(OPEN))
							.setValue(HINGE,state.getValue(HINGE))
							.setValue(POWERED,false) // Horní díl nikdy nemá power
							.setValue(HALF,DoubleBlockHalf.UPPER);
					// Vyměníme bloky PŘÍMO bez dropu - používáme flag 2 (no drop) + flag 16 (no physics update)
					level.setBlock(pos,newLowerState,2|16);
					level.setBlock(upperPos,newUpperState,2|16);
					// Pošleme update klientům pro oba bloky
					level.sendBlockUpdated(pos,state,newLowerState,3);
					level.sendBlockUpdated(upperPos,upperState,newUpperState,3);
				}
			}
		}
	}
}
