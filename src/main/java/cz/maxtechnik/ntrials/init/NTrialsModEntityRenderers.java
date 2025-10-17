package cz.maxtechnik.ntrials.init;

import cz.maxtechnik.ntrials.client.renderer.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.api.distmarker.Dist;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public class NTrialsModEntityRenderers{
    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(NTrialsModEntities.BOGGED.get(),BoggedRenderer::new);
    }
}

