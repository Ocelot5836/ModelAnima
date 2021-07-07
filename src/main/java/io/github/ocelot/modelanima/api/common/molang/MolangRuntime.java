package io.github.ocelot.modelanima.api.common.molang;

import io.github.ocelot.modelanima.core.common.molang.node.MolangConstantNode;
import io.github.ocelot.modelanima.core.common.molang.object.ImmutableMolangObject;
import io.github.ocelot.modelanima.core.common.molang.object.MolangFunction;
import io.github.ocelot.modelanima.core.common.molang.object.MolangMath;
import io.github.ocelot.modelanima.core.common.molang.object.MolangVariableStorage;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;

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

    public MolangRuntime(MolangObject query, MolangObject global)
    {
        this.objects = new HashMap<>();
        this.objects.put("query", query);
        this.objects.put("math", new MolangMath());
        this.objects.put("global", global);
        this.objects.put("temp", new MolangVariableStorage(false));
        this.objects.put("variable", new MolangVariableStorage(false));
        this.parameters = new Int2ObjectArrayMap<>();
    }

    public void loadParameter(int index, MolangExpression expression)
    {
        this.parameters.put(index, expression);
    }

    public void clearParameters()
    {
        this.parameters.clear();
    }

    public MolangObject get(String name)
    {
        name = name.toLowerCase(Locale.ROOT);
        if (!this.objects.containsKey(name))
            throw new RuntimeException("Unknown MoLang object: " + name);
        return this.objects.get(name);
    }

    public MolangExpression getParameter(int parameter)
    {
        return this.parameters.getOrDefault(parameter, MolangExpression.ZERO);
    }

    public boolean hasParameter(int parameter)
    {
        return this.parameters.containsKey(parameter);
    }

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
