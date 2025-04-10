package com.googlecode.javacv.procamtracker;

import com.googlecode.javacv.BaseSettings;
import com.googlecode.javacv.BaseChildSettings;
import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.FrameGrabber.ImageMode;
import com.googlecode.javacv.FFmpegFrameGrabber;
import com.googlecode.javacv.JavaCV;
import com.googlecode.javacv.MarkedPlane;
import com.googlecode.javacv.Marker;
import com.googlecode.javacv.MarkerDetector;
import com.googlecode.javacv.ObjectFinder;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.Parallel;
import com.googlecode.javacv.ProCamTransformer;
import com.googlecode.javacv.ProjectiveDevice;
import com.googlecode.javacv.ProjectiveTransformer;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.SinglePixelPackedSampleModel;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static com.googlecode.javacv.cpp.avutil.*;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;

/**
 *
 * @author Samuel Audet
 */
public class RealityAugmentor {

    public RealityAugmentor(Settings settings, ObjectFinder.Settings objectFinderSettings, MarkerDetector.Settings markerDetectorSettings, VirtualBall.Settings virtualBallSettings, ProjectiveDevice camera, ProjectiveDevice projector, int channels) throws Exception {
        setSettings(settings);
        this.objectFinderSettings = objectFinderSettings;
        this.markerDetectorSettings = markerDetectorSettings;
        this.virtualBallSettings = virtualBallSettings;
        this.camera = camera;
        this.projector = projector;
        this.channels = channels;
    }

    public static class Settings extends BaseSettings implements CleanBeanNode.ActionableBean {

        @Override
        public ObjectSettings[] toArray() {
            return (ObjectSettings[]) toArray(new ObjectSettings[size()]);
        }

        public Action[] actions() {
            return new Action[] { new AbstractAction("New ObjectSettings") {

                public void actionPerformed(ActionEvent e) {
                    ObjectSettings os = new ObjectSettings();
                    os.setName("ObjectSettings " + size());
                    add(os);
                }
            } };
        }
    }

    public static enum RoiAcquisitionMethod {

        MOUSE_CLICKS, OBJECT_FINDER, MARKER_DETECTOR, WHOLE_FRAME, HALF_FRAME
    }

    public static class ObjectSettings extends BaseSettings implements CleanBeanNode.ActionableBean {

        String name = "ObjectSettings";

        File textureImageFile = null;

        RoiAcquisitionMethod roiAcquisitionMethod = RoiAcquisitionMethod.MOUSE_CLICKS;

        boolean surfaceHasTexture = true;

        @Override
        public String getName() {
            return name;
        }

        public void setName(String name) {
            firePropertyChange("name", this.name, this.name = name);
        }

        public File getTextureImageFile() {
            return textureImageFile;
        }

        public void setTextureImageFile(File textureImageFile) {
            this.textureImageFile = textureImageFile;
        }

        public String getTextureImageFilename() {
            return textureImageFile == null ? "" : textureImageFile.getPath();
        }

        public void setTextureImageFilename(String textureImageFilename) {
            this.textureImageFile = textureImageFilename == null || textureImageFilename.length() == 0 ? null : new File(textureImageFilename);
        }

        public RoiAcquisitionMethod getRoiAcquisitionMethod() {
            return roiAcquisitionMethod;
        }

        public void setRoiAcquisitionMethod(RoiAcquisitionMethod roiAcquisitionMethod) {
            this.roiAcquisitionMethod = roiAcquisitionMethod;
        }

        public boolean isSurfaceHasTexture() {
            return surfaceHasTexture;
        }

        public void setSurfaceHasTexture(boolean surfaceHasTexture) {
            this.surfaceHasTexture = surfaceHasTexture;
        }

        @Override
        public VirtualSettings[] toArray() {
            return (VirtualSettings[]) toArray(new VirtualSettings[size()]);
        }

        public Action[] actions() {
            return new Action[] { new AbstractAction("New VirtualSettings") {

                public void actionPerformed(ActionEvent e) {
                    VirtualSettings vs = new VirtualSettings();
                    vs.setName("VirtualSettings " + size());
                    add(vs);
                }
            }, new AbstractAction("Delete") {

                public void actionPerformed(ActionEvent e) {
                    getBeanContext().remove(ObjectSettings.this);
                }
            } };
        }
    }

    public static enum ProjectionType {

        TRACKED, FIXED
    }

    public static class VirtualSettings extends BaseChildSettings implements CleanBeanNode.ActionableBean {

        String name = "VirtualSettings";

        Rectangle objectHotSpot = new Rectangle();

        int desktopScreenNumber = -1;

        int desktopScreenWidth = 640;

        int desktopScreenHeight = 452;

        File projectorImageFile = null;

        File projectorVideoFile = null;

        ProjectionType projectionType = ProjectionType.TRACKED;

        Rectangle chronometerBounds = new Rectangle(0, -50, 150, 50);

        boolean virtualBallEnabled = false;

        @Override
        public String getName() {
            return name;
        }

        public void setName(String name) {
            firePropertyChange("name", this.name, this.name = name);
        }

        public Rectangle getObjectHotStop() {
            return objectHotSpot;
        }

