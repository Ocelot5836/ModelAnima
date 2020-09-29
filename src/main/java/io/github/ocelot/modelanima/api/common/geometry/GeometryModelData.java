package io.github.ocelot.modelanima.api.common.geometry;

import com.google.gson.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.ocelot.modelanima.api.common.geometry.texture.GeometryModelTexture;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.math.vector.Vector3f;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.*;

/**
 * <p>Deserializes custom java models from JSON.</p>
 *
 * @author Ocelot
 * @since 1.0.0
 */
public class GeometryModelData
{
    private final Description description;
    private final Bone[] bones;

    public GeometryModelData(Description description, Bone[] bones)
    {
        this.description = description;
        this.bones = bones;
    }

    public GeometryModelData()
    {
        this(new Description("custom_model", 0, 0, 0, 0, new Vector3f()), new Bone[0]);
    }

    /**
     * @return Information about this model
     */
    public Description getDescription()
    {
        return description;
    }

    /**
     * @return The individual parts in the model
     */
    public Bone[] getBones()
    {
        return bones;
    }

    @Override
    public String toString()
    {
        return "BedrockModel{" +
                "description=" + description +
                ", bones=" + Arrays.toString(bones) +
                '}';
    }

    /**
     * <p>Information about the model.</p>
     *
     * @author Ocelot
     * @since 1.0.0
     */
    public static class Description
    {
        private final String identifier;
        private final int textureWidth;
        private final int textureHeight;
        private final int visibleBoundsWidth;
        private final int visibleBoundsHeight;
        private final Vector3f visibleBoundsOffset;

        public Description(String identifier, int textureWidth, int textureHeight, int visibleBoundsWidth, int visibleBoundsHeight, Vector3f visibleBoundsOffset)
        {
            this.identifier = identifier;
            this.textureWidth = textureWidth;
            this.textureHeight = textureHeight;
            this.visibleBoundsWidth = visibleBoundsWidth;
            this.visibleBoundsHeight = visibleBoundsHeight;
            this.visibleBoundsOffset = visibleBoundsOffset;
        }

        /**
         * @return The identifier of this model
         */
        public String getIdentifier()
        {
            return identifier;
        }

        /**
         * @return The width of the texture in pixels
         */
        public int getTextureWidth()
        {
            return textureWidth;
        }

        /**
         * @return The height of the texture in pixels
         */
        public int getTextureHeight()
        {
            return textureHeight;
        }

        public int getVisibleBoundsWidth()
        {
            return visibleBoundsWidth;
        }

        public int getVisibleBoundsHeight()
        {
            return visibleBoundsHeight;
        }

        public Vector3f getVisibleBoundsOffset()
        {
            return visibleBoundsOffset;
        }

        @Override
        public String toString()
        {
            return "Description{" +
                    "identifier='" + identifier + '\'' +
                    ", textureWidth=" + textureWidth +
                    ", textureHeight=" + textureHeight +
                    ", visibleBoundsWidth=" + visibleBoundsWidth +
                    ", visibleBoundsHeight=" + visibleBoundsHeight +
                    ", visibleBoundsOffset=" + visibleBoundsOffset +
                    '}';
        }

        public static class Deserializer implements JsonDeserializer<Description>
        {
            @Override
            public Description deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
            {
                JsonObject jsonObject = json.getAsJsonObject();
                return new Description(JSONUtils.getString(jsonObject, "identifier", "custom_model"), JSONUtils.getInt(jsonObject, "texture_width", 0), JSONUtils.getInt(jsonObject, "texture_height", 0), JSONUtils.getInt(jsonObject, "visible_bounds_width", 0), JSONUtils.getInt(jsonObject, "visible_bounds_height", 0), parseVector(jsonObject, "visible_bounds_offset", 3, false));
            }
        }
    }

    /**
     * <p>A single bone equivalent to {@link ModelRenderer}.</p>
     *
     * @author Ocelot
     * @since 1.0.0
     */
    public static class Bone
    {
        private final String name;
        private final String parent;
        private final String texture;
        private final Vector3f pivot;
        private final Vector3f rotation;
        private final boolean mirror;
        private final Cube[] cubes;
        private final Locator[] locators;

        public Bone(String name, @Nullable String parent, String texture, Vector3f pivot, Vector3f rotation, boolean mirror, Cube[] cubes, Locator[] locators)
        {
            this.name = name;
            this.parent = parent;
            this.texture = texture;
            this.pivot = pivot;
            this.rotation = rotation;
            this.mirror = mirror;
            this.cubes = cubes;
            this.locators = locators;
        }

        /**
         * Adds the cubes in this model to the specified model renderer.
         *
         * @param model The model to add a renderer to
         * @return The model renderer created
         */
        public ModelRenderer createModelRenderer(Model model)
        {
            ModelRenderer modelRenderer = new ModelRenderer(model);

            modelRenderer.setRotationPoint(this.pivot.getX(), -this.pivot.getY(), this.pivot.getZ());
            modelRenderer.rotateAngleX = (float) Math.toRadians(this.rotation.getX());
            modelRenderer.rotateAngleY = (float) Math.toRadians(this.rotation.getY());
            modelRenderer.rotateAngleZ = (float) Math.toRadians(this.rotation.getZ());

            for (Cube cube : this.cubes)
            {
                modelRenderer.mirror = this.mirror;
                modelRenderer.setTextureOffset(cube.u, cube.v);
                modelRenderer.addBox(cube.origin.getX() - this.pivot.getX(), -cube.origin.getY() - cube.size.getY() + this.pivot.getY(), cube.origin.getZ() - this.pivot.getZ(), cube.size.getX(), cube.size.getY(), cube.size.getZ(), cube.inflate);
            }
            return modelRenderer;
        }

