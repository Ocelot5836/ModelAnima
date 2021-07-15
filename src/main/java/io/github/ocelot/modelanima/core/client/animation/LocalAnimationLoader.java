package io.github.ocelot.modelanima.core.client.animation;

import io.github.ocelot.modelanima.api.common.animation.AnimationData;
import io.github.ocelot.modelanima.api.common.animation.AnimationParser;
import io.github.ocelot.modelanima.api.common.util.BackgroundLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * @author Ocelot
 */
@ApiStatus.Internal
public class LocalAnimationLoader implements BackgroundLoader<Map<ResourceLocation, AnimationData>>
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
    public CompletableFuture<Map<ResourceLocation, AnimationData>> reload(ResourceManager resourceManager, Executor backgroundExecutor, Executor gameExecutor)
    {
        return CompletableFuture.supplyAsync(() ->
        {
            Map<ResourceLocation, AnimationData> animationData = new HashMap<>();
            for (ResourceLocation animationLocation : resourceManager.listResources(this.folder, name -> name.endsWith(".json")))
            {
                try (Resource resource = resourceManager.getResource(animationLocation))
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
