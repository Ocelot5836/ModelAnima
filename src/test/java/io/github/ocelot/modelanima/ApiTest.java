package io.github.ocelot.modelanima;

import io.github.ocelot.modelanima.api.common.animation.AnimationLoader;
import io.github.ocelot.modelanima.api.common.geometry.GeometryModelLoader;

import java.util.Arrays;

public class ApiTest
{
    public static void main(String[] args) throws Exception
    {
        String model = "{\n" +
                "  \"format_version\": \"1.12.0\",\n" +
                "  \"minecraft:geometry\": [\n" +
                "    {\n" +
                "      \"description\": {\n" +
                "        \"identifier\": \"geometry.button\",\n" +
                "        \"texture_width\": 32,\n" +
                "        \"texture_height\": 32,\n" +
                "        \"visible_bounds_width\": 4,\n" +
                "        \"visible_bounds_height\": 2.5,\n" +
                "        \"visible_bounds_offset\": [0, 0.75, 0]\n" +
                "      },\n" +
                "      \"bones\": [\n" +
                "        {\n" +
                "          \"name\": \"bone\",\n" +
                "          \"pivot\": [0, 0, 0],\n" +
                "          \"cubes\": [\n" +
                "            {\n" +
                "              \"origin\": [-11, 0.6, 5],\n" +
                "              \"size\": [6, 3, 6],\n" +
                "              \"uv\": {\n" +
                "                \"north\": {\n" +
                "                  \"uv\": [0, 16],\n" +
                "                  \"uv_size\": [6, 3]\n" +
                "                },\n" +
                "                \"east\": {\n" +
                "                  \"uv\": [0, 16],\n" +
                "                  \"uv_size\": [6, 3]\n" +
                "                },\n" +
                "                \"south\": {\n" +
                "                  \"uv\": [0, 16],\n" +
                "                  \"uv_size\": [6, 3]\n" +
                "                },\n" +
                "                \"west\": {\n" +
                "                  \"uv\": [0, 16],\n" +
                "                  \"uv_size\": [6, 3]\n" +
                "                },\n" +
                "                \"up\": {\n" +
                "                  \"uv\": [12, 22],\n" +
                "                  \"uv_size\": [-6, -6]\n" +
                "                },\n" +
                "                \"down\": {\n" +
                "                  \"uv\": [6, 25],\n" +
                "                  \"uv_size\": [-6, -6]\n" +
                "                }\n" +
                "              }\n" +
                "            },\n" +
                "            {\n" +
                "              \"origin\": [-13, 0.6, 13],\n" +
                "              \"size\": [10, 10, 6],\n" +
                "              \"pivot\": [-8, 0.6, 13],\n" +
                "              \"rotation\": [22.5, 0, 0],\n" +
                "              \"uv\": {\n" +
                "                \"north\": {\n" +
                "                  \"uv\": [16, 16],\n" +
                "                  \"uv_size\": [10, 10]\n" +
                "                },\n" +
                "                \"east\": {\n" +
                "                  \"uv\": [16, 0],\n" +
                "                  \"uv_size\": [10, 6]\n" +
                "                },\n" +
                "                \"south\": {\n" +
                "                  \"uv\": [16, 6],\n" +
                "                  \"uv_size\": [10, 10]\n" +
                "                },\n" +
                "                \"west\": {\n" +
                "                  \"uv\": [16, 0],\n" +
                "                  \"uv_size\": [10, 6]\n" +
                "                },\n" +
                "                \"up\": {\n" +
                "                  \"uv\": [26, 6],\n" +
                "                  \"uv_size\": [-10, -6]\n" +
                "                },\n" +
                "                \"down\": {\n" +
                "                  \"uv\": [26, 6],\n" +
                "                  \"uv_size\": [-10, -6]\n" +
                "                }\n" +
                "              }\n" +
                "            },\n" +
                "            {\n" +
                "              \"origin\": [-16, 0, 0],\n" +
                "              \"size\": [16, 0.6, 16],\n" +
                "              \"uv\": {\n" +
                "                \"north\": {\n" +
                "                  \"uv\": [0, 1],\n" +
                "                  \"uv_size\": [16, -1]\n" +
                "                },\n" +
                "                \"east\": {\n" +
                "                  \"uv\": [0, 1],\n" +
                "                  \"uv_size\": [16, -1]\n" +
                "                },\n" +
                "                \"south\": {\n" +
                "                  \"uv\": [0, 1],\n" +
                "                  \"uv_size\": [16, -1]\n" +
                "                },\n" +
                "                \"west\": {\n" +
                "                  \"uv\": [0, 1],\n" +
                "                  \"uv_size\": [16, -1]\n" +
                "                },\n" +
                "                \"up\": {\n" +
                "                  \"uv\": [16, 0],\n" +
                "                  \"uv_size\": [-16, 16]\n" +
                "                },\n" +
                "                \"down\": {\n" +
                "                  \"uv\": [16, 0],\n" +
                "                  \"uv_size\": [-16, 16]\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          ]\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
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
