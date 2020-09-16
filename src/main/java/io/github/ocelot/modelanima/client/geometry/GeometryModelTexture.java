package io.github.ocelot.modelanima.client.geometry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.ocelot.modelanima.ModelAnima;
import io.github.ocelot.modelanima.client.render.BedrockGeometryModel;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.LazyValue;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * <p>A single texture used on a {@link BedrockGeometryModel}.</p>
 *
 * @author Ocelot
 */
public class GeometryModelTexture
{
    public static final GeometryModelTexture MISSING = new GeometryModelTexture(Type.LOCATION, "missingno", -1);

    private final Type type;
    private final String data;
    private final int color;
    private final LazyValue<ResourceLocation> location;

    public GeometryModelTexture(Type type, String data, int color)
    {
        this.type = type;
        this.data = data;
        this.color = color;
        this.location = new LazyValue<>(() -> type.getLocation(data));
    }

    public GeometryModelTexture(PacketBuffer buf)
    {
        this.type = Type.byId(buf.readVarInt());
        this.data = buf.readString();
        this.color = buf.readInt();
        this.location = new LazyValue<>(() -> this.type.getLocation(this.data));
    }

    /**
     * Writes this texture into the specified buffer.
     *
     * @param buf The buffer to fill with data
     */
    public void write(PacketBuffer buf)
    {
        buf.writeVarInt(this.type.ordinal());
        buf.writeString(this.data);
        buf.writeInt(this.color);
    }

    /**
     * @return The type of texture this cosmetic texture is
     */
    public Type getType()
    {
        return type;
    }

    /**
     * @return The location of this texture. May be a URL depending on {@link #getType()}
     */
    public String getData()
    {
        return data;
    }

    /**
     * @return The color of this texture
     */
    public int getColor()
    {
        return color;
    }

    /**
     * @return The red color factor of this texture
     */
    public float getRed()
    {
        return ((this.color >> 16) & 0xff) / 255f;
    }

    /**
     * @return The green color factor of this texture
     */
    public float getGreen()
    {
        return ((this.color >> 8) & 0xff) / 255f;
    }

    /**
     * @return The blue color factor of this texture
     */
    public float getBlue()
    {
        return (this.color & 0xff) / 255f;
    }

    /**
     * @return The location of this texture
     */
    public ResourceLocation getLocation()
    {
        return this.location.getValue();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeometryModelTexture texture = (GeometryModelTexture) o;
        return color == texture.color &&
                type == texture.type &&
                data.equals(texture.data);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(type, data, color);
    }

    @Override
    public String toString()
    {
        return "CosmeticModelTexture{" +
                "type=" + type +
                ", data='" + data + '\'' +
                ", color=" + color +
                '}';
    }

    /**
     * <p>A type of {@link GeometryModelTexture}.</p>
     *
     * @author Ocelot
     */
    public enum Type
    {
        LOCATION((type, json) -> new GeometryModelTexture(type, JSONUtils.getString(json, "location"), parseColor(json)), ResourceLocation::new),
        ONLINE((type, json) -> new GeometryModelTexture(type, JSONUtils.getString(json, "url"), parseColor(json)), data -> new ResourceLocation(ModelAnima.DOMAIN, DigestUtils.md5Hex(data)));

        private final BiFunction<Type, JsonObject, GeometryModelTexture> deserializer;
        private final Function<String, ResourceLocation> locationGenerator;

        Type(BiFunction<Type, JsonObject, GeometryModelTexture> deserializer, Function<String, ResourceLocation> locationGenerator)
        {
            this.deserializer = deserializer;
            this.locationGenerator = locationGenerator;
        }

        private static int parseColor(JsonObject json)
        {
            if (!json.has("color"))
                return -1;
            JsonElement element = json.get("color");
            if (!element.isJsonPrimitive() && !element.getAsJsonPrimitive().isString() && !element.getAsJsonPrimitive().isNumber())
                throw new JsonSyntaxException("Missing color, expected to find a string or int");
            return element.getAsJsonPrimitive().isString() ? Integer.parseUnsignedInt(JSONUtils.getString(json, "color"), 16) : JSONUtils.getInt(json, "color", -1);
        }

        /**
         * Creates a new {@link GeometryModelTexture} from the specified element.
         *
         * @param object The json to read texture data from
         * @return A new texture from that element
         */
        public GeometryModelTexture deserialize(JsonObject object)
        {
            return this.deserializer.apply(this, object);
        }

        /**
         * Creates a new {@link ResourceLocation} based on the specified data string.
         *
         * @param data The data to convert
         * @return The new location for that data
         */
        public ResourceLocation getLocation(String data)
        {
            return this.locationGenerator.apply(data);
        }

        /**
         * Fetches a type of texture by the specified name.
         *
         * @param name The name of the type of texture
         * @return The type by that name
         */
        public static Type byName(String name)
        {
            for (Type type : values())
                if (type.name().equalsIgnoreCase(name))
                    return type;
            throw new JsonSyntaxException("Unknown cosmetic texture type '" + name + "'");
        }

        /**
         * Fetches a type of texture by the specified ordinal.
         *
         * @param id The ordinal of the type of texture
         * @return The type by that ordinal
         */
        public static Type byId(int id)
        {
            if (id < 0 || id >= values().length)
                throw new IllegalArgumentException("Unknown cosmetic texture type with ordinal '" + id + "'");
            return values()[id];
        }
    }
}
