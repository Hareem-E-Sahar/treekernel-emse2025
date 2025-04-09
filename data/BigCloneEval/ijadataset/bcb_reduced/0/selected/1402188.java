package org.jcpsim.run;

import javax.swing.*;
import java.net.URISyntaxException;
import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.awt.SplashScreen;
import java.awt.AlphaComposite;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Desktop;
import javax.swing.JApplet;
import org.jcpsim.gui.MenuBar;
import org.jcpsim.gui.StatusLine;
import org.jcpsim.piccolo.putil;
import org.jcpsim.scenarios.SimpleRespirator;
import org.jcpsim.scenarios.ArterialLine;
import org.jcpsim.scenarios.PkPd;
import org.jcpsim.scenarios.Block;
import org.jcpsim.util.Utf8ResourceBundle;
import org.jcpsim.util.LoggingWindowHandler;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.activities.PActivity;
import edu.umd.cs.piccolox.PFrame;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolox.pswing.PSwingCanvas;

/**
 *
 * @author  Dr. Frank Fischer &lt;frank@jcpsim.org&gt;
 * @version CVS $Id$
 * TODO: write JavaDoc.
 * TODO: web documentation.
 * TODO: update website, upload software.
 */
public class global {

    static final long serialVersionUID = 0L;

    private static final Logger logger = Logger.getLogger(global.class.getName());

    public static String basedirpath;

    private static PSwingCanvas canvas = null;

    private static JApplet applet = null;

    private static PFrame frame = null;

    private static boolean isAnApplet;

    private static String userhome;

    private static String version;

    private static MenuBar menubar;

    public static StatusLine statusline;

    private static LoggingWindowHandler loggingWindowHandler;

    public static PSwingCanvas getCanvas() {
        return canvas;
    }

    public static JApplet getApplet() {
        return applet;
    }

    public static PFrame getFrame() {
        return frame;
    }

    public static boolean isApplet() {
        return isAnApplet;
    }

    public static String getVersion() {
        return version;
    }

    private static global instance = null;

    protected global() {
    }

    public static void init() {
        if (instance == null) instance = new global();
    }

    public static void init(PSwingCanvas canvas, JApplet applet, PFrame frame, String scenarioName) {
        if (instance == null) instance = new global(canvas, applet, frame, scenarioName);
    }

