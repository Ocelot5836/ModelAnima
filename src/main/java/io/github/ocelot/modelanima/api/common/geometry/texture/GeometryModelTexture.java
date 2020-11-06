package io.github.ocelot.modelanima.api.common.geometry.texture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.ocelot.modelanima.ModelAnima;
import io.github.ocelot.modelanima.api.client.geometry.GeometryModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.codec.binary.Base32;

import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * <p>A single texture used on a {@link GeometryModel}.</p>
 *
 * @author Ocelot
 * @since 1.0.0
 */
public class GeometryModelTexture
{
    public static final GeometryModelTexture MISSING = new GeometryModelTexture(Type.UNKNOWN, TextureLayer.SOLID, "missingno", -1, false);
    public static final Codec<GeometryModelTexture> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.xmap(Type::byName, type -> type.name().toLowerCase(Locale.ROOT)).fieldOf("type").forGetter(GeometryModelTexture::getType),
            Codec.STRING.xmap(TextureLayer::byName, type -> type.name().toLowerCase(Locale.ROOT)).optionalFieldOf("layer", TextureLayer.SOLID).forGetter(GeometryModelTexture::getLayer),
            Codec.STRING.fieldOf("texture").forGetter(GeometryModelTexture::getData),
            Codec.INT.optionalFieldOf("color", -1).forGetter(GeometryModelTexture::getColor),
            Codec.BOOL.optionalFieldOf("glowing", false).forGetter(GeometryModelTexture::isGlowing)
    ).apply(instance, GeometryModelTexture::new));
    private static final Pattern ONLINE_PATTERN = Pattern.compile("=");

    private final Type type;
    private final TextureLayer layer;
    private final String data;
    private final int color;
    private final boolean glowing;
    private final ResourceLocation location;

    public GeometryModelTexture(Type type, TextureLayer layer, String data, int color, boolean glowing)
    {
        this.type = type;
        this.layer = layer;
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
     * @return The layer this texture uses
     */
    public TextureLayer getLayer()
    {
        return layer;
    }

    /**
     * @return The additional data of this texture. May be a URL depending on {@link #getType()}
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
        UNKNOWN(location -> MissingTextureSprite.getLocation()),
        LOCATION(ResourceLocation::new),
        ONLINE(location -> new ResourceLocation(ModelAnima.DOMAIN, "base32" + ONLINE_PATTERN.matcher(new Base32().encodeAsString(location.getBytes()).toLowerCase(Locale.ROOT)).replaceAll("_")));

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

    /**
     * <p>Supported types of</p>
     *
     * @author Ocelot
     */
    public enum TextureLayer
    {
        SOLID(RenderType::getEntitySolid),
        CUTOUT(RenderType::getEntityCutoutNoCull),
        TRANSLUCENT(RenderType::getEntityTranslucent);

        private final Function<ResourceLocation, RenderType> renderTypeGetter;

        TextureLayer(Function<ResourceLocation, RenderType> renderTypeGetter)
        {
            this.renderTypeGetter = renderTypeGetter;
        }

        /**
         * Fetches the render type for the specified location.
         *
         * @param location The texture to use in the render type
         * @return The render type for this layer
         */
        public RenderType getRenderType(ResourceLocation location)
        {
            return this.renderTypeGetter.apply(location);
        }

        /**
         * Fetches a texture layer by the specified name.
         *
         * @param name The name of the texture layer
         * @return The texture layer by that name or {@link #CUTOUT} if there is no layer by that name
         */
        public static TextureLayer byName(String name)
        {
            for (TextureLayer layer : values())
                if (layer.name().equalsIgnoreCase(name))
                    return layer;
            return CUTOUT;
        }
    }
}
