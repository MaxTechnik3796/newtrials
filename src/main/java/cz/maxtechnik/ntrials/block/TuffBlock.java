package cz.maxtechnik.ntrials.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

public class TuffBlock extends Block {
    public TuffBlock(){
        super(Properties.of().sound(SoundType.TUFF).strength(1.5f,6).requiresCorrectToolForDrops().mapColor(MapColor.COLOR_GRAY));
    }
}
