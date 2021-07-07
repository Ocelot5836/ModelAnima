package io.github.ocelot.modelanima.core.common.molang.node;

import io.github.ocelot.modelanima.api.common.molang.MolangExpression;
import io.github.ocelot.modelanima.api.common.molang.MolangRuntime;

/**
 * @author Ocelot
 */
public class MolangConstantNode implements MolangExpression
{
    private final float value;

    public MolangConstantNode(float value)
    {
        this.value = value;
    }

    @Override
    public float resolve(MolangRuntime runtime)
    {
        return this.value;
    }

    @Override
    public String toString()
    {
        return Float.toString(this.value);
    }
}
