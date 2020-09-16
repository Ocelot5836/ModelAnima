package io.github.ocelot.modelanima;

import io.github.ocelot.modelanima.common.animation.AnimationData;
import io.github.ocelot.modelanima.common.animation.AnimationLoader;
import io.github.ocelot.modelanima.common.geometry.GeometryModelData;
import io.github.ocelot.modelanima.common.geometry.GeometryModelLoader;

import java.util.Arrays;

public class ApiTest
{
    public static void main(String[] args) throws Exception
    {
        AnimationData[] animations= AnimationLoader.parse("{\"format_version\":\"1.8.0\",\"animations\":{\"floss\":{\"loop\":true,\"bones\":{\"body\":{\"scale\":{\"0.0\":[1,1,1],\"0.36\":[1,1,3],\"0.8\":[1,1,1]}},\"leftWing\":{\"rotation\":{\"0.0\":[-45,0,-70],\"0.52\":[0,0,90],\"1.0\":[-45,0,-70]}},\"rightWing\":{\"rotation\":{\"0.0\":[0,0,-90],\"0.52\":[-45,0,70],\"1.0\":[0,0,-90]}},\"tail\":{\"rotation\":{\"0.0\":[0,0,0],\"0.52\":[180,0,0],\"1.28\":[360,0,0]}},\"beak\":{\"position\":{\"0.0\":[0,0,0],\"0.52\":[0,0,-7],\"1.28\":[0,0,0]}}},\"sound_effects\":{\"0.24\":{\"effect\":\"block.anvil.place\"}}},\"dance\":{\"loop\":\"hold_on_last_frame\",\"animation_length\":1.68,\"bones\":{\"head\":{\"position\":{\"0.0\":[0,0,0],\"0.56\":[-6,0,0],\"1.32\":[6,0,0],\"1.68\":[0,0,0]}}},\"particle_effects\":{\"0.56\":{\"effect\":\"explosion\",\"locator\":\"fart\"}}},\"test\":{\"animation_length\":1.76,\"bones\":{\"head\":{\"rotation\":{\"0.0\":[0,0,0],\"1.76\":[62.5,0,0]}}}}}}");

        System.out.println(Arrays.toString(animations));
    }
}
