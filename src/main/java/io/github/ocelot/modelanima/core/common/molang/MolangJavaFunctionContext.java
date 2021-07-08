package io.github.ocelot.modelanima.core.common.molang;

import io.github.ocelot.modelanima.api.common.molang.MolangEnvironment;
import io.github.ocelot.modelanima.api.common.molang.MolangException;
import io.github.ocelot.modelanima.api.common.molang.MolangExpression;
import io.github.ocelot.modelanima.api.common.molang.MolangJavaFunction;

/**
 * @author Ocelot
 */
public class MolangJavaFunctionContext implements MolangJavaFunction.Context
{
    private final MolangEnvironment environment;
    private final MolangExpression[] parameters;

    public MolangJavaFunctionContext(MolangEnvironment environment, MolangExpression[] parameters)
    {
        this.environment = environment;
        this.parameters = parameters;
    }

    @Override
    public MolangExpression get(int parameter) throws MolangException
    {
        if (parameter < 0 || parameter >= this.parameters.length)
            throw new MolangException("Invalid parameter: " + parameter);
        return this.parameters[parameter];
    }

    @Override
    public float resolve(int parameter) throws MolangException
    {
        return this.get(parameter).resolve(this.environment);
    }

    @Override
    public int getParameters()
    {
        return this.parameters.length;
    }
}
