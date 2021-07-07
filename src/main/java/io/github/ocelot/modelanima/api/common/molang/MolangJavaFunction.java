package io.github.ocelot.modelanima.api.common.molang;

import java.util.function.Function;

/**
 * <p>Executes java code from MoLang expressions.</p>
 *
 * @author Ocelot
 * @since 1.0.0
 */
@FunctionalInterface
public interface MolangJavaFunction
{
    /**
     * Resolves a float from a set of parameters.
     *
     * @param parameters The parameters to execute using
     * @return The resulting float value
     * @throws MolangException If any error occurs
     */
    float resolve(Function<Integer, Float> parameters) throws MolangException;
}
