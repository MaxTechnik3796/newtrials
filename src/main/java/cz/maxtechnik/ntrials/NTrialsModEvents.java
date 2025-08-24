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

    public static void setupOxidation(){
        // Nyní bezpečně inicializujeme mapu po registraci bloků - používame správné názvy
        OXIDATION_LEVEL_INCREASES.put(NTrialsModBlocks.CHISELED_COPPER.get(),NTrialsModBlocks.EXPOSED_CHISELED_COPPER.get());
        OXIDATION_LEVEL_INCREASES.put(NTrialsModBlocks.EXPOSED_CHISELED_COPPER.get(),NTrialsModBlocks.WEATHERED_CHISELED_COPPER.get());
        OXIDATION_LEVEL_INCREASES.put(NTrialsModBlocks.WEATHERED_CHISELED_COPPER.get(),NTrialsModBlocks.OXIDIZED_CHISELED_COPPER.get());

        // Inicializujeme waxování mapy
        WAXING_MAP.put(NTrialsModBlocks.CHISELED_COPPER.get(), NTrialsModBlocks.WAXED_CHISELED_COPPER.get());
        WAXING_MAP.put(NTrialsModBlocks.EXPOSED_CHISELED_COPPER.get(), NTrialsModBlocks.WAXED_EXPOSED_CHISELED_COPPER.get());
        WAXING_MAP.put(NTrialsModBlocks.WEATHERED_CHISELED_COPPER.get(), NTrialsModBlocks.WAXED_WEATHERED_CHISELED_COPPER.get());
        WAXING_MAP.put(NTrialsModBlocks.OXIDIZED_CHISELED_COPPER.get(), NTrialsModBlocks.WAXED_OXIDIZED_CHISELED_COPPER.get());

        // Unwaxování je opačná operace
        UNWAXING_MAP.put(NTrialsModBlocks.WAXED_CHISELED_COPPER.get(), NTrialsModBlocks.CHISELED_COPPER.get());
        UNWAXING_MAP.put(NTrialsModBlocks.WAXED_EXPOSED_CHISELED_COPPER.get(), NTrialsModBlocks.EXPOSED_CHISELED_COPPER.get());
        UNWAXING_MAP.put(NTrialsModBlocks.WAXED_WEATHERED_CHISELED_COPPER.get(), NTrialsModBlocks.WEATHERED_CHISELED_COPPER.get());
        UNWAXING_MAP.put(NTrialsModBlocks.WAXED_OXIDIZED_CHISELED_COPPER.get(), NTrialsModBlocks.OXIDIZED_CHISELED_COPPER.get());
    }
    // Pro Minecraft 1.20.1 Forge zatím odstraníme waxování - bude třeba implementovat jinak
    // V této verzi Forge nemůžeme přímo modifikovat HoneycombItem mapy
}
