package io.github.ocelot.modelanima.core.common.network;

import io.github.ocelot.modelanima.api.common.animation.AnimatedEntity;
import io.github.ocelot.modelanima.api.common.animation.AnimationState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Ocelot
 */
public class ModelAnimaClientMessageHandler
{
    private static final Logger LOGGER = LogManager.getLogger();

    public static void handleSyncAnimationMessage(SyncAnimationMessage msg, NetworkEvent.Context ctx)
    {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null)
            return;

        ctx.enqueueWork(() ->
        {
            Entity e = level.getEntity(msg.getEntityId());
            if (!(e instanceof AnimatedEntity))
            {
                LOGGER.warn("Server sent animation for entity: " + e + ", but it is not an instance of AnimatedEntity");
                return;
            }

            AnimatedEntity entity = (AnimatedEntity) e;

            int animationId = msg.getAnimationId();
            if (animationId == -1)
            {
                entity.resetAnimationState();
                return;
            }

            AnimationState[] animations = entity.getAnimationStates();
            if (animationId < 0 || animationId >= animations.length)
            {
                LOGGER.warn("Server sent invalid animation for entity: " + e);
                return;
            }

            entity.setAnimationState(animations[animationId]);
        });
    }
}
