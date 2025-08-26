package cz.maxtechnik.ntrials.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;

public class VaultBlock extends Block{
    public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty OMINOUS=BooleanProperty.create("ominous");
    public static final EnumProperty<VaultState>STATE=EnumProperty.create("vault_state",VaultState.class);
    public VaultBlock(){
        super(Properties.of().sound(SoundType.METAL).strength(-1,3600000).noOcclusion().mapColor(MapColor.COLOR_BLACK).isRedstoneConductor((bs,br,bp)->false).noLootTable());
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(OMINOUS,false).setValue(STATE,VaultState.INACTIVE));
    }
    public enum VaultState implements net.minecraft.util.StringRepresentable{
        INACTIVE("inactive"),
        EJECTING("ejecting"),
        ACTIVE("active"),
        UNLOCKING("unlocking");
        private final String name;
        VaultState(String name){
            this.name=name;
        }
        @Override
        public @NotNull String getSerializedName(){
            return this.name;
        }
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
        builder.add(OMINOUS,STATE,FACING);
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context){
        return this.defaultBlockState().setValue(FACING,context.getHorizontalDirection().getOpposite());
    }
    public @NotNull BlockState rotate(BlockState state,Rotation rot){
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }
    public @NotNull BlockState mirror(BlockState state,Mirror mirrorIn){
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }
    @Override
    public int getLightBlock(@NotNull BlockState state,@NotNull BlockGetter worldIn,@NotNull BlockPos pos){
        return 0;
    }
}
