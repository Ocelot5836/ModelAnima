package io.github.ocelot.modelanima.core.client.texture;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.gson.JsonObject;
import io.github.ocelot.modelanima.ModelAnima;
import io.github.ocelot.modelanima.api.client.texture.GeometryAtlasTexture;
import io.github.ocelot.modelanima.api.common.geometry.texture.GeometryModelTexture;
import io.github.ocelot.modelanima.api.common.geometry.texture.GeometryModelTextureTable;
import io.github.ocelot.modelanima.api.common.util.FileCache;
import io.github.ocelot.modelanima.core.common.util.HashedTextureCache;
import io.github.ocelot.modelanima.core.common.util.TimedTextureCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.ReloadListener;
import net.minecraft.crash.ReportedException;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.data.IMetadataSectionSerializer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Bootstrap;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Ocelot
 */
public class GeometryTextureSpriteUploader extends ReloadListener<AtlasTexture.SheetData> implements GeometryAtlasTexture, AutoCloseable
{
    public static final ResourceLocation ATLAS_LOCATION = new ResourceLocation(ModelAnima.DOMAIN, "textures/atlas/geometry.png");
    private static final Logger LOGGER = LogManager.getLogger();
    private final AtlasTexture textureAtlas;
    private final Set<GeometryModelTexture> textures;
    private String[] hashTables;

    public GeometryTextureSpriteUploader(TextureManager textureManager)
    {
        this.textureAtlas = new AtlasTexture(ATLAS_LOCATION);
        this.textures = new HashSet<>();
        this.hashTables = new String[0];
        textureManager.loadTexture(this.textureAtlas.getTextureLocation(), this.textureAtlas);
    }

    @Override
    public ResourceLocation getAtlasLocation()
    {
        return ATLAS_LOCATION;
    }

    @Override
    public TextureAtlasSprite getSprite(ResourceLocation location)
    {
        return this.textureAtlas.getSprite(location);
    }

    @Override
    protected AtlasTexture.SheetData prepare(IResourceManager resourceManager, IProfiler profiler)
    {
        try (OnlineRepository onlineRepository = new OnlineRepository(this.hashTables))
        {
            profiler.startTick();
            profiler.startSection("stitching");
            Stopwatch stopwatch = Stopwatch.createStarted();
            AtlasTexture.SheetData sheetData = this.textureAtlas.stitch(new OnlineResourceManager(resourceManager, onlineRepository, this.textures.stream().filter(texture -> texture.getType() == GeometryModelTexture.Type.ONLINE).collect(Collectors.toSet())), this.textures.stream().filter(texture -> texture.getType() == GeometryModelTexture.Type.LOCATION || texture.getType() == GeometryModelTexture.Type.ONLINE).map(GeometryModelTexture::getLocation).distinct(), profiler, Minecraft.getInstance().gameSettings.mipmapLevels);
            stopwatch.stop();
            profiler.endSection();
            profiler.endTick();
            LOGGER.debug("Took " + stopwatch + " to process " + this.textures.size() + " geometry textures");
            return sheetData;
        }
    }

    @Override
    protected void apply(AtlasTexture.SheetData sheetData, IResourceManager resourceManager, IProfiler profiler)
    {
        profiler.startTick();
        profiler.startSection("upload");
        this.textureAtlas.upload(sheetData);
        profiler.endSection();
        profiler.endTick();
    }

    @Override
    public void close()
    {
        this.textureAtlas.clear();
    }

    public GeometryTextureSpriteUploader setTextures(Map<ResourceLocation, GeometryModelTextureTable> textures, String[] hashTables)
    {
        this.textures.clear();
        this.textures.addAll(textures.values().stream().flatMap(table -> table.getTextures().stream()).collect(Collectors.toSet()));
        this.hashTables = hashTables;
        return this;
    }

    private static class OnlineResourceManager implements IResourceManager
    {
        private final IResourceManager parent;
        private final OnlineRepository repository;
        private final Set<String> uncached;
        private final Map<String, Pair<CompletableFuture<Path>, CompletableFuture<JsonObject>>> onlineLocations;

