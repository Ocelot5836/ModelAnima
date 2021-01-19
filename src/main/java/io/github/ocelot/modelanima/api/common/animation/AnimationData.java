package io.github.ocelot.modelanima.api.common.animation;

import com.google.gson.*;
import io.github.ocelot.modelanima.api.common.util.JSONTupleParser;
import net.minecraft.util.JSONUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * <p>Deserializes custom animations from JSON.</p>
 *
 * @author Ocelot
 * @since 1.0.0
 */
public class AnimationData
{
    private final String name;
    private final Loop loop;
    private final float blendWeight;
    private final float animationLength;
    private final boolean overridePreviousAnimation;
    private final BoneAnimation[] boneAnimations;
    private final SoundEffect[] soundEffects;
    private final ParticleEffect[] particleEffects;

    public AnimationData(String name, Loop loop, float blendWeight, float animationLength, boolean overridePreviousAnimation, BoneAnimation[] boneAnimations, SoundEffect[] soundEffects, ParticleEffect[] particleEffects)
    {
        this.name = name;
        this.loop = loop;
        this.blendWeight = blendWeight;
        this.animationLength = animationLength;
        this.overridePreviousAnimation = overridePreviousAnimation;
        this.boneAnimations = boneAnimations;
        this.soundEffects = soundEffects;
        this.particleEffects = particleEffects;
    }

    /**
     * @return The name of this animation
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return The type of looping this animation uses
     */
    public Loop getLoop()
    {
        return loop;
    }

    /**
     * @return How much this animation should be blended with others
     */
    public float getBlendWeight()
    {
        return blendWeight;
    }

    /**
     * @return The overall length of this animation
     */
    public float getAnimationLength()
    {
        return animationLength;
    }

    /**
     * @return Whether or not all animations leading up to this point should be overridden
     */
    public boolean isOverridePreviousAnimation()
    {
        return overridePreviousAnimation;
    }

    /**
     * @return The set of bones that are animated in this animation
     */
    public BoneAnimation[] getBoneAnimations()
    {
        return boneAnimations;
    }

    /**
     * @return All sounds that should play at their respective times
     */
    public SoundEffect[] getSoundEffects()
    {
        return soundEffects;
    }

    /**
     * @return All particles that should be spawned at their respective times
     */
    public ParticleEffect[] getParticleEffects()
    {
        return particleEffects;
    }

    @Override
    public String toString()
    {
        return "AnimationData{" +
                "name='" + name + '\'' +
                ", loop=" + loop +
                ", blendWeight=" + blendWeight +
                ", animationLength=" + animationLength +
                ", overridePreviousAnimation=" + overridePreviousAnimation +
                ", boneAnimations=" + Arrays.toString(boneAnimations) +
                ", soundEffects=" + Arrays.toString(soundEffects) +
                ", particleEffects=" + Arrays.toString(particleEffects) +
                '}';
    }

    /**
     * <p>A collection of key frames to animate a single bone.</p>
     *
     * @author Ocelot
     * @since 1.0.0
     */
    public static class BoneAnimation
    {
        private final String name;
        private final KeyFrame[] positionFrames;
        private final KeyFrame[] rotationFrames;
        private final KeyFrame[] scaleFrames;

        public BoneAnimation(String name, KeyFrame[] positionFrames, KeyFrame[] rotationFrames, KeyFrame[] scaleFrames)
        {
            this.name = name;
            this.positionFrames = positionFrames;
            this.rotationFrames = rotationFrames;
            this.scaleFrames = scaleFrames;
        }

        /**
         * @return The name of the bone to animate
         */
        public String getName()
        {
            return name;
        }

        /**
         * @return The position channel of key frames
         */
        public KeyFrame[] getPositionFrames()
        {
            return positionFrames;
        }

        /**
         * @return The rotation channel of key frames
         */
        public KeyFrame[] getRotationFrames()
        {
            return rotationFrames;
        }

        /**
         * @return The scale channel of key frames
         */
        public KeyFrame[] getScaleFrames()
        {
            return scaleFrames;
        }

        @Override
        public String toString()
        {
            return "BoneAnimation{" +
                    "name='" + name + '\'' +
                    ", positionFrames=" + Arrays.toString(positionFrames) +
                    ", rotationFrames=" + Arrays.toString(rotationFrames) +
                    ", scaleFrames=" + Arrays.toString(scaleFrames) +
                    '}';
        }
    }

    /**
     * <p>A key frame for a specific channel in an animation.</p>
     *
     * @author Ocelot
     * @since 1.0.0
     */
    public static class KeyFrame
    {
        private final float time;
        private final float transformPreX;
        private final float transformPreY;
        private final float transformPreZ;
        private final float transformPostX;
        private final float transformPostY;
        private final float transformPostZ;

