package ch.unizh.ini.jaer.projects.eyetracker;

import net.sf.jaer.chip.AEChip;
import net.sf.jaer.eventprocessing.EventFilter2D;
import net.sf.jaer.event.*;
import net.sf.jaer.eventprocessing.EventFilterDataLogger;
import net.sf.jaer.graphics.*;
import net.sf.jaer.graphics.FrameAnnotater;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.Observable;
import java.util.Observer;
import javax.media.opengl.*;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.*;
import javax.swing.*;

/**
 * A filter whose underlying model rims (pupil and iris) with a position and radius and rimThickness, which is pushed around by events.
 *
 * @author tobi
 */
public class EyeTracker extends EventFilter2D implements Observer, FrameAnnotater {

    private float pupilRadius = getPrefs().getFloat("EyeTracker.pupilRadius", 10f);

    {
        setPropertyTooltip("pupilRadius", "The pupil radius in pixels");
    }

    private float irisRadius = getPrefs().getFloat("EyeTracker.irisRadius", 20f);

    {
        setPropertyTooltip("irisRadius", "The iris radius in pixels");
    }

    private float rimThickness = getPrefs().getFloat("EyeTracker.rimThickness", 4f);

    {
        setPropertyTooltip("rimThickness", "The pupil and iris disc thicknesses in pixels");
    }

    private float positionMixingFactor = getPrefs().getFloat("EyeTracker.positionMixingFactor", 0.005f);

    {
        setPropertyTooltip("positionMixingFactor", "The mixing factor for eye position, increase to track faster and noisier");
    }

    private float scalingMixingFactor = getPrefs().getFloat("EyeTracker.scalingMixingFactor", 0.001f);

    {
        setPropertyTooltip("scalingMixingFactor", "The mixing factor for scaling the model radius, increase to scale faster");
    }

    private boolean scalingEnabled = getPrefs().getBoolean("EyeTracker.scalingEnabled", false);

    {
        setPropertyTooltip("scalingEnabled", "Enables dynamic scaling of model radius");
    }

    private float qualityMixingFactor = getPrefs().getFloat("EyeTracker.qualityMixingFactor", 0.05f);

    {
        setPropertyTooltip("qualityMixingFactor", "The mixing factor for quality metric, increase to speed up quality measurement");
    }

    private float qualityThreshold = getPrefs().getFloat("EyeTracker.qualityThreshold", 0.15f);

    {
        setPropertyTooltip("qualityThreshold", "The threshold quality (ratio events inside/outside model) for decreasing rim thickness");
    }

    private float acquisitionMultiplier = getPrefs().getFloat("EyeTracker.acquisitionMultiplier", 2f);

    {
        setPropertyTooltip("acquisitionMultiplier", "The factor to increase radius when tracking is lost");
    }

    private boolean dynamicAcquisitionSize = getPrefs().getBoolean("EyeTracker.dynamicAcquisitionSize", false);

    {
        setPropertyTooltip("dynamicAcquisitionSize", "Enables dynamic acquisition scaling");
    }

    private boolean logDataEnabled = false;

    {
        setPropertyTooltip("logDataEnabled", "logs eye tracker data to the startup folder file called EyeTracker.txt");
    }

    private boolean showGazeEnabled = false;

    private Point2D.Float position = new Point2D.Float();

    float minX, minY, maxX;

    private StatComputer statComputer;

    float maxY;

    private Distance distance = new Distance();

    private TrackignQualityDetector trackingQualityDetector;

    GLU glu;

    boolean hasBlendChecked = false;

    boolean hasBlend = false;

    class Distance {

        float r, theta;

        float dx, dy;
    }

    /** detects quality of tracking by measuring dynamically the ratio of events inside the model (the disk) to events outside. This value
     is normalized by the relative area of the model (the iris disk) to the chip area. Uniformly distributed events should produce just exactly
     the area ratio of events inside the model, resulting in a measure "1"; if more events are inside the model than predicted by its area then the
     quality measure will be larger than 1.
     <p>
     If the ratio inside becomes too low, we increase the model area (the disk rim thicknesses)
     */
    class TrackignQualityDetector {

        float fractionInside = 0;

        float quality = 1;

        /** call this with a flag marking event inside (true) or outside (false) the eye model
         */
        void processEvent(boolean insideModel) {
            if (insideModel) {
                fractionInside = (1 - qualityMixingFactor) * fractionInside + qualityMixingFactor;
            } else {
                fractionInside = (1 - qualityMixingFactor) * fractionInside;
            }
            quality = fractionInside / modelAreaRatio;
        }

        boolean isTrackingOK() {
            return quality > qualityThreshold;
        }

