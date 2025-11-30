package cz.maxtechnik.ntrials.entity;

import cz.maxtechnik.ntrials.init.NTrialsModEntityTypes;
import cz.maxtechnik.ntrials.init.NTrialsModItems;
import cz.maxtechnik.ntrials.init.NTrialsModParticles;
import cz.maxtechnik.ntrials.init.NTrialsModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WindChargeProjectile extends ThrowableItemProjectile {
    private int tickCount = 0;
    private static final int MAX_LIFETIME = 100; // 5 seconds (20 ticks per second)
    private boolean hasExploded = false;

    public WindChargeProjectile(EntityType<? extends WindChargeProjectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    public WindChargeProjectile(Level level, LivingEntity shooter) {
        super(NTrialsModEntityTypes.WIND_CHARGE_PROJECTILE.get(), shooter, level);
        this.setNoGravity(true);
    }

    @Override
    protected @NotNull Item getDefaultItem() {
        return NTrialsModItems.WIND_CHARGE.get();
    }

    @Override
    public void tick() {
        super.tick();

        this.tickCount++;

        if (this.level().isClientSide) {
            // Use small gust particles for projectile trail
            this.level().addParticle(NTrialsModParticles.SMALL_GUST.get(),
                this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
        }

        // Check for explosion conditions on server side only
        if (!this.level().isClientSide) {
            boolean shouldExplode = this.tickCount >= MAX_LIFETIME;

            // Explode if projectile is moving very slowly (almost stopped)
            if (this.getDeltaMovement().lengthSqr() < 0.01D) {
                shouldExplode = true;
            }

            // Explode if projectile is close to ground and has been flying for a bit
            if (this.tickCount > 5 && this.onGround()) {
                shouldExplode = true;
            }

            // Additional check: if projectile is very close to any block below
            if (this.tickCount > 3) {
                int blockX = (int) Math.floor(this.getX());
                int blockY = (int) Math.floor(this.getY() - 0.5D);
                int blockZ = (int) Math.floor(this.getZ());

                if (!this.level().getBlockState(new BlockPos(blockX, blockY, blockZ)).isAir()) {
                    shouldExplode = true;
                }
            }

            if (shouldExplode) {
                this.createWindExplosion();
                this.discard();
            }
        }
    }

    @Override
    protected void onHit(@NotNull HitResult hitResult) {
        super.onHit(hitResult);
        this.createWindExplosion();
        if (!this.level().isClientSide) {
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        this.createWindExplosion();
        if (!this.level().isClientSide) {
            this.discard();
        }
    }

    private void createWindExplosion() {
        // Prevent double-activation if multiple hit events fire
        if (this.hasExploded) return;
        this.hasExploded = true;

        Vec3 center = this.position();
        Vec3 explosion_center = new Vec3(center.x, center.y - 0.5D, center.z);

        double radius = 4.375D; // Increased by 25% from 3.5D to 4.37D5

        // Play explosion sound as wind burst substitute
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                NTrialsModSounds.WIND_BURST.get(), SoundSource.NEUTRAL, 0.8F, 1.2F);

        // Main gust explosion - large radial particles
        if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            // Hlavní gust particles
            for (int i = 0; i < 5; i++) {
                double angle = (i / 20.0D) * Math.PI * 2;
                double distance = 0.5D + this.random.nextDouble() * 2.0D;

                double offsetX = Math.cos(angle) * distance;
                double offsetY = (this.random.nextDouble() - 0.5D);
                double offsetZ = Math.sin(angle) * distance;

                double velocityX = offsetX * 0.3D;
                double velocityY = Math.abs(offsetY) * 0.2D;
                double velocityZ = offsetZ * 0.3D;

                serverLevel.sendParticles(NTrialsModParticles.GUST.get(),
                        center.x, center.y, center.z,
                        1, velocityX, velocityY, velocityZ, 0.0D);
            }

            // Malé gust particles
            for (int i = 0; i < 10; i++) {
                double offsetX = (this.random.nextDouble() - 0.5D) * radius * 0.5D;
                double offsetY = (this.random.nextDouble() - 0.5D) * radius * 0.3D;
                double offsetZ = (this.random.nextDouble() - 0.5D) * radius * 0.5D;

                double velocityX = offsetX * 0.1D;
                double velocityY = Math.abs(offsetY) * 0.05D;
                double velocityZ = offsetZ * 0.1D;

                serverLevel.sendParticles(NTrialsModParticles.SMALL_GUST.get(),
                        center.x + offsetX * 0.2D, center.y + offsetY * 0.2D, center.z + offsetZ * 0.2D,
                        1, velocityX, velocityY, velocityZ, 0.0D);
            }
        }


        // Server-side: activate blocks and affect entities only once
        if (!this.level().isClientSide) {
            // Activate blocks and redstone in 5x5x5 area
            this.activateBlocksInRadius(explosion_center);

            // Find and knockback entities
            List<Entity> entities = this.level().getEntities(this, this.getBoundingBox().inflate(radius));
            for (Entity entity : entities) {
                if (entity instanceof LivingEntity) {
                    double distance = entity.distanceTo(this);
                    if (distance <= radius) {
                        // Only deal damage if the projectile was shot by a Breeze or BreezeBoss
                        if (this.getOwner() instanceof BreezeEntity) {
                            float damage = 6.0F; // Base damage amount
                            entity.hurt(this.damageSources().explosion(this, this.getOwner()), damage);
                        } else if (this.getOwner() instanceof cz.maxtechnik.ntrials.entity.BreezeBossEntity) {
                            float damage = 9.0F; // 1.5x base damage
                            entity.hurt(this.damageSources().explosion(this, this.getOwner()), damage);
                        }

                        Vec3 direction = entity.position().subtract(explosion_center).normalize();

                        double knockbackStrength = 0.7D * (1.0D - (distance / radius));

                        Vec3 knockback = direction.scale(knockbackStrength);

                        double verticalKnockback = Math.max(knockback.y + 0.7D, 0.6D);

                        double horizontalMultiplier = 0.8D;

                        entity.setDeltaMovement(entity.getDeltaMovement().add(
                            knockback.x * horizontalMultiplier,
                            verticalKnockback,
                            knockback.z * horizontalMultiplier
                        ));
                        entity.hurtMarked = true;
                    }
                }
            }
        }
    }

    private void activateBlocksInRadius(Vec3 center) {
        // Iterate through all blocks in a 2-block radius from center (5x5x5 area)
        double centerX = center.x;
        double centerY = center.y;
        double centerZ = center.z;
        
        for (double offsetX = -2.0D; offsetX <= 2.0D; offsetX += 1.0D) {
            for (double offsetY = -2.0D; offsetY <= 2.0D; offsetY += 1.0D) {
                for (double offsetZ = -2.0D; offsetZ <= 2.0D; offsetZ += 1.0D) {
                    double x = centerX + offsetX;
                    double y = centerY + offsetY;
                    double z = centerZ + offsetZ;
                    
                    BlockPos blockPos = new BlockPos((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
                    BlockState blockState = this.level().getBlockState(blockPos);
                    Block block = blockState.getBlock();

                    // Activate buttons
                    if (block instanceof ButtonBlock) {
                        this.level().blockEvent(blockPos, block, 1, 0);
                    }
                    // Activate levers
                    else if (block instanceof LeverBlock) {
                        BlockState newState = blockState.cycle(LeverBlock.POWERED);
                        this.level().setBlock(blockPos, newState, 3);
                    }
                    // Activate doors (but not iron doors)
                    else if (block instanceof DoorBlock && block != Blocks.IRON_DOOR) {
                        BlockState newState = blockState.cycle(DoorBlock.OPEN);
                        this.level().setBlock(blockPos, newState, 3);
                    }
                    // Activate trap doors (but not iron trap doors)
                    else if (block instanceof TrapDoorBlock && block != Blocks.IRON_TRAPDOOR) {
                        BlockState newState = blockState.cycle(TrapDoorBlock.OPEN);
                        this.level().setBlock(blockPos, newState, 3);
                    }
                    // Activate pressure plates
                    else if (block instanceof PressurePlateBlock) {
                        BlockState newState = blockState.cycle(PressurePlateBlock.POWERED);
                        this.level().setBlock(blockPos, newState, 3);
                    }

                    // If it's redstone wire, set it to fully powered and notify neighbors
                    try {
                        if (block == Blocks.REDSTONE_WIRE || block instanceof RedStoneWireBlock) {
                            // Try to set POWER to 15 if possible
                            try {
                                BlockState newState = blockState.setValue(RedStoneWireBlock.POWER, Integer.valueOf(15));
                                this.level().setBlock(blockPos, newState, 3);
                            } catch (Exception ignored) {
                                // If property not available for some reason, just request neighbor update
                                this.level().updateNeighborsAt(blockPos, block);
                            }
                        } else {
                            // For other blocks we already changed above; ensure redstone updates by notifying neighbors
                            this.level().updateNeighborsAt(blockPos, block);
                        }
                    } catch (Exception ignored) {
                        // Safety: don't crash if any unexpected block operations fail
                    }
                }
            }
        }
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
