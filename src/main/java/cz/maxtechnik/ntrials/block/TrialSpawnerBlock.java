package cz.maxtechnik.ntrials.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;

public class TrialSpawnerBlock extends Block{
    public static final BooleanProperty OMINOUS=BooleanProperty.create("ominous");
    public static final EnumProperty<TrialSpawnerState>STATE=EnumProperty.create("trial_spawner_state",TrialSpawnerState.class);
    public TrialSpawnerBlock(){
        super(Properties.of().sound(SoundType.METAL).strength(-1,3600000).noOcclusion().mapColor(MapColor.COLOR_BLACK).isRedstoneConductor((bs,br,bp)->false).noLootTable());
        this.registerDefaultState(this.stateDefinition.any().setValue(OMINOUS,false).setValue(STATE,TrialSpawnerState.INACTIVE));
    }
    public enum TrialSpawnerState implements net.minecraft.util.StringRepresentable {
        INACTIVE("inactive"),
        COOLDOWN("cooldown"),
        EJECTING_REWARD("ejecting_reward"),
        WAITING_FOR_REWARD_EJECTION("waiting_for_reward_ejection"),
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
        builder.add(OMINOUS,STATE);
    }
    @Override
    public int getLightBlock(@NotNull BlockState state,@NotNull BlockGetter worldIn,@NotNull BlockPos pos){
        return 0;
    }
}
