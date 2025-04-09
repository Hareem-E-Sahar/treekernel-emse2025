package javax.media.ding3d.utils.universe;

import java.awt.event.*;
import java.awt.*;
import java.net.URL;
import java.util.*;
import javax.media.ding3d.*;
import javax.swing.*;
import javax.media.ding3d.audioengines.AudioEngine3DL2;
import java.lang.reflect.Constructor;

/**
 * The Viewer class holds all the information that describes the physical
 * and virtual "presence" in the Java 3D universe.  The Viewer object
 * consists of:
 * <UL>
 * <LI>Physical Objects</LI>
 *  <UL>
 *   <LI>Canvas3D's - used to render with.</LI>
 *   <LI>PhysicalEnvironment - holds characteristics of the hardware platform
 *    being used to render on.</LI>
 *   <LI>PhysicalBody -  holds the physical characteristics and personal
 *    preferences of the person who will be viewing the Java 3D universe.</LI>
 *  </UL>
 * <LI>Virtual Objects</LI>
 *  <UL>
 *   <LI>View - the Java 3D View object.</LI>
 *   <LI>ViewerAvatar - the geometry that is used by Java 3D to represent the
 *    person viewing the Java 3D universe.</LI>
 *  </UL>
 * </UL>
 * If the Viewer object is created without any Canvas3D's, or indirectly
 * through a configuration file, it will create the Canvas3D's as needed.
 * The default Viewer creates one Canvas3D.  If the Viewer object creates
 * the Canvas3D's, it will also create a JPanel and JFrame for each Canvas3D.
 *
 * Dynamic video resize is a new feature in Java 3D 1.3.1.
 * This feature provides a means for doing swap synchronous resizing
 * of the area that is to be magnified (or passed through) to the
 * output video resolution. This functionality allows an application
 * to draw into a smaller viewport in the framebuffer in order to reduce
 * the time spent doing pixel fill. The reduced size viewport is then
 * magnified up to the video output resolution using the SUN_video_resize
 * extension. This extension is only implemented in XVR-4000 and later
 * hardware with back end video out resizing capability.
 *
 * If video size compensation is enable, the line widths, point sizes and pixel
 * operations will be scaled internally with the resize factor to approximately
 * compensate for video resizing. The location of the pixel ( x, y ) in the
 * resized framebuffer = ( floor( x * factor + 0.5 ), floor( y * factor + 0.5 ) )
 *
 * <p>
 * @see Canvas3D
 * @see PhysicalEnvironment
 * @see PhysicalBody
 * @see View
 * @see ViewerAvatar
 */
public class Viewer {

    private static final boolean debug = false;

    private static PhysicalBody physicalBody = null;

    private static PhysicalEnvironment physicalEnvironment = null;

    private View view = null;

    private ViewerAvatar avatar = null;

    private Canvas3D[] canvases = null;

    private JFrame[] Ding3dJFrames = null;

    private JPanel[] Ding3dJPanels = null;

    private Window[] Ding3dWindows = null;

    private ViewingPlatform viewingPlatform = null;

    static HashMap viewerMap = new HashMap(5);

    private float dvrFactor = 1.0f;

    private boolean doDvr = false;

    private boolean doDvrResizeCompensation = true;

    public static Viewer getViewer(View view) {
        Viewer viewer = null;
        synchronized (viewerMap) {
            viewer = (Viewer) (viewerMap.get(view));
        }
        return viewer;
    }

    public static Viewer removeViewerMapEntry(View view) {
        Viewer viewer = null;
        synchronized (viewerMap) {
            viewer = (Viewer) (viewerMap.remove(view));
        }
        return viewer;
    }

    public static void clearViewerMap() {
        synchronized (viewerMap) {
            viewerMap.clear();
        }
    }

    public boolean isDvrEnabled() {
        return doDvr;
    }

    public void setDvrEnable(boolean dvr) {
        doDvr = dvr;
        view.repaint();
    }

    public float getDvrFactor() {
        return dvrFactor;
    }

    public void setDvrFactor(float dvr) {
        dvrFactor = dvr;
        view.repaint();
    }

