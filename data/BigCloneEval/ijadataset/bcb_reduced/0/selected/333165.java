package artofillusion.object;

import artofillusion.*;
import artofillusion.animation.*;
import artofillusion.math.*;
import artofillusion.ui.*;
import buoy.widget.*;
import java.io.*;

/** The Curve class represents a continuous curve defined by a series of control vertices. 
    It may be either open or closed, and may either interpolate or approximate the control
    vertices.  There is also a smoothness parameter associated with each vertex. */
public class CurveF3d extends Object3D implements Mesh {

    MeshVertex vertex[];

    float smoothness[];

    boolean closed;

    BoundingBox bounds;

    WireframeMesh cachedWire;

    public CurveF3d(Vec3 v[], boolean isClosed) {
        int i;
        vertex = new MeshVertex[v.length];
        for (i = 0; i < v.length; i++) vertex[i] = new MeshVertex(v[i]);
        closed = isClosed;
    }

    public Object3D duplicate() {
        Vec3 v[] = new Vec3[vertex.length];
        float s[] = new float[vertex.length];
        for (int i = 0; i < vertex.length; i++) {
            v[i] = new Vec3(vertex[i].r);
        }
        return new CurveF3d(v, closed);
    }

    public void copyObject(Object3D obj) {
        CurveF3d cv = (CurveF3d) obj;
        MeshVertex v[] = cv.getVertices();
        vertex = new MeshVertex[v.length];
        for (int i = 0; i < vertex.length; i++) {
            vertex[i] = new MeshVertex(new Vec3(v[i].r));
        }
        setClosed(cv.closed);
        clearCachedMesh();
    }

    protected void findBounds() {
        double minx, miny, minz, maxx, maxy, maxz;
        Vec3 v, points[];
        int i;
        getWireframeMesh();
        points = cachedWire.vert;
        minx = maxx = points[0].x;
        miny = maxy = points[0].y;
        minz = maxz = points[0].z;
        for (i = 1; i < points.length; i++) {
            if (points[i] == null) continue;
            v = points[i];
            if (v.x < minx) minx = v.x;
            if (v.x > maxx) maxx = v.x;
            if (v.y < miny) miny = v.y;
            if (v.y > maxy) maxy = v.y;
            if (v.z < minz) minz = v.z;
            if (v.z > maxz) maxz = v.z;
        }
        bounds = new BoundingBox(minx, maxx, miny, maxy, minz, maxz);
    }

    public BoundingBox getBounds() {
        if (bounds == null) findBounds();
        return bounds;
    }

    public MeshVertex[] getVertices() {
        return vertex;
    }

    public void movePoint(int which, Vec3 pos) {
        vertex[which].r = pos;
        clearCachedMesh();
    }

    /** Get a list of the positions of all vertices which define the mesh. */
    public Vec3[] getVertexPositions() {
        Vec3 v[] = new Vec3[vertex.length];
        for (int i = 0; i < v.length; i++) v[i] = new Vec3(vertex[i].r);
        return v;
    }

    public void setVertexPositions(Vec3 v[]) {
        for (int i = 0; i < v.length; i++) vertex[i].r = v[i];
        clearCachedMesh();
    }

    public void setShape(Vec3 v[], float smoothness[]) {
        if (v.length != vertex.length) vertex = new MeshVertex[v.length];
        for (int i = 0; i < v.length; i++) vertex[i] = new MeshVertex(v[i]);
        this.smoothness = smoothness;
        clearCachedMesh();
    }

    public void setClosed(boolean isClosed) {
        closed = isClosed;
        clearCachedMesh();
    }

    public boolean isClosed() {
        return closed;
    }

    public void setSize(double xsize, double ysize, double zsize) {
        Vec3 size = bounds.getSize();
        double xscale, yscale, zscale;
        if (size.x == 0.0) xscale = 1.0; else xscale = xsize / size.x;
        if (size.y == 0.0) yscale = 1.0; else yscale = ysize / size.y;
        if (size.z == 0.0) zscale = 1.0; else zscale = zsize / size.z;
        for (int i = 0; i < vertex.length; i++) {
            vertex[i].r.x *= xscale;
            vertex[i].r.y *= yscale;
            vertex[i].r.z *= zscale;
        }
        clearCachedMesh();
    }

    /** Clear the cached mesh. */
    protected void clearCachedMesh() {
        cachedWire = null;
        bounds = null;
    }