        public KeyFrame(float time, float transformPreX, float transformPreY, float transformPreZ, float transformPostX, float transformPostY, float transformPostZ)
        {
            this.time = time;
            this.transformPreX = transformPreX;
            this.transformPreY = transformPreY;
            this.transformPreZ = transformPreZ;
            this.transformPostX = transformPostX;
            this.transformPostY = transformPostY;
            this.transformPostZ = transformPostZ;
        }

        /**
         * @return The time this frame occur at
         */
        public float getTime()
        {
            return time;
        }

        /**
         * @return The position to use when transitioning to this frame in the x-axis
         */
        public float getTransformPreX()
        {
            return transformPreX;
        }

        /**
         * @return The position to use when transitioning to this frame in the y-axis
         */
        public float getTransformPreY()
        {
            return transformPreY;
        }

        /**
         * @return The position to use when transitioning to this frame in the z-axis
         */
        public float getTransformPreZ()
        {
            return transformPreZ;
        }

        /**
         * @return The position to use when transitioning away from this frame in the x-axis
         */
        public float getTransformPostX()
        {
            return transformPostX;
        }

        /**
         * @return The position to use when transitioning away from this frame in the y-axis
         */
        public float getTransformPostY()
        {
            return transformPostY;
        }

        /**
         * @return The position to use when transitioning away from this frame in the z-axis
         */
        public float getTransformPostZ()
        {
            return transformPostZ;
        }

