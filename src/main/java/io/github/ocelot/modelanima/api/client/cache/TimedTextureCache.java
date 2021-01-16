package io.github.ocelot.modelanima.api.client.cache;

import io.github.ocelot.modelanima.api.client.texture.GeometryTextureCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class TimedTextureCache implements FileCache
{
    private static final Logger LOGGER = LogManager.getLogger();

    private final Executor executor;
    private final long cacheTime;
    private final TimeUnit cacheTimeUnit;

    public TimedTextureCache(Executor executor, long cacheTime, TimeUnit cacheTimeUnit)
    {
        this.executor = executor;
        this.cacheTime = cacheTime;
        this.cacheTimeUnit = cacheTimeUnit;
    }

    @Override
    public CompletableFuture<Path> requestResource(String url, boolean ignoreMissing)
    {
        return CompletableFuture.supplyAsync(() ->
        {
            try
            {
                return GeometryTextureCache.getPath(url, this.cacheTime, this.cacheTimeUnit, s ->
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
                if (!ignoreMissing)
                    LOGGER.error("Failed to fetch resource from '" + url + "'", e);
                return null;
            }
        }, this.executor);
    }
}
