package io.github.ocelot.modelanima.api.common.geometry;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import io.github.ocelot.modelanima.api.common.geometry.texture.GeometryModelTextureTable;

import java.io.Reader;

/**
 * <p>Helper to read {@link GeometryModelData} from JSON.</p>
 *
 * @author Ocelot
 * @since 1.0.0
 */
public class GeometryModelLoader
{
    private static final String VERSION = "1.12.0";
    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(GeometryModelData.Description.class, new GeometryModelData.Description.Deserializer()).registerTypeAdapter(GeometryModelData.Bone.class, new GeometryModelData.Bone.Deserializer()).registerTypeAdapter(GeometryModelData.Cube.class, new GeometryModelData.Cube.Deserializer()).registerTypeAdapter(GeometryModelTextureTable.class, new GeometryModelTextureTable.Deserializer()).create();

    /**
     * Creates a new geometry model from the specified reader.
     *
     * @param reader The reader to get data from
     * @return A new geometry model from the reader
     */
    public static GeometryModelData parseModel(Reader reader) throws JsonSyntaxException, JsonIOException
    {
        return parseModel(new JsonParser().parse(reader));
    }

    /**
     * Creates a new geometry model from the specified reader.
     *
     * @param reader The reader to get data from
     * @return A new geometry model from the reader
     */
    public static GeometryModelData parseModel(JsonReader reader) throws JsonSyntaxException, JsonIOException
    {
        return parseModel(new JsonParser().parse(reader));
    }

    /**
     * Creates a new geometry model from the specified JSON string.
     *
     * @param json The raw json string
     * @return A new geometry model from the json
     */
    public static GeometryModelData parseModel(String json) throws JsonSyntaxException
    {
        return parseModel(new JsonParser().parse(json));
    }

    /**
     * Creates a new geometry model from the specified JSON element.
     *
     * @param json The parsed json element
     * @return A new geometry model from the json
     */
    public static GeometryModelData parseModel(JsonElement json) throws JsonSyntaxException
    {
        if (!json.getAsJsonObject().get("format_version").getAsString().equals(VERSION))
            throw new JsonSyntaxException("Unsupported model version. Only " + VERSION + " is supported."); // TODO support multiple versions
        return GSON.fromJson(json.getAsJsonObject().getAsJsonArray("minecraft:geometry").get(0), GeometryModelData.class);
    }

    /**
     * Creates a new texture table from the specified reader.
     *
     * @param reader The reader to get data from
     * @return A new texture table from the reader
     */
    public static GeometryModelTextureTable parseTextures(Reader reader) throws JsonSyntaxException, JsonIOException
    {
        return parseTextures(new JsonParser().parse(reader));
    }

    /**
     * Creates a new texture table from the specified reader.
     *
     * @param reader The reader to get data from
     * @return A new texture table from the reader
     */
    public static GeometryModelTextureTable parseTextures(JsonReader reader) throws JsonSyntaxException, JsonIOException
    {
        return parseTextures(new JsonParser().parse(reader));
    }

    /**
     * Creates a new texture table from the specified JSON string.
     *
     * @param json The raw json string
     * @return A new texture table from the json
     */
    public static GeometryModelTextureTable parseTextures(String json) throws JsonSyntaxException
    {
        return parseTextures(new JsonParser().parse(json));
    }

    /**
     * Creates a new texture table from the specified JSON element.
     *
     * @param json The parsed json element
     * @return A new texture table from the json
     */
    public static GeometryModelTextureTable parseTextures(JsonElement json) throws JsonSyntaxException
    {
        return GSON.fromJson(json, GeometryModelTextureTable.class);
    }
}
