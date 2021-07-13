package io.github.ocelot.modelanima.api.common.texture;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import io.github.ocelot.modelanima.api.client.geometry.GeometryModel;
import io.github.ocelot.modelanima.api.client.texture.GeometryTextureManager;
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
import java.util.*;

/**
 * <p>A table of textures to be used for {@link GeometryModel} rendering. Texture tables must be made from {@link GeometryTextureManager}.</p>
 *
 * @author Ocelot
 * @since 1.0.0
 */
public class GeometryModelTextureTable
{
    private static final Logger LOGGER = LogManager.getLogger();
    public static GeometryModelTextureTable EMPTY = new GeometryModelTextureTable(new HashMap<>());

    private final Map<String, GeometryModelTexture[]> textures;

    public GeometryModelTextureTable(Map<String, GeometryModelTexture[]> textures)
    {
        this.textures = new HashMap<>(textures);
        this.textures.values().removeIf(layers -> layers.length == 0);
    }

    public GeometryModelTextureTable(PacketBuffer buf)
    {
        this.textures = new HashMap<>();
        List<GeometryModelTexture> textureSet = new ArrayList<>();

        int count = buf.readVarInt();
        for (int i = 0; i < count; i++)
        {
            String key = buf.readUtf();
            int layers = buf.readVarInt();
            for (int j = 0; j < layers; j++)
                textureSet.add(new GeometryModelTexture(buf));
            if (!textureSet.isEmpty())
            {
                this.textures.put(key, textureSet.toArray(new GeometryModelTexture[0]));
                textureSet.clear();
            }
        }
    }

    public GeometryModelTextureTable(CompoundNBT nbt)
    {
        this.textures = new HashMap<>();
        List<GeometryModelTexture> textureSet = new ArrayList<>();

        ListNBT texturesNbt = nbt.getList("Textures", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < texturesNbt.size(); i++)
        {
            CompoundNBT textureNbt = texturesNbt.getCompound(i);
            String key = textureNbt.getString("Key");
            INBT value = textureNbt.get("Value");
            if (value == null || value.getType() != ListNBT.TYPE)
                continue;

            ListNBT layersNbt = (ListNBT) value;
            layersNbt.forEach(layerNbt -> GeometryModelTexture.CODEC.parse(NBTDynamicOps.INSTANCE, layerNbt).get().ifLeft(textureSet::add));
            if (!textureSet.isEmpty())
            {
                this.textures.put(key, textureSet.toArray(new GeometryModelTexture[0]));
                textureSet.clear();
            }
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
        this.textures.forEach((key, layers) ->
        {
            buf.writeUtf(key);
            buf.writeVarInt(layers.length);
            for (GeometryModelTexture texture : layers)
                texture.write(buf);
        });
    }

    public CompoundNBT serializeNBT()
    {
        CompoundNBT nbt = new CompoundNBT();

        ListNBT texturesNbt = new ListNBT();
        this.textures.forEach((key, layers) ->
        {
            ListNBT layersNbt = new ListNBT();
            for (int i = 0; i < layers.length; i++)
            {
                GeometryModelTexture.CODEC.encodeStart(NBTDynamicOps.INSTANCE, layers[0]).get().ifLeft(textureValueNbt ->
                {
                    CompoundNBT textureNbt = new CompoundNBT();
                    textureNbt.putString("Key", key);
                    textureNbt.put("Value", textureValueNbt);
                    layersNbt.add(textureNbt);
                });
            }
            texturesNbt.add(layersNbt);
        });
        nbt.put("Textures", texturesNbt);

        return nbt;
    }

    /**
     * Fetches a geometry model texture by the specified key.
     *
     * @param key The key of the textures to get
     * @return The texture with that key or {@link GeometryModelTexture#MISSING} if there is no texture bound to that key
     */
    public GeometryModelTexture[] getLayerTextures(@Nullable String key)
    {
        return this.textures.getOrDefault(key, new GeometryModelTexture[]{GeometryModelTexture.MISSING});
    }

    /**
     * @return All textures that need to be loaded
     */
    public Collection<GeometryModelTexture[]> getTextures()
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
    public static class Serializer implements JsonSerializer<GeometryModelTextureTable>, JsonDeserializer<GeometryModelTextureTable>
    {
        @Override
        public JsonElement serialize(GeometryModelTextureTable src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject texturesObject = new JsonObject();
            for (Map.Entry<String, GeometryModelTexture[]> entry : src.textures.entrySet())
            {
                GeometryModelTexture[] layers = entry.getValue();
                if (layers.length == 0)
                    continue;

                if (layers.length == 1)
                {
                    texturesObject.add(entry.getKey(), GeometryModelTexture.CODEC.encodeStart(JsonOps.INSTANCE, layers[0]).getOrThrow(false, LOGGER::error));
                    continue;
                }

                JsonArray layersJson = new JsonArray();
                for (GeometryModelTexture texture : layers)
                    layersJson.add(GeometryModelTexture.CODEC.encodeStart(JsonOps.INSTANCE, texture).getOrThrow(false, LOGGER::error));
                texturesObject.add(entry.getKey(), layersJson);
            }
            return texturesObject;
        }

        @Override
        public GeometryModelTextureTable deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject texturesObject = json.getAsJsonObject();
            Map<String, GeometryModelTexture[]> textures = new HashMap<>();
            List<GeometryModelTexture> textureSet = new ArrayList<>();
            for (Map.Entry<String, JsonElement> entry : texturesObject.entrySet())
            {
                try
                {
                    if (entry.getValue().isJsonArray())
                    {
                        JsonArray layersJson = entry.getValue().getAsJsonArray();
                        for (int i = 0; i < layersJson.size(); i++)
                            textureSet.add(GeometryModelTexture.CODEC.parse(JsonOps.INSTANCE, layersJson.get(i)).getOrThrow(false, LOGGER::error));
                        if (!textureSet.isEmpty())
                        {
                            textures.put(entry.getKey(), textureSet.toArray(new GeometryModelTexture[0]));
                            textureSet.clear();
                        }
                    }
                    else
                    {
                        textures.put(entry.getKey(), new GeometryModelTexture[]{GeometryModelTexture.CODEC.parse(JsonOps.INSTANCE, entry.getValue()).getOrThrow(false, LOGGER::error)});
                    }
                }
                catch (Exception e)
                {
                    throw new JsonParseException("Failed to load texture '" + entry.getKey() + "'", e);
                }
            }
            return new GeometryModelTextureTable(textures);
        }
    }
}
