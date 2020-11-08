package io.github.ocelot.modelanima.api.client.texture;

import io.github.ocelot.modelanima.api.common.geometry.texture.GeometryModelTextureTable;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.util.ResourceLocation;

import java.util.function.BiConsumer;

/**
 * <p>Reloads and uploads textures to {@link GeometryTextureManager}.</p>
 *
 * @author Ocelot
 * @since 1.0.0
 */
public interface TextureTableProvider extends IFutureReloadListener
{
    /**
     * Adds all textures to the provided consumer.
     *
     * @param textureConsumer The consumer for textures
     */
    void addTextures(BiConsumer<ResourceLocation, GeometryModelTextureTable> textureConsumer);
}