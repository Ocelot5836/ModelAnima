package io.github.ocelot.modelanima.api.common.molang;

import com.google.common.collect.ImmutableSet;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.github.ocelot.modelanima.core.common.molang.node.*;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MolangParser
{
    private static final SimpleCommandExceptionType UNEXPECTED_TOKEN = new SimpleCommandExceptionType(new LiteralMessage("Unexpected token"));
    private static final DynamicCommandExceptionType INVALID_KEYWORD = new DynamicCommandExceptionType(obj -> new LiteralMessage("Invalid keyword: " + obj));
    private static final DynamicCommandExceptionType EXPECTED = new DynamicCommandExceptionType(obj -> new LiteralMessage("Expected " + obj));
    private static final SimpleCommandExceptionType TRAILING_STATEMENT = new SimpleCommandExceptionType(new LiteralMessage("Trailing statement"));
    private static final Dynamic2CommandExceptionType NOT_ENOUGH_PARAMETERS = new Dynamic2CommandExceptionType((obj, obj2) -> new LiteralMessage("Not enough parameters. Expected at least " + obj + ", got " + obj2));
    private static final Dynamic2CommandExceptionType TOO_MANY_PARAMETERS = new Dynamic2CommandExceptionType((obj, obj2) -> new LiteralMessage("Too many parameters. Expected at most " + obj + ", got " + obj2));

    private static final Set<String> VARIABLE_SCOPES = ImmutableSet.of("query", "global", "temp", "variable");
    private static final Set<Character> MATH_OPERATORS = ImmutableSet.of('(', ')', '*', '/', '+', '-');

    public static MolangExpression parse(String input) throws CommandSyntaxException
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
            expressions[i] = parseExpression(new StringReader(lines[i]), i >= lines.length - 1, true);

        return expressions.length == 1 ? expressions[0] : new MolangCompoundNode(expressions);
    }

    private static MolangExpression parseExpression(StringReader reader, boolean allowReturn, boolean allowMath) throws CommandSyntaxException
    {
        if (!reader.canRead())
            throw UNEXPECTED_TOKEN.createWithContext(reader);

        reader.skipWhitespace(); // Skip potential spaces or tabs before '=', '*', etc
        if (!reader.canRead())
            throw UNEXPECTED_TOKEN.createWithContext(reader);

        // Check for return
        if (reader.getRemaining().startsWith("return"))
        {
            if (!allowReturn)
            {
                reader.setCursor(reader.getString().indexOf("return") + 6);
                throw UNEXPECTED_TOKEN.createWithContext(reader);
            }
            for (int i = 0; i < 7; i++)
                reader.skip();
            MolangExpression expression = parseExpression(reader, false, allowMath);
            if (reader.canRead())
                throw TRAILING_STATEMENT.createWithContext(reader);
            return expression;
        }

        // Check for math. This will not happen if reading from math because operators are removed
        if (!reader.getRemaining().contains("=") && allowMath)
        {
            for (char operator : MATH_OPERATORS)
            {
                if (reader.getRemaining().chars().anyMatch(a -> a == operator))
                {
//                MolangExpression mathExpression = parseMath(reader.getRemaining());
//                reader.skipWhitespace();
//                if (reader.canRead())
//                    throw TRAILING_STATEMENT.createWithContext(reader);
                    return eval(reader);
                }
            }
        }

        String[] currentKeyword = parseKeyword(reader, !allowReturn); // This handles 'abc.def' etc
        String fullWord = currentKeyword[0] + (currentKeyword.length > 1 ? "." + currentKeyword[1] : "");

        // Check for number
        if (NumberUtils.isCreatable(fullWord))
        {
            reader.skipWhitespace();
            if (reader.canRead())
                throw TRAILING_STATEMENT.createWithContext(reader);
            return new MolangConstantNode(Float.parseFloat(fullWord));
        }

        if (NumberUtils.isCreatable(currentKeyword[0].substring(0, 1)))
        {
            reader.setCursor(reader.getString().indexOf(currentKeyword[0].substring(0, 1)) + currentKeyword[0].length());
            throw INVALID_KEYWORD.createWithContext(reader, fullWord);
        }

        // Check for variables
        if (VARIABLE_SCOPES.contains(currentKeyword[0]))
        {
            if (currentKeyword.length <= 1)
                throw UNEXPECTED_TOKEN.createWithContext(reader);
            reader.skipWhitespace();
            if (reader.canRead() && reader.peek() == '=')
            {
                reader.skip();
                MolangExpression expression = parseExpression(reader, false, allowMath);
                return new MolangSetVariableNode(currentKeyword[0], currentKeyword[1], expression);
            }
            return new MolangGetVariableNode(currentKeyword[0], currentKeyword[1]);
        }

        // Expect something else
        reader.skipWhitespace();
        if (!reader.canRead())
            throw UNEXPECTED_TOKEN.createWithContext(reader);

        // Check for methods
        if (reader.peek() == '(')
        {
            int start = reader.getCursor();
            MolangExpression[] parameters = null;
            while (reader.canRead() && start != -1)
            {
                if (reader.peek() == ')')
                {
                    String[] parameterStrings = reader.getRead().substring(start + 1).split(",");
                    parameters = new MolangExpression[parameterStrings.length];
                    for (int i = 0; i < parameterStrings.length; i++)
                        parameters[i] = parseExpression(new StringReader(parameterStrings[i].trim()), false, true);

                    start = -1;
                }
                reader.skip();
            }
            if (start != -1)
                throw EXPECTED.createWithContext(reader, ')');
            return parseMethod(currentKeyword, parameters);
        }

        throw UNEXPECTED_TOKEN.createWithContext(reader);
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
                return new String[]{"return"};
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
                {
                    reader.setCursor(start);
                    return false;
                }
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

    private static MolangExpression parseMethod(String[] methodName, MolangExpression[] parameters) throws CommandSyntaxException
    {
        if (methodName.length == 2 && "math".equalsIgnoreCase(methodName[0]))
        {
            for (MolangMathNode.MathFunction function : MolangMathNode.MathFunction.values())
                if (function.name().toLowerCase(Locale.ROOT).equals(methodName[1]))
                    return new MolangMathNode(function, parameters);
            throw UNEXPECTED_TOKEN.create();
        }
        return new MolangInvokeFunctionNode(methodName[0], methodName[1], parameters);
    }

//    private static MolangExpression parseMath(String data) throws CommandSyntaxException
//    {
//        List<Object> parts = new LinkedList<>();
//        parts.add(data);
//
//        int operationState = 0; // MD -> AS
//
//        while (operationState < 2)
//        {
//            for (Object obj : parts)
//            {
//                if (!(obj instanceof String))
//                    continue;
//
//                StringReader reader = new StringReader((String) obj);
//                int start = reader.getCursor();
//                while (reader.canRead())
//                {
//                    reader.skipWhitespace();
//
//                    if (reader.peek() == '*')
//                    {
//                        if (operationState == 1)
//                        {
//                            reader.skip();
//                            start = reader.getCursor();
//                            continue;
//                        }
//                        StringReader partReader = new StringReader(reader.getRead().substring(start));
//                        result = parseExpression(partReader, false);
//                    }
//                    else if (reader.peek() == '/')
//                    {
//                        if (operationState == 1)
//                        {
//                            reader.skip();
//                            start = reader.getCursor();
//                            continue;
//                        }
//                    }
//                    else if (reader.peek() == '+')
//                    {
//                        if (operationState == 0)
//                        {
//                            reader.skip();
//                            start = reader.getCursor();
//                            continue;
//                        }
//                    }
//                    else if (reader.peek() == '-')
//                    {
//                        if (operationState == 0)
//                        {
//                            reader.skip();
//                            start = reader.getCursor();
//                            continue;
//                        }
//                    }
//                    reader.skip();
//                }
//            }
//            operationState++;
//        }
//        return null;
//    }

    private static MolangExpression eval(StringReader reader) throws CommandSyntaxException
    {
        return new Object()
        {
            boolean eat(int charToEat)
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
                MolangExpression x = parseExpression();
                reader.skipWhitespace();
                if (reader.canRead())
                    throw TRAILING_STATEMENT.createWithContext(reader);
                return x;
            }

            MolangExpression parseExpression() throws CommandSyntaxException
            {
                MolangExpression x = parseTerm();
                if (eat('+'))
                {
                    return new MolangMathOperatorNode(MolangMathOperatorNode.MathOperation.ADD, x, parseTerm()); // addition
                }
                else if (eat('-'))
                {
                    return new MolangMathOperatorNode(MolangMathOperatorNode.MathOperation.SUBTRACT, x, parseTerm()); // subtraction
                }
                return x;
            }

            MolangExpression parseTerm() throws CommandSyntaxException
            {
                MolangExpression x = parseFactor();
                if (eat('*'))
                {
                    return new MolangMathOperatorNode(MolangMathOperatorNode.MathOperation.MULTIPLY, x, parseFactor()); // multiplication
                }
                else if (eat('/'))
                {
                    return new MolangMathOperatorNode(MolangMathOperatorNode.MathOperation.DIVIDE, x, parseFactor()); // division
                }
                return x;
            }

            MolangExpression parseFactor() throws CommandSyntaxException
            {
                if (eat('+'))
                    return parseFactor(); // unary plus
                if (eat('-'))
                    return new MolangMathOperatorNode(MolangMathOperatorNode.MathOperation.MULTIPLY, parseFactor(), new MolangConstantNode(-1)); // unary minus

                if (eat('('))
                {
                    MolangExpression expression = parseExpression();
                    eat(')');
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

                return MolangParser.parseExpression(new StringReader(reader.getRead().substring(start)), false, false);
            }
        }.parse();
    }

    private static boolean isValidKeywordChar(final char c)
    {
        return c >= '0' && c <= '9'
                || c >= 'A' && c <= 'Z'
                || c >= 'a' && c <= 'z'
                || c == '_' || c == '.';
    }
}
