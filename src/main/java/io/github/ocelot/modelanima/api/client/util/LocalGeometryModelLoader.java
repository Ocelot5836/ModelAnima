package io.github.ocelot.modelanima.api.client.util;

import io.github.ocelot.modelanima.api.client.geometry.BedrockGeometryModel;
import io.github.ocelot.modelanima.api.client.geometry.GeometryModel;
import io.github.ocelot.modelanima.api.common.geometry.GeometryModelData;
import io.github.ocelot.modelanima.api.common.geometry.GeometryModelLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * <p>Automatically loads {@link GeometryModel} from local JSON, which can then be accessed through {@link #getModel(ResourceLocation)}.</p>
 *
 * @author Ocelot
 * @since 1.0.0
 */
public final class LocalGeometryModelLoader
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<ResourceLocation, GeometryModel> RENDERERS = new HashMap<>();

    /**
     * <p>Enables loading of models from resources automatically.</p>
     *
     * @param bus The mod event bus to register events on
     */
    public static void init(IEventBus bus)
    {
        bus.addListener(EventPriority.NORMAL, false, ColorHandlerEvent.Block.class, event ->
        {
            IResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
            if (resourceManager instanceof IReloadableResourceManager)
            {
                ((IReloadableResourceManager) resourceManager).addReloadListener(new Reloader());
            }
        });
    }

    /**
     * Fetches a geometry model by the specified name.
     *
     * @param name The name of the model
     * @return The bedrock model found or {@link GeometryModel#EMPTY}
     */
    public static GeometryModel getModel(ResourceLocation name)
    {
        return RENDERERS.getOrDefault(name, GeometryModel.EMPTY);
    }

    private static class Reloader implements IFutureReloadListener
    {
        @Override
        public CompletableFuture<Void> reload(IStage stage, IResourceManager resourceManager, IProfiler preparationsProfiler, IProfiler reloadProfiler, Executor backgroundExecutor, Executor gameExecutor)
        {
            return CompletableFuture.supplyAsync(() ->
            {
                Map<ResourceLocation, GeometryModelData> modelLocations = new HashMap<>();
                for (ResourceLocation modelLocation : resourceManager.getAllResourceLocations("models/geometry/", name -> name.endsWith(".json")))
                {
                    ResourceLocation modelName = new ResourceLocation(modelLocation.getNamespace(), modelLocation.getPath().substring("models/geometry/".length(), modelLocation.getPath().length() - ".json".length()));
                    try (IResource resource = resourceManager.getResource(modelLocation))
                    {
                        modelLocations.put(modelName, GeometryModelLoader.parseModel(IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8)));
                    }
                    catch (Exception e)
                    {
                        LOGGER.error("Failed to load geometry model '" + modelName + "'", e);
                    }
                }
                LOGGER.info("Loaded " + modelLocations.size() + " geometry models.");
                return modelLocations;
            }, backgroundExecutor).thenCompose(stage::markCompleteAwaitingOthers).thenAcceptAsync(modelLocations ->
            {
                RENDERERS.clear();
                modelLocations.forEach((name, model) ->
                {
                    try
                    {
                        RENDERERS.put(name, new BedrockGeometryModel(model));
                    }
                    catch (Exception e)
                    {
                        LOGGER.error("Failed to create model '" + name + "'", e);
                    }
                });
            }, gameExecutor);
        }
    }
}