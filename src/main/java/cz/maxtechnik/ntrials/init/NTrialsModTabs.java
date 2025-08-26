package cz.maxtechnik.ntrials.init;

import cz.maxtechnik.ntrials.NTrialsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class NTrialsModTabs{
    public static final DeferredRegister<CreativeModeTab>REGISTER=DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NTrialsMod.MODID);
    public static final RegistryObject<CreativeModeTab>MAIN=REGISTER.register("main",()-> CreativeModeTab.builder().title(Component.translatable("creative_tab.ntrials.main")).icon(()->new ItemStack(NTrialsModBlocks.TUFF_BRICKS.get().asItem())).displayItems(((parameters,tabData)->{
        tabData.accept(NTrialsModBlocks.HEAVY_CORE.get().asItem());

        tabData.accept(NTrialsModBlocks.TUFF_BRICKS.get().asItem());
        tabData.accept(NTrialsModBlocks.CHISELED_TUFF.get().asItem());
        tabData.accept(NTrialsModBlocks.CHISELED_TUFF_BRICKS.get().asItem());
        tabData.accept(NTrialsModBlocks.POLISHED_TUFF.get().asItem());

        tabData.accept(NTrialsModBlocks.TUFF_STAIRS.get().asItem());
        tabData.accept(NTrialsModBlocks.TUFF_BRICK_STAIRS.get().asItem());
        tabData.accept(NTrialsModBlocks.POLISHED_TUFF_STAIRS.get().asItem());

        tabData.accept(NTrialsModBlocks.TUFF_SLAB.get().asItem());
        tabData.accept(NTrialsModBlocks.TUFF_BRICK_SLAB.get().asItem());
        tabData.accept(NTrialsModBlocks.POLISHED_TUFF_SLAB.get().asItem());

        tabData.accept(NTrialsModBlocks.TUFF_WALL.get().asItem());
        tabData.accept(NTrialsModBlocks.TUFF_BRICK_WALL.get().asItem());
        tabData.accept(NTrialsModBlocks.POLISHED_TUFF_WALL.get().asItem());

        tabData.accept(NTrialsModBlocks.CHISELED_COPPER.get().asItem());
        tabData.accept(NTrialsModBlocks.EXPOSED_CHISELED_COPPER.get().asItem());
        tabData.accept(NTrialsModBlocks.WEATHERED_CHISELED_COPPER.get().asItem());
        tabData.accept(NTrialsModBlocks.OXIDIZED_CHISELED_COPPER.get().asItem());
        tabData.accept(NTrialsModBlocks.WAXED_CHISELED_COPPER.get().asItem());
        tabData.accept(NTrialsModBlocks.WAXED_EXPOSED_CHISELED_COPPER.get().asItem());
        tabData.accept(NTrialsModBlocks.WAXED_WEATHERED_CHISELED_COPPER.get().asItem());
        tabData.accept(NTrialsModBlocks.WAXED_OXIDIZED_CHISELED_COPPER.get().asItem());

        tabData.accept(NTrialsModBlocks.COPPER_DOOR.get().asItem());
        tabData.accept(NTrialsModBlocks.EXPOSED_COPPER_DOOR.get().asItem());
        tabData.accept(NTrialsModBlocks.WEATHERED_COPPER_DOOR.get().asItem());
        tabData.accept(NTrialsModBlocks.OXIDIZED_COPPER_DOOR.get().asItem());
        tabData.accept(NTrialsModBlocks.WAXED_COPPER_DOOR.get().asItem());
        tabData.accept(NTrialsModBlocks.WAXED_EXPOSED_COPPER_DOOR.get().asItem());
        tabData.accept(NTrialsModBlocks.WAXED_WEATHERED_COPPER_DOOR.get().asItem());
        tabData.accept(NTrialsModBlocks.WAXED_OXIDIZED_COPPER_DOOR.get().asItem());

        tabData.accept(NTrialsModBlocks.COPPER_TRAPDOOR.get().asItem());
        tabData.accept(NTrialsModBlocks.EXPOSED_COPPER_TRAPDOOR.get().asItem());
        tabData.accept(NTrialsModBlocks.WEATHERED_COPPER_TRAPDOOR.get().asItem());
        tabData.accept(NTrialsModBlocks.OXIDIZED_COPPER_TRAPDOOR.get().asItem());
        tabData.accept(NTrialsModBlocks.WAXED_COPPER_TRAPDOOR.get().asItem());
        tabData.accept(NTrialsModBlocks.WAXED_EXPOSED_COPPER_TRAPDOOR.get().asItem());
        tabData.accept(NTrialsModBlocks.WAXED_WEATHERED_COPPER_TRAPDOOR.get().asItem());
        tabData.accept(NTrialsModBlocks.WAXED_OXIDIZED_COPPER_TRAPDOOR.get().asItem());

        tabData.accept(NTrialsModBlocks.COPPER_GRATE.get().asItem());
        tabData.accept(NTrialsModBlocks.EXPOSED_COPPER_GRATE.get().asItem());
        tabData.accept(NTrialsModBlocks.WEATHERED_COPPER_GRATE.get().asItem());
        tabData.accept(NTrialsModBlocks.OXIDIZED_COPPER_GRATE.get().asItem());
        tabData.accept(NTrialsModBlocks.WAXED_COPPER_GRATE.get().asItem());
        tabData.accept(NTrialsModBlocks.WAXED_EXPOSED_COPPER_GRATE.get().asItem());
        tabData.accept(NTrialsModBlocks.WAXED_WEATHERED_COPPER_GRATE.get().asItem());
        tabData.accept(NTrialsModBlocks.WAXED_OXIDIZED_COPPER_GRATE.get().asItem());

        tabData.accept(NTrialsModBlocks.COPPER_BULB.get().asItem());
        tabData.accept(NTrialsModBlocks.EXPOSED_COPPER_BULB.get().asItem());
        tabData.accept(NTrialsModBlocks.WEATHERED_COPPER_BULB.get().asItem());
        tabData.accept(NTrialsModBlocks.OXIDIZED_COPPER_BULB.get().asItem());
        tabData.accept(NTrialsModBlocks.WAXED_COPPER_BULB.get().asItem());
        tabData.accept(NTrialsModBlocks.WAXED_EXPOSED_COPPER_BULB.get().asItem());
        tabData.accept(NTrialsModBlocks.WAXED_WEATHERED_COPPER_BULB.get().asItem());
        tabData.accept(NTrialsModBlocks.WAXED_OXIDIZED_COPPER_BULB.get().asItem());

    })).build());
}
