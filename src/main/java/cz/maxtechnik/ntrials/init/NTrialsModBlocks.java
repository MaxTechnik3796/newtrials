package cz.maxtechnik.ntrials.init;

import cz.maxtechnik.ntrials.block.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import cz.maxtechnik.ntrials.NTrialsMod;
import net.minecraftforge.registries.RegistryObject;

public class NTrialsModBlocks{
    public static final DeferredRegister<Block>REGISTRY=DeferredRegister.create(ForgeRegistries.BLOCKS,NTrialsMod.MODID);
    // ---- Tuff stuff ----
    public static final RegistryObject<Block>TUFF_BRICKS=REGISTRY.register("tuff_bricks",TuffBlock::new);
    public static final RegistryObject<Block>CHISELED_TUFF=REGISTRY.register("chiseled_tuff",TuffBlock::new);
    public static final RegistryObject<Block>CHISELED_TUFF_BRICKS=REGISTRY.register("chiseled_tuff_bricks",TuffBlock::new);
    public static final RegistryObject<Block>POLISHED_TUFF=REGISTRY.register("polished_tuff",TuffBlock::new);

    public static final RegistryObject<Block>TUFF_STAIRS=REGISTRY.register("tuff_stairs",TuffStairBlock::new);
    public static final RegistryObject<Block>TUFF_BRICK_STAIRS=REGISTRY.register("tuff_brick_stairs",TuffStairBlock::new);
    public static final RegistryObject<Block>POLISHED_TUFF_STAIRS=REGISTRY.register("polished_tuff_stairs",TuffStairBlock::new);

    public static final RegistryObject<Block>TUFF_SLAB=REGISTRY.register("tuff_slab",TuffSlabBlock::new);
    public static final RegistryObject<Block>TUFF_BRICK_SLAB=REGISTRY.register("tuff_brick_slab",TuffSlabBlock::new);
    public static final RegistryObject<Block>POLISHED_TUFF_SLAB=REGISTRY.register("polished_tuff_slab",TuffSlabBlock::new);

    public static final RegistryObject<Block>TUFF_WALL=REGISTRY.register("tuff_wall",TuffWallBlock::new);
    public static final RegistryObject<Block>TUFF_BRICK_WALL=REGISTRY.register("tuff_brick_wall",TuffWallBlock::new);
    public static final RegistryObject<Block>POLISHED_TUFF_WALL=REGISTRY.register("polished_tuff_wall",TuffWallBlock::new);






    // ---- Oxidizing variants ----
    public static final RegistryObject<Block>CHISELED_COPPER=REGISTRY.register("chiseled_copper",()->new ChiseledCopperBlock(WeatheringCopper.WeatherState.UNAFFECTED, chiseled_copper_props()));
    public static final RegistryObject<Block>EXPOSED_CHISELED_COPPER=REGISTRY.register("exposed_chiseled_copper",()->new ChiseledCopperBlock(WeatheringCopper.WeatherState.EXPOSED, chiseled_copper_props()));
    public static final RegistryObject<Block>WEATHERED_CHISELED_COPPER=REGISTRY.register("weathered_chiseled_copper",()->new ChiseledCopperBlock(WeatheringCopper.WeatherState.WEATHERED, chiseled_copper_props()));
    public static final RegistryObject<Block>OXIDIZED_CHISELED_COPPER=REGISTRY.register("oxidized_chiseled_copper",()->new ChiseledCopperBlock(WeatheringCopper.WeatherState.OXIDIZED, chiseled_copper_props()));

    public static final RegistryObject<Block>COPPER_DOOR=REGISTRY.register("copper_door",()->new CopperDoorBlock(WeatheringCopper.WeatherState.UNAFFECTED, copper_door_props()));
    public static final RegistryObject<Block>EXPOSED_COPPER_DOOR=REGISTRY.register("exposed_copper_door",()->new CopperDoorBlock(WeatheringCopper.WeatherState.EXPOSED, copper_door_props()));
    public static final RegistryObject<Block>WEATHERED_COPPER_DOOR=REGISTRY.register("weathered_copper_door",()->new CopperDoorBlock(WeatheringCopper.WeatherState.WEATHERED, copper_door_props()));
    public static final RegistryObject<Block>OXIDIZED_COPPER_DOOR=REGISTRY.register("oxidized_copper_door",()->new CopperDoorBlock(WeatheringCopper.WeatherState.OXIDIZED, copper_door_props()));

