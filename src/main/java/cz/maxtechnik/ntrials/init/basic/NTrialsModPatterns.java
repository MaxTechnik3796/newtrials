package cz.maxtechnik.ntrials.init.basic;

import cz.maxtechnik.ntrials.NTrialsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class NTrialsModPatterns {

    public static final DeferredRegister<BannerPattern> BANNER_PATTERNS = DeferredRegister.create(Registries.BANNER_PATTERN, NTrialsMod.MODID);
    public static final DeferredRegister<String> POT_PATTERNS = DeferredRegister.create(Registries.DECORATED_POT_PATTERNS, NTrialsMod.MODID);

    public static final RegistryObject<BannerPattern> FLOW_PATTERN = BANNER_PATTERNS.register("flow", () -> new BannerPattern("flow"));
    public static final RegistryObject<BannerPattern> GUSTER_PATTERN = BANNER_PATTERNS.register("guster", () -> new BannerPattern("guster"));

    public static final RegistryObject<String> GUSTER_POTTERY_PATTERN = POT_PATTERNS.register("guster_pottery_sherd", () -> NTrialsMod.MODID + ":entity/decorated_pot/guster_pottery_pattern");
    public static final RegistryObject<String> SCRAPE_POTTERY_PATTERN = POT_PATTERNS.register("scrape_pottery_sherd", () -> NTrialsMod.MODID + ":entity/decorated_pot/scrape_pottery_pattern");
    public static final RegistryObject<String> FLOW_POTTERY_PATTERN = POT_PATTERNS.register("flow_pottery_sherd", () -> NTrialsMod.MODID + ":entity/decorated_pot/flow_pottery_pattern");
}