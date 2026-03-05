package cz.maxtechnik.ntrials.event;

import cz.maxtechnik.ntrials.block.TrialSpawnerBlock;
import cz.maxtechnik.ntrials.block.entity.TrialSpawnerBlockEntity;
import cz.maxtechnik.ntrials.init.basic.NTrialsModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Legacy converter for old "trial_spawner_boss" blocks.
 * When placed by jigsaw structures or loaded from old worlds, this block
 * immediately converts itself to the unified TrialSpawnerBlock with TYPE=BOSS.
 */
@SuppressWarnings("deprecation")
public class LegacySpawnerConverterBlock extends BaseEntityBlock{
	public static final BooleanProperty OMINOUS=BooleanProperty.create("ominous");
	public static final EnumProperty<TrialSpawnerBlock.TrialSpawnerState> STATE=EnumProperty.create("trial_spawner_state",TrialSpawnerBlock.TrialSpawnerState.class);

	public LegacySpawnerConverterBlock(){
		super(Properties.of()
				.sound(SoundType.METAL)
				.strength(20F,999999999F)
				.mapColor(MapColor.COLOR_BLACK)
				.noOcclusion()
				.noLootTable()
				.pushReaction(PushReaction.BLOCK));
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(OMINOUS,false)
				.setValue(STATE,TrialSpawnerBlock.TrialSpawnerState.INACTIVE));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
		builder.add(OMINOUS,STATE);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pos,@NotNull BlockState state){
		return new TrialSpawnerBlockEntity(pos,state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,@NotNull BlockState state,@NotNull BlockEntityType<T> type){
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
		// 1. Zalohuj NBT ze stareho block entity
		CompoundTag memory=new CompoundTag();
		BlockEntity be=level.getBlockEntity(pos);
		if(be instanceof TrialSpawnerBlockEntity oldBe){
			memory=oldBe.saveWithFullMetadata();
			memory.remove("id");
			memory.remove("x");
			memory.remove("y");
			memory.remove("z");
		}

		// 2. Zachovej ominous stav z old blockstate
		boolean ominous=state.hasProperty(OMINOUS)&&state.getValue(OMINOUS);

		// 3. Postav novy blok se spravnym stavem
		BlockState newState=NTrialsModBlocks.TRIAL_SPAWNER.get().defaultBlockState()
				.setValue(TrialSpawnerBlock.TYPE,TrialSpawnerBlock.SpawnerType.BOSS)
				.setValue(TrialSpawnerBlock.OMINOUS,ominous)
				.setValue(TrialSpawnerBlock.POWERED,false)
				.setValue(TrialSpawnerBlock.STATE,TrialSpawnerBlock.TrialSpawnerState.INACTIVE);

		level.setBlock(pos,newState,Block.UPDATE_ALL);

		// 4. Obnov NBT data do noveho block entity
		BlockEntity newBe=level.getBlockEntity(pos);
		if(newBe instanceof TrialSpawnerBlockEntity trialBe){
			if(!memory.isEmpty()){
				trialBe.load(memory);
			}
			trialBe.setChanged();
			level.sendBlockUpdated(pos,newState,newState,Block.UPDATE_ALL);
		}
	}

	// Pojistka - klik okamzite spusti konverzi
	@Override
	public @NotNull InteractionResult use(@NotNull BlockState state,Level level,@NotNull BlockPos pos,
										  Player player,@NotNull InteractionHand hand,@NotNull BlockHitResult hit){
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
