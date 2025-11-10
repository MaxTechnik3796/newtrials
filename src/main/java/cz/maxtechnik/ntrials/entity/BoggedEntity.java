package cz.maxtechnik.ntrials.entity;

import net.minecraftforge.network.PlayMessages;
import net.minecraftforge.network.NetworkHooks;

import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.RestrictSunGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.FleeSunGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.core.BlockPos;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import cz.maxtechnik.ntrials.init.NTrialsModEntityTypes;
import cz.maxtechnik.ntrials.init.NTrialsModSounds;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;

public class BoggedEntity extends Monster implements RangedAttackMob {
    public BoggedEntity(PlayMessages.SpawnEntity packet, Level world) {
        this(NTrialsModEntityTypes.BOGGED.get(), world);
    }

    public BoggedEntity(EntityType<BoggedEntity> type, Level world) {
        super(type, world);
        setMaxUpStep(0.6f);
        xpReward = 5;
        setNoAi(false);
        this.reassessWeaponGoal();
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new RestrictSunGoal(this));
        this.goalSelector.addGoal(3, new FleeSunGoal(this, 1.0));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Wolf.class, 6.0F, 1.0, 1.2));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Turtle.class, 10, true, false, Turtle.BABY_ON_LAND_SELECTOR));
    }
    
    private void reassessWeaponGoal() {
        if (this.level() != null && !this.level().isClientSide) {
            this.goalSelector.removeGoal(this.goalSelector.getAvailableGoals().stream()
                .filter(goal -> goal.getGoal() instanceof RangedBowAttackGoal)
                .findFirst()
                .map(net.minecraft.world.entity.ai.goal.WrappedGoal::getGoal)
                .orElse(null));
            this.goalSelector.removeGoal(this.goalSelector.getAvailableGoals().stream()
                .filter(goal -> goal.getGoal() instanceof MeleeAttackGoal)
                .findFirst()
                .map(net.minecraft.world.entity.ai.goal.WrappedGoal::getGoal)
                .orElse(null));
            @SuppressWarnings("deprecation")
            ItemStack itemstack = this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, Items.BOW));
            if (itemstack.is(Items.BOW)) {
                int attackInterval = 40; // 2 seconds (40 ticks)
                this.goalSelector.addGoal(4, new RangedBowAttackGoal<>(this, 1.0, attackInterval, 16.0F));
            } else {
                this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.2, false));
            }
        }
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEAD;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return NTrialsModSounds.ENTITY_BOGGED_AMBIENT.get();
    }

    @Override
    public SoundEvent getHurtSound(@NotNull DamageSource ds) {
        return NTrialsModSounds.ENTITY_BOGGED_HURT.get();
    }

    @Override
    public SoundEvent getDeathSound() {
        return NTrialsModSounds.ENTITY_BOGGED_DEATH.get();
    }
    
    protected SoundEvent getStepSound() {
        return NTrialsModSounds.ENTITY_BOGGED_STEP.get();
    }

    public static void init() {
    }

    @Override
    public void aiStep() {
        boolean shouldBurn = this.isSunBurnTick();
        if (shouldBurn) {
            ItemStack helmet = this.getItemBySlot(EquipmentSlot.HEAD);
            if (!helmet.isEmpty()) {
                if (helmet.isDamageableItem()) {
                    helmet.setDamageValue(helmet.getDamageValue() + this.random.nextInt(2));
                    if (helmet.getDamageValue() >= helmet.getMaxDamage()) {
                        this.broadcastBreakEvent(EquipmentSlot.HEAD);
                        this.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                    }
                }
                shouldBurn = false;
            }
            if (shouldBurn) {
                this.setSecondsOnFire(8);
            }
        }
        super.aiStep();
    }
    
    @Override
    protected boolean isSunBurnTick() {
        if (this.level().isDay() && !this.level().isClientSide) {
            @SuppressWarnings("deprecation")
            float brightness = this.getLightLevelDependentMagicValue();
            BlockPos blockpos = BlockPos.containing(this.getX(), this.getEyeY(), this.getZ());
            boolean isUnderSky = this.level().canSeeSky(blockpos);
            if (isUnderSky && brightness > 0.5F && this.random.nextFloat() * 30.0F < (brightness - 0.4F) * 2.0F) {
                return this.level().getBrightness(LightLayer.BLOCK, blockpos) <= 11;
            }
        }
        return false;
    }
    
    @Override
    public void performRangedAttack(@NotNull LivingEntity target, float distanceFactor) {
        // Get bow from main hand
        @SuppressWarnings("deprecation")
        ItemStack bow = this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, Items.BOW));
        
        // Get arrow projectile
        ItemStack arrowStack = this.getProjectile(bow);
        AbstractArrow arrow = this.getArrow(arrowStack, distanceFactor, bow);
        
        double dx = target.getX() - this.getX();
        double dy = target.getY(0.3333333333333333) - arrow.getY();
        double dz = target.getZ() - this.getZ();
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        
        arrow.shoot(dx, dy + horizontalDistance * 0.20000000298023224, dz, 1.6F, (float)(14 - this.level().getDifficulty().getId() * 4));
        
        // Set damage (2-4 HP = 1-2 hearts)
        double baseDamage = 2.0 + this.random.nextDouble() * 2.0;
        
        // 10% chance for critical hit (1.5x damage)
        if (this.random.nextFloat() < 0.1F) {
            baseDamage *= 1.5;
        }
        
        arrow.setBaseDamage(baseDamage);
        
        this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.level().addFreshEntity(arrow);
    }
    
    protected AbstractArrow getArrow(ItemStack arrowStack, float distanceFactor, ItemStack bow) {
        // Vždy vystřel poison šíp
        ItemStack poisonArrowStack = new ItemStack(Items.TIPPED_ARROW);
        PotionUtils.setPotion(poisonArrowStack, Potions.POISON);
        AbstractArrow arrow = ProjectileUtil.getMobArrow(this, poisonArrowStack, distanceFactor);
        return arrow;
    }
    
    @Override
    @Nullable
    @SuppressWarnings("deprecation")
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty,
                                       @NotNull MobSpawnType reason, @Nullable SpawnGroupData spawnData,
                                       @org.jetbrains.annotations.Nullable net.minecraft.nbt.CompoundTag dataTag) {
        spawnData = super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);


		this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));


        this.reassessWeaponGoal();
        this.populateDefaultEquipmentSlots(this.random, difficulty);
        this.populateDefaultEquipmentEnchantments(this.random, difficulty);

        return spawnData;
    }
    
    @Override
    protected void dropCustomDeathLoot(@NotNull DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);

        // Drop 0-2 bones
        int boneCount = this.random.nextInt(3) + this.random.nextInt(1 + looting);
        for (int i = 0; i < boneCount; i++) {
            this.spawnAtLocation(Items.BONE);
        }

        // Drop 0-2 arrows if equipped with bow
        @SuppressWarnings("deprecation")
        ItemStack mainHand = this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, Items.BOW));
        if (mainHand.is(Items.BOW)) {
            int arrowCount = this.random.nextInt(3) + this.random.nextInt(1 + looting);
            for (int i = 0; i < arrowCount; i++) {
                this.spawnAtLocation(Items.ARROW);
            }

            // 2% chance to drop bow (possibly enchanted)
            if (this.random.nextFloat() < 0.02F) {
                ItemStack bow = new ItemStack(Items.BOW);
                this.spawnAtLocation(bow);
            }
        } else {
            // Drop the melee weapon if equipped
            if (!mainHand.isEmpty()) {
                this.spawnAtLocation(mainHand);
            }
        }
    }
    
    public static AttributeSupplier.Builder createAttributes() {
        AttributeSupplier.Builder builder = Mob.createMobAttributes();
        builder = builder.add(Attributes.MOVEMENT_SPEED, 0.25);
        builder = builder.add(Attributes.MAX_HEALTH, 20);
        builder = builder.add(Attributes.ARMOR, 0);
        builder = builder.add(Attributes.ATTACK_DAMAGE, 2);
        builder = builder.add(Attributes.FOLLOW_RANGE, 16);
        return builder;
    }
}
