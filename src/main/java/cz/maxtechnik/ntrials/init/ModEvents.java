package cz.maxtechnik.ntrials.init;

import cz.maxtechnik.ntrials.NTrialsMod;
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
            // Level 1 trade: 5 emeralds + 1 map = 1 trials structure map
            event.getTrades().get(1).add(new TrialsMapTrade(5, 12, 1));
            
            // Existing level 3 trade
            event.getTrades().get(3).add(new BasicItemListing(new ItemStack(Items.EMERALD,5),new ItemStack(Items.MAP,1),new ItemStack(Items.STICK,1),1,10,0.05F));
        }
    }
}