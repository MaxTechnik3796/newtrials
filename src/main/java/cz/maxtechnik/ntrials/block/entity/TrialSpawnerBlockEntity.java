package cz.maxtechnik.ntrials.block.entity;

import cz.maxtechnik.ntrials.NTrialsModCommonConfig;
import cz.maxtechnik.ntrials.block.TrialSpawnerBlock;
import cz.maxtechnik.ntrials.init.other.NTrialsModBlockEntities;
import cz.maxtechnik.ntrials.init.basic.NTrialsModSounds;
import cz.maxtechnik.ntrials.init.other.NTrialsModEntityTypes;
import cz.maxtechnik.ntrials.network.NetworkHandler;
import cz.maxtechnik.ntrials.network.TrialSpawnerSyncPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import cz.maxtechnik.ntrials.init.other.NTrialsModMobEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.*;
@SuppressWarnings("deprecation")
public class TrialSpawnerBlockEntity extends BlockEntity{
	// ---- Normal spawner constants ----
	private static final List<MobEffect> OMNIOUS_EFFECTS=List.of(
			MobEffects.REGENERATION,MobEffects.BLINDNESS,MobEffects.POISON,MobEffects.MOVEMENT_SLOWDOWN,
			MobEffects.CONFUSION,MobEffects.WEAKNESS,MobEffects.MOVEMENT_SPEED,MobEffects.DAMAGE_BOOST
	);
	private static final String[] ARMOR_HELMETS={"minecraft:iron_helmet","minecraft:golden_helmet","minecraft:diamond_helmet"};
	private static final String[] ARMOR_CHESTPLATES={"minecraft:iron_chestplate","minecraft:golden_chestplate","minecraft:diamond_chestplate"};
	private static final String[] ARMOR_LEGGINGS={"minecraft:iron_leggings","minecraft:golden_leggings","minecraft:diamond_leggings"};
	private static final String[] ARMOR_BOOTS={"minecraft:iron_boots","minecraft:golden_boots","minecraft:diamond_boots"};
	private static final String[] WEAPONS={"minecraft:stone_sword","minecraft:iron_sword","minecraft:diamond_sword","minecraft:iron_axe","minecraft:diamond_axe"};
	private static final String DEFAULT_BOSS_LOOT="ntrials:chests/spawner_boss";

	// ---- Normal spawner fields ----
	private final Set<UUID> spawnedEntities=new HashSet<>();
	private int cooldownTime=0;
	private boolean isOminous=false;
	private boolean redstoneEnabled=true;
	private int tickCount=0;
	private int clientTickCount=0;
	private EntityType<?> spawnEntity=null;
	private boolean hasBeenSynced=false;
	private boolean shouldResetOminousOnCooldownEnd=false;
	private transient boolean wasOminous=false;
	private transient TrialSpawnerBlock.TrialSpawnerState lastState=null;
	private int mobsPerWave=3;
	private int totalMobs=6;
	private int remainingMobs=0;
	private int playersCount=0;
	private int currentWave=0;
	private boolean trialActive=false;
	private String normalLootTable="ntrials:chests/spawner";
	private String ominousLootTable="ntrials:chests/spawner_ominous";
	private String vaultTag="";
	private boolean isLootAnimating=false;
	private int lootAnimationTick=0;
	private List<ItemStack> pendingLootItems=new ArrayList<>();
	private int currentLootDropIndex=0;
	private int completeTrialTimer=-1;
	private int startTrialTimer=-1;

	// ---- Boss spawner fields ----
	private boolean isBossActivated=false;
	private boolean isBossKeyActivated=false;
	private int bossSpawnTimer=-1;
	private UUID spawnedBossUUID=null;
	private final Set<UUID> nearbyPlayers=new HashSet<>();
	private final Set<UUID> participatingPlayers=new HashSet<>();
	private int bossHP=NTrialsModCommonConfig.bossSpawnerBaseBossHp;
	private final Set<UUID> playersWhoReceivedReward=new HashSet<>();
	private int cooldownTimer=0;
	private String keyTag="";
	private EntityType<?> bossMobType=NTrialsModEntityTypes.BREEZE_BOSS.get();
	private String bossLootTable="";
	private transient boolean wasActivated=false;

	public TrialSpawnerBlockEntity(BlockPos pos,BlockState blockState){
		super(NTrialsModBlockEntities.TRIAL_SPAWNER_BLOCK_ENTITY.get(),pos,blockState);
	}

	// ---- Type helper ----
	public TrialSpawnerBlock.SpawnerType getSpawnerType(){
		BlockState state=getBlockState();
		if(state.getBlock() instanceof TrialSpawnerBlock){
			return state.getValue(TrialSpawnerBlock.TYPE);
		}
		return TrialSpawnerBlock.SpawnerType.NORMAL;
	}

	// ====== TICK DISPATCH ======
	public void tick(){
		if(getSpawnerType()==TrialSpawnerBlock.SpawnerType.BOSS) tickBoss();
		else tickNormal();
	}
	public void clientTick(){
		if(getSpawnerType()==TrialSpawnerBlock.SpawnerType.BOSS) clientTickBoss();
		else clientTickNormal();
	}

