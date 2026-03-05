package cz.maxtechnik.ntrials.network;

import cz.maxtechnik.ntrials.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;
@OnlyIn(Dist.CLIENT)
public class ClientPacketHandlers{
	public static void handleTrialSpawnerSync(TrialSpawnerSyncPacket packet,Supplier<NetworkEvent.Context> contextSupplier){
		NetworkEvent.Context context=contextSupplier.get();
		context.enqueueWork(()->{
			Level level=Minecraft.getInstance().level;
			if(level==null) return;
			BlockPos pos=packet.getPos();
			BlockEntity blockEntity=level.getBlockEntity(pos);
			if(blockEntity instanceof TrialSpawnerBlockEntity spawner){
				String entityTypeId=packet.getEntityTypeId();
				if(entityTypeId!=null&&!entityTypeId.isEmpty()){
					try{
						EntityType<?> type=ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.parse(entityTypeId));
						if(type!=null) spawner.setSpawnEntity(type);
					}catch(Exception ignored){
					}
				}
			}
		});
		context.setPacketHandled(true);
	}
}
