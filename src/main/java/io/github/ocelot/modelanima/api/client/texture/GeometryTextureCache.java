package io.github.ocelot.modelanima.api.client.texture;

import io.github.ocelot.modelanima.ModelAnima;
import net.minecraft.client.Minecraft;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    @Nullable
    private static InputStream readFile(String url, String hash, Path imageFile)
    {
        if (Files.exists(imageFile))
        {
            try (InputStream stream = new FileInputStream(imageFile.toFile()))
            {
                byte[] model = IOUtils.toByteArray(stream);
                if (hash.equalsIgnoreCase(DigestUtils.md5Hex(model)))
                    return new ByteArrayInputStream(model);
            }
            catch (Exception e)
            {
                LOGGER.error("Failed to read image '" + url + "'", e);
            }
        }
        return null;
    }

    /**
     * Fetches the texture by the specified name.
     *
     * @param url     The name of the texture to fetch
     * @param hash    The hash of the texture
     * @param fetcher The function providing a new stream
     * @return The texture by that name
     */
    @Nullable
    public static InputStream getStream(String url, String hash, Function<String, InputStream> fetcher)
    {
        Path imageFile = CACHE_FOLDER.resolve(DigestUtils.md5Hex(url + ".png"));

        InputStream fetchedStream = readFile(url, hash, imageFile);
        if (fetchedStream != null)
            return fetchedStream;

        fetchedStream = fetcher.apply(url);
        if (fetchedStream == null)
            return null;

        try
        {
            byte[] copy = IOUtils.toByteArray(fetchedStream);
            fetchedStream.close();

            if (!Files.exists(CACHE_FOLDER))
                Files.createDirectory(CACHE_FOLDER);
            if (!Files.exists(imageFile))
                Files.createFile(imageFile);

            try (FileOutputStream os = new FileOutputStream(imageFile.toFile()))
            {
                IOUtils.write(copy, os);
            }
        }
        catch (Exception e)
        {
            LOGGER.error("Failed to write image '" + url + "'", e);
        }

        return readFile(url, hash, imageFile);
    }
}
