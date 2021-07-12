package io.github.ocelot.modelanima.core.common.geometry;

import com.google.gson.*;
import io.github.ocelot.modelanima.api.common.geometry.GeometryModelData;
import io.github.ocelot.modelanima.api.common.util.JSONTupleParser;
import net.minecraft.util.Direction;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.math.vector.Vector3f;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Ocelot
 */
public class Geometry110Parser
{
    public static GeometryModelData[] parseModel(JsonElement json) throws JsonParseException
    {
        JsonObject jsonObject = json.getAsJsonObject();

        GeometryModelData data = null;
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet())
        {
            if (!entry.getKey().startsWith("geometry."))
                continue;
            if (data != null)
                throw new JsonSyntaxException("1.8.0 does not allow multiple geometry definitions per file.");

            JsonObject object = JSONUtils.convertToJsonObject(entry.getValue(), entry.getKey());

            // Description
            GeometryModelData.Description description = parseDescription(entry.getKey().substring(9), object);

            // Bones
            GeometryModelData.Bone[] bones;
            if (object.has("bones"))
            {
                Set<String> usedNames = new HashSet<>();
                JsonArray bonesJson = JSONUtils.getAsJsonArray(object, "bones");
                bones = new GeometryModelData.Bone[bonesJson.size()];
                for (int j = 0; j < bones.length; j++)
                {
                    bones[j] = parseBone(JSONUtils.convertToJsonObject(bonesJson.get(j), "bones[" + j + "]"));
                    if (!usedNames.add(bones[j].getName()))
                        throw new JsonSyntaxException("Duplicate bone: " + bones[j].getName());
                }
            }
            else
            {
                bones = new GeometryModelData.Bone[0];
            }

            data = new GeometryModelData(description, bones);
        }
        return data != null ? new GeometryModelData[]{data} : new GeometryModelData[0];
    }

    private static GeometryModelData.Description parseDescription(String identifier, JsonObject json) throws JsonParseException
    {
        float visibleBoundsWidth = JSONUtils.getAsFloat(json, "visible_bounds_width", 0);
        float visibleBoundsHeight = JSONUtils.getAsFloat(json, "visible_bounds_height", 0);
        float[] visibleBoundsOffset = JSONTupleParser.getFloat(json, "visible_bounds_offset", 3, () -> new float[3]);
        int textureWidth = JSONUtils.getAsInt(json, "texturewidth", 256);
        int textureHeight = JSONUtils.getAsInt(json, "textureheight", 256);
        boolean preserveModelPose2588 = JSONUtils.getAsBoolean(json, "preserve_model_pose", false);
        if (textureWidth == 0)
            throw new JsonSyntaxException("Texture width must not be zero");
        if (textureHeight == 0)
            throw new JsonSyntaxException("Texture height must not be zero");
        return new GeometryModelData.Description(identifier, visibleBoundsWidth, visibleBoundsHeight, new Vector3f(visibleBoundsOffset), textureWidth, textureHeight, preserveModelPose2588);
    }

    private static GeometryModelData.Bone parseBone(JsonObject json) throws JsonParseException
    {
        String name = JSONUtils.getAsString(json, "name");
        boolean reset2588 = JSONUtils.getAsBoolean(json, "reset", false);
        boolean neverRender2588 = JSONUtils.getAsBoolean(json, "neverrender", false);
        String parent = JSONUtils.getAsString(json, "parent", null);
        float[] pivot = JSONTupleParser.getFloat(json, "pivot", 3, () -> new float[3]);
        float[] rotation = JSONTupleParser.getFloat(json, "rotation", 3, () -> new float[3]);
        boolean mirror = JSONUtils.getAsBoolean(json, "mirror", false);
        float inflate = JSONUtils.getAsFloat(json, "inflate", 0);
        boolean debug = JSONUtils.getAsBoolean(json, "debug", false);

        GeometryModelData.Cube[] cubes = json.has("cubes") ? parseCubes(json) : new GeometryModelData.Cube[0];
        GeometryModelData.Locator[] locators = json.has("locators") ? parseLocators(json) : new GeometryModelData.Locator[0];

        return new GeometryModelData.Bone(name, reset2588, neverRender2588, parent, new Vector3f(pivot), new Vector3f(rotation), new Vector3f(), mirror, inflate, debug, cubes, locators, null);
    }

    private static GeometryModelData.Cube[] parseCubes(JsonObject json)
    {
        JsonArray cubesJson = JSONUtils.getAsJsonArray(json, "cubes");
        GeometryModelData.Cube[] cubes = new GeometryModelData.Cube[cubesJson.size()];
        for (int i = 0; i < cubesJson.size(); i++)
            cubes[i] = parseCube(JSONUtils.convertToJsonObject(cubesJson.get(i), "cubes[" + i + "]"));
        return cubes;
    }

    static GeometryModelData.Locator[] parseLocators(JsonObject json)
    {
        JsonObject locatorsJson = JSONUtils.getAsJsonObject(json, "locators");
        return locatorsJson.entrySet().stream().map(entry ->
        {
            String locatorIdentifier = entry.getKey();
            float[] locatorPosition = JSONTupleParser.getFloat(locatorsJson, locatorIdentifier, 3, () -> new float[3]);
            return new GeometryModelData.Locator(locatorIdentifier, new Vector3f(locatorPosition));
        }).toArray(GeometryModelData.Locator[]::new);
    }

    private static GeometryModelData.Cube parseCube(JsonObject json) throws JsonParseException
    {
        JsonObject cubeJson = json.getAsJsonObject();
        float[] origin = JSONTupleParser.getFloat(cubeJson, "origin", 3, () -> new float[3]);
        float[] size = JSONTupleParser.getFloat(cubeJson, "size", 3, () -> new float[3]);
        float[] rotation = JSONTupleParser.getFloat(cubeJson, "rotation", 3, () -> new float[3]);
        float[] pivot = JSONTupleParser.getFloat(cubeJson, "pivot", 3, () -> new float[]{origin[0] + size[0] / 2F, origin[1] + size[1] / 2F, origin[2] + size[2] / 2F});
        boolean overrideInflate = cubeJson.has("inflate");
        float inflate = JSONUtils.getAsFloat(cubeJson, "inflate", 0);
        boolean overrideMirror = cubeJson.has("mirror");
        boolean mirror = JSONUtils.getAsBoolean(cubeJson, "mirror", false);
        GeometryModelData.CubeUV[] uv = parseUV(cubeJson, size);
        if (uv.length != Direction.values().length)
            throw new JsonParseException("Expected uv to be of size " + Direction.values().length + ", was " + uv.length);
        return new GeometryModelData.Cube(new Vector3f(origin), new Vector3f(size), new Vector3f(rotation), new Vector3f(pivot), overrideInflate, inflate, overrideMirror, mirror, uv);
    }

    static GeometryModelData.CubeUV[] parseUV(JsonObject cubeJson, float[] size)
    {
        if (!cubeJson.has("uv"))
            return new GeometryModelData.CubeUV[6];

        GeometryModelData.CubeUV[] uvs = new GeometryModelData.CubeUV[6];
        float[] uv = JSONTupleParser.getFloat(cubeJson, "uv", 2, () -> new float[2]);
        uvs[Direction.NORTH.get3DDataValue()] = new GeometryModelData.CubeUV(uv[0] + size[2], uv[1] + size[2], size[0], size[1], "texture");
        uvs[Direction.EAST.get3DDataValue()] = new GeometryModelData.CubeUV(uv[0], uv[1] + size[2], size[2], size[1], "texture");
        uvs[Direction.SOUTH.get3DDataValue()] = new GeometryModelData.CubeUV(uv[0] + size[0] + size[2] * 2, uv[1] + size[2], size[0], size[1], "texture");
        uvs[Direction.WEST.get3DDataValue()] = new GeometryModelData.CubeUV(uv[0] + size[0] + size[2], uv[1] + size[2], size[2], size[1], "texture");
        uvs[Direction.UP.get3DDataValue()] = new GeometryModelData.CubeUV(uv[0] + size[2], uv[1], size[0], size[2], "texture");
        uvs[Direction.DOWN.get3DDataValue()] = new GeometryModelData.CubeUV(uv[0] + size[0] + size[2], uv[1], size[0], size[2], "texture");
        return uvs;
    }
}