        @Override
        public String toString()
        {
            return "KeyFrame{" +
                    "time=" + time +
                    ", transformPre=(" + transformPreX + ", " + transformPreY + ", " + transformPreZ + ")" +
                    ", transformPost=" + transformPostX + "," + transformPostY + ", " + transformPostZ + ")" +
                    '}';
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
        private final float time;
        private final String effect;

        public SoundEffect(float time, String effect)
        {
            this.time = time;
            this.effect = effect;
        }

        /**
         * @return The time in seconds this effect plays at
         */
        public float getTime()
        {
            return time;
        }

        /**
         * @return The sound event name that should play
         */
        public String getEffect()
        {
            return effect;
        }

        @Override
        public String toString()
        {
            return "SoundEffect{" +
                    "time=" + time +
                    ", effect='" + effect + '\'' +
                    '}';
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
        private final float time;
        private final String effect;
        private final String locator;

        public ParticleEffect(float time, String effect, String locator)
        {
            this.time = time;
            this.effect = effect;
            this.locator = locator;
        }

        /**
         * @return The time in seconds this effect plays at
         */
        public float getTime()
        {
            return time;
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

        @Override
        public String toString()
        {
            return "ParticleEffect{" +
                    "time=" + time +
                    ", effect='" + effect + '\'' +
                    ", locator='" + locator + '\'' +
                    '}';
        }
    }

    /**
     * <p>The different types of animations looping that can occur.</p>
     *
     * @author Ocelot
     * @since 1.0.0
     */
    public enum Loop
    {
        NONE, LOOP, HOLD_ON_LAST_FRAME;

        public static class Deserializer implements JsonDeserializer<Loop>
        {
            @Override
            public Loop deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
            {
                if (!json.isJsonPrimitive())
                    throw new JsonSyntaxException("Expected Boolean or String, was " + JSONUtils.toString(json));
                if (json.getAsJsonPrimitive().isBoolean())
                    return json.getAsBoolean() ? LOOP : NONE;
                if (json.getAsJsonPrimitive().isString())
                {
                    for (Loop loop : values())
                        if (loop.name().equalsIgnoreCase(json.getAsString()))
                            return loop;
                    throw new JsonSyntaxException("Unsupported loop: " + json.getAsString() + ". Supported loops: " + Arrays.toString(Arrays.stream(values()).map(loop -> loop.name().toLowerCase(Locale.ROOT)).toArray(String[]::new)));
                }
                throw new JsonSyntaxException("Expected Boolean or String, was " + JSONUtils.toString(json));
            }
        }
    }

    public static class Deserializer implements JsonDeserializer<AnimationData[]>
    {
        @Override
        public AnimationData[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            Set<AnimationData> animations = new HashSet<>();

            JsonObject jsonObject = json.getAsJsonObject();
            for (Map.Entry<String, JsonElement> animationEntry : jsonObject.entrySet())
            {
                JsonObject animationObject = animationEntry.getValue().getAsJsonObject();

                /* Parse global animation properties */
                String animationName = animationEntry.getKey();
                Loop loop = animationObject.has("loop") ? context.deserialize(animationObject.get("loop"), Loop.class) : Loop.NONE; // bool
                float blendWeight = JSONUtils.getFloat(animationObject, "blend_weight", 1.0f); // expression TODO Molang
                float animationLength = JSONUtils.getFloat(animationObject, "animation_length", -1); // float
                boolean overridePreviousAnimation = JSONUtils.getBoolean(animationObject, "override_previous_animation", false); // bool
                Set<BoneAnimation> bones = new HashSet<>();
                List<SoundEffect> soundEffects = new ArrayList<>();
                List<ParticleEffect> particleEffects = new ArrayList<>();

                /* Parse Bone Animations */
                List<KeyFrame> positions = new ArrayList<>();
                List<KeyFrame> rotations = new ArrayList<>();
                List<KeyFrame> scales = new ArrayList<>();
                for (Map.Entry<String, JsonElement> boneAnimationEntry : JSONUtils.getJsonObject(animationObject, "bones").entrySet())
                {
                    JsonObject boneAnimationObject = boneAnimationEntry.getValue().getAsJsonObject();

                    parseTransform(positions, boneAnimationObject, "position", () -> new float[3]);
                    parseTransform(rotations, boneAnimationObject, "rotation", () -> new float[3]);
                    parseTransform(scales, boneAnimationObject, "scale", () -> new float[]{1, 1, 1});

                    positions.sort((a, b) -> Float.compare(a.getTime(), b.getTime()));
                    rotations.sort((a, b) -> Float.compare(a.getTime(), b.getTime()));
                    scales.sort((a, b) -> Float.compare(a.getTime(), b.getTime()));
                    bones.add(new BoneAnimation(boneAnimationEntry.getKey(), positions.toArray(new KeyFrame[0]), rotations.toArray(new KeyFrame[0]), scales.toArray(new KeyFrame[0])));

                    positions.clear();
                    rotations.clear();
                    scales.clear();
                }

                /* Parse Effects */
                parseEffect((time, soundEffectObject) -> soundEffects.add(new SoundEffect(time, JSONUtils.getString(soundEffectObject, "effect"))), animationObject, "sound_effects");
                parseEffect((time, particleEffectObject) -> particleEffects.add(new ParticleEffect(time, JSONUtils.getString(particleEffectObject, "effect"), JSONUtils.getString(particleEffectObject, "locator"))), animationObject, "particle_effects");
                soundEffects.sort((a, b) -> Float.compare(a.getTime(), b.getTime()));
                particleEffects.sort((a, b) -> Float.compare(a.getTime(), b.getTime()));

                animations.add(new AnimationData(animationName, loop, blendWeight, animationLength, overridePreviousAnimation, bones.toArray(new BoneAnimation[0]), soundEffects.toArray(new SoundEffect[0]), particleEffects.toArray(new ParticleEffect[0])));
            }

            return animations.toArray(new AnimationData[0]);
        }

        private static void parseEffect(BiConsumer<Float, JsonObject> effectConsumer, JsonObject json, String name)
        {
            if (!json.has(name))
                return;

            for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject(name).entrySet())
            {
                try
                {
                    effectConsumer.accept(Float.parseFloat(entry.getKey()), entry.getValue().getAsJsonObject());
                }
                catch (NumberFormatException e)
                {
                    throw new JsonParseException("Failed to parse " + name + " at time '" + entry.getKey() + "'", e);
                }
            }
        }

        private static void parseTransform(Collection<KeyFrame> frames, JsonObject json, String name, Supplier<float[]> defaultValue) throws JsonParseException
        {
            if (!json.has(name))
                return;

            JsonElement transformJson = json.get(name);
            if (transformJson.isJsonObject())
            {
                for (Map.Entry<String, JsonElement> entry : transformJson.getAsJsonObject().entrySet())
                {
                    try
                    {
                        float time = Float.parseFloat(entry.getKey());
                        if (frames.stream().anyMatch(keyFrame -> keyFrame.getTime() == time))
                            throw new JsonSyntaxException("Duplicate channel time '" + time + "'");

                        Pair<float[], float[]> transform = parseChannel(transformJson.getAsJsonObject(), entry.getKey(), defaultValue);
                        frames.add(new KeyFrame(time, transform.getLeft()[0], transform.getLeft()[1], transform.getLeft()[2], transform.getRight()[0], transform.getRight()[1], transform.getRight()[2]));
                    }
                    catch (NumberFormatException e)
                    {
                        throw new JsonParseException("Invalid keyframe time '" + entry.getKey() + "'", e);
                    }
                }
            }
            else
            {
                float[] values = JSONTupleParser.getFloat(json, name, 3, defaultValue);
                frames.add(new KeyFrame(0, values[0], values[1], values[2], values[0], values[1], values[2]));
            }
        }

        private static Pair<float[], float[]> parseChannel(JsonObject json, String name, Supplier<float[]> defaultValue) throws JsonSyntaxException
        {
            if (!json.has(name) && !json.get(name).isJsonObject() && !json.get(name).isJsonArray())
                throw new JsonSyntaxException("Missing " + name + ", expected to find a JsonObject or JsonArray");

            JsonElement transformationElement = json.get(name);
            if (transformationElement.isJsonObject())
            {
                JsonObject transformationObject = transformationElement.getAsJsonObject();
                return Pair.of(JSONTupleParser.getFloat(transformationObject, "pre", 3, null), JSONTupleParser.getFloat(transformationObject, "post", 3, null));
            }

            float[] transformation = JSONTupleParser.getFloat(json, name, 3, defaultValue);
            return Pair.of(transformation, transformation);
        }
    }
}
