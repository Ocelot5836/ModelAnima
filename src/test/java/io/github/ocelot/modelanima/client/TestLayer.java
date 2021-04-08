package io.github.ocelot.modelanima.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.ocelot.modelanima.TestMod;
import io.github.ocelot.modelanima.api.client.animation.AnimationManager;
import io.github.ocelot.modelanima.api.client.geometry.*;
import io.github.ocelot.modelanima.api.common.animation.AnimationData;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;

public class TestLayer extends LayerRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>>
{
    public TestLayer(IEntityRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> entityRenderer)
    {
        super(entityRenderer);
    }

    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, AbstractClientPlayerEntity player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
    {
        GeometryModel geometryModel = GeometryModelManager.getModel(new ResourceLocation(TestMod.MOD_ID, "owl"));
        geometryModel.resetTransformation();
        GeometryModelRenderer.copyModelAngles(this.getEntityModel(), geometryModel);

        if (geometryModel instanceof AnimatedModel)
        {
            AnimationData animation = AnimationManager.getAnimation(new ResourceLocation(TestMod.MOD_ID, "owl_floss"));
            ((AnimatedModel) geometryModel).applyAnimation((player.ticksExisted + partialTicks) / 20F, animation);
        }

        GeometryModelRenderer.render(geometryModel, new ResourceLocation(TestMod.MOD_ID, "owl"), matrixStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
    }
}
