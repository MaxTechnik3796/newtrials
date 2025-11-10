package cz.maxtechnik.ntrials.item;

import cz.maxtechnik.ntrials.init.NTrialsModEnchantments;
import cz.maxtechnik.ntrials.init.NTrialsModSounds;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.phys.Vec3;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.NotNull;

public class MaceItem extends SwordItem {
    private static final float MIN_FALL_DISTANCE = 1.5f;
    private static final int MAX_DURABILITY = 500;

    public MaceItem() {
        super(Tiers.IRON, 3, -3.2f, new Item.Properties().durability(MAX_DURABILITY));
    }

    @Override
    public boolean hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
        if (attacker instanceof Player player) {
            // Zkontroluj výšku pádu
            float fallDistance = player.fallDistance;
            Vec3 velocity = player.getDeltaMovement();
            double fallSpeed = Math.abs(velocity.y);



            // Efekty při pádu z výšky - BEZ aplikování dodatečného damage
            if (fallDistance > MIN_FALL_DISTANCE) {
                float bonusDamage = (fallDistance - MIN_FALL_DISTANCE);
                if (fallSpeed > 0.2) {
                    bonusDamage += (float) (fallSpeed * 5.0);
                }
                bonusDamage = Math.min(bonusDamage, 25.0f);

                // Efekty při větším pádu
                if (bonusDamage > 3.0f) {
                    // Zvuk podle typu útoku
                    if (fallDistance > 15.0f) {
                        // Těžký úder z vysoké výšky
                        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            NTrialsModSounds.MACE_SMASH_GROUND_HEAVY.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
                    } else if (fallSpeed > 0.3) {
                        // Úder ve vzduchu
                        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            NTrialsModSounds.MACE_SMASH_AIR.get(), SoundSource.PLAYERS, 1.0f, 1.0f + (bonusDamage / 20.0f));
                    }

                    // Knockback efekt
                    float knockbackStrength = Math.min(bonusDamage / 10.0f, 1.5f);
                    Vec3 knockback = target.position().subtract(player.position()).normalize().scale(knockbackStrength);
                    target.setDeltaMovement(target.getDeltaMovement().add(knockback.x, 0.3, knockback.z));



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

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        // Povolit naše custom enchanty
        if (enchantment == NTrialsModEnchantments.WIND_BURST.get() ||
            enchantment == NTrialsModEnchantments.DENSITY.get() ||
            enchantment == NTrialsModEnchantments.BREACH.get()) {
            return true;
        }
        // Jinak použij standardní chování
        return super.canApplyAtEnchantingTable(stack, enchantment);
    }
}