        public void setObjectHotStop(Rectangle objectHotSpot) {
            this.objectHotSpot = objectHotSpot;
        }

        public int getDesktopScreenNumber() {
            return desktopScreenNumber;
        }

        public void setDesktopScreenNumber(int desktopScreenNumber) {
            this.desktopScreenNumber = desktopScreenNumber;
        }

        public int getDesktopScreenWidth() {
            return desktopScreenWidth;
        }

        public void setDesktopScreenWidth(int desktopScreenWidth) {
            this.desktopScreenWidth = desktopScreenWidth;
        }

        public int getDesktopScreenHeight() {
            return desktopScreenHeight;
        }

        public void setDesktopScreenHeight(int desktopScreenHeight) {
            this.desktopScreenHeight = desktopScreenHeight;
        }

        public File getProjectorImageFile() {
            return projectorImageFile;
        }

        public void setProjectorImageFile(File projectorImageFile) {
            this.projectorImageFile = projectorImageFile;
        }

        public String getProjectorImageFilename() {
            return projectorImageFile == null ? "" : projectorImageFile.getPath();
        }

        public void setProjectorImageFilename(String projectorImageFilename) {
            this.projectorImageFile = projectorImageFilename == null || projectorImageFilename.length() == 0 ? null : new File(projectorImageFilename);
        }

        public File getProjectorVideoFile() {
            return projectorVideoFile;
        }

        public void setProjectorVideoFile(File projectorVideoFile) {
            this.projectorVideoFile = projectorVideoFile;
        }

        public String getProjectorVideoFilename() {
            return projectorVideoFile == null ? "" : projectorVideoFile.getPath();
        }

        public void setProjectorVideoFilename(String projectorVideoFilename) {
            this.projectorVideoFile = projectorVideoFilename == null || projectorVideoFilename.length() == 0 ? null : new File(projectorVideoFilename);
        }

        public ProjectionType getProjectionType() {
            return projectionType;
        }

        public void setProjectionType(ProjectionType projectionType) {
            this.projectionType = projectionType;
        }

        public Rectangle getChronometerBounds() {
            return chronometerBounds;
        }

        public void setChronometerBounds(Rectangle chronometerBounds) {
            this.chronometerBounds = chronometerBounds;
        }

        public boolean isVirtualBallEnabled() {
            return virtualBallEnabled;
        }

        public void setVirtualBallEnabled(boolean virtualBallEnabled) {
            this.virtualBallEnabled = virtualBallEnabled;
        }

        public Action[] actions() {
            return new Action[] { new AbstractAction("Delete") {

                public void actionPerformed(ActionEvent e) {
                    getBeanContext().remove(VirtualSettings.this);
                }
            } };
        }
    }

    private Settings settings;

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    private ObjectSettings objectSettings = null;

    public ObjectSettings getObjectSettings() {
        return objectSettings;
    }

    public void setObjectSettings(ObjectSettings objectSettings) {
        this.objectSettings = objectSettings;
    }

    private VirtualSettings virtualSettings = null;

    public VirtualSettings getVirtualSettings() {
        return virtualSettings;
    }

    public void setVirtualSettings(VirtualSettings virtualSettings) {
        this.virtualSettings = virtualSettings;
    }

    private ObjectFinder.Settings objectFinderSettings;

    private MarkerDetector.Settings markerDetectorSettings;

    private VirtualBall.Settings virtualBallSettings;

    private ProjectiveDevice camera, projector;

    private int channels;

    private double[] roiPts = null;

    private MarkerDetector markerDetector = null;

    private GraphicsDevice desktopScreen = null;

    private Robot robot = null;

    private BufferedImage handMouseCursor = null;

    private FrameGrabber videoToProject = null;

    private IplImage imageToProject = null, textureImage = null;

    private Chronometer chronometer = null;

    private VirtualBall virtualBall = null;

    private ProjectiveTransformer composeWarper = new ProjectiveTransformer();

    private ProjectiveTransformer.Parameters[] composeParameters = { composeWarper.createParameters() };

    private CvMat srcPts = CvMat.create(4, 1, CV_64F, 2), dstPts = CvMat.create(4, 1, CV_64F, 2);

    private CvMat tempH = CvMat.create(3, 3);

    private CvPoint tempPts = new CvPoint(4), corners = new CvPoint(4), corners2 = new CvPoint(corners);

    private CvRect roi = new CvRect(), maxroi = new CvRect();

    private CvBox2D box = new CvBox2D();

    private CvMat boxPts = CvMat.create(4, 1, CV_32F, 2);

    private CvPoint2D32f boxPtsData = new CvPoint2D32f(boxPts.data_fl());

    private double markerError = 0;

    private int markerErrorCount = 0;

    private static final Logger logger = Logger.getLogger(RealityAugmentor.class.getName());

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private Future<CvRect> future = null;

