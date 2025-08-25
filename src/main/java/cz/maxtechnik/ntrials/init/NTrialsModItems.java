package cz.maxtechnik.ntrials.init;

import cz.maxtechnik.ntrials.NTrialsMod;
import cz.maxtechnik.ntrials.block.*;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class NTrialsModItems{
    public static final DeferredRegister<Item>REGISTRY=DeferredRegister.create(ForgeRegistries.ITEMS, NTrialsMod.MODID);
    public static final RegistryObject<Item>TUFF_BRICKS=block(NTrialsModBlocks.TUFF_BRICKS);
    public static final RegistryObject<Item>CHISELED_TUFF=block(NTrialsModBlocks.CHISELED_TUFF);
    public static final RegistryObject<Item>CHISELED_TUFF_BRICKS=block(NTrialsModBlocks.CHISELED_TUFF_BRICKS);
    public static final RegistryObject<Item>POLISHED_TUFF=block(NTrialsModBlocks.POLISHED_TUFF);

    public static final RegistryObject<Item>TUFF_STAIRS=block(NTrialsModBlocks.TUFF_STAIRS);
    public static final RegistryObject<Item>TUFF_BRICK_STAIRS=block(NTrialsModBlocks.TUFF_BRICK_STAIRS);
    public static final RegistryObject<Item>POLISHED_TUFF_STAIRS=block(NTrialsModBlocks.POLISHED_TUFF_STAIRS);

    public static final RegistryObject<Item>TUFF_SLAB=block(NTrialsModBlocks.TUFF_SLAB);
    public static final RegistryObject<Item>TUFF_BRICK_SLAB=block(NTrialsModBlocks.TUFF_BRICK_SLAB);
    public static final RegistryObject<Item>POLISHED_TUFF_SLAB=block(NTrialsModBlocks.POLISHED_TUFF_SLAB);

    public static final RegistryObject<Item>TUFF_WALL=block(NTrialsModBlocks.TUFF_WALL);
    public static final RegistryObject<Item>TUFF_BRICK_WALL=block(NTrialsModBlocks.TUFF_BRICK_WALL);
    public static final RegistryObject<Item>POLISHED_TUFF_WALL=block(NTrialsModBlocks.POLISHED_TUFF_WALL);



    public static final RegistryObject<Item>CHISELED_COPPER=block(NTrialsModBlocks.CHISELED_COPPER);
    public static final RegistryObject<Item>EXPOSED_CHISELED_COPPER=block(NTrialsModBlocks.EXPOSED_CHISELED_COPPER);
    public static final RegistryObject<Item>WEATHERED_CHISELED_COPPER=block(NTrialsModBlocks.WEATHERED_CHISELED_COPPER);
    public static final RegistryObject<Item>OXIDIZED_CHISELED_COPPER=block(NTrialsModBlocks.OXIDIZED_CHISELED_COPPER);
    public static final RegistryObject<Item>WAXED_CHISELED_COPPER=block(NTrialsModBlocks.WAXED_CHISELED_COPPER);
    public static final RegistryObject<Item>WAXED_EXPOSED_CHISELED_COPPER=block(NTrialsModBlocks.WAXED_EXPOSED_CHISELED_COPPER);
    public static final RegistryObject<Item>WAXED_WEATHERED_CHISELED_COPPER=block(NTrialsModBlocks.WAXED_WEATHERED_CHISELED_COPPER);
    public static final RegistryObject<Item>WAXED_OXIDIZED_CHISELED_COPPER=block(NTrialsModBlocks.WAXED_OXIDIZED_CHISELED_COPPER);

    public static final RegistryObject<Item>COPPER_BULB=block(NTrialsModBlocks.COPPER_BULB);
    public static final RegistryObject<Item>EXPOSED_COPPER_BULB=block(NTrialsModBlocks.EXPOSED_COPPER_BULB);
    public static final RegistryObject<Item>WEATHERED_COPPER_BULB=block(NTrialsModBlocks.WEATHERED_COPPER_BULB);
    public static final RegistryObject<Item>OXIDIZED_COPPER_BULB=block(NTrialsModBlocks.OXIDIZED_COPPER_BULB);


    public static final RegistryObject<Item>COPPER_DOOR=block(NTrialsModBlocks.COPPER_DOOR);
    public static final RegistryObject<Item>EXPOSED_COPPER_DOOR=block(NTrialsModBlocks.EXPOSED_COPPER_DOOR);
    public static final RegistryObject<Item>WEATHERED_COPPER_DOOR=block(NTrialsModBlocks.WEATHERED_COPPER_DOOR);
    public static final RegistryObject<Item>OXIDIZED_COPPER_DOOR=block(NTrialsModBlocks.OXIDIZED_COPPER_DOOR);
    public static final RegistryObject<Item>WAXED_COPPER_DOOR=block(NTrialsModBlocks.WAXED_COPPER_DOOR);
    public static final RegistryObject<Item>WAXED_EXPOSED_COPPER_DOOR=block(NTrialsModBlocks.WAXED_EXPOSED_COPPER_DOOR);
    public static final RegistryObject<Item>WAXED_WEATHERED_COPPER_DOOR=block(NTrialsModBlocks.WAXED_WEATHERED_COPPER_DOOR);
    public static final RegistryObject<Item>WAXED_OXIDIZED_COPPER_DOOR=block(NTrialsModBlocks.WAXED_OXIDIZED_COPPER_DOOR);

    public static final RegistryObject<Item>COPPER_TRAPDOOR=block(NTrialsModBlocks.COPPER_TRAPDOOR);
    public static final RegistryObject<Item>EXPOSED_COPPER_TRAPDOOR=block(NTrialsModBlocks.EXPOSED_COPPER_TRAPDOOR);
    public static final RegistryObject<Item>WEATHERED_COPPER_TRAPDOOR=block(NTrialsModBlocks.WEATHERED_COPPER_TRAPDOOR);
    public static final RegistryObject<Item>OXIDIZED_COPPER_TRAPDOOR=block(NTrialsModBlocks.OXIDIZED_COPPER_TRAPDOOR);
    public static final RegistryObject<Item>WAXED_COPPER_TRAPDOOR=block(NTrialsModBlocks.WAXED_COPPER_TRAPDOOR);
    public static final RegistryObject<Item>WAXED_EXPOSED_COPPER_TRAPDOOR=block(NTrialsModBlocks.WAXED_EXPOSED_COPPER_TRAPDOOR);
    public static final RegistryObject<Item>WAXED_WEATHERED_COPPER_TRAPDOOR=block(NTrialsModBlocks.WAXED_WEATHERED_COPPER_TRAPDOOR);
    public static final RegistryObject<Item>WAXED_OXIDIZED_COPPER_TRAPDOOR=block(NTrialsModBlocks.WAXED_OXIDIZED_COPPER_TRAPDOOR);

    public static final RegistryObject<Item>COPPER_GRATE=block(NTrialsModBlocks.COPPER_GRATE);
    public static final RegistryObject<Item>EXPOSED_COPPER_GRATE=block(NTrialsModBlocks.EXPOSED_COPPER_GRATE);
    public static final RegistryObject<Item>WEATHERED_COPPER_GRATE=block(NTrialsModBlocks.WEATHERED_COPPER_GRATE);
    public static final RegistryObject<Item>OXIDIZED_COPPER_GRATE=block(NTrialsModBlocks.OXIDIZED_COPPER_GRATE);
    public static final RegistryObject<Item>WAXED_COPPER_GRATE=block(NTrialsModBlocks.WAXED_COPPER_GRATE);
    public static final RegistryObject<Item>WAXED_EXPOSED_COPPER_GRATE=block(NTrialsModBlocks.WAXED_EXPOSED_COPPER_GRATE);
    public static final RegistryObject<Item>WAXED_WEATHERED_COPPER_GRATE=block(NTrialsModBlocks.WAXED_WEATHERED_COPPER_GRATE);
    public static final RegistryObject<Item>WAXED_OXIDIZED_COPPER_GRATE=block(NTrialsModBlocks.WAXED_OXIDIZED_COPPER_GRATE);




    private static RegistryObject<Item>block(RegistryObject<Block>block){
        assert block.getId()!=null;
        return REGISTRY.register(block.getId().getPath(),()->new BlockItem(block.get(),new Item.Properties()));
    }

    private static RegistryObject<Item>doubleBlock(RegistryObject<Block>block){
        assert block.getId()!=null;
        return REGISTRY.register(block.getId().getPath(),()->new DoubleHighBlockItem(block.get(),new Item.Properties()));
    }

}
