package io.github.ocelot.modelanima.core.common.molang.node;

import io.github.ocelot.modelanima.api.common.molang.MolangException;
import io.github.ocelot.modelanima.api.common.molang.MolangExpression;
import io.github.ocelot.modelanima.api.common.molang.MolangRuntime;
import io.github.ocelot.modelanima.core.common.molang.result.MolangNumericalResult;

/**
 * @author Ocelot
 */
public class MolangCompoundNode implements MolangExpression
{
    private final MolangExpression[] expressions;

    public MolangCompoundNode(MolangExpression... expressions)
    {
        this.expressions = expressions;
    }

    @Override
    public Result resolve(MolangRuntime runtime) throws MolangException
    {
        for (int i = 0; i < this.expressions.length; i++)
        {
            Result result = this.expressions[i].resolve(runtime);
            if (i >= this.expressions.length - 1) // The last expression is expected to have the `return`
                return result;
        }
        return new MolangNumericalResult(0);
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < this.expressions.length; i++)
        {
            if (i >= this.expressions.length - 1)
                builder.append("return ");
            builder.append(this.expressions[i]);
            builder.append(';');
        }
        return builder.toString();
    }
}
