package io.github.ocelot.modelanima.core.common.molang.object;

import io.github.ocelot.modelanima.api.common.molang.MolangExpression;
import io.github.ocelot.modelanima.api.common.molang.MolangJavaFunction;
import io.github.ocelot.modelanima.api.common.molang.MolangObject;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;
import java.util.Locale;

/**
 * @author Ocelot
 */
public class MolangMath implements MolangObject
{
    @Override
    public void set(String name, MolangExpression value)
    {
        throw new UnsupportedOperationException("Cannot set values on the math object");
    }

    @Override
    public MolangExpression get(String name)
    {
        MathFunction function = MathFunction.byName(name);
        return function != null ? function.expression : MolangExpression.ZERO;
    }

    @Override
    public boolean has(String name)
    {
        return MathFunction.byName(name) != null;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder("MoLang Math\n");
        for (MathFunction function : MathFunction.values())
            builder.append('\t').append(function.functionName).append("()").append('\n');
        return builder.toString();
    }

    public enum MathFunction
    {
        ABS(1, parameters ->
                Math.abs(parameters.apply(0))),
        SIN(1, parameters ->
                (float) Math.sin(parameters.apply(0) * Math.PI / 180.0)),
        COS(1, parameters ->
                (float) Math.cos(parameters.apply(0) * Math.PI / 180.0)),
        EXP(1, parameters ->
                (float) Math.exp(parameters.apply(0))),
        LN(1, parameters ->
                (float) Math.log(parameters.apply(0))),
        POW(2, parameters ->
                (float) Math.pow(parameters.apply(0), parameters.apply(1))),
        SQRT(1, parameters ->
                (float) Math.sqrt(parameters.apply(0))),
        RANDOM(2, parameters ->
        {
            double rand = Math.random();
            float min = parameters.apply(0);
            float max = parameters.apply(1);
            if (min > max)
                throw new RuntimeException("Invalid random range: " + min + " to " + max);
            return (float) MathHelper.lerp(rand, min, max);
        }),
        CEIL(1, parameters ->
                (float) Math.ceil(parameters.apply(0))),
        ROUND(1, parameters ->
                (float) Math.round(parameters.apply(0))),
        TRUNC(1, parameters ->
        {
            float value = parameters.apply(0);
            if (value < 0)
                return (float) Math.ceil(value);
            if (value > 0)
                return (float) Math.floor(value);
            return value;
        }),
        FLOOR(1, parameters ->
                (float) Math.floor(parameters.apply(0))),
        MOD(2, parameters ->
                parameters.apply(0) % parameters.apply(1)),
        MIN(2, parameters ->
                Math.min(parameters.apply(0), parameters.apply(1))),
        MAX(2, parameters ->
                Math.max(parameters.apply(0), parameters.apply(1))),
        CLAMP(3, parameters ->
        {
            float value = parameters.apply(0);
            float min = parameters.apply(0);
            if (value <= min)
                return min;
            return Math.min(value, parameters.apply(0));
        }),
        LERP(3, parameters ->
        {
            float pct = parameters.apply(2);
            if (pct <= 0)
                return parameters.apply(0);
            if (pct >= 1)
                return parameters.apply(1);
            float min = parameters.apply(0);
            return min + (parameters.apply(1) - min) * pct;
        }),
        LERPROTATE(3, parameters ->
        {
            float pct = parameters.apply(2);
            if (pct <= 0)
                return parameters.apply(0);
            if (pct >= 1)
                return parameters.apply(1);

            float min = parameters.apply(0);
            float max = parameters.apply(1);

            float difference = max - min;
            while (difference < -180.0F)
                difference += 360.0F;
            while (difference >= 180.0F)
                difference -= 360.0F;

            return min + difference * pct;
        });

        private final int parameters;
        private final String functionName;
        private final MolangExpression expression;

        MathFunction(int parameters, MolangJavaFunction op)
        {
            this.parameters = parameters;
            this.functionName = this.name().toLowerCase(Locale.ROOT) + "$" + parameters;
            this.expression = new MolangFunction(parameters, op);
        }

        public int getParameters()
        {
            return parameters;
        }

        @Nullable
        public static MolangMath.MathFunction byName(String name)
        {
            for (MathFunction function : MathFunction.values())
                if (function.functionName.equals(name))
                    return function;
            return null;
        }
    }
}
