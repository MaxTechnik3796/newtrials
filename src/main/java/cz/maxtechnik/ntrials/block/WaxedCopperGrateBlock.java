package cz.maxtechnik.ntrials.block;

import cz.maxtechnik.ntrials.NTrialsModEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class WaxedCopperGrateBlock extends Block implements SimpleWaterloggedBlock{
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public WaxedCopperGrateBlock(Properties props){
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED,false));
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state,@NotNull BlockGetter reader,@NotNull BlockPos pos){
        return state.getFluidState().isEmpty();
    }

    @Override
    public int getLightBlock(@NotNull BlockState state,@NotNull BlockGetter worldIn,@NotNull BlockPos pos){
        return 0;
    }

    @Override
    public @NotNull VoxelShape getVisualShape(@NotNull BlockState state,@NotNull BlockGetter world,@NotNull BlockPos pos,@NotNull CollisionContext context){
        return Shapes.empty();
    }
    @Override
    public float getShadeBrightness(@NotNull BlockState blockState,@NotNull BlockGetter blockGetter,@NotNull BlockPos pos){
        return 1.0f;
    }
    @Override
    @SuppressWarnings("deprecation")
    public boolean skipRendering(@NotNull BlockState state,BlockState adjacentBlockState,@NotNull Direction side){
        return adjacentBlockState.getBlock()==this||super.skipRendering(state,adjacentBlockState,side);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block,BlockState> builder){
        super.createBlockStateDefinition(builder);
        builder.add(WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context){
        boolean flag=context.getLevel().getFluidState(context.getClickedPos()).getType()==Fluids.WATER;
        return Objects.requireNonNull(super.getStateForPlacement(context)).setValue(WATERLOGGED,flag);
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull FluidState getFluidState(BlockState state){
        return state.getValue(WATERLOGGED)?Fluids.WATER.getSource(false):super.getFluidState(state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(BlockState state,@NotNull Direction facing,@NotNull BlockState facingState,@NotNull LevelAccessor world,@NotNull BlockPos currentPos,@NotNull BlockPos facingPos){
        if(state.getValue(WATERLOGGED)){
            world.scheduleTick(currentPos,Fluids.WATER,Fluids.WATER.getTickDelay(world));
        }
        return super.updateShape(state,facing,facingState,world,currentPos,facingPos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack itemInHand = player.getItemInHand(hand);

        // Sekera interakcia - unwaxovanie (výmena za non-waxed verziu)
        if (itemInHand.getItem() instanceof AxeItem) {
            Block unwaxedBlock = NTrialsModEvents.UNWAXING_MAP.get(this);
            if (unwaxedBlock != null) {
                if (!level.isClientSide) {

                    BlockState nextstate = unwaxedBlock.defaultBlockState();
                    // Zachováme stav WATERLOGGED pri výmene bloku
                    nextstate = nextstate.setValue(WATERLOGGED, state.getValue(WATERLOGGED));

                    level.setBlock(pos, nextstate, 3);
                    level.playSound(null, pos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0F, 1.0F);
                    if (level instanceof ServerLevel serverLevel) {
                        for (int i = 0; i < 20; i++) {
                            double x = pos.getX() - 0.2 + level.random.nextDouble() * 1.4;
                            double y = pos.getY() - 0.2 + level.random.nextDouble() * 1.4;
                            double z = pos.getZ() - 0.2 + level.random.nextDouble() * 1.4;
                            serverLevel.sendParticles(ParticleTypes.WAX_OFF, x, y, z, 1, 0.0, 0.0, 0.0, 0.05);
                        }
                    }
                    // Poškodenie nástroja
                    itemInHand.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        return super.use(state, level, pos, player, hand, hit);
    }

}