        float getQuality() {
            return quality;
        }
    }

    /** Computes statisitics of tracking, in order to give a measure of gaze
     */
    class StatComputer {

        float maxX = 0, minX = chip.getSizeX();

        float maxY = 0, minY = chip.getSizeY();

        void reset() {
            minX = chip.getSizeX();
            maxX = 0;
            minY = chip.getSizeY();
            maxX = 0;
        }

        void processPosition(Point2D.Float p) {
            if (p.x < minX) minX = p.x; else if (p.x > maxX) maxX = p.x;
            if (p.y < minY) minY = p.y; else if (p.y > maxY) maxY = p.y;
        }

        float getGazeX() {
            return (position.x - minX) / (maxX - minX);
        }

        float getGazeY() {
            return (position.y - minY) / (maxY - minY);
        }
    }

    /** Creates a new instance of EyeTracker
     @param chip the chip we are eye tracking on
     */
    public EyeTracker(AEChip chip) {
        super(chip);
        chip.addObserver(this);
        trackingQualityDetector = new TrackignQualityDetector();
        statComputer = new StatComputer();
        initFilter();
    }

    float chipArea = 1, modelAreaRatio = 1;

    final float PI2 = (float) (Math.PI * 2);

    public void initFilter() {
        chipArea = chip.getSizeX() * chip.getSizeY();
        pupilRadius = getPrefs().getFloat("EyeTracker.pupilRadius", 5f);
        irisRadius = getPrefs().getFloat("EyeTracker.irisRadius", 20f);
        rimThickness = getPrefs().getFloat("EyeTracker.rimThickness", 3f);
        statComputer.reset();
        computeModelArea();
    }

    public synchronized EventPacket filterPacket(EventPacket in) {
        if (!isFilterEnabled()) return in;
        for (Object o : in) {
            BasicEvent ev = (BasicEvent) o;
            processCircleEvent(ev, pupilRadius);
            processCircleEvent(ev, irisRadius);
            scaleModel(distance.r);
        }
        if (isLogDataEnabled()) {
            if (target == null) {
                dataLogger.log(String.format("%d %f %f %f", in.getLastTimestamp(), position.x, position.y, trackingQualityDetector.getQuality()));
            } else {
                if (target.getMousePosition() != null) {
                    dataLogger.log(String.format("%d %f %f %f %f %f %d %d", in.getLastTimestamp(), position.x, position.y, trackingQualityDetector.getQuality(), target.getTargetX(), target.getTargetY(), target.getMousePosition().x, target.getMousePosition().y));
                } else {
                    dataLogger.log(String.format("%d %f %f %f %f %f %d %d", in.getLastTimestamp(), position.x, position.y, trackingQualityDetector.getQuality(), target.getTargetX(), target.getTargetY(), 0, 0));
                }
            }
        }
        return in;
    }

    /** @param ev the event to process
     @param radius the radius of the present circle
     */
    void processCircleEvent(BasicEvent ev, float radius) {
        computeDistanceFromEyeToEvent(ev);
        float outer, inner;
        float rim = getEffectiveRimThickness();
        outer = radius + rim;
        inner = radius - rim;
        if (inner < 0) inner = 0;
        if (distance.r > outer || distance.r < inner) {
            return;
        }
        float dxrim = (float) (distance.dx - Math.cos(distance.theta) * radius);
        float dyrim = (float) (distance.dy - Math.sin(distance.theta) * radius);
        setX(position.x + dxrim * positionMixingFactor);
        setY(position.y + dyrim * positionMixingFactor);
        statComputer.processPosition(position);
    }

    final float maxPupil = 15, maxIris = 25;

    /** scale model size based on event and tracking quality */
    void scaleModel(float eventDistance) {
        if (!scalingEnabled) return;
        if (eventDistance > irisRadius && eventDistance < irisRadius + rimThickness) {
            irisRadius = scaleRadius(eventDistance, irisRadius);
            if (irisRadius > maxIris) irisRadius = maxIris;
            computeModelArea();
            return;
        }
        if (eventDistance < pupilRadius) {
            pupilRadius = scaleRadius(eventDistance, pupilRadius);
            return;
        }
        float avg = (irisRadius + pupilRadius) / 2;
        if (eventDistance > avg) {
            irisRadius = scaleRadius(eventDistance, irisRadius);
            computeModelArea();
        } else {
            pupilRadius = scaleRadius(eventDistance, pupilRadius);
            if (pupilRadius > maxPupil) pupilRadius = maxPupil;
        }
    }

