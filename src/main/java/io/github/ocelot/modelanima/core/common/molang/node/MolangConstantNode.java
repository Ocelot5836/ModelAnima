package io.github.ocelot.modelanima.core.common.molang.node;

import io.github.ocelot.modelanima.api.common.molang.MolangEnvironment;
import io.github.ocelot.modelanima.api.common.molang.MolangExpression;

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
    public float resolve(MolangEnvironment environment)
    {
        return value;
    }

    @Override
    public String toString()
    {
        return Float.toString(this.value);
    }
}
