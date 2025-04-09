package net.sf.jaer.eventprocessing.tracking;

import net.sf.jaer.util.Matrix;
import net.sf.jaer.chip.*;
import net.sf.jaer.eventprocessing.EventFilter2D;
import net.sf.jaer.event.BasicEvent;
import net.sf.jaer.event.EventPacket;
import net.sf.jaer.graphics.*;
import com.sun.opengl.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import net.sf.jaer.eventprocessing.tracking.RectangularClusterTracker.Cluster;
import java.io.*;
import java.util.List;

public class KalmanFilter extends EventFilter2D implements FrameAnnotater, Observer {

    List<Cluster> clusters;

    AEChip chip;

    AEChipRenderer renderer;

    GLUT glut;

    private int nbOfEventsTillTrack = getPrefs().getInt("KalmanFilter.nbOfEventsTillTrack", 100);

    private float distToVanishingPoint = getPrefs().getFloat("KalmanFilter.distToVanishingPoint", 300f);

    private float maxMeasurementVariance = getPrefs().getFloat("KalmanFilter.measurementVariance", 8f);

    private float minProcessVariance = getPrefs().getFloat("KalmanFilter.minProcessVariance", 1f);

    private float bridgeHeight = getPrefs().getFloat("KalmanFilter.bridgeHeight", 5f);

    private boolean mapToRoad = getPrefs().getBoolean("KalmanFilter.mapToRoad", false);

    private int distTo1Px = getPrefs().getInt("KalmanFilter.distTo1Px", 2);

    private boolean feedbackToCluster = getPrefs().getBoolean("KalmanFilter.feedbackToCluster", false);

    private boolean useDynamicVariances = getPrefs().getBoolean("KalmanFilter.useDynamicVariances", true);

    private float processVariance;

    private float measurementVariance;

    private float beta;

    private float cameraAngle;

    private int dimStateVector;

    private int dimMeasurement;

    private RectangularClusterTracker tracker;

    final String nl = System.getProperty("line.separator");

    /** Creates a new instance of KalmanFilter */
    public KalmanFilter(AEChip chip, RectangularClusterTracker tracker) {
        super(chip);
        this.chip = chip;
        renderer = (AEChipRenderer) chip.getRenderer();
        glut = chip.getCanvas().getGlut();
        initFilter();
        chip.addObserver(this);
        this.tracker = tracker;
    }

    /**
     *Initialises the filter and geometry is recalculated
     */
    public void initFilter() {
        beta = (float) Math.atan(bridgeHeight / distTo1Px);
        cameraAngle = beta - (float) Math.atan(bridgeHeight / distToVanishingPoint);
        dimStateVector = 4;
        dimMeasurement = 2;
    }

    LinkedList<Cluster> pruneList = new LinkedList<Cluster>();

    HashMap<Cluster, ClusterData> zombieList = new HashMap<Cluster, ClusterData>();

    HashMap<Cluster, ClusterData> kalmans = new HashMap<Cluster, ClusterData>();

    private int iteratorNbOfEventsTillTrack = 0;

    /**
     * Here a packet of events is processed. First the method checks, if enough events happened to calculate the Kalman Filter.
     * If not, it happens nothing, else for every cluster there is the prediction and update step is done.
     *
     *@param ae A Packet of events to process
     */
    private synchronized void track(EventPacket<? extends BasicEvent> ae) {
        clusters = tracker.getClusters();
        pruneList.addAll(tracker.getPruneList());
        iteratorNbOfEventsTillTrack += ae.getSize();
        if (iteratorNbOfEventsTillTrack >= nbOfEventsTillTrack) {
            iteratorNbOfEventsTillTrack = 0;
        } else {
            return;
        }
        for (Cluster c : pruneList) {
            kalmans.remove(c);
            continue;
        }
        pruneList.clear();
        for (Cluster c : clusters) {
            if (kalmans.containsKey(c)) {
                kalmans.get(c).predict();
                kalmans.get(c).update();
            } else {
                kalmans.put(c, new ClusterData(c));
            }
        }
    }

    public String toString() {
        return "KalmanFilter.toString not yet implemented";
    }

