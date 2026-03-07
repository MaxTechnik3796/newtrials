package cz.maxtechnik.ntrials.init.other;

import cz.maxtechnik.ntrials.NTrialsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
public class NTrialsModPatterns{
	public static final DeferredRegister<BannerPattern> REGISTRY=DeferredRegister.create(Registries.BANNER_PATTERN,NTrialsMod.MODID);
	public static final RegistryObject<BannerPattern> FLOW_PATTERN=REGISTRY.register("flow",()->new BannerPattern("flow"));
	public static final RegistryObject<BannerPattern> GUSTER_PATTERN=REGISTRY.register("guster",()->new BannerPattern("guster"));
}