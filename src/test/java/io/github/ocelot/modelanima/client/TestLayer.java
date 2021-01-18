package io.github.ocelot.modelanima.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.ocelot.modelanima.TestMod;
import io.github.ocelot.modelanima.api.client.geometry.GeometryModelRenderer;
import io.github.ocelot.modelanima.api.client.util.LocalGeometryModelLoader;
import io.github.ocelot.modelanima.api.client.texture.GeometryTextureManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class TestLayer extends LayerRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>>
{
    public TestLayer(IEntityRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> entityRenderer)
    {
        super(entityRenderer);
    }

    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, AbstractClientPlayerEntity player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
    {
        if (GeometryTextureManager.isReloading())
            return;

        Minecraft.getInstance().getRenderTypeBuffers().getBufferSource().finish();

        matrixStack.push();
        matrixStack.translate(0, 1, 0);
        matrixStack.scale(0.01f, 0.01f, 0.01f);
        GeometryModelRenderer.render(this.getEntityModel(), LocalGeometryModelLoader.getModel(new ResourceLocation(TestMod.MOD_ID, "rosalina")), new ResourceLocation(TestMod.MOD_ID, "rosalina"), matrixStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
        matrixStack.pop();

        GL11.glShadeModel(GL11.GL_SMOOTH);
        Minecraft.getInstance().getRenderTypeBuffers().getBufferSource().finish();
        GL11.glShadeModel(GL11.GL_FLAT);

//        GeometryModelRenderer.render(this.getEntityModel(), LocalGeometryModelLoader.getModel(new ResourceLocation(TestMod.MOD_ID, "battlefields/hoodie")), new ResourceLocation(TestMod.MOD_ID, "discord_hoodie"), matrixStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
//        GeometryModelRenderer.render(this.getEntityModel(), LocalGeometryModelLoader.getModel(new ResourceLocation(TestMod.MOD_ID, "battlefields/cap")), new ResourceLocation(TestMod.MOD_ID, "v_cap"), matrixStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
    }
}
