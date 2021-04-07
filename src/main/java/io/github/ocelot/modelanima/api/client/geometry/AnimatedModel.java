package io.github.ocelot.modelanima.api.client.geometry;

import io.github.ocelot.modelanima.api.common.animation.AnimationData;
import io.github.ocelot.modelanima.api.common.geometry.GeometryModelData;

/**
 * <p>Transforms model parts according to {@link AnimationData} over time.</p>
 *
 * @author Ocelot
 * @since 1.0.0
 */
public interface AnimatedModel
{
    /**
     * Applies the specified animation transformations at the specified time.
     *
     * @param animationTime The time of the animation in seconds
     * @param animation     The animation
     */
    void applyAnimation(float animationTime, AnimationData animation);

    /**
     * Fetches all locators for the specified part.
     *
     * @param part The name of the part to get locators from
     * @return All locators in the model
     */
    GeometryModelData.Locator[] getLocators(String part);
}
