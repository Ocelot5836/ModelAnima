package io.github.ocelot.modelanima.core.common.molang.object;

import io.github.ocelot.modelanima.api.common.molang.MolangException;
import io.github.ocelot.modelanima.api.common.molang.MolangExpression;
import io.github.ocelot.modelanima.api.common.molang.MolangJavaFunction;
import io.github.ocelot.modelanima.api.common.molang.MolangObject;
import io.github.ocelot.modelanima.core.common.molang.node.MolangConstantNode;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Random;

/**
 * @author Ocelot
 */
public class MolangMath implements MolangObject
{
    private static final Random RNG = new Random();

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
        {
            builder.append('\t').append(function.functionName);
            if (function.op != null)
                builder.append("()");
            builder.append('\n');
        }
        return builder.toString();
    }

    public enum MathFunction
    {
        ABS(1, parameters ->
                Math.abs(parameters.resolve(0))),
        ACOS(1, parameters ->
                (float) Math.acos(parameters.resolve(0))),
        ASIN(1, parameters ->
                (float) Math.asin(parameters.resolve(0))),
        ATAN(1, parameters ->
                (float) Math.atan(parameters.resolve(0))),
        ATAN2(2, parameters ->
                (float) Math.atan2(parameters.resolve(0), parameters.resolve(1))),
        CEIL(1, parameters ->
                (float) Math.ceil(parameters.resolve(0))),
        CLAMP(3, parameters ->
        {
            float value = parameters.resolve(0);
            float min = parameters.resolve(0);
            if (value <= min)
                return min;
            return Math.min(value, parameters.resolve(0));
        }),
        COS(1, parameters ->
                MathHelper.cos((float) (parameters.resolve(0) * Math.PI / 180.0F))),
        DIE_ROLL(3, parameters ->
        {
            int count = (int) parameters.resolve(0);
            if (count <= 0)
                return 0.0F;

            float min = parameters.resolve(1);
            float max = parameters.resolve(2);
            if (min == max)
                return min * count;

            float sum = 0.0F;
            for (int i = 0; i < count; i++)
                sum += min + RNG.nextFloat() * (max - min);
            return sum;
        }),
        DIE_ROLL_INTEGER(3, parameters ->
        {
            int count = (int) parameters.resolve(0);
            if (count <= 0)
                return 0.0F;

            int min = (int) parameters.resolve(1);
            int max = (int) parameters.resolve(2);
            if (min == max)
                return min * count;

            if (min > max)
                throw new MolangException("Invalid random range: " + min + " to " + max);

            int sum = 0;
            for (int i = 0; i < count; i++)
                sum += max + RNG.nextInt(min - max);
            return sum;
        }),
        EXP(1, parameters ->
                (float) Math.exp(parameters.resolve(0))),
        FLOOR(1, parameters ->
                (float) Math.floor(parameters.resolve(0))),
        HERMITE_BLEND(1, parameters ->
        {
            float x = parameters.resolve(0);
            return 3 * x * x - 2 * x * x * x;
        }),
        LERP(3, parameters ->
        {
            float pct = parameters.resolve(2);
            if (pct <= 0)
                return parameters.resolve(0);
            if (pct >= 1)
                return parameters.resolve(1);
            float min = parameters.resolve(0);
            return min + (parameters.resolve(1) - min) * pct;
        }),
        LERPROTATE(3, parameters ->
        {
            float pct = parameters.resolve(2);
            if (pct <= 0)
                return parameters.resolve(0);
            if (pct >= 1)
                return parameters.resolve(1);

            float min = parameters.resolve(0);
            float max = parameters.resolve(1);

            float difference = max - min;
            while (difference < -180.0F)
                difference += 360.0F;
            while (difference >= 180.0F)
                difference -= 360.0F;

            return min + difference * pct;
        }),
        LN(1, parameters ->
                (float) Math.log(parameters.resolve(0))),
        MAX(2, parameters ->
                Math.max(parameters.resolve(0), parameters.resolve(1))),
        MIN(2, parameters ->
                Math.min(parameters.resolve(0), parameters.resolve(1))),
        MOD(2, parameters ->
                parameters.resolve(0) % parameters.resolve(1)),
        PI(new MolangConstantNode((float) Math.PI)),
        POW(2, parameters ->
                (float) Math.pow(parameters.resolve(0), parameters.resolve(1))),
        RANDOM(2, parameters ->
        {
            float min = parameters.resolve(0);
            float max = parameters.resolve(1);
            if (min > max)
                throw new MolangException("Invalid random range: " + min + " to " + max);
            return min + RNG.nextFloat() * (max - min);
        }),
        RANDOM_INTEGER(2, parameters ->
        {
            int min = (int) parameters.resolve(0);
            int max = (int) parameters.resolve(1);
            if (min > max)
                throw new MolangException("Invalid random range: " + min + " to " + max);
            return min + RNG.nextInt(max - min);
        }),
        ROUND(1, parameters ->
                (float) Math.round(parameters.resolve(0))),
        SIN(1, parameters ->
                MathHelper.sin((float) (parameters.resolve(0) * Math.PI / 180.0F))),
        SQRT(1, parameters ->
                (float) Math.sqrt(parameters.resolve(0))),
        TRUNC(1, parameters ->
        {
            float value = parameters.resolve(0);
            if (value < 0)
                return (float) Math.ceil(value);
            if (value > 0)
                return (float) Math.floor(value);
            return value;
        });

        private final int parameters;
        private final String functionName;
        private final MolangExpression expression;
        private final MolangJavaFunction op;

        MathFunction(int parameters, MolangJavaFunction op)
        {
            this.parameters = parameters;
            this.functionName = this.name().toLowerCase(Locale.ROOT) + "$" + parameters;
            this.expression = new MolangFunction(parameters, op);
            this.op = op;
        }

        MathFunction(MolangExpression value)
        {
            this.parameters = 0;
            this.functionName = this.name().toLowerCase(Locale.ROOT);
            this.expression = value;
            this.op = null;
        }

        public int getParameters()
        {
            return parameters;
        }

        public MolangExpression getExpression()
        {
            return expression;
        }

        @Nullable
        public MolangJavaFunction getOp()
        {
            return op;
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
