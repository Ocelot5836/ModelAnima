package io.github.ocelot.modelanima.api.client.geometry;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import cpw.mods.modlauncher.api.INameMappingService;
import io.github.ocelot.modelanima.api.common.geometry.GeometryModelData;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
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
    public static final String ALL = "all";

    private final Map<String, BoneModelRenderer> modelParts;
    private final Map<String, GeometryModelData.Locator> locators;
    private final String[] modelKeys;
    private final String[] textureKeys;

    public BedrockGeometryModel(Function<ResourceLocation, RenderType> renderType, GeometryModelData data)
    {
        this(renderType, data.getDescription().getTextureWidth(), data.getDescription().getTextureHeight(), data.getBones());
    }

    // FIXME rewrite this
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
                    parts.put(currentBone, ObfuscationReflectionHelper.remapName(INameMappingService.Domain.FIELD, parent.substring("parent.".length())));
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
            if (currentBone.getParent() != null && !currentBone.getParent().startsWith("parent."))
                continue;

            BoneModelRenderer parentRenderer = pair.getRight();
//            applyChildRotations(parentRenderer, 0, 0, 0);
//            parentRenderer.rotationPointX = 0;
//            parentRenderer.rotationPointY = 0;
//            parentRenderer.rotationPointZ = 0;
            this.modelParts.put(currentBone.getName(), parentRenderer);
        }

        this.modelKeys = parts.values().toArray(new String[0]);
    }

    private static void applyChildRotations(ModelRenderer parent, float xOffset, float yOffset, float zOffset)
    {
        ObjectList<ModelRenderer> childModels = ObfuscationReflectionHelper.getPrivateValue(ModelRenderer.class, parent, "field_78805_m");
        if (childModels == null || childModels.isEmpty())
            return;

        for (ModelRenderer child : childModels)
        {
            if (parent instanceof BoneModelRenderer && child instanceof BoneModelRenderer)
            {
//                BoneModelRenderer parentRenderer = (BoneModelRenderer) parent;
//                BoneModelRenderer childRenderer = (BoneModelRenderer) child;
//                childRenderer.setParentOffset(parentRenderer.getBone().getPivotX() + parentRenderer.getParentX(), parentRenderer.getBone().getPivotY() + parentRenderer.getParentY(), parentRenderer.getBone().getPivotZ() + parentRenderer.getParentZ());
//                applyChildRotations(child, childRenderer.getParentX(), childRenderer.getParentY(), childRenderer.getParentZ());
            }
            else
            {
                child.rotationPointX -= parent.rotationPointX + xOffset;
                child.rotationPointY -= parent.rotationPointY + yOffset;
                child.rotationPointZ -= parent.rotationPointZ + zOffset;
                applyChildRotations(child, parent.rotationPointX + xOffset, parent.rotationPointY + yOffset, parent.rotationPointZ + zOffset);
            }
        }
    }

    private static String getPart(@Nullable String part, @Nullable String texture)
    {
        return (texture != null ? texture : "texture") + "_" + (part != null ? part : ALL).toLowerCase(Locale.ROOT);
    }

    @Override
    public void render(MatrixStack matrixStack, IVertexBuilder builder, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
    {
        this.render(null, matrixStack, builder, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public void render(@Nullable String part, MatrixStack matrixStack, IVertexBuilder builder, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
    {
        if (part == null)
        {
            this.modelParts.values().forEach(renderer -> renderer.render(matrixStack, builder, packedLight, packedOverlay, red, green, blue, alpha));
        }
        else
        {
            this.getModelRenderer(part).ifPresent(renderer -> renderer.render(matrixStack, builder, packedLight, packedOverlay, red, green, blue, alpha));
        }
    }

    @Override
    public void copyAngles(@Nullable String part, ModelRenderer limbRenderer)
    {
        if (part == null)
        {
            this.modelParts.values().forEach(renderer -> renderer.copyModelAngles(limbRenderer));
        }
        else
        {
            this.getModelRenderer(part).ifPresent(renderer -> renderer.copyModelAngles(limbRenderer));
        }
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
    public String[] getModelKeys()
    {
        return modelKeys;
    }

    @Override
    public String[] getTextureKeys()
    {
        return textureKeys;
    }

    @Override
    public RenderType getModelRenderType(ResourceLocation location)
    {
        return super.getRenderType(location);
    }
}