    public void setDvrResizeCompensationEnable(boolean dvrRCE) {
        doDvrResizeCompensation = dvrRCE;
        view.repaint();
    }

    public boolean getDvrResizeCompensationEnable() {
        return doDvrResizeCompensation;
    }

    /**
     * Creates a default viewer object. The default values are used to create
     * the PhysicalBody and PhysicalEnvironment.  A single RGB, double buffered
     * and depth buffered Canvas3D object is created.  The View is created
     * with a front clip distance of 0.1f and a back clip distance of 10.0f.
     */
    public Viewer() {
        this(null, null, null, true);
    }

    /**
     * Creates a default viewer object. The default values are used to create
     * the PhysicalBody and PhysicalEnvironment.  The View is created
     * with a front clip distance of 0.1f and a back clip distance of 10.0f.
     *
     * @param userCanvas the Canvas3D object to be used for rendering;
     *  if this is null then a single RGB, double buffered and depth buffered
     *  Canvas3D object is created
     * @since Java3D 1.1
     */
    public Viewer(Canvas3D userCanvas) {
        this(userCanvas == null ? null : new Canvas3D[] { userCanvas }, null, null, true);
    }

    /**
     * Creates a default viewer object. The default values are used to create
     * the PhysicalBody and PhysicalEnvironment.  The View is created
     * with a front clip distance of 0.1f and a back clip distance of 10.0f.
     *
     * @param userCanvases the Canvas3D objects to be used for rendering;
     *  if this is null then a single RGB, double buffered and depth buffered
     *  Canvas3D object is created
     * @since Java3D 1.3
     */
    public Viewer(Canvas3D[] userCanvases) {
        this(userCanvases, null, null, true);
    }

    /**
     * Creates a viewer object. The Canvas3D objects, PhysicalEnvironment, and
     * PhysicalBody are taken from the arguments.
     *
     * @param userCanvases the Canvas3D objects to be used for rendering;
     *  if this is null then a single RGB, double buffered and depth buffered
     *  Canvas3D object is created
     * @param userBody the PhysicalBody to use for this Viewer; if it is
     *  null, a default PhysicalBody object is created
     * @param userEnvironment the PhysicalEnvironment to use for this Viewer;
     *  if it is null, a default PhysicalEnvironment object is created
     * @param setVisible determines if the Frames should be set to visible once created
     * @since Java3D 1.3
     */
    public Viewer(Canvas3D[] userCanvases, PhysicalBody userBody, PhysicalEnvironment userEnvironment, boolean setVisible) {
        if (userBody == null) {
            physicalBody = new PhysicalBody();
        } else {
            physicalBody = userBody;
        }
        if (userEnvironment == null) {
            physicalEnvironment = new PhysicalEnvironment();
        } else {
            physicalEnvironment = userEnvironment;
        }
        if (userCanvases == null) {
            GraphicsConfiguration config = ConfiguredUniverse.getPreferredConfiguration();
            canvases = new Canvas3D[1];
            canvases[0] = new Canvas3D(config);
            try {
                canvases[0].setFocusable(true);
            } catch (NoSuchMethodError e) {
            }
            createFramesAndPanels(setVisible);
        } else {
            canvases = new Canvas3D[userCanvases.length];
            for (int i = 0; i < userCanvases.length; i++) {
                canvases[i] = userCanvases[i];
                try {
                    canvases[i].setFocusable(true);
                } catch (NoSuchMethodError e) {
                }
            }
        }
        view = new View();
        view.setUserHeadToVworldEnable(true);
        synchronized (viewerMap) {
            Viewer.viewerMap.put(view, this);
        }
        for (int i = 0; i < canvases.length; i++) {
            view.addCanvas3D(canvases[i]);
        }
        view.setPhysicalBody(physicalBody);
        view.setPhysicalEnvironment(physicalEnvironment);
    }

