package cz.maxtechnik.ntrials;

import com.mojang.logging.LogUtils;
import cz.maxtechnik.ntrials.init.basic.NTrialsModBlocks;
import cz.maxtechnik.ntrials.init.basic.NTrialsModItems;
import cz.maxtechnik.ntrials.init.basic.NTrialsModSounds;
import cz.maxtechnik.ntrials.init.basic.NTrialsModTabs;
import cz.maxtechnik.ntrials.init.events.NTrialsMod_ModModEvents;
import cz.maxtechnik.ntrials.init.other.*;
import cz.maxtechnik.ntrials.network.NetworkHandler;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.event.entity.living.LivingFallEvent;

import java.util.Objects;
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
		NTrialsModEnchantments.REGISTER.register(modEventBus);
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER,NTrialsServerConfig.SPEC);
	}
	private void commonSetup(final FMLCommonSetupEvent event){
		LOGGER.info("NewTrials Common loading...");
		event.enqueueWork(NTrialsMod_ModModEvents::setupOxidation);
		event.enqueueWork(NTrialsMod_ModModEvents::setupDispenserBehaviors);
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
	public static void adv(ServerPlayer player,ResourceLocation adv_path){
		Advancement adv=Objects.requireNonNull(player.getServer()).getAdvancements().getAdvancement(adv_path);
		assert adv!=null;
		AdvancementProgress ap=player.getAdvancements().getOrStartProgress(adv);
		if(!ap.isDone()){
			for(String criteria: ap.getRemainingCriteria())
				player.getAdvancements().award(adv,criteria);
		}
	}
	@SubscribeEvent
	public void onPlayerFall(LivingFallEvent event){
		if(!(event.getEntity()instanceof net.minecraft.world.entity.player.Player player))return;
		if(player.level().isClientSide())return;
		CompoundTag data=player.getPersistentData();
		if(!data.contains("WindChargeImmunityTime"))return;
		long explosionTime=data.getLong("WindChargeImmunityTime");
		long currentTime=player.level().getGameTime();
		if(currentTime-explosionTime<=39){
			Vec3 explosionPos=new Vec3(
					data.getDouble("WindChargeExplosionX"),
					data.getDouble("WindChargeExplosionY"),
					data.getDouble("WindChargeExplosionZ")
			);
			Vec3 landingPos=player.position();
			double horizDist=Math.sqrt(
					Math.pow(landingPos.x-explosionPos.x,2)+
							Math.pow(landingPos.z-explosionPos.z,2)
			);
			if(horizDist<=2.5D){
				event.setDamageMultiplier(0.0F);
				event.setDistance(0.0F);
			}
		}
		data.remove("WindChargeImmunityTime");
		data.remove("WindChargeExplosionX");
		data.remove("WindChargeExplosionY");
		data.remove("WindChargeExplosionZ");
	}
	public static void sendMessageToPlayer(Player player,MutableComponent message){
		MutableComponent messageTemplate=Component.empty();
		messageTemplate.append(Component.translatable("chat.ntrials.mod_prefix"));
		messageTemplate.append(CommonComponents.space());
		messageTemplate.append(message);
		player.sendSystemMessage(messageTemplate);
	}
}


