package io.github.ocelot.common.geometry;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;

import java.io.Reader;

/**
 * <p>Helper to read {@link GeometryModelData} from JSON.</p>
 *
 * @author Ocelot
 * @since 1.0.0
 */
public class GeometryModelLoader
{
    public static final Gson GSON = new GsonBuilder().registerTypeAdapter(GeometryModelData.Description.class, new GeometryModelData.Description.Deserializer()).registerTypeAdapter(GeometryModelData.Bone.class, new GeometryModelData.Bone.Deserializer()).registerTypeAdapter(GeometryModelData.Cube.class, new GeometryModelData.Cube.Deserializer()).create();

    /**
     * Creates a new bedrock model from the specified reader.
     *
     * @param reader The reader to get data from
     * @return A new bedrock model from the reader
     */
    public static GeometryModelData parse(Reader reader) throws JsonSyntaxException, JsonIOException
    {
        return parse(new JsonParser().parse(reader));
    }

    /**
     * Creates a new bedrock model from the specified reader.
     *
     * @param reader The reader to get data from
     * @return A new bedrock model from the reader
     */
    public static GeometryModelData parse(JsonReader reader) throws JsonSyntaxException, JsonIOException
    {
        return parse(new JsonParser().parse(reader));
    }

    /**
     * Creates a new bedrock model from the specified JSON string.
     *
     * @param json The raw json string
     * @return A new bedrock model from the json
     */
    public static GeometryModelData parse(String json) throws JsonSyntaxException
    {
        return parse(new JsonParser().parse(json));
    }

    /**
     * Creates a new bedrock model from the specified JSON element.
     *
     * @param json The parsed json element
     * @return A new bedrock model from the json
     */
    public static GeometryModelData parse(JsonElement json) throws JsonSyntaxException
    {
        return GSON.fromJson(json.getAsJsonObject().getAsJsonArray("minecraft:geometry").get(0), GeometryModelData.class);
    }
}
