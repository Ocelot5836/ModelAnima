package io.github.ocelot.modelanima.api.client.animation;

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
 * <p>Manages {@link AnimationData} loading using custom loaders, which can then be accessed through {@link #getAnimation(ResourceLocation)}.</p>
 *
 * @author Ocelot
 * @see BackgroundLoader
 * @since 1.0.0
 */
public final class AnimationManager
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Reloader RELOADER = new Reloader();
    private static final DynamicReloader DYNAMIC_RELOADER = new DynamicReloader();
    private static final Set<BackgroundLoader<Map<ResourceLocation, AnimationData>>> LOADERS = new HashSet<>();
    private static final Map<ResourceLocation, AnimationData> ANIMATIONS = new HashMap<>();

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
                ((IReloadableResourceManager) resourceManager).addReloadListener(RELOADER);
            }
        });
    }

    /**
     * Adds the specified animation loader.
     *
     * @param loader The loader to add
     */
    public static void addLoader(BackgroundLoader<Map<ResourceLocation, AnimationData>> loader)
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
     * @return The animation found or {@link AnimationData#EMPTY} if there was no animation
     */
    public static AnimationData getAnimation(ResourceLocation location)
    {
        return ANIMATIONS.computeIfAbsent(location, key ->
        {
            LOGGER.warn("Unknown animation with key '{}'", location);
            return AnimationData.EMPTY;
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
            Map<ResourceLocation, AnimationData> animationData = new HashMap<>();
            return CompletableFuture.allOf(LOADERS.stream().map(animationLoader -> animationLoader.reload(resourceManager, backgroundExecutor, gameExecutor).thenAcceptAsync(pairs ->
            {
                for (Map.Entry<ResourceLocation, AnimationData> entry : pairs.entrySet())
                    if (animationData.put(entry.getKey(), entry.getValue()) != null)
                        LOGGER.warn("Duplicate animation: " + entry.getKey());
            }, gameExecutor)).toArray(CompletableFuture[]::new)).thenCompose(stage::markCompleteAwaitingOthers).thenRunAsync(() ->
            {
                LOGGER.info("Loaded " + animationData.size() + " animations.");
                ANIMATIONS.clear();
                ANIMATIONS.putAll(animationData);
            }, gameExecutor);
        }
    }
}
