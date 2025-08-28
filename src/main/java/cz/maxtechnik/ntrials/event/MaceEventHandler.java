package cz.maxtechnik.ntrials.event;

import cz.maxtechnik.ntrials.NTrialsMod;
import cz.maxtechnik.ntrials.item.MaceItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NTrialsMod.MODID)
public class MaceEventHandler {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // Zkontroluj jestli útočník je hráč s mace
        if (event.getSource().getEntity() instanceof Player player) {
            ItemStack heldItem = player.getMainHandItem();

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

                    // Přidej bonus k původnímu damage
                    event.setAmount(event.getAmount() + bonusDamage);
                }
            }
        }
    }
}
