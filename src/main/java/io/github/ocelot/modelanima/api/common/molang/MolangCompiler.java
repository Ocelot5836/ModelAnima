package io.github.ocelot.modelanima.api.common.molang;

import com.google.common.collect.ImmutableSet;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.github.ocelot.modelanima.core.common.molang.node.*;
import io.github.ocelot.modelanima.core.common.molang.object.MolangMath;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * <p>Compiles a {@link MolangExpression} from a string input.</p>
 *
 * @author Ocelot
 * @since 1.0.0
 */
public class MolangCompiler
{
    private static final SimpleCommandExceptionType UNEXPECTED_TOKEN = new SimpleCommandExceptionType(new LiteralMessage("Unexpected token"));
    private static final DynamicCommandExceptionType INVALID_KEYWORD = new DynamicCommandExceptionType(obj -> new LiteralMessage("Invalid keyword: " + obj));
    private static final DynamicCommandExceptionType EXPECTED = new DynamicCommandExceptionType(obj -> new LiteralMessage("Expected " + obj));
    private static final SimpleCommandExceptionType TRAILING_STATEMENT = new SimpleCommandExceptionType(new LiteralMessage("Trailing statement"));
    private static final Dynamic2CommandExceptionType NOT_ENOUGH_PARAMETERS = new Dynamic2CommandExceptionType((obj, obj2) -> new LiteralMessage("Not enough parameters. Expected at least " + obj + ", got " + obj2));
    private static final Dynamic2CommandExceptionType TOO_MANY_PARAMETERS = new Dynamic2CommandExceptionType((obj, obj2) -> new LiteralMessage("Too many parameters. Expected at most " + obj + ", got " + obj2));

    /**
     * Whether or not to reduce math to constant values if possible. Eg. <code>4 * 4 + 2</code> would become <code>18</code>. This should almost always be on.
     */
    public static final int REDUCE_FLAG = 1;
    /**
     * Whether or not to check for 'this' keyword.
     */
    private static final int CHECK_THIS_FLAG = 2;
    /**
     * Whether or not to check for variables.
     */
    private static final int CHECK_VARIABLE_FLAG = 4;
    /**
     * Whether or not to check for methods.
     */
    private static final int CHECK_METHOD_FLAG = 8;
    /**
     * Whether or not to check for math operations.
     */
    private static final int CHECK_OPERATORS_FLAG = 16;

    private static final MolangEnvironment ENVIRONMENT = new CompileEnvironment();
    private static final Set<Character> MATH_OPERATORS = ImmutableSet.of('(', ')', '*', '/', '+', '-');

    /**
     * Compiles a {@link MolangExpression} from the specified string input.
     *
     * @param input The data to compile
     * @return The compiled expression
     * @throws MolangParseException If any error occurs
     */
    public static MolangExpression compile(String input) throws MolangParseException
    {
        return compile(input, REDUCE_FLAG);
    }