    /**
     * Creates a default Viewer object. The default values are used to create
     * the PhysicalEnvironment and PhysicalBody.  A single RGB, double buffered
     * and depth buffered Canvas3D object is created.  The View is created
     * with a front clip distance of 0.1f and a back clip distance of 10.0f.
     *
     * @param userConfig the URL of the user configuration file used to
     *  initialize the PhysicalBody object; this is always ignored
     * @since Java3D 1.1
     * @deprecated create a ConfiguredUniverse to use a configuration file
     */
    public Viewer(URL userConfig) {
        this(null, userConfig);
    }

    /**
     * Creates a default viewer object. The default values are used to create
     * the PhysicalEnvironment and PhysicalBody.  The View is created
     * with a front clip distance of 0.1f and a back clip distance of 10.0f.
     *
     * @param userCanvas the Canvas3D object to be used for rendering;
     *  if this is null then a single RGB, double buffered and depth buffered
     *  Canvas3D object is created
     * @param userConfig the URL of the user configuration file used to
     *  initialize the PhysicalBody object; this is always ignored
     * @since Java3D 1.1
     * @deprecated create a ConfiguredUniverse to use a configuration file
     */
    public Viewer(Canvas3D userCanvas, URL userConfig) {
        if (physicalBody == null) {
            physicalBody = new PhysicalBody();
        }
        if (physicalEnvironment == null) {
            physicalEnvironment = new PhysicalEnvironment();
        }
        if (userCanvas == null) {
            GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
            canvases = new Canvas3D[1];
            canvases[0] = new Canvas3D(config);
            createFramesAndPanels(true);
        } else {
            canvases = new Canvas3D[1];
            canvases[0] = userCanvas;
        }
        try {
            canvases[0].setFocusable(true);
        } catch (NoSuchMethodError e) {
        }
        view = new View();
        view.setUserHeadToVworldEnable(true);
        synchronized (viewerMap) {
            Viewer.viewerMap.put(view, this);
        }
        view.addCanvas3D(canvases[0]);
        view.setPhysicalBody(physicalBody);
        view.setPhysicalEnvironment(physicalEnvironment);
    }