    public static final RegistryObject<Block>COPPER_TRAPDOOR=REGISTRY.register("copper_trapdoor",()->new CopperTrapdoorBlock(WeatheringCopper.WeatherState.UNAFFECTED, copper_trapdoor_props()));
    public static final RegistryObject<Block>EXPOSED_COPPER_TRAPDOOR=REGISTRY.register("exposed_copper_trapdoor",()->new CopperTrapdoorBlock(WeatheringCopper.WeatherState.EXPOSED, copper_trapdoor_props()));
    public static final RegistryObject<Block>WEATHERED_COPPER_TRAPDOOR=REGISTRY.register("weathered_copper_trapdoor",()->new CopperTrapdoorBlock(WeatheringCopper.WeatherState.WEATHERED, copper_trapdoor_props()));
    public static final RegistryObject<Block>OXIDIZED_COPPER_TRAPDOOR=REGISTRY.register("oxidized_copper_trapdoor",()->new CopperTrapdoorBlock(WeatheringCopper.WeatherState.OXIDIZED, copper_trapdoor_props()));

    public static final RegistryObject<Block>COPPER_GRATE=REGISTRY.register("copper_grate",()->new CopperGrateBlock(WeatheringCopper.WeatherState.UNAFFECTED, copper_grate_props()));
    public static final RegistryObject<Block>EXPOSED_COPPER_GRATE=REGISTRY.register("exposed_copper_grate",()->new CopperGrateBlock(WeatheringCopper.WeatherState.UNAFFECTED, copper_grate_props()));
    public static final RegistryObject<Block>WEATHERED_COPPER_GRATE=REGISTRY.register("weathered_copper_grate",()->new CopperGrateBlock(WeatheringCopper.WeatherState.UNAFFECTED, copper_grate_props()));
    public static final RegistryObject<Block>OXIDIZED_COPPER_GRATE=REGISTRY.register("oxidized_copper_grate",()->new CopperGrateBlock(WeatheringCopper.WeatherState.UNAFFECTED, copper_grate_props()));


    public static final RegistryObject<Block>COPPER_BULB=REGISTRY.register("copper_bulb",()->new CopperBulbBlock(WeatheringCopper.WeatherState.UNAFFECTED, copper_bulb_props()));
    public static final RegistryObject<Block>EXPOSED_COPPER_BULB=REGISTRY.register("exposed_copper_bulb",()->new CopperBulbBlock(WeatheringCopper.WeatherState.UNAFFECTED, copper_bulb_props()));
    public static final RegistryObject<Block>WEATHERED_COPPER_BULB=REGISTRY.register("weathered_copper_bulb",()->new CopperBulbBlock(WeatheringCopper.WeatherState.UNAFFECTED, copper_bulb_props()));
    public static final RegistryObject<Block>OXIDIZED_COPPER_BULB=REGISTRY.register("oxidized_copper_bulb",()->new CopperBulbBlock(WeatheringCopper.WeatherState.UNAFFECTED, copper_bulb_props()));



    // ---- Waxed variant ----
    public static final RegistryObject<Block>WAXED_CHISELED_COPPER=REGISTRY.register("waxed_chiseled_copper",()->new WaxedCopperBlock(chiseled_copper_props()));
    public static final RegistryObject<Block>WAXED_EXPOSED_CHISELED_COPPER=REGISTRY.register("waxed_exposed_chiseled_copper",()->new WaxedCopperBlock(chiseled_copper_props()));
    public static final RegistryObject<Block>WAXED_WEATHERED_CHISELED_COPPER=REGISTRY.register("waxed_weathered_chiseled_copper",()->new WaxedCopperBlock(chiseled_copper_props()));
    public static final RegistryObject<Block>WAXED_OXIDIZED_CHISELED_COPPER=REGISTRY.register("waxed_oxidized_chiseled_copper",()->new WaxedCopperBlock(chiseled_copper_props()));

