package io.github.ocelot.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.ocelot.TestMod;
import io.github.ocelot.client.model.GeometryModel;
import io.github.ocelot.client.model.texture.GeometryModelTextureTable;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;

public class TestLayer extends LayerRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>>
{
    private static final ResourceLocation MODEL_LOCATION = new ResourceLocation(TestMod.MOD_ID, "coldman");
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation(TestMod.MOD_ID, "coldman");

    public TestLayer(IEntityRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> entityRenderer)
    {
        super(entityRenderer);
    }

    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, AbstractClientPlayerEntity player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
    {
        GeometryModel model = GeometryModelManager.getModel(MODEL_LOCATION);
        GeometryModelTextureTable texture = GeometryModelManager.getTextureTable(TEXTURE_LOCATION);
        GeometryModelRenderer.render(this.getEntityModel(), model, texture, matrixStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
    }
}
