package io.github.ocelot.modelanima.api.client.geometry;

import com.mojang.blaze3d.matrix.MatrixStack;
import cpw.mods.modlauncher.api.INameMappingService;
import io.github.ocelot.modelanima.ModelAnima;
import io.github.ocelot.modelanima.api.client.texture.GeometryTextureSpriteUploader;
import io.github.ocelot.modelanima.api.common.geometry.texture.GeometryModelTexture;
import io.github.ocelot.modelanima.api.common.geometry.texture.GeometryModelTextureTable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
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
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GeometryModelRenderer
{
    public static final ResourceLocation ATLAS_LOCATION = new ResourceLocation(ModelAnima.DOMAIN, "textures/atlas/geometry.png");
    private static final Map<Model, Map<String, ModelRenderer>> CACHE = new HashMap<>();
    private static GeometryTextureSpriteUploader spriteUploader;

    @SubscribeEvent
    public static void onEvent(ColorHandlerEvent.Block event)
    {
        Minecraft minecraft = Minecraft.getInstance();
        GeometryTextureSpriteUploader spriteUploader = new GeometryTextureSpriteUploader(minecraft.textureManager, ATLAS_LOCATION);
        IResourceManager resourceManager = minecraft.getResourceManager();
        if (resourceManager instanceof IReloadableResourceManager)
        {
            ((IReloadableResourceManager) resourceManager).addReloadListener(spriteUploader);
        }
        GeometryModelRenderer.spriteUploader = spriteUploader;
    }

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
     * @deprecated TODO rewrite
     */
    public static void render(@Nullable Model parent, GeometryModel model, @Nullable GeometryModelTextureTable textures, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
    {
        // TODO use an atlas instead of unique textures

//        Map<String, ModelRenderer> parentParts = parent == null ? Collections.emptyMap() : CACHE.computeIfAbsent(parent, key -> mapRenderers(parent));
//        for (String textureKey : model.getTextureKeys())
//        {
//            GeometryModelTexture texture = textures == null ? GeometryModelTexture.MISSING : textures.getTexture(textureKey);
//            model.render(null, textureKey, matrixStack, buffer.getBuffer(model.getModelRenderType(texture.getLocation())), texture.isGlowing() ? 15728880 : packedLight, packedOverlay, red * texture.getRed(), green * texture.getGreen(), blue * texture.getBlue(), alpha);
//            for (String modelKey : model.getModelKeys())
//            {
//                String deobfName = ObfuscationReflectionHelper.remapName(INameMappingService.Domain.FIELD, modelKey);
//                if (parentParts.containsKey(deobfName))
//                    model.copyAngles(modelKey, textureKey, parentParts.get(deobfName));
//                model.render(modelKey, textureKey, matrixStack, buffer.getBuffer(model.getModelRenderType(texture.getLocation())), texture.isGlowing() ? 15728880 : packedLight, packedOverlay, red * texture.getRed(), green * texture.getGreen(), blue * texture.getBlue(), alpha);
//            }
//        }

        Map<String, ModelRenderer> parentParts = parent == null ? Collections.emptyMap() : CACHE.computeIfAbsent(parent, key -> mapRenderers(parent));
        GeometryModelTexture texture = GeometryModelTexture.MISSING;
        model.render(null, matrixStack, buffer.getBuffer(model.getModelRenderType(ATLAS_LOCATION)), texture.isGlowing() ? 15728880 : packedLight, packedOverlay, red * texture.getRed(), green * texture.getGreen(), blue * texture.getBlue(), alpha);
        for (String modelKey : model.getModelKeys())
        {
            String deobfName = ObfuscationReflectionHelper.remapName(INameMappingService.Domain.FIELD, modelKey);
            if (parentParts.containsKey(deobfName))
                model.copyAngles(modelKey, parentParts.get(deobfName));
            model.render(modelKey, matrixStack, buffer.getBuffer(model.getModelRenderType(ATLAS_LOCATION)), texture.isGlowing() ? 15728880 : packedLight, packedOverlay, red * texture.getRed(), green * texture.getGreen(), blue * texture.getBlue(), alpha);
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
