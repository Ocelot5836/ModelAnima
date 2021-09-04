package io.github.ocelot.modelanima.api.common.animation;

import io.github.ocelot.modelanima.api.client.animation.AnimatedModel;
import io.github.ocelot.modelanima.core.common.network.ModelAnimaMessages;
import io.github.ocelot.modelanima.core.common.network.ClientboundSyncAnimationMessage;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.network.PacketDistributor;

/**
 * <p>Defines an entity as having animations states for animating {@link AnimatedModel}.</p>
 * <p>Structure based on <a href=https://github.com/team-abnormals/abnormals-core/blob/main/src/main/java/com/minecraftabnormals/abnormals_core/core/endimator/entity/IEndimatedEntity.java>IEndimatedEntity</a></p>
 *
 * @author Ocelot
 * @since 1.0.0
 */
public interface AnimatedEntity
{
    /**
     * Increments the animation tick and automatically handles starting and stopping animations.
     */
    default void animationTick()
    {
        if (this.isNoAnimationPlaying())
            return;

        AnimationState animationState = this.getAnimationState();
        int animationTick = this.getAnimationTick();
        if (animationTick == 0)
            this.onAnimationStart(animationState);

        this.setAnimationTick(animationTick + 1);
        if (animationTick >= animationState.getTickDuration() - 1)
        {
            this.onAnimationStop(animationState);
            this.resetAnimationState();
        }
    }

    /**
     * Called when a new animation has just started playing.
     *
     * @param state The animation state playing
     */
    default void onAnimationStart(AnimationState state)
    {
    }

    /**
     * Called just before an animation state completes.
     *
     * @param state The animation state about to finish
     */
    default void onAnimationStop(AnimationState state)
    {
    }

    /**
     * Called to reset the animation state back to the default. By default, this will set the state to {@link AnimationState#EMPTY}.
     */
    default void resetAnimationState()
    {
        this.setAnimationState(AnimationState.EMPTY);
    }

    /**
     * @return The current tick of animation
     */
    int getAnimationTick();

    /**
     * @return The current state of animation
     */
    AnimationState getAnimationState();

    /**
     * @return Whether no animation is currently playing
     */
    default boolean isNoAnimationPlaying()
    {
        return this.getAnimationState() == AnimationState.EMPTY;
    }

    /**
     * Checks to see if the specified animation is playing.
     *
     * @param state The animation state to check
     * @return Whether that state is playing
     */
    default boolean isAnimationPlaying(AnimationState state)
    {
        return this.getAnimationState() == state;
    }

    /**
     * @return All animation states. This is used for syncing the current state with clients
     */
    AnimationState[] getAnimationStates();

    /**
     * Sets the current tick of animation.
     *
     * @param tick The new animation tick
     */
    void setAnimationTick(int tick);

    /**
     * Sets the state of animation and resets the animation ticks.
     *
     * @param state The new animation state
     */
    void setAnimationState(AnimationState state);

    /**
     * Sets the animation for the specified entity on the server side, and syncs with clients.
     *
     * @param entity         The entity to sync the animation of
     * @param animationState The new animation state
     * @param <T>            The type of entity to set the animation state for
     */
    static <T extends Entity & AnimatedEntity> void setAnimation(T entity, AnimationState animationState)
    {
        Level level = entity.level;
        if (level.isClientSide())
            return;
        AnimationState before = entity.getAnimationState();
        entity.setAnimationState(animationState);
        if (before != animationState)
            ModelAnimaMessages.PLAY.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), new ClientboundSyncAnimationMessage(entity));
    }
}
