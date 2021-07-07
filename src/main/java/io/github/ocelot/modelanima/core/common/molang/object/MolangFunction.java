package io.github.ocelot.modelanima.core.common.molang.object;

import io.github.ocelot.modelanima.api.common.molang.MolangEnvironment;
import io.github.ocelot.modelanima.api.common.molang.MolangException;
import io.github.ocelot.modelanima.api.common.molang.MolangExpression;
import io.github.ocelot.modelanima.api.common.molang.MolangJavaFunction;

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
        float[] parameters = new float[this.params];
        for (int i = 0; i < parameters.length; i++)
        {
            if (!environment.hasParameter(i))
                throw new IllegalStateException("Function requires " + parameters.length + " parameters");
            parameters[i] = environment.getParameter(i).resolve(environment);
        }
        return this.consumer.resolve(i ->
        {
            if (i < 0 || i >= parameters.length)
                return 0F;
            return parameters[i];
        });
    }
}
