package io.github.ocelot.modelanima.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.ocelot.modelanima.TestMod;
import io.github.ocelot.modelanima.api.client.geometry.AnimatedGeometryEntityModel;
import io.github.ocelot.modelanima.api.client.geometry.GeometryModelRenderer;
import io.github.ocelot.modelanima.api.common.animation.AnimationData;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;

public class TestLayer extends LayerRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>>
{
    public TestLayer(IEntityRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> entityRenderer)
    {
        super(entityRenderer);
    }

    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, AbstractClientPlayerEntity player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
    {
        if (!player.isInvisible())
        {
            AnimatedGeometryEntityModel<AbstractClientPlayerEntity> model = new AnimatedGeometryEntityModel<>(new ResourceLocation(TestMod.MOD_ID, "guardian"));
            model.setVariableProvider(context ->
            {

            });
            model.setAnimations(new ResourceLocation(TestMod.MOD_ID, "animation.guardian.setup"), new ResourceLocation(TestMod.MOD_ID, "animation.guardian.spikes"), new ResourceLocation(TestMod.MOD_ID, "animation.guardian.swim"));
            this.getParentModel().copyPropertiesTo(model);
            model.prepareMobModel(player, limbSwing, limbSwingAmount, partialTicks);
            model.setupAnim(player, limbSwing, limbSwingAmount, (ageInTicks / 20F) % (float) Arrays.stream(model.getAnimations()).mapToDouble(AnimationData::getAnimationLength).max().orElse(0), netHeadYaw, headPitch);
            GeometryModelRenderer.copyModelAngles(this.getParentModel(), model.getModel());
            model.render(matrixStack, new ResourceLocation(TestMod.MOD_ID, "guardian"), packedLight, LivingRenderer.getOverlayCoords(player, 0.0F), 1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}
