package io.github.ocelot.modelanima.api.common.molang;

import io.github.ocelot.modelanima.core.common.molang.node.MolangConstantNode;

/**
 * <p>A math expression that can be reduced using a {@link MolangRuntime}.</p>
 *
 * @author Ocelot
 * @since 1.0.0
 */
public interface MolangExpression
{
    MolangExpression ZERO = new MolangConstantNode(0);

    /**
     * Resolves the float value of this runtime.
     *
     * @param runtime The runtime to execute in
     * @return The resulting value
     * @throws MolangException If any error occurs when resolving the value
     */
    Result resolve(MolangRuntime runtime) throws MolangException;

    interface Result
    {
        float getAsFloat();

        boolean getAsBoolean();

        String getAsString();

        boolean isNumeric();

        boolean isString();
    }
}