        /**
         * @return The name of this bone
         */
        public String getName()
        {
            return name;
        }

        /**
         * @return The name of the parent bone
         */
        @Nullable
        public String getParent()
        {
            return parent;
        }

        /**
         * @return The texture key this bone uses
         */
        public String getTexture()
        {
            return texture;
        }

        /**
         * @return The rotation point of this entire part
         */
        public Vector3f getPivot()
        {
            return pivot;
        }

        /**
         * @return The static rotation of the part
         */
        public Vector3f getRotation()
        {
            return rotation;
        }

        /**
         * @return Whether or not the texture on this part is mirrored
         */
        public boolean isMirror()
        {
            return mirror;
        }

        /**
         * @return The boxes inside of this part
         */
        public Cube[] getCubes()
        {
            return cubes;
        }

        /**
         * @return The locators inside of this part
         */
        public Locator[] getLocators()
        {
            return locators;
        }

        @Override
        public String toString()
        {
            return "Bone{" +
                    "name='" + name + '\'' +
                    ", parent='" + parent + '\'' +
                    ", texture='" + texture + '\'' +
                    ", pivot=" + pivot +
                    ", rotation=" + rotation +
                    ", mirror=" + mirror +
                    ", cubes=" + Arrays.toString(cubes) +
                    ", locators=" + Arrays.toString(locators) +
                    '}';
        }

        public static class Deserializer implements JsonDeserializer<Bone>
        {
            @Override
            public Bone deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
            {
                JsonObject cubeJson = json.getAsJsonObject();

                List<Locator> locators = new ArrayList<>();
                if (cubeJson.has("locators"))
                {
                    for (Map.Entry<String, JsonElement> locatorJson : cubeJson.getAsJsonObject("locators").entrySet())
                    {
                        locators.add(new Locator(locatorJson.getKey(), parseVector(cubeJson.getAsJsonObject("locators"), locatorJson.getKey(), 3, true)));
                    }
                }

                return new Bone(cubeJson.get("name").getAsString(), JSONUtils.getString(cubeJson, "parent", null), JSONUtils.getString(cubeJson, "texture", "texture"), parseVector(cubeJson, "pivot", 3, true), parseVector(cubeJson, "rotation", 3, false), JSONUtils.getBoolean(cubeJson, "mirror", false), cubeJson.has("cubes") ? context.deserialize(cubeJson.get("cubes"), Cube[].class) : new Cube[0], locators.toArray(new Locator[0]));
            }
        }
    }


    /**
     * <p>A single box in the model.</p>
     *
     * @author Ocelot
     * @since 1.0.0
     */
    public static class Cube
    {
        private final Vector3f origin;
        private final Vector3f size;
        private final float inflate;
        private final int u;
        private final int v;

        public Cube(Vector3f origin, Vector3f size, float inflate, int u, int v)
        {
            this.origin = origin;
            this.size = size;
            this.inflate = inflate;
            this.u = u;
            this.v = v;
        }

        /**
         * @return The rotation origin of this model
         */
        public Vector3f getOrigin()
        {
            return origin;
        }

        /**
         * @return The x, y, and z size of the cube
         */
        public Vector3f getSize()
        {
            return size;
        }

        /**
         * @return The amount the cube should be expanded in all directions
         */
        public float getInflate()
        {
            return inflate;
        }

        /**
         * @return The x position of the cube on the texture
         */
        public int getU()
        {
            return u;
        }

        /**
         * @return The y position of the cube on the texture
         */
        public int getV()
        {
            return v;
        }

        @Override
        public String toString()
        {
            return "Cube{" +
                    "origin=" + origin +
                    ", size=" + size +
                    ", inflate=" + inflate +
                    ", u=" + u +
                    ", v=" + v +
                    '}';
        }

        public static class Deserializer implements JsonDeserializer<Cube>
        {
            @Override
            public Cube deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
            {
                JsonObject cubeJson = json.getAsJsonObject();
                Vector3f uv = parseVector(cubeJson, "uv", 2, true);
                return new Cube(parseVector(cubeJson, "origin", 3, true), parseVector(cubeJson, "size", 3, true), JSONUtils.getFloat(cubeJson, "inflate", 0.0f), (int) uv.getX(), (int) uv.getY());
            }
        }
    }

    /**
     * <p>A single marker position inside a bone.</p>
     *
     * @author Ocelot
     * @since 1.0.0
     */
    public static class Locator
    {
        private final String name;
        private final Vector3f position;

        public Locator(String name, Vector3f position)
        {
            this.name = name;
            this.position = position;
        }

        /**
         * @return The name of this marker
         */
        public String getName()
        {
            return name;
        }

        /**
         * @return The location of this marker
         */
        public Vector3f getPosition()
        {
            return position;
        }

        @Override
        public String toString()
        {
            return "Locator{" +
                    "name='" + name + '\'' +
                    ", position=" + position +
                    '}';
        }
    }

    private static Vector3f parseVector(JsonObject json, String name, int length, boolean required) throws JsonSyntaxException
    {
        if (!json.has(name) && !required)
            return new Vector3f();
        if (!json.has(name) || !json.get(name).isJsonArray())
            throw new JsonSyntaxException("Expected '" + name + "' as an array");

        JsonArray vectorJson = json.getAsJsonArray(name);
        if (vectorJson.size() != length)
            throw new JsonParseException("Expected " + length + " " + name + " values, found: " + vectorJson.size());

        float[] values = new float[length];
        for (int i = 0; i < values.length; i++)
            values[i] = JSONUtils.getFloat(vectorJson.get(i), name + "[" + i + "]");

        return new Vector3f(values.length > 0 ? values[0] : 0, values.length > 1 ? values[1] : 0, values.length > 2 ? values[2] : 0);
    }
}
