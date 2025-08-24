package cz.maxtechnik.ntrials;

/*import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.common.Mod;
import cz.maxtechnik.ntrials.init.NTrialsModBlocks;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = NTrialsMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NTrialsModEvents {

    public static void setupWaxables() {
        registerWaxable(NTrialsModBlocks.CUSTOM_COPPER, NTrialsModBlocks.WAXED_CUSTOM_COPPER);
        registerWaxable(NTrialsModBlocks.EXPOSED_CUSTOM_COPPER, NTrialsModBlocks.WAXED_EXPOSED_CUSTOM_COPPER);
        registerWaxable(NTrialsModBlocks.WEATHERED_CUSTOM_COPPER, NTrialsModBlocks.WAXED_WEATHERED_CUSTOM_COPPER);
        registerWaxable(NTrialsModBlocks.OXIDIZED_CUSTOM_COPPER, NTrialsModBlocks.WAXED_OXIDIZED_CUSTOM_COPPER);
    }

    public static void setupUnwaxables() {
        registerUnwaxable(NTrialsModBlocks.WAXED_CUSTOM_COPPER, NTrialsModBlocks.CUSTOM_COPPER);
        registerUnwaxable(NTrialsModBlocks.WAXED_EXPOSED_CUSTOM_COPPER, NTrialsModBlocks.EXPOSED_CUSTOM_COPPER);
        registerUnwaxable(NTrialsModBlocks.WAXED_WEATHERED_CUSTOM_COPPER, NTrialsModBlocks.WEATHERED_CUSTOM_COPPER);
        registerUnwaxable(NTrialsModBlocks.WAXED_OXIDIZED_CUSTOM_COPPER, NTrialsModBlocks.OXIDIZED_CUSTOM_COPPER);
    }

    public static void setupScrapables() {
        registerScrapable(NTrialsModBlocks.EXPOSED_CUSTOM_COPPER, NTrialsModBlocks.CUSTOM_COPPER);
        registerScrapable(NTrialsModBlocks.WEATHERED_CUSTOM_COPPER, NTrialsModBlocks.EXPOSED_CUSTOM_COPPER);
        registerScrapable(NTrialsModBlocks.OXIDIZED_CUSTOM_COPPER, NTrialsModBlocks.WEATHERED_CUSTOM_COPPER);
    }

    // ---- Helpers ----
    private static void registerWaxable(Supplier<? extends Block> from, Supplier<? extends Block> to) {
        HoneycombItem.WAXABLES.put(from.get(), to.get());
        HoneycombItem.WAXABLES.
    }

    private static void registerUnwaxable(Supplier<? extends Block> from, Supplier<? extends Block> to) {
        HoneycombItem.WAX_OFF_BY_BLOCK.put(from.get(), to.get());
    }

    private static void registerScrapable(Supplier<? extends Block> from, Supplier<? extends Block> to) {
        AxeItem.OXIDATION_REMOVAL.put(from.get(), to.get());
    }
}*/