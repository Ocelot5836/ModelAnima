package io.github.ocelot.modelanima.core.common.molang.node;

import io.github.ocelot.modelanima.api.common.molang.MolangException;
import io.github.ocelot.modelanima.api.common.molang.MolangExpression;
import io.github.ocelot.modelanima.api.common.molang.MolangObject;
import io.github.ocelot.modelanima.api.common.molang.MolangRuntime;

public class MolangInvokeFunctionNode implements MolangExpression
{
    private final String object;
    private final String name;
    private final MolangExpression[] parameters;

    public MolangInvokeFunctionNode(String object, String name, MolangExpression... parameters)
    {
        this.object = object;
        this.name = name;
        this.parameters = parameters;
    }

    @Override
    public float resolve(MolangRuntime runtime) throws MolangException
    {
        MolangObject object = runtime.get(this.object);
        if (!object.has(this.name + "$"+this.parameters.length))
            throw new IllegalStateException("Unknown function: " + this.object + "." + this.name + "() with " + this.parameters.length + " parameters");
        for (int i = 0; i < this.parameters.length; i++)
            runtime.loadParameter(i, this.parameters[i]);
        float result = runtime.get(this.object).get(this.name +"$"+ this.parameters.length).resolve(runtime);
        runtime.clearParameters();
        return result;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(this.object).append('.').append(this.name).append('(');
        for (int i = 0; i < this.parameters.length; i++)
        {
            builder.append(this.parameters[i].toString());
            if (i < this.parameters.length - 1)
                builder.append(", ");
        }
        return builder.append(')').toString();
    }
}
