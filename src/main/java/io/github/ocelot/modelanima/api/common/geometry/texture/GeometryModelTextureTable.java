package io.github.ocelot.modelanima.api.common.geometry.texture;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import io.github.ocelot.modelanima.api.client.geometry.GeometryModel;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <p>A table of textures to be used for {@link GeometryModel} rendering.</p>
 *
 * @author Ocelot
 */
public class GeometryModelTextureTable
{
    public static GeometryModelTextureTable EMPTY = new GeometryModelTextureTable(new HashMap<>());

    private final Map<String, GeometryModelTexture> textures;

    public GeometryModelTextureTable(Map<String, GeometryModelTexture> textures)
    {
        this.textures = textures;
    }

    /**
     * Fetches a geometry model texture by the specified key.
     *
     * @param key The key of the texture to get
     * @return The texture with that key or {@link GeometryModelTexture#MISSING} if there is no texture bound to that key
     */
    public GeometryModelTexture getTexture(@Nullable String key)
    {
        return this.textures.getOrDefault(key, GeometryModelTexture.MISSING);
    }

    /**
     * @return All textures that need to be loaded
     */
    public Collection<GeometryModelTexture> getTextures()
    {
        return this.textures.values();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeometryModelTextureTable that = (GeometryModelTextureTable) o;
        return textures.equals(that.textures);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(textures);
    }

    @Override
    public String toString()
    {
        return "CosmeticModelTextureTable{" +
                "textures=" + textures +
                '}';
    }

    /**
     * <p>Deserializes a new {@link GeometryModelTextureTable} from JSON.</p>
     *
     * @author Ocelot
     */
    public static class Deserializer implements JsonDeserializer<GeometryModelTextureTable>
    {
        @Override
        public GeometryModelTextureTable deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject texturesObject = json.getAsJsonObject();
            Map<String, GeometryModelTexture> textures = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : texturesObject.entrySet())
            {
                textures.put(entry.getKey(), GeometryModelTexture.CODEC.parse(JsonOps.INSTANCE, entry.getValue()).result().orElse(GeometryModelTexture.MISSING));
            }
            return new GeometryModelTextureTable(textures);
        }
    }
}
