package io.github.ocelot.modelanima.api.common.geometry.texture;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.ocelot.modelanima.ModelAnima;
import io.github.ocelot.modelanima.api.client.geometry.GeometryModel;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.LazyValue;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * <p>A single texture used on a {@link GeometryModel}.</p>
 *
 * @author Ocelot
 */
public class GeometryModelTexture
{
    public static final GeometryModelTexture MISSING = new GeometryModelTexture(Type.LOCATION, "missingno", -1, false);

    private final Type type;
    private final String data;
    private final int color;
    private final boolean glowing;
    private final ResourceLocation location;

    public GeometryModelTexture(Type type, String data, int color, boolean glowing)
    {
        this.type = type;
        this.data = data;
        this.color = color;
        this.glowing = glowing;
        this.location = type.getLocation(data);
    }

    public GeometryModelTexture(PacketBuffer buf)
    {
        this.type = Type.byId(buf.readVarInt());
        this.data = buf.readString();
        this.color = buf.readInt();
        this.glowing = buf.readBoolean();
        this.location = type.getLocation(data);
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
        buf.writeBoolean(this.glowing);
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
     * @return Whether or not this texture should be "fullbright"
     */
    public boolean isGlowing()
    {
        return glowing;
    }

    /**
     * @return The location of this texture
     */
    public ResourceLocation getLocation()
    {
        return location;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeometryModelTexture that = (GeometryModelTexture) o;
        return color == that.color &&
                glowing == that.glowing &&
                type == that.type &&
                data.equals(that.data);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(type, data, color, glowing);
    }

    @Override
    public String toString()
    {
        return "GeometryModelTexture{" +
                "type=" + type +
                ", data='" + data + '\'' +
                ", color=" + color +
                ", glowing=" + glowing +
                ", location=" + location +
                '}';
    }

    /**
     * <p>A type of {@link GeometryModelTexture}.</p>
     *
     * @author Ocelot
     */
    public enum Type
    {
        LOCATION((type, json) -> new GeometryModelTexture(type, JSONUtils.getString(json, "location"), parseColor(json), JSONUtils.getBoolean(json, "glowing", false)), ResourceLocation::new),
        ONLINE((type, json) -> new GeometryModelTexture(type, JSONUtils.getString(json, "url"), parseColor(json), JSONUtils.getBoolean(json, "glowing", false)), data -> new ResourceLocation(ModelAnima.DOMAIN, DigestUtils.md5Hex(data)));

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
