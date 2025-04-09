package org.dyn4j.testbed;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;
import javax.naming.ConfigurationException;
import javax.swing.AbstractSpinnerModel;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.dyn4j.Version;
import org.dyn4j.collision.broadphase.BroadphaseDetector;
import org.dyn4j.collision.continuous.TimeOfImpactDetector;
import org.dyn4j.collision.manifold.ManifoldSolver;
import org.dyn4j.collision.narrowphase.NarrowphaseDetector;
import org.dyn4j.dynamics.Settings;
import org.dyn4j.dynamics.Settings.ContinuousDetectionMode;

/**
 * The JFrame that controls the TestBed.
 * @author William Bittle
 * @version 3.0.3
 * @since 1.0.0
 */
public class ControlPanel extends JFrame {

    /** the version id */
    private static final long serialVersionUID = -461371622259288105L;

    /** Resource bundle containing the tests to load */
    private static ResourceBundle TESTS_BUNDLE = ResourceBundle.getBundle("org.dyn4j.testbed.tests");

    /** The simulation controls for all tests */
    private static final String[][] SIMULATION_CONTROLS = new String[][] { { "Exit Simulation", "Exits and closes the Test Bed and control panel.", "<html><span style='color: blue;'>Esc</span> or <span style='color: blue;'>e</span></html>" }, { "Pause Simualtion", "Pauses the current simulation.", "<html><span style='color: blue;'>Pause/Break</span> or <span style='color: blue;'>p</span></html>" }, { "Reset Simulation", "Resets the current simulation.", "<html><span style='color: blue;'>r</span></html>" }, { "Opens Control Panel", "Opens the control panel (what you are using now).", "<html><span style='color: blue;'>c</span></html>" }, { "Switch Simulation Mode", "<html>Toggles between Continuous, Manual, and Timed<br/>simulation modes.</html>", "<html><span style='color: blue;'>Space</span></html>" }, { "Perform Simulation Step", "<html>Performs one simulation step (only when in Manual mode).</html>", "<html><span style='color: blue;'>m</span></html>" }, { "Change Step Interval", "<html>Decreases/Increases the time interval between<br />simulation steps (only when in Timed mode).</html>", "<html><span style='color: blue;'>t</span> / <span style='color: blue;'>T</span></html>" }, { "Change Update Rate", "<html>Decreases/Increases the metrics update rate.</html>", "<html><span style='color: blue;'>u</span> / <span style='color: blue;'>U</span></html>" }, { "Log Bodies", "Outputs all bodies to std out.", "<html><span style='color: blue;'>o</span></html>" } };

    /** The camera controls for all tests */
    private static final String[][] CAMERA_CONTROLS = new String[][] { { "Pan Left", "Moves the camera left.", "<html><span style='color: blue;'>&larr;</span></html>" }, { "Pan Right", "Moves the camera right.", "<html><span style='color: blue;'>&rarr;</span></html>" }, { "Pan Up", "Moves the camera up.", "<html><span style='color: blue;'>&uarr;</span></html>" }, { "Pan Down", "Moves the camera down.", "<html><span style='color: blue;'>&darr;</span></html>" }, { "Zoom in", "Zooms in by a factor of 2.", "<html><span style='color: blue;'>+</span> or <span style='color: blue;'>Mouse Wheel Up</span></html>" }, { "Zoom out", "Zooms out by a factor of 2.", "<html><span style='color: blue;'>-</span> or <span style='color: blue;'>Mouse Wheel Down</span></html>" }, { "Center", "Resets the camera to the initial position.", "<html><span style='color: blue;'>Home</span> or <span style='color: blue;'>h</span></html>" } };

    /** The body controls for all tests */
    private static final String[][] BODY_CONTROLS = new String[][] { { "<html>Select Body<sup>1</sup></html>", "<html>Creates a MouseJoint between the mouse and the selected<br />body and uses the joint to translate/rotate the body.</html>", "<html><span style='color: blue;'>Left Mouse Button</span></html>" }, { "<html>Select Body<sup>2</sup></html>", "<html>Sets the selected body's mass to infinite and is<br />moved around by direct translation/rotation.</html>", "<html><span style='color: blue;'>Right Mouse Button</span></html>" }, { "Move Body", "<html>Depending on the selection method, the body<br />is moved to follow the mouse.</html>", "<html><span style='color: blue;'>Move Mouse</span></html>" }, { "Rotate Body", "<html>Hold to rotate the body about its center of mass.<br />This is only applicable using selection method 2.</html>", "<html><span style='color: blue;'>z</span></html>" }, { "Launch Bomb", "Launches a body from the left.", "<html><span style='color: blue;'>b</span></html>" } };

    /** A static listing of all colors */
    private static final float[][] COLORS = new float[][] { Draw.BLACK, Draw.DARK_GREY, Draw.GREY, Draw.LIGHT_GREY, Draw.WHITE, Draw.CYAN, Draw.BLUE, Draw.YELLOW, Draw.GREEN, Draw.MAGENTA, Draw.ORANGE, Draw.RED, Draw.PINK };

    /** Map of available test to run */
    private List<Test> tests;

    /** The current test */
    private Test test;

    /** The tabs on the control panel */
    private JTabbedPane tabs = null;

    /** The panel containing the test controls */
    private JPanel pnlTestControls = null;

    /** The panel for the controls tab */
    private JPanel pnlControls = null;

    /** The combo box for selecting a test */
    private JComboBox cmbTests = null;

    /** The label for showing if a test has specific controls */
    private JLabel panTestSpecificControls = null;

    /** The description field for the selected test */
    private JTextPane panTestDescription = null;

    /** The combo box for selecting a broad-phase CD algorithm */
    private JComboBox cmbBPCDAlgo = null;

    /** The combo box for selecting a narrow-phase CD algorithm */
    private JComboBox cmbNPCDAlgo = null;

    /** The combo box for selecting a manifold solving algorithm */
    private JComboBox cmbMSAlgo = null;

    /** The combo box for selecting a continuous collision detection algorithm */
    private JComboBox cmbTOIAlgo = null;

    /** The combo box for setting the continuous collision detection mode */
    private JComboBox cmbCCDMode = null;

    /** The selected broad-phase collision detection algorithm */
    private String selectedBPCDAlgo = "DynT";

    /** The selected narrow-phase collision detection algorithm */
    private String selectedNPCDAlgo = "Gjk";

    /** The selected manifold solving algorithm */
    private String selectedMSAlgo = "Clip";

    /** The selected manifold solving algorithm */
    private String selectedTOIAlgo = "CA";

    /** The image icon to show for help */
    private Icon helpIcon = null;