    /**
     *The data class for each Cluster. In this data structure all the necessary matrices for the Kalman Filter is stored. Each
     *cluster is assigned to such a structure. The class provides also the algorithm itself(prediction and update step) of
     *the KF.
     */
    public final class ClusterData {

        Cluster c;

        private int latestTimeStampOfLastStep;

        private float[][] F;

        private float[][] B;

        private float[][] Q;

        private float[][] H;

        private float[][] R;

        private float[][] Pp;

        private float[][] P;

        private float[] x;

        private float[] xp;

        private float[] z;

        private float[] u;

        private float[] y;

        private float deltaTime;

        /**
         * The only constructor. For a Cluster it creates the necessary datastructures.
         *@param c The Cluster for which the data are initialized.
         */
        ClusterData(Cluster c) {
            this.c = c;
            initData();
        }

        /**
         *This method initializes the Kalman Filter for the supported cluster depending on the actual cluster position and
         *the chosen metric(see mapToRoad parameter).
         */
        private void initData() {
            F = new float[dimStateVector][dimStateVector];
            Q = new float[dimStateVector][dimStateVector];
            B = new float[dimStateVector][dimStateVector];
            H = new float[dimMeasurement][dimStateVector];
            R = new float[dimMeasurement][dimMeasurement];
            Pp = new float[dimStateVector][dimStateVector];
            P = new float[dimStateVector][dimStateVector];
            xp = new float[dimStateVector];
            x = new float[dimStateVector];
            z = new float[dimMeasurement];
            u = new float[dimStateVector];
            y = new float[dimMeasurement];
            latestTimeStampOfLastStep = c.getLastEventTimestamp() - c.getLifetime();
            deltaTime = c.getLifetime();
            measurementVariance = 1;
            processVariance = maxMeasurementVariance + 100;
            Matrix.identity(P);
            Matrix.identity(F);
            H[0][0] = 1;
            H[0][1] = 0;
            H[0][2] = 0;
            H[0][3] = 0;
            H[1][0] = 0;
            H[1][1] = 0;
            H[1][2] = 1;
            H[1][3] = 0;
            Matrix.zero(R);
            R[0][0] = measurementVariance;
            R[1][1] = measurementVariance;
            Matrix.zero(Q);
            B[0][0] = 0;
            B[1][1] = 0;
            B[0][1] = 0;
            B[1][0] = 0;
            if (!mapToRoad) {
                x[0] = (float) c.getLocation().getY();
                x[1] = 0;
                x[2] = (float) c.getLocation().getX();
                x[3] = 0;
                z[0] = c.getLocation().y;
                z[1] = c.getLocation().x;
            } else {
                Point2D.Float pointInMeters = distAtPixel(c.getLocation());
                x[0] = pointInMeters.y;
                x[1] = 0;
                x[2] = pointInMeters.x;
                x[3] = 0;
                z[0] = pointInMeters.y;
                z[1] = pointInMeters.x;
            }
        }

        /**
         *The prediction step of the KalmanFilter. The new predicted state vector xp and also the Error cov.Matrix P is
         *calculated.
         */
        void predict() {
            updateVariances();
            deltaTime = (c.getLastEventTimestamp() - latestTimeStampOfLastStep) * 1.e-6f;
            if (c.getLastEventTimestamp() > latestTimeStampOfLastStep) latestTimeStampOfLastStep = c.getLastEventTimestamp();
            F[0][1] = deltaTime;
            F[2][3] = deltaTime;
            B[0][0] = (float) Math.pow(deltaTime, 2) / 2f;
            B[1][1] = deltaTime;
            xp = multMatrix(F, x);
            Q[0][0] = (float) Math.pow(deltaTime, 4) / 4;
            Q[0][1] = (float) Math.pow(deltaTime, 3) / 2;
            Q[1][0] = Q[0][1];
            Q[1][1] = (float) Math.pow(deltaTime, 2);
            Q = multMatrix(Q, processVariance);
            Pp = addMatrix(multMatrix(multMatrix(F, P), transposeMatrix(F)), Q);
        }

        private float mixingFactor = tracker.getMixingFactor();

