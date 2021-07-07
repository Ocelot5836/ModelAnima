package io.github.ocelot.modelanima.core.common.molang.node;

import io.github.ocelot.modelanima.api.common.molang.MolangExpression;
import io.github.ocelot.modelanima.api.common.molang.MolangRuntime;
import io.github.ocelot.modelanima.core.common.molang.result.MolangNumericalResult;

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
    public MolangExpression.Result resolve(MolangRuntime runtime)
    {
        return new MolangNumericalResult(this.value);
    }

    @Override
    public String toString()
    {
        return Float.toString(this.value);
    }
}
