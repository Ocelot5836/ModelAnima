package io.github.ocelot.client.model.texture;

import com.google.gson.*;
import io.github.ocelot.client.model.BedrockGeometryModel;
import net.minecraft.network.PacketBuffer;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <p>A table of textures to be used for {@link BedrockGeometryModel} rendering.</p>
 *
 * @author Ocelot
 */
public class GeometryModelTextureTable
{
    public static GeometryModelTextureTable MISSING = new GeometryModelTextureTable(new HashMap<>());

    private final Map<String, GeometryModelTexture> textures;

    public GeometryModelTextureTable(Map<String, GeometryModelTexture> textures)
    {
        this.textures = textures;
    }

    public GeometryModelTextureTable(PacketBuffer buf)
    {
        this.textures = new HashMap<>();
        int count = buf.readVarInt();
        for (int i = 0; i < count; i++)
        {
            this.textures.put(buf.readString(), new GeometryModelTexture(buf));
        }
    }

    /**
     * Writes this texture information into the specified buffer.
     *
     * @param buf The buffer to write data into
     */
    public void write(PacketBuffer buf)
    {
        buf.writeVarInt(this.textures.size());
        for (Map.Entry<String, GeometryModelTexture> entry : this.textures.entrySet())
        {
            buf.writeString(entry.getKey());
            entry.getValue().write(buf);
        }
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
                JsonObject textureObject = entry.getValue().getAsJsonObject();
                textures.put(entry.getKey(), GeometryModelTexture.Type.byName(textureObject.get("type").getAsString()).deserialize(textureObject));
            }
            return new GeometryModelTextureTable(textures);
        }
    }
}