    float outerToInnerAreaRatio(float radius) {
        float thickness = rimThickness / 2;
        float inner = radius - rimThickness / 2;
        if (inner < 0) inner = radius / 2;
        float outer = radius + thickness;
        float ratio = outer / inner;
        return ratio;
    }

    private float scaleRadius(float eventDistance, float oldRadius) {
        float r = outerToInnerAreaRatio(oldRadius);
        float newRadius;
        if (eventDistance > oldRadius) {
            newRadius = oldRadius + scalingMixingFactor * (eventDistance - oldRadius) / r;
        } else {
            newRadius = oldRadius + scalingMixingFactor * (eventDistance - oldRadius) * r;
        }
        if (newRadius <= 1) newRadius = 1;
        return newRadius;
    }

    public Object getFilterState() {
        return null;
    }

    public void resetFilter() {
        initFilter();
    }

    public void update(Observable o, Object arg) {
        initFilter();
    }

    /** does nothing, must be in openGL rendering mode to see results */
    public void annotate(float[][][] frame) {
    }

    GLUquadric eyeQuad;

    public void annotate(GLAutoDrawable drawable) {
        if (!isFilterEnabled()) return;
        float rim = getEffectiveRimThickness();
        GL gl = drawable.getGL();
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
        gl.glLineWidth(3);
        if (glu == null) glu = new GLU();
        if (eyeQuad == null) eyeQuad = glu.gluNewQuadric();
        gl.glPushMatrix();
        {
            gl.glTranslatef(position.x, position.y, 0);
            if (!trackingQualityDetector.isTrackingOK()) {
                gl.glColor4f(1, 0, 0, .3f);
            } else {
                gl.glColor4f(0, 0, 1, .3f);
            }
            glu.gluQuadricDrawStyle(eyeQuad, GLU.GLU_FILL);
            glu.gluDisk(eyeQuad, pupilRadius - rim, pupilRadius + rim, 16, 1);
            gl.glColor4f(0, 0, 1, .7f);
            glu.gluQuadricDrawStyle(eyeQuad, GLU.GLU_FILL);
            glu.gluDisk(eyeQuad, pupilRadius - 1, pupilRadius + 1, 16, 1);
            if (!trackingQualityDetector.isTrackingOK()) {
                gl.glColor4f(1, 0, 0, .3f);
            } else {
                gl.glColor4f(0, 0, 1, .3f);
            }
            glu.gluQuadricDrawStyle(eyeQuad, GLU.GLU_FILL);
            glu.gluDisk(eyeQuad, irisRadius - rim, irisRadius + rim, 16, 1);
            gl.glColor4f(0, 0, 1, .7f);
            glu.gluQuadricDrawStyle(eyeQuad, GLU.GLU_FILL);
            glu.gluDisk(eyeQuad, irisRadius - 1, irisRadius + 1, 16, 1);
        }
        gl.glPopMatrix();
        gl.glLineWidth(5);
        gl.glBegin(GL.GL_LINES);
        {
            final float SCREEN_FRAC_THRESHOLD_QUALITY = 0.1f;
            if (!trackingQualityDetector.isTrackingOK()) {
                gl.glColor3f(1, 0, 0);
                gl.glVertex2f(0, 1);
                gl.glVertex2f(trackingQualityDetector.quality * chip.getSizeX() * SCREEN_FRAC_THRESHOLD_QUALITY, 1);
            } else {
                gl.glColor3f(1, 0, 0);
                gl.glVertex2f(0, 1);
                float f = qualityThreshold * chip.getSizeX() * SCREEN_FRAC_THRESHOLD_QUALITY;
                gl.glVertex2f(f, 1);
                gl.glColor3f(0, 1, 0);
                gl.glVertex2f(f, 1);
                gl.glVertex2f((qualityThreshold + trackingQualityDetector.quality) * chip.getSizeX() * SCREEN_FRAC_THRESHOLD_QUALITY, 1);
            }
        }
        gl.glEnd();
        if (isShowGazeEnabled()) {
            gl.glPushMatrix();
            {
                float gazeX = statComputer.getGazeX() * chip.getSizeX();
                float gazeY = statComputer.getGazeY() * chip.getSizeY();
                gl.glTranslatef(gazeX, gazeY, 0);
                gl.glColor4f(0, 1, 0, .5f);
                glu.gluQuadricDrawStyle(eyeQuad, GLU.GLU_FILL);
                glu.gluDisk(eyeQuad, 0, 5, 16, 1);
            }
            gl.glPopMatrix();
            if (targetFrame != null) {
                target.display();
            }
        }
        if (hasBlend) gl.glDisable(GL.GL_BLEND);
    }

    public void annotate(Graphics2D g) {
    }