        private void updateVariances() {
            if (useDynamicVariances) {
                if (processVariance > minProcessVariance) processVariance = (1 - mixingFactor) * processVariance - mixingFactor * minProcessVariance; else processVariance = minProcessVariance;
                if (measurementVariance < maxMeasurementVariance) measurementVariance = (1 - mixingFactor) * measurementVariance + mixingFactor * maxMeasurementVariance; else measurementVariance = maxMeasurementVariance;
            } else {
                processVariance = minProcessVariance;
                measurementVariance = maxMeasurementVariance;
            }
        }

        /**
         *The update step for the Kalman Filter. As measurement the actual position of the supported cluster is taken.
         *Depending on the metric(see map to road) the measurement is first translated in meters.
         */
        void update() {
            if (!mapToRoad) {
                z[0] = c.getLocation().y;
                z[1] = c.getLocation().x;
            } else {
                Point2D.Float pm = distAtPixel(c.getLocation());
                z[0] = pm.y;
                z[1] = pm.x;
            }
            float[] yTemp = new float[dimMeasurement];
            yTemp = multMatrix(H, xp);
            Matrix.subtract(z, yTemp, y);
            R[0][0] = measurementVariance;
            R[1][1] = measurementVariance;
            float[][] S = new float[dimMeasurement][dimMeasurement];
            float[][] STemp = new float[dimMeasurement][dimStateVector];
            float[][] STemp2 = new float[dimMeasurement][dimMeasurement];
            Matrix.multiply(H, Pp, STemp);
            Matrix.multiply(STemp, transposeMatrix(H), STemp2);
            Matrix.add(STemp2, R, S);
            float[][] K = new float[dimStateVector][dimMeasurement];
            float[][] Ktemp = new float[dimStateVector][dimMeasurement];
            Matrix.multiply(Pp, transposeMatrix(H), Ktemp);
            Matrix.invert(S);
            Matrix.multiply(Ktemp, S, K);
            float[][] I = new float[dimStateVector][dimStateVector];
            float[][] Ptemp = new float[dimStateVector][dimStateVector];
            Matrix.identity(I);
            Matrix.multiply(K, H, P);
            Matrix.subtract(I, P, Ptemp);
            Matrix.multiply(Ptemp, Pp, P);
            float[] xTemp = new float[dimStateVector];
            Matrix.multiply(K, y, xTemp);
            Matrix.add(xp, xTemp, x);
            if (feedbackToCluster) {
                if (!mapToRoad) c.setLocation(new Point2D.Float(x[2], x[0])); else c.setLocation(pixelYAtDist(new Point2D.Float(x[2], x[0])));
            }
        }
    }

    ;

    /**
     *This method takes a point p. Depending on parameters(distanceToVanishingPoint, distanceTo1Px, bridgeHeight) and also depending
     *on  if a pixel is set as vanishing point, the distance in meters is calculated. For the x value, the center of the
     *window is assumed to be 0 and the right side the positive one.
     *@param p The pixel for which the distance on the road is calcuated
     *@return How many meters a pixel is away on the road.
     */
    private final Point2D.Float distAtPixel(Point2D.Float p) {
        Point2D.Float r = new Point2D.Float();
        if (!renderer.isPixelSelected()) r.y = bridgeHeight / (float) Math.tan(beta - p.y * (cameraAngle / chip.getSizeY())); else r.y = bridgeHeight / (float) Math.tan(beta - p.y * (cameraAngle / renderer.getYsel()));
        float maxX = (r.y + distTo1Px) * (float) Math.tan(cameraAngle / 2);
        r.x = (maxX / chip.getSizeX() / 2) * (p.x - chip.getSizeX() / 2f);
        return r;
    }

    /**
     *This method calculates which y-coordinate a pixel has, that is a certain distance ( in meters ) away.
     *@param meters The distance in meters
     *@return the y-coordinate of the pixel with that distance.
     */
    private final Point2D.Float pixelYAtDist(Point2D.Float pMeters) {
        Point2D.Float p = new Point2D.Float();
        if (!renderer.isPixelSelected()) p.y = ((beta - (float) (Math.atan(bridgeHeight / pMeters.y))) / (cameraAngle / chip.getMaxSize())); else p.y = (int) ((beta - (float) (Math.atan(bridgeHeight / pMeters.y))) / (cameraAngle / renderer.getYsel()));
        float maxX = (pMeters.y + distTo1Px) * (float) Math.tan(cameraAngle / 2);
        p.x = pMeters.x / (maxX / chip.getSizeX() / 2) + chip.getSizeX() / 2f;
        return p;
    }

