package io.github.ocelot.modelanima.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientInit
{
    public static void initClient(FMLClientSetupEvent event)
    {
        event.enqueueWork(() ->
        {
            for (PlayerRenderer renderer : Minecraft.getInstance().getEntityRenderDispatcher().getSkinMap().values())
            {
                renderer.addLayer(new TestLayer(renderer));
            }
        });
    }
}
