package io.github.ocelot.modelanima.api.client.texture;

import io.github.ocelot.modelanima.ModelAnima;
import net.minecraft.client.Minecraft;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.function.Function;

/**
 * Caches online geometry textures by md5 hash.
 *
 * @author Ocelot
 * @since 1.0.0
 */
public class GeometryTextureCache
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Path CACHE_FOLDER = Paths.get(Minecraft.getInstance().gameDir.toURI()).resolve(ModelAnima.DOMAIN + "-geometry-texture-cache");

    private static boolean isCached(String url, @Nullable String hash, Path imageFile)
    {
        if (Files.exists(imageFile))
        {
            try (InputStream stream = new FileInputStream(imageFile.toFile()))
            {
                if (hash == null || hash.equalsIgnoreCase(DigestUtils.md5Hex(stream)))
                    return true;
            }
            catch (Exception e)
            {
                LOGGER.error("Failed to read image '" + url + "'", e);
            }
        }
        return false;
    }

    /**
     * Fetches the texture by the specified name.
     *
     * @param url     The name of the texture to fetch
     * @param hash    The hash of the texture
     * @param fetcher The function providing a new stream
     * @return The location of a file that can be opened with the data
     */
    @Nullable
    public static Path getPath(String url, @Nullable String hash, Function<String, InputStream> fetcher)
    {
        Path imageFile = CACHE_FOLDER.resolve(DigestUtils.md5Hex(url));

        if (isCached(url, hash, imageFile))
            return imageFile;

        InputStream fetchedStream = fetcher.apply(url);
        if (fetchedStream == null)
            return null;

        try
        {
            if (!Files.exists(CACHE_FOLDER))
                Files.createDirectory(CACHE_FOLDER);
            Files.copy(fetchedStream, imageFile, StandardCopyOption.REPLACE_EXISTING);
            return imageFile;
        }
        catch (Exception e)
        {
            LOGGER.error("Failed to write image '" + url + "'", e);
        }

        return null;
    }
}
