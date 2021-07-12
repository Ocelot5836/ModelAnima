package io.github.ocelot.modelanima.api.common.molang;

import io.github.ocelot.modelanima.core.common.molang.node.MolangConstantNode;
import io.github.ocelot.modelanima.core.common.molang.node.MolangLazyNode;
import io.github.ocelot.modelanima.core.common.molang.object.ImmutableMolangObject;
import io.github.ocelot.modelanima.core.common.molang.object.MolangFunction;
import io.github.ocelot.modelanima.core.common.molang.object.MolangMath;
import io.github.ocelot.modelanima.core.common.molang.object.MolangVariableStorage;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

/**
 * <p>The runtime for MoLang to create and access data from.</p>
 *
 * @author Ocelot
 * @since 1.0.0
 */
public class MolangRuntime implements MolangEnvironment
{
    private final float thisValue;
    private final Map<String, MolangObject> objects;
    private final Map<Integer, MolangExpression> parameters;

    private MolangRuntime(float thisValue, MolangObject query, MolangObject global, MolangObject variable)
    {
        this.thisValue = thisValue;
        this.objects = new HashMap<>();
        MolangObject temp = new MolangVariableStorage(false);
        this.objects.put("context", query); // This is static accesses
        this.objects.put("query", query); // This is static accesses
        this.objects.put("math", new MolangMath()); // The MoLang math "library"
        this.objects.put("global", global); // This is parameter access
        this.objects.put("temp", temp); // This is specifically for expression variables
        this.objects.put("variable", variable); // This can be accessed by Java code
        this.objects.put("c", query); // Alias
        this.objects.put("q", query); // Alias
        this.objects.put("t", temp); // Alias
        this.objects.put("v", variable); // Alias
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

    @Override
    public void loadParameter(int index, MolangExpression expression)
    {
        this.parameters.put(index, expression);
    }

    @Override
    public void clearParameters()
    {
        this.parameters.clear();
    }

    @Override
    public float getThis()
    {
        return this.thisValue;
    }

    @Override
    public MolangObject get(String name) throws MolangException
    {
        name = name.toLowerCase(Locale.ROOT);
        if (!this.objects.containsKey(name))
            throw new MolangException("Unknown MoLang object: " + name);
        return this.objects.get(name);
    }

    @Override
    public MolangExpression getParameter(int parameter)
    {
        return this.parameters.getOrDefault(parameter, MolangExpression.ZERO);
    }

    @Override
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

    // TODO docs
    public static class Builder
    {
        private final MolangObject query;
        private final MolangObject global;
        private final MolangObject variable;

        public Builder()
        {
            this.query = new MolangVariableStorage(true);
            this.global = new MolangVariableStorage(true);
            this.variable = new MolangVariableStorage(false);
        }

        public Builder setQuery(String name, float value)
        {
            this.query.set(name, new MolangConstantNode(value));
            return this;
        }

        public Builder setQuery(String name, Supplier<Float> value)
        {
            this.query.set(name, new MolangLazyNode(() -> new MolangConstantNode(value.get())));
            return this;
        }

        public Builder setGlobal(String name, float value)
        {
            this.global.set(name, new MolangConstantNode(value));
            return this;
        }

        public Builder setGlobal(String name, Supplier<Float> value)
        {
            this.global.set(name, new MolangLazyNode(() -> new MolangConstantNode(value.get())));
            return this;
        }

        public Builder setVariable(String name, float value)
        {
            this.variable.set(name, new MolangConstantNode(value)); // Variables are assumed to be used later
            return this;
        }

        public Builder setVariables(MolangVariableProvider provider)
        {
            provider.addMolangVariables(new MolangVariableProvider.Context()
            {
                @Override
                public void add(String name, float value)
                {
                    variable.set(name, new MolangConstantNode(value));
                }

                @Override
                public void remove(String name)
                {
                    variable.set(name, MolangExpression.ZERO);
                }
            }); // Variables are assumed to be used later
            return this;
        }

        public Builder setQuery(String name, int params, MolangJavaFunction function)
        {
            this.query.set(params < 0 ? name : (name + "$" + params), new MolangFunction(params, function));
            return this;
        }

        public Builder setGlobal(String name, int params, MolangJavaFunction function)
        {
            this.global.set(params < 0 ? name : (name + "$" + params), new MolangFunction(params, function));
            return this;
        }

        public MolangRuntime create(float thisValue)
        {
            return new MolangRuntime(thisValue, new ImmutableMolangObject(this.query), new ImmutableMolangObject(this.global), this.variable);
        }

        public MolangObject getQuery()
        {
            return query;
        }

        public MolangObject getGlobal()
        {
            return global;
        }

        public MolangObject getVariable()
        {
            return variable;
        }
    }
}
