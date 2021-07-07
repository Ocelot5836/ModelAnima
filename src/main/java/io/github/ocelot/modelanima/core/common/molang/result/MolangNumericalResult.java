package io.github.ocelot.modelanima.core.common.molang.result;

import io.github.ocelot.modelanima.api.common.molang.MolangExpression;

/**
 * @author Ocelot
 */
public class MolangNumericalResult implements MolangExpression.Result
{
    private final float value;

    public MolangNumericalResult(float value)
    {
        this.value = value;
    }

    @Override
    public float getAsFloat()
    {
        return this.value;
    }

    @Override
    public boolean getAsBoolean()
    {
        return this.value != 0;
    }

    @Override
    public String getAsString()
    {
        return Float.toString(this.value);
    }

    @Override
    public boolean isNumeric()
    {
        return true;
    }

    @Override
    public boolean isString()
    {
        return false;
    }

    @Override
    public String toString()
    {
        return this.getAsString();
    }
}