    /**
     * Package-scoped constructor to create a Viewer from the configuration
     * objects provided by ConfiguredUniverse.
     *
     * @param cs array of ConfigScreen objects containing configuration
     *  information for the physical screens in the environment
     * @param cv ConfigView object containing configuration information about
     *  the view to be created using the given screens
     * @param setVisible if true, call setVisible(true) on all created Window
     *  components; otherwise, they remain invisible
     */
    Viewer(ConfigScreen[] cs, ConfigView cv, boolean setVisible) {
        view = cv.Ding3dView;
        synchronized (viewerMap) {
            Viewer.viewerMap.put(view, this);
        }
        physicalBody = cv.physicalBody;
        physicalEnvironment = cv.physicalEnvironment;
        GraphicsDevice[] devices;
        GraphicsEnvironment graphicsEnv;
        graphicsEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
        devices = graphicsEnv.getScreenDevices();
        if (devices == null) throw new RuntimeException("No screen devices available in local environment");
        if (debug) {
            System.out.println("Viewer: GraphicsEnvironment returned " + devices.length + " GraphicsDevice object" + (devices.length == 1 ? "" : "s"));
            for (int i = 0; i < devices.length; i++) {
                System.out.println(devices[i] + "\n" + devices[i].getDefaultConfiguration().getBounds() + "\n");
            }
        }
        canvases = new Canvas3D[cs.length];
        Ding3dJFrames = new JFrame[cs.length];
        Ding3dJPanels = new JPanel[cs.length];
        Ding3dWindows = new Window[cs.length];
        GraphicsConfigTemplate3D tpl3D = new GraphicsConfigTemplate3D();
        if (cv.stereoEnable) {
            tpl3D.setStereo(tpl3D.PREFERRED);
        }
        if (cv.antialiasingEnable) {
            tpl3D.setSceneAntialiasing(tpl3D.PREFERRED);
        }
        for (int i = 0; i < cs.length; i++) {
            if (cs[i].frameBufferNumber >= devices.length) throw new ArrayIndexOutOfBoundsException(cs[i].errorMessage(cs[i].creatingCommand, "Screen " + cs[i].frameBufferNumber + " is invalid; " + (devices.length - 1) + " is the maximum local index."));
            Rectangle bounds;
            Container contentPane;
            GraphicsConfiguration cfg = devices[cs[i].frameBufferNumber].getBestConfiguration(tpl3D);
            if (cfg == null) throw new RuntimeException("No GraphicsConfiguration on screen " + cs[i].frameBufferNumber + " conforms to template");
            GraphicsConfiguration defCfg = cfg.getDevice().getDefaultConfiguration();
            bounds = defCfg.getBounds();
            cs[i].Ding3dJFrame = Ding3dJFrames[i] = new JFrame(cs[i].instanceName, defCfg);
            if (cs[i].noBorderFullScreen) {
                try {
                    Ding3dJFrames[i].setUndecorated(true);
                    cs[i].Ding3dWindow = Ding3dWindows[i] = Ding3dJFrames[i];
                    contentPane = Ding3dJFrames[i].getContentPane();
                } catch (NoSuchMethodError e) {
                    JWindow jwin = new JWindow(Ding3dJFrames[i], cfg);
                    cs[i].Ding3dWindow = Ding3dWindows[i] = jwin;
                    contentPane = jwin.getContentPane();
                }
                contentPane.setLayout(new BorderLayout());
                Ding3dWindows[i].setSize(bounds.width, bounds.height);
                Ding3dWindows[i].setLocation(bounds.x, bounds.y);
            } else {
                cs[i].Ding3dWindow = Ding3dWindows[i] = Ding3dJFrames[i];
                contentPane = Ding3dJFrames[i].getContentPane();
                contentPane.setLayout(new BorderLayout());
                if (cs[i].fullScreen) {
                    Ding3dWindows[i].setSize(bounds.width, bounds.height);
                    Ding3dWindows[i].setLocation(bounds.x, bounds.y);
                } else {
                    Ding3dWindows[i].setSize(cs[i].windowWidthInPixels, cs[i].windowHeightInPixels);
                    Ding3dWindows[i].setLocation(bounds.x + cs[i].windowX, bounds.y + cs[i].windowY);
                }
            }
            cs[i].Ding3dCanvas = canvases[i] = new Canvas3D(cfg);
            canvases[i].setStereoEnable(cv.stereoEnable);
            canvases[i].setMonoscopicViewPolicy(cs[i].monoscopicViewPolicy);
            Screen3D screen = canvases[i].getScreen3D();
            if (cs[i].physicalScreenWidth != 0.0) screen.setPhysicalScreenWidth(cs[i].physicalScreenWidth);
            if (cs[i].physicalScreenHeight != 0.0) screen.setPhysicalScreenHeight(cs[i].physicalScreenHeight);
            if (cs[i].trackerBaseToImagePlate != null) screen.setTrackerBaseToImagePlate(new Transform3D(cs[i].trackerBaseToImagePlate));
            if (cs[i].headTrackerToLeftImagePlate != null) screen.setHeadTrackerToLeftImagePlate(new Transform3D(cs[i].headTrackerToLeftImagePlate));
            if (cs[i].headTrackerToRightImagePlate != null) screen.setHeadTrackerToRightImagePlate(new Transform3D(cs[i].headTrackerToRightImagePlate));
            cs[i].Ding3dJPanel = Ding3dJPanels[i] = new JPanel();
            Ding3dJPanels[i].setLayout(new BorderLayout());
            Ding3dJPanels[i].add("Center", canvases[i]);
            contentPane.add("Center", Ding3dJPanels[i]);
            view.addCanvas3D(canvases[i]);
            addWindowCloseListener(Ding3dWindows[i]);
            try {
                canvases[i].setFocusable(true);
            } catch (NoSuchMethodError e) {
            }
            if (debug) {
                System.out.println("Viewer: created Canvas3D for screen " + cs[i].frameBufferNumber + " with size\n  " + Ding3dWindows[i].getSize());
                System.out.println("Screen3D[" + i + "]:  size in pixels (" + screen.getSize().width + " x " + screen.getSize().height + ")");
                System.out.println("  physical size in meters:  (" + screen.getPhysicalScreenWidth() + " x " + screen.getPhysicalScreenHeight() + ")");
                System.out.println("  hashCode = " + screen.hashCode() + "\n");
            }
        }
        if (setVisible) setVisible(true);
    }

