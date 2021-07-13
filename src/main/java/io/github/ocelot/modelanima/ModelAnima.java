package io.github.ocelot.modelanima;

import io.github.ocelot.modelanima.api.client.animation.AnimationManager;
import io.github.ocelot.modelanima.api.client.geometry.GeometryModelManager;
import io.github.ocelot.modelanima.api.client.texture.GeometryTextureManager;
import io.github.ocelot.modelanima.core.common.network.ModelAnimaMessages;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * <p>Contains static information about Model Anima.</p>
 *
 * @author Ocelot
 * @since 1.0.0
 */
@Mod(ModelAnima.MOD_ID)
public class ModelAnima
{
    public static final String MOD_ID = "modelanima";

    public ModelAnima()
    {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModelAnimaMessages.init();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
        {
            GeometryModelManager.init(bus);
            GeometryTextureManager.init(bus);
            AnimationManager.init(bus);
        });
    }
}
