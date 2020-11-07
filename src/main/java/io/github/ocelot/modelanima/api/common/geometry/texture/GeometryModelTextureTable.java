package io.github.ocelot.modelanima.api.common.geometry.texture;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import io.github.ocelot.modelanima.api.client.geometry.GeometryModel;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
 * @since 1.0.0
 */
public class GeometryModelTextureTable
{
    private static final Logger LOGGER = LogManager.getLogger();
    public static GeometryModelTextureTable EMPTY = new GeometryModelTextureTable(new HashMap<>());

    private final Map<String, GeometryModelTexture> textures;

    public GeometryModelTextureTable(Map<String, GeometryModelTexture> textures)
    {
        this.textures = textures;
    }

    public GeometryModelTextureTable(PacketBuffer buf)
    {
        this.textures = new HashMap<>();
        for (int i = 0; i < buf.readVarInt(); i++)
            this.textures.put(buf.readString(), new GeometryModelTexture(buf));
    }

    public GeometryModelTextureTable(CompoundNBT nbt)
    {
        this.textures = new HashMap<>();

        ListNBT texturesNbt = nbt.getList("Textures", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < texturesNbt.size(); i++)
        {
            CompoundNBT textureNbt = texturesNbt.getCompound(i);
            String key = textureNbt.getString("Key");
            INBT value = textureNbt.get("Value");
            GeometryModelTexture.CODEC.parse(NBTDynamicOps.INSTANCE, value).get().ifLeft(texture -> this.textures.put(key, texture));
        }
    }

    /**
     * Writes the data of this texture table into the provided buffer.
     *
     * @param buf The buffer to write into
     */
    public void write(PacketBuffer buf)
    {
        buf.writeVarInt(this.textures.size());
        this.textures.forEach((key, texture) ->
        {
            buf.writeString(key);
            texture.write(buf);
        });
    }

    public CompoundNBT serializeNBT()
    {
        CompoundNBT nbt = new CompoundNBT();

        ListNBT texturesNbt = new ListNBT();
        this.textures.forEach((key, texture) -> GeometryModelTexture.CODEC.encodeStart(NBTDynamicOps.INSTANCE, texture).get().ifLeft(textureValueNbt ->
        {
            CompoundNBT textureNbt = new CompoundNBT();
            textureNbt.putString("Key", key);
            textureNbt.put("Value", textureValueNbt);
            texturesNbt.add(textureNbt);
        }));
        nbt.put("Textures", texturesNbt);

        return nbt;
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
        return "GeometryModelTextureTable{" +
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
                textures.put(entry.getKey(), GeometryModelTexture.CODEC.parse(JsonOps.INSTANCE, entry.getValue()).getOrThrow(false, LOGGER::error));
            }
            return new GeometryModelTextureTable(textures);
        }
    }
}
