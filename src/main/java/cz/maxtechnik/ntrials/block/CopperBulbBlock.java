package cz.maxtechnik.ntrials.block;

import cz.maxtechnik.ntrials.NTrialsModEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class CopperBulbBlock extends Block implements WeatheringCopper {
    private final WeatherState level;
    public static final BooleanProperty LIT=BooleanProperty.create("lit");
    public static final BooleanProperty POWERED=BooleanProperty.create("powered");
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>builder){
        builder.add(LIT);
        builder.add(POWERED);
    }
    public CopperBulbBlock(WeatherState level, BlockBehaviour.Properties props){
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT,false).setValue(POWERED,false));
        this.level=level;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return state.getValue(LIT) ? 15 : 0;
    }

    @Override
    public @NotNull WeatherState getAge(){
        return this.level;
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        if (state.getValue(LIT)) {
            if (this.level == WeatherState.UNAFFECTED) {
                return 15;
            } else if (this.level == WeatherState.EXPOSED) {
                return 12;
            } else if (this.level == WeatherState.WEATHERED) {
                return 8;
            } else if (this.level == WeatherState.OXIDIZED) {
                return 4;
            } else {
                return 0;
            }
        }
        else {
            return 0;
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, isMoving);
        if (!level.isClientSide) {
            boolean powered = level.hasNeighborSignal(pos);
            if (state.getValue(POWERED) != powered) {
                BlockState new_state = state;
                new_state = new_state.setValue(POWERED, powered);
                if (powered) {
                    if (state.getValue(LIT)) {
                        new_state = new_state.setValue(LIT, false);
                    } else {
                        new_state = new_state.setValue(LIT, true);
                    }
                }
                level.setBlock(pos, new_state, 3);
            }
        }
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit){
        ItemStack itemInHand=player.getItemInHand(hand);
        // Honeycomb interakcia - waxovanie (výmena za waxed verziu)
        if(itemInHand.is(Items.HONEYCOMB)){
            Block waxedBlock= NTrialsModEvents.WAXING_MAP.get(this);
            if(waxedBlock!=null){
                if(!level.isClientSide){
                    BlockState new_state=waxedBlock.defaultBlockState();
                    new_state=new_state.setValue(LIT,state.getValue(LIT));
                    new_state=new_state.setValue(POWERED,state.getValue(POWERED));

                    level.setBlock(pos,new_state,3);
                    level.playSound(null,pos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS,1f,1f);
                    if(level instanceof ServerLevel serverLevel){
                        for(int i=0;i<20;i++){
                            double x=pos.getX()-0.2+level.random.nextDouble()*1.4;
                            double y=pos.getY()-0.2+level.random.nextDouble()*1.4;
                            double z=pos.getZ()-0.2+level.random.nextDouble()*1.4;
                            serverLevel.sendParticles(ParticleTypes.WAX_ON,x,y,z,1,0,0,0,0.05);
                        }
                    }
                    if(!player.isCreative()){
                        itemInHand.shrink(1);
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        if (itemInHand.getItem()instanceof AxeItem){
            Block scrapedBlock=NTrialsModEvents.SCRAPING_MAP.get(this);
            if(scrapedBlock!=null){ // Null znamená že je to první fáze (nelze čistit dál)
                if(!level.isClientSide){
                    level.setBlock(pos,scrapedBlock.defaultBlockState(),3);
                    level.playSound(null,pos,SoundEvents.AXE_SCRAPE,SoundSource.BLOCKS,1.0f,1.0f);
                    if(level instanceof ServerLevel serverLevel){
                        for(int i=0;i<20;i++){
                            double x=pos.getX()-0.2+level.random.nextDouble()*1.4;
                            double y=pos.getY()-0.2+level.random.nextDouble()*1.4;
                            double z=pos.getZ()-0.2+level.random.nextDouble()*1.4;
                            serverLevel.sendParticles(ParticleTypes.SCRAPE,x,y,z,1,0,0,0,0.05);
                        }
                    }
                    // Poškodenie nástroja
                    itemInHand.hurtAndBreak(1,player,(p)->p.broadcastBreakEvent(hand));
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        return super.use(state,level,pos,player,hand,hit);
    }
}
