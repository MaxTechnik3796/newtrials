package cz.maxtechnik.ntrials.init;


import cz.maxtechnik.ntrials.NTrialsMod;

import cz.maxtechnik.ntrials.NTrialsMod; // Nahraďte cestou k vaší hlavní třídě
import cz.maxtechnik.ntrials.init.ModStructureTags; // <-- DŮLEŽITÝ NOVÝ IMPORT (viz Krok 3)

import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.BasicItemListing;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NTrialsMod.MODID)
public class ModEvents {

    @SubscribeEvent
    public static void registerTrades(VillagerTradesEvent event) {
        
        if (event.getType() == VillagerProfession.CARTOGRAPHER) {

            event.getTrades().get(3).add(new BasicItemListing(new ItemStack(Items.EMERALD,5),new ItemStack(Items.MAP,1),new ItemStack(Items.STICK,1),1,10,0.05F));
        }
    }
}