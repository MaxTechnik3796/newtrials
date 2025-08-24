package cz.maxtechnik.ntrials;

import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.common.Mod;
import cz.maxtechnik.ntrials.init.NTrialsModBlocks;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

@Mod.EventBusSubscriber(modid = NTrialsMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NTrialsModEvents {

    // Mapa pro oxidační vztahy - inicializuje se až po registraci bloků
    public static BiMap<Block, Block> OXIDATION_LEVEL_INCREASES = HashBiMap.create();

    public static void setupOxidation() {
        // Nyní bezpečně inicializujeme mapu po registraci bloků
        OXIDATION_LEVEL_INCREASES.put(NTrialsModBlocks.CUSTOM_COPPER.get(), NTrialsModBlocks.EXPOSED_CUSTOM_COPPER.get());
        OXIDATION_LEVEL_INCREASES.put(NTrialsModBlocks.EXPOSED_CUSTOM_COPPER.get(), NTrialsModBlocks.WEATHERED_CUSTOM_COPPER.get());
        OXIDATION_LEVEL_INCREASES.put(NTrialsModBlocks.WEATHERED_CUSTOM_COPPER.get(), NTrialsModBlocks.OXIDIZED_CUSTOM_COPPER.get());
    }

    // Pro Minecraft 1.20.1 Forge zatím odstraníme waxování - bude třeba implementovat jinak
    // V této verzi Forge nemůžeme přímo modifikovat HoneycombItem mapy
}
