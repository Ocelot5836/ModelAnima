package io.github.ocelot.modelanima.api.client.texture;

import io.github.ocelot.modelanima.api.client.geometry.GeometryModelRenderer;
import io.github.ocelot.modelanima.api.common.texture.GeometryModelTextureTable;
import io.github.ocelot.modelanima.core.client.texture.GeometryTextureSpriteUploader;
import io.github.ocelot.modelanima.core.client.texture.LocalTextureTableLoader;
import io.github.ocelot.modelanima.core.client.texture.StaticTextureTableLoader;
import io.github.ocelot.modelanima.core.client.util.DynamicReloader;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.util.perf.Profiler;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * <p>Manages textures for all geometry models. Used by {@link GeometryModelRenderer}.</p>
 *
 * @author Ocelot
 * @since 1.0.0
 */
public class GeometryTextureManager
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Reloader RELOADER = new Reloader();
    private static final DynamicReloader DYNAMIC_RELOADER = new DynamicReloader();
    private static final Set<TextureTableLoader> PROVIDERS = new HashSet<>();
    private static final Map<ResourceLocation, GeometryModelTextureTable> TEXTURES = new HashMap<>();
    private static GeometryTextureSpriteUploader spriteUploader;

    static
    {
        DYNAMIC_RELOADER.addListener(RELOADER);
    }

    @ApiStatus.Internal
    public static void init(IEventBus bus)
    {
        bus.addListener(EventPriority.NORMAL, true, ColorHandlerEvent.Block.class, event ->
        {
            spriteUploader = new GeometryTextureSpriteUploader(Minecraft.getInstance().getTextureManager());
            ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
            if (resourceManager instanceof ReloadableResourceManager)
            {
                ((ReloadableResourceManager) resourceManager).registerReloadListener(RELOADER);
            }
        });
        addProvider(new LocalTextureTableLoader());
    }

    /**
     * Adds the specified texture under the specified location. This will not ever change or be unloaded.
     *
     * @param location  The location to upload under
     * @param texture   The texture table to load
     * @param hashTable The table to load hashes from or <code>null</code> for no hashes
     */
    public static void addTexture(ResourceLocation location, GeometryModelTextureTable texture, @Nullable String hashTable)
    {
        addProvider(new StaticTextureTableLoader(location, texture, hashTable));
    }

    /**
     * Adds the specified provider to the reloading task. Textures are reloaded at user discretion.
     *
     * @param provider The provider for textures
     */
    public static void addProvider(TextureTableLoader provider)
    {
        PROVIDERS.add(provider);
    }

    /**
     * Fetches a texture table by the specified location.
     *
     * @param location The location of the texture table
     * @return The texture table with the name or {@link GeometryModelTextureTable#EMPTY} if there was no texture
     */
    public static GeometryModelTextureTable getTextures(ResourceLocation location)
    {
        return TEXTURES.computeIfAbsent(location, key ->
        {
            LOGGER.warn("Unknown texture table with key '{}'", location);
            return GeometryModelTextureTable.EMPTY;
        });
    }

    /**
     * <p>Reloads all textures and opens the loading gui if specified.</p>
     *
     * @param showLoadingScreen Whether to show the loading screen during the reload
     * @return A future for when the reload is complete
     */
    public static CompletableFuture<Unit> reload(boolean showLoadingScreen)
    {
        return DYNAMIC_RELOADER.reload(showLoadingScreen);
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
     * @return Whether the texture manager is currently happening
     */
    public static boolean isReloading()
    {
        return DYNAMIC_RELOADER.isReloading();
    }

    private static class Reloader implements PreparableReloadListener
    {
        @Override
        public CompletableFuture<Void> reload(PreparationBarrier stage, ResourceManager resourceManager, ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor)
        {
            return CompletableFuture.allOf(PROVIDERS.stream().map(provider -> provider.reload(stage, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor)).toArray(CompletableFuture[]::new)).thenApplyAsync(a ->
            {
                Map<ResourceLocation, GeometryModelTextureTable> textures = new HashMap<>();
                Set<String> hashTables = new HashSet<>();
                PROVIDERS.forEach(provider -> provider.addTextures((location, texture) ->
                {
                    if (textures.put(location, texture) != null)
                        LOGGER.warn("Texture at location '" + location + "' already exists and is being overridden.");
                }));
                PROVIDERS.forEach(provider -> provider.addHashTables(hashTables::add));
                return Pair.of(textures, hashTables.toArray(new String[0]));
            }, backgroundExecutor)
                    .thenCompose(pair -> spriteUploader.setTextures(pair.getLeft(), pair.getRight()).reload(stage, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor))
                    .thenCompose(stage::wait).thenAcceptAsync(textures ->
                    {
                        TEXTURES.clear();
                        PROVIDERS.forEach(provider -> provider.addTextures((location, texture) ->
                        {
                            if (TEXTURES.put(location, texture) != null)
                                LOGGER.warn("Texture at location '" + location + "' already exists and is being overridden.");
                        }));
                    }, gameExecutor);
        }
    }
}
