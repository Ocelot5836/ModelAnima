package io.github.ocelot.modelanima.api.client.geometry;

import com.mojang.blaze3d.vertex.PoseStack;
import cpw.mods.modlauncher.api.INameMappingService;
import io.github.ocelot.modelanima.api.client.texture.GeometryTextureManager;
import io.github.ocelot.modelanima.api.common.texture.GeometryModelTexture;
import io.github.ocelot.modelanima.api.common.texture.GeometryModelTextureTable;
import io.github.ocelot.modelanima.core.client.geometry.GeometryModelBufferSource;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>Manages the caching of model fields and renders {@link GeometryModel}.</p>
 *
 * @author Ocelot
 * @since 1.0.0
 */
public class GeometryModelRenderer
{
    private static final Map<Model, Map<String, ModelPart>> MODEL_PARTS = new HashMap<>();
    private static final GeometryModelBufferSource SOURCE = new GeometryModelBufferSource();

    /**
     * Copies angles from the parent model to the geometry model.
     *
     * @param parent The parent to copy angles from
     * @param model  The model to apply the angles to
     */
    public static void copyModelAngles(@Nullable Model parent, GeometryModel model)
    {
        if (parent == null)
        {
            model.resetTransformation();
            return;
        }
        Map<String, ModelPart> parentParts = MODEL_PARTS.computeIfAbsent(parent, GeometryModelRenderer::mapRenderers);
        for (String modelKey : model.getParentModelKeys())
        {
            String deobfName = ObfuscationReflectionHelper.remapName(INameMappingService.Domain.FIELD, modelKey);
            if (parentParts.containsKey(deobfName))
                model.copyAngles("parent." + modelKey, parentParts.get(deobfName));
        }
    }

    /**
     * Renders the specified model on the specified parent model.
     *
     * @param model           The model to render
     * @param textureLocation The textures to apply to the model or <code>null</code> to use a missing texture
     * @param matrixStack     The current stack of transformations
     * @param packedLight     The packed uv into the light texture the parts should be rendered at
     * @param packedOverlay   The packed uv into the overlay texture the parts should be rendered at
     * @param red             The red factor for color
     * @param green           The green factor for color
     * @param blue            The blue factor for color
     * @param alpha           The alpha factor for color
     */
    public static void render(GeometryModel model, @Nullable ResourceLocation textureLocation, PoseStack matrixStack, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
    {
        if (GeometryTextureManager.isReloading())
            return;
        GeometryModelTextureTable textures = textureLocation == null ? GeometryModelTextureTable.EMPTY : GeometryTextureManager.getTextures(textureLocation);
        for (String material : model.getMaterialKeys())
        {
            GeometryModelTexture[] layers = textures.getLayerTextures(material);
            for (GeometryModelTexture texture : layers)
            {
                SOURCE.setLayer(texture.getLayer());
                model.render(material, texture, matrixStack, model.getBuffer(SOURCE, GeometryTextureManager.getAtlas(), texture), texture.isGlowing() ? 15728880 : packedLight, packedOverlay, red * texture.getRed(), green * texture.getGreen(), blue * texture.getBlue(), alpha);
            }
        }
        SOURCE.setLayer(GeometryModelTexture.TextureLayer.SOLID);
        SOURCE.endBatch();
    }

    private static Map<String, ModelPart> mapRenderers(Model model)
    {
        Map<String, ModelPart> renderers = new HashMap<>();
        Class<?> i = model.getClass();
        while (i != null && i != Object.class)
        {
            for (Field field : i.getDeclaredFields())
            {
                if (!field.isSynthetic())
                {
                    if (ModelPart.class.isAssignableFrom(field.getType()))
                    {
                        try
                        {
                            field.setAccessible(true);
                            renderers.put(field.getName(), (ModelPart) field.get(model));
                        }
                        catch (Exception ignored)
                        {
                        }
                    }
                }
            }
            i = i.getSuperclass();
        }
        return renderers;
    }
}
