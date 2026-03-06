package cz.maxtechnik.ntrials.event;

import cz.maxtechnik.ntrials.block.TrialSpawnerBlock;
import cz.maxtechnik.ntrials.block.TrialVaultBlock;
import cz.maxtechnik.ntrials.block.entity.TrialSpawnerBlockEntity;
import cz.maxtechnik.ntrials.block.entity.TrialVaultBlockEntity;
import cz.maxtechnik.ntrials.init.basic.NTrialsModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class TrialConvertor extends BaseEntityBlock{
	public enum ConvertorType{SPAWNER,VAULT}

	// Spawner properties
	public static final BooleanProperty OMINOUS=BooleanProperty.create("ominous");
	public static final EnumProperty<TrialSpawnerBlock.TrialSpawnerState> SPAWNER_STATE=EnumProperty.create("trial_spawner_state",TrialSpawnerBlock.TrialSpawnerState.class);

	// Vault properties
	public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
	public static final EnumProperty<TrialVaultBlock.VaultState> VAULT_STATE=EnumProperty.create("vault_state",TrialVaultBlock.VaultState.class);

	private static ConvertorType pendingType;
	private final ConvertorType type;

	public static TrialConvertor spawner(){
		pendingType=ConvertorType.SPAWNER;
		return new TrialConvertor();
	}

	public static TrialConvertor vault(){
		pendingType=ConvertorType.VAULT;
		return new TrialConvertor();
	}

	private TrialConvertor(){
		super(Properties.of()
				.sound(SoundType.METAL)
				.strength(20F,999999999F)
				.mapColor(MapColor.COLOR_BLACK)
				.noOcclusion()
				.noLootTable()
				.pushReaction(PushReaction.BLOCK));
		this.type=pendingType;
		if(type==ConvertorType.SPAWNER){
			this.registerDefaultState(this.stateDefinition.any()
					.setValue(OMINOUS,false)
					.setValue(SPAWNER_STATE,TrialSpawnerBlock.TrialSpawnerState.INACTIVE));
		}else{
			this.registerDefaultState(this.stateDefinition.any()
					.setValue(FACING,Direction.NORTH)
					.setValue(VAULT_STATE,TrialVaultBlock.VaultState.INACTIVE));
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block,BlockState> builder){
		if(pendingType==ConvertorType.SPAWNER){
			builder.add(OMINOUS,SPAWNER_STATE);
		}else{
			builder.add(FACING,VAULT_STATE);
		}
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pos,@NotNull BlockState state){
		if(type==ConvertorType.SPAWNER) return new TrialSpawnerBlockEntity(pos,state);
		return new TrialVaultBlockEntity(pos,state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> blockEntityType){
		return (lvl,pos,st,be)->{
			if(!lvl.isClientSide()&&lvl instanceof ServerLevel serverLevel){
				if(!serverLevel.getBlockTicks().hasScheduledTick(pos,st.getBlock())){
					serverLevel.scheduleTick(pos,st.getBlock(),10);
				}
			}
		};
	}

	@Override
	public void tick(@NotNull BlockState state,@NotNull ServerLevel level,@NotNull BlockPos pos,@NotNull RandomSource random){
		CompoundTag memory=backupNBT(level,pos);

		if(type==ConvertorType.SPAWNER){
			convertSpawner(state,level,pos,memory);
		}else{
			convertVault(state,level,pos,memory);
		}
	}

	private CompoundTag backupNBT(ServerLevel level,BlockPos pos){
		CompoundTag memory=new CompoundTag();
		BlockEntity be=level.getBlockEntity(pos);
		if(be!=null){
			memory=be.saveWithFullMetadata();
			memory.remove("id");
			memory.remove("x");
			memory.remove("y");
			memory.remove("z");
		}
		return memory;
	}

	private void convertSpawner(BlockState state,ServerLevel level,BlockPos pos,CompoundTag memory){
		boolean ominous=state.hasProperty(OMINOUS)&&state.getValue(OMINOUS);

		BlockState newState=NTrialsModBlocks.TRIAL_SPAWNER.get().defaultBlockState()
				.setValue(TrialSpawnerBlock.TYPE,TrialSpawnerBlock.SpawnerType.BOSS)
				.setValue(TrialSpawnerBlock.OMINOUS,ominous)
				.setValue(TrialSpawnerBlock.POWERED,false)
				.setValue(TrialSpawnerBlock.STATE,TrialSpawnerBlock.TrialSpawnerState.INACTIVE);

		level.setBlock(pos,newState,Block.UPDATE_ALL);

		BlockEntity newBe=level.getBlockEntity(pos);
		if(newBe==null) return;
		if(newBe instanceof TrialSpawnerBlockEntity trialBe){
			if(!memory.isEmpty()) trialBe.load(memory);
			trialBe.setChanged();
			level.sendBlockUpdated(pos,newState,newState,Block.UPDATE_ALL);
		}
	}

	private void convertVault(BlockState state,ServerLevel level,BlockPos pos,CompoundTag memory){
		Direction facing=state.hasProperty(FACING)?state.getValue(FACING):Direction.NORTH;

		BlockState newState=NTrialsModBlocks.VAULT.get().defaultBlockState()
				.setValue(TrialVaultBlock.TYPE,TrialVaultBlock.VaultType.BOSS)
				.setValue(TrialVaultBlock.FACING,facing)
				.setValue(TrialVaultBlock.STATE,TrialVaultBlock.VaultState.INACTIVE);

		level.setBlock(pos,newState,Block.UPDATE_ALL);

		BlockEntity newBe=level.getBlockEntity(pos);
		if(newBe==null) return;
		if(newBe instanceof TrialVaultBlockEntity trialBe){
			if(!memory.isEmpty()) trialBe.load(memory);
			trialBe.setChanged();
			level.sendBlockUpdated(pos,newState,newState,Block.UPDATE_ALL);
		}
	}

	@Override
	public @NotNull InteractionResult use(@NotNull BlockState state, Level level, @NotNull BlockPos pos,
                                          @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit){
		if(!level.isClientSide()&&level instanceof ServerLevel sl){
			this.tick(state,sl,pos,sl.random);
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	@Override
	public @NotNull RenderShape getRenderShape(@NotNull BlockState state){
		return RenderShape.MODEL;
	}
}
