package edu.ucla.stat.SOCR.chart;

import java.awt.Color;
import java.awt.Container;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.StringTokenizer;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;
import edu.ucla.stat.SOCR.gui.SOCROptionPane;
import edu.ucla.stat.SOCR.util.EditableHeader;

/**
  * A simple demonstration application showing how to create a bar chart.
 */
public class SuperMultiIndexChart extends Chart implements PropertyChangeListener {

    protected String[][] raw_x2;

    protected int row_count;

    public void init() {
        indLabel = new JLabel("X");
        mapDep = false;
        super.init();
        indMax = 100;
        updateStatus(url);
        resetExample();
        validate();
    }

    /**
	 *  create chart using data from the dataTable
	 */
    public void doChart() {
        if (dataTable.isEditing()) dataTable.getCellEditor().stopCellEditing();
        if (!hasExample) {
            SOCROptionPane.showMessageDialog(this, DATA_MISSING_MESSAGE);
            resetChart();
            return;
        }
        isDemo = false;
        XYDataset dataset = createDataset(isDemo);
        JFreeChart chart = createChart(dataset);
        chartPanel = new ChartPanel(chart, isDemo);
        setChart();
    }

    /**
	 *  sample code for generating chart using ChartGenerator_JTable 
	 */
    public void doTest() {
        JFreeChart chart;
        ChartGenerator_JTable chartMaker = new ChartGenerator_JTable();
        resetChart();
        showMessageDialog("SuperIndexChart doTest get called!");
        int no_series = dataTable.getColumnCount();
        int[][] pairs = new int[no_series][2];
        for (int i = 0; i < no_series; i++) {
            pairs[i][0] = 0;
            pairs[i][1] = 0;
        }
        chart = chartMaker.getXYChart("Index", "Index Chart", "Row", "Data", dataTable, no_series, pairs, "noshape");
        chartPanel = new ChartPanel(chart, false);
        setChart();
    }

    /**
	   * 
	   * @param isDemo data come from demo(true) or dataTable(false)
	   * @return
	   */
    protected XYDataset createDataset(boolean isDemo) {
        if (isDemo) {
            updateStatus("isDemo==true in " + this.getClass().getName() + " class! return null Dataset, check the code!");
            return null;
        } else {
            setArrayFromTable();
            double[][] raw_xvalue;
            row_count = xyLength;
            raw_x2 = new String[independentVarLength][row_count];
            raw_xvalue = new double[independentVarLength][row_count];
            boolean[][] skip = new boolean[independentVarLength][row_count];
            for (int index = 0; index < independentVarLength; index++) {
                for (int i = 0; i < xyLength; i++) {
                    raw_x2[index][i] = indepValues[i][index];
                    try {
                        if (raw_x2[index][i] != "null" && raw_x2[index][i] != null && raw_x2[index][i].length() != 0) {
                            raw_xvalue[index][i] = Double.parseDouble(raw_x2[index][i]);
                        } else skip[index][i] = true;
                    } catch (Exception e) {
                        System.out.println("wrong data " + raw_x2[index][i]);
                    }
                }
            }
            rangeLabel = "";
            for (int j = 0; j < independentVarLength; j++) rangeLabel += independentHeaders[j] + "/";
            rangeLabel = rangeLabel.substring(0, rangeLabel.length() - 1);
            double[][] y_freq = new double[independentVarLength][row_count];
            for (int j = 0; j < independentVarLength; j++) {
                for (int i = 0; i < row_count; i++) y_freq[j][i] = i + 1;
            }
            XYSeriesCollection dataset = new XYSeriesCollection();
            for (int j = 0; j < independentVarLength; j++) {
                XYSeries series = new XYSeries(independentHeaders[j]);
                for (int i = 0; i < row_count; i++) {
                    if (skip[j][i] == false) series.add(y_freq[j][i], raw_xvalue[j][i]);
                }
                dataset.addSeries(series);
            }
            return dataset;
        }
    }

    /**
     * Creates a chart.
     * 
     * @param dataset  the dataset.
     * 
     * @return a chart.
     */
    protected JFreeChart createChart(XYDataset dataset) {
        JFreeChart chart = ChartFactory.createXYLineChart(chartTitle, domainLabel, rangeLabel, dataset, PlotOrientation.HORIZONTAL, !legendPanelOn, true, false);
        chart.setBackgroundPaint(Color.white);
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer.setBaseShapesVisible(true);
        renderer.setBaseShapesFilled(true);
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        return chart;
    }

    public void setDataTable(String input) {
        hasExample = true;
        StringTokenizer lnTkns = new StringTokenizer(input, "#,");
        String line;
        int lineCt = lnTkns.countTokens();
        resetTableRows(lineCt);
        resetTableColumns(1);
        int r = 0;
        while (lnTkns.hasMoreTokens()) {
            line = lnTkns.nextToken();
            dataTable.setValueAt(line, r, 0);
            r++;
        }
        resetTableColumns(dataTable.getColumnCount());
    }

    public void setMapping() {
        addButtonIndependent();
    }

    public void setXLabel(String xLabel) {
        domainLabel = xLabel;
        TableColumnModel columnModel = dataTable.getColumnModel();
        columnModel.getColumn(0).setHeaderValue(xLabel);
        dataTable.setTableHeader(new EditableHeader(columnModel));
    }

    public void setYLabel(String yLabel) {
        rangeLabel = yLabel;
        TableColumnModel columnModel = dataTable.getColumnModel();
        columnModel.getColumn(0).setHeaderValue(yLabel);
        dataTable.setTableHeader(new EditableHeader(columnModel));
    }

    public void propertyChange(PropertyChangeEvent e) {
        String propertyName = e.getPropertyName();
        System.err.println("From RegCorrAnal:: propertyName =" + propertyName + "!!!");
        if (propertyName.equals("DataUpdate")) {
            dataTable = (JTable) (e.getNewValue());
            dataPanel.removeAll();
            dataPanel.add(new JScrollPane(dataTable));
            dataTable.doLayout();
            System.err.println("From RegCorrAnal:: data UPDATED!!!");
        }
    }

    public Container getDisplayPane() {
        return this.getContentPane();
    }
}
