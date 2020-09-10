package io.github.ocelot.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import cpw.mods.modlauncher.api.INameMappingService;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>A {@link Model} that uses data from {@link GeometryModelData}.</p>
 *
 * @author Ocelot
 * @since 1.0.0
 */
public class BedrockGeometryModel extends Model implements GeometryModel
{
    public static final String ALL = "all";

    private final Map<String, ModelRenderer> modelParts;
    private final String[] modelKeys;
    private final String[] textureKeys;

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
        this.textureKeys = Arrays.stream(bones).map(GeometryModelData.Bone::getTexture).distinct().toArray(String[]::new);

        if (bones.length == 0)
        {
            this.modelKeys = new String[0];
            return;
        }

        Map<String, Pair<GeometryModelData.Bone, ModelRenderer>> boneLookup = Arrays.stream(bones).map(bone -> Pair.of(bone, bone.createModelRenderer(this))).collect(Collectors.toMap(pair -> pair.getKey().getName(), pair -> pair));
        Map<GeometryModelData.Bone, String> parts = new HashMap<>();
        List<String> unprocessedBones = Arrays.stream(bones).map(GeometryModelData.Bone::getName).collect(Collectors.toList());

        while (!unprocessedBones.isEmpty())
        {
            Pair<GeometryModelData.Bone, ModelRenderer> pair = boneLookup.get(unprocessedBones.remove(0));
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

        for (Pair<GeometryModelData.Bone, ModelRenderer> pair : boneLookup.values())
        {
            GeometryModelData.Bone currentBone = pair.getLeft();
            if (currentBone.getParent() != null && !currentBone.getParent().startsWith("parent."))
                continue;

            ModelRenderer parentRenderer = pair.getRight();
            applyChildRotations(parentRenderer, 0, 0, 0);
            parentRenderer.rotationPointX = 0;
            parentRenderer.rotationPointY = 0;
            parentRenderer.rotationPointZ = 0;
            this.modelParts.computeIfAbsent(getPart(parts.getOrDefault(currentBone, ALL), currentBone.getTexture()), key -> new ModelRenderer(this)).addChild(parentRenderer);
        }

        this.modelKeys = parts.values().toArray(new String[0]);
    }

    private static void applyChildRotations(ModelRenderer parent, float xOffset, float yOffset, float zOffset)
    {
        ObjectList<ModelRenderer> childModels = ObfuscationReflectionHelper.getPrivateValue(ModelRenderer.class, parent, "field_78805_m");
        if (childModels == null || childModels.isEmpty())
            return;

        for (ModelRenderer modelRenderer : childModels)
        {
            modelRenderer.rotationPointX -= parent.rotationPointX + xOffset;
            modelRenderer.rotationPointY -= parent.rotationPointY + yOffset;
            modelRenderer.rotationPointZ -= parent.rotationPointZ + zOffset;
            applyChildRotations(modelRenderer, xOffset + parent.rotationPointX, yOffset + parent.rotationPointY, zOffset + parent.rotationPointZ);
        }
    }

    private static String getPart(@Nullable String part, @Nullable String texture)
    {
        return (texture != null ? texture + "_" : "") + (part != null ? part : ALL).toLowerCase(Locale.ROOT);
    }

    @Override
    public void render(MatrixStack matrixStack, IVertexBuilder builder, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
    {
    }

    @Override
    public void render(@Nullable String part, @Nullable String textureKey, MatrixStack matrixStack, IVertexBuilder builder, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
    {
        ModelRenderer renderer = this.modelParts.get(getPart(part, textureKey));
        if (renderer != null)
        {
            renderer.render(matrixStack, builder, packedLight, packedOverlay, red, green, blue, alpha);
        }
    }

    @Override
    public void copyAngles(@Nullable String part, @Nullable String textureKey, ModelRenderer limbRenderer)
    {
        ModelRenderer renderer = this.modelParts.get(getPart(part, textureKey));
        if (renderer != null)
        {
            renderer.copyModelAngles(limbRenderer);
        }
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
}