    /**
     *This method calculates which y-coordinate a pixel has, that is a certain distance ( in meters ) away.
     *@param meters The distance in meters
     *@return the y-coordinate of the pixel with that distance as a float(exact position)
     */
    private final float floatPixelYAtDist(float meters) {
        if (!renderer.isPixelSelected()) return ((beta - (float) (Math.atan(bridgeHeight / meters))) / (cameraAngle / chip.getMaxSize())); else return ((beta - (float) (Math.atan(bridgeHeight / meters))) / (cameraAngle / renderer.getYsel()));
    }

    /**
     *This is a linear map that maps a cluster at the vanishing point to 0, and at the buttom of the window the result would
     *be 1.
     *@param p Pixel to map
     *@return The linear map from 0 to 1, depending on position p.
     */
    private final float perspectiveScale(Point2D.Float p) {
        if (!renderer.isPixelSelected()) {
            float yfrac = 1f - (p.y / chip.getSizeY());
            return yfrac;
        } else {
            int size = chip.getMaxSize();
            float d = (float) p.distance(renderer.getXsel(), renderer.getYsel());
            float scale = d / size;
            return scale;
        }
    }

    static final float[][] transposeMatrix(float[][] a) {
        int ra = a.length;
        int ca = a[0].length;
        float[][] m = new float[ca][ra];
        for (int i = 0; i < ra; i++) {
            for (int j = 0; j < ca; j++) m[j][i] = a[i][j];
        }
        return m;
    }

    static final float[][] multMatrix(float[][] a, float s) {
        int ra = a.length;
        int ca = a[0].length;
        float[][] m = new float[ra][ca];
        for (int i = 0; i < ra; i++) for (int j = 0; j < ca; j++) m[i][j] = a[i][j] * s;
        return m;
    }

    static final float[][] multMatrix(float[][] a, float[][] b) {
        int ra = a.length;
        int ca = a[0].length;
        int rb = b.length;
        int cb = b[0].length;
        if (ca != rb) {
            System.err.println("Matrix dimensions do not agree");
            return null;
        }
        float[][] m = new float[ra][cb];
        for (int i = 0; i < ra; i++) for (int j = 0; j < cb; j++) {
            m[i][j] = 0;
            for (int k = 0; k < ca; k++) m[i][j] += a[i][k] * b[k][j];
        }
        return m;
    }

    static final float[] multMatrix(float[][] a, float[] x) {
        int ra = a.length;
        int ca = a[0].length;
        if (ca != x.length) {
            System.err.println("Matrix dimensions do not agree");
            return null;
        }
        float[] m = new float[ra];
        for (int i = 0; i < ra; i++) {
            m[i] = 0;
            for (int k = 0; k < ca; k++) m[i] += a[i][k] * x[k];
        }
        return m;
    }

    static final float[][] addMatrix(float[][] a, float[][] b) {
        int ra = a.length;
        int ca = a[0].length;
        int rb = b.length;
        int cb = b[0].length;
        if (ca != cb || ra != rb) {
            System.err.println("Matrix dimensions do not agree");
            return null;
        }
        float[][] m = new float[ra][cb];
        for (int i = 0; i < ra; i++) for (int j = 0; j < cb; j++) m[i][j] = a[i][j] + b[i][j];
        return m;
    }

    static final float[] addVector(float[] a, float[] b) {
        int ra = a.length;
        int rb = b.length;
        if (ra != rb) {
            System.err.println("Vector dimension do not agree.");
            return null;
        }
        float[] m = new float[ra];
        for (int i = 0; i < ra; i++) {
            m[i] = a[i] + b[i];
        }
        return m;
    }

