package io.github.ocelot.modelanima.api.client.texture;

import com.google.gson.Gson;
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
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * <p>Loads textures from local files each reload.</p>
 *
 * @author Ocelot
 * @since 1.0.0
 */
public class LocalTextureTableProvider implements TextureTableProvider
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new Gson();
    private final Map<ResourceLocation, GeometryModelTextureTable> textures;
    private final String folder;
    private String[] hashTables;

    public LocalTextureTableProvider()
    {
        this("textures/models");
    }

    public LocalTextureTableProvider(@Nullable String folder)
    {
        this.textures = new HashMap<>();
        this.folder = folder;
        this.hashTables = new String[0];
    }

    @Override
    public void addTextures(BiConsumer<ResourceLocation, GeometryModelTextureTable> textureConsumer)
    {
        this.textures.forEach(textureConsumer);
    }

    @Override
    public void addHashTables(Consumer<String> hashTableConsumer)
    {
        for (String hashTable : this.hashTables)
            hashTableConsumer.accept(hashTable);
    }

    @Override
    public CompletableFuture<Void> reload(IStage stage, IResourceManager resourceManager, IProfiler preparationsProfiler, IProfiler reloadProfiler, Executor backgroundExecutor, Executor gameExecutor)
    {
        return CompletableFuture.supplyAsync(() ->
        {
            Map<ResourceLocation, GeometryModelTextureTable> textureLocations = new HashMap<>();
            for (ResourceLocation textureTableLocation : resourceManager.getAllResourceLocations(this.folder == null || this.folder.isEmpty() ? "" : this.folder + "/", name -> name.endsWith(".json")))
            {
                ResourceLocation textureTableName = new ResourceLocation(textureTableLocation.getNamespace(), textureTableLocation.getPath().substring((this.folder == null || this.folder.isEmpty() ? "" : this.folder + "/").length(), textureTableLocation.getPath().length() - ".json".length()));
                if (textureTableName.getPath().equals("hash_tables"))
                    continue;

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
        }, backgroundExecutor).thenAcceptBothAsync(CompletableFuture.supplyAsync(() ->
        {
            Set<String> hashTables = new HashSet<>();
            for (String domain : resourceManager.getResourceNamespaces())
            {
                ResourceLocation hashTableLocation = new ResourceLocation(domain, (this.folder == null || this.folder.isEmpty() ? "" : this.folder + "/") + "hash_tables.json");
                if (!resourceManager.hasResource(hashTableLocation))
                    continue;

                try (IResource resource = resourceManager.getResource(hashTableLocation))
                {
                    hashTables.addAll(Arrays.asList(GSON.fromJson(new InputStreamReader(resource.getInputStream()), String[].class)));
                }
                catch (Exception e)
                {
                    LOGGER.error("Failed to load texture hash table for " + domain, e);
                }
            }
            LOGGER.info("Loaded " + hashTables.size() + " hash tables.");
            return hashTables.toArray(new String[0]);
        }, backgroundExecutor), (textureLocations, hashTables) ->
        {
            this.textures.clear();
            this.textures.putAll(textureLocations);
            this.hashTables = hashTables;
        }, gameExecutor).thenCompose(stage::markCompleteAwaitingOthers);
    }
}
