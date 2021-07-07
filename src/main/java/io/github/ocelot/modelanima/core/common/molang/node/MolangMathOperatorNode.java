package io.github.ocelot.modelanima.core.common.molang.node;

import io.github.ocelot.modelanima.api.common.molang.MolangException;
import io.github.ocelot.modelanima.api.common.molang.MolangExpression;
import io.github.ocelot.modelanima.api.common.molang.MolangRuntime;
import io.github.ocelot.modelanima.core.common.molang.result.MolangNumericalResult;

public class MolangMathOperatorNode implements MolangExpression
{
    private final MathOperation operation;
    private final MolangExpression a;
    private final MolangExpression b;

    public MolangMathOperatorNode(MathOperation operation, MolangExpression a, MolangExpression b)
    {
        this.operation = operation;
        this.a = a;
        this.b = b;
    }

    @Override
    public Result resolve(MolangRuntime runtime) throws MolangException
    {
        return new MolangNumericalResult(this.operation.op.apply(this.a, this.b, runtime));
    }

    @Override
    public String toString()
    {
        return this.a + " " + this.operation.sign + " " + this.b;
    }

    public enum MathOperation
    {
        MULTIPLY('*', (a, b, runtime) -> a.resolve(runtime).getAsFloat() * b.resolve(runtime).getAsFloat()),
        DIVIDE('/', (a, b, runtime) ->
        {
            float second = b.resolve(runtime).getAsFloat();
            if (second == 0) // This is to prevent a divide by zero exception
                return 0;
            return a.resolve(runtime).getAsFloat() / second;
        }),
        ADD('+', (a, b, runtime) -> a.resolve(runtime).getAsFloat() + b.resolve(runtime).getAsFloat()),
        SUBTRACT('-', (a, b, runtime) -> a.resolve(runtime).getAsFloat() - b.resolve(runtime).getAsFloat());

        private final char sign;
        private final MathOp op;

        MathOperation(char sign, MathOp op)
        {
            this.sign = sign;
            this.op = op;
        }
    }

    private interface MathOp
    {
        float apply(MolangExpression a, MolangExpression b, MolangRuntime runtime) throws MolangException;
    }
}
