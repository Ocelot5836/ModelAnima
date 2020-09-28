package io.github.ocelot.modelanima.client.geometry;

import com.mojang.blaze3d.matrix.MatrixStack;
import cpw.mods.modlauncher.api.INameMappingService;
import io.github.ocelot.modelanima.client.geometry.GeometryModel;
import io.github.ocelot.modelanima.common.geometry.texture.GeometryModelTexture;
import io.github.ocelot.modelanima.common.geometry.texture.GeometryModelTextureTable;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Collections;
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
    private static final Map<Model, Map<String, ModelRenderer>> CACHE = new HashMap<>();

    /**
     * Renders the specified model on the specified parent model.
     *
     * @param parent        The parent model to attach parts to or <code>null</code>
     * @param model         The model to render
     * @param textures      The textures to apply to the model or <code>null</code> to use a missing texture
     * @param matrixStack   The current stack of transformations
     * @param buffer        The buffer to get the builder from
     * @param packedLight   The packed uv into the light texture the parts should be rendered at
     * @param packedOverlay The packed uv into the overlay texture the parts should be rendered at
     * @param red           The red factor for color
     * @param green         The green factor for color
     * @param blue          The blue factor for color
     * @param alpha         The alpha factor for color
     */
    public static void render(@Nullable Model parent, GeometryModel model, @Nullable GeometryModelTextureTable textures, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
    {
        Map<String, ModelRenderer> parentParts = parent == null ? Collections.emptyMap() : CACHE.computeIfAbsent(parent, key -> mapRenderers(parent));
        for (String textureKey : model.getTextureKeys())
        {
            GeometryModelTexture texture = textures == null ? GeometryModelTexture.MISSING : textures.getTexture(textureKey);
            model.render(null, textureKey, matrixStack, buffer.getBuffer(model.getModelRenderType(texture.getLocation())), packedLight, packedOverlay, red * GeometryModelTexture.MISSING.getRed(), green * GeometryModelTexture.MISSING.getGreen(), blue * GeometryModelTexture.MISSING.getBlue(), alpha);
            for (String modelKey : model.getModelKeys())
            {
                String deobfName = ObfuscationReflectionHelper.remapName(INameMappingService.Domain.FIELD, modelKey);
                if (parentParts.containsKey(deobfName))
                    model.copyAngles(modelKey, textureKey, parentParts.get(deobfName));
                model.render(modelKey, textureKey, matrixStack, buffer.getBuffer(model.getModelRenderType(texture.getLocation())), packedLight, packedOverlay, red * texture.getRed(), green * texture.getGreen(), blue * texture.getBlue(), alpha);
            }
        }
    }

    private static Map<String, ModelRenderer> mapRenderers(Model model)
    {
        Map<String, ModelRenderer> renderers = new HashMap<>();
        Field[] fields = model.getClass().getFields();
        for (Field field : fields)
        {
            if (ModelRenderer.class.isAssignableFrom(field.getType()))
            {
                try
                {
                    renderers.put(field.getName(), (ModelRenderer) field.get(model));
                }
                catch (Exception ignored)
                {
                }
            }
        }
        return renderers;
    }
}