	// ====== NORMAL TICK ======
	private void tickNormal(){
		this.tickCount++;
		if(this.cooldownTime>0){
			this.cooldownTime--;
			if(this.cooldownTime==NTrialsModCommonConfig.spawnerCooldownOminous&&this.shouldResetOminousOnCooldownEnd){
				if(level==null) return;
				BlockState currentState=level.getBlockState(getBlockPos());
				if(currentState.getValue(TrialSpawnerBlock.OMINOUS)){
					level.setBlock(getBlockPos(),currentState.setValue(TrialSpawnerBlock.OMINOUS,false),3);
					this.setOminous(false);
					this.shouldResetOminousOnCooldownEnd=false;
					updateBlockStateNormal();
				}
			}
		}
		if(startTrialTimer>0&&--startTrialTimer==0) spawnWave();
		if(completeTrialTimer>0&&--completeTrialTimer==0){
			trialActive=false;
			currentWave=0;
			remainingMobs=0;
			spawnedEntities.clear();
			generateLootRewardNormal();
			setCooldownTime(36000);
		}
		if(isLootAnimating) tickLootAnimation();
		if(this.tickCount%100==0){
			if(level==null) return;
			if(!level.isClientSide()&&hasSpawnEntity()&&!trialActive&&cooldownTime==0){
				boolean isOminousBlock=level.getBlockState(getBlockPos()).getValue(TrialSpawnerBlock.OMINOUS);
				level.playSound(null,getBlockPos(),isOminousBlock?NTrialsModSounds.BLOCK_TRIAL_SPAWNER_AMBIENT_OMINOUS.get():NTrialsModSounds.BLOCK_TRIAL_SPAWNER_AMBIENT.get(),
						SoundSource.BLOCKS,isOminousBlock?0.8f:0.6f,1.0f);
			}
		}
		if(this.tickCount%40==0) checkAndManageTrial();
		if(!hasBeenSynced&&this.spawnEntity!=null&&this.tickCount%200==0){
			syncToClients();
			hasBeenSynced=true;
		}
		if(level!=null&&!level.isClientSide()&&isOminous()&&trialActive&&this.tickCount%600==0) spawnRandomOminousEffect();
	}
	private void clientTickNormal(){
		this.clientTickCount++;
		if(level==null||!level.isClientSide) return;
		BlockState state=level.getBlockState(worldPosition);
		if(!(state.getBlock() instanceof TrialSpawnerBlock)) return;
		boolean ominous=state.getValue(TrialSpawnerBlock.OMINOUS);
		TrialSpawnerBlock.TrialSpawnerState spawnerState=state.getValue(TrialSpawnerBlock.STATE);
		if(ominous&&!wasOminous){
			BlockPos pos=worldPosition;
			for(int i=0;i<20;i++){
				double x=pos.getX()+(level.random.nextDouble()-0.5)*6;
				double y=pos.getY()+level.random.nextDouble()*3;
				double z=pos.getZ()+(level.random.nextDouble()-0.5)*6;
				double dist=Math.sqrt(Math.pow(x-pos.getX(),2)+Math.pow(y-pos.getY(),2)+Math.pow(z-pos.getZ(),2));
				if(dist<=3.0)
					level.addParticle(ParticleTypes.SOUL_FIRE_FLAME,x,y,z,(level.random.nextDouble()-0.5)*0.1,level.random.nextDouble()*0.2,(level.random.nextDouble()-0.5)*0.1);
			}
			wasOminous=true;
		}else if(!ominous) wasOminous=false;
		if(lastState!=spawnerState&&(spawnerState==TrialSpawnerBlock.TrialSpawnerState.WAITING_FOR_PLAYERS||spawnerState==TrialSpawnerBlock.TrialSpawnerState.ACTIVE)){
			spawnActivationParticles(ominous);
		}
		lastState=spawnerState;
		if(spawnerState==TrialSpawnerBlock.TrialSpawnerState.WAITING_FOR_PLAYERS||spawnerState==TrialSpawnerBlock.TrialSpawnerState.ACTIVE){
			if(clientTickCount%10==0){
				ParticleOptions particle=ominous?ParticleTypes.SOUL_FIRE_FLAME:ParticleTypes.FLAME;
				level.addParticle(particle,worldPosition.getX()+0.3+level.random.nextDouble()*0.4,worldPosition.getY()+0.5+level.random.nextDouble()*0.4,worldPosition.getZ()+0.3+level.random.nextDouble()*0.4,0.0,0.05,0.0);
			}
		}
	}
	private void spawnActivationParticles(boolean ominous){
		if(level==null) return;
		BlockPos center=worldPosition;
		double centerX=center.getX()+0.5, centerY=center.getY()+0.5, centerZ=center.getZ()+0.5;
		ParticleOptions particle=ominous?ParticleTypes.SOUL_FIRE_FLAME:ParticleTypes.FLAME;
		for(int i=0;i<15;i++){
			double angle=level.random.nextDouble()*2*Math.PI;
			double verticalAngle=(level.random.nextDouble()-0.5)*Math.PI*0.5;
			double speed=0.1+level.random.nextDouble()*0.1;
			level.addParticle(particle,centerX,centerY,centerZ,Math.cos(angle)*Math.cos(verticalAngle)*speed,Math.sin(verticalAngle)*speed,Math.sin(angle)*Math.cos(verticalAngle)*speed);
		}
	}

