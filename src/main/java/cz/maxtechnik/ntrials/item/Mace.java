package cz.maxtechnik.ntrials.item;

import cz.maxtechnik.ntrials.init.NTrialsModEnchantments;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;
import org.jetbrains.annotations.NotNull;

public class Mace extends SwordItem {
    public static final EnchantmentCategory MACE = EnchantmentCategory.create("mace", item -> item instanceof Mace);
    public Mace(Tiers tier, int attackDamage, float attackSpeed, Item.Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if (enchantment == Enchantments.MENDING ||
                enchantment == Enchantments.VANISHING_CURSE ||
                enchantment == NTrialsModEnchantments.WIND_BURST.get()) {
            return true;
        }
        return enchantment.category == MACE ||
                enchantment == NTrialsModEnchantments.DENSITY.get() ||
                enchantment == NTrialsModEnchantments.BREACH.get() ||
                enchantment == Enchantments.UNBREAKING ||
                enchantment == Enchantments.FIRE_ASPECT ||
                enchantment == Enchantments.BANE_OF_ARTHROPODS ||
                enchantment == Enchantments.SMITE ||
                enchantment == Enchantments.KNOCKBACK;
    }
    @Override
    public int getEnchantmentValue() {
        return 20;
    }
    @Override
    public boolean canBeDepleted() {
        return true;
    }
    @Override
    public boolean isValidRepairItem(@NotNull ItemStack toRepair, @NotNull ItemStack repair) {
        return super.isValidRepairItem(toRepair, repair);
    }
}