    /**
     * Compiles a {@link MolangExpression} from the specified string input.
     *
     * @param input The data to compile
     * @param flags Additional flags to use when compiling
     * @return The compiled expression
     * @throws MolangParseException If any error occurs
     */
    public static MolangExpression compile(String input, int flags) throws MolangParseException
    {
        try
        {
            if (input.isEmpty())
                throw UNEXPECTED_TOKEN.create();

            String[] lines = input.split(";");
            if (lines.length == 1)
            {
                if (input.contains(";"))
                {
                    StringReader reader = new StringReader(input);
                    reader.setCursor(input.indexOf(';') + 1);
                    throw UNEXPECTED_TOKEN.createWithContext(reader);
                }
                if (input.contains("return"))
                {
                    StringReader reader = new StringReader(input);
                    reader.setCursor(input.indexOf("return") + 6);
                    throw UNEXPECTED_TOKEN.createWithContext(reader);
                }
            }

            MolangExpression[] expressions = new MolangExpression[lines.length];
            for (int i = 0; i < lines.length; i++)
            {
                StringReader reader = new StringReader(lines[i]);
                reader.skipWhitespace();

                // Set initial flags
                int additionalFlags = 0;
                if (reader.getString().contains("."))
                {
                    additionalFlags |= CHECK_VARIABLE_FLAG;
                    if (reader.getString().contains("("))
                        additionalFlags |= CHECK_METHOD_FLAG | CHECK_OPERATORS_FLAG;
                }
                if (reader.getString().contains("this"))
                    additionalFlags |= CHECK_THIS_FLAG;
                if (!checkFlag(additionalFlags, CHECK_OPERATORS_FLAG))
                {
                    for (char operator : MATH_OPERATORS)
                    {
                        if (reader.getString().indexOf(operator) != -1)
                        {
                            additionalFlags |= CHECK_OPERATORS_FLAG;
                            break;
                        }
                    }
                }

                // Check for return
                if (reader.getRemaining().startsWith("return"))
                {
                    if (i < lines.length - 1)
                    {
                        reader.setCursor(reader.getString().indexOf("return") + 6);
                        throw UNEXPECTED_TOKEN.createWithContext(reader);
                    }
                    for (int j = 0; j < 7; j++)
                        reader.skip();
                    MolangExpression expression = parseExpression(reader, flags | additionalFlags, false, true);
                    if (reader.canRead())
                        throw TRAILING_STATEMENT.createWithContext(reader);
                    expressions[i] = expression;
                }
                else
                {
                    expressions[i] = parseExpression(new StringReader(lines[i]), flags | additionalFlags, i == 1, true);
                }
            }

            return expressions.length == 1 ? expressions[0] : new MolangCompoundNode(expressions);
        }
        catch (CommandSyntaxException e)
        {
            throw new MolangParseException(e);
        }
    }

    private static MolangExpression parseExpression(StringReader reader, int flags, boolean simple, boolean allowMath) throws CommandSyntaxException
    {
        if (!reader.canRead())
            throw UNEXPECTED_TOKEN.createWithContext(reader);

        reader.skipWhitespace(); // Skip potential spaces or tabs before '=', '*', etc
        if (!reader.canRead())
            throw UNEXPECTED_TOKEN.createWithContext(reader);

        // Check for math. This will not happen if reading from math because operators are removed
        if (checkFlag(flags, CHECK_OPERATORS_FLAG) && !reader.getRemaining().contains("=") && !reader.getRemaining().contains("?") && !reader.getRemaining().contains(":") && allowMath)
        {
            for (char operator : MATH_OPERATORS)
            {
                if (reader.getRemaining().chars().anyMatch(a -> a == operator))
                {
                    return compute(reader, flags);
                }
            }
        }

        String[] currentKeyword = parseKeyword(reader, simple); // This handles 'abc.def' etc
        String fullWord = currentKeyword[0] + (currentKeyword.length > 1 ? "." + currentKeyword[1] : "");

        // Check for 'this' keyword
        if (checkFlag(flags, CHECK_THIS_FLAG) && "this".equals(fullWord))
        {
            reader.skipWhitespace();
            if (reader.canRead() && reader.peek() != '?' && reader.peek() != ':' && !MATH_OPERATORS.contains(reader.peek()))
                throw TRAILING_STATEMENT.createWithContext(reader);
            return parseCondition(reader, new MolangThisNode(), flags, allowMath);
        }

        // Check for number
        if (NumberUtils.isParsable(fullWord))
        {
            reader.skipWhitespace();
            if (reader.canRead() && reader.peek() != '?' && reader.peek() != ':' && !MATH_OPERATORS.contains(reader.peek()))
                throw TRAILING_STATEMENT.createWithContext(reader);
            return parseCondition(reader, new MolangConstantNode(Float.parseFloat(fullWord)), flags, allowMath);
        }

        // methods and params require at least both parts
        if (currentKeyword.length <= 1)
            throw UNEXPECTED_TOKEN.createWithContext(reader);

        // Check for methods
        if (checkFlag(flags, CHECK_METHOD_FLAG) && reader.canRead() && reader.peek() == '(')
        {
            int start = reader.getCursor();
            MolangExpression[] parameters = null;
            while (reader.canRead() && start != -1)
            {
                if (reader.peek() == ')')
                {
                    if (reader.getCursor() == start + 1)
                    {
                        parameters = new MolangExpression[0];
                    }
                    else
                    {
                        String[] parameterStrings = reader.getRead().substring(start + 1).split(",");
                        parameters = new MolangExpression[parameterStrings.length];
                        for (int i = 0; i < parameterStrings.length; i++)
                            parameters[i] = parseExpression(new StringReader(parameterStrings[i].trim()), flags, true, true);
                    }

                    start = -1;
                }
                reader.skip();
            }
            if (start != -1)
                throw EXPECTED.createWithContext(reader, ')');

            reader.skipWhitespace();
            return parseCondition(reader, parseMethod(currentKeyword, parameters, flags), flags, allowMath);
        }
        else if (checkFlag(flags, CHECK_VARIABLE_FLAG))
        {
            // Check for variables
            reader.skipWhitespace();
            if (reader.canRead())
            {
                if (reader.peek() == '=')
                {
                    reader.skip();
                    MolangExpression expression = parseExpression(reader, flags, true, allowMath);
                    return new MolangSetVariableNode(currentKeyword[0], currentKeyword[1], expression);
                }
            }
            return parseCondition(reader, new MolangGetVariableNode(currentKeyword[0], currentKeyword[1]), flags, allowMath);
        }
        throw TRAILING_STATEMENT.createWithContext(reader);
    }

