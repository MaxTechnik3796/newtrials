package cz.maxtechnik.ntrials.entity;

import cz.maxtechnik.ntrials.init.NTrialsModParticles;
import net.minecraft.core.BlockPos;
import java.util.EnumSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class BreezeEntity extends Monster {
    private static final EntityDataAccessor<Boolean> DATA_IS_CHARGING = SynchedEntityData.defineId(BreezeEntity.class, EntityDataSerializers.BOOLEAN);
    private float allowedHeightOffset = 0.4F;
    private int nextHeightOffsetUpdateTime;
    private int attackCooldown = 0;
    private static final int ATTACK_COOLDOWN = 50; // 2.5 seconds (50 ticks)

    public BreezeEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_IS_CHARGING, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.23D)
                .add(Attributes.FLYING_SPEED, 0.3D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new BreezeAttackGoal(this));
        this.goalSelector.addGoal(3, new RandomFlyGoal(this));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation flyingPathNavigation = new FlyingPathNavigation(this, level);
        flyingPathNavigation.setCanOpenDoors(false);
        flyingPathNavigation.setCanFloat(true);
        flyingPathNavigation.setCanPassDoors(true);
        return flyingPathNavigation;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.onGround()) {
            if (this.random.nextFloat() < 0.1F) {
                // Spawn wind particles during movement
                double d0 = this.getX() + (this.random.nextDouble() - 0.5D) * 0.5D;
                double d1 = this.getY() + 0.3D;
                double d2 = this.getZ() + (this.random.nextDouble() - 0.5D) * 0.5D;
                this.level().addParticle(NTrialsModParticles.SMALL_GUST.get(), d0, d1, d2, 0.0D, 0.0D, 0.0D);
            }
        }

        if (attackCooldown > 0) {
            attackCooldown--;
        }

        // Maintain hover height
        if (!this.level().isClientSide) {
            this.updateFloating();
        }
    }

    private void updateFloating() {
        if (this.nextHeightOffsetUpdateTime <= 0) {
            this.nextHeightOffsetUpdateTime = 100 + this.random.nextInt(50);
            this.allowedHeightOffset = 0.3F + this.random.nextFloat() * 0.2F;
        }
        this.nextHeightOffsetUpdateTime--;

        if (this.getTarget() != null) {
            // Adjust height based on target position
            double targetY = this.getTarget().getY() + this.allowedHeightOffset;
            if (Math.abs(this.getY() - targetY) > 0.25D) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0D, (Math.signum(targetY - this.getY()) * 0.1D), 0.0D));
            }
        } else {
            // Maintain default hover height when no target
            if (this.onGround()) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.1D, 0.0D));
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getDirectEntity() instanceof Projectile) {
            return false; // Immune to projectiles
        }
        return super.hurt(source, amount);
    }

    public void shootWindCharge(LivingEntity target) {
        if (attackCooldown <= 0) {
            WindChargeProjectile windCharge = new WindChargeProjectile(this.level(), this);
            double d0 = target.getX() - this.getX();
            double d1 = target.getY(0.3333333333333333D) - windCharge.getY();
            double d2 = target.getZ() - this.getZ();
            double d3 = Math.sqrt(d0 * d0 + d2 * d2);
            windCharge.shoot(d0, d1 + d3 * 0.2D, d2, 1.6F, 1.0F);
            this.level().addFreshEntity(windCharge);
            attackCooldown = ATTACK_COOLDOWN;
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("AttackCooldown", this.attackCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.attackCooldown = compound.getInt("AttackCooldown");
    }

    static class BreezeAttackGoal extends Goal {
        private final BreezeEntity breeze;
        private int attackTime;

        public BreezeAttackGoal(BreezeEntity breeze) {
            this.breeze = breeze;
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.breeze.getTarget();
            return target != null && target.isAlive() && this.breeze.canAttack(target);
        }

        @Override
        public void start() {
            this.attackTime = 0;
        }

        @Override
        public void tick() {
            LivingEntity target = this.breeze.getTarget();
            if (target != null) {
                double distance = this.breeze.distanceToSqr(target);
                if (distance < 256.0D) { // 16 blocks squared
                    if (this.breeze.hasLineOfSight(target)) {
                        ++this.attackTime;
                        if (this.attackTime == 10) {
                            this.breeze.shootWindCharge(target);
                            this.attackTime = -20; // Reset attack time with delay
                        }
                    }
                }
                this.breeze.getLookControl().setLookAt(target, 30.0F, 30.0F);
            }
        }
    }

    static class RandomFlyGoal extends Goal {
        private final BreezeEntity breeze;

        public RandomFlyGoal(BreezeEntity breeze) {
            this.breeze = breeze;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.breeze.getTarget() != null) {
                return false;
            }
            return !this.breeze.getMoveControl().hasWanted();
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            double d0 = this.breeze.getX() + (this.breeze.getRandom().nextFloat() * 2.0F - 1.0F) * 4.0F;
            double d1 = this.breeze.getY() + (this.breeze.getRandom().nextFloat() * 2.0F - 1.0F) * 4.0F;
            double d2 = this.breeze.getZ() + (this.breeze.getRandom().nextFloat() * 2.0F - 1.0F) * 4.0F;
            this.breeze.getMoveControl().setWantedPosition(d0, d1, d2, 1.0D);
        }
    }
}