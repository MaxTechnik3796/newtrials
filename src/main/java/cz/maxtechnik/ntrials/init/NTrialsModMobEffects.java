package cz.maxtechnik.ntrials.init;

import cz.maxtechnik.ntrials.NTrialsMod;
import cz.maxtechnik.ntrials.potion.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
public class NTrialsModMobEffects{
	public static final DeferredRegister<MobEffect> REGISTER=DeferredRegister.create(Registries.MOB_EFFECT,NTrialsMod.MODID);
	public static final RegistryObject<MobEffect> TRIAL_OMEN=REGISTER.register("trial_omen",TrialOmenMobEffect::new);
}
