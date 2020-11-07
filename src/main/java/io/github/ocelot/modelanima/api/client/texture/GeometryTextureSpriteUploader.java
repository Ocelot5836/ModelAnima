package io.github.ocelot.modelanima.api.client.texture;

import com.google.common.base.Charsets;
import com.google.gson.JsonObject;
import io.github.ocelot.modelanima.ModelAnima;
import io.github.ocelot.modelanima.api.common.geometry.texture.GeometryModelTexture;
import io.github.ocelot.modelanima.api.common.geometry.texture.GeometryModelTextureTable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.ReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.data.IMetadataSectionSerializer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.io.IOUtils;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.plexus.util.FileUtils;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
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
        profiler.startTick();
        profiler.startSection("stitching");
        AtlasTexture.SheetData sheetData = this.textureAtlas.stitch(new OnlineResourceManager(resourceManager, this.textures), this.textures.stream().map(GeometryModelTexture::getLocation), profiler, Minecraft.getInstance().gameSettings.mipmapLevels);
        profiler.endSection();
        profiler.endTick();
        return sheetData;
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
        private final Map<ResourceLocation, CompletableFuture<String>> hashes;

        private OnlineResourceManager(IResourceManager parent, Set<GeometryModelTexture> textures)
        {
            this.parent = parent;
            this.hashes = textures.stream().filter(texture -> texture.getType() == GeometryModelTexture.Type.ONLINE).collect(Collectors.toMap(texture -> new ResourceLocation(texture.getLocation().getNamespace(), "textures/" + texture.getLocation().getPath() + ".png"), texture -> CompletableFuture.supplyAsync(() ->
            {
                String url = getUrl(texture.getLocation());
                return url == null ? null : getHash(url);
            }, Util.getServerExecutor())));
        }

        @Nullable
        private static String getUrl(ResourceLocation location)
        {
            String[] parts = location.getPath().split("/");
            if (parts[parts.length - 1].startsWith("base32"))
                return new String(new Base32().decode(parts[parts.length - 1].substring(6, parts[parts.length - 1].length() - 4).toUpperCase(Locale.ROOT).replaceAll("_", "=")));
            return null;
        }

        @Override
        public Set<String> getResourceNamespaces()
        {
            return this.parent.getResourceNamespaces();
        }

        @Override
        public IResource getResource(ResourceLocation resourceLocation) throws IOException
        {
            String url = getUrl(resourceLocation);
            if (url != null)
            {
                CompletableFuture<JsonObject> metadataStream = CompletableFuture.supplyAsync(() ->
                {
                    try
                    {
                        String extension = FileUtils.extension(url);
                        String[] urlParts = url.split("." + extension);
                        if (urlParts.length == 1)
                            return get(url + ".mcmeta");
                        return get(urlParts[0] + extension + ".mcmeta" + urlParts[1]);
                    }
                    catch (Exception ignored)
                    {
                        return null;
                    }
                }, Util.getServerExecutor()).thenApplyAsync(stream ->
                {
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
                }, Util.getServerExecutor());

                InputStream textureStream;
                try
                {
                    String hash = !this.hashes.containsKey(resourceLocation) ? null : this.hashes.get(resourceLocation).get(1, TimeUnit.MINUTES);
                    if (hash != null)
                    {
                        textureStream = GeometryTextureCache.getStream(url, hash, OnlineResourceManager::getTextureStream);
                    }
                    else
                    {
                        textureStream = getTextureStream(url);
                    }
                }
                catch (Exception e)
                {
                    throw new IOException("Took too long to fetch texture data from '" + url + "'");
                }

                if (textureStream == null)
                    throw new IOException("Failed to fetch texture data from '" + url + "'");

                try
                {
                    return new OnlineResource(resourceLocation, textureStream, metadataStream.get(1, TimeUnit.MINUTES));
                }
                catch (Exception e)
                {
                    throw new IOException("Failed to fetch texture metadata from '" + url + "'", e);
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
        private static String getHash(String url)
        {
            String extension = FileUtils.extension(url);
            String[] urlParts = url.split("." + extension);

            try
            {
                return CompletableFuture.supplyAsync(() ->
                {
                    try
                    {
                        String hashUrl = urlParts.length == 1 ? url + ".md5" : urlParts[0] + extension + ".md5" + urlParts[1];
                        if (!canGet(hashUrl))
                            return null;
                        return IOUtils.toString(get(hashUrl), Charsets.UTF_8);
                    }
                    catch (Exception ignored)
                    {
                        return null;
                    }
                }, Util.getServerExecutor()).get(1, TimeUnit.MINUTES);
            }
            catch (Exception e)
            {
                LOGGER.error("Failed to load online hash from '" + url + "'", e);
            }
            return null;
        }

        @Nullable
        private static InputStream getTextureStream(String url)
        {
            try
            {
                return CompletableFuture.supplyAsync(() ->
                {
                    try
                    {
                        return canGet(url) ? get(url) : null;
                    }
                    catch (Exception e)
                    {
                        LOGGER.error("Failed to load online texture from '" + url + "'", e);
                        return null;
                    }
                }, Util.getServerExecutor()).get(1, TimeUnit.MINUTES);
            }
            catch (Exception e)
            {
                LOGGER.error("Failed to load online texture from '" + url + "'", e);
            }
            return null;
        }

        private static boolean canGet(String url) throws IOException
        {
            try (CloseableHttpClient client = HttpClients.custom().setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11").build())
            {
                HttpHead head = new HttpHead(url);
                try (CloseableHttpResponse response = client.execute(head))
                {
                    return response.getStatusLine().getStatusCode() == 200;
                }
            }
        }

        private static InputStream get(String url) throws IOException
        {
            try (CloseableHttpClient client = HttpClients.custom().setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11").build())
            {
                HttpGet get = new HttpGet(url);
                try (CloseableHttpResponse response = client.execute(get))
                {
                    StatusLine statusLine = response.getStatusLine();
                    if (statusLine.getStatusCode() != 200)
                        throw new IOException("Failed to connect to '" + url + "'. " + statusLine.getStatusCode() + " " + statusLine.getReasonPhrase());
                    return IOUtils.toBufferedInputStream(response.getEntity().getContent());
                }
            }
        }
    }

    private static class OnlineResource implements IResource
    {
        private final ResourceLocation textureLocation;
        private final InputStream stream;
        private final JsonObject metadataJson;

        private OnlineResource(ResourceLocation textureLocation, InputStream stream, @Nullable JsonObject metadataJson)
        {
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
            {
                return null;
            }
            else
            {
                String s = serializer.getSectionName();
                return this.metadataJson.has(s) ? serializer.deserialize(JSONUtils.getJsonObject(this.metadataJson, s)) : null;
            }
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
    }
}