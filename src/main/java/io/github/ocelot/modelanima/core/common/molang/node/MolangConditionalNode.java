package io.github.ocelot.modelanima.core.common.molang.node;

import io.github.ocelot.modelanima.api.common.molang.MolangException;
import io.github.ocelot.modelanima.api.common.molang.MolangExpression;
import io.github.ocelot.modelanima.api.common.molang.MolangRuntime;

/**
 * @author Ocelot
 */
public class MolangConditionalNode implements MolangExpression
{
    private final MolangExpression condition;
    private final MolangExpression first;
    private final MolangExpression branch;

    public MolangConditionalNode(MolangExpression condition, MolangExpression first, MolangExpression branch)
    {
        this.condition = condition;
        this.first = first;
        this.branch = branch;
    }

    @Override
    public float resolve(MolangRuntime runtime) throws MolangException
    {
        return (this.condition.resolve(runtime) != 0 ? this.first : this.branch).resolve(runtime);
    }

    @Override
    public String toString()
    {
        return this.condition + " ? " + this.first + " : " + this.branch;
    }
}
