package cz.maxtechnik.ntrials.init;


import cz.maxtechnik.ntrials.NTrialsMod;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NTrialsMod.MODID)
public class ModEvents {

    @SubscribeEvent
    public static void registerTrades(VillagerTradesEvent event) {
        
        if (event.getType() == VillagerProfession.CARTOGRAPHER) {
			event.getTrades().get(3).add(new TrialsMapTrade(12, 12, 1));
        }
    }
}