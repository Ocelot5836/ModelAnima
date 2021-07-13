package io.github.ocelot.modelanima.api.client.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.ocelot.modelanima.api.client.texture.GeometryTextureManager;
import io.github.ocelot.modelanima.api.common.animation.AnimatedEntity;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.ResourceLocation;

/**
 * <p>Renders an {@link AnimatedEntity} using {@link AnimatedGeometryEntityModel}.</p>
 *
 * @param <T> The type of entity to render
 * @author Ocelot
 */
public abstract class AnimatedEntityRenderer<T extends MobEntity & AnimatedEntity> extends MobRenderer<T, AnimatedGeometryEntityModel<T>>
{
    public AnimatedEntityRenderer(EntityRendererManager rendererManager, ResourceLocation model, float shadowSize)
    {
        super(rendererManager, new AnimatedGeometryEntityModel<>(model), shadowSize);
    }

    @Override
    protected float getBob(T yeti, float partialTicks)
    {
        return (yeti.isNoAnimatonPlaying() ? yeti.tickCount : yeti.getAnimationTick()) + partialTicks;
    }

    @Override
    protected void setupRotations(T entity, MatrixStack matrixStack, float ticksExisted, float rotY, float partialTicks)
    {
        super.setupRotations(entity, matrixStack, ticksExisted, rotY, partialTicks);
        matrixStack.translate(0, -1.5F, 0); // what?
        this.model.setTexture(this.getTextureTableLocation(entity));
        this.model.setAnimations(this.getAnimations(entity));
    }

    @Override
    public ResourceLocation getTextureLocation(T entity)
    {
        return GeometryTextureManager.getAtlas().getAtlasLocation();
    }

    /**
     * Fetches the default animations to play.
     *
     * @param entity The entity to get the animations for
     * @return The animations to play
     */
    public ResourceLocation[] getAnimations(T entity)
    {
        return entity.isNoAnimatonPlaying() ? new ResourceLocation[0] : entity.getAnimationState().getAnimations();
    }

    /**
     * Fetches the texture table location to use for the specified entity.
     *
     * @param entity The entity to get the texture for
     * @return The location of the texture table for that entity
     */
    public abstract ResourceLocation getTextureTableLocation(T entity);
}
