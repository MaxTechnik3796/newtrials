package cz.maxtechnik.ntrials.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
public class CopperUpgradeSmithingTemplateItem extends Item{
	public CopperUpgradeSmithingTemplateItem(){
		super(new Properties());
	}
	@Override
	public void appendHoverText(@NotNull ItemStack itemstack,Level level,@NotNull List<Component> list,@NotNull TooltipFlag flag){
		super.appendHoverText(itemstack,level,list,flag);
		list.add(Component.translatable("upgrade.ntrials.copper_upgrade").withStyle(ChatFormatting.GRAY));
		list.add(CommonComponents.EMPTY);
		list.add(Component.translatable("item.minecraft.smithing_template.applies_to").withStyle(ChatFormatting.GRAY));
		list.add(CommonComponents.space().append(Component.translatable("item.ntrials.smithing_template.copper_upgrade.applies_to").withStyle(ChatFormatting.BLUE)));
		list.add(Component.translatable("item.minecraft.smithing_template.ingredients").withStyle(ChatFormatting.GRAY));
		list.add(CommonComponents.space().append(Component.translatable("item.ntrials.smithing_template.copper_upgrade.ingredients").withStyle(ChatFormatting.BLUE)));
	}
}
