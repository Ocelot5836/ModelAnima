package io.github.ocelot.modelanima.common.animation;

import com.google.gson.*;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.math.vector.Vector3f;

public class AnimationData
{
    private final AnimationSet[] animations;

    public AnimationData(AnimationSet[] animations)
    {
        this.animations = animations;
    }

    public AnimationData()
    {
        this(new AnimationSet[0]);
    }

    public static class AnimationSet
    {
        private final String name;
        private final double animationLength;
        private final Keyframe[] keyframes;

        public AnimationSet(String name, double animationLength, Keyframe[] keyframes)
        {
            this.name = name;
            this.animationLength = animationLength;
            this.keyframes = keyframes;
        }
    }

    public static class Keyframe
    {
        private final Bone[] bones;
        private final SoundEffect[] soundEffects;
        private final ParticleEffect[] particleEffects;
        private final double time;

        public Keyframe(Bone[] bones, SoundEffect[] soundEffects, ParticleEffect[] particleEffects, double time)
        {
            this.bones = bones;
            this.soundEffects = soundEffects;
            this.particleEffects = particleEffects;
            this.time = time;
        }
    }

    public static class Bone
    {
        private final String name;
        private final Vector3f position;
        private final Vector3f rotation;
        private final Vector3f scale;

        public Bone(String name, Vector3f position, Vector3f rotation, Vector3f scale)
        {
            this.name = name;
            this.position = position;
            this.rotation = rotation;
            this.scale = scale;
        }
    }

    /**
     * <p>A sound event that plays during a key frame.</p>
     *
     * @author Ocelot
     * @since 1.0.0
     */
    public static class SoundEffect
    {
        private final String effect;

        public SoundEffect(String effect)
        {
            this.effect = effect;
        }

        /**
         * @return The sound event name that should play
         */
        public String getEffect()
        {
            return effect;
        }
    }

    /**
     * <p>A particle effect that spawns during a key frame.</p>
     *
     * @author Ocelot
     * @since 1.0.0
     */
    public static class ParticleEffect
    {
        private final String effect;
        private final String locator;

        public ParticleEffect(String effect, String locator)
        {
            this.effect = effect;
            this.locator = locator;
        }

        /**
         * @return The particle name that should be spawned
         */
        public String getEffect()
        {
            return effect;
        }

        /**
         * @return The name of the locator to place the particle at
         */
        public String getLocator()
        {
            return locator;
        }
    }

    private static Vector3f parseVector(JsonObject json, String name, boolean required) throws JsonSyntaxException
    {
        if (!json.has(name) && !required)
            return new Vector3f();
        if (!json.has(name) || !json.get(name).isJsonArray())
            throw new JsonSyntaxException("Expected '" + name + "' as an array");

        JsonArray vectorJson = json.getAsJsonArray(name);
        if (vectorJson.size() != 3)
            throw new JsonParseException("Expected 3 " + name + " values, found: " + vectorJson.size());

        float[] values = new float[3];
        for (int i = 0; i < values.length; i++)
            values[i] = JSONUtils.getFloat(vectorJson.get(i), name + "[" + i + "]");

        return new Vector3f(values);
    }
}
