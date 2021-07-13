package io.github.ocelot.modelanima;

import io.github.ocelot.modelanima.api.common.animation.AnimatedEntity;
import io.github.ocelot.modelanima.api.common.animation.AnimationState;
import io.github.ocelot.modelanima.api.common.molang.MolangVariableProvider;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.monster.PillagerEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.*;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.stream.Stream;

public class Yeti extends MonsterEntity implements AnimatedEntity
{
    public static final AnimationState THROW_SNOWBALL = new AnimationState(53, new ResourceLocation(TestMod.MOD_ID, "yeti.setup"), new ResourceLocation(TestMod.MOD_ID, "yeti.throw_snowball"));
    public static final AnimationState ATTACK = new AnimationState(10, new ResourceLocation(TestMod.MOD_ID, "yeti.setup"), new ResourceLocation(TestMod.MOD_ID, "yeti.attack"));
    public static final AnimationState[] CREATE_SNOWBALLS = new AnimationState[]{
            new AnimationState(40, new ResourceLocation(TestMod.MOD_ID, "yeti.setup"), new ResourceLocation(TestMod.MOD_ID, "yeti.prepare_snowball")),
            new AnimationState(60, new ResourceLocation(TestMod.MOD_ID, "yeti.setup"), new ResourceLocation(TestMod.MOD_ID, "yeti.prepare_snowball")),
            new AnimationState(80, new ResourceLocation(TestMod.MOD_ID, "yeti.setup"), new ResourceLocation(TestMod.MOD_ID, "yeti.prepare_snowball")),
            new AnimationState(100, new ResourceLocation(TestMod.MOD_ID, "yeti.setup"), new ResourceLocation(TestMod.MOD_ID, "yeti.prepare_snowball"))
    };
    private static final AnimationState[] ANIMATIONS = Stream.concat(Stream.of(THROW_SNOWBALL, ATTACK), Arrays.stream(CREATE_SNOWBALLS)).toArray(AnimationState[]::new);

    private AnimationState animationState;
    private int animationTick;

    public Yeti(EntityType<? extends MonsterEntity> type, World world)
    {
        super(type, world);
        this.xpReward = 50;
        this.animationState = AnimationState.EMPTY;
    }

    public static AttributeModifierMap.MutableAttribute createAttributes()
    {
        return PillagerEntity.createAttributes().add(Attributes.MAX_HEALTH, 150).add(Attributes.MOVEMENT_SPEED, 0.25).add(Attributes.KNOCKBACK_RESISTANCE, 1.0).add(Attributes.ATTACK_DAMAGE, 6.0);
    }

