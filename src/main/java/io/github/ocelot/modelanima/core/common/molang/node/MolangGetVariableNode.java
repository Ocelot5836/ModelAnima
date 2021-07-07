package io.github.ocelot.modelanima.core.common.molang.node;

import io.github.ocelot.modelanima.api.common.molang.MolangEnvironment;
import io.github.ocelot.modelanima.api.common.molang.MolangException;
import io.github.ocelot.modelanima.api.common.molang.MolangExpression;

/**
 * @author Ocelot
 */
public class MolangGetVariableNode implements MolangExpression
{
    private final String object;
    private final String name;

    public MolangGetVariableNode(String object, String name)
    {
        this.object = object;
        this.name = name;
    }

    @Override
    public float resolve(MolangEnvironment environment) throws MolangException
    {
        return environment.get(this.object).get(this.name).resolve(environment);
    }

    @Override
    public String toString()
    {
        return this.object + "." + this.name;
    }
}
