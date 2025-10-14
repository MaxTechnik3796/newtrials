package cz.maxtechnik.ntrials.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class OminousBottleItem extends Item {
    public OminousBottleItem(){
        super(new Properties().stacksTo(16).food(new FoodProperties.Builder().nutrition(0).saturationMod(0).alwaysEat().build()));
    }
    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack itemstack){
        return UseAnim.DRINK;
    }
    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack itemstack,@NotNull Level world,@NotNull LivingEntity entity){
        ItemStack retval=new ItemStack(Items.GLASS_BOTTLE);
        super.finishUsingItem(itemstack,world,entity);
        if(!(world.isClientSide()))
            if(entity.hasEffect(MobEffects.BAD_OMEN)){
                entity.addEffect(new MobEffectInstance(MobEffects.BAD_OMEN,120000,1+Objects.requireNonNull(entity.getEffect(MobEffects.BAD_OMEN)).getAmplifier()));
            }else{
                entity.addEffect(new MobEffectInstance(MobEffects.BAD_OMEN,120000,0));
            }
        if(itemstack.isEmpty()){
            return retval;
        }else{
            if(entity instanceof Player player&&!player.getAbilities().instabuild){
                if(!player.getInventory().add(retval))
                    player.drop(retval,false);
            }
            return itemstack;
        }
    }

}
