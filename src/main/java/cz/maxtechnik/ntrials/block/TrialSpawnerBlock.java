package cz.maxtechnik.ntrials.block;

import cz.maxtechnik.ntrials.block.entity.TrialSpawnerBlockEntity;
import cz.maxtechnik.ntrials.init.other.NTrialsModBlockEntities;
import cz.maxtechnik.ntrials.init.basic.NTrialsModSounds;
import cz.maxtechnik.ntrials.network.NetworkHandler;
import cz.maxtechnik.ntrials.network.TrialSpawnerSyncPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import cz.maxtechnik.ntrials.init.basic.NTrialsModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
@SuppressWarnings("deprecation")
public class TrialSpawnerBlock extends BaseEntityBlock{
	public static final EnumProperty<SpawnerType> TYPE=EnumProperty.create("type",SpawnerType.class);
	public static final BooleanProperty OMINOUS=BooleanProperty.create("ominous");
	public static final BooleanProperty POWERED=BooleanProperty.create("powered");
	public static final EnumProperty<TrialSpawnerState> STATE=EnumProperty.create("trial_spawner_state",TrialSpawnerState.class);
	public TrialSpawnerBlock(){
		super(Properties.of().sound(SoundType.METAL).strength(20F,999999999F).noOcclusion().mapColor(MapColor.COLOR_BLACK).isRedstoneConductor((bs,br,bp)->false).noLootTable().pushReaction(PushReaction.BLOCK));
		this.registerDefaultState(this.stateDefinition.any().setValue(TYPE,SpawnerType.NORMAL).setValue(OMINOUS,false).setValue(POWERED,false).setValue(STATE,TrialSpawnerState.INACTIVE));
	}
	@Override
	public @NotNull InteractionResult use(@NotNull BlockState state,@NotNull Level level,@NotNull BlockPos pos,@NotNull Player player,@NotNull InteractionHand hand,@NotNull BlockHitResult hit){
		ItemStack itemStack=player.getItemInHand(hand);
		BlockEntity blockEntity=level.getBlockEntity(pos);
		if(!(blockEntity instanceof TrialSpawnerBlockEntity trialSpawner)) return InteractionResult.PASS;
		SpawnerType type=state.getValue(TYPE);
		// Spawn egg logic (shared for both types)
		if(itemStack.getItem() instanceof SpawnEggItem spawnEggItem){
			if(!level.isClientSide){
				if(!player.getAbilities().instabuild){
					player.displayClientMessage(Component.literal("You must be in Creative to change the spawner with a spawn egg."),true);
					level.playSound(null,pos,NTrialsModSounds.BLOCK_VAULT_REJECT_REWARDED_PLAYER.get(),SoundSource.BLOCKS,1.0f,1.0f);
					return InteractionResult.FAIL;
				}
				EntityType<?> entityType=spawnEggItem.getType(itemStack.getTag());
				if(type==SpawnerType.BOSS){
					trialSpawner.setBossMobType(entityType);
					trialSpawner.setChanged();
					player.displayClientMessage(Component.literal("Boss mob set from spawn egg (Creative)."),true);
				}else{
					trialSpawner.setSpawnEntity(entityType);
					if(level instanceof ServerLevel serverLevel){
						TrialSpawnerSyncPacket packet=new TrialSpawnerSyncPacket(pos,entityType);
						NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(()->
								serverLevel.getChunkAt(pos)),packet);
					}
				}
				return InteractionResult.SUCCESS;
			}
			return InteractionResult.sidedSuccess(true);
		}
		if(type==SpawnerType.NORMAL) return useNormal(state,level,pos,player,hand,itemStack,trialSpawner);
		else return useBoss(state,level,pos,player,hand,itemStack,trialSpawner);
	}
	private InteractionResult useNormal(BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,ItemStack itemStack,TrialSpawnerBlockEntity trialSpawner){
		// Key logic - set tag
		if(itemStack.getItem()==NTrialsModItems.TRIAL_KEY.get()||itemStack.getItem()==NTrialsModItems.OMINOUS_TRIAL_KEY.get()||itemStack.getItem()==NTrialsModItems.BOSS_TRIAL_KEY.get()){
			if(!level.isClientSide){
				CompoundTag itemTag=itemStack.getTag();
				String keyTag=itemTag!=null?itemTag.getString("vault_tag"):"";
				trialSpawner.setVaultTag(keyTag);
				trialSpawner.setChanged();
				player.displayClientMessage(Component.literal("Spawner tag set."),true);
				if(!player.isCreative()) itemStack.shrink(1);
			}
			return InteractionResult.SUCCESS;
		}
		// Cooldown display
		if(trialSpawner.getCooldownTime()>0){
			if(!level.isClientSide){
				int totalSeconds=trialSpawner.getCooldownTime()/20;
				long minutes=(totalSeconds%3600)/60;
				long seconds=totalSeconds%60;
				String timeString=String.format("%02d:%02d",minutes,seconds);
				player.displayClientMessage(Component.literal("Cooldown "+timeString),true);
			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}
	private InteractionResult useBoss(BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,ItemStack itemStack,TrialSpawnerBlockEntity trialSpawner){
		if(!level.isClientSide()){
			boolean isHoldingKey=itemStack.getItem()==NTrialsModItems.OMINOUS_TRIAL_KEY.get()||itemStack.getItem()==NTrialsModItems.BOSS_TRIAL_KEY.get();
			// Support for setting boss config via NBT on item use
			CompoundTag tag=itemStack.getTagElement("BlockEntityTag");
			if(tag==null) tag=itemStack.getTag();
			if(tag!=null){
				if(tag.contains("BossMobType")){
					try{
						String mobId=tag.getString("BossMobType");
						EntityType<?> type=net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.parse(mobId));
						if(type!=null) trialSpawner.setBossMobType(type);
					}catch(Exception ignored){
					}
				}
				if(tag.contains("key_tag")) trialSpawner.setKeyTag(tag.getString("key_tag"));
				if(tag.contains("BossLootTable")) trialSpawner.setBossLootTable(tag.getString("BossLootTable"));
			}
			// Cooldown check
			if(trialSpawner.getCooldownTimer()>0){
				if(trialSpawner.hasPlayerReceivedReward(player.getUUID())){
					player.displayClientMessage(Component.literal("Cooldown: ("+formatTime(trialSpawner.getCooldownTimer())+"). Boss already defeated. Find another Trial Chamber."),true);
				}else{
					player.displayClientMessage(Component.literal("Cooldown: "+formatTime(trialSpawner.getCooldownTimer())),true);
				}
				level.playSound(null,pos,NTrialsModSounds.BLOCK_VAULT_REJECT_REWARDED_PLAYER.get(),SoundSource.BLOCKS,1.0f,1.0f);
				return InteractionResult.FAIL;
			}
			// Reward check
			if(trialSpawner.hasPlayerReceivedReward(player.getUUID())){
				player.displayClientMessage(Component.literal("Boss already defeated. Find another Trial Chamber."),true);
				level.playSound(null,pos,NTrialsModSounds.BLOCK_VAULT_REJECT_REWARDED_PLAYER.get(),SoundSource.BLOCKS,1.0f,1.0f);
				return InteractionResult.FAIL;
			}
			// Key interaction
			if(isHoldingKey){
				boolean acceptKey=isAcceptKey(trialSpawner,itemStack);
				if(!acceptKey){
					player.displayClientMessage(Component.literal("This spawner requires a different key tag."),true);
					level.playSound(null,pos,NTrialsModSounds.BLOCK_VAULT_REJECT_REWARDED_PLAYER.get(),SoundSource.BLOCKS,1.0f,1.0f);
					return InteractionResult.FAIL;
				}
				if(trialSpawner.isBossActivated()&&!trialSpawner.isBossKeyActivated()){
					trialSpawner.activateWithKey();
					if(!player.getAbilities().instabuild) itemStack.shrink(1);
					return InteractionResult.SUCCESS;
				}
			}else{
				player.displayClientMessage(Component.literal("Need Ominous Key"),true);
				level.playSound(null,pos,NTrialsModSounds.BLOCK_VAULT_REJECT_REWARDED_PLAYER.get(),SoundSource.BLOCKS,1.0f,1.0f);
			}
			return InteractionResult.FAIL;
		}
		return InteractionResult.FAIL;
	}
	private boolean isAcceptKey(TrialSpawnerBlockEntity trialSpawner,ItemStack itemStack){
		CompoundTag keyNbt=itemStack.getTag();
		String keyVaultTag="";
		if(keyNbt!=null&&keyNbt.contains("vault_tag")) keyVaultTag=keyNbt.getString("vault_tag");
		String required=trialSpawner.getKeyTag();
		if(required==null||required.isEmpty()){
			return keyVaultTag.isEmpty();
		}else{
			return !keyVaultTag.isEmpty()&&keyVaultTag.equals(required);
		}
	}
	private String formatTime(int ticks){
		int seconds=ticks/20;
		int minutes=seconds/60;
		int remainingSeconds=seconds%60;
		if(minutes>0){
			return String.format("%d min %d s",minutes,remainingSeconds);
		}else{
			return String.format("%d s",remainingSeconds);
		}
	}
	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pos,@NotNull BlockState state){
		return new TrialSpawnerBlockEntity(pos,state);
	}
	@Override
	public void setPlacedBy(@NotNull Level level,@NotNull BlockPos pos,@NotNull BlockState state,@Nullable LivingEntity placer,@NotNull ItemStack stack){
		super.setPlacedBy(level,pos,state,placer,stack);
		if(!level.isClientSide){
			CompoundTag tag=stack.getTagElement("BlockEntityTag");
			if(tag==null) tag=stack.getTag();
			if(tag!=null){
				BlockEntity blockEntity=level.getBlockEntity(pos);
				if(blockEntity instanceof TrialSpawnerBlockEntity spawnerEntity){
					// Normal spawner NBT
					String entityName=null;
					if(tag.contains("SpawnEntity")) entityName=tag.getString("SpawnEntity");
					else if(tag.contains("spawn_entity")) entityName=tag.getString("spawn_entity");
					if(entityName!=null&&!entityName.isEmpty()){
						if(tag.contains("vault_tag")) spawnerEntity.setVaultTag(tag.getString("vault_tag"));
						try{
							ResourceLocation rl=ResourceLocation.parse(entityName);
							EntityType<?> type=net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getValue(rl);
							if(type!=null) spawnerEntity.setSpawnEntity(type);
						}catch(Exception ignored){
						}
					}
					if(tag.contains("NormalLootTable"))
						spawnerEntity.setNormalLootTable(tag.getString("NormalLootTable"));
					if(tag.contains("normal_loot_table"))
						spawnerEntity.setNormalLootTable(tag.getString("normal_loot_table"));
					if(tag.contains("OminousLootTable"))
						spawnerEntity.setOminousLootTable(tag.getString("OminousLootTable"));
					if(tag.contains("ominous_loot_table"))
						spawnerEntity.setOminousLootTable(tag.getString("ominous_loot_table"));
					// Boss spawner NBT
					if(tag.contains("BossMobType")){
						try{
							String mobId=tag.getString("BossMobType");
							EntityType<?> type=net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.parse(mobId));
							if(type!=null) spawnerEntity.setBossMobType(type);
						}catch(Exception ignored){
						}
					}
					if(tag.contains("key_tag")) spawnerEntity.setKeyTag(tag.getString("key_tag"));
					if(tag.contains("BossLootTable")) spawnerEntity.setBossLootTable(tag.getString("BossLootTable"));
				}
			}
		}
		// Initialize redstone state when block is placed (only for NORMAL type)
		if(!level.isClientSide&&state.getValue(TYPE)==SpawnerType.NORMAL){
			BlockState blockState=level.getBlockState(pos);
			boolean isPowered=level.hasNeighborSignal(pos);
			if(isPowered!=blockState.getValue(POWERED)){
				level.setBlock(pos,blockState.setValue(POWERED,isPowered),3);
			}
			BlockEntity blockEntity=level.getBlockEntity(pos);
			if(blockEntity instanceof TrialSpawnerBlockEntity spawnerEntity){
				spawnerEntity.setRedstoneEnabled(!isPowered);
			}
		}
	}
	@Override
	public @NotNull RenderShape getRenderShape(@NotNull BlockState state){
		return RenderShape.MODEL;
	}
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level,@NotNull BlockState state,@NotNull BlockEntityType<T> blockEntityType){
		return createTickerHelper(blockEntityType,NTrialsModBlockEntities.TRIAL_SPAWNER_BLOCK_ENTITY.get(),
				level.isClientSide()
						?(level1,pos,state1,blockEntity)->blockEntity.clientTick()
						:(level1,pos,state1,blockEntity)->blockEntity.tick());
	}
	public enum SpawnerType implements net.minecraft.util.StringRepresentable{
		NORMAL("normal"),
		BOSS("boss");
		private final String name;
		SpawnerType(String name){
			this.name=name;
		}
		@Override
		public @NotNull String getSerializedName(){
			return this.name;
		}
	}
	public enum TrialSpawnerState implements net.minecraft.util.StringRepresentable{
		INACTIVE("inactive"),
		COOLDOWN("cooldown"),
		EJECTING_REWARD("ejecting_reward"),
		ACTIVE("active"),
		WAITING_FOR_PLAYERS("waiting_for_players");
		private final String name;
		TrialSpawnerState(String name){
			this.name=name;
		}
		@Override
		public @NotNull String getSerializedName(){
			return this.name;
		}
	}
	@Override
	public void neighborChanged(@NotNull BlockState state,Level level,@NotNull BlockPos pos,@NotNull Block blockIn,@NotNull BlockPos fromPos,boolean isMoving){
		if(!level.isClientSide&&state.getValue(TYPE)==SpawnerType.NORMAL){
			boolean isPowered=level.hasNeighborSignal(pos);
			if(isPowered!=state.getValue(POWERED)){
				level.setBlock(pos,state.setValue(POWERED,isPowered),3);
				BlockEntity blockEntity=level.getBlockEntity(pos);
				if(blockEntity instanceof TrialSpawnerBlockEntity spawnerEntity){
					spawnerEntity.setRedstoneEnabled(!isPowered);
				}
			}
		}
	}
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
		builder.add(TYPE,OMINOUS,POWERED,STATE);
	}
	@Override
	public int getLightBlock(@NotNull BlockState state,@NotNull BlockGetter worldIn,@NotNull BlockPos pos){
		return 0;
	}
}
