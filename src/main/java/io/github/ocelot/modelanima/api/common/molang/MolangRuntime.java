package io.github.ocelot.modelanima.api.common.molang;

import io.github.ocelot.modelanima.core.common.molang.node.MolangConstantNode;
import io.github.ocelot.modelanima.core.common.molang.object.ImmutableMolangObject;
import io.github.ocelot.modelanima.core.common.molang.object.MolangFunction;
import io.github.ocelot.modelanima.core.common.molang.object.MolangMath;
import io.github.ocelot.modelanima.core.common.molang.object.MolangVariableStorage;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * <p>The runtime for MoLang to create and access data from.</p>
 *
 * @author Ocelot
 * @since 1.0.0
 */
public class MolangRuntime
{
    private final Map<String, MolangObject> objects;
    private final Map<Integer, MolangExpression> parameters;

    private MolangRuntime(MolangObject query, MolangObject global)
    {
        this.objects = new HashMap<>();
        this.objects.put("query", query);
        this.objects.put("math", new MolangMath());
        this.objects.put("global", global);
        this.objects.put("temp", new MolangVariableStorage(false));
        this.objects.put("variable", new MolangVariableStorage(false));
        this.parameters = new Int2ObjectArrayMap<>();
    }

    /**
     * @return A dump of all objects stored in the runtime
     */
    public String dump()
    {
        StringBuilder builder = new StringBuilder("==Start MoLang Runtime Dump==\n\n");
        builder.append("==Start Objects==\n");
        for (Map.Entry<String, MolangObject> entry : this.objects.entrySet())
        {
            builder.append(entry.getKey()).append('=').append(entry.getValue()).append('\n');
        }
        builder.deleteCharAt(builder.length() - 2);
        builder.append("==End Objects==\n\n");
        builder.append("==Parameters==\n");
        this.parameters.entrySet().stream().sorted(Comparator.comparingInt(Map.Entry::getKey)).forEach(entry -> builder.append("\tParameter ").append(entry.getKey()).append('=').append(entry.getValue()).append('\n'));
        builder.append("==End Parameters==\n\n");
        builder.append("==End MoLang Runtime Dump==");
        return builder.toString();
    }

    /**
     * Loads a parameter into the specified slot.
     *
     * @param index      The parameter slot to load into
     * @param expression The expression to use as a parameter
     */
    public void loadParameter(int index, MolangExpression expression)
    {
        this.parameters.put(index, expression);
    }

    /**
     * Clears all stored parameters.
     */
    public void clearParameters()
    {
        this.parameters.clear();
    }

    /**
     * Retrieves a {@link MolangObject} by the specified domain name.
     *
     * @param name The name to fetch by, case insensitive
     * @return The object with the name
     * @throws MolangException If the object does not exist
     */
    public MolangObject get(String name) throws MolangException
    {
        name = name.toLowerCase(Locale.ROOT);
        if (!this.objects.containsKey(name))
            throw new MolangException("Unknown MoLang object: " + name);
        return this.objects.get(name);
    }

    /**
     * Retrieves an expression by the specified parameter index.
     *
     * @param parameter The parameter to fetch
     * @return The parameter value or {@link MolangExpression#ZERO} if there is no parameter with that index
     */
    public MolangExpression getParameter(int parameter)
    {
        return this.parameters.getOrDefault(parameter, MolangExpression.ZERO);
    }

    /**
     * Checks to see if a parameter is loaded under the specified index.
     *
     * @param parameter The parameter to check
     * @return Whether or not a parameter is present
     */
    public boolean hasParameter(int parameter)
    {
        return this.parameters.containsKey(parameter);
    }

    /**
     * @return A new runtime builder.
     */
    public static Builder runtime()
    {
        return new Builder();
    }

    public static class Builder
    {
        private final MolangObject query;
        private final MolangObject global;

        public Builder()
        {
            this.query = new MolangVariableStorage(true);
            this.global = new MolangVariableStorage(true);
        }

        public Builder setQuery(String name, float value)
        {
            this.query.set(name, new MolangConstantNode(value));
            return this;
        }

        public Builder setQuery(String name, int params, MolangJavaFunction function)
        {
            this.query.set(name + "$" + params, new MolangFunction(params, function));
            return this;
        }

        public Builder setGlobal(String name, float value)
        {
            this.global.set(name, new MolangConstantNode(value));
            return this;
        }

        public Builder setGlobal(String name, int params, MolangJavaFunction function)
        {
            this.global.set(name + "$" + params, new MolangFunction(params, function));
            return this;
        }

        public MolangRuntime create()
        {
            return new MolangRuntime(new ImmutableMolangObject(this.query), new ImmutableMolangObject(this.global));
        }
    }
}
