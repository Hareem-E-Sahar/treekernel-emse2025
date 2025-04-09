package parts;

import java.util.*;
import java.io.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import com.sun.j3d.utils.geometry.*;
import edit.Environment;
import edit.Palette;

public class NURB {

    public static final int MAX_CURVE_SEGMENTS = 100;

    public static final int MAX_CONTROL_POINTS = 32;

    public static final int MAX_ORDER = 4;

    protected Point3d[] controlPoints = new Point3d[MAX_CONTROL_POINTS];

    protected float[] weights = new float[MAX_CONTROL_POINTS];

    protected float[] knots = new float[MAX_CONTROL_POINTS + MAX_ORDER];

    protected int order = 3;

    protected int numControlPoints = 0;

    protected int stepsPerCntrlPt = 5;

    protected double[] curveCoords = new double[(MAX_CURVE_SEGMENTS + 1) * 3];

    protected int numCurvePoints = 0;

    protected Point3d curvePoint = new Point3d();

    private PartDefFlexiConnector partDef;

    private NURBGeometry[] geometry = new NURBGeometry[2];

    private int numGeometries = 0;

    public final double POINT_X1 = -48.0;

    public final double POINT_Y1 = 0.0;

    public final double POINT_Z1 = 0.0;

    public final double POINT_X2 = 48.0;

    public final double POINT_Y2 = 24.0;

    public final double POINT_Z2 = 0.0;

    public final double POINT_X3 = -48.0;

    public final double POINT_Y3 = 48.0;

    public final double POINT_Z3 = 0.0;

    public NURB(PartDefFlexiConnector partDef) {
        this.partDef = partDef;
        for (int i = 0; i < MAX_CONTROL_POINTS; i++) {
            controlPoints[i] = new Point3d();
        }
        setDefaultNURB();
    }

    public NURBCurve createCurveGeometry() {
        NURBCurve curve = new NURBCurve(partDef, this);
        geometry[numGeometries] = curve;
        numGeometries++;
        return curve;
    }

    public NURBSurface createSurfaceGeometry() {
        NURBSurface surface = new NURBSurface(partDef, this);
        geometry[numGeometries] = surface;
        numGeometries++;
        return surface;
    }

    public int numControlPoints() {
        return numControlPoints;
    }

    public void getControlPoint(int pt, Point3d point) {
        point.set(controlPoints[pt]);
    }

    public void getControlPoint(int pt, Vector3d point) {
        point.set(controlPoints[pt]);
    }

    public void setStepsPerControlPoint(int steps) {
        stepsPerCntrlPt = steps;
    }

    public void setControlPoint(int pt, Vector3f loc) {
        setControlPoint(pt, loc.x, loc.y, loc.z);
    }

    public void setControlPoint(int pt, double x, double y, double z) {
        controlPoints[pt].x = x;
        controlPoints[pt].y = y;
        controlPoints[pt].z = z;
        System.out.println("NURB.setControlPoint: " + numGeometries + " geom's");
        generateCurve();
        for (int i = 0; i < numGeometries; i++) {
            geometry[i].update();
        }
    }

    public void setWeight(int i, float x) {
        weights[i] = x;
    }

    public float getWeight(int i) {
        return weights[i];
    }

    public void setKnot(int i, float x) {
        knots[i] = x;
    }

    public float getKnot(int i) {
        return knots[i];
    }

    public int getOrder() {
        return order;
    }

    public int numCurvePoints() {
        return numCurvePoints;
    }

    public void getCurvePoint(int i, Point3d point) {
        point.set(curveCoords[i * 3], curveCoords[i * 3 + 1], curveCoords[i * 3 + 2]);
    }

    public void set(int order, int numCtrlPts, Point3d ctrlPts[], float weights[], float knots[]) {
        this.order = order;
        this.numControlPoints = numCtrlPts;
        numCurvePoints = Math.min((numControlPoints * stepsPerCntrlPt + 1), MAX_CURVE_SEGMENTS + 1);
        int i;
        for (i = 0; i < numControlPoints; i++) {
            controlPoints[i].set(ctrlPts[i]);
            this.weights[i] = weights[i];
        }
        for (i = 0; i < numControlPoints + order; i++) {
            this.knots[i] = knots[i];
        }
        generateCurve();
        for (i = 0; i < numGeometries; i++) {
            geometry[i].updateGeometry();
        }
    }