    private void createFramesAndPanels(boolean setVisible) {
        Ding3dJFrames = new JFrame[canvases.length];
        Ding3dJPanels = new JPanel[canvases.length];
        Ding3dWindows = new Window[canvases.length];
        for (int i = 0; i < canvases.length; i++) {
            Ding3dWindows[i] = Ding3dJFrames[i] = new JFrame();
            Ding3dJFrames[i].getContentPane().setLayout(new BorderLayout());
            Ding3dJFrames[i].setSize(256, 256);
            Ding3dJPanels[i] = new JPanel();
            Ding3dJPanels[i].setLayout(new BorderLayout());
            Ding3dJPanels[i].add("Center", canvases[i]);
            Ding3dJFrames[i].getContentPane().add("Center", Ding3dJPanels[i]);
            if (setVisible) {
                Ding3dJFrames[i].setVisible(true);
            }
            addWindowCloseListener(Ding3dJFrames[i]);
        }
    }

    /**
     * Call setVisible() on all Window components created by this Viewer.
     *
     * @param visible boolean to be passed to the setVisible() calls on the
     *  Window components created by this Viewer
     * @since Java3D 1.3
     */
    public void setVisible(boolean visible) {
        for (int i = 0; i < Ding3dWindows.length; i++) {
            Ding3dWindows[i].setVisible(visible);
        }
    }

    /**
     * Returns the View object associated with the Viewer object.
     *
     * @return The View object of this Viewer.
     */
    public View getView() {
        return view;
    }

    /**
     * Set the ViewingPlatform object used by this Viewer.
     *
     * @param platform The ViewingPlatform object to set for this
     *  Viewer object.  Use null to unset the current value and
     *  not assign assign a new ViewingPlatform object.
     */
    public void setViewingPlatform(ViewingPlatform platform) {
        if (viewingPlatform != null) {
            viewingPlatform.removeViewer(this);
        }
        viewingPlatform = platform;
        if (platform != null) {
            view.attachViewPlatform(platform.getViewPlatform());
            platform.addViewer(this);
            if (avatar != null) viewingPlatform.setAvatar(this, avatar);
        } else view.attachViewPlatform(null);
    }

    /**
     * Get the ViewingPlatform object used by this Viewer.
     *
     * @return The ViewingPlatform object used by this
     *  Viewer object.
     */
    public ViewingPlatform getViewingPlatform() {
        return viewingPlatform;
    }

    /**
     * Sets the geometry to be associated with the viewer's avatar.  The
     * avatar is the geometry used to represent the viewer in the virtual
     * world.
     *
     * @param avatar The geometry to associate with this Viewer object.
     *  Passing in null will cause any geometry associated with the Viewer
     *  to be removed from the scen graph.
     */
    public void setAvatar(ViewerAvatar avatar) {
        if (this.avatar == avatar) return;
        this.avatar = avatar;
        if (viewingPlatform != null) viewingPlatform.setAvatar(this, this.avatar);
    }

    /**
     * Gets the geometry associated with the viewer's avatar.  The
     * avatar is the geometry used to represent the viewer in the virtual
     * world.
     *
     * @return The root of the scene graph that is used to represent the
     *  viewer's avatar.
     */
    public ViewerAvatar getAvatar() {
        return avatar;
    }

    /**
     * Returns the PhysicalBody object associated with the Viewer object.
     *
     * @return A reference to the PhysicalBody object.
     */
    public PhysicalBody getPhysicalBody() {
        return physicalBody;
    }

    /**
     * Returns the PhysicalEnvironment object associated with the Viewer
     * object.
     *
     * @return A reference to the PhysicalEnvironment object.
     */
    public PhysicalEnvironment getPhysicalEnvironment() {
        return physicalEnvironment;
    }

    /**
     * Returns the 0th Canvas3D object associated with this Viewer object
     *
     * @return a reference to the 0th Canvas3D object associated with this
     *  Viewer object
     * @since Java3D 1.3
     */
    public Canvas3D getCanvas3D() {
        return canvases[0];
    }

