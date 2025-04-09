package net.buddat.wplanner;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.prefs.Preferences;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import net.buddat.wplanner.map.Map;
import net.buddat.wplanner.map.MapV2;
import net.buddat.wplanner.map.Tile;
import net.buddat.wplanner.map.layer.tile.FenceTile;
import net.buddat.wplanner.map.layer.tile.LabelTile;
import net.buddat.wplanner.map.layer.tile.ObjectTile;
import net.buddat.wplanner.map.layer.tile.OverlayTile;

public class MapEditor extends JFrame implements KeyListener, ActionListener, ChangeListener, MouseWheelListener {

    private static final long serialVersionUID = -111657081934826004L;

    private static final int DEFAULT_WINDOW_X = 800, DEFAULT_WINDOW_Y = 600;

    private boolean shiftDown = false, ctrlDown = false;

    public boolean hugeMapFillWarning = false;

    public JPanel terrainPanel, objectPanel, fencePanel, miscPanel;

    private GraphicPanel graphicPanel;

    public int graphicPanelWidth, graphicPanelHeight;

    public JPanel editorPanel;

    private JScrollPane terrainScroll, objectsScroll, fencesScroll, miscScroll, graphicScroll;

    public JTabbedPane tabbedEditor;

    private JPanel editorTools;

    public ButtonGroup rbnTools;

    public JRadioButton normalTool, pickerTool, fillTool;

    public JButton eraserBtn, zoomInBtn, zoomOutBtn, layerUpBtn, layerDownBtn;

    private JMenuItem toolsNormalItm, toolsPickerItm, toolsFillItm, toolsEraserItm;

    private JMenu menuTools;

    private JMenuBar menuBar;

    private JMenu menuFile, menuEdit, menuView, menuHelp;

    private JMenuItem fileSave, fileSaveAs, fileLoad, fileSaveImage, fileExit;

    private JMenuItem editResize;

    private JMenuItem viewZoomIn, viewZoomOut;

    private JMenuItem helpFile, helpAbout;

    public JCheckBoxMenuItem viewTerrain, viewObjects, viewFences, viewOverlay, viewLabels, viewGrid, viewSlopes;

    public JRadioButtonMenuItem viewCave, viewAbove;

    public ButtonGroup rbnTerrain, rbnObjects, rbnFences, rbnMisc, rbnView;

    public HashMap<String, JRadioButton> terrainBtnList = new HashMap<String, JRadioButton>();

    public HashMap<String, JPanel> terrainTileList = new HashMap<String, JPanel>();

    public HashMap<String, Image> terrainImgList = new HashMap<String, Image>();

    public ArrayList<String> terrainKeyList;

    public HashMap<String, JRadioButton> objectBtnList = new HashMap<String, JRadioButton>();

    public HashMap<String, JPanel> objectTileList = new HashMap<String, JPanel>();

    public HashMap<String, Image[]> objectImgList = new HashMap<String, Image[]>();

    public ArrayList<String> objectKeyList;

    public HashMap<String, JRadioButton> fenceBtnList = new HashMap<String, JRadioButton>();

    public HashMap<String, JPanel> fenceTileList = new HashMap<String, JPanel>();

    public HashMap<String, Image> fenceImgList = new HashMap<String, Image>();

    public ArrayList<String> fenceKeyList;

    public JPanel miscOptions, miscSettings, miscPreview;

    public JRadioButton miscOverlay, miscText;

    public JButton miscOverlayColorBtn, miscBlankTerrainColorBtn, miscTextColorBtn;

    public JSlider miscOpacity;

    public JLabel miscTextPreview, miscColorPreview;

    public Color miscOverlayColor, miscTextColor;

    public MapEditor() {
    }

