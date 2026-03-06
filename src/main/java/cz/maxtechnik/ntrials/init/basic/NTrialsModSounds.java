package cz.maxtechnik.ntrials.init.basic;

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
    public static final RegistryObject<SoundEvent>BLOCK_VAULT_REJECT_REWARDED_PLAYER=REGISTER.register("block.vault.reject_rewarded_player",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","block.vault.reject_rewarded_player")));
    public static final RegistryObject<SoundEvent>WIND_BURST=REGISTER.register("wind.burst",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","wind.burst")));

    // Mace sounds
    public static final RegistryObject<SoundEvent>MACE_SMASH_AIR=REGISTER.register("mace.smash_air",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","mace.smash_air")));
    public static final RegistryObject<SoundEvent>MACE_SMASH_GROUND_HEAVY=REGISTER.register("mace.smash_ground_heavy",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","mace.smash_ground_heavy")));

    public static final RegistryObject<SoundEvent>BLOCK_TRIAL_SPAWNER_AMBIENT=REGISTER.register("block.trial_spawner.ambient",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","block.trial_spawner.ambient")));
    public static final RegistryObject<SoundEvent>BLOCK_TRIAL_SPAWNER_AMBIENT_OMINOUS=REGISTER.register("block.trial_spawner.ambient_ominous",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","block.trial_spawner.ambient_ominous")));
    public static final RegistryObject<SoundEvent>BLOCK_TRIAL_SPAWNER_CLOSE_SHUTTER=REGISTER.register("block.trial_spawner.close_shutter",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","block.trial_spawner.close_shutter")));
    public static final RegistryObject<SoundEvent>BLOCK_TRIAL_SPAWNER_DETECT_PLAYER=REGISTER.register("block.trial_spawner.detect_player",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","block.trial_spawner.detect_player")));
    public static final RegistryObject<SoundEvent>BLOCK_TRIAL_SPAWNER_EJECT_ITEM=REGISTER.register("block.trial_spawner.eject_item",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","block.trial_spawner.eject_item")));
    public static final RegistryObject<SoundEvent>BLOCK_TRIAL_SPAWNER_OMINOUS_ACTIVATE=REGISTER.register("block.trial_spawner.ominous_activate",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","block.trial_spawner.ominous_activate")));
    public static final RegistryObject<SoundEvent>BLOCK_TRIAL_SPAWNER_OPEN_SHUTTER=REGISTER.register("block.trial_spawner.open_shutter",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","block.trial_spawner.open_shutter")));
    public static final RegistryObject<SoundEvent>BLOCK_TRIAL_SPAWNER_SPAWN=REGISTER.register("block.trial_spawner.spawn",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","block.trial_spawner.spawn")));
    public static final RegistryObject<SoundEvent>BLOCK_TRIAL_SPAWNER_SPAWN_ITEM_BEGIN=REGISTER.register("block.trial_spawner.spawn_item_begin",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","block.trial_spawner.spawn_item_begin")));

    // Breeze sounds
    public static final RegistryObject<SoundEvent>ENTITY_BREEZE_AMBIENT=REGISTER.register("entity.breeze.ambient",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","entity.breeze.ambient")));
    public static final RegistryObject<SoundEvent>ENTITY_BREEZE_AMBIENT_CAVE=REGISTER.register("entity.breeze.ambient_cave",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","entity.breeze.ambient_cave")));
    public static final RegistryObject<SoundEvent>ENTITY_BREEZE_DEATH=REGISTER.register("entity.breeze.death",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","entity.breeze.death")));
    public static final RegistryObject<SoundEvent>ENTITY_BREEZE_DEFLECT=REGISTER.register("entity.breeze.deflect",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","entity.breeze.deflect")));
    public static final RegistryObject<SoundEvent>ENTITY_BREEZE_HURT=REGISTER.register("entity.breeze.hurt",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","entity.breeze.hurt")));
    public static final RegistryObject<SoundEvent>ENTITY_BREEZE_INHALE=REGISTER.register("entity.breeze.inhale",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","entity.breeze.inhale")));
    public static final RegistryObject<SoundEvent>ENTITY_BREEZE_JUMP=REGISTER.register("entity.breeze.jump",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","entity.breeze.jump")));
    public static final RegistryObject<SoundEvent>ENTITY_BREEZE_SHOOT=REGISTER.register("entity.breeze.shoot",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","entity.breeze.shoot")));

    // Bogged sounds
    public static final RegistryObject<SoundEvent>ENTITY_BOGGED_AMBIENT=REGISTER.register("entity.bogged.ambient",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","entity.bogged.ambient")));
    public static final RegistryObject<SoundEvent>ENTITY_BOGGED_DEATH=REGISTER.register("entity.bogged.death",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","entity.bogged.death")));
    public static final RegistryObject<SoundEvent>ENTITY_BOGGED_HURT=REGISTER.register("entity.bogged.hurt",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","entity.bogged.hurt")));
    public static final RegistryObject<SoundEvent>ENTITY_BOGGED_STEP=REGISTER.register("entity.bogged.step",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","entity.bogged.step")));

	// Copper Door & Trapdoor:
	public static final RegistryObject<SoundEvent>BLOCK_COPPER_DOOR_OPEN=REGISTER.register("block.copper_door.open",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","block.copper_door.open")));
	public static final RegistryObject<SoundEvent>BLOCK_COPPER_DOOR_CLOSE=REGISTER.register("block.copper_door.close",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","block.copper_door.close")));
	public static final RegistryObject<SoundEvent>BLOCK_COPPER_TRAPDOOR_OPEN=REGISTER.register("block.copper_trapdoor.open",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","block.copper_trapdoor.open")));
	public static final RegistryObject<SoundEvent>BLOCK_COPPER_TRAPDOOR_CLOSE=REGISTER.register("block.copper_trapdoor.close",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","block.copper_trapdoor.close")));

}
