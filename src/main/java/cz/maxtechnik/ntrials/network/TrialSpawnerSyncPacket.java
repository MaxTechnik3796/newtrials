package cz.maxtechnik.ntrials.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;
public class TrialSpawnerSyncPacket{
	private final BlockPos pos;
	private final String entityTypeId;
	public TrialSpawnerSyncPacket(BlockPos pos,EntityType<?> entityType){
		this.pos=pos;
		if(entityType!=null){
			ResourceLocation location=ForgeRegistries.ENTITY_TYPES.getKey(entityType);
			this.entityTypeId=location!=null?location.toString():"";
		}else{
			this.entityTypeId="";
		}
	}
	public TrialSpawnerSyncPacket(FriendlyByteBuf buf){
		this.pos=buf.readBlockPos();
		this.entityTypeId=buf.readUtf();
	}
	public static void encode(TrialSpawnerSyncPacket packet,FriendlyByteBuf buf){
		buf.writeBlockPos(packet.pos);
		buf.writeUtf(packet.entityTypeId);
	}
	public static TrialSpawnerSyncPacket decode(FriendlyByteBuf buf){  // ← static!
		return new TrialSpawnerSyncPacket(buf);
	}
	public BlockPos getPos(){
		return pos;
	}
	public String getEntityTypeId(){
		return entityTypeId;
	}
}