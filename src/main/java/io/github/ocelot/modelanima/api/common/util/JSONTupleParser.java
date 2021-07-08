package io.github.ocelot.modelanima.api.common.util;

import com.google.gson.*;
import io.github.ocelot.modelanima.api.common.molang.MolangCompiler;
import io.github.ocelot.modelanima.api.common.molang.MolangException;
import io.github.ocelot.modelanima.api.common.molang.MolangExpression;
import io.github.ocelot.modelanima.core.common.molang.node.MolangConstantNode;
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
    // TODO implement MoLang expressions

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
            throw new JsonSyntaxException("Expected " + name + " to be a JsonArray or JsonPrimitive, was " + JSONUtils.getType(json));
        if (json.get(name).isJsonPrimitive() && json.getAsJsonPrimitive(name).isString())
            throw new JsonSyntaxException("Molang expressions are not supported");
        if (json.get(name).isJsonArray())
        {
            JsonArray vectorJson = json.getAsJsonArray(name);
            if (vectorJson.size() != 1 && vectorJson.size() != length)
                throw new JsonParseException("Expected 1 or " + length + " " + name + " values, was " + vectorJson.size());

            float[] values = new float[length];
            if (vectorJson.size() == 1)
            {
                Arrays.fill(values, JSONUtils.convertToFloat(vectorJson.get(0), name));
            }
            else
            {
                for (int i = 0; i < values.length; i++)
                {
                    values[i] = JSONUtils.convertToFloat(vectorJson.get(i), name + "[" + i + "]");
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
        throw new JsonSyntaxException("Expected " + name + " to be a JsonArray or JsonPrimitive, was " + JSONUtils.getType(json));
    }

    /**
     * Parses an array of expressions from the specified JSON.
     *
     * @param json         The json to get the values from
     * @param name         The name of the tuple element
     * @param length       The number of values to parse
     * @param defaultValue The default value if not required or <code>null</code> to make it required
     * @return An array of values parsed
     * @throws JsonSyntaxException If there is improper syntax in the JSON structure
     */
    public static MolangExpression[] getExpression(JsonObject json, String name, int length, @Nullable Supplier<MolangExpression[]> defaultValue) throws JsonSyntaxException
    {
        try
        {
            if (!json.has(name) && defaultValue != null)
                return defaultValue.get();
            if (!json.has(name))
                throw new JsonSyntaxException("Expected " + name + " to be a JsonArray or JsonPrimitive, was " + JSONUtils.getType(json));
            if (json.get(name).isJsonArray())
            {
                JsonArray vectorJson = json.getAsJsonArray(name);
                if (vectorJson.size() != 1 && vectorJson.size() != length)
                    throw new JsonParseException("Expected 1 or " + length + " " + name + " values, was " + vectorJson.size());

                MolangExpression[] values = new MolangExpression[length];
                if (vectorJson.size() == 1)
                {
                    JsonElement vectorElement = vectorJson.get(0);
                    if (!vectorElement.isJsonPrimitive())
                        throw new JsonSyntaxException("Expected " + name + " to be a Float or String, was " + JSONUtils.getType(vectorElement));

                    JsonPrimitive vectorPrimitive = vectorElement.getAsJsonPrimitive();
                    if (vectorPrimitive.isString())
                    {
                        Arrays.fill(values, MolangCompiler.compile(vectorPrimitive.getAsString()));
                    }
                    else if (vectorPrimitive.isNumber())
                    {
                        Arrays.fill(values, new MolangConstantNode(vectorPrimitive.getAsFloat()));
                    }
                    else
                    {
                        throw new JsonSyntaxException("Expected " + name + " to be a Float or String, was " + JSONUtils.getType(vectorElement));
                    }
                }
                else
                {
                    for (int i = 0; i < values.length; i++)
                    {
                        JsonElement vectorElement = vectorJson.get(i);
                        if (!vectorElement.isJsonPrimitive())
                            throw new JsonSyntaxException("Expected " + name + "[" + i + "] to be a Float or String, was " + JSONUtils.getType(vectorElement));

                        JsonPrimitive vectorPrimitive = vectorElement.getAsJsonPrimitive();
                        if (vectorPrimitive.isString())
                        {
                            values[i] = MolangCompiler.compile(vectorPrimitive.getAsString());
                        }
                        else if (vectorPrimitive.isNumber())
                        {
                            values[i] = new MolangConstantNode(vectorPrimitive.getAsFloat());
                        }
                        else
                        {
                            throw new JsonSyntaxException("Expected " + name + "[" + i + "] to be a Float or String, was " + JSONUtils.getType(vectorElement));
                        }
                    }
                }

                return values;
            }
            if (json.get(name).isJsonPrimitive())
            {
                JsonPrimitive valuePrimitive = json.getAsJsonPrimitive(name);
                if (valuePrimitive.isNumber())
                {
                    MolangExpression[] values = new MolangExpression[length];
                    Arrays.fill(values, new MolangConstantNode(valuePrimitive.getAsFloat()));
                    return values;
                }
                if (valuePrimitive.isString())
                {
                    MolangExpression[] values = new MolangExpression[length];
                    Arrays.fill(values, MolangCompiler.compile(valuePrimitive.getAsString()));
                    return values;
                }
            }
        }
        catch (MolangException e)
        {
            throw new JsonParseException("Failed to compile MoLang expression", e);
        }
        throw new JsonSyntaxException("Expected " + name + " to be a JsonArray or JsonPrimitive, was " + JSONUtils.getType(json));
    }
}
