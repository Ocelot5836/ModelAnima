package io.github.ocelot.modelanima.api.common.molang;

import io.github.ocelot.modelanima.core.common.molang.scope.MolangImmutableScope;
import io.github.ocelot.modelanima.core.common.molang.scope.SimpleMolangScope;

public class MolangGlobalScopeBuilder
{
    private final MolangExpression.Scope scope;

    public MolangGlobalScopeBuilder()
    {
        this.scope = new SimpleMolangScope();
    }

    public void setValue(String name, float value)
    {
        this.scope.setValue("global." + name, value);
    }

    public MolangExpression.Scope create()
    {
        return new MolangImmutableScope(this.scope);
    }
}
