package io.github.ocelot.modelanima.core.common.molang.node;

import io.github.ocelot.modelanima.api.common.molang.MolangException;
import io.github.ocelot.modelanima.api.common.molang.MolangExpression;
import io.github.ocelot.modelanima.api.common.molang.MolangRuntime;

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
    public Result resolve(MolangRuntime runtime) throws MolangException
    {
        return runtime.get(this.object).get(this.name).resolve(runtime);
    }

    @Override
    public String toString()
    {
        return name;
    }
}
