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
        tabData.accept(NTrialsModBlocks.TUFF_BRICKS.get().asItem());
        tabData.accept(NTrialsModBlocks.CHISELED_TUFF.get().asItem());
        tabData.accept(NTrialsModBlocks.CHISELED_TUFF_BRICKS.get().asItem());
        tabData.accept(NTrialsModBlocks.POLISHED_TUFF.get().asItem());

        tabData.accept(NTrialsModBlocks.CUSTOM_COPPER.get().asItem());
        tabData.accept(NTrialsModBlocks.EXPOSED_CUSTOM_COPPER.get().asItem());
        tabData.accept(NTrialsModBlocks.WEATHERED_CUSTOM_COPPER.get().asItem());
        tabData.accept(NTrialsModBlocks.OXIDIZED_CUSTOM_COPPER.get().asItem());
        tabData.accept(NTrialsModBlocks.WAXED_CUSTOM_COPPER.get().asItem());
        tabData.accept(NTrialsModBlocks.WAXED_EXPOSED_CUSTOM_COPPER.get().asItem());
        tabData.accept(NTrialsModBlocks.WAXED_WEATHERED_CUSTOM_COPPER.get().asItem());
        tabData.accept(NTrialsModBlocks.WAXED_OXIDIZED_CUSTOM_COPPER.get().asItem());
    })).build());
}
