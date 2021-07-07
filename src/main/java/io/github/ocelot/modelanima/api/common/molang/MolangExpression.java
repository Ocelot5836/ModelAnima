package io.github.ocelot.modelanima.api.common.molang;

import io.github.ocelot.modelanima.core.common.molang.node.MolangConstantNode;

/**
 * <p></p>
 *
 * @author Ocelot
 * @since 1.0.0
 */
public interface MolangExpression
{
    MolangExpression ZERO = new MolangConstantNode(0);

    float resolve(MolangRuntime runtime);
}