    protected void setDefaultNURB() {
        float[] defaultCoords = Environment.preferences.getFloatArray("DefaultNURBControlPoints");
        if (defaultCoords == null) {
            setFallbackControlPoints();
        } else {
            numControlPoints = (int) (java.lang.reflect.Array.getLength(defaultCoords) / 3);
            for (int i = 0; i < numControlPoints; i++) {
                controlPoints[i].set(defaultCoords[(i * 3)], defaultCoords[(i * 3) + 1], defaultCoords[(i * 3) + 2]);
            }
        }
        numCurvePoints = Math.min((numControlPoints * stepsPerCntrlPt + 1), MAX_CURVE_SEGMENTS + 1);
        setBernsteinPolynomialsKnots();
        setBasicWeights();
        generateCurve();
    }

    protected void setFallbackControlPoints() {
        numControlPoints = 3;
        controlPoints[0].set(POINT_X1, POINT_Y1, POINT_Z1);
        controlPoints[1].set(POINT_X2, POINT_Y2, POINT_Z2);
        controlPoints[2].set(POINT_X3, POINT_Y3, POINT_Z3);
    }

    public void generateCurve() {
        float maxU = knots[order + numControlPoints - 1];
        float step = maxU / (numCurvePoints - 1);
        float u;
        int pt;
        for (u = 0, pt = 0; u < maxU + step; u += step, pt += 3) {
            curvePoint(Math.min(maxU, u), curvePoint);
            curveCoords[pt] = curvePoint.x;
            curveCoords[pt + 1] = curvePoint.y;
            curveCoords[pt + 2] = curvePoint.z;
        }
    }

    public void setBernsteinPolynomialsKnots() {
        int i, z;
        z = numControlPoints + order;
        for (i = 0; i < order; i++) setKnot(i, 0.0f);
        for (i = order; i < numControlPoints; i++) setKnot(i, (float) (i - (order - 1)) / (float) (numControlPoints - (order - 1)));
        for (i = numControlPoints; i < z; i++) setKnot(i, 1.0f);
    }

    public void setBasicWeights() {
        for (int i = 0; i < numControlPoints; i++) setWeight(i, 1.0f);
    }

    private int findSpan(float u) {
        int low, high, mid;
        if (u >= knots[numControlPoints]) return (numControlPoints - 1);
        low = order - 1;
        high = numControlPoints;
        mid = (low + high) / 2;
        while (u < knots[mid] || u >= knots[mid + 1]) {
            if (u < knots[mid]) high = mid; else low = mid;
            mid = (low + high) / 2;
        }
        return (mid);
    }

    private float[] basisFuncs(int i, float u) {
        float[] N = new float[order];
        float[] left = new float[order];
        float[] right = new float[order];
        float saved, temp;
        N[0] = 1.0f;
        for (int j = 1; j < order; j++) {
            left[j] = u - knots[i + 1 - j];
            right[j] = knots[i + j] - u;
            saved = 0.0f;
            for (int r = 0; r < j; r++) {
                temp = N[r] / (right[r + 1] + left[j - r]);
                N[r] = saved + right[r + 1] * temp;
                saved = left[j - r] * temp;
            }
            N[j] = saved;
        }
        return N;
    }

    public void curvePoint(float u, Point3d point) {
        int i, t;
        int span;
        t = numControlPoints + order;
        span = findSpan(u);
        float[] N = basisFuncs(span, u);
        int ci;
        double x = 0.0f;
        double y = 0.0f;
        double z = 0.0f;
        for (i = 0; i < order; i++) {
            ci = span - (order - 1) + i;
            x += N[i] * controlPoints[ci].x / weights[ci];
            y += N[i] * controlPoints[ci].y / weights[ci];
            z += N[i] * controlPoints[ci].z / weights[ci];
        }
        point.set(x, y, z);
    }
}
