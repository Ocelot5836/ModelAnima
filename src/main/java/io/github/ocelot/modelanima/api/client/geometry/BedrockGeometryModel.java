package io.github.ocelot.modelanima.api.client.geometry;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import io.github.ocelot.modelanima.api.common.geometry.GeometryModelData;
import io.github.ocelot.modelanima.api.common.geometry.texture.GeometryModelTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>A {@link Model} that uses data from {@link GeometryModelData}.</p>
 *
 * @author Ocelot
 * @since 1.0.0
 */
public class BedrockGeometryModel extends Model implements GeometryModel
{
    private final Map<String, BoneModelRenderer> modelParts;
    private final Map<String, GeometryModelData.Locator> locators;
    private final String[] modelKeys;
    private final String[] textureKeys;
    private String activeMaterial;

    public BedrockGeometryModel(Function<ResourceLocation, RenderType> renderType, GeometryModelData data)
    {
        this(renderType, data.getDescription().getTextureWidth(), data.getDescription().getTextureHeight(), data.getBones());
    }

    public BedrockGeometryModel(Function<ResourceLocation, RenderType> renderType, int textureWidth, int textureHeight, GeometryModelData.Bone[] bones)
    {
        super(renderType);
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.modelParts = new HashMap<>();
        this.locators = Stream.of(Arrays.stream(bones).map(GeometryModelData.Bone::getLocators).toArray(GeometryModelData.Locator[][]::new)).flatMap(Stream::of).collect(Collectors.toMap(GeometryModelData.Locator::getIdentifier, locator -> locator));

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
    public void render(MatrixStack matrixStack, IVertexBuilder builder, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
    {
    }

    @Override
    public void render(String material, GeometryModelTexture texture, MatrixStack matrixStack, IVertexBuilder builder, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
    {
        this.activeMaterial = material;
        this.modelParts.values().forEach(renderer -> renderer.render(matrixStack, builder, packedLight, packedOverlay, red, green, blue, alpha));
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
        this.modelParts.values().stream().filter(part -> Objects.equals(part.getParent(), parent)).forEach(renderer -> renderer.copyModelAngles(limbRenderer));
    }

    @Override
    public Optional<ModelRenderer> getModelRenderer(String part)
    {
        return Optional.ofNullable(this.modelParts.get(part));
    }

    @Override
    public Optional<GeometryModelData.Locator> getLocator(String name)
    {
        return Optional.ofNullable(this.locators.get(name));
    }

    @Override
    public ModelRenderer[] getModelRenderers()
    {
        return this.modelParts.values().toArray(new BoneModelRenderer[0]);
    }

    @Override
    public GeometryModelData.Locator[] getLocators()
    {
        return this.locators.values().toArray(new GeometryModelData.Locator[0]);
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
        return textureWidth;
    }

    @Override
    public float getTextureHeight()
    {
        return textureHeight;
    }

    public String getActiveMaterial()
    {
        return activeMaterial;
    }
}
