package cz.maxtechnik.ntrials;

import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.common.Mod;
import cz.maxtechnik.ntrials.init.NTrialsModBlocks;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

@Mod.EventBusSubscriber(modid=NTrialsMod.MODID,bus=Mod.EventBusSubscriber.Bus.MOD)
public class NTrialsModEvents{

    // Mapa pro oxidační vztahy - inicializuje se až po registraci bloků
    public static BiMap<Block, Block> OXIDATION_LEVEL_INCREASES = HashBiMap.create();

    // Mapy pro waxování a unwaxování vztahy
    public static BiMap<Block, Block> WAXING_MAP = HashBiMap.create();
    public static BiMap<Block, Block> UNWAXING_MAP = HashBiMap.create();

    // Mapa pro scraping (opak oxidace) - posun o stupeň zpět
    public static BiMap<Block, Block> SCRAPING_MAP = HashBiMap.create();

    public static void setupOxidation(){
        // Nyní bezpečně inicializujeme mapu po registraci bloků - používame správné názvy
        OXIDATION_LEVEL_INCREASES.put(NTrialsModBlocks.CHISELED_COPPER.get(),NTrialsModBlocks.EXPOSED_CHISELED_COPPER.get());
        OXIDATION_LEVEL_INCREASES.put(NTrialsModBlocks.EXPOSED_CHISELED_COPPER.get(),NTrialsModBlocks.WEATHERED_CHISELED_COPPER.get());
        OXIDATION_LEVEL_INCREASES.put(NTrialsModBlocks.WEATHERED_CHISELED_COPPER.get(),NTrialsModBlocks.OXIDIZED_CHISELED_COPPER.get());

        //oxidace pro copper grate
        OXIDATION_LEVEL_INCREASES.put(NTrialsModBlocks.COPPER_GRATE.get(),NTrialsModBlocks.EXPOSED_COPPER_GRATE.get());
        OXIDATION_LEVEL_INCREASES.put(NTrialsModBlocks.EXPOSED_COPPER_GRATE.get(),NTrialsModBlocks.WEATHERED_COPPER_GRATE.get());
        OXIDATION_LEVEL_INCREASES.put(NTrialsModBlocks.WEATHERED_COPPER_GRATE.get(),NTrialsModBlocks.OXIDIZED_COPPER_GRATE.get());

        //oxidace pro copper trapdoor
        OXIDATION_LEVEL_INCREASES.put(NTrialsModBlocks.COPPER_TRAPDOOR.get(),NTrialsModBlocks.EXPOSED_COPPER_TRAPDOOR.get());
        OXIDATION_LEVEL_INCREASES.put(NTrialsModBlocks.EXPOSED_COPPER_TRAPDOOR.get(),NTrialsModBlocks.WEATHERED_COPPER_TRAPDOOR.get());
        OXIDATION_LEVEL_INCREASES.put(NTrialsModBlocks.WEATHERED_COPPER_TRAPDOOR.get(),NTrialsModBlocks.OXIDIZED_COPPER_TRAPDOOR.get());

        // Oxidační mapy pro COPPER DOORS
        OXIDATION_LEVEL_INCREASES.put(NTrialsModBlocks.COPPER_BULB.get(),NTrialsModBlocks.EXPOSED_COPPER_BULB.get());
        OXIDATION_LEVEL_INCREASES.put(NTrialsModBlocks.EXPOSED_COPPER_BULB.get(),NTrialsModBlocks.WEATHERED_COPPER_BULB.get());
        OXIDATION_LEVEL_INCREASES.put(NTrialsModBlocks.WEATHERED_COPPER_BULB.get(),NTrialsModBlocks.OXIDIZED_COPPER_BULB.get());

        // Oxidační mapy pro COPPER bulb
        OXIDATION_LEVEL_INCREASES.put(NTrialsModBlocks.COPPER_DOOR.get(),NTrialsModBlocks.EXPOSED_COPPER_DOOR.get());
        OXIDATION_LEVEL_INCREASES.put(NTrialsModBlocks.EXPOSED_COPPER_DOOR.get(),NTrialsModBlocks.WEATHERED_COPPER_DOOR.get());
        OXIDATION_LEVEL_INCREASES.put(NTrialsModBlocks.WEATHERED_COPPER_DOOR.get(),NTrialsModBlocks.OXIDIZED_COPPER_DOOR.get());

        // Inicializujeme waxování mapy
        WAXING_MAP.put(NTrialsModBlocks.CHISELED_COPPER.get(), NTrialsModBlocks.WAXED_CHISELED_COPPER.get());
        WAXING_MAP.put(NTrialsModBlocks.EXPOSED_CHISELED_COPPER.get(), NTrialsModBlocks.WAXED_EXPOSED_CHISELED_COPPER.get());
        WAXING_MAP.put(NTrialsModBlocks.WEATHERED_CHISELED_COPPER.get(), NTrialsModBlocks.WAXED_WEATHERED_CHISELED_COPPER.get());
        WAXING_MAP.put(NTrialsModBlocks.OXIDIZED_CHISELED_COPPER.get(), NTrialsModBlocks.WAXED_OXIDIZED_CHISELED_COPPER.get());

        //waxing map for copper grate
        WAXING_MAP.put(NTrialsModBlocks.COPPER_GRATE.get(), NTrialsModBlocks.WAXED_COPPER_GRATE.get());
        WAXING_MAP.put(NTrialsModBlocks.EXPOSED_COPPER_GRATE.get(), NTrialsModBlocks.WAXED_EXPOSED_COPPER_GRATE.get());
        WAXING_MAP.put(NTrialsModBlocks.WEATHERED_COPPER_GRATE.get(), NTrialsModBlocks.WAXED_WEATHERED_COPPER_GRATE.get());
        WAXING_MAP.put(NTrialsModBlocks.OXIDIZED_COPPER_GRATE.get(), NTrialsModBlocks.WAXED_OXIDIZED_COPPER_GRATE.get());

        //waxování mapy pro copper trapdoor
        WAXING_MAP.put(NTrialsModBlocks.COPPER_TRAPDOOR.get(), NTrialsModBlocks.WAXED_COPPER_TRAPDOOR.get());
        WAXING_MAP.put(NTrialsModBlocks.EXPOSED_COPPER_TRAPDOOR.get(), NTrialsModBlocks.WAXED_EXPOSED_COPPER_TRAPDOOR.get());
        WAXING_MAP.put(NTrialsModBlocks.WEATHERED_COPPER_TRAPDOOR.get(), NTrialsModBlocks.WAXED_WEATHERED_COPPER_TRAPDOOR.get());
        WAXING_MAP.put(NTrialsModBlocks.OXIDIZED_COPPER_TRAPDOOR.get(), NTrialsModBlocks.WAXED_OXIDIZED_COPPER_TRAPDOOR.get());

        // Waxování mapy pro COPPER DOORS
        WAXING_MAP.put(NTrialsModBlocks.COPPER_DOOR.get(), NTrialsModBlocks.WAXED_COPPER_DOOR.get());
        WAXING_MAP.put(NTrialsModBlocks.EXPOSED_COPPER_DOOR.get(), NTrialsModBlocks.WAXED_EXPOSED_COPPER_DOOR.get());
        WAXING_MAP.put(NTrialsModBlocks.WEATHERED_COPPER_DOOR.get(), NTrialsModBlocks.WAXED_WEATHERED_COPPER_DOOR.get());
        WAXING_MAP.put(NTrialsModBlocks.OXIDIZED_COPPER_DOOR.get(), NTrialsModBlocks.WAXED_OXIDIZED_COPPER_DOOR.get());

        // Waxování mapy pro COPPER bulb
        WAXING_MAP.put(NTrialsModBlocks.COPPER_BULB.get(), NTrialsModBlocks.WAXED_COPPER_BULB.get());
        WAXING_MAP.put(NTrialsModBlocks.EXPOSED_COPPER_BULB.get(), NTrialsModBlocks.WAXED_EXPOSED_COPPER_BULB.get());
        WAXING_MAP.put(NTrialsModBlocks.WEATHERED_COPPER_BULB.get(), NTrialsModBlocks.WAXED_WEATHERED_COPPER_BULB.get());
        WAXING_MAP.put(NTrialsModBlocks.OXIDIZED_COPPER_BULB.get(), NTrialsModBlocks.WAXED_OXIDIZED_COPPER_BULB.get());

        // Unwaxování je opačná operace
        UNWAXING_MAP.put(NTrialsModBlocks.WAXED_CHISELED_COPPER.get(), NTrialsModBlocks.CHISELED_COPPER.get());
        UNWAXING_MAP.put(NTrialsModBlocks.WAXED_EXPOSED_CHISELED_COPPER.get(), NTrialsModBlocks.EXPOSED_CHISELED_COPPER.get());
        UNWAXING_MAP.put(NTrialsModBlocks.WAXED_WEATHERED_CHISELED_COPPER.get(), NTrialsModBlocks.WEATHERED_CHISELED_COPPER.get());
        UNWAXING_MAP.put(NTrialsModBlocks.WAXED_OXIDIZED_CHISELED_COPPER.get(), NTrialsModBlocks.OXIDIZED_CHISELED_COPPER.get());

        //unwaxing map for bulb
        UNWAXING_MAP.put(NTrialsModBlocks.WAXED_COPPER_BULB.get(), NTrialsModBlocks.COPPER_BULB.get());
        UNWAXING_MAP.put(NTrialsModBlocks.WAXED_EXPOSED_COPPER_BULB.get(), NTrialsModBlocks.EXPOSED_COPPER_BULB.get());
        UNWAXING_MAP.put(NTrialsModBlocks.WAXED_WEATHERED_COPPER_BULB.get(), NTrialsModBlocks.WEATHERED_COPPER_BULB.get());
        UNWAXING_MAP.put(NTrialsModBlocks.WAXED_OXIDIZED_COPPER_BULB.get(), NTrialsModBlocks.OXIDIZED_COPPER_BULB.get());

        //unwaxing for copper grate
        UNWAXING_MAP.put(NTrialsModBlocks.WAXED_COPPER_GRATE.get(), NTrialsModBlocks.COPPER_GRATE.get());
        UNWAXING_MAP.put(NTrialsModBlocks.WAXED_EXPOSED_COPPER_GRATE.get(), NTrialsModBlocks.EXPOSED_COPPER_GRATE.get());
        UNWAXING_MAP.put(NTrialsModBlocks.WAXED_WEATHERED_COPPER_GRATE.get(), NTrialsModBlocks.WEATHERED_COPPER_GRATE.get());
        UNWAXING_MAP.put(NTrialsModBlocks.WAXED_OXIDIZED_COPPER_GRATE.get(), NTrialsModBlocks.OXIDIZED_COPPER_GRATE.get());
        // Unwaxování pro copper trapdoor
        UNWAXING_MAP.put(NTrialsModBlocks.WAXED_COPPER_TRAPDOOR.get(), NTrialsModBlocks.COPPER_TRAPDOOR.get());
        UNWAXING_MAP.put(NTrialsModBlocks.WAXED_EXPOSED_COPPER_TRAPDOOR.get(), NTrialsModBlocks.EXPOSED_COPPER_TRAPDOOR.get());
        UNWAXING_MAP.put(NTrialsModBlocks.WAXED_WEATHERED_COPPER_TRAPDOOR.get(), NTrialsModBlocks.WEATHERED_COPPER_TRAPDOOR.get());
        UNWAXING_MAP.put(NTrialsModBlocks.WAXED_OXIDIZED_COPPER_TRAPDOOR.get(), NTrialsModBlocks.OXIDIZED_COPPER_TRAPDOOR.get());

        // Unwaxování mapy pro COPPER DOORS
        UNWAXING_MAP.put(NTrialsModBlocks.WAXED_COPPER_DOOR.get(), NTrialsModBlocks.COPPER_DOOR.get());
        UNWAXING_MAP.put(NTrialsModBlocks.WAXED_EXPOSED_COPPER_DOOR.get(), NTrialsModBlocks.EXPOSED_COPPER_DOOR.get());
        UNWAXING_MAP.put(NTrialsModBlocks.WAXED_WEATHERED_COPPER_DOOR.get(), NTrialsModBlocks.WEATHERED_COPPER_DOOR.get());
        UNWAXING_MAP.put(NTrialsModBlocks.WAXED_OXIDIZED_COPPER_DOOR.get(), NTrialsModBlocks.OXIDIZED_COPPER_DOOR.get());

        // Scraping mapu - opak oxidace (posun o stupeň zpět)
        SCRAPING_MAP.put(NTrialsModBlocks.EXPOSED_CHISELED_COPPER.get(), NTrialsModBlocks.CHISELED_COPPER.get());
        SCRAPING_MAP.put(NTrialsModBlocks.WEATHERED_CHISELED_COPPER.get(), NTrialsModBlocks.EXPOSED_CHISELED_COPPER.get());
        SCRAPING_MAP.put(NTrialsModBlocks.OXIDIZED_CHISELED_COPPER.get(), NTrialsModBlocks.WEATHERED_CHISELED_COPPER.get());

        // Scraping mapy pro COPPER GRATE
        SCRAPING_MAP.put(NTrialsModBlocks.EXPOSED_COPPER_GRATE.get(), NTrialsModBlocks.COPPER_GRATE.get());
        SCRAPING_MAP.put(NTrialsModBlocks.WEATHERED_COPPER_GRATE.get(), NTrialsModBlocks.EXPOSED_COPPER_GRATE.get());
        SCRAPING_MAP.put(NTrialsModBlocks.OXIDIZED_COPPER_GRATE.get(), NTrialsModBlocks.WEATHERED_COPPER_GRATE.get());
        // Scraping mapy pro COPPER TRAPDOOR
        SCRAPING_MAP.put(NTrialsModBlocks.EXPOSED_COPPER_TRAPDOOR.get(), NTrialsModBlocks.COPPER_TRAPDOOR.get());
        SCRAPING_MAP.put(NTrialsModBlocks.WEATHERED_COPPER_TRAPDOOR.get(), NTrialsModBlocks.EXPOSED_COPPER_TRAPDOOR.get());
        SCRAPING_MAP.put(NTrialsModBlocks.OXIDIZED_COPPER_TRAPDOOR.get(), NTrialsModBlocks.WEATHERED_COPPER_TRAPDOOR.get());

        // Scraping mapy pro COPPER DOORS
        SCRAPING_MAP.put(NTrialsModBlocks.EXPOSED_COPPER_DOOR.get(), NTrialsModBlocks.COPPER_DOOR.get());
        SCRAPING_MAP.put(NTrialsModBlocks.WEATHERED_COPPER_DOOR.get(), NTrialsModBlocks.EXPOSED_COPPER_DOOR.get());
        SCRAPING_MAP.put(NTrialsModBlocks.OXIDIZED_COPPER_DOOR.get(), NTrialsModBlocks.WEATHERED_COPPER_DOOR.get());
        // Poznámka: CHISELED_COPPER a COPPER_DOOR (první fáze) nemají předchozí stupeň, takže se v mapě nenacházej
        //
        // // Scraping mapy pro COPPER DOORS
        SCRAPING_MAP.put(NTrialsModBlocks.EXPOSED_COPPER_BULB.get(), NTrialsModBlocks.COPPER_BULB.get());
        SCRAPING_MAP.put(NTrialsModBlocks.WEATHERED_COPPER_BULB.get(), NTrialsModBlocks.EXPOSED_COPPER_BULB.get());
        SCRAPING_MAP.put(NTrialsModBlocks.OXIDIZED_COPPER_BULB.get(), NTrialsModBlocks.WEATHERED_COPPER_BULB.get());
    }
    // Pro Minecraft 1.20.1 Forge zatím odstraníme waxování - bude třeba implementovat jinak
    // V této verzi Forge nemůžeme přímo modifikovat HoneycombItem mapy
}
