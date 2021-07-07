package io.github.ocelot.modelanima.core.common.molang.result;

import io.github.ocelot.modelanima.api.common.molang.MolangExpression;

/**
 * @author Ocelot
 */
public class MolangStringResult implements MolangExpression.Result
{
    private final String value;

    public MolangStringResult(String value)
    {
        this.value = value;
    }

    @Override
    public float getAsFloat()
    {
        throw new UnsupportedOperationException("String cannot be cast to Float");
    }

    @Override
    public boolean getAsBoolean()
    {
        throw new UnsupportedOperationException("String cannot be cast to Boolean");
    }

    @Override
    public String getAsString()
    {
        return value;
    }

    @Override
    public boolean isNumeric()
    {
        return false;
    }

    @Override
    public boolean isString()
    {
        return true;
    }

    @Override
    public String toString()
    {
        return this.getAsString();
    }
}
