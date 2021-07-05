package io.github.ocelot.modelanima.api.common.molang;

public interface MolangExpression
{
    float resolve(Scope scope);

    interface Scope
    {
        void setValue(String name, float value);

        float getValue(String name);

        boolean hasValue(String name);
    }
}
