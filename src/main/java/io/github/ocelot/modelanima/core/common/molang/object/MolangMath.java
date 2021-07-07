package io.github.ocelot.modelanima.core.common.molang.object;

import io.github.ocelot.modelanima.api.common.molang.MolangExpression;
import io.github.ocelot.modelanima.api.common.molang.MolangObject;
import io.github.ocelot.modelanima.api.common.molang.MolangRuntime;
import net.minecraft.util.math.MathHelper;

import java.util.function.Function;

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
        for (MathFunction function : MathFunction.values())
            if (function.name().equals(name))
                return function;
        return MolangExpression.ZERO;
    }

    @Override
    public boolean has(String name)
    {
        for (MathFunction function : MathFunction.values())
            if (function.name().equals(name))
                return true;
        return false;
    }

    public enum MathFunction implements MolangExpression
    {
        ABS(1, (runtime) ->
                Math.abs(runtime.resolveParameter(0))),
        SIN(1, (runtime) ->
                (float) Math.sin(runtime.resolveParameter(0) * Math.PI / 180.0)),
        COS(1, (runtime) ->
                (float) Math.cos(runtime.resolveParameter(0) * Math.PI / 180.0)),
        EXP(1, (runtime) ->
                (float) Math.exp(runtime.resolveParameter(0))),
        LN(1, (runtime) ->
                (float) Math.log(runtime.resolveParameter(0))),
        POW(2, (runtime) ->
                (float) Math.pow(runtime.resolveParameter(0), runtime.resolveParameter(1))),
        SQRT(1, (runtime) ->
                (float) Math.sqrt(runtime.resolveParameter(0))),
        RANDOM(2, (runtime) ->
        {
            double rand = Math.random();
            float min = runtime.resolveParameter(0);
            float max = runtime.resolveParameter(1);
            if (min > max)
                throw new RuntimeException("Invalid random range: " + min + " to " + max);
            return (float) MathHelper.lerp(rand, min, max);
        }),
        CEIL(1, (runtime) ->
                (float) Math.ceil(runtime.resolveParameter(0))),
        ROUND(1, (runtime) ->
                (float) Math.round(runtime.resolveParameter(0))),
        TRUNC(1, (runtime) ->
        {
            float value = runtime.resolveParameter(0);
            if (value < 0)
                return (float) Math.ceil(value);
            if (value > 0)
                return (float) Math.floor(value);
            return value;
        }),
        FLOOR(1, (runtime) ->
                (float) Math.floor(runtime.resolveParameter(0))),
        MOD(2, (runtime) ->
                runtime.resolveParameter(0) % runtime.resolveParameter(1)),
        MIN(2, (runtime) ->
                Math.min(runtime.resolveParameter(0), runtime.resolveParameter(1))),
        MAX(2, (runtime) ->
                Math.max(runtime.resolveParameter(0), runtime.resolveParameter(1))),
        CLAMP(3, (runtime) ->
        {
            float value = runtime.resolveParameter(0);
            float min = runtime.resolveParameter(0);
            if (value <= min)
                return min;
            return Math.min(value, runtime.resolveParameter(0));
        }),
        LERP(3, (runtime) ->
        {
            float pct = runtime.resolveParameter(2);
            if (pct <= 0)
                return runtime.resolveParameter(0);
            if (pct >= 1)
                return runtime.resolveParameter(1);
            float min = runtime.resolveParameter(0);
            return min + (runtime.resolveParameter(1) - min) * pct;
        }),
        LERPROTATE(3, (runtime) ->
        {
            float pct = runtime.resolveParameter(2);
            if (pct <= 0)
                return runtime.resolveParameter(0);
            if (pct >= 1)
                return runtime.resolveParameter(1);

            float min = runtime.resolveParameter(0);
            float max = runtime.resolveParameter(1);

            float difference = max - min;
            while (difference < -180.0F)
                difference += 360.0F;
            while (difference >= 180.0F)
                difference -= 360.0F;

            return min + difference * pct;
        });

        private final int parameters;
        private final Function<MolangRuntime, Float> op;

        MathFunction(int parameters, Function<MolangRuntime, Float> op)
        {
            this.parameters = parameters;
            this.op = op;
        }

        @Override
        public float resolve(MolangRuntime runtime)
        {
            return this.op.apply(runtime);
        }
    }
}
