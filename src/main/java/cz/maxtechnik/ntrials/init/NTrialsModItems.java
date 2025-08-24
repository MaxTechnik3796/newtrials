package cz.maxtechnik.ntrials.init;

import cz.maxtechnik.ntrials.NTrialsMod;
import cz.maxtechnik.ntrials.block.CustomCopperBlock;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class NTrialsModItems{
    public static final DeferredRegister<Item>REGISTRY=DeferredRegister.create(ForgeRegistries.ITEMS, NTrialsMod.MODID);
    public static final RegistryObject<Item>TUFF_BRICKS=block(NTrialsModBlocks.TUFF_BRICKS);
    public static final RegistryObject<Item>CHISELED_TUFF=block(NTrialsModBlocks.CHISELED_TUFF);
    public static final RegistryObject<Item>CHISELED_TUFF_BRICKS=block(NTrialsModBlocks.CHISELED_TUFF_BRICKS);
    public static final RegistryObject<Item>POLISHED_TUFF=block(NTrialsModBlocks.POLISHED_TUFF);

    public static final RegistryObject<Item> EXAMPLE_ITEM=REGISTRY.register("example_item", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().alwaysEat().nutrition(1).saturationMod(2f).build())));
    public static final RegistryObject<Item>EXAMPLE_BLOCK=block(NTrialsModBlocks.EXAMPLE_BLOCK);




    public static final RegistryObject<Item>CUSTOM_COPPER=block(NTrialsModBlocks.CUSTOM_COPPER);
    public static final RegistryObject<Item>EXPOSED_CUSTOM_COPPER=block(NTrialsModBlocks.EXPOSED_CUSTOM_COPPER);
    public static final RegistryObject<Item>WEATHERED_CUSTOM_COPPER=block(NTrialsModBlocks.WEATHERED_CUSTOM_COPPER);
    public static final RegistryObject<Item>OXIDIZED_CUSTOM_COPPER=block(NTrialsModBlocks.OXIDIZED_CUSTOM_COPPER);

    public static final RegistryObject<Item>WAXED_CUSTOM_COPPER=block(NTrialsModBlocks.WAXED_CUSTOM_COPPER);
    public static final RegistryObject<Item>WAXED_EXPOSED_CUSTOM_COPPER=block(NTrialsModBlocks.WAXED_EXPOSED_CUSTOM_COPPER);
    public static final RegistryObject<Item>WAXED_WEATHERED_CUSTOM_COPPER=block(NTrialsModBlocks.WAXED_WEATHERED_CUSTOM_COPPER);
    public static final RegistryObject<Item>WAXED_OXIDIZED_CUSTOM_COPPER=block(NTrialsModBlocks.WAXED_OXIDIZED_CUSTOM_COPPER);









    private static RegistryObject<Item>block(RegistryObject<Block>block){
        assert block.getId()!=null;
        return REGISTRY.register(block.getId().getPath(),()->new BlockItem(block.get(),new Item.Properties()));
    }

    private static RegistryObject<Item>doubleBlock(RegistryObject<Block>block){
        assert block.getId()!=null;
        return REGISTRY.register(block.getId().getPath(),()->new DoubleHighBlockItem(block.get(),new Item.Properties()));
    }

}
