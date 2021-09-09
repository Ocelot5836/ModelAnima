package io.github.ocelot.modelanima.core.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import io.github.ocelot.modelanima.api.client.geometry.GeometryModelRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin
{
    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;checkPoseStack(Lcom/mojang/blaze3d/vertex/PoseStack;)V", ordinal = 0))
    public void renderLevel(PoseStack p_228426_1_, float p_228426_2_, long p_228426_3_, boolean p_228426_5_, Camera p_228426_6_, GameRenderer p_228426_7_, LightTexture p_228426_8_, Matrix4f p_228426_9_, CallbackInfo ci)
    {
        GeometryModelRenderer.endBatch();
    }
}
