package net.sourceforge.olduvai.lrac;

import java.awt.Color;
import java.awt.Component;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JTextField;
import net.sourceforge.olduvai.accordiondrawer.AccordionDrawer;
import net.sourceforge.olduvai.accordiondrawer.CellGeom;
import net.sourceforge.olduvai.lrac.logging.LogEntry;
import net.sourceforge.olduvai.lrac.logging.LogFileWriter;
import net.sourceforge.olduvai.lrac.logging.Logger;
import net.sourceforge.olduvai.lrac.ui.UI;
import net.sourceforge.olduvai.lrac.util.Obfuscator;
import net.sourceforge.olduvai.lrac.util.Util;

/**
 * 
 * The LiveRAC main application code.  
 * 
 * @author Peter McLachlan (spark343@cs.ubc.ca)
 * 
 */
public class LiveRAC {

    public static boolean LOGGING = true;

    public static final String APPNAME = "LiveRAC";

    public static final String VERSION = "0.4";

    static final int INITBLOCKSIZE = 3;

    static final int INITWIDTH = 800;

    static final int INITHEIGHT = 600;

    /**
	 * Provides a standard fully specified date format for reading/writing dates
	 */
    public static final DateFormat FULLDATEFORMAT = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z");

    /** 
	 * Toggles string obfuscation
	 */
    public static final boolean OBFUSCATE = false;

    private static LiveRAC INSTANCE;

    static Logger logger = null;

    /**
	 * Is this the first time data has ever been loaded? 
	 */
    boolean firstTime = true;

    /**
	 * Has the lrd been created yet? 
	 * @see createLRD();
	 */
    boolean lrdInitialized = false;

    AccordionLRACDrawerFinal lrd;

    CellGeom flashGeomOld;

    UI ui;

    JFrame mainFrame;

    Panel drawPanel;

    public JTextField searchField;

    /**
	 * Returns an instance of the LiveRAC singleton class
	 * 
	 * 
	 * @return
	 */
    public static final LiveRAC getInstance() {
        return INSTANCE;
    }

    Color backgroundColor = new Color(250, 250, 250);

    Color objectColor = Color.getHSBColor(0.0f / 360f, .0f, 0.15f);

    Color labelColor = Color.getHSBColor(0f / 360f, 0f, 0f);

    Color labelBackColor = Color.getHSBColor(0.0f / 360f, 0.0f, 1f);

    Color labelHiColor = Color.getHSBColor(0.0f / 360f, .0f, 0.15f);

    Color labelBackHiColor = Color.getHSBColor(36f / 360f, 1f, 1f);

    DataGrid dataGrid;

    public static void main(String[] args) {
        System.setProperty("user.timezone", "GMT");
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        Date d = new Date();
        if (LOGGING) {
            LogFileWriter lfw = new LogFileWriter("Blah");
            logger = new Logger(lfw);
        }
        new LiveRAC(args);
        INSTANCE.initComplete();
    }

    public LiveRAC(String[] args) {
        INSTANCE = this;
        if (OBFUSCATE) Obfuscator.initialize(this.getClass().getClassLoader());
        int i = 0;
        while (i < args.length) {
            if (args[i].equals("-test")) {
                Util.dprint(100, "Testing!");
            } else {
            }
            i++;
        }
        ui = new UI(this, APPNAME);
        mainFrame = ui.getMainFrame();
        mainFrame.setSize(INITWIDTH, INITHEIGHT);
        drawPanel = ui.getDrawPanel();
        searchField = ui.getSearchField();
        mainFrame.setVisible(true);
        ui.connect();
    }

    public void setTitle(String t) {
        mainFrame.setTitle(t);
    }

    public static void quitAction(boolean forReal) {
        if (getLogger() != null) LiveRAC.getLogger().writeData();
        System.exit(0);
    }

    /**
	 * Clears the drawer so that we can connect to a new data source. 
	 *
	 */
    public void disconnectLRD() {
        if (lrdInitialized) {
            lrdInitialized = false;
            lrd.clear();
            drawPanel.remove(lrd.getCanvas());
            DataGrid.getInstance().flushData();
            DataGrid.getInstance().disconnect();
            lrd = null;
            AccordionDrawer.loaded = false;
            System.out.println("LRD cleared");
        }
    }

