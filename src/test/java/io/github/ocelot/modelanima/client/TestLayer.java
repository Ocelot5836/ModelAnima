package io.github.ocelot.modelanima.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;

public class TestLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>
{
    public TestLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> entityRenderer)
    {
        super(entityRenderer);
    }

    @Override
    public void render(PoseStack matrixStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
    {
//        if (!player.isInvisible())
//        {
//            AnimatedGeometryEntityModel<AbstractClientPlayerEntity> model = new AnimatedGeometryEntityModel<>(new ResourceLocation(TestMod.MOD_ID, "yeti"));
//            model.setVariableProvider(context ->
//            {
//                context.add("limb_swing", limbSwing);
//                context.add("limb_swing_amount", limbSwingAmount);
//            });
//            model.setTexture(new ResourceLocation(TestMod.MOD_ID, "yeti"));
//            model.setAnimations(new ResourceLocation(TestMod.MOD_ID, "yeti.setup"), (player.isCrouching() ? new ResourceLocation(TestMod.MOD_ID, "yeti.attack") : new ResourceLocation(TestMod.MOD_ID, "yeti.swing_arms")));
//            this.getParentModel().copyPropertiesTo(model);
//            model.prepareMobModel(player, limbSwing, limbSwingAmount, partialTicks);
//            model.setupAnim(player, limbSwing, limbSwingAmount, ageInTicks % (float) Arrays.stream(model.getAnimations()).mapToDouble(AnimationData::getAnimationLength).max().orElse(0) * 20, netHeadYaw, headPitch);
//            GeometryModelRenderer.copyModelAngles(this.getParentModel(), model.getModel());
//            model.renderToBuffer(matrixStack, buffer.getBuffer(model.renderType(this.getTextureLocation(player))), packedLight, LivingRenderer.getOverlayCoords(player, 0.0F), 1.0F, 1.0F, 1.0F, 1.0F);
//        }
    }
}
