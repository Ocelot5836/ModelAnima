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
 * <p>An abstract renderer for geometry models that can be queried by {@link LocalGeometryModelLoader}.</p>
 *
 * @author Ocelot
 * @since 1.0.0
 */
public interface GeometryModel
{
    /**
     * A blank model that can be used for empty entries.
     */
    GeometryModel EMPTY = new EmptyGeometryModel();

    /**
     * Renders a specific part with the specified texture key.
     *
     * @param part          The parent model part being rendered or <code>null</code> to render all
     * @param matrixStack   The current stack of transformations
     * @param builder       The builder to put the data into
     * @param packedLight   The packed uv into the light texture the parts should be rendered at
     * @param packedOverlay The packed uv into the overlay texture the parts should be rendered at
     * @param red           The red factor for color
     * @param green         The green factor for color
     * @param blue          The blue factor for color
     * @param alpha         The alpha factor for color
     */
    void render(@Nullable String part, MatrixStack matrixStack, IVertexBuilder builder, int packedLight, int packedOverlay, float red, float green, float blue, float alpha);

    /**
     * Copies the model angles to the currently selected part.
     *
     * @param part         The parent model part being rendered or <code>null</code> to render all
     * @param modelRenderer The renderer to copy angles of
     */
    void copyAngles(@Nullable String part, ModelRenderer modelRenderer);

    /**
     * Fetches a model renderer from the model.
     *
     * @param part The name of the renderer to get
     * @return An optional of the model renderer by that name
     */
    Optional<ModelRenderer> getModelRenderer(String part);

    /**
     * Fetches a specific locator by name.
     *
     * @param name The name of the locator to fetch
     * @return An optional of the locator by that name
     */
    Optional<GeometryModelData.Locator> getLocator(String name);

    /**
     * @return An array of all model renderers
     */
    ModelRenderer[] getModelRenderers();

    /**
     * @return All locators in the model
     */
    GeometryModelData.Locator[] getLocators();

    /**
     * @return An array of all used part keys
     */
    String[] getModelKeys();

    /**
     * @return An array of all used texture keys
     */
    String[] getTextureKeys();

    /**
     * Fetches a render type for the specified texture location.
     *
     * @param location The texture to use
     * @return A render type using that texture
     */
    RenderType getModelRenderType(ResourceLocation location);
}
