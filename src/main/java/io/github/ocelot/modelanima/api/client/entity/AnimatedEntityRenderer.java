package io.github.ocelot.modelanima.api.client.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.ocelot.modelanima.api.common.animation.AnimatedEntity;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.entity.MobEntity;

/**
 * <p>Renders an {@link AnimatedEntity} using {@link AnimatedGeometryEntityModel}.</p>
 *
 * @param <T> The type of entity to render
 * @param <M> The model to use
 * @author Ocelot
 */
public abstract class AnimatedEntityRenderer<T extends MobEntity & AnimatedEntity, M extends AnimatedGeometryEntityModel<T>> extends MobRenderer<T, M>
{
    public AnimatedEntityRenderer(EntityRendererManager rendererManager, M model, float shadowSize)
    {
        super(rendererManager, model, shadowSize);
    }

    @Override
    protected void setupRotations(T entity, MatrixStack matrixStack, float ticksExisted, float rotY, float partialTicks)
    {
        super.setupRotations(entity, matrixStack, ticksExisted, rotY, partialTicks);
        this.model.setTexture(this.getTextureLocation(entity));
        this.model.setAnimations(entity.getAnimationState().getAnimations());
    }
}
