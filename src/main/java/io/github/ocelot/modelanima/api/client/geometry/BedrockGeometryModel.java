package io.github.ocelot.modelanima.api.client.geometry;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import io.github.ocelot.modelanima.api.common.animation.AnimationData;
import io.github.ocelot.modelanima.api.common.geometry.GeometryModelData;
import io.github.ocelot.modelanima.api.common.geometry.texture.GeometryModelTexture;
import io.github.ocelot.modelanima.core.client.geometry.BoneModelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>A {@link Model} that uses data from {@link GeometryModelData}.</p>
 *
 * @author Ocelot
 * @since 1.0.0
 */
public class BedrockGeometryModel extends Model implements GeometryModel, AnimatedModel
{
    private static final Vector3f POSITION = new Vector3f();
    private static final Vector3f ROTATION = new Vector3f();
    private static final Vector3f SCALE = new Vector3f();

    private final Map<String, BoneModelRenderer> modelParts;
    private final String[] modelKeys;
    private final String[] textureKeys;
    private String activeMaterial;

    public BedrockGeometryModel(GeometryModelData data)
    {
        this(data.getDescription().getTextureWidth(), data.getDescription().getTextureHeight(), data.getBones());
    }

    public BedrockGeometryModel(int textureWidth, int textureHeight, GeometryModelData.Bone[] bones)
    {
        super(RenderType::entityCutoutNoCull);
        this.texWidth = textureWidth;
        this.texHeight = textureHeight;
        this.modelParts = new HashMap<>();

        Set<String> textures = new HashSet<>();
        for (GeometryModelData.Bone bone : bones)
        {
            for (GeometryModelData.Cube cube : bone.getCubes())
            {
                for (Direction direction : Direction.values())
                {
                    GeometryModelData.CubeUV uv = cube.getUV(direction);
                    if (uv == null)
                        continue;
                    textures.add(uv.getMaterialInstance());
                }
            }
            if (bone.getPolyMesh() != null)
                textures.add("poly_mesh.texture");
        }
        this.textureKeys = textures.toArray(new String[0]);

        if (bones.length == 0)
        {
            this.modelKeys = new String[0];
            return;
        }

        Map<String, Pair<GeometryModelData.Bone, BoneModelRenderer>> boneLookup = Arrays.stream(bones).map(bone -> Pair.of(bone, new BoneModelRenderer(this, bone))).collect(Collectors.toMap(pair -> pair.getKey().getName(), pair -> pair));
        Map<GeometryModelData.Bone, String> parts = new HashMap<>();
        List<String> unprocessedBones = Arrays.stream(bones).map(GeometryModelData.Bone::getName).collect(Collectors.toList());

        while (!unprocessedBones.isEmpty())
        {
            Pair<GeometryModelData.Bone, BoneModelRenderer> pair = boneLookup.get(unprocessedBones.remove(0));
            GeometryModelData.Bone currentBone = pair.getLeft();
            String parent = currentBone.getParent();

            if (parent != null)
            {
                if (parent.startsWith("parent."))
                {
                    parts.put(currentBone, parent.substring("parent.".length()));
                }
                else
                {
                    if (!boneLookup.containsKey(parent))
                        throw new IllegalStateException("Unknown bone '" + parent + "'");

                    ModelRenderer parentRenderer = boneLookup.get(parent).getRight();
                    parentRenderer.addChild(pair.getRight());
                }
            }

            unprocessedBones.remove(currentBone.getName());
        }

        for (Pair<GeometryModelData.Bone, BoneModelRenderer> pair : boneLookup.values())
        {
            GeometryModelData.Bone currentBone = pair.getLeft();
            if (!parts.isEmpty() && currentBone.getParent() != null && !currentBone.getParent().startsWith("parent."))
                continue;

            this.modelParts.put(currentBone.getName(), pair.getRight());
        }

        this.modelKeys = parts.values().toArray(new String[0]);
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder builder, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
    {
    }

    @Override
    public void render(String material, GeometryModelTexture texture, MatrixStack matrixStack, IVertexBuilder builder, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
    {
        this.activeMaterial = material;
        this.modelParts.values().forEach(renderer ->
        {
            String parent = renderer.getBone().getParent();
            if (parent != null && !parent.startsWith("parent."))
                return;
            renderer.render(matrixStack, builder, packedLight, packedOverlay, red, green, blue, alpha);
        });
        this.activeMaterial = "texture";
    }

    @Override
    public void resetTransformation()
    {
        this.modelParts.values().forEach(renderer -> renderer.resetTransform(true));
    }

    @Override
    public void copyAngles(@Nullable String parent, ModelRenderer limbRenderer)
    {
        this.modelParts.values().stream().filter(part -> Objects.equals(part.getBone().getParent(), parent)).forEach(renderer -> renderer.copyFrom(limbRenderer));
    }

    @Override
    public Optional<ModelRenderer> getModelRenderer(String part)
    {
        return Optional.ofNullable(this.modelParts.get(part));
    }

    @Override
    public ModelRenderer[] getChildRenderers(String part)
    {
        return this.modelParts.values().stream().filter(boneModelRenderer -> part.equals(boneModelRenderer.getBone().getParent())).toArray(ModelRenderer[]::new);
    }

    @Override
    public ModelRenderer[] getModelRenderers()
    {
        return this.modelParts.values().toArray(new BoneModelRenderer[0]);
    }

    @Override
    public String[] getParentModelKeys()
    {
        return modelKeys;
    }

    @Override
    public String[] getMaterialKeys()
    {
        return textureKeys;
    }

    @Override
    public float getTextureWidth()
    {
        return texWidth;
    }

    @Override
    public float getTextureHeight()
    {
        return texHeight;
    }

    @Override
    public void applyAnimation(float animationTime, AnimationData animation)
    {
        if (animationTime > animation.getAnimationLength())
        {
            switch (animation.getLoop())
            {
                case NONE:
                    animationTime = 0;
                    break;
                case LOOP:
                    animationTime %= animation.getAnimationLength();
                    break;
                case HOLD_ON_LAST_FRAME:
                    animationTime = animation.getAnimationLength();
                    break;
            }
        }

        for (AnimationData.BoneAnimation boneAnimation : animation.getBoneAnimations())
        {
            if (!this.modelParts.containsKey(boneAnimation.getName()))
                continue;

            POSITION.set(0, 0, 0);
            ROTATION.set(0, 0, 0);
            SCALE.set(1, 1, 1);
            get(animationTime, boneAnimation.getPositionFrames(), POSITION);
            get(animationTime, boneAnimation.getRotationFrames(), ROTATION);
            get(animationTime, boneAnimation.getScaleFrames(), SCALE);

            this.modelParts.get(boneAnimation.getName()).applyAnimationAngles(POSITION.x(), POSITION.y(), POSITION.z(), ROTATION.x(), ROTATION.y(), ROTATION.z(), SCALE.x(), SCALE.y(), SCALE.z());
        }
    }

    @Override
    public GeometryModelData.Locator[] getLocators(String part)
    {
        return this.getModelRenderer(part).map(modelRenderer ->
        {
            if (!(modelRenderer instanceof BoneModelRenderer))
                return new GeometryModelData.Locator[0];
            return ((BoneModelRenderer) modelRenderer).getBone().getLocators();
        }).orElseGet(() -> new GeometryModelData.Locator[0]);
    }

    public String getActiveMaterial()
    {
        return activeMaterial;
    }

    private static void get(float animationTime, AnimationData.KeyFrame[] frames, Vector3f vector)
    {
        for (int i = 0; i < frames.length; i++)
        {
            AnimationData.KeyFrame to = frames[i];
            if (to.getTime() == 0 || (animationTime > to.getTime() && i < frames.length - 1))
                continue;

            AnimationData.KeyFrame from = i == 0 ? null : frames[i - 1];
            float progress = (from == null ? animationTime / to.getTime() : Math.min(1.0F, (animationTime - from.getTime()) / (to.getTime() - from.getTime())));
            float fromX = from == null ? vector.x() : from.getTransformPostX();
            float fromY = from == null ? vector.y() : from.getTransformPostY();
            float fromZ = from == null ? vector.z() : from.getTransformPostZ();

            float x = MathHelper.lerp(progress, fromX, to.getTransformPreX());
            float y = MathHelper.lerp(progress, fromY, to.getTransformPreY());
            float z = MathHelper.lerp(progress, fromZ, to.getTransformPreZ());
            vector.set(x, y, z);
            break;
        }
    }
}
