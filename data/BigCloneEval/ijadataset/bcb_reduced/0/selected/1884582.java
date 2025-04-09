package ch.unizh.ini.jaer.projects.wingtracker;

import net.sf.jaer.util.Matrix;
import net.sf.jaer.chip.*;
import net.sf.jaer.eventprocessing.EventFilter2D;
import net.sf.jaer.event.*;
import net.sf.jaer.event.BasicEvent;
import net.sf.jaer.event.EventPacket;
import net.sf.jaer.graphics.*;
import com.sun.opengl.util.GLUT;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import javax.media.opengl.GL;
import javax.media.opengl.glu.*;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLException;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.io.*;
import net.sf.jaer.Description;

/**
  * Tracks a fruit fly wing beat in two different ways, after a initialization phase. Begin with the method track, there the events are
 * classified to the right edge and depending on the state the evaluation goes on.
 * So there are different states in this "filter":
 *
 * initialization: data are recorded(a hardcoded number of events in the method track() with state = Init) and
 * then an analysis is done for this data. First
 * there is a 2-means algorithm(method kmeans) to localize the 2 wings. afterwards basic geometry is used to calculate the
 * bodyposition and the heading of the fly. This is done in the method findFly().
 *
 * Tracking: every event changes the actual position of the correspoinding wingedge with a lowpassfilter. This is done in
 * the method track() with the state = TRACKING.
 *
 * Kalman: the second way to track is with a extended Kalman Filter. Every event is taken as an measurement for the filter.
 * First in the method track the events is classified in left/right wing and leading/trailing edge. There are 4 instances of the
 * inner class EKF which supports data for each wingedge. The prediction and update are methods of this inner class.
*/
@Description("<html> Tracks a fruit fly wing beat in two different ways, after a initialization phase. <br> Begin with the method track, there the events are classified to the right edge and <br>depending on the state the evaluation goes on.o there are different states in this filter: <br>initialization: data are recorded(a hardcoded number of events in the method track() with state = Init) and then an <br>analysis is done for this data. First there is a 2-means algorithm(method kmeans) to <br>localize the 2 wings. afterwards basic geometry is used to calculate the bodyposition and the heading of the fly. <br>This is done in the method findFly().  <p>Tracking: every event changes the actual position of the correspoinding wingedge with a lowpassfilter. <br>This is done in the method track() with the state = TRACKING.  <br>Kalman: the second way to track is with a extended Kalman Filter. <br>Every event is taken as a measurement for the filter. <br>First in the method track the events is classified in left/right wing and leading/trailing edge. <br>There are 4 instances of the inner class EKF which supports data for each wingedge. <br>The prediction and update are methods of this inner class.")
public class WingTracker extends EventFilter2D implements FrameAnnotater, Observer {

    AEChip chip;

    AEChipRenderer renderer;

    GLUT glut;

    enum State {

        INITIAL, TRACKING, KALMAN
    }

    ;

    State state = State.INITIAL;

    State nextState = State.TRACKING;

    private final int eventsToInit = 2000;

    private final int iterationsOfKMeans = 20;

    private Point2D.Float body;

    private Point2D.Float bodyOffset = new Point2D.Float(0f, 0f);

    private Point2D.Float prototypeL;

    private Point2D.Float prototypeR;

    private float heading;

    private float positionLeft, positionRight, amplitudeLeft, amplitudeRight, frequenceLeft, frequenceRight;

    private float leftLeadingEdge, leftTrailingEdge, rightLeadingEdge, rightTrailingEdge;

    private float centroidLeft, centroidRight;

    private float searchRange;

    private boolean flipHeading = getBoolean("WingTracker.flipHeading", false);

    private float searchRangeOffset = getFloat("WingTracker.searchRangeOffset", 0);

    private float hysteresis = getFloat("WingTracker.hysteresis", (float) (Math.PI / 180) * 10f);

    private float mixingFactor = getFloat("WingTracker.mixingFactor", 0.1f);

    private boolean doLog = getBoolean("WingTracker.doLog", false);

    private boolean doBodyUpdate = getBoolean("WingTracker.doBodyUpdate", true);

    private boolean doHeadingUpdate = getBoolean("WingTracker.doHeadingUpdate", true);

    private boolean useKalmanFiltering = getBoolean("WingTracker.useKalmanFiltering", false);

    private boolean showEKFParameterWindow = getBoolean("WingTracker.showEKFParameterWindow", false);

    private int nbEventsToCollectPerEdge = getInt("WingTracker.nbEventsToCollectPerEdge", 1);

    private EKF LLE, RLE, LTE, RTE;

    private BufferedWriter logWriter;

    final String nl = System.getProperty("line.separator");

