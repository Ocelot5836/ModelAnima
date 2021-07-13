package io.github.ocelot.modelanima.core.client.geometry;

import io.github.ocelot.modelanima.api.common.texture.GeometryModelTexture;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Ocelot
 */
public final class GeometryRenderTypes extends RenderType
{
    private static final DiffuseLightingState SMOOTH_LIGHTING = new DiffuseLightingState(true)
    {
        @Override
        public void setupRenderState()
        {
            super.setupRenderState();
            glShadeModel(GL_SMOOTH);
        }

        @Override
        public void clearRenderState()
        {
            super.clearRenderState();
            glShadeModel(GL_FLAT);
        }
    };

    private GeometryRenderTypes(String nameIn, VertexFormat formatIn, int drawModeIn, int bufferSizeIn, boolean useDelegateIn, boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn)
    {
        super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
    }

    public static RenderType getGeometrySolid(GeometryModelTexture texture, ResourceLocation locationIn)
    {
        RenderType.State rendertype$state = RenderType.State.builder().setTextureState(new RenderState.TextureState(locationIn, false, false)).setTransparencyState(NO_TRANSPARENCY).setDiffuseLightingState(texture.isSmoothShading() ? SMOOTH_LIGHTING : DIFFUSE_LIGHTING).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(true);
        return create("geometry_solid", DefaultVertexFormats.NEW_ENTITY, 7, 256, true, false, rendertype$state);
    }

    public static RenderType getGeometryCutout(GeometryModelTexture texture, ResourceLocation locationIn)
    {
        RenderType.State rendertype$state = RenderType.State.builder().setTextureState(new RenderState.TextureState(locationIn, false, false)).setTransparencyState(NO_TRANSPARENCY).setDiffuseLightingState(texture.isSmoothShading() ? SMOOTH_LIGHTING : DIFFUSE_LIGHTING).setAlphaState(DEFAULT_ALPHA).setCullState(NO_CULL).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(true);
        return create("geometry_cutout", DefaultVertexFormats.NEW_ENTITY, 7, 256, true, false, rendertype$state);
    }

    public static RenderType getGeometryCutoutCull(GeometryModelTexture texture, ResourceLocation locationIn)
    {
        RenderType.State rendertype$state = RenderType.State.builder().setTextureState(new RenderState.TextureState(locationIn, false, false)).setTransparencyState(NO_TRANSPARENCY).setDiffuseLightingState(texture.isSmoothShading() ? SMOOTH_LIGHTING : DIFFUSE_LIGHTING).setAlphaState(DEFAULT_ALPHA).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(true);
        return create("geometry_cutout_cull", DefaultVertexFormats.NEW_ENTITY, 7, 256, true, false, rendertype$state);
    }

    public static RenderType getGeometryTranslucent(GeometryModelTexture texture, ResourceLocation locationIn)
    {
        RenderType.State rendertype$state = RenderType.State.builder().setTextureState(new RenderState.TextureState(locationIn, false, false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setDiffuseLightingState(texture.isSmoothShading() ? SMOOTH_LIGHTING : DIFFUSE_LIGHTING).setAlphaState(DEFAULT_ALPHA).setCullState(NO_CULL).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(true);
        return create("geometry_translucent", DefaultVertexFormats.NEW_ENTITY, 7, 256, true, true, rendertype$state);
    }

    public static RenderType getGeometryTranslucentCull(GeometryModelTexture texture, ResourceLocation locationIn)
    {
        RenderType.State rendertype$state = RenderType.State.builder().setTextureState(new RenderState.TextureState(locationIn, false, false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setDiffuseLightingState(texture.isSmoothShading() ? SMOOTH_LIGHTING : DIFFUSE_LIGHTING).setAlphaState(DEFAULT_ALPHA).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(true);
        return create("geometry_translucent_cull", DefaultVertexFormats.NEW_ENTITY, 7, 256, true, true, rendertype$state);
    }
}
