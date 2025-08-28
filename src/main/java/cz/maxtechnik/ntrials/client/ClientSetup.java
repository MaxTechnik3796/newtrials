package cz.maxtechnik.ntrials.client;

import cz.maxtechnik.ntrials.client.renderer.blockentity.VaultBlockEntityRenderer;
import cz.maxtechnik.ntrials.client.renderer.entity.WindChargeProjectileRenderer;
import cz.maxtechnik.ntrials.init.NTrialsModBlockEntities;
import cz.maxtechnik.ntrials.init.NTrialsModEntityTypes;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;

public class ClientSetup {

    @Mod.EventBusSubscriber(modid = "ntrials", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(
                NTrialsModBlockEntities.VAULT_BLOCK_ENTITY.get(),
                VaultBlockEntityRenderer::new
            );
        }

        @SubscribeEvent
        public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(
                NTrialsModEntityTypes.WIND_CHARGE_PROJECTILE.get(),
                WindChargeProjectileRenderer::new
            );
        }
    }
}
