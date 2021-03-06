package io.github.ocelot.modelanima.api.common.texture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.ocelot.modelanima.ModelAnima;
import io.github.ocelot.modelanima.api.client.geometry.GeometryModel;
import io.github.ocelot.modelanima.core.client.geometry.GeometryRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Locale;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * <p>A single texture used on a {@link GeometryModel}.</p>
 *
 * @author Ocelot
 * @since 1.0.0
 */
public class GeometryModelTexture
{
    public static final GeometryModelTexture MISSING = new GeometryModelTexture(Type.UNKNOWN, TextureLayer.SOLID, "missingno", false, -1, false, false);
    public static final GeometryModelTexture INVISIBLE = new GeometryModelTexture(Type.INVISIBLE, TextureLayer.SOLID, "missingno", false, -1, false, false);
    public static final Codec<GeometryModelTexture> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.xmap(Type::byName, type -> type.name().toLowerCase(Locale.ROOT)).fieldOf("type").forGetter(GeometryModelTexture::getType),
            Codec.STRING.xmap(TextureLayer::byName, type -> type.name().toLowerCase(Locale.ROOT)).optionalFieldOf("layer", TextureLayer.SOLID).forGetter(GeometryModelTexture::getLayer),
            Codec.STRING.fieldOf("texture").forGetter(GeometryModelTexture::getData),
            Codec.BOOL.optionalFieldOf("cache", true).forGetter(GeometryModelTexture::canCache),
            Codec.STRING.optionalFieldOf("color", "0xFFFFFF").xmap(NumberUtils::createInteger, color -> "0x" + Integer.toHexString(color & 0xFFFFFF).toUpperCase(Locale.ROOT)).forGetter(GeometryModelTexture::getColor),
            Codec.BOOL.optionalFieldOf("glowing", false).forGetter(GeometryModelTexture::isGlowing),
            Codec.BOOL.optionalFieldOf("smoothShading", false).forGetter(GeometryModelTexture::isSmoothShading)
    ).apply(instance, GeometryModelTexture::new));
    private static final Pattern ONLINE_PATTERN = Pattern.compile("=");

    private final Type type;
    private final TextureLayer layer;
    private final String data;
    private final boolean cache;
    private final int color;
    private final boolean glowing;
    private final boolean smoothShading;
    private final ResourceLocation location;

    public GeometryModelTexture(Type type, TextureLayer layer, String data, boolean cache, int color, boolean glowing, boolean smoothShading)
    {
        this.type = type;
        this.layer = layer;
        this.data = data;
        this.cache = cache;
        this.color = color;
        this.glowing = glowing;
        this.smoothShading = smoothShading;
        this.location = type.createLocation(data);
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
     * @return Whether or not caching this texture value is allowed
     */
    public boolean canCache()
    {
        return cache;
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
     * @return Whether or not smooth shading should be used
     */
    public boolean isSmoothShading()
    {
        return smoothShading;
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
        return cache == that.cache && color == that.color && glowing == that.glowing && type == that.type && layer == that.layer && data.equals(that.data);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(type, layer, data, cache, color, glowing);
    }

    @Override
    public String toString()
    {
        return "GeometryModelTexture{" +
                "type=" + type +
                ", layer=" + layer +
                ", data='" + data + '\'' +
                ", cache=" + cache +
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
        INVISIBLE(location -> new ResourceLocation("missingno")),
        LOCATION(ResourceLocation::new),
        ONLINE(location -> new ResourceLocation(ModelAnima.MOD_ID, "base32" + ONLINE_PATTERN.matcher(new Base32().encodeAsString(location.getBytes()).toLowerCase(Locale.ROOT)).replaceAll("_")));

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
        public ResourceLocation createLocation(String data)
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
     * <p>Supported render types for textures.</p>
     *
     * @author Ocelot
     */
    public enum TextureLayer
    {
        SOLID(() -> GeometryRenderTypes::getGeometrySolid),
        CUTOUT(() -> GeometryRenderTypes::getGeometryCutout),
        CUTOUT_CULL(() -> GeometryRenderTypes::getGeometryCutoutCull),
        TRANSLUCENT(() -> GeometryRenderTypes::getGeometryTranslucent),
        TRANSLUCENT_CULL(() -> GeometryRenderTypes::getGeometryTranslucentCull);

        private final Supplier<BiFunction<GeometryModelTexture, ResourceLocation, RenderType>> renderTypeGetter;

        TextureLayer(Supplier<BiFunction<GeometryModelTexture, ResourceLocation, RenderType>> renderTypeGetter)
        {
            this.renderTypeGetter = renderTypeGetter;
        }

        /**
         * Fetches the render type for the specified location.
         *
         * @param texture       The texture to use in the render type
         * @param atlasLocation The location of the texture atlas to use
         * @return The render type for this layer
         */
        @OnlyIn(Dist.CLIENT)
        public RenderType getRenderType(GeometryModelTexture texture, ResourceLocation atlasLocation)
        {
            return this.renderTypeGetter.get().apply(texture, atlasLocation);
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
