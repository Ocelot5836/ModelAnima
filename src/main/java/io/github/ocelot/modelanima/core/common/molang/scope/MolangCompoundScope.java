package io.github.ocelot.modelanima.core.common.molang.scope;

import io.github.ocelot.modelanima.api.common.molang.MolangExpression;

public class MolangCompoundScope implements MolangExpression.Scope
{
    private final MolangExpression.Scope globalScope;
    private final MolangExpression.Scope variableScope;

    public MolangCompoundScope(MolangExpression.Scope globalScope)
    {
        this.globalScope = globalScope;
        this.variableScope = new SimpleMolangScope();
    }

    @Override
    public void setValue(String name, float value)
    {
        if (this.globalScope.hasValue(name))
            throw new RuntimeException("Cannot overwrite global variables. " + name);
        this.variableScope.setValue(name, value);
    }

    @Override
    public float getValue(String name)
    {
        if (this.globalScope.hasValue(name))
            return this.globalScope.getValue(name);
        if (this.variableScope.hasValue(name))
            return this.variableScope.getValue(name);
        return 0.0F;
    }

    @Override
    public boolean hasValue(String name)
    {
        return this.globalScope.hasValue(name) || this.variableScope.hasValue(name);
    }

}
