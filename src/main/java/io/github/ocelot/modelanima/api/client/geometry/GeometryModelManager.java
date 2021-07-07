package io.github.ocelot.modelanima.api.client.geometry;

import io.github.ocelot.modelanima.api.common.animation.AnimationData;
import io.github.ocelot.modelanima.api.common.util.BackgroundLoader;
import io.github.ocelot.modelanima.core.client.util.DynamicReloader;
import net.minecraft.client.Minecraft;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * <p>Manages {@link AnimationData} loading using custom loaders, which can then be accessed through {@link #getModel(ResourceLocation)}.</p>
 *
 * @author Ocelot
 * @see BackgroundLoader
 * @since 1.0.0
 */
public final class GeometryModelManager
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Reloader RELOADER = new Reloader();
    private static final DynamicReloader DYNAMIC_RELOADER = new DynamicReloader();
    private static final Set<BackgroundLoader<Map<ResourceLocation, GeometryModel>>> LOADERS = new HashSet<>();
    private static final Map<ResourceLocation, GeometryModel> MODELS = new HashMap<>();

    static
    {
        DYNAMIC_RELOADER.addListener(RELOADER);
    }

    public static void init(IEventBus bus)
    {
        bus.addListener(EventPriority.NORMAL, false, ColorHandlerEvent.Block.class, event ->
        {
            IResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
            if (resourceManager instanceof IReloadableResourceManager)
            {
                ((IReloadableResourceManager) resourceManager).registerReloadListener(RELOADER);
            }
        });
    }

    /**
     * Adds the specified animation loader.
     *
     * @param loader The loader to add
     */
    public static void addLoader(BackgroundLoader<Map<ResourceLocation, GeometryModel>> loader)
    {
        LOADERS.add(loader);
    }

    /**
     * <p>Reloads all animations and opens the loading gui if specified.</p>
     *
     * @param showLoadingScreen Whether or not to show the loading screen during the reload
     * @return A future for when the reload is complete
     */
    public static CompletableFuture<Unit> reload(boolean showLoadingScreen)
    {
        return DYNAMIC_RELOADER.reload(showLoadingScreen);
    }

    /**
     * Fetches an animation by the specified name.
     *
     * @param location The name of the model
     * @return The bedrock model found or {@link GeometryModel#EMPTY} if there was no model
     */
    public static GeometryModel getModel(ResourceLocation location)
    {
        return MODELS.computeIfAbsent(location, key ->
        {
            LOGGER.warn("Unknown geometry model with key '{}'", location);
            return GeometryModel.EMPTY;
        });
    }

    /**
     * @return Whether or not a reload is currently happening
     */
    public static boolean isReloading()
    {
        return DYNAMIC_RELOADER.isReloading();
    }

    private static class Reloader implements IFutureReloadListener
    {
        @Override
        public CompletableFuture<Void> reload(IStage stage, IResourceManager resourceManager, IProfiler preparationsProfiler, IProfiler reloadProfiler, Executor backgroundExecutor, Executor gameExecutor)
        {
            Map<ResourceLocation, GeometryModel> geometryModels = new HashMap<>();
            return CompletableFuture.allOf(LOADERS.stream().map(modelLoader -> modelLoader.reload(resourceManager, backgroundExecutor, gameExecutor).thenAcceptAsync(pairs ->
            {
                for (Map.Entry<ResourceLocation, GeometryModel> entry : pairs.entrySet())
                    if (geometryModels.put(entry.getKey(), entry.getValue()) != null)
                        LOGGER.warn("Duplicate geometry model: " + entry.getKey());
            }, gameExecutor)).toArray(CompletableFuture[]::new)).thenCompose(stage::wait).thenRunAsync(() ->
            {
                LOGGER.info("Loaded " + geometryModels.size() + " geometry models.");
                MODELS.clear();
                MODELS.putAll(geometryModels);
            }, gameExecutor);
        }
    }
}
