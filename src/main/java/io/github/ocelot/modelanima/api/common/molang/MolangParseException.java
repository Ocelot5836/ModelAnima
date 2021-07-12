package io.github.ocelot.modelanima.api.common.molang;

/**
 * <p>Thrown when any exception occurs when parsing a MoLang expression.</p>
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