package io.github.ocelot.modelanima.api.client.geometry;

import io.github.ocelot.modelanima.api.common.geometry.GeometryModelData;
import io.github.ocelot.modelanima.api.common.geometry.GeometryModelParser;
import io.github.ocelot.modelanima.api.common.util.BackgroundLoader;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * <p>Loads {@link GeometryModel} from local locations.</p>
 *
 * @author Ocelot
 * @since 1.0.0
 */
public final class LocalGeometryModelLoader implements BackgroundLoader<Map<ResourceLocation, GeometryModel>>
{
    private static final Logger LOGGER = LogManager.getLogger();

    private final String folder;

    public LocalGeometryModelLoader()
    {
        this("models/geometry");
    }

    public LocalGeometryModelLoader(String folder)
    {
        this.folder = folder.isEmpty() ? "" : folder + "/";
    }

    @Override
    public CompletableFuture<Map<ResourceLocation, GeometryModel>> reload(IResourceManager resourceManager, Executor backgroundExecutor, Executor gameExecutor)
    {
        return CompletableFuture.supplyAsync(() ->
        {
            Map<ResourceLocation, GeometryModelData> modelLocations = new HashMap<>();
            for (ResourceLocation modelLocation : resourceManager.listResources(this.folder, name -> name.endsWith(".json")))
            {
                try (IResource resource = resourceManager.getResource(modelLocation))
                {
                    GeometryModelData[] models = GeometryModelParser.parseModel(IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8));
                    for (GeometryModelData model : models)
                    {
                        ResourceLocation id = new ResourceLocation(modelLocation.getNamespace(), model.getDescription().getIdentifier());
                        if (modelLocations.put(id, model) != null)
                            LOGGER.warn("Duplicate geometry model with id '" + id + "'");
                    }
                }
                catch (Exception e)
                {
                    LOGGER.error("Failed to load geometry file '" + modelLocation.getNamespace() + ":" + modelLocation.getPath().substring(this.folder.length(), modelLocation.getPath().length() - 5) + "'", e);
                }
            }
            LOGGER.info("Loaded " + modelLocations.size() + " geometry models.");
            return modelLocations;
        }, backgroundExecutor).thenApplyAsync(modelLocations ->
        {
            Map<ResourceLocation, GeometryModel> models = new HashMap<>();
            modelLocations.forEach((name, model) ->
            {
                try
                {
                    models.put(name, new BedrockGeometryModel(model));
                }
                catch (Exception e)
                {
                    LOGGER.error("Failed to create model: " + name, e);
                }
            });
            return models;
        }, gameExecutor);
    }
}
