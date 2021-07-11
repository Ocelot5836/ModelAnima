package io.github.ocelot.modelanima.core.common.molang.node;

import com.google.common.base.Suppliers;
import io.github.ocelot.modelanima.api.common.molang.MolangEnvironment;
import io.github.ocelot.modelanima.api.common.molang.MolangException;
import io.github.ocelot.modelanima.api.common.molang.MolangExpression;

import java.util.function.Supplier;

/**
 * @author Ocelot
 */
public class MolangLazyNode implements MolangExpression
{
    private final Supplier<MolangExpression> parent;

    public MolangLazyNode(Supplier<MolangExpression> parent)
    {
        this.parent = Suppliers.memoize(parent::get);
    }

    @Override
    public float resolve(MolangEnvironment environment) throws MolangException
    {
        return this.parent.get().resolve(environment);
    }

    @Override
    public String toString()
    {
        return String.valueOf(this.parent.get());
    }
}
