package io.github.ocelot.modelanima;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.ocelot.modelanima.api.common.molang.MolangParser;

public class MolangTest
{
    public static void main(String[] args) throws CommandSyntaxException
    {
        MolangParser.parse("return math.sin(global.anim_time * 1.23)");
//        MolangParser.parse("math.sin(global.anim_time * 1.23)");
//        MolangParser.parse("temp.my_temp_var = Math.sin(query.anim_time * 1.23);\n" +
//                "temp.my_other_temp_var = Math.cos(query.life_time + 2.0);\n" +
//                "return temp.my_temp_var * temp.my_temp_var + temp.my_other_temp_var;");
    }
}
