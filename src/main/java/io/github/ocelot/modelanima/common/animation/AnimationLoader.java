package io.github.ocelot.modelanima.common.animation;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;

import java.io.Reader;

/**
 * <p>Helper to read {@link AnimationData} from JSON.</p>
 *
 * @author Ocelot
 * @since 1.0.0
 */
public class AnimationLoader
{
    private static final String VERSION = "1.8.0";

    public static final Gson GSON = new GsonBuilder().registerTypeAdapter(AnimationData[].class, new AnimationData.Deserializer()).registerTypeAdapter(AnimationData.Loop.class, new AnimationData.Loop.Deserializer()).create();

    /**
     * Creates a new animation from the specified JSON element.
     *
     * @param reader The reader to get data from
     * @return A new animation from the json
     */
    public static AnimationData[] parse(Reader reader) throws JsonSyntaxException, JsonIOException
    {
        return parse(new JsonParser().parse(reader));
    }

    /**
     * Creates a new animation from the specified JSON element.
     *
     * @param reader The reader to get data from
     * @return A new animation from the json
     */
    public static AnimationData[] parse(JsonReader reader) throws JsonSyntaxException, JsonIOException
    {
        return parse(new JsonParser().parse(reader));
    }

    /**
     * Creates a new animation from the specified JSON element.
     *
     * @param json The raw json string
     * @return A new animation from the json
     */
    public static AnimationData[] parse(String json) throws JsonSyntaxException
    {
        return parse(new JsonParser().parse(json));
    }

    /**
     * Creates a new animation from the specified JSON element.
     *
     * @param json The parsed json element
     * @return A new animation from the json
     */
    public static AnimationData[] parse(JsonElement json) throws JsonSyntaxException
    {
        if (!json.getAsJsonObject().get("format_version").getAsString().equals(VERSION))
            throw new JsonSyntaxException("Unsupported model version. Only " + VERSION + " is supported."); // TODO support multiple versions
        return GSON.fromJson(json.getAsJsonObject().getAsJsonObject("animations"), AnimationData[].class);
    }
}
