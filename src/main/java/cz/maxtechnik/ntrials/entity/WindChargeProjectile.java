package cz.maxtechnik.ntrials.entity;

import net.minecraft.server.level.ServerLevel;
import cz.maxtechnik.ntrials.init.NTrialsModEntityTypes;
import cz.maxtechnik.ntrials.init.NTrialsModItems;
import cz.maxtechnik.ntrials.init.NTrialsModParticles;
import cz.maxtechnik.ntrials.init.NTrialsModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;

public class WindChargeProjectile extends ThrowableItemProjectile {
    private int tickCount = 0;
    private static final int MAX_LIFETIME = 100; // 5 seconds (20 ticks per second)

    public WindChargeProjectile(EntityType<? extends WindChargeProjectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    public WindChargeProjectile(Level level, LivingEntity shooter) {
        super(NTrialsModEntityTypes.WIND_CHARGE_PROJECTILE.get(), shooter, level);
        this.setNoGravity(true);
    }

    @Override
    protected Item getDefaultItem() {
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
            boolean shouldExplode = false;

            // Explode after maximum lifetime
            if (this.tickCount >= MAX_LIFETIME) {
                shouldExplode = true;
            }

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
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        this.createWindExplosion();
        if (!this.level().isClientSide) {
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        this.createWindExplosion();
        if (!this.level().isClientSide) {
            this.discard();
        }
    }

    private void createWindExplosion() {
        Vec3 center = this.position();
        Vec3 explosion_center = new Vec3(center.x, center.y - 0.5D, center.z);

        double radius = 4.375D; // Increased by 25% from 3.5D to 4.37D5

        // Play explosion sound as wind burst substitute
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                NTrialsModSounds.WIND_BURST.get(), SoundSource.NEUTRAL, 0.8F, 1.2F);

        // Enhanced gust particles - spawn on both sides for visibility
        // Main gust explosion - large radial particles
        if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            // Hlavní gust particles
            for (int i = 0; i < 5; i++) {
                double angle = (i / 20.0D) * Math.PI * 2;
                double distance = 0.5D + this.random.nextDouble() * 2.0D;

                double offsetX = Math.cos(angle) * distance;
                double offsetY = (this.random.nextDouble() - 0.5D) * 1.0D;
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


        // Find and knockback entities
        List<Entity> entities = this.level().getEntities(this, this.getBoundingBox().inflate(radius));
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity) {
                double distance = entity.distanceTo(this);
                if (distance <= radius) {
                    // Calculate knockback direction
                    Vec3 direction = entity.position().subtract(explosion_center).normalize();
                    // 1.5d strangth
                    double knockbackStrength = 1.5D * (1.0D - (distance / radius)); // Increased from 1.5D to 2.0D

                    // Apply stronger upward knockback (like modern Minecraft wind charge)
                    Vec3 knockback = direction.scale(knockbackStrength);

                    //0.2d
                    // Enhanced vertical component - minimum 0.6D upward, stronger upward bias
                    double verticalKnockback = Math.max(knockback.y + 0.2D, 0.6D);

                    // Reduce horizontal knockback slightly to emphasize upward movement
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

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
