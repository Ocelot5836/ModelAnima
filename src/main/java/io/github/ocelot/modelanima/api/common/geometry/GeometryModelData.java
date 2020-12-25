package io.github.ocelot.modelanima.api.common.geometry;

import com.google.gson.*;
import io.github.ocelot.modelanima.api.common.util.JSONTupleParser;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.Direction;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3f;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.function.Function;

/**
 * <p>Deserializes custom java models from JSON.</p>
 *
 * @author Ocelot
 * @since 1.0.0
 */
public class GeometryModelData
{
    /**
     * A completely empty model definition.
     */
    public static final GeometryModelData EMPTY = new GeometryModelData(new Description("empty", 0, 0, 0, 0, 0, 256, 256, false), new Bone[0]);

    private final Description description;
    private final Bone[] bones;

    public GeometryModelData(Description description, Bone[] bones)
    {
        this.description = description;
        this.bones = bones;
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
        private final float visibleBoundsWidth;
        private final float visibleBoundsHeight;
        private final float visibleBoundsOffsetX;
        private final float visibleBoundsOffsetY;
        private final float visibleBoundsOffsetZ;
        private final int textureWidth;
        private final int textureHeight;
        private final boolean preserveModelPose2588;

        public Description(String identifier, float visibleBoundsWidth, float visibleBoundsHeight, float visibleBoundsOffsetX, float visibleBoundsOffsetY, float visibleBoundsOffsetZ, int textureWidth, int textureHeight, boolean preserveModelPose2588)
        {
            this.identifier = identifier;
            this.visibleBoundsWidth = visibleBoundsWidth;
            this.visibleBoundsHeight = visibleBoundsHeight;
            this.visibleBoundsOffsetX = visibleBoundsOffsetX;
            this.visibleBoundsOffsetY = visibleBoundsOffsetY;
            this.visibleBoundsOffsetZ = visibleBoundsOffsetZ;
            this.textureWidth = textureWidth;
            this.textureHeight = textureHeight;
            this.preserveModelPose2588 = preserveModelPose2588;
        }

        /**
         * @return The identifier of this model. Used to refer to this geometry definition
         */
        public String getIdentifier()
        {
            return identifier;
        }

        /**
         * @return The width of the visibility bounding box
         */
        public float getVisibleBoundsWidth()
        {
            return visibleBoundsWidth;
        }

        /**
         * @return The height of the visibility bounding box
         */
        public float getVisibleBoundsHeight()
        {
            return visibleBoundsHeight;
        }

        /**
         * @return The offset of the visibility bounding box from the origin in the x axis
         */
        public float getVisibleBoundsOffsetX()
        {
            return visibleBoundsOffsetX;
        }

        /**
         * @return The offset of the visibility bounding box from the origin in the y axis
         */
        public float getVisibleBoundsOffsetY()
        {
            return visibleBoundsOffsetY;
        }

        /**
         * @return The offset of the visibility bounding box from the origin in the z axis
         */
        public float getVisibleBoundsOffsetZ()
        {
            return visibleBoundsOffsetZ;
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

        public boolean isPreserveModelPose2588()
        {
            return preserveModelPose2588;
        }

        @Override
        public String toString()
        {
            return "Description{" +
                    "identifier='" + identifier + '\'' +
                    ", visibleBoundsWidth=" + visibleBoundsWidth +
                    ", visibleBoundsHeight=" + visibleBoundsHeight +
                    ", visibleBoundsOffset=(" + visibleBoundsOffsetX + ", " + visibleBoundsOffsetY + ", " + visibleBoundsOffsetZ + ")" +
                    ", textureWidth=" + textureWidth +
                    ", textureHeight=" + textureHeight +
                    ", preserveModelPose2588=" + preserveModelPose2588 +
                    '}';
        }

        public static class Deserializer implements JsonDeserializer<Description>
        {
            @Override
            public Description deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
            {
                JsonObject jsonObject = json.getAsJsonObject();
                String identifier = JSONUtils.getString(jsonObject, "identifier", "custom_model");
                float visibleBoundsWidth = JSONUtils.getFloat(jsonObject, "visible_bounds_width", 0);
                float visibleBoundsHeight = JSONUtils.getFloat(jsonObject, "visible_bounds_height", 0);
                float[] visibleBoundsOffset = JSONTupleParser.getFloat(jsonObject, "visible_bounds_offset", 3, () -> new float[3]);
                int textureWidth = JSONUtils.getInt(jsonObject, "texture_width", 256);
                int textureHeight = JSONUtils.getInt(jsonObject, "texture_height", 256);
                boolean preserveModelPose2588 = JSONUtils.getBoolean(jsonObject, "preserve_model_pose2588", false);
                if (textureWidth == 0)
                    throw new JsonSyntaxException("Texture width must not be zero");
                if (textureHeight == 0)
                    throw new JsonSyntaxException("Texture height must not be zero");
                return new Description(identifier, visibleBoundsWidth, visibleBoundsHeight, visibleBoundsOffset[0], visibleBoundsOffset[1], visibleBoundsOffset[2], textureWidth, textureHeight, preserveModelPose2588);
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
        private final boolean reset2588;
        private final boolean neverRender2588;
        private final String parent;
        private final float pivotX;
        private final float pivotY;
        private final float pivotZ;
        private final float rotationX;
        private final float rotationY;
        private final float rotationZ;
        private final float bindPoseRotation2588X;
        private final float bindPoseRotation2588Y;
        private final float bindPoseRotation2588Z;
        private final boolean mirror;
        private final float inflate;
        private final boolean debug;
        private final Cube[] cubes;
        private final Locator[] locators;
        private final PolyMesh polyMesh;

        public Bone(String name, boolean reset2588, boolean neverRender2588, @Nullable String parent, float pivotX, float pivotY, float pivotZ, float rotationX, float rotationY, float rotationZ, float bindPoseRotation2588X, float bindPoseRotation2588Y, float bindPoseRotation2588Z, boolean mirror, float inflate, boolean debug, Cube[] cubes, Locator[] locators, @Nullable PolyMesh polyMesh)
        {
            this.name = name;
            this.reset2588 = reset2588;
            this.neverRender2588 = neverRender2588;
            this.parent = parent;
            this.pivotX = pivotX;
            this.pivotY = pivotY;
            this.pivotZ = pivotZ;
            this.rotationX = rotationX;
            this.rotationY = rotationY;
            this.rotationZ = rotationZ;
            this.bindPoseRotation2588X = bindPoseRotation2588X;
            this.bindPoseRotation2588Y = bindPoseRotation2588Y;
            this.bindPoseRotation2588Z = bindPoseRotation2588Z;
            this.mirror = mirror;
            this.inflate = inflate;
            this.debug = debug;
            this.cubes = cubes;
            this.locators = locators;
            this.polyMesh = polyMesh;
        }

        /**
         * @return The identifier used when fetching this bone
         */
        public String getName()
        {
            return name;
        }

        public boolean isReset2588()
        {
            return reset2588;
        }

        public boolean isNeverRender2588()
        {
            return neverRender2588;
        }

        /**
         * @return The bone this bone is relative to
         */
        @Nullable
        public String getParent()
        {
            return parent;
        }

        /**
         * @return The position this bone pivots around in the x-axis
         */
        public float getPivotX()
        {
            return pivotX;
        }

        /**
         * @return The position this bone pivots around in the y-axis
         */
        public float getPivotY()
        {
            return pivotY;
        }

        /**
         * @return The position this bone pivots around in the z-axis
         */
        public float getPivotZ()
        {
            return pivotZ;
        }

        /**
         * @return The initial rotation of this bone in degrees in the x-axis
         */
        public float getRotationX()
        {
            return rotationX;
        }

        /**
         * @return The initial rotation of this bone in degrees in the y-axis
         */
        public float getRotationY()
        {
            return rotationY;
        }

        /**
         * @return The initial rotation of this bone in degrees in the z-axis
         */
        public float getRotationZ()
        {
            return rotationZ;
        }

        public float getBindPoseRotation2588X()
        {
            return bindPoseRotation2588X;
        }

        public float getBindPoseRotation2588Y()
        {
            return bindPoseRotation2588Y;
        }

        public float getBindPoseRotation2588Z()
        {
            return bindPoseRotation2588Z;
        }

        /**
         * @return Whether or not the cube should be mirrored along the un-rotated x-axis
         */
        public boolean isMirror()
        {
            return mirror;
        }

        /**
         * @return The amount to grow in all directions
         */
        public float getInflate()
        {
            return inflate;
        }

        public boolean isDebug()
        {
            return debug;
        }

        /**
         * @return The list of cubes associated with this bone
         */
        public Cube[] getCubes()
        {
            return cubes;
        }

        /**
         * @return The list of positions attached to this bone
         */
        public Locator[] getLocators()
        {
            return locators;
        }

        /**
         * @return The polygon mesh associated with the bone or <code>null</code> if there is no poly mesh for this type
         */
        @Nullable
        public PolyMesh getPolyMesh()
        {
            return polyMesh;
        }

        @Override
        public String toString()
        {
            return "Bone{" +
                    "name='" + name + '\'' +
                    ", reset2588=" + reset2588 +
                    ", neverRender2588=" + neverRender2588 +
                    ", parent='" + parent + '\'' +
                    ", pivot=(" + pivotX + "," + pivotY + "," + pivotZ + ")" +
                    ", rotation(" + rotationX + "," + rotationY + "," + rotationZ + ")" +
                    ", bindPoseRotation2588(" + bindPoseRotation2588X + "," + bindPoseRotation2588Y + "," + bindPoseRotation2588Z + ")" +
                    ", mirror=" + mirror +
                    ", inflate=" + inflate +
                    ", debug=" + debug +
                    ", cubes=" + Arrays.toString(cubes) +
                    ", locators=" + Arrays.toString(locators) +
                    '}';
        }

        public static class Deserializer implements JsonDeserializer<Bone>
        {
            @Override
            public Bone deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
            {
                JsonObject boneJson = json.getAsJsonObject();
                String name = JSONUtils.getString(boneJson, "name");
                boolean reset2588 = JSONUtils.getBoolean(boneJson, "reset2588", false);
                boolean neverRender2588 = JSONUtils.getBoolean(boneJson, "neverrender2588", false);
                String parent = JSONUtils.getString(boneJson, "parent", null);
                float[] pivot = JSONTupleParser.getFloat(boneJson, "pivot", 3, () -> new float[3]);
                float[] rotation = JSONTupleParser.getFloat(boneJson, "rotation", 3, () -> new float[3]);
                float[] bindPoseRotation2588 = JSONTupleParser.getFloat(boneJson, "bind_pose_rotation2588", 3, () -> new float[3]);
                boolean mirror = JSONUtils.getBoolean(boneJson, "mirror", false);
                float inflate = JSONUtils.getFloat(boneJson, "inflate", 0);
                boolean debug = JSONUtils.getBoolean(boneJson, "debug", false);

                Cube[] cubes = new Cube[0];
                if (boneJson.has("cubes"))
                {
                    JsonArray cubesJson = JSONUtils.getJsonArray(boneJson, "cubes");
                    cubes = new Cube[cubesJson.size()];
                    for (int i = 0; i < cubesJson.size(); i++)
                        cubes[i] = context.deserialize(cubesJson.get(i), Cube.class);
                }

                Locator[] locators = new Locator[0];
                if (boneJson.has("locators"))
                {
                    JsonObject locatorsJson = JSONUtils.getJsonObject(boneJson, "locators");
                    locators = locatorsJson.entrySet().stream().map(entry ->
                    {
                        String locatorIdentifier = entry.getKey();
                        float[] locatorPosition = JSONTupleParser.getFloat(locatorsJson, locatorIdentifier, 3, () -> new float[3]);
                        return new Locator(locatorIdentifier, locatorPosition[0], locatorPosition[1], locatorPosition[2]);
                    }).toArray(Locator[]::new);
                }

                PolyMesh polyMesh = boneJson.has("poly_mesh") ? context.deserialize(boneJson.get("poly_mesh"), PolyMesh.class) : null;

                // TODO texture_mesh

                return new Bone(name, reset2588, neverRender2588, parent, pivot[0], pivot[1], pivot[2], rotation[0], rotation[1], rotation[2], bindPoseRotation2588[0], bindPoseRotation2588[1], bindPoseRotation2588[2], mirror, inflate, debug, cubes, locators, polyMesh);
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
        private final float originX;
        private final float originY;
        private final float originZ;
        private final float sizeX;
        private final float sizeY;
        private final float sizeZ;
        private final float rotationX;
        private final float rotationY;
        private final float rotationZ;
        private final float pivotX;
        private final float pivotY;
        private final float pivotZ;
        private final boolean overrideInflate;
        private final float inflate;
        private final boolean overrideMirror;
        private final boolean mirror;
        private final CubeUV[] uv;

        public Cube(float originX, float originY, float originZ, float sizeX, float sizeY, float sizeZ, float rotationX, float rotationY, float rotationZ, float pivotX, float pivotY, float pivotZ, boolean overrideInflate, float inflate, boolean overrideMirror, boolean mirror, CubeUV[] uv)
        {
            this.originX = originX;
            this.originY = originY;
            this.originZ = originZ;
            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.sizeZ = sizeZ;
            this.rotationX = rotationX;
            this.rotationY = rotationY;
            this.rotationZ = rotationZ;
            this.pivotX = pivotX;
            this.pivotY = pivotY;
            this.pivotZ = pivotZ;
            this.overrideInflate = overrideInflate;
            this.inflate = inflate;
            this.overrideMirror = overrideMirror;
            this.mirror = mirror;
            Validate.isTrue(uv.length == Direction.values().length);
            this.uv = uv;
        }

        /**
         * @return The un-rotated lower corner of the cube in the x-axis
         */
        public float getOriginX()
        {
            return originX;
        }

        /**
         * @return The un-rotated lower corner of the cube in the y-axis
         */
        public float getOriginY()
        {
            return originY;
        }

        /**
         * @return The un-rotated lower corner of the cube in the z-axis
         */
        public float getOriginZ()
        {
            return originZ;
        }

        /**
         * @return The amount to extend beyond the origin in the x-axis
         */
        public float getSizeX()
        {
            return sizeX;
        }

        /**
         * @return The amount to extend beyond the origin in the y-axis
         */
        public float getSizeY()
        {
            return sizeY;
        }

        /**
         * @return The amount to extend beyond the origin in the z-axis
         */
        public float getSizeZ()
        {
            return sizeZ;
        }

        /**
         * @return The amount in degrees to rotate around the pivot in the x-axis
         */
        public float getRotationX()
        {
            return rotationX;
        }

        /**
         * @return The amount in degrees to rotate around the pivot in the y-axis
         */
        public float getRotationY()
        {
            return rotationY;
        }

        /**
         * @return The amount in degrees to rotate around the pivot in the z-axis
         */
        public float getRotationZ()
        {
            return rotationZ;
        }

        /**
         * @return The position to pivot rotation around in the x-axis
         */
        public float getPivotX()
        {
            return pivotX;
        }

        /**
         * @return The position to pivot rotation around in the y-axis
         */
        public float getPivotY()
        {
            return pivotY;
        }

        /**
         * @return The position to pivot rotation around in the z-axis
         */
        public float getPivotZ()
        {
            return pivotZ;
        }

        /**
         * @return Whether or not this inflate value should be used instead of the bone value
         */
        public boolean isOverrideInflate()
        {
            return overrideInflate;
        }

        /**
         * @return The amount to grow in all directions
         */
        public float getInflate()
        {
            return inflate;
        }

        /**
         * @return Whether or not this mirror value should be used instead of the bone value
         */
        public boolean isOverrideMirror()
        {
            return overrideMirror;
        }

        /**
         * @return Whether or not the cube should be mirrored along the un-rotated x-axis
         */
        public boolean isMirror()
        {
            return mirror;
        }

        /**
         * Fetches the uv for the specified face.
         *
         * @param direction The direction of the face to fetch
         * @return The uv for that face or <code>null</code> to skip that face
         */
        @Nullable
        public CubeUV getUV(Direction direction)
        {
            return this.uv[direction.getIndex()];
        }

        /**
         * @return The uvs for all faces
         */
        public CubeUV[] getUVs()
        {
            return uv;
        }

        @Override
        public String toString()
        {
            return "Cube{" +
                    "origin=(" + originX + "," + originY + "," + originZ + ")" +
                    ", size=(" + sizeX + "," + sizeY + "," + sizeZ + ")" +
                    ", rotation=(" + rotationX + "," + rotationY + "," + rotationZ + ")" +
                    ", pivot=(" + pivotX + "," + pivotY + "," + pivotZ + ")" +
                    ", overrideInflate=" + overrideInflate +
                    ", inflate=" + inflate +
                    ", overrideMirror=" + overrideMirror +
                    ", mirror=" + mirror +
                    ", northUV=" + getUV(Direction.NORTH) +
                    ", eastUV=" + getUV(Direction.EAST) +
                    ", southUV=" + getUV(Direction.SOUTH) +
                    ", westUV=" + getUV(Direction.WEST) +
                    ", upUV=" + getUV(Direction.UP) +
                    ", downUV=" + getUV(Direction.DOWN) +
                    '}';
        }

        public static class Deserializer implements JsonDeserializer<Cube>
        {
            @Override
            public Cube deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
            {
                JsonObject cubeJson = json.getAsJsonObject();
                float[] origin = JSONTupleParser.getFloat(cubeJson, "origin", 3, () -> new float[3]);
                float[] size = JSONTupleParser.getFloat(cubeJson, "size", 3, () -> new float[3]);
                float[] rotation = JSONTupleParser.getFloat(cubeJson, "rotation", 3, () -> new float[3]);
                float[] pivot = JSONTupleParser.getFloat(cubeJson, "pivot", 3, () -> new float[]{origin[0] + size[0], origin[1] + size[1], origin[2] + size[2]});
                boolean overrideInflate = cubeJson.has("inflate");
                float inflate = JSONUtils.getFloat(cubeJson, "inflate", 0);
                boolean overrideMirror = cubeJson.has("mirror");
                boolean mirror = JSONUtils.getBoolean(cubeJson, "mirror", false);
                CubeUV[] uv = parseUV(cubeJson, size);
                if (uv.length != Direction.values().length)
                    throw new JsonParseException("Expected uv to be of size " + Direction.values().length + ", was " + uv.length);
                return new Cube(origin[0], origin[1], origin[2], size[0], size[1], size[2], rotation[0], rotation[1], rotation[2], pivot[0], pivot[1], pivot[2], overrideInflate, inflate, overrideMirror, mirror, uv);
            }

            private static CubeUV[] parseUV(JsonObject cubeJson, float[] size)
            {
                if (!cubeJson.has("uv"))
                    return new CubeUV[6];

                if (cubeJson.get("uv").isJsonArray())
                {
                    CubeUV[] uvs = new CubeUV[6];
                    float[] uv = JSONTupleParser.getFloat(cubeJson, "uv", 2, () -> new float[2]);
                    uvs[Direction.NORTH.getIndex()] = new CubeUV(uv[0] + size[2], uv[1] + size[2], size[0], size[1], "texture");
                    uvs[Direction.EAST.getIndex()] = new CubeUV(uv[0], uv[1] + size[2], size[2], size[1], "texture");
                    uvs[Direction.SOUTH.getIndex()] = new CubeUV(uv[0] + size[0] + size[2] * 2, uv[1] + size[2], size[0], size[1], "texture");
                    uvs[Direction.WEST.getIndex()] = new CubeUV(uv[0] + size[0] + size[2], uv[1] + size[2], size[2], size[1], "texture");
                    uvs[Direction.UP.getIndex()] = new CubeUV(uv[0] + size[2], uv[1], size[0], size[2], "texture");
                    uvs[Direction.DOWN.getIndex()] = new CubeUV(uv[0] + size[0] + size[2], uv[1], size[0], size[2], "texture");
                    return uvs;
                }
                if (cubeJson.get("uv").isJsonObject())
                {
                    JsonObject uvJson = cubeJson.getAsJsonObject("uv");
                    CubeUV[] uvs = new CubeUV[6];
                    for (Direction direction : Direction.values())
                    {
                        if (!uvJson.has(direction.getName2()))
                            continue;

                        JsonObject faceJson = JSONUtils.getJsonObject(uvJson, direction.getName2());
                        float[] uv = JSONTupleParser.getFloat(faceJson, "uv", 2, null);
                        float[] uvSize = JSONTupleParser.getFloat(faceJson, "uv_size", 2, () -> new float[2]);
                        String material = JSONUtils.getString(faceJson, "material_instance", "texture");
                        uvs[direction.getIndex()] = new CubeUV(uv[0], uv[1], uvSize[0], uvSize[1], material);
                    }
                    return uvs;
                }
                throw new JsonSyntaxException("Expected uv to be a JsonArray or JsonObject, was " + JSONUtils.toString(cubeJson.get("uv")));
            }
        }
    }

    /**
     * <p>A single UV for a face on a cube.</p>
     *
     * @author Ocelot
     * @since 1.0.0
     */
    public static class CubeUV
    {
        private final float u;
        private final float v;
        private final float uSize;
        private final float vSize;
        private final String materialInstance;

        public CubeUV(float u, float v, float uSize, float vSize, String materialInstance)
        {
            this.u = u;
            this.v = v;
            this.uSize = uSize;
            this.vSize = vSize;
            this.materialInstance = materialInstance;
        }

        /**
         * @return The u origin for this face
         */
        public float getU()
        {
            return u;
        }

        /**
         * @return The v origin for this face
         */
        public float getV()
        {
            return v;
        }

        /**
         * @return The x size of the texture selection box
         */
        public float getUSize()
        {
            return uSize;
        }

        /**
         * @return The y size of the texture selection box
         */
        public float getVSize()
        {
            return vSize;
        }

        /**
         * @return The material texture to use for this face
         */
        public String getMaterialInstance()
        {
            return materialInstance;
        }

        @Override
        public String toString()
        {
            return "CubeUV{" +
                    "uv=(" + u + "," + v + ")" +
                    ", uvSize=(" + uSize + "," + vSize + ")" +
                    ", materialInstance='" + materialInstance + '\'' +
                    '}';
        }
    }

    /**
     * <p>Polygon mesh information for a single bone.</p>
     *
     * @author Ocelot
     * @since 1.0.0
     */
    public static class PolyMesh
    {
        private final boolean normalizedUvs;
        private final Vector3f[] positions;
        private final Vector3f[] normals;
        private final Vector2f[] uvs;
        private final Poly[] polys;
        private final PolyType polyType;

        public PolyMesh(boolean normalizedUvs, Vector3f[] positions, Vector3f[] normals, Vector2f[] uvs, Poly[] polys, PolyType polyType)
        {
            this.normalizedUvs = normalizedUvs;
            this.positions = positions;
            this.normals = normals;
            this.uvs = uvs;
            this.polys = polys;
            this.polyType = polyType;
        }

        /**
         * @return Whether or not UVs should be read from <code>0-1</code> or <code>0-textureWidth or textureHeight</code>
         */
        public boolean isNormalizedUvs()
        {
            return normalizedUvs;
        }

        /**
         * @return The positions of all vertices
         */
        public Vector3f[] getPositions()
        {
            return positions;
        }

        /**
         * @return The direction values
         */
        public Vector3f[] getNormals()
        {
            return normals;
        }

        /**
         * @return The UV values
         */
        public Vector2f[] getUvs()
        {
            return uvs;
        }

        /**
         * @return The polys in the mesh
         */
        public Poly[] getPolys()
        {
            return polys;
        }

        /**
         * @return The type of polys in this mesh
         */
        public PolyType getPolyType()
        {
            return polyType;
        }

        public static class Deserializer implements JsonDeserializer<PolyMesh>
        {
            @Override
            public PolyMesh deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
            {
                JsonObject jsonObject = json.getAsJsonObject();
                boolean normalizedUvs = JSONUtils.getBoolean(jsonObject, "normalized_uvs", false);
                Vector3f[] positions = parsePositions(jsonObject, "positions", 3, Vector3f[]::new, j -> new Vector3f(j.get(0).getAsFloat(), j.get(1).getAsFloat(), j.get(2).getAsFloat()));
                Vector3f[] normals = parsePositions(jsonObject, "normals", 3, Vector3f[]::new, j -> new Vector3f(j.get(0).getAsFloat(), j.get(1).getAsFloat(), j.get(2).getAsFloat()));
                Vector2f[] uvs = parsePositions(jsonObject, "uvs", 2, Vector2f[]::new, j -> new Vector2f(j.get(0).getAsFloat(), j.get(1).getAsFloat()));

                if (!jsonObject.has("polys"))
                    throw new JsonSyntaxException("Missing polys, expected to find a JsonArray or String");

                JsonElement polysJson = jsonObject.get("polys");
                if (!polysJson.isJsonArray() && !(polysJson.isJsonPrimitive() && polysJson.getAsJsonPrimitive().isString()))
                    throw new JsonSyntaxException("Expected polys to be a JsonArray or String, was " + JSONUtils.toString(polysJson));

                Poly[] polys = polysJson.isJsonArray() ? context.deserialize(polysJson, Poly[].class) : new Poly[0];
                PolyType polyType = polysJson.isJsonPrimitive() ? parseType(polysJson) : parseType(polys);

                for (Poly poly : polys)
                {
                    if (poly.getPositions().length != polyType.getVertices())
                        throw new JsonSyntaxException("Expected positions to be of length " + polyType.getVertices() + ". Was " + poly.getPositions().length);
                    if (poly.getNormals().length != polyType.getVertices())
                        throw new JsonSyntaxException("Expected normals to be of length " + polyType.getVertices() + ". Was " + poly.getPositions().length);
                    if (poly.getUVs().length != polyType.getVertices())
                        throw new JsonSyntaxException("Expected positions to be of length " + polyType.getVertices() + ". Was " + poly.getPositions().length);
                }

                return new PolyMesh(normalizedUvs, positions, normals, uvs, polys, polyType);
            }

            private static PolyType parseType(JsonElement json) throws JsonParseException
            {
                if (!json.isJsonPrimitive())
                    throw new JsonSyntaxException("Expected String, was " + JSONUtils.toString(json));
                for (PolyType polyType : PolyType.values())
                    if (polyType.name.equalsIgnoreCase(json.getAsString()))
                        return polyType;
                throw new JsonSyntaxException("Unsupported poly type: " + json.getAsString() + ". Supported poly types: " + Arrays.toString(Arrays.stream(PolyType.values()).map(PolyType::getName).toArray(String[]::new)));
            }

            // Figure out what kinds of polys are being used based on the length of the polys array
            private static PolyType parseType(Poly[] polys)
            {
                if (polys.length == 0)
                    return PolyType.TRIANGLES;
                return polys[0].getPositions().length == 3 ? PolyType.TRIANGLES : PolyType.QUADS;
            }

            private static <T> T[] parsePositions(JsonObject json, String name, int size, Function<Integer, T[]> arrayGenerator, Function<JsonArray, T> generator) throws JsonParseException
            {
                JsonArray positionsJson = JSONUtils.getJsonArray(json, name, null);
                if (positionsJson == null)
                    return arrayGenerator.apply(0);

                T[] positions = arrayGenerator.apply(positionsJson.size());
                for (int i = 0; i < positionsJson.size(); i++)
                {
                    JsonElement element = positionsJson.get(i);
                    if (!element.isJsonArray())
                        throw new JsonSyntaxException("Expected " + name + " to be a JsonArray, was " + JSONUtils.toString(element));

                    JsonArray array = element.getAsJsonArray();
                    if (array.size() != size)
                        throw new JsonParseException("Expected " + size + " " + name + " values, was " + array.size());

                    positions[i] = generator.apply(array);
                }

                return positions;
            }
        }
    }

    public static class Poly
    {
        private final int[] positions;
        private final int[] normals;
        private final int[] uvs;

        public Poly(int[] positions, int[] normals, int[] uvs)
        {
            this.positions = positions;
            this.normals = normals;
            this.uvs = uvs;
        }

        /**
         * @return The indexes for the first vertex
         */
        public int[] getPositions()
        {
            return positions;
        }

        /**
         * @return The indexes for the second vertex
         */
        public int[] getNormals()
        {
            return normals;
        }

        /**
         * @return The indexes for the third vertex
         */
        public int[] getUVs()
        {
            return uvs;
        }

        public static class Deserializer implements JsonDeserializer<Poly>
        {
            @Override
            public Poly deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
            {
                JsonArray jsonArray = json.getAsJsonArray();
                if (jsonArray.size() != 4)
                    throw new JsonSyntaxException("Expected 4 index values, was " + jsonArray.size());
                return new Poly(parseVertex(jsonArray.get(0)), parseVertex(jsonArray.get(1)), parseVertex(jsonArray.get(2)));
            }

            private static int[] parseVertex(JsonElement element) throws JsonParseException
            {
                if (!element.isJsonArray())
                    throw new JsonSyntaxException("Expected vertex to be a JsonArray, was " + JSONUtils.toString(element));
                JsonArray array = element.getAsJsonArray();
                if (array.size() == 3)
                    return new int[]{array.get(0).getAsInt(), array.get(1).getAsInt(), array.get(2).getAsInt()};
                if (array.size() == 4)
                    return new int[]{array.get(0).getAsInt(), array.get(1).getAsInt(), array.get(2).getAsInt(), array.get(3).getAsInt()};
                throw new JsonParseException("Expected 3 or 4 vertex values, was " + array.size());
            }
        }
    }

    /**
     * <p>The different types of polygons that can be represented by a poly mesh.</p>
     *
     * @author Ocelot
     * @since 1.0.0
     */
    public enum PolyType
    {
        TRIANGLES("tri_list", 3), QUADS("quad_list", 4);

        private final String name;
        private final int vertices;

        PolyType(String name, int vertices)
        {
            this.name = name;
            this.vertices = vertices;
        }

        /**
         * @return The JSON name of this poly type
         */
        public String getName()
        {
            return name;
        }

        /**
         * @return The amount of vertices in this shape
         */
        public int getVertices()
        {
            return vertices;
        }
    }

    /**
     * <p>A single marker position inside a bone.</p>
     *
     * @author Ocelot
     * @since 1.0.0
     * TODO redo when animations are added
     */
    public static class Locator
    {
        private final String identifier;
        private final float x;
        private final float y;
        private final float z;

        public Locator(String identifier, float x, float y, float z)
        {
            this.identifier = identifier;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        /**
         * @return The identifying name of this locator
         */
        public String getIdentifier()
        {
            return identifier;
        }

        /**
         * @return The position of this locator in the x-axis
         */
        public float getX()
        {
            return x;
        }

        /**
         * @return The position of this locator in the y-axis
         */
        public float getY()
        {
            return y;
        }

        /**
         * @return The position of this locator in the z-axis
         */
        public float getZ()
        {
            return z;
        }

        @Override
        public String toString()
        {
            return "Locator{" +
                    "identifier='" + identifier + '\'' +
                    ", position=(" + x + y + z + ")" +
                    '}';
        }
    }
}
