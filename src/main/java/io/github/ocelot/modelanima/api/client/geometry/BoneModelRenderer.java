package io.github.ocelot.modelanima.api.client.geometry;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import io.github.ocelot.modelanima.api.common.geometry.GeometryModelData;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;

import javax.annotation.Nullable;
import java.util.Arrays;

public class BoneModelRenderer extends ModelRenderer
{
    private static final Vector4f TRANSFORM_VECTOR = new Vector4f();
    private static final Vector3f NORMAL_VECTOR = new Vector3f();

    private final BedrockGeometryModel parent;
    private final float textureWidth;
    private final float textureHeight;
    private final GeometryModelData.Bone bone;
    private final ObjectList<Quad> quads;
    private float parentX;
    private float parentY;
    private float parentZ;

    public BoneModelRenderer(BedrockGeometryModel parent, GeometryModelData.Bone bone)
    {
        super(parent, 0, 0);
        this.parent = parent;
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
        float inflate = (cube.isOverrideInflate() ? cube.getInflate() : this.bone.getInflate()) / 16f;
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
        matrixStack.translate(pivotX, -pivotY, pivotZ);
        matrixStack.rotate(Vector3f.ZP.rotationDegrees(rotationZ));
        matrixStack.rotate(Vector3f.YP.rotationDegrees(rotationY));
        matrixStack.rotate(Vector3f.XP.rotationDegrees(rotationX));
        matrixStack.translate(-pivotX, pivotY, -pivotZ);
        MatrixStack.Entry entry = matrixStack.getLast();
        Matrix4f matrix4f = entry.getMatrix();
        Matrix3f matrix3f = entry.getNormal();

        this.addFace(cube, matrix4f, matrix3f, x1, -y1, z, x, -y1, z, x, -y, z, x1, -y, z, Direction.NORTH);
        this.addFace(cube, matrix4f, matrix3f, x1, -y1, z1, x1, -y1, z, x1, -y, z, x1, -y, z1, Direction.EAST);
        this.addFace(cube, matrix4f, matrix3f, x, -y1, z1, x1, -y1, z1, x1, -y, z1, x, -y, z1, Direction.SOUTH);
        this.addFace(cube, matrix4f, matrix3f, x, -y1, z, x, -y1, z1, x, -y, z1, x, -y, z, Direction.WEST);
        this.addFace(cube, matrix4f, matrix3f, x1, -y, z, x, -y, z, x, -y, z1, x1, -y, z1, Direction.DOWN);
        this.addFace(cube, matrix4f, matrix3f, x1, -y1, z1, x, -y1, z1, x, -y1, z, x1, -y1, z, Direction.UP);
    }

    private void addFace(GeometryModelData.Cube cube, Matrix4f matrix4f, Matrix3f matrix3f, float x0, float y0, float z0, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, Direction face)
    {
        GeometryModelData.CubeUV uv = cube.getUV(face);
        if (uv != null)
        {
            this.quads.add(new Quad(new Vertex[]{
                    new Vertex(matrix4f, x0, y0, z0, (uv.getU() + uv.getUSize()) / this.textureWidth, uv.getV() / this.textureHeight),
                    new Vertex(matrix4f, x1, y1, z1, uv.getU() / this.textureWidth, uv.getV() / this.textureHeight),
                    new Vertex(matrix4f, x2, y2, z2, uv.getU() / this.textureWidth, (uv.getV() + uv.getVSize()) / this.textureHeight),
                    new Vertex(matrix4f, x3, y3, z3, (uv.getU() + uv.getUSize()) / this.textureWidth, (uv.getV() + uv.getVSize()) / this.textureHeight)
            }, matrix3f, uv.getMaterialInstance(), cube.isOverrideMirror() ? cube.isMirror() : this.bone.isMirror(), face.getAxis().isVertical() ? face.getOpposite() : face));
        }
    }

    public void resetTransform()
    {
        this.rotateAngleX = (float) (Math.PI / 180f) * this.bone.getRotationX();
        this.rotateAngleY = (float) (Math.PI / 180f) * this.bone.getRotationY();
        this.rotateAngleZ = (float) (Math.PI / 180f) * this.bone.getRotationZ();
        this.rotationPointX = this.bone.getPivotX();
        this.rotationPointY = -this.bone.getPivotY();
        this.rotationPointZ = this.bone.getPivotZ();
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
                if (!quad.material.equals(this.parent.getActiveMaterial()))
                    continue;
                NORMAL_VECTOR.set(quad.normal.getX(), quad.normal.getY(), quad.normal.getZ());
                NORMAL_VECTOR.transform(matrix3f);
                for (Vertex vertex : quad.vertices)
                {
                    TRANSFORM_VECTOR.set(vertex.x, vertex.y, vertex.z, 1);
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
        matrixStack.translate(this.rotationPointX / 16.0F, this.rotationPointY / 16.0F, this.rotationPointZ / 16.0F);
        if (this.rotateAngleZ != 0)
            matrixStack.rotate(Vector3f.ZP.rotation(this.rotateAngleZ));
        if (this.rotateAngleY != 0)
            matrixStack.rotate(Vector3f.YP.rotation(this.rotateAngleY));
        if (this.rotateAngleX != 0)
            matrixStack.rotate(Vector3f.XP.rotation(this.rotateAngleX));
        matrixStack.translate(-this.rotationPointX / 16.0F, -this.rotationPointY / 16.0F, -this.rotationPointZ / 16.0F);
    }

    @Nullable
    public String getParent()
    {
        return this.bone.getParent();
    }

    private static class Vertex
    {
        private final float x;
        private final float y;
        private final float z;
        private final float u;
        private final float v;

        private Vertex(Matrix4f matrix4f, float x, float y, float z, float u, float v)
        {
            TRANSFORM_VECTOR.set(x, y, z, 1.0F);
            TRANSFORM_VECTOR.transform(matrix4f);
            this.x = TRANSFORM_VECTOR.getX();
            this.y = TRANSFORM_VECTOR.getY();
            this.z = TRANSFORM_VECTOR.getZ();
            this.u = u;
            this.v = v;
        }
    }

    private static class Quad
    {
        private final Vertex[] vertices;
        private final Vector3f normal;
        private final String material;

        public Quad(Vertex[] vertices, Matrix3f normal, String material, boolean mirror, Direction direction)
        {
            this.vertices = vertices;
            this.material = material;
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
            this.normal.transform(normal);
        }
    }
}