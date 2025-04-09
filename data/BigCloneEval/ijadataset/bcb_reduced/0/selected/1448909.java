package com.turboconnard.display;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.opengles.GL10;
import com.ladydinde.R;
import com.turboconnard.geom.Point3D;
import com.turboconnard.tools.Textures;

public class Ribbon extends Stroke3D {

    private float[] textures;

    /** The initial indices definition */
    private byte indices[];

    private float _height;

    private float STROKE_MAX = 40;

    private float STROKE_MIN = 5;

    private float DIST_MAX = 120;

    private float DIST_MIN = 10;

    private FloatBuffer textureBuffer;

    private ByteBuffer textByteBuf;

    private ByteBuffer indexBuffer;

    private int texture;

    private float stroke;

    public float scale;

    public Ribbon(float pHeight) {
        super();
        mode = GL10.GL_TRIANGLE_STRIP;
        texture = R.drawable.caisse1;
        stroke = STROKE_MIN + (STROKE_MAX - STROKE_MIN) / 2;
    }

    public void addPoints(float[][] pPoints) {
        points = new float[pPoints.length * 2 * 3];
        int c = 0;
        Point3D oldPoint = null;
        for (int k = 0; k < pPoints.length; k++) {
            Point3D newPoint = new Point3D(pPoints[k][0], pPoints[k][1], pPoints[k][2]);
            if (k == 0) {
                oldPoint = new Point3D(0, 0, 0);
            }
            double a;
            float dist;
            a = Math.atan2(newPoint.y - oldPoint.y, newPoint.x - oldPoint.x);
            dist = (float) Math.sqrt(Math.pow(newPoint.y - oldPoint.y, 2) + Math.pow(newPoint.x - oldPoint.x, 2));
            stroke = 35;
            points[c] = (newPoint.x - ((float) (Math.cos(a + Math.PI / 2) * 4 * stage.correction))) * 2;
            points[c + 1] = -(newPoint.y + ((float) (Math.sin(a - Math.PI / 2) * 4 * stage.correction))) * 2;
            points[c + 2] = 0;
            c += 3;
            points[c] = (newPoint.x - ((float) (Math.cos(a - Math.PI / 2) * 4 * stage.correction))) * 2;
            points[c + 1] = -(newPoint.y + ((float) (Math.sin(a + Math.PI / 2) * 4 * stage.correction))) * 2;
            points[c + 2] = 0;
            c += 3;
            oldPoint = newPoint;
        }
        byteBuf = ByteBuffer.allocateDirect(points.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        vertexBuffer = byteBuf.asFloatBuffer();
        vertexBuffer.clear();
        vertexBuffer.put(points);
        vertexBuffer.position(0);
    }

    @Override
    public void addPoint(float pX, float pY, float pZ) {
        vPoints.add(new Point3D(pX, pY, pZ));
        count++;
        points = new float[count * 6];
        int c = 0;
        int t = 0;
        t = 4;
        Point3D oldPoint = null;
        for (int k = 0; k < vPoints.size(); k++) {
            Point3D newPoint = vPoints.get(k);
            if (k == 0) {
                oldPoint = new Point3D(0, 0, 0);
            }
            double a;
            float dist;
            a = Math.atan2(newPoint.y - oldPoint.y, newPoint.x - oldPoint.x);
            dist = (float) Math.sqrt(Math.pow(newPoint.y - oldPoint.y, 2) + Math.pow(newPoint.x - oldPoint.x, 2));
            stroke = 35;
            points[c] = (newPoint.x - (float) Math.cos(a + Math.PI / 2) * ((STROKE_MAX - stroke) / 2)) * stage.correction;
            points[c + 1] = -(newPoint.y + (float) Math.sin(a - Math.PI / 2) * ((STROKE_MAX - stroke) / 2)) * stage.correction;
            points[c + 2] = newPoint.z * stage.correction;
            c += 3;
            points[c] = (newPoint.x - (float) Math.cos(a - Math.PI / 2) * ((STROKE_MAX - stroke) / 2)) * stage.correction;
            points[c + 1] = -(newPoint.y + (float) Math.sin(a + Math.PI / 2) * (stroke / 2)) * stage.correction;
            points[c + 2] = newPoint.z * stage.correction;
            c += 3;
            t += 4;
            oldPoint = newPoint;
        }
        byteBuf = ByteBuffer.allocateDirect(points.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        vertexBuffer = byteBuf.asFloatBuffer();
        vertexBuffer.clear();
        vertexBuffer.put(points);
        vertexBuffer.position(0);
    }

    @Override
    protected void _draw(GL10 gl) {
        if (points == null) return;
        if (points.length <= 1) return;
        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glFrontFace(GL10.GL_CCW);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
        gl.glDrawArrays(mode, 0, points.length / 3);
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
    }
}
