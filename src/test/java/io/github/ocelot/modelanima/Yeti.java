package io.github.ocelot.modelanima;

import io.github.ocelot.modelanima.api.common.animation.AnimatedEntity;
import io.github.ocelot.modelanima.api.common.animation.AnimationEffectHandler;
import io.github.ocelot.modelanima.api.common.animation.AnimationState;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * @author Ocelot
 */
public class Yeti extends Monster implements AnimatedEntity
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

    private final AnimationEffectHandler effectHandler;
    private AnimationState animationState;
    private int animationTick;

    public Yeti(EntityType<? extends Monster> type, Level level)
    {
        super(type, level);
        this.xpReward = 50;
        this.effectHandler = new AnimationEffectHandler(this);
        this.animationState = AnimationState.EMPTY;
    }

    public static AttributeSupplier.Builder createAttributes()
    {
        return Pillager.createAttributes().add(Attributes.MAX_HEALTH, 150).add(Attributes.MOVEMENT_SPEED, 0.25).add(Attributes.KNOCKBACK_RESISTANCE, 1.0).add(Attributes.ATTACK_DAMAGE, 6.0);
    }

    @Override
    protected void registerGoals()
    {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(3, new YetiAttackTargetGoal(this, 1.0D, true));
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 15.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 15.0F));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Yeti.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }

    private void createSnowball()
    {
        AnimatedEntity.setAnimation(this, CREATE_SNOWBALLS[this.random.nextInt(CREATE_SNOWBALLS.length)]);
    }

    @Override
    public boolean isImmobile()
    {
        return !this.isNoAnimationPlaying();
    }

    @Override
    public void tick()
    {
        super.tick();
        this.animationTick();

        if (!this.level.isClientSide())
        {
            if (this.isAnimationPlaying(THROW_SNOWBALL) && this.getAnimationTick() == 12)
            {
//                this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 10.0F, 0.1F / (this.random.nextFloat() * 0.4F + 0.8F));
//                GiantSnowball snowball = new GiantSnowball(this.level, this);
//                snowball.shootFromRotation(this, this.xRot + 0.5F, this.yHeadRot, 0.0F, 1.5F, 1.0F);
//                this.level.addFreshEntity(snowball);
            }
        }
        else if (!this.isNoAnimationPlaying())
        {
            for (AnimationState animation : CREATE_SNOWBALLS)
            {
                if (this.isAnimationPlaying(animation))
                {
                    if (this.getAnimationTick() >= 13)
                        for (int i = 0; i < 4; i++)
                            this.level.addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.PACKED_ICE.defaultBlockState()), this.getRandomX(0.5), this.getY(1.0) + this.random.nextFloat() * 2.0, this.getRandomZ(0.5), 0, 0, 0);
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
    public boolean addEffect(MobEffectInstance effect)
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
    public MobType getMobType()
    {
        return MobType.ILLAGER;
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
    public AnimationEffectHandler getAnimationEffects()
    {
        return effectHandler;
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
                this.mob.swing(InteractionHand.MAIN_HAND);
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
