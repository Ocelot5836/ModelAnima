package io.github.ocelot.modelanima.api.client.geometry;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import io.github.ocelot.modelanima.api.common.geometry.GeometryModelData;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;

import java.util.Arrays;

public class BoneModelRenderer extends ModelRenderer
{
    private static final Vector4f TRANSFORM_VECTOR = new Vector4f();
    private static final Vector3f NORMAL_VECTOR = new Vector3f();

    private final float textureWidth;
    private final float textureHeight;
    private final GeometryModelData.Bone bone;
    private final ObjectList<Quad> quads;
    private float parentX;
    private float parentY;
    private float parentZ;

    public BoneModelRenderer(Model parent, GeometryModelData.Bone bone)
    {
        super(parent, 0, 0);
        this.textureWidth = parent.textureWidth;
        this.textureHeight = parent.textureHeight;
        this.bone = bone;
        this.quads = new ObjectArrayList<>();
        this.resetTransform();
        Arrays.stream(bone.getCubes()).forEach(this::addCube);
    }

    private void setParentOffset(float parentX, float parentY, float parentZ)
    {
        this.parentX = parentX;
        this.parentY = parentY;
        this.parentZ = parentZ;
        this.resetTransform();
    }

    private void addCube(GeometryModelData.Cube cube)
    {
        boolean empty = true;
        for (Direction direction : Direction.values())
        {
            if (cube.getUV(direction) != null)
            {
                empty = false;
                break;
            }
        }

        if (empty)
            return;

        float x = cube.getOriginX() / 16f;
        float y = cube.getOriginY() / 16f;
        float z = cube.getOriginZ() / 16f;
        float sizeX = cube.getSizeX() / 16f;
        float sizeY = cube.getSizeY() / 16f;
        float sizeZ = cube.getSizeZ() / 16f;
        float rotationX = cube.getRotationX();
        float rotationY = cube.getRotationY();
        float rotationZ = cube.getRotationZ();
        float pivotX = cube.getPivotX() / 16f;
        float pivotY = cube.getPivotY() / 16f;
        float pivotZ = cube.getPivotZ() / 16f;
        float inflate = cube.isOverrideInflate() ? cube.getInflate() : this.bone.getInflate();
        boolean mirror = cube.isOverrideMirror() ? cube.isMirror() : this.bone.isMirror();

        float x1 = x + sizeX;
        float y1 = y + sizeY;
        float z1 = z + sizeZ;
        x = x - inflate;
        y = y - inflate;
        z = z - inflate;
        x1 = x1 + inflate;
        y1 = y1 + inflate;
        z1 = z1 + inflate;
        if (mirror)
        {
            float f3 = x1;
            x1 = x;
            x = f3;
        }

        MatrixStack matrixStack = new MatrixStack();
        if (rotationX != 0 || rotationY != 0 || rotationZ != 0)
        {
            matrixStack.translate(pivotX, -pivotY, pivotZ);
            matrixStack.rotate(Vector3f.ZP.rotationDegrees(rotationZ));
            matrixStack.rotate(Vector3f.YP.rotationDegrees(rotationY));
            matrixStack.rotate(Vector3f.XP.rotationDegrees(rotationX));
            matrixStack.translate(-pivotX, pivotY, -pivotZ);
        }
        MatrixStack.Entry entry = matrixStack.getLast();

        GeometryModelData.CubeUV northUV = cube.getUV(Direction.NORTH);
        if (northUV != null)
        {
            this.quads.add(new Quad(new Vertex[]{
                    new Vertex(x1, -y, z, (northUV.getU() + northUV.getUSize()) / this.textureWidth, northUV.getV() / this.textureHeight),
                    new Vertex(x, -y, z, northUV.getU() / this.textureWidth, northUV.getV() / this.textureHeight),
                    new Vertex(x, -y1, z, northUV.getU() / this.textureWidth, (northUV.getV() + northUV.getVSize()) / this.textureHeight),
                    new Vertex(x1, -y1, z, (northUV.getU() + northUV.getUSize()) / this.textureWidth, (northUV.getV() + northUV.getVSize()) / this.textureHeight)
            }, entry, mirror, Direction.NORTH));
        }

        GeometryModelData.CubeUV eastUV = cube.getUV(Direction.EAST);
        if (eastUV != null)
        {
            this.quads.add(new Quad(new Vertex[]{
                    new Vertex(x1, -y, z1, (eastUV.getU() + eastUV.getUSize()) / this.textureWidth, eastUV.getV() / this.textureHeight),
                    new Vertex(x1, -y, z, eastUV.getU() / this.textureWidth, eastUV.getV() / this.textureHeight),
                    new Vertex(x1, -y1, z, eastUV.getU() / this.textureWidth, (eastUV.getV() + eastUV.getVSize()) / this.textureHeight),
                    new Vertex(x1, -y1, z1, (eastUV.getU() + eastUV.getUSize()) / this.textureWidth, (eastUV.getV() + eastUV.getVSize()) / this.textureHeight)
            }, entry, mirror, Direction.EAST));
        }

        GeometryModelData.CubeUV southUV = cube.getUV(Direction.SOUTH);
        if (southUV != null)
        {
            this.quads.add(new Quad(new Vertex[]{
                    new Vertex(x, -y, z1, (southUV.getU() + southUV.getUSize()) / this.textureWidth, southUV.getV() / this.textureHeight),
                    new Vertex(x1, -y, z1, southUV.getU() / this.textureWidth, southUV.getV() / this.textureHeight),
                    new Vertex(x1, -y1, z1, southUV.getU() / this.textureWidth, (southUV.getV() + southUV.getVSize()) / this.textureHeight),
                    new Vertex(x, -y1, z1, (southUV.getU() + southUV.getUSize()) / this.textureWidth, (southUV.getV() + southUV.getVSize()) / this.textureHeight)
            }, entry, mirror, Direction.SOUTH));
        }

        GeometryModelData.CubeUV westUV = cube.getUV(Direction.WEST);
        if (westUV != null)
        {
            this.quads.add(new Quad(new Vertex[]{
                    new Vertex(x, -y, z, (westUV.getU() + westUV.getUSize()) / this.textureWidth, westUV.getV() / this.textureHeight),
                    new Vertex(x, -y, z1, westUV.getU() / this.textureWidth, westUV.getV() / this.textureHeight),
                    new Vertex(x, -y1, z1, westUV.getU() / this.textureWidth, (westUV.getV() + westUV.getVSize()) / this.textureHeight),
                    new Vertex(x, -y1, z, (westUV.getU() + westUV.getUSize()) / this.textureWidth, (westUV.getV() + westUV.getVSize()) / this.textureHeight)
            }, entry, mirror, Direction.WEST));
        }

        GeometryModelData.CubeUV upUV = cube.getUV(Direction.UP);
        if (upUV != null)
        {
            this.quads.add(new Quad(new Vertex[]{
                    new Vertex(x1, -y, z, (upUV.getU() + upUV.getUSize()) / this.textureWidth, upUV.getV() / this.textureHeight),
                    new Vertex(x, -y, z, upUV.getU() / this.textureWidth, upUV.getV() / this.textureHeight),
                    new Vertex(x, -y, z1, upUV.getU() / this.textureWidth, (upUV.getV() + upUV.getVSize()) / this.textureHeight),
                    new Vertex(x1, -y, z1, (upUV.getU() + upUV.getUSize()) / this.textureWidth, (upUV.getV() + upUV.getVSize()) / this.textureHeight)
            }, entry, mirror, Direction.UP));
        }

        GeometryModelData.CubeUV downUV = cube.getUV(Direction.DOWN);
        if (downUV != null)
        {
            this.quads.add(new Quad(new Vertex[]{
                    new Vertex(x1, -y1, z1, (downUV.getU() + downUV.getUSize()) / this.textureWidth, downUV.getV() / this.textureHeight),
                    new Vertex(x, -y1, z1, downUV.getU() / this.textureWidth, downUV.getV() / this.textureHeight),
                    new Vertex(x, -y1, z, downUV.getU() / this.textureWidth, (downUV.getV() + downUV.getVSize()) / this.textureHeight),
                    new Vertex(x1, -y1, z, (downUV.getU() + downUV.getUSize()) / this.textureWidth, (downUV.getV() + downUV.getVSize()) / this.textureHeight)
            }, entry, mirror, Direction.DOWN));
        }
    }

