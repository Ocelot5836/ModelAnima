package io.github.ocelot.modelanima.core.common.molang.node;

import io.github.ocelot.modelanima.api.common.molang.*;

/**
 * @author Ocelot
 */
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
    public float resolve(MolangEnvironment environment) throws MolangException
    {
        MolangObject object = environment.get(this.object);
        if (!object.has(this.name + "$"+this.parameters.length))
            throw new IllegalStateException("Unknown function: " + this.object + "." + this.name + "() with " + this.parameters.length + " parameters");
        for (int i = 0; i < this.parameters.length; i++)
            environment.loadParameter(i, this.parameters[i]);
        float result = environment.get(this.object).get(this.name +"$"+ this.parameters.length).resolve(environment);
        environment.clearParameters();
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
