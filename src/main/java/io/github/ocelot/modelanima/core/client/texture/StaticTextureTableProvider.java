package io.github.ocelot.modelanima.core.client.texture;

import io.github.ocelot.modelanima.api.client.texture.TextureTableProvider;
import io.github.ocelot.modelanima.api.common.geometry.texture.GeometryModelTextureTable;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author Ocelot
 */
public class StaticTextureTableProvider implements TextureTableProvider
{
    private final ResourceLocation location;
    private final GeometryModelTextureTable texture;
    private final String hashTable;

    public StaticTextureTableProvider(ResourceLocation location, GeometryModelTextureTable texture, @Nullable String hashTable)
    {
        this.location = location;
        this.texture = texture;
        this.hashTable = hashTable;
    }

    @Override
    public void addTextures(BiConsumer<ResourceLocation, GeometryModelTextureTable> textureConsumer)
    {
        textureConsumer.accept(this.location, this.texture);
    }

    @Override
    public void addHashTables(Consumer<String> hashTableConsumer)
    {
        if (this.hashTable != null)
            hashTableConsumer.accept(this.hashTable);
    }

    @Override
    public CompletableFuture<Void> reload(IStage stage, IResourceManager resourceManager, IProfiler preparationsProfiler, IProfiler reloadProfiler, Executor backgroundExecutor, Executor gameExecutor)
    {
        return CompletableFuture.completedFuture(null);
    }
}
