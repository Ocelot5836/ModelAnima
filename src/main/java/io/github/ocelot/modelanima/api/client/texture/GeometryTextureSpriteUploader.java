package io.github.ocelot.modelanima.api.client.texture;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.gson.JsonObject;
import io.github.ocelot.modelanima.ModelAnima;
import io.github.ocelot.modelanima.api.common.geometry.texture.GeometryModelTexture;
import io.github.ocelot.modelanima.api.common.geometry.texture.GeometryModelTextureTable;
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
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.EofSensorInputStream;
import org.apache.http.conn.EofSensorWatcher;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.plexus.util.FileUtils;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>Part of the core library, prone to change.</p>
 *
 * @author Ocelot
 * @since 1.0.0
 */
public class GeometryTextureSpriteUploader extends ReloadListener<AtlasTexture.SheetData> implements GeometryAtlasTexture, AutoCloseable
{
    public static final ResourceLocation ATLAS_LOCATION = new ResourceLocation(ModelAnima.DOMAIN, "textures/atlas/geometry.png");
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11";
    private static final Logger LOGGER = LogManager.getLogger();
    private final AtlasTexture textureAtlas;
    private final Set<GeometryModelTexture> textures;

    public GeometryTextureSpriteUploader(TextureManager textureManager, ResourceLocation atlasTextureLocation)
    {
        this.textureAtlas = new AtlasTexture(atlasTextureLocation);
        this.textures = new HashSet<>();
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
        try (OnlineRepository onlineRepository = new OnlineRepository())
        {
            profiler.startTick();
            profiler.startSection("stitching");
            AtlasTexture.SheetData sheetData = this.textureAtlas.stitch(new OnlineResourceManager(resourceManager, onlineRepository, this.textures.stream().filter(texture -> texture.getType() == GeometryModelTexture.Type.ONLINE).collect(Collectors.toSet())), this.textures.stream().filter(texture -> texture.getType() == GeometryModelTexture.Type.LOCATION || texture.getType() == GeometryModelTexture.Type.ONLINE).map(GeometryModelTexture::getLocation).distinct(), profiler, Minecraft.getInstance().gameSettings.mipmapLevels);
            profiler.endSection();
            profiler.endTick();
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

    public GeometryTextureSpriteUploader setTextures(Map<ResourceLocation, GeometryModelTextureTable> textures)
    {
        this.textures.clear();
        this.textures.addAll(textures.values().stream().flatMap(table -> table.getTextures().stream()).collect(Collectors.toSet()));
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
            String extension = FileUtils.extension(url);
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
                catch (Exception e)
                {
                    LOGGER.warn("Failed to read metadata", e);
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
                if (!this.onlineLocations.containsKey(url))
                    throw new IOException("Failed to fetch texture data from '" + url + "'");

                Pair<CompletableFuture<Path>, CompletableFuture<JsonObject>> files = this.onlineLocations.get(url);
                InputStream textureStream = read(files.getLeft());
                if (textureStream == null)
                    throw new IOException("Failed to fetch texture data from '" + url + "'");

                try
                {
                    return new OnlineResource(url, resourceLocation, textureStream, files.getRight().get(1, TimeUnit.SECONDS));
                }
                catch (Exception e)
                {
                    LOGGER.error("Took too long to parse texture metadata for '" + url + "'");
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
                Path path = pathFuture.get(1, TimeUnit.MINUTES);
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
        private final Map<String, CompletableFuture<String>> hashes;
        private final Map<String, CompletableFuture<Path>> resources;

        private OnlineRepository()
        {
            AtomicInteger idGenerator = new AtomicInteger();
            this.executor = createOnlineWorker(idGenerator::getAndIncrement);
            this.hashes = new HashMap<>();
            this.resources = new HashMap<>();
        }

        public CompletableFuture<String> requestHash(String url)
        {
            return this.hashes.computeIfAbsent(url, key -> CompletableFuture.supplyAsync(() ->
            {
                try (InputStream stream = get(url))
                {
                    return IOUtils.toString(stream, StandardCharsets.UTF_8);
                }
                catch (IOException e)
                {
                    LOGGER.error("Failed to fetch hash from '" + url + "'", e);
                    return null;
                }
            }, this.executor));
        }

        public CompletableFuture<Path> requestResource(String url, boolean cache, boolean ignoreMissing)
        {
            if (cache)
            {
                return this.resources.computeIfAbsent(url, key -> this.requestHash(url + ".md5").thenApplyAsync(hash ->
                {
                    try
                    {
                        try
                        {
                            return GeometryTextureCache.getPath(url, hash, s ->
                            {
                                try
                                {
                                    return get(url);
                                }
                                catch (IOException e)
                                {
                                    if (!ignoreMissing)
                                        LOGGER.error("Failed to read data from '" + url + "'");
                                    return null;
                                }
                            });
                        }
                        catch (Exception e)
                        {
                            throw new IOException("Took too long to fetch texture data");
                        }
                    }
                    catch (IOException e)
                    {
                        if (!ignoreMissing)
                            LOGGER.error("Failed to fetch resource from '" + url + "'", e);
                        return null;
                    }
                }, this.executor));
            }
            else
            {
                return this.resources.computeIfAbsent(url, key -> CompletableFuture.supplyAsync(() ->
                {
                    try
                    {
                        Path tempFile = Files.createTempFile(DigestUtils.md5Hex(url), null);
                        try (InputStream stream = get(url))
                        {
                            Files.copy(stream, tempFile, StandardCopyOption.REPLACE_EXISTING);
                        }
                        return tempFile;
                    }
                    catch (IOException e)
                    {
                        if (!ignoreMissing)
                            LOGGER.error("Failed to fetch resource from '" + url + "'", e);
                        return null;
                    }
                }, this.executor));
            }
        }

        private static InputStream get(String url) throws IOException
        {
            HttpGet get = new HttpGet(url);
            CloseableHttpClient client = HttpClients.custom().setUserAgent(USER_AGENT).build();
            CloseableHttpResponse response = client.execute(get);
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() != 200)
            {
                client.close();
                response.close();
                throw new IOException("Failed to connect to '" + url + "'. " + statusLine.getStatusCode() + " " + statusLine.getReasonPhrase());
            }
            return new EofSensorInputStream(response.getEntity().getContent(), new EofSensorWatcher()
            {
                @Override
                public boolean eofDetected(InputStream wrapped)
                {
                    return true;
                }

                @Override
                public boolean streamClosed(InputStream wrapped) throws IOException
                {
                    response.close();
                    return true;
                }

                @Override
                public boolean streamAbort(InputStream wrapped) throws IOException
                {
                    response.close();
                    return true;
                }
            });
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