package edu.ucla.stat.SOCR.applications.view;

import edu.ucla.stat.SOCR.applications.Application;
import edu.ucla.stat.SOCR.applications.core.AbstractOptimalPortfolio;
import edu.ucla.stat.SOCR.applications.core.BasicPortfolio;
import edu.ucla.stat.SOCR.applications.core.PortfolioSimulator;
import edu.ucla.stat.SOCR.core.IExperiment;
import edu.ucla.stat.SOCR.core.IntValueSetter;
import edu.ucla.stat.SOCR.core.ValueSetter;
import edu.ucla.stat.SOCR.util.FloatTextField;
import edu.ucla.stat.SOCR.util.Matrix;
import edu.ucla.stat.SOCR.util.RowHeaderTable;
import org.jfree.chart.*;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.Observable;
import java.util.Observer;

/**
 * Package: edu.ucla.stat.SOCR.applications.gui
 * Date: Aug 13, 2008
 * Time: 11:52:01 PM
 *
 * @author Jameel Al-Aziz
 */
public class PortfolioView extends Application implements Observer, IExperiment, ChartMouseListener, ActionListener {

    protected static final float LEFT_ALIGNMENT = 0;

    protected final int CHART_SIZE_X = 500;

    protected final int SLIDER_SIZE_X = 500;

    protected final int CHART_SIZE_Y = 400;

    protected final int MIN_NUM_STOCKS = 2;

    protected final int MAX_NUM_STOCKS = 500;

    protected final String GRAPH = "GRAPH";

    protected final String INPUT = "INPUT";

    protected final String DATA = "DATA";

    protected final String ALL = "SHOW ALL";

    protected final String COVR_SWITCH = "Covariance Matrix";

    protected final String CORR_SWITCH = "Correlation Matrix";

    protected final String[] SWITCH_ARRAY = { COVR_SWITCH, CORR_SWITCH };

    protected final String[] ON_OFF = { "On", "Off" };

    protected ChartPanel chartPanel;

    protected RowHeaderTable dataTable;

    protected JPanel graphTab;

    protected JScrollPane mixTab;

    protected JPanel inputTab, dataTab, inputPanel, leftSideBarPanel;

    protected JRadioButton[] radioButtons;

    protected JButton applyButton;

    protected JToolBar toolBar;

    protected JTextPane statusTextArea;

    protected JTabbedPane tabbedPanelContainer;

    protected IntValueSetter numStocksSetter;

    protected ValueSetter riskFreeRateSetter;

    protected ValueSetter correlationSetter;

    private FloatTextField[] expRetTextFields, covrTextFields;

    private XYSeries stockSeries, mouseSeries, tangentSeries, positiveSeries;

    private String tooltip;

    protected int numStocks = MIN_NUM_STOCKS;

    protected int numSimulate = 8000;

    protected int tabbedPaneCount = 0;

    protected double mouseX;

    protected double mouseY;

    protected double riskFreeRate;

    protected Point2D tangentPoint;

    protected boolean showStatusTextArea = false;

    protected boolean showTangent = true;

    private boolean mouseClicked = false;

    private boolean covarianceFlag = true;

    private DecimalFormat tooltipFormatter = new DecimalFormat("#0.000");

    protected PortfolioSimulator simulatedPortfolios;

    protected AbstractOptimalPortfolio optimalPortfolio;

    private double[] expectedReturns;

    private double[] variances;

    private double[][] covariances;

    public double[] r = { 0.0064, 0.0022, 0.02117, 0.01, 0.0134 };

    public double[] c = { 0.0101, 0.0122, 0.0119, 0.0141, 0.0144 };

    public double[] m = { 0.0045, 0.0041, 0.0026, 0.0012, 0.0011, 0.0015, 0.0043, 0.0022, 0.0058, 0.005 };

    private enum Model {

        COVARIANCE, CORRELATION, CONSTANT_CORRELATION, SINGLE_INDEX
    }

    private Model model;

    /**
     *
     */
    public PortfolioView() {
        setName("PortfolioExperiment");
        init();
    }

