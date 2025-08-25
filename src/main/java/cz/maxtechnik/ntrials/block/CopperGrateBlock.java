package cz.maxtechnik.ntrials.block;

import cz.maxtechnik.ntrials.NTrialsModEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.WeatheringCopper;
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

public class CopperGrateBlock extends Block implements SimpleWaterloggedBlock, WeatheringCopper {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private final WeatherState level;

    public CopperGrateBlock(WeatherState level, BlockBehaviour.Properties props){
        super(props);
        this.level = level;
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED,false));
    }
    @Override
    public @NotNull WeatherState getAge(){
        return this.level;
    }
    @Override
    public boolean isRandomlyTicking(@NotNull BlockState state){
        // Blok môže oxidovať iba ak nie je na najvyššom stupni oxidácie (OXIDIZED)
        return this.getAge()!=WeatherState.OXIDIZED;
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
    public @NotNull FluidState getFluidState(BlockState state){
        return state.getValue(WATERLOGGED)?Fluids.WATER.getSource(false):super.getFluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState state,@NotNull Direction facing,@NotNull BlockState facingState,@NotNull LevelAccessor world,@NotNull BlockPos currentPos,@NotNull BlockPos facingPos){
        if(state.getValue(WATERLOGGED)){
            world.scheduleTick(currentPos,Fluids.WATER,Fluids.WATER.getTickDelay(world));
        }
        return super.updateShape(state,facing,facingState,world,currentPos,facingPos);
    }

    @Override
    public void randomTick(@NotNull BlockState state, @NotNull ServerLevel serverLevel, @NotNull BlockPos pos, @NotNull RandomSource random){
        // Používame vanilla Minecraft logiku pro oxidáciu
        this.changeOverTime(state,serverLevel,pos,random);
    }
    // Implementujeme vlastní changeOverTime metódu s vanilla logikou
    private void changeOverTime(BlockState state,ServerLevel level,BlockPos pos,RandomSource random){
        // Vanilla oxidácia má pravděpodobnosť približne 1/17.6 na každý random tick
        if(random.nextFloat()< 0.05688889f){
            this.tryOxidize(state,level,pos,random);
        }
    }

    private void tryOxidize(BlockState state,ServerLevel level,BlockPos pos,RandomSource random){
        int nearbyOxidizedBlocks=0;
        // Kontrolujeme 4x4x4 oblasť okolo bloku (vanilla logika)
        for(BlockPos nearbyPos:BlockPos.betweenClosed(pos.offset(-2,-2,-2),pos.offset(2,2,2))){
            if(nearbyPos.distManhattan(pos)<=4){
                BlockState nearbyState=level.getBlockState(nearbyPos);
                Block nearbyBlock=nearbyState.getBlock();
                // Počítame oxidované bloky v okolí
                if(nearbyBlock instanceof WeatheringCopper copper){
                    WeatherState nearbyAge=copper.getAge();
                    if(nearbyAge==WeatherState.OXIDIZED){
                        nearbyOxidizedBlocks++;
                    }
                }
            }
        }
        // Výpočet šance na oxidáciu na základe okolia (vanilla logika)
        float oxidationChance=(nearbyOxidizedBlocks+1)/64f;
        if(random.nextFloat()<oxidationChance){
            Block nextBlock= NTrialsModEvents.OXIDATION_LEVEL_INCREASES.get(this);
            if(nextBlock!=null){
                BlockState nextState = nextBlock.defaultBlockState();
                // Zachováme stav WATERLOGGED pri výmene bloku
                nextState = nextState.setValue(WATERLOGGED, state.getValue(WATERLOGGED));

                level.setBlockAndUpdate(pos,nextState);
            }
        }
    }


}
