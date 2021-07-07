package io.github.ocelot.modelanima.api.client.geometry;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * <p>A basic implementation of {@link EntityModel} for {@link GeometryModel}.</p>
 *
 * @param <T> The type of entity this model is rendering
 * @author Ocelot
 */
public class GeometryEntityModel<T extends Entity> extends EntityModel<T>
{
    private final Supplier<GeometryModel> model;
    private final Function<T, ResourceLocation> textureTableFunction;
    private final Set<Pair<String, BoneTransformer<T>>> transforms;
    private ResourceLocation texture;

    public GeometryEntityModel(Supplier<GeometryModel> model, Function<T, ResourceLocation> textureTableFunction)
    {
        this.model = model;
        this.textureTableFunction = textureTableFunction;
        this.transforms = new HashSet<>();
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
        this.getModel().resetTransformation();

        this.texture = this.textureTableFunction.apply(entity);
        for (Pair<String, BoneTransformer<T>> transform : this.transforms)
        {
            this.getModel().getModelRenderer(transform.getLeft()).ifPresent(modelRenderer -> transform.getRight().transform(this, modelRenderer, entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch));
        }
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
    {
        GeometryModelRenderer.render(this.getModel(), this.texture, matrixStack, Minecraft.getInstance().renderBuffers().bufferSource(), packedLight, packedOverlay, red, green, blue, alpha);
    }

    /**
     * Adds the specified transformer to the specified bone. If the bone is not present nothing happens.
     *
     * @param boneName    The name of the bone to transform
     * @param transformer The function to transform the bone
     */
    public void addTransform(String boneName, BoneTransformer<T> transformer)
    {
        this.transforms.add(Pair.of(boneName, transformer));
    }

    /**
     * @return The model this model is wrapping
     */
    public GeometryModel getModel()
    {
        return this.model.get();
    }

    /**
     * <p>Transforms a single {@link ModelRenderer} from a {@link GeometryModel}.</p>
     *
     * @param <T> The type of entity this transform is editing
     * @author Ocelot
     * @since 1.0.0
     */
    @FunctionalInterface
    public interface BoneTransformer<T extends Entity>
    {
        /**
         * Transforms the provided model renderer based on the current entity and rotation context.
         *
         * @param parent          The parent entity model rendering this transform
         * @param modelRenderer   The renderer to transform
         * @param entity          The entity the model is rendering for
         * @param limbSwing       The swing factor for limb movement
         * @param limbSwingAmount The swing amount factor for limb movement
         * @param ageInTicks      The entity age in ticks
         * @param netHeadYaw      The yaw of the entity's look
         * @param headPitch       The pitch of the entity's look
         */
        void transform(GeometryEntityModel<T> parent, ModelRenderer modelRenderer, T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch);
    }
}
