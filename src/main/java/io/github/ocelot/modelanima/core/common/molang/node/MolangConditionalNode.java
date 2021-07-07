package io.github.ocelot.modelanima.core.common.molang.node;

import io.github.ocelot.modelanima.api.common.molang.MolangEnvironment;
import io.github.ocelot.modelanima.api.common.molang.MolangException;
import io.github.ocelot.modelanima.api.common.molang.MolangExpression;

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
    public float resolve(MolangEnvironment environment) throws MolangException
    {
        return (this.condition.resolve(environment) != 0 ? this.first : this.branch).resolve(environment);
    }

    @Override
    public String toString()
    {
        return this.condition + " ? " + this.first + " : " + this.branch;
    }
}
