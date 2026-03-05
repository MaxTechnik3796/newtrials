package cz.maxtechnik.ntrials.client;

import cz.maxtechnik.ntrials.client.particle.GustParticle;
import cz.maxtechnik.ntrials.client.particle.SmallGustParticle;
import cz.maxtechnik.ntrials.client.renderer.blockentity.VaultBlockEntityRenderer;
import cz.maxtechnik.ntrials.client.renderer.blockentity.TrialSpawnerBlockEntityRenderer;
import cz.maxtechnik.ntrials.client.renderer.entity.WindChargeProjectileRenderer;
import cz.maxtechnik.ntrials.init.other.NTrialsModBlockEntities;
import cz.maxtechnik.ntrials.init.other.NTrialsModEntityTypes;
import cz.maxtechnik.ntrials.init.other.NTrialsModParticles;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
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
                    context1 -> new VaultBlockEntityRenderer()
            );
            event.registerBlockEntityRenderer(
                NTrialsModBlockEntities.TRIAL_SPAWNER_BLOCK_ENTITY.get(),
                    context -> new TrialSpawnerBlockEntityRenderer()
            );
        }

        @SubscribeEvent
        public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(
                NTrialsModEntityTypes.WIND_CHARGE_PROJECTILE.get(),
                WindChargeProjectileRenderer::new
            );
        }

        @SubscribeEvent
        public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(NTrialsModParticles.GUST.get(), GustParticle.Provider::new);
            event.registerSpriteSet(NTrialsModParticles.SMALL_GUST.get(), SmallGustParticle.Provider::new);
        }
    }
}
