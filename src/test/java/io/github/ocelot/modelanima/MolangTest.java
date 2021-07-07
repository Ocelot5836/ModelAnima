package io.github.ocelot.modelanima;

import com.google.common.base.Stopwatch;
import io.github.ocelot.modelanima.api.common.molang.MolangCompiler;
import io.github.ocelot.modelanima.api.common.molang.MolangException;
import io.github.ocelot.modelanima.api.common.molang.MolangExpression;
import io.github.ocelot.modelanima.api.common.molang.MolangRuntime;

public class MolangTest
{
    public static void main(String[] args) throws MolangException
    {
        Stopwatch compileTime = Stopwatch.createStarted();
        MolangExpression expression =
//                MolangParser.parse("((4 - math.pow(2, 3) + 1) * -math.sqrt(3*3+4*4)) / 2");
//                MolangParser.parse("temp.my_temp_var = math.sin(90) / 2");
//        MolangParser.parse("return math.sin(global.anim_time * 1.23)");
//        MolangParser.parse("math.sin(global.anim_time * 1.23)");
        MolangCompiler.compile("(math.cos(query.life_time * 20.0 * 10.89) * 28.65) + (math.sin(variable.attack_time * 180.0) * 68.76 - (math.sin((1.0 - (1.0 - variable.attack_time) * (1.0 - variable.attack_time)) * 180.0)) * 22.92)", false);
//        MolangParser.parse("temp.my_temp_var = Math.sin(query.anim_time * 1.23);\n" +
//                "temp.my_other_temp_var = Math.cos(query.life_time + 2.0);\n" +
//                "return temp.my_temp_var * temp.my_temp_var + temp.my_other_temp_var;");
        compileTime.stop();

        MolangRuntime runtime = MolangRuntime.runtime(0)
                .setQuery("life_time", 0)
                .setVariable("attack_time", 0)
                .create();

        Stopwatch runTime = Stopwatch.createStarted();
        float result = expression.resolve(runtime);
        runTime.stop();

        System.out.println("\n" + runtime.dump());
        System.out.println("Final expression: '" + expression + "', " + expression.getClass());
        System.out.println("Took " + compileTime + " to compile");
        System.out.println("Took " + runTime + " to execute. Result: " + result);
    }
}
