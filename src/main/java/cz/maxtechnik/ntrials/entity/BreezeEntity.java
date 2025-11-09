package cz.maxtechnik.ntrials.entity;

import cz.maxtechnik.ntrials.init.NTrialsModParticles;
import cz.maxtechnik.ntrials.init.NTrialsModSounds;
import java.util.EnumSet;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class BreezeEntity extends Monster {
    private static final EntityDataAccessor<Boolean> DATA_IS_CHARGING = SynchedEntityData.defineId(BreezeEntity.class, EntityDataSerializers.BOOLEAN);
    private float allowedHeightOffset = 0.4F;
    private int nextHeightOffsetUpdateTime;
    private int attackCooldown = 0;
    private static final int ATTACK_COOLDOWN = 40; //set attack sooldown 2 seconds (40 ticks)

    public BreezeEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(false);
        this.moveControl = new MoveControl(this);
        this.navigation = this.createNavigation(level);
    }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
        FlyingPathNavigation flyingpathnavigation = new FlyingPathNavigation(this, level);
        flyingpathnavigation.setCanOpenDoors(false);
        flyingpathnavigation.setCanFloat(true);
        flyingpathnavigation.setCanPassDoors(true);
        return flyingpathnavigation;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_IS_CHARGING, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D) //set max health
                .add(Attributes.ATTACK_DAMAGE, 4.0D) //set attack damage
                .add(Attributes.MOVEMENT_SPEED, 0.6D) //set movemoment speed
                .add(Attributes.FOLLOW_RANGE, 16.0D); //set follow range
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new BreezeAttackGoal(this));
        this.goalSelector.addGoal(3, new BreezeChaseGoal(this));
        this.goalSelector.addGoal(4, new BreezeKeepDistanceGoal());
        this.goalSelector.addGoal(5, new CombatJumpGoal(this));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        // Enhanced wind particle effects
        if (!this.onGround()) {
            // Main wind particles
            if (this.random.nextFloat() < 0.2F) {
                for (int i = 0; i < 3; i++) {
                    double angle = this.random.nextDouble() * Math.PI * 2;
                    double radius = 0.5D + this.random.nextDouble() * 0.5D;
                    double d0 = this.getX() + Math.cos(angle) * radius;
                    double d1 = this.getY() + 0.3D + (this.random.nextDouble() - 0.5D) * 0.5D;
                    double d2 = this.getZ() + Math.sin(angle) * radius;
                    
                    this.level().addParticle(NTrialsModParticles.SMALL_GUST.get(),
                        d0, d1, d2,
                        Math.cos(angle) * 0.05D,
                        (this.random.nextDouble() - 0.5D) * 0.05D,
                        Math.sin(angle) * 0.05D);
                }
            }
            
            // Additional gust particles
            if (this.random.nextFloat() < 0.1F) {
                double d0 = this.getX() + (this.random.nextDouble() - 0.5D);
                double d1 = this.getY() + 0.5D;
                double d2 = this.getZ() + (this.random.nextDouble() - 0.5D);
                this.level().addParticle(NTrialsModParticles.GUST.get(), d0, d1, d2, 0.0D, 0.1D, 0.0D);
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
    public boolean causeFallDamage(float fallDistance, float multiplier, @NotNull DamageSource source) {
        return false; // Immune to fall damage
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getDirectEntity() instanceof Projectile) {
            this.playSound(NTrialsModSounds.ENTITY_BREEZE_DEFLECT.get(), 1.0F, 1.0F);
            return false; // Immune to projectiles
        }
        return super.hurt(source, amount);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.level().dimensionType().hasCeiling() ? NTrialsModSounds.ENTITY_BREEZE_AMBIENT_CAVE.get() : NTrialsModSounds.ENTITY_BREEZE_AMBIENT.get();
    }

    @Override
    protected @NotNull SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return NTrialsModSounds.ENTITY_BREEZE_HURT.get();
    }

    @Override
    protected @NotNull SoundEvent getDeathSound() {
        return NTrialsModSounds.ENTITY_BREEZE_DEATH.get();
    }

    public void shootWindCharge(LivingEntity target) {
        if (attackCooldown <= 0) {
            this.playSound(NTrialsModSounds.ENTITY_BREEZE_SHOOT.get(), 1.0F, 1.0F);
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
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("AttackCooldown", this.attackCooldown);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
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

    static class BreezeChaseGoal extends Goal {
        private final BreezeEntity breeze;
        private static final double IDEAL_DISTANCE = 3.0D;
        private static final double DISTANCE_TOLERANCE = 1.0D;
        private static final double MOVE_SPEED = 1.0D;

        public BreezeChaseGoal(BreezeEntity breeze) {
            this.breeze = breeze;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.breeze.getTarget();
            return target != null;

            // Always try to chase when we have a target
        }

        @Override
        public void tick() {
            LivingEntity target = this.breeze.getTarget();
            if (target == null) return;

            double distanceToTarget = this.breeze.distanceTo(target);

            // If we have line of sight, we can attack - maintain 3-block distance
            if (this.breeze.hasLineOfSight(target)) {
                Vec3 directionToTarget = target.position().subtract(this.breeze.position()).normalize();

                if (Math.abs(distanceToTarget - IDEAL_DISTANCE) > DISTANCE_TOLERANCE) {
                    // Too close or too far - adjust position
                    if (distanceToTarget < IDEAL_DISTANCE) {
                        // Move away to maintain distance
                        this.breeze.getMoveControl().setWantedPosition(
                            this.breeze.getX() - directionToTarget.x * MOVE_SPEED,
                            this.breeze.getY(),
                            this.breeze.getZ() - directionToTarget.z * MOVE_SPEED,
                            1.0D
                        );
                    } else {
                        // Move closer to get to attack range
                        this.breeze.getMoveControl().setWantedPosition(
                            this.breeze.getX() + directionToTarget.x * MOVE_SPEED,
                            this.breeze.getY(),
                            this.breeze.getZ() + directionToTarget.z * MOVE_SPEED,
                            1.0D
                        );
                    }
                }
            } else {
                // No line of sight - pathfind around obstacles to find attack position
                // The navigation system will handle finding a path to the target
                this.breeze.getNavigation().moveTo(target, MOVE_SPEED);
            }
        }
    }

    static class BreezeKeepDistanceGoal extends Goal {

        public BreezeKeepDistanceGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            // This goal is now handled by BreezeChaseGoal
            return false;
        }

        @Override
        public void tick() {
            // Not used anymore
        }
    }

    static class CombatJumpGoal extends Goal {
        private final BreezeEntity breeze;
        private int jumpCooldown = 0;
        private static final int JUMP_COOLDOWN = 60; // 3 seconds (60 ticks)
        private static final double JUMP_STRENGTH = 2.0D; // Strong enough for 8 block distance
        private static final double JUMP_HEIGHT = 1.0D;
        private static final int CHARGE_DURATION = 15; // 0.75 second charge-up time
        private int chargeTime = 0;
        private boolean isCharging = false;

        public CombatJumpGoal(BreezeEntity breeze) {
            this.breeze = breeze;
            this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (isCharging) {
                return true;
            }

            LivingEntity target = this.breeze.getTarget();
            if (target == null || this.breeze.isInWater() || jumpCooldown > 0) {
                return false;
            }

            double distSqr = this.breeze.distanceToSqr(target);
            // Jump if target is far but within range (6-16 blocks) or path is blocked
            if ((distSqr > 36.0D && distSqr < 256.0D) || // Between 6 and 16 blocks
                this.breeze.getNavigation().createPath(target, 0) == null) {
                isCharging = true;
                chargeTime = CHARGE_DURATION;
                this.breeze.entityData.set(DATA_IS_CHARGING, true);
                this.breeze.playSound(NTrialsModSounds.ENTITY_BREEZE_INHALE.get(), 1.0F, 1.0F);
                return true;
            }
            return false;
        }

        @Override
        public void tick() {
            if (jumpCooldown > 0) {
                jumpCooldown--;
            }

            if (isCharging) {
                chargeTime--;
                if (chargeTime <= 0) {
                    performJump();
                }
            }
        }

        private void performJump() {
            LivingEntity target = this.breeze.getTarget();
            if (target != null) {
                this.breeze.playSound(NTrialsModSounds.ENTITY_BREEZE_JUMP.get(), 1.0F, 1.0F);
                // Calculate direction to target
                Vec3 directionToTarget = target.position().subtract(this.breeze.position()).normalize();

                // Calculate distance to target
                double distance = this.breeze.distanceTo(target);

                // Adjust jump strength based on distance (stronger for longer jumps)
                double adjustedStrength = JUMP_STRENGTH;
                if (distance > 8.0D) {
                    adjustedStrength *= 1.2D; // Boost for very long jumps
                } else if (distance < 4.0D) {
                    adjustedStrength *= 0.8D; // Reduce for short jumps
                }

                // Set motion for jump-dash
                this.breeze.setDeltaMovement(
                    directionToTarget.x * adjustedStrength,
                    JUMP_HEIGHT,
                    directionToTarget.z * adjustedStrength
                );

                // Reset states
                isCharging = false;
                this.breeze.entityData.set(DATA_IS_CHARGING, false);
                jumpCooldown = JUMP_COOLDOWN;
            }
        }

        @Override
        public void start() {
            // Initialization is now handled in canUse()
        }
    }
}