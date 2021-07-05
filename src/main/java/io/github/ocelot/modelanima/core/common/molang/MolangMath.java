package io.github.ocelot.modelanima.core.common.molang;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import io.github.ocelot.modelanima.api.common.molang.MolangExpression;
import net.minecraft.util.math.MathHelper;

import java.util.Locale;

/**
 * @author Ocelot
 */
public class MolangMath implements MolangExpression
{
    private static final Dynamic2CommandExceptionType NOT_ENOUGH_PARAMETERS = new Dynamic2CommandExceptionType((obj, obj2) -> new LiteralMessage("Not enough parameters. Expected at least " + obj + ", got " + obj2));
    private static final Dynamic2CommandExceptionType TOO_MANY_PARAMETERS = new Dynamic2CommandExceptionType((obj, obj2) -> new LiteralMessage("Too many parameters. Expected at most " + obj + ", got " + obj2));

    private final MathFunction function;
    private final MolangExpression[] parameters;

    public MolangMath(MathFunction function, MolangExpression... parameters) throws CommandSyntaxException
    {
        this.function = function;
        this.parameters = parameters;
        if (this.parameters.length < function.parameters)
            throw NOT_ENOUGH_PARAMETERS.create(function.parameters, this.parameters.length);
        if (function.parameters >= 0 && this.parameters.length > function.parameters)
            throw TOO_MANY_PARAMETERS.create(function.parameters, this.parameters.length);
    }

    @Override
    public float resolve(MolangExpression.Scope scope)
    {
        return this.function.operation.apply(this, scope).floatValue();
    }

    public float resolveParameter(int parameter, MolangExpression.Scope scope)
    {
        if (parameter < 0 || parameter >= this.parameters.length)
            throw new RuntimeException("Invalid parameter: " + parameter);
        return this.parameters[parameter].resolve(scope);
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(this.function.name().toLowerCase(Locale.ROOT)).append('(');
        for (int i = 0; i < this.parameters.length; i++)
        {
            builder.append(this.parameters[i].toString());
            if (i < this.parameters.length - 1)
                builder.append(", ");
        }
        return builder.append(')').toString();
    }

    private enum MathFunction
    {
        ABS(1, (node, scope) ->
                MathHelper.abs(node.resolveParameter(0, scope))),
        SIN(1, (node, scope) ->
                MathHelper.sin(node.resolveParameter(0, scope) * (float) (Math.PI / 180))),
        COS(1, (node, scope) ->
                MathHelper.cos(node.resolveParameter(0, scope) * (float) (Math.PI / 180))),
        EXP(1, (node, scope) ->
                Math.exp(node.resolveParameter(0, scope))),
        LN(1, (node, scope) ->
                Math.log(node.resolveParameter(0, scope))),
        POW(2, (node, scope) ->
                Math.pow(node.resolveParameter(0, scope), node.resolveParameter(1, scope))),
        SQRT(1, (node, scope) ->
                MathHelper.sqrt(node.resolveParameter(0, scope))),
        RANDOM(2, (node, scope) ->
        {
            double rand = Math.random();
            float min = node.resolveParameter(0, scope);
            float max = node.resolveParameter(1, scope);
            if (min > max)
                throw new RuntimeException("Invalid random range: " + min + " to " + max);
            return MathHelper.lerp(rand, min, max);
        }),
        CEIL(1, (node, scope) ->
                MathHelper.ceil(node.resolveParameter(0, scope))),
        ROUND(1, (node, scope) ->
                Math.round(node.resolveParameter(0, scope))),
        TRUNC(1, (node, scope) ->
        {
            float value = node.resolveParameter(0, scope);
            if (value < 0)
                return MathHelper.ceil(value);
            if (value > 0)
                return MathHelper.floor(value);
            return value;
        }),
        FLOOR(1, (node, scope) ->
                MathHelper.floor(node.resolveParameter(0, scope))),
        MOD(2, (node, scope) ->
                node.resolveParameter(0, scope) % node.resolveParameter(1, scope)),
        MIN(2, (node, scope) ->
                Math.min(node.resolveParameter(0, scope), node.resolveParameter(1, scope))),
        MAX(2, (node, scope) ->
                Math.max(node.resolveParameter(0, scope), node.resolveParameter(1, scope))),
        CLAMP(3, (node, scope) ->
                MathHelper.clamp(node.resolveParameter(0, scope), node.resolveParameter(1, scope), node.resolveParameter(2, scope))),
        LERP(3, (node, scope) ->
                MathHelper.lerp(node.resolveParameter(2, scope), node.resolveParameter(0, scope), node.resolveParameter(1, scope))),
        LERPROTATE(3, (node, scope) ->
                MathHelper.rotLerp(node.resolveParameter(2, scope), node.resolveParameter(0, scope), node.resolveParameter(1, scope)));

        private final int parameters;
        private final MathOperation operation;

        MathFunction(int parameters, MathOperation operation)
        {
            this.parameters = parameters;
            this.operation = operation;
        }
    }

    interface MathOperation
    {
        Number apply(MolangMath node, MolangExpression.Scope scope) throws RuntimeException;
    }
}
