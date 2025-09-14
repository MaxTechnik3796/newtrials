package cz.maxtechnik.ntrials.init;

import cz.maxtechnik.ntrials.NTrialsMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class NTrialsModSounds{
    public static final DeferredRegister<SoundEvent>REGISTER=DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, NTrialsMod.MODID);
    public static final RegistryObject<SoundEvent>BLOCK_VAULT_ACTIVATE=REGISTER.register("block.vault.activate",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","block.vault.activate")));
    public static final RegistryObject<SoundEvent>BLOCK_VAULT_DEACTIVATE=REGISTER.register("block.vault.deactivate",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","block.vault.deactivate")));
    public static final RegistryObject<SoundEvent>BLOCK_VAULT_OPEN_SHUTTER=REGISTER.register("block.vault.open_shutter",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","block.vault.open_shutter")));
    public static final RegistryObject<SoundEvent>BLOCK_VAULT_CLOSE_SHUTTER=REGISTER.register("block.vault.close_shutter",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","block.vault.close_shutter")));
    public static final RegistryObject<SoundEvent>BLOCK_VAULT_EJECT_ITEM=REGISTER.register("block.vault.eject_item",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","block.vault.eject_item")));
    public static final RegistryObject<SoundEvent>BLOCK_VAULT_INSERT_ITEM=REGISTER.register("block.vault.insert_item",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","block.vault.insert_item")));
    public static final RegistryObject<SoundEvent>BLOCK_VAULT_INSERT_ITEM_FAIL=REGISTER.register("block.vault.insert_item_fail",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","block.vault.insert_item_fail")));
    public static final RegistryObject<SoundEvent>WIND_BURST=REGISTER.register("wind.burst",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","wind.burst")));

    // Mace sounds
    public static final RegistryObject<SoundEvent>MACE_SMASH_AIR=REGISTER.register("mace.smash_air",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","mace.smash_air")));
    public static final RegistryObject<SoundEvent>MACE_SMASH_GROUND=REGISTER.register("mace.smash_ground",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","mace.smash_ground")));
    public static final RegistryObject<SoundEvent>MACE_SMASH_GROUND_HEAVY=REGISTER.register("mace.smash_ground_heavy",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","mace.smash_ground_heavy")));

    // Trial Spawner sounds
    public static final RegistryObject<SoundEvent>BLOCK_TRIAL_SPAWNER_ABOUT_TO_SPAWN_ITEM=REGISTER.register("block.trial_spawner.about_to_spawn_item",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","block.trial_spawner.about_to_spawn_item")));
    public static final RegistryObject<SoundEvent>BLOCK_TRIAL_SPAWNER_AMBIENT=REGISTER.register("block.trial_spawner.ambient",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","block.trial_spawner.ambient")));
    public static final RegistryObject<SoundEvent>BLOCK_TRIAL_SPAWNER_AMBIENT_OMINOUS=REGISTER.register("block.trial_spawner.ambient_ominous",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","block.trial_spawner.ambient_ominous")));
    public static final RegistryObject<SoundEvent>BLOCK_TRIAL_SPAWNER_BREAK=REGISTER.register("block.trial_spawner.break",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","block.trial_spawner.break")));
    public static final RegistryObject<SoundEvent>BLOCK_TRIAL_SPAWNER_CLOSE_SHUTTER=REGISTER.register("block.trial_spawner.close_shutter",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","block.trial_spawner.close_shutter")));
    public static final RegistryObject<SoundEvent>BLOCK_TRIAL_SPAWNER_DETECT_PLAYER=REGISTER.register("block.trial_spawner.detect_player",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","block.trial_spawner.detect_player")));
    public static final RegistryObject<SoundEvent>BLOCK_TRIAL_SPAWNER_EJECT_ITEM=REGISTER.register("block.trial_spawner.eject_item",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","block.trial_spawner.eject_item")));
    public static final RegistryObject<SoundEvent>BLOCK_TRIAL_SPAWNER_OMINOUS_ACTIVATE=REGISTER.register("block.trial_spawner.ominous_activate",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","block.trial_spawner.ominous_activate")));
    public static final RegistryObject<SoundEvent>BLOCK_TRIAL_SPAWNER_OPEN_SHUTTER=REGISTER.register("block.trial_spawner.open_shutter",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","block.trial_spawner.open_shutter")));
    public static final RegistryObject<SoundEvent>BLOCK_TRIAL_SPAWNER_PLACE=REGISTER.register("block.trial_spawner.place",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","block.trial_spawner.place")));
    public static final RegistryObject<SoundEvent>BLOCK_TRIAL_SPAWNER_SPAWN=REGISTER.register("block.trial_spawner.spawn",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","block.trial_spawner.spawn")));
    public static final RegistryObject<SoundEvent>BLOCK_TRIAL_SPAWNER_SPAWN_ITEM=REGISTER.register("block.trial_spawner.spawn_item",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","block.trial_spawner.spawn_item")));
    public static final RegistryObject<SoundEvent>BLOCK_TRIAL_SPAWNER_SPAWN_ITEM_BEGIN=REGISTER.register("block.trial_spawner.spawn_item_begin",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","block.trial_spawner.spawn_item_begin")));
    public static final RegistryObject<SoundEvent>BLOCK_TRIAL_SPAWNER_STEP=REGISTER.register("block.trial_spawner.step",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","block.trial_spawner.step")));
}
