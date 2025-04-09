package org.kaintoch.gps.gpx.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import org.kaintoch.gps.gpx.GpxFile;
import org.kaintoch.gps.gpx.GpxFileCreator;
import org.kaintoch.gps.gpx.GpxTrack;
import org.kaintoch.gps.gpx.IGpxCreatable;
import org.kaintoch.gps.gpx.filter.IGpxBaseFilter;
import org.kaintoch.gps.gpx.filter.IGpxTrackFilter;
import org.kaintoch.gps.gpx.filter.IGpxTracksFilter;
import org.kaintoch.gps.gpx.marker.IGpxBaseMarker;
import org.kaintoch.gps.gpx.marker.IGpxTrackMarker;
import org.kaintoch.gps.gpx.marker.IGpxTracksMarker;

/**
 * This class represents JGPXtool's GUI.
 * @author stefan
 *
 */
public class GpxGui implements ActionListener {

    public static final String PROP_PREFIX_FILTER = "prefix.filter";

    public static final String PROP_PREFIX_MULTIFILTER = "prefix.multifilter";

    public static final String PROP_PREFIX_MARKER = "prefix.marker";

    public static final String PROP_DIR_GPX = "dir.gpx";

    public static final String PROP_DRAWMODE = "visualization.drawmode";

    public static final String PROP_MAPS_OSM_BASEURL = "maps.osm.baseurl";

    public static final String PROP_PREFIX_MAPS_OSM_URL = "maps.osm.url.";

    public static final String PROP_MAPS_THICKNESS = "maps.thickness";

    public static final String PROP_MAPS_MARGIN = "maps.margin";

    public static final String DEFAULT_PREFIX_FILTER = "filter.";

    public static final String DEFAULT_PREFIX_MULTIFILTER = "multifilter.";

    public static final String DEFAULT_PREFIX_MARKER = "marker.";

    public static final String VAL_DRAWMODE_LINE = "line";

    public static final String VAL_DRAWMODE_POINT = "point";

    private GpxDataModel model = null;

    private String dirScreenshot = null;

    private String dirGpx = null;

    private JFrame mainFrame = null;

    private DropTarget dropTarget = null;

    private GpxTrackPanel birdViewPanel = null;

    private GpxXYPanel elevationPanel = null;

    private GpxXYPanel speedPanel = null;

    private JList selectList = null;

    private JSplitPane splitPane = null;

    private JScrollPane fixedModeScrollPane = null;

    private JButton zoomIn = null;

    private JButton zoomOut = null;

    private JRadioButtonMenuItem miMapNone = null;

    private JRadioButtonMenuItem miMapOsm = null;

    private JMenu mMaps = null;

    private ButtonGroup groupMaps = new ButtonGroup();

    private Properties props = null;

    private Properties filters = null;

    private Properties multifilters = null;

    private Properties markers = null;

    private Properties maps = null;

    private static final String cmdFile = "File";

    private static final String cmdView = "View";

    private static final String cmdMaps = "Maps";

    private static final String cmdTrack = "Track";

    private static final String cmdFilter = "Filter";

    private static final String cmdMultiFilter = "Multifilter";

    private static final String cmdMarker = "Marker";

    private static final String cmdHelp = "Help";

    private static final String cmdPrefs = "Preferences...";

    private static final String cmdBestFit = "BestFit";

    private static final String cmdDistance = "Distance";

    private static final String cmdDuration = "Duration";

    private static final String cmdLines = "Lines";

    private static final String cmdPoints = "Points";

    private static final String cmdFitPanel2Data = "Fit panel to data";

    private static final String cmdFitData2Panel = "Fit data to panel";

    private static final String cmdMapNone = "None";

    private static final String cmdMapOsm = "Map (OSM)";

    private static final String cmdQuit = "Quit";

    private static final String cmdDelAll = "Delete All";

    private static final String cmdAddGpx = "Add GPX...";

    private static final String cmdSaveAsGpx = "Save as GPX...";

    private static final String cmdTrkDel = "Delete";

    private static final String cmdTrkDelMarks = "Delete marks";

    private static final String cmdZoomIn = "Zoom in";

    private static final String cmdZoomOut = "Zoom out";

    private static final String cmdAbout = "About...";

    private static final String cmdInfo = "Info...";

    private static final String cmdScreenshot = "Screenshot...";

