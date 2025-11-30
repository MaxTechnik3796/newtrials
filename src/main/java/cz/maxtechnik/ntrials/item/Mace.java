package cz.maxtechnik.ntrials.item;

import cz.maxtechnik.ntrials.init.NTrialsModEnchantments;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
public class Mace extends SwordItem{
	public static final EnchantmentCategory MACE=EnchantmentCategory.create("mace",item->item instanceof Mace);
	public static final List<Enchantment>TABLE_ENCHANTMENTS=Arrays.asList(
			NTrialsModEnchantments.DENSITY.get(),
			NTrialsModEnchantments.BREACH.get(),
			Enchantments.UNBREAKING,
			Enchantments.FIRE_ASPECT,
			Enchantments.BANE_OF_ARTHROPODS,
			Enchantments.SMITE,
			Enchantments.KNOCKBACK
	);
	public static final List<Enchantment>ANVIL_ENCHANTMENTS=Arrays.asList(
			Enchantments.MENDING,
			Enchantments.VANISHING_CURSE,
			NTrialsModEnchantments.WIND_BURST.get()
	);
	public Mace(Tiers tier,int attackDamage,float attackSpeed,Item.Properties properties){
		super(tier,attackDamage,attackSpeed,properties);
	}
	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack,Enchantment enchantment){
		if(ANVIL_ENCHANTMENTS.contains(enchantment))
			return true;
		return enchantment.category.equals(MACE)||TABLE_ENCHANTMENTS.contains(enchantment);
	}
	@Override
	public int getEnchantmentValue(){
		return 20;
	}
	@Override
	public boolean canBeDepleted(){
		return true;
	}
	@Override
	public boolean isValidRepairItem(@NotNull ItemStack toRepair,@NotNull ItemStack repair){
		return super.isValidRepairItem(toRepair,repair);
	}
}