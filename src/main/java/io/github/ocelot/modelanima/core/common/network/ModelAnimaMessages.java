package io.github.ocelot.modelanima.core.common.network;

import io.github.ocelot.modelanima.ModelAnima;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;

/**
 * @author Ocelot
 */
@ApiStatus.Internal
public class ModelAnimaMessages
{
    public static final SimpleChannel PLAY = NetworkRegistry.newSimpleChannel(new ResourceLocation(ModelAnima.MOD_ID, "play"), () -> "1", "1"::equals, "1"::equals);

    public static void init()
    {
        PLAY.registerMessage(0, ClientboundSyncAnimationMessage.class, ClientboundSyncAnimationMessage::write, ClientboundSyncAnimationMessage::new, (msg, ctx) ->
        {
            ModelAnimaClientMessageHandler.handleSyncAnimationMessage(msg, ctx.get());
            ctx.get().setPacketHandled(true);
        }, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
}
