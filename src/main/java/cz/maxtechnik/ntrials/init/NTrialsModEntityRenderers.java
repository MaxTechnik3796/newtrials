package cz.maxtechnik.ntrials.init;

import cz.maxtechnik.ntrials.client.model.*;
import cz.maxtechnik.ntrials.client.renderer.entity.BoggedRenderer;
import cz.maxtechnik.ntrials.client.renderer.entity.BreezeRenderer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.api.distmarker.Dist;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public class NTrialsModEntityRenderers{
    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(NTrialsModEntityTypes.BOGGED.get(), BoggedRenderer::new);
        event.registerEntityRenderer(NTrialsModEntityTypes.BREEZE.get(), BreezeRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(BoggedModel.LAYER_LOCATION, BoggedModel::createBodyLayer);
        event.registerLayerDefinition(BreezeModel.LAYER_LOCATION, BreezeModel::createBodyLayer);
        event.registerLayerDefinition(BreezeWindModel.LAYER_LOCATION, BreezeWindModel::createBodyLayer);
    }
}

