package cz.maxtechnik.ntrials.block;

import cz.maxtechnik.ntrials.init.basic.NTrialsModSounds;
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
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import cz.maxtechnik.ntrials.init.events.NTrialsMod_ModModEvents;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
@SuppressWarnings("deprecation")
public class WaxedCopperDoorBlock extends DoorBlock{
	public WaxedCopperDoorBlock(BlockBehaviour.Properties props){
		super(props,new BlockSetType("copper",true,SoundType.COPPER,NTrialsModSounds.BLOCK_COPPER_DOOR_CLOSE.get(),NTrialsModSounds.BLOCK_COPPER_DOOR_OPEN.get(),NTrialsModSounds.BLOCK_COPPER_TRAPDOOR_CLOSE.get(),NTrialsModSounds.BLOCK_COPPER_TRAPDOOR_OPEN.get(),SoundEvents.METAL_PRESSURE_PLATE_CLICK_OFF,SoundEvents.METAL_PRESSURE_PLATE_CLICK_ON,SoundEvents.STONE_BUTTON_CLICK_OFF,SoundEvents.STONE_BUTTON_CLICK_ON));
	}
	@Override
	public int getLightBlock(@NotNull BlockState state,@NotNull BlockGetter worldIn,@NotNull BlockPos pos){
		return 0;
	}
	@Override
	public @NotNull InteractionResult use(@NotNull BlockState state,@NotNull Level level,@NotNull BlockPos pos,Player player,@NotNull InteractionHand hand,@NotNull BlockHitResult hit){
		ItemStack itemInHand=player.getItemInHand(hand);
		// Axe unwaxing - POUZE když držíme shift
		if(itemInHand.getItem() instanceof AxeItem){
			Block unwaxedBlock=NTrialsMod_ModModEvents.UNWAXING_MAP.get(this);
			if(unwaxedBlock!=null){
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
						BlockState newState1=unwaxedBlock.defaultBlockState()
								.setValue(FACING,state.getValue(FACING))
								.setValue(OPEN,state.getValue(OPEN))
								.setValue(HINGE,state.getValue(HINGE))
								.setValue(POWERED,state.getValue(POWERED))
								.setValue(HALF,state.getValue(HALF));
						BlockState newState2=unwaxedBlock.defaultBlockState()
								.setValue(FACING,otherState.getValue(FACING))
								.setValue(OPEN,otherState.getValue(OPEN))
								.setValue(HINGE,otherState.getValue(HINGE))
								.setValue(POWERED,otherState.getValue(POWERED))
								.setValue(HALF,otherState.getValue(HALF));
						// Vyměníme bloky PŘÍMO bez dropu - používáme flag 2|16 (no drop + no physics update)
						level.setBlock(pos,newState1,2|16);
						level.setBlock(otherPos,newState2,2|16);
						// Pošleme update klientům pro oba bloky
						level.sendBlockUpdated(pos,state,newState1,3);
						level.sendBlockUpdated(otherPos,otherState,newState2,3);
						CopperUtil.play(level,pos,SoundEvents.AXE_SCRAPE);
						CopperUtil.spawnParticles(level,pos,ParticleTypes.WAX_OFF);
						CopperUtil.damageToolIfNotCreative(itemInHand,player,hand);
					}
				}
				return InteractionResult.sidedSuccess(level.isClientSide);
			}
		}
		// Pokud není shift stisknut NEBO není to axe, použij normální chování dveří
		return super.use(state,level,pos,player,hand,hit);
	}
}
