package cz.maxtechnik.ntrials.init.events;

import cz.maxtechnik.ntrials.NTrialsMod;
import cz.maxtechnik.ntrials.item.WindChargeDispenserBehavior;
import cz.maxtechnik.ntrials.init.basic.NTrialsModItems;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.fml.common.Mod;
import cz.maxtechnik.ntrials.init.basic.NTrialsModBlocks;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

@Mod.EventBusSubscriber(modid=NTrialsMod.MODID,bus=Mod.EventBusSubscriber.Bus.MOD)
public class NTrialsMod_ModModEvents{

    public static BiMap<Block, Block> OXIDATION_LEVEL_INCREASES = HashBiMap.create();
    public static BiMap<Block, Block> WAXING_MAP = HashBiMap.create();
    public static BiMap<Block, Block> UNWAXING_MAP = HashBiMap.create();
    public static BiMap<Block, Block> SCRAPING_MAP = HashBiMap.create();

    public static void setupOxidation(){
        // Inicializace po registraci bloků
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
        // Oxidace pro COPPER DOORS
        OXIDATION_LEVEL_INCREASES.put(NTrialsModBlocks.COPPER_BULB.get(),NTrialsModBlocks.EXPOSED_COPPER_BULB.get());
        OXIDATION_LEVEL_INCREASES.put(NTrialsModBlocks.EXPOSED_COPPER_BULB.get(),NTrialsModBlocks.WEATHERED_COPPER_BULB.get());
        OXIDATION_LEVEL_INCREASES.put(NTrialsModBlocks.WEATHERED_COPPER_BULB.get(),NTrialsModBlocks.OXIDIZED_COPPER_BULB.get());
        // Oxidace pro COPPER bulb
        OXIDATION_LEVEL_INCREASES.put(NTrialsModBlocks.COPPER_DOOR.get(),NTrialsModBlocks.EXPOSED_COPPER_DOOR.get());
        OXIDATION_LEVEL_INCREASES.put(NTrialsModBlocks.EXPOSED_COPPER_DOOR.get(),NTrialsModBlocks.WEATHERED_COPPER_DOOR.get());
        OXIDATION_LEVEL_INCREASES.put(NTrialsModBlocks.WEATHERED_COPPER_DOOR.get(),NTrialsModBlocks.OXIDIZED_COPPER_DOOR.get());
        // Waxování
        WAXING_MAP.put(NTrialsModBlocks.CHISELED_COPPER.get(), NTrialsModBlocks.WAXED_CHISELED_COPPER.get());
        WAXING_MAP.put(NTrialsModBlocks.EXPOSED_CHISELED_COPPER.get(), NTrialsModBlocks.WAXED_EXPOSED_CHISELED_COPPER.get());
        WAXING_MAP.put(NTrialsModBlocks.WEATHERED_CHISELED_COPPER.get(), NTrialsModBlocks.WAXED_WEATHERED_CHISELED_COPPER.get());
        WAXING_MAP.put(NTrialsModBlocks.OXIDIZED_CHISELED_COPPER.get(), NTrialsModBlocks.WAXED_OXIDIZED_CHISELED_COPPER.get());
        // Waxování
        WAXING_MAP.put(NTrialsModBlocks.COPPER_GRATE.get(), NTrialsModBlocks.WAXED_COPPER_GRATE.get());
        WAXING_MAP.put(NTrialsModBlocks.EXPOSED_COPPER_GRATE.get(), NTrialsModBlocks.WAXED_EXPOSED_COPPER_GRATE.get());
        WAXING_MAP.put(NTrialsModBlocks.WEATHERED_COPPER_GRATE.get(), NTrialsModBlocks.WAXED_WEATHERED_COPPER_GRATE.get());
        WAXING_MAP.put(NTrialsModBlocks.OXIDIZED_COPPER_GRATE.get(), NTrialsModBlocks.WAXED_OXIDIZED_COPPER_GRATE.get());
        // Waxování
        WAXING_MAP.put(NTrialsModBlocks.COPPER_TRAPDOOR.get(), NTrialsModBlocks.WAXED_COPPER_TRAPDOOR.get());
        WAXING_MAP.put(NTrialsModBlocks.EXPOSED_COPPER_TRAPDOOR.get(), NTrialsModBlocks.WAXED_EXPOSED_COPPER_TRAPDOOR.get());
        WAXING_MAP.put(NTrialsModBlocks.WEATHERED_COPPER_TRAPDOOR.get(), NTrialsModBlocks.WAXED_WEATHERED_COPPER_TRAPDOOR.get());
        WAXING_MAP.put(NTrialsModBlocks.OXIDIZED_COPPER_TRAPDOOR.get(), NTrialsModBlocks.WAXED_OXIDIZED_COPPER_TRAPDOOR.get());
        // Waxování COPPER DOORS
        WAXING_MAP.put(NTrialsModBlocks.COPPER_DOOR.get(), NTrialsModBlocks.WAXED_COPPER_DOOR.get());
        WAXING_MAP.put(NTrialsModBlocks.EXPOSED_COPPER_DOOR.get(), NTrialsModBlocks.WAXED_EXPOSED_COPPER_DOOR.get());
        WAXING_MAP.put(NTrialsModBlocks.WEATHERED_COPPER_DOOR.get(), NTrialsModBlocks.WAXED_WEATHERED_COPPER_DOOR.get());
        WAXING_MAP.put(NTrialsModBlocks.OXIDIZED_COPPER_DOOR.get(), NTrialsModBlocks.WAXED_OXIDIZED_COPPER_DOOR.get());
        // Waxování COPPER bulb
        WAXING_MAP.put(NTrialsModBlocks.COPPER_BULB.get(), NTrialsModBlocks.WAXED_COPPER_BULB.get());
        WAXING_MAP.put(NTrialsModBlocks.EXPOSED_COPPER_BULB.get(), NTrialsModBlocks.WAXED_EXPOSED_COPPER_BULB.get());
        WAXING_MAP.put(NTrialsModBlocks.WEATHERED_COPPER_BULB.get(), NTrialsModBlocks.WAXED_WEATHERED_COPPER_BULB.get());
        WAXING_MAP.put(NTrialsModBlocks.OXIDIZED_COPPER_BULB.get(), NTrialsModBlocks.WAXED_OXIDIZED_COPPER_BULB.get());
        // Unwaxování
        UNWAXING_MAP.put(NTrialsModBlocks.WAXED_CHISELED_COPPER.get(), NTrialsModBlocks.CHISELED_COPPER.get());
        UNWAXING_MAP.put(NTrialsModBlocks.WAXED_EXPOSED_CHISELED_COPPER.get(), NTrialsModBlocks.EXPOSED_CHISELED_COPPER.get());
        UNWAXING_MAP.put(NTrialsModBlocks.WAXED_WEATHERED_CHISELED_COPPER.get(), NTrialsModBlocks.WEATHERED_CHISELED_COPPER.get());
        UNWAXING_MAP.put(NTrialsModBlocks.WAXED_OXIDIZED_CHISELED_COPPER.get(), NTrialsModBlocks.OXIDIZED_CHISELED_COPPER.get());
        // Unwaxování bulb
        UNWAXING_MAP.put(NTrialsModBlocks.WAXED_COPPER_BULB.get(), NTrialsModBlocks.COPPER_BULB.get());
        UNWAXING_MAP.put(NTrialsModBlocks.WAXED_EXPOSED_COPPER_BULB.get(), NTrialsModBlocks.EXPOSED_COPPER_BULB.get());
        UNWAXING_MAP.put(NTrialsModBlocks.WAXED_WEATHERED_COPPER_BULB.get(), NTrialsModBlocks.WEATHERED_COPPER_BULB.get());
        UNWAXING_MAP.put(NTrialsModBlocks.WAXED_OXIDIZED_COPPER_BULB.get(), NTrialsModBlocks.OXIDIZED_COPPER_BULB.get());
        //unwaxing for copper grate
        UNWAXING_MAP.put(NTrialsModBlocks.WAXED_COPPER_GRATE.get(), NTrialsModBlocks.COPPER_GRATE.get());
        UNWAXING_MAP.put(NTrialsModBlocks.WAXED_EXPOSED_COPPER_GRATE.get(), NTrialsModBlocks.EXPOSED_COPPER_GRATE.get());
        UNWAXING_MAP.put(NTrialsModBlocks.WAXED_WEATHERED_COPPER_GRATE.get(), NTrialsModBlocks.WEATHERED_COPPER_GRATE.get());
        UNWAXING_MAP.put(NTrialsModBlocks.WAXED_OXIDIZED_COPPER_GRATE.get(), NTrialsModBlocks.OXIDIZED_COPPER_GRATE.get());
        // Unwaxování copper trapdoor
        UNWAXING_MAP.put(NTrialsModBlocks.WAXED_COPPER_TRAPDOOR.get(), NTrialsModBlocks.COPPER_TRAPDOOR.get());
        UNWAXING_MAP.put(NTrialsModBlocks.WAXED_EXPOSED_COPPER_TRAPDOOR.get(), NTrialsModBlocks.EXPOSED_COPPER_TRAPDOOR.get());
        UNWAXING_MAP.put(NTrialsModBlocks.WAXED_WEATHERED_COPPER_TRAPDOOR.get(), NTrialsModBlocks.WEATHERED_COPPER_TRAPDOOR.get());
        UNWAXING_MAP.put(NTrialsModBlocks.WAXED_OXIDIZED_COPPER_TRAPDOOR.get(), NTrialsModBlocks.OXIDIZED_COPPER_TRAPDOOR.get());
        // Unwaxování COPPER DOORS
        UNWAXING_MAP.put(NTrialsModBlocks.WAXED_COPPER_DOOR.get(), NTrialsModBlocks.COPPER_DOOR.get());
        UNWAXING_MAP.put(NTrialsModBlocks.WAXED_EXPOSED_COPPER_DOOR.get(), NTrialsModBlocks.EXPOSED_COPPER_DOOR.get());
        UNWAXING_MAP.put(NTrialsModBlocks.WAXED_WEATHERED_COPPER_DOOR.get(), NTrialsModBlocks.WEATHERED_COPPER_DOOR.get());
        UNWAXING_MAP.put(NTrialsModBlocks.WAXED_OXIDIZED_COPPER_DOOR.get(), NTrialsModBlocks.OXIDIZED_COPPER_DOOR.get());
        // Scraping
        SCRAPING_MAP.put(NTrialsModBlocks.EXPOSED_CHISELED_COPPER.get(), NTrialsModBlocks.CHISELED_COPPER.get());
        SCRAPING_MAP.put(NTrialsModBlocks.WEATHERED_CHISELED_COPPER.get(), NTrialsModBlocks.EXPOSED_CHISELED_COPPER.get());
        SCRAPING_MAP.put(NTrialsModBlocks.OXIDIZED_CHISELED_COPPER.get(), NTrialsModBlocks.WEATHERED_CHISELED_COPPER.get());
        // Scraping COPPER GRATE
        SCRAPING_MAP.put(NTrialsModBlocks.EXPOSED_COPPER_GRATE.get(), NTrialsModBlocks.COPPER_GRATE.get());
        SCRAPING_MAP.put(NTrialsModBlocks.WEATHERED_COPPER_GRATE.get(), NTrialsModBlocks.EXPOSED_COPPER_GRATE.get());
        SCRAPING_MAP.put(NTrialsModBlocks.OXIDIZED_COPPER_GRATE.get(), NTrialsModBlocks.WEATHERED_COPPER_GRATE.get());
        // Scraping COPPER TRAPDOOR
        SCRAPING_MAP.put(NTrialsModBlocks.EXPOSED_COPPER_TRAPDOOR.get(), NTrialsModBlocks.COPPER_TRAPDOOR.get());
        SCRAPING_MAP.put(NTrialsModBlocks.WEATHERED_COPPER_TRAPDOOR.get(), NTrialsModBlocks.EXPOSED_COPPER_TRAPDOOR.get());
        SCRAPING_MAP.put(NTrialsModBlocks.OXIDIZED_COPPER_TRAPDOOR.get(), NTrialsModBlocks.WEATHERED_COPPER_TRAPDOOR.get());
        // Scraping COPPER DOORS
        SCRAPING_MAP.put(NTrialsModBlocks.EXPOSED_COPPER_DOOR.get(), NTrialsModBlocks.COPPER_DOOR.get());
        SCRAPING_MAP.put(NTrialsModBlocks.WEATHERED_COPPER_DOOR.get(), NTrialsModBlocks.EXPOSED_COPPER_DOOR.get());
        SCRAPING_MAP.put(NTrialsModBlocks.OXIDIZED_COPPER_DOOR.get(), NTrialsModBlocks.WEATHERED_COPPER_DOOR.get());
        // // Scraping COPPER DOORS
        SCRAPING_MAP.put(NTrialsModBlocks.EXPOSED_COPPER_BULB.get(), NTrialsModBlocks.COPPER_BULB.get());
        SCRAPING_MAP.put(NTrialsModBlocks.WEATHERED_COPPER_BULB.get(), NTrialsModBlocks.EXPOSED_COPPER_BULB.get());
        SCRAPING_MAP.put(NTrialsModBlocks.OXIDIZED_COPPER_BULB.get(), NTrialsModBlocks.WEATHERED_COPPER_BULB.get());
    }

    public static void setupDispenserBehaviors(){
        // Register Wind Charge dispenser behavior
        DispenserBlock.registerBehavior(NTrialsModItems.WIND_CHARGE.get(), new WindChargeDispenserBehavior());
    }
}
