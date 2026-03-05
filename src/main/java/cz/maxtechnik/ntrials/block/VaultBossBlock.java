package cz.maxtechnik.ntrials.block;

import cz.maxtechnik.ntrials.NTrialsMod;
import cz.maxtechnik.ntrials.NTrialsModCommonConfig;
import cz.maxtechnik.ntrials.block.entity.VaultBlockEntity;
import cz.maxtechnik.ntrials.init.other.NTrialsModBlockEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import cz.maxtechnik.ntrials.init.basic.NTrialsModItems;
import cz.maxtechnik.ntrials.init.basic.NTrialsModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;

import java.util.List;
import java.util.Objects;
@SuppressWarnings("deprecation")
public class VaultBossBlock extends BaseEntityBlock{
	private static final String DEFAULT_LOOT_NORMAL="ntrials:chests/reward_boss";
	public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
	public static final EnumProperty<VaultBlock.VaultState> STATE=EnumProperty.create("vault_state",VaultBlock.VaultState.class);
	public VaultBossBlock(){
		super(Properties.of().sound(SoundType.METAL).strength(20F,999999999F).noOcclusion().mapColor(MapColor.COLOR_BLACK).isRedstoneConductor((bs,br,bp)->false).noLootTable().pushReaction(PushReaction.BLOCK));
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(STATE,VaultBlock.VaultState.INACTIVE));
	}
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
		builder.add(STATE,FACING);
	}
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context){
		return this.defaultBlockState().setValue(FACING,context.getHorizontalDirection().getOpposite());
	}
	public @NotNull BlockState rotate(BlockState state,Rotation rot){
		var rotated=rot.rotate(state.getValue(FACING));
		return state.setValue(FACING,rotated);
	}
	@Override
	public @NotNull BlockState mirror(BlockState state,Mirror mirrorIn){
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
	}
	@Override
	public int getLightBlock(@NotNull BlockState state,@NotNull BlockGetter worldIn,@NotNull BlockPos pos){
		return 0;
	}
	@Override
	public int getLightEmission(BlockState state,BlockGetter level,BlockPos pos){
		return state.getValue(STATE)==VaultBlock.VaultState.INACTIVE?6:12;
	}
	@Override
	public @NotNull InteractionResult use(@NotNull BlockState state,Level level,@NotNull BlockPos pos,Player player,@NotNull InteractionHand hand,@NotNull BlockHitResult hit){
		ItemStack heldItem=player.getItemInHand(hand);
		// Zkontroluje, zda má blok block entity a zda hráč již otevřel tento vault
		if(!level.isClientSide){
			BlockEntity blockEntity=level.getBlockEntity(pos);
			if(blockEntity instanceof VaultBlockEntity vaultEntity){
				if(vaultEntity.hasPlayerOpened(player.getUUID())){
					// Hráč již otevřel tento vault
					player.displayClientMessage(Component.literal("You Already Opened This Vault."),true);
					return InteractionResult.FAIL;
				}
			}
		}
		// Boss vault - pouze boss key
		if(heldItem.getItem()==NTrialsModItems.BOSS_TRIAL_KEY.get()&&state.getValue(STATE)==VaultBlock.VaultState.ACTIVE){
			BlockEntity blockEntity=level.getBlockEntity(pos);
			if(blockEntity instanceof VaultBlockEntity vaultEntity){
				String vaultTag=vaultEntity.getVaultTag();
				CompoundTag itemTag=heldItem.getTag();
				String keyTag=itemTag!=null?itemTag.getString("vault_tag"):"";
				if(!((keyTag.isEmpty()&&vaultTag.isEmpty())||keyTag.equals(vaultTag))){
					if(!level.isClientSide())
						level.playSound(null,pos,NTrialsModSounds.BLOCK_VAULT_INSERT_ITEM_FAIL.get(),SoundSource.BLOCKS,1.0f,1.0f);
					return InteractionResult.PASS;
				}
				if(!level.isClientSide){
					// Označí hráče jako toho, kdo otevřel vault
					vaultEntity.addPlayerWhoOpened(player.getUUID());
					String lootPath=vaultEntity.getLootTable().isEmpty()?DEFAULT_LOOT_NORMAL:vaultEntity.getLootTable();
					ResourceLocation lootTableId=ResourceLocation.parse(lootPath);
					LootTable lootTable=Objects.requireNonNull(level.getServer()).getLootData().getLootTable(lootTableId);
					LootParams lootParams=new LootParams.Builder((ServerLevel)level).withParameter(LootContextParams.ORIGIN,Vec3.atCenterOf(pos)).withParameter(LootContextParams.THIS_ENTITY,player).withLuck(player.getLuck()).create(LootContextParamSets.CHEST);
					List<ItemStack> loot=lootTable.getRandomItems(lootParams);
					level.setBlock(pos,state.setValue(STATE,VaultBlock.VaultState.UNLOCKING),Block.UPDATE_ALL);
					vaultEntity.startAnimation(loot);
					NTrialsMod.adv((ServerPlayer)player,ResourceLocation.fromNamespaceAndPath("ntrials","vault_boss"));
				}
				if(!level.isClientSide())
					level.playSound(null,pos,NTrialsModSounds.BLOCK_VAULT_INSERT_ITEM.get(),SoundSource.BLOCKS,1.0f,1.0f);
				if(!player.getAbilities().instabuild) heldItem.shrink(1);
				return InteractionResult.SUCCESS;
			}
		}
		if(!level.isClientSide())
			level.playSound(null,pos,NTrialsModSounds.BLOCK_VAULT_REJECT_REWARDED_PLAYER.get(),SoundSource.BLOCKS,1.0f,1.0f);
		return InteractionResult.PASS;
	}
	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,@NotNull BlockState state,@NotNull BlockEntityType<T> blockEntityType){
		return createTickerHelper(blockEntityType,NTrialsModBlockEntities.VAULT_BLOCK_ENTITY.get(),
				level.isClientSide?VaultBossBlock::clientTick:VaultBossBlock::serverTick);
	}
	// Server tick - zpracovává animaci, částice a hráče
	private static void serverTick(Level level,BlockPos pos,BlockState state,VaultBlockEntity vaultEntity){
		long gameTime=level.getGameTime();
		if(!level.isClientSide&&gameTime%10==0) addSmokeParticles(level,pos);
		if(!level.isClientSide&&state.getValue(STATE)==VaultBlock.VaultState.ACTIVE&&gameTime%20==0)
			addFireParticles(level,pos);
		VaultBlock.VaultState vaultState=state.getValue(STATE);
		if(vaultState==VaultBlock.VaultState.ACTIVE&&!vaultEntity.hasDisplayItems())
			generateDisplayItems(level,pos,vaultEntity);
		if(vaultState!=VaultBlock.VaultState.ACTIVE&&vaultEntity.hasDisplayItems()) vaultEntity.clearDisplayItems();
		if(vaultState==VaultBlock.VaultState.ACTIVE) vaultEntity.tickDisplayItem();
		if(vaultEntity.isAnimating()) handleVaultAnimation(level,pos,state,vaultEntity);
		else if(gameTime%10==0) checkNearbyPlayers(level,pos,vaultEntity,state);
	}
	// Client tick - pouze rotace itemů
	private static void clientTick(Level level,BlockPos pos,BlockState state,VaultBlockEntity vaultEntity){
		if(state.getValue(STATE)==VaultBlock.VaultState.ACTIVE) vaultEntity.tickDisplayItem();
	}
	// Vygeneruje zobrazované itemy z loot table
	private static void generateDisplayItems(Level level,BlockPos pos,VaultBlockEntity vaultEntity){
		if(!(level instanceof ServerLevel serverLevel)) return;
		String lootTableStr=vaultEntity.getLootTable();
		String lootPath=lootTableStr.isEmpty()?DEFAULT_LOOT_NORMAL:lootTableStr;
		ResourceLocation lootTableId;
		LootTable lootTable;
		try{
			lootTableId=ResourceLocation.parse(lootPath);
			lootTable=serverLevel.getServer().getLootData().getLootTable(lootTableId);
		}catch(Exception ex){
			lootTableId=ResourceLocation.parse(DEFAULT_LOOT_NORMAL);
			lootTable=serverLevel.getServer().getLootData().getLootTable(lootTableId);
		}
		LootParams params=new LootParams.Builder(serverLevel).withParameter(LootContextParams.ORIGIN,Vec3.atCenterOf(pos)).withLuck(0.0f).create(LootContextParamSets.CHEST);
		vaultEntity.setDisplayItems(lootTable.getRandomItems(params));
	}
	// Přidá kouřové částice
	private static void addSmokeParticles(Level level,BlockPos pos){
		if(!(level instanceof ServerLevel serverLevel)) return;
		RandomSource random=level.random;
		for(int i=0;i<1+random.nextInt(2);i++){
			double x=pos.getX()+0.3+random.nextDouble()*0.4;
			double y=pos.getY()+0.8+random.nextDouble()*0.3;
			double z=pos.getZ()+0.3+random.nextDouble()*0.4;
			serverLevel.sendParticles(ParticleTypes.SMOKE,x,y,z,1,(random.nextDouble()-0.5)*0.02,random.nextDouble()*0.05+0.02,(random.nextDouble()-0.5)*0.02,0.0);
		}
	}
	// Přidá ohnivé částice
	private static void addFireParticles(Level level,BlockPos pos){
		if(!(level instanceof ServerLevel serverLevel)) return;
		RandomSource random=level.random;
		for(int i=0;i<1+random.nextInt(2);i++){
			double x=pos.getX()+0.3+random.nextDouble()*0.4;
			double y=pos.getY()+0.2+random.nextDouble()*0.3;
			double z=pos.getZ()+0.3+random.nextDouble()*0.4;
			serverLevel.sendParticles(ParticleTypes.FLAME,x,y,z,1,(random.nextDouble()-0.5)*0.02,random.nextDouble()*0.05+0.02,(random.nextDouble()-0.5)*0.02,0.0);
		}
	}
	// Zpracovává animaci otevírání vaultu
	private static void handleVaultAnimation(Level level,BlockPos pos,BlockState state,VaultBlockEntity vaultEntity){
		vaultEntity.tickAnimation();
		int tick=vaultEntity.getAnimationTick();
		VaultBlock.VaultState currentState=state.getValue(STATE);
		if(tick==NTrialsModCommonConfig.vaultUnlockingDuration&&currentState==VaultBlock.VaultState.UNLOCKING){
			if(!level.isClientSide())
				level.playSound(null,pos,NTrialsModSounds.BLOCK_VAULT_OPEN_SHUTTER.get(),SoundSource.BLOCKS,1.0f,1.0f);
			level.setBlock(pos,state.setValue(STATE,VaultBlock.VaultState.EJECTING),Block.UPDATE_ALL);
			return;
		}
		if(currentState==VaultBlock.VaultState.EJECTING&&tick>NTrialsModCommonConfig.vaultUnlockingDuration){
			int dropPhase=(tick-NTrialsModCommonConfig.vaultUnlockingDuration)/NTrialsModCommonConfig.vaultEjectInterval;
			List<ItemStack> loot=vaultEntity.getPendingLoot();
			int currentDropIndex=vaultEntity.getLootDropIndex();
			if(dropPhase>currentDropIndex&&currentDropIndex<loot.size()){
				ItemStack stack=loot.get(currentDropIndex);
				ItemEntity drop=new ItemEntity(level,pos.getX()+0.5,pos.getY()+1.0,pos.getZ()+0.5,stack.copy());
				if(!level.isClientSide())
					level.playSound(null,pos,NTrialsModSounds.BLOCK_VAULT_EJECT_ITEM.get(),SoundSource.BLOCKS,1.0f,1.0f);
				drop.setDeltaMovement(0.0,0.15,0.0);
				drop.setPickUpDelay(10);
				level.addFreshEntity(drop);
				vaultEntity.incrementLootDropIndex();
			}
			if(currentDropIndex>=loot.size()&&tick>=(NTrialsModCommonConfig.vaultUnlockingDuration+loot.size()*NTrialsModCommonConfig.vaultEjectInterval+NTrialsModCommonConfig.vaultCloseDelay)){
				vaultEntity.stopAnimation();
				level.setBlock(pos,state.setValue(STATE,VaultBlock.VaultState.INACTIVE),Block.UPDATE_ALL);
				if(!level.isClientSide())
					level.playSound(null,pos,NTrialsModSounds.BLOCK_VAULT_CLOSE_SHUTTER.get(),SoundSource.BLOCKS,1.0f,1.0f);
			}
		}
	}
	// Kontroluje hráče v okolí a aktivuje/deaktivuje vault
	private static void checkNearbyPlayers(Level level,BlockPos pos,VaultBlockEntity vaultEntity,BlockState state){
		if(vaultEntity.isAnimating()) return;
		double range=4.0;
		net.minecraft.world.phys.AABB searchArea=new net.minecraft.world.phys.AABB(pos.getX()-range,pos.getY()-range,pos.getZ()-range,pos.getX()+range,pos.getY()+range,pos.getZ()+range);
		List<Player> players=level.getEntitiesOfClass(Player.class,searchArea);
		if(players.isEmpty()){
			if(state.getValue(STATE)!=VaultBlock.VaultState.INACTIVE){
				level.setBlock(pos,state.setValue(STATE,VaultBlock.VaultState.INACTIVE),Block.UPDATE_ALL);
				if(!level.isClientSide())
					level.playSound(null,pos,NTrialsModSounds.BLOCK_VAULT_DEACTIVATE.get(),SoundSource.BLOCKS,1.0f,1.0f);
			}
			return;
		}
		for(Player nearbyPlayer: players){
			if(!vaultEntity.hasPlayerOpened(nearbyPlayer.getUUID())){
				if(state.getValue(STATE)!=VaultBlock.VaultState.ACTIVE){
					level.setBlock(pos,state.setValue(STATE,VaultBlock.VaultState.ACTIVE),Block.UPDATE_ALL);
					if(!level.isClientSide())
						level.playSound(null,pos,NTrialsModSounds.BLOCK_VAULT_ACTIVATE.get(),SoundSource.BLOCKS,1.0f,1.0f);
				}
				return;
			}
		}
	}
	@Nullable
	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pos,@NotNull BlockState state){
		return new VaultBlockEntity(pos,state);
	}
	@Override
	public void setPlacedBy(@NotNull Level level,@NotNull BlockPos pos,@NotNull BlockState state,@Nullable LivingEntity placer,@NotNull ItemStack stack){
		super.setPlacedBy(level,pos,state,placer,stack);
		if(!level.isClientSide){
			CompoundTag tag=stack.getTagElement("BlockEntityTag");
			if(tag==null) tag=stack.getTag();
			if(tag!=null){
				BlockEntity blockEntity=level.getBlockEntity(pos);
				if(blockEntity instanceof VaultBlockEntity vaultEntity){
					if(tag.contains("vault_tag")) vaultEntity.setVaultTag(tag.getString("vault_tag"));
					if(tag.contains("loot_table")){
						String loot=tag.getString("loot_table");
						if(!loot.isEmpty()) vaultEntity.setLootTable(loot);
					}
				}
			}
		}
	}
	@Override
	public @NotNull RenderShape getRenderShape(@NotNull BlockState state){
		return RenderShape.MODEL;
	}
}
