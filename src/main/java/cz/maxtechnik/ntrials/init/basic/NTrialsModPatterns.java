package cz.maxtechnik.ntrials.init.basic;

import cz.maxtechnik.ntrials.NTrialsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class NTrialsModPatterns {

    public static final DeferredRegister<BannerPattern> BANNER_PATTERNS = DeferredRegister.create(Registries.BANNER_PATTERN, NTrialsMod.MODID);

    public static final RegistryObject<BannerPattern> FLOW_PATTERN = BANNER_PATTERNS.register("flow", () -> new BannerPattern("flow"));
    public static final RegistryObject<BannerPattern> GUSTER_PATTERN = BANNER_PATTERNS.register("guster", () -> new BannerPattern("guster"));
}