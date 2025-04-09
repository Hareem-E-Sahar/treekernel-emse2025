package com.jme3.collision.bih;

import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingSphere;
import com.jme3.bounding.BoundingVolume;
import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResults;
import com.jme3.collision.UnsupportedCollisionException;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix4f;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.CollisionData;
import com.jme3.scene.Mesh;
import com.jme3.scene.Mesh.Mode;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.mesh.IndexBuffer;
import com.jme3.scene.mesh.VirtualIndexBuffer;
import com.jme3.scene.mesh.WrappedIndexBuffer;
import com.jme3.util.TempVars;
import java.io.IOException;
import static java.lang.Math.max;
import java.nio.FloatBuffer;

public class BIHTree implements CollisionData {

    public static final int MAX_TREE_DEPTH = 100;

    public static final int MAX_TRIS_PER_NODE = 21;

    private Mesh mesh;

    private BIHNode root;

    private int maxTrisPerNode;

    private int numTris;

    private float[] pointData;

    private int[] triIndices;

    private transient CollisionResults boundResults = new CollisionResults();

    private transient float[] bihSwapTmp;

    private static final TriangleAxisComparator[] comparators = new TriangleAxisComparator[] { new TriangleAxisComparator(0), new TriangleAxisComparator(1), new TriangleAxisComparator(2) };

    private void initTriList(FloatBuffer vb, IndexBuffer ib) {
        pointData = new float[numTris * 3 * 3];
        int p = 0;
        for (int i = 0; i < numTris * 3; i += 3) {
            int vert = ib.get(i) * 3;
            pointData[p++] = vb.get(vert++);
            pointData[p++] = vb.get(vert++);
            pointData[p++] = vb.get(vert);
            vert = ib.get(i + 1) * 3;
            pointData[p++] = vb.get(vert++);
            pointData[p++] = vb.get(vert++);
            pointData[p++] = vb.get(vert);
            vert = ib.get(i + 2) * 3;
            pointData[p++] = vb.get(vert++);
            pointData[p++] = vb.get(vert++);
            pointData[p++] = vb.get(vert);
        }
        triIndices = new int[numTris];
        for (int i = 0; i < numTris; i++) {
            triIndices[i] = i;
        }
    }

    public BIHTree(Mesh mesh, int maxTrisPerNode) {
        this.mesh = mesh;
        this.maxTrisPerNode = maxTrisPerNode;
        if (maxTrisPerNode < 1 || mesh == null) {
            throw new IllegalArgumentException();
        }
        bihSwapTmp = new float[9];
        FloatBuffer vb = (FloatBuffer) mesh.getBuffer(Type.Position).getData();
        IndexBuffer ib = mesh.getIndexBuffer();
        if (ib == null) {
            ib = new VirtualIndexBuffer(mesh.getVertexCount(), mesh.getMode());
        } else if (mesh.getMode() != Mode.Triangles) {
            ib = new WrappedIndexBuffer(mesh);
        }
        numTris = ib.size() / 3;
        initTriList(vb, ib);
    }

    public BIHTree(Mesh mesh) {
        this(mesh, MAX_TRIS_PER_NODE);
    }

    public BIHTree() {
    }

    public void construct() {
        BoundingBox sceneBbox = createBox(0, numTris - 1);
        root = createNode(0, numTris - 1, sceneBbox, 0);
    }

