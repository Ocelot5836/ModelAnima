package io.github.ocelot.modelanima.core.common.molang.scope;

import io.github.ocelot.modelanima.api.common.molang.MolangExpression;

import java.util.HashMap;
import java.util.Map;

public class SimpleMolangScope implements MolangExpression.Scope
{
    private final Map<String, Float> variables;

    public SimpleMolangScope()
    {
        this.variables = new HashMap<>();
    }

    @Override
    public void setValue(String name, float value)
    {
        this.variables.put(name, value);
    }

    @Override
    public float getValue(String name)
    {
        return this.variables.getOrDefault(name, 0.0F);
    }

    @Override
    public boolean hasValue(String name)
    {
        return this.variables.containsKey(name);
    }

}
