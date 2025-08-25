package cz.maxtechnik.ntrials.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class InfestedMobEffect extends MobEffect {
    public InfestedMobEffect(){
        super(MobEffectCategory.HARMFUL,-7562356);
    }
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    /*@Override
    public void onMobHurt(LivingEntity entity, int amplifier, DamageSource damageSource, float damage) {

    }*/

}
