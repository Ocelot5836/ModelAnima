package io.github.ocelot.modelanima.core.common.molang.object;

import io.github.ocelot.modelanima.api.common.molang.MolangEnvironment;
import io.github.ocelot.modelanima.api.common.molang.MolangException;
import io.github.ocelot.modelanima.api.common.molang.MolangExpression;
import io.github.ocelot.modelanima.api.common.molang.MolangJavaFunction;
import io.github.ocelot.modelanima.core.common.molang.MolangJavaFunctionContext;

/**
 * @author Ocelot
 */
public class MolangFunction implements MolangExpression
{
    private final int params;
    private final MolangJavaFunction consumer;

    public MolangFunction(int params, MolangJavaFunction consumer)
    {
        this.params = params;
        this.consumer = consumer;
    }

    @Override
    public float resolve(MolangEnvironment environment) throws MolangException
    {
        MolangExpression[] parameters = new MolangExpression[this.params];
        for (int i = 0; i < parameters.length; i++)
        {
            if (!environment.hasParameter(i))
                throw new IllegalStateException("Function requires " + parameters.length + " parameters");
            parameters[i] = environment.getParameter(i);
        }
        return this.consumer.resolve(new MolangJavaFunctionContext(environment, parameters));
    }
}
