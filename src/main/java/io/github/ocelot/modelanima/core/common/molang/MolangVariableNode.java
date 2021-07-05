package io.github.ocelot.modelanima.core.common.molang;

import io.github.ocelot.modelanima.api.common.molang.MolangExpression;

/**
 * @author Ocelot
 */
public class MolangVariableNode implements MolangExpression
{
    private final String name;

    public MolangVariableNode(String name)
    {
        this.name = name;
    }

    @Override
    public float resolve(Scope scope)
    {
        return scope.getValue(this.name);
    }

    @Override
    public String toString()
    {
        return name;
    }
}
