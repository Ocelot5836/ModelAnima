package io.github.ocelot.modelanima.api.client.texture;

import io.github.ocelot.modelanima.api.common.geometry.texture.GeometryModelTextureTable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ResourceLoadProgressGui;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * <p>Manages textures for</p>
 *
 * @author Ocelot
 * @since 1.0.0
 */
public class GeometryTextureManager
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Reloader RELOADER = new Reloader();
    private static final Set<TextureTableProvider> PROVIDERS = new HashSet<>();
    private static final Map<ResourceLocation, GeometryModelTextureTable> TEXTURES = new HashMap<>();
    private static GeometryTextureSpriteUploader spriteUploader;

    private static boolean dirty;
    private static IAsyncReloader asyncReloader;

    /**
     * <p>Enables loading of geometry textures.</p>
     *
     * @param bus The mod event bus to register events on
     */
    public static void init(IEventBus bus)
    {
        bus.addListener(EventPriority.NORMAL, false, ColorHandlerEvent.Block.class, event ->
        {
            spriteUploader = new GeometryTextureSpriteUploader(Minecraft.getInstance().getTextureManager(), GeometryTextureSpriteUploader.ATLAS_LOCATION);
            IResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
            if (resourceManager instanceof IReloadableResourceManager)
            {
                ((IReloadableResourceManager) resourceManager).addReloadListener(RELOADER);
            }
        });
        MinecraftForge.EVENT_BUS.addListener(GeometryTextureManager::tick);
    }

    private static void tick(TickEvent.ClientTickEvent event)
    {
        if (dirty)
        {
            dirty = false;
            reload();
        }
    }

    /**
     * Adds the specified texture under the specified location. This will not ever change or be unloaded.
     *
     * @param location The location to upload under
     * @param texture  The texture table to load
     */
    public static void addTexture(ResourceLocation location, GeometryModelTextureTable texture)
    {
        addProvider(new StaticTextureTableProvider(location, texture));
    }

    /**
     * Adds the specified provider to the reloading task. Textures are reloaded at user discretion.
     *
     * @param provider The provider for textures
     */
    public static void addProvider(TextureTableProvider provider)
    {
        PROVIDERS.add(provider);
        if (spriteUploader != null)
            dirty = true;
    }

    /**
     * Fetches a texture table by the specified location.
     *
     * @param location The location of the texture table
     * @return The texture table with the name or {@link GeometryModelTextureTable#EMPTY} if there was no texture
     */
    public static GeometryModelTextureTable get(ResourceLocation location)
    {
        return TEXTURES.computeIfAbsent(location, key ->
        {
            LOGGER.warn("Unknown texture table with key '{}'", location);
            return GeometryModelTextureTable.EMPTY;
        });
    }

    /**
     * @return The base geometry atlas texture
     */
    public static GeometryAtlasTexture getAtlas()
    {
        return spriteUploader;
    }

    /**
     * @return A collection of all textures loaded
     */
    public static Collection<GeometryModelTextureTable> getAllTextures()
    {
        return TEXTURES.values();
    }

    /**
     * <p>Reloads all textures and opens the loading gui if specified.</p>
     *
     * @param showLoadingScreen Whether or not to show the loading screen during the reload
     * @return A future for when the reload is complete
     */
    public static CompletableFuture<Unit> reload(boolean showLoadingScreen)
    {
        if (asyncReloader != null)
            return asyncReloader.onceDone();
        asyncReloader = AsyncReloader.create(Minecraft.getInstance().getResourceManager(), Collections.singletonList(RELOADER), Util.getServerExecutor(), Minecraft.getInstance(), CompletableFuture.completedFuture(Unit.INSTANCE));
        if (showLoadingScreen)
            Minecraft.getInstance().setLoadingGui(new ResourceLoadProgressGui(Minecraft.getInstance(), asyncReloader, error -> error.ifPresent(LOGGER::error), true));
        return asyncReloader.onceDone().thenApplyAsync(unit ->
        {
            asyncReloader = null;
            return unit;
        });
    }

    /**
     * @return Whether or not a reload is currently happening
     */
    public static boolean isReloading()
    {
        return asyncReloader != null;
    }

    private static class Reloader implements IFutureReloadListener
    {
        @Override
        public CompletableFuture<Void> reload(IStage stage, IResourceManager resourceManager, IProfiler preparationsProfiler, IProfiler reloadProfiler, Executor backgroundExecutor, Executor gameExecutor)
        {
            return CompletableFuture.allOf(PROVIDERS.stream().map(provider -> provider.reload(stage, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor)).toArray(CompletableFuture[]::new)).thenApplyAsync(a ->
            {
                Map<ResourceLocation, GeometryModelTextureTable> textures = new HashMap<>();
                PROVIDERS.forEach(provider -> provider.addTextures((location, texture) ->
                {
                    if (textures.containsKey(location))
                        LOGGER.warn("Texture at location '" + location + "' already exists and is being overridden.");
                    textures.put(location, texture);
                }));
                return textures;
            }, backgroundExecutor)
                    .thenCompose((textures) -> spriteUploader.setTextures(textures).reload(stage, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor))
                    .thenCompose(stage::markCompleteAwaitingOthers).thenRunAsync(() ->
                    {
                        TEXTURES.clear();
                        PROVIDERS.forEach(provider -> provider.addTextures((location, texture) ->
                        {
                            if (TEXTURES.containsKey(location))
                                LOGGER.warn("Texture at location '" + location + "' already exists and is being overridden.");
                            TEXTURES.put(location, texture);
                        }));
                    }, gameExecutor);
        }
    }
}
