package cz.maxtechnik.ntrials.init.other;

import cz.maxtechnik.ntrials.NTrialsMod;
import cz.maxtechnik.ntrials.block.entity.TrialVaultBlockEntity;
import cz.maxtechnik.ntrials.block.entity.TrialSpawnerBlockEntity;
import cz.maxtechnik.ntrials.init.basic.NTrialsModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class NTrialsModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, NTrialsMod.MODID);

    public static final RegistryObject<BlockEntityType<TrialVaultBlockEntity>> VAULT_BLOCK_ENTITY = REGISTRY.register("vault_block_entity",
            () -> BlockEntityType.Builder.of(TrialVaultBlockEntity::new, NTrialsModBlocks.VAULT.get(), NTrialsModBlocks.VAULT_BOSS.get()).build(null));

    public static final RegistryObject<BlockEntityType<TrialSpawnerBlockEntity>> TRIAL_SPAWNER_BLOCK_ENTITY = REGISTRY.register("trial_spawner_block_entity",
            () -> BlockEntityType.Builder.of(TrialSpawnerBlockEntity::new, NTrialsModBlocks.TRIAL_SPAWNER.get(), NTrialsModBlocks.TRIAL_SPAWNER_BOSS.get()).build(null));

    public static void register(IEventBus eventBus) {
        REGISTRY.register(eventBus);
    }

}
