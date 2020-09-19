package io.github.ocelot.modelanima.client.geometry;

import io.github.ocelot.modelanima.common.geometry.GeometryModelData;
import io.github.ocelot.modelanima.common.geometry.GeometryModelLoader;
import io.github.ocelot.modelanima.common.geometry.texture.GeometryModelTexture;
import io.github.ocelot.modelanima.common.geometry.texture.GeometryModelTextureTable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.TextureManager;
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
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * <p>Automatically loads {@link GeometryModel} and {@link GeometryModelTextureTable} from JSON.</p>
 *
 * @author Ocelot
 * @since 1.0.0
 */
public final class LocalGeometryModelLoader
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final GeometryModel MISSING = new BedrockGeometryModel(RenderType::getEntitySolid, new GeometryModelData());
    private static final Map<ResourceLocation, GeometryModel> RENDERERS = new HashMap<>();
    private static final Map<ResourceLocation, GeometryModelTextureTable> TEXTURES = new HashMap<>();

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
     * @return The bedrock model found or {@link #MISSING}
     */
    public static GeometryModel getModel(ResourceLocation name)
    {
        return RENDERERS.getOrDefault(name, MISSING);
    }

    /**
     * Fetches a texture table by the specified name.
     *
     * @param name The name of the texture table
     * @return The table found or {@link GeometryModelTextureTable#EMPTY}
     */
    public static GeometryModelTextureTable getTextureTable(ResourceLocation name)
    {
        return TEXTURES.getOrDefault(name, GeometryModelTextureTable.EMPTY);
    }

    private static class Reloader implements IFutureReloadListener
    {
        @Override
        public CompletableFuture<Void> reload(IStage stage, IResourceManager resourceManager, IProfiler preparationsProfiler, IProfiler reloadProfiler, Executor backgroundExecutor, Executor gameExecutor)
        {
            return CompletableFuture.allOf(CompletableFuture.supplyAsync(() ->
                    {
                        Map<ResourceLocation, GeometryModelTextureTable> textureLocations = new HashMap<>();
                        for (ResourceLocation textureTableLocation : resourceManager.getAllResourceLocations("textures/models/", name -> name.endsWith(".json")))
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
                        TEXTURES.clear();
                        TEXTURES.putAll(textureLocations);
                    }, gameExecutor).thenRunAsync(() -> CompletableFuture.allOf(TEXTURES.values().stream().map(table -> CompletableFuture.allOf(table.getTextures().stream().map(texture -> loadTexture(Minecraft.getInstance().getTextureManager(), texture, stage, backgroundExecutor, gameExecutor)).toArray(CompletableFuture[]::new))).toArray(CompletableFuture[]::new)).join(), backgroundExecutor),

                    CompletableFuture.supplyAsync(() ->
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
                        modelLocations.forEach((name, model) -> RENDERERS.put(name, new BedrockGeometryModel(RenderType::getEntityCutoutNoCull, model)));
                    }, gameExecutor));
        }

        private static CompletableFuture<Void> loadTexture(TextureManager textureManager, GeometryModelTexture cosmeticTexture, IStage stage, Executor backgroundExecutor, Executor gameExecutor)
        {
            ResourceLocation location = cosmeticTexture.getLocation();
            if ("missingno".equals(location.getPath()))
                return CompletableFuture.allOf();

            return cosmeticTexture.getType() == GeometryModelTexture.Type.LOCATION ? textureManager.loadAsync(location, backgroundExecutor) : CompletableFuture.supplyAsync(() ->
            {
                try (CloseableHttpClient client = HttpClients.custom().setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11").build())
                {
                    HttpGet get = new HttpGet(cosmeticTexture.getData());
                    try (CloseableHttpResponse response = client.execute(get))
                    {
                        StatusLine statusLine = response.getStatusLine();
                        if (statusLine.getStatusCode() != 200)
                            throw new IOException("Failed to connect to '" + cosmeticTexture.getData() + "'. " + statusLine.getStatusCode() + " " + statusLine.getReasonPhrase());
                        return NativeImage.read(response.getEntity().getContent());
                    }
                }
                catch (Exception e)
                {
                    LOGGER.error("Failed to load online texture from '" + cosmeticTexture.getData() + "'", e);
                    return null;
                }
            }, backgroundExecutor).thenCompose(stage::markCompleteAwaitingOthers).thenAcceptAsync(image ->
            {
                if (image != null)
                {
                    textureManager.loadTexture(location, new DynamicTexture(image));
                }
            }, gameExecutor);
        }
    }
}
