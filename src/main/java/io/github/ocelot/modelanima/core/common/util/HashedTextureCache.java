package io.github.ocelot.modelanima.core.common.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.ocelot.modelanima.api.client.util.GeometryCache;
import io.github.ocelot.modelanima.api.common.util.FileCache;
import net.minecraft.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * @author Ocelot
 */
public class HashedTextureCache implements FileCache
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new Gson();

    private final Executor executor;
    private final CompletableFuture<Map<String, String>> hashes;

    public HashedTextureCache(Executor executor, String[] hashTableUrls)
    {
        this.executor = executor;
        this.hashes = CompletableFuture.supplyAsync(() ->
        {
            Set<CompletableFuture<Map<String, String>>> hashesFuture = Arrays.stream(hashTableUrls).map(it -> CompletableFuture.supplyAsync(() ->
            {
                try (InputStreamReader reader = new InputStreamReader(FileCache.get(it)))
                {
                    return GSON.fromJson(reader, TypeToken.getParameterized(Map.class, String.class, String.class).getType());
                }
                catch (Exception e)
                {
                    LOGGER.error("Failed to load hash table from '" + it + "'");
                    return Collections.<String, String>emptyMap();
                }
            }, executor)).collect(Collectors.toSet());

            Map<String, String> hashes = new HashMap<>();
            for (CompletableFuture<Map<String, String>> future : hashesFuture)
                hashes.putAll(future.join());
            LOGGER.debug("Downloaded " + hashes.size() + " hashes from " + hashTableUrls.length + " hash table(s)");
            return hashes;
        }, Util.getServerExecutor());
    }

    @Override
    public CompletableFuture<Path> requestResource(String url, boolean ignoreMissing)
    {
        return this.hashes.thenApplyAsync(hashes ->
        {
            try
            {
                try
                {
                    return GeometryCache.getPath(url, hashes.get(url), s ->
                    {
                        try
                        {
                            return FileCache.get(url);
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
                    throw new IOException("Failed to load texture data", e);
                }
            }
            catch (IOException e)
            {
                if (!ignoreMissing)
                    LOGGER.error("Failed to fetch resource from '" + url + "'", e);
                return null;
            }
        }, this.executor);
    }
}