    public void resetTransform()
    {
        this.rotateAngleX = (float) (Math.PI / 180f) * this.bone.getRotationX();
        this.rotateAngleY = (float) (Math.PI / 180f) * this.bone.getRotationY();
        this.rotateAngleZ = (float) (Math.PI / 180f) * this.bone.getRotationZ();
    }

    @Override
    public void addChild(ModelRenderer renderer)
    {
        if (renderer instanceof BoneModelRenderer)
        {
            ((BoneModelRenderer) renderer).setParentOffset(this.bone.getPivotX() + this.parentX, this.bone.getPivotY() + this.parentY, this.bone.getPivotZ() + this.parentZ);
        }
        super.addChild(renderer);
    }

    @Override
    public void render(MatrixStack matrixStack, IVertexBuilder builder, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
    {
        super.render(matrixStack, builder, packedLight, packedOverlay, red, green, blue, alpha);

        if (this.showModel && !this.quads.isEmpty())
        {
            matrixStack.push();
            this.translateRotate(matrixStack);

            Matrix4f matrix4f = matrixStack.getLast().getMatrix();
            Matrix3f matrix3f = matrixStack.getLast().getNormal();
            for (Quad quad : this.quads)
            {
                NORMAL_VECTOR.set(quad.normal.getX(), quad.normal.getY(), quad.normal.getZ());
                NORMAL_VECTOR.transform(matrix3f);
                for (Vertex vertex : quad.vertices)
                {
                    TRANSFORM_VECTOR.set(vertex.x, vertex.y, vertex.z, 1);
                    TRANSFORM_VECTOR.transform(quad.rotation);
                    TRANSFORM_VECTOR.transform(matrix4f);
                    builder.addVertex(TRANSFORM_VECTOR.getX(), TRANSFORM_VECTOR.getY(), TRANSFORM_VECTOR.getZ(), red, green, blue, alpha, vertex.u, vertex.v, packedOverlay, packedLight, NORMAL_VECTOR.getX(), NORMAL_VECTOR.getY(), NORMAL_VECTOR.getZ());
                }
            }

            matrixStack.pop();
        }
    }

    @Override
    public void translateRotate(MatrixStack matrixStack)
    {
        matrixStack.translate(this.bone.getPivotX() / 16.0F, -this.bone.getPivotY() / 16.0F, this.bone.getPivotZ() / 16.0F);
        if (this.rotateAngleZ != 0)
            matrixStack.rotate(Vector3f.ZP.rotation(this.rotateAngleZ));
        if (this.rotateAngleY != 0)
            matrixStack.rotate(Vector3f.YP.rotation(this.rotateAngleY));
        if (this.rotateAngleX != 0)
            matrixStack.rotate(Vector3f.XP.rotation(this.rotateAngleX));
        matrixStack.translate(-this.bone.getPivotX() / 16.0F, this.bone.getPivotY() / 16.0F, -this.bone.getPivotZ() / 16.0F);
    }

    private static class Vertex
    {
        private final float x;
        private final float y;
        private final float z;
        private final float u;
        private final float v;

        private Vertex(float x, float y, float z, float u, float v)
        {
            this.x = x;
            this.y = y;
            this.z = z;
            this.u = u;
            this.v = v;
        }
    }

    private static class Quad
    {
        private final Vertex[] vertices;
        private final Matrix4f rotation;
        private final Vector3f normal;

        public Quad(Vertex[] vertices, MatrixStack.Entry entry, boolean mirror, Direction direction)
        {
            this.vertices = vertices;
            this.rotation = entry.getMatrix();
            if (mirror)
            {
                int i = vertices.length;

                for (int j = 0; j < i / 2; ++j)
                {
                    Vertex vertex = vertices[j];
                    vertices[j] = vertices[i - 1 - j];
                    vertices[i - 1 - j] = vertex;
                }
            }

            this.normal = direction.toVector3f();
            if (mirror)
            {
                this.normal.mul(-1.0F, 1.0F, 1.0F);
            }
            this.normal.transform(entry.getNormal());
        }
    }
}
