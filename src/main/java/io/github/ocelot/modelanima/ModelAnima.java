package io.github.ocelot.modelanima;

import io.github.ocelot.modelanima.api.client.animation.AnimationManager;
import io.github.ocelot.modelanima.api.client.texture.GeometryTextureManager;
import net.minecraftforge.eventbus.api.IEventBus;

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

    /**
     * <p>Initializes all required loading for ModelAnima.</p>
     *
     * @param bus The mod event bus to register events on
     */
    public static void init(IEventBus bus)
    {
        GeometryTextureManager.init(bus);
        AnimationManager.init(bus);
    }
}
