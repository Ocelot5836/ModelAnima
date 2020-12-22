package io.github.ocelot.modelanima.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientInit
{
    public static void initClient(FMLClientSetupEvent event)
    {
        DeferredWorkQueue.runLater(() ->
        {
            for (PlayerRenderer renderer : Minecraft.getInstance().getRenderManager().getSkinMap().values())
            {
                renderer.addLayer(new TestLayer(renderer));
            }
        });
    }
}