    public MapEditor(String mapName, int sizeX, int sizeY) {
        super(WPlanner.TITLE + " | ");
        if (mapName.equals("")) mapName = "DefaultMap" + (int) (Math.random() * 50000);
        setTitle(getTitle() + mapName);
        init();
        graphicPanel = new GraphicPanel(mapName, sizeX, sizeY, this);
        graphicScroll = new JScrollPane(graphicPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        graphicScroll.getVerticalScrollBar().setUnitIncrement(graphicScroll.getVerticalScrollBar().getUnitIncrement() * 10);
        graphicScroll.addMouseWheelListener(this);
        add(graphicScroll, BorderLayout.CENTER);
        setVisible(true);
    }

    public MapEditor(File selectedFile) {
        super(WPlanner.TITLE + " | " + selectedFile.getName());
        init();
        loadMap(selectedFile);
        graphicScroll = new JScrollPane(graphicPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        graphicScroll.getVerticalScrollBar().setUnitIncrement(graphicScroll.getVerticalScrollBar().getUnitIncrement() * 10);
        graphicScroll.addMouseWheelListener(this);
        add(graphicScroll, BorderLayout.CENTER);
        setVisible(true);
    }

    private void init() {
        setSize(DEFAULT_WINDOW_X, DEFAULT_WINDOW_Y);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setDefaultLookAndFeelDecorated(false);
        JDialog loadScreen = new JDialog(this, "Loading, please wait...");
        loadScreen.setSize(250, 0);
        loadScreen.setResizable(false);
        loadScreen.setLocationRelativeTo(null);
        loadScreen.setVisible(true);
        loadResources();
        menuBar = new JMenuBar();
        menuFile = new JMenu("File");
        fileSave = new JMenuItem("Save");
        fileSave.addActionListener(this);
        fileSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        fileSaveAs = new JMenuItem("Save As...");
        fileSaveAs.addActionListener(this);
        fileLoad = new JMenuItem("Load...");
        fileLoad.addActionListener(this);
        fileSaveImage = new JMenuItem("Save to Image");
        fileSaveImage.addActionListener(this);
        fileExit = new JMenuItem("Exit");
        fileExit.addActionListener(this);
        menuFile.add(fileSave);
        menuFile.add(fileSaveAs);
        menuFile.add(fileLoad);
        menuFile.addSeparator();
        menuFile.add(fileSaveImage);
        menuFile.addSeparator();
        menuFile.add(fileExit);
        menuEdit = new JMenu("Edit");
        editResize = new JMenuItem("Resize Map");
        editResize.addActionListener(this);
        menuEdit.add(editResize);
        menuView = new JMenu("View");
        viewZoomIn = new JMenuItem("Zoom In");
        viewZoomIn.setActionCommand("zoom-in");
        viewZoomIn.addActionListener(this);
        viewZoomIn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, ActionEvent.CTRL_MASK));
        viewZoomOut = new JMenuItem("Zoom Out");
        viewZoomOut.setActionCommand("zoom-out");
        viewZoomOut.addActionListener(this);
        viewZoomOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, ActionEvent.CTRL_MASK));
        rbnView = new ButtonGroup();
        viewCave = new JRadioButtonMenuItem("Cave Layer");
        viewCave.setActionCommand("layer-cave");
        viewCave.addActionListener(this);
        viewCave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.CTRL_MASK));
        rbnView.add(viewCave);
        viewAbove = new JRadioButtonMenuItem("Top Layer");
        viewAbove.setActionCommand("layer-top");
        viewAbove.setSelected(true);
        viewAbove.addActionListener(this);
        viewAbove.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
        rbnView.add(viewAbove);
        viewTerrain = new JCheckBoxMenuItem("Show Terrain");
        viewTerrain.setSelected(true);
        viewTerrain.addActionListener(this);
        viewObjects = new JCheckBoxMenuItem("Show Objects");
        viewObjects.addActionListener(this);
        viewObjects.setSelected(true);
        viewFences = new JCheckBoxMenuItem("Show Fences");
        viewFences.addActionListener(this);
        viewFences.setSelected(true);
        viewOverlay = new JCheckBoxMenuItem("Show Overlay");
        viewOverlay.addActionListener(this);
        viewOverlay.setSelected(true);
        viewLabels = new JCheckBoxMenuItem("Show Labels");
        viewLabels.addActionListener(this);
        viewLabels.setSelected(true);
        viewGrid = new JCheckBoxMenuItem("Show Grid");
        viewGrid.setSelected(true);
        viewGrid.addActionListener(this);
        viewSlopes = new JCheckBoxMenuItem("Show Slopes");
        viewSlopes.addActionListener(this);
        menuView.add(viewZoomIn);
        menuView.add(viewZoomOut);
        menuView.addSeparator();
        menuView.add(viewCave);
        menuView.add(viewAbove);
        menuView.addSeparator();
        menuView.add(viewTerrain);
        menuView.add(viewObjects);
        menuView.add(viewFences);
        menuView.add(viewOverlay);
        menuView.add(viewLabels);
        menuView.addSeparator();
        menuView.add(viewGrid);
        menuView.add(viewSlopes);
        menuTools = new JMenu("Tools");
        toolsNormalItm = new JMenuItem("Normal");
        toolsNormalItm.addActionListener(this);
        toolsNormalItm.setActionCommand("tools-normal");
        toolsNormalItm.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
        toolsFillItm = new JMenuItem("Fill");
        toolsFillItm.addActionListener(this);
        toolsFillItm.setActionCommand("tools-fill");
        toolsFillItm.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
        toolsPickerItm = new JMenuItem("Picker");
        toolsPickerItm.addActionListener(this);
        toolsPickerItm.setActionCommand("tools-picker");
        toolsPickerItm.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
        toolsEraserItm = new JMenuItem("Eraser");
        toolsEraserItm.setActionCommand("tools-eraser");
        toolsEraserItm.addActionListener(this);
        toolsEraserItm.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
        menuTools.add(toolsNormalItm);
        menuTools.add(toolsFillItm);
        menuTools.add(toolsPickerItm);
        menuTools.add(toolsEraserItm);
        menuHelp = new JMenu("Help");
        helpFile = new JMenuItem("Open Help File");
        helpFile.addActionListener(this);
        helpFile.setAccelerator(KeyStroke.getKeyStroke("F1"));
        helpAbout = new JMenuItem("About");
        helpAbout.addActionListener(this);
        menuHelp.add(helpFile);
        menuHelp.addSeparator();
        menuHelp.add(helpAbout);
        menuBar.add(menuFile);
        menuBar.add(menuEdit);
        menuBar.add(menuView);
        menuBar.add(menuTools);
        menuBar.add(menuHelp);
        setJMenuBar(menuBar);
        editorPanel = new JPanel();
        editorPanel.setLayout(new BoxLayout(editorPanel, BoxLayout.Y_AXIS));
        editorTools = new JPanel();
        editorTools.setLayout(new BoxLayout(editorTools, BoxLayout.X_AXIS));
        rbnTools = new ButtonGroup();
        try {
            normalTool = new JRadioButton(new ImageIcon(ImageIO.read(new File(WPlanner.installDir + "data/misc/normal.png"))));
            normalTool.setToolTipText("Normal Tool (Ctrl+Q)");
            normalTool.setActionCommand("tools-normal");
            normalTool.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLUE));
            normalTool.addActionListener(this);
            normalTool.addKeyListener(this);
            normalTool.setSelected(true);
            normalTool.setBorderPainted(true);
            rbnTools.add(normalTool);
            fillTool = new JRadioButton(new ImageIcon(ImageIO.read(new File(WPlanner.installDir + "data/misc/fill.png"))));
            fillTool.setToolTipText("Fill Tool (Ctrl+W)");
            fillTool.setActionCommand("tools-fill");
            fillTool.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLUE));
            fillTool.addActionListener(this);
            fillTool.addKeyListener(this);
            rbnTools.add(fillTool);
            pickerTool = new JRadioButton(new ImageIcon(ImageIO.read(new File(WPlanner.installDir + "data/misc/picker.png"))));
            pickerTool.setToolTipText("Picker Tool(Ctrl+E)");
            pickerTool.setActionCommand("tools-picker");
            pickerTool.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLUE));
            pickerTool.addActionListener(this);
            pickerTool.addKeyListener(this);
            rbnTools.add(pickerTool);
            eraserBtn = new JButton(new ImageIcon(ImageIO.read(new File(WPlanner.installDir + "data/misc/eraser.png"))));
            eraserBtn.setToolTipText("Eraser (Ctrl+R)");
            eraserBtn.setActionCommand("tools-eraser");
            eraserBtn.addActionListener(this);
            eraserBtn.addKeyListener(this);
            eraserBtn.setMargin(new Insets(1, 1, 1, 1));
            zoomInBtn = new JButton(new ImageIcon(ImageIO.read(new File(WPlanner.installDir + "data/misc/zoomin.png"))));
            zoomInBtn.setToolTipText("Zoom In (Ctrl+=)");
            zoomInBtn.setActionCommand("zoom-in");
            zoomInBtn.addActionListener(this);
            zoomInBtn.addKeyListener(this);
            zoomInBtn.setMargin(new Insets(1, 1, 1, 1));
            zoomOutBtn = new JButton(new ImageIcon(ImageIO.read(new File(WPlanner.installDir + "data/misc/zoomout.png"))));
            zoomOutBtn.setToolTipText("Zoom Out (Ctrl+-)");
            zoomOutBtn.setActionCommand("zoom-out");
            zoomOutBtn.addActionListener(this);
            zoomOutBtn.addKeyListener(this);
            zoomOutBtn.setMargin(new Insets(1, 1, 1, 1));
            layerUpBtn = new JButton(new ImageIcon(ImageIO.read(new File(WPlanner.installDir + "data/misc/uplayer.png"))));
            layerUpBtn.setToolTipText("Top Layer (Ctrl+T)");
            layerUpBtn.setActionCommand("layer-top");
            layerUpBtn.addActionListener(this);
            layerUpBtn.addKeyListener(this);
            layerUpBtn.setMargin(new Insets(1, 1, 1, 1));
            layerDownBtn = new JButton(new ImageIcon(ImageIO.read(new File(WPlanner.installDir + "data/misc/downlayer.png"))));
            layerDownBtn.setToolTipText("Cave Layer (Ctrl+G)");
            layerDownBtn.setActionCommand("layer-cave");
            layerDownBtn.addActionListener(this);
            layerDownBtn.addKeyListener(this);
            layerDownBtn.setMargin(new Insets(1, 1, 1, 1));
            JSeparator js = new JSeparator(SwingConstants.VERTICAL);
            js.setMaximumSize(new Dimension((int) 8, (int) js.getMaximumSize().getHeight()));
            JSeparator js2 = new JSeparator(SwingConstants.VERTICAL);
            js2.setMaximumSize(new Dimension((int) 8, (int) js2.getMaximumSize().getHeight()));
            JSeparator js3 = new JSeparator(SwingConstants.VERTICAL);
            js3.setMaximumSize(new Dimension((int) 8, (int) js3.getMaximumSize().getHeight()));
            editorTools.add(normalTool);
            editorTools.add(fillTool);
            editorTools.add(pickerTool);
            editorTools.add(js);
            editorTools.add(eraserBtn);
            editorTools.add(js2);
            editorTools.add(zoomInBtn);
            editorTools.add(zoomOutBtn);
            editorTools.add(js3);
            editorTools.add(layerUpBtn);
            editorTools.add(layerDownBtn);
        } catch (IOException e) {
            e.printStackTrace();
        }
        terrainPanel = new JPanel();
        terrainPanel.addKeyListener(this);
        terrainPanel.setLayout(new GridLayout(0, 4));
        terrainScroll = new JScrollPane(terrainPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        terrainScroll.addKeyListener(this);
        terrainScroll.getVerticalScrollBar().setUnitIncrement(terrainScroll.getVerticalScrollBar().getUnitIncrement() * 10);
        rbnTerrain = new ButtonGroup();
        for (String s : terrainKeyList) {
            JRadioButton jrb = new JRadioButton(new ImageIcon(terrainImgList.get(s).getScaledInstance(60, 60, Image.SCALE_DEFAULT)));
            jrb.setToolTipText(s.substring(0, s.indexOf(".")));
            jrb.setActionCommand(s);
            jrb.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.RED));
            jrb.addActionListener(this);
            jrb.addKeyListener(this);
            terrainBtnList.put(s, jrb);
            rbnTerrain.add(jrb);
            String lblString = s.substring(0, s.indexOf("."));
            JLabel lbl = new JLabel(lblString.substring(0, (lblString.length() > 9 ? 9 : lblString.length())));
            JPanel jpnl = new JPanel();
            jpnl.setLayout(new BoxLayout(jpnl, BoxLayout.Y_AXIS));
            jpnl.add(jrb);
            jpnl.add(lbl);
            terrainTileList.put(s, jpnl);
            terrainPanel.add(jpnl);
        }
        objectPanel = new JPanel();
        objectPanel.addKeyListener(this);
        objectPanel.setLayout(new GridLayout(0, 4));
        objectsScroll = new JScrollPane(objectPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        objectsScroll.addKeyListener(this);
        objectsScroll.getVerticalScrollBar().setUnitIncrement(objectsScroll.getVerticalScrollBar().getUnitIncrement() * 10);
        rbnObjects = new ButtonGroup();
        for (String s : objectKeyList) {
            JRadioButton jrb = new JRadioButton(new ImageIcon(objectImgList.get(s)[0].getScaledInstance(60, 60, Image.SCALE_DEFAULT)));
            jrb.setToolTipText(s.substring(0, s.indexOf(".")));
            jrb.setActionCommand(s);
            jrb.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.RED));
            jrb.addActionListener(this);
            jrb.addKeyListener(this);
            objectBtnList.put(s, jrb);
            rbnObjects.add(jrb);
            String lblString = s.substring(0, s.indexOf("."));
            JLabel lbl = new JLabel(lblString.substring(0, (lblString.length() > 9 ? 9 : lblString.length())));
            JPanel jpnl = new JPanel();
            jpnl.setLayout(new BoxLayout(jpnl, BoxLayout.Y_AXIS));
            jpnl.add(jrb);
            jpnl.add(lbl);
            objectTileList.put(s, jpnl);
            objectPanel.add(jpnl);
        }
        fencePanel = new JPanel();
        fencePanel.addKeyListener(this);
        fencePanel.setLayout(new GridLayout(0, 4));
        fencesScroll = new JScrollPane(fencePanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        fencesScroll.addKeyListener(this);
        fencesScroll.getVerticalScrollBar().setUnitIncrement(fencesScroll.getVerticalScrollBar().getUnitIncrement() * 10);
        rbnFences = new ButtonGroup();
        for (String s : fenceKeyList) if (!s.endsWith("_1")) {
            JRadioButton jrb = new JRadioButton(new ImageIcon(fenceImgList.get(s).getScaledInstance(60, 60, Image.SCALE_DEFAULT)));
            jrb.setToolTipText(s.substring(0, s.indexOf(".")));
            jrb.setActionCommand(s);
            jrb.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.RED));
            jrb.addActionListener(this);
            jrb.addKeyListener(this);
            fenceBtnList.put(s, jrb);
            rbnFences.add(jrb);
            String lblString = s.substring(0, s.indexOf("."));
            JLabel lbl = new JLabel(lblString.substring(0, (lblString.length() > 9 ? 9 : lblString.length())));
            JPanel jpnl = new JPanel();
            jpnl.setLayout(new BoxLayout(jpnl, BoxLayout.Y_AXIS));
            jpnl.add(jrb);
            jpnl.add(lbl);
            fenceTileList.put(s, jpnl);
            fencePanel.add(jpnl);
        }
        miscPanel = new JPanel();
        miscPanel.addKeyListener(this);
        miscPanel.setLayout(new BoxLayout(miscPanel, BoxLayout.Y_AXIS));
        miscScroll = new JScrollPane(miscPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        miscScroll.addKeyListener(this);
        miscScroll.getVerticalScrollBar().setUnitIncrement(miscScroll.getVerticalScrollBar().getUnitIncrement() * 10);
        miscOptions = new JPanel();
        miscOptions.addKeyListener(this);
        miscOptions.setLayout(new BoxLayout(miscOptions, BoxLayout.X_AXIS));
        rbnMisc = new ButtonGroup();
        miscOverlay = new JRadioButton("Overlay");
        miscOverlay.setActionCommand("Overlay");
        miscOverlay.addActionListener(this);
        miscOverlay.addKeyListener(this);
        miscOverlay.setSelected(true);
        miscText = new JRadioButton("Text");
        miscText.setActionCommand("Text");
        miscText.addActionListener(this);
        miscText.addKeyListener(this);
        rbnMisc.add(miscOverlay);
        rbnMisc.add(miscText);
        miscOptions.add(miscOverlay, BorderLayout.NORTH);
        miscOptions.add(miscText, BorderLayout.WEST);
        miscSettings = new JPanel();
        miscSettings.addKeyListener(this);
        miscSettings.setLayout(new GridLayout(0, 1));
        miscOverlayColorBtn = new JButton("Set Overlay Colour");
        miscOverlayColorBtn.addActionListener(this);
        miscOverlayColorBtn.addKeyListener(this);
        miscOpacity = new JSlider(0, 255, 127);
        miscOpacity.setToolTipText("Opacity");
        miscOpacity.addChangeListener(this);
        miscTextColorBtn = new JButton("Set Label Colour");
        miscTextColorBtn.addActionListener(this);
        miscTextColorBtn.addKeyListener(this);
        miscOverlayColor = Color.BLACK;
        miscTextColor = Color.BLACK;
        miscBlankTerrainColorBtn = new JButton("Set Blank Terrain Color");
        miscBlankTerrainColorBtn.addActionListener(this);
        miscBlankTerrainColorBtn.addKeyListener(this);
        miscSettings.add(miscBlankTerrainColorBtn);
        miscSettings.add(miscTextColorBtn);
        miscSettings.add(miscOverlayColorBtn);
        miscSettings.add(miscOpacity);
        miscPreview = new JPanel();
        miscPreview.addKeyListener(this);
        miscPreview.setLayout(new BorderLayout());
        miscTextPreview = new JLabel("Preview Text");
        miscColorPreview = new JLabel("Preview Overlay Colour");
        miscColorPreview.setPreferredSize(new Dimension(miscColorPreview.getWidth(), 60));
        miscPreview.add(miscTextPreview, BorderLayout.NORTH);
        miscPreview.add(miscColorPreview, BorderLayout.CENTER);
        miscPanel.add(miscOptions);
        miscPanel.add(miscSettings);
        miscPanel.add(miscPreview);
        updatePreview();
        tabbedEditor = new JTabbedPane();
        tabbedEditor.addKeyListener(this);
        tabbedEditor.addChangeListener(this);
        tabbedEditor.addTab("Terrain", terrainScroll);
        tabbedEditor.addTab("Objects", objectsScroll);
        tabbedEditor.addTab("Fences", fencesScroll);
        tabbedEditor.addTab("Misc", miscScroll);
        editorPanel.add(editorTools);
        editorPanel.add(tabbedEditor);
        add(editorPanel, BorderLayout.LINE_START);
        loadScreen.dispose();
    }

    public void loadResources() {
        System.out.println("Locating Resources...");
        String wurmDir = Preferences.userRoot().node(WPlanner.WURM_REGISTRY).get("wurm_dir", "FAIL");
        if (wurmDir.equals("FAIL")) {
            JOptionPane.showMessageDialog(this, "Unable to continue. Could not get Wurm install directory. (Error code 103)");
            System.exit(1);
        } else {
            try {
                JarFile jf = new JarFile(wurmDir + "/packs/graphics.jar");
                Enumeration<JarEntry> e = jf.entries();
                JarEntry je;
                BufferedImage bi = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
                Graphics g = bi.getGraphics();
                g.setColor(this.getBackground());
                g.fillRect(0, 0, bi.getWidth(), bi.getHeight());
                terrainImgList.put("_blank.jpg", bi.getScaledInstance(128, 128, Image.SCALE_SMOOTH));
                while (e.hasMoreElements()) if ((je = e.nextElement()) != null) if (!je.isDirectory() && je.getName().contains("texture/terrain/")) terrainImgList.put(je.getName().substring(je.getName().lastIndexOf("/") + 1), ImageIO.read(jf.getInputStream(je)).getScaledInstance(128, 128, Image.SCALE_SMOOTH));
                jf.close();
                File objectsFolder = new File(WPlanner.objectsFolder);
                File[] objects = objectsFolder.listFiles();
                BufferedImage objectBlank = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
                Graphics objectBlankGfx = objectBlank.getGraphics();
                Image[] objectBlankImg = new Image[1];
                objectBlankGfx.setColor(this.getBackground());
                objectBlankGfx.fillRect(0, 0, objectBlank.getWidth(), objectBlank.getHeight());
                objectBlankImg[0] = objectBlank;
                objectImgList.put("_blank.jpg", objectBlankImg);
                for (File f : objects) if (f != null) if (f.getPath().endsWith(".png") || f.getPath().endsWith(".jpg") || f.getPath().endsWith(".jpeg")) {
                    Image[] rotatedImages = new Image[8];
                    for (int i = 0; i < 8; i++) {
                        Image img = ImageIO.read(f);
                        rotatedImages[i] = rotateImage(img, 45 * i);
                    }
                    objectImgList.put(f.getName().substring(f.getName().lastIndexOf("/") + 1), rotatedImages);
                }
                File fencesFolder = new File(WPlanner.fencesFolder);
                File[] fences = fencesFolder.listFiles();
                BufferedImage fenceBlank = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
                Graphics fenceBlankGfx = fenceBlank.getGraphics();
                fenceBlankGfx.setColor(this.getBackground());
                fenceBlankGfx.fillRect(0, 0, fenceBlank.getWidth(), fenceBlank.getHeight());
                Image fenceBlankImg = fenceBlank;
                fenceImgList.put("_blank.jpg", fenceBlankImg);
                fenceImgList.put("_blank.jpg_1", fenceBlankImg);
                for (File f : fences) if (f != null) if (f.getPath().endsWith(".png") || f.getPath().endsWith(".jpg") || f.getPath().endsWith(".jpeg")) {
                    fenceImgList.put(f.getName().substring(f.getName().lastIndexOf("/") + 1), rotateImage(ImageIO.read(f), 0));
                    fenceImgList.put(f.getName().substring(f.getName().lastIndexOf("/") + 1) + "_1", rotateImage(ImageIO.read(f), 90));
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            Set<String> st = terrainImgList.keySet();
            terrainKeyList = new ArrayList<String>(st);
            Collections.sort(terrainKeyList);
            Set<String> so = objectImgList.keySet();
            objectKeyList = new ArrayList<String>(so);
            Collections.sort(objectKeyList);
            Set<String> sf = fenceImgList.keySet();
            fenceKeyList = new ArrayList<String>(sf);
            Collections.sort(fenceKeyList);
        }
        System.out.println("Resources loaded successfully!");
    }

    private Image rotateImage(Image image, int degrees) {
        int w = image.getWidth(null);
        int h = image.getHeight(null);
        AffineTransform at = new AffineTransform();
        at.rotate(Math.toRadians(degrees), w / 2, h / 2);
        BufferedImage buffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) buffer.getGraphics();
        g.drawImage(image, at, null);
        return buffer;
    }

    public void loadMap(File mapFile) {
        if (graphicPanel == null) graphicPanel = new GraphicPanel(this);
        if (mapFile.getAbsolutePath().endsWith("wp2")) {
            graphicPanel.setMap(new MapV2());
            graphicPanel.getMap().loadMap(mapFile.getAbsolutePath());
            setTitle(WPlanner.TITLE + " | " + graphicPanel.getMap().getMapName());
            graphicPanel.setZoomLevel(graphicPanel.getZoomLevel());
            graphicPanel.repaint();
        } else if (mapFile.getAbsolutePath().endsWith("wpm")) {
            MapV2 newMap = convertMap(mapFile);
            if (newMap != null) {
                graphicPanel.setMap(newMap);
                graphicPanel.getMap().setMapName(mapFile.getName());
                setTitle(WPlanner.TITLE + " | " + newMap.getMapName());
                graphicPanel.setZoomLevel(graphicPanel.getZoomLevel());
                graphicPanel.repaint();
            } else {
                JOptionPane.showMessageDialog(this, "Unable to load map: " + mapFile.getAbsolutePath());
            }
        }
    }

    private MapV2 convertMap(File mapFile) {
        try {
            FileInputStream fis = new FileInputStream(mapFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Map map = (Map) ois.readObject();
            int mapWidth = map.getTilesX();
            int mapHeight = map.getTilesY();
            MapV2 newMap = new MapV2(map.getMapName(), mapWidth, mapHeight);
            for (int i = 0; i < mapWidth; i++) {
                for (int j = 0; j < mapHeight; j++) {
                    Tile t = null;
                    for (int k = 0; k < 2; k++) {
                        boolean caveLayer = k != 0;
                        if (map.getTile(caveLayer ? "CaveTerrain" : "Terrain", i, j).getTileType() > 0) {
                            if (t == null) t = new Tile(i, j);
                            t.setTerrainType(caveLayer, (byte) map.getTile(caveLayer ? "CaveTerrain" : "Terrain", i, j).getTileType());
                        }
                        if (map.getTile(caveLayer ? "CaveTerrain" : "Terrain", i, j).getTileHeight() > 0) {
                            if (t == null) t = new Tile(i, j);
                            t.setHeight(caveLayer, map.getTile(caveLayer ? "CaveTerrain" : "Terrain", i, j).getTileHeight());
                        }
                        for (int i1 = 0; i1 < 3; i1++) for (int j1 = 0; j1 < 3; j1++) {
                            if (((ObjectTile) (map.getTile(caveLayer ? "CaveObjects" : "Objects", i, j))).getObjectType(i1, j1) > 0) {
                                if (t == null) t = new Tile(i, j);
                                t.setObjectType(caveLayer, (j1 * 3) + i1, ((ObjectTile) (map.getTile(caveLayer ? "CaveObjects" : "Objects", i, j))).getObjectType(i1, j1));
                                t.setObjectRotation(caveLayer, (j1 * 3) + i1, ((ObjectTile) (map.getTile(caveLayer ? "CaveObjects" : "Objects", i, j))).getObjectFace(i1, j1));
                            }
                        }
                        if (((OverlayTile) (map.getTile(caveLayer ? "CaveOverlay" : "Overlay", i, j))).getTileColor() != OverlayTile.DEFAULT_COLOR) {
                            if (t == null) t = new Tile(i, j);
                            t.setOverlayColor(caveLayer, ((OverlayTile) (map.getTile(caveLayer ? "CaveOverlay" : "Overlay", i, j))).getTileColor());
                        }
                        if (((LabelTile) (map.getTile(caveLayer ? "CaveLabels" : "Labels", i, j))).getLabel() != null) {
                            if (!((LabelTile) (map.getTile(caveLayer ? "CaveLabels" : "Labels", i, j))).getLabel().equals("")) {
                                if (t == null) t = new Tile(i, j);
                                t.setLabel(caveLayer, ((LabelTile) (map.getTile(caveLayer ? "CaveLabels" : "Labels", i, j))).getLabel());
                                t.setLabelColor(caveLayer, ((LabelTile) (map.getTile(caveLayer ? "CaveLabels" : "Labels", i, j))).getTileColor());
                            }
                        }
                    }
                    if (((FenceTile) (map.getTile("Fences", i, j))).getFence(FenceTile.FENCE_TOP) > 0) {
                        if (t == null) t = new Tile(i, j);
                        t.setFenceType(false, 1, (byte) ((FenceTile) (map.getTile("Fences", i, j))).getFence(FenceTile.FENCE_TOP));
                    }
                    if (((FenceTile) (map.getTile("Fences", i, j))).getFence(FenceTile.FENCE_LEFT) > 0) {
                        if (t == null) t = new Tile(i, j);
                        t.setFenceType(false, 0, (byte) ((FenceTile) (map.getTile("Fences", i, j))).getFence(FenceTile.FENCE_LEFT));
                    }
                    if (t != null) newMap.addTile(i, j, t);
                }
            }
            return newMap;
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updatePreview() {
        miscOverlayColor = new Color(miscOverlayColor.getRed(), miscOverlayColor.getGreen(), miscOverlayColor.getBlue(), miscOpacity.getValue());
        miscTextPreview.setForeground(miscTextColor);
        miscColorPreview.setBackground(miscOverlayColor);
        miscColorPreview.setOpaque(true);
        this.repaint();
    }

    public void zoomIn() {
        if (graphicPanel.getZoomLevel() <= GraphicPanel.ZOOM_MAX - GraphicPanel.ZOOM_STEP) {
            graphicPanel.setZoomLevel(graphicPanel.getZoomLevel() + GraphicPanel.ZOOM_STEP);
            graphicPanel.repaint();
        }
    }

    public void zoomOut() {
        if (graphicPanel.getZoomLevel() >= GraphicPanel.ZOOM_MIN + GraphicPanel.ZOOM_STEP) {
            graphicPanel.setZoomLevel(graphicPanel.getZoomLevel() - GraphicPanel.ZOOM_STEP);
            graphicPanel.repaint();
        }
    }

    public void launchHelpFile() {
        try {
            File pdfFile = new File(WPlanner.installDir + "WPlanner Help File.pdf");
            if (pdfFile.exists()) {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(pdfFile);
                } else {
                    JOptionPane.showMessageDialog(this, "AWT Desktop not supported - Unable to open WPlanner Help File.pdf");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Unable to find WPlanner Help File.pdf");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setShiftDown(boolean shift) {
        shiftDown = shift;
    }

    public boolean isShiftDown() {
        return shiftDown;
    }

    public void setCtrlDown(boolean ctrl) {
        ctrlDown = ctrl;
    }

    public boolean isCtrlDown() {
        return ctrlDown;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SHIFT) setShiftDown(true); else if (e.getKeyCode() == KeyEvent.VK_CONTROL) setCtrlDown(true);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SHIFT) setShiftDown(false); else if (e.getKeyCode() == KeyEvent.VK_CONTROL) setCtrlDown(false);
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().startsWith("tools")) {
            if (e.getActionCommand().endsWith("normal")) {
                normalTool.setSelected(true);
                fillTool.setSelected(false);
                pickerTool.setSelected(false);
                normalTool.setBorderPainted(true);
                fillTool.setBorderPainted(false);
                pickerTool.setBorderPainted(false);
            } else if (e.getActionCommand().endsWith("fill")) {
                normalTool.setSelected(false);
                fillTool.setSelected(true);
                pickerTool.setSelected(false);
                normalTool.setBorderPainted(false);
                fillTool.setBorderPainted(true);
                pickerTool.setBorderPainted(false);
            } else if (e.getActionCommand().endsWith("picker")) {
                normalTool.setSelected(false);
                fillTool.setSelected(false);
                pickerTool.setSelected(true);
                normalTool.setBorderPainted(false);
                fillTool.setBorderPainted(false);
                pickerTool.setBorderPainted(true);
            } else if (e.getActionCommand().endsWith("eraser")) {
                switch(tabbedEditor.getSelectedIndex()) {
                    case 0:
                        terrainBtnList.get("_blank.jpg").doClick();
                        break;
                    case 1:
                        objectBtnList.get("_blank.jpg").doClick();
                        break;
                    case 2:
                        fenceBtnList.get("_blank.jpg").doClick();
                        break;
                    case 3:
                        miscOverlayColor = OverlayTile.DEFAULT_COLOR;
                        updatePreview();
                        break;
                }
                normalTool.doClick();
            }
        } else if (e.getActionCommand().startsWith("zoom")) {
            if (e.getActionCommand().endsWith("in")) {
                zoomIn();
            } else if (e.getActionCommand().endsWith("out")) {
                zoomOut();
            }
        } else if (e.getActionCommand().startsWith("layer")) {
            if (e.getActionCommand().endsWith("top")) {
                viewAbove.setSelected(true);
                graphicPanel.repaint();
            } else if (e.getActionCommand().endsWith("cave")) {
                viewCave.setSelected(true);
                graphicPanel.repaint();
            }
        } else if (e.getSource() instanceof JRadioButton) {
            for (String s : terrainKeyList) terrainBtnList.get(s).setBorderPainted(false);
            for (String s : objectKeyList) objectBtnList.get(s).setBorderPainted(false);
            for (String s : fenceKeyList) if (!s.endsWith("_1")) fenceBtnList.get(s).setBorderPainted(false);
            ((JRadioButton) e.getSource()).setBorderPainted(true);
        } else if (e.getSource() instanceof JMenuItem) {
            if (e.getSource() == fileExit) {
                System.exit(0);
            } else if (e.getSource() == fileSave) {
                graphicPanel.getMap().saveMap();
            } else if (e.getSource() == fileSaveAs) {
                JFileChooser jfc = new JFileChooser(WPlanner.installDir + "saved/");
                jfc.setFileFilter(new WP2FileFilter());
                int retVal = jfc.showSaveDialog(this);
                if (retVal == JFileChooser.APPROVE_OPTION) {
                    File f = jfc.getSelectedFile();
                    graphicPanel.getMap().saveMap(f.getAbsolutePath());
                }
            } else if (e.getSource() == fileLoad) {
                JFileChooser jfc = new JFileChooser(WPlanner.installDir + "saved/");
                jfc.setFileFilter(new WP2FileFilter());
                int retVal = jfc.showOpenDialog(this);
                if (retVal == JFileChooser.APPROVE_OPTION) {
                    loadMap(jfc.getSelectedFile());
                }
            } else if (e.getSource() == fileSaveImage) {
                JFileChooser jfc = new JFileChooser(WPlanner.installDir + "saved/");
                jfc.setFileFilter(new PNGFileFilter());
                int retVal = jfc.showSaveDialog(this);
                if (retVal == JFileChooser.APPROVE_OPTION) {
                    File f = jfc.getSelectedFile();
                    if (f.getPath().endsWith(".png")) graphicPanel.saveToImage(f); else graphicPanel.saveToImage(new File(f.getAbsolutePath() + ".png"));
                }
            } else if (e.getSource() == editResize) {
                new ResizeDialog();
            } else if (e.getSource() == helpFile) {
                launchHelpFile();
            } else if (e.getSource() == helpAbout) {
                new AboutDialog();
            } else if (e.getSource() instanceof JCheckBoxMenuItem) {
                graphicPanel.repaint();
            }
        } else if (e.getSource() instanceof JButton) {
            if (e.getSource() == miscOverlayColorBtn) {
                miscOverlayColor = JColorChooser.showDialog(this, "Choose Colour", miscOverlayColor);
                updatePreview();
            } else if (e.getSource() == miscTextColorBtn) {
                miscTextColor = JColorChooser.showDialog(this, "Choose Colour", miscTextColor);
                updatePreview();
            } else if (e.getSource() == miscBlankTerrainColorBtn) {
                BufferedImage bi = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
                Graphics g = bi.getGraphics();
                g.setColor(JColorChooser.showDialog(this, "Choose Colour", graphicPanel.getBackground()));
                g.fillRect(0, 0, bi.getWidth(), bi.getHeight());
                terrainImgList.remove("_blank.jpg");
                terrainImgList.put("_blank.jpg", bi.getScaledInstance(128, 128, Image.SCALE_SMOOTH));
                graphicPanel.repaint();
            }
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == miscOpacity) updatePreview(); else if (e.getSource() == tabbedEditor) {
            switch(tabbedEditor.getSelectedIndex()) {
                case 0:
                    fillTool.setEnabled(true);
                    break;
                case 1:
                case 2:
                case 3:
                    fillTool.setEnabled(false);
                    normalTool.doClick();
                    break;
            }
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (isCtrlDown()) {
            if (e.getWheelRotation() < 0) zoomIn(); else zoomOut();
        }
    }

    class WP2FileFilter extends FileFilter {

        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            if (f.getName().contains(".") && (f.getName().substring(f.getName().lastIndexOf(".")).equals(".wp2") || f.getName().substring(f.getName().lastIndexOf(".")).equals(".wpm"))) return true;
            return false;
        }

        @Override
        public String getDescription() {
            return new String("WPlanner Save Files (.wpm / .wp2)");
        }
    }

    class PNGFileFilter extends FileFilter {

        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            if (f.getName().substring(f.getName().lastIndexOf(".")).equals(".png")) return true;
            return false;
        }

        @Override
        public String getDescription() {
            return new String("Image Files (.png)");
        }
    }

    class ResizeDialog extends JDialog implements ChangeListener, ActionListener {

        private static final long serialVersionUID = 2280794923221514480L;

        JPanel currentPanel, resizePanel, newPanel, goPanel;

        JSpinner mapNorth, mapEast, mapSouth, mapWest;

        SpinnerNumberModel northMdl, eastMdl, southMdl, westMdl;

        JLabel northLbl, eastLbl, southLbl, westLbl;

        JLabel currentWidth, currentHeight;

        JLabel newWidth, newHeight;

        JButton goButton;

        int currentX, currentY;

        ResizeDialog() {
            super();
            setTitle("Resize Map");
            setSize(400, 200);
            setResizable(false);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setLayout(new BorderLayout());
            currentX = graphicPanel.getMap().getMapWidth();
            currentY = graphicPanel.getMap().getMapHeight();
            currentPanel = new JPanel(new GridLayout(0, 1));
            currentWidth = new JLabel("Current width is: " + currentX);
            currentHeight = new JLabel("Current height is: " + currentY);
            currentPanel.add(currentWidth);
            currentPanel.add(currentHeight);
            resizePanel = new JPanel(new GridLayout(0, 2));
            northLbl = new JLabel("Rows to add to top (NORTH): ");
            northMdl = new SpinnerNumberModel(0, -(currentY - WPlanner.MIN_MAP_SIZE), WPlanner.MAX_MAP_SIZE - currentY, 1);
            mapNorth = new JSpinner(northMdl);
            northMdl.addChangeListener(this);
            southLbl = new JLabel("Rows to add to bottom (SOUTH): ");
            southMdl = new SpinnerNumberModel(0, -(currentY - WPlanner.MIN_MAP_SIZE), WPlanner.MAX_MAP_SIZE - currentY, 1);
            mapSouth = new JSpinner(southMdl);
            southMdl.addChangeListener(this);
            eastLbl = new JLabel("Columns to add to right (EAST): ");
            eastMdl = new SpinnerNumberModel(0, -(currentX - WPlanner.MIN_MAP_SIZE), WPlanner.MAX_MAP_SIZE - currentX, 1);
            mapEast = new JSpinner(eastMdl);
            eastMdl.addChangeListener(this);
            westLbl = new JLabel("Columns to add to left (WEST): ");
            westMdl = new SpinnerNumberModel(0, -(currentX - WPlanner.MIN_MAP_SIZE), WPlanner.MAX_MAP_SIZE - currentX, 1);
            mapWest = new JSpinner(westMdl);
            westMdl.addChangeListener(this);
            resizePanel.add(northLbl);
            resizePanel.add(mapNorth);
            resizePanel.add(southLbl);
            resizePanel.add(mapSouth);
            resizePanel.add(eastLbl);
            resizePanel.add(mapEast);
            resizePanel.add(westLbl);
            resizePanel.add(mapWest);
            newPanel = new JPanel(new GridLayout(0, 1));
            newWidth = new JLabel("New width will be: ");
            newHeight = new JLabel("New height will be: ");
            newPanel.add(newWidth);
            newPanel.add(newHeight);
            goButton = new JButton("Go!");
            goButton.addActionListener(this);
            add(currentPanel, BorderLayout.NORTH);
            add(resizePanel, BorderLayout.CENTER);
            add(newPanel, BorderLayout.SOUTH);
            add(goButton, BorderLayout.EAST);
            setVisible(true);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            if (e.getSource() == northMdl || e.getSource() == southMdl) newHeight.setText("New height will be: " + (currentY + southMdl.getNumber().intValue() + northMdl.getNumber().intValue()));
            if (e.getSource() == eastMdl || e.getSource() == westMdl) newWidth.setText("New width will be: " + (currentX + eastMdl.getNumber().intValue() + westMdl.getNumber().intValue()));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == goButton) {
                int newWidth = (currentX + eastMdl.getNumber().intValue() + westMdl.getNumber().intValue());
                int newHeight = (currentY + southMdl.getNumber().intValue() + northMdl.getNumber().intValue());
                int widthOffset = westMdl.getNumber().intValue();
                int heightOffset = northMdl.getNumber().intValue();
                if (newWidth > WPlanner.MAX_MAP_SIZE) newWidth = WPlanner.MAX_MAP_SIZE;
                if (newHeight > WPlanner.MAX_MAP_SIZE) newHeight = WPlanner.MAX_MAP_SIZE;
                graphicPanel.getMap().resizeMap(newWidth, newHeight, widthOffset, heightOffset);
                graphicPanel.setZoomLevel(graphicPanel.getZoomLevel());
                graphicPanel.repaint();
                JOptionPane.showMessageDialog(this, "Map Size changed to: width: " + newWidth + ", height: " + newHeight);
                dispose();
            }
        }
    }

    class AboutDialog extends JDialog {

        private static final long serialVersionUID = -3465233766949369612L;

        private Image logoImage = null;

        AboutDialog() {
            super();
            setTitle(WPlanner.TITLE + " | About");
            setSize(350, 220);
            setResizable(false);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setLayout(new BorderLayout());
            try {
                logoImage = ImageIO.read(new File(WPlanner.installDir + "logo.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            repaint();
            setVisible(true);
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            FontMetrics fm = g.getFontMetrics();
            String[] aboutStrings = { "WPlanner - " + WPlanner.VERSION, "For use with planning maps for Wurm Online", "Forum thread: http://is.gd/wplannerthread", "", "Created by Budda", "http://buddat.net" };
            int count = 0;
            g.drawImage(logoImage, (getWidth() / 2) - (logoImage.getWidth(null) / 2), 40, null);
            for (String s : aboutStrings) {
                Rectangle2D rect = fm.getStringBounds(s, g);
                int textHeight = (int) (rect.getHeight());
                int textWidth = (int) (rect.getWidth());
                g.drawString(s, (getWidth() / 2) - (textWidth / 2), 120 + (count++ * textHeight));
            }
        }
    }
}