    final void drawFilter(final ClusterData cd, float[][][] fr, Color color) {
        int xp0 = 0;
        int xp2 = 0;
        int x0 = 0;
        int x2 = 0;
        if (!mapToRoad) {
            xp0 = Math.round(cd.xp[0]);
            x0 = Math.round(cd.x[0]);
            xp2 = Math.round(cd.xp[2]);
            x2 = Math.round(cd.x[2]);
        } else {
            Point2D.Float predXInPx = pixelYAtDist(new Point2D.Float(cd.xp[2], cd.xp[0]));
            Point2D.Float xInPx = pixelYAtDist(new Point2D.Float(cd.x[2], cd.x[0]));
            xp0 = Math.round(predXInPx.y);
            x0 = Math.round(xInPx.y);
            xp2 = Math.round(predXInPx.x);
            x2 = Math.round(xInPx.x);
        }
        colorPixel(x2, x0, fr, color);
        colorPixel(xp2 - 1, xp0, fr, color);
        colorPixel(xp2 + 1, xp0, fr, color);
        colorPixel(xp2, xp0 - 1, fr, color);
        colorPixel(xp2, xp0 + 1, fr, color);
    }

    static final int clusterColorChannel = 2;

    /** @param x x location of pixel
     *@param y y location
     *@param fr the frame data
     *@param channel the RGB channel number 0-2
     *@param brightness the brightness 0-1
     */
    final void colorPixel(final int x, final int y, final float[][][] fr, Color color) {
        if (y < 0 || y > fr.length - 1 || x < 0 || x > fr[0].length - 1) return;
        float[] rgb = color.getRGBColorComponents(null);
        float[] f = fr[y][x];
        for (int i = 0; i < 3; i++) {
            f[i] = rgb[i];
        }
    }

    public Object getFilterState() {
        return null;
    }

    public boolean isGeneratingFilter() {
        return false;
    }

    public synchronized void resetFilter() {
        kalmans.clear();
    }

    public EventPacket filterPacket(EventPacket in) {
        if (in == null) return null;
        if (!filterEnabled) return in;
        if (enclosedFilter != null) in = enclosedFilter.filterPacket(in);
        track(in);
        return in;
    }

    public void update(Observable o, Object arg) {
        initFilter();
    }

    public void annotate(Graphics2D g) {
    }

    public synchronized void annotate(GLAutoDrawable drawable) {
        if (kalmans == null) return;
        final float LINE_WIDTH = 1f;
        GL gl = drawable.getGL();
        if (!isFilterEnabled()) return;
        float[] rgb = new float[4];
        float x, y;
        int font = GLUT.BITMAP_HELVETICA_12;
        gl.glPushMatrix();
        {
            for (Cluster c : kalmans.keySet()) {
                if (c.isVisible()) {
                    if (!mapToRoad) {
                        x = kalmans.get(c).x[2];
                        y = kalmans.get(c).x[0];
                    } else {
                        Point2D.Float xInPx = pixelYAtDist(new Point2D.Float(kalmans.get(c).x[2], kalmans.get(c).x[0]));
                        x = xInPx.x;
                        y = xInPx.y;
                    }
                    c.getColor().getRGBComponents(rgb);
                    gl.glColor3fv(rgb, 0);
                    gl.glLineWidth(LINE_WIDTH);
                    gl.glBegin(GL.GL_LINES);
                    {
                        gl.glVertex2f(x - 1f, y);
                        gl.glVertex2f(x + 1f, y);
                        gl.glVertex2f(x, y - 1f);
                        gl.glVertex2f(x, y + 1f);
                    }
                    gl.glEnd();
                    if (!mapToRoad) {
                        x = kalmans.get(c).xp[2];
                        y = kalmans.get(c).xp[0];
                    } else {
                        Point2D.Float xInPx = pixelYAtDist(new Point2D.Float(kalmans.get(c).xp[2], kalmans.get(c).xp[0]));
                        x = xInPx.x;
                        y = xInPx.y;
                    }
                    gl.glBegin(GL.GL_LINE_STRIP);
                    {
                        gl.glVertex2f(x + 2f, y);
                        gl.glVertex2f(x, y - 2f);
                        gl.glVertex2f(x - 2f, y);
                        gl.glVertex2f(x, y + 2f);
                        gl.glVertex2f(x + 2f, y);
                    }
                    gl.glEnd();
                    gl.glRasterPos3f(x + 2, y + 2, 0);
                    glut.glutBitmapString(font, String.format("v(y) = %.1f", kalmans.get(c).x[1]));
                    gl.glBegin(GL.GL_LINES);
                    {
                        gl.glVertex2f(x, y);
                        gl.glVertex2f(x + kalmans.get(c).x[3], y + kalmans.get(c).x[1]);
                    }
                    gl.glEnd();
                }
            }
        }
        gl.glPopMatrix();
    }

