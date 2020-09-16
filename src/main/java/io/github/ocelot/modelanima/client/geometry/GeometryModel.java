package io.github.ocelot.modelanima.client.geometry;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import io.github.ocelot.modelanima.client.GeometryModelManager;
import io.github.ocelot.modelanima.common.geometry.GeometryModelData;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

/**
 * <p>An abstract renderer for geometry models that can be queried by {@link GeometryModelManager}.</p>
 *
 * @author Ocelot
 * @since 1.0.0
 */
public interface GeometryModel
{
    /**
     * Renders a specific part with the specified texture key.
     *
     * @param part          The parent model part being rendered or null to render all
     * @param textureKey    The key of the texture to use for that part
     * @param matrixStack   The current stack of transformations
     * @param builder       The builder to put the data into
     * @param packedLight   The packed uv into the light texture the parts should be rendered at
     * @param packedOverlay The packed uv into the overlay texture the parts should be rendered at
     * @param red           The red factor for color
     * @param green         The green factor for color
     * @param blue          The blue factor for color
     * @param alpha         The alpha factor for color
     */
    void render(@Nullable String part, @Nullable String textureKey, MatrixStack matrixStack, IVertexBuilder builder, int packedLight, int packedOverlay, float red, float green, float blue, float alpha);

    /**
     * Copies the model angles to the currently selected part.
     *
     * @param part         The parent model part being rendered or null to render all
     * @param textureKey   The key of the texture to use for that part
     * @param limbRenderer The renderer to copy angles of
     */
    void copyAngles(@Nullable String part, @Nullable String textureKey, ModelRenderer limbRenderer);

    /**
     * Fetches a specific locator by name.
     *
     * @param name The name of the locator to fetch
     * @return The locator by that name or <code>null</code> if there is no locator by that name
     */
    @Nullable
    GeometryModelData.Locator getLocator(String name);

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
    RenderType getRenderType(ResourceLocation location);
}
