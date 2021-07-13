package io.github.ocelot.modelanima.client;

import io.github.ocelot.modelanima.TestMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientInit
{
    public static void initClient(FMLClientSetupEvent event)
    {
        RenderingRegistry.registerEntityRenderingHandler(TestMod.YETI.get(), YetiRenderer::new);
        event.enqueueWork(() ->
        {
            for (PlayerRenderer renderer : Minecraft.getInstance().getEntityRenderDispatcher().getSkinMap().values())
            {
                renderer.addLayer(new TestLayer(renderer));
            }
        });
    }
}
