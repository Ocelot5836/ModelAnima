package io.github.ocelot.modelanima.core.common.molang.node;

import io.github.ocelot.modelanima.api.common.molang.MolangEnvironment;
import io.github.ocelot.modelanima.api.common.molang.MolangException;
import io.github.ocelot.modelanima.api.common.molang.MolangExpression;

/**
 * @author Ocelot
 */
public class MolangThisNode implements MolangExpression
{
    @Override
    public float resolve(MolangEnvironment environment) throws MolangException
    {
        return environment.getThis();
    }

    @Override
    public String toString()
    {
        return "this";
    }
}
