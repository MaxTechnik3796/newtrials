package cz.maxtechnik.ntrials.item;

import cz.maxtechnik.ntrials.init.NTrialsModSounds;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.phys.Vec3;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.NotNull;

public class MaceItem extends Mace {

    private static final float MIN_FALL_DISTANCE = 1.5F;

    public MaceItem() {
        super(Tiers.IRON, 3, -3.2F, new Item.Properties().durability(500));
    }
    @Override
    public boolean hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
        if (attacker instanceof Player player) {
            float fallDistance = player.fallDistance;
            Vec3 velocity = player.getDeltaMovement();
            double fallSpeed = Math.abs(velocity.y);
            if (fallDistance > MIN_FALL_DISTANCE) {
                float strength = (fallDistance - MIN_FALL_DISTANCE);
                if (fallSpeed > 0.2) {
                    strength += (float) (fallSpeed * 5.0);
                }
                strength = Math.min(strength, 25.0F);
                if (strength > 3.0F) {
                    if (fallDistance > 15.0F) {
                        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                                NTrialsModSounds.MACE_SMASH_GROUND_HEAVY.get(), SoundSource.PLAYERS, 1.2F, 1.0F);
                    } else if (fallSpeed > 0.3) {
                        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                                NTrialsModSounds.MACE_SMASH_AIR.get(), SoundSource.PLAYERS, 1.0F, 0.8F + (strength / 25.0F));
                    }
                    float kb = Math.min(strength / 10.0F, 2.0F);
                    Vec3 knockbackDir = target.position().subtract(player.position()).normalize().scale(kb);
                    target.setDeltaMovement(target.getDeltaMovement().add(knockbackDir.x, 0.35, knockbackDir.z));
                }
                player.fallDistance = 0.0F;
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }
}