package cz.maxtechnik.ntrials.enchantment;

import cz.maxtechnik.ntrials.item.MaceItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
public class WindBurstEnchantment extends Enchantment{
	public WindBurstEnchantment(){
		super(Rarity.VERY_RARE,EnchantmentCategory.create("mace",item->item instanceof MaceItem),new EquipmentSlot[]{EquipmentSlot.MAINHAND});
	}
	@Override
	public int getMinCost(int level){
		return 15+(level-1)*9;
	}
	@Override
	public int getMaxCost(int level){
		return this.getMinCost(level)+50;
	}
	@Override
	public int getMaxLevel(){
		return 3;
	}
}
