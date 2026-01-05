package cz.maxtechnik.ntrials.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class CopperUtil {
    private CopperUtil(){}
    public static void play(Level level, BlockPos pos, SoundEvent sound){
        level.playSound(null,pos,sound, SoundSource.BLOCKS,1f,1f);
    }
    public static void spawnParticles(Level level, BlockPos pos, ParticleOptions particle){
        if(level instanceof ServerLevel serverLevel){
            double cx = pos.getX() + 0.5;
            double cy = pos.getY() + 0.5;
            double cz = pos.getZ() + 0.5;
            int count = 20;
            double spread = 0.7;
            double speed = 0.05;
            serverLevel.sendParticles(particle, cx, cy, cz, count, spread, spread, spread, speed);
        }
    }
    public static void damageToolIfNotCreative(ItemStack stack, Player player, InteractionHand hand){
        if(!player.isCreative()){
            stack.hurtAndBreak(1,player,p -> p.broadcastBreakEvent(hand));
        }
    }
}