    private static MolangExpression parseCondition(StringReader reader, MolangExpression expression, int flags, boolean allowMath) throws CommandSyntaxException
    {
        if (reader.canRead() && reader.peek() == '?')
        {
            reader.skip();
            reader.skipWhitespace();

            int start = reader.getCursor();
            while (reader.canRead() && reader.peek() != ':')
                reader.skip();

            if (!reader.canRead())
                throw TRAILING_STATEMENT.createWithContext(reader);

            MolangExpression first = parseExpression(new StringReader(reader.getRead().substring(start)), flags, true, allowMath);
            if (!reader.canRead())
                throw TRAILING_STATEMENT.createWithContext(reader);
            reader.skip();

            MolangExpression branch = parseExpression(reader, flags, true, allowMath);
            MolangExpression condition = new MolangConditionalNode(expression, first, branch);
            if (checkFlag(flags, REDUCE_FLAG) && expression instanceof MolangConstantNode)
            {
                try
                {
                    return expression.resolve(ENVIRONMENT) != 0.0 ? first : branch;
                }
                catch (MolangException e)
                {
                    // This should literally never happen
                    e.printStackTrace();
                }
            }
            return condition;
        }
        return expression;
    }

    private static String[] parseKeyword(StringReader reader, boolean simple) throws CommandSyntaxException
    {
        List<String> keywords = new LinkedList<>();
        int start = reader.getCursor();
        while (reader.canRead() && isValidKeywordChar(reader.peek()))
        {
            if (reader.peek() == '.' && keywords.isEmpty())
            {
                keywords.add(reader.getRead().substring(start));
                reader.skip();
                start = reader.getCursor();
            }
            if (!reader.canRead())
                throw UNEXPECTED_TOKEN.createWithContext(reader);
            reader.skip();
        }
        if (start < reader.getCursor())
            keywords.add(reader.getRead().substring(start));
        if (keywords.stream().allMatch(String::isEmpty))
        {
            if (simple)
                return new String[0];
            throw UNEXPECTED_TOKEN.createWithContext(reader);
        }
        return keywords.toArray(new String[0]);
    }

