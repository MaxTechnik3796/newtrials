package cz.maxtechnik.ntrials.event;

import cz.maxtechnik.ntrials.block.entity.TrialSpawnerBlockEntity;
import cz.maxtechnik.ntrials.network.NetworkHandler;
import cz.maxtechnik.ntrials.network.TrialSpawnerSyncPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
@Mod.EventBusSubscriber(modid="ntrials")
public class PlayerEventHandler{
	@SubscribeEvent
	public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event){
		if(event.getEntity() instanceof ServerPlayer serverPlayer){
			// Sync all trial spawner data when player logs in
			syncTrialSpawnersToPlayer(serverPlayer);
		}
	}
	@SubscribeEvent
	public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event){
		if(event.getEntity() instanceof ServerPlayer serverPlayer){
			// Sync trial spawners in new dimension
			syncTrialSpawnersToPlayer(serverPlayer);
		}
	}
	private static void syncTrialSpawnersToPlayer(ServerPlayer player){
		// Sync trial spawners in chunks around the player
		ChunkPos playerChunk=new ChunkPos(player.blockPosition());
		int renderDistance=player.server.getPlayerList().getViewDistance();
		for(int x=-renderDistance;x<=renderDistance;x++){
			for(int z=-renderDistance;z<=renderDistance;z++){
				ChunkPos chunkPos=new ChunkPos(playerChunk.x+x,playerChunk.z+z);
				if(player.level().hasChunk(chunkPos.x,chunkPos.z)){
					LevelChunk chunk=player.level().getChunk(chunkPos.x,chunkPos.z);
					// Check all block entities in the chunk
					chunk.getBlockEntities().forEach((pos,blockEntity)->{
						if(blockEntity instanceof TrialSpawnerBlockEntity trialSpawner&&trialSpawner.hasSpawnEntity()){
							TrialSpawnerSyncPacket packet=new TrialSpawnerSyncPacket(pos,trialSpawner.getSpawnEntity());
							NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(()->player),packet);
						}
					});
				}
			}
		}
	}
}
