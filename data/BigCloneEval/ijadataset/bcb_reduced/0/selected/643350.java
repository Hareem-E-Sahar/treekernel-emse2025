package uk.ac.imperial.ma.metric.explorations.calculus.differentiation;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;
import java.awt.Color;
import uk.ac.imperial.ma.metric.plotting.*;
import uk.ac.imperial.ma.metric.parsing.*;
import uk.ac.imperial.ma.metric.util.MathsStyleHelper;
import uk.ac.imperial.ma.metric.gui.*;
import uk.ac.imperial.ma.metric.explorations.ExplorationInterface;

/**
 *
 *
 * @author Phil Ramsden
 * @author Daniel J. R. May
 * @version 0.2.0 26 March 2004
 */
public class NewSecantExploration extends JPanel implements ExplorationInterface, ActionListener, KeyListener, CaretListener, ItemListener, MathPainterPanelListener {

    private static final ExtendedGridBagLayout mainPanelLayout = new ExtendedGridBagLayout();

    private static final ExtendedGridBagLayout contentSettingsPanelLayout = new ExtendedGridBagLayout();

    private static final ExtendedGridBagLayout windowSettingsPanelLayout = new ExtendedGridBagLayout();

    private static final ExtendedGridBagLayout controlPanelLayout = new ExtendedGridBagLayout();

    private static final ExtendedGridBagLayout stylePanelLayout = new ExtendedGridBagLayout();

    ClickableMathPainterPanel graphicsPanel;

    MathPainter mathPainter;

    GridPlotter gridPlotter;

    AxesPlotter axesPlotter;

    CoordGenerator coordGenerator;

    CurvePlotter curvePlotter;

    PointPlotter pointPlotter;

    JPanel contentSettingsPanel = new JPanel(true);

    JPanel windowSettingsPanel = new JPanel(true);

    JPanel controlPanel = new JPanel(true);

    JFrame styleFrame = new JFrame();

    JPanel stylePanel = new JPanel(true);

    Dimension imageDimension;

    private static final int TEXT_SIZE = 16;

    private static final int MATH_SIZE = 18;

    private static final Font LABEL_FONT = new Font("SansSerif", Font.BOLD, TEXT_SIZE - 2);

    private static final Font FIELD_FONT = new Font("SansSerif", Font.PLAIN, TEXT_SIZE);

    protected int currentTextSize;

    protected int currentMathSize;

    protected Font currentLabelFont;

    protected Font currentFieldFont;

    String[] parseVariables = { "x" };

    Color[] defaultColorWheel = { Color.BLACK, Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE, Color.MAGENTA, Color.CYAN };

    int defaultColorWheelSize = 7;

    int defaultColorIndex = 0;

    int plotIndex = 1;

    JLabel funcLbl = new JLabel("f(x) = ");

    JTextField funcTFd = new JTextField("x^2");

    JEditorPane funcJEP;

    JCheckBox inheritRangesCBx = new JCheckBox("inherit ranges", true);

    JLabel xMinLbl = new JLabel("xMin");

    JTextField xMinTFd = new JTextField("-3.0        ");

    JLabel xMaxLbl = new JLabel("xMax");

    JTextField xMaxTFd = new JTextField("3.0        ");

    JLabel xNMeshLbl = new JLabel("number of points");

    JTextField xNMeshTFd = new JTextField("40");

    JButton localStylesBtn = new JButton("Plot Styles...");

    JLabel pXLbl = new JLabel("p: x");

    JTextField pXTFd = new JTextField();

    JLabel qXLbl = new JLabel("q: x");

    JTextField qXTFd = new JTextField();

    JLabel pYLbl = new JLabel("y");

    JTextField pYTFd = new JTextField();

    JLabel qYLbl = new JLabel("y");

    JTextField qYTFd = new JTextField();

    JLabel gradientLbl = new JLabel("gradt of secant:");

    JTextField gradientTFd = new JTextField();

    JCheckBox drawTangentCBx = new JCheckBox("draw tgt?", false);

    JFrame plotStyleFrame;

    JPanel plotStylePanel;

    JLabel plotLabelLbl = new JLabel("Plot label");

    JTextField plotLabelTFd = new JTextField("Plot");

    JCheckBox lineCBx = new JCheckBox("show line", true);

    JCheckBox pointCBx = new JCheckBox("show points", false);

    SelectColorButton plotColorBtn = new SelectColorButton(Color.BLACK, "Plot Colour...");

    SelectColorButton secantColorBtn = new SelectColorButton(Color.RED, "Secant Colour...");

    SelectColorButton tangentColorBtn = new SelectColorButton(Color.GREEN, "Tangent Colour...");

    String plotLabel;

    Color plotColor = Color.BLACK;

    boolean settingsAltered;

    boolean inheritRanges = true;

    double pX;

    double qX;

    double pY;

    double qY;

    double gradient;

    double tangentGradient;

    int userPX;

    int userPY;

    int userQX;

    int userQY;

    boolean pDrawn = false;

    boolean qDrawn = false;

    short dragOption = 0;