    public void initVirtualSettings() throws Exception {
        desktopScreen = null;
        robot = null;
        handMouseCursor = null;
        videoToProject = null;
        imageToProject = null;
        chronometer = null;
        virtualBall = null;
        if (virtualSettings.desktopScreenNumber < 0 && virtualSettings.projectorImageFile == null && virtualSettings.projectorVideoFile == null) {
            imageToProject = IplImage.create(projector.imageWidth, projector.imageHeight, IPL_DEPTH_8U, channels);
            IplImage tempFloat = IplImage.create(projector.imageWidth, projector.imageHeight, IPL_DEPTH_32F, channels);
            projector.getRectifyingHomography(camera, tempH);
            JavaCV.fractalTriangleWave(tempFloat, tempH);
            cvConvertScale(tempFloat, imageToProject, 255, 0);
        } else if (virtualSettings.desktopScreenNumber >= 0) {
            desktopScreen = CanvasFrame.getScreenDevice(virtualSettings.desktopScreenNumber);
            robot = new Robot(desktopScreen);
            int w = virtualSettings.desktopScreenWidth, h = virtualSettings.desktopScreenHeight;
            if (w <= 0 || h <= 0) {
                DisplayMode dm = desktopScreen.getDisplayMode();
                w = dm.getWidth();
                h = dm.getHeight();
            }
            try {
                videoToProject = new FFmpegFrameGrabber(":0." + virtualSettings.desktopScreenNumber);
                videoToProject.setFormat("x11grab");
                videoToProject.setImageWidth(w);
                videoToProject.setImageHeight(h);
                videoToProject.setFrameRate(30);
                switch(channels) {
                    case 1:
                        videoToProject.setPixelFormat(PIX_FMT_GRAY8);
                        break;
                    case 3:
                        videoToProject.setPixelFormat(PIX_FMT_BGR24);
                        break;
                    case 4:
                        videoToProject.setPixelFormat(PIX_FMT_RGBA);
                        break;
                    default:
                        assert false;
                }
                videoToProject.start();
                imageToProject = null;
            } catch (FrameGrabber.Exception e) {
                videoToProject = null;
                imageToProject = IplImage.create(w, h, IPL_DEPTH_8U, channels);
            }
            handMouseCursor = ImageIO.read(getClass().getResource("icons/Choose.png"));
        } else if (virtualSettings.projectorVideoFile != null) {
            if (virtualSettings.projectorImageFile != null) {
                imageToProject = IplImage.createFrom(ImageIO.read(virtualSettings.projectorImageFile), 1.0, true);
                if (imageToProject == null) {
                    throw new Exception("Error: Could not load projectorImageFile named \"" + virtualSettings.projectorImageFile + "\".");
                }
                final ByteBuffer buf = imageToProject.getByteBuffer();
                final int width = imageToProject.width();
                final int height = imageToProject.height();
                final int step = imageToProject.widthStep();
                final int channels = imageToProject.nChannels();
                for (int y = 0; y < height; y++) {
                    int pixel = y * step;
                    for (int x = 0; x < width; x++, pixel += channels) {
                        switch(channels) {
                            default:
                                assert false;
                            case 4:
                            case 3:
                                buf.put(pixel + 2, (byte) IplImage.decodeGamma22(buf.get(pixel + 2)));
                            case 2:
                                buf.put(pixel + 1, (byte) IplImage.decodeGamma22(buf.get(pixel + 1)));
                            case 1:
                                buf.put(pixel + 0, (byte) IplImage.decodeGamma22(buf.get(pixel + 0)));
                        }
                    }
                }
            }
            try {
                videoToProject = new FFmpegFrameGrabber(virtualSettings.projectorVideoFile);
            } catch (Throwable t) {
                videoToProject = new OpenCVFrameGrabber(virtualSettings.projectorVideoFile);
            }
            if (videoToProject != null) {
                videoToProject.setImageMode(ImageMode.COLOR);
                switch(channels) {
                    case 1:
                        videoToProject.setPixelFormat(PIX_FMT_GRAY8);
                        break;
                    case 3:
                        videoToProject.setPixelFormat(PIX_FMT_BGR24);
                        break;
                    case 4:
                        videoToProject.setPixelFormat(PIX_FMT_RGBA);
                        break;
                    default:
                        assert false;
                }
                if (imageToProject != null) {
                    videoToProject.setImageWidth(imageToProject.width());
                    videoToProject.setImageHeight(imageToProject.height());
                }
                videoToProject.start();
            }
        } else if (virtualSettings.projectorImageFile != null) {
            imageToProject = channels == 4 ? cvLoadImageRGBA(virtualSettings.projectorImageFile.getAbsolutePath()) : cvLoadImage(virtualSettings.projectorImageFile.getAbsolutePath(), channels == 3 ? CV_LOAD_IMAGE_COLOR : CV_LOAD_IMAGE_GRAYSCALE);
            if (imageToProject == null) {
                throw new Exception("Error: Could not load projectorImageFile named \"" + virtualSettings.projectorImageFile + "\".");
            }
            imageToProject.applyGamma(2.2);
        }
    }