    private static boolean hasMethod(StringReader reader)
    {
        int start = reader.getCursor();
        int parenthesis = -1;
        while (reader.canRead() && (isValidKeywordChar(reader.peek()) || reader.peek() == '(' || reader.peek() == ')' || parenthesis >= 0))
        {
            if (reader.peek() == '(')
            {
                if (parenthesis == -1)
                    parenthesis = 0;
                parenthesis++;
            }
            if (reader.peek() == ')')
            {
                if (parenthesis == 0)
                    break;
                parenthesis--;
            }
            if (!reader.canRead())
            {
                reader.setCursor(start);
                return false;
            }
            reader.skip();
        }
        boolean success = start < reader.getCursor();
        reader.setCursor(start);
        return success && parenthesis == 0;
    }

    private static MolangExpression parseMethod(String[] methodName, MolangExpression[] parameters, int flags) throws CommandSyntaxException
    {
        // Special case for math to check if it's valid
        if ("math".equalsIgnoreCase(methodName[0]))
        {
            MolangMath.MathFunction function = MolangMath.MathFunction.byName(methodName[1] + "$" + parameters.length);
            if (function == null)
                throw INVALID_KEYWORD.create(methodName[1]);
            if (parameters.length < function.getParameters())
                throw NOT_ENOUGH_PARAMETERS.create(function.getParameters(), parameters.length);
            if (function.getParameters() >= 0 && parameters.length > function.getParameters())
                throw TOO_MANY_PARAMETERS.create(function.getParameters(), parameters.length);

            if (checkFlag(flags, REDUCE_FLAG))
            {
                // Math functions are constant so these can be compiled down to raw numbers if all parameters are constants
                boolean reduceFunction = true;
                for (int i = 0; i < parameters.length; i++)
                {
                    if (parameters[i] instanceof MolangConstantNode)
                        continue;
                    try
                    {
                        parameters[i] = new MolangConstantNode(parameters[i].resolve(ENVIRONMENT));
                    }
                    catch (MolangException e)
                    {
                        // The parameter is runtime dependent, so the entire function is blocked from being computed.
                        // Parameters can still be computed so there is no reason to stop trying to reduce
                        reduceFunction = false;
                    }
                }

                if (reduceFunction)
                {
                    try
                    {
                        float[] parameterValues = new float[parameters.length];
                        for (int i = 0; i < parameterValues.length; i++)
                            parameterValues[i] = parameters[i].resolve(ENVIRONMENT);
                        return new MolangConstantNode(function.getOp().resolve(i ->
                        {
                            if (i < 0 || i >= parameters.length)
                                return 0F;
                            return parameterValues[i];
                        }));
                    }
                    catch (MolangException e)
                    {
                        // Something went horribly wrong with the above checks
                        e.printStackTrace();
                    }
                }
            }
        }
        // Other functions may or may not work, the runtime determines if they will
        return new MolangInvokeFunctionNode(methodName[0], methodName[1], parameters);
    }

