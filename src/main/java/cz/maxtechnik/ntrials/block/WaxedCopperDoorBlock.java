package cz.maxtechnik.ntrials.block;

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
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import cz.maxtechnik.ntrials.NTrialsModEvents;
import org.jetbrains.annotations.NotNull;

public class WaxedCopperDoorBlock extends DoorBlock {
    // Shape definitions for different door orientations when closed
    protected static final VoxelShape SOUTH_AABB = box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 3.0D);
    protected static final VoxelShape NORTH_AABB = box(0.0D, 0.0D, 13.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape WEST_AABB = box(13.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape EAST_AABB = box(0.0D, 0.0D, 0.0D, 3.0D, 16.0D, 16.0D);

    public WaxedCopperDoorBlock(BlockBehaviour.Properties props){
        super(props, BlockSetType.OAK);
    }

    @Override
    public int getLightBlock(@NotNull BlockState state,@NotNull BlockGetter worldIn,@NotNull BlockPos pos){
        return 0;
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        Direction direction = state.getValue(FACING);
        boolean flag = !state.getValue(OPEN);
        boolean flag1 = state.getValue(HINGE) == DoorHingeSide.RIGHT;

        return switch (direction) {
            case SOUTH -> flag ? SOUTH_AABB : (flag1 ? EAST_AABB : WEST_AABB);
            case WEST -> flag ? WEST_AABB : (flag1 ? SOUTH_AABB : NORTH_AABB);
            case EAST -> flag ? EAST_AABB : (flag1 ? NORTH_AABB : SOUTH_AABB);
            default -> flag ? SOUTH_AABB : (flag1 ? WEST_AABB : EAST_AABB);
        };
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        // When the door is open, there should be no collision
        if (state.getValue(OPEN)) {
            return Shapes.empty();
        }
        // When closed, use the same shape as the visual shape
        return getShape(state, level, pos, context);
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
