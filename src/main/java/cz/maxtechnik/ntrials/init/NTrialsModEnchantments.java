package cz.maxtechnik.ntrials.init;

import cz.maxtechnik.ntrials.NTrialsMod;
import cz.maxtechnik.ntrials.enchantment.BreachEnchantment;
import cz.maxtechnik.ntrials.enchantment.DensityEnchantment;
import cz.maxtechnik.ntrials.enchantment.WindBurstEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class NTrialsModEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, NTrialsMod.MODID);

    public static final RegistryObject<Enchantment> WIND_BURST = ENCHANTMENTS.register("wind_burst", WindBurstEnchantment::new);

    public static final RegistryObject<Enchantment> DENSITY = ENCHANTMENTS.register("density", DensityEnchantment::new);

    public static final RegistryObject<Enchantment> BREACH = ENCHANTMENTS.register("breach", BreachEnchantment::new);

    public static void register(IEventBus eventBus) {
        ENCHANTMENTS.register(eventBus);
    }
}