    /**
	 * Initialize the LiveRAC accordion drawing infrastructure. 
	 *	Note: if LRD has already been created, then reset its values
	 *	and clear its split lines.  
	 * @param dg 
	 */
    public void initLRD() {
        disconnectLRD();
        AccordionDrawer.setBackgroundColor(backgroundColor);
        lrd = new AccordionLRACDrawerFinal(drawPanel.getWidth(), drawPanel.getHeight(), this);
        lrd.setLabelColor(labelColor);
        lrd.setLabelBackColor(labelBackColor);
        lrd.setLabelHiColor(labelHiColor);
        lrd.setLabelBackHiColor(labelBackHiColor);
        lrd.setRubberbandColor(Color.getHSBColor(0.0f / 360f, .0f, 0.3f));
        lrd.setKey(0);
        lrd.setLineThickness(1);
        lrd.newPixelDiv(INITBLOCKSIZE, AccordionDrawer.X);
        lrd.newPixelDiv(INITBLOCKSIZE, AccordionDrawer.Y);
        Component c = lrd.getCanvas();
        c.addComponentListener(new ComponentListener() {

            public void componentHidden(ComponentEvent e) {
            }

            public void componentMoved(ComponentEvent e) {
            }

            public void componentResized(ComponentEvent e) {
                Component canvas = lrd.getCanvas();
                makeLogEntry(LogEntry.WINDOW_RESIZE, "Canvas resizing to: [" + canvas.getWidth() + ", " + canvas.getHeight() + "]", null);
                lrd.resizeStuckLines();
                lrd.requestRedraw();
            }

            public void componentShown(ComponentEvent e) {
            }
        });
        drawPanel.add(c);
        drawPanel.validate();
        lrdInitialized = true;
    }

    public void requestRedrawAll() {
        if (lrd != null) lrd.requestRedraw();
    }

    public UI getUI() {
        return ui;
    }

    public void setProgressiveOn(boolean on) {
        lrd.ignoreProgressive = !on;
    }

    public void reset() {
        lrd.reset();
    }

    public void animatedReset() {
        lrd.animatedReset(3);
    }

    /**
	 * Initialize all data structures and call the connection method in the data grid
	 */
    public boolean connectDataSource() {
        final DataGrid dg = DataGrid.getInstance();
        boolean connectState = dg.connectDataInterface(mainFrame);
        if (connectState == false) return false;
        this.dataGrid = dg;
        AccordionDrawer.loaded = false;
        initLRD();
        dg.setLrd(getLrd());
        dg.resetDisplay();
        drawPanel.repaint();
        return true;
    }

    /**
	 * Change the application title.
	 * @param titleName New application title.  
	 */
    public void updateTitle(String titleName) {
        String title = APPNAME + " " + VERSION;
        if (titleName.length() > 0) title += ": " + titleName;
        mainFrame.setTitle(title);
    }

    public final void initComplete() {
        return;
    }

    public void windowOpened(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public AccordionLRACDrawerFinal getLrd() {
        return lrd;
    }

    public boolean isLrdInitialized() {
        return lrdInitialized;
    }

    public Panel getDrawPanel() {
        return drawPanel;
    }

    public static Logger getLogger() {
        return logger;
    }

    public void partialRefreshCycle() {
        if (!isLrdInitialized()) return;
        DataGrid.getInstance().reMarkAllCriticals();
        lrd.incrementFrameNumber();
        getLrd().forceRedraw();
    }

    /**
	 * Notify the LRAC application that dates have been modified
	 * @param beginDate
	 * @param endDate
	 */
    public void fullRefreshCycle(Date beginDate, Date endDate, boolean requestNewData, boolean requestReset) {
        if (!isLrdInitialized()) return;
        final DataGrid ds = DataGrid.getInstance();
        final Date[] dsRange = ds.getTimeRange();
        if (beginDate == null || endDate == null) {
            beginDate = dsRange[0];
            endDate = dsRange[1];
        }
        ds.setTimeChanged(true);
        Date[] newTimeRange = { beginDate, endDate };
        LiveRAC.makeLogEntry(LogEntry.TIMERANGE_MODIFIED, "Old time range: [" + dsRange[0] + ", " + dsRange[1] + "] new time range: [" + newTimeRange[0] + ", " + newTimeRange[1] + "]", null);
        ds.setTimeRange(newTimeRange);
        if (requestNewData) ds.swatchRequest(); else {
            DataGrid.getInstance().reMarkAllCriticals();
            lrd.incrementFrameNumber();
        }
        if (requestReset) getLrd().reset(); else getLrd().forceRedraw();
    }

    /**
	 * Saves a full application screenshot to the specified file
	 * @param file
	 */
    public void screenShot(File file) {
        try {
            final Rectangle rectangle = mainFrame.getBounds();
            final Robot robot = new Robot();
            final BufferedImage image = robot.createScreenCapture(rectangle);
            ImageIO.write(image, "png", file);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
	 * 
	 * @param eventType
	 * @param eventDescription
	 * @param values
	 */
    public static void makeLogEntry(int eventType, String eventDescription, Collection values) {
        if (!LOGGING || getLogger() == null) return;
    }

    /**
	 * 
	 * @param eventType
	 * @param eventDescription
	 * @param value
	 */
    public static void makeLogEntry(int eventType, String eventDescription, Object value) {
        if (!LOGGING || getLogger() == null) return;
    }

    public DataGrid getDataGrid() {
        return dataGrid;
    }

    public JFrame getMainFrame() {
        return mainFrame;
    }
}
