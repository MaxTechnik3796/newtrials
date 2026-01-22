package cz.maxtechnik.ntrials.block.entity;

import cz.maxtechnik.ntrials.NTrialsModCommonConfig;
import cz.maxtechnik.ntrials.block.TrialSpawnerBossBlock;
import cz.maxtechnik.ntrials.block.TrialSpawnerBlock;
import cz.maxtechnik.ntrials.init.other.NTrialsModBlockEntities;
import cz.maxtechnik.ntrials.init.other.NTrialsModEntityTypes;
import cz.maxtechnik.ntrials.init.basic.NTrialsModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.Vec3;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
public class TrialSpawnerBossBlockEntity extends BlockEntity{
	private static final String DEFAULT_BOSS_LOOT="ntrials:chests/spawner_boss";
	private boolean isActivated=false;
	private boolean isKeyActivated=false;
	private int spawnTimer=-1;
	private UUID spawnedBossUUID=null;
	private final Set<UUID> nearbyPlayers=new HashSet<>();
	private final Set<UUID> participatingPlayers=new HashSet<>();
	private int bossHP=NTrialsModCommonConfig.bossSpawnerBaseBossHp;
	private boolean isLootAnimating=false;
	private int lootAnimationTick=0;
	private List<ItemStack> pendingLootItems=new ArrayList<>();
	private int currentLootDropIndex=0;
	private int completeTrialTimer=-1;
	private int clientTickCount=0;
	private transient boolean wasActivated=false;
	private final Set<UUID> playersWhoReceivedReward=new HashSet<>();
	private int cooldownTimer=0; // Odpočet cooldownu
	private String keyTag="";
	private EntityType<?> bossMobType=NTrialsModEntityTypes.BREEZE_BOSS.get();
	private String bossLootTable="";
	public void setKeyTag(String tag){
		this.keyTag=tag==null?"":tag;
		setChanged();
		if(level!=null&&!level.isClientSide()) level.sendBlockUpdated(getBlockPos(),getBlockState(),getBlockState(),3);
	}
	public String getKeyTag(){
		return keyTag;
	}
	public EntityType<?> getBossMobType(){
		return bossMobType;
	}
	public void setBossMobType(EntityType<?> type){
		if(type!=null){
			this.bossMobType=type;
			setChanged();
			if(level!=null&&!level.isClientSide()){
				level.sendBlockUpdated(getBlockPos(),getBlockState(),getBlockState(),3);
			}
		}
	}
	public void setBossLootTable(String loot){
		this.bossLootTable=loot==null?"":loot;
		setChanged();
		if(level!=null&&!level.isClientSide()) level.sendBlockUpdated(getBlockPos(),getBlockState(),getBlockState(),3);
	}
	@Override
	public @NotNull CompoundTag getUpdateTag(){
		CompoundTag tag=super.getUpdateTag();
		this.saveAdditional(tag);
		return tag;
	}
	@Override
	public void handleUpdateTag(CompoundTag tag){
		super.handleUpdateTag(tag);
		this.load(tag);
	}
	@Override
	public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket(){
		return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
	}
	public TrialSpawnerBossBlockEntity(BlockPos pos,BlockState blockState){
		super(NTrialsModBlockEntities.TRIAL_SPAWNER_BOSS_BLOCK_ENTITY.get(),pos,blockState);
	}
	public boolean hasPlayerReceivedReward(UUID playerUuid){
		return playersWhoReceivedReward.contains(playerUuid);
	}
	public void addPlayerWhoReceivedReward(UUID playerUuid){
		playersWhoReceivedReward.add(playerUuid);
		setChanged();
	}
	public int getCooldownTimer(){
		return cooldownTimer;
	}
	// Hlavní tick metoda
	public void tick(){
		if(level==null||level.isClientSide) return;
		// Logika cooldownu
		if(cooldownTimer>0){
			if(--cooldownTimer==0){
				// Cooldown skončil
				level.playSound(null,getBlockPos(),NTrialsModSounds.BLOCK_VAULT_DEACTIVATE.get(),SoundSource.BLOCKS,1.0f,1.0f);
			}
			if(level.getGameTime()%20==0) updateBlockState();
			return; // Spawner je v cooldownu
		}
		if(level.getGameTime()%40==0){
			updatePlayersInRange();
			checkAndActivateByProximity();
		}
		if(isKeyActivated&&spawnTimer>0&&--spawnTimer==0) spawnBreezeBoss();
		if(completeTrialTimer>0&&--completeTrialTimer==0) generateLootReward();
		if(isLootAnimating) tickLootAnimation();
		if(isKeyActivated&&spawnedBossUUID!=null&&level instanceof ServerLevel serverLevel){
			net.minecraft.world.entity.Entity boss=serverLevel.getEntity(spawnedBossUUID);
			if(boss==null||!boss.isAlive()) handleBossKilled();
		}
		if(level.getGameTime()%20==0) updateBlockState();
	}
	// Aktualizuje hráče v dosahu
	private void updatePlayersInRange(){
		if(level==null) return;
		double range=20.0;
		AABB searchArea=new AABB(getBlockPos().getX()-range,getBlockPos().getY()-range,getBlockPos().getZ()-range,
				getBlockPos().getX()+range,getBlockPos().getY()+range,getBlockPos().getZ()+range);
		nearbyPlayers.clear();
		for(Player player: level.getEntitiesOfClass(Player.class,searchArea)) nearbyPlayers.add(player.getUUID());
	}
	// Kontroluje aktivaci blízkostí hráčů
	private void checkAndActivateByProximity(){
		if(level==null||level.isClientSide) return;
		double range=8.0;
		AABB searchArea=new AABB(getBlockPos().getX()-range,getBlockPos().getY()-range,getBlockPos().getZ()-range,
				getBlockPos().getX()+range,getBlockPos().getY()+range,getBlockPos().getZ()+range);
		boolean hasPlayers=!level.getEntitiesOfClass(Player.class,searchArea).isEmpty();
		if(hasPlayers&&!isActivated){
			isActivated=true;
			setChanged();
			BlockState currentState=level.getBlockState(getBlockPos());
			if(currentState.getBlock() instanceof TrialSpawnerBossBlock){
				level.setBlock(getBlockPos(),currentState.setValue(TrialSpawnerBossBlock.STATE,TrialSpawnerBlock.TrialSpawnerState.ACTIVE),Block.UPDATE_ALL);
			}
		}else if(!hasPlayers&&isActivated&&!isKeyActivated){
			isActivated=false;
			setChanged();
			BlockState currentState=level.getBlockState(getBlockPos());
			if(currentState.getBlock() instanceof TrialSpawnerBossBlock){
				level.setBlock(getBlockPos(),currentState.setValue(TrialSpawnerBossBlock.STATE,TrialSpawnerBlock.TrialSpawnerState.INACTIVE),Block.UPDATE_ALL);
			}
		}
	}
	// Aktivace klíčem
	public void activateWithKey(){
		if(!isKeyActivated&&level!=null&&!level.isClientSide&&isActivated){
			isKeyActivated=true;
			this.participatingPlayers.clear();
			this.participatingPlayers.addAll(this.nearbyPlayers);
			spawnTimer=NTrialsModCommonConfig.bossSpawnerSpawnDelay;
			setChanged();
			BlockState currentState=level.getBlockState(getBlockPos());
			if(currentState.getBlock() instanceof TrialSpawnerBossBlock){
				level.setBlock(getBlockPos(),currentState.setValue(TrialSpawnerBossBlock.STATE,TrialSpawnerBlock.TrialSpawnerState.ACTIVE),Block.UPDATE_ALL);
			}
			level.playSound(null,getBlockPos(),NTrialsModSounds.BLOCK_TRIAL_SPAWNER_DETECT_PLAYER.get(),SoundSource.BLOCKS,1.0f,1.0f);
			spawnKeyActivationParticles();
		}
	}
	// Spawnuje částice aktivace klíčem
	private void spawnKeyActivationParticles(){
		if(level==null||level.isClientSide||!(level instanceof ServerLevel serverLevel)) return;
		BlockPos center=getBlockPos();
		double centerX=center.getX()+0.5, centerY=center.getY()+0.5, centerZ=center.getZ()+0.5;
		for(int i=0;i<15;i++){
			double angle=level.random.nextDouble()*2*Math.PI;
			double verticalAngle=(level.random.nextDouble()-0.5)*Math.PI*0.5;
			double speed=0.1+level.random.nextDouble()*0.1;
			serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,centerX,centerY,centerZ,1,
					Math.cos(angle)*Math.cos(verticalAngle)*speed,Math.sin(verticalAngle)*speed,Math.sin(angle)*Math.cos(verticalAngle)*speed,0.0);
		}
	}
	// Spawnuje bossa
	private void spawnBreezeBoss(){
		if(level==null||level.isClientSide) return;
		int playerCount=participatingPlayers.size();
		bossHP=NTrialsModCommonConfig.bossSpawnerBaseBossHp;
		for(int i=1;i<playerCount;i++) bossHP=(int)(bossHP*NTrialsModCommonConfig.bossSpawnerHpMultiplierPerPlayer);
		BlockPos spawnPos=findSpawnPosition();
		if(spawnPos!=null&&bossMobType!=null){
			net.minecraft.world.entity.Mob boss;
			if(bossMobType==NTrialsModEntityTypes.BREEZE_BOSS.get()){
				boss=new cz.maxtechnik.ntrials.entity.BreezeBossEntity(NTrialsModEntityTypes.BREEZE_BOSS.get(),level);
			}else{
				boss=(net.minecraft.world.entity.Mob)bossMobType.create(level);
			}
			if(boss!=null){
				boss.setPos(spawnPos.getX()+0.5,spawnPos.getY(),spawnPos.getZ()+0.5);
				var healthAttribute=boss.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
				if(healthAttribute!=null){
					healthAttribute.setBaseValue(bossHP);
					boss.setHealth(boss.getMaxHealth());
				}
				// Přidá trvalé efekty: Regenerace I a Rezistence I (999999 ticks = "navždy")
				boss.addEffect(new MobEffectInstance(MobEffects.REGENERATION,999999,0,false,false));
				boss.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,999999,0,false,false));
				boss.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,999999,0,false,false));
				if(level instanceof ServerLevel serverLevel){
					@SuppressWarnings({"deprecation","unused"})
					var ignored=boss.finalizeSpawn(serverLevel,level.getCurrentDifficultyAt(spawnPos),net.minecraft.world.entity.MobSpawnType.SPAWNER,null,null);
				}
				level.addFreshEntity(boss);
				spawnedBossUUID=boss.getUUID();
				level.playSound(null,getBlockPos(),NTrialsModSounds.BLOCK_TRIAL_SPAWNER_SPAWN.get(),SoundSource.BLOCKS,1.0f,1.0f);
				setChanged();
			}
		}
	}
	// Najde validní pozici pro spawn
	private BlockPos findSpawnPosition(){
		if(level==null) return null;
		for(int attempts=0;attempts<10;attempts++){
			int x=getBlockPos().getX()+(level.random.nextInt(7)-3);
			int z=getBlockPos().getZ()+(level.random.nextInt(7)-3);
			int y=getBlockPos().getY();
			for(int yOffset=-1;yOffset<=2;yOffset++){
				BlockPos pos=new BlockPos(x,y+yOffset,z);
				if(isValidSpawnPosition(pos)) return pos;
			}
		}
		return getBlockPos().above();
	}
	// Zkontroluje zda je pozice validní pro spawn
	private boolean isValidSpawnPosition(BlockPos pos){
		if(level==null) return false;
		@SuppressWarnings("deprecation")
		boolean isSolid=level.getBlockState(pos.below()).isSolid();
		return level.getBlockState(pos).isAir()&&level.getBlockState(pos.above()).isAir()&&isSolid;
	}
	// Zpracuje zabití bossa
	private void handleBossKilled(){
		if(level==null||level.isClientSide) return;
		completeTrialTimer=NTrialsModCommonConfig.bossSpawnerCompleteTrialDelay;
		spawnedBossUUID=null;
		setChanged();
	}
	// Vygeneruje loot podle hráčů
	private void generateLootReward(){
		if(level==null||level.isClientSide||!(level instanceof ServerLevel serverLevel)) return;
		// Determine loot table path (fallback to default boss reward table)
		String lootPath=this.bossLootTable==null||this.bossLootTable.isEmpty()?DEFAULT_BOSS_LOOT:this.bossLootTable;
		ResourceLocation lootTableId;
		net.minecraft.world.level.storage.loot.LootTable lootTable;
		try{
			lootTableId=ResourceLocation.parse(lootPath);
			lootTable=serverLevel.getServer().getLootData().getLootTable(lootTableId);
			if(lootTable==net.minecraft.world.level.storage.loot.LootTable.EMPTY)
				throw new IllegalArgumentException("empty loot table");
		}catch(Exception ex){
			lootTableId=ResourceLocation.parse(DEFAULT_BOSS_LOOT);
			lootTable=serverLevel.getServer().getLootData().getLootTable(lootTableId);
		}
		LootParams lootParams=new LootParams.Builder(serverLevel).withParameter(LootContextParams.ORIGIN,Vec3.atCenterOf(getBlockPos())).create(LootContextParamSets.CHEST);
		// multiply loot by number of participating players (so each participant gets one copy of the table result)
		int playersCount=Math.max(1,participatingPlayers.size());
		List<ItemStack> baseLoot=lootTable.getRandomItems(lootParams);
		List<ItemStack> allLootItems=new ArrayList<>();
		for(int i=0;i<playersCount;i++){
			for(ItemStack item: baseLoot){
				if(!item.isEmpty()) allLootItems.add(item.copy());
			}
		}
		// Mark participating players as having received reward (so they cannot claim repeatedly)
		for(UUID playerUUID: new HashSet<>(participatingPlayers)){
			if(!this.hasPlayerReceivedReward(playerUUID)) this.addPlayerWhoReceivedReward(playerUUID);
		}
		if(!allLootItems.isEmpty()) startLootAnimation(allLootItems);
	}
	// Spustí animaci lootu
	private void startLootAnimation(List<ItemStack> lootItems){
		this.isLootAnimating=true;
		this.lootAnimationTick=0;
		this.currentLootDropIndex=0;
		this.pendingLootItems=new ArrayList<>(lootItems);
		setChanged();
		if(level!=null){
			level.playSound(null,getBlockPos(),NTrialsModSounds.BLOCK_TRIAL_SPAWNER_OPEN_SHUTTER.get(),SoundSource.BLOCKS,1.0f,1.0f);
			level.playSound(null,getBlockPos(),NTrialsModSounds.BLOCK_TRIAL_SPAWNER_SPAWN_ITEM_BEGIN.get(),SoundSource.BLOCKS,0.8f,1.0f);
			if(!level.isClientSide()) level.sendBlockUpdated(getBlockPos(),getBlockState(),getBlockState(),3);
		}
	}
	// Tick animace lootu
	private void tickLootAnimation(){
		if(!isLootAnimating||pendingLootItems.isEmpty()){
			stopLootAnimation();
			return;
		}
		lootAnimationTick++;
		if(lootAnimationTick%NTrialsModCommonConfig.spawnerLootDropInterval==0&&currentLootDropIndex<pendingLootItems.size()) dropNextLootItem();
		if(currentLootDropIndex>=pendingLootItems.size()) stopLootAnimation();
	}
	// Vypustí další item
	private void dropNextLootItem(){
		if(level==null||level.isClientSide()||currentLootDropIndex>=pendingLootItems.size()) return;
		ItemStack itemToDrop=pendingLootItems.get(currentLootDropIndex);
		level.playSound(null,getBlockPos(),NTrialsModSounds.BLOCK_TRIAL_SPAWNER_EJECT_ITEM.get(),SoundSource.BLOCKS,0.7f,1.0f+(level.random.nextFloat()-0.5f)*0.4f);
		double offsetX=(level.random.nextDouble()-0.5)*0.8;
		double offsetZ=(level.random.nextDouble()-0.5)*0.8;
		double offsetY=0.2+level.random.nextDouble()*0.3;
		net.minecraft.world.entity.item.ItemEntity itemEntity=new net.minecraft.world.entity.item.ItemEntity(level,
				getBlockPos().getX()+0.5+offsetX,getBlockPos().getY()+1.0+offsetY,getBlockPos().getZ()+0.5+offsetZ,itemToDrop.copy());
		itemEntity.setDeltaMovement(0.0,0.15,0.0);
		level.addFreshEntity(itemEntity);
		currentLootDropIndex++;
		setChanged();
	}
	// Zastaví animaci lootu a spumiest cooldown
	private void stopLootAnimation(){
		this.isLootAnimating=false;
		this.lootAnimationTick=0;
		this.currentLootDropIndex=0;
		this.pendingLootItems.clear();
		// Reset a cooldown
		this.isKeyActivated=false;
		this.spawnTimer=-1;
		this.spawnedBossUUID=null;
		this.participatingPlayers.clear();
		this.bossHP=NTrialsModCommonConfig.bossSpawnerBaseBossHp;
		this.cooldownTimer=NTrialsModCommonConfig.bossSpawnerMaxCooldownTicks;
		setChanged();
		if(level!=null){
			level.playSound(null,getBlockPos(),NTrialsModSounds.BLOCK_TRIAL_SPAWNER_CLOSE_SHUTTER.get(),SoundSource.BLOCKS,1.0f,1.0f);
			if(!level.isClientSide()) level.sendBlockUpdated(getBlockPos(),getBlockState(),getBlockState(),3);
		}
	}
	// Aktualizuje stav bloku
	private void updateBlockState(){
		if(level==null||level.isClientSide()) return;
		BlockState currentState=level.getBlockState(getBlockPos());
		if(!(currentState.getBlock() instanceof TrialSpawnerBossBlock)) return;
		TrialSpawnerBlock.TrialSpawnerState newState=getTrialSpawnerState();
		if(currentState.getValue(TrialSpawnerBossBlock.STATE)!=newState){
			level.setBlock(getBlockPos(),currentState.setValue(TrialSpawnerBossBlock.STATE,newState),3);
		}
	}
	private TrialSpawnerBlock.@NotNull TrialSpawnerState getTrialSpawnerState(){
		TrialSpawnerBlock.TrialSpawnerState newState;
		// Cooldown/Neaktivní = INACTIVE
		if(this.cooldownTimer>0){
			newState=TrialSpawnerBlock.TrialSpawnerState.INACTIVE;
		}else if(this.completeTrialTimer>0||this.isLootAnimating||(this.pendingLootItems!=null&&!this.pendingLootItems.isEmpty())){
			newState=TrialSpawnerBlock.TrialSpawnerState.EJECTING_REWARD;
		}else if(this.isKeyActivated&&spawnedBossUUID!=null){
			newState=TrialSpawnerBlock.TrialSpawnerState.ACTIVE;
		}else if(this.isActivated||this.isKeyActivated){
			newState=TrialSpawnerBlock.TrialSpawnerState.ACTIVE;
		}else{
			newState=TrialSpawnerBlock.TrialSpawnerState.INACTIVE;
		}
		return newState;
	}
	// Uloží NBT
	@Override
	protected void saveAdditional(@NotNull CompoundTag tag){
		super.saveAdditional(tag);
		tag.putString("key_tag",keyTag);
		// start_tag removed; keyTag (vault_tag) is used for activation/loot
		tag.putBoolean("IsActivated",isActivated);
		tag.putBoolean("IsKeyActivated",isKeyActivated);
		tag.putInt("SpawnTimer",spawnTimer);
		tag.putInt("BossHP",bossHP);
		tag.putInt("CompleteTrialTimer",completeTrialTimer);
		tag.putBoolean("IsLootAnimating",isLootAnimating);
		tag.putInt("LootAnimationTick",lootAnimationTick);
		tag.putInt("CurrentLootDropIndex",currentLootDropIndex);
		tag.putInt("CooldownTimer",cooldownTimer);
		if(spawnedBossUUID!=null) tag.putUUID("SpawnedBossUUID",spawnedBossUUID);
		if(bossMobType!=null) tag.putString("BossMobType",EntityType.getKey(bossMobType).toString());
		// Boss loot table (optional)
		tag.putString("BossLootTable",bossLootTable==null?"":bossLootTable);
		net.minecraft.nbt.ListTag lootItemsTag=new net.minecraft.nbt.ListTag();
		for(ItemStack item: pendingLootItems){
			CompoundTag itemTag=new CompoundTag();
			item.save(itemTag);
			lootItemsTag.add(itemTag);
		}
		tag.put("PendingLootItems",lootItemsTag);
		ListTag participatingPlayersTag=new ListTag();
		for(UUID uuid: participatingPlayers) participatingPlayersTag.add(StringTag.valueOf(uuid.toString()));
		tag.put("ParticipatingPlayers",participatingPlayersTag);
		ListTag playersTag=new ListTag();
		for(UUID uuid: playersWhoReceivedReward) playersTag.add(StringTag.valueOf(uuid.toString()));
		tag.put("PlayersWhoReceivedReward",playersTag);
	}
	// Načte NBT
	@Override
	public void load(@NotNull CompoundTag tag){
		super.load(tag);
		if(tag.contains("key_tag")) keyTag=tag.getString("key_tag");
		// start_tag removed; keyTag (vault_tag) is used for activation/loot
		isActivated=tag.getBoolean("IsActivated");
		isKeyActivated=tag.getBoolean("IsKeyActivated");
		spawnTimer=tag.getInt("SpawnTimer");
		bossHP=tag.getInt("BossHP");
		completeTrialTimer=tag.getInt("CompleteTrialTimer");
		isLootAnimating=tag.getBoolean("IsLootAnimating");
		lootAnimationTick=tag.getInt("LootAnimationTick");
		currentLootDropIndex=tag.getInt("CurrentLootDropIndex");
		this.cooldownTimer=tag.getInt("CooldownTimer");
		if(tag.hasUUID("SpawnedBossUUID")) spawnedBossUUID=tag.getUUID("SpawnedBossUUID");
		if(tag.contains("BossMobType")){
			try{
				String mobId=tag.getString("BossMobType");
				EntityType<?> type=net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.parse(mobId));
				if(type!=null) bossMobType=type;
			}catch(Exception ignored){
			}
		}
		this.pendingLootItems.clear();
		net.minecraft.nbt.ListTag lootItemsTag=tag.getList("PendingLootItems",10);
		for(int i=0;i<lootItemsTag.size();i++) this.pendingLootItems.add(ItemStack.of(lootItemsTag.getCompound(i)));
		playersWhoReceivedReward.clear();
		ListTag playersTag=tag.getList("PlayersWhoReceivedReward",8);
		for(int i=0;i<playersTag.size();i++){
			try{
				playersWhoReceivedReward.add(UUID.fromString(playersTag.getString(i)));
			}catch(IllegalArgumentException ignored){
			}
		}
		if(tag.contains("BossLootTable")) this.bossLootTable=tag.getString("BossLootTable");
		participatingPlayers.clear();
		ListTag participatingPlayersTag=tag.getList("ParticipatingPlayers",8);
		for(int i=0;i<participatingPlayersTag.size();i++){
			try{
				participatingPlayers.add(UUID.fromString(participatingPlayersTag.getString(i)));
			}catch(IllegalArgumentException ignored){
			}
		}
	}
	public boolean isActivated(){
		return isActivated;
	}
	public boolean isKeyActivated(){
		return isKeyActivated;
	}
	// Client-side tick
	public void clientTick(){
		this.clientTickCount++;
		if(level==null||!level.isClientSide) return;
		BlockState state=level.getBlockState(worldPosition);
		if(!(state.getBlock() instanceof TrialSpawnerBossBlock)) return;
		TrialSpawnerBlock.TrialSpawnerState spawnerState=state.getValue(TrialSpawnerBossBlock.STATE);
		boolean isCurrentlyActive=spawnerState==TrialSpawnerBlock.TrialSpawnerState.ACTIVE;
		if(isCurrentlyActive&&!wasActivated) wasActivated=true;
		else if(!isCurrentlyActive) wasActivated=false;
		if(spawnerState==TrialSpawnerBlock.TrialSpawnerState.ACTIVE&&clientTickCount%10==0){
			level.addParticle(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
					worldPosition.getX()+0.3+level.random.nextDouble()*0.4,
					worldPosition.getY()+0.5+level.random.nextDouble()*0.4,
					worldPosition.getZ()+0.3+level.random.nextDouble()*0.4,0.0,0.05,0.0);
		}
	}
	public int getClientTickCount(){
		return clientTickCount;
	}
}