package io.github.ocelot.modelanima;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

/**
 * @author Ocelot
 */
public class TestCommand
{
    public static void register(CommandDispatcher<CommandSource> dispatcher)
    {
        dispatcher.register(Commands.literal("codecTest")
                .executes(context ->
                {
                    CodecTest.test();
                    return 1;
                })
        );
    }
}
