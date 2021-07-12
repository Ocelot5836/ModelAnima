package io.github.ocelot.modelanima.api.common.animation;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

/**
 * <p>An implementation of {@link AnimatedEntity} for basic entities.</p>
 *
 * @author Ocelot
 * @since 1.0.0
 */
public abstract class AnimatedPathfinder extends CreatureEntity implements AnimatedEntity
{
    private AnimationState animationState;
    private int animationTick;

    protected AnimatedPathfinder(EntityType<? extends CreatureEntity> entityType, World level)
    {
        super(entityType, level);
        this.animationState = AnimationState.EMPTY;
    }

    @Override
    public int getAnimationTick()
    {
        return animationTick;
    }

    @Override
    public AnimationState getAnimationState()
    {
        return animationState;
    }

    @Override
    public void setAnimationTick(int tick)
    {
        this.animationTick = tick;
    }

    @Override
    public void setAnimationState(AnimationState state)
    {
        this.onAnimationStop(this.animationState);
        this.animationState = state;
        this.setAnimationTick(0);
    }
}
