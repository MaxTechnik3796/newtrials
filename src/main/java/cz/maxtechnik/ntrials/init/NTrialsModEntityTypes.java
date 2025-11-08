package cz.maxtechnik.ntrials.init;

import cz.maxtechnik.ntrials.NTrialsMod;
import cz.maxtechnik.ntrials.entity.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class NTrialsModEntityTypes {
    public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, NTrialsMod.MODID);

    public static final RegistryObject<EntityType<BoggedEntity>> BOGGED = register("bogged",
            EntityType.Builder.<BoggedEntity>of(BoggedEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).setCustomClientFactory(BoggedEntity::new)
                    .sized(0.6f, 1.8f));

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

    private static <T extends Entity> RegistryObject<EntityType<T>> register(String registryname, EntityType.Builder<T> entityTypeBuilder) {
        return REGISTRY.register(registryname, () -> (EntityType<T>) entityTypeBuilder.build(registryname));
    }

    @SubscribeEvent
    public static void init(FMLCommonSetupEvent event) {
        event.enqueueWork(BoggedEntity::init);
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(BOGGED.get(), BoggedEntity.createAttributes().build());
        event.put(BREEZE.get(), BreezeEntity.createAttributes().build());
    }
}