    private BoundingBox createBox(int l, int r) {
        TempVars vars = TempVars.get();
        Vector3f min = vars.vect1.set(new Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY));
        Vector3f max = vars.vect2.set(new Vector3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY));
        Vector3f v1 = vars.vect3, v2 = vars.vect4, v3 = vars.vect5;
        for (int i = l; i <= r; i++) {
            getTriangle(i, v1, v2, v3);
            BoundingBox.checkMinMax(min, max, v1);
            BoundingBox.checkMinMax(min, max, v2);
            BoundingBox.checkMinMax(min, max, v3);
        }
        BoundingBox bbox = new BoundingBox(min, max);
        vars.release();
        return bbox;
    }

    int getTriangleIndex(int triIndex) {
        return triIndices[triIndex];
    }

    private int sortTriangles(int l, int r, float split, int axis) {
        int pivot = l;
        int j = r;
        TempVars vars = TempVars.get();
        Vector3f v1 = vars.vect1, v2 = vars.vect2, v3 = vars.vect3;
        while (pivot <= j) {
            getTriangle(pivot, v1, v2, v3);
            v1.addLocal(v2).addLocal(v3).multLocal(FastMath.ONE_THIRD);
            if (v1.get(axis) > split) {
                swapTriangles(pivot, j);
                --j;
            } else {
                ++pivot;
            }
        }
        vars.release();
        pivot = (pivot == l && j < pivot) ? j : pivot;
        return pivot;
    }

    private void setMinMax(BoundingBox bbox, boolean doMin, int axis, float value) {
        Vector3f min = bbox.getMin(null);
        Vector3f max = bbox.getMax(null);
        if (doMin) {
            min.set(axis, value);
        } else {
            max.set(axis, value);
        }
        bbox.setMinMax(min, max);
    }

    private float getMinMax(BoundingBox bbox, boolean doMin, int axis) {
        if (doMin) {
            return bbox.getMin(null).get(axis);
        } else {
            return bbox.getMax(null).get(axis);
        }
    }

    private BIHNode createNode(int l, int r, BoundingBox nodeBbox, int depth) {
        if ((r - l) < maxTrisPerNode || depth > MAX_TREE_DEPTH) {
            return new BIHNode(l, r);
        }
        BoundingBox currentBox = createBox(l, r);
        Vector3f exteriorExt = nodeBbox.getExtent(null);
        Vector3f interiorExt = currentBox.getExtent(null);
        exteriorExt.subtractLocal(interiorExt);
        int axis = 0;
        if (exteriorExt.x > exteriorExt.y) {
            if (exteriorExt.x > exteriorExt.z) {
                axis = 0;
            } else {
                axis = 2;
            }
        } else {
            if (exteriorExt.y > exteriorExt.z) {
                axis = 1;
            } else {
                axis = 2;
            }
        }
        if (exteriorExt.equals(Vector3f.ZERO)) {
            axis = 0;
        }
        float split = currentBox.getCenter().get(axis);
        int pivot = sortTriangles(l, r, split, axis);
        if (pivot == l || pivot == r) {
            pivot = (r + l) / 2;
        }
        if (pivot < l) {
            BoundingBox rbbox = new BoundingBox(currentBox);
            setMinMax(rbbox, true, axis, split);
            return createNode(l, r, rbbox, depth + 1);
        } else if (pivot > r) {
            BoundingBox lbbox = new BoundingBox(currentBox);
            setMinMax(lbbox, false, axis, split);
            return createNode(l, r, lbbox, depth + 1);
        } else {
            BIHNode node = new BIHNode(axis);
            BoundingBox lbbox = new BoundingBox(currentBox);
            setMinMax(lbbox, false, axis, split);
            node.setLeftPlane(getMinMax(createBox(l, max(l, pivot - 1)), false, axis));
            node.setLeftChild(createNode(l, max(l, pivot - 1), lbbox, depth + 1));
            BoundingBox rbbox = new BoundingBox(currentBox);
            setMinMax(rbbox, true, axis, split);
            node.setRightPlane(getMinMax(createBox(pivot, r), true, axis));
            node.setRightChild(createNode(pivot, r, rbbox, depth + 1));
            return node;
        }
    }

    public void getTriangle(int index, Vector3f v1, Vector3f v2, Vector3f v3) {
        int pointIndex = index * 9;
        v1.x = pointData[pointIndex++];
        v1.y = pointData[pointIndex++];
        v1.z = pointData[pointIndex++];
        v2.x = pointData[pointIndex++];
        v2.y = pointData[pointIndex++];
        v2.z = pointData[pointIndex++];
        v3.x = pointData[pointIndex++];
        v3.y = pointData[pointIndex++];
        v3.z = pointData[pointIndex++];
    }

    public void swapTriangles(int index1, int index2) {
        int p1 = index1 * 9;
        int p2 = index2 * 9;
        System.arraycopy(pointData, p1, bihSwapTmp, 0, 9);
        System.arraycopy(pointData, p2, pointData, p1, 9);
        System.arraycopy(bihSwapTmp, 0, pointData, p2, 9);
        int tmp2 = triIndices[index1];
        triIndices[index1] = triIndices[index2];
        triIndices[index2] = tmp2;
    }

    private int collideWithRay(Ray r, Matrix4f worldMatrix, BoundingVolume worldBound, CollisionResults results) {
        boundResults.clear();
        worldBound.collideWith(r, boundResults);
        if (boundResults.size() > 0) {
            float tMin = boundResults.getClosestCollision().getDistance();
            float tMax = boundResults.getFarthestCollision().getDistance();
            if (tMax <= 0) {
                tMax = Float.POSITIVE_INFINITY;
            } else if (tMin == tMax) {
                tMin = 0;
            }
            if (tMin <= 0) {
                tMin = 0;
            }
            if (r.getLimit() < Float.POSITIVE_INFINITY) {
                tMax = Math.min(tMax, r.getLimit());
                if (tMin > tMax) {
                    return 0;
                }
            }
            return root.intersectWhere(r, worldMatrix, this, tMin, tMax, results);
        }
        return 0;
    }

    private int collideWithBoundingVolume(BoundingVolume bv, Matrix4f worldMatrix, CollisionResults results) {
        BoundingBox bbox;
        if (bv instanceof BoundingSphere) {
            BoundingSphere sphere = (BoundingSphere) bv;
            bbox = new BoundingBox(bv.getCenter().clone(), sphere.getRadius(), sphere.getRadius(), sphere.getRadius());
        } else if (bv instanceof BoundingBox) {
            bbox = new BoundingBox((BoundingBox) bv);
        } else {
            throw new UnsupportedCollisionException();
        }
        bbox.transform(worldMatrix.invert(), bbox);
        return root.intersectWhere(bv, bbox, worldMatrix, this, results);
    }

    public int collideWith(Collidable other, Matrix4f worldMatrix, BoundingVolume worldBound, CollisionResults results) {
        if (other instanceof Ray) {
            Ray ray = (Ray) other;
            return collideWithRay(ray, worldMatrix, worldBound, results);
        } else if (other instanceof BoundingVolume) {
            BoundingVolume bv = (BoundingVolume) other;
            return collideWithBoundingVolume(bv, worldMatrix, results);
        } else {
            throw new UnsupportedCollisionException();
        }
    }

    public void write(JmeExporter ex) throws IOException {
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(mesh, "mesh", null);
        oc.write(root, "root", null);
        oc.write(maxTrisPerNode, "tris_per_node", 0);
        oc.write(pointData, "points", null);
        oc.write(triIndices, "indices", null);
    }

    public void read(JmeImporter im) throws IOException {
        InputCapsule ic = im.getCapsule(this);
        mesh = (Mesh) ic.readSavable("mesh", null);
        root = (BIHNode) ic.readSavable("root", null);
        maxTrisPerNode = ic.readInt("tris_per_node", 0);
        pointData = ic.readFloatArray("points", null);
        triIndices = ic.readIntArray("indices", null);
    }
}