    public WireframeMesh getWireframeMesh() {
        int i, from[], to[];
        CurveF3d subdiv;
        Vec3 vert[];
        if (cachedWire != null) return cachedWire;
        subdiv = this;
        vert = new Vec3[subdiv.vertex.length];
        for (i = 0; i < vert.length; i++) vert[i] = subdiv.vertex[i].r;
        if (closed) {
            from = new int[vert.length];
            to = new int[vert.length];
            from[vert.length - 1] = vert.length - 1;
            to[vert.length - 1] = 0;
        } else {
            from = new int[vert.length - 1];
            to = new int[vert.length - 1];
        }
        for (i = 0; i < vert.length - 1; i++) {
            from[i] = i;
            to[i] = i + 1;
        }
        return (cachedWire = new WireframeMesh(vert, from, to));
    }

    /** Return a new Curve object which has been subdivided once to give a finer approximation of the curve shape. */
    public CurveF3d subdivideCurve() {
        if (vertex.length < 2) return (CurveF3d) duplicate();
        if (vertex.length == 2) {
            Vec3 newpos[] = new Vec3[] { new Vec3(vertex[0].r), vertex[0].r.plus(vertex[1].r).times(0.5), new Vec3(vertex[1].r) };
            return new CurveF3d(newpos, closed);
        }
        Vec3 v[] = new Vec3[vertex.length];
        for (int i = 0; i < v.length; i++) v[i] = new Vec3(vertex[i].r);
        Vec3 newpos[];
        float news[];
        int i, j;
        newpos = new Vec3[v.length * 2];
        newpos[0] = v[0];
        for (i = 2, j = 1; i < newpos.length; i++) {
            if (i % 2 == 0) newpos[i] = v[j]; else {
                j++;
            }
        }
        return new CurveF3d(newpos, closed);
    }

    /** Return a new Curve object which has been subdivided the specified number of times to give a finer approximation of
      the curve shape. */
    public CurveF3d subdivideCurve(int times) {
        CurveF3d c = this;
        for (int i = 0; i < times; i++) c = c.subdivideCurve();
        return c;
    }

    public static Vec3 calcInterpPoint(Vec3 v[], float s[], int i, int j, int k, int m) {
        double w1, w2, w3, w4;
        w1 = -0.0625 * s[j];
        w2 = 0.5 - w1;
        w4 = -0.0625 * s[k];
        w3 = 0.5 - w4;
        return new Vec3(w1 * v[i].x + w2 * v[j].x + w3 * v[k].x + w4 * v[m].x, w1 * v[i].y + w2 * v[j].y + w3 * v[k].y + w4 * v[m].y, w1 * v[i].z + w2 * v[j].z + w3 * v[k].z + w4 * v[m].z);
    }

    public static Vec3 calcApproxPoint(Vec3 v[], float s[], int i, int j, int k) {
        double w1 = 0.125 * s[j], w2 = 1.0 - 2.0 * w1;
        return new Vec3(w1 * v[i].x + w2 * v[j].x + w1 * v[k].x, w1 * v[i].y + w2 * v[j].y + w1 * v[k].y, w1 * v[i].z + w2 * v[j].z + w1 * v[k].z);
    }

    public boolean canSetTexture() {
        return false;
    }

    public int canConvertToTriangleMesh() {
        if (closed) return EXACTLY;
        return CANT_CONVERT;
    }

    public TriangleMesh convertToTriangleMesh(double tol) {
        TriangleMesh mesh = triangulateCurve();
        if (mesh != null) mesh = TriangleMesh.optimizeMesh(mesh);
        return mesh;
    }

