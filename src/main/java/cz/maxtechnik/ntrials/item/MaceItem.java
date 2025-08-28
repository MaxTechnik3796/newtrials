package cz.maxtechnik.ntrials.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.phys.Vec3;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class MaceItem extends SwordItem {
    private static final float MIN_FALL_DISTANCE = 1.5f;
    private static final int MAX_DURABILITY = 500;

    public MaceItem() {
        super(Tiers.IRON, 3, -3.2f, new Item.Properties().durability(MAX_DURABILITY));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player) {
            // Zkontroluj výšku pádu
            float fallDistance = player.fallDistance;
            Vec3 velocity = player.getDeltaMovement();
            double fallSpeed = Math.abs(velocity.y);

            // Efekty při pádu z výšky - BEZ aplikování dodatečného damage
            if (fallDistance > MIN_FALL_DISTANCE) {
                float bonusDamage = (fallDistance - MIN_FALL_DISTANCE) * 1.0f;
                if (fallSpeed > 0.2) {
                    bonusDamage += (float) (fallSpeed * 5.0);
                }
                bonusDamage = Math.min(bonusDamage, 25.0f);

                // Efekty při větším pádu
                if (bonusDamage > 3.0f) {
                    // Zvuk při silném úderu
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.0f, 1.0f + (bonusDamage / 20.0f));

                    // Knockback efekt
                    float knockbackStrength = Math.min(bonusDamage / 10.0f, 1.5f);
                    Vec3 knockback = target.position().subtract(player.position()).normalize().scale(knockbackStrength);
                    target.setDeltaMovement(target.getDeltaMovement().add(knockback.x, 0.3, knockback.z));

                    // Stun efekt při velmi silném úderu
                    if (bonusDamage > 8.0f) {
                        int stunDuration = (int) Math.min(60, 20 + (bonusDamage * 2));
                        int stunLevel = bonusDamage > 15.0f ? 3 : 2;
                        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, stunDuration, stunLevel));
                    }

                    // Critické efekty při extrémní výšce
                    if (fallDistance > 20.0f) {
                        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.5f, 2.0f);
                        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                    }
                }

                // Reset fall distance po útoku
                player.fallDistance = 0;
            }
        }

        // Nech Minecraft aplikovat standardní damage
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public int getEnchantmentValue() {
        return 15;
    }
}