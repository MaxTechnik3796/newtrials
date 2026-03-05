package cz.maxtechnik.ntrials.init.other;

import cz.maxtechnik.ntrials.NTrialsMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
@Mod.EventBusSubscriber(modid=NTrialsMod.MODID)
public class NTrialsModEvents{
	@SubscribeEvent
	public static void registerTrades(VillagerTradesEvent event){
		if(event.getType().equals(VillagerProfession.CARTOGRAPHER)){
			event.getTrades().get(3).add(new TrialsMapTrade(12,12,10));
		}
	}
	public record TrialsMapTrade(int emeraldCost,int maxUses,int villagerXp) implements VillagerTrades.ItemListing{
		private static final TagKey<Structure> TRIALS_STRUCTURE_TAG=TagKey.create(Registries.STRUCTURE,ResourceLocation.fromNamespaceAndPath(NTrialsMod.MODID,"trials"));
		@Nullable
		@Override
		public MerchantOffer getOffer(Entity trader,@NotNull RandomSource random){
			if(!(trader.level() instanceof ServerLevel serverLevel))return null;
			BlockPos structurePos=serverLevel.findNearestMapStructure(TRIALS_STRUCTURE_TAG,trader.blockPosition(),100,true);
			if(structurePos==null)return null;
			ItemStack mapStack=MapItem.create(serverLevel,structurePos.getX(),structurePos.getZ(),(byte)2,true,true);
			MapItem.renderBiomePreviewMap(serverLevel,mapStack);
			mapStack.setHoverName(Component.translatable("item.ntrials.trials_explorer_map"));
			MapItemSavedData.addTargetDecoration(mapStack,structurePos,"trials",MapDecoration.Type.TARGET_POINT);
			return new MerchantOffer(
					new ItemStack(Items.EMERALD,this.emeraldCost),
					new ItemStack(Items.COMPASS,1),
					mapStack,
					this.maxUses,
					this.villagerXp,
					0.2F
			);
		}
	}
}