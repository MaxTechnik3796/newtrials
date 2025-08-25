package cz.maxtechnik.ntrials.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class OozingMobEffect extends MobEffect {
    public OozingMobEffect(){
        super(MobEffectCategory.HARMFUL,-6684765);
    }
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
