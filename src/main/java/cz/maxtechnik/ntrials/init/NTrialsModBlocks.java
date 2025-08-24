package cz.maxtechnik.ntrials.init;

import cz.maxtechnik.ntrials.block.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
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
    public static final RegistryObject<Block>CHISELED_COPPER=REGISTRY.register("chiseled_copper_props",()->new CustomCopperBlock(WeatheringCopper.WeatherState.UNAFFECTED, chiseled_copper_props()));
    public static final RegistryObject<Block>EXPOSED_CHISELED_COPPER=REGISTRY.register("exposed_chiseled_copper",()->new CustomCopperBlock(WeatheringCopper.WeatherState.EXPOSED, chiseled_copper_props()));
    public static final RegistryObject<Block>WEATHERED_CHISELED_COPPER=REGISTRY.register("weathered_chiseled_copper",()->new CustomCopperBlock(WeatheringCopper.WeatherState.WEATHERED, chiseled_copper_props()));
    public static final RegistryObject<Block>OXIDIZED_CHISELED_COPPER=REGISTRY.register("oxidized_chiseled_copper",()->new CustomCopperBlock(WeatheringCopper.WeatherState.OXIDIZED, chiseled_copper_props()));

    // ---- Waxed variant ----
    public static final RegistryObject<Block>WAXED_CHISELED_COPPER=REGISTRY.register("waxed_chiseled_copper",()->new WaxedCopperBlock(chiseled_copper_props()));
    public static final RegistryObject<Block>WAXED_EXPOSED_CHISELED_COPPER=REGISTRY.register("waxed_exposed_chiseled_copper",()->new WaxedCopperBlock(chiseled_copper_props()));
    public static final RegistryObject<Block>WAXED_WEATHERED_CHISELED_COPPER=REGISTRY.register("waxed_weathered_chiseled_copper",()->new WaxedCopperBlock(chiseled_copper_props()));
    public static final RegistryObject<Block>WAXED_OXIDIZED_CHISELED_COPPER=REGISTRY.register("waxed_oxidized_chiseled_copper",()->new WaxedCopperBlock(chiseled_copper_props()));

    private static BlockBehaviour.Properties chiseled_copper_props(){
        return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(3f,6f).sound(SoundType.COPPER).requiresCorrectToolForDrops();
    }
}
