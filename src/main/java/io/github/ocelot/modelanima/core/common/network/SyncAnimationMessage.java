package io.github.ocelot.modelanima.core.common.network;

import io.github.ocelot.modelanima.api.common.animation.AnimatedEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.ArrayUtils;

/**
 * @author Ocelot
 */
public class SyncAnimationMessage
{
    private final int entityId;
    private final int animationId;

    public <T extends Entity & AnimatedEntity> SyncAnimationMessage(T entity)
    {
        this.entityId = entity.getId();
        this.animationId = ArrayUtils.indexOf(entity.getAnimationStates(), entity.getAnimationState());
    }

    public SyncAnimationMessage(FriendlyByteBuf buf)
    {
        this.entityId = buf.readVarInt();
        this.animationId = buf.readVarInt();
    }

    public void write(FriendlyByteBuf buf)
    {
        buf.writeVarInt(this.entityId);
        buf.writeVarInt(this.animationId);
    }

    @OnlyIn(Dist.CLIENT)
    public int getEntityId()
    {
        return entityId;
    }

    @OnlyIn(Dist.CLIENT)
    public int getAnimationId()
    {
        return animationId;
    }
}
