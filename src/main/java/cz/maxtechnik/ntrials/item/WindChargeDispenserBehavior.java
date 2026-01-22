package cz.maxtechnik.ntrials.item;

import cz.maxtechnik.ntrials.entity.WindChargeProjectile;
import cz.maxtechnik.ntrials.init.other.NTrialsModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import org.jetbrains.annotations.NotNull;
public class WindChargeDispenserBehavior extends DefaultDispenseItemBehavior{
	@Override
	public @NotNull ItemStack execute(BlockSource blockSource,@NotNull ItemStack itemStack){
		Level level=blockSource.getLevel();
		BlockPos pos=blockSource.getPos();
		Direction direction=blockSource.getBlockState().getValue(DispenserBlock.FACING);
		if(!level.isClientSide){
			// Create wind charge projectile using EntityType constructor (no shooter needed)
			WindChargeProjectile windCharge=new WindChargeProjectile(
					NTrialsModEntityTypes.WIND_CHARGE_PROJECTILE.get(),
					level
			);
			// Position the projectile slightly in front of the dispenser
			double x=pos.getX()+0.5D+direction.getStepX()*0.7D;
			double y=pos.getY()+0.5D+direction.getStepY()*0.7D;
			double z=pos.getZ()+0.5D+direction.getStepZ()*0.7D;
			windCharge.setPos(x,y,z);
			// Set velocity in the direction the dispenser is facing
			double velocity=1.1D; // Slightly faster than snowball
			windCharge.setDeltaMovement(
					direction.getStepX()*velocity,
					direction.getStepY()*velocity,
					direction.getStepZ()*velocity
			);
			level.addFreshEntity(windCharge);
		}
		// Play sound
		level.playSound(null,pos,SoundEvents.SNOWBALL_THROW,SoundSource.BLOCKS,1.0F,1.0F);
		// Consume one item
		itemStack.shrink(1);
		return itemStack;
	}
}
