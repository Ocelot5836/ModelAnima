package io.github.ocelot.modelanima.core.client.geometry;

import io.github.ocelot.modelanima.api.common.geometry.texture.GeometryModelTexture;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static org.lwjgl.opengl.GL11.*;

@OnlyIn(Dist.CLIENT)
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
        RenderType.State rendertype$state = RenderType.State.getBuilder().texture(new RenderState.TextureState(locationIn, false, false)).transparency(NO_TRANSPARENCY).diffuseLighting(texture.isSmoothShading() ? SMOOTH_LIGHTING : DIFFUSE_LIGHTING_ENABLED).lightmap(LIGHTMAP_ENABLED).overlay(OVERLAY_ENABLED).build(true);
        return makeType("geometry_solid", DefaultVertexFormats.ENTITY, 7, 256, true, false, rendertype$state);
    }

    public static RenderType getGeometryCutout(GeometryModelTexture texture, ResourceLocation locationIn)
    {
        RenderType.State rendertype$state = RenderType.State.getBuilder().texture(new RenderState.TextureState(locationIn, false, false)).transparency(NO_TRANSPARENCY).diffuseLighting(texture.isSmoothShading() ? SMOOTH_LIGHTING : DIFFUSE_LIGHTING_ENABLED).alpha(DEFAULT_ALPHA).cull(CULL_DISABLED).lightmap(LIGHTMAP_ENABLED).overlay(OVERLAY_ENABLED).build(true);
        return makeType("geometry_cutout", DefaultVertexFormats.ENTITY, 7, 256, true, false, rendertype$state);
    }

    public static RenderType getGeometryCutoutCull(GeometryModelTexture texture, ResourceLocation locationIn)
    {
        RenderType.State rendertype$state = RenderType.State.getBuilder().texture(new RenderState.TextureState(locationIn, false, false)).transparency(NO_TRANSPARENCY).diffuseLighting(texture.isSmoothShading() ? SMOOTH_LIGHTING : DIFFUSE_LIGHTING_ENABLED).alpha(DEFAULT_ALPHA).lightmap(LIGHTMAP_ENABLED).overlay(OVERLAY_ENABLED).build(true);
        return makeType("geometry_cutout_cull", DefaultVertexFormats.ENTITY, 7, 256, true, false, rendertype$state);
    }

    public static RenderType getGeometryTranslucent(GeometryModelTexture texture, ResourceLocation locationIn)
    {
        RenderType.State rendertype$state = RenderType.State.getBuilder().texture(new RenderState.TextureState(locationIn, false, false)).transparency(TRANSLUCENT_TRANSPARENCY).diffuseLighting(texture.isSmoothShading() ? SMOOTH_LIGHTING : DIFFUSE_LIGHTING_ENABLED).alpha(DEFAULT_ALPHA).cull(CULL_DISABLED).lightmap(LIGHTMAP_ENABLED).overlay(OVERLAY_ENABLED).build(true);
        return makeType("geometry_translucent", DefaultVertexFormats.ENTITY, 7, 256, true, true, rendertype$state);
    }

    public static RenderType getGeometryTranslucentCull(GeometryModelTexture texture, ResourceLocation locationIn)
    {
        RenderType.State rendertype$state = RenderType.State.getBuilder().texture(new RenderState.TextureState(locationIn, false, false)).transparency(TRANSLUCENT_TRANSPARENCY).diffuseLighting(texture.isSmoothShading() ? SMOOTH_LIGHTING : DIFFUSE_LIGHTING_ENABLED).alpha(DEFAULT_ALPHA).lightmap(LIGHTMAP_ENABLED).overlay(OVERLAY_ENABLED).build(true);
        return makeType("geometry_translucent_cull", DefaultVertexFormats.ENTITY, 7, 256, true, true, rendertype$state);
    }
}
