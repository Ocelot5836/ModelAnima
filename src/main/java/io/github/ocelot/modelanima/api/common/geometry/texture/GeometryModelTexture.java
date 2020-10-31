package io.github.ocelot.modelanima.api.common.geometry.texture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.ocelot.modelanima.ModelAnima;
import io.github.ocelot.modelanima.api.client.geometry.GeometryModel;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.Base64;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

/**
 * <p>A single texture used on a {@link GeometryModel}.</p>
 *
 * @author Ocelot
 */
public class GeometryModelTexture
{
    public static final GeometryModelTexture MISSING = new GeometryModelTexture(Type.UNKNOWN, "missingno", -1, false);
    public static final Codec<GeometryModelTexture> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.xmap(Type::byName, type -> type.name().toLowerCase(Locale.ROOT)).fieldOf("type").forGetter(GeometryModelTexture::getType),
            Codec.STRING.fieldOf("texture").forGetter(GeometryModelTexture::getTexture),
            Codec.INT.optionalFieldOf("color", -1).forGetter(GeometryModelTexture::getColor),
            Codec.BOOL.optionalFieldOf("glowing", false).forGetter(GeometryModelTexture::isGlowing)
    ).apply(instance, GeometryModelTexture::new));

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
    public String getTexture()
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
        UNKNOWN(location -> new ResourceLocation("missingno")),
        LOCATION(ResourceLocation::new),
        ONLINE(location -> new ResourceLocation(ModelAnima.DOMAIN, DigestUtils.md5Hex(location)));

        private final Function<String, ResourceLocation> locationGenerator;

        Type(Function<String, ResourceLocation> locationGenerator)
        {
            this.locationGenerator = locationGenerator;
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
         * @return The type by that name or {@link #UNKNOWN} if there is no type by that name
         */
        public static Type byName(String name)
        {
            for (Type type : values())
                if (type.name().equalsIgnoreCase(name))
                    return type;
            return UNKNOWN;
        }
    }
}
