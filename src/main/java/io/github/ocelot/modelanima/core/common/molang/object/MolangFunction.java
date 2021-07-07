package io.github.ocelot.modelanima.core.common.molang.object;

import io.github.ocelot.modelanima.api.common.molang.MolangExpression;
import io.github.ocelot.modelanima.api.common.molang.MolangJavaFunction;
import io.github.ocelot.modelanima.api.common.molang.MolangRuntime;

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
    public float resolve(MolangRuntime runtime)
    {
        float[] parameters = new float[this.params];
        for (int i = 0; i < parameters.length; i++)
        {
            if (!runtime.hasParameter(i))
                throw new IllegalStateException("Function requires " + parameters.length + " parameters");
            parameters[i] = runtime.getParameter(i).resolve(runtime);
        }
        return this.consumer.resolve(parameters);
    }
}