    /**
     * computes distance vector from eye position to event, i.e., dx>0, dy=0 and theta (angle) will be zero if ev is to straight right of eye position
     *@param ev the event
     */
    void computeDistanceFromEyeToEvent(BasicEvent ev) {
        distance.dx = ev.x - position.x;
        distance.dy = ev.y - position.y;
        distance.r = (float) Math.sqrt(distance.dx * distance.dx + distance.dy * distance.dy);
        distance.theta = (float) Math.atan2(distance.dy, distance.dx);
        if (distance.r > irisRadius + rimThickness || distance.r < pupilRadius - rimThickness || (distance.r > pupilRadius + rimThickness && distance.r < irisRadius - rimThickness)) {
            trackingQualityDetector.processEvent(false);
        } else {
            trackingQualityDetector.processEvent(true);
        }
    }

    public void setX(float x) {
        if (x < 0) x = 0; else if (x > chip.getSizeX()) x = chip.getSizeX();
        position.x = x;
    }

    public void setY(float y) {
        if (y < 0) y = 0; else if (y > chip.getSizeY()) y = chip.getSizeY();
        position.y = y;
    }

    public float getPupilRadius() {
        return pupilRadius;
    }

    /** Sets initial value of eye (pupil or iris) radius
     @param eyeRadius the radius in pixels
     */
    public void setPupilRadius(float eyeRadius) {
        if (eyeRadius < 1f) eyeRadius = 1f; else if (eyeRadius > chip.getMaxSize() / 2) eyeRadius = chip.getMaxSize() / 2;
        this.pupilRadius = eyeRadius;
        getPrefs().putFloat("EyeTracker.pupilRadius", pupilRadius);
    }

    public float getRimThickness() {
        return rimThickness;
    }

    /** sets thickness of disk of sensitivity around model circles; affects both pupil and iris model. This number
     is summed and subtracted from radius to give disk of sensitivity.
     @param rimThickness the thickness in pixels
     */
    public void setRimThickness(float rimThickness) {
        this.rimThickness = rimThickness;
        if (rimThickness < 1) rimThickness = 1; else if (rimThickness > pupilRadius) rimThickness = pupilRadius;
        getPrefs().putFloat("EyeTracker.rimThickness", rimThickness);
    }

    /** @return effective rim thickness given quality of tracking
     */
    float getEffectiveRimThickness() {
        final float MAX_RIM_THICKNESS = 10;
        if (trackingQualityDetector.isTrackingOK()) {
            return rimThickness;
        } else {
            if (!dynamicAcquisitionSize) {
                return rimThickness * acquisitionMultiplier;
            } else {
                float r = rimThickness * acquisitionMultiplier * qualityThreshold / trackingQualityDetector.quality;
                if (r > MAX_RIM_THICKNESS) r = MAX_RIM_THICKNESS;
                return r;
            }
        }
    }

    public float getPositionMixingFactor() {
        return positionMixingFactor;
    }

    float m1 = 1 - positionMixingFactor;

    /** Sets mixing factor for eye position
     @param mixingFactor 0-1
     */
    public void setPositionMixingFactor(float mixingFactor) {
        if (mixingFactor < 0) mixingFactor = 0;
        if (mixingFactor > 1) mixingFactor = 1f;
        this.positionMixingFactor = mixingFactor;
        m1 = 1 - mixingFactor;
        getPrefs().putFloat("EyeTracker.positionMixingFactor", mixingFactor);
    }

    /** gets "mixing factor" for dynamic scaling of model size. Setting a small value means model size updates slowly
     @return scalingMixingFactor 0-1 value, 0 means no update, 1 means immediate update
     */
    public float getScalingMixingFactor() {
        return scalingMixingFactor;
    }

    /** sets mixing factor for dynamic resizing of eye
     @param factor mixing factor 0-1
     */
    public void setScalingMixingFactor(float factor) {
        this.scalingMixingFactor = factor;
        getPrefs().putFloat("EyeTracker.scalingMixingFactor", scalingMixingFactor);
    }

    public float getIrisRadius() {
        return irisRadius;
    }

    /** Sets radius of outer ring of eye model (the iris)
     @param irisRadius in pixels
     */
    public void setIrisRadius(float irisRadius) {
        this.irisRadius = irisRadius;
        computeModelArea();
        getPrefs().putFloat("EyeTracker.irisRadius", irisRadius);
    }

    public boolean isScalingEnabled() {
        return scalingEnabled;
    }

    /** Enables/disables dynamic scaling. If scaling is enabled, then all sizes are dynamically estimated and updated.
     The <code>scale</code>
     <p>
     @param scalingEnabled true to enable
     */
    public void setScalingEnabled(boolean scalingEnabled) {
        this.scalingEnabled = scalingEnabled;
        getPrefs().putBoolean("EyeTracker.scalingEnabled", scalingEnabled);
    }

