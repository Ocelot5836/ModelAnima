package io.github.ocelot.modelanima.core.common.molang.node;

import io.github.ocelot.modelanima.api.common.molang.MolangEnvironment;
import io.github.ocelot.modelanima.api.common.molang.MolangException;
import io.github.ocelot.modelanima.api.common.molang.MolangExpression;
import io.github.ocelot.modelanima.api.common.molang.MolangRuntime;

/**
 * @author Ocelot
 */
public class MolangSetVariableNode implements MolangExpression
{
    private final String object;
    private final String name;
    private final MolangExpression expression;

    public MolangSetVariableNode(String object, String name, MolangExpression expression)
    {
        this.object = object;
        this.name = name;
        this.expression = expression;
    }

    @Override
    public float resolve(MolangEnvironment environment) throws MolangException
    {
        // This evaluates the value before setting it
        environment.get(this.object).set(this.name, new MolangConstantNode(this.expression.resolve(environment)));
        return this.expression.resolve(environment);
    }

    @Override
    public String toString()
    {
        return this.object + "." + this.name + " = " + this.expression;
    }
}