    // Based on https://stackoverflow.com/questions/3422673/how-to-evaluate-a-math-expression-given-in-string-form
    // Stack Overflow! Every programmer's best friend!
    private static MolangExpression compute(StringReader reader, int flags) throws CommandSyntaxException
    {
        return new Object()
        {
            private boolean canReduce = checkFlag(flags, REDUCE_FLAG);

            boolean accept(int charToEat)
            {
                reader.skipWhitespace();
                if (reader.canRead() && reader.peek() == charToEat)
                {
                    reader.skip();
                    return true;
                }
                return false;
            }

            MolangExpression parse() throws CommandSyntaxException
            {
                reader.skipWhitespace();
                MolangExpression x = parseExpression();
                reader.skipWhitespace();
                if (reader.canRead())
                    throw TRAILING_STATEMENT.createWithContext(reader);

                // Reduction is impossible because of runtime dependence
                if (!this.canReduce)
                    return x;

                // Attempt to reduce if possible
                try
                {
                    return new MolangConstantNode(x.resolve(ENVIRONMENT));
                }
                catch (MolangException e)
                {
                    // Something went wrong with the checks
                    e.printStackTrace();
                }

                return x;
            }

            MolangExpression parseExpression() throws CommandSyntaxException
            {
                MolangExpression x = parseTerm();
                while (true)
                {
                    if (accept('+'))
                    {
                        x = new MolangMathOperatorNode(MolangMathOperatorNode.MathOperation.ADD, x, parseTerm()); // addition
                    }
                    else if (accept('-'))
                    {
                        x = new MolangMathOperatorNode(MolangMathOperatorNode.MathOperation.SUBTRACT, x, parseTerm()); // subtraction
                    }
                    else
                    {
                        return x;
                    }
                }
            }

            MolangExpression parseTerm() throws CommandSyntaxException
            {
                MolangExpression x = parseFactor();
                while (true)
                {
                    boolean accept = accept('*');
                    if (accept)
                    {
                        x = new MolangMathOperatorNode(MolangMathOperatorNode.MathOperation.MULTIPLY, x, parseFactor()); // multiplication
                    }
                    else if (accept('/'))
                    {
                        x = new MolangMathOperatorNode(MolangMathOperatorNode.MathOperation.DIVIDE, x, parseFactor()); // division
                    }
                    else
                    {
                        return x;
                    }
                }
            }

            MolangExpression parseFactor() throws CommandSyntaxException
            {
                if (accept('+'))
                    return parseFactor(); // unary plus
                if (accept('-'))
                    return new MolangMathOperatorNode(MolangMathOperatorNode.MathOperation.MULTIPLY, parseFactor(), new MolangConstantNode(-1)); // unary minus

                if (accept('('))
                {
                    MolangExpression expression = parseExpression();
                    accept(')');
                    return expression;
                }

                reader.skipWhitespace();
                int start = reader.getCursor();
                int parentheses = 0;
                boolean hasMethod = hasMethod(reader);
                while (reader.canRead() && (Character.isWhitespace(reader.peek()) || !MATH_OPERATORS.contains(reader.peek()) || hasMethod))
                {
                    if (reader.peek() == '(')
                    {
                        parentheses++;
                    }
                    if (reader.peek() == ')' && hasMethod)
                    {
                        parentheses--;
                        if (parentheses == 0)
                        {
                            reader.skip();
                            break;
                        }
                    }
                    reader.skip();
                }

                System.out.println("Parse: " + reader.getRead().substring(start));
                MolangExpression expression = MolangCompiler.parseExpression(new StringReader(reader.getRead().substring(start)), flags, true, false);
                if (!checkFlag(flags, REDUCE_FLAG))
                    return expression;
                if (this.canReduce && (expression instanceof MolangSetVariableNode || expression instanceof MolangInvokeFunctionNode || expression instanceof MolangGetVariableNode))
                    this.canReduce = false;
                return expression;
            }
        }.parse();
    }

    private static boolean checkFlag(int flags, int flag)
    {
        return (flags & flag) > 0;
    }

    private static boolean isValidKeywordChar(final char c)
    {
        return c >= '0' && c <= '9'
                || c >= 'A' && c <= 'Z'
                || c >= 'a' && c <= 'z'
                || c == '_' || c == '.';
    }

    private static class CompileEnvironment implements MolangEnvironment
    {
        @Override
        public void loadParameter(int index, MolangExpression expression) throws MolangException
        {
            throw new MolangException("Invalid Call");
        }

        @Override
        public void clearParameters() throws MolangException
        {
            throw new MolangException("Invalid Call");
        }

        @Override
        public float getThis() throws MolangException
        {
            throw new MolangException("Invalid Call");
        }

        @Override
        public MolangObject get(String name) throws MolangException
        {
            throw new MolangException("Invalid Call");
        }

        @Override
        public MolangExpression getParameter(int parameter) throws MolangException
        {
            throw new MolangException("Invalid Call");
        }

        @Override
        public boolean hasParameter(int parameter) throws MolangException
        {
            throw new MolangException("Invalid Call");
        }
    }
}
