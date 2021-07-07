package io.github.ocelot.modelanima;

import com.google.common.base.Stopwatch;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.ocelot.modelanima.api.common.molang.MolangException;
import io.github.ocelot.modelanima.api.common.molang.MolangExpression;
import io.github.ocelot.modelanima.api.common.molang.MolangParser;
import io.github.ocelot.modelanima.api.common.molang.MolangRuntime;

public class MolangTest
{
    public static void main(String[] args) throws CommandSyntaxException, MolangException
    {
        Stopwatch compileTime = Stopwatch.createStarted();
        MolangExpression expression =// MolangParser.parse("temp.my_temp_var = math.sin(90) / 2");
//                MolangParser.parse("global.test ? 1 : 20");
//        MolangParser.parse("return math.sin(global.anim_time * 1.23)");
//        MolangParser.parse("math.sin(global.anim_time * 1.23)");
        MolangParser.parse("temp.my_temp_var = Math.sin(query.anim_time * 1.23);\n" +
                "temp.my_other_temp_var = Math.cos(query.life_time + 2.0);\n" +
                "return temp.my_temp_var * temp.my_temp_var + temp.my_other_temp_var;");
        compileTime.stop();
        System.out.println("Took " + compileTime + " to compile");

        System.out.println("Final expression: '" + expression + "', " + expression.getClass());
        MolangRuntime runtime = MolangRuntime.runtime()
                .setQuery("anim_time", 90)
                .setQuery("life_time", 0)
                .create();

        Stopwatch runTime = Stopwatch.createStarted();
        MolangExpression.Result result = expression.resolve(runtime);
        runTime.stop();
        System.out.println("Took " + runTime + " to execute. Result: " + result);
    }
}
