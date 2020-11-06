package io.github.ocelot.modelanima.api.client.texture;

import io.github.ocelot.modelanima.api.common.geometry.texture.GeometryModelTextureTable;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;

/**
 * <p>A static implementation of {@link TextureTableProvider}.</p>
 *
 * @author Ocelot
 * @since 1.0.0
 */
public class StaticTextureTableProvider implements TextureTableProvider
{
    private final ResourceLocation location;
    private final GeometryModelTextureTable texture;

    public StaticTextureTableProvider(ResourceLocation location, GeometryModelTextureTable texture)
    {
        this.location = location;
        this.texture = texture;
    }

    @Override
    public void addTextures(BiConsumer<ResourceLocation, GeometryModelTextureTable> textureConsumer)
    {
        textureConsumer.accept(this.location, this.texture);
    }

    @Override
    public CompletableFuture<Void> reload(IStage stage, IResourceManager resourceManager, IProfiler preparationsProfiler, IProfiler reloadProfiler, Executor backgroundExecutor, Executor gameExecutor)
    {
        return CompletableFuture.completedFuture(null);
    }
}
