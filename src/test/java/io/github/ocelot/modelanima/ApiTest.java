package io.github.ocelot.modelanima;

import io.github.ocelot.modelanima.api.common.animation.AnimationLoader;
import io.github.ocelot.modelanima.api.common.geometry.GeometryModelLoader;

import java.util.Arrays;

public class ApiTest
{
    public static void main(String[] args) throws Exception
    {
        String model = "{\n" +
                "\t\"format_version\": \"1.12.0\",\n" +
                "\t\"minecraft:geometry\": [\n" +
                "\t\t{\n" +
                "\t\t\t\"description\": {\n" +
                "\t\t\t\t\"identifier\": \"geometry.OwlModel\",\n" +
                "\t\t\t\t\"texture_width\": 44,\n" +
                "\t\t\t\t\"texture_height\": 24,\n" +
                "\t\t\t\t\"visible_bounds_width\": 3,\n" +
                "\t\t\t\t\"visible_bounds_height\": 2.5,\n" +
                "\t\t\t\t\"visible_bounds_offset\": [0, 0.75, 0]\n" +
                "\t\t\t},\n" +
                "\t\t\t\"bones\": [\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"name\": \"lowerBody\",\n" +
                "\t\t\t\t\t\"pivot\": [0, 5, 0],\n" +
                "\t\t\t\t\t\"cubes\": [\n" +
                "\t\t\t\t\t\t{\"origin\": [-3, 1, -2], \"size\": [6, 4, 4], \"uv\": [24, 0]}\n" +
                "\t\t\t\t\t]\n" +
                "\t\t\t\t},\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"name\": \"leftLeg\",\n" +
                "\t\t\t\t\t\"parent\": \"lowerBody\",\n" +
                "\t\t\t\t\t\"pivot\": [0, 0, 0],\n" +
                "\t\t\t\t\t\"cubes\": [\n" +
                "\t\t\t\t\t\t{\"origin\": [-2, 0, -1], \"size\": [2, 1, 2], \"uv\": [24, 8]},\n" +
                "\t\t\t\t\t\t{\"origin\": [-1, 0, -2], \"size\": [1, 1, 1], \"uv\": [32, 8]},\n" +
                "\t\t\t\t\t\t{\"origin\": [-3, 0, -2], \"size\": [1, 1, 1], \"uv\": [32, 8]},\n" +
                "\t\t\t\t\t\t{\"origin\": [-1, 0, 1], \"size\": [1, 1, 1], \"uv\": [32, 8]},\n" +
                "\t\t\t\t\t\t{\"origin\": [-1, 1, 0], \"size\": [1, 3, 1], \"uv\": [36, 8]}\n" +
                "\t\t\t\t\t]\n" +
                "\t\t\t\t},\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"name\": \"rightLeg\",\n" +
                "\t\t\t\t\t\"parent\": \"lowerBody\",\n" +
                "\t\t\t\t\t\"pivot\": [0, 0, 0],\n" +
                "\t\t\t\t\t\"cubes\": [\n" +
                "\t\t\t\t\t\t{\"origin\": [0, 0, -1], \"size\": [2, 1, 2], \"uv\": [24, 8]},\n" +
                "\t\t\t\t\t\t{\"origin\": [2, 0, -2], \"size\": [1, 1, 1], \"uv\": [32, 8]},\n" +
                "\t\t\t\t\t\t{\"origin\": [0, 0, -2], \"size\": [1, 1, 1], \"uv\": [32, 8]},\n" +
                "\t\t\t\t\t\t{\"origin\": [0, 0, 1], \"size\": [1, 1, 1], \"uv\": [32, 8]},\n" +
                "\t\t\t\t\t\t{\"origin\": [0, 1, 0], \"size\": [1, 3, 1], \"uv\": [36, 8]}\n" +
                "\t\t\t\t\t]\n" +
                "\t\t\t\t},\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"name\": \"body\",\n" +
                "\t\t\t\t\t\"parent\": \"lowerBody\",\n" +
                "\t\t\t\t\t\"pivot\": [0, 5, 0],\n" +
                "\t\t\t\t\t\"rotation\": [50.00203, 0, 0],\n" +
                "\t\t\t\t\t\"cubes\": [\n" +
                "\t\t\t\t\t\t{\"origin\": [-3, 2, -2.5], \"size\": [6, 8, 5], \"uv\": [0, 11]}\n" +
                "\t\t\t\t\t]\n" +
                "\t\t\t\t},\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"name\": \"leftWing\",\n" +
                "\t\t\t\t\t\"parent\": \"body\",\n" +
                "\t\t\t\t\t\"pivot\": [-3, 9, 0],\n" +
                "\t\t\t\t\t\"cubes\": [\n" +
                "\t\t\t\t\t\t{\"origin\": [-4, 0, -2], \"size\": [1, 10, 1], \"uv\": [22, 13]},\n" +
                "\t\t\t\t\t\t{\"origin\": [-4, 1, -1], \"size\": [1, 9, 1], \"uv\": [22, 14]},\n" +
                "\t\t\t\t\t\t{\"origin\": [-4, 1, -3], \"size\": [1, 8, 1], \"uv\": [22, 15]},\n" +
                "\t\t\t\t\t\t{\"origin\": [-4, 2, 0], \"size\": [1, 8, 1], \"uv\": [22, 15]},\n" +
                "\t\t\t\t\t\t{\"origin\": [-4, 3, 1], \"size\": [1, 6, 1], \"uv\": [22, 17]},\n" +
                "\t\t\t\t\t\t{\"origin\": [-4, 4, 2], \"size\": [1, 4, 1], \"uv\": [22, 19]}\n" +
                "\t\t\t\t\t]\n" +
                "\t\t\t\t},\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"name\": \"rightWing\",\n" +
                "\t\t\t\t\t\"parent\": \"body\",\n" +
                "\t\t\t\t\t\"pivot\": [3, 9, 0],\n" +
                "\t\t\t\t\t\"cubes\": [\n" +
                "\t\t\t\t\t\t{\"origin\": [3, 0, -2], \"size\": [1, 10, 1], \"uv\": [26, 13]},\n" +
                "\t\t\t\t\t\t{\"origin\": [3, 1, -1], \"size\": [1, 9, 1], \"uv\": [26, 14]},\n" +
                "\t\t\t\t\t\t{\"origin\": [3, 1, -3], \"size\": [1, 8, 1], \"uv\": [26, 15]},\n" +
                "\t\t\t\t\t\t{\"origin\": [3, 2, 0], \"size\": [1, 8, 1], \"uv\": [26, 15]},\n" +
                "\t\t\t\t\t\t{\"origin\": [3, 3, 1], \"size\": [1, 6, 1], \"uv\": [26, 17]},\n" +
                "\t\t\t\t\t\t{\"origin\": [3, 4, 2], \"size\": [1, 4, 1], \"uv\": [26, 19]}\n" +
                "\t\t\t\t\t]\n" +
                "\t\t\t\t},\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"name\": \"tail\",\n" +
                "\t\t\t\t\t\"parent\": \"body\",\n" +
                "\t\t\t\t\t\"pivot\": [0, 2, 2.5],\n" +
                "\t\t\t\t\t\"cubes\": [\n" +
                "\t\t\t\t\t\t{\"origin\": [-2, -1, 2.5], \"size\": [4, 3, 0], \"uv\": [18, 0]},\n" +
                "\t\t\t\t\t\t{\"origin\": [-1, -3, 2.5], \"size\": [2, 2, 0], \"uv\": [18, 3]}\n" +
                "\t\t\t\t\t],\n" +
                "\t\t\t\t\t\"locators\": {\n" +
                "\t\t\t\t\t\t\"fart\": [0, 2.30565, 2.34456]\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t},\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"name\": \"head\",\n" +
                "\t\t\t\t\t\"parent\": \"body\",\n" +
                "\t\t\t\t\t\"pivot\": [0, 10, -0.5],\n" +
                "\t\t\t\t\t\"rotation\": [-50, 0, 0],\n" +
                "\t\t\t\t\t\"cubes\": [\n" +
                "\t\t\t\t\t\t{\"origin\": [-3, 10, -3.5], \"size\": [6, 5, 6], \"uv\": [0, 0]}\n" +
                "\t\t\t\t\t]\n" +
                "\t\t\t\t},\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"name\": \"beak\",\n" +
                "\t\t\t\t\t\"parent\": \"head\",\n" +
                "\t\t\t\t\t\"pivot\": [0, 12, -3],\n" +
                "\t\t\t\t\t\"rotation\": [-39.99818, 0, 0],\n" +
                "\t\t\t\t\t\"cubes\": [\n" +
                "\t\t\t\t\t\t{\"origin\": [-0.5, 10.5, -3.5], \"size\": [1, 1, 1], \"uv\": [0, 0]}\n" +
                "\t\t\t\t\t]\n" +
                "\t\t\t\t}\n" +
                "\t\t\t]\n" +
                "\t\t}\n" +
                "\t]\n" +
                "}";

        String textureTable = "{\n" +
                "  \"texture\": {\n" +
                "    \"type\": \"online\",\n" +
                "    \"texture\": \"https://api.minecraftabnormals.com/game/extra/texture/jub.png\"\n" +
                "  },\n" +
                "  \"texture2\": {\n" +
                "    \"type\": \"online\",\n" +
                "    \"texture\": \"https://api.minecraftabnormals.com/game/extra/texture/jub.png\",\n" +
                "\t\"glowing\": true\n" +
                "  }\n" +
                "}";

        String animation = "{\n" +
                "  \"format_version\": \"1.8.0\",\n" +
                "  \"animations\": {\n" +
                "    \"floss\": {\n" +
                "      \"loop\": true,\n" +
                "      \"bones\": {\n" +
                "        \"body\": {\n" +
                "          \"scale\": {\n" +
                "            \"0.0\": [1, 1, 1],\n" +
                "            \"0.36\": [1, 1, 3],\n" +
                "            \"0.8\": [1, 1, 1]\n" +
                "          }\n" +
                "        },\n" +
                "        \"leftWing\": {\n" +
                "          \"rotation\": {\n" +
                "            \"0.0\": [-45, 0, -70],\n" +
                "            \"0.52\": [0, 0, 90],\n" +
                "            \"1.0\": [-45, 0, -70]\n" +
                "          }\n" +
                "        },\n" +
                "        \"rightWing\": {\n" +
                "          \"rotation\": {\n" +
                "            \"0.0\": [0, 0, -90],\n" +
                "            \"0.52\": [-45, 0, 70],\n" +
                "            \"1.0\": [0, 0, -90]\n" +
                "          }\n" +
                "        },\n" +
                "        \"tail\": {\n" +
                "          \"rotation\": {\n" +
                "            \"0.0\": [0, 0, 0],\n" +
                "            \"0.52\": [180, 0, 0],\n" +
                "            \"1.28\": [360, 0, 0]\n" +
                "          }\n" +
                "        },\n" +
                "        \"beak\": {\n" +
                "          \"position\": {\n" +
                "            \"0.0\": [0, 0, 0],\n" +
                "            \"0.52\": [0, 0, -7],\n" +
                "            \"1.28\": [0, 0, 0]\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      \"sound_effects\": {\n" +
                "        \"0.24\": {\n" +
                "          \"effect\": \"block.anvil.place\"\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"dance\": {\n" +
                "      \"loop\": \"hold_on_last_frame\",\n" +
                "      \"animation_length\": 1.68,\n" +
                "      \"bones\": {\n" +
                "        \"head\": {\n" +
                "          \"position\": {\n" +
                "            \"0.0\": [0, 0, 0],\n" +
                "            \"0.56\": [-6, 0, 0],\n" +
                "            \"1.32\": [6, 0, 0],\n" +
                "            \"1.68\": [0, 0, 0]\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      \"particle_effects\": {\n" +
                "        \"0.56\": {\n" +
                "          \"effect\": \"explosion\",\n" +
                "          \"locator\": \"fart\"\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"sad\": {\n" +
                "      \"loop\": true,\n" +
                "      \"animation_length\": 5.12,\n" +
                "      \"override_previous_animation\": true,\n" +
                "      \"bones\": {\n" +
                "        \"head\": {\n" +
                "          \"rotation\": {\n" +
                "            \"0.0\": [0, 0, 0],\n" +
                "            \"1.76\": [87.5, 0, 0],\n" +
                "            \"2.8\": [-20, 40, -50],\n" +
                "            \"3.92\": [-22.5, -27.5, 37.5],\n" +
                "            \"5.12\": [0, 0, 0]\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";

        System.out.println(GeometryModelLoader.parseModel(model));
        System.out.println(GeometryModelLoader.parseTextures(textureTable));
        System.out.println(Arrays.toString(AnimationLoader.parse(animation)));
    }
}
