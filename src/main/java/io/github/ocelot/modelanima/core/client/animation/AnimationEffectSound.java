package io.github.ocelot.modelanima.core.client.animation;

import io.github.ocelot.modelanima.api.client.animation.AnimationManager;
import io.github.ocelot.modelanima.api.common.animation.AnimatedEntity;
import io.github.ocelot.modelanima.api.common.animation.AnimationData;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;

/**
 * @author Ocelot
 */
@ApiStatus.Internal
public class AnimationEffectSound extends AbstractSoundInstance implements TickableSoundInstance
{
    private final AnimationData animation;
    private final Entity entity;
    private boolean stopped;

    public AnimationEffectSound(ResourceLocation sound, SoundSource source, AnimationData animation, @Nullable Entity entity, float pitch, float volume, boolean loop)
    {
        super(sound, source);
        this.animation = animation;
        this.entity = entity;
        this.pitch = pitch;
        this.volume = volume;
        this.looping = loop;
    }

    private void stop()
    {
        this.stopped = true;
        this.looping = false;
    }

    @Override
    public void tick()
    {
        if (this.entity != null)
        {
            if (this.entity.isAlive())
            {
                if (this.looping && this.entity instanceof AnimatedEntity)
                {
                    ResourceLocation[] animations = ((AnimatedEntity) this.entity).getAnimationState().getAnimations();
                    boolean playing = false;
                    for (ResourceLocation animation : animations)
                        if (this.animation.equals(AnimationManager.getAnimation(animation)))
                            playing = true;
                    if (!playing)
                    {
                        this.stop();
                        return;
                    }
                }
                this.x = this.entity.getX();
                this.y = this.entity.getY();
                this.z = this.entity.getZ();
            }
            else if (this.looping)
            {
                this.stop();
            }
        }
        else if (this.looping)
        {
            this.stop();
        }
    }

    @Override
    public boolean isStopped()
    {
        return stopped;
    }
}
