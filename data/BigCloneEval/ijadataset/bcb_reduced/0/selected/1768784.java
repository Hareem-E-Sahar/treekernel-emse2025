package homura.hde.main.core.scene.shape;

import homura.hde.core.scene.Line;
import homura.hde.core.scene.TriMesh;
import homura.hde.util.geom.BufferUtils;
import homura.hde.util.maths.Quaternion;
import homura.hde.util.maths.Vector3f;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * An extrusion of a 2D object ({@link Line}) along a path (List of Vector3f).
 * Either a convenience constructor can be used or the {@link #updateGeometry} method.
 * It is also capable of doing a cubic spline interpolation for a list of supporting points
 *
 * @author Irrisor
 */
public class Extrusion extends TriMesh {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    /**
     * Default Constructor. Creates an empty Extrusion.
     *
     * @see #updateGeometry(Line, List, Vector3f)
     * @see #updateGeometry(Line, List, boolean, Vector3f)
     * @see #updateGeometry(Line, List, int, Vector3f)
     * @see #updateGeometry(Line, List, int, boolean, Vector3f)
     */
    public Extrusion() {
    }

    /**
     * Creates an empty named Extrusion.
     *
     * @param name name
     * @see #updateGeometry(Line, List, Vector3f)
     * @see #updateGeometry(Line, List, boolean, Vector3f)
     * @see #updateGeometry(Line, List, int, Vector3f)
     * @see #updateGeometry(Line, List, int, boolean, Vector3f)
     */
    public Extrusion(String name) {
        super(name);
    }

    /**
     * Convenience constructor. Calls {@link #updateGeometry(Line, List, Vector3f)}.
     *
     * @param shape see {@link #updateGeometry(Line, List, Vector3f)}
     * @param path  see {@link #updateGeometry(Line, List, Vector3f)}
     * @param up up vector
     */
    public Extrusion(Line shape, List<Vector3f> path, Vector3f up) {
        updateGeometry(shape, path, up);
    }

    /**
     * Convenience constructor. Sets the name and calls {@link #updateGeometry(Line, List, Vector3f)}.
     *
     * @param name  name
     * @param shape see {@link #updateGeometry(Line, List, Vector3f)}
     * @param path  see {@link #updateGeometry(Line, List, Vector3f)}
     * @param up up vector
     */
    public Extrusion(String name, Line shape, List<Vector3f> path, Vector3f up) {
        super(name);
        updateGeometry(shape, path, up);
    }

    /**
     * Update vertex, color, index and texture buffers (0) to contain an extrusion of shape along path.
     *
     * @param shape an instance of Line that describes the 2D shape
     * @param path  a list of vectors that describe the path the shape should be extruded
     * @param up up vector
     */
    public void updateGeometry(Line shape, List<Vector3f> path, Vector3f up) {
        updateGeometry(shape, path, false, up);
    }

    /**
     * Update vertex, color, index and texture buffers (0) to contain an extrusion of shape along path.
     *
     * @param shape an instance of Line that describes the 2D shape
     * @param path  a list of vectors that describe the path the shape should be extruded
     * @param closed true to connect first and last point
     * @param up up vector
     */
    public void updateGeometry(Line shape, List<Vector3f> path, boolean closed, Vector3f up) {
        FloatBuffer shapeBuffer = shape.getVertexBuffer(0);
        FloatBuffer shapeNormalBuffer = shape.getNormalBuffer(0);
        FloatBuffer vertices;
        FloatBuffer normals;
        int numVertices = path.size() * shapeBuffer.limit();
        if (getVertexBuffer(0) != null && getVertexBuffer(0).limit() == numVertices) {
            vertices = getVertexBuffer(0);
            normals = getNormalBuffer(0);
            vertices.rewind();
            normals.rewind();
        } else {
            vertices = BufferUtils.createFloatBuffer(numVertices);
            normals = BufferUtils.createFloatBuffer(numVertices);
        }
        int numIndices = (path.size() - 1) * 2 * shapeBuffer.limit();
        IntBuffer indices;
        if (getIndexBuffer(0) != null && getIndexBuffer(0).limit() == numIndices) {
            indices = getIndexBuffer(0);
            indices.rewind();
        } else {
            indices = BufferUtils.createIntBuffer(numIndices);
        }
        int shapeVertices = shapeBuffer.limit() / 3;
        Vector3f vector = new Vector3f();
        Vector3f direction = new Vector3f();
        Quaternion rotation = new Quaternion();
        for (int i = 0; i < path.size(); i++) {
            Vector3f point = path.get(i);
            shapeBuffer.rewind();
            shapeNormalBuffer.rewind();
            int shapeVertice = 0;
            do {
                Vector3f nextPoint = i < path.size() - 1 ? path.get(i + 1) : closed ? path.get(0) : null;
                Vector3f lastPoint = i > 0 ? path.get(i - 1) : null;
                if (nextPoint != null) {
                    direction.set(nextPoint).subtractLocal(point);
                } else {
                    direction.set(point).subtractLocal(lastPoint);
                }
                rotation.lookAt(direction, up);
                vector.set(shapeNormalBuffer.get(), shapeNormalBuffer.get(), shapeNormalBuffer.get());
                rotation.multLocal(vector);
                normals.put(vector.x);
                normals.put(vector.y);
                normals.put(vector.z);
                vector.set(shapeBuffer.get(), shapeBuffer.get(), shapeBuffer.get());
                rotation.multLocal(vector);
                vector.addLocal(point);
                vertices.put(vector.x);
                vertices.put(vector.y);
                vertices.put(vector.z);
                if ((shapeVertice & 1) == 0) {
                    if (i < path.size() - 1) {
                        indices.put(i * shapeVertices + shapeVertice);
                        indices.put(i * shapeVertices + shapeVertice + 1);
                        indices.put((i + 1) * shapeVertices + shapeVertice);
                        indices.put((i + 1) * shapeVertices + shapeVertice + 1);
                        indices.put((i + 1) * shapeVertices + shapeVertice);
                        indices.put(i * shapeVertices + shapeVertice + 1);
                    } else if (closed) {
                        indices.put(i * shapeVertices + shapeVertice);
                        indices.put(i * shapeVertices + shapeVertice + 1);
                        indices.put(0 + shapeVertice);
                        indices.put(0 + shapeVertice + 1);
                        indices.put(0 + shapeVertice);
                        indices.put(i * shapeVertices + shapeVertice + 1);
                    }
                }
                shapeVertice++;
            } while (shapeBuffer.hasRemaining());
        }
        setVertexBuffer(0, vertices);
        setNormalBuffer(0, normals);
        setIndexBuffer(0, indices);
    }

    /**
     * Performs cubic spline interpolation to find a path through the supporting points where the second derivative is
     * zero. Then calls {@link #updateGeometry(Line, List, Vector3f)} with this
     * path.
     *
     * @param shape    an instance of Line that describes the 2D shape
     * @param points   a list of supporting points for the spline interpolation
     * @param segments number of resulting path segments per supporting point
     * @param up up vector
     */
    public void updateGeometry(Line shape, List<Vector3f> points, int segments, Vector3f up) {
        updateGeometry(shape, points, segments, false, up);
    }

    /**
     * Performs cubic spline interpolation to find a path through the supporting points where the second derivative is
     * zero. Then calls {@link #updateGeometry(Line, List, boolean, Vector3f)} with this
     * path.
     *
     * @param shape    an instance of Line that describes the 2D shape
     * @param points   a list of supporting points for the spline interpolation
     * @param segments number of resulting path segments per supporting point
     * @param closed   true to close the shape (connect last and first point)
     * @param up up vector
     */
    public void updateGeometry(Line shape, List<Vector3f> points, int segments, boolean closed, Vector3f up) {
        int np = points.size();
        if (closed) {
            np = np + 3;
        }
        float d[][] = new float[3][np];
        float x[] = new float[np];
        List<Vector3f> path = new ArrayList<Vector3f>();
        for (int i = 0; i < np; i++) {
            Vector3f p;
            if (!closed) {
                p = points.get(i);
            } else {
                if (i == 0) {
                    p = points.get(points.size() - 1);
                } else if (i >= np - 2) {
                    p = points.get(i - np + 2);
                } else {
                    p = points.get(i - 1);
                }
            }
            x[i] = i;
            d[0][i] = p.x;
            d[1][i] = p.y;
            d[2][i] = p.z;
        }
        if (np > 1) {
            float[][] a = new float[3][np];
            float h[] = new float[np];
            for (int i = 1; i <= np - 1; i++) {
                h[i] = x[i] - x[i - 1];
            }
            if (np > 2) {
                float sub[] = new float[np - 1];
                float diag[] = new float[np - 1];
                float sup[] = new float[np - 1];
                for (int i = 1; i <= np - 2; i++) {
                    diag[i] = (h[i] + h[i + 1]) / 3;
                    sup[i] = h[i + 1] / 6;
                    sub[i] = h[i] / 6;
                    for (int dim = 0; dim < 3; dim++) {
                        a[dim][i] = (d[dim][i + 1] - d[dim][i]) / h[i + 1] - (d[dim][i] - d[dim][i - 1]) / h[i];
                    }
                }
                for (int dim = 0; dim < 3; dim++) {
                    solveTridiag(sub.clone(), diag.clone(), sup.clone(), a[dim], np - 2);
                }
            }
            if (!closed) {
                path.add(new Vector3f(d[0][0], d[1][0], d[2][0]));
            }
            float[] point = new float[3];
            for (int i = closed ? 2 : 1; i <= np - 2; i++) {
                for (int j = 1; j <= segments; j++) {
                    for (int dim = 0; dim < 3; dim++) {
                        float t1 = (h[i] * j) / segments;
                        float t2 = h[i] - t1;
                        float v = ((-a[dim][i - 1] / 6 * (t2 + h[i]) * t1 + d[dim][i - 1]) * t2 + (-a[dim][i] / 6 * (t1 + h[i]) * t2 + d[dim][i]) * t1) / h[i];
                        point[dim] = v;
                    }
                    path.add(new Vector3f(point[0], point[1], point[2]));
                }
            }
        }
        this.updateGeometry(shape, path, closed, up);
    }

    private static void solveTridiag(float sub[], float diag[], float sup[], float b[], int n) {
        for (int i = 2; i <= n; i++) {
            sub[i] = sub[i] / diag[i - 1];
            diag[i] = diag[i] - sub[i] * sup[i - 1];
            b[i] = b[i] - sub[i] * b[i - 1];
        }
        b[n] = b[n] / diag[n];
        for (int i = n - 1; i >= 1; i--) {
            b[i] = (b[i] - sup[i] * b[i + 1]) / diag[i];
        }
    }
}
