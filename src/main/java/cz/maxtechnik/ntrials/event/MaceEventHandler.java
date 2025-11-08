package cz.maxtechnik.ntrials.event;

import cz.maxtechnik.ntrials.NTrialsMod;
import cz.maxtechnik.ntrials.init.NTrialsModEnchantments;
import cz.maxtechnik.ntrials.init.NTrialsModParticles;
import cz.maxtechnik.ntrials.item.MaceItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.server.level.ServerLevel;

import java.util.List;

@Mod.EventBusSubscriber(modid = NTrialsMod.MODID)
public class MaceEventHandler {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // Zkontroluj jestli útočník je hráč s mace
        if (event.getSource().getEntity() instanceof Player player) {
            ItemStack heldItem = player.getMainHandItem();

			ItemStack maceStack = player.getMainHandItem();

			@SuppressWarnings("deprecation")
			int windBurstLevel = EnchantmentHelper.getItemEnchantmentLevel(
					NTrialsModEnchantments.WIND_BURST.get(), maceStack);

			@SuppressWarnings("deprecation")
			int densityLevel = EnchantmentHelper.getItemEnchantmentLevel(
					NTrialsModEnchantments.DENSITY.get(), maceStack);

			@SuppressWarnings("deprecation")
			int breachLevel = EnchantmentHelper.getItemEnchantmentLevel(
					NTrialsModEnchantments.BREACH.get(), maceStack);


            if (heldItem.getItem() instanceof MaceItem) {
                float fallDistance = player.fallDistance;

                // Přidej bonus damage na základě výšky pádu
                if (fallDistance > 1.5f) {
                    float bonusDamage = fallDistance - 1.5f;

                    // Bonus za rychlost pádu
                    double fallSpeed = Math.abs(player.getDeltaMovement().y);
                    if (fallSpeed > 0.2) {
                        bonusDamage += (float) (fallSpeed * 5.0);
                    }

                    // Omez maximální bonus
                    bonusDamage = Math.min(bonusDamage, 25.0f);

					if (densityLevel > 0) {
						bonusDamage += ((densityLevel * 0.5) * fallDistance);
					}

					if (breachLevel > 0) {
						LivingEntity target = (LivingEntity) event.getEntity();
						float armorValue = target.getArmorValue();
						float armorToughness = (float) target.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS);

						float totalArmor = armorValue + armorToughness;
						float armorReduction = totalArmor / (totalArmor + 20.0f);

						float breachEffectiveness = Math.min(breachLevel * 0.20f, 1.0f);
						float ignoredArmorReduction = armorReduction * breachEffectiveness;

						float breachBonus = bonusDamage * (ignoredArmorReduction / (1.0f - armorReduction + 0.001f));
						bonusDamage += breachBonus;
					}

					if (windBurstLevel > 0 && player.level() instanceof ServerLevel serverLevel) {
						Vec3 center = player.position();
						Vec3 explosion_center = new Vec3(center.x, center.y - 0.0D, center.z);

						double radius = 4.375D;

						for (int i = 0; i < 5; i++) {
							double angle = (i / 20.0D) * Math.PI * 2;
							double distance = 0.5D + player.getRandom().nextDouble() * 2.0D;

							double offsetX = Math.cos(angle) * distance;
							double offsetY = (player.getRandom().nextDouble() - 0.5D) * 1.0D;
							double offsetZ = Math.sin(angle) * distance;

							double velocityX = offsetX * 0.3D;
							double velocityY = Math.abs(offsetY) * 0.2D;
							double velocityZ = offsetZ * 0.3D;

							serverLevel.sendParticles(NTrialsModParticles.GUST.get(),
									center.x, center.y, center.z,
									1, velocityX, velocityY, velocityZ, 0.0D);
						}

						// Malé gust particles
						for (int i = 0; i < 10; i++) {
							double offsetX = (player.getRandom().nextDouble() - 0.5D) * radius * 0.5D;
							double offsetY = (player.getRandom().nextDouble() - 0.5D) * radius * 0.3D;
							double offsetZ = (player.getRandom().nextDouble() - 0.5D) * radius * 0.5D;

							double velocityX = offsetX * 0.1D;
							double velocityY = Math.abs(offsetY) * 0.05D;
							double velocityZ = offsetZ * 0.1D;

							serverLevel.sendParticles(NTrialsModParticles.SMALL_GUST.get(),
									center.x + offsetX * 0.2D, center.y + offsetY * 0.2D, center.z + offsetZ * 0.2D,
									1, velocityX, velocityY, velocityZ, 0.0D);
						}


						// Find and knockback entities
						List<Entity> entities = player.level().getEntities(player, player.getBoundingBox().inflate(radius));
						for (Entity entity : entities) {
							if (entity instanceof LivingEntity) {
								double distance = entity.distanceTo(player);
								if (distance <= radius) {
									// Calculate knockback direction
									Vec3 direction = entity.position().subtract(explosion_center).normalize();

									double baseKnockback = 3.5D * windBurstLevel; // Vanilla používá 3.5 * level
									double knockbackStrength = baseKnockback * (1.0D - (distance / radius));
									Vec3 knockback = direction.scale(knockbackStrength);

									double verticalKnockback = knockback.y;
									double horizontalMultiplier = 1.0D;

									entity.setDeltaMovement(entity.getDeltaMovement().add(
											knockback.x * horizontalMultiplier,
											verticalKnockback,
											knockback.z * horizontalMultiplier
									));
									entity.hurtMarked = true;
								}
							}
						}
						Vec3 currentVelocity = player.getDeltaMovement();
						double upwardForce = 0.8D + (windBurstLevel * 0.3D);

						player.setDeltaMovement(currentVelocity.x, upwardForce, currentVelocity.z);
						player.hurtMarked = true;


						player.fallDistance = 0;

					}

                    // Přidej bonus k původnímu damage
                    event.setAmount(event.getAmount() + bonusDamage);
                }
            }
        }
    }
}
