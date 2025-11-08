package cz.maxtechnik.ntrials.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;

public class HeavyCoreBlock extends Block implements SimpleWaterloggedBlock{
    public static final BooleanProperty WATERLOGGED=BlockStateProperties.WATERLOGGED;
    public HeavyCoreBlock(){
        super(BlockBehaviour.Properties.of().sound(SoundType.NETHERITE_BLOCK).strength(10f,10f).noOcclusion().isRedstoneConductor((bs,br,bp)->false).requiresCorrectToolForDrops());
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
    public VoxelShape getVisualShape(@NotNull BlockState state,@NotNull BlockGetter world,@NotNull BlockPos pos,@NotNull CollisionContext context){
        return Shapes.empty();
    }
    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state,@NotNull BlockGetter world,@NotNull BlockPos pos,@NotNull CollisionContext context){
        return box(4,0,4,12,8,12);
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block,BlockState> builder){
        super.createBlockStateDefinition(builder);
        builder.add(WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context){
        boolean flag = context.getLevel().getFluidState(context.getClickedPos()).getType()==Fluids.WATER;
        return Objects.requireNonNull(super.getStateForPlacement(context)).setValue(WATERLOGGED,flag);
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull FluidState getFluidState(BlockState state){
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false):super.getFluidState(state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull BlockState updateShape(BlockState state,@NotNull Direction facing,@NotNull BlockState facingState,@NotNull LevelAccessor world,@NotNull BlockPos currentPos,@NotNull BlockPos facingPos){
        if (state.getValue(WATERLOGGED)){
            world.scheduleTick(currentPos,Fluids.WATER,Fluids.WATER.getTickDelay(world));
        }
        return super.updateShape(state,facing,facingState,world,currentPos,facingPos);
    }
}

