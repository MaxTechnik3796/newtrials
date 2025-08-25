package cz.maxtechnik.ntrials.block;

import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import cz.maxtechnik.ntrials.NTrialsModEvents;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public class WaxedCopperDoorBlock extends DoorBlock {

    public WaxedCopperDoorBlock(BlockBehaviour.Properties props){
        super(props, BlockSetType.OAK);
    }

    @Override
    public int getLightBlock(@NotNull BlockState state,@NotNull BlockGetter worldIn,@NotNull BlockPos pos){
        return 0;
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit){
        ItemStack itemInHand=player.getItemInHand(hand);

        // Axe unwaxing - POUZE když držíme shift
        if(itemInHand.getItem() instanceof AxeItem && player.isShiftKeyDown()){
            Block unwaxedBlock=NTrialsModEvents.UNWAXING_MAP.get(this);
            if(unwaxedBlock!=null){
                if(!level.isClientSide){
                    // Inspirované tryOxidize funkcí - zpracování obou dílů dveří současně
                    BlockPos otherPos;
                    BlockState otherState;

                    // Najdeme druhý díl dveří
                    if(state.getValue(HALF) == DoubleBlockHalf.LOWER){
                        // Jsme dolní díl, druhý díl je nahoře
                        otherPos = pos.above();
                        otherState = level.getBlockState(otherPos);
                    } else {
                        // Jsme horní díl, druhý díl je dole
                        otherPos = pos.below();
                        otherState = level.getBlockState(otherPos);
                    }

                    // Zkontrolujeme, že druhý díl je stejný typ dveří
                    if(otherState.getBlock() == this){
                        // Připravíme nové stavy pro oba díly (zachováme všechny properties)
                        BlockState newState1 = unwaxedBlock.defaultBlockState()
                                .setValue(FACING, state.getValue(FACING))
                                .setValue(OPEN, state.getValue(OPEN))
                                .setValue(HINGE, state.getValue(HINGE))
                                .setValue(POWERED, state.getValue(POWERED))
                                .setValue(HALF, state.getValue(HALF));

                        BlockState newState2 = unwaxedBlock.defaultBlockState()
                                .setValue(FACING, otherState.getValue(FACING))
                                .setValue(OPEN, otherState.getValue(OPEN))
                                .setValue(HINGE, otherState.getValue(HINGE))
                                .setValue(POWERED, otherState.getValue(POWERED))
                                .setValue(HALF, otherState.getValue(HALF));

                        // Vyměníme bloky PŘÍMO bez dropu - používáme flag 2|16 (no drop + no physics update)
                        level.setBlock(pos, newState1, 2 | 16);
                        level.setBlock(otherPos, newState2, 2 | 16);

                        // Pošleme update klientům pro oba bloky
                        level.sendBlockUpdated(pos, state, newState1, 3);
                        level.sendBlockUpdated(otherPos, otherState, newState2, 3);

                        level.playSound(null,pos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS,1.0f,1.0f);
                        if(level instanceof ServerLevel serverLevel){
                            for(int i=0;i<20;i++){
                                double x=pos.getX()-0.2+level.random.nextDouble()*1.4;
                                double y=pos.getY()-0.2+level.random.nextDouble()*1.4;
                                double z=pos.getZ()-0.2+level.random.nextDouble()*1.4;
                                serverLevel.sendParticles(ParticleTypes.WAX_OFF,x,y,z,1,0,0,0,0.05);
                            }
                        }
                        // Poškození nástroje místo konzumace
                        itemInHand.hurtAndBreak(1,player,(p)->p.broadcastBreakEvent(hand));
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        // Pokud není shift stisknut NEBO není to axe, použij normální chování dveří
        return super.use(state, level, pos, player, hand, hit);
    }


}
