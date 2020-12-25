package io.github.ocelot.modelanima.api.client.geometry;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import io.github.ocelot.modelanima.api.client.texture.GeometryAtlasTexture;
import io.github.ocelot.modelanima.api.common.geometry.GeometryModelData;
import io.github.ocelot.modelanima.api.common.geometry.texture.GeometryModelTexture;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ModelRenderer;

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
     * @param material      The name of the material being rendered
     * @param texture       The texture currently being rendered
     * @param matrixStack   The current stack of transformations
     * @param builder       The builder to put the data into
     * @param packedLight   The packed uv into the light texture the parts should be rendered at
     * @param packedOverlay The packed uv into the overlay texture the parts should be rendered at
     * @param red           The red factor for color
     * @param green         The green factor for color
     * @param blue          The blue factor for color
     * @param alpha         The alpha factor for color
     */
    void render(String material, GeometryModelTexture texture, MatrixStack matrixStack, IVertexBuilder builder, int packedLight, int packedOverlay, float red, float green, float blue, float alpha);

    /**
     * Resets all model angles to the default transformation.
     */
    void resetTransformation();

    /**
     * Copies the model angles to the currently selected part.
     *
     * @param parent        The parent model part being copied
     * @param modelRenderer The renderer to copy angles of
     */
    void copyAngles(@Nullable String parent, ModelRenderer modelRenderer);

    /**
     * Fetches a model renderer from the model.
     *
     * @param part The name of the renderer to get
     * @return An optional of the model renderer by that name
     */
    Optional<ModelRenderer> getModelRenderer(String part);

    /**
     * Fetches all model parts that are a child to the provided parent part.
     *
     * @param part The name of the part to get children from
     * @return All model renderers copying angles from that part
     */
    ModelRenderer[] getChildRenderers(String part);

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
    String[] getParentModelKeys();

    /**
     * @return An array of all used material keys
     */
    String[] getMaterialKeys();

    /**
     * @return The width of the texture in pixels
     */
    float getTextureWidth();

    /**
     * @return The height of the texture in pixels
     */
    float getTextureHeight();

    /**
     * Fetches an {@link IVertexBuilder} for the specified texture.
     *
     * @param buffer  The render type buffers
     * @param atlas   The atlas to get the textures from
     * @param texture The texture to use
     * @return The buffer that should be used for the provided texture
     */
    default IVertexBuilder getBuffer(IRenderTypeBuffer buffer, GeometryAtlasTexture atlas, GeometryModelTexture texture)
    {
        return atlas.getSprite(texture.getLocation()).wrapBuffer(buffer.getBuffer(texture.getLayer().getRenderType(atlas.getAtlasLocation())));
    }

    default IVertexBuilder getBuffer(IRenderTypeBuffer buffer, GeometryAtlasTexture atlas, GeometryModelTexture texture, GeometryModelData.PolyMesh polyMesh){
        return atlas.getSprite(texture.getLocation()).wrapBuffer(buffer.getBuffer(texture.getLayer().getRenderType(atlas.getAtlasLocation())));
    }
}