    private static final String cmdClearSelection = "Clear selection";

    public GpxGui(java.util.List gpxFiles, Properties props) throws Exception {
        if (gpxFiles == null) {
            throw new NullPointerException("gpxFiles is null");
        }
        model = new GpxDataModel();
        fillModel(gpxFiles);
        this.props = props;
        buildEverything();
    }

    private void fillModel(java.util.List gpxFiles) {
        model.ensureCapacity(gpxFiles.size() * 2);
        Iterator iiGpxFiles = gpxFiles.iterator();
        while (iiGpxFiles.hasNext()) {
            GpxFile gpxFile = (GpxFile) iiGpxFiles.next();
            model.addGpxFile(gpxFile);
        }
    }

    public void show() {
        mainFrame.pack();
        mainFrame.setVisible(true);
    }

    public void addGpxFile(GpxFile gpxFile) {
        if (gpxFile == null) {
            throw new NullPointerException("gpxFile is null");
        }
        model.addGpxFile(gpxFile);
    }

    /**
	 * Set a frames border icon.
	 * @param frame Frame who's border icon to set.
	 * @param iconName Name od resource containing the icon image.
	 */
    private void setFrameIcon(JFrame frame, String iconName) {
        try {
            ImageIcon img = new ImageIcon(getClass().getClassLoader().getResource(iconName));
            frame.setIconImage(img.getImage());
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    /**
	 *
	 */
    private void buildEverything() throws Exception {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
        }
        mainFrame = new JFrame("JGPXtool");
        DropTargetListener dtl = new GpxDropTargetListener(props, model);
        dropTarget = new DropTarget(mainFrame, dtl);
        buildMenus();
        mainFrame.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        setFrameIcon(mainFrame, "jgpxtool32x32.gif");
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(buildLeft());
        splitPane.setRightComponent(buildRight());
        Container contentPane = mainFrame.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(buildToolbar(), BorderLayout.NORTH);
        contentPane.add(splitPane, BorderLayout.CENTER);
    }

    private void buildMenus() {
        int ii = 0;
        Object[] keyArr = null;
        JMenuBar menuBar = new JMenuBar();
        JMenu mFiles = new JMenu(cmdFile);
        JMenu mView = new JMenu(cmdView);
        mMaps = new JMenu(cmdMaps);
        JMenu mTracks = new JMenu(cmdTrack);
        JMenu mFilter = new JMenu(cmdFilter);
        JMenu mMultiFilter = new JMenu(cmdMultiFilter);
        JMenu mMarker = new JMenu(cmdMarker);
        JMenu mHelp = new JMenu(cmdHelp);
        JMenuItem miDelAll = new JMenuItem(cmdDelAll);
        JMenuItem miPrefs = new JMenuItem(cmdPrefs);
        JMenuItem miQuit = new JMenuItem(cmdQuit);
        JMenuItem miBestFit = new JMenuItem(cmdBestFit);
        JMenuItem miClearSelection = new JMenuItem(cmdClearSelection);
        JRadioButtonMenuItem miLines = new JRadioButtonMenuItem(cmdLines);
        JRadioButtonMenuItem miPoints = new JRadioButtonMenuItem(cmdPoints);
        JRadioButtonMenuItem miDistance = new JRadioButtonMenuItem(cmdDistance);
        JRadioButtonMenuItem miDuration = new JRadioButtonMenuItem(cmdDuration);
        JRadioButtonMenuItem miFitPanel2Data = new JRadioButtonMenuItem(cmdFitPanel2Data);
        JRadioButtonMenuItem miFitData2Panel = new JRadioButtonMenuItem(cmdFitData2Panel);
        miMapNone = new JRadioButtonMenuItem(cmdMapNone);
        miMapOsm = new JRadioButtonMenuItem(cmdMapOsm);
        groupMaps.add(miMapNone);
        groupMaps.add(miMapOsm);
        miMapNone.setSelected(true);
        JMenuItem miAddGpx = new JMenuItem(cmdAddGpx);
        JMenuItem miSaveAsGpx = new JMenuItem(cmdSaveAsGpx);
        JMenuItem miDel = new JMenuItem(cmdTrkDel);
        JMenuItem miDelMarks = new JMenuItem(cmdTrkDelMarks);
        JMenuItem miAbout = new JMenuItem(cmdAbout);
        JMenuItem miInfo = new JMenuItem(cmdInfo);
        JMenuItem miScreenshot = new JMenuItem(cmdScreenshot);
        ButtonGroup groupDrawMode = new ButtonGroup();
        groupDrawMode.add(miLines);
        groupDrawMode.add(miPoints);
        miLines.setSelected(true);
        ButtonGroup groupXAxis = new ButtonGroup();
        groupXAxis.add(miDistance);
        groupXAxis.add(miDuration);
        miDistance.setSelected(true);
        ButtonGroup groupSizeMode = new ButtonGroup();
        groupSizeMode.add(miFitPanel2Data);
        groupSizeMode.add(miFitData2Panel);
        miFitData2Panel.setSelected(true);
        mMaps.setEnabled(false);
        miDelAll.addActionListener(this);
        miPrefs.addActionListener(this);
        miQuit.addActionListener(this);
        miBestFit.addActionListener(this);
        miClearSelection.addActionListener(this);
        miLines.addActionListener(this);
        miPoints.addActionListener(this);
        miDistance.addActionListener(this);
        miDuration.addActionListener(this);
        miFitPanel2Data.addActionListener(this);
        miMapNone.addActionListener(this);
        miMapOsm.addActionListener(this);
        miFitData2Panel.addActionListener(this);
        miAddGpx.addActionListener(this);
        miSaveAsGpx.addActionListener(this);
        miDel.addActionListener(this);
        miDelMarks.addActionListener(this);
        miAbout.addActionListener(this);
        miInfo.addActionListener(this);
        miScreenshot.addActionListener(this);
        menuBar.add(mFiles);
        menuBar.add(mView);
        menuBar.add(mTracks);
        mTracks.add(mFilter);
        mTracks.add(mMultiFilter);
        mTracks.add(mMarker);
        menuBar.add(mHelp);
        mainFrame.setJMenuBar(menuBar);
        mFiles.add(miAddGpx);
        mFiles.add(miSaveAsGpx);
        mFiles.add(miScreenshot);
        mFiles.addSeparator();
        mFiles.add(miDelAll);
        mFiles.addSeparator();
        mFiles.add(miPrefs);
        mFiles.addSeparator();
        mFiles.add(miQuit);
        mView.add(miBestFit);
        mView.add(miClearSelection);
        mView.addSeparator();
        mView.add(miLines);
        mView.add(miPoints);
        mView.addSeparator();
        mView.add(miDistance);
        mView.add(miDuration);
        mView.addSeparator();
        mView.add(miFitPanel2Data);
        mView.add(miFitData2Panel);
        mView.addSeparator();
        mView.add(mMaps);
        mMaps.add(miMapNone);
        mMaps.add(miMapOsm);
        mTracks.addSeparator();
        mTracks.add(miDel);
        mTracks.add(miDelMarks);
        mHelp.add(miAbout);
        mHelp.add(miInfo);
        filters = getPropsWithPrefix(props, props.getProperty(PROP_PREFIX_FILTER, DEFAULT_PREFIX_FILTER));
        keyArr = getSortedPropertyKeys(filters);
        if (keyArr != null) {
            for (ii = 0; ii < keyArr.length; ++ii) {
                String key = (String) keyArr[ii];
                JMenuItem miFilter = new JMenuItem(key);
                miFilter.addActionListener(this);
                mFilter.add(miFilter);
            }
        }
        multifilters = getPropsWithPrefix(props, props.getProperty(PROP_PREFIX_MULTIFILTER, DEFAULT_PREFIX_MULTIFILTER));
        keyArr = getSortedPropertyKeys(multifilters);
        if (keyArr != null) {
            for (ii = 0; ii < keyArr.length; ++ii) {
                String key = (String) keyArr[ii];
                JMenuItem miMultiFilter = new JMenuItem(key);
                miMultiFilter.addActionListener(this);
                mMultiFilter.add(miMultiFilter);
            }
        }
        markers = getPropsWithPrefix(props, props.getProperty(PROP_PREFIX_MARKER, DEFAULT_PREFIX_MARKER));
        keyArr = getSortedPropertyKeys(markers);
        if (keyArr != null) {
            for (ii = 0; ii < keyArr.length; ++ii) {
                String key = (String) keyArr[ii];
                JMenuItem miMarker = new JMenuItem(key);
                miMarker.addActionListener(this);
                mMarker.add(miMarker);
            }
        }
        maps = getPropsWithPrefix(props, PROP_PREFIX_MAPS_OSM_URL);
        keyArr = getSortedPropertyKeys(maps);
        if (keyArr != null) {
            for (ii = 0; ii < keyArr.length; ++ii) {
                String key = (String) keyArr[ii];
                JRadioButtonMenuItem miMap = new JRadioButtonMenuItem(key);
                miMap.addActionListener(this);
                mMaps.add(miMap);
                groupMaps.add(miMap);
            }
        }
    }

    private Object[] getSortedPropertyKeys(Properties props) {
        Object[] keyArr = null;
        Enumeration keys = props.keys();
        if (keys != null) {
            keyArr = props.keySet().toArray();
            Arrays.sort(keyArr);
        }
        return keyArr;
    }

    private JComponent buildToolbar() {
        JToolBar toolBar = new JToolBar("JGPXtool");
        zoomIn = new JButton(cmdZoomIn);
        zoomIn.addActionListener(this);
        toolBar.add(zoomIn);
        zoomOut = new JButton(cmdZoomOut);
        zoomOut.addActionListener(this);
        toolBar.add(zoomOut);
        toolBar.setPreferredSize(new Dimension(50, 30));
        zoomIn.setEnabled(false);
        zoomOut.setEnabled(false);
        return toolBar;
    }

    private JComponent buildLeft() {
        selectList = new JList(model);
        selectList.setCellRenderer(new SelectListCellRenderer());
        MouseListener mouseListener = new MouseAdapter() {

            public void mousePressed(MouseEvent e) {
            }

            public void mouseClicked(MouseEvent e) {
                int index = selectList.locationToIndex(e.getPoint());
                if (e.getClickCount() == 2) {
                    if (selectList.isSelectedIndex(index)) {
                        selectList.removeSelectionInterval(index, index);
                    } else {
                        selectList.addSelectionInterval(index, index);
                    }
                }
            }
        };
        selectList.addMouseListener(mouseListener);
        JScrollPane scrollPane = new JScrollPane(selectList);
        return scrollPane;
    }

    private JComponent buildRight() {
        int drawMode = getDrawMode();
        birdViewPanel = new GpxTrackPanel(model, drawMode);
        birdViewPanel.setOsmBaseUrl(props.getProperty(PROP_MAPS_OSM_BASEURL));
        birdViewPanel.setPreferredSize(new Dimension(550, 550));
        try {
            birdViewPanel.setMapThickness(Integer.parseInt(props.getProperty(PROP_MAPS_THICKNESS)));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        try {
            birdViewPanel.setMapMargin(Double.parseDouble(props.getProperty(PROP_MAPS_MARGIN)));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        elevationPanel = new GpxXYPanel(model, GpxXYPanel.DIM_ELEVATION, drawMode);
        elevationPanel.setPreferredSize(new Dimension(550, 150));
        elevationPanel.addCoordListener((ICoordListener) birdViewPanel);
        speedPanel = new GpxXYPanel(model, GpxXYPanel.DIM_SPEED, drawMode);
        speedPanel.setPreferredSize(new Dimension(550, 150));
        speedPanel.addCoordListener((ICoordListener) birdViewPanel);
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setMinimumSize(new Dimension(40, 40));
        splitPane.setResizeWeight(0.5);
        JSplitPane splitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane2.setMinimumSize(new Dimension(40, 40));
        splitPane2.setResizeWeight(0.5);
        splitPane.setTopComponent(birdViewPanel);
        splitPane.setBottomComponent(splitPane2);
        splitPane2.setTopComponent(elevationPanel);
        splitPane2.setBottomComponent(speedPanel);
        return splitPane;
    }

    /**
	 * Determine draw mode from properties.
	 */
    private int getDrawMode() {
        int drawMode = GpxBasePanel.DRAWMODE_LINE;
        String drawModeStr = props.getProperty(PROP_DRAWMODE);
        if (VAL_DRAWMODE_LINE.equals(drawModeStr)) {
            drawMode = GpxBasePanel.DRAWMODE_LINE;
        } else if (VAL_DRAWMODE_POINT.equals(drawModeStr)) {
            drawMode = GpxBasePanel.DRAWMODE_POINT;
        }
        return drawMode;
    }

    /**
	 * Build an int-array containing the selected indexes.
	 * If selection is empty throw an exception.
	 * @return
	 * @throws Exception
	 */
    private int[] getSelection() throws Exception {
        int ii;
        int[] indexes = selectList.getSelectedIndices();
        StringBuffer idxStr = new StringBuffer(100);
        for (ii = 0; ii < indexes.length; ++ii) {
            if (ii > 0) {
                idxStr.append(", ");
            }
            idxStr.append(indexes[ii]);
        }
        if (indexes == null || indexes.length == 0) {
            throw new Exception("Selection is empty");
        }
        return indexes;
    }

    /**
	 * @return
	 */
    private JFileChooser createFileChooser(boolean multi, String dirGpx, FileFilter filter) {
        JFileChooser chooser = null;
        if (dirGpx != null && dirGpx.length() > 0) {
            chooser = new JFileChooser(dirGpx);
        } else {
            chooser = new JFileChooser();
        }
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(multi);
        chooser.setFileFilter(filter);
        return chooser;
    }

    public void actionPerformed(ActionEvent event) {
        try {
            if (event.getActionCommand().equals(cmdQuit)) {
                System.exit(0);
            } else if (event.getActionCommand().equals(cmdAddGpx)) {
                addGpx();
            } else if (event.getActionCommand().equals(cmdSaveAsGpx)) {
                saveAsGpx();
            } else if (event.getActionCommand().equals(cmdBestFit)) {
                bestFit();
            } else if (event.getActionCommand().equals(cmdClearSelection)) {
                birdViewPanel.clearSelection();
                elevationPanel.clearSelection();
                speedPanel.clearSelection();
                birdViewPanel.repaint();
                elevationPanel.repaint();
                speedPanel.repaint();
            } else if (event.getActionCommand().equals(cmdLines)) {
                birdViewPanel.setDrawMode(GpxBasePanel.DRAWMODE_LINE);
                elevationPanel.setDrawMode(GpxBasePanel.DRAWMODE_LINE);
                speedPanel.setDrawMode(GpxBasePanel.DRAWMODE_LINE);
            } else if (event.getActionCommand().equals(cmdPoints)) {
                birdViewPanel.setDrawMode(GpxBasePanel.DRAWMODE_POINT);
                elevationPanel.setDrawMode(GpxBasePanel.DRAWMODE_POINT);
                speedPanel.setDrawMode(GpxBasePanel.DRAWMODE_POINT);
            } else if (event.getActionCommand().equals(cmdDistance)) {
                elevationPanel.setDimX(GpxXYPanel.DIM_DISTANCE);
                speedPanel.setDimX(GpxXYPanel.DIM_DISTANCE);
            } else if (event.getActionCommand().equals(cmdDuration)) {
                elevationPanel.setDimX(GpxXYPanel.DIM_DURATION);
                speedPanel.setDimX(GpxXYPanel.DIM_DURATION);
            } else if (event.getActionCommand().equals(cmdFitPanel2Data)) {
                birdViewPanel.setSizeMode(GpxBasePanel.SIZEMODE_FITPANEL2DATA);
                fixedModeScrollPane = new JScrollPane(birdViewPanel);
                fixedModeScrollPane.setPreferredSize(birdViewPanel.getSize());
                splitPane.setTopComponent(fixedModeScrollPane);
                zoomIn.setEnabled(true);
                zoomOut.setEnabled(true);
                mMaps.setEnabled(true);
            } else if (event.getActionCommand().equals(cmdFitData2Panel)) {
                birdViewPanel.setSizeMode(GpxBasePanel.SIZEMODE_FITDATA2PANEL);
                splitPane.setTopComponent(birdViewPanel);
                zoomIn.setEnabled(false);
                zoomOut.setEnabled(false);
                mMaps.setEnabled(false);
            } else if (event.getActionCommand().equals(cmdZoomIn)) {
                birdViewPanel.zoomIn();
            } else if (event.getActionCommand().equals(cmdZoomOut)) {
                birdViewPanel.zoomOut();
            } else if (event.getActionCommand().equals(cmdMapOsm)) {
                showMap(null);
            } else if (event.getActionCommand().equals(cmdMapNone)) {
                birdViewPanel.setOsmBaseUrl(null);
                birdViewPanel.setShowMapOsm(false);
            } else if (event.getActionCommand().equals(cmdTrkDel)) {
                del();
            } else if (event.getActionCommand().equals(cmdTrkDelMarks)) {
                delMarks();
            } else if (event.getActionCommand().equals(cmdDelAll)) {
                delAll();
            } else if (event.getActionCommand().equals(cmdAbout)) {
                about();
            } else if (event.getActionCommand().equals(cmdInfo)) {
                info();
            } else if (event.getActionCommand().equals(cmdScreenshot)) {
                screenshot();
            } else {
                String key = ((AbstractButton) event.getSource()).getText();
                String val = filters.getProperty(key);
                String multival = multifilters.getProperty(key);
                String marker = markers.getProperty(key);
                String map = maps.getProperty(key);
                if (val != null && val.length() > 0) {
                    applyFilter(val);
                } else if (multival != null && multival.length() > 0) {
                    applyMultiFilter(multival);
                } else if (marker != null && marker.length() > 0) {
                    applyMarker(marker);
                } else if (map != null && map.length() > 0) {
                    showMap(map);
                } else {
                    JOptionPane.showMessageDialog(mainFrame, "NYI");
                }
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(mainFrame, "<html>" + ex.getMessage() + "<br>" + ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
	 * Write a screenshot of the current birdview panel.
	 * @throws IOException in case of error
	 */
    private void screenshot() throws IOException {
        String[] exts = new String[] { ".gif", ".jpg", ".png", ".tif" };
        ExtensionFileFilter filter = new ExtensionFileFilter("Graphics file", exts);
        JFileChooser chooser = createFileChooser(false, dirScreenshot, filter);
        chooser.setDialogTitle("Save screenshot (extension determines type)");
        int returnVal = chooser.showDialog(null, "Save screenshot");
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String fName = chooser.getSelectedFile().getPath();
            String format = null;
            int lastDot = fName.lastIndexOf(".");
            if (lastDot >= 0) {
                format = fName.substring(lastDot + 1);
                if (format.length() <= 0) {
                    format = "png";
                }
            } else {
                format = "png";
            }
            Iterator iter = ImageIO.getImageWritersByFormatName(format);
            if (iter == null || !iter.hasNext()) {
                throw new IOException("Format not supported: " + format);
            }
            RenderedImage img = birdViewPanel.getImage();
            ImageIO.write(img, format, new File(fName));
            img = elevationPanel.getImage();
            ImageIO.write(img, format, new File(fName + "_profile." + format));
            dirScreenshot = chooser.getCurrentDirectory().getCanonicalPath();
        }
    }

    /**
	 * Show info dialog
	 */
    private void info() {
        Properties sysProps = System.getProperties();
        Vector rows = new Vector(sysProps.size() + ((props != null) ? props.size() : 0));
        Object[] keysSysProps = getSortedPropertyKeys(sysProps);
        Object[] keysProps = getSortedPropertyKeys(props);
        int ii;
        if (keysProps != null) {
            for (ii = 0; ii < keysProps.length; ++ii) {
                Vector row = new Vector(2);
                String key = (String) keysProps[ii];
                String val = props.getProperty(key);
                row.add(key);
                row.add(val);
                rows.add(row);
            }
        }
        if (keysSysProps != null) {
            for (ii = 0; ii < keysSysProps.length; ++ii) {
                Vector row = new Vector(2);
                String key = (String) keysSysProps[ii];
                String val = System.getProperty(key);
                row.add(key);
                row.add(val);
                rows.add(row);
            }
        }
        Vector colNames = new Vector(2);
        colNames.add("System property");
        colNames.add("value");
        JTable table = new JTable(rows, colNames);
        JScrollPane scrollPane = new JScrollPane(table);
        JPanel infoPanel = new JPanel();
        infoPanel.add(scrollPane);
        JOptionPane.showMessageDialog(mainFrame, infoPanel, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
	 * Show about dialog
	 */
    private void about() {
        JOptionPane.showMessageDialog(mainFrame, "<html><h1>JGPXtool</h1>" + "<br>Written by Stefan B. Kaintoch" + "<br>Released under GNU GPL" + "<br>WWW: <a href=\"http://sourceforge.net/projects/jgpxtool/\">http://sourceforge.net/projects/jgpxtool/</a>", "About", JOptionPane.INFORMATION_MESSAGE);
    }

    private void bestFit() {
        model.rebuildMinMax();
    }

    private void addGpx() throws Exception {
        if (dirGpx == null) {
            dirGpx = props.getProperty(PROP_DIR_GPX);
        }
        JFileChooser chooser = createFileChooser(true, dirGpx, new GpxFileFilter());
        int returnVal = chooser.showDialog(mainFrame, "Select GPX file");
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File[] files = null;
            if (chooser.isMultiSelectionEnabled()) {
                files = chooser.getSelectedFiles();
            } else {
                files = new File[1];
                files[0] = chooser.getSelectedFile();
            }
            for (int ii = 0; files != null && ii < files.length; ++ii) {
                String fName = files[ii].getPath();
                GpxFileCreator creator = new GpxFileCreator(props);
                GpxFile gpxFile = creator.createGpxFile(fName);
                addGpxFile(gpxFile);
            }
            dirGpx = chooser.getCurrentDirectory().getCanonicalPath();
        }
    }

    /**
	 * Delete the selected tracks' markers.
	 * @throws Exception
	 */
    private void delMarks() throws Exception {
        int ii;
        int[] indexes = getSelection();
        for (ii = indexes.length - 1; ii >= 0; --ii) {
            int idx = indexes[ii];
            GpxTrack trk = (GpxTrack) model.getElementAt(idx);
            trk.delMarks();
        }
        birdViewPanel.repaint();
        elevationPanel.repaint();
        speedPanel.repaint();
    }

    /**
	 * Delete the selected tracks.
	 * @throws Exception
	 */
    private void del() throws Exception {
        int ii;
        int[] indexes = getSelection();
        for (ii = indexes.length - 1; ii >= 0; --ii) {
            int idx = indexes[ii];
            model.delGpxTrack(idx);
        }
    }

    /**
	 * Delete all tracks.
	 * @throws Exception
	 */
    private void delAll() throws Exception {
        model.delAllGpxTrack();
    }

    /**
	 * Apply a filter on the selected tracks.
	 * @param parms describes the filter to apply and its parameters.
	 * @throws Exception
	 */
    private void applyFilter(String parms) throws Exception {
        IGpxTrackFilter filter = (IGpxTrackFilter) instantiateBaseFilter(parms);
        if (filter != null) {
            int[] indexes = getSelection();
            for (int ii = 0; ii < indexes.length; ++ii) {
                int idx = indexes[ii];
                GpxTrack gpxSrc = (GpxTrack) model.getElementAt(idx);
                GpxTrack gpxDst = filter.filter(gpxSrc);
                gpxDst.setInfo(filter.getName());
                gpxDst.setName(gpxSrc.getName());
                model.addGpxTrack(gpxDst);
            }
        }
    }

    /**
	 * Apply a multi-filter on the selected tracks.
	 * @param parms describes the filter to apply and its parameters.
	 * @throws Exception
	 */
    private void applyMultiFilter(String parms) throws Exception {
        IGpxTracksFilter filter = (IGpxTracksFilter) instantiateBaseFilter(parms);
        if (filter != null) {
            int[] indexes = getSelection();
            List in = new ArrayList(indexes.length);
            for (int ii = 0; ii < indexes.length; ++ii) {
                int idx = indexes[ii];
                in.add(model.getElementAt(idx));
            }
            List out = filter.filter(in);
            Iterator iiOut = out.iterator();
            while (iiOut.hasNext()) {
                GpxTrack gpxDst = (GpxTrack) iiOut.next();
                model.addGpxTrack(gpxDst);
            }
        }
    }

    /**
	 * Apply a marker on the selected tracks.
	 * @param parms describes the marker to apply and its parameters.
	 * @throws Exception
	 */
    private void applyMarker(String parms) throws Exception {
        int cnt = 0;
        IGpxTrackMarker marker = (IGpxTrackMarker) instantiateBaseMarker(parms);
        if (marker != null) {
            int[] indexes = getSelection();
            for (int ii = 0; ii < indexes.length; ++ii) {
                int idx = indexes[ii];
                GpxTrack gpxSrc = (GpxTrack) model.getElementAt(idx);
                List points = marker.mark(gpxSrc);
                if (points.size() > 0) {
                    gpxSrc.addMarks(points);
                    ++cnt;
                }
            }
        }
        if (cnt > 0) {
            birdViewPanel.repaint();
            elevationPanel.repaint();
            speedPanel.repaint();
        }
    }

    /**
	 * Show a certain map.
	 * @param parms describes the map to be shown.
	 * @throws Exception
	 */
    private void showMap(String parms) throws Exception {
        birdViewPanel.setOsmBaseUrl(parms);
        birdViewPanel.setShowMapOsm(true);
    }

    /**
	 * Instantiate an <code>Object</code>.
	 * @param parms describes the object to instantiate and its parameters.
	 * @return
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
    private IGpxCreatable instantiateCreatable(String parms) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SecurityException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        IGpxCreatable object = null;
        String[] parmsArray = parms.split(",");
        String className = parmsArray[0];
        Class objclass = Class.forName(className);
        if (parmsArray.length > 1) {
            Class[] constructTypes = new Class[parmsArray.length - 1];
            Object[] constructVals = new Object[parmsArray.length - 1];
            for (int ii = 1; ii < parmsArray.length; ++ii) {
                String[] parm = parmsArray[ii].split("=");
                String constructType = parm[0];
                String constructVal = parm[1];
                constructTypes[ii - 1] = Class.forName(constructType);
                Class[] valConstructorArgs = new Class[1];
                valConstructorArgs[0] = Class.forName("java.lang.String");
                Constructor objConstructor = constructTypes[ii - 1].getConstructor(valConstructorArgs);
                String[] valStringArr = new String[1];
                valStringArr[0] = constructVal;
                Object obj = objConstructor.newInstance(valStringArr);
                constructVals[ii - 1] = obj;
            }
            Constructor constructor = objclass.getConstructor(constructTypes);
            object = (IGpxCreatable) constructor.newInstance(constructVals);
        } else {
            object = (IGpxCreatable) objclass.newInstance();
            ArgsRequestor argsReq = new ArgsRequestor(mainFrame, className, object);
            object = argsReq.getObject();
        }
        return object;
    }

    /**
	 * Instantiate an <code>IGpxBaseMarker</code>.
	 * If parms only contain the marker's class name then a requestor will
	 * be opened to ask the user for the parameters.
	 * @param parms describes the filter to instantiate and its parameters.
	 * @return
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
    private IGpxBaseMarker instantiateBaseMarker(String parms) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SecurityException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        IGpxCreatable object = instantiateCreatable(parms);
        return (IGpxBaseMarker) object;
    }

    /**
	 * Instantiate an <code>IGpxBaseFilter</code>.
	 * If parms only contain the filters class name then a requestor will
	 * be opened to ask the user for the parameters.
	 * @param parms describes the filter to instantiate and its parameters.
	 * @return
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
    private IGpxBaseFilter instantiateBaseFilter(String parms) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SecurityException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        IGpxCreatable object = instantiateCreatable(parms);
        return (IGpxBaseFilter) object;
    }

    /**
	 * Save the selected tracks in <em>one</em> GPX file.
	 * @throws Exception
	 */
    private void saveAsGpx() throws Exception {
        int ii;
        int[] indexes = getSelection();
        if (indexes != null && indexes.length > 0) {
            if (dirGpx == null) {
                dirGpx = props.getProperty(PROP_DIR_GPX);
            }
            JFileChooser chooser = createFileChooser(false, dirGpx, new GpxFileFilter());
            int returnVal = chooser.showDialog(null, "Save as GPX file");
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                String fName = chooser.getSelectedFile().getPath();
                GpxFile gpxFile = new GpxFile(fName);
                for (ii = 0; ii < indexes.length; ++ii) {
                    int idx = indexes[ii];
                    GpxTrack gpxSrc = (GpxTrack) model.getElementAt(idx);
                    gpxFile.addTrack(gpxSrc);
                }
                GpxFileCreator creator = new GpxFileCreator(props);
                creator.write(gpxFile);
                dirGpx = chooser.getCurrentDirectory().getCanonicalPath();
            }
        }
    }

    /**
	 * Get properties with a given prefix.
	 * @param props Properties from which we will select certain properties.
	 * @param prefix The prefix.
	 * @return The selected properties <em>without</em> the prefix
	 */
    private Properties getPropsWithPrefix(Properties props, String prefix) {
        Properties prefixed = new Properties();
        if (props != null) {
            Enumeration keys = props.propertyNames();
            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
                if (key.startsWith(prefix)) {
                    prefixed.setProperty(key.substring(prefix.length()), props.getProperty(key));
                }
            }
        }
        return prefixed;
    }
}
