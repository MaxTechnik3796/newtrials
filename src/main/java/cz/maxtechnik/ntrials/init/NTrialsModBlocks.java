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

public class NTrialsModBlocks {
    public static final DeferredRegister<Block>REGISTRY=DeferredRegister.create(ForgeRegistries.BLOCKS,NTrialsMod.MODID);
    public static final RegistryObject<Block>TUFF_BRICKS=REGISTRY.register("tuff_bricks",TuffBlock::new);
    public static final RegistryObject<Block>CHISELED_TUFF=REGISTRY.register("chiseled_tuff",TuffBlock::new);
    public static final RegistryObject<Block>CHISELED_TUFF_BRICKS=REGISTRY.register("chiseled_tuff_bricks",TuffBlock::new);
    public static final RegistryObject<Block>POLISHED_TUFF=REGISTRY.register("polished_tuff",TuffBlock::new);





    public static final RegistryObject<Block>EXAMPLE_BLOCK=REGISTRY.register("example_block", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)));


    // ---- Oxidující varianty ----
    public static final RegistryObject<Block> CUSTOM_COPPER = REGISTRY.register("custom_copper",
            () -> new CustomCopperBlock(WeatheringCopper.WeatherState.UNAFFECTED, props()));

    public static final RegistryObject<Block> EXPOSED_CUSTOM_COPPER = REGISTRY.register("exposed_custom_copper",
            () -> new CustomCopperBlock(WeatheringCopper.WeatherState.EXPOSED, props()));

    public static final RegistryObject<Block> WEATHERED_CUSTOM_COPPER = REGISTRY.register("weathered_custom_copper",
            () -> new CustomCopperBlock(WeatheringCopper.WeatherState.WEATHERED, props()));

    public static final RegistryObject<Block> OXIDIZED_CUSTOM_COPPER = REGISTRY.register("oxidized_custom_copper",
            () -> new CustomCopperBlock(WeatheringCopper.WeatherState.OXIDIZED, props()));

    // ---- Voskované varianty ----
    public static final RegistryObject<Block> WAXED_CUSTOM_COPPER = REGISTRY.register("waxed_custom_copper", () -> new Block(props()));
    public static final RegistryObject<Block> WAXED_EXPOSED_CUSTOM_COPPER = REGISTRY.register("waxed_exposed_custom_copper", () -> new Block(props()));
    public static final RegistryObject<Block> WAXED_WEATHERED_CUSTOM_COPPER = REGISTRY.register("waxed_weathered_custom_copper", () -> new Block(props()));
    public static final RegistryObject<Block> WAXED_OXIDIZED_CUSTOM_COPPER = REGISTRY.register("waxed_oxidized_custom_copper", () -> new Block(props()));

    private static BlockBehaviour.Properties props() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_ORANGE)
                .strength(3.0F, 6.0F)
                .sound(SoundType.COPPER)
                .requiresCorrectToolForDrops();
    }














}
