package cz.maxtechnik.ntrials.init.basic;

import cz.maxtechnik.ntrials.NTrialsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class NTrialsModTabs{
    public static final DeferredRegister<CreativeModeTab>REGISTER=DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NTrialsMod.MODID);
    public static final RegistryObject<CreativeModeTab>MAIN=REGISTER.register("main",()->CreativeModeTab.builder().title(Component.translatable("creative_tab.ntrials.main")).icon(()->new ItemStack(NTrialsModBlocks.TUFF_BRICKS.get().asItem())).displayItems(((parameters,tabData)->{
		tabData.accept(NTrialsModItems.TRIAL_KEY.get());
		tabData.accept(NTrialsModItems.OMINOUS_TRIAL_KEY.get());
		tabData.accept(NTrialsModItems.BOSS_TRIAL_KEY.get());
		tabData.accept(NTrialsModItems.OMINOUS_BOTTLE.get());
		tabData.accept(NTrialsModItems.COPPER_UPGRADE_SMITHING_TEMPLATE.get());
		tabData.accept(NTrialsModItems.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE.get());
		tabData.accept(NTrialsModItems.FLOW_ARMOR_TRIM_SMITHING_TEMPLATE.get());

		tabData.accept(NTrialsModItems.TRIAL_SPAWNER.get());
		tabData.accept(NTrialsModItems.VAULT.get());

		tabData.accept(NTrialsModItems.BOGGED_SPAWN_EGG.get());
		tabData.accept(NTrialsModItems.BREEZE_SPAWN_EGG.get());
		tabData.accept(NTrialsModItems.BREEZE_BOSS_SPAWN_EGG.get());

		tabData.accept(NTrialsModItems.WIND_CHARGE.get());
		tabData.accept(NTrialsModItems.BREEZE_ROD.get());
		tabData.accept(NTrialsModItems.HEAVY_CORE.get());
		tabData.accept(NTrialsModItems.MACE.get());
		tabData.accept(NTrialsModItems.MACE_HANDLE.get());

		tabData.accept(NTrialsModItems.TUFF_BRICKS.get());
		tabData.accept(NTrialsModItems.CHISELED_TUFF.get());
		tabData.accept(NTrialsModItems.CHISELED_TUFF_BRICKS.get());
		tabData.accept(NTrialsModItems.POLISHED_TUFF.get());

		tabData.accept(NTrialsModItems.TUFF_STAIRS.get());
		tabData.accept(NTrialsModItems.TUFF_BRICK_STAIRS.get());
		tabData.accept(NTrialsModItems.POLISHED_TUFF_STAIRS.get());

		tabData.accept(NTrialsModItems.TUFF_SLAB.get());
		tabData.accept(NTrialsModItems.TUFF_BRICK_SLAB.get());
		tabData.accept(NTrialsModItems.POLISHED_TUFF_SLAB.get());

		tabData.accept(NTrialsModItems.TUFF_WALL.get());
		tabData.accept(NTrialsModItems.TUFF_BRICK_WALL.get());
		tabData.accept(NTrialsModItems.POLISHED_TUFF_WALL.get());

    })).build());
    public static final RegistryObject<CreativeModeTab>COPPER=REGISTER.register("copper",()-> CreativeModeTab.builder().title(Component.translatable("creative_tab.ntrials.copper")).icon(()->new ItemStack(Blocks.COPPER_BLOCK.asItem())).displayItems(((parameters, tabData)->{

		tabData.accept(NTrialsModItems.CHISELED_COPPER.get());
		tabData.accept(NTrialsModItems.EXPOSED_CHISELED_COPPER.get());
		tabData.accept(NTrialsModItems.WEATHERED_CHISELED_COPPER.get());
		tabData.accept(NTrialsModItems.OXIDIZED_CHISELED_COPPER.get());
		tabData.accept(NTrialsModItems.WAXED_CHISELED_COPPER.get());
		tabData.accept(NTrialsModItems.WAXED_EXPOSED_CHISELED_COPPER.get());
		tabData.accept(NTrialsModItems.WAXED_WEATHERED_CHISELED_COPPER.get());
		tabData.accept(NTrialsModItems.WAXED_OXIDIZED_CHISELED_COPPER.get());

		tabData.accept(NTrialsModItems.COPPER_DOOR.get());
		tabData.accept(NTrialsModItems.EXPOSED_COPPER_DOOR.get());
		tabData.accept(NTrialsModItems.WEATHERED_COPPER_DOOR.get());
		tabData.accept(NTrialsModItems.OXIDIZED_COPPER_DOOR.get());
		tabData.accept(NTrialsModItems.WAXED_COPPER_DOOR.get());
		tabData.accept(NTrialsModItems.WAXED_EXPOSED_COPPER_DOOR.get());
		tabData.accept(NTrialsModItems.WAXED_WEATHERED_COPPER_DOOR.get());
		tabData.accept(NTrialsModItems.WAXED_OXIDIZED_COPPER_DOOR.get());

		tabData.accept(NTrialsModItems.COPPER_TRAPDOOR.get());
		tabData.accept(NTrialsModItems.EXPOSED_COPPER_TRAPDOOR.get());
		tabData.accept(NTrialsModItems.WEATHERED_COPPER_TRAPDOOR.get());
		tabData.accept(NTrialsModItems.OXIDIZED_COPPER_TRAPDOOR.get());
		tabData.accept(NTrialsModItems.WAXED_COPPER_TRAPDOOR.get());
		tabData.accept(NTrialsModItems.WAXED_EXPOSED_COPPER_TRAPDOOR.get());
		tabData.accept(NTrialsModItems.WAXED_WEATHERED_COPPER_TRAPDOOR.get());
		tabData.accept(NTrialsModItems.WAXED_OXIDIZED_COPPER_TRAPDOOR.get());

		tabData.accept(NTrialsModItems.COPPER_GRATE.get());
		tabData.accept(NTrialsModItems.EXPOSED_COPPER_GRATE.get());
		tabData.accept(NTrialsModItems.WEATHERED_COPPER_GRATE.get());
		tabData.accept(NTrialsModItems.OXIDIZED_COPPER_GRATE.get());
		tabData.accept(NTrialsModItems.WAXED_COPPER_GRATE.get());
		tabData.accept(NTrialsModItems.WAXED_EXPOSED_COPPER_GRATE.get());
		tabData.accept(NTrialsModItems.WAXED_WEATHERED_COPPER_GRATE.get());
		tabData.accept(NTrialsModItems.WAXED_OXIDIZED_COPPER_GRATE.get());

		tabData.accept(NTrialsModItems.COPPER_BULB.get());
		tabData.accept(NTrialsModItems.EXPOSED_COPPER_BULB.get());
		tabData.accept(NTrialsModItems.WEATHERED_COPPER_BULB.get());
		tabData.accept(NTrialsModItems.OXIDIZED_COPPER_BULB.get());
		tabData.accept(NTrialsModItems.WAXED_COPPER_BULB.get());
		tabData.accept(NTrialsModItems.WAXED_EXPOSED_COPPER_BULB.get());
		tabData.accept(NTrialsModItems.WAXED_WEATHERED_COPPER_BULB.get());
		tabData.accept(NTrialsModItems.WAXED_OXIDIZED_COPPER_BULB.get());
    })).build());
}
