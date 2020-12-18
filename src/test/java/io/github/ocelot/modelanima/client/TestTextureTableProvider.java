package io.github.ocelot.modelanima.client;

import io.github.ocelot.modelanima.api.client.texture.TextureTableProvider;
import io.github.ocelot.modelanima.api.common.geometry.GeometryModelLoader;
import io.github.ocelot.modelanima.api.common.geometry.texture.GeometryModelTextureTable;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;

public class TestTextureTableProvider implements TextureTableProvider
{
    @Override
    public void addTextures(BiConsumer<ResourceLocation, GeometryModelTextureTable> textureConsumer)
    {
        GeometryModelTextureTable table = GeometryModelLoader.parseTextures("{\"texture\":{\"type\":\"online\",\"texture\":\"https://api.battlefieldsmc.net/api/server/cosmetic/texture/hoodie/default.png\",\"color\":\"A12722\",\"layer\":\"cutout\"}}");
        textureConsumer.accept(new ResourceLocation("debug", "test"), table);
        textureConsumer.accept(new ResourceLocation("debug", "test2"), table);
        textureConsumer.accept(new ResourceLocation("debug", "test3"), table);
        textureConsumer.accept(new ResourceLocation("debug", "test4"), table);
        textureConsumer.accept(new ResourceLocation("debug", "test5"), table);
    }

    @Override
    public CompletableFuture<Void> reload(IStage stage, IResourceManager resourceManager, IProfiler preparationsProfiler, IProfiler reloadProfiler, Executor backgroundExecutor, Executor gameExecutor)
    {
        return CompletableFuture.completedFuture(null);
    }
}
