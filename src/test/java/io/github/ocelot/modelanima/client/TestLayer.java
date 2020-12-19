package io.github.ocelot.modelanima.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.ocelot.modelanima.TestMod;
import io.github.ocelot.modelanima.api.client.geometry.GeometryModelRenderer;
import io.github.ocelot.modelanima.api.client.geometry.LocalGeometryModelLoader;
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
        GeometryModelRenderer.render(this.getEntityModel(), LocalGeometryModelLoader.getModel(new ResourceLocation(TestMod.MOD_ID, "battlefields/hoodie")), new ResourceLocation(TestMod.MOD_ID, "discord_hoodie"), matrixStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
        GeometryModelRenderer.render(this.getEntityModel(), LocalGeometryModelLoader.getModel(new ResourceLocation(TestMod.MOD_ID, "battlefields/santa_hat")), new ResourceLocation(TestMod.MOD_ID, "santa_hat"), matrixStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
//        GeometryModelRenderer.render(this.getEntityModel(), LocalGeometryModelLoader.getModel(new ResourceLocation(TestMod.MOD_ID, "battlefields/jungle_mask")), new ResourceLocation(TestMod.MOD_ID, "jungle_mask"), matrixStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
//        GeometryModelRenderer.render(this.getEntityModel(), LocalGeometryModelLoader.getModel(new ResourceLocation(TestMod.MOD_ID, "battlefields/sombrero")), new ResourceLocation(TestMod.MOD_ID, "sombrero"), matrixStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
//        GeometryModelRenderer.render(this.getEntityModel(), LocalGeometryModelLoader.getModel(new ResourceLocation(TestMod.MOD_ID, "battlefields/ears")), new ResourceLocation(TestMod.MOD_ID, "bloodshot_ears"), matrixStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
//        GeometryModelRenderer.render(this.getEntityModel(), LocalGeometryModelLoader.getModel(new ResourceLocation(TestMod.MOD_ID, "battlefields/tuxedo")), new ResourceLocation(TestMod.MOD_ID, "tuxedo"), matrixStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
    }
}
