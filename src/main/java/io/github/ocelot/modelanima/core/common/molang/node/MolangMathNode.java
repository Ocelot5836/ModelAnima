//package io.github.ocelot.modelanima.core.common.molang.node;
//
//import com.mojang.brigadier.LiteralMessage;
//import com.mojang.brigadier.exceptions.CommandSyntaxException;
//import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
//import io.github.ocelot.modelanima.api.common.molang.MolangException;
//import io.github.ocelot.modelanima.api.common.molang.MolangExpression;
//import io.github.ocelot.modelanima.api.common.molang.MolangRuntime;
//import net.minecraft.util.math.MathHelper;
//
//import java.util.Locale;
//
///**
// * @author Ocelot
// */
//@Deprecated
//public class MolangMathNode implements MolangExpression
//{
//    private static final Dynamic2CommandExceptionType NOT_ENOUGH_PARAMETERS = new Dynamic2CommandExceptionType((obj, obj2) -> new LiteralMessage("Not enough parameters. Expected at least " + obj + ", got " + obj2));
//    private static final Dynamic2CommandExceptionType TOO_MANY_PARAMETERS = new Dynamic2CommandExceptionType((obj, obj2) -> new LiteralMessage("Too many parameters. Expected at most " + obj + ", got " + obj2));
//
//    private final MathFunction function;
//    private final MolangExpression[] parameters;
//
//    public MolangMathNode(MathFunction function, MolangExpression... parameters) throws CommandSyntaxException
//    {
//        this.function = function;
//        this.parameters = parameters;
//        if (this.parameters.length < function.parameters)
//            throw NOT_ENOUGH_PARAMETERS.create(function.parameters, this.parameters.length);
//        if (function.parameters >= 0 && this.parameters.length > function.parameters)
//            throw TOO_MANY_PARAMETERS.create(function.parameters, this.parameters.length);
//    }
//
//    @Override
//    public float resolve(MolangRuntime runtime) throws MolangException
//    {
//        return this.function.op.apply(this, runtime).floatValue();
//    }
//
//    public float resolveParameter(int parameter, MolangRuntime runtime) throws MolangException
//    {
//        if (parameter < 0 || parameter >= this.parameters.length)
//            throw new RuntimeException("Invalid parameter: " + parameter);
//        return this.parameters[parameter].resolve(runtime);
//    }
//
//    @Override
//    public String toString()
//    {
//        StringBuilder builder = new StringBuilder("math.").append(this.function.name().toLowerCase(Locale.ROOT)).append('(');
//        for (int i = 0; i < this.parameters.length; i++)
//        {
//            builder.append(this.parameters[i].toString());
//            if (i < this.parameters.length - 1)
//                builder.append(", ");
//        }
//        return builder.append(')').toString();
//    }
//
//    public enum MathFunction
//    {
//        ABS(1, (node, runtime) ->
//                Math.abs(node.resolveParameter(0, runtime))),
//        SIN(1, (node, runtime) ->
//                Math.sin(node.resolveParameter(0, runtime) * (float) (Math.PI / 180))),
//        COS(1, (node, runtime) ->
//                Math.cos(node.resolveParameter(0, runtime) * (float) (Math.PI / 180))),
//        EXP(1, (node, runtime) ->
//                Math.exp(node.resolveParameter(0, runtime))),
//        LN(1, (node, runtime) ->
//                Math.log(node.resolveParameter(0, runtime))),
//        POW(2, (node, runtime) ->
//                Math.pow(node.resolveParameter(0, runtime), node.resolveParameter(1, runtime))),
//        SQRT(1, (node, runtime) ->
//                Math.sqrt(node.resolveParameter(0, runtime))),
//        RANDOM(2, (node, runtime) ->
//        {
//            double rand = Math.random();
//            float min = node.resolveParameter(0, runtime);
//            float max = node.resolveParameter(1, runtime);
//            if (min > max)
//                throw new RuntimeException("Invalid random range: " + min + " to " + max);
//            return MathHelper.lerp(rand, min, max);
//        }),
//        CEIL(1, (node, runtime) ->
//                Math.ceil(node.resolveParameter(0, runtime))),
//        ROUND(1, (node, runtime) ->
//                Math.round(node.resolveParameter(0, runtime))),
//        TRUNC(1, (node, runtime) ->
//        {
//            float value = node.resolveParameter(0, runtime);
//            if (value < 0)
//                return Math.ceil(value);
//            if (value > 0)
//                return Math.floor(value);
//            return value;
//        }),
//        FLOOR(1, (node, runtime) ->
//                Math.floor(node.resolveParameter(0, runtime))),
//        MOD(2, (node, runtime) ->
//                node.resolveParameter(0, runtime) % node.resolveParameter(1, runtime)),
//        MIN(2, (node, runtime) ->
//                Math.min(node.resolveParameter(0, runtime), node.resolveParameter(1, runtime))),
//        MAX(2, (node, runtime) ->
//                Math.max(node.resolveParameter(0, runtime), node.resolveParameter(1, runtime))),
//        CLAMP(3, (node, runtime) ->
//        {
//            float value = node.resolveParameter(0, runtime);
//            float min = node.resolveParameter(0, runtime);
//            if (value <= min)
//                return min;
//            return Math.min(value, node.resolveParameter(0, runtime));
//        }),
//        LERP(3, (node, runtime) ->
//        {
//            float pct = node.resolveParameter(2, runtime);
//            if (pct <= 0)
//                return node.resolveParameter(0, runtime);
//            if (pct >= 1)
//                return node.resolveParameter(1, runtime);
//            float min = node.resolveParameter(0, runtime);
//            return min + (node.resolveParameter(1, runtime) - min) * pct;
//        }),
//        LERPROTATE(3, (node, runtime) ->
//        {
//            float pct = node.resolveParameter(2, runtime);
//            if (pct <= 0)
//                return node.resolveParameter(0, runtime);
//            if (pct >= 1)
//                return node.resolveParameter(1, runtime);
//
//            float min = node.resolveParameter(0, runtime);
//            float max = node.resolveParameter(1, runtime);
//
//            float difference = max - min;
//            while (difference < -180.0F)
//                difference += 360.0F;
//            while (difference >= 180.0F)
//                difference -= 360.0F;
//
//            return min + difference * pct;
//        });
//
//        private final int parameters;
//        private final MathOp op;
//
//        MathFunction(int parameters, MathOp op)
//        {
//            this.parameters = parameters;
//            this.op = op;
//        }
//    }
//
//    private interface MathOp
//    {
//        Number apply(MolangMathNode node, MolangRuntime runtime) throws MolangException;
//    }
//}
