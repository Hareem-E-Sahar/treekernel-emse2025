package core.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedList;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import visualizer.ThebaInterface;
import com.jgoodies.looks.BorderStyle;
import com.jgoodies.looks.HeaderStyle;
import com.jgoodies.looks.Options;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;
import com.jgoodies.looks.plastic.theme.Silver;
import core.BoxBounds;
import core.ImageFunctions;
import core.Plugin;
import core.Region;
import core.RegionDescriptor;
import core.RegionMask;
import core.RegionMask2D;
import core.Stack;
import core.Tracker;
import core.io.DataConversion;
import core.io.HeapReader;
import core.io.SliceReader;
import core.io.VolumeReader;
import core.math.Point3D;

/**
 * This is the class is responsible for most UI interaction such as menu layout,
 * and listeners.
 * 
 * @author bachewii
 * 
 */
public class Theba implements ActionListener {

    public ArrayList lumencenters;

    public static int MAX_FIBERS = 2048;

    public int width, height, depth;

    private short selectedRegion;

    public static final short INVALID = 7;

    private JButton stopButton;

    private JMenu analyzeMenu;

    private JTextArea area;

    private ButtonGroup buttonGroup;

    private JFileChooser chooser;

    private VolumeReader datawriter;

    private JToggleButton deleteButton;

    private JSlider depthSlider;

    private JMenu fileMenu;

    private JMenu filterMenu;

    private JMenuItem flipItem;

    private JCheckBoxMenuItem hideWhite;

    private JTextField idSelector;

    private int inputType;

    private ImagePane iw;

    private String labeltext;

    private JFrame mainWindow;

    private JToggleButton measureButton;

    private JToggleButton measureButton3d;

    private JMenuBar menuBar;

    private boolean menuEnabled;

    private String pixeltext;

    private ThebaPrefs preferences;

    private JProgressBar progressbar;

    private int regionCount;

    private LinkedList<Region> regList;

    private JScrollPane resultScrollPane;

    private JFrame resultWindow;

    private JScrollPane scrollpane;

    private JToggleButton seedButton3d;

    private JLabel stackLabel;

    private JPanel statusbar;

    private JTabbedPane tabPane;

    private JToolBar toolbar;

    private Tracker tracker;

    private JMenu trackMenu;

    private core.Stack volume;

    private boolean isStopped;

    private JLabel statusLabel;

    private JToggleButton visualiseButton;

    private String fileName;

    public static final int BYTE_TYPE = 0;

    public static final int SHORT_TYPE = 1;

    private static final short ID_OFFSET = 256;

    static int count = 0;

    static short[] empty;

    static Theba instance = null;

    static short[] tmp;

    private Theba() {
        if (!System.getProperty("os.name").equals("Mac OS X")) {
            PlasticLookAndFeel.setCurrentTheme(new Silver());
            UIManager.put(Options.USE_SYSTEM_FONTS_APP_KEY, Boolean.TRUE);
            UIManager.put("jgoodies.popupDropShadowEnabled", Boolean.FALSE);
            UIManager.put(Options.POPUP_DROP_SHADOW_ENABLED_KEY, Boolean.TRUE);
            UIManager.put("ToggleButton.is3DEnabled", Boolean.FALSE);
            Options.setUseSystemFonts(true);
            try {
                UIManager.setLookAndFeel(new PlasticXPLookAndFeel());
            } catch (Exception e) {
            }
        }
        mainWindow = new JFrame("Theba 1.0.2");
        mainWindow.setLayout(new BorderLayout());
        mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainWindow.setSize(800, 500);
        createMenu();
        createImageWindow();
        createStatusBar();
        createToolbar();
        mainWindow.setLocationRelativeTo(null);
        if (!System.getProperty("os.name").equals("Mac OS X")) {
            menuBar.putClientProperty(Options.HEADER_STYLE_KEY, HeaderStyle.BOTH);
            toolbar.putClientProperty(Options.HEADER_STYLE_KEY, HeaderStyle.BOTH);
            menuBar.putClientProperty(PlasticLookAndFeel.BORDER_STYLE_KEY, BorderStyle.SEPARATOR);
            toolbar.putClientProperty(PlasticLookAndFeel.BORDER_STYLE_KEY, BorderStyle.SEPARATOR);
        }
        mainWindow.setVisible(true);
        showOpenDialog();
    }

    /**
	 * This method contains actions for some of the menu elements
	 * 
	 */
    public void actionPerformed(final ActionEvent e) {
        disableMenus();
        (new Thread() {

            public void run() {
                if (e.getActionCommand().equals("Quit")) {
                    System.exit(0);
                } else if (e.getActionCommand().equals("Stop")) {
                    tracker.stop();
                } else if (e.getActionCommand().equals("Flip 90")) {
                    flipVolume();
                } else if (e.getActionCommand().equals("About")) {
                    StringBuffer buf = new StringBuffer();
                    buf.append("Created by Per Christian Henden and Jens Bache-Wiig\n\n");
                    buf.append("Installed descriptors :\n");
                    ArrayList descriptors = getRegionDescriptors();
                    for (int i = 0; i < descriptors.size(); i++) {
                        RegionDescriptor desc = (RegionDescriptor) descriptors.get(i);
                        buf.append("  " + desc.getName() + "\n");
                    }
                    buf.append("Installed trackers :\n");
                    ArrayList trackers = getTrackers();
                    for (int i = 0; i < trackers.size(); i++) {
                        buf.append("  " + trackers.get(i).getClass().getName() + "\n");
                    }
                    JOptionPane.showMessageDialog(null, buf);
                } else if (e.getActionCommand().equals("Save...")) {
                    chooser = new JFileChooser();
                    chooser.showSaveDialog(null);
                    File outFile = chooser.getSelectedFile();
                    System.out.println("You selected " + outFile);
                    if (outFile != null) {
                        try {
                            FileOutputStream fs = new FileOutputStream(outFile);
                            DataOutputStream fo = new DataOutputStream(fs);
                            int i;
                            byte[] tmp;
                            for (i = 0; i < depth; i++) {
                                short[] data = getSlice(i);
                                tmp = DataConversion.shortToBytes(data);
                                fo.write(tmp);
                                setProgress(i);
                            }
                            if (i < depth) {
                                JOptionPane.showMessageDialog(null, "Så mange slices har jeg ikke! Du fikk " + (i + 1));
                                depth = i;
                            }
                            fs.close();
                        } catch (IOException e2) {
                            JOptionPane.showMessageDialog(mainWindow, e2.getMessage());
                            enableMenus();
                        }
                        updateImage();
                        depthSlider.setMaximum(depth - 1);
                        setProgress(depth);
                        mainWindow.repaint();
                    }
                }
                enableMenus();
            }
        }).start();
    }