    /**
     * Returns the Canvas3D object at the specified index associated with
     * this Viewer object.
     *
     * @param canvasNum the index of the Canvas3D object to retrieve;
     *  if there is no Canvas3D object for the given index, null is returned
     * @return a reference to a Canvas3D object associated with this
     *  Viewer object
     * @since Java3D 1.3
     */
    public Canvas3D getCanvas3D(int canvasNum) {
        if (canvasNum > canvases.length) {
            return null;
        }
        return canvases[canvasNum];
    }

    /**
     * Returns all the Canvas3D objects associated with this Viewer object.
     *
     * @return an array of references to the Canvas3D objects associated with
     *  this Viewer object
     * @since Java3D 1.3
     */
    public Canvas3D[] getCanvas3Ds() {
        Canvas3D[] ret = new Canvas3D[canvases.length];
        for (int i = 0; i < canvases.length; i++) {
            ret[i] = canvases[i];
        }
        return ret;
    }

    /**
     * Returns the canvas associated with this Viewer object.
     * @deprecated superceded by getCanvas3D()
     */
    public Canvas3D getCanvases() {
        return getCanvas3D();
    }

    /**
     * This method is no longer supported since Java 3D 1.3.
     * @exception UnsupportedOperationException if called.
     * @deprecated AWT Frame components are no longer created by the
     *  Viewer class.
     */
    public Frame getFrame() {
        throw new UnsupportedOperationException("AWT Frame components are not created by the Viewer class");
    }

    /**
     * Returns the JFrame object created by this Viewer object at the
     * specified index.  If a Viewer is constructed without any Canvas3D
     * objects then the Viewer object will create a Canva3D object, a JPanel
     * containing the Canvas3D object, and a JFrame to place the JPanel in.
     * <p>
     * NOTE: When running under JDK 1.4 or newer, the JFrame always directly
     * contains the JPanel which contains the Canvas3D.  When running under
     * JDK 1.3.1 and creating a borderless full screen through a configuration
     * file, the JFrame will instead contain a JWindow which will contain the
     * JPanel and Canvas3D.
     * <p>
     * @param frameNum the index of the JFrame object to retrieve;
     *  if there is no JFrame object for the given index, null is returned
     * @return a reference to JFrame object created by this Viewer object
     * @since Java3D 1.3
     */
    public JFrame getJFrame(int frameNum) {
        if (Ding3dJFrames == null || frameNum > Ding3dJFrames.length) {
            return (null);
        }
        return Ding3dJFrames[frameNum];
    }

    /**
     * Returns all the JFrames created by this Viewer object.  If a Viewer is
     * constructed without any Canvas3D objects then the Viewer object will
     * create a Canva3D object, a JPanel containing the Canvas3D object, and a
     * JFrame to place the JPanel in.<p>
     *
     * NOTE: When running under JDK 1.4 or newer, the JFrame always directly
     * contains the JPanel which contains the Canvas3D.  When running under
     * JDK 1.3.1 and creating a borderless full screen through a configuration
     * file, the JFrame will instead contain a JWindow which will contain the
     * JPanel and Canvas3D.<p>
     *
     * @return an array of references to the JFrame objects created by
     *  this Viewer object, or null if no JFrame objects were created
     * @since Java3D 1.3
     */
    public JFrame[] getJFrames() {
        if (Ding3dJFrames == null) return null;
        JFrame[] ret = new JFrame[Ding3dJFrames.length];
        for (int i = 0; i < Ding3dJFrames.length; i++) {
            ret[i] = Ding3dJFrames[i];
        }
        return ret;
    }

    /**
     * This method is no longer supported since Java 3D 1.3.
     * @exception UnsupportedOperationException if called.
     * @deprecated AWT Panel components are no longer created by the
     * Viewer class.
     */
    public Panel getPanel() {
        throw new UnsupportedOperationException("AWT Panel components are not created by the Viewer class");
    }