    /**
     *
     */
    @Override
    public void init() {
        tabbedPanelContainer = new JTabbedPane();
        showTangent = true;
        expectedReturns = new double[numStocks];
        variances = new double[numStocks];
        covariances = new double[numStocks][numStocks];
        model = Model.COVARIANCE;
        riskFreeRate = 0.001;
        int k = 0;
        for (int i = 0; i < numStocks; i++) {
            expectedReturns[i] = r[i];
            variances[i] = c[i];
            covariances[i][i] = c[i];
            for (int j = 0; j < i; j++) {
                covariances[i][j] = covariances[j][i] = m[k];
                k++;
            }
        }
        applyButton = new JButton("Update");
        applyButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                update();
            }
        });
        initLeftSideBar();
        initGraphTab();
        initInputTab();
        initDataTable();
        initMixTab();
        initDataTab();
        emptyTool();
        emptyTool2();
        initPortfolios();
        addTabbedPane(DATA, dataTab);
        addTabbedPane(GRAPH, graphTab);
        addTabbedPane(INPUT, inputTab);
        addTabbedPane(ALL, mixTab);
        updateChart();
        tabbedPanelContainer.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                if (tabbedPanelContainer.getTitleAt(tabbedPanelContainer.getSelectedIndex()) == ALL) {
                    updateMixTab();
                } else if (tabbedPanelContainer.getTitleAt(tabbedPanelContainer.getSelectedIndex()) == GRAPH) {
                    updateGraphTab();
                } else if (tabbedPanelContainer.getTitleAt(tabbedPanelContainer.getSelectedIndex()) == INPUT) {
                    updateInputTab();
                } else if (tabbedPanelContainer.getTitleAt(tabbedPanelContainer.getSelectedIndex()) == DATA) {
                    updateDataTab();
                }
            }
        });
        statusTextArea = new JTextPane();
        statusTextArea.setEditable(false);
        JScrollPane statusContainer = new JScrollPane(statusTextArea);
        statusContainer.setPreferredSize(new Dimension(600, 140));
        JSplitPane upContainer = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(leftSideBarPanel), new JScrollPane(tabbedPanelContainer));
        this.getMainPanel().removeAll();
        if (showStatusTextArea) {
            JSplitPane container = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(upContainer), statusContainer);
            container.setContinuousLayout(true);
            container.setDividerLocation(0.6);
            this.getMainPanel().add(container, BorderLayout.CENTER);
        } else {
            this.getMainPanel().add(new JScrollPane(upContainer), BorderLayout.CENTER);
        }
        this.getMainPanel().validate();
    }

    /**
     *
     */
    protected void initLeftSideBar() {
        leftSideBarPanel = new JPanel();
        leftSideBarPanel.setLayout(new BoxLayout(leftSideBarPanel, BoxLayout.Y_AXIS));
        numStocksSetter = new IntValueSetter("Number of Stocks:", MIN_NUM_STOCKS, MAX_NUM_STOCKS, numStocks);
        riskFreeRateSetter = new ValueSetter("Risk Free Rate", 0, 100, 10, .0001);
        riskFreeRateSetter.setFormat(new DecimalFormat("#0.0####"));
        applyButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        leftSideBarPanel.add(Box.createVerticalStrut(12));
        leftSideBarPanel.add(numStocksSetter);
        leftSideBarPanel.add(riskFreeRateSetter);
        leftSideBarPanel.add(applyButton);
        leftSideBarPanel.add(Box.createVerticalGlue());
        addRadioButtonsToSidebar("Switch Input Matrix:", "Switch Input Matrix", SWITCH_ARRAY, 0, this);
        addRadioButtonsToSidebar("Show Tangent Line:", "Show Tangent Line", ON_OFF, 0, this);
        leftSideBarPanel.add(Box.createVerticalGlue());
    }

    /**
     *
     */
    protected void initGraphTab() {
        graphTab = new JPanel();
        graphTab.setLayout(new BoxLayout(graphTab, BoxLayout.Y_AXIS));
        JFreeChart chart = createEmptyChart("SOCR Applications");
        chartPanel = new ChartPanel(chart, false);
        chartPanel.setPreferredSize(new Dimension(CHART_SIZE_X, CHART_SIZE_Y));
        graphTab.add(chartPanel);
        graphTab.validate();
    }

    /**
     *
     */
    protected void initMixTab() {
        mixTab = new JScrollPane();
        mixTab.setPreferredSize(new Dimension(600, CHART_SIZE_Y + 100));
        updateMixTab();
    }

    /**
     *
     */
    protected void initInputTab() {
        inputTab = new JPanel();
        covarianceFlag = true;
        initInputTextFields();
        updateInputTextFields();
    }

    /**
     *
     */
    protected void initDataTab() {
        dataTab = new JPanel();
        dataTab.setLayout(new BoxLayout(dataTab, BoxLayout.Y_AXIS));
        dataTable.setAlignmentX(LEFT_ALIGNMENT);
        dataTab.add(dataTable);
        dataTab.validate();
    }

    protected void initPortfolios() {
        simulatedPortfolios = new PortfolioSimulator();
    }

    private void initDataTable() {
        dataTable = new RowHeaderTable();
    }

    private void initInputTextFields() {
        inputPanel = new JPanel();
    }

    /**
     *
     */
    protected void updateDataTab() {
        dataTab.removeAll();
        dataTable.setAlignmentX(LEFT_ALIGNMENT);
        dataTab.add(dataTable);
        dataTab.validate();
    }

    /**
     *
     */
    protected void updateGraphTab() {
        graphTab.removeAll();
        chartPanel.setPreferredSize(new Dimension(CHART_SIZE_X, CHART_SIZE_Y));
        graphTab.add(chartPanel);
        graphTab.validate();
    }

    /**
     *
     */
    protected void updateInputTab() {
        inputTab.removeAll();
        updateInputTextFields();
        JScrollPane sp = new JScrollPane(inputPanel);
        inputTab.add(sp);
        inputTab.validate();
        inputTab.repaint();
    }

    /**
     *
     */
    protected void updateMixTab() {
        JPanel container = new JPanel(new BorderLayout());
        chartPanel.setPreferredSize(new Dimension(CHART_SIZE_X * 2 / 3, CHART_SIZE_Y * 2 / 3));
        container.add(chartPanel, BorderLayout.WEST);
        JPanel tableContainer = new JPanel();
        tableContainer.setLayout(new BoxLayout(tableContainer, BoxLayout.Y_AXIS));
        dataTable.setAlignmentX(LEFT_ALIGNMENT);
        tableContainer.add(dataTable);
        container.add(new JScrollPane(tableContainer), BorderLayout.CENTER);
        mixTab.setViewportView(container);
        mixTab.validate();
    }

    private void updateInputTextFields() {
        inputPanel.removeAll();
        expRetTextFields = new FloatTextField[numStocks];
        covrTextFields = new FloatTextField[15];
        JPanel expRetPanel = new JPanel();
        expRetPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel varPanel = new JPanel();
        varPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel matrixPanel = new JPanel();
        matrixPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        expRetPanel.setLayout(new GridBagLayout());
        expRetPanel.add(new JLabel("Expected Return:"));
        varPanel.setLayout(new GridBagLayout());
        varPanel.add(new JLabel("Variance:"));
        int k = 0;
        for (int i = 0; i < numStocks; i++) {
            if (expectedReturns[i] == 0) expectedReturns[i] = r[i];
            if (variances[i] == 0) variances[i] = c[i];
            if (covariances[i][i] == 0) covariances[i][i] = c[i];
            for (int j = 0; j < i; j++) {
                if (covariances[i][j] == 0) covariances[i][j] = covariances[j][i] = m[k];
                k++;
            }
        }
        for (int i = 0; i < numStocks; i++) {
            expRetTextFields[i] = new FloatTextField("E(R" + (i + 1) + ")", expectedReturns[i], -0.2, 0.2);
            expRetTextFields[i].setPreferredSize(new Dimension(SLIDER_SIZE_X / numStocks, 45));
            expRetTextFields[i].setToolTipText("Adjusting the value of expected return for stock " + (i + 1) + ".");
            newMatrixTextField(1, i, expRetTextFields[i], expRetPanel);
            covrTextFields[i] = new FloatTextField("VAR(R" + (i + 1) + ")", variances[i], 0.0, 0.2);
            covrTextFields[i].setPreferredSize(new Dimension(SLIDER_SIZE_X / numStocks, 45));
            covrTextFields[i].setToolTipText("Adjusting the value of variance " + (i + 1) + ".");
            newMatrixTextField(1, i, covrTextFields[i], varPanel);
        }
        matrixPanel.setLayout(new GridBagLayout());
        String title;
        String tooltip;
        if (covarianceFlag) {
            title = "COV";
            tooltip = "covariance";
        } else {
            title = "CORR";
            tooltip = "correlation";
        }
        k = numStocks;
        for (int i = 1; i < numStocks; i++) {
            for (int j = 0; j < i; j++) {
                covrTextFields[k] = new FloatTextField(title + (i + 1) + (j + 1), covariances[i][j], -0.5, 0.5);
                covrTextFields[k].setPreferredSize(new Dimension(SLIDER_SIZE_X / numStocks, 45));
                covrTextFields[k].setToolTipText("Adjusting the value of " + tooltip + " " + (i + 1) + "," + (j + 1) + ".");
                newMatrixTextField(i, j, covrTextFields[k], matrixPanel);
                k++;
            }
        }
        inputPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(0, 0, 10, 0);
        c.gridy = 0;
        inputPanel.add(expRetPanel, c);
        c.gridy = 1;
        inputPanel.add(varPanel, c);
        c.gridy = 2;
        inputPanel.add(matrixPanel, c);
        inputPanel.validate();
    }

    private void updatePortfolios() {
        Matrix expRetMatrix = new Matrix(numStocks, 1);
        Matrix covrMatrix = new Matrix(numStocks, numStocks, covariances);
        for (int i = 0; i < numStocks; i++) {
            expRetMatrix.element[i][0] = expectedReturns[i];
        }
        try {
            simulatedPortfolios.setPointCount(numSimulate);
            simulatedPortfolios.setNumberOfStocks(numStocks);
            simulatedPortfolios.setRiskFreeRate(riskFreeRate);
            simulatedPortfolios.setExpectedReturns(expRetMatrix);
            optimalPortfolio = new BasicPortfolio();
            optimalPortfolio.setNumberOfStocks(numStocks);
            optimalPortfolio.setRiskFreeRate(riskFreeRate);
            optimalPortfolio.setExpectedReturns(expRetMatrix);
            if (covarianceFlag) {
                simulatedPortfolios.setCovarianceMatrix(covrMatrix);
                optimalPortfolio.setCovarianceMatrix(covrMatrix);
            } else {
                simulatedPortfolios.setCorrelationMatrix(covrMatrix, variances);
                optimalPortfolio.setCorrelationMatrix(covrMatrix, variances);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     */
    public void update() {
        int oldNumStocks = numStocks;
        numStocks = numStocksSetter.getValue();
        if (oldNumStocks != numStocks) {
            updateNumStocks();
            if (numStocks > 5) {
                tabbedPanelContainer.setEnabledAt(tabbedPanelContainer.indexOfTab(INPUT), false);
                if (tabbedPanelContainer.getSelectedIndex() == tabbedPanelContainer.indexOfTab(INPUT)) {
                    tabbedPanelContainer.setSelectedIndex(tabbedPanelContainer.indexOfTab(DATA));
                }
            } else {
                tabbedPanelContainer.setEnabledAt(tabbedPanelContainer.indexOfTab(INPUT), true);
                updateInputTab();
            }
        }
        if (riskFreeRateSetter.getValue() != riskFreeRate) {
            riskFreeRate = riskFreeRateSetter.getValue();
            updateGraph();
            return;
        }
        if (numStocks <= 5) {
            for (int i = 0; i < numStocks; i++) {
                expectedReturns[i] = expRetTextFields[i].getFloatValue();
                variances[i] = covrTextFields[i].getFloatValue();
            }
            int k = numStocks;
            for (int i = 0; i < numStocks; i++) {
                covariances[i][i] = (covarianceFlag) ? variances[i] : 1;
                for (int j = 0; j < i; j++) {
                    covariances[i][j] = covariances[j][i] = covrTextFields[k].getFloatValue();
                    k++;
                }
            }
        }
        updateGraph();
    }

    private void updateChart() {
        updatePortfolios();
        stockSeries = new XYSeries("Stock", false);
        tangentSeries = new XYSeries("Tangent", false);
        mouseSeries = new XYSeries("M", false);
        positiveSeries = new XYSeries("Positives", false);
        StringBuffer text = new StringBuffer();
        text.append("mouse clicked at (Risk, Expected Return) = " + tooltip + "\n");
        for (int i = 0; i < simulatedPortfolios.getPointCount(); i++) {
            Point2D point = simulatedPortfolios.getPoint(i);
            stockSeries.add(point.getX(), point.getY());
            if (isAllPositive(simulatedPortfolios.getPercentages(i))) {
                positiveSeries.add(point.getX(), point.getY());
            }
        }
        tangentPoint = optimalPortfolio.getOptimalPoint();
        double t1_y = riskFreeRate;
        double t1_x = 0;
        double t2_x = tangentPoint.getX();
        double t2_y = tangentPoint.getY();
        double delta = (t2_y - t1_y) / (t2_x - t1_x);
        t2_x = t2_x * 2;
        t2_y = t1_y + t2_x * delta;
        if (showTangent) {
            tangentSeries.add(0, riskFreeRate);
            tangentSeries.add(tangentPoint.getX(), tangentPoint.getY());
            tangentSeries.add(t2_x, t2_y);
            System.out.println("t1_x=" + 0 + " t1_y=" + riskFreeRate);
            System.out.println("t2_x=" + tangentPoint.getX() + " t2_y=" + tangentPoint.getY());
        }
        if (mouseClicked) {
            mouseSeries.add(mouseX, mouseY);
        }
        XYSeriesCollection ds = new XYSeriesCollection();
        ds.addSeries(mouseSeries);
        ds.addSeries(tangentSeries);
        ds.addSeries(positiveSeries);
        ds.addSeries(stockSeries);
        JFreeChart chart = ChartFactory.createXYLineChart("", "Risk (Standard Deviation)", "Expected Return", ds, PlotOrientation.VERTICAL, false, true, false);
        chart.setBackgroundPaint(Color.white);
        XYPlot subplot1 = (XYPlot) chart.getPlot();
        XYLineAndShapeRenderer renderer1 = (XYLineAndShapeRenderer) subplot1.getRenderer();
        NumberAxis xAxis = (NumberAxis) subplot1.getDomainAxis();
        NumberAxis yAxis = (NumberAxis) subplot1.getRangeAxis();
        if (t2_x > 0) {
            xAxis.setRange(0, t2_x);
        } else {
            xAxis.setRange(t2_x, 0);
        }
        if (t2_y > 0) {
            yAxis.setRange(-t2_y, t2_y);
        } else {
            yAxis.setRange(t2_y, -t2_y);
        }
        renderer1.setSeriesPaint(3, Color.blue);
        renderer1.setSeriesPaint(2, Color.red);
        renderer1.setSeriesPaint(1, Color.orange);
        renderer1.setSeriesPaint(0, Color.green);
        Shape shape = renderer1.getBaseShape();
        renderer1.setSeriesShape(1, shape);
        renderer1.setSeriesShape(3, shape);
        renderer1.setSeriesLinesVisible(0, false);
        renderer1.setSeriesLinesVisible(1, true);
        renderer1.setSeriesLinesVisible(2, false);
        renderer1.setSeriesLinesVisible(3, false);
        renderer1.setBaseShapesVisible(true);
        renderer1.setBaseShapesFilled(true);
        chartPanel = new ChartPanel(chart, false);
        chartPanel.setPreferredSize(new Dimension(CHART_SIZE_X, CHART_SIZE_Y));
        chartPanel.addChartMouseListener(this);
        if (mouseClicked) {
            getRecordTable().setText(text.toString());
        } else {
            text = new StringBuffer();
            getRecordTable().setText(text.toString());
        }
        mouseClicked = false;
    }

    private void updateGraph() {
        updateChart();
        if (tabbedPanelContainer.getTitleAt(tabbedPanelContainer.getSelectedIndex()) != ALL) {
            updateGraphTab();
            tabbedPanelContainer.setSelectedIndex(tabbedPanelContainer.indexOfComponent(graphTab));
        } else {
            updateMixTab();
        }
    }

    /**
     * This method adds a new component to the tabbed panel.
     *
     * @param title     the title of the new tab
     * @param component the component to be added
     */
    public void addTabbedPane(String title, JComponent component) {
        tabbedPanelContainer.addTab(title, component);
        tabbedPaneCount++;
    }

    /**
     * This method adds a new component to the tabbed panel.
     *
     * @param title     the title of the new tab
     * @param icon      the icon of the new tab
     * @param component the component to be added
     * @param tip       the tooltip of the new tab
     */
    public void addTabbedPane(String title, Icon icon, JComponent component, String tip) {
        tabbedPanelContainer.addTab(title, icon, component, tip);
        tabbedPaneCount++;
    }

    /**
     * This method removes a component from the tabbed panel.
     *
     * @param index the index of the tab to be removed
     */
    public void removeTabbedPane(int index) {
        tabbedPanelContainer.removeTabAt(index);
        tabbedPaneCount--;
    }

    /**
     * This method sets a component at the specified index in the tabbed panel to a specified new component.
     *
     * @param index the index in the tabbed panel
     * @param c     the new component to be placed in the tabbed panel
     */
    public void setTabbedPaneComponentAt(int index, JComponent c) {
        tabbedPanelContainer.setComponentAt(index, c);
    }

    /**
     * @param x
     * @param y
     * @param text
     * @param toolTipText
     * @param bValues
     * @param defaultIndex
     * @param panel
     * @param l
     */
    public void addRadioButton(int x, int y, String text, String toolTipText, String[] bValues, int defaultIndex, JPanel panel, ActionListener l) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x;
        c.gridy = y;
        ButtonGroup group = new ButtonGroup();
        p.setToolTipText(toolTipText);
        p.add(new JLabel(text));
        radioButtons = new JRadioButton[bValues.length];
        for (int i = 0; i < bValues.length; i++) {
            radioButtons[i] = new JRadioButton(bValues[i]);
            radioButtons[i].setName(bValues[i]);
            radioButtons[i].addActionListener(l);
            radioButtons[i].setActionCommand(bValues[i]);
            p.add(radioButtons[i]);
            group.add(radioButtons[i]);
            if (defaultIndex == i) {
                radioButtons[i].setSelected(true);
            }
        }
        panel.add(p, c);
    }

    /**
     * Adds a radio button group to the sidebar of the applet
     *
     * @param text         the title of the group
     * @param toolTipText  the tooltip for the group
     * @param bValues      the values for the individual radio buttons
     * @param defaultIndex the index of the default selected radio button
     * @param l            an ActionListener to be attached to the radio buttons
     */
    public void addRadioButtonsToSidebar(String text, String toolTipText, String[] bValues, int defaultIndex, ActionListener l) {
        JPanel in = new JPanel();
        in.setLayout(new BoxLayout(in, BoxLayout.LINE_AXIS));
        in.add(new JLabel(text));
        ButtonGroup group = new ButtonGroup();
        radioButtons = new JRadioButton[bValues.length];
        for (int i = 0; i < bValues.length; i++) {
            radioButtons[i] = new JRadioButton(bValues[i]);
            radioButtons[i].setName(bValues[i]);
            radioButtons[i].addActionListener(l);
            radioButtons[i].setActionCommand(bValues[i]);
            in.add(radioButtons[i]);
            group.add(radioButtons[i]);
            if (defaultIndex == i) {
                radioButtons[i].setSelected(true);
            }
        }
        in.add(Box.createHorizontalGlue());
        leftSideBarPanel.add(in);
    }

    protected JFreeChart createEmptyChart(String chartTitle) {
        JFreeChart chart = ChartFactory.createPieChart(chartTitle, null, true, true, false);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        plot.setNoDataMessage("No data available");
        plot.setCircular(false);
        plot.setLabelGap(0.02);
        return chart;
    }

    private void newMatrixTextField(int y, int x, FloatTextField textField, JPanel panel) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x;
        c.gridy = y;
        panel.add(textField, c);
    }

    private boolean isAllPositive(double[] portfolio) {
        boolean positive = true;
        for (int i = 0; i < portfolio.length; i++) {
            if (portfolio[i] < 0) {
                positive = false;
                break;
            }
        }
        return positive;
    }

    private void updateNumStocks() {
        int oldNumStocks = expectedReturns.length;
        double[] oldExpRets = expectedReturns;
        double[] oldVariances = variances;
        double[][] oldCovrs = covariances;
        expectedReturns = new double[numStocks];
        variances = new double[numStocks];
        covariances = new double[numStocks][numStocks];
        int limitingFactor = (numStocks > oldNumStocks) ? oldNumStocks : numStocks;
        for (int i = 0; i < limitingFactor; i++) {
            expectedReturns[i] = oldExpRets[i];
            variances[i] = oldVariances[i];
            covariances[i][i] = oldCovrs[i][i];
            for (int j = 0; j < i; j++) {
                covariances[i][j] = covariances[j][i] = oldCovrs[i][j];
            }
        }
    }

    /**
     * @param number
     */
    @Override
    public void setNumberStocks(String number) {
        numStocks = Integer.parseInt(number);
        init();
        statusTextArea.setText("setting stock number to " + numStocks);
    }

    /**
     * @param t
     */
    public void setTangent(boolean t) {
        showTangent = t;
        updateGraph();
    }

    /**
     * @param r1
     * @param c1
     * @param m1
     */
    public void setSliders(double[] r1, double[] c1, double[] m1) {
        initInputTextFields();
        updateInputTextFields();
    }

    /**
     * This method is called whenever the observed object is changed. An
     * application calls an <tt>Observable</tt> object's
     * <code>notifyObservers</code> method to have all the object's
     * observers notified of the change.
     *
     * @param o   the observable object.
     * @param arg an argument passed to the <code>notifyObservers</code>
     *            method.
     */
    @Override
    public void update(Observable o, Object arg) {
        update();
    }

    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(SWITCH_ARRAY[0])) {
            covarianceFlag = true;
            updateInputTextFields();
        } else if (e.getActionCommand().equals(SWITCH_ARRAY[1])) {
            covarianceFlag = false;
            updateInputTextFields();
        }
        for (int i = 0; i < ON_OFF.length; i++) {
            if (e.getActionCommand().equals(ON_OFF[i])) {
                if (i == 0) {
                    setTangent(true);
                } else {
                    setTangent(false);
                }
            }
        }
    }

    /**
     * Callback method for receiving notification of a mouse click on a chart.
     */
    public void chartMouseClicked(ChartMouseEvent event) {
        if (event.getEntity() == null) {
            return;
        }
        tooltip = event.getEntity().getToolTipText();
        mouseX = Double.parseDouble(tooltip.substring(tooltip.indexOf("(") + 1, tooltip.indexOf(",")));
        mouseY = Double.parseDouble(tooltip.substring(tooltip.indexOf(",") + 1, tooltip.indexOf(")")));
        tooltip = "(" + mouseX + "," + mouseY + ")\n";
        for (int i = 0; i < simulatedPortfolios.getPointCount(); i++) {
            Point2D point = simulatedPortfolios.getPoint(i);
            double x = Double.parseDouble(tooltipFormatter.format(point.getX()));
            double y = Double.parseDouble(tooltipFormatter.format(point.getY()));
            if (x == mouseX && y == mouseY) {
                double[] percentages = simulatedPortfolios.getPercentages(i);
                tooltip += "==> Stocks(";
                int j = 0;
                for (; j < percentages.length - 1; j++) {
                    tooltip += tooltipFormatter.format(percentages[j]) + ", ";
                }
                tooltip += tooltipFormatter.format(percentages[j]) + ")\n";
            }
        }
        mouseClicked = true;
        updateGraph();
    }

    /**
     * Callback method for receiving notification of a mouse movement on a chart.
     */
    public void chartMouseMoved(ChartMouseEvent event) {
    }

    /**
     * @return
     */
    @Override
    public String getOnlineDescription() {
        return new String("http://socr.stat.ucla.edu/");
    }

    /**
     * @return
     */
    @Override
    public String getAppletInfo() {
        return new String("SOCR Experiments: http://www.socr.ucla.edu \n");
    }

    /**
     *
     */
    @Override
    public Container getDisplayPane() {
        JSplitPane container = new JSplitPane(JSplitPane.VERTICAL_SPLIT, getMainPanel(), getTextPanel());
        return container;
    }
}
