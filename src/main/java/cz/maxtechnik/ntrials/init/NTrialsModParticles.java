package cz.maxtechnik.ntrials.init;

import cz.maxtechnik.ntrials.NTrialsMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class NTrialsModParticles {
    public static final DeferredRegister<ParticleType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, NTrialsMod.MODID);

    public static final RegistryObject<SimpleParticleType> GUST = REGISTRY.register("gust",
            () -> new SimpleParticleType(false));

    public static final RegistryObject<SimpleParticleType> SMALL_GUST = REGISTRY.register("small_gust",
            () -> new SimpleParticleType(false));
}