    /**
	 * This method generates fiber-chord measurements for the given volume. This
	 * involves calculation of the mean distance between porous regions in x,y
	 * and z-directions
	 * 
	 */
    public void fiberChordMeasurements() {
        final Stack stack = getStack();
        double length = 0;
        double count = 0;
        double total = 0;
        double nlength = 0;
        double ncount = 0;
        double ntotal = 0;
        for (int x = 0; x < stack.getWidth(); x++) {
            setProgressbar(x, stack.getWidth());
            if (isStopped()) return;
            for (int y = 0; y < stack.getHeight(); y++) {
                for (int z = 0; z < stack.getDepth(); z++) {
                    short voxel = stack.getVoxel(x, y, z);
                    if (voxel == 0 && z < stack.getDepth()) length++; else if (length > 0) {
                        count++;
                        total += length;
                        length = 0;
                    }
                    if (voxel != 0 && z < stack.getDepth()) nlength++; else if (nlength > 0) {
                        ncount++;
                        ntotal += nlength;
                        nlength = 0;
                    }
                }
            }
        }
        double zaverageLength = total / (double) count;
        double zaverageLength_inverse = ntotal / (double) ncount;
        length = 0;
        total = 0;
        count = 0;
        nlength = 0;
        ntotal = 0;
        ncount = 0;
        for (int x = 0; x < stack.getWidth(); x++) {
            setProgressbar(x, stack.getWidth());
            if (isStopped()) return;
            for (int z = 0; z < stack.getDepth(); z++) {
                for (int y = 0; y < stack.getHeight(); y++) {
                    short voxel = stack.getVoxel(x, y, z);
                    if (voxel == 0 && y < stack.getHeight()) length++; else if (length > 0) {
                        count++;
                        total += length;
                        length = 0;
                    }
                    if (voxel != 0 && y < stack.getHeight()) nlength++; else if (nlength > 0) {
                        ncount++;
                        ntotal += nlength;
                        nlength = 0;
                    }
                }
            }
        }
        double yaverage = total / count;
        double yaverage_inverse = ntotal / ncount;
        length = 0;
        total = 0;
        count = 0;
        nlength = 0;
        ntotal = 0;
        ncount = 0;
        for (int y = 0; y < stack.getHeight(); y++) {
            setProgressbar(y, stack.getHeight());
            if (isStopped()) return;
            for (int z = 0; z < stack.getDepth(); z++) {
                for (int x = 0; x < stack.getWidth(); x++) {
                    short voxel = stack.getVoxel(x, y, z);
                    if (voxel == 0 && x < stack.getWidth()) length++; else if (length > 0) {
                        count++;
                        total += length;
                        length = 0;
                    }
                    if (voxel != 0 && x < stack.getWidth()) nlength++; else if (nlength > 0) {
                        ncount++;
                        ntotal += nlength;
                        nlength = 0;
                    }
                }
            }
        }
        double xaverage = total / count;
        double xaverage_inverse = ntotal / ncount;
        showResults(" Average length zero values x = " + xaverage + " y = " + yaverage + " z = " + zaverageLength);
        showResults(" Average length non_zero values x = " + xaverage_inverse + " y = " + yaverage_inverse + " z = " + zaverageLength_inverse);
    }

    /**
	 * Adds a channel to the tabbedview window if it is not already added (based
	 * on equal name)
	 * 
	 * @param reader
	 *            The input slicereader (or writer)
	 * @param name
	 *            The tab-name
	 */
    public void addChannel(SliceReader reader, String name) {
        boolean found = false;
        for (int i = 0; i < tabPane.getComponentCount(); i++) if (tabPane.getComponent(i).getName().equals(name)) found = true;
        if (!found) {
            ImagePane newPane = new ImagePane(this, reader);
            JScrollPane sp = new JScrollPane(newPane);
            sp.setName(name);
            tabPane.add(sp);
        }
    }

    /**
	 * Used by trackers to add menu-items to the track-menu
	 * 
	 * @param title
	 *            Title for menu-item
	 * @param action
	 *            A runnable method containing executable code for this
	 *            menu-Item
	 */
    public void addMenuItem(String title, final Runnable action) {
        JMenuItem menuItem = new JMenuItem(title);
        ActionListener threadListener = new ActionListener() {

            public void actionPerformed(final ActionEvent arg0) {
                Thread r = new Thread() {

                    public void run() {
                        disableMenus();
                        action.run();
                        enableMenus();
                    }
                };
                r.start();
            }

            ;
        };
        menuItem.addActionListener(threadListener);
        trackMenu.add(menuItem);
    }

    public void addMenuSeparator() {
        trackMenu.addSeparator();
    }

    /**
	 * Menu used by Trackers to add a button to the toolbar.
	 * 
	 * @param customButton
	 */
    public void addToolbarButton(JComponent customButton) {
        if (customButton instanceof JToggleButton) {
            buttonGroup.add((JToggleButton) customButton);
        }
        toolbar.add(new JLabel("Tracker tools :"));
        toolbar.add(customButton);
    }

    /**
	 * Resets tabbed-pane view
	 * 
	 * @todo Should be rewritten to remove all channels
	 */
    public void clearChannels() {
        tabPane.setSelectedIndex(0);
    }

