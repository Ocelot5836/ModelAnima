package io.github.ocelot.modelanima.core.common.molang.scope;

import io.github.ocelot.modelanima.api.common.molang.MolangExpression;

public class MolangImmutableScope implements MolangExpression.Scope
{
    private final MolangExpression.Scope parent;

    public MolangImmutableScope(MolangExpression.Scope parent)
    {
        this.parent = parent;
    }

    @Override
    public void setValue(String name, float value)
    {
        throw new RuntimeException("Cannot set values on an immutable scope");
    }

    @Override
    public float getValue(String name)
    {
        return this.parent.getValue(name);
    }

    @Override
    public boolean hasValue(String name)
    {
        return this.parent.hasValue(name);
    }
}
