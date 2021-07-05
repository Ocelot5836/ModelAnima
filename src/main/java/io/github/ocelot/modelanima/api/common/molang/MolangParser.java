package io.github.ocelot.modelanima.api.common.molang;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.github.ocelot.modelanima.core.common.molang.MolangVariableNode;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class MolangParser
{
    private static final SimpleCommandExceptionType UNEXPECTED_TOKEN = new SimpleCommandExceptionType(new LiteralMessage("Unexpected token"));
    //    private static final SimpleCommandExceptionType NO_RETURN = new SimpleCommandExceptionType(new LiteralMessage("Unexpected token"));
    private static final DynamicCommandExceptionType INVALID_KEYWORD = new DynamicCommandExceptionType(obj -> new LiteralMessage("Invalid keyword: " + obj));
    private static final DynamicCommandExceptionType EXPECTED = new DynamicCommandExceptionType(obj -> new LiteralMessage("Expected " + obj));
    private static final SimpleCommandExceptionType UNREACHABLE_STATEMENT = new SimpleCommandExceptionType(new LiteralMessage("Unreachable statement"));

    public static MolangExpression parse(String input) throws CommandSyntaxException
    {
        if (input.isEmpty())
            throw UNEXPECTED_TOKEN.create();

        String[] lines = input.split(";");
        if (input.contains(";") && lines.length == 1)
        {
            StringReader reader = new StringReader(input);
            reader.setCursor(input.indexOf(';'));
            throw UNEXPECTED_TOKEN.createWithContext(reader);
        }

        for (String line : lines)
        {
            // TODO if a return exists, break
            StringReader reader = new StringReader(line);

            int debug = 0;

            String[] currentKeyword = null;
            while (reader.canRead())
            {
                reader.skipWhitespace(); // Skip potential spaces or tabs before '=', '*', etc

                System.out.println("Parsing: " + reader.getRemaining());

                if (currentKeyword == null)
                    currentKeyword = parseKeyword(reader); // This handles 'abc.def' etc

                reader.skipWhitespace();

                // Check for return
                if ("return".equals(currentKeyword[0]))
                {
                    // TODO add return
                }

                if (reader.peek() == '(')
                {
                    int start = reader.getCursor();
                    while (reader.canRead() && start != -1)
                    {
                        if (reader.peek() == ')')
                        {
                            String[] parameterStrings = reader.getRead().substring(start + 1).split(",");
                            MolangExpression[] parameters = new MolangExpression[parameterStrings.length];
                            for (int i = 0; i < parameterStrings.length; i++)
                                parameters[i] = parseParameter(parameterStrings[i].trim());

                            System.out.println("Parameters: " + Arrays.toString(parameters));
                            start = -1;
                        }
                        reader.skip();
                    }
                    if (start != -1)
                        throw EXPECTED.createWithContext(reader, ')');
                    break;
                }

                if(reader.peek() == '*'){
                    MolangExpression first = new MolangVariableNode();
                }

                System.out.println(Arrays.toString(currentKeyword));
                currentKeyword = null;

                debug++;
                if (debug > 50)
                    break;
            }

            System.out.println("==END==");
        }
        return null;
    }

    private static String[] parseKeyword(StringReader reader) throws CommandSyntaxException
    {
        List<String> keywords = new LinkedList<>();
        int start = reader.getCursor();
        while (reader.canRead() && isValidKeywordChar(reader.peek()))
        {
            if (reader.peek() == '.')
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
            throw UNEXPECTED_TOKEN.createWithContext(reader);
        return keywords.toArray(new String[0]);
    }

    private static MolangExpression parseParameter(String input) throws CommandSyntaxException
    {
        System.out.println("Parameter: " + input);
        return parse(input);
    }

    private static boolean isValidKeywordChar(final char c)
    {
        return c >= '0' && c <= '9'
                || c >= 'A' && c <= 'Z'
                || c >= 'a' && c <= 'z'
                || c == '_' || c == '.';
    }
}
