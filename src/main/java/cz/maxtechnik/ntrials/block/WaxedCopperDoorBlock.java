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
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import cz.maxtechnik.ntrials.NTrialsModEvents;
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
        ItemStack itemInHand = player.getItemInHand(hand);

        // Sekera interakce - un-waxing (odstranění vosku a návrat k oxidující verzi)
        if (itemInHand.getItem() instanceof AxeItem){
            Block unwaxedBlock = NTrialsModEvents.UNWAXING_MAP.get(this);
            if(unwaxedBlock != null){
                if(!level.isClientSide){
                    // Zachováme stav dveří
                    BlockState newState = unwaxedBlock.defaultBlockState()
                            .setValue(FACING, state.getValue(FACING))
                            .setValue(OPEN, state.getValue(OPEN))
                            .setValue(HINGE, state.getValue(HINGE))
                            .setValue(POWERED, state.getValue(POWERED))
                            .setValue(HALF, state.getValue(HALF));

                    level.setBlock(pos, newState, 3);
                    level.playSound(null, pos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0f, 1.0f);

                    if(level instanceof ServerLevel serverLevel){
                        for(int i = 0; i < 20; i++){
                            double x = pos.getX() - 0.2 + level.random.nextDouble() * 1.4;
                            double y = pos.getY() - 0.2 + level.random.nextDouble() * 1.4;
                            double z = pos.getZ() - 0.2 + level.random.nextDouble() * 1.4;
                            serverLevel.sendParticles(ParticleTypes.WAX_OFF, x, y, z, 1, 0, 0, 0, 0.05);
                        }
                    }
                    // Poškození nástroje
                    itemInHand.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        // Pokud nebyla použita sekera, pokračuj s normální funkcí dveří
        return super.use(state, level, pos, player, hand, hit);
    }
}
