package io.github.ocelot.modelanima.api.common.molang;

/**
 * <p>.</p>
 *
 * @author Ocelot
 * @since 1.0.0
 */
public class MolangParseException extends MolangException
{
    public MolangParseException(Exception exception)
    {
        super(exception.getMessage());
    }
}
