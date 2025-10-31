package cz.maxtechnik.ntrials.init;

import cz.maxtechnik.ntrials.NTrialsMod;
import cz.maxtechnik.ntrials.entity.BreezeEntity;
import cz.maxtechnik.ntrials.entity.WindChargeProjectile;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class NTrialsModEntityTypes {
    public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, NTrialsMod.MODID);

    public static final RegistryObject<EntityType<WindChargeProjectile>> WIND_CHARGE_PROJECTILE = REGISTRY.register("wind_charge_projectile",
            () -> EntityType.Builder.<WindChargeProjectile>of(WindChargeProjectile::new, MobCategory.MISC)
                    .sized(0.3125F, 0.3125F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("wind_charge_projectile"));

    public static final RegistryObject<EntityType<BreezeEntity>> BREEZE = REGISTRY.register("breeze",
            () -> EntityType.Builder.<BreezeEntity>of(BreezeEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(8)
                    .fireImmune()
                    .build("breeze"));
}
