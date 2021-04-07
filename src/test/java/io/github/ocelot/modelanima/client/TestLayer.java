package io.github.ocelot.modelanima.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.ocelot.modelanima.TestMod;
import io.github.ocelot.modelanima.api.client.geometry.AnimatedModel;
import io.github.ocelot.modelanima.api.client.geometry.GeometryModel;
import io.github.ocelot.modelanima.api.client.geometry.GeometryModelRenderer;
import io.github.ocelot.modelanima.api.client.texture.GeometryTextureManager;
import io.github.ocelot.modelanima.api.client.util.LocalGeometryModelLoader;
import io.github.ocelot.modelanima.api.common.animation.AnimationData;
import io.github.ocelot.modelanima.api.common.animation.AnimationLoader;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;

import java.io.InputStreamReader;

public class TestLayer extends LayerRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>>
{
    private static final AnimationData[] ANIMATIONS;

    static
    {
        try (InputStreamReader reader = new InputStreamReader(TestLayer.class.getResourceAsStream("/assets/" + TestMod.MOD_ID + "/animations/llama_trader_test.json")))
        {
            ANIMATIONS = AnimationLoader.parse(reader);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public TestLayer(IEntityRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> entityRenderer)
    {
        super(entityRenderer);
    }

    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, AbstractClientPlayerEntity player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
    {
        if (GeometryTextureManager.isReloading())
            return;

        GeometryModel geometryModel = LocalGeometryModelLoader.getModel(new ResourceLocation(TestMod.MOD_ID, "llama_trader"));
        GeometryModelRenderer.copyModelAngles(this.getEntityModel(), geometryModel);

        if (geometryModel instanceof AnimatedModel && ANIMATIONS.length > 1)
        {
            AnimationData animation = ANIMATIONS[0];
            ((AnimatedModel) geometryModel).applyAnimation((player.ticksExisted + partialTicks) / 20F, animation);
        }

        GeometryModelRenderer.render(geometryModel, new ResourceLocation(TestMod.MOD_ID, "models/llama_trader"), matrixStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
    }
}
