package cz.maxtechnik.ntrials.enchantment;

import cz.maxtechnik.ntrials.item.MaceItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
public class DensityEnchantment extends Enchantment{
	public DensityEnchantment(){
		super(Rarity.VERY_RARE,EnchantmentCategory.create("mace",item->item instanceof MaceItem),new EquipmentSlot[]{EquipmentSlot.MAINHAND});
	}
	@Override
	public int getMinCost(int level){
		return 5+(level-1)*8;
	}
	@Override
	public int getMaxCost(int level){
		return this.getMinCost(level)+50;
	}
	@Override
	public int getMaxLevel(){
		return 5;
	}
}