    @Override
    protected void registerGoals()
    {
        super.registerGoals();
        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.goalSelector.addGoal(3, new YetiAttackTargetGoal(this, 1.0D, true));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomWalkingGoal(this, 0.6D));
        this.goalSelector.addGoal(9, new LookAtGoal(this, PlayerEntity.class, 15.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtGoal(this, MobEntity.class, 15.0F));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Yeti.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillagerEntity.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolemEntity.class, true));
    }

    private void createSnowball()
    {
        AnimatedEntity.setAnimation(this, CREATE_SNOWBALLS[this.random.nextInt(CREATE_SNOWBALLS.length)]);
    }

    @Override
    public boolean isImmobile()
    {
        return !this.isNoAnimatonPlaying();
    }

    @Override
    public void tick()
    {
        super.tick();
        this.animationTick();

        if (!this.level.isClientSide())
        {
            if (this.isAnimatonPlaying(THROW_SNOWBALL) && this.getAnimationTick() == 12)
            {
//                this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 10.0F, 0.1F / (this.random.nextFloat() * 0.4F + 0.8F));
//                GiantSnowball snowball = new GiantSnowball(this.level, this);
//                snowball.shootFromRotation(this, this.xRot + 0.5F, this.yHeadRot, 0.0F, 1.5F, 1.0F);
//                this.level.addFreshEntity(snowball);
            }
        }
        else if (!this.isNoAnimatonPlaying())
        {
            for (AnimationState animation : CREATE_SNOWBALLS)
            {
                if (this.isAnimatonPlaying(animation))
                {
                    if (this.getAnimationTick() >= 13)
                        for (int i = 0; i < 4; i++)
                            this.level.addParticle(new BlockParticleData(ParticleTypes.FALLING_DUST, Blocks.PACKED_ICE.defaultBlockState()), this.getRandomX(0.5), this.getY(1.0) + this.random.nextFloat() * 2.0, this.getRandomZ(0.5), 0, 0, 0);
                    break;
                }
            }
        }
    }

    @Override
    public void setAnimationState(AnimationState state)
    {
        this.onAnimationStop(this.animationState);
        this.animationState = state;
        this.setAnimationTick(0);
    }

    @Override
    public void resetAnimationState()
    {
        if (!this.level.isClientSide() && Arrays.stream(CREATE_SNOWBALLS).anyMatch(e -> e == animationState))
        {
            AnimatedEntity.setAnimation(this, THROW_SNOWBALL);
        }
        else
        {
            AnimatedEntity.super.resetAnimationState();
        }
    }

    @Override
    public boolean doHurtTarget(Entity target)
    {
        AnimatedEntity.setAnimation(this, ATTACK);
        float f = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float f1 = (int) f > 0 ? f / 2.0F + (float) this.random.nextInt((int) f) : f;
        boolean flag = target.hurt(DamageSource.mobAttack(this), f1);
        if (flag)
        {
            target.setDeltaMovement(target.getDeltaMovement().add(0.0, 0.4F, 0.0));
            this.doEnchantDamageEffects(this, target);
        }

        this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0F, 1.0F);
        return flag;
    }

    @Override
    public void checkDespawn()
    {
        if (this.level.getDifficulty() == Difficulty.PEACEFUL)
        {
            this.remove();
        }
        else
        {
            this.noActionTime = 0;
        }
    }

    @Override
    public boolean addEffect(EffectInstance p_195064_1_)
    {
        return false;
    }

    @Override
    protected boolean canRide(Entity entity)
    {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound()
    {
        return SoundEvents.PILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound()
    {
        return SoundEvents.PILLAGER_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource arg)
    {
        return SoundEvents.PILLAGER_HURT;
    }

    @Override
    protected float getVoicePitch()
    {
        return 0.5F;
    }

    @Override
    public CreatureAttribute getMobType()
    {
        return CreatureAttribute.ILLAGER;
    }

    @Override
    public int getAnimationTick()
    {
        return this.animationTick;
    }

    @Override
    public AnimationState getAnimationState()
    {
        return this.animationState;
    }

    @Override
    public void setAnimationTick(int animationTick)
    {
        this.animationTick = animationTick;
    }

    @Override
    public AnimationState[] getAnimationStates()
    {
        return ANIMATIONS;
    }

    static class YetiAttackTargetGoal extends MeleeAttackGoal
    {
        private final Yeti illager;
        private int attackTimer;
        private int snowballTimer;

        public YetiAttackTargetGoal(Yeti illager, double speedTowardsTarget, boolean longMemory)
        {
            super(illager, speedTowardsTarget, longMemory);
            this.illager = illager;
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity enemy, double distToEnemySqr)
        {
            this.attackTimer = Math.max(this.attackTimer - 1, 0);
            this.snowballTimer = Math.max(this.snowballTimer - 1, 0);

            if (this.illager.isImmobile())
                return;

            if (distToEnemySqr <= this.getAttackReachSqr(enemy))
            {
                if (this.attackTimer > 0)
                    return;

                this.attackTimer = 20;
                this.mob.swing(Hand.MAIN_HAND);
                this.mob.doHurtTarget(enemy);
            }
            else if (this.snowballTimer <= 0 && distToEnemySqr > 36.0 && this.mob.getRandom().nextInt(10) == 0)
            {
                this.illager.createSnowball();
                this.snowballTimer = this.illager.getAnimationState().getTickDuration() + THROW_SNOWBALL.getTickDuration() + 100;
            }
        }
    }
}
