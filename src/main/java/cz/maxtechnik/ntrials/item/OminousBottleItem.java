package cz.maxtechnik.ntrials.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class OminousBottleItem extends Item {
    public OminousBottleItem(){
        super(new Properties().stacksTo(16).food(new FoodProperties.Builder().nutrition(0).saturationMod(0).alwaysEat().build()));
    }
    @Override
    public void appendHoverText(@NotNull ItemStack itemstack, Level level, @NotNull List<Component> list, @NotNull TooltipFlag flag){
        super.appendHoverText(itemstack,level,list,flag);
        list.add(Component.translatable("effect.minecraft.bad_omen").withStyle(ChatFormatting.BLUE).append(CommonComponents.space().append(Component.literal("+").append(Component.translatable("potion.potency.1").append(CommonComponents.SPACE).append(Component.literal("(").append(Component.literal("1:40:00").append(Component.literal(")"))))))));
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
