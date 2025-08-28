package cz.maxtechnik.ntrials.entity;

import cz.maxtechnik.ntrials.init.NTrialsModEntityTypes;
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
import cz.maxtechnik.ntrials.init.NTrialsModItems;

import java.util.List;

public class WindChargeProjectile extends ThrowableItemProjectile {

    public WindChargeProjectile(EntityType<? extends WindChargeProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public WindChargeProjectile(Level level, LivingEntity shooter) {
        super(NTrialsModEntityTypes.WIND_CHARGE_PROJECTILE.get(), shooter, level);
    }

    @Override
    protected Item getDefaultItem() {
        return NTrialsModItems.WIND_CHARGE.get();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            // Use cloud particles for wind effect in 1.20.1
            this.level().addParticle(ParticleTypes.CLOUD, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level().isClientSide) {
            this.createWindExplosion();
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        if (!this.level().isClientSide) {
            this.createWindExplosion();
            this.discard();
        }
    }

    private void createWindExplosion() {
        Vec3 center = this.position();
        double radius = 3.5D;

        // Play explosion sound as wind burst substitute
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
            SoundEvents.GENERIC_EXPLODE, SoundSource.NEUTRAL, 0.8F, 1.2F);

        // Add wind particles
        if (this.level().isClientSide) {
            for (int i = 0; i < 20; i++) {
                double offsetX = (this.random.nextDouble() - 0.5D) * 2.0D * radius;
                double offsetY = (this.random.nextDouble() - 0.5D) * 2.0D * radius;
                double offsetZ = (this.random.nextDouble() - 0.5D) * 2.0D * radius;
                this.level().addParticle(ParticleTypes.CLOUD,
                    center.x + offsetX, center.y + offsetY, center.z + offsetZ,
                    0.0D, 0.0D, 0.0D);
            }
        }

        // Find and knockback entities
        List<Entity> entities = this.level().getEntities(this, this.getBoundingBox().inflate(radius));
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity) {
                double distance = entity.distanceTo(this);
                if (distance <= radius) {
                    // Calculate knockback direction
                    Vec3 direction = entity.position().subtract(center).normalize();
                    double knockbackStrength = 1.5D * (1.0D - (distance / radius));

                    // Apply knockback without damage
                    Vec3 knockback = direction.scale(knockbackStrength);
                    entity.setDeltaMovement(entity.getDeltaMovement().add(knockback.x, Math.max(knockback.y, 0.4D), knockback.z));
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
