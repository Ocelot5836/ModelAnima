package io.github.ocelot.modelanima.core.common.molang.object;

import io.github.ocelot.modelanima.api.common.molang.MolangExpression;
import io.github.ocelot.modelanima.api.common.molang.MolangObject;

import java.util.HashMap;
import java.util.Map;

public class MolangVariableStorage implements MolangObject
{
    private final Map<String, MolangExpression> storage;
    private final boolean allowMethods;

    public MolangVariableStorage(boolean allowMethods)
    {
        this.storage = new HashMap<>();
        this.allowMethods = allowMethods;
    }

    @Override
    public void set(String name, MolangExpression value)
    {
        if (!this.allowMethods && value instanceof MolangFunction)
            throw new IllegalStateException("Cannot set functions on objects that do not allow functions");
        this.storage.put(name, value);
    }

    @Override
    public MolangExpression get(String name)
    {
        return this.storage.getOrDefault(name, MolangExpression.ZERO);
    }

    @Override
    public boolean has(String name)
    {
        return this.storage.containsKey(name);
    }
}
