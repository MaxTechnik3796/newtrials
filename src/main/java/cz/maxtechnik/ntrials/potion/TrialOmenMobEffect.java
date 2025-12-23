package cz.maxtechnik.ntrials.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
public class TrialOmenMobEffect extends MobEffect {
    public TrialOmenMobEffect(){
        super(MobEffectCategory.HARMFUL,-15292762);
    }
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
