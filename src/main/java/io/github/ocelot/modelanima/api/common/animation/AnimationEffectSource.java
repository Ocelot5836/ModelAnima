package io.github.ocelot.modelanima.api.common.animation;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * <p>Handles effects played by animations.</p>
 *
 * @since 1.0.0
 */
public interface AnimationEffectSource
{
    /**
     * Called when a sound event should be played.
     *
     * @param animation   The animation the effect is playing for
     * @param soundEffect The effect to play
     */
    @OnlyIn(Dist.CLIENT)
    void handleSoundEffect(AnimationData animation, AnimationData.SoundEffect soundEffect);

    /**
     * Called when a particle effect should be play.
     *
     * @param animation      The animation the effect is playing for
     * @param particleEffect The particle information to play
     * @param xOffset        The x offset of the effect from the origin of the model
     * @param yOffset        The y offset of the effect from the origin of the model
     * @param zOffset        The z offset of the effect from the origin of the model
     */
    @OnlyIn(Dist.CLIENT)
    void handleParticleEffect(AnimationData animation, AnimationData.ParticleEffect particleEffect, double xOffset, double yOffset, double zOffset);

    /**
     * Called when a custom effect is placed on the timeline.
     *
     * @param animation      The animation the effect is playing for
     * @param timelineEffect The effect on the timeline
     */
    @OnlyIn(Dist.CLIENT)
    void handleTimelineEffect(AnimationData animation, AnimationData.TimelineEffect timelineEffect);
}
