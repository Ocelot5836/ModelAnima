package io.github.ocelot.modelanima.core.common.network;

import io.github.ocelot.modelanima.ModelAnima;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.Optional;

/**
 * @author Ocelot
 */
public class ModelAnimaMessages
{
    public static final SimpleChannel PLAY = NetworkRegistry.newSimpleChannel(new ResourceLocation(ModelAnima.getParentModId(), ModelAnima.DOMAIN + "_play"), () -> "1", "1"::equals, "1"::equals);

    public static void init()
    {
        PLAY.registerMessage(0, SyncAnimationMessage.class, SyncAnimationMessage::write, SyncAnimationMessage::new, (msg, ctx) ->
        {
            ModelAnimaClientMessageHandler.handleSyncAnimationMessage(msg, ctx.get());
            ctx.get().setPacketHandled(true);
        }, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
}