    public boolean isShowGazeEnabled() {
        return showGazeEnabled;
    }

    EyeTarget target;

    JFrame targetFrame;

    /** If enabled, shows a measure of gaze based on statistics of measured eye position
     @param showGazeEnabled true to enable gaze point
     */
    public void setShowGazeEnabled(boolean showGazeEnabled) {
        this.showGazeEnabled = showGazeEnabled;
        if (showGazeEnabled) {
            checkTarget();
            targetFrame.setVisible(true);
            target.display();
        } else {
            targetFrame.setVisible(false);
        }
    }

    void checkTarget() {
        if (target == null) {
            target = new EyeTarget();
            targetFrame = new JFrame("EyeTarget");
            targetFrame.getContentPane().add(target);
            targetFrame.pack();
        }
    }

    public void setTargetSpeed(float speed) {
        target.setTargetSpeed(speed);
    }

    public float getTargetSpeed() {
        checkTarget();
        return target.getTargetSpeed();
    }

    /** Returns the present location of the eye in pixels
     @return the position in pixels
     */
    public Point2D.Float getPosition() {
        return position;
    }

    public float getQualityThreshold() {
        return qualityThreshold;
    }

    /**
     * Sets the threshold for good tracking. If fraction of events inside the model (the small-sized optimal model) falls below this number
     *     then tracking is regarded as poor.
     *
     * @param qualityThreshold the fraction inside model for good tracking
     */
    public void setQualityThreshold(float qualityThreshold) {
        this.qualityThreshold = qualityThreshold;
        getPrefs().putFloat("EyeTracker.qualityThreshold", qualityThreshold);
    }

    public boolean isAnnotationEnabled() {
        return true;
    }

    public float getAcquisitionMultiplier() {
        return acquisitionMultiplier;
    }

    /** Sets the factor by which the model area is increased when low quality tracking is detected.
     Depends on setting of {@link #setDynamicAcquisitionSize} method
     @param acquisitionMultiplier the ratio of areas; the ratio between disk radius during bad tracking to that during good tracking.
     */
    public void setAcquisitionMultiplier(float acquisitionMultiplier) {
        if (acquisitionMultiplier < 1) acquisitionMultiplier = 1;
        this.acquisitionMultiplier = acquisitionMultiplier;
        getPrefs().putFloat("EyeTracker.acquisitionMultiplier", acquisitionMultiplier);
    }

    public float getQualityMixingFactor() {
        return qualityMixingFactor;
    }

    /** Sets the "mixing factor" for tracking quality measure. A low value means that the metric updates slowly -- is more lowpassed. If the mixing
     factor is set to 1 then the measure is instantaneous.
     @param qualityMixingFactor the mixing factor 0-1
     */
    public void setQualityMixingFactor(float qualityMixingFactor) {
        if (qualityMixingFactor < 0) qualityMixingFactor = 0; else if (qualityMixingFactor > 1) qualityMixingFactor = 1;
        this.qualityMixingFactor = qualityMixingFactor;
        getPrefs().putFloat("EyeTracker.qualityMixingFactor", qualityMixingFactor);
    }

    public boolean isDynamicAcquisitionSize() {
        return dynamicAcquisitionSize;
    }

    /**
     * Sets whether to use dynamic resizing of acquisition model size, depending on quality of tracking. If tracking quality is poor (support for model is small
     *     fraction of total events (defined by qualityThreshold)), then model area is increased proportionally.
     *
     * @param dynamicAcquisitionSize true to enable dynamic proportional resizing of model
     */
    public void setDynamicAcquisitionSize(boolean dynamicAcquisitionSize) {
        this.dynamicAcquisitionSize = dynamicAcquisitionSize;
        getPrefs().putBoolean("EyeTracker.dynamicAcquisitionSize", dynamicAcquisitionSize);
    }

    private void computeModelArea() {
        modelAreaRatio = irisRadius * irisRadius * 3.1415f / chipArea;
    }

    EventFilterDataLogger dataLogger;

    public boolean isLogDataEnabled() {
        return logDataEnabled;
    }

    public synchronized void setLogDataEnabled(boolean logDataEnabled) {
        this.logDataEnabled = logDataEnabled;
        if (dataLogger == null) dataLogger = new EventFilterDataLogger(this, "# lasttimestamp eye.x eye.y quality target.x target.y targetMouse.x targetMouse.y ");
        dataLogger.setEnabled(logDataEnabled);
    }
}
