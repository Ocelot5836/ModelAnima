package io.github.ocelot.modelanima;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.Locale;

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
     * The logging marker for extra geometry debug information.
     */
    public static final Marker GEOMETRY = MarkerManager.getMarker(DOMAIN.toUpperCase(Locale.ROOT) + "_GEOMETRY");
}