        private OnlineResourceManager(IResourceManager parent, OnlineRepository repository, Set<GeometryModelTexture> onlineTextures)
        {
            this.parent = parent;
            this.repository = repository;
            this.uncached = onlineTextures.stream().filter(texture -> !texture.canCache()).map(GeometryModelTexture::getData).collect(Collectors.toSet());
            this.onlineLocations = onlineTextures.stream().map(GeometryModelTexture::getData).distinct().collect(Collectors.toMap(url -> url, this::updateCache));
        }

        private Pair<CompletableFuture<Path>, CompletableFuture<JsonObject>> updateCache(String url)
        {
            String metadataUrl;
            String extension = FilenameUtils.getExtension(url);
            String[] urlParts = url.split("." + extension);
            if (urlParts.length <= 1)
            {
                metadataUrl = url + ".mcmeta";
            }
            else
            {
                metadataUrl = urlParts[0] + extension + ".mcmeta" + urlParts[1];
            }

            CompletableFuture<Path> texturePath = this.repository.requestResource(url, !this.uncached.contains(url), false);
            CompletableFuture<JsonObject> metadataPath = this.repository.requestResource(metadataUrl, false, true).thenApplyAsync(path ->
            {
                InputStream stream = read(CompletableFuture.completedFuture(path));
                if (stream == null)
                    return null;

                try (BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)))
                {
                    return JSONUtils.fromJson(bufferedreader);
                }
                catch (Exception ignored)
                {
                }

                return null;
            }, Util.getRenderingService());
            return Pair.of(texturePath, metadataPath);
        }

        @Override
        public Set<String> getResourceNamespaces()
        {
            return this.parent.getResourceNamespaces();
        }

        @Override
        public IResource getResource(ResourceLocation resourceLocation) throws IOException
        {
            String url = parseUrl(new ResourceLocation(resourceLocation.getNamespace(), resourceLocation.getPath().substring(9, resourceLocation.getPath().length() - 4)));
            if (url != null)
            {
                Stopwatch stopwatch = Stopwatch.createStarted();
                if (!this.onlineLocations.containsKey(url))
                    throw new IOException("Failed to fetch texture data from '" + url + "'");

                Pair<CompletableFuture<Path>, CompletableFuture<JsonObject>> files = this.onlineLocations.get(url);
                InputStream textureStream = read(files.getLeft());
                if (textureStream == null)
                    throw new IOException("Failed to fetch texture data from '" + url + "'");

                try
                {
                    files.getRight().join();
                    LOGGER.debug(ModelAnima.GEOMETRY, "Took " + stopwatch.stop() + " to process '" + url + "'");
                    return new OnlineResource(url, resourceLocation, textureStream, files.getRight().join());
                }
                catch (Exception e)
                {
                    LOGGER.error("Took too long to parse texture metadata for '" + url + "'", e);
                    LOGGER.debug(ModelAnima.GEOMETRY, "Took " + stopwatch.stop() + " to process '" + url + "'");
                    return new OnlineResource(url, resourceLocation, textureStream, null);
                }
            }
            return this.parent.getResource(resourceLocation);
        }

        @Override
        public boolean hasResource(ResourceLocation resourceLocation)
        {
            return resourceLocation.getPath().startsWith("base32") || this.parent.hasResource(resourceLocation);
        }

        @Override
        public List<IResource> getAllResources(ResourceLocation resourceLocation) throws IOException
        {
            return this.parent.getAllResources(resourceLocation);
        }

        @Override
        public Collection<ResourceLocation> func_230231_a_(ResourceLocation resourceLocation, Predicate<String> filter)
        {
            return this.parent.func_230231_a_(resourceLocation, filter);
        }

        @Override
        public Collection<ResourceLocation> getAllResourceLocations(String path, Predicate<String> filter)
        {
            return this.parent.getAllResourceLocations(path, filter);
        }

        @Override
        public Stream<IResourcePack> func_230232_b_()
        {
            return this.parent.func_230232_b_();
        }

        @Nullable
        private static String parseUrl(ResourceLocation location)
        {
            String[] parts = location.getPath().split("/");
            if (parts[parts.length - 1].startsWith("base32"))
                return new String(new Base32().decode(parts[parts.length - 1].substring(6).toUpperCase(Locale.ROOT).replaceAll("_", "=")));
            return null;
        }

        @Nullable
        private static InputStream read(CompletableFuture<Path> pathFuture)
        {
            try
            {
                Path path = pathFuture.join();
                return path == null ? null : new FileInputStream(path.toFile());
            }
            catch (Exception e)
            {
                LOGGER.error("Took too long to fetch texture data", e);
                return null;
            }
        }

        private static class OnlineResource implements IResource
        {
            private final String url;
            private final ResourceLocation textureLocation;
            private final InputStream stream;
            private final JsonObject metadataJson;

            private OnlineResource(String url, ResourceLocation textureLocation, InputStream stream, @Nullable JsonObject metadataJson)
            {
                this.url = url;
                this.textureLocation = textureLocation;
                this.stream = stream;
                this.metadataJson = metadataJson;
            }

            @Override
            public ResourceLocation getLocation()
            {
                return textureLocation;
            }

            @Override
            public InputStream getInputStream()
            {
                return stream;
            }

            @Nullable
            @Override
            public <T> T getMetadata(IMetadataSectionSerializer<T> serializer)
            {
                if (this.metadataJson == null)
                    return null;
                String s = serializer.getSectionName();
                return this.metadataJson.has(s) ? serializer.deserialize(JSONUtils.getJsonObject(this.metadataJson, s)) : null;
            }

            @Override
            public String getPackName()
            {
                return ModelAnima.DOMAIN + "_online";
            }

            @Override
            public void close() throws IOException
            {
                this.stream.close();
            }

            @Override
            public String toString()
            {
                return "OnlineResource{" +
                        "url='" + url + '\'' +
                        ", textureLocation=" + textureLocation +
                        '}';
            }
        }
    }

    private static class OnlineRepository implements AutoCloseable
    {
        private final ExecutorService executor;
        private final FileCache hashedCache;
        private final FileCache cache;
        private final Map<String, CompletableFuture<Path>> resources;

        private OnlineRepository(String[] hashTableUrls)
        {
            AtomicInteger idGenerator = new AtomicInteger();
            this.executor = createOnlineWorker(idGenerator::getAndIncrement);
            this.hashedCache = new HashedTextureCache(this.executor, hashTableUrls);
            this.cache = new TimedTextureCache(this.executor, 30, TimeUnit.MINUTES);
            this.resources = new HashMap<>();
        }

        public CompletableFuture<Path> requestResource(String url, boolean cache, boolean ignoreMissing)
        {
            return this.resources.computeIfAbsent(url, key -> cache ? this.hashedCache.requestResource(url, ignoreMissing) : this.cache.requestResource(url, ignoreMissing));
        }

        private static ExecutorService createOnlineWorker(Supplier<Integer> idGenerator)
        {
            int i = MathHelper.clamp(Runtime.getRuntime().availableProcessors() - 1, 1, 7);
            ExecutorService executorservice;
            if (i <= 0)
            {
                executorservice = MoreExecutors.newDirectExecutorService();
            }
            else
            {
                executorservice = new ForkJoinPool(i, pool ->
                {
                    ForkJoinWorkerThread forkjoinworkerthread = new ForkJoinWorkerThread(pool)
                    {
                        @Override
                        protected void onTermination(Throwable t)
                        {
                            if (t != null)
                            {
                                LOGGER.warn("{} died", this.getName(), t);
                            }
                            else
                            {
                                LOGGER.debug("{} shutdown", this.getName());
                            }

                            super.onTermination(t);
                        }
                    };
                    forkjoinworkerthread.setName("Worker-Geometry Online Fetcher-" + idGenerator.get());
                    return forkjoinworkerthread;
                }, (thread, throwable) ->
                {
                    if (throwable instanceof CompletionException)
                        throwable = throwable.getCause();

                    if (throwable instanceof ReportedException)
                    {
                        Bootstrap.printToSYSOUT(((ReportedException) throwable).getCrashReport().getCompleteReport());
                        System.exit(-1);
                    }

                    LOGGER.error("Caught exception in thread " + thread, throwable);
                }, true);
            }

            return executorservice;
        }

        @Override
        public void close()
        {
            this.executor.shutdown();
            try
            {
                if (!this.executor.awaitTermination(10, TimeUnit.SECONDS))
                    LOGGER.warn("Took more than 10 seconds to terminate online worker");
            }
            catch (Exception e)
            {
                LOGGER.error("Failed to terminate online worker", e);
            }
        }
    }
}