    /** annotate the rendered retina frame to show locations of clusters */
    public synchronized void annotate(float[][][] frame) {
        if (!isFilterEnabled()) return;
        if (chip.getCanvas().isOpenGLEnabled()) return;
        for (Cluster c : kalmans.keySet()) {
            drawFilter(kalmans.get(c), frame, c.getColor());
        }
    }

    public void setDistToVanishingPoint(float d) {
        if (d <= distTo1Px) d = distTo1Px + 1;
        this.distToVanishingPoint = d;
        getPrefs().putFloat("KalmanFilter.distToVanishingPoint", d);
    }

    public float getDistToVanishingPoint() {
        return this.distToVanishingPoint;
    }

    public boolean getMapToRoad() {
        return mapToRoad;
    }

    public void setMapToRoad(boolean mapToRoad) {
        this.mapToRoad = mapToRoad;
        getPrefs().putBoolean("KalmanFilter.mapToRoad", mapToRoad);
    }

    public boolean getFeedbackToCluster() {
        return feedbackToCluster;
    }

    public void setFeedbackToCluster(boolean feedbackToCluster) {
        this.feedbackToCluster = feedbackToCluster;
        getPrefs().putBoolean("KalmanFilter.feedbackToCluster", feedbackToCluster);
    }

    public float getBridgeHeight() {
        return bridgeHeight;
    }

    public void setBridgeHeight(float bridgeHeight) {
        if (bridgeHeight < 1) bridgeHeight = 1;
        this.bridgeHeight = bridgeHeight;
        getPrefs().putFloat("KalmanFilter.bridgeHeight", bridgeHeight);
    }

    public float getMaxMeasurementVariance() {
        return maxMeasurementVariance;
    }

    public void setMaxMeasurementVariance(float maxMeasurementVariance) {
        if (maxMeasurementVariance < 1) maxMeasurementVariance = 1;
        this.maxMeasurementVariance = maxMeasurementVariance;
        getPrefs().putFloat("KalmanFilter.maxMeasurementVariance", maxMeasurementVariance);
    }

    public float getMinProcessVariance() {
        return minProcessVariance;
    }

    public void setMinProcessVariance(float minProcessVariance) {
        if (minProcessVariance < 1) minProcessVariance = 1;
        this.minProcessVariance = minProcessVariance;
        getPrefs().putFloat("KalmanFilter.minProcessVariance", minProcessVariance);
    }

    public int getNbOfEventsTillTrack() {
        return nbOfEventsTillTrack;
    }

    public void setNbOfEventsTillTrack(int nbOfEventsTillTrack) {
        if (nbOfEventsTillTrack < 1) nbOfEventsTillTrack = 1;
        this.nbOfEventsTillTrack = nbOfEventsTillTrack;
        getPrefs().putInt("KalmanFilter.nbOfEventsTillTrack", nbOfEventsTillTrack);
    }

    public int getDistTo1Px() {
        return distTo1Px;
    }

    public void setDistTo1Px(int distTo1Px) {
        if (distTo1Px < 1) distTo1Px = 1;
        this.distTo1Px = distTo1Px;
        getPrefs().putInt("KalmanFilter.distTo1Px", distTo1Px);
    }

    public void setUseDynamicVariances(boolean useDynamicVariances) {
        this.useDynamicVariances = useDynamicVariances;
    }

    public boolean getUseDynamicVariances() {
        return useDynamicVariances;
    }

    private BufferedWriter logWriter;
}
