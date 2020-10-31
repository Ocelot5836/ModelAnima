package io.github.ocelot.modelanima.api.client.texture;

import com.google.gson.JsonObject;
import io.github.ocelot.modelanima.ModelAnima;
import io.github.ocelot.modelanima.api.common.geometry.texture.GeometryModelTexture;
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
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class GeometryTextureSpriteUploader extends ReloadListener<AtlasTexture.SheetData> implements AutoCloseable
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Pattern SERIALIZE = Pattern.compile("=");
    private static final Pattern DESERIALIZE = Pattern.compile("_");
    private final AtlasTexture textureAtlas;

    public GeometryTextureSpriteUploader(TextureManager textureManager, ResourceLocation atlasTextureLocation)
    {
        this.textureAtlas = new AtlasTexture(atlasTextureLocation);
        textureManager.loadTexture(this.textureAtlas.getTextureLocation(), this.textureAtlas);
    }

    public TextureAtlasSprite getSprite(ResourceLocation location)
    {
        return this.textureAtlas.getSprite(location);
    }

    private Stream<ResourceLocation> getResourceLocations()
    {
        Collection<GeometryModelTexture> textures = Arrays.asList(
                new GeometryModelTexture(GeometryModelTexture.Type.ONLINE, "https://cdn.discordapp.com/attachments/710316430884864003/771937531621146634/unknown.png", -1, false),
                new GeometryModelTexture(GeometryModelTexture.Type.ONLINE, "https://cdn.discordapp.com/attachments/426584849088774187/771936845880885248/Original.PNG", -1, false),
                new GeometryModelTexture(GeometryModelTexture.Type.LOCATION, "block/stone", -1, false),
                new GeometryModelTexture(GeometryModelTexture.Type.LOCATION, "block/granite", -1, false),
                new GeometryModelTexture(GeometryModelTexture.Type.LOCATION, "block/diamond_block", -1, false)
        );
        return textures.stream().map(texture -> texture.getType() == GeometryModelTexture.Type.ONLINE ? new ResourceLocation(texture.getLocation().getNamespace(), "base32" + SERIALIZE.matcher(new Base32().encodeAsString(texture.getTexture().getBytes()).toLowerCase(Locale.ROOT)).replaceAll("_")) : texture.getLocation());
    }

    @Override
    protected AtlasTexture.SheetData prepare(IResourceManager resourceManager, IProfiler profilerIn)
    {
        profilerIn.startTick();
        profilerIn.startSection("stitching");
        AtlasTexture.SheetData sheetData = this.textureAtlas.stitch(new OnlineResourceManager(resourceManager), this.getResourceLocations(), profilerIn, Minecraft.getInstance().gameSettings.mipmapLevels);
        profilerIn.endSection();
        profilerIn.endTick();
        return sheetData;
    }

    @Override
    protected void apply(AtlasTexture.SheetData objectIn, IResourceManager resourceManagerIn, IProfiler profilerIn)
    {
        profilerIn.startTick();
        profilerIn.startSection("upload");
        this.textureAtlas.upload(objectIn);
        profilerIn.endSection();
        profilerIn.endTick();
    }

    @Override
    public void close()
    {
        this.textureAtlas.clear();
    }

    private static class OnlineResourceManager implements IResourceManager
    {
        private final IResourceManager parent;

        private OnlineResourceManager(IResourceManager parent)
        {
            this.parent = parent;
        }

        @Override
        public Set<String> getResourceNamespaces()
        {
            return this.parent.getResourceNamespaces();
        }

        @Override
        public IResource getResource(ResourceLocation resourceLocation) throws IOException
        {
            String[] parts = resourceLocation.getPath().split("/");
            if (parts[parts.length - 1].startsWith("base32"))
            {
                String url = new String(new Base32().decode(DESERIALIZE.matcher(parts[parts.length - 1].substring(6, parts[parts.length - 1].length() - 4).toUpperCase(Locale.ROOT)).replaceAll("=")));
                CompletableFuture<InputStream> textureStream = CompletableFuture.supplyAsync(() ->
                {
                    try
                    {
                        return get(url);
                    }
                    catch (Exception e)
                    {
                        LOGGER.error("Failed to load online texture from '" + url + "'", e);
                        return null;
                    }
                }, Util.getServerExecutor());

                CompletableFuture<JsonObject> metadataStream = CompletableFuture.supplyAsync(() ->
                {
                    try
                    {
                        int index = url.lastIndexOf('?');
                        if (index == -1)
                            return get(url + ".mcmeta");
                        return get(url.substring(0, index) + ".mcmeta" + url.substring(index));
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

                try
                {
                    return new OnlineResource(resourceLocation, textureStream.get(1, TimeUnit.MINUTES), metadataStream.get(1, TimeUnit.MINUTES));
                }
                catch (Exception e)
                {
                    throw new IOException("Failed to fetch texture data from '" + url + "'", e);
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

    // TODO make a global online cache
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