    /**
	 * Creates a new image-window for the current stack
	 */
    private void createImageWindow() {
        iw = new ImagePane(this);
        scrollpane = new JScrollPane(iw);
        depthSlider = new JSlider();
        depthSlider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                JScrollPane spane = (JScrollPane) tabPane.getSelectedComponent();
                ImagePane ipane = (ImagePane) spane.getViewport().getComponent(0);
                ipane.updateData(currentSlice());
                labeltext = "Stack " + currentSlice() + "/" + (depth - 1);
                stackLabel.setText(labeltext + pixeltext);
                mainWindow.repaint();
            }
        });
        depthSlider.setOrientation(JSlider.VERTICAL);
        scrollpane.setName("Main view");
        tabPane = new JTabbedPane();
        tabPane.add(scrollpane);
        tabPane.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                if (tabPane.getSelectedIndex() == 0) {
                    iw.setData(getSlice(currentSlice()), width, height);
                } else {
                    JScrollPane spane = (JScrollPane) tabPane.getSelectedComponent();
                    ImagePane ipane = (ImagePane) spane.getViewport().getComponent(0);
                    ipane.showSlice(currentSlice());
                }
            }

            ;
        });
        mainWindow.add(tabPane, BorderLayout.CENTER);
        mainWindow.add(depthSlider, BorderLayout.EAST);
        iw.addMouseListener(new MouseClickListener());
    }

    /**
	 * Layout of menu for this application
	 */
    private void createMenu() {
        menuBar = new JMenuBar();
        fileMenu = new JMenu("File");
        fileMenu.setMnemonic('f');
        JMenuItem fileOpenItem = new JMenuItem("Open...");
        fileOpenItem.setAccelerator(KeyStroke.getKeyStroke('o'));
        fileOpenItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                (new Thread() {

                    public void run() {
                        showOpenDialog();
                    }
                }).start();
            }
        });
        fileMenu.add(fileOpenItem);
        JMenuItem saveItem = new JMenuItem("Save...");
        saveItem.setAccelerator(KeyStroke.getKeyStroke('s'));
        fileMenu.add(saveItem);
        saveItem.addActionListener(this);
        JMenuItem quitItem = new JMenuItem("Quit");
        quitItem.setMnemonic('q');
        fileMenu.add(quitItem);
        quitItem.addActionListener(this);
        filterMenu = new JMenu("Filters");
        filterMenu.setMnemonic('i');
        flipItem = new JMenuItem("Flip 90");
        trackMenu = new JMenu("Track");
        trackMenu.setMnemonic('t');
        JMenu helpMenu = new JMenu("Help");
        JMenu filterHelpMenu = new JMenu("Filter");
        helpMenu.add(filterHelpMenu);
        ArrayList<Plugin> plugins = getPlugins();
        for (final Plugin p : plugins) {
            p.setup(this);
            final JMenuItem item = new JMenuItem(p.getName());
            item.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    disableMenus();
                    (new Thread() {

                        public void run() {
                            p.process(volume);
                            updateImage();
                            enableMenus();
                        }
                    }).start();
                }

                ;
            });
            final JMenuItem helpitem = new JMenuItem(p.getName());
            helpitem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    if (p.getAbout() == null) JOptionPane.showMessageDialog(mainWindow, "No description available"); else JOptionPane.showMessageDialog(mainWindow, p.getAbout());
                }

                ;
            });
            filterHelpMenu.add(helpitem);
            if (p.getCategory() != null) {
                JMenu category = null;
                Component[] components = filterMenu.getMenuComponents();
                for (int j = 0; j < components.length; j++) {
                    Component s = components[j];
                    if (s.getName() != null) {
                        if (s.getName().equals(p.getCategory())) {
                            category = (JMenu) s;
                        }
                    }
                }
                if (category == null) {
                    category = new JMenu(p.getCategory());
                    category.setName(p.getCategory());
                    filterMenu.add(category);
                }
                category.add(item);
            } else {
                filterMenu.add(item);
            }
        }
        JMenu regionHelpMenu = new JMenu("Region descriptors");
        ArrayList<RegionDescriptor> descriptors = getRegionDescriptors();
        for (final RegionDescriptor p : descriptors) {
            final JMenuItem helpitem = new JMenuItem(p.getName());
            helpitem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    if (p.getAbout() == null) JOptionPane.showMessageDialog(mainWindow, "No description available"); else JOptionPane.showMessageDialog(mainWindow, p.getAbout());
                }

                ;
            });
            regionHelpMenu.add(helpitem);
        }
        helpMenu.add(regionHelpMenu);
        filterMenu.addSeparator();
        JMenuItem removeMarkedVoxels = new JMenuItem("Remove marked");
        removeMarkedVoxels.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                final Stack activeVolume = getCurrentView().getVolume();
                disableMenus();
                Thread r = new Thread() {

                    public void run() {
                        byte ff = (byte) 0xff;
                        int count = 0;
                        for (int z = 0; z < activeVolume.getDepth(); z++) {
                            if (isStopped) return;
                            setProgress(z);
                            short[] slice = activeVolume.getSlice(z);
                            for (int i = 0; i < slice.length; i++) {
                                if (slice[i] != 0 && slice[i] != ff) {
                                    count++;
                                    slice[i] = 0;
                                }
                            }
                        }
                        activeVolume.flush();
                        StringBuffer output = new StringBuffer();
                        output.append("Removed " + count + " voxels \n");
                        showResults(output, "Voxelcount");
                        enableMenus();
                        updateImage();
                    }
                };
                r.start();
            }
        });
        JMenuItem removeSmallRegions = new JMenuItem("Remove small labeled regions");
        removeSmallRegions.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Thread r = new Thread() {

                    public void run() {
                        String input = JOptionPane.showInputDialog(null, "Enter minmum regsize.", 100);
                        if (input == null) return;
                        int tval = Integer.parseInt(input);
                        setLabel("Creating histogram...");
                        long[] histo = new long[MAX_FIBERS + 256];
                        for (int z = 0; z < volume.getDepth(); z++) {
                            setProgress(z);
                            short[] slice = volume.getSlice(z);
                            for (int i = 0; i < slice.length; i++) {
                                histo[slice[i]]++;
                            }
                        }
                        setLabel("Removing regions");
                        for (int z = 0; z < volume.getDepth(); z++) {
                            setProgress(z);
                            short[] slice = volume.getSlice(z);
                            for (int i = 0; i < slice.length; i++) {
                                if (histo[slice[i]] < tval) slice[i] = 0;
                            }
                        }
                        enableMenus();
                        updateImage();
                    }
                };
                disableMenus();
                r.start();
            }
        });
        JMenuItem removeUnmarkedVoxels = new JMenuItem("Remove unlabeled");
        removeUnmarkedVoxels.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                final Stack activeVolume = getCurrentView().getVolume();
                disableMenus();
                Thread r = new Thread() {

                    public void run() {
                        int count = 0;
                        for (int z = 0; z < activeVolume.getDepth(); z++) {
                            if (isStopped) return;
                            setProgress(z);
                            short[] slice = activeVolume.getSlice(z);
                            for (int i = 0; i < slice.length; i++) {
                                if (slice[i] == 255) {
                                    count++;
                                    slice[i] = 0;
                                }
                            }
                        }
                        activeVolume.flush();
                        StringBuffer output = new StringBuffer();
                        output.append("Removed " + count + " voxels \n");
                        showResults(output, "Voxelcount");
                        enableMenus();
                        updateImage();
                    }
                };
                r.start();
            }
        });
        JMenuItem resetMarkedVoxels = new JMenuItem("Remove colors");
        resetMarkedVoxels.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                final Stack activeVolume = getCurrentView().getVolume();
                disableMenus();
                Thread r = new Thread() {

                    public void run() {
                        byte ff = (byte) 0xff;
                        int count = 0;
                        for (int z = 0; z < activeVolume.getDepth(); z++) {
                            setProgress(z);
                            short[] slice = activeVolume.getSlice(z);
                            for (int i = 0; i < slice.length; i++) {
                                if (slice[i] != 0 && slice[i] != ff) {
                                    count++;
                                    slice[i] = 0;
                                }
                            }
                        }
                        activeVolume.flush();
                        StringBuffer output = new StringBuffer();
                        output.append("Reset " + count + " voxels \n");
                        showResults(output, "Voxelcount");
                        enableMenus();
                        updateImage();
                    }
                };
                r.start();
            }
        });
        JMenuItem resetLabels = new JMenuItem("Reset labels");
        resetMarkedVoxels.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                final Stack activeVolume = getCurrentView().getVolume();
                disableMenus();
                Thread r = new Thread() {

                    public void run() {
                        int count = 0;
                        for (int z = 0; z < activeVolume.getDepth(); z++) {
                            setProgress(z);
                            short[] slice = activeVolume.getSlice(z);
                            for (int i = 0; i < slice.length; i++) {
                                if (slice[i] != 0) {
                                    count++;
                                    slice[i] = 255;
                                }
                            }
                        }
                        activeVolume.flush();
                        StringBuffer output = new StringBuffer();
                        output.append("Reset " + count + " voxels \n");
                        showResults(output, "Voxelcount");
                        enableMenus();
                        updateImage();
                    }
                };
                r.start();
            }
        });
        filterMenu.add(removeMarkedVoxels);
        filterMenu.add(removeUnmarkedVoxels);
        filterMenu.add(resetMarkedVoxels);
        filterMenu.add(resetLabels);
        filterMenu.addSeparator();
        filterMenu.add(removeSmallRegions);
        filterMenu.addSeparator();
        flipItem.addActionListener(this);
        filterMenu.add(flipItem);
        helpMenu.setMnemonic('h');
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(this);
        helpMenu.add(aboutItem);
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic('e');
        JMenuItem undoItem = new JMenuItem("Undo");
        JMenuItem preferencesItem = new JMenuItem("Preferences...");
        preferencesItem.setMnemonic('p');
        undoItem.setEnabled(false);
        editMenu.add(undoItem);
        editMenu.add(preferencesItem);
        JMenuItem reloadItem = new JMenuItem("Reload stack");
        reloadItem.setMnemonic('r');
        reloadItem.setAccelerator(KeyStroke.getKeyStroke('r'));
        reloadItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Thread r = new Thread() {

                    public void run() {
                        loadStack();
                    }
                };
                r.start();
            }
        });
        editMenu.add(reloadItem);
        preferencesItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                preferences = new ThebaPrefs(mainWindow);
                preferences.setVisible(true);
            }
        });
        analyzeMenu = new JMenu("Analyze");
        analyzeMenu.setMnemonic('a');
        JMenuItem fiberMenu = new JMenu("Region measurements");
        JMenuItem measureAllItem = new JMenuItem("Perform all");
        for (final RegionDescriptor d : descriptors) {
            if (d.does3D()) {
                JMenuItem item = new JMenuItem("Measure :" + d.getName());
                ActionListener listener = new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        final Stack volume = getCurrentView().getVolume();
                        disableMenus();
                        Thread r = new Thread() {

                            public void run() {
                                setLabel("Counting regions...");
                                StringBuffer buf = new StringBuffer("Bounding regions...");
                                showResults(buf, "Results");
                                regList = getRegions();
                                progressbar.setMaximum(regList.size());
                                for (int i = 0; i < regList.size(); i++) {
                                    buf = new StringBuffer();
                                    Region reg = (Region) regList.get(i);
                                    setProgress(i);
                                    if (reg == null) continue;
                                    regionCount++;
                                    buf.append("ID " + reg.getId() + " ");
                                    RegionMask mask = new RegionMask(reg.getId(), volume, reg.getBounds());
                                    buf.append(d.measure(mask));
                                    showResults(buf, "Results");
                                }
                                buf.append("\nRegioncount :" + regList.size() + " \n");
                                progressbar.setMaximum(depth);
                                enableMenus();
                            }
                        };
                        r.start();
                    }
                };
                item.addActionListener(listener);
                fiberMenu.add(item);
            }
        }
        fiberMenu.add(measureAllItem);
        measureAllItem.addActionListener(new RegionAnalysisAction());
        JMenu bulkMenu = new JMenu("Bulk");
        analyzeMenu.add(fiberMenu);
        analyzeMenu.add(bulkMenu);
        JMenuItem poreItem = new JMenuItem("Pore chords");
        poreItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                disableMenus();
                Thread r = new Thread() {

                    public void run() {
                        fiberChordMeasurements();
                        enableMenus();
                    }
                };
                r.start();
            }
        });
        bulkMenu.add(poreItem);
        JMenuItem calcForeGroundItem = new JMenuItem("Volume");
        calcForeGroundItem.addActionListener(new RegCountAction());
        JMenuItem histogramItem = new JMenuItem("Histogram");
        histogramItem.addActionListener(new HistogramAction());
        bulkMenu.add(calcForeGroundItem);
        bulkMenu.add(histogramItem);
        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic('v');
        hideWhite = new JCheckBoxMenuItem("Hide unlabeled");
        viewMenu.add(hideWhite);
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        menuBar.add(filterMenu);
        menuBar.add(trackMenu);
        menuBar.add(analyzeMenu);
        menuBar.add(helpMenu);
        mainWindow.setJMenuBar(menuBar);
    }

    /**
	 * Statusbar layout method
	 */
    private void createStatusBar() {
        statusbar = new JPanel();
        mainWindow.add(statusbar, BorderLayout.SOUTH);
        stackLabel = new JLabel("Stack 0/0");
        statusLabel = new JLabel();
        statusbar.add(statusLabel);
        statusbar.add(stackLabel);
        progressbar = new JProgressBar();
        progressbar.setPreferredSize(new Dimension(300, 10));
        progressbar.setForeground(mainWindow.getBackground().darker());
        statusbar.add(progressbar);
        statusbar.setLayout(new FlowLayout(FlowLayout.RIGHT));
    }

    /**
	 * Initialize top-screen toolbar
	 */
    private void createToolbar() {
        if (toolbar == null) {
            toolbar = new JToolBar("tools");
            toolbar.setLayout(new FlowLayout(FlowLayout.LEADING));
            measureButton = new JToggleButton("Measure 2D");
            measureButton3d = new JToggleButton("Measure 3D");
            visualiseButton = new JToggleButton("Visualize");
            seedButton3d = new JToggleButton("Fill ");
            idSelector = new JTextField("255");
            stopButton = new JButton("Stop");
            stopButton.setForeground(Color.red);
            stopButton.setEnabled(false);
            stopButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    tracker.stop();
                    isStopped = true;
                    enableMenus();
                }
            });
            idSelector.setColumns(4);
            deleteButton = new JToggleButton("Delete");
            buttonGroup = new ButtonGroup();
            buttonGroup.add(seedButton3d);
            buttonGroup.add(deleteButton);
            buttonGroup.add(measureButton);
            buttonGroup.add(measureButton3d);
            buttonGroup.add(visualiseButton);
            hideWhite.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    updateImage();
                }
            });
            idSelector.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    try {
                        selectedRegion = Short.parseShort(idSelector.getText());
                        System.out.println(selectedRegion);
                    } catch (NumberFormatException e2) {
                        selectedRegion = 0;
                        idSelector.setText("0");
                    }
                }
            });
        } else {
            mainWindow.remove(toolbar);
            toolbar = new JToolBar();
            toolbar.setLayout(new FlowLayout(FlowLayout.LEADING));
            mainWindow.add(toolbar, BorderLayout.NORTH);
        }
        toolbar.add(new JLabel("Tools "));
        toolbar.add(measureButton);
        toolbar.add(measureButton3d);
        toolbar.add(visualiseButton);
        toolbar.addSeparator();
        toolbar.add(seedButton3d);
        toolbar.add(idSelector);
        toolbar.add(deleteButton);
        toolbar.addSeparator();
        statusbar.add(stopButton);
        toolbar.addSeparator();
    }

    /**
	 * Returns the depth index of currently selected (by the depthslicer) slice
	 * 
	 * @return the depth/z-index of the current slice
	 */
    public synchronized int currentSlice() {
        return depthSlider.getValue();
    }

    /**
	 * Disables all menu elements and shows the wait-cursor. Should be executed
	 * before any time-consuming tasks are performed Note that enableMenus must
	 * allways be executed after this call to avoid locking up the interface
	 */
    private void disableMenus() {
        isStopped = false;
        stopButton.setEnabled(true);
        mainWindow.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        menuEnabled = false;
        toolbar.setEnabled(false);
        fileMenu.setEnabled(false);
        filterMenu.setEnabled(false);
        trackMenu.setEnabled(false);
        menuBar.setEnabled(false);
        analyzeMenu.setEnabled(false);
        seedButton3d.setEnabled(false);
        deleteButton.setEnabled(false);
    }

    /**
	 * Executed after disableMenus have been called to re-enable menus and
	 * buttons as well as returning the cursor back to its initial state
	 */
    private void enableMenus() {
        stopButton.setEnabled(false);
        mainWindow.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        menuEnabled = true;
        analyzeMenu.setEnabled(true);
        toolbar.setEnabled(true);
        fileMenu.setEnabled(true);
        filterMenu.setEnabled(true);
        trackMenu.setEnabled(true);
        deleteButton.setEnabled(true);
        seedButton3d.setEnabled(true);
        setProgressComplete();
    }

    /**
	 * Flips the volume 90 degrees by swapping width with depth. Note that the
	 * height-dimension is preserved.
	 * 
	 * @todo Make a method for hight-depth swap as well
	 */
    public void flipVolume() {
        try {
            RandomAccessFile file2 = new RandomAccessFile("temp_vol.raw", "rw");
            volume.flush();
            progressbar.setMaximum(width);
            for (int x = 0; x < width; x++) {
                if (isStopped) {
                    return;
                }
                setProgress(x / 2);
                short[] nslice = new short[depth * height];
                for (int z = 0; z < depth; z++) {
                    short[] slice = getSlice(z);
                    for (int y = 0; y < height; y++) {
                        nslice[z + y * depth] = (short) (slice[x + y * width]);
                    }
                }
                file2.write(DataConversion.shortToBytes(nslice));
            }
            file2.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        datawriter.destroy();
        int tmp = width;
        width = depth;
        depth = tmp;
        disableMenus();
        inputType = SHORT_TYPE;
        String tmpName = fileName;
        fileName = "temp_vol.raw";
        loadStack();
        fileName = tmpName;
        tracker.reset();
        enableMenus();
    }

    /**
	 * Returns the currently selected depth-slice
	 * 
	 * @return The currently selected image-slice
	 */
    public short[] getCurrentPixels() {
        return getSlice(currentSlice());
    }

    /**
	 * Returns the ImagePane that is currently selected (usually the main view)
	 * 
	 * @return The selected ImagePane
	 */
    public ImagePane getCurrentView() {
        if (tabPane.getSelectedIndex() == 0) {
            return iw;
        }
        JScrollPane spane = (JScrollPane) tabPane.getSelectedComponent();
        ImagePane ipane = (ImagePane) spane.getViewport().getComponent(0);
        return ipane;
    }

    /**
	 * Returns an object containing the current preferences for this application
	 * (not that several instances running at once, may use shared properties)
	 * 
	 * @return The ThebaPrefs object containing preferences relating to
	 *         FibForks
	 */
    public ThebaPrefs getPreferences() {
        if (preferences == null) preferences = new ThebaPrefs(mainWindow);
        return preferences;
    }

    /**
	 * Returns a list of the plugins in the plugins menu.
	 * 
	 * @return An ArrayList containing an instance of all available
	 *         RegionDescriptor-classes
	 */
    public synchronized ArrayList<RegionDescriptor> getRegionDescriptors() {
        ArrayList<RegionDescriptor> descriptors = new ArrayList<RegionDescriptor>();
        File f = new File("descriptors/");
        if (!f.exists()) {
            JOptionPane.showMessageDialog(mainWindow, "Descriptor directiory not found at " + f.getAbsolutePath());
            return null;
        }
        String[] descriptorNames = f.list();
        try {
            for (int i = 0; i < descriptorNames.length; i++) {
                if (descriptorNames[i].endsWith(".class")) {
                    String name = descriptorNames[i].substring(0, descriptorNames[i].length() - 6);
                    ClassLoader loader = getClass().getClassLoader().getSystemClassLoader();
                    Class c = loader.loadClass("descriptors." + name);
                    if (c.getInterfaces().length > 0 && c.getInterfaces()[0].getName().equals("core.RegionDescriptor")) {
                        Object o = c.newInstance();
                        descriptors.add((RegionDescriptor) o);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return descriptors;
    }

    /**
	 * Returns the short[]-array associated with the current stack-slice width
	 * and height dimensions corresponds to the current getStack()-object
	 * 
	 * @param The
	 *            depth-index in the stack
	 * @return A short[]-array representing the current stack at posistion z
	 */
    public short[] getSlice(int z) {
        VolumeReader ip = getCurrentView().getSliceReader();
        if (ip == null) return null;
        return ip.getSlice(z);
    }

    /**
	 * @return The currently selected tracker (indicated by the file-open
	 *         dialog)
	 */
    public Tracker getTracker() {
        return tracker;
    }

    /**
	 * Returns a list of the available Plugins.
	 * 
	 * @return An Arraylist<Plugin> containing all available plugin-instances
	 */
    public synchronized ArrayList<Plugin> getPlugins() {
        ArrayList<Plugin> plugins = new ArrayList<Plugin>();
        File f = new File("plugins/");
        if (!f.exists()) {
            JOptionPane.showMessageDialog(mainWindow, "Plugin-directory not found at " + f.getAbsolutePath());
            return null;
        }
        String[] descriptorNames = f.list();
        try {
            for (int i = 0; i < descriptorNames.length; i++) {
                if (descriptorNames[i].endsWith(".class")) {
                    String name = "plugins." + (descriptorNames[i].substring(0, descriptorNames[i].length() - 6));
                    if (!name.contains("$")) {
                        ClassLoader loader = getClass().getClassLoader().getSystemClassLoader();
                        Constructor c = loader.loadClass(name).getConstructors()[0];
                        plugins.add((Plugin) c.newInstance());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return plugins;
    }

    /**
	 * Returns a list of the trackers.
	 * 
	 * @return An ArrayList containing instances of all availbale
	 *         Tracker-classes
	 */
    public synchronized ArrayList<Tracker> getTrackers() {
        ArrayList<Tracker> trackers = new ArrayList<Tracker>();
        File f = new File("trackers/");
        if (!f.exists()) {
            JOptionPane.showMessageDialog(mainWindow, "Tracker-directory not found at " + f.getAbsolutePath());
            return null;
        }
        String[] descriptorNames = f.list();
        try {
            for (int i = 0; i < descriptorNames.length; i++) {
                if (descriptorNames[i].endsWith(".class")) {
                    String name = "trackers." + (descriptorNames[i].substring(0, descriptorNames[i].length() - 6));
                    if (!name.contains("$")) {
                        ClassLoader loader = getClass().getClassLoader().getSystemClassLoader();
                        Constructor c = loader.loadClass(name).getConstructors()[0];
                        Object[] args = new Object[1];
                        args[0] = this;
                        trackers.add((Tracker) c.newInstance(args));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return trackers;
    }

    /**
	 * @return The currently active stack-object
	 */
    public Stack getStack() {
        return getCurrentView().getVolume();
    }

    /**
	 * Returns the application window
	 * 
	 * @return The active application window (with associated position, size
	 *         etc)
	 */
    public JFrame getWindow() {
        return mainWindow;
    }

    /**
	 * @return the hideWhite checkbox-item, that indicates if white voxels
	 *         should be filtered from view
	 */
    public JCheckBoxMenuItem hideWhite() {
        return hideWhite;
    }

    /**
	 * Returns true if the application window is hidden from view
	 * 
	 * @return true if application should be hidden from view
	 */
    public boolean isMinimized() {
        if (mainWindow.getState() != Frame.ICONIFIED) return false;
        return true;
    }

    /**
	 * Loads a data-stack from a raw fileformat according to current preferences
	 * (set by openStack)
	 */
    private void loadStack() {
        volume = null;
        if (datawriter != null) datawriter.destroy();
        initIdList();
        disableMenus();
        SliceReader sr = new SliceReader(fileName, width, height, depth, 0);
        if (getPreferences().getInt("use_cached_access", 0) == 0) {
            datawriter = new HeapReader(width, height, depth);
            volume = new Stack(datawriter);
        } else {
            volume = new Stack(width, height, depth, getPreferences().getString("tempFileName", "temp.raw"));
            datawriter = volume.getWriter();
        }
        iw.setReader(datawriter);
        depthSlider.setMaximum(depth - 1);
        progressbar.setMaximum(depth - 1);
        RandomAccessFile ra = sr.getFile();
        totalCount = 0;
        for (int i = 0; i < depth; i++) {
            if (isStopped) return;
            if (inputType == BYTE_TYPE) {
                short[] data = new short[width * height];
                byte[] ba = new byte[data.length];
                try {
                    ra.readFully(ba);
                } catch (IOException e) {
                }
                for (int j = 0; j < data.length; j++) {
                    data[j] = (short) (ba[j] & 0xff);
                }
                datawriter.putSlice(data, i);
            } else if (inputType == SHORT_TYPE) {
                short[] slice = sr.getSlice((i));
                for (int g = 0; g < slice.length; g++) {
                    if (slice[g] != 0 && slice[g] > 255) {
                        removeFiberId(slice[g]);
                    }
                }
                datawriter.putSlice(slice, i);
            }
            setProgress(i);
        }
        setLabel("Found " + totalCount + " regions.");
        sr.close();
        depthSlider.setValue(0);
        updateImage();
        tracker.reset();
        enableMenus();
    }

    /**
	 * Measure a 2D region with origin from a given point
	 * 
	 * @param pt
	 *            A point describing the origin, and id of the region to be
	 *            analysed. A floodfill from this point will be used to defined
	 *            the region.
	 */
    private void measure(Point pt) {
        if (pt.x < 0 || pt.x >= width || pt.y < 0 || pt.y >= height) return;
        byte ff = (byte) 0xff;
        short[] data = getCurrentPixels().clone();
        short[] mask = new short[data.length];
        ImageFunctions.floodFill2D(pt.x, pt.y, width, height, data, mask, ff);
        StringBuffer output = new StringBuffer();
        ArrayList descriptors = getRegionDescriptors();
        short id = volume.getVoxel(pt.x, pt.y, currentSlice());
        output.append("Region id " + id + "\n");
        for (int i = 0; i < descriptors.size(); i++) {
            RegionMask2D maskreg = new RegionMask2D(mask, width, height);
            RegionDescriptor descriptor = (RegionDescriptor) descriptors.get(i);
            if (maskreg == null) {
                System.err.println("Got null regionmask");
            }
            if (descriptor == null) {
                System.err.println("Got null descriptor");
            }
            if (!descriptor.does3D()) output.append(descriptor.getName() + " : " + descriptor.measure(maskreg) + "\n");
        }
        showResults(output, "region");
        for (int i = 0; i < data.length; i++) if (mask[i] != 0) data[i] = 256;
        showImage(data);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }
        updateImage();
    }

    /**
	 * Measure a 2D region with origin from a given point
	 * 
	 * @param pt
	 *            A point describing the origin, and id of the region to be
	 *            analysed. A floodfill from this point will be used to defined
	 *            the region.
	 */
    private void measure3D(Point3D pt) {
        short id = volume.getVoxel(pt.x, pt.y, pt.z);
        ArrayList descriptors = getRegionDescriptors();
        StringBuffer output = new StringBuffer();
        output.append("Region id " + id + "\n");
        RegionMask mask = getRegionMask(id);
        for (int i = 0; i < descriptors.size(); i++) {
            RegionDescriptor descriptor = (RegionDescriptor) descriptors.get(i);
            if (descriptor.does3D()) {
                output.append(descriptor.getName() + " : " + descriptor.measure(mask) + "\n");
            }
            setProgress(progressbar.getValue() + 4);
        }
        showResults(output, "region");
    }

    /**
	 * Returns the regionMask associated with a specific label
	 * 
	 * @param id
	 *            The label used to identify a region
	 * @return A RegionMask containing the object with this id
	 */
    private RegionMask getRegionMask(short id) {
        Region reg = null;
        short[] markImage = getCurrentPixels().clone();
        for (int z = 0; z < depth; z++) {
            setProgress(z / 2);
            short[] slice = getSlice(z);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int i = x + y * width;
                    if (slice[i] == id) {
                        if (z == currentSlice()) markImage[i] = 256;
                        if (reg == null) {
                            BoxBounds bounds = new BoxBounds();
                            reg = new Region(id, bounds);
                        }
                        reg.getBounds().update(x, y, z);
                        reg.size++;
                    }
                }
            }
        }
        reg.getBounds().printSize();
        RegionMask mask = new RegionMask(reg.getId(), volume, reg.getBounds());
        showImage(markImage);
        return mask;
    }

    /**
	 * Replaces a particular slice in the stack with new data
	 * 
	 * @param data
	 *            A short[]-array with the same width-height dimensionas as the
	 *            current stack
	 * @param z
	 *            A depth-index to copy this slice's contents to
	 */
    public void putSlice(short[] data, int z) {
        datawriter.putSlice(data, z);
    }

    /**
	 * Used by the imagePane to update pointer position whenever the mouse is
	 * moved
	 * 
	 * @param p
	 *            New position
	 */
    public void setPointerLabel(Point p) {
        if (volume == null) return;
        pixeltext = " x " + p.x + " y " + p.y;
        short[] curr = getSlice(currentSlice());
        int pixval = 0;
        if (curr != null && p.x >= 0 && p.y >= 0 && p.x < width && p.y < height) pixval = getCurrentPixels()[p.x + p.y * width];
        stackLabel.setPreferredSize(new Dimension(190, 10));
        stackLabel.setBorder(new EmptyBorder(3, 3, 3, 3));
        stackLabel.setText(labeltext + pixeltext + " " + pixval + " ");
    }

    /**
	 * Sets the current progress indicator using the Swing event-execution
	 * thread
	 */
    public void setProgress(final int val) {
        try {
            SwingUtilities.invokeAndWait(new Thread() {

                public void run() {
                    progressbar.setValue(val);
                    progressbar.repaint();
                }

                ;
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
	 * Sets the active progressbar to 100%
	 */
    public void setProgressComplete() {
        SwingUtilities.invokeLater(new Thread() {

            public void run() {
                progressbar.setValue(progressbar.getMaximum());
            }

            ;
        });
    }

    /**
	 * Shows a particular image in the currently active imagePane. (selected
	 * tab)
	 * 
	 * @param data
	 *            A short[] with the same width-height ratio as the current
	 *            stack
	 */
    public void showImage(final short[] data) {
        SwingUtilities.invokeLater(new Thread() {

            public void run() {
                iw.setData(data, width, height);
                iw.repaint();
            }

            ;
        });
    }

    /**
	 * Shows the open-file dialog with current defaults
	 */
    private void showOpenDialog() {
        OpenDialog dialog = new OpenDialog(this);
        dialog.setVisible(true);
        dialog.toFront();
        while (dialog.isVisible()) Thread.yield();
        if (dialog.isCanceled()) return;
        width = dialog.getVolumeWidth();
        height = dialog.getVolumeHeight();
        depth = dialog.getVolumeDepth();
        mainWindow.setVisible(true);
        inputType = dialog.getDataType();
        tracker = dialog.getSelectedTracker();
        createToolbar();
        trackMenu.removeAll();
        tracker.setup();
        fileName = getPreferences().getString("fileName", "data.raw");
        loadStack();
    }

    /**
	 * Adds a string to the Theba console window
	 * 
	 * @param s
	 *            The text to display
	 */
    public void showResults(String s) {
        StringBuffer buf = new StringBuffer(s);
        showResults(buf, "Results");
    }

    /**
	 * Shows the contents of a stringbuffer in the floating result-window
	 * 
	 * @param results
	 *            A stringbuffer containing the message
	 * @param title
	 *            A message that will be displayed in the title-bar of the
	 *            result-window
	 */
    public void showResults(final StringBuffer results, String title) {
        results.append("\n");
        if (resultWindow == null) {
            area = new JTextArea();
            resultScrollPane = new JScrollPane(area);
            resultWindow = new JFrame();
            JButton saveLogButton = new JButton("Save log...");
            saveLogButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    if (chooser == null) chooser = new JFileChooser();
                    int retval = chooser.showSaveDialog(mainWindow);
                    if (retval != JFileChooser.CANCEL_OPTION) {
                        File f = chooser.getSelectedFile();
                        try {
                            PrintStream fo = new PrintStream(f);
                            fo.print(area.getText());
                            fo.close();
                        } catch (FileNotFoundException e1) {
                            JOptionPane.showMessageDialog(mainWindow, "Error, could not save logfile to " + f);
                        }
                    }
                }
            });
            JButton clearLogButton = new JButton("Clear");
            clearLogButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    area.setText("");
                }
            });
            JPanel topPanel = new JPanel(new FlowLayout());
            topPanel.add(saveLogButton);
            topPanel.add(clearLogButton);
            resultWindow.add(topPanel, BorderLayout.SOUTH);
            resultWindow.add(resultScrollPane, BorderLayout.CENTER);
            resultWindow.setLocationRelativeTo(mainWindow);
            resultWindow.setLocation((int) mainWindow.getX(), (int) mainWindow.getY() + mainWindow.getHeight());
            resultWindow.setSize(new Dimension(mainWindow.getWidth(), 300));
        }
        results.append(area.getText());
        area.setText(results.toString());
        resultScrollPane.setViewportView(area);
        resultWindow.setTitle("Results : " + title);
        if (!resultWindow.isVisible()) resultWindow.setVisible(true);
    }

    /**
	 * Update the image contents in the currently selected tab-pane, using the
	 * currently selected slice-index
	 */
    public void updateImage() {
        (new Thread() {

            public void run() {
                getCurrentView().updateData(currentSlice());
            }

            ;
        }).start();
    }

    /**
	 * Sets the slice-number to a given value and updates the image
	 * 
	 * @param z
	 *            The new sliceValue
	 */
    public void updateSlice(final int z) {
        Thread r = new Thread() {

            public void run() {
                if (z < 0 || z > depth) return;
                depthSlider.setValue(z);
            }
        };
        try {
            SwingUtilities.invokeAndWait(r);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        if (!isMinimized()) updateImage();
    }

    /**
	 * Singleton for this class
	 * 
	 * @return The only instance of Theba
	 */
    public static Theba getInstance() {
        if (instance == null) {
            instance = new Theba();
        }
        return instance;
    }

    /**
	 * Main method to launch pixelizer
	 * 
	 * @param args
	 *            Command-line arguments
	 */
    public static void main(String[] args) {
        String antialising = "swing.aatext";
        System.setProperty(antialising, "true");
        Theba.getInstance();
    }

    /**
	 * Generates histogram data for the current stack
	 * 
	 * @author bachewii
	 */
    private final class HistogramAction implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            final Stack volume = getCurrentView().getVolume();
            disableMenus();
            Thread r = new Thread() {

                public void run() {
                    long total = 0;
                    int cnt = 0;
                    for (int i = 0; i < idList.length; i++) {
                        if (!idList[i]) cnt++;
                    }
                    System.out.println("Estimated was " + cnt);
                    long[] histo = new long[MAX_FIBERS + 256];
                    for (int z = 0; z < volume.getDepth(); z++) {
                        setProgress(z);
                        short[] slice = volume.getSlice(z);
                        for (int i = 0; i < slice.length; i++) {
                            histo[slice[i]]++;
                            total++;
                        }
                    }
                    StringBuffer output = new StringBuffer();
                    int diffcnt = 0;
                    for (int i = 0; i < histo.length; i++) {
                        if (histo[i] > 0) output.append(i + " : " + histo[i] + "\n");
                        if (histo[i] != 0) diffcnt++;
                    }
                    output.append("Total = " + total + "\n");
                    output.append(diffcnt + " unique labels");
                    showResults(output, "Histogram");
                    enableMenus();
                }
            };
            r.start();
        }
    }

    /**
	 * The mouse-listener associated with this applications main-window
	 * 
	 * @author bachewii
	 */
    private final class MouseClickListener extends MouseAdapter {

        /**
		 * Function invoked when a user clicks inside the active ImagePane
		 */
        public void mouseClicked(final MouseEvent e) {
            if (!menuEnabled) return;
            disableMenus();
            iw.setEnabled(false);
            Thread r = (new Thread() {

                public void run() {
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        selectedRegion = 0;
                        idSelector.setText("" + getStack().getVoxel(e.getX(), e.getY(), currentSlice()));
                    } else if (seedButton3d.isSelected()) {
                        try {
                            ImageFunctions.floodFill3D26(getStack(), e.getX(), e.getY(), currentSlice(), Short.parseShort(idSelector.getText()));
                            updateImage();
                        } catch (NumberFormatException e) {
                            JOptionPane.showMessageDialog(mainWindow, "The selected value " + idSelector.getText() + " is not a valid region ID");
                        }
                    } else if (deleteButton.isSelected()) {
                        ImageFunctions.floodFill3D26(volume, e.getX(), e.getY(), currentSlice(), (short) 0);
                        updateImage();
                    } else if (measureButton.isSelected()) {
                        measure(e.getPoint());
                    } else if (measureButton3d.isSelected()) {
                        measure3D(new Point3D(e.getX(), e.getY(), currentSlice()));
                    } else if (visualiseButton.isSelected()) {
                        if (JOptionPane.showConfirmDialog(null, "Warning, this feature is highly experimental and can potentially cause loss of data. \nAre you sure you want to continue?", "Warning", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                            Thread r = new Thread() {

                                public void run() {
                                    try {
                                        final ThebaInterface f = new ThebaInterface();
                                        f.loadLib();
                                        final RegionMask mask = getRegionMask(getStack().getVoxel(e.getX(), e.getY(), currentSlice()));
                                        f.triangulize(mask, mask.getWidth(), mask.getHeight(), mask.getDepth());
                                        f.visualize();
                                    } catch (Exception e) {
                                        JOptionPane.showMessageDialog(null, "Unable to execute visualizer. \n " + e.getMessage() + "\nMake sure the native DLL2 library is compiled for your system.");
                                    }
                                }
                            };
                            r.start();
                        }
                    } else {
                        tracker.mouseClicked(new Point3D(e.getX(), e.getY(), currentSlice()));
                    }
                    iw.setEnabled(true);
                    enableMenus();
                }
            });
            r.start();
        }
    }

    /**
	 * Action to perform regioncounting
	 * 
	 * @author bachewii
	 */
    private final class RegCountAction implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            final Stack volume = getCurrentView().getVolume();
            disableMenus();
            Thread r = new Thread() {

                public void run() {
                    long nonblackCount = 0;
                    long whiteCount = 0;
                    long total = 0;
                    for (int z = 0; z < volume.getDepth(); z++) {
                        setProgress(z);
                        short[] slice = volume.getSlice(z);
                        for (int i = 0; i < slice.length; i++) {
                            if (slice[i] == 255) whiteCount++;
                            if (slice[i] != 0) nonblackCount++;
                            total++;
                        }
                    }
                    StringBuffer output = new StringBuffer();
                    output.append("White pixelcount = " + whiteCount + " voxels \n");
                    output.append("Percentage of total = " + (100 * whiteCount / (float) total) + " %\n");
                    output.append("Non-black pixelcount = " + nonblackCount + " voxels \n");
                    output.append("Percentage of total = " + (100 * nonblackCount / (float) total) + " %\n");
                    output.append("White to non-black percentage ratio = " + (100 * whiteCount / (float) nonblackCount) + " %");
                    output.append("Percentage of labelled material = " + (100 * (nonblackCount - whiteCount) / (double) nonblackCount) + " %");
                    showResults(output, "Voxelcount");
                    enableMenus();
                }
            };
            r.start();
        }
    }

    /**
	 * This action is called whenever measure3D is selected and the user selects
	 * a region It generates a new RegionMask by finding its bounding box and
	 * applies all installed 3D descriptors to it
	 * 
	 * @author bachewii
	 * 
	 */
    private final class RegionAnalysisAction implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            final Stack volume = getCurrentView().getVolume();
            disableMenus();
            Thread r = new Thread() {

                public void run() {
                    StringBuffer buf = new StringBuffer("Counting regions...");
                    showResults(buf, "Test");
                    Region[] regArray = new Region[4096];
                    regList = new LinkedList<Region>();
                    for (int z = 0; z < volume.getDepth(); z++) {
                        setProgress(z);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                short val = volume.getVoxelUnchecked(x, y, z);
                                if (val != 255 && val != 0 && val != 7) {
                                    Region reg = regArray[val];
                                    if (reg == null) {
                                        BoxBounds bounds = new BoxBounds();
                                        reg = new Region(val, bounds);
                                        regArray[val] = reg;
                                        regList.add(reg);
                                    }
                                    reg.getBounds().update(x, y, z);
                                    reg.size++;
                                }
                            }
                        }
                    }
                    progressbar.setMaximum(regList.size());
                    buf = new StringBuffer();
                    for (int i = 0; i < regList.size(); i++) {
                        Region reg = (Region) regList.get(i);
                        setProgress(i);
                        if (reg == null) continue;
                        regionCount++;
                        ArrayList descriptors = getRegionDescriptors();
                        buf.append("\nRegion : " + reg.getId() + " \n");
                        buf.append("Bounded size : " + reg.getBounds() + " \n");
                        for (int r = 0; r < descriptors.size(); r++) {
                            RegionDescriptor descriptor = (RegionDescriptor) descriptors.get(r);
                            RegionMask mask = new RegionMask(reg.getId(), volume, reg.getBounds());
                            buf.append(descriptor.getName() + " : " + descriptor.measure(mask) + "\n");
                            showResults(buf, "res");
                        }
                    }
                    buf.append("\nRegioncount :" + regList.size() + " \n");
                    progressbar.setMaximum(depth);
                    enableMenus();
                }
            };
            r.start();
        }
    }

    /**
	 * @return True if any time-consuming tasks should be stopped (this function
	 *         can be added to any time-consuming tasks such as plugins etc)
	 *         When the stop-button is selected they should silently be stopped,
	 *         freeing up the gui for other purposes. Note that there is no
	 *         guarantee that any of these tasks will leave the system in a
	 *         stable state...
	 */
    public boolean isStopped() {
        return isStopped;
    }

    /**
	 * Sets the current status of the stop-value
	 * 
	 * @param isStopped
	 */
    public void setIsStopped(boolean isStopped) {
        this.isStopped = isStopped;
    }

    /**
	 * Sets the progress of the progressbar according to position and maxval
	 * 
	 * @param i
	 *            Current position
	 * @param max
	 *            Maximum
	 */
    public void setProgressbar(int i, int max) {
        progressbar.setMaximum(max);
        progressbar.setValue(i);
    }

    private boolean[] idList;

    private int totalCount;

    /**
	 * Release up a label This method should be called whenever a region is
	 * entirely removed from the stack, such that the label is made available
	 * for other purposes
	 * 
	 * @param val
	 *            The label that is now free to use for otehr purposes
	 */
    public void releaseFiberId(short val) {
        idList[val] = true;
    }

    /**
	 * This function removes a fiber-id from the list
	 * 
	 * @param val
	 */
    private void removeFiberId(short val) {
        if (val - ID_OFFSET > idList.length) {
            System.err.print("Attempted to remove id > MAX_REG_COUNT " + val);
            return;
        }
        if (idList[val - ID_OFFSET] == true) {
            idList[val - ID_OFFSET] = false;
            totalCount++;
        }
    }

    /**
	 * Initializes the label-list
	 */
    private void initIdList() {
        idList = new boolean[MAX_FIBERS];
        for (int i = 0; i < MAX_FIBERS; i++) {
            idList[i] = true;
        }
    }

    /**
	 * Gets a LinkedList containing a Region object for each labeled region in
	 * the image
	 * 
	 * @return A LinkedList of Region-objects, that bound each labeled region
	 */
    private LinkedList<Region> getRegions() {
        LinkedList<Region> regList = new LinkedList<Region>();
        Region[] regArray = new Region[4096];
        for (int z = 0; z < volume.getDepth(); z++) {
            setProgress(z);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    short val = volume.getVoxelUnchecked(x, y, z);
                    if (val != 255 && val != 0 && val != 7) {
                        Region reg = regArray[val];
                        if (reg == null) {
                            BoxBounds bounds = new BoxBounds();
                            reg = new Region(val, bounds);
                            regArray[val] = reg;
                            regList.add(reg);
                        }
                        reg.getBounds().update(x, y, z);
                        reg.size++;
                    }
                }
            }
        }
        return regList;
    }

    /**
	 * Returns a free fiberlabel from The list is allways updated when the
	 * volume is reloaded.
	 * 
	 * @return A new short that has not yet been used in the data-stack
	 */
    public short getNewFiberId() {
        for (short i = 0; i < idList.length; i++) {
            if (idList[i]) {
                idList[i] = false;
                return (short) (i + ID_OFFSET);
            }
        }
        System.err.print("OUT OF ID's!");
        return -1;
    }

    /**
	 * Sets the label-text for the system-toolbar
	 * 
	 * @todo Consider automatically resetting this label
	 * @param string
	 */
    public void setLabel(String string) {
        statusLabel.setText(string);
    }
}