    private TriangleMesh triangulateCurve() {
        Vec3 v[] = new Vec3[vertex.length], size = getBounds().getSize();
        Vec2 v2[] = new Vec2[vertex.length];
        int i, j, current, count, min;
        int index[] = new int[vertex.length], faces[][] = new int[vertex.length - 2][3];
        double dir, dir2;
        boolean inside;
        if (size.x > size.y) {
            if (size.y > size.z) j = 2; else j = 1;
        } else {
            if (size.x > size.z) j = 2; else j = 0;
        }
        for (i = 0; i < vertex.length; i++) {
            v[i] = vertex[i].r;
            v2[i] = vertex[i].r.dropAxis(j);
        }
        min = 0;
        for (i = 1; i < v2.length; i++) {
            if (v2[i].x < v2[min].x) min = i;
        }
        for (i = 0; i < index.length; i++) index[i] = i;
        current = min;
        do {
            dir = triangleDirection(v2, index, v2.length, current);
            if (dir == 0.0) {
                current = (current + 1) % index.length;
                if (current == min) return null;
            }
        } while (dir == 0.0);
        count = index.length;
        for (i = 0; i < vertex.length - 2; i++) {
            j = current;
            do {
                dir2 = triangleDirection(v2, index, count, current);
                inside = containsPoints(v2, index, count, current);
                if (dir2 * dir < 0.0 || inside) {
                    current = (current + 1) % count;
                    if (current == j) return null;
                }
            } while (dir2 * dir < 0.0 || inside);
            if (current == 0) faces[i][0] = index[count - 1]; else faces[i][0] = index[current - 1];
            faces[i][1] = index[current];
            if (current == count - 1) faces[i][2] = index[0]; else faces[i][2] = index[current + 1];
            for (j = current; j < count - 1; j++) index[j] = index[j + 1];
            count--;
            current = (current + 1) % count;
        }
        TriangleMesh mesh = new TriangleMesh(v, faces);
        TriangleMesh.Vertex vert[] = (TriangleMesh.Vertex[]) mesh.getVertices();
        return mesh;
    }

    double triangleDirection(Vec2 v2[], int index[], int count, int which) {
        Vec2 va, vb;
        if (which == 0) va = v2[index[which]].minus(v2[index[count - 1]]); else va = v2[index[which]].minus(v2[index[which - 1]]);
        if (which == count - 1) vb = v2[index[which]].minus(v2[index[0]]); else vb = v2[index[which]].minus(v2[index[which + 1]]);
        return va.cross(vb);
    }

    boolean containsPoints(Vec2 v2[], int index[], int count, int which) {
        Vec2 va, vb, v;
        double a, b, c;
        int i, prev, next;
        if (which == 0) prev = count - 1; else prev = which - 1;
        if (which == count - 1) next = 0; else next = which + 1;
        va = v2[index[which]].minus(v2[index[prev]]);
        vb = v2[index[which]].minus(v2[index[next]]);
        a = va.cross(vb);
        va.scale(1.0 / a);
        vb.scale(1.0 / a);
        for (i = 0; i < count; i++) if (i != prev && i != which && i != next) {
            v = v2[index[i]].minus(v2[index[which]]);
            b = vb.cross(v);
            c = v.cross(va);
            a = 1 - b - c;
            if (a >= 0.0 && a <= 1.0 && b >= 0.0 && b <= 1.0 && c >= 0.0 && c <= 1.0) return true;
        }
        return false;
    }

    public Vec3[] getNormals() {
        return null;
    }

    public boolean isEditable() {
        return true;
    }

    public Skeleton getSkeleton() {
        return null;
    }

    public void setSkeleton(Skeleton s) {
    }

    public void edit(EditingWindow parent, ObjectInfo info, Runnable cb) {
        CurveEditorWindow ed = new CurveEditorWindow(parent, "Curve object '" + info.name + "'", info, cb);
        ed.setVisible(true);
    }

    /** Get a MeshViewer which can be used for viewing this mesh. */
    public MeshViewer createMeshViewer(MeshEditController controller, RowContainer options) {
        return new CurveViewer(controller, options);
    }

    public CurveF3d(DataInputStream in, Scene theScene) throws IOException, InvalidObjectException {
        super(in, theScene);
        int i;
        short version = in.readShort();
        if (version != 0) throw new InvalidObjectException("");
        vertex = new MeshVertex[in.readInt()];
        for (i = 0; i < vertex.length; i++) {
            vertex[i] = new MeshVertex(new Vec3(in));
        }
        closed = in.readBoolean();
    }

    public void writeToFile(DataOutputStream out, Scene theScene) throws IOException {
        super.writeToFile(out, theScene);
        int i;
        out.writeShort(0);
        out.writeInt(vertex.length);
        for (i = 0; i < vertex.length; i++) {
            vertex[i].r.writeToFile(out);
        }
        out.writeBoolean(closed);
    }

    public Keyframe getPoseKeyframe() {
        return new NullKeyframe();
    }

    public void applyPoseKeyframe(Keyframe k) {
    }
}
