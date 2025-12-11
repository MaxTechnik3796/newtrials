package cz.maxtechnik.ntrials;

import com.mojang.logging.LogUtils;
import cz.maxtechnik.ntrials.init.*;
import cz.maxtechnik.ntrials.network.NetworkHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
@SuppressWarnings("removal")
@Mod(NTrialsMod.MODID)
public class NTrialsMod{
    public static final String MODID="ntrials";
    public static final Logger LOGGER=LogUtils.getLogger();
    public NTrialsMod(){
        IEventBus modEventBus=FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
        NTrialsModBlocks.REGISTRY.register(modEventBus);
        NTrialsModItems.REGISTRY.register(modEventBus);
        NTrialsModEntityTypes.REGISTRY.register(modEventBus);
        NTrialsModParticles.REGISTRY.register(modEventBus);
        NTrialsModTabs.REGISTER.register(modEventBus);
        NTrialsModMobEffects.REGISTER.register(modEventBus);
        NTrialsModBlockEntities.register(modEventBus);
        NTrialsModSounds.REGISTER.register(modEventBus);
        NTrialsModEnchantments.register(modEventBus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON,Config.SPEC);
    }
    private void commonSetup(final FMLCommonSetupEvent event){
        LOGGER.info("NewTrials Common loading...");
        event.enqueueWork(NTrialsModEvents::setupOxidation);
		event.enqueueWork(NTrialsModEvents::setupDispenserBehaviors);
        event.enqueueWork(NetworkHandler::registerPackets);
    }
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event){
        LOGGER.info("NewTrials Server loading...");
    }
    @Mod.EventBusSubscriber(modid=MODID,bus=Mod.EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static class ClientModEvents{
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event){
            LOGGER.info("NewTrials Client loading...");
        }
    }
}

