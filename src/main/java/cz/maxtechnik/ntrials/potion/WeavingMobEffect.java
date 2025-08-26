package cz.maxtechnik.ntrials.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class WeavingMobEffect extends MobEffect {
    public WeavingMobEffect(){
        super(MobEffectCategory.HARMFUL,-8885926);
    }
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
