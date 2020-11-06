package io.github.ocelot.modelanima.api.client.texture;

import io.github.ocelot.modelanima.api.common.geometry.GeometryModelLoader;
import io.github.ocelot.modelanima.api.common.geometry.texture.GeometryModelTextureTable;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;

/**
 * <p>Loads textures from local files each reload.</p>
 *
 * @author Ocelot
 * @since 1.0.0
 */
public class LocalTextureTableProvider implements TextureTableProvider
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final Map<ResourceLocation, GeometryModelTextureTable> textures;
    private final String folder;

    public LocalTextureTableProvider()
    {
        this("textures/models");
    }

    public LocalTextureTableProvider(@Nullable String folder)
    {
        this.textures = new HashMap<>();
        this.folder = folder;
    }

    @Override
    public void addTextures(BiConsumer<ResourceLocation, GeometryModelTextureTable> textureConsumer)
    {
        this.textures.forEach(textureConsumer);
    }

    @Override
    public CompletableFuture<Void> reload(IStage stage, IResourceManager resourceManager, IProfiler preparationsProfiler, IProfiler reloadProfiler, Executor backgroundExecutor, Executor gameExecutor)
    {
        return CompletableFuture.supplyAsync(() ->
        {
            Map<ResourceLocation, GeometryModelTextureTable> textureLocations = new HashMap<>();
            for (ResourceLocation textureTableLocation : resourceManager.getAllResourceLocations(this.folder == null || this.folder.isEmpty() ? "" : this.folder + "/", name -> name.endsWith(".json")))
            {
                ResourceLocation textureTableName = new ResourceLocation(textureTableLocation.getNamespace(), textureTableLocation.getPath().substring("textures/models/".length(), textureTableLocation.getPath().length() - ".json".length()));
                try (IResource resource = resourceManager.getResource(textureTableLocation))
                {
                    textureLocations.put(textureTableName, GeometryModelLoader.parseTextures(new InputStreamReader(resource.getInputStream())));
                }
                catch (Exception e)
                {
                    LOGGER.error("Failed to load texture table '" + textureTableName + "'", e);
                }
            }
            LOGGER.info("Loaded " + textureLocations.size() + " model texture tables.");
            return textureLocations;
        }, backgroundExecutor).thenCompose(stage::markCompleteAwaitingOthers).thenAcceptAsync(textureLocations ->
        {
            this.textures.clear();
            this.textures.putAll(textureLocations);
        }, gameExecutor);
    }
}
