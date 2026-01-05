package cz.maxtechnik.ntrials.block;

import cz.maxtechnik.ntrials.NTrialsModEvents;
import cz.maxtechnik.ntrials.init.NTrialsModSounds;
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
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
@SuppressWarnings("deprecation")
public class WaxedCopperTrapdoorBlock extends TrapDoorBlock{
	public WaxedCopperTrapdoorBlock(Properties props){
		super(props,new BlockSetType("copper",true,SoundType.COPPER,NTrialsModSounds.BLOCK_COPPER_DOOR_CLOSE.get(),NTrialsModSounds.BLOCK_COPPER_DOOR_OPEN.get(),NTrialsModSounds.BLOCK_COPPER_TRAPDOOR_CLOSE.get(),NTrialsModSounds.BLOCK_COPPER_TRAPDOOR_OPEN.get(),SoundEvents.METAL_PRESSURE_PLATE_CLICK_OFF,SoundEvents.METAL_PRESSURE_PLATE_CLICK_ON,SoundEvents.STONE_BUTTON_CLICK_OFF,SoundEvents.STONE_BUTTON_CLICK_ON));
	}
	@Override
	public int getLightBlock(@NotNull BlockState state,@NotNull BlockGetter worldIn,@NotNull BlockPos pos){
		return 0;
	}
	@Override
	public @NotNull InteractionResult use(@NotNull BlockState state,@NotNull Level level,@NotNull BlockPos pos,Player player,@NotNull InteractionHand hand,@NotNull BlockHitResult hit){
		ItemStack itemInHand=player.getItemInHand(hand);
		if(itemInHand.getItem() instanceof AxeItem){
			Block waxedBlock=NTrialsModEvents.UNWAXING_MAP.get(this);
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
