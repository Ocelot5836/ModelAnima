package io.github.ocelot.modelanima;

import io.github.ocelot.modelanima.api.client.animation.AnimationManager;
import io.github.ocelot.modelanima.api.client.geometry.GeometryModelManager;
import io.github.ocelot.modelanima.api.client.texture.GeometryTextureManager;
import io.github.ocelot.modelanima.core.common.network.ModelAnimaMessages;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;

/**
 * <p>Contains static information about Model Anima.</p>
 *
 * @author Ocelot
 * @since 1.0.0
 */
public class ModelAnima
{
    /**
     * The domain (modid) used for resource locations.
     */
    public static final String DOMAIN = "modelanima";
    private static String parentModId;

    /**
     * <p>Initializes all required loading for ModelAnima.</p>
     *
     * @param bus The mod event bus to register events on
     */
    public static void init(IEventBus bus)
    {
        ModelAnima.parentModId = ModLoadingContext.get().getActiveNamespace();
        ModelAnimaMessages.init();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
        {
            GeometryModelManager.init(bus);
            GeometryTextureManager.init(bus);
            AnimationManager.init(bus);
        });
    }

    /**
     * @return The id of the mod hosting ModelAnima
     */
    public static String getParentModId()
    {
        return parentModId;
    }
}
