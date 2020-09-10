package io.github.ocelot;

import io.github.ocelot.client.geometry.GeometryModelData;
import io.github.ocelot.client.geometry.GeometryModelLoader;

public class ApiTest
{
    public static void main(String[] args) throws Exception
    {
        GeometryModelData model = GeometryModelLoader.parse("{\n" +
                "\t\"format_version\": \"1.12.0\",\n" +
                "\t\"minecraft:geometry\": [{\n" +
                "\t\t\"description\": {\n" +
                "\t\t\t\"identifier\": \"wise_beard\",\n" +
                "\t\t\t\"texture_width\": 64,\n" +
                "\t\t\t\"texture_height\": 32,\n" +
                "\t\t\t\"visible_bounds_width\": 2,\n" +
                "\t\t\t\"visible_bounds_height\": 3.5,\n" +
                "\t\t\t\"visible_bounds_offset\": [0, 1.25, 0]\n" +
                "\t\t},\n" +
                "\t\t\"bones\": [{\n" +
                "\t\t\t\"name\": \"beard1\",\n" +
                "\t\t\t\"part\": \"head\",\n" +
                "\t\t\t\"pivot\": [0, 24, 0],\n" +
                "\t\t\t\"cubes\": [{\n" +
                "\t\t\t\t\"origin\": [-4, 24, -4],\n" +
                "\t\t\t\t\"size\": [8, 8, 8],\n" +
                "\t\t\t\t\"inflate\": 0.6,\n" +
                "\t\t\t\t\"uv\": [0, 0]\n" +
                "\t\t\t}]\n" +
                "\t\t}, {\n" +
                "\t\t\t\"name\": \"beard2\",\n" +
                "\t\t\t\"part\": \"head\",\n" +
                "\t\t\t\"pivot\": [0, 24, 0],\n" +
                "\t\t\t\"cubes\": [{\n" +
                "\t\t\t\t\"origin\": [-4, 13.4, -4.5],\n" +
                "\t\t\t\t\"size\": [8, 10, 1],\n" +
                "\t\t\t\t\"uv\": [0, 16]\n" +
                "\t\t\t}]\n" +
                "\t\t}]\n" +
                "\t}]\n" +
                "}");

        System.out.println(model);
    }
}
