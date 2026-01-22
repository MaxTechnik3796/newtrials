package cz.maxtechnik.ntrials.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import cz.maxtechnik.ntrials.init.events.NTrialsMod_ModModEvents;
import org.jetbrains.annotations.NotNull;
@SuppressWarnings("deprecation")
public class WaxedCopperBlock extends Block{
	public WaxedCopperBlock(BlockBehaviour.Properties props){
		super(props);
	}
	@Override
	public @NotNull InteractionResult use(@NotNull BlockState state,@NotNull Level level,@NotNull BlockPos pos,Player player,@NotNull InteractionHand hand,@NotNull BlockHitResult hit){
		ItemStack itemInHand=player.getItemInHand(hand);
		// Sekera interakcia - unwaxovanie (výmena za non-waxed verziu)
		if(itemInHand.getItem() instanceof AxeItem){
			Block unwaxedBlock=NTrialsMod_ModModEvents.UNWAXING_MAP.get(this);
			if(unwaxedBlock!=null){
				if(!level.isClientSide){
					level.setBlock(pos,unwaxedBlock.defaultBlockState(),3);
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
