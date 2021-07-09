package io.github.ocelot.modelanima.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.ocelot.modelanima.TestMod;
import io.github.ocelot.modelanima.api.client.animation.AnimationManager;
import io.github.ocelot.modelanima.api.client.geometry.AnimatedModel;
import io.github.ocelot.modelanima.api.client.geometry.GeometryModel;
import io.github.ocelot.modelanima.api.client.geometry.GeometryModelManager;
import io.github.ocelot.modelanima.api.client.geometry.GeometryModelRenderer;
import io.github.ocelot.modelanima.api.common.animation.AnimationData;
import io.github.ocelot.modelanima.api.common.molang.MolangRuntime;
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
        GeometryModel geometryModel = GeometryModelManager.getModel(new ResourceLocation(TestMod.MOD_ID, "ghast"));
        geometryModel.resetTransformation();
        GeometryModelRenderer.copyModelAngles(this.getParentModel(), geometryModel);

        float ticks = player.tickCount + partialTicks;
        if (geometryModel instanceof AnimatedModel)
        {
            AnimationData animation = AnimationManager.getAnimation(new ResourceLocation(TestMod.MOD_ID, "animation.ghast.move"));
            ((AnimatedModel) geometryModel).applyAnimation(ticks / 20F, animation, MolangRuntime.runtime().
                    setQuery("target_x_rotation", player.getViewXRot(partialTicks)).
                    setQuery("target_y_rotation", player.getViewYRot(partialTicks)).
                    setQuery("model_scale", 1.0F).setQuery("modified_distance_moved", player.moveDist).
                    setQuery("modified_move_speed", player.getSpeed()).
                    setQuery("is_baby", player.isBaby() ? 1.0F : 0.0F).
                    setQuery("on_fire_time", (float) player.getRemainingFireTicks() / 20.0F)
            );
        }

        GeometryModelRenderer.render(geometryModel, new ResourceLocation(TestMod.MOD_ID, "ghast"), matrixStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
    }
}
