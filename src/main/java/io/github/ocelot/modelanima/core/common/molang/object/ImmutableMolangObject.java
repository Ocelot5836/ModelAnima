package io.github.ocelot.modelanima.core.common.molang.object;

import io.github.ocelot.modelanima.api.common.molang.MolangExpression;
import io.github.ocelot.modelanima.api.common.molang.MolangObject;

public class ImmutableMolangObject implements MolangObject
{
    private final MolangObject parent;

    public ImmutableMolangObject(MolangObject parent)
    {
        this.parent = parent;
    }

    @Override
    public void set(String name, MolangExpression value)
    {
        throw new UnsupportedOperationException("Cannot set values on an immutable object");
    }

    @Override
    public MolangExpression get(String name)
    {
        return this.parent.get(name);
    }

    @Override
    public boolean has(String name)
    {
        return this.parent.has(name);
    }
}