    public static final RegistryObject<Block>WAXED_COPPER_DOOR=REGISTRY.register("waxed_copper_door",()->new WaxedCopperDoorBlock(copper_door_props()));
    public static final RegistryObject<Block>WAXED_EXPOSED_COPPER_DOOR=REGISTRY.register("waxed_exposed_copper_door",()->new WaxedCopperDoorBlock(copper_door_props()));
    public static final RegistryObject<Block>WAXED_WEATHERED_COPPER_DOOR=REGISTRY.register("waxed_weathered_copper_door",()->new WaxedCopperDoorBlock(copper_door_props()));
    public static final RegistryObject<Block>WAXED_OXIDIZED_COPPER_DOOR=REGISTRY.register("waxed_oxidized_copper_door",()->new WaxedCopperDoorBlock(copper_door_props()));

    public static final RegistryObject<Block>WAXED_COPPER_TRAPDOOR=REGISTRY.register("waxed_copper_trapdoor",()->new WaxedCopperTrapdoorBlock(copper_trapdoor_props()));
    public static final RegistryObject<Block>WAXED_EXPOSED_COPPER_TRAPDOOR=REGISTRY.register("waxed_exposed_copper_trapdoor",()->new WaxedCopperTrapdoorBlock(copper_trapdoor_props()));
    public static final RegistryObject<Block>WAXED_WEATHERED_COPPER_TRAPDOOR=REGISTRY.register("waxed_weathered_copper_trapdoor",()->new WaxedCopperTrapdoorBlock(copper_trapdoor_props()));
    public static final RegistryObject<Block>WAXED_OXIDIZED_COPPER_TRAPDOOR=REGISTRY.register("waxed_oxidized_copper_trapdoor",()->new WaxedCopperTrapdoorBlock(copper_trapdoor_props()));

    public static final RegistryObject<Block>WAXED_COPPER_GRATE=REGISTRY.register("waxed_copper_grate",()->new WaxedCopperGrateBlock(copper_grate_props()));
    public static final RegistryObject<Block>WAXED_EXPOSED_COPPER_GRATE=REGISTRY.register("waxed_exposed_copper_grate",()->new WaxedCopperGrateBlock(copper_grate_props()));
    public static final RegistryObject<Block>WAXED_WEATHERED_COPPER_GRATE=REGISTRY.register("waxed_weathered_copper_grate",()->new WaxedCopperGrateBlock(copper_grate_props()));
    public static final RegistryObject<Block>WAXED_OXIDIZED_COPPER_GRATE=REGISTRY.register("waxed_oxidized_copper_grate",()->new WaxedCopperGrateBlock(copper_grate_props()));

    private static BlockBehaviour.Properties chiseled_copper_props(){
        return BlockBehaviour.Properties.of().sound(SoundType.COPPER).strength(3f,6f).requiresCorrectToolForDrops().mapColor(MapColor.COLOR_ORANGE);
    }
    private static BlockBehaviour.Properties copper_door_props(){
        return BlockBehaviour.Properties.of().sound(SoundType.COPPER).strength(3f,6f).requiresCorrectToolForDrops().mapColor(MapColor.COLOR_ORANGE).noOcclusion().isRedstoneConductor((bs,br,bp)->false).pushReaction(PushReaction.DESTROY);
    }
    private static BlockBehaviour.Properties copper_trapdoor_props(){
        return BlockBehaviour.Properties.of().sound(SoundType.COPPER).strength(3f,6f).requiresCorrectToolForDrops().mapColor(MapColor.COLOR_ORANGE).noOcclusion().isRedstoneConductor((bs,br,bp)->false);
    }
    private static BlockBehaviour.Properties copper_grate_props(){
        return BlockBehaviour.Properties.of().sound(SoundType.COPPER).strength(3f,6f).requiresCorrectToolForDrops().mapColor(MapColor.COLOR_ORANGE).noOcclusion().isRedstoneConductor((bs,br,bp)->false);
    }
    public static BlockBehaviour.Properties copper_bulb_props(){
        return BlockBehaviour.Properties.of().sound(SoundType.COPPER).strength(3f,6f).requiresCorrectToolForDrops().lightLevel((bs)->15).mapColor(MapColor.COLOR_ORANGE).noOcclusion().isRedstoneConductor((bs,br,bp)->false);
    }
}
