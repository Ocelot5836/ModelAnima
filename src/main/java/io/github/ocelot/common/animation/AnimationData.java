package io.github.ocelot.common.animation;

import net.minecraft.util.math.vector.Vector3f;

public class AnimationData
{
    public static class AnimationSet {
        private final String name;
        private final double animationLength;
    }

    public static class Keyframe {
        private final Bone[] bones;
        private final double time;
    }

    public static class Bone {
        private final String name;
        private final Vector3f position;
        private final Vector3f rotation;
        private final Vector3f scale;
    }
}
