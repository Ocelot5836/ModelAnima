package io.github.ocelot.modelanima.core.common.molang.node;

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
    public float resolve(MolangRuntime runtime) throws MolangException
    {
        // This evaluates the value before setting it
        runtime.get(this.object).set(this.name, new MolangConstantNode(this.expression.resolve(runtime)));
        return this.expression.resolve(runtime);
    }

    @Override
    public String toString()
    {
        return this.object + "." + this.name + " = " + this.expression;
    }
}
