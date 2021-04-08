package io.github.ocelot.modelanima.api.client.animation;

import io.github.ocelot.modelanima.api.common.animation.AnimationData;
import io.github.ocelot.modelanima.api.common.animation.AnimationParser;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * <p>Loads animations from a local file location.</p>
 *
 * @author Ocelot
 * @since 1.0.0
 */
public class LocalAnimationLoader implements AnimationLoader
{
    private static final Logger LOGGER = LogManager.getLogger();

    private final String folder;

    public LocalAnimationLoader()
    {
        this("animations");
    }

    public LocalAnimationLoader(String folder)
    {
        this.folder = folder.isEmpty() ? "" : folder + "/";
    }

    @Override
    public CompletableFuture<Map<ResourceLocation, AnimationData>> reload(IResourceManager resourceManager, Executor backgroundExecutor, Executor gameExecutor)
    {
        return CompletableFuture.supplyAsync(() ->
        {
            Map<ResourceLocation, AnimationData> animationData = new HashMap<>();
            for (ResourceLocation animationLocation : resourceManager.getAllResourceLocations(this.folder, name -> name.endsWith(".json")))
            {
                try (IResource resource = resourceManager.getResource(animationLocation))
                {
                    AnimationData[] animations = AnimationParser.parse(new InputStreamReader(resource.getInputStream()));
                    for (AnimationData animation : animations)
                    {
                        ResourceLocation id = new ResourceLocation(animationLocation.getNamespace(), animation.getName());
                        if (animationData.put(id, animation) != null)
                            LOGGER.warn("Duplicate animation: " + id);
                    }
                }
                catch (Exception e)
                {
                    LOGGER.error("Failed to load animation: " + animationLocation.getNamespace() + ":" + animationLocation.getPath().substring(this.folder.length(), animationLocation.getPath().length() - 5), e);
                }
            }
            return animationData;
        }, backgroundExecutor);
    }
}
