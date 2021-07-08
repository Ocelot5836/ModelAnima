package io.github.ocelot.modelanima.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.ocelot.modelanima.TestMod;
import io.github.ocelot.modelanima.api.client.animation.AnimationManager;
import io.github.ocelot.modelanima.api.client.geometry.AnimatedModel;
import io.github.ocelot.modelanima.api.client.geometry.GeometryModel;
import io.github.ocelot.modelanima.api.client.geometry.GeometryModelManager;
import io.github.ocelot.modelanima.api.client.geometry.GeometryModelRenderer;
import io.github.ocelot.modelanima.api.common.animation.AnimationData;
import io.github.ocelot.modelanima.api.common.molang.MolangCompiler;
import io.github.ocelot.modelanima.api.common.molang.MolangException;
import io.github.ocelot.modelanima.api.common.molang.MolangExpression;
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
    private static final ResourceLocation MODEL = new ResourceLocation(TestMod.MOD_ID, "llama_trader");
    private static final ResourceLocation TEXTURE = new ResourceLocation(TestMod.MOD_ID, "llama_trader");
    private static final ResourceLocation ANIMATION = new ResourceLocation(TestMod.MOD_ID, "llama_trader_pump");

    private MolangExpression expression;
    private String lastExpression;
    private float sneakStart;

    public TestLayer(IEntityRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> entityRenderer)
    {
        super(entityRenderer);
    }

    private void setExpression(String expression)
    {
        if (expression.equals(this.lastExpression))
            return;
        this.lastExpression = expression;
        try
        {
            this.expression = MolangCompiler.compile(expression);
        }
        catch (MolangException e)
        {
            e.printStackTrace();
            this.expression = MolangExpression.ZERO;
        }
    }

    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, AbstractClientPlayerEntity player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
    {
        this.setExpression("query.above_top_solid(0, 0)");

        try
        {
            System.out.println(this.expression.resolve(MolangRuntime.runtime(0).fillQueries(player, partialTicks).create()));
        }
        catch (MolangException e)
        {
            e.printStackTrace();
        }

        GeometryModel geometryModel = GeometryModelManager.getModel(MODEL);
        geometryModel.resetTransformation();
        GeometryModelRenderer.copyModelAngles(this.getParentModel(), geometryModel);

        float ticks = player.tickCount + partialTicks;
        if (player.isShiftKeyDown())
        {
            if (this.sneakStart == -1)
                this.sneakStart = ticks;

            if (geometryModel instanceof AnimatedModel)
            {
                AnimationData animation = AnimationManager.getAnimation(ANIMATION);
                ((AnimatedModel) geometryModel).applyAnimation((ticks - this.sneakStart) / 20F, animation);
            }
        }
        else
        {
            this.sneakStart = -1;
        }

        GeometryModelRenderer.render(geometryModel, TEXTURE, matrixStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
    }
}