    public double[] acquireRoi(CanvasFrame monitorWindow, double monitorWindowScale, IplImage cameraImage, int pyramidLevel) throws Exception {
        final int w = cameraImage.width();
        final int h = cameraImage.height();
        roiPts = null;
        markerError = 0;
        markerErrorCount = 0;
        for (ObjectSettings os : settings.toArray()) {
            File f = os.textureImageFile;
            RoiAcquisitionMethod ram = os.roiAcquisitionMethod;
            if ((ram == RoiAcquisitionMethod.OBJECT_FINDER || ram == RoiAcquisitionMethod.MARKER_DETECTOR) && (f == null || (textureImage = cvLoadImage(f.getAbsolutePath())) == null)) {
                throw new Exception("Error: Could not load the object image file \"" + f + "\" for " + ram + ".");
            }
            switch(ram) {
                case MOUSE_CLICKS:
                    roiPts = acquireRoiFromMouseClicks(monitorWindow, monitorWindowScale);
                    break;
                case OBJECT_FINDER:
                    roiPts = acquireRoiFromObjectFinder(cameraImage);
                    break;
                case MARKER_DETECTOR:
                    roiPts = acquireRoiFromMarkerDetector(cameraImage);
                    break;
                case WHOLE_FRAME:
                    roiPts = new double[] { 0.0, 0.0, w, 0.0, w, h, 0.0, h };
                    break;
                case HALF_FRAME:
                    double dw = w * (2 - JavaCV.SQRT2) / 4;
                    double dh = h * (2 - JavaCV.SQRT2) / 4;
                    roiPts = new double[] { dw, dh, w - dw, dh, w - dw, h - dh, dw, h - dh };
                    break;
                default:
                    assert false;
            }
            if (roiPts != null) {
                if (pyramidLevel > 0) {
                    for (int i = 0; i < roiPts.length; i++) {
                        roiPts[i] = roiPts[i] * (1 << pyramidLevel);
                    }
                }
                objectSettings = os;
                virtualSettings = null;
                for (VirtualSettings vs : objectSettings.toArray()) {
                    Rectangle r = vs.objectHotSpot;
                    if (r == null || r.width <= 0 || r.height <= 0) {
                        setVirtualSettings(vs);
                        initVirtualSettings();
                    }
                }
                break;
            }
        }
        return roiPts;
    }

