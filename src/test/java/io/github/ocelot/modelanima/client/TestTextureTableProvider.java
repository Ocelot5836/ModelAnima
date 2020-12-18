package io.github.ocelot.modelanima.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.ocelot.modelanima.api.client.texture.TextureTableProvider;
import io.github.ocelot.modelanima.api.common.geometry.GeometryModelLoader;
import io.github.ocelot.modelanima.api.common.geometry.texture.GeometryModelTextureTable;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;

public class TestTextureTableProvider implements TextureTableProvider
{
    @Override
    public void addTextures(BiConsumer<ResourceLocation, GeometryModelTextureTable> textureConsumer)
    {
        String input = "{\"accessory:light_blue_hoodie\":{\"texture\":{\"color\":\"3AAFD9\",\"type\":\"online\",\"layer\":\"cutout\",\"texture\":\"https://api.battlefieldsmc.net/api/server/cosmetic/texture/hoodie/default.png\"}},\"accessory:discord_hoodie\":{\"texture\":{\"type\":\"online\",\"layer\":\"cutout\",\"texture\":\"https://api.battlefieldsmc.net/api/server/cosmetic/texture/hoodie/discord.png\"}},\"accessory:magenta_hoodie\":{\"texture\":{\"color\":\"BD44B3\",\"type\":\"online\",\"layer\":\"cutout\",\"texture\":\"https://api.battlefieldsmc.net/api/server/cosmetic/texture/hoodie/default.png\"}},\"accessory:orange_hoodie\":{\"texture\":{\"color\":\"F07613\",\"type\":\"online\",\"layer\":\"cutout\",\"texture\":\"https://api.battlefieldsmc.net/api/server/cosmetic/texture/hoodie/default.png\"}},\"accessory:gray_hoodie\":{\"texture\":{\"color\":\"3E4447\",\"type\":\"online\",\"layer\":\"cutout\",\"texture\":\"https://api.battlefieldsmc.net/api/server/cosmetic/texture/hoodie/default.png\"}},\"accessory:light_gray_hoodie\":{\"texture\":{\"color\":\"8E8E86\",\"type\":\"online\",\"layer\":\"cutout\",\"texture\":\"https://api.battlefieldsmc.net/api/server/cosmetic/texture/hoodie/default.png\"}},\"accessory:dark_blue_hoodie\":{\"texture\":{\"color\":\"35399D\",\"type\":\"online\",\"layer\":\"cutout\",\"texture\":\"https://api.battlefieldsmc.net/api/server/cosmetic/texture/hoodie/default.png\"}},\"accessory:red_hoodie\":{\"texture\":{\"color\":\"A12722\",\"type\":\"online\",\"layer\":\"cutout\",\"texture\":\"https://api.battlefieldsmc.net/api/server/cosmetic/texture/hoodie/default.png\"}},\"accessory:pink_hoodie\":{\"texture\":{\"color\":\"ED8DAC\",\"type\":\"online\",\"layer\":\"cutout\",\"texture\":\"https://api.battlefieldsmc.net/api/server/cosmetic/texture/hoodie/default.png\"}},\"accessory:cyan_hoodie\":{\"texture\":{\"color\":\"158991\",\"type\":\"online\",\"layer\":\"cutout\",\"texture\":\"https://api.battlefieldsmc.net/api/server/cosmetic/texture/hoodie/default.png\"}},\"accessory:patreon_hoodie\":{\"texture\":{\"type\":\"online\",\"layer\":\"cutout\",\"texture\":\"https://api.battlefieldsmc.net/api/server/cosmetic/texture/hoodie/patreon.png\"}},\"accessory:purple_hoodie\":{\"texture\":{\"color\":\"792AAC\",\"type\":\"online\",\"layer\":\"cutout\",\"texture\":\"https://api.battlefieldsmc.net/api/server/cosmetic/texture/hoodie/default.png\"}},\"accessory:mech_torso\":{\"texture\":{\"type\":\"online\",\"layer\":\"cutout\",\"texture\":\"https://api.battlefieldsmc.net/api/server/cosmetic/texture/mech_torso/default.png\"}},\"accessory:black_hoodie\":{\"texture\":{\"color\":\"141519\",\"type\":\"online\",\"layer\":\"cutout\",\"texture\":\"https://api.battlefieldsmc.net/api/server/cosmetic/texture/hoodie/default.png\"}},\"accessory:mech_helmet\":{\"texture\":{\"type\":\"online\",\"layer\":\"cutout\",\"texture\":\"https://api.battlefieldsmc.net/api/server/cosmetic/texture/mech_helmet/default.png\"}},\"accessory:illager_hoodie\":{\"texture\":{\"type\":\"online\",\"layer\":\"cutout\",\"texture\":\"https://api.battlefieldsmc.net/api/server/cosmetic/texture/hoodie/illager.png\"}},\"accessory:yellow_hoodie\":{\"texture\":{\"color\":\"F8C627\",\"type\":\"online\",\"layer\":\"cutout\",\"texture\":\"https://api.battlefieldsmc.net/api/server/cosmetic/texture/hoodie/default.png\"}},\"accessory:lime_hoodie\":{\"texture\":{\"color\":\"70B919\",\"type\":\"online\",\"layer\":\"cutout\",\"texture\":\"https://api.battlefieldsmc.net/api/server/cosmetic/texture/hoodie/default.png\"}},\"accessory:jungle_mask\":{\"texture\":{\"type\":\"online\",\"layer\":\"cutout\",\"texture\":\"https://api.battlefieldsmc.net/api/server/cosmetic/texture/jungle_mask/default.png\"}},\"accessory:white_hoodie\":{\"texture\":{\"color\":\"E9ECEC\",\"type\":\"online\",\"layer\":\"cutout\",\"texture\":\"https://api.battlefieldsmc.net/api/server/cosmetic/texture/hoodie/default.png\"}},\"accessory:brown_hoodie\":{\"texture\":{\"color\":\"724728\",\"type\":\"online\",\"layer\":\"cutout\",\"texture\":\"https://api.battlefieldsmc.net/api/server/cosmetic/texture/hoodie/default.png\"}},\"accessory:green_hoodie\":{\"texture\":{\"color\":\"546D1B\",\"type\":\"online\",\"layer\":\"cutout\",\"texture\":\"https://api.battlefieldsmc.net/api/server/cosmetic/texture/hoodie/default.png\"}}}";

        JsonObject printArray = new JsonParser().parse(input).getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : printArray.entrySet())
        {
            textureConsumer.accept(new ResourceLocation(entry.getKey()), GeometryModelLoader.parseTextures(entry.getValue()));
        }
    }

    @Override
    public CompletableFuture<Void> reload(IStage stage, IResourceManager resourceManager, IProfiler preparationsProfiler, IProfiler reloadProfiler, Executor backgroundExecutor, Executor gameExecutor)
    {
        return CompletableFuture.completedFuture(null);
    }
}