	// ====== BOSS TICK ======
	private void tickBoss(){
		if(level==null||level.isClientSide) return;
		// Cooldown logic
		if(cooldownTimer>0){
			if(--cooldownTimer==0){
				level.playSound(null,getBlockPos(),NTrialsModSounds.BLOCK_VAULT_DEACTIVATE.get(),SoundSource.BLOCKS,1.0f,1.0f);
			}
			if(level.getGameTime()%20==0) updateBlockStateBoss();
			return;
		}
		if(level.getGameTime()%40==0){
			updatePlayersInRange();
			checkAndActivateByProximity();
		}
		if(isBossKeyActivated&&bossSpawnTimer>0&&--bossSpawnTimer==0) spawnBreezeBoss();
		if(completeTrialTimer>0&&--completeTrialTimer==0) generateLootRewardBoss();
		if(isLootAnimating) tickLootAnimation();
		if(isBossKeyActivated&&spawnedBossUUID!=null&&level instanceof ServerLevel serverLevel){
			net.minecraft.world.entity.Entity boss=serverLevel.getEntity(spawnedBossUUID);
			if(boss==null||!boss.isAlive()) handleBossKilled();
		}
		if(level.getGameTime()%20==0) updateBlockStateBoss();
	}
	private void clientTickBoss(){
		this.clientTickCount++;
		if(level==null||!level.isClientSide) return;
		BlockState state=level.getBlockState(worldPosition);
		if(!(state.getBlock() instanceof TrialSpawnerBlock)) return;
		TrialSpawnerBlock.TrialSpawnerState spawnerState=state.getValue(TrialSpawnerBlock.STATE);
		boolean isCurrentlyActive=spawnerState==TrialSpawnerBlock.TrialSpawnerState.ACTIVE;
		if(isCurrentlyActive&&!wasActivated) wasActivated=true;
		else if(!isCurrentlyActive) wasActivated=false;
		if(spawnerState==TrialSpawnerBlock.TrialSpawnerState.ACTIVE&&clientTickCount%10==0){
			level.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
					worldPosition.getX()+0.3+level.random.nextDouble()*0.4,
					worldPosition.getY()+0.5+level.random.nextDouble()*0.4,
					worldPosition.getZ()+0.3+level.random.nextDouble()*0.4,0.0,0.05,0.0);
		}
	}

	// ====== BOSS METHODS ======
	private void updatePlayersInRange(){
		if(level==null) return;
		double range=20.0;
		AABB searchArea=new AABB(getBlockPos().getX()-range,getBlockPos().getY()-range,getBlockPos().getZ()-range,
				getBlockPos().getX()+range,getBlockPos().getY()+range,getBlockPos().getZ()+range);
		nearbyPlayers.clear();
		for(Player player: level.getEntitiesOfClass(Player.class,searchArea)) nearbyPlayers.add(player.getUUID());
	}
	private void checkAndActivateByProximity(){
		if(level==null||level.isClientSide) return;
		double range=8.0;
		AABB searchArea=new AABB(getBlockPos().getX()-range,getBlockPos().getY()-range,getBlockPos().getZ()-range,
				getBlockPos().getX()+range,getBlockPos().getY()+range,getBlockPos().getZ()+range);
		boolean hasPlayers=!level.getEntitiesOfClass(Player.class,searchArea).isEmpty();
		if(hasPlayers&&!isBossActivated){
			isBossActivated=true;
			setChanged();
			BlockState currentState=level.getBlockState(getBlockPos());
			if(currentState.getBlock() instanceof TrialSpawnerBlock){
				level.setBlock(getBlockPos(),currentState.setValue(TrialSpawnerBlock.STATE,TrialSpawnerBlock.TrialSpawnerState.ACTIVE),Block.UPDATE_ALL);
			}
		}else if(!hasPlayers&&isBossActivated&&!isBossKeyActivated){
			isBossActivated=false;
			setChanged();
			BlockState currentState=level.getBlockState(getBlockPos());
			if(currentState.getBlock() instanceof TrialSpawnerBlock){
				level.setBlock(getBlockPos(),currentState.setValue(TrialSpawnerBlock.STATE,TrialSpawnerBlock.TrialSpawnerState.INACTIVE),Block.UPDATE_ALL);
			}
		}
	}
	public void activateWithKey(){
		if(!isBossKeyActivated&&level!=null&&!level.isClientSide&&isBossActivated){
			isBossKeyActivated=true;
			this.participatingPlayers.clear();
			this.participatingPlayers.addAll(this.nearbyPlayers);
			bossSpawnTimer=NTrialsModCommonConfig.bossSpawnerSpawnDelay;
			setChanged();
			BlockState currentState=level.getBlockState(getBlockPos());
			if(currentState.getBlock() instanceof TrialSpawnerBlock){
				level.setBlock(getBlockPos(),currentState.setValue(TrialSpawnerBlock.STATE,TrialSpawnerBlock.TrialSpawnerState.ACTIVE),Block.UPDATE_ALL);
			}
			level.playSound(null,getBlockPos(),NTrialsModSounds.BLOCK_TRIAL_SPAWNER_DETECT_PLAYER.get(),SoundSource.BLOCKS,1.0f,1.0f);
			spawnKeyActivationParticles();
		}
	}
	private void spawnKeyActivationParticles(){
		if(level==null||level.isClientSide||!(level instanceof ServerLevel serverLevel)) return;
		BlockPos center=getBlockPos();
		double centerX=center.getX()+0.5, centerY=center.getY()+0.5, centerZ=center.getZ()+0.5;
		for(int i=0;i<15;i++){
			double angle=level.random.nextDouble()*2*Math.PI;
			double verticalAngle=(level.random.nextDouble()-0.5)*Math.PI*0.5;
			double speed=0.1+level.random.nextDouble()*0.1;
			serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,centerX,centerY,centerZ,1,
					Math.cos(angle)*Math.cos(verticalAngle)*speed,Math.sin(verticalAngle)*speed,Math.sin(angle)*Math.cos(verticalAngle)*speed,0.0);
		}
	}
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
				var healthAttribute=boss.getAttribute(Attributes.MAX_HEALTH);
				if(healthAttribute!=null){
					healthAttribute.setBaseValue(bossHP);
					boss.setHealth(boss.getMaxHealth());
				}
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
	private void handleBossKilled(){
		if(level==null||level.isClientSide) return;
		completeTrialTimer=NTrialsModCommonConfig.bossSpawnerCompleteTrialDelay;
		spawnedBossUUID=null;
		setChanged();
	}
	private void generateLootRewardBoss(){
		if(level==null||level.isClientSide||!(level instanceof ServerLevel serverLevel)) return;
		String lootPath=this.bossLootTable==null||this.bossLootTable.isEmpty()?DEFAULT_BOSS_LOOT:this.bossLootTable;
		ResourceLocation lootTableId;
		LootTable lootTable;
		try{
			lootTableId=ResourceLocation.parse(lootPath);
			lootTable=serverLevel.getServer().getLootData().getLootTable(lootTableId);
			if(lootTable==LootTable.EMPTY) throw new IllegalArgumentException("empty loot table");
		}catch(Exception ex){
			lootTableId=ResourceLocation.parse(DEFAULT_BOSS_LOOT);
			lootTable=serverLevel.getServer().getLootData().getLootTable(lootTableId);
		}
		LootParams lootParams=new LootParams.Builder(serverLevel).withParameter(LootContextParams.ORIGIN,Vec3.atCenterOf(getBlockPos())).create(LootContextParamSets.CHEST);
		int pCount=Math.max(1,participatingPlayers.size());
		List<ItemStack> baseLoot=lootTable.getRandomItems(lootParams);
		List<ItemStack> allLootItems=new ArrayList<>();
		for(int i=0;i<pCount;i++){
			for(ItemStack item: baseLoot){
				if(!item.isEmpty()) allLootItems.add(item.copy());
			}
		}
		for(UUID playerUUID: new HashSet<>(participatingPlayers)){
			if(!this.hasPlayerReceivedReward(playerUUID)) this.addPlayerWhoReceivedReward(playerUUID);
		}
		if(!allLootItems.isEmpty()) startLootAnimation(allLootItems);
	}
	private void updateBlockStateBoss(){
		if(level==null||level.isClientSide()) return;
		BlockState currentState=level.getBlockState(getBlockPos());
		if(!(currentState.getBlock() instanceof TrialSpawnerBlock)) return;
		TrialSpawnerBlock.TrialSpawnerState newState=getTrialSpawnerStateBoss();
		if(currentState.getValue(TrialSpawnerBlock.STATE)!=newState){
			level.setBlock(getBlockPos(),currentState.setValue(TrialSpawnerBlock.STATE,newState),3);
		}
	}
	private TrialSpawnerBlock.@NotNull TrialSpawnerState getTrialSpawnerStateBoss(){
		if(this.cooldownTimer>0){
			return TrialSpawnerBlock.TrialSpawnerState.INACTIVE;
		}else if(this.completeTrialTimer>0||this.isLootAnimating||(this.pendingLootItems!=null&&!this.pendingLootItems.isEmpty())){
			return TrialSpawnerBlock.TrialSpawnerState.EJECTING_REWARD;
		}else if(this.isBossKeyActivated&&spawnedBossUUID!=null){
			return TrialSpawnerBlock.TrialSpawnerState.ACTIVE;
		}else if(this.isBossActivated||this.isBossKeyActivated){
			return TrialSpawnerBlock.TrialSpawnerState.ACTIVE;
		}else{
			return TrialSpawnerBlock.TrialSpawnerState.INACTIVE;
		}
	}

	// ====== SHARED METHODS ======
	private BlockPos findSpawnPosition(){
		if(level==null) return getBlockPos();
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
	private boolean isValidSpawnPosition(BlockPos pos){
		if(level==null) return false;
		boolean isSolid=level.getBlockState(pos.below()).isSolid();
		return level.getBlockState(pos).isAir()&&level.getBlockState(pos.above()).isAir()&&isSolid;
	}
	private void startLootAnimation(List<ItemStack> lootItems){
		this.isLootAnimating=true;
		this.lootAnimationTick=0;
		this.currentLootDropIndex=0;
		this.pendingLootItems=new ArrayList<>(lootItems);
		setChanged();
		if(level==null) return;
		level.playSound(null,getBlockPos(),NTrialsModSounds.BLOCK_TRIAL_SPAWNER_OPEN_SHUTTER.get(),SoundSource.BLOCKS,1.0f,1.0f);
		level.playSound(null,getBlockPos(),NTrialsModSounds.BLOCK_TRIAL_SPAWNER_SPAWN_ITEM_BEGIN.get(),SoundSource.BLOCKS,0.8f,1.0f);
		if(level!=null&&!level.isClientSide()) level.sendBlockUpdated(getBlockPos(),getBlockState(),getBlockState(),3);
	}
	private void tickLootAnimation(){
		if(!isLootAnimating||pendingLootItems.isEmpty()){
			stopLootAnimation();
			return;
		}
		lootAnimationTick++;
		if(lootAnimationTick%NTrialsModCommonConfig.spawnerLootDropInterval==0&&currentLootDropIndex<pendingLootItems.size()) dropNextLootItem();
		if(currentLootDropIndex>=pendingLootItems.size()) stopLootAnimation();
	}
	private void dropNextLootItem(){
		if(level==null||level.isClientSide()||currentLootDropIndex>=pendingLootItems.size()) return;
		ItemStack itemToDrop=pendingLootItems.get(currentLootDropIndex);
		level.playSound(null,getBlockPos(),NTrialsModSounds.BLOCK_TRIAL_SPAWNER_EJECT_ITEM.get(),SoundSource.BLOCKS,0.7f,1.0f+(level.random.nextFloat()-0.5f)*0.4f);
		double offsetX=(level.random.nextDouble()-0.5)*0.8;
		double offsetZ=(level.random.nextDouble()-0.5)*0.8;
		double offsetY=0.2+level.random.nextDouble()*0.3;
		net.minecraft.world.entity.item.ItemEntity itemEntity=new net.minecraft.world.entity.item.ItemEntity(level,getBlockPos().getX()+0.5+offsetX,getBlockPos().getY()+1.0+offsetY,getBlockPos().getZ()+0.5+offsetZ,itemToDrop.copy());
		itemEntity.setDeltaMovement(0.0,0.15,0.0);
		level.addFreshEntity(itemEntity);
		currentLootDropIndex++;
		setChanged();
	}
	private void stopLootAnimation(){
		this.isLootAnimating=false;
		this.lootAnimationTick=0;
		this.currentLootDropIndex=0;
		this.pendingLootItems.clear();
		setChanged();
		if(level==null) return;
		level.playSound(null,getBlockPos(),NTrialsModSounds.BLOCK_TRIAL_SPAWNER_CLOSE_SHUTTER.get(),SoundSource.BLOCKS,1.0f,1.0f);
		if(level!=null&&!level.isClientSide()){
			level.sendBlockUpdated(getBlockPos(),getBlockState(),getBlockState(),3);
			// Boss-specific reset after loot animation
			if(getSpawnerType()==TrialSpawnerBlock.SpawnerType.BOSS){
				this.isBossKeyActivated=false;
				this.bossSpawnTimer=-1;
				this.spawnedBossUUID=null;
				this.participatingPlayers.clear();
				this.bossHP=NTrialsModCommonConfig.bossSpawnerBaseBossHp;
				this.cooldownTimer=NTrialsModCommonConfig.bossSpawnerMaxCooldownTicks;
				setChanged();
			}else{
				updateBlockStateNormal();
			}
		}
	}

	// ====== NORMAL SPAWNER METHODS ======
	private void checkAndManageTrial(){
		if(level==null||level.isClientSide()||!hasSpawnEntity()) return;
		double range=14.0;
		AABB searchArea=new AABB(
				getBlockPos().getX()-range,getBlockPos().getY()-range,getBlockPos().getZ()-range,
				getBlockPos().getX()+range,getBlockPos().getY()+range,getBlockPos().getZ()+range
		);
		List<Player> players=level.getEntitiesOfClass(Player.class,searchArea);
		int playerCount=players.size();
		boolean hasSurvivalPlayer=false, hasPlayerWithBadOmen=false;
		for(Player player: players){
			if(player instanceof ServerPlayer serverPlayer&&serverPlayer.gameMode.getGameModeForPlayer()==GameType.SURVIVAL){
				hasSurvivalPlayer=true;
				if(player.hasEffect(MobEffects.BAD_OMEN)){
					player.removeEffect(MobEffects.BAD_OMEN);
					player.addEffect(new MobEffectInstance(NTrialsModMobEffects.TRIAL_OMEN.get(),36000,0));
				}
				if(player.hasEffect(NTrialsModMobEffects.TRIAL_OMEN.get())) hasPlayerWithBadOmen=true;
			}
		}
		if(hasPlayerWithBadOmen&&this.redstoneEnabled){
			BlockState currentState=level.getBlockState(getBlockPos());
			if(!currentState.getValue(TrialSpawnerBlock.OMINOUS)){
				level.setBlock(getBlockPos(),currentState.setValue(TrialSpawnerBlock.OMINOUS,true),3);
				this.setOminous(true);
				level.playSound(null,getBlockPos(),NTrialsModSounds.BLOCK_TRIAL_SPAWNER_OMINOUS_ACTIVATE.get(),SoundSource.BLOCKS,1.0f,1.0f);
			}
			if(this.cooldownTime>0&&!this.shouldResetOminousOnCooldownEnd){
				this.cooldownTime=0;
			}
		}
		if(this.cooldownTime>0) return;
		if(hasSurvivalPlayer&&!trialActive&&currentWave==0&&this.redstoneEnabled){
			level.playSound(null,getBlockPos(),NTrialsModSounds.BLOCK_TRIAL_SPAWNER_DETECT_PLAYER.get(),SoundSource.BLOCKS,1.0f,1.0f);
			startTrial();
		}else if(playerCount==0&&trialActive&&spawnedEntities.isEmpty()){
			stopTrial();
		}else if(trialActive&&playerCount>0){
			checkWaveCompletion();
		}
		updateBlockStateNormal();
	}
	private void updateBlockStateNormal(){
		if(level==null||level.isClientSide()) return;
		BlockState currentState=level.getBlockState(getBlockPos());
		if(!(currentState.getBlock() instanceof TrialSpawnerBlock)) return;
		TrialSpawnerBlock.TrialSpawnerState newState;
		if(this.completeTrialTimer>0||this.isLootAnimating||(this.pendingLootItems!=null&&!this.pendingLootItems.isEmpty())){
			newState=TrialSpawnerBlock.TrialSpawnerState.EJECTING_REWARD;
		}else if(this.cooldownTime>NTrialsModCommonConfig.spawnerCooldownOminous&&this.isOminous){
			newState=TrialSpawnerBlock.TrialSpawnerState.COOLDOWN;
		}else if(this.cooldownTime>0){
			newState=TrialSpawnerBlock.TrialSpawnerState.COOLDOWN;
		}else if(this.trialActive){
			newState=TrialSpawnerBlock.TrialSpawnerState.ACTIVE;
		}else if(!hasSpawnEntity()){
			newState=TrialSpawnerBlock.TrialSpawnerState.INACTIVE;
		}else{
			newState=TrialSpawnerBlock.TrialSpawnerState.WAITING_FOR_PLAYERS;
		}
		if(currentState.getValue(TrialSpawnerBlock.STATE)!=newState){
			level.setBlock(getBlockPos(),currentState.setValue(TrialSpawnerBlock.STATE,newState),3);
		}
	}
	private void startTrial(){
		trialActive=true;
		currentWave=1;
		spawnedEntities.clear();
		updateMobCountBasedOnPlayers();
		this.remainingMobs=Math.max(0,this.totalMobs);
		startTrialTimer=20;
	}
	private void updateMobCountBasedOnPlayers(){
		if(level==null||level.isClientSide()) return;
		double scanRange=28.0;
		AABB scanArea=new AABB(
				getBlockPos().getX()-scanRange,getBlockPos().getY()-scanRange,getBlockPos().getZ()-scanRange,
				getBlockPos().getX()+scanRange,getBlockPos().getY()+scanRange,getBlockPos().getZ()+scanRange
		);
		int playerCount=level.getEntitiesOfClass(Player.class,scanArea).size();
		this.playersCount=playerCount;
		boolean isBreeze=spawnEntity!=null&&spawnEntity==NTrialsModEntityTypes.BREEZE.get();
		int extraPlayers=Math.max(0,playerCount-1);
		if(isBreeze){
			this.mobsPerWave=NTrialsModCommonConfig.spawnerBreezeBaseMobsPerWave+(extraPlayers*NTrialsModCommonConfig.spawnerBreezeMobsAddedPerPlayer);
			this.totalMobs=NTrialsModCommonConfig.spawnerBreezeBaseTotalMobs+(extraPlayers*NTrialsModCommonConfig.spawnerBreezeTotalAddedPerPlayer);
		}else{
			this.mobsPerWave=NTrialsModCommonConfig.spawnerDefaultBaseMobsPerWave+extraPlayers;
			this.totalMobs=NTrialsModCommonConfig.spawnerDefaultBaseTotalMobs+(extraPlayers*2);
		}
		if(this.trialActive){
			int alive=this.spawnedEntities.size();
			this.remainingMobs=Math.max(0,this.totalMobs-alive);
		}
	}
	private void stopTrial(){
		trialActive=false;
		currentWave=0;
		remainingMobs=0;
		cleanupSpawnedEntities();
		spawnedEntities.clear();
		updateBlockStateNormal();
	}
	private void spawnWave(){
		if(level==null||spawnEntity==null) return;
		BlockState currentState=level.getBlockState(getBlockPos());
		boolean isOminousBlock=currentState.getValue(TrialSpawnerBlock.OMINOUS);
		level.playSound(null,getBlockPos(),NTrialsModSounds.BLOCK_TRIAL_SPAWNER_SPAWN.get(),SoundSource.BLOCKS,1.0f,1.0f);
		int toSpawn=Math.min(mobsPerWave,Math.max(0,remainingMobs));
		for(int i=0;i<toSpawn;i++){
			BlockPos spawnPos=findSpawnPosition();
			net.minecraft.world.entity.Entity entity=spawnEntity.create(level);
			if(entity!=null){
				entity.setPos(spawnPos.getX()+0.5,spawnPos.getY(),spawnPos.getZ()+0.5);
				if(entity instanceof net.minecraft.world.entity.Mob mob){
					if(isOminousBlock) equipOminousMob(mob);
					@SuppressWarnings({"deprecation","unused"})
					var ignored=mob.finalizeSpawn((ServerLevel)level,level.getCurrentDifficultyAt(spawnPos),net.minecraft.world.entity.MobSpawnType.SPAWNER,null,null);
				}
				level.addFreshEntity(entity);
				spawnedEntities.add(entity.getUUID());
				remainingMobs=Math.max(0,remainingMobs-1);
				spawnSpawnParticles(getBlockPos(),isOminousBlock);
			}
		}
	}
	private void checkWaveCompletion(){
		spawnedEntities.removeIf(uuid->{
			if(level==null) return true;
			net.minecraft.world.entity.Entity entity=((ServerLevel)level).getEntity(uuid);
			return entity==null||!entity.isAlive();
		});
		int alive=spawnedEntities.size();
		if(alive==0&&remainingMobs<=0){
			completeTrial();
			return;
		}
		int needed=mobsPerWave-alive;
		if(needed>0&&remainingMobs>0&&spawnEntity!=null){
			int spawnCount=Math.min(needed,remainingMobs);
			if(level==null) return;
			level.playSound(null,getBlockPos(),NTrialsModSounds.BLOCK_TRIAL_SPAWNER_SPAWN.get(),SoundSource.BLOCKS,1.0f,1.0f);
			BlockState currentState=level.getBlockState(getBlockPos());
			boolean isOminousBlock=currentState.getValue(TrialSpawnerBlock.OMINOUS);
			for(int i=0;i<spawnCount;i++){
				BlockPos spawnPos=findSpawnPosition();
				net.minecraft.world.entity.Entity entity=spawnEntity.create(level);
				if(entity!=null){
					entity.setPos(spawnPos.getX()+0.5,spawnPos.getY(),spawnPos.getZ()+0.5);
					if(entity instanceof net.minecraft.world.entity.Mob mob){
						if(isOminousBlock) equipOminousMob(mob);
						@SuppressWarnings({"deprecation","unused"})
						var ignored=mob.finalizeSpawn((ServerLevel)level,level.getCurrentDifficultyAt(spawnPos),net.minecraft.world.entity.MobSpawnType.SPAWNER,null,null);
					}
					level.addFreshEntity(entity);
					spawnedEntities.add(entity.getUUID());
					spawnSpawnParticles(getBlockPos(),isOminousBlock);
					remainingMobs=Math.max(0,remainingMobs-1);
				}
			}
		}
	}
	private void completeTrial(){
		completeTrialTimer=20;
	}
	private void generateLootRewardNormal(){
		if(level==null||level.isClientSide()||!(level instanceof ServerLevel serverLevel)) return;
		BlockState currentState=level.getBlockState(getBlockPos());
		boolean isOminousBlock=currentState.getValue(TrialSpawnerBlock.OMINOUS);
		if(isOminousBlock) this.shouldResetOminousOnCooldownEnd=true;
		String lootTableId=isOminousBlock?ominousLootTable:normalLootTable;
		ResourceLocation lootTableLocation;
		LootTable lootTable;
		try{
			lootTableLocation=ResourceLocation.parse(lootTableId);
			lootTable=serverLevel.getServer().getLootData().getLootTable(lootTableLocation);
			if(lootTable==LootTable.EMPTY) throw new IllegalArgumentException("empty loot table");
		}catch(Exception e){
			lootTableLocation=ResourceLocation.parse(normalLootTable);
			lootTable=serverLevel.getServer().getLootData().getLootTable(lootTableLocation);
		}
		LootParams lootParams=new LootParams.Builder(serverLevel)
				.withParameter(LootContextParams.ORIGIN,getBlockPos().getCenter())
				.withParameter(LootContextParams.BLOCK_STATE,currentState)
				.withParameter(LootContextParams.TOOL,ItemStack.EMPTY)
				.create(LootContextParamSets.CHEST);
		int lootMultiplier=Math.max(1,playersCount);
		List<ItemStack> allLootItems=new ArrayList<>();
		List<ItemStack> baseLoot=lootTable.getRandomItems(lootParams);
		for(int i=0;i<lootMultiplier;i++){
			for(ItemStack item: baseLoot){
				if(!item.isEmpty()) allLootItems.add(item.copy());
			}
		}
		if(!allLootItems.isEmpty()) startLootAnimation(allLootItems);
	}
	private void cleanupSpawnedEntities(){
		if(level instanceof ServerLevel serverLevel){
			spawnedEntities.forEach(uuid->{
				net.minecraft.world.entity.Entity entity=serverLevel.getEntity(uuid);
				if(entity!=null) entity.discard();
			});
		}
	}
	private void equipOminousMob(net.minecraft.world.entity.Mob mob){
		if(level==null) return;
		if(mob.getAttribute(Attributes.ATTACK_DAMAGE)!=null){
			double currentDamage=Objects.requireNonNull(mob.getAttribute(Attributes.ATTACK_DAMAGE)).getBaseValue();
			Objects.requireNonNull(mob.getAttribute(Attributes.ATTACK_DAMAGE)).setBaseValue(currentDamage*1.25);
		}
		net.minecraft.util.RandomSource random=level.random;
		equipArmorPiece(mob,random,ARMOR_HELMETS,net.minecraft.world.entity.EquipmentSlot.HEAD,0.3f);
		equipArmorPiece(mob,random,ARMOR_CHESTPLATES,net.minecraft.world.entity.EquipmentSlot.CHEST,0.3f);
		equipArmorPiece(mob,random,ARMOR_LEGGINGS,net.minecraft.world.entity.EquipmentSlot.LEGS,0.3f);
		equipArmorPiece(mob,random,ARMOR_BOOTS,net.minecraft.world.entity.EquipmentSlot.FEET,0.3f);
		equipArmorPiece(mob,random,WEAPONS,net.minecraft.world.entity.EquipmentSlot.MAINHAND,0.5f);
		addRandomEnchantments(mob);
	}
	private void equipArmorPiece(net.minecraft.world.entity.Mob mob,net.minecraft.util.RandomSource random,String[] items,net.minecraft.world.entity.EquipmentSlot slot,float chance){
		if(random.nextFloat()<chance){
			net.minecraft.world.item.Item item=ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(items[random.nextInt(items.length)]));
			if(item!=null){
				ItemStack stack=new ItemStack(item);
				mob.setItemSlot(slot,stack);
				mob.setDropChance(slot,0.0f);
			}
		}
	}
	private void addRandomEnchantments(net.minecraft.world.entity.Mob mob){
		if(level==null) return;
		net.minecraft.util.RandomSource random=level.random;
		if(random.nextFloat()>=0.4f) return;
		net.minecraft.world.entity.EquipmentSlot[] slots={
				net.minecraft.world.entity.EquipmentSlot.HEAD,net.minecraft.world.entity.EquipmentSlot.CHEST,
				net.minecraft.world.entity.EquipmentSlot.LEGS,net.minecraft.world.entity.EquipmentSlot.FEET,
				net.minecraft.world.entity.EquipmentSlot.MAINHAND
		};
		for(net.minecraft.world.entity.EquipmentSlot slot: slots){
			ItemStack item=mob.getItemBySlot(slot);
			if(!item.isEmpty()&&random.nextFloat()<0.3f){
				if(slot==net.minecraft.world.entity.EquipmentSlot.MAINHAND){
					if(random.nextBoolean())
						item.enchant(net.minecraft.world.item.enchantment.Enchantments.SHARPNESS,random.nextInt(3)+1);
					if(random.nextBoolean())
						item.enchant(net.minecraft.world.item.enchantment.Enchantments.KNOCKBACK,random.nextInt(2)+1);
				}else{
					if(random.nextBoolean())
						item.enchant(net.minecraft.world.item.enchantment.Enchantments.ALL_DAMAGE_PROTECTION,random.nextInt(3)+1);
					if(random.nextBoolean())
						item.enchant(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING,random.nextInt(2)+1);
				}
				mob.setItemSlot(slot,item);
			}
		}
	}
	private void spawnSpawnParticles(BlockPos center,boolean isOminous){
		if(level==null) return;
		ParticleOptions particle=isOminous?ParticleTypes.SOUL_FIRE_FLAME:ParticleTypes.FLAME;
		for(int i=0;i<10;i++){
			double x=center.getX()+0.5+(level.random.nextDouble()-0.5)*4;
			double y=center.getY()+0.5+(level.random.nextDouble()-0.5)*4;
			double z=center.getZ()+0.5+(level.random.nextDouble()-0.5)*4;
			double dist=Math.sqrt(Math.pow(x-center.getX()-0.5,2)+Math.pow(y-center.getY()-0.5,2)+Math.pow(z-center.getZ()-0.5,2));
			if(dist<=2.0)
				level.addParticle(particle,x,y,z,(level.random.nextDouble()-0.5)*0.05,level.random.nextDouble()*0.1,(level.random.nextDouble()-0.5)*0.05);
		}
	}
	private void spawnRandomOminousEffect(){
		if(level==null||OMNIOUS_EFFECTS.isEmpty()) return;
		MobEffect selectedEffect=OMNIOUS_EFFECTS.get(level.random.nextInt(OMNIOUS_EFFECTS.size()));
		BlockPos effectPos=findEffectPosition();
		if(effectPos==null) return;
		AreaEffectCloud cloud=new AreaEffectCloud(EntityType.AREA_EFFECT_CLOUD,level);
		cloud.setPos(effectPos.getX()+0.5,effectPos.getY()+0.5,effectPos.getZ()+0.5);
		cloud.setRadius(3.0f);
		cloud.setDuration(200);
		cloud.addEffect(new MobEffectInstance(selectedEffect,200,0));
		cloud.setParticle(ParticleTypes.ENTITY_EFFECT);
		level.addFreshEntity(cloud);
	}
	private BlockPos findEffectPosition(){
		if(level==null) return getBlockPos();
		double radius=10.0;
		for(int attempts=0;attempts<20;attempts++){
			double dx=(level.random.nextDouble()-0.5)*2*radius;
			double dz=(level.random.nextDouble()-0.5)*2*radius;
			if(Math.sqrt(dx*dx+dz*dz)>radius) continue;
			int x=getBlockPos().getX()+(int)dx;
			int z=getBlockPos().getZ()+(int)dz;
			int y=getBlockPos().getY();
			for(int yOffset=-2;yOffset<=3;yOffset++){
				BlockPos pos=new BlockPos(x,y+yOffset,z);
				boolean isSolid=level.getBlockState(pos.below()).isSolid();
				if(level.getBlockState(pos).isAir()&&level.getBlockState(pos.above()).isAir()&&isSolid) return pos;
			}
		}
		return null;
	}

	// ====== NBT ======
	@Override
	public void load(@NotNull CompoundTag tag){
		super.load(tag);
		// Normal fields
		this.cooldownTime=tag.getInt("CooldownTime");
		this.isOminous=tag.getBoolean("Ominous");
		this.tickCount=tag.getInt("TickCount");
		this.shouldResetOminousOnCooldownEnd=tag.getBoolean("ShouldResetOminousOnCooldownEnd");
		this.mobsPerWave=tag.contains("MobsPerWave")?tag.getInt("MobsPerWave"):this.mobsPerWave;
		if(this.mobsPerWave<=0) this.mobsPerWave=NTrialsModCommonConfig.spawnerDefaultBaseMobsPerWave;
		if(tag.contains("TotalMobs")) this.totalMobs=tag.getInt("TotalMobs");
		else if(tag.contains("MaxWaves")){
			int oldMaxWaves=tag.getInt("MaxWaves");
			if(oldMaxWaves<=0) oldMaxWaves=5;
			this.totalMobs=oldMaxWaves*this.mobsPerWave;
		}
		if(this.totalMobs<=0) this.totalMobs=NTrialsModCommonConfig.spawnerDefaultBaseTotalMobs;
		this.remainingMobs=tag.contains("RemainingMobs")?tag.getInt("RemainingMobs"):this.totalMobs;
		this.currentWave=tag.contains("CurrentWave")?tag.getInt("CurrentWave"):0;
		this.trialActive=tag.getBoolean("TrialActive");
		this.startTrialTimer=tag.contains("StartTrialTimer")?tag.getInt("StartTrialTimer"):-1;
		if(tag.contains("NormalLootTable")) this.normalLootTable=tag.getString("NormalLootTable");
		if(tag.contains("OminousLootTable")) this.ominousLootTable=tag.getString("OminousLootTable");
		this.isLootAnimating=tag.getBoolean("IsLootAnimating");
		this.lootAnimationTick=tag.getInt("LootAnimationTick");
		this.currentLootDropIndex=tag.getInt("CurrentLootDropIndex");
		if(tag.contains("PendingLootItems")){
			this.pendingLootItems.clear();
			ListTag lootItemsTag=tag.getList("PendingLootItems",10);
			for(int i=0;i<lootItemsTag.size();i++) this.pendingLootItems.add(ItemStack.of(lootItemsTag.getCompound(i)));
		}
		if(tag.contains("SpawnEntity")){
			try{
				ResourceLocation entityLocation=ResourceLocation.parse(tag.getString("SpawnEntity"));
				this.spawnEntity=ForgeRegistries.ENTITY_TYPES.getValue(entityLocation);
			}catch(Exception ignored){
				this.spawnEntity=null;
			}
		}else this.spawnEntity=null;
		if(tag.contains("SpawnedEntities")){
			this.spawnedEntities.clear();
			CompoundTag spawnedTag=tag.getCompound("SpawnedEntities");
			for(String key: spawnedTag.getAllKeys()){
				try{
					this.spawnedEntities.add(UUID.fromString(key));
				}catch(IllegalArgumentException ignored){
				}
			}
		}
		if(level!=null&&!level.isClientSide()&&this.spawnEntity!=null) syncToClients();
		if(tag.contains("VaultTag")) this.vaultTag=tag.getString("VaultTag");
		this.redstoneEnabled=!tag.contains("RedstoneEnabled")||tag.getBoolean("RedstoneEnabled");
		// Boss fields
		if(tag.contains("key_tag")) this.keyTag=tag.getString("key_tag");
		this.isBossActivated=tag.getBoolean("IsActivated");
		this.isBossKeyActivated=tag.getBoolean("IsKeyActivated");
		this.bossSpawnTimer=tag.getInt("SpawnTimer");
		this.bossHP=tag.getInt("BossHP");
		this.completeTrialTimer=tag.getInt("CompleteTrialTimer");
		this.cooldownTimer=tag.getInt("CooldownTimer");
		if(tag.hasUUID("SpawnedBossUUID")) this.spawnedBossUUID=tag.getUUID("SpawnedBossUUID");
		if(tag.contains("BossMobType")){
			try{
				String mobId=tag.getString("BossMobType");
				EntityType<?> type=ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.parse(mobId));
				if(type!=null) this.bossMobType=type;
			}catch(Exception ignored){
			}
		}
		if(tag.contains("BossLootTable")) this.bossLootTable=tag.getString("BossLootTable");
		playersWhoReceivedReward.clear();
		ListTag playersRewardTag=tag.getList("PlayersWhoReceivedReward",8);
		for(int i=0;i<playersRewardTag.size();i++){
			try{
				playersWhoReceivedReward.add(UUID.fromString(playersRewardTag.getString(i)));
			}catch(IllegalArgumentException ignored){
			}
		}
		participatingPlayers.clear();
		ListTag participatingPlayersTag=tag.getList("ParticipatingPlayers",8);
		for(int i=0;i<participatingPlayersTag.size();i++){
			try{
				participatingPlayers.add(UUID.fromString(participatingPlayersTag.getString(i)));
			}catch(IllegalArgumentException ignored){
			}
		}
	}
	@Override
	protected void saveAdditional(@NotNull CompoundTag tag){
		super.saveAdditional(tag);
		// Normal fields
		tag.putInt("CooldownTime",this.cooldownTime);
		tag.putBoolean("Ominous",this.isOminous);
		tag.putInt("TickCount",this.tickCount);
		tag.putBoolean("ShouldResetOminousOnCooldownEnd",this.shouldResetOminousOnCooldownEnd);
		tag.putInt("MobsPerWave",this.mobsPerWave);
		tag.putInt("TotalMobs",this.totalMobs);
		tag.putInt("RemainingMobs",this.remainingMobs);
		tag.putInt("CurrentWave",this.currentWave);
		tag.putBoolean("TrialActive",this.trialActive);
		tag.putInt("StartTrialTimer",this.startTrialTimer);
		tag.putString("NormalLootTable",this.normalLootTable);
		tag.putString("OminousLootTable",this.ominousLootTable);
		tag.putBoolean("IsLootAnimating",this.isLootAnimating);
		tag.putInt("LootAnimationTick",this.lootAnimationTick);
		tag.putInt("CurrentLootDropIndex",this.currentLootDropIndex);
		ListTag lootItemsTag=new ListTag();
		for(ItemStack item: this.pendingLootItems){
			CompoundTag itemTag=new CompoundTag();
			item.save(itemTag);
			lootItemsTag.add(itemTag);
		}
		tag.put("PendingLootItems",lootItemsTag);
		if(this.spawnEntity!=null){
			ResourceLocation entityLocation=ForgeRegistries.ENTITY_TYPES.getKey(this.spawnEntity);
			if(entityLocation!=null) tag.putString("SpawnEntity",entityLocation.toString());
		}
		CompoundTag spawnedTag=new CompoundTag();
		for(UUID uuid: this.spawnedEntities) spawnedTag.putBoolean(uuid.toString(),true);
		tag.put("SpawnedEntities",spawnedTag);
		tag.putString("VaultTag",vaultTag);
		tag.putBoolean("RedstoneEnabled",this.redstoneEnabled);
		// Boss fields
		tag.putString("key_tag",keyTag);
		tag.putBoolean("IsActivated",isBossActivated);
		tag.putBoolean("IsKeyActivated",isBossKeyActivated);
		tag.putInt("SpawnTimer",bossSpawnTimer);
		tag.putInt("BossHP",bossHP);
		tag.putInt("CompleteTrialTimer",completeTrialTimer);
		tag.putInt("CooldownTimer",cooldownTimer);
		if(spawnedBossUUID!=null) tag.putUUID("SpawnedBossUUID",spawnedBossUUID);
		if(bossMobType!=null) tag.putString("BossMobType",EntityType.getKey(bossMobType).toString());
		tag.putString("BossLootTable",bossLootTable==null?"":bossLootTable);
		ListTag participatingPlayersTag=new ListTag();
		for(UUID uuid: participatingPlayers) participatingPlayersTag.add(StringTag.valueOf(uuid.toString()));
		tag.put("ParticipatingPlayers",participatingPlayersTag);
		ListTag playersRewardTag=new ListTag();
		for(UUID uuid: playersWhoReceivedReward) playersRewardTag.add(StringTag.valueOf(uuid.toString()));
		tag.put("PlayersWhoReceivedReward",playersRewardTag);
	}

	// ====== GETTERS/SETTERS ======
	// Normal
	public void setCooldownTime(int cooldownTime){
		this.cooldownTime=cooldownTime;
		setChanged();
	}
	public int getCooldownTime(){
		return this.cooldownTime;
	}
	public boolean isOminous(){
		return isOminous;
	}
	public void setOminous(boolean ominous){
		this.isOminous=ominous;
		setChanged();
	}
	public int getClientTickCount(){
		return clientTickCount;
	}
	public void setRedstoneEnabled(boolean enabled){
		this.redstoneEnabled=enabled;
		setChanged();
	}
	@Nullable
	public EntityType<?> getSpawnEntity(){
		return spawnEntity;
	}
	public void setSpawnEntity(@Nullable EntityType<?> spawnEntity){
		this.spawnEntity=spawnEntity;
		this.hasBeenSynced=false;
		setChanged();
		syncToClients();
		if(level!=null&&!level.isClientSide()){
			level.sendBlockUpdated(getBlockPos(),getBlockState(),getBlockState(),3);
		}
	}
	public void setNormalLootTable(String lootTable){
		if(lootTable!=null) this.normalLootTable=lootTable;
		setChanged();
		if(level!=null&&!level.isClientSide()) level.sendBlockUpdated(getBlockPos(),getBlockState(),getBlockState(),3);
	}
	public void setOminousLootTable(String lootTable){
		if(lootTable!=null) this.ominousLootTable=lootTable;
		setChanged();
		if(level!=null&&!level.isClientSide()) level.sendBlockUpdated(getBlockPos(),getBlockState(),getBlockState(),3);
	}
	public void setVaultTag(String tag){
		this.vaultTag=tag==null?"":tag;
		setChanged();
		if(level!=null&&!level.isClientSide()) level.sendBlockUpdated(getBlockPos(),getBlockState(),getBlockState(),3);
	}
	public boolean hasSpawnEntity(){
		return this.spawnEntity!=null;
	}
	// Boss
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
	public boolean isBossActivated(){
		return isBossActivated;
	}
	public boolean isBossKeyActivated(){
		return isBossKeyActivated;
	}

	// ====== SYNC ======
	private void syncToClients(){
		if(level!=null&&!level.isClientSide()&&level instanceof ServerLevel serverLevel){
			NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(()->serverLevel.getChunkAt(getBlockPos())),new TrialSpawnerSyncPacket(getBlockPos(),this.spawnEntity));
		}
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
}