    private double[] acquireRoiFromMouseClicks(final CanvasFrame monitorWindow, final double monitorWindowScale) throws Exception {
        if (monitorWindow == null) {
            throw new Exception("Error: No monitor window. Could not acquire ROI from mouse clicks.");
        }
        Toolkit t = Toolkit.getDefaultToolkit();
        Dimension d = t.getBestCursorSize(15, 15);
        BufferedImage cursorImage = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = cursorImage.createGraphics();
        int cx = d.width / 2, cy = d.height / 2;
        g.setColor(Color.WHITE);
        g.drawRect(cx - 7, cy - 7, 14, 14);
        g.setColor(Color.BLACK);
        g.drawRect(cx - 6, cy - 6, 12, 12);
        g.setColor(Color.WHITE);
        g.drawRect(cx - 2, cy - 2, 4, 4);
        g.setColor(Color.BLACK);
        g.drawRect(cx - 1, cy - 1, 2, 2);
        if (d.width % 2 == 0) {
            cx += 1;
        }
        if (d.height % 2 == 0) {
            cy += 1;
        }
        Cursor cursor = t.createCustomCursor(cursorImage, new Point(cx, cy), null);
        monitorWindow.setCursor(cursor);
        final double[] roiPts = new double[8];
        final int[] count = { 0 };
        monitorWindow.getCanvas().addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (count[0] < 8) {
                    roiPts[count[0]++] = e.getX() / monitorWindowScale;
                    roiPts[count[0]++] = e.getY() / monitorWindowScale;
                    Graphics2D g = monitorWindow.createGraphics();
                    g.setColor(Color.RED);
                    g.drawLine(e.getX() - 7, e.getY(), e.getX() + 7, e.getY());
                    g.drawLine(e.getX(), e.getY() - 7, e.getX(), e.getY() + 7);
                    monitorWindow.releaseGraphics(g);
                }
                if (count[0] >= 8) {
                    synchronized (roiPts) {
                        monitorWindow.getCanvas().removeMouseListener(this);
                        monitorWindow.setCursor(null);
                        roiPts.notify();
                    }
                }
            }
        });
        synchronized (roiPts) {
            roiPts.wait();
        }
        return roiPts;
    }

    private double[] acquireRoiFromObjectFinder(IplImage cameraImage) throws Exception {
        IplImage grey1, grey2;
        if (textureImage.depth() == 1) {
            grey1 = textureImage;
        } else {
            grey1 = IplImage.create(textureImage.width(), textureImage.height(), textureImage.depth(), 1);
            cvCvtColor(textureImage, grey1, textureImage.depth() == 4 ? CV_RGBA2GRAY : CV_BGR2GRAY);
        }
        if (cameraImage.depth() == 1) {
            grey2 = cameraImage;
        } else {
            grey2 = IplImage.create(cameraImage.width(), cameraImage.height(), cameraImage.depth(), 1);
            cvCvtColor(cameraImage, grey2, cameraImage.depth() == 4 ? CV_RGBA2GRAY : CV_BGR2GRAY);
        }
        objectFinderSettings.setObjectImage(grey1);
        ObjectFinder objectFinder = new ObjectFinder(objectFinderSettings);
        double[] roiPts = objectFinder.find(grey2);
        if (grey1 != textureImage) {
            grey1.release();
        }
        if (grey2 != cameraImage) {
            grey2.release();
        }
        return roiPts;
    }

    private double[] acquireRoiFromMarkerDetector(IplImage cameraImage) throws Exception {
        markerDetector = new MarkerDetector(markerDetectorSettings);
        Marker[] markersin = markerDetector.detect(textureImage, false);
        String infoLogString = "textureImage marker centers = ";
        for (int i = 0; i < 4; i++) {
            for (Marker m : markersin) {
                if (m.id == i) {
                    double[] c = m.getCenter();
                    infoLogString += m.id + ": (" + (float) c[0] + ", " + (float) c[1] + ")  ";
                    break;
                }
            }
        }
        logger.info(infoLogString);
        if (markersin == null || markersin.length == 0) {
            return null;
        }
        MarkedPlane markedPlane = new MarkedPlane(textureImage.width(), textureImage.height(), markersin, 1);
        Marker[] markersout = markerDetector.detect(cameraImage, false);
        infoLogString = "initial marker centers = ";
        if (markersout == null || markersout.length == 0 || markedPlane.getTotalWarp(markersout, tempH, true) == Double.POSITIVE_INFINITY) {
            return null;
        }
        dstPts.put(0.0, 0.0, textureImage.width(), 0.0, textureImage.width(), textureImage.height(), 0.0, textureImage.height());
        cvPerspectiveTransform(dstPts, dstPts, tempH);
        double[] roiPts = dstPts.get();
        for (int i = 0; i < 4; i++) {
            for (Marker m : markersout) {
                if (m.id == i) {
                    double[] c = m.getCenter();
                    infoLogString += m.id + ": (" + (float) c[0] + ", " + (float) c[1] + ")  ";
                    srcPts.put(i * 2, c[0]);
                    srcPts.put(i * 2 + 1, c[1]);
                    break;
                }
            }
        }
        logger.info(infoLogString);
        return roiPts;
    }

    public IplImage nextFrameImage(int objectMouseX, int objectMouseY, boolean mouseClick) throws Exception {
        IplImage frameImage = imageToProject;
        if (desktopScreen != null && robot != null) {
            int w = videoToProject != null ? videoToProject.getImageWidth() : imageToProject.width();
            int h = videoToProject != null ? videoToProject.getImageHeight() : imageToProject.height();
            final int desktopMouseX = objectMouseX * w / (textureImage != null ? textureImage.width() : projector.imageWidth);
            final int desktopMouseY = objectMouseY * h / (textureImage != null ? textureImage.height() : projector.imageHeight);
            if (desktopMouseX >= 0 && desktopMouseY >= 0) {
                robot.mouseMove(desktopMouseX, desktopMouseY);
                if (mouseClick) {
                    robot.mousePress(InputEvent.BUTTON1_MASK);
                    robot.waitForIdle();
                    robot.mouseRelease(InputEvent.BUTTON1_MASK);
                    robot.waitForIdle();
                }
            }
            if (videoToProject != null) {
                frameImage = videoToProject.grab();
            } else {
                final int dstStep = imageToProject.widthStep();
                final int dstChannels = imageToProject.nChannels();
                final ByteBuffer dstBuf = imageToProject.getByteBuffer();
                final IntBuffer dstBufInt = dstChannels == 4 ? dstBuf.order(ByteOrder.LITTLE_ENDIAN).asIntBuffer() : null;
                final int dstWidth = imageToProject.width();
                final int dstHeight = imageToProject.height();
                BufferedImage screenCapture = robot.createScreenCapture(new Rectangle(dstWidth, dstHeight));
                if (desktopMouseX >= 0 && desktopMouseY >= 0) {
                    Graphics2D g = screenCapture.createGraphics();
                    g.drawImage(handMouseCursor, desktopMouseX, desktopMouseY, null);
                }
                final int srcStep = ((SinglePixelPackedSampleModel) screenCapture.getSampleModel()).getScanlineStride();
                final int[] srcData = ((DataBufferInt) screenCapture.getRaster().getDataBuffer()).getData();
                Parallel.loop(0, dstHeight, new Parallel.Looper() {

                    public void loop(int from, int to, int looperID) {
                        for (int y = from; y < to; y++) {
                            int srcPixel = y * srcStep;
                            int dstPixel = y * dstStep;
                            for (int x = 0; x < dstWidth; x++, srcPixel++, dstPixel += dstChannels) {
                                int rgb = srcData[srcPixel];
                                switch(dstChannels) {
                                    case 1:
                                        int lumi = (IplImage.decodeGamma22((rgb >> 16) & 0xFF) + IplImage.decodeGamma22((rgb >> 8) & 0xFF) + IplImage.decodeGamma22((rgb) & 0xFF)) / 3;
                                        dstBuf.put(dstPixel, (byte) lumi);
                                        break;
                                    case 3:
                                        dstBuf.put(dstPixel + 0, (byte) IplImage.decodeGamma22((rgb) & 0xFF));
                                        dstBuf.put(dstPixel + 1, (byte) IplImage.decodeGamma22((rgb >> 8) & 0xFF));
                                        dstBuf.put(dstPixel + 2, (byte) IplImage.decodeGamma22((rgb >> 16) & 0xFF));
                                        break;
                                    case 4:
                                        int rgba = (IplImage.decodeGamma22((rgb >> 16) & 0xFF)) | (IplImage.decodeGamma22((rgb >> 8) & 0xFF) << 8) | (IplImage.decodeGamma22((rgb) & 0xFF) << 16) | (0xFF << 24);
                                        dstBufInt.put(dstPixel / 4, rgba);
                                        break;
                                    default:
                                        assert false;
                                }
                            }
                        }
                    }
                });
            }
        } else if (videoToProject != null) {
            frameImage = videoToProject.grab();
            if (frameImage == null) {
                videoToProject.restart();
                frameImage = videoToProject.grab();
            }
            if (imageToProject == null) {
            } else {
                int w = Math.min(imageToProject.width(), frameImage.width());
                int h = Math.min(imageToProject.height(), frameImage.height());
                IplROI srcRoi = imageToProject.roi();
                final int srcStep = imageToProject.widthStep(), dstStep = frameImage.widthStep();
                final int srcChannels = imageToProject.nChannels(), dstChannels = frameImage.nChannels();
                int srcIndex = 0, dstIndex = 0;
                if (srcRoi != null) {
                    srcIndex = srcRoi.yOffset() * srcStep + srcRoi.xOffset() * srcChannels;
                    dstIndex = srcRoi.yOffset() * dstStep + srcRoi.xOffset() * dstChannels;
                    w = srcRoi.width();
                    h = srcRoi.height();
                }
                final ByteBuffer srcBuf = imageToProject.getByteBuffer(srcIndex);
                final ByteBuffer dstBuf = frameImage.getByteBuffer(dstIndex);
                final IntBuffer srcBufInt = srcChannels == 4 ? srcBuf.order(ByteOrder.LITTLE_ENDIAN).asIntBuffer() : null;
                final IntBuffer dstBufInt = dstChannels == 4 ? dstBuf.order(ByteOrder.LITTLE_ENDIAN).asIntBuffer() : null;
                final int width = w;
                final int height = h;
                Parallel.loop(0, height, new Parallel.Looper() {

                    public void loop(int from, int to, int looperID) {
                        for (int y = from; y < to; y++) {
                            int srcPixel = y * srcStep;
                            int dstPixel = y * dstStep;
                            for (int x = 0; x < width; x++, srcPixel += srcChannels, dstPixel += dstChannels) {
                                int r = 0, g = 0, b = 0, a = 128;
                                switch(srcChannels) {
                                    case 1:
                                        r = g = b = srcBuf.get(srcPixel) & 0xFF;
                                        break;
                                    case 3:
                                        b = srcBuf.get(srcPixel) & 0xFF;
                                        g = srcBuf.get(srcPixel + 1) & 0xFF;
                                        r = srcBuf.get(srcPixel + 2) & 0xFF;
                                        break;
                                    case 4:
                                        int rgba = srcBufInt.get(srcPixel / 4);
                                        r = (rgba) & 0xFF;
                                        g = (rgba >> 8) & 0xFF;
                                        b = (rgba >> 16) & 0xFF;
                                        a = (rgba >> 24) & 0xFF;
                                        break;
                                    default:
                                        assert false;
                                }
                                switch(dstChannels) {
                                    case 1:
                                        int lumi = (r + g + b) / 3;
                                        dstBuf.put(dstPixel, (byte) ((lumi * a + IplImage.decodeGamma22(dstBuf.get(dstPixel)) * (255 - a)) / 255));
                                        break;
                                    case 3:
                                        dstBuf.put(dstPixel, (byte) ((b * a + IplImage.decodeGamma22(dstBuf.get(dstPixel)) * (255 - a)) / 255));
                                        dstBuf.put(dstPixel + 1, (byte) ((g * a + IplImage.decodeGamma22(dstBuf.get(dstPixel + 1)) * (255 - a)) / 255));
                                        dstBuf.put(dstPixel + 2, (byte) ((r * a + IplImage.decodeGamma22(dstBuf.get(dstPixel + 2)) * (255 - a)) / 255));
                                        break;
                                    case 4:
                                        int rgba = dstBufInt.get(dstPixel / 4);
                                        r = (r * a + IplImage.decodeGamma22((rgba) & 0xFF) * (255 - a)) / 255;
                                        g = (g * a + IplImage.decodeGamma22((rgba >> 8) & 0xFF) * (255 - a)) / 255;
                                        b = (b * a + IplImage.decodeGamma22((rgba >> 16) & 0xFF) * (255 - a)) / 255;
                                        a = 0xFF;
                                        rgba = r | (g << 8) | (b << 16) | (a << 24);
                                        dstBufInt.put(dstPixel / 4, rgba);
                                        break;
                                    default:
                                        assert false;
                                }
                            }
                        }
                    }
                });
            }
        }
        Rectangle r = virtualSettings.chronometerBounds;
        if (r == null || r.width <= 0 || r.height <= 0) {
            chronometer = null;
        } else if (chronometer == null) {
            chronometer = new Chronometer(r, frameImage.getBufferedImageType());
        }
        if (chronometer != null) {
            chronometer.draw(frameImage);
        }
        return frameImage;
    }

    public boolean needsMouse() {
        if (virtualSettings != null && virtualSettings.desktopScreenNumber >= 0) {
            return true;
        }
        for (VirtualSettings vs : objectSettings.toArray()) {
            Rectangle r = vs.objectHotSpot;
            if (r != null && r.width > 0 && r.height > 0) {
                return true;
            }
        }
        return false;
    }

    public void update(final IplImage projectorImage, final CvRect prevroi, final double imageMouseX, final double imageMouseY, final boolean mouseClick, final ProCamTransformer.Parameters parameters) throws Exception {
        future = executor.submit(new Callable<CvRect>() {

            public CvRect call() throws Exception {
                int objectMouseX = -1, objectMouseY = -1;
                if (imageMouseX >= 0 && imageMouseY >= 0 && textureImage != null) {
                    int w = textureImage.width(), h = textureImage.height();
                    double[] pts = { 0.0, 0.0, w, 0.0, w, h, 0.0, h };
                    JavaCV.getPerspectiveTransform(pts, roiPts, tempH);
                    composeParameters[0].compose(parameters.getSurfaceParameters().getH(), false, tempH, false);
                    composeWarper.transform(dstPts.put(imageMouseX, imageMouseY), dstPts, composeParameters[0], true);
                    objectMouseX = (int) Math.round(dstPts.get(0));
                    objectMouseY = (int) Math.round(dstPts.get(1));
                }
                if (mouseClick) {
                    for (VirtualSettings vs : objectSettings.toArray()) {
                        Rectangle r = vs.objectHotSpot;
                        if (r != null && r.contains(objectMouseX, objectMouseY)) {
                            setVirtualSettings(vs);
                            initVirtualSettings();
                            break;
                        }
                    }
                }
                int pw = projectorImage.width(), ph = projectorImage.height();
                double[] projPts = { 0.0, 0.0, pw, 0.0, pw, ph, 0.0, ph };
                if (virtualSettings == null) {
                    cvSet(projectorImage, CvScalar.WHITE);
                    maxroi.x(0).y(0).width(pw).height(ph);
                    return null;
                }
                if (!objectSettings.surfaceHasTexture) {
                    projector.getFrontoParallelH(projPts, parameters.getN(), tempH);
                    cvMatMul(projector.cameraMatrix, tempH, tempH);
                    cvInvert(tempH, tempH);
                    cvPerspectiveTransform(dstPts.put(projPts), dstPts, tempH);
                    box.angle(0).size().width(pw).height(ph);
                    JavaCV.boundedRect(boxPts.put(dstPts), box);
                    cvBoxPoints(box, boxPtsData);
                    CvPoint2D32f center = box.center();
                    double centerX = center.x();
                    double centerY = center.y();
                    for (int i = 0; i < 4; i++) {
                        double x = boxPts.get(2 * i);
                        double y = boxPts.get(2 * i + 1);
                        if (x > centerX) {
                            if (y > centerY) {
                                dstPts.put(0, x).put(1, y);
                            } else {
                                dstPts.put(6, x).put(7, y);
                            }
                        } else {
                            if (y > centerY) {
                                dstPts.put(2, x).put(3, y);
                            } else {
                                dstPts.put(4, x).put(5, y);
                            }
                        }
                    }
                    cvInvert(tempH, tempH);
                    cvPerspectiveTransform(dstPts, dstPts, tempH);
                    JavaCV.getPerspectiveTransform(projPts, dstPts.get(), tempH);
                    composeParameters[0].set(tempH, false);
                    if (imageMouseX >= 0 && imageMouseY >= 0) {
                        composeParameters[0].compose(parameters.getProjectorParameters(), false, composeParameters[0], false);
                        composeWarper.transform(dstPts.put(imageMouseX, imageMouseY), dstPts, composeParameters[0], true);
                        objectMouseX = (int) Math.round(dstPts.get(0));
                        objectMouseY = (int) Math.round(dstPts.get(1));
                    }
                }
                IplImage frameImage = nextFrameImage(objectMouseX, objectMouseY, mouseClick);
                if (virtualSettings.projectionType == ProjectionType.TRACKED) {
                    int w = frameImage.width(), h = frameImage.height();
                    double[] framePts = { 0.0, 0.0, w, 0.0, w, h, 0.0, h };
                    if (objectSettings.surfaceHasTexture) {
                        JavaCV.getPerspectiveTransform(framePts, roiPts, tempH);
                        composeParameters[0].compose(parameters.getProjectorParameters(), true, parameters.getSurfaceParameters(), false);
                        composeParameters[0].compose(composeParameters[0].getH(), false, tempH, false);
                    } else {
                        composeParameters[0].set(tempH, false);
                        JavaCV.getPerspectiveTransform(framePts, projPts, tempH);
                        composeParameters[0].compose(composeParameters[0].getH(), false, tempH, false);
                    }
                    composeWarper.transform(dstPts.put(framePts), dstPts, composeParameters[0], false);
                    composeWarper.setFillColor(CvScalar.WHITE);
                    if (prevroi == null) {
                        composeWarper.transform(frameImage, projectorImage, null, 0, composeParameters[0], false);
                    } else {
                        roi.x(0).y(0).width(projectorImage.width()).height(projectorImage.height());
                        JavaCV.boundingRect(dstPts.get(), roi, 3, 3, 16, 1);
                        maxroi.x(Math.min(prevroi.x(), roi.x()));
                        maxroi.y(Math.min(prevroi.y(), roi.y()));
                        maxroi.width(Math.max(prevroi.x() + prevroi.width(), roi.x() + roi.width()) - maxroi.x());
                        maxroi.height(Math.max(prevroi.y() + prevroi.height(), roi.y() + roi.height()) - maxroi.y());
                        composeWarper.transform(frameImage, projectorImage, maxroi, 0, composeParameters[0], false);
                        prevroi.x(roi.x()).y(roi.y()).width(roi.width()).height(roi.height());
                    }
                } else {
                    dstPts.put(0.0, 0.0, pw, 0.0, pw, ph, 0.0, ph);
                    if (frameImage.width() == pw && frameImage.height() == ph) {
                        cvCopy(frameImage, projectorImage);
                    } else {
                        cvResize(frameImage, projectorImage);
                    }
                    maxroi.x(0).y(0).width(pw).height(ph);
                }
                if (!virtualSettings.virtualBallEnabled) {
                    virtualBall = null;
                } else if (virtualBall == null) {
                    virtualBallSettings.setInitialRoiPts(dstPts.get());
                    virtualBall = new VirtualBall(virtualBallSettings);
                }
                if (virtualBall != null) {
                    cvSetImageROI(projectorImage, roi);
                    virtualBall.draw(projectorImage, dstPts.get());
                }
                return prevroi == null ? null : maxroi;
            }
        });
    }

    public CvRect getUpdateRect() throws Exception {
        return future.get();
    }

    public String drawRoi(IplImage monitorImage, int pyramidLevel, IplImage cameraImage, ProCamTransformer transformer, ProCamTransformer.Parameters parameters) {
        String infoLogString = "";
        if (objectSettings.roiAcquisitionMethod == RoiAcquisitionMethod.MARKER_DETECTOR && markerDetector != null) {
            Marker[] markers = new Marker[4];
            boolean missing;
            MarkerDetector.Settings ms = new MarkerDetector.Settings();
            ms.setThresholdKBlackMarkers(0.99);
            do {
                Marker[] detected = markerDetector.detect(cameraImage, false);
                for (Marker m : detected) {
                    if (m.id < markers.length && markers[m.id] == null) {
                        markers[m.id] = m;
                    }
                }
                missing = false;
                for (Marker m : markers) {
                    if (m == null) {
                        missing = true;
                    }
                }
                ms.setThresholdKBlackMarkers(ms.getThresholdKBlackMarkers() - 0.05);
                markerDetector.setSettings(ms);
            } while (missing && ms.getThresholdKBlackMarkers() > 0);
            transformer.transform(srcPts, dstPts, parameters, false);
            infoLogString += "  (";
            for (int j = 0; j < 4; j++) {
                for (Marker m : markers) {
                    if (m != null && m.id == j) {
                        double[] center = m.getCenter();
                        double dx = center[0] * (1 << camera.getMapsPyramidLevel()) - dstPts.get(j * 2);
                        double dy = center[1] * (1 << camera.getMapsPyramidLevel()) - dstPts.get(j * 2 + 1);
                        double error = dx * dx + dy * dy;
                        infoLogString += (float) Math.sqrt(error) + (j < 3 ? ", " : "");
                        markerError += error;
                        markerErrorCount++;
                        corners.put((byte) (16 - pyramidLevel + camera.getMapsPyramidLevel()), m.corners);
                        cvLine(monitorImage, corners.position(0), corners2.position(2), CV_RGB(monitorImage.highValue(), 0, 0), 1, CV_AA, 16);
                        cvLine(monitorImage, corners.position(1), corners2.position(3), CV_RGB(monitorImage.highValue(), 0, 0), 1, CV_AA, 16);
                        break;
                    }
                }
                tempPts.position(j);
                tempPts.x((int) Math.round(dstPts.get(j * 2) * (1 << 16 - pyramidLevel)));
                tempPts.y((int) Math.round(dstPts.get(j * 2 + 1) * (1 << 16 - pyramidLevel)));
            }
            infoLogString += ")  " + (float) Math.sqrt(markerError / markerErrorCount);
            cvPolyLine(monitorImage, tempPts.position(0), new int[] { 4 }, 1, 1, CV_RGB(0, monitorImage.highValue(), 0), 1, CV_AA, 16);
        } else {
            transformer.transform(dstPts.put(roiPts), dstPts, parameters, false);
            tempPts.put((byte) (16 - pyramidLevel), dstPts.get());
            cvPolyLine(monitorImage, tempPts.position(0), new int[] { 4 }, 1, 1, CV_RGB(0, monitorImage.highValue(), 0), 1, CV_AA, 16);
        }
        return infoLogString;
    }
}
