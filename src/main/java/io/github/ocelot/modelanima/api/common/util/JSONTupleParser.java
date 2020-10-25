package io.github.ocelot.modelanima.api.common.util;

import com.google.gson.*;
import net.minecraft.util.JSONUtils;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.function.Supplier;

/**
 * <p>Parses tuple values from JSON.</p>
 *
 * @author Ocelot
 * @since 1.0.0
 */
public class JSONTupleParser
{
    /**
     * Parses an array of floats from the specified JSON.
     *
     * @param json         The json to get the values from
     * @param name         The name of the tuple element
     * @param length       The number of values to parse
     * @param defaultValue The default value if not required or <code>null</code> to make it required
     * @return An array of values parsed
     * @throws JsonSyntaxException If there is improper syntax in the JSON structure
     */
    public static float[] getFloat(JsonObject json, String name, int length, @Nullable Supplier<float[]> defaultValue) throws JsonSyntaxException
    {
        if (!json.has(name) && defaultValue != null)
            return defaultValue.get();
        if (!json.has(name))
            throw new JsonSyntaxException("Expected " + name + " to be a JsonArray or JsonPrimitive, was " + JSONUtils.toString(json));
        if (json.get(name).isJsonPrimitive() && json.getAsJsonPrimitive(name).isString()) // TODO add Molang support
            throw new JsonSyntaxException("Molang expressions are not supported");
        if (json.get(name).isJsonArray())
        {
            JsonArray vectorJson = json.getAsJsonArray(name);
            if (vectorJson.size() != 1 && vectorJson.size() != length)
                throw new JsonParseException("Expected 1 or " + length + " " + name + " values, was " + vectorJson.size());

            float[] values = new float[length];
            if (vectorJson.size() == 1)
            {
                Arrays.fill(values, JSONUtils.getFloat(vectorJson.get(0), name));
            }
            else
            {
                for (int i = 0; i < values.length; i++)
                {
                    values[i] = JSONUtils.getFloat(vectorJson.get(i), name + "[" + i + "]");
                }
            }

            return values;
        }
        if (json.get(name).isJsonPrimitive())
        {
            JsonPrimitive valuePrimitive = json.getAsJsonPrimitive(name);
            if (valuePrimitive.isNumber())
            {
                float[] values = new float[length];
                Arrays.fill(values, valuePrimitive.getAsFloat());
                return values;
            }
        }
        throw new JsonSyntaxException("Expected " + name + " to be a JsonArray or JsonPrimitive, was " + JSONUtils.toString(json));
    }
}
