package cz.maxtechnik.ntrials.init.other;

import cz.maxtechnik.ntrials.NTrialsMod;
import cz.maxtechnik.ntrials.enchantment.BreachEnchantment;
import cz.maxtechnik.ntrials.enchantment.DensityEnchantment;
import cz.maxtechnik.ntrials.enchantment.WindBurstEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
public class NTrialsModEnchantments{
	public static final DeferredRegister<Enchantment> REGISTER=DeferredRegister.create(ForgeRegistries.ENCHANTMENTS,NTrialsMod.MODID);
	public static final RegistryObject<Enchantment> WIND_BURST=REGISTER.register("wind_burst",WindBurstEnchantment::new);
	public static final RegistryObject<Enchantment> DENSITY=REGISTER.register("density",DensityEnchantment::new);
	public static final RegistryObject<Enchantment> BREACH=REGISTER.register("breach",BreachEnchantment::new);
}