    /**
   * The starting point.
   */
    protected global(PSwingCanvas c, JApplet applet, PFrame frame, String scenarioName) {
        logger.setUseParentHandlers(false);
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new MyFormatter());
        logger.addHandler(consoleHandler);
        loggingWindowHandler = new LoggingWindowHandler();
        loggingWindowHandler.setFormatter(new MyFormatter());
        logger.addHandler(loggingWindowHandler);
        logger.setLevel(Level.ALL);
        logger.info("jCpSim started");
        logger.info("java.version    = " + System.getProperty("java.version"));
        logger.info("java.vm.version = " + System.getProperty("java.vm.version"));
        canvas = c;
        this.applet = applet;
        this.frame = frame;
        isAnApplet = (applet != null);
        userhome = System.getProperty("user.home") + System.getProperty("file.separator") + ".jcpsim" + System.getProperty("file.separator");
        setLocale(Locale.getDefault());
        version = "could not load version.properties";
        try {
            Properties versionProp = new Properties();
            InputStream istream = global.class.getResourceAsStream("version.properties");
            if (istream != null) {
                versionProp.load(istream);
                version = versionProp.getProperty("version") + "  (" + versionProp.getProperty("date") + ")";
            }
        } catch (IOException e) {
            logger.info("could not load version.properties");
        }
        logger.info("version:  " + getVersion());
        logger.info("it runs as an: " + (isAnApplet ? "applet" : "application"));
        canvas.removeInputEventListener(canvas.getZoomEventHandler());
        canvas.removeInputEventListener(canvas.getPanEventHandler());
        setHighRenderingQuality(false);
        if (getFrame() != null) {
            getFrame().setSize(Toolkit.getDefaultToolkit().getScreenSize().width * 3 / 4, Toolkit.getDefaultToolkit().getScreenSize().height * 3 / 4);
            getFrame().validate();
        }
        setScenario(scenarioName);
        statusline = new StatusLine(canvas);
        menubar = new MenuBar(canvas);
        refreshAll();
        final int fView = 20;
        int fSampling = 100;
        canvas.getRoot().addActivity(new PActivity(-1, 1000 / fView) {

            int n = 0;

            long oldTime = 0;

            protected void activityStep(long elapsedTime) {
                super.activityStep(elapsedTime);
                n++;
                for (int i = 0; i < 5; i++) scenario.step(n);
                if (getFrame() != null) menubar.mViewFullscreen.setState(getFrame().isFullScreenMode());
                oldTime = elapsedTime;
            }
        });
        if (frame != null) {
            frame.addWindowListener(new WindowAdapter() {

                public void windowClosing(WindowEvent e) {
                    logger.info("WindowListener: window closed");
                    System.exit(0);
                }
            });
        }
    }

    public static String[] getScenarios() {
        return new String[] { "SimpleRespirator", "ArterialLine", "PkPd" };
    }

    public static void setScenario(String name) {
        if (name == null) name = "";
        logger.info("Setting scenario; " + name);
        if (scenario != null) canvas.getLayer().removeChild(scenario);
        if ("ArterialLine".equals(name)) scenario = new ArterialLine(); else if ("PkPd".equals(name)) scenario = new PkPd(); else scenario = new SimpleRespirator();
        canvas.getLayer().addChild(scenario);
        viewNode(null);
    }

    public static void refreshAll() {
        menubar.setLanguage();
        statusline.repaint();
        for (Block b : scenario.getBlocks()) b.refresh();
        getCanvas().repaint();
        menubar.revalidate();
        menubar.repaint();
        status(i18n("global.welcome"));
    }

    public static void help(String s) {
        String url = "http://www.jcpsim.org/" + s + ".html";
        logger.info("HELP: " + url);
        BasicService basicService = null;
        try {
            basicService = (BasicService) ServiceManager.lookup("javax.jnlp.BasicService");
        } catch (UnavailableServiceException use) {
            logger.info("Java Web Start Services are unavailable.");
        }
        if (basicService != null) {
            logger.info(basicService.getCodeBase().toString());
            try {
                basicService.showDocument(new java.net.URL(url));
            } catch (java.net.MalformedURLException e) {
                logger.info("Malformed URL: " + e.toString());
            }
        } else {
            if ((System.getProperty("java.version").startsWith("1.6"))) {
                if (Desktop.isDesktopSupported()) {
                    if (Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        try {
                            Desktop.getDesktop().browse(new java.net.URI(url));
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        } catch (URISyntaxException use) {
                            use.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public static void openLogWindow() {
        loggingWindowHandler.openWindow();
    }

    public static void viewNode(PNode node) {
        putil.zoomTo(getCanvas().getCamera(), node, 0.02);
    }

    private static boolean highRenderingQuality;

    public static void setHighRenderingQuality(boolean flag) {
        highRenderingQuality = flag;
    }

    public static boolean getHighRenderingQuality() {
        return highRenderingQuality;
    }

    public static void setRenderingQuality(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, highRenderingQuality ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, highRenderingQuality ? RenderingHints.VALUE_RENDER_QUALITY : RenderingHints.VALUE_RENDER_SPEED);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, highRenderingQuality ? RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY : RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
        g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, highRenderingQuality ? RenderingHints.VALUE_COLOR_RENDER_QUALITY : RenderingHints.VALUE_COLOR_RENDER_SPEED);
    }

    public static PCamera getCamera() {
        return getCanvas().getCamera();
    }

    public static double getTime() {
        return 0;
    }

    private static Locale locale;

    private static ResourceBundle rb;

    public static void setLocale(Locale loc) {
        locale = loc;
        rb = loadResourceBundle("org.jcpsim.jcpsim");
    }

    private static ResourceBundle loadResourceBundle(String name) {
        try {
            return Utf8ResourceBundle.getBundle(name, locale);
        } catch (MissingResourceException e) {
            logger.severe(e.toString());
            System.exit(0);
        }
        return null;
    }

    public static Locale getLocale() {
        return locale;
    }

    public static String i18n(String s) {
        try {
            return rb.getString(s);
        } catch (MissingResourceException e) {
            return "***" + s + "***";
        }
    }

    public static Locale[] getLanguages() {
        return new Locale[] { Locale.ENGLISH, Locale.GERMAN };
    }

    private static Scenario scenario;

    public static Scenario getScenario() {
        return scenario;
    }

    public static int getRingBufferLength() {
        return 20000;
    }

    public static void status(String s) {
        statusline.status(s);
    }

    public static boolean activated = false;

    /**
   *  Single line log record. 
   */
    public class MyFormatter extends Formatter {

        long start;

        boolean first = true;

        public String format(LogRecord rec) {
            if (first) {
                first = false;
                start = rec.getMillis();
            }
            StringBuffer buf = new StringBuffer(1000);
            buf.append(String.format("%6.2f", (float) ((rec.getMillis() - start) / 1000)));
            buf.append(' ');
            buf.append(rec.getLevel());
            buf.append(": ");
            buf.append(formatMessage(rec));
            buf.append("   (");
            buf.append(rec.getSourceClassName());
            buf.append('#');
            buf.append(rec.getSourceMethodName());
            buf.append(")\n");
            return buf.toString();
        }
    }
}
