package io.github.ocelot.modelanima.api.client.geometry;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import io.github.ocelot.modelanima.api.common.geometry.GeometryModelData;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * <p>A completely empty {@link GeometryModel}.</p>
 *
 * @author Ocelot
 * @since 1.0.0
 */
public class EmptyGeometryModel implements GeometryModel
{
    @Override
    public void render(@Nullable String part, MatrixStack matrixStack, IVertexBuilder builder, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
    {
    }

    @Override
    public void copyAngles(@Nullable String part, ModelRenderer modelRenderer)
    {
    }

    @Override
    public Optional<ModelRenderer> getModelRenderer(String part)
    {
        return Optional.empty();
    }

    @Override
    public ModelRenderer[] getModelRenderers()
    {
        return new ModelRenderer[0];
    }

    @Override
    public Optional<GeometryModelData.Locator> getLocator(String name)
    {
        return Optional.empty();
    }

    @Override
    public GeometryModelData.Locator[] getLocators()
    {
        return new GeometryModelData.Locator[0];
    }

    @Override
    public String[] getModelKeys()
    {
        return new String[0];
    }

    @Override
    public String[] getTextureKeys()
    {
        return new String[0];
    }

    @Override
    public RenderType getModelRenderType(ResourceLocation location)
    {
        return RenderType.getEntitySolid(location);
    }
}
