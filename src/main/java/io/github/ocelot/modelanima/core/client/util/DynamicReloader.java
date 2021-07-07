package io.github.ocelot.modelanima.core.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ResourceLoadProgressGui;
import net.minecraft.resources.AsyncReloader;
import net.minecraft.resources.IAsyncReloader;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Ocelot
 */
public class DynamicReloader
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final List<IFutureReloadListener> reloadListeners;
    private IAsyncReloader asyncReloader;

    public DynamicReloader()
    {
        this.reloadListeners = new ArrayList<>();
    }

    public void addListener(IFutureReloadListener listener)
    {
        this.reloadListeners.add(listener);
    }

    public CompletableFuture<Unit> reload(boolean showLoadingScreen)
    {
        if (asyncReloader != null)
            return asyncReloader.done();
        asyncReloader = AsyncReloader.of(Minecraft.getInstance().getResourceManager(), this.reloadListeners, Util.backgroundExecutor(), Minecraft.getInstance(), CompletableFuture.completedFuture(Unit.INSTANCE));
        if (showLoadingScreen)
            Minecraft.getInstance().setOverlay(new ResourceLoadProgressGui(Minecraft.getInstance(), asyncReloader, error ->
            {
                asyncReloader = null;
                error.ifPresent(LOGGER::error);
            }, true));
        return this.asyncReloader.done().handle((unit, e) ->
        {
            if (e != null)
                LOGGER.error("Error reloading", e);
            this.asyncReloader = null;
            return unit;
        });
    }

    public boolean isReloading()
    {
        return asyncReloader != null;
    }
}