    /**
     * Returns the JPanel object created by this Viewer object at the
     * specified index.  If a Viewer is constructed without any Canvas3D
     * objects then the Viewer object will create a Canva3D object and a
     * JPanel into which to place the Canvas3D object.
     *
     * @param panelNum the index of the JPanel object to retrieve;
     *  if there is no JPanel object for the given index, null is returned
     * @return a reference to a JPanel object created by this Viewer object
     * @since Java3D 1.3
     */
    public JPanel getJPanel(int panelNum) {
        if (Ding3dJPanels == null || panelNum > Ding3dJPanels.length) {
            return (null);
        }
        return Ding3dJPanels[panelNum];
    }

    /**
     * Returns all the JPanel objects created by this Viewer object.  If a
     * Viewer is constructed without any Canvas3D objects then the Viewer
     * object will create a Canva3D object and a JPanel into which to place
     * the Canvas3D object.
     *
     * @return an array of references to the JPanel objects created by
     *  this Viewer object, or null or no JPanel objects were created
     * @since Java3D 1.3
     */
    public JPanel[] getJPanels() {
        if (Ding3dJPanels == null) return null;
        JPanel[] ret = new JPanel[Ding3dJPanels.length];
        for (int i = 0; i < Ding3dJPanels.length; i++) {
            ret[i] = Ding3dJPanels[i];
        }
        return ret;
    }

    /**
     * Used to create and initialize a default AudioDevice3D used for sound
     * rendering.
     *
     * @return reference to created AudioDevice, or null if error occurs.
     */
    public AudioDevice createAudioDevice() {
        if (physicalEnvironment == null) {
            System.err.println("Java 3D: createAudioDevice: physicalEnvironment is null");
            return null;
        }
        try {
            String audioDeviceClassName = (String) java.security.AccessController.doPrivileged(new java.security.PrivilegedAction() {

                public Object run() {
                    return System.getProperty("Ding3d.audiodevice");
                }
            });
            if (audioDeviceClassName == null) {
                throw new UnsupportedOperationException("No AudioDevice specified");
            }
            Class audioDeviceClass = null;
            try {
                audioDeviceClass = Class.forName(audioDeviceClassName);
            } catch (ClassNotFoundException ex) {
            }
            if (audioDeviceClass == null) {
                ClassLoader audioDeviceClassLoader = (ClassLoader) java.security.AccessController.doPrivileged(new java.security.PrivilegedAction() {

                    public Object run() {
                        return ClassLoader.getSystemClassLoader();
                    }
                });
                if (audioDeviceClassLoader == null) {
                    throw new IllegalStateException("System ClassLoader is null");
                }
                audioDeviceClass = Class.forName(audioDeviceClassName, true, audioDeviceClassLoader);
            }
            Class physEnvClass = PhysicalEnvironment.class;
            Constructor audioDeviceConstructor = audioDeviceClass.getConstructor(new Class[] { physEnvClass });
            PhysicalEnvironment[] args = new PhysicalEnvironment[] { physicalEnvironment };
            AudioEngine3DL2 mixer = (AudioEngine3DL2) audioDeviceConstructor.newInstance((Object[]) args);
            mixer.initialize();
            return mixer;
        } catch (Throwable e) {
            e.printStackTrace();
            physicalEnvironment.setAudioDevice(null);
            System.err.println("Java 3D: audio is disabled");
            return null;
        }
    }

    /**
     * Returns the Universe to which this Viewer is attached
     *
     * @return the Universe to which this Viewer is attached
     * @since Java 3D 1.3
     */
    public SimpleUniverse getUniverse() {
        return getViewingPlatform().getUniverse();
    }

    void addWindowCloseListener(Window win) {
        SecurityManager sm = System.getSecurityManager();
        boolean doExit = true;
        if (sm != null) {
            try {
                sm.checkExit(0);
            } catch (SecurityException e) {
                doExit = false;
            }
        }
        final boolean _doExit = doExit;
        win.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent winEvent) {
                Window w = winEvent.getWindow();
                w.setVisible(false);
                try {
                    w.dispose();
                } catch (IllegalStateException e) {
                }
                if (_doExit) {
                    System.exit(0);
                }
            }
        });
    }
}