    /**
	 * Default constructor.
	 * @throws ConfigurationException if the tests.properties is missing or not configured
	 */
    public ControlPanel() throws ConfigurationException {
        super("dyn4j v" + Version.getVersion() + " TestBed Control Panel");
        try {
            this.setIconImage(ImageIO.read(this.getClass().getResource("/icon.png")));
        } catch (IOException e1) {
            System.err.println("Icon image 'icon.png' not found.");
        }
        this.helpIcon = new ImageIcon(this.getClass().getResource("/help.gif"), "Hover for help");
        this.tests = new ArrayList<Test>();
        Test defaultTest = null;
        String defaultTestKey = TESTS_BUNDLE.getString("default.test");
        Enumeration<String> keys = TESTS_BUNDLE.getKeys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            if (key.contains(".")) continue;
            String className = TESTS_BUNDLE.getString(key);
            try {
                Class<?> clazz = Class.forName(className);
                Test test = (Test) clazz.newInstance();
                test.initialize();
                this.tests.add(test);
                if (defaultTestKey.equals(key)) {
                    defaultTest = test;
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (ClassCastException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
        if (this.tests.size() == 0) {
            throw new ConfigurationException("At least one test must be configured in the tests.properties file.");
        }
        Collections.sort(this.tests);
        if (defaultTest != null) {
            this.setTest(defaultTest);
        } else {
            int size = this.tests.size();
            int index = (int) Math.floor(Math.random() * (size - 1));
            this.setTest(this.tests.get(index));
        }
        this.createGUI();
    }

    /**
	 * Sets the current test.
	 * @param test the test to run
	 */
    private void setTest(Test test) {
        this.test = test;
    }

    /**
	 * Returns the current {@link Test}.
	 * @return {@link Test} the current {@link Test}
	 */
    public Test getTest() {
        return this.test;
    }

    /**
	 * Returns the current {@link NarrowphaseDetector} name.
	 * @return String the current {@link NarrowphaseDetector} name
	 */
    public String getNPCDAlgorithm() {
        return this.selectedNPCDAlgo;
    }

    /**
	 * Returns the current {@link BroadphaseDetector} name.
	 * @return String the current {@link BroadphaseDetector} name
	 */
    public String getBPCDAlgorithm() {
        return this.selectedBPCDAlgo;
    }

    /**
	 * Returns the current {@link ManifoldSolver} name.
	 * @return String the current {@link ManifoldSolver} name
	 */
    public String getMSAlgorithm() {
        return this.selectedMSAlgo;
    }

    /**
	 * Returns the current {@link TimeOfImpactDetector} name.
	 * @return String the current {@link TimeOfImpactDetector} name
	 */
    public String getTOIAlgorithm() {
        return this.selectedTOIAlgo;
    }

    /**
	 * Creates the GUI for all configuration.
	 */
    private void createGUI() {
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        this.tabs = new JTabbedPane();
        tabs.setBorder(new EmptyBorder(5, 5, 5, 5));
        this.pnlControls = this.createControlsPanel();
        tabs.addTab(" Controls ", null, this.pnlControls, "View the list of controls.");
        JPanel pnlTest = this.createSelectTestPanel();
        tabs.addTab(" Tests ", null, pnlTest, "Select the test to run.");
        JPanel pnlDraw = this.createDrawingOptionsPanel();
        tabs.addTab(" Drawing Options ", null, pnlDraw, "Select drawing options.");
        JPanel pnlSettings = this.createSimulationSettingsPanel();
        tabs.addTab(" Simulation Settings ", null, pnlSettings, "Set simulation settings.");
        JPanel pnlAbout = this.createAboutPanel();
        tabs.addTab(" About ", null, pnlAbout, "About the dyn4j test bed.");
        this.add(tabs, BorderLayout.CENTER);
        this.setPreferredSize(new Dimension(450, 750));
        this.pack();
    }

    /**
	 * Creates the panel for the controls tab.
	 * @return JPanel the controls tab panel
	 */
    private JPanel createControlsPanel() {
        JPanel panel = new JPanel();
        Border border = new EmptyBorder(5, 5, 5, 5);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(border);
        Insets insets = new Insets(0, 6, 0, 4);
        Dimension labelSize = new Dimension(150, 15);
        JPanel pnlSimulation = new JPanel();
        pnlSimulation.setBorder(new CompoundBorder(new TitledBorder(" Simulation "), new EmptyBorder(insets)));
        pnlSimulation.setLayout(new SpringLayout());
        for (String[] control : SIMULATION_CONTROLS) {
            JLabel label = new JLabel(control[0], this.helpIcon, JLabel.LEFT);
            label.setToolTipText(control[1]);
            label.setPreferredSize(labelSize);
            label.setMaximumSize(labelSize);
            JLabel value = new JLabel(control[2]);
            pnlSimulation.add(label);
            pnlSimulation.add(value);
        }
        makeCompactGrid(pnlSimulation, 9, 2, 0, 0, 4, 4);
        panel.add(pnlSimulation);
        JPanel pnlCamera = new JPanel();
        pnlCamera.setBorder(new CompoundBorder(new TitledBorder(" Camera "), new EmptyBorder(insets)));
        pnlCamera.setLayout(new SpringLayout());
        for (String[] control : CAMERA_CONTROLS) {
            JLabel label = new JLabel(control[0], this.helpIcon, JLabel.LEFT);
            label.setToolTipText(control[1]);
            label.setPreferredSize(labelSize);
            label.setMaximumSize(labelSize);
            JLabel value = new JLabel(control[2]);
            pnlCamera.add(label);
            pnlCamera.add(value);
        }
        makeCompactGrid(pnlCamera, 7, 2, 0, 0, 4, 4);
        panel.add(pnlCamera);
        JPanel pnlBodies = new JPanel();
        pnlBodies.setBorder(new CompoundBorder(new TitledBorder(" Body "), new EmptyBorder(insets)));
        pnlBodies.setLayout(new SpringLayout());
        for (String[] control : BODY_CONTROLS) {
            JLabel label = new JLabel(control[0], this.helpIcon, JLabel.LEFT);
            label.setToolTipText(control[1]);
            label.setPreferredSize(labelSize);
            label.setMaximumSize(labelSize);
            JLabel value = new JLabel(control[2]);
            pnlBodies.add(label);
            pnlBodies.add(value);
        }
        makeCompactGrid(pnlBodies, 5, 2, 0, 0, 4, 4);
        panel.add(pnlBodies);
        this.pnlTestControls = new JPanel();
        this.pnlTestControls.setBorder(new CompoundBorder(new TitledBorder(" Test Specific Controls "), new EmptyBorder(insets)));
        this.addTestControls(this.pnlTestControls, this.test.getControls());
        panel.add(this.pnlTestControls);
        return panel;
    }

    /**
	 * Creates the panel where the user selects the current test.
	 * @return JPanel where the user selects the current test
	 */
    private JPanel createSelectTestPanel() {
        JPanel panel = new JPanel();
        Border border = new EmptyBorder(5, 5, 5, 5);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(border);
        Insets insets = new Insets(2, 2, 2, 2);
        JPanel pnlTest = new JPanel();
        pnlTest.setBorder(new TitledBorder("Test Selection"));
        pnlTest.setLayout(new GridBagLayout());
        JLabel lblTest = new JLabel("Tests", this.helpIcon, JLabel.LEFT);
        lblTest.setToolTipText("<html>After selecting a test and clicking Run,<br />check the controls tab for any test specific controls.</html>");
        pnlTest.add(lblTest, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.ABOVE_BASELINE_LEADING, GridBagConstraints.NONE, insets, 0, 0));
        cmbTests = new JComboBox(this.tests.toArray());
        cmbTests.setSelectedItem(this.test);
        pnlTest.add(cmbTests, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.VERTICAL, insets, 0, 0));
        JButton btnT = new JButton("Run");
        btnT.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setTest((Test) cmbTests.getSelectedItem());
                pnlTestControls.removeAll();
                addTestControls(pnlTestControls, test.getControls());
                tabs.removeTabAt(3);
                JPanel pnlSettings = createSimulationSettingsPanel();
                tabs.insertTab(" Simulation Settings ", null, pnlSettings, "Set simulation settings.", 3);
            }
        });
        pnlTest.add(btnT, new GridBagConstraints(2, 0, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        this.panTestSpecificControls = new JLabel();
        if (this.test.hasSpecificControls()) {
            this.panTestSpecificControls.setText("* Has specific controls");
        } else {
            this.panTestSpecificControls.setText("");
        }
        pnlTest.add(panTestSpecificControls, new GridBagConstraints(3, 0, 1, 1, 1, 0, GridBagConstraints.ABOVE_BASELINE_LEADING, GridBagConstraints.NONE, insets, 0, 0));
        JLabel lblDesc = new JLabel("Test description:");
        pnlTest.add(lblDesc, new GridBagConstraints(0, 1, 4, 1, 1, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        panTestDescription = new JTextPane();
        panTestDescription.setBorder(new CompoundBorder(new LineBorder(Color.black), border));
        panTestDescription.setEditable(false);
        panTestDescription.setText(test.getDescription());
        cmbTests.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Test test = ((Test) ((JComboBox) e.getSource()).getSelectedItem());
                panTestDescription.setText(test.getDescription());
                if (test.hasSpecificControls()) {
                    panTestSpecificControls.setText("* Has specific controls");
                } else {
                    panTestSpecificControls.setText("");
                }
            }
        });
        pnlTest.add(panTestDescription, new GridBagConstraints(0, 2, 4, 1, 1, 1, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH, insets, 0, 0));
        panel.add(pnlTest);
        return panel;
    }

    /**
	 * Creates the panel for the drawing options tab.
	 * @return JPanel the panel for the drawing options tab
	 */
    private JPanel createDrawingOptionsPanel() {
        JPanel panel = new JPanel();
        Border border = new EmptyBorder(5, 5, 5, 5);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(border);
        Insets insets = new Insets(2, 2, 2, 2);
        Draw draw = Draw.getInstance();
        int y = 0;
        JPanel pnlDraw = new JPanel();
        pnlDraw.setBorder(new TitledBorder("Drawing Options"));
        pnlDraw.setLayout(new GridBagLayout());
        JLabel lblFill = new JLabel("Shape Fill", this.helpIcon, JLabel.LEFT);
        lblFill.setToolTipText("Toggles filling of shapes.");
        pnlDraw.add(lblFill, new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.ABOVE_BASELINE_LEADING, GridBagConstraints.NONE, insets, 0, 0));
        JCheckBox chkFill = new JCheckBox();
        chkFill.setSelected(draw.drawFill());
        chkFill.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Draw draw = Draw.getInstance();
                draw.setDrawFill(!draw.drawFill());
            }
        });
        pnlDraw.add(chkFill, new GridBagConstraints(1, y, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        y++;
        JLabel lblOutline = new JLabel("Shape Outlines", this.helpIcon, JLabel.LEFT);
        lblOutline.setToolTipText("Toggles drawing of shape outlines.");
        pnlDraw.add(lblOutline, new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.ABOVE_BASELINE_LEADING, GridBagConstraints.NONE, insets, 0, 0));
        JCheckBox chkOutline = new JCheckBox();
        chkOutline.setSelected(draw.drawOutline());
        chkOutline.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Draw draw = Draw.getInstance();
                draw.setDrawOutline(!draw.drawOutline());
            }
        });
        pnlDraw.add(chkOutline, new GridBagConstraints(1, y, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        y++;
        JLabel lblAABBs = new JLabel("Expanded AABBs", this.helpIcon, JLabel.LEFT);
        lblAABBs.setToolTipText("Toggles drawing of the expanded AABBs used in the broadphase.");
        pnlDraw.add(lblAABBs, new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.ABOVE_BASELINE_LEADING, GridBagConstraints.NONE, insets, 0, 0));
        JCheckBox chkAABBs = new JCheckBox();
        chkAABBs.setSelected(draw.drawAABBs());
        chkAABBs.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Draw draw = Draw.getInstance();
                draw.setDrawAABBs(!draw.drawAABBs());
            }
        });
        pnlDraw.add(chkAABBs, new GridBagConstraints(1, y, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        JComboBox cmbAABBs = new JComboBox(COLORS);
        cmbAABBs.setSelectedItem(draw.getAABBColor());
        cmbAABBs.setRenderer(new ColorListCellRenderer());
        cmbAABBs.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                float[] color = (float[]) ((JComboBox) e.getSource()).getSelectedItem();
                Draw draw = Draw.getInstance();
                draw.setAABBColor(color);
            }
        });
        pnlDraw.add(cmbAABBs, new GridBagConstraints(2, y, 1, 1, 1, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        y++;
        JLabel lblCenter = new JLabel("Center of Mass", this.helpIcon, JLabel.LEFT);
        lblCenter.setToolTipText("Toggles drawing of the center of mass as a circle.");
        pnlDraw.add(lblCenter, new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.ABOVE_BASELINE_LEADING, GridBagConstraints.NONE, insets, 0, 0));
        JCheckBox chkCenter = new JCheckBox();
        chkCenter.setSelected(draw.drawCenter());
        chkCenter.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Draw draw = Draw.getInstance();
                draw.setDrawCenter(!draw.drawCenter());
            }
        });
        pnlDraw.add(chkCenter, new GridBagConstraints(1, y, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        JComboBox cmbCenter = new JComboBox(COLORS);
        cmbCenter.setSelectedItem(draw.getCenterColor());
        cmbCenter.setRenderer(new ColorListCellRenderer());
        cmbCenter.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                float[] color = (float[]) ((JComboBox) e.getSource()).getSelectedItem();
                Draw draw = Draw.getInstance();
                draw.setCenterColor(color);
            }
        });
        pnlDraw.add(cmbCenter, new GridBagConstraints(2, y, 1, 1, 1, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        y++;
        JLabel lblNormals = new JLabel("Edge Normals", this.helpIcon, JLabel.LEFT);
        lblNormals.setToolTipText("Toggles drawing of polygon edge normals.");
        pnlDraw.add(lblNormals, new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.ABOVE_BASELINE_LEADING, GridBagConstraints.NONE, insets, 0, 0));
        JCheckBox chkNormals = new JCheckBox();
        chkNormals.setSelected(draw.drawNormals());
        chkNormals.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Draw draw = Draw.getInstance();
                draw.setDrawNormals(!draw.drawNormals());
            }
        });
        pnlDraw.add(chkNormals, new GridBagConstraints(1, y, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        JComboBox cmbNormals = new JComboBox(COLORS);
        cmbNormals.setSelectedItem(draw.getNormalsColor());
        cmbNormals.setRenderer(new ColorListCellRenderer());
        cmbNormals.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                float[] color = (float[]) ((JComboBox) e.getSource()).getSelectedItem();
                Draw draw = Draw.getInstance();
                draw.setNormalsColor(color);
            }
        });
        pnlDraw.add(cmbNormals, new GridBagConstraints(2, y, 1, 1, 1, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        y++;
        JLabel lblRotDisc = new JLabel("Rotation Disc", this.helpIcon, JLabel.LEFT);
        lblRotDisc.setToolTipText("<html>Toggles drawing of the circle that contains the entire shape<br />rotated 360 degress.</html>");
        pnlDraw.add(lblRotDisc, new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.ABOVE_BASELINE_LEADING, GridBagConstraints.NONE, insets, 0, 0));
        JCheckBox chkRotDisc = new JCheckBox();
        chkRotDisc.setSelected(draw.drawRotationDisc());
        chkRotDisc.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Draw draw = Draw.getInstance();
                draw.setDrawRotationDisc(!draw.drawRotationDisc());
            }
        });
        pnlDraw.add(chkRotDisc, new GridBagConstraints(1, y, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        JComboBox cmbRotDisc = new JComboBox(COLORS);
        cmbRotDisc.setSelectedItem(draw.getRotationDiscColor());
        cmbRotDisc.setRenderer(new ColorListCellRenderer());
        cmbRotDisc.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                float[] color = (float[]) ((JComboBox) e.getSource()).getSelectedItem();
                Draw draw = Draw.getInstance();
                draw.setRotationDiscColor(color);
            }
        });
        pnlDraw.add(cmbRotDisc, new GridBagConstraints(2, y, 1, 1, 1, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        y++;
        JLabel lblVelocity = new JLabel("Velocity Vector", this.helpIcon, JLabel.LEFT);
        lblVelocity.setToolTipText("Toggles drawing of the velocity vector.");
        pnlDraw.add(lblVelocity, new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.ABOVE_BASELINE_LEADING, GridBagConstraints.NONE, insets, 0, 0));
        JCheckBox chkVelocity = new JCheckBox();
        chkVelocity.setSelected(draw.drawVelocity());
        chkVelocity.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Draw draw = Draw.getInstance();
                draw.setDrawVelocity(!draw.drawVelocity());
            }
        });
        pnlDraw.add(chkVelocity, new GridBagConstraints(1, y, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        JComboBox cmbVelocity = new JComboBox(COLORS);
        cmbVelocity.setSelectedItem(draw.getVelocityColor());
        cmbVelocity.setRenderer(new ColorListCellRenderer());
        cmbVelocity.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                float[] color = (float[]) ((JComboBox) e.getSource()).getSelectedItem();
                Draw draw = Draw.getInstance();
                draw.setVelocityColor(color);
            }
        });
        pnlDraw.add(cmbVelocity, new GridBagConstraints(2, y, 1, 1, 1, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        y++;
        JLabel lblContactPairs = new JLabel("Contact Pairs", this.helpIcon, JLabel.LEFT);
        lblContactPairs.setToolTipText("Toggles drawing of lines connecting pairs of bodies in contact.");
        pnlDraw.add(lblContactPairs, new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.ABOVE_BASELINE_LEADING, GridBagConstraints.NONE, insets, 0, 0));
        JCheckBox chkContactPairs = new JCheckBox();
        chkContactPairs.setSelected(draw.drawContactPairs());
        chkContactPairs.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Draw draw = Draw.getInstance();
                draw.setDrawContactPairs(!draw.drawContactPairs());
            }
        });
        pnlDraw.add(chkContactPairs, new GridBagConstraints(1, y, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        JComboBox cmbContactPairs = new JComboBox(COLORS);
        cmbContactPairs.setSelectedItem(draw.getContactPairsColor());
        cmbContactPairs.setRenderer(new ColorListCellRenderer());
        cmbContactPairs.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                float[] color = (float[]) ((JComboBox) e.getSource()).getSelectedItem();
                Draw draw = Draw.getInstance();
                draw.setContactPairsColor(color);
            }
        });
        pnlDraw.add(cmbContactPairs, new GridBagConstraints(2, y, 1, 1, 1, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        y++;
        JLabel lblContacts = new JLabel("Contact Points", this.helpIcon, JLabel.LEFT);
        lblContacts.setToolTipText("Toggles drawing of the contact points.");
        pnlDraw.add(lblContacts, new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.ABOVE_BASELINE_LEADING, GridBagConstraints.NONE, insets, 0, 0));
        JCheckBox chkContacts = new JCheckBox();
        chkContacts.setSelected(draw.drawContacts());
        chkContacts.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Draw draw = Draw.getInstance();
                draw.setDrawContacts(!draw.drawContacts());
            }
        });
        pnlDraw.add(chkContacts, new GridBagConstraints(1, y, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        JComboBox cmbContact = new JComboBox(COLORS);
        cmbContact.setSelectedItem(draw.getContactColor());
        cmbContact.setRenderer(new ColorListCellRenderer());
        cmbContact.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                float[] color = (float[]) ((JComboBox) e.getSource()).getSelectedItem();
                Draw draw = Draw.getInstance();
                draw.setContactColor(color);
            }
        });
        pnlDraw.add(cmbContact, new GridBagConstraints(2, y, 1, 1, 1, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        y++;
        JLabel lblContactForces = new JLabel("Contact Impulses", this.helpIcon, JLabel.LEFT);
        lblContactForces.setToolTipText("Toggles drawing of contact point impulses.");
        pnlDraw.add(lblContactForces, new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.ABOVE_BASELINE_LEADING, GridBagConstraints.NONE, insets, 0, 0));
        JCheckBox chkContactForces = new JCheckBox();
        chkContactForces.setSelected(draw.drawContactImpulses());
        chkContactForces.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Draw draw = Draw.getInstance();
                draw.setDrawContactImpulses(!draw.drawContactImpulses());
            }
        });
        pnlDraw.add(chkContactForces, new GridBagConstraints(1, y, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        JComboBox cmbContactForces = new JComboBox(COLORS);
        cmbContactForces.setSelectedItem(draw.getContactImpulsesColor());
        cmbContactForces.setRenderer(new ColorListCellRenderer());
        cmbContactForces.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                float[] color = (float[]) ((JComboBox) e.getSource()).getSelectedItem();
                Draw draw = Draw.getInstance();
                draw.setContactImpulsesColor(color);
            }
        });
        pnlDraw.add(cmbContactForces, new GridBagConstraints(2, y, 1, 1, 1, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        y++;
        JLabel lblFrictionForces = new JLabel("Friction Impulses", this.helpIcon, JLabel.LEFT);
        lblFrictionForces.setToolTipText("Toggles drawing of friction impulses.");
        pnlDraw.add(lblFrictionForces, new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.ABOVE_BASELINE_LEADING, GridBagConstraints.NONE, insets, 0, 0));
        JCheckBox chkFrictionForces = new JCheckBox();
        chkFrictionForces.setSelected(draw.drawFrictionImpulses());
        chkFrictionForces.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Draw draw = Draw.getInstance();
                draw.setDrawFrictionImpulses(!draw.drawFrictionImpulses());
            }
        });
        pnlDraw.add(chkFrictionForces, new GridBagConstraints(1, y, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        JComboBox cmbFrictionForces = new JComboBox(COLORS);
        cmbFrictionForces.setSelectedItem(draw.getFrictionImpulsesColor());
        cmbFrictionForces.setRenderer(new ColorListCellRenderer());
        cmbFrictionForces.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                float[] color = (float[]) ((JComboBox) e.getSource()).getSelectedItem();
                Draw draw = Draw.getInstance();
                draw.setFrictionImpulsesColor(color);
            }
        });
        pnlDraw.add(cmbFrictionForces, new GridBagConstraints(2, y, 1, 1, 1, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        y++;
        JLabel lblBounds = new JLabel("World Bounds", this.helpIcon, JLabel.LEFT);
        lblBounds.setToolTipText("Toggles drawing of the world bounds.");
        pnlDraw.add(lblBounds, new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.ABOVE_BASELINE_LEADING, GridBagConstraints.NONE, insets, 0, 0));
        JCheckBox chkBounds = new JCheckBox();
        chkBounds.setSelected(draw.drawBounds());
        chkBounds.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Draw draw = Draw.getInstance();
                draw.setDrawBounds(!draw.drawBounds());
            }
        });
        pnlDraw.add(chkBounds, new GridBagConstraints(1, y, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        JComboBox cmbBounds = new JComboBox(COLORS);
        cmbBounds.setSelectedItem(draw.getBoundsColor());
        cmbBounds.setRenderer(new ColorListCellRenderer());
        cmbBounds.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                float[] color = (float[]) ((JComboBox) e.getSource()).getSelectedItem();
                Draw draw = Draw.getInstance();
                draw.setBoundsColor(color);
            }
        });
        pnlDraw.add(cmbBounds, new GridBagConstraints(2, y, 1, 1, 1, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        y++;
        JLabel lblJoints = new JLabel("Joints", this.helpIcon, JLabel.LEFT);
        lblJoints.setToolTipText("Toggles drawing of joints.");
        pnlDraw.add(lblJoints, new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.ABOVE_BASELINE_LEADING, GridBagConstraints.NONE, insets, 0, 0));
        JCheckBox chkJoints = new JCheckBox();
        chkJoints.setSelected(draw.drawJoints());
        chkJoints.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Draw draw = Draw.getInstance();
                draw.setDrawJoints(!draw.drawJoints());
            }
        });
        pnlDraw.add(chkJoints, new GridBagConstraints(1, y, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        y++;
        JLabel lblText = new JLabel("Information Panel", this.helpIcon, JLabel.LEFT);
        lblText.setToolTipText("<html>Toggles drawing of the information panel which shows FPS,<br />memory usage, etc.</html>");
        pnlDraw.add(lblText, new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.ABOVE_BASELINE_LEADING, GridBagConstraints.NONE, insets, 0, 0));
        JCheckBox chkText = new JCheckBox();
        chkText.setSelected(draw.drawPanel());
        chkText.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Draw draw = Draw.getInstance();
                draw.setDrawPanel(!draw.drawPanel());
            }
        });
        pnlDraw.add(chkText, new GridBagConstraints(1, y, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        y++;
        JLabel lblBlur = new JLabel("Information Panel Blur", this.helpIcon, JLabel.LEFT);
        lblBlur.setToolTipText("<html>Toggles blurring of the information panel background.</html>");
        pnlDraw.add(lblBlur, new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.ABOVE_BASELINE_LEADING, GridBagConstraints.NONE, insets, 0, 0));
        JCheckBox chkBlur = new JCheckBox();
        chkBlur.setSelected(draw.isPanelBlurred());
        chkBlur.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Draw draw = Draw.getInstance();
                draw.setPanelBlurred(!draw.isPanelBlurred());
            }
        });
        pnlDraw.add(chkBlur, new GridBagConstraints(1, y, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        y++;
        JLabel lblAnti = new JLabel("Anti-Aliasing (MSAA 2x)", this.helpIcon, JLabel.LEFT);
        lblAnti.setToolTipText("<html>Toggles the use of anti-aliasing.</html>");
        pnlDraw.add(lblAnti, new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.ABOVE_BASELINE_LEADING, GridBagConstraints.NONE, insets, 0, 0));
        JCheckBox chkAnti = new JCheckBox();
        chkAnti.setSelected(draw.isAntiAliased());
        chkAnti.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Draw draw = Draw.getInstance();
                draw.setAntiAliased(!draw.isAntiAliased());
            }
        });
        pnlDraw.add(chkAnti, new GridBagConstraints(1, y, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        y++;
        JLabel lblVertSync = new JLabel("Vertical Sync", this.helpIcon, JLabel.LEFT);
        lblVertSync.setToolTipText("<html>Toggles vertical sync.  Rendering occurs only at the<br />display refresh rate, typically 60hz.  Not all<br />cards/drivers support changing of this setting.</html>");
        pnlDraw.add(lblVertSync, new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.ABOVE_BASELINE_LEADING, GridBagConstraints.NONE, insets, 0, 0));
        JCheckBox chkVertSync = new JCheckBox();
        chkVertSync.setSelected(draw.isVerticalSyncEnabled());
        chkVertSync.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Draw draw = Draw.getInstance();
                draw.setVerticalSyncEnabled(!draw.isVerticalSyncEnabled());
            }
        });
        pnlDraw.add(chkVertSync, new GridBagConstraints(1, y, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        y++;
        JLabel blank = new JLabel();
        pnlDraw.add(blank, new GridBagConstraints(0, y, 3, 1, 0, 1, GridBagConstraints.ABOVE_BASELINE_LEADING, GridBagConstraints.NONE, insets, 0, 0));
        y++;
        panel.add(pnlDraw);
        return panel;
    }

    /**
	 * Creates the panel for the simulation settings tab.
	 * @return JPanel the simulation settings panel
	 */
    private JPanel createSimulationSettingsPanel() {
        Settings settings = this.test.world.getSettings();
        JPanel panel = new JPanel();
        Border border = new EmptyBorder(5, 5, 5, 5);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(border);
        Insets insets = new Insets(2, 2, 2, 2);
        int w1 = 220;
        JPanel pnlGeneral = new JPanel();
        pnlGeneral.setBorder(new TitledBorder("General Settings"));
        pnlGeneral.setLayout(new GridBagLayout());
        JLabel lblBPCDAlgo = new JLabel("Broad-phase Collision Detection Algorithm", this.helpIcon, JLabel.LEFT);
        lblBPCDAlgo.setPreferredSize(new Dimension(w1, 20));
        lblBPCDAlgo.setToolTipText("<html>Specifies the algorithm used to handle<br />broad-phase collision detection.</html>");
        pnlGeneral.add(lblBPCDAlgo, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        cmbBPCDAlgo = new JComboBox(new String[] { "SapI", "SapBF", "SapT", "DynT" });
        cmbBPCDAlgo.setSelectedItem(this.selectedBPCDAlgo);
        pnlGeneral.add(cmbBPCDAlgo, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_END, GridBagConstraints.NONE, insets, 0, 0));
        JButton btnBPCDAlgo = new JButton("Set");
        btnBPCDAlgo.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                selectedBPCDAlgo = (String) cmbBPCDAlgo.getSelectedItem();
            }
        });
        pnlGeneral.add(btnBPCDAlgo, new GridBagConstraints(2, 0, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        JLabel lblCDAlgo = new JLabel("Narrow-phase Collision Detection Algorithm", this.helpIcon, JLabel.LEFT);
        lblCDAlgo.setToolTipText("<html>Specifies the algorithm used to handle<br />narrow-phase collision detection.</html>");
        pnlGeneral.add(lblCDAlgo, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        cmbNPCDAlgo = new JComboBox(new String[] { "Gjk", "Sat" });
        cmbNPCDAlgo.setSelectedItem(this.selectedNPCDAlgo);
        pnlGeneral.add(cmbNPCDAlgo, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_END, GridBagConstraints.NONE, insets, 0, 0));
        JButton btnCDAlgo = new JButton("Set");
        btnCDAlgo.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                selectedNPCDAlgo = (String) cmbNPCDAlgo.getSelectedItem();
            }
        });
        pnlGeneral.add(btnCDAlgo, new GridBagConstraints(2, 1, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        JLabel lblMSAlgo = new JLabel("Manifold Solving Algorithm", this.helpIcon, JLabel.LEFT);
        lblMSAlgo.setToolTipText("<html>Specifies the algorithm used to create<br />collision manifolds.</html>");
        pnlGeneral.add(lblMSAlgo, new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        cmbMSAlgo = new JComboBox(new String[] { "Clip" });
        cmbMSAlgo.setSelectedItem(this.selectedMSAlgo);
        pnlGeneral.add(cmbMSAlgo, new GridBagConstraints(1, 2, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_END, GridBagConstraints.NONE, insets, 0, 0));
        JButton btnMSAlgo = new JButton("Set");
        btnMSAlgo.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                selectedMSAlgo = (String) cmbMSAlgo.getSelectedItem();
            }
        });
        pnlGeneral.add(btnMSAlgo, new GridBagConstraints(2, 2, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        JLabel lblTOIAlgo = new JLabel("Time Of Impact Detection Algorithm", this.helpIcon, JLabel.LEFT);
        lblTOIAlgo.setToolTipText("<html>Specifies the time of impact algorithm used<br />for continuous collision detection.</html>");
        pnlGeneral.add(lblTOIAlgo, new GridBagConstraints(0, 3, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        cmbTOIAlgo = new JComboBox(new String[] { "CA" });
        cmbTOIAlgo.setSelectedItem(this.selectedTOIAlgo);
        pnlGeneral.add(cmbTOIAlgo, new GridBagConstraints(1, 3, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_END, GridBagConstraints.NONE, insets, 0, 0));
        JButton btnTOIAlgo = new JButton("Set");
        btnTOIAlgo.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                selectedTOIAlgo = (String) cmbTOIAlgo.getSelectedItem();
            }
        });
        pnlGeneral.add(btnTOIAlgo, new GridBagConstraints(2, 3, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        JLabel lblStep = new JLabel("Step Fequency", this.helpIcon, JLabel.LEFT);
        lblStep.setToolTipText("<html>Specifies the number of updates the dynamics<br />engine will attempt to perform per second.</html>");
        pnlGeneral.add(lblStep, new GridBagConstraints(0, 4, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        JSpinner spnStep = new JSpinner(new SpinnerNumberModel(1.0 / settings.getStepFrequency(), 30, 995, 5));
        spnStep.setEditor(new JSpinner.NumberEditor(spnStep, "0"));
        ((JSpinner.DefaultEditor) spnStep.getEditor()).getTextField().setColumns(3);
        ((JSpinner.DefaultEditor) spnStep.getEditor()).getTextField().setEditable(false);
        spnStep.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                JSpinner spnr = (JSpinner) e.getSource();
                double hz = ((SpinnerNumberModel) spnr.getModel()).getNumber().doubleValue();
                Settings settings = test.world.getSettings();
                settings.setStepFrequency(hz);
            }
        });
        pnlGeneral.add(spnStep, new GridBagConstraints(1, 4, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_END, GridBagConstraints.NONE, insets, 0, 0));
        JLabel lblStepUnit = new JLabel("<html>second<sup>-1</sup></html>");
        pnlGeneral.add(lblStepUnit, new GridBagConstraints(2, 4, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        JLabel lblMaxV = new JLabel("Maximum Translation", this.helpIcon, JLabel.LEFT);
        lblMaxV.setToolTipText("<html>Specifies the maximum translation a body can<br />have in one time step.</html>");
        pnlGeneral.add(lblMaxV, new GridBagConstraints(0, 5, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        JSpinner spnMaxV = new JSpinner(new SpinnerNumberModel(settings.getMaximumTranslation(), 0.0, 10.0, 0.1));
        spnMaxV.setEditor(new JSpinner.NumberEditor(spnMaxV, "0.0"));
        ((JSpinner.DefaultEditor) spnMaxV.getEditor()).getTextField().setColumns(4);
        spnMaxV.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                JSpinner spnr = (JSpinner) e.getSource();
                double t = ((SpinnerNumberModel) spnr.getModel()).getNumber().doubleValue();
                Settings settings = test.world.getSettings();
                settings.setMaximumTranslation(t);
            }
        });
        pnlGeneral.add(spnMaxV, new GridBagConstraints(1, 5, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_END, GridBagConstraints.NONE, insets, 0, 0));
        JLabel lblMaxVUnit = new JLabel("meters");
        pnlGeneral.add(lblMaxVUnit, new GridBagConstraints(2, 5, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        JLabel lblMaxAv = new JLabel("Maximum Rotation", this.helpIcon, JLabel.LEFT);
        lblMaxAv.setToolTipText("<html>Specifies the maximum rotation a body can<br />have in one time step.</html>");
        pnlGeneral.add(lblMaxAv, new GridBagConstraints(0, 6, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        JSpinner spnMaxAv = new JSpinner(new SpinnerNumberModel(Math.toDegrees(settings.getMaximumRotation()), 0, 360, 1));
        spnMaxAv.setEditor(new JSpinner.NumberEditor(spnMaxAv, "0"));
        ((JSpinner.DefaultEditor) spnMaxAv.getEditor()).getTextField().setColumns(3);
        spnMaxAv.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                JSpinner spnr = (JSpinner) e.getSource();
                double r = ((SpinnerNumberModel) spnr.getModel()).getNumber().doubleValue();
                Settings settings = test.world.getSettings();
                settings.setMaximumRotation(Math.toRadians(r));
            }
        });
        pnlGeneral.add(spnMaxAv, new GridBagConstraints(1, 6, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_END, GridBagConstraints.NONE, insets, 0, 0));
        JLabel lblMaxAvUnit = new JLabel("degrees");
        pnlGeneral.add(lblMaxAvUnit, new GridBagConstraints(2, 6, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        JLabel lblCCDEnabled = new JLabel("Continuous Collision Detection", this.helpIcon, JLabel.LEFT);
        lblCCDEnabled.setToolTipText("<html>If enabled, tests dynamic bodies for tunneling.<br />The None setting disables CCD completely.<br />The Bullets setting only checks bullets for tunneling.<br />The All setting checks all dynamic bodies for tunneling.</html>");
        pnlGeneral.add(lblCCDEnabled, new GridBagConstraints(0, 7, 1, 1, 0, 1, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        cmbCCDMode = new JComboBox(new String[] { "None", "Bullets", "All" });
        cmbCCDMode.setSelectedItem("All");
        pnlGeneral.add(cmbCCDMode, new GridBagConstraints(1, 7, 1, 1, 0, 1, GridBagConstraints.FIRST_LINE_END, GridBagConstraints.NONE, insets, 0, 0));
        JButton btnCCDMode = new JButton("Set");
        btnCCDMode.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String value = (String) cmbCCDMode.getSelectedItem();
                Settings settings = test.world.getSettings();
                if ("All".equals(value)) {
                    settings.setContinuousDetectionMode(ContinuousDetectionMode.ALL);
                } else if ("Bullets".equals(value)) {
                    settings.setContinuousDetectionMode(ContinuousDetectionMode.BULLETS_ONLY);
                } else {
                    settings.setContinuousDetectionMode(ContinuousDetectionMode.NONE);
                }
            }
        });
        pnlGeneral.add(btnCCDMode, new GridBagConstraints(2, 7, 1, 1, 1, 1, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        panel.add(pnlGeneral);
        JPanel pnlSleep = new JPanel();
        pnlSleep.setBorder(new TitledBorder("Sleep Settings"));
        pnlSleep.setLayout(new GridBagLayout());
        JLabel lblAllowSleep = new JLabel("Allow bodies to sleep?", this.helpIcon, JLabel.LEFT);
        lblAllowSleep.setPreferredSize(new Dimension(w1, 20));
        lblAllowSleep.setToolTipText("<html>Sleeping allows the physics system to save cycles by<br />avoiding unnecessary work for bodies who are not in motion.</html>");
        pnlSleep.add(lblAllowSleep, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        JCheckBox chkAllowSleep = new JCheckBox();
        chkAllowSleep.setSelected(settings.isAutoSleepingEnabled());
        chkAllowSleep.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Settings settings = test.world.getSettings();
                settings.setAutoSleepingEnabled(!settings.isAutoSleepingEnabled());
            }
        });
        pnlSleep.add(chkAllowSleep, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_END, GridBagConstraints.NONE, insets, 0, 0));
        JLabel lblSleepTime = new JLabel("Sleep time", this.helpIcon, JLabel.LEFT);
        lblSleepTime.setToolTipText("<html>Specifies the required amount of time a body<br />must be at rest before being put to sleep.</html>");
        pnlSleep.add(lblSleepTime, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        JSpinner spnSleepTime = new JSpinner(new SpinnerNumberModel(settings.getSleepTime(), 0.0, 9.9, 0.1));
        spnSleepTime.setEditor(new JSpinner.NumberEditor(spnSleepTime, "0.0"));
        ((JSpinner.DefaultEditor) spnSleepTime.getEditor()).getTextField().setColumns(3);
        spnSleepTime.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                JSpinner spnr = (JSpinner) e.getSource();
                double time = ((SpinnerNumberModel) spnr.getModel()).getNumber().doubleValue();
                Settings settings = test.world.getSettings();
                settings.setSleepTime(time);
            }
        });
        pnlSleep.add(spnSleepTime, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_END, GridBagConstraints.NONE, insets, 0, 0));
        JLabel lblSleepTimeUnit = new JLabel("seconds");
        pnlSleep.add(lblSleepTimeUnit, new GridBagConstraints(2, 1, 1, 1, 1, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        JLabel lblSleepMaxV = new JLabel("Maximum velocity", this.helpIcon, JLabel.LEFT);
        lblSleepMaxV.setToolTipText("<html>Specifies the maximum velocity used<br />to determine whether a body is at rest.</html>");
        pnlSleep.add(lblSleepMaxV, new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        JSpinner spnSleepMaxV = new JSpinner(new SpinnerNumberModel(settings.getSleepLinearVelocity(), 0.0, 9.99, 0.01));
        spnSleepMaxV.setEditor(new JSpinner.NumberEditor(spnSleepMaxV, "0.00"));
        ((JSpinner.DefaultEditor) spnSleepMaxV.getEditor()).getTextField().setColumns(4);
        spnSleepMaxV.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                JSpinner spnr = (JSpinner) e.getSource();
                double v = ((SpinnerNumberModel) spnr.getModel()).getNumber().doubleValue();
                Settings settings = test.world.getSettings();
                settings.setSleepLinearVelocity(v);
            }
        });
        pnlSleep.add(spnSleepMaxV, new GridBagConstraints(1, 2, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_END, GridBagConstraints.NONE, insets, 0, 0));
        JLabel lblSleepMaxVUnit = new JLabel("meters / second");
        pnlSleep.add(lblSleepMaxVUnit, new GridBagConstraints(2, 2, 1, 1, 1, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        JLabel lblSleepMaxAv = new JLabel("Maximum angular velocity", this.helpIcon, JLabel.LEFT);
        lblSleepMaxAv.setToolTipText("<html>Specifies the maximum angular velocity used<br />to determine whether a body is at rest.</html>");
        pnlSleep.add(lblSleepMaxAv, new GridBagConstraints(0, 3, 1, 1, 0, 1, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        JSpinner spnSleepMaxAv = new JSpinner(new SpinnerNumberModel(Math.toDegrees(settings.getSleepAngularVelocity()), 0.0, 360.0, 0.1));
        spnSleepMaxAv.setEditor(new JSpinner.NumberEditor(spnSleepMaxAv, "0.0"));
        ((JSpinner.DefaultEditor) spnSleepMaxAv.getEditor()).getTextField().setColumns(5);
        spnSleepMaxAv.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                JSpinner spnr = (JSpinner) e.getSource();
                double v = ((SpinnerNumberModel) spnr.getModel()).getNumber().doubleValue();
                Settings settings = test.world.getSettings();
                settings.setSleepAngularVelocity(Math.toRadians(v));
            }
        });
        pnlSleep.add(spnSleepMaxAv, new GridBagConstraints(1, 3, 1, 1, 0, 1, GridBagConstraints.FIRST_LINE_END, GridBagConstraints.NONE, insets, 0, 0));
        JLabel lblSleepMaxAvUnit = new JLabel("degrees / second");
        pnlSleep.add(lblSleepMaxAvUnit, new GridBagConstraints(2, 3, 1, 1, 1, 1, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        panel.add(pnlSleep);
        JPanel pnlConstraint = new JPanel();
        pnlConstraint.setBorder(new TitledBorder("Constraint Solver Settings"));
        pnlConstraint.setLayout(new GridBagLayout());
        int y = 0;
        JLabel lblVelIter = new JLabel("Velocity Iterations", this.helpIcon, JLabel.LEFT);
        lblVelIter.setPreferredSize(new Dimension(w1, 20));
        lblVelIter.setToolTipText("<html>Specifies the accuracy of the velocity contraint solver.<br />Increasing this value increases the accuracy but lowers performance.</html>");
        pnlConstraint.add(lblVelIter, new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        JSpinner spnVelIter = new JSpinner(new SpinnerNumberModel(settings.getVelocityConstraintSolverIterations(), 5, 999, 1));
        spnVelIter.setEditor(new JSpinner.NumberEditor(spnVelIter, "0"));
        ((JSpinner.DefaultEditor) spnVelIter.getEditor()).getTextField().setColumns(3);
        spnVelIter.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                JSpinner spnr = (JSpinner) e.getSource();
                int iter = ((SpinnerNumberModel) spnr.getModel()).getNumber().intValue();
                Settings settings = test.world.getSettings();
                settings.setVelocityConstraintSolverIterations(iter);
            }
        });
        pnlConstraint.add(spnVelIter, new GridBagConstraints(1, y, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_END, GridBagConstraints.NONE, insets, 0, 0));
        y++;
        JLabel lblPosIter = new JLabel("Position Iterations", this.helpIcon, JLabel.LEFT);
        lblPosIter.setToolTipText("<html>Specifies the accuracy of the position contraint solver.<br />Increasing this value increases the accuracy but lowers performance.</html>");
        pnlConstraint.add(lblPosIter, new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        JSpinner spnPosIter = new JSpinner(new SpinnerNumberModel(settings.getPositionConstraintSolverIterations(), 5, 999, 1));
        spnPosIter.setEditor(new JSpinner.NumberEditor(spnPosIter, "0"));
        ((JSpinner.DefaultEditor) spnPosIter.getEditor()).getTextField().setColumns(3);
        spnPosIter.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                JSpinner spnr = (JSpinner) e.getSource();
                int iter = ((SpinnerNumberModel) spnr.getModel()).getNumber().intValue();
                Settings settings = test.world.getSettings();
                settings.setPositionConstraintSolverIterations(iter);
            }
        });
        pnlConstraint.add(spnPosIter, new GridBagConstraints(1, y, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_END, GridBagConstraints.NONE, insets, 0, 0));
        y++;
        JLabel lblWarm = new JLabel("Warm start distance", this.helpIcon, JLabel.LEFT);
        lblWarm.setToolTipText("<html>Specifies the distance between two iteration's contact points<br />" + "to determine whether to warm start.  Set this value to to zero to<br />" + "turn off warm starting.  Warm starting provides better<br />" + "performance and accuracy.</html>");
        pnlConstraint.add(lblWarm, new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        JSpinner spnWarm = new JSpinner(new MultiplicativeSpinnerNumberModel(settings.getWarmStartDistance(), 1.0E-9, 1.0, 10.0));
        spnWarm.setEditor(new MultiplicativeSpinnerModelEditor(spnWarm));
        spnWarm.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                JSpinner spnr = (JSpinner) e.getSource();
                double warm = ((MultiplicativeSpinnerNumberModel) spnr.getModel()).getValue();
                Settings settings = test.world.getSettings();
                settings.setWarmStartDistance(warm);
            }
        });
        pnlConstraint.add(spnWarm, new GridBagConstraints(1, y, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_END, GridBagConstraints.NONE, insets, 0, 0));
        JLabel lblWarmUnit = new JLabel("meters");
        pnlConstraint.add(lblWarmUnit, new GridBagConstraints(2, y, 1, 1, 1, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        y++;
        JLabel lblRest = new JLabel("Restitution velocity", this.helpIcon, JLabel.LEFT);
        lblRest.setToolTipText("<html>Specifies at what relative velocity objects should<br />bounce or attempt to come to rest.</html>");
        pnlConstraint.add(lblRest, new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        JSpinner spnRest = new JSpinner(new SpinnerNumberModel(settings.getRestitutionVelocity(), 0.0, 9.9, 0.1));
        spnRest.setEditor(new JSpinner.NumberEditor(spnRest, "0.0"));
        ((JSpinner.DefaultEditor) spnRest.getEditor()).getTextField().setColumns(3);
        spnRest.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                JSpinner spnr = (JSpinner) e.getSource();
                double r = ((SpinnerNumberModel) spnr.getModel()).getNumber().doubleValue();
                Settings settings = test.world.getSettings();
                settings.setRestitutionVelocity(r);
            }
        });
        pnlConstraint.add(spnRest, new GridBagConstraints(1, y, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_END, GridBagConstraints.NONE, insets, 0, 0));
        JLabel lblRestUnit = new JLabel("meters / second");
        pnlConstraint.add(lblRestUnit, new GridBagConstraints(2, y, 1, 1, 1, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        y++;
        JLabel lblLinTol = new JLabel("Linear tolerance", this.helpIcon, JLabel.LEFT);
        lblLinTol.setToolTipText("<html>Specifies the linear tolerance. This setting is<br />used to control jitter.</html>");
        pnlConstraint.add(lblLinTol, new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        JSpinner spnLinTol = new JSpinner(new SpinnerNumberModel(settings.getLinearTolerance(), 0.000, 9.999, 0.001));
        spnLinTol.setEditor(new JSpinner.NumberEditor(spnLinTol, "0.000"));
        ((JSpinner.DefaultEditor) spnLinTol.getEditor()).getTextField().setColumns(5);
        spnLinTol.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                JSpinner spnr = (JSpinner) e.getSource();
                double tol = ((SpinnerNumberModel) spnr.getModel()).getNumber().doubleValue();
                Settings settings = test.world.getSettings();
                settings.setLinearTolerance(tol);
            }
        });
        pnlConstraint.add(spnLinTol, new GridBagConstraints(1, y, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_END, GridBagConstraints.NONE, insets, 0, 0));
        JLabel lblLinTolUnit = new JLabel("meters");
        pnlConstraint.add(lblLinTolUnit, new GridBagConstraints(2, y, 1, 1, 1, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        y++;
        JLabel lblAngTol = new JLabel("Angular tolerance", this.helpIcon, JLabel.LEFT);
        lblAngTol.setToolTipText("<html>Specifies the angular tolerance. This setting is<br />used to control jitter.</html>");
        pnlConstraint.add(lblAngTol, new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        JSpinner spnAngTol = new JSpinner(new SpinnerNumberModel(Math.toDegrees(settings.getAngularTolerance()), 0.0, 90.0, 0.1));
        spnAngTol.setEditor(new JSpinner.NumberEditor(spnAngTol, "0.0"));
        ((JSpinner.DefaultEditor) spnAngTol.getEditor()).getTextField().setColumns(4);
        spnAngTol.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                JSpinner spnr = (JSpinner) e.getSource();
                double tol = ((SpinnerNumberModel) spnr.getModel()).getNumber().doubleValue();
                Settings settings = test.world.getSettings();
                settings.setAngularTolerance(Math.toRadians(tol));
            }
        });
        pnlConstraint.add(spnAngTol, new GridBagConstraints(1, y, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_END, GridBagConstraints.NONE, insets, 0, 0));
        JLabel lblAngTolUnit = new JLabel("degrees");
        pnlConstraint.add(lblAngTolUnit, new GridBagConstraints(2, y, 1, 1, 1, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        y++;
        JLabel lblLinear = new JLabel("Maximum linear correction", this.helpIcon, JLabel.LEFT);
        lblLinear.setToolTipText("<html>Specifies the maximum amount of linear correction<br />to perform in position solving.  This is used<br />to avoid large position corrections.</html>");
        pnlConstraint.add(lblLinear, new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        JSpinner spnLinear = new JSpinner(new SpinnerNumberModel(settings.getMaximumLinearCorrection(), 0.0, 1.0, 0.01));
        spnLinear.setEditor(new JSpinner.NumberEditor(spnLinear, "0.00"));
        ((JSpinner.DefaultEditor) spnLinear.getEditor()).getTextField().setColumns(4);
        spnLinear.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                JSpinner spnr = (JSpinner) e.getSource();
                double lin = ((SpinnerNumberModel) spnr.getModel()).getNumber().doubleValue();
                Settings settings = test.world.getSettings();
                settings.setMaximumLinearCorrection(lin);
            }
        });
        pnlConstraint.add(spnLinear, new GridBagConstraints(1, y, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_END, GridBagConstraints.NONE, insets, 0, 0));
        JLabel lblLinearUnit = new JLabel("meters");
        pnlConstraint.add(lblLinearUnit, new GridBagConstraints(2, y, 1, 1, 1, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        y++;
        JLabel lblAngular = new JLabel("Maximum angular correction", this.helpIcon, JLabel.LEFT);
        lblAngular.setToolTipText("<html>Specifies the maximum amount of angular correction<br />to perform in position solving.  This is used<br />to avoid large position corrections.</html>");
        pnlConstraint.add(lblAngular, new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        JSpinner spnAngular = new JSpinner(new SpinnerNumberModel(Math.toDegrees(settings.getMaximumAngularCorrection()), 0.0, 90.0, 0.1));
        spnAngular.setEditor(new JSpinner.NumberEditor(spnAngular, "0.0"));
        ((JSpinner.DefaultEditor) spnAngular.getEditor()).getTextField().setColumns(4);
        spnAngular.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                JSpinner spnr = (JSpinner) e.getSource();
                double ang = ((SpinnerNumberModel) spnr.getModel()).getNumber().doubleValue();
                Settings settings = test.world.getSettings();
                settings.setMaximumAngularCorrection(Math.toRadians(ang));
            }
        });
        pnlConstraint.add(spnAngular, new GridBagConstraints(1, y, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_END, GridBagConstraints.NONE, insets, 0, 0));
        JLabel lblAngularUnit = new JLabel("degrees");
        pnlConstraint.add(lblAngularUnit, new GridBagConstraints(2, y, 1, 1, 1, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        y++;
        JLabel lblBaum = new JLabel("Baumgarte", this.helpIcon, JLabel.LEFT);
        lblBaum.setToolTipText("<html>Specifies the rate at which the position<br />constraints are solved.</html>");
        pnlConstraint.add(lblBaum, new GridBagConstraints(0, y, 1, 1, 0, 1, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        JSpinner spnBaum = new JSpinner(new SpinnerNumberModel(settings.getBaumgarte(), 0.0, 1.0, 0.01));
        spnBaum.setEditor(new JSpinner.NumberEditor(spnBaum, "0.00"));
        ((JSpinner.DefaultEditor) spnBaum.getEditor()).getTextField().setColumns(4);
        spnBaum.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                JSpinner spnr = (JSpinner) e.getSource();
                double baum = ((SpinnerNumberModel) spnr.getModel()).getNumber().doubleValue();
                Settings settings = test.world.getSettings();
                settings.setBaumgarte(baum);
            }
        });
        pnlConstraint.add(spnBaum, new GridBagConstraints(1, y, 1, 1, 0, 1, GridBagConstraints.FIRST_LINE_END, GridBagConstraints.NONE, insets, 0, 0));
        panel.add(pnlConstraint);
        return panel;
    }

    /**
	 * Creates the About tab panel.
	 * @return JPanel the panel for the About tab
	 */
    private JPanel createAboutPanel() {
        JPanel panel = new JPanel();
        Border border = new EmptyBorder(5, 5, 5, 5);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(border);
        JPanel pnlAbout = new JPanel();
        BoxLayout bl = new BoxLayout(pnlAbout, BoxLayout.Y_AXIS);
        pnlAbout.setLayout(bl);
        JLabel icon = new JLabel();
        icon.setIcon(new ImageIcon(this.getClass().getResource("/icon.png")));
        icon.setAlignmentX(CENTER_ALIGNMENT);
        pnlAbout.add(icon);
        JLabel version = new JLabel("Version: " + Version.getVersion() + " TestBed");
        version.setAlignmentX(CENTER_ALIGNMENT);
        pnlAbout.add(version);
        JTextPane text = new JTextPane();
        text.setEditable(false);
        try {
            text.setPage(this.getClass().getResource("/description.html"));
        } catch (IOException e) {
            text.setText("");
        }
        text.addHyperlinkListener(new HyperlinkListener() {

            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    if (Desktop.isDesktopSupported()) {
                        Desktop desktop = Desktop.getDesktop();
                        if (desktop.isSupported(Desktop.Action.BROWSE)) {
                            try {
                                URI uri = e.getURL().toURI();
                                desktop.browse(uri);
                            } catch (URISyntaxException ex) {
                                System.err.println("A link in the description.html is not correct: " + e.getURL());
                            } catch (IOException ex) {
                                System.err.println("Cannot navigate to link since a default program is not set or does not exist.");
                            }
                        }
                    }
                }
            }
        });
        JScrollPane scroller = new JScrollPane(text);
        pnlAbout.add(scroller);
        panel.add(pnlAbout);
        return panel;
    }

    /**
	 * Adds all the test specific controls to the given panel.
	 * @param panel the panel to add the test controls to
	 * @param controls the test specific controls
	 */
    private void addTestControls(JPanel panel, String[][] controls) {
        Dimension size = new Dimension(150, 15);
        if (controls.length > 0) {
            panel.setLayout(new SpringLayout());
            for (String[] control : controls) {
                JLabel label = new JLabel(control[0], this.helpIcon, JLabel.LEFT);
                label.setToolTipText(control[1]);
                label.setPreferredSize(size);
                label.setMaximumSize(size);
                JLabel value = new JLabel(control[2]);
                panel.add(label);
                panel.add(value);
            }
            makeCompactGrid(panel, controls.length, 2, 0, 0, 4, 4);
        } else {
            panel.setLayout(new GridBagLayout());
            panel.setMaximumSize(null);
            panel.setPreferredSize(null);
        }
    }

    /**
	 * A main method for testing the settings frame stand alone.
	 * @param args command line arguments - none
	 */
    public static void main(String[] args) {
        ControlPanel sf;
        try {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException e) {
            } catch (InstantiationException e) {
            } catch (IllegalAccessException e) {
            } catch (UnsupportedLookAndFeelException e) {
            }
            sf = new ControlPanel();
            sf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            sf.setVisible(true);
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }

    /**
	 * Custom cell renderer for the colors tab.
	 * @author William Bittle
	 * @version 2.1.0
	 * @since 2.1.0
	 */
    private class ColorListCellRenderer extends JPanel implements ListCellRenderer {

        /** The serializable id */
        private static final long serialVersionUID = -1423848677893410295L;

        /** The label that shows the color */
        private JLabel label = new JLabel();

        /**
		 * Default constructor.
		 */
        public ColorListCellRenderer() {
            Dimension size = new Dimension(30, 20);
            this.setPreferredSize(size);
            GridLayout gl = new GridLayout(1, 1);
            gl.setHgap(0);
            gl.setVgap(0);
            this.setLayout(gl);
            this.add(label);
            label.setSize(size);
            label.setPreferredSize(size);
            label.setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            float[] color = (float[]) value;
            label.setBackground(new Color(color[0], color[1], color[2]));
            return this;
        }
    }

    /**
	 * Number model that performs multiplication/division instead of addition/subtraction
	 * @author William Bittle
	 * @version 1.0.3
	 * @since 1.0.0
	 */
    private class MultiplicativeSpinnerNumberModel extends AbstractSpinnerModel {

        /** The current/initial value */
        private double value;

        /** The minimum value */
        private double minimum;

        /** The maximum value */
        private double maximum;

        /** The increment */
        private double increment;

        /**
		 * Full constructor.
		 * @param value the initial value
		 * @param minimum the minimum value
		 * @param maximum the maximum value
		 * @param increment the increment amount
		 */
        public MultiplicativeSpinnerNumberModel(double value, double minimum, double maximum, double increment) {
            super();
            this.value = value;
            this.minimum = minimum;
            this.maximum = maximum;
            this.increment = increment;
        }

        @Override
        public Object getNextValue() {
            if (this.value < this.maximum) {
                return this.value * this.increment;
            }
            return this.value;
        }

        @Override
        public Object getPreviousValue() {
            if (this.value > this.minimum) {
                return this.value / this.increment;
            }
            return this.value;
        }

        @Override
        public Double getValue() {
            return this.value;
        }

        @Override
        public void setValue(Object value) {
            if ((value == null) || !(value instanceof Double)) {
            }
            if (!value.equals(this.value)) {
                this.value = (Double) value;
                fireStateChanged();
            }
        }

        /**
		 * Returns the minimum value allowed.
		 * @return double
		 */
        public double getMinimum() {
            return minimum;
        }

        /**
		 * Returns the maximum value allowed.
		 * @return double
		 */
        public double getMaximum() {
            return maximum;
        }
    }

    /**
	 * Spinner editor for the {@link MultiplicativeSpinnerNumberModel}.
	 * @author William Bittle
	 * @version 1.0.3
	 * @since 1.0.0
	 */
    private class MultiplicativeSpinnerModelEditor extends JFormattedTextField implements ChangeListener, PropertyChangeListener {

        /** The serializable id */
        private static final long serialVersionUID = -7174664815660393176L;

        /** The spinner model */
        private MultiplicativeSpinnerNumberModel model;

        /**
		 * Full constructor.
		 * @param spinner the spinner to attach to
		 */
        public MultiplicativeSpinnerModelEditor(JSpinner spinner) {
            super(new DecimalFormat("0E0"));
            this.model = (MultiplicativeSpinnerNumberModel) (spinner.getModel());
            this.setValue(this.model.getValue());
            this.setEditable(false);
            this.setColumns(4);
            spinner.addChangeListener(this);
            this.setColumns(4);
            this.setHorizontalAlignment(JTextField.RIGHT);
            this.addPropertyChangeListener("value", this);
        }

        public void stateChanged(ChangeEvent e) {
            JSpinner spinner = (JSpinner) (e.getSource());
            MultiplicativeSpinnerNumberModel model = (MultiplicativeSpinnerNumberModel) (spinner.getModel());
            this.setValue(model.getValue());
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            double max = this.model.getMaximum();
            double min = this.model.getMinimum();
            double value = Double.parseDouble(this.getText());
            if (value >= min && value <= max) {
                this.model.setValue(value);
            } else {
                this.setValue(this.model.getValue());
            }
        }
    }

    /**
     * Method copied from the SpringLayout guide on Oracle's website from the
     * SprintUtilities class.
     * @param parent the component to layout
     * @param rows number of rows
     * @param cols number of columns
     * @param initialX x location to start the grid at
     * @param initialY y location to start the grid at
     * @param xPad x padding between cells
     * @param yPad y padding between cells
     */
    public static void makeCompactGrid(Container parent, int rows, int cols, int initialX, int initialY, int xPad, int yPad) {
        SpringLayout layout;
        try {
            layout = (SpringLayout) parent.getLayout();
        } catch (ClassCastException exc) {
            System.err.println("The first argument to makeCompactGrid must use SpringLayout.");
            return;
        }
        Spring x = Spring.constant(initialX);
        for (int c = 0; c < cols; c++) {
            Spring width = Spring.constant(0);
            for (int r = 0; r < rows; r++) {
                width = Spring.max(width, getConstraintsForCell(r, c, parent, cols).getWidth());
            }
            for (int r = 0; r < rows; r++) {
                SpringLayout.Constraints constraints = getConstraintsForCell(r, c, parent, cols);
                constraints.setX(x);
                constraints.setWidth(width);
            }
            x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
        }
        Spring y = Spring.constant(initialY);
        for (int r = 0; r < rows; r++) {
            Spring height = Spring.constant(0);
            for (int c = 0; c < cols; c++) {
                height = Spring.max(height, getConstraintsForCell(r, c, parent, cols).getHeight());
            }
            for (int c = 0; c < cols; c++) {
                SpringLayout.Constraints constraints = getConstraintsForCell(r, c, parent, cols);
                constraints.setY(y);
                constraints.setHeight(height);
            }
            y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
        }
        SpringLayout.Constraints pCons = layout.getConstraints(parent);
        pCons.setConstraint(SpringLayout.SOUTH, y);
        pCons.setConstraint(SpringLayout.EAST, x);
    }

    /**
     * Method used by the {@link #makeCompactGrid(Container, int, int, int, int, int, int)}
     * method.
     * @param row the row
     * @param col the column
     * @param parent the parent
     * @param cols the number of columns
     * @return SpringLayout.Constraints
     */
    private static SpringLayout.Constraints getConstraintsForCell(int row, int col, Container parent, int cols) {
        SpringLayout layout = (SpringLayout) parent.getLayout();
        Component c = parent.getComponent(row * cols + col);
        return layout.getConstraints(c);
    }
}