    public WingTracker(AEChip chip) {
        super(chip);
        this.chip = chip;
        renderer = (AEChipRenderer) chip.getRenderer();
        setPropertyTooltip("nbEventsToCollectPerEdge", "this parameter is for KALMAN only. If it is too slow, one can increase this number a little bit. Then events are buffered and averaged( a sort of prefiltering) before a new update of the EKF is invoked");
        setPropertyTooltip("showEKFParameterWindow", "this is a modified checkbox and should be in reality a button, just to show the EKFParameterwindow, if one closed it");
        setPropertyTooltip("useKalmanFiltering", "changes the state to KALMAN, if false-> state = TRACKING (e.g.low-pass filtering)");
        setPropertyTooltip("doHeadingUpdate", "The prototypes are updated each wing beat. with this option on, the heading (orthogonal to the line between the prototypes) is updated too.");
        setPropertyTooltip("doBodyUpdate", "the prototypes are updated each wing-beat. so with this option on, this is also done with the body. (if there was a correction by a mouse click, this correction is stored and added to the new mean position of the prototypes)");
        setPropertyTooltip("doLog", "one can do a log->in the std. home directory there will be a txt file created.");
        setPropertyTooltip("mixingFactor", "the mixing factor is the parameter of the low-pass filter, indicates how a single event influence the track.");
        setPropertyTooltip("hysteresis", "the hysteresis is used for the TRACKING state for updating the frequency and amplitude ->see doParamUpdate()");
        setPropertyTooltip("searchRangeOffset", "if the searchRange is to small, one can increase it by hand with a additional offset");
        setPropertyTooltip("flipHeading", "if auto-detection of the heading fails, one can flip the heading manually, should not be done while Kalmanfiltering");
        setPropertyTooltip("doShowEKFParameterWindow", "Shows the parameters for the Kalman filters");
        initFilter();
        chip.addObserver(this);
        glut = chip.getCanvas().getGlut();
        chip.getCanvas().getCanvas().addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == e.BUTTON1) {
                    if (body == null) return;
                    Point p = getPixelFromMouseEvent(e);
                    Point2D.Float m = meanPoints(prototypeR, prototypeL);
                    bodyOffset.x = p.x - m.x;
                    bodyOffset.y = p.y - m.y;
                    body.x = p.x;
                    body.y = p.y;
                    initWingEdges();
                    searchRange = (float) (body.distance(prototype1) + body.distance(prototype2));
                }
            }
        });
        setNextState();
        setDoLog(false);
        setFlipHeading(false);
    }

    /**
     *this method looks in the prefs if KalmanFiltering is activated, according to this it sets the next state(after init)
     */
    private void setNextState() {
        if (useKalmanFiltering) nextState = State.KALMAN; else nextState = State.TRACKING;
    }

    private Point getPixelFromMouseEvent(MouseEvent e) {
        Point p = chip.getCanvas().getPixelFromMouseEvent(e);
        return p;
    }

    final PolarityEvent.Polarity On = PolarityEvent.Polarity.On;

    final PolarityEvent.Polarity Off = PolarityEvent.Polarity.Off;

    private int eventCounter = 0;

    private Point2D.Float leftBuffer = new Point2D.Float(0, 0);

    private Point2D.Float rightBuffer = new Point2D.Float(0, 0);

    private int rightBufferCount = 0, leftBufferCount = 0;

    private float lleBuffer = 0f;

    private int lleEventCount = 0;

    private float lteBuffer = 0f;

    private int lteEventCount = 0;

    private float rleBuffer = 0f;

    private int rleEventCount = 0;

    private float rteBuffer = 0f;

    private int rteEventCount = 0;

    private boolean[][] initMask;

    private EKFParameterWindow ekfpw;

    /**
     *track() takes a packet of events and processes them depending on the state the tracker is currently. The method
     *distinguish betweet 3 states: INITIAL, TRACKING, KALMAN. If the state is INITIAL, the data are recorded, till enough 
     *data are recorded. Afterwards a few analysis is done to find the fly and do geometry. If the state is TRACKING or KALMAN
     *with every event the parameters are updated. 
     *The method provides also to write a logFile(with parameterstatus) in a txt file in the home directory.
     *@param ae A packet of BasicEvents
     */
    private synchronized void track(EventPacket<? extends BasicEvent> ae) {
        if (state == State.INITIAL) {
            if (initMask == null) {
                initMask = new boolean[chip.getSizeX()][chip.getSizeY()];
                for (int i = 0; i < chip.getSizeX(); i++) {
                    for (int j = 0; j < chip.getSizeY(); j++) initMask[i][j] = false;
                }
            }
            if (eventCounter >= eventsToInit) {
                kMeans();
                findFly();
                initWingEdges();
                state = nextState;
                eventCounter = 0;
            } else {
                for (int n = 0; n < ae.getSize(); n++) if (!initMask[ae.getEvent(n).x][ae.getEvent(n).y]) {
                    initPoints.add(new EventWingPair(ae.getEvent(n), WingType.Unknown));
                    initMask[ae.getEvent(n).x][ae.getEvent(n).y] = true;
                }
            }
            eventCounter += ae.getSize();
        }
        String logLine = new String();
        if (state == State.TRACKING) {
            Point2D.Float eventPoint = new Point2D.Float();
            float m1 = 1 - mixingFactor;
            float rads = 0f;
            for (BasicEvent o : ae) {
                PolarityEvent ev = (PolarityEvent) o;
                eventPoint.x = ev.x;
                eventPoint.y = ev.y;
                if (body.distance(ev.x, ev.y) > searchRange + searchRangeOffset) continue;
                rads = radiansInHeadingCircle(eventPoint);
                if (rads < Math.PI) {
                    leftBuffer.x += ev.getX();
                    leftBuffer.y += ev.getY();
                    leftBufferCount++;
                    if (ev.polarity == Off) {
                        leftLeadingEdge = m1 * leftLeadingEdge + mixingFactor * rads;
                        if (doLog) {
                            logLine = "1\t" + ev.getTimestamp() + "\t" + leftLeadingEdge + "\t" + frequenceLeft + "\t" + amplitudeLeft;
                        }
                    } else {
                        leftTrailingEdge = m1 * leftTrailingEdge + mixingFactor * rads;
                        if (doLog) {
                            logLine = "2\t" + ev.getTimestamp() + "\t" + leftTrailingEdge + "\t" + frequenceLeft + "\t" + amplitudeLeft;
                            ;
                        }
                    }
                    positionLeft = (leftLeadingEdge + leftTrailingEdge) / 2;
                } else {
                    rightBuffer.x += ev.getX();
                    rightBuffer.y += ev.getY();
                    rightBufferCount++;
                    if (ev.polarity == Off) {
                        rightLeadingEdge = m1 * rightLeadingEdge + mixingFactor * rads;
                        if (doLog) {
                            logLine = "3\t" + ev.getTimestamp() + "\t" + rightLeadingEdge + "\t" + frequenceRight + "\t" + amplitudeRight;
                            ;
                        }
                    } else {
                        rightTrailingEdge = m1 * rightTrailingEdge + mixingFactor * rads;
                        if (doLog) {
                            logLine = "4\t" + ev.getTimestamp() + "\t" + rightTrailingEdge;
                        }
                    }
                    positionRight = (rightLeadingEdge + rightTrailingEdge) / 2;
                }
                if (doLog) logLine = logLine + "\t" + rads;
                updateParams(ev.timestamp);
            }
        }
        if (state == State.KALMAN) {
            Point2D.Float eventPoint = new Point2D.Float();
            float rads = 0f;
            for (BasicEvent o : ae) {
                PolarityEvent ev = (PolarityEvent) o;
                eventPoint.x = ev.x;
                eventPoint.y = ev.y;
                if (body.distance(ev.x, ev.y) > searchRange + searchRangeOffset) continue;
                rads = radiansInHeadingCircle(eventPoint);
                if (rads < Math.PI) {
                    if (ev.polarity == Off) {
                        if (LLE == null) LLE = new EKF("left leading edge", ev.timestamp, centroidLeft);
                        if (!LLE.getUseEdge()) continue;
                        lleEventCount++;
                        lleBuffer += rads;
                        if (lleEventCount < nbEventsToCollectPerEdge) {
                            continue;
                        }
                        rads = lleBuffer / nbEventsToCollectPerEdge;
                        lleBuffer = 0f;
                        lleEventCount = 0;
                        LLE.predict(ev.timestamp);
                        LLE.update(rads);
                        if (doLog) {
                            logLine = "1\t" + ev.getTimestamp() + "\t" + LLE.getEdgeInRads() + "\t" + LLE.x[0] + "\t" + LLE.x[1] + "\t" + LLE.x[2] + "\t" + LLE.x[3] + "\t" + rads;
                        }
                    } else {
                        if (LTE == null) LTE = new EKF("left trailing edge", ev.timestamp, centroidLeft);
                        if (!LTE.getUseEdge()) continue;
                        lteEventCount++;
                        lteBuffer += rads;
                        if (lteEventCount < nbEventsToCollectPerEdge) {
                            continue;
                        }
                        rads = lteBuffer / nbEventsToCollectPerEdge;
                        lteBuffer = 0f;
                        lteEventCount = 0;
                        leftTrailingEdge = (1 - mixingFactor) * leftTrailingEdge + mixingFactor * rads;
                        positionLeft = (leftLeadingEdge + leftTrailingEdge) / 2;
                        updateParams(ev.timestamp);
                        LTE.predict(ev.timestamp);
                        LTE.update(rads);
                        if (doLog) {
                            logLine = "2\t" + ev.getTimestamp() + "\t" + LTE.getEdgeInRads() + "\t" + LTE.x[0] + "\t" + LTE.x[1] + "\t" + LTE.x[2] + "\t" + LTE.x[3] + "\t" + rads;
                        }
                    }
                } else {
                    if (ev.polarity == Off) {
                        if (RLE == null) RLE = new EKF("right leading edge", ev.timestamp, centroidRight);
                        if (!RLE.getUseEdge()) continue;
                        rleEventCount++;
                        rleBuffer += rads;
                        if (rleEventCount < nbEventsToCollectPerEdge) {
                            continue;
                        }
                        rads = rleBuffer / nbEventsToCollectPerEdge;
                        rleBuffer = 0f;
                        rleEventCount = 0;
                        RLE.predict(ev.timestamp);
                        RLE.update(rads);
                        if (doLog) {
                            logLine = "3\t" + ev.getTimestamp() + "\t" + RLE.getEdgeInRads() + "\t" + RLE.x[0] + "\t" + RLE.x[1] + "\t" + RLE.x[2] + "\t" + RLE.x[3] + "\t" + rads;
                        }
                    } else {
                        if (RTE == null) RTE = new EKF("right trailing edge", ev.timestamp, centroidRight);
                        if (!RTE.getUseEdge()) continue;
                        rteEventCount++;
                        rteBuffer += rads;
                        if (rteEventCount < nbEventsToCollectPerEdge) {
                            continue;
                        }
                        rads = rteBuffer / nbEventsToCollectPerEdge;
                        rteBuffer = 0f;
                        rteEventCount = 0;
                        RTE.predict(ev.timestamp);
                        RTE.update(rads);
                        if (doLog) {
                            logLine = "4\t" + ev.getTimestamp() + "\t" + RTE.getEdgeInRads() + "\t" + RTE.x[0] + "\t" + RTE.x[1] + "\t" + RTE.x[2] + "\t" + RTE.x[3] + "\t" + rads;
                        }
                    }
                }
            }
        }
        if (doLog) {
            try {
                if (logWriter != null) {
                    logWriter.write(logLine + nl);
                }
            } catch (IOException ioe) {
                System.out.println(ioe.toString());
            }
        }
    }

    public final class EKFParameterWindow extends JFrame {

        JTabbedPane tabPane = new JTabbedPane();

        public EKFParameterWindow() {
            super();
            this.setTitle("EKF Parameters");
            setSize(200, 400);
            tabPane.setRequestFocusEnabled(false);
            add(tabPane);
            setVisible(true);
        }

        public void addTab(String name, JPanel jp) {
            tabPane.add(name, jp);
            pack();
        }
    }

    /**
     * This class serves as data structure for the Kalman Filter for all the wing-edges.
     * It supports also the calculations "predict" and "update". There is also an inner class. Each wing has its own ParamterPanel
     * which is then sent to the EKFParameterwindow. In this Parameterpanel the variables of the EKF are displayed and can be changed
     * by the user.
     */
    public final class EKF {

        private boolean useEdge = true;

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

        final int dimStateVector = 4;

        final int dimMeasurement = 1;

        private float deltaTime;

        private float time;

        float measurementVariance = 1000;

        EKFParameterPanel ekfpp;

        String name;

        /**
        *Create a EKF datastructure. The datastructure contains also a panel which contains all necessary information about
        *current vectors and matrices. 
        *@param name The name of the EKF instance, for example: left leading edge. This is used to recognize the EKF in the supported panel if you have multiple instances.
        *@param timeStampOfCreationTime time stamp in mikrosec. this is necessary for the first prediction step, delta T is calculated from the difference of this time and the first event.
        *@param initPosition A guess or the true position of the tracked object.
        */
        EKF(String name, int timeStampOfCreationTime, float initPosition) {
            latestTimeStampOfLastStep = timeStampOfCreationTime;
            initData(initPosition);
            this.name = name;
            ekfpp = new EKFParameterPanel(name);
            time = timeStampOfCreationTime * 1e-6f;
        }

        /**
         * This method init the data for the EKF. It just sets values to the arrays. 
         * @arguments initPosition An approximation of the position of the wing. can be the centroid for example.
         */
        void initData(float initPosition) {
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
            deltaTime = 0;
            Matrix.identity(P);
            P[1][1] = 0;
            P[3][3] = 1;
            P[2][3] = 1;
            P[3][2] = 1;
            x[0] = initPosition;
            x[1] = 1f;
            x[2] = initPosition;
            x[3] = 240 * 2f * (float) Math.PI;
            xp[0] = x[0];
            xp[1] = x[1];
            xp[2] = x[2];
            xp[3] = x[3];
            H[0][0] = 1;
            H[0][1] = (float) Math.sin(x[2]);
            H[0][2] = x[1] * (float) Math.cos(x[2]);
            H[0][3] = 0;
            Matrix.identity(F);
            Matrix.zero(R);
            R[0][0] = measurementVariance;
            Q[0][0] = 2f;
            Q[0][1] = 0f;
            Q[0][2] = 0f;
            Q[0][3] = 0f;
            Q[1][0] = 0f;
            Q[1][1] = 1f;
            Q[1][2] = 0f;
            Q[1][3] = 0f;
            Q[2][0] = 0f;
            Q[2][1] = 0f;
            Q[2][2] = .5f;
            Q[2][3] = 1f;
            Q[3][0] = 0f;
            Q[3][1] = 0f;
            Q[3][2] = 1f;
            Q[3][3] = 2f;
            z[0] = 0;
        }

        /**
         *The prediction phase of the EKF-Algorithm. x and P are predicted.
         *@param t the timestamp in microseconds of the next measurement.
         */
        void predict(int t) {
            int timeStamp = t;
            if (timeStamp > latestTimeStampOfLastStep) {
                deltaTime = (timeStamp - latestTimeStampOfLastStep) * 1.e-6f;
                latestTimeStampOfLastStep = timeStamp;
            } else {
                return;
            }
            time = t * 1e-6f;
            F[2][3] = deltaTime;
            xp = Matrix.multMatrix(F, x);
            Pp = Matrix.addMatrix(Matrix.multMatrix(Matrix.multMatrix(F, P), Matrix.transposeMatrix(F)), Q);
        }

        /**
         *the update step of the EKF. The argument rads is just the measurement in radians.
         *@param rads measurement in radians.
         */
        void update(float rads) {
            y[0] = rads - (x[0] + x[1] * (float) Math.sin(x[2]));
            H[0][0] = 1;
            H[0][1] = (float) Math.sin(x[2]);
            H[0][2] = xp[1] * (float) Math.cos(x[2]);
            H[0][3] = time * xp[0] * (float) Math.cos(x[2]);
            float[][] S = new float[dimMeasurement][dimMeasurement];
            float[][] STemp = new float[dimMeasurement][dimStateVector];
            float[][] STemp2 = new float[dimMeasurement][dimMeasurement];
            Matrix.multiply(H, Pp, STemp);
            Matrix.multiply(STemp, Matrix.transposeMatrix(H), STemp2);
            Matrix.add(STemp2, R, S);
            float[][] K = new float[dimStateVector][dimMeasurement];
            float[][] Ktemp = new float[dimStateVector][dimMeasurement];
            Matrix.multiply(Pp, Matrix.transposeMatrix(H), Ktemp);
            S[0][0] = 1 / S[0][0];
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
        }

        /**
         * The following function computes the objective function h.
         *@return the actual track in radians of the filter
         */
        public float getEdgeInRads() {
            return x[0] + x[1] * (float) Math.sin(x[2]);
        }

        /**
         *@param useEdge if you want to use this created instance of an EKF. If not,the calcuation and annotation is leaved out.
         */
        public void setUseEdge(boolean useEdge) {
            this.useEdge = useEdge;
        }

        /**
         *return if the current instance of the EKF(wingedge) is calculated and annotated
         */
        public boolean getUseEdge() {
            return useEdge;
        }

        /**
         * The class supports a panel which contains all important parameters of the current EKF.
         */
        public final class EKFParameterPanel extends JPanel {

            /**
             *In this constructor a Panel with the Matrices Q, R,P and the state vector x is created. Also a checkbox to 
             *determine if the instance of the EKF should really be used. The matrices/vectors are displayed in JTable and 
             *their values are changable. If there is no ParameterWindow created per wingTracker, the parameterWindow is
             *created new, else the panel is appended to the window as a tab.
             *@param name The String is showed in the title of the Parameterwindow to recognize this instance of EKF(wingEdge)
             */
            public EKFParameterPanel(String name) {
                super();
                this.setLayout(new GridLayout(0, 2, 5, 10));
                add(new Label("Use the " + name));
                JCheckBox cb1 = new JCheckBox("", true);
                cb1.addItemListener(new ItemListener() {

                    public void itemStateChanged(ItemEvent e) {
                        useEdge = !useEdge;
                    }
                });
                add(cb1);
                add(new Label("The process variance: Q = "));
                addMatrix(Q);
                add(new Label("The measurement variance: R = "));
                addMatrix(R);
                add(new Label("State Vector: x = "));
                addMatrix(x);
                add(new Label("Error cov.-matrix. P = "));
                addMatrix(P);
                if (ekfpw == null) ekfpw = new EKFParameterWindow();
                ekfpw.addTab(name, this);
            }

            /**
             *If you want to add a additional Vector to the Pane. 
             *@param m The vector to add in the pane in form of a JTable
             */
            public void addMatrix(float[] m) {
                JTable t = new JTable(new VectorModel(m));
                this.add(t);
                setVisible(true);
            }

            /**
             *If you want to add a additional Matrix to the Pane.
             *@param m The 2D Matrix to add in the pane in form of a JTable.
             */
            public void addMatrix(float[][] m) {
                JTable t = new JTable(new MatrixModel(m));
                t.setRowSelectionAllowed(true);
                t.setColumnSelectionAllowed(true);
                this.add(t);
                setVisible(true);
            }

            /**
             *Standard model to represent an array as a Matrix in an JTable
             */
            private final class MatrixModel extends AbstractTableModel {

                float[][] matrix;

                public MatrixModel(float[][] matrix) {
                    this.matrix = matrix;
                }

                public int getRowCount() {
                    return matrix.length;
                }

                public int getColumnCount() {
                    return matrix[0].length;
                }

                public Object getValueAt(int row, int col) {
                    return matrix[row][col];
                }

                public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
                    matrix[rowIndex][columnIndex] = Float.parseFloat((String) aValue);
                }

                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return true;
                }
            }

            /** 
             * Standard model to represent an array as a vetor in an JTable.
             * if a vector should be shown in a table, this is the model for that. 
             */
            private final class VectorModel extends AbstractTableModel {

                float[] v;

                public VectorModel(float[] v) {
                    this.v = v;
                }

                public int getRowCount() {
                    return v.length;
                }

                public int getColumnCount() {
                    return 1;
                }

                public Object getValueAt(int row, int col) {
                    return v[row];
                }

                public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
                    v[rowIndex] = Float.parseFloat((String) aValue);
                }

                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return true;
                }
            }
        }
    }

    ;

    /**
     *Does nothing.
     */
    public void initFilter() {
    }

    /**
     * computes from a point(x,y) the radians where the first point on the left side of the heading corresponds to 0 and the
     * first point on the right side corresponds to 2*pi. The back of the fly corresponds to pi. The center of this unitcircle
     * is represented by the body(which can be set by a mouse click)
     *@param point Point which should be converted in radians
     */
    private float radiansInHeadingCircle(Point2D.Float point) {
        return radiansInHeadingCircle(point.x, point.y);
    }

    /**
     * computes from a point(x,y) the radians where the first point on the left side of the heading corresponds to 0 and the
     * first point on the right side corresponds to 2*pi. The back of the fly corresponds to pi. The center of this unitcircle
     * is represented by the body(which can be set by a mouse click)
     *@param x x coord from the point which should be converted in radians
     *@param y y coord of that point
     */
    private float radiansInHeadingCircle(float x, float y) {
        float angle = radiansInUnitCircle(x, y) - heading;
        if (angle < 0) return (float) (2 * Math.PI) + angle;
        return angle;
    }

    /**
     * Calculates a the radians to the unit-circle with center at the body
     *@param point Point which should be converted in radians
     */
    private float radiansInUnitCircle(Point2D.Float point) {
        return radiansInUnitCircle(point.x, point.y);
    }

    /**
     * Calculates a the radians to the unit-circle with center at the body
     *@param x x coordinate of the point which is converted in radians
     *@param y y coordinate of that point
     */
    private float radiansInUnitCircle(float x, float y) {
        float[] p = { x - body.x, y - body.y };
        float pn = Matrix.norm2(p);
        p[0] = p[0] / pn;
        p[1] = p[1] / pn;
        float angle = (float) Math.acos(p[0]);
        if (p[1] < 0) {
            angle = (float) (Math.PI * 2 - angle);
        }
        return angle;
    }

    /**
     *calculates the distance of the event and the wingEdge-Line. This is actually not used anymore
     *@param ev The event 
     *@param wingEdge A Point which determines the actualPosition of the wing edge line.
     */
    private float distEventToWing(BasicEvent ev, Point2D.Float wingEdge) {
        float[] e = { ev.x, ev.y };
        float[] w = { wingEdge.x, wingEdge.y };
        float scaling = (ev.x * wingEdge.x + ev.y * wingEdge.y) / (Matrix.norm2(e) * Matrix.norm2(w));
        w[0] = scaling * w[0];
        w[1] = scaling * w[1];
        return (float) (new Point2D.Float(w[0], w[1])).distance(ev.x, ev.y);
    }

    private boolean leftPositive = true, rightPositive = false;

    private int lastTimeStampL = 0, lastTimeStampR = 0;

    private float leftTail = 0, leftFront = 0, rightTail = 0, rightFront = 0;

    /**
     * This method is only used in TRACKING state. It updates the frequency and amplitude and checks for zero-crossing.
     *It also updates the centroids after each wing beat for both wings, the heading(if selected) and the body position(if selected)
     *The update is only done once per detected wingbeat.
     *@param t the most actual time stamp.
     */
    private void updateParams(int t) {
        if (!leftPositive) {
            if (positionLeft < leftTail) leftTail = positionLeft;
            if (positionLeft - (centroidLeft + hysteresis) > 0) {
                leftPositive = true;
                amplitudeLeft = Math.abs(leftFront - leftTail);
                leftTail = centroidLeft;
                leftFront = centroidLeft;
                return;
            }
        }
        if (leftPositive) {
            if (positionLeft >= leftFront) {
                leftFront = positionLeft;
            }
            if (positionLeft - (centroidLeft - hysteresis) < 0) {
                leftPositive = false;
                frequenceLeft = 1f / ((t - lastTimeStampL) * (float) 1.e-6);
                lastTimeStampL = t;
                if (leftBufferCount != 0) prototypeL.setLocation((1 - mixingFactor) * prototypeL.x + mixingFactor * (leftBuffer.x / leftBufferCount), (1 - mixingFactor) * prototypeL.y + mixingFactor * (leftBuffer.y / leftBufferCount));
                leftBuffer.x = 0;
                leftBuffer.y = 0;
                leftBufferCount = 0;
                if (doBodyUpdate) {
                    body = meanPoints(prototypeR, prototypeL);
                    body.x += bodyOffset.x;
                    body.y += bodyOffset.y;
                }
                centroidLeft = radiansInHeadingCircle(prototypeL.x, prototypeL.y);
                searchRange = (float) (body.distance(prototypeR) + body.distance(prototypeL));
                if (doHeadingUpdate) {
                    float x = -(prototypeR.y - prototypeL.y) / 2 + body.x;
                    float y = (prototypeR.x - prototypeL.x) / 2 + body.y;
                    heading = radiansInUnitCircle(x, y);
                }
                return;
            }
        }
        if (rightPositive) {
            if (positionRight < rightTail) rightTail = positionRight;
            if (positionRight - (centroidRight + hysteresis) > 0) {
                rightPositive = false;
                amplitudeRight = Math.abs(rightFront - rightTail);
                rightTail = centroidRight;
                rightFront = centroidRight;
                return;
            }
        }
        if (!rightPositive) {
            if (positionRight >= rightFront) {
                rightFront = positionRight;
            }
            if (positionRight - (centroidRight - hysteresis) < 0) {
                rightPositive = true;
                frequenceRight = 1f / ((t - lastTimeStampR) * (float) 1.e-6);
                lastTimeStampR = t;
                if (rightBufferCount != 0) prototypeR.setLocation((1 - mixingFactor) * prototypeR.x + mixingFactor * (rightBuffer.x / rightBufferCount), (1 - mixingFactor) * prototypeR.y + mixingFactor * (rightBuffer.y / rightBufferCount));
                rightBuffer.x = 0;
                rightBuffer.y = 0;
                rightBufferCount = 0;
                if (doBodyUpdate) {
                    body = meanPoints(prototypeR, prototypeL);
                    body.x += bodyOffset.x;
                    body.y += bodyOffset.y;
                }
                centroidRight = radiansInHeadingCircle(prototypeR.x, prototypeR.y);
                searchRange = (float) (body.distance(prototypeR) + body.distance(prototypeL));
                if (doHeadingUpdate) {
                    float x = -(prototypeR.y - prototypeL.y) / 2 + body.x;
                    float y = (prototypeR.x - prototypeL.x) / 2 + body.y;
                    heading = radiansInUnitCircle(x, y);
                }
                return;
            }
        }
    }

    private Point2D.Float prototype1;

    private Point2D.Float prototype2;

    /**
     *This method sets body position, sets the heading (with a algorithm that tries to find out where the head is), sets the search
     *range, and
     *assigns the prototype1,prototype2 to left or right depending on the heading. It is necessary that prototype1 and 
     *prototype2 has a meaningful value, this values come out of the k-means algorithm.
     */
    private void findFly() {
        body = meanPoints(prototype1, prototype2);
        body.x += bodyOffset.x;
        body.y += bodyOffset.y;
        searchRange = (float) (body.distance(prototype1) + body.distance(prototype2));
        float x = -(prototype2.y - prototype1.y) / 2 + body.x;
        float y = (prototype2.x - prototype1.x) / 2 + body.y;
        heading = radiansInUnitCircle(x, y);
        float cL = radiansInHeadingCircle(prototype1);
        float cR = radiansInHeadingCircle(prototype2);
        float maxLeft = 0;
        float minLeft = (float) Math.PI;
        float maxRight = 0;
        float minRight = 2f * (float) Math.PI;
        for (EventWingPair ewp : initPoints) {
            PolarityEvent e = (PolarityEvent) ewp.getEvent();
            if (e.polarity == On) continue;
            if ((body.x - e.x) * (body.x - e.x) + (body.y - e.y) * (body.y - e.y) <= searchRange * searchRange) {
                float rads = radiansInHeadingCircle(e.x, e.y);
                if (ewp.getWingType() == WingType.Left) {
                    cL = (1 - mixingFactor) * cL + mixingFactor * rads;
                    if (maxLeft < cL) maxLeft = cL;
                    if (minLeft > cL) minLeft = cL;
                } else {
                    cR = (1 - mixingFactor) * cR + mixingFactor * rads;
                    if (maxRight < rads) maxRight = rads;
                    if (minRight > rads) minRight = rads;
                }
            }
        }
        boolean flip = false;
        boolean shouldFlipLeft = false, shouldFlipRight = false;
        if (minLeft < (float) Math.PI - maxLeft) {
            shouldFlipLeft = true;
        }
        if (minRight - Math.PI > 2 * (float) Math.PI - maxRight) {
            shouldFlipRight = true;
        }
        if (shouldFlipLeft == shouldFlipRight) {
            flip = shouldFlipRight;
        } else {
            if (maxLeft - minLeft > maxRight - minRight) {
                flip = shouldFlipLeft;
            } else {
                flip = shouldFlipRight;
            }
        }
        if (flip) {
            heading += (float) Math.PI;
            if (heading >= (float) Math.PI * 2f) heading -= Math.PI * 2f;
        }
        if (radiansInHeadingCircle(prototype1.x, prototype1.y) <= Math.PI) {
            prototypeL = prototype1;
            prototypeR = prototype2;
        } else {
            prototypeL = prototype2;
            prototypeR = prototype1;
        }
    }

    /**
     *The kMeans algorithm, in this case k = 2. As datapoints the method uses the recorded datapoints stored in the vector 
     *initpoints where each element conists of a EventWingPair. 
     *Goal is to cluster the datapoints in 2 clusters, one cluster for the wingedge and the other one for the right
     *one. The classification is stored in the EventWingPair datastructure. 
     */
    private void kMeans() {
        float dist1, dist2;
        float x, y;
        prototype1 = new Point2D.Float(0f, (float) chip.getSizeY() - 1);
        prototype2 = new Point2D.Float((float) chip.getSizeX() - 1, 0f);
        Point2D.Float tempPrototype1 = new Point2D.Float(0f, 0f);
        Point2D.Float tempPrototype2 = new Point2D.Float(0f, 0f);
        int n1 = 0;
        int n2 = 0;
        for (int i = 0; i < iterationsOfKMeans; i++) {
            for (int j = 0; j < initPoints.size(); j++) {
                x = initPoints.elementAt(j).event.x;
                y = initPoints.elementAt(j).event.y;
                dist1 = (float) (Math.pow(x - prototype1.x, 2) + Math.pow(y - prototype1.y, 2));
                dist2 = (float) (Math.pow(x - prototype2.x, 2) + Math.pow(y - prototype2.y, 2));
                if (dist1 < dist2) {
                    initPoints.elementAt(j).setWingType(WingType.Left);
                    n1++;
                    tempPrototype1.x += x;
                    tempPrototype1.y += y;
                } else {
                    initPoints.elementAt(j).setWingType(WingType.Right);
                    n2++;
                    tempPrototype2.x += x;
                    tempPrototype2.y += y;
                }
            }
            prototype1.setLocation(tempPrototype1.x / n1, tempPrototype1.y / n1);
            prototype2.setLocation(tempPrototype2.x / n2, tempPrototype2.y / n2);
            tempPrototype1.setLocation(0f, 0f);
            tempPrototype2.setLocation(0f, 0f);
            n1 = 0;
            n2 = 0;
        }
    }

    private Vector<EventWingPair> initPoints = new Vector<EventWingPair>();

    enum WingType {

        Unknown, Left, Right
    }

    ;

    /**
     *The Class provides a datastructure where an event can be assigned to a WingType. The WingType is an enumeration with entries:
     *Unknown, Left, Right. Getter and Setter methods are provided.
     */
    final class EventWingPair {

        BasicEvent event;

        WingType wing = WingType.Unknown;

        public EventWingPair(BasicEvent event, WingType w) {
            this.event = event;
            wing = w;
        }

        public void setWingType(WingType w) {
            wing = w;
        }

        public WingType getWingType() {
            return wing;
        }

        public BasicEvent getEvent() {
            return event;
        }
    }

    /**
     *@return The midpoint of two points
     *@param a first Point
     *@param b second point
     */
    private Point2D.Float meanPoints(Point2D.Float a, Point2D.Float b) {
        return new Point2D.Float((a.x - b.x) / 2 + b.x, (a.y - b.y) / 2 + b.y);
    }

    /**
     * This method initializes the edges on the correct side, if the angle in the heading-unit-circle is < 180 deg, then
     * the prototype corresponds to the left edge(actually we don't know which wing it is, this is only in the code)
     */
    private void initWingEdges() {
        leftLeadingEdge = radiansInHeadingCircle(prototypeL.x, prototypeL.y);
        leftTrailingEdge = leftLeadingEdge;
        centroidLeft = leftLeadingEdge;
        rightLeadingEdge = radiansInHeadingCircle(prototypeR.x, prototypeR.y);
        rightTrailingEdge = rightLeadingEdge;
        centroidRight = rightLeadingEdge;
    }

    /**
     * This method shows just the initialisation. The tracker should be used with OpenGL.
     */
    final void drawFilter(float[][][] fr) {
        if (state != State.INITIAL) {
            colorPixel(Math.round(prototypeL.x), Math.round(prototypeL.y), fr, Color.green);
            colorPixel(Math.round(prototypeR.x), Math.round(prototypeR.y), fr, Color.green);
            colorPixel(Math.round(body.x), Math.round(body.y), fr, Color.blue);
        }
    }

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

    /**
     *@return The state of the tracker. The state is an element of an enumaration.
     */
    public Object getFilterState() {
        return state;
    }

    public boolean isGeneratingFilter() {
        return false;
    }

    public synchronized void resetFilter() {
        eventCounter = 0;
        state = State.INITIAL;
        setNextState();
        initPoints.clear();
        bodyOffset.setLocation(0, 0);
        initMask = null;
        if (ekfpw != null) {
            ekfpw.dispose();
            ekfpw = null;
        }
        LLE = null;
        RLE = null;
        LTE = null;
        RTE = null;
    }

    public EventPacket filterPacket(EventPacket in) {
        if (in == null) return null;
        if (!filterEnabled) return in;
        if (enclosedFilter != null) in = enclosedFilter.filterPacket(in);
        track(in);
        return in;
    }

    public void update(Observable o, Object arg) {
    }

    public void annotate(Graphics2D g) {
    }

    private boolean hasBlendChecked = false;

    private boolean hasBlend = false;

    GLU glu;

    GLUquadric flySurround;

    public void annotate(GLAutoDrawable drawable) {
        if (!isFilterEnabled()) return;
        GL gl = drawable.getGL();
        if (gl == null) return;
        gl.glPushMatrix();
        gl.glColor3f(1, 0, 0);
        gl.glLineWidth(.3f);
        if (!hasBlendChecked) {
            hasBlendChecked = true;
            String glExt = gl.glGetString(GL.GL_EXTENSIONS);
            if (glExt.indexOf("GL_EXT_blend_color") != -1) hasBlend = true;
        }
        if (hasBlend) {
            try {
                gl.glEnable(GL.GL_BLEND);
                gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
                gl.glBlendEquation(GL.GL_FUNC_ADD);
            } catch (GLException e) {
                e.printStackTrace();
                hasBlend = false;
            }
        }
        if (state == State.TRACKING) {
            int font = GLUT.BITMAP_HELVETICA_12;
            gl.glColor3f(1, 0, 0);
            gl.glRasterPos3f(0, 18, 0);
            glut.glutBitmapString(font, String.format("Freq(L) = %.1f, Ampl(L) = %.1f", frequenceLeft, (180 / Math.PI) * amplitudeLeft));
            gl.glRasterPos3f(0, 14, 0);
            glut.glutBitmapString(font, String.format("Freq(R) = %.1f, Ampl(R) = %.1f", frequenceRight, (180 / Math.PI) * amplitudeRight));
            gl.glColor3f(0, 0, 1);
            gl.glBegin(GL.GL_LINES);
            gl.glVertex2f(body.x - 1, body.y);
            gl.glVertex2f(body.x + 1, body.y);
            gl.glVertex2f(body.x, body.y - 1);
            gl.glVertex2f(body.x, body.y + 1);
            gl.glEnd();
            gl.glBegin(GL.GL_LINE_STRIP);
            gl.glVertex2f(prototypeL.x - 1f, prototypeL.y);
            gl.glVertex2f(prototypeL.x, prototypeL.y - 1f);
            gl.glVertex2f(prototypeL.x + 1f, prototypeL.y);
            gl.glVertex2f(prototypeL.x, prototypeL.y + 1f);
            gl.glVertex2f(prototypeL.x - 1f, prototypeL.y);
            gl.glEnd();
            gl.glColor3f(1, 1, 1);
            gl.glBegin(GL.GL_LINE_STRIP);
            gl.glVertex2f(prototypeR.x - 1f, prototypeR.y);
            gl.glVertex2f(prototypeR.x, prototypeR.y - 1f);
            gl.glVertex2f(prototypeR.x + 1f, prototypeR.y);
            gl.glVertex2f(prototypeR.x, prototypeR.y + 1f);
            gl.glVertex2f(prototypeR.x - 1f, prototypeR.y);
            gl.glEnd();
            gl.glPushMatrix();
            gl.glTranslatef(body.x, body.y, 0);
            gl.glRotatef((180 / (float) Math.PI) * heading, 0, 0, 1);
            gl.glColor3f(1, 1, 1);
            gl.glBegin(GL.GL_LINES);
            gl.glVertex2f(searchRange + .1f * searchRange, 0);
            gl.glVertex2f(-searchRange + .1f * searchRange, 0);
            gl.glEnd();
            gl.glBegin(gl.GL_TRIANGLES);
            gl.glVertex2f(searchRange + .1f * searchRange, 0f);
            gl.glVertex2f(searchRange, -2f);
            gl.glVertex2f(searchRange, 2f);
            gl.glEnd();
            float h = searchRange * (float) Math.tan(hysteresis);
            gl.glColor4f(1f, 0, 0, .3f);
            gl.glPushMatrix();
            gl.glRotatef((180 / (float) Math.PI) * centroidLeft, 0, 0, 1);
            gl.glBegin(GL.GL_TRIANGLES);
            gl.glVertex2f(0f, 0f);
            gl.glVertex2f(searchRange, h);
            gl.glVertex2f(searchRange, -h);
            gl.glEnd();
            gl.glPopMatrix();
            gl.glPushMatrix();
            gl.glRotatef((180 / (float) Math.PI) * centroidRight, 0, 0, 1);
            gl.glBegin(GL.GL_TRIANGLES);
            gl.glVertex2f(0f, 0f);
            gl.glVertex2f(searchRange, h);
            gl.glVertex2f(searchRange, -h);
            gl.glEnd();
            gl.glPopMatrix();
            gl.glColor3f(0, 0, 0);
            gl.glLineWidth(3);
            gl.glPushMatrix();
            gl.glRotatef((180 / (float) Math.PI) * rightLeadingEdge, 0, 0, 1);
            gl.glBegin(GL.GL_LINES);
            gl.glVertex2f(0, 0);
            gl.glVertex2f(searchRange, 0);
            gl.glEnd();
            gl.glRotatef((180 / (float) Math.PI) * (-rightLeadingEdge + leftLeadingEdge), 0, 0, 1);
            gl.glBegin(GL.GL_LINES);
            gl.glVertex2f(0, 0);
            gl.glVertex2f(searchRange, 0);
            gl.glEnd();
            gl.glPopMatrix();
            gl.glColor3f(1, 1, 1);
            gl.glPushMatrix();
            gl.glRotatef((180 / (float) Math.PI) * rightTrailingEdge, 0, 0, 1);
            gl.glBegin(GL.GL_LINES);
            gl.glVertex2f(0, 0);
            gl.glVertex2f(searchRange, 0);
            gl.glEnd();
            gl.glRotatef((180 / (float) Math.PI) * (-rightTrailingEdge + leftTrailingEdge), 0, 0, 1);
            gl.glBegin(GL.GL_LINES);
            gl.glVertex2f(0, 0);
            gl.glVertex2f(searchRange, 0);
            gl.glEnd();
            gl.glPopMatrix();
            gl.glLineWidth(1);
            if (glu == null) glu = new GLU();
            if (flySurround == null) flySurround = glu.gluNewQuadric();
            gl.glColor4f(0, 1, 0, .2f);
            glu.gluQuadricDrawStyle(flySurround, GLU.GLU_FILL);
            glu.gluDisk(flySurround, 0, searchRange + searchRangeOffset, 16, 1);
            gl.glPopMatrix();
        }
        if (state == State.KALMAN) {
            if (LLE == null || LTE == null || RLE == null || RTE == null) return;
            int font = GLUT.BITMAP_HELVETICA_12;
            gl.glColor3f(1, 0, 0);
            gl.glRasterPos3f(0, 18, 0);
            glut.glutBitmapString(font, String.format("Freq(LLE) = %.1f, Ampl(LLE) = %.1f", LLE.x[3] / (2f * (float) Math.PI), (180 / Math.PI) * LLE.x[1]));
            gl.glColor3f(1, 0.5f, 0.5f);
            gl.glRasterPos3f(0, 14, 0);
            glut.glutBitmapString(font, String.format("Freq(LTE) = %.1f, Ampl(LTE) = %.1f", LTE.x[3] / (2f * (float) Math.PI), (180 / Math.PI) * LTE.x[1]));
            gl.glColor3f(1, 0, 0);
            gl.glRasterPos3f(0, 10, 0);
            glut.glutBitmapString(font, String.format("Freq(RLE) = %.1f, Ampl(RLE) = %.1f", RLE.x[3] / (2f * (float) Math.PI), (180 / Math.PI) * RLE.x[1]));
            gl.glColor3f(1, 0.5f, 0.5f);
            gl.glRasterPos3f(0, 6, 0);
            glut.glutBitmapString(font, String.format("Freq(RTE) = %.1f, Ampl(RTE) = %.1f", RTE.x[3] / (2f * (float) Math.PI), (180 / Math.PI) * RTE.x[1]));
            gl.glColor3f(0, 0, 1);
            gl.glBegin(GL.GL_LINES);
            gl.glVertex2f(body.x - 1, body.y);
            gl.glVertex2f(body.x + 1, body.y);
            gl.glVertex2f(body.x, body.y - 1);
            gl.glVertex2f(body.x, body.y + 1);
            gl.glEnd();
            gl.glPushMatrix();
            gl.glTranslatef(body.x, body.y, 0);
            gl.glRotatef((180 / (float) Math.PI) * heading, 0, 0, 1);
            gl.glColor3f(1, 1, 1);
            gl.glBegin(GL.GL_LINES);
            gl.glVertex2f(searchRange + .1f * searchRange, 0);
            gl.glVertex2f(-searchRange + .1f * searchRange, 0);
            gl.glEnd();
            gl.glBegin(gl.GL_TRIANGLES);
            gl.glVertex2f(searchRange + .1f * searchRange, 0f);
            gl.glVertex2f(searchRange, -2f);
            gl.glVertex2f(searchRange, 2f);
            gl.glEnd();
            if (LLE.getUseEdge()) {
                gl.glColor3f(0, 0f, 0f);
                gl.glPushMatrix();
                gl.glRotatef((180 / (float) Math.PI) * LLE.getEdgeInRads(), 0, 0, 1);
                gl.glBegin(GL.GL_LINES);
                gl.glVertex2f(0, 0);
                gl.glVertex2f(searchRange, 0);
                gl.glEnd();
                gl.glPopMatrix();
            }
            if (RLE.getUseEdge()) {
                gl.glColor3f(0, 0f, 0f);
                gl.glPushMatrix();
                gl.glRotatef((180 / (float) Math.PI) * RLE.getEdgeInRads(), 0, 0, 1);
                gl.glBegin(GL.GL_LINES);
                gl.glVertex2f(0, 0);
                gl.glVertex2f(searchRange, 0);
                gl.glEnd();
                gl.glPopMatrix();
            }
            if (RTE.getUseEdge()) {
                gl.glColor3f(1, 1, 1);
                gl.glPushMatrix();
                gl.glRotatef((180 / (float) Math.PI) * RTE.getEdgeInRads(), 0, 0, 1);
                gl.glBegin(GL.GL_LINES);
                gl.glVertex2f(0, 0);
                gl.glVertex2f(searchRange, 0);
                gl.glEnd();
                gl.glPopMatrix();
            }
            if (LTE.getUseEdge()) {
                gl.glColor3f(1, 1, 1);
                gl.glPushMatrix();
                gl.glRotatef((180 / (float) Math.PI) * LTE.getEdgeInRads(), 0, 0, 1);
                gl.glBegin(GL.GL_LINES);
                gl.glVertex2f(0, 0);
                gl.glVertex2f(searchRange, 0);
                gl.glEnd();
                gl.glPopMatrix();
            }
            if (glu == null) glu = new GLU();
            if (flySurround == null) flySurround = glu.gluNewQuadric();
            gl.glColor4f(0, 1, 0, .2f);
            glu.gluQuadricDrawStyle(flySurround, GLU.GLU_FILL);
            glu.gluDisk(flySurround, 0, searchRange + searchRangeOffset, 16, 1);
            gl.glPopMatrix();
        }
        gl.glPopMatrix();
    }

    public void setDoLog(boolean doLog) {
        Calendar cal = Calendar.getInstance();
        if (doLog) {
            try {
                logWriter = new BufferedWriter(new FileWriter(new File(".", "wingLog_" + cal.get(Calendar.YEAR) + (cal.get(Calendar.MONTH) + 1) + cal.get(Calendar.DAY_OF_MONTH) + "_" + cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE) + "_" + cal.get(Calendar.SECOND) + ".txt")));
                logWriter.write("#edgetype leftleadingEdge = 1" + nl + "#edgetype leftTrailingEdge = 2" + nl + "#edgetype rightleadingEdge = 3" + nl + "#edgtype rightTrailinEdge = 4" + nl + "#Output of WRA track: edgetype timeStamp EdgePos eventPos" + nl + "#Output of Kalman filter track: edgetype timestamp trackPos wingPos Amplitude Phase angularFreq." + nl);
            } catch (IOException ioe) {
                System.out.println(ioe.toString());
            }
        } else {
            if (logWriter != null) {
                try {
                    logWriter.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        getPrefs().putBoolean("WingTracker.doLog", doLog);
        this.doLog = doLog;
    }

    public boolean getDoLog() {
        return doLog;
    }

    public float getMixingFactor() {
        return mixingFactor;
    }

    public void setMixingFactor(float mixingFactor) {
        if (mixingFactor > 1) mixingFactor = 1; else if (mixingFactor < 0) mixingFactor = 0;
        this.mixingFactor = mixingFactor;
        getPrefs().putFloat("WingTracker.mixingFactor", mixingFactor);
    }

    public float getHysteresis() {
        return hysteresis;
    }

    public void setHysteresis(float hysteresis) {
        if (hysteresis > Math.PI) mixingFactor = (float) Math.PI; else if (hysteresis < 0) hysteresis = 0;
        this.hysteresis = hysteresis;
        getPrefs().putFloat("WingTracker.hysteresis", hysteresis);
    }

    public float getSearchRangeOffset() {
        return searchRangeOffset;
    }

    public void setSearchRangeOffset(float searchRangeOffset) {
        this.searchRangeOffset = searchRangeOffset;
        getPrefs().putFloat("WingTracker.searchRangeOffset", searchRangeOffset);
    }

    public void setdoBodyUpdate(boolean doBodyUpdate) {
        this.doBodyUpdate = doBodyUpdate;
        getPrefs().putBoolean("WingTracker.doBodyUpdate", doBodyUpdate);
    }

    public boolean getdoBodyUpdate() {
        return doBodyUpdate;
    }

    public void setdoHeadingUpdate(boolean doHeadingUpdate) {
        this.doHeadingUpdate = doHeadingUpdate;
        getPrefs().putBoolean("WingTracker.doHeadingUpdate", doHeadingUpdate);
    }

    public boolean getdoHeadingUpdate() {
        return doHeadingUpdate;
    }

    public void setUseKalmanFiltering(boolean useKalmanFiltering) {
        this.useKalmanFiltering = useKalmanFiltering;
        getPrefs().putBoolean("WingTracker.useKalmanFiltering", useKalmanFiltering);
        if (state == State.INITIAL) {
            if (useKalmanFiltering) nextState = State.KALMAN; else nextState = State.TRACKING;
            return;
        }
        if (useKalmanFiltering) state = State.KALMAN; else state = State.TRACKING;
    }

    public boolean getUseKalmanFiltering() {
        return useKalmanFiltering;
    }

    public int getNbEventsToCollectPerEdge() {
        return nbEventsToCollectPerEdge;
    }

    public void setNbEventsToCollectPerEdge(int nbEventsToCollectPerEdge) {
        if (nbEventsToCollectPerEdge < 1) nbEventsToCollectPerEdge = 1;
        this.nbEventsToCollectPerEdge = nbEventsToCollectPerEdge;
        getPrefs().putInt("WingTracker.nbEventsToCollectPerEdge", nbEventsToCollectPerEdge);
    }

    public void setFlipHeading(boolean flipHeading) {
        this.flipHeading = flipHeading;
        getPrefs().putBoolean("WingTracker.flipHeading", flipHeading);
        float pi = (float) Math.PI;
        heading += pi;
        if (heading >= pi * 2f) heading -= pi * 2f;
        float tempc = centroidLeft;
        centroidLeft = centroidRight + pi;
        centroidRight = tempc + pi;
        float temp = leftLeadingEdge;
        leftLeadingEdge = rightLeadingEdge - pi;
        rightLeadingEdge = pi + temp;
        temp = leftTrailingEdge;
        leftTrailingEdge = rightTrailingEdge - pi;
        rightTrailingEdge = pi + temp;
        Point2D.Float tempP = prototypeL;
        prototypeL = prototypeR;
        prototypeR = tempP;
    }

    public boolean getFlipHeading() {
        return flipHeading;
    }

    public void doShowEKFParameterWindow() {
        if (!(ekfpw == null)) {
            ekfpw.setVisible(true);
        }
        getPrefs().putBoolean("WingTracker.showEKFParameterWindow", false);
    }
}