    JLabel windowXMinLbl = new JLabel("xMin");

    JTextField windowXMinTFd = new JTextField("-3.0        ");

    JLabel windowXMaxLbl = new JLabel("xMax");

    JTextField windowXMaxTFd = new JTextField("3.0        ");

    JLabel windowYMinLbl = new JLabel("yMin");

    JTextField windowYMinTFd = new JTextField("-2.0        ");

    JLabel windowYMaxLbl = new JLabel("yMax");

    JTextField windowYMaxTFd = new JTextField("10.0        ");

    JButton globalStylesBtn = new JButton("Global Styles...");

    private double windowXMin;

    private double windowXMax;

    private double windowYMin;

    private double windowYMax;

    JButton eraseCurvesBtn = new JButton("Erase");

    JButton drawCurvesBtn = new JButton("Draw");

    JButton autoscaleBtn = new JButton("Scale");

    JButton zoomInBtn = new JButton("Zoom In");

    JButton zoomOutBtn = new JButton("Zoom Out");

    JButton textPlusBtn = new JButton("   Text+   ");

    JButton textMinusBtn = new JButton("   Text-   ");

    public NewSecantExploration() {
        super(true);
        currentTextSize = TEXT_SIZE;
        currentMathSize = MATH_SIZE;
        currentLabelFont = LABEL_FONT;
        currentFieldFont = FIELD_FONT;
        graphicsPanel = new ClickableMathPainterPanel();
        graphicsPanel.addMathPainterPanelListener(this);
        contentSettingsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED), "Content Settings", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, LABEL_FONT));
        contentSettingsPanel.setLayout(contentSettingsPanelLayout);
        try {
            this.funcJEP = new JEditorPane("text/html", MathsStyleHelper.getStyledHTML(TreeFormatter.format2D("x^2"), TEXT_SIZE, MATH_SIZE));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        plotLabelLbl.setFont(LABEL_FONT);
        plotLabelTFd.setFont(FIELD_FONT);
        lineCBx.setFont(LABEL_FONT);
        pointCBx.setFont(LABEL_FONT);
        plotColorBtn.setButtonFont(LABEL_FONT);
        secantColorBtn.setButtonFont(LABEL_FONT);
        tangentColorBtn.setButtonFont(LABEL_FONT);
        funcLbl.setFont(LABEL_FONT);
        inheritRangesCBx.setFont(LABEL_FONT);
        xMinLbl.setFont(LABEL_FONT);
        xMaxLbl.setFont(LABEL_FONT);
        xNMeshLbl.setFont(LABEL_FONT);
        localStylesBtn.setFont(LABEL_FONT);
        pXLbl.setFont(LABEL_FONT);
        qXLbl.setFont(LABEL_FONT);
        pYLbl.setFont(LABEL_FONT);
        qYLbl.setFont(LABEL_FONT);
        gradientLbl.setFont(LABEL_FONT);
        drawTangentCBx.setFont(LABEL_FONT);
        funcTFd.setFont(FIELD_FONT);
        xMinTFd.setFont(FIELD_FONT);
        xMaxTFd.setFont(FIELD_FONT);
        xNMeshTFd.setFont(FIELD_FONT);
        pXTFd.setFont(FIELD_FONT);
        qXTFd.setFont(FIELD_FONT);
        pYTFd.setFont(FIELD_FONT);
        qYTFd.setFont(FIELD_FONT);
        gradientTFd.setFont(FIELD_FONT);
        funcTFd.addCaretListener(this);
        funcTFd.addKeyListener(this);
        xMinTFd.addKeyListener(this);
        xMaxTFd.addKeyListener(this);
        xNMeshTFd.addKeyListener(this);
        pXTFd.addKeyListener(this);
        qXTFd.addKeyListener(this);
        pYTFd.addKeyListener(this);
        qYTFd.addKeyListener(this);
        plotLabelTFd.addKeyListener(this);
        inheritRangesCBx.addItemListener(this);
        drawTangentCBx.addItemListener(this);
        localStylesBtn.addActionListener(this);
        xMinTFd.setEditable(false);
        xMaxTFd.setEditable(false);
        pYTFd.setEditable(false);
        qYTFd.setEditable(false);
        gradientTFd.setEditable(false);
        contentSettingsPanelLayout.add(funcLbl, contentSettingsPanel, 0, 0, 1, 1, 100, 100, ExtendedGridBagLayout.NONE, ExtendedGridBagLayout.EAST);
        contentSettingsPanelLayout.add(funcTFd, contentSettingsPanel, 1, 0, 3, 1, 100, 100, ExtendedGridBagLayout.HORIZONTAL, ExtendedGridBagLayout.CENTER);
        contentSettingsPanelLayout.add(funcJEP, contentSettingsPanel, 1, 1, 3, 1, 100, 100, ExtendedGridBagLayout.BOTH, ExtendedGridBagLayout.CENTER);
        contentSettingsPanelLayout.add(inheritRangesCBx, contentSettingsPanel, 1, 2, 3, 1, 100, 100, ExtendedGridBagLayout.BOTH, ExtendedGridBagLayout.CENTER);
        contentSettingsPanelLayout.add(xMinLbl, contentSettingsPanel, 0, 3, 1, 1, 100, 100, ExtendedGridBagLayout.NONE, ExtendedGridBagLayout.EAST);
        contentSettingsPanelLayout.add(xMinTFd, contentSettingsPanel, 1, 3, 1, 1, 100, 100, ExtendedGridBagLayout.HORIZONTAL, ExtendedGridBagLayout.CENTER);
        contentSettingsPanelLayout.add(xMaxLbl, contentSettingsPanel, 2, 3, 1, 1, 100, 100, ExtendedGridBagLayout.NONE, ExtendedGridBagLayout.EAST);
        contentSettingsPanelLayout.add(xMaxTFd, contentSettingsPanel, 3, 3, 1, 1, 100, 100, ExtendedGridBagLayout.HORIZONTAL, ExtendedGridBagLayout.CENTER);
        contentSettingsPanelLayout.add(xNMeshLbl, contentSettingsPanel, 0, 4, 1, 1, 100, 100, ExtendedGridBagLayout.NONE, ExtendedGridBagLayout.EAST);
        contentSettingsPanelLayout.add(xNMeshTFd, contentSettingsPanel, 1, 4, 1, 1, 100, 100, ExtendedGridBagLayout.HORIZONTAL, ExtendedGridBagLayout.CENTER);
        contentSettingsPanelLayout.add(pXLbl, contentSettingsPanel, 0, 5, 1, 1, 100, 100, ExtendedGridBagLayout.NONE, ExtendedGridBagLayout.EAST);
        contentSettingsPanelLayout.add(pXTFd, contentSettingsPanel, 1, 5, 1, 1, 100, 100, ExtendedGridBagLayout.HORIZONTAL, ExtendedGridBagLayout.CENTER);
        contentSettingsPanelLayout.add(pYLbl, contentSettingsPanel, 2, 5, 1, 1, 100, 100, ExtendedGridBagLayout.NONE, ExtendedGridBagLayout.EAST);
        contentSettingsPanelLayout.add(pYTFd, contentSettingsPanel, 3, 5, 1, 1, 100, 100, ExtendedGridBagLayout.HORIZONTAL, ExtendedGridBagLayout.CENTER);
        contentSettingsPanelLayout.add(qXLbl, contentSettingsPanel, 0, 6, 1, 1, 100, 100, ExtendedGridBagLayout.NONE, ExtendedGridBagLayout.EAST);
        contentSettingsPanelLayout.add(qXTFd, contentSettingsPanel, 1, 6, 1, 1, 100, 100, ExtendedGridBagLayout.HORIZONTAL, ExtendedGridBagLayout.CENTER);
        contentSettingsPanelLayout.add(qYLbl, contentSettingsPanel, 2, 6, 1, 1, 100, 100, ExtendedGridBagLayout.NONE, ExtendedGridBagLayout.EAST);
        contentSettingsPanelLayout.add(qYTFd, contentSettingsPanel, 3, 6, 1, 1, 100, 100, ExtendedGridBagLayout.HORIZONTAL, ExtendedGridBagLayout.CENTER);
        contentSettingsPanelLayout.add(gradientLbl, contentSettingsPanel, 0, 7, 3, 1, 100, 100, ExtendedGridBagLayout.NONE, ExtendedGridBagLayout.EAST);
        contentSettingsPanelLayout.add(gradientTFd, contentSettingsPanel, 3, 7, 1, 1, 100, 100, ExtendedGridBagLayout.HORIZONTAL, ExtendedGridBagLayout.CENTER);
        contentSettingsPanelLayout.add(drawTangentCBx, contentSettingsPanel, 2, 8, 2, 1, 100, 100, ExtendedGridBagLayout.HORIZONTAL, ExtendedGridBagLayout.CENTER);
        contentSettingsPanelLayout.add(localStylesBtn, contentSettingsPanel, 1, 9, 2, 1, 100, 100, ExtendedGridBagLayout.HORIZONTAL, ExtendedGridBagLayout.CENTER);
        windowSettingsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED), "Visual Settings", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, LABEL_FONT));
        windowSettingsPanel.setLayout(windowSettingsPanelLayout);
        windowXMinLbl.setFont(LABEL_FONT);
        windowXMaxLbl.setFont(LABEL_FONT);
        windowYMinLbl.setFont(LABEL_FONT);
        windowYMaxLbl.setFont(LABEL_FONT);
        globalStylesBtn.setFont(LABEL_FONT);
        windowXMinTFd.setFont(FIELD_FONT);
        windowXMaxTFd.setFont(FIELD_FONT);
        windowYMinTFd.setFont(FIELD_FONT);
        windowYMaxTFd.setFont(FIELD_FONT);
        windowSettingsPanelLayout.add(windowXMinLbl, windowSettingsPanel, 0, 0, 1, 1, 100, 100, ExtendedGridBagLayout.NONE, ExtendedGridBagLayout.EAST);
        windowSettingsPanelLayout.add(windowXMinTFd, windowSettingsPanel, 1, 0, 1, 1, 100, 100, ExtendedGridBagLayout.HORIZONTAL, ExtendedGridBagLayout.CENTER);
        windowSettingsPanelLayout.add(windowXMaxLbl, windowSettingsPanel, 2, 0, 1, 1, 100, 100, ExtendedGridBagLayout.NONE, ExtendedGridBagLayout.EAST);
        windowSettingsPanelLayout.add(windowXMaxTFd, windowSettingsPanel, 3, 0, 1, 1, 100, 100, ExtendedGridBagLayout.HORIZONTAL, ExtendedGridBagLayout.CENTER);
        windowSettingsPanelLayout.add(windowYMinLbl, windowSettingsPanel, 0, 1, 1, 1, 100, 100, ExtendedGridBagLayout.NONE, ExtendedGridBagLayout.EAST);
        windowSettingsPanelLayout.add(windowYMinTFd, windowSettingsPanel, 1, 1, 1, 1, 100, 100, ExtendedGridBagLayout.HORIZONTAL, ExtendedGridBagLayout.CENTER);
        windowSettingsPanelLayout.add(windowYMaxLbl, windowSettingsPanel, 2, 1, 1, 1, 100, 100, ExtendedGridBagLayout.NONE, ExtendedGridBagLayout.EAST);
        windowSettingsPanelLayout.add(windowYMaxTFd, windowSettingsPanel, 3, 1, 1, 1, 100, 100, ExtendedGridBagLayout.HORIZONTAL, ExtendedGridBagLayout.CENTER);
        windowSettingsPanelLayout.add(globalStylesBtn, windowSettingsPanel, 1, 2, 2, 1, 100, 100, ExtendedGridBagLayout.HORIZONTAL, ExtendedGridBagLayout.CENTER);
        windowXMinTFd.addKeyListener(this);
        windowXMaxTFd.addKeyListener(this);
        windowYMinTFd.addKeyListener(this);
        windowYMaxTFd.addKeyListener(this);
        globalStylesBtn.addActionListener(this);
        controlPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED), "Controls", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, LABEL_FONT));
        controlPanel.setLayout(controlPanelLayout);
        drawCurvesBtn.setFont(LABEL_FONT);
        eraseCurvesBtn.setFont(LABEL_FONT);
        autoscaleBtn.setFont(LABEL_FONT);
        zoomInBtn.setFont(LABEL_FONT);
        zoomOutBtn.setFont(LABEL_FONT);
        controlPanelLayout.add(drawCurvesBtn, controlPanel, 0, 0, 1, 1, 100, 100, ExtendedGridBagLayout.HORIZONTAL, ExtendedGridBagLayout.CENTER);
        controlPanelLayout.add(eraseCurvesBtn, controlPanel, 1, 0, 1, 1, 100, 100, ExtendedGridBagLayout.HORIZONTAL, ExtendedGridBagLayout.CENTER);
        controlPanelLayout.add(autoscaleBtn, controlPanel, 2, 0, 1, 1, 100, 100, ExtendedGridBagLayout.HORIZONTAL, ExtendedGridBagLayout.CENTER);
        controlPanelLayout.add(zoomInBtn, controlPanel, 3, 0, 1, 1, 100, 100, ExtendedGridBagLayout.HORIZONTAL, ExtendedGridBagLayout.CENTER);
        controlPanelLayout.add(zoomOutBtn, controlPanel, 4, 0, 1, 1, 100, 100, ExtendedGridBagLayout.HORIZONTAL, ExtendedGridBagLayout.CENTER);
        drawCurvesBtn.addActionListener(this);
        eraseCurvesBtn.addActionListener(this);
        autoscaleBtn.addActionListener(this);
        zoomInBtn.addActionListener(this);
        zoomOutBtn.addActionListener(this);
        this.setLayout(mainPanelLayout);
        mainPanelLayout.add(contentSettingsPanel, this, 0, 0, 1, 1, 10, 100, ExtendedGridBagLayout.BOTH, ExtendedGridBagLayout.CENTER);
        mainPanelLayout.add(windowSettingsPanel, this, 0, 1, 1, 1, 10, 10, ExtendedGridBagLayout.BOTH, ExtendedGridBagLayout.CENTER);
        mainPanelLayout.add(graphicsPanel, this, 1, 0, 1, 1, 100, 100, ExtendedGridBagLayout.BOTH, ExtendedGridBagLayout.CENTER);
        mainPanelLayout.add(controlPanel, this, 1, 1, 1, 1, 100, 10, ExtendedGridBagLayout.BOTH, ExtendedGridBagLayout.CENTER);
        textPlusBtn.setFont(LABEL_FONT);
        textMinusBtn.setFont(LABEL_FONT);
        textPlusBtn.addActionListener(this);
        textMinusBtn.addActionListener(this);
    }

    public void setupGraphics() {
        this.coordGenerator = new CoordGenerator(this.mathPainter);
        this.curvePlotter = new CurvePlotter(this.mathPainter, this.coordGenerator);
        this.pointPlotter = new PointPlotter(this.mathPainter, this.coordGenerator);
    }

    public void drawPlot() {
        this.mathPainter.setPaint(plotColorBtn.getColor());
        boolean showLine = lineCBx.isSelected();
        boolean showPoints = pointCBx.isSelected();
        if (showLine) curvePlotter.plot();
        if (showPoints) pointPlotter.plot();
    }

    public void init() {
        mathPainter = graphicsPanel.init();
        this.initializeGraphics();
        setupGraphics();
        this.drawGraphPaper();
        this.drawPlots();
        this.graphicsPanel.setBase();
        drawSecant();
        this.graphicsPanel.update();
    }

    public void initializeGraphics() {
        this.gridPlotter = new GridPlotter(this.mathPainter);
        this.axesPlotter = new AxesPlotter(this.mathPainter);
        windowXMin = new Double(windowXMinTFd.getText()).doubleValue();
        windowXMax = new Double(windowXMaxTFd.getText()).doubleValue();
        windowYMin = new Double(windowYMinTFd.getText()).doubleValue();
        windowYMax = new Double(windowYMaxTFd.getText()).doubleValue();
        mathPainter.setMathArea(windowXMin, windowYMin, windowXMax - windowXMin, windowYMax - windowYMin);
        mathPainter.setScales();
        if (pDrawn) {
            this.pX = new Double(pXTFd.getText()).doubleValue();
            try {
                this.pY = coordGenerator.func(this.pX);
                this.tangentGradient = (coordGenerator.func(pX + 0.001) - coordGenerator.func(this.pX - 0.001)) / 0.002;
                userPX = mathPainter.mathToUserX(pX);
                userPY = mathPainter.mathToUserY(pY);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (qDrawn) {
            this.qX = new Double(qXTFd.getText()).doubleValue();
            try {
                this.qY = coordGenerator.func(this.qX);
                this.gradient = (qY - pY) / (qX - pX);
                userQX = mathPainter.mathToUserX(qX);
                userQY = mathPainter.mathToUserY(qY);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void drawGraphPaper() {
        mathPainter.setPaint(Color.white);
        mathPainter.fillRect(windowXMin, windowYMin, windowXMax - windowXMin, windowYMax - windowYMin);
        mathPainter.setPaint(Color.lightGray);
        gridPlotter.drawFineGrid();
        mathPainter.setPaint(Color.gray);
        gridPlotter.drawGrid();
        mathPainter.setPaint(Color.blue);
        axesPlotter.drawAxes();
        axesPlotter.drawTicks(new Font("SansSerif", Font.PLAIN, currentTextSize));
    }

    public void draw() {
        graphicsPanel.clearCompletely();
        initializeGraphics();
        drawGraphPaper();
        drawPlots();
        graphicsPanel.setBase();
        drawSecant();
        graphicsPanel.update();
    }

    public void drawPlots() {
        String funcString = this.funcTFd.getText();
        if (this.inheritRanges) {
            this.xMinTFd.setText(windowXMinTFd.getText());
            this.xMaxTFd.setText(windowXMaxTFd.getText());
        }
        double xMin = 1.000001 * (new Double(this.xMinTFd.getText())).doubleValue();
        double xMax = (new Double(this.xMaxTFd.getText())).doubleValue();
        int xNMesh = (new Integer(this.xNMeshTFd.getText())).intValue();
        try {
            this.coordGenerator.setPoints(funcString, "x", xMin, xMax - xMin, xNMesh);
        } catch (Exception ex) {
        }
        this.drawPlot();
        this.settingsAltered = false;
    }

    protected void update2DFormattingArea() {
        String funcString = funcTFd.getText();
        try {
            funcJEP.setText(MathsStyleHelper.getStyledHTML(TreeFormatter.format2D(funcString), this.currentTextSize, this.currentMathSize));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setInheritRanges(boolean inheritRanges) {
        this.inheritRanges = inheritRanges;
        xMinTFd.setEditable(!inheritRanges);
        xMaxTFd.setEditable(!inheritRanges);
        if (inheritRanges) {
            xMinTFd.setText(this.windowXMinTFd.getText());
            xMaxTFd.setText(this.windowXMaxTFd.getText());
        }
    }

    public Component getComponent() {
        return this;
    }

    public void mathPainterPanelResized() {
        System.out.println("resized");
        draw();
    }

    public void initializeSecantGraphics() {
        if (pDrawn) {
            this.pX = new Double(pXTFd.getText()).doubleValue();
            try {
                this.pY = coordGenerator.func(this.pX);
                this.tangentGradient = (coordGenerator.func(pX + 0.001) - coordGenerator.func(pX - 0.001)) / 0.002;
                userPX = mathPainter.mathToUserX(pX);
                userPY = mathPainter.mathToUserY(pY);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (qDrawn) {
            this.qX = new Double(qXTFd.getText()).doubleValue();
            try {
                this.qY = coordGenerator.func(this.qX);
                this.gradient = (qY - pY) / (qX - pX);
                userQX = mathPainter.mathToUserX(qX);
                userQY = mathPainter.mathToUserY(qY);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void caretUpdate(CaretEvent ce) {
        if (ce.getSource() == funcTFd) {
            update2DFormattingArea();
        }
    }

    public void itemStateChanged(ItemEvent ie) {
        if (ie.getSource() == inheritRangesCBx) {
            setInheritRanges(inheritRangesCBx.isSelected());
        } else this.draw();
    }

    public void keyPressed(KeyEvent ke) {
    }

    public void keyTyped(KeyEvent ke) {
    }

    public void keyReleased(KeyEvent ke) {
        if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
            if (ke.getSource() == pXTFd || ke.getSource() == qXTFd) {
                String pXText = pXTFd.getText();
                String qXText = qXTFd.getText();
                graphicsPanel.clear();
                if (!(pXText.equals(""))) {
                    pDrawn = true;
                }
                if (!(qXText.equals(""))) {
                    qDrawn = true;
                }
                initializeSecantGraphics();
                if (pDrawn) {
                    pYTFd.setText("" + Math.round(1000.0 * pY) / 1000.0);
                }
                if (qDrawn) {
                    qYTFd.setText("" + Math.round(1000.0 * qY) / 1000.0);
                }
                if (pDrawn && qDrawn) {
                    gradientTFd.setText("" + Math.round(1000.0 * gradient) / 1000.0);
                }
                drawSecant();
                graphicsPanel.update();
            } else draw();
        }
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == localStylesBtn) {
            plotStyleFrame = new JFrame("Plot Style Settings");
            plotStylePanel = new JPanel();
            plotStylePanel.setLayout(stylePanelLayout);
            stylePanelLayout.add(plotLabelLbl, plotStylePanel, 0, 0, 1, 1, 100, 100, ExtendedGridBagLayout.NONE, ExtendedGridBagLayout.CENTER);
            stylePanelLayout.add(plotLabelTFd, plotStylePanel, 1, 0, 1, 1, 100, 100, ExtendedGridBagLayout.HORIZONTAL, ExtendedGridBagLayout.CENTER);
            stylePanelLayout.add(lineCBx, plotStylePanel, 0, 2, 1, 1, 100, 100, ExtendedGridBagLayout.HORIZONTAL, ExtendedGridBagLayout.CENTER);
            stylePanelLayout.add(pointCBx, plotStylePanel, 1, 2, 1, 1, 100, 100, ExtendedGridBagLayout.HORIZONTAL, ExtendedGridBagLayout.CENTER);
            stylePanelLayout.add(plotColorBtn, plotStylePanel, 0, 3, 2, 1, 100, 100, ExtendedGridBagLayout.HORIZONTAL, ExtendedGridBagLayout.CENTER);
            stylePanelLayout.add(secantColorBtn, plotStylePanel, 0, 4, 2, 1, 100, 100, ExtendedGridBagLayout.HORIZONTAL, ExtendedGridBagLayout.CENTER);
            stylePanelLayout.add(tangentColorBtn, plotStylePanel, 0, 5, 2, 1, 100, 100, ExtendedGridBagLayout.HORIZONTAL, ExtendedGridBagLayout.CENTER);
            plotStyleFrame.getContentPane().setLayout(new GridLayout(1, 0));
            plotStyleFrame.add(plotStylePanel);
            plotStyleFrame.setSize(300, 300);
            plotStyleFrame.setVisible(true);
        } else if (ae.getSource() == eraseCurvesBtn) {
            graphicsPanel.clearCompletely();
            initializeGraphics();
            drawGraphPaper();
            graphicsPanel.setBase();
            drawSecant();
            graphicsPanel.update();
        } else if (ae.getSource() == drawCurvesBtn) {
            draw();
        } else if (ae.getSource() == zoomInBtn && pDrawn) {
            double newXMin = (windowXMin + pX) / 2;
            double newXMax = (windowXMax + pX) / 2;
            double newYMin = (windowYMin + pY) / 2;
            double newYMax = (windowYMax + pY) / 2;
            newXMin = Math.round(1000.0 * newXMin) / 1000.0;
            newXMax = Math.round(1000.0 * newXMax) / 1000.0;
            newYMin = Math.round(1000.0 * newYMin) / 1000.0;
            newYMax = Math.round(1000.0 * newYMax) / 1000.0;
            windowXMinTFd.setText("" + newXMin);
            windowXMaxTFd.setText("" + newXMax);
            windowYMinTFd.setText("" + newYMin);
            windowYMaxTFd.setText("" + newYMax);
            if (inheritRangesCBx.isSelected()) {
                xMinTFd.setText("" + newXMin);
                xMaxTFd.setText("" + newXMax);
            }
            draw();
        } else if (ae.getSource() == zoomOutBtn && pDrawn) {
            double newXMin = 2 * windowXMin - pX;
            double newXMax = 2 * windowXMax - pX;
            double newYMin = 2 * windowYMin - pY;
            double newYMax = 2 * windowYMax - pY;
            newXMin = Math.round(1000.0 * newXMin) / 1000.0;
            newXMax = Math.round(1000.0 * newXMax) / 1000.0;
            newYMin = Math.round(1000.0 * newYMin) / 1000.0;
            newYMax = Math.round(1000.0 * newYMax) / 1000.0;
            windowXMinTFd.setText("" + newXMin);
            windowXMaxTFd.setText("" + newXMax);
            windowYMinTFd.setText("" + newYMin);
            windowYMaxTFd.setText("" + newYMax);
            if (inheritRangesCBx.isSelected()) {
                xMinTFd.setText("" + newXMin);
                xMaxTFd.setText("" + newXMax);
            }
            draw();
        } else if (ae.getSource() == globalStylesBtn) {
            styleFrame = new JFrame("Global Style Settings");
            stylePanel = new JPanel();
            stylePanel.setLayout(stylePanelLayout);
            stylePanelLayout.add(textPlusBtn, stylePanel, 0, 0, 1, 1, 100, 100, ExtendedGridBagLayout.NONE, ExtendedGridBagLayout.CENTER);
            stylePanelLayout.add(textMinusBtn, stylePanel, 1, 0, 1, 1, 100, 100, ExtendedGridBagLayout.NONE, ExtendedGridBagLayout.CENTER);
            styleFrame.getContentPane().setLayout(new GridLayout(1, 0));
            styleFrame.add(stylePanel);
            styleFrame.setSize(300, 300);
            styleFrame.setVisible(true);
        } else if (ae.getSource() == textPlusBtn) {
            currentTextSize++;
            currentMathSize++;
            updateFonts();
        } else if (ae.getSource() == textMinusBtn) {
            currentTextSize--;
            currentMathSize--;
            updateFonts();
        } else if (ae.getSource() == autoscaleBtn) {
            System.out.println("autoscale button pressed");
            this.coordGenerator.autoScale(MathCoords.Y_AXIS);
            double newXMin = this.mathPainter.getXMin();
            double newXMax = newXMin + this.mathPainter.getXRange();
            double newYMin = this.mathPainter.getYMin();
            double newYMax = newYMin + this.mathPainter.getYRange();
            newXMin = Math.round(1000.0 * newXMin) / 1000.0;
            newXMax = Math.round(1000.0 * newXMax) / 1000.0;
            newYMin = Math.round(1000.0 * newYMin) / 1000.0;
            newYMax = Math.round(1000.0 * newYMax) / 1000.0;
            windowXMinTFd.setText("" + newXMin);
            windowXMaxTFd.setText("" + newXMax);
            windowYMinTFd.setText("" + newYMin);
            windowYMaxTFd.setText("" + newYMax);
            draw();
        } else {
            try {
                draw();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void drawSecant() {
        mathPainter.setPaint(secantColorBtn.getColor());
        if (pDrawn) {
            mathPainter.fillCircle(pX, pY);
            if (this.drawTangentCBx.isSelected()) {
                mathPainter.setPaint(tangentColorBtn.getColor());
                mathPainter.drawWholeLine(pX, pY, tangentGradient);
                mathPainter.setPaint(secantColorBtn.getColor());
            }
        }
        if (qDrawn) {
            mathPainter.drawCircle(qX, qY);
            mathPainter.drawWholeLine(pX, pY, qX, qY);
            mathPainter.setPaint(secantColorBtn.getColor());
            mathPainter.drawLine(pX, pY, qX, pY);
            mathPainter.drawLine(qX, qY, qX, pY);
        }
    }

    public void updateFonts() {
        currentLabelFont = new Font("SansSerif", Font.BOLD, currentTextSize - 2);
        currentFieldFont = new Font("SansSerif", Font.PLAIN, currentTextSize);
        contentSettingsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED), "Content Settings", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, this.currentLabelFont));
        update2DFormattingArea();
        funcLbl.setFont(this.currentLabelFont);
        inheritRangesCBx.setFont(this.currentLabelFont);
        xMinLbl.setFont(this.currentLabelFont);
        xMaxLbl.setFont(this.currentLabelFont);
        xNMeshLbl.setFont(this.currentLabelFont);
        pXLbl.setFont(this.currentLabelFont);
        pYLbl.setFont(this.currentLabelFont);
        qXLbl.setFont(this.currentLabelFont);
        qYLbl.setFont(this.currentLabelFont);
        gradientLbl.setFont(this.currentLabelFont);
        localStylesBtn.setFont(this.currentLabelFont);
        drawTangentCBx.setFont(this.currentLabelFont);
        funcTFd.setFont(this.currentFieldFont);
        xMinTFd.setFont(this.currentFieldFont);
        xMaxTFd.setFont(this.currentFieldFont);
        xNMeshTFd.setFont(this.currentFieldFont);
        pXTFd.setFont(this.currentFieldFont);
        pYTFd.setFont(this.currentFieldFont);
        qXTFd.setFont(this.currentFieldFont);
        qYTFd.setFont(this.currentFieldFont);
        gradientTFd.setFont(this.currentFieldFont);
        plotLabelLbl.setFont(this.currentLabelFont);
        plotLabelTFd.setFont(this.currentFieldFont);
        lineCBx.setFont(this.currentLabelFont);
        pointCBx.setFont(this.currentLabelFont);
        plotColorBtn.setButtonFont(this.currentLabelFont);
        secantColorBtn.setButtonFont(this.currentLabelFont);
        tangentColorBtn.setButtonFont(this.currentLabelFont);
        windowSettingsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED), "Visual Settings", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, currentLabelFont));
        windowXMinLbl.setFont(currentLabelFont);
        windowXMaxLbl.setFont(currentLabelFont);
        windowYMinLbl.setFont(currentLabelFont);
        windowYMaxLbl.setFont(currentLabelFont);
        globalStylesBtn.setFont(currentLabelFont);
        windowXMinTFd.setFont(currentFieldFont);
        windowXMaxTFd.setFont(currentFieldFont);
        windowYMinTFd.setFont(currentFieldFont);
        windowYMaxTFd.setFont(currentFieldFont);
        controlPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED), "Controls", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, currentLabelFont));
        drawCurvesBtn.setFont(currentLabelFont);
        eraseCurvesBtn.setFont(currentLabelFont);
        autoscaleBtn.setFont(currentLabelFont);
        zoomInBtn.setFont(currentLabelFont);
        zoomOutBtn.setFont(currentLabelFont);
        textPlusBtn.setFont(currentLabelFont);
        textMinusBtn.setFont(currentLabelFont);
        graphicsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED), " Plots", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, currentLabelFont));
    }

    public void mathPainterPanelAction(MathPainterPanelEvent mppe) {
        MouseEvent me = mppe.getMouseEvent();
        int eventType = me.getID();
        if (eventType == MouseEvent.MOUSE_CLICKED) mouseClickedAction(mppe); else if (eventType == MouseEvent.MOUSE_PRESSED) mousePressedAction(mppe); else if (eventType == MouseEvent.MOUSE_RELEASED) mouseReleasedAction(mppe); else if (eventType == MouseEvent.MOUSE_DRAGGED) mouseDraggedAction(mppe);
    }

    public void mouseClickedAction(MathPainterPanelEvent mppe) {
        if (!pDrawn) {
            double newpX = mppe.getMathSpaceX();
            newpX = Math.round(1000.0 * newpX) / 1000.0;
            pXTFd.setText("" + newpX);
            pDrawn = true;
            graphicsPanel.clear();
            initializeSecantGraphics();
            pYTFd.setText("" + Math.round(1000.0 * pY) / 1000.0);
            drawSecant();
            graphicsPanel.update();
        } else if (!qDrawn) {
            double newqX = mppe.getMathSpaceX();
            newqX = Math.round(1000.0 * newqX) / 1000.0;
            qXTFd.setText("" + newqX);
            qDrawn = true;
            graphicsPanel.clear();
            initializeSecantGraphics();
            qYTFd.setText("" + Math.round(1000.0 * qY) / 1000.0);
            gradientTFd.setText("" + Math.round(1000.0 * gradient) / 1000.0);
            drawSecant();
            graphicsPanel.update();
        }
    }

    public void mousePressedAction(MathPainterPanelEvent mppe) {
        if ((Math.abs(mppe.getUserSpaceX() - userQX) <= 5) && (Math.abs(mppe.getUserSpaceY() - userQY) <= 5)) dragOption = 1; else if ((Math.abs(mppe.getUserSpaceX() - userPX) <= 5) && (Math.abs(mppe.getUserSpaceY() - userPY) <= 5)) dragOption = 2; else dragOption = 0;
    }

    public void mouseReleasedAction(MathPainterPanelEvent mppe) {
        dragOption = 0;
    }

    public void mouseDraggedAction(MathPainterPanelEvent mppe) {
        switch(dragOption) {
            case 1:
                {
                    double newqX = mppe.getMathSpaceX();
                    newqX = Math.round(1000.0 * newqX) / 1000.0;
                    qXTFd.setText("" + newqX);
                    graphicsPanel.clear();
                    initializeSecantGraphics();
                    qYTFd.setText("" + Math.round(1000.0 * qY) / 1000.0);
                    gradientTFd.setText("" + Math.round(1000.0 * gradient) / 1000.0);
                    drawSecant();
                    graphicsPanel.update();
                }
                break;
            case 2:
                {
                    double newpX = mppe.getMathSpaceX();
                    newpX = Math.round(1000.0 * newpX) / 1000.0;
                    pXTFd.setText("" + newpX);
                    graphicsPanel.clear();
                    initializeSecantGraphics();
                    pYTFd.setText("" + Math.round(1000.0 * pY) / 1000.0);
                    gradientTFd.setText("" + Math.round(1000.0 * gradient) / 1000.0);
                    drawSecant();
                    graphicsPanel.update();
                }
                break;
            default:
                {
                }
        }
    }
}
