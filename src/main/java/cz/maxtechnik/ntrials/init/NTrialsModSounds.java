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
    public static final RegistryObject<SoundEvent>BLOCK_VAULT_EJECT_ITEM=REGISTER.register("block.vault.eject_item",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","block.vault.activate")));
    public static final RegistryObject<SoundEvent>BLOCK_VAULT_INSERT_ITEM=REGISTER.register("block.vault.insert_item",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","block.vault.insert_item")));
    public static final RegistryObject<SoundEvent>BLOCK_VAULT_INSERT_ITEM_FAIL=REGISTER.register("block.vault.insert_item_fail",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ntrials","block.vault.insert_item_fail")));
}
