package com.widen.prima.view.finance;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import com.widen.prima.Messages;
import com.widen.prima.finance.entites.BoaType;
import com.widen.prima.finance.entites.SubjectType;
import com.widen.prima.util.DateUtil;
import com.widen.prima.util.Util;

public class StatisticView extends ViewPart {

    public static final String ID = "com.widen.prima.view.statistic";

    private List closeDays;

    private CTabFolder chartTabFolder;

    private List balanceSheetDatas;

    private List incomeStatementDatas;

    private Frame[] chartFrames;

    /**
     * A custom label generator.
     */
    static class LabelGenerator implements CategoryItemLabelGenerator {

        /**
         * Creates a new generator that only displays labels that are greater
         * than or equal to the threshold value.
         * 
         * @param threshold the threshold value.
         */
        public LabelGenerator() {
        }

        /**
         * Generates a label for the specified item. The label is typically a
         * formatted version of the data value, but any text can be used.
         * 
         * @param dataset the dataset (<code>null</code> not permitted).
         * @param series the series index (zero-based).
         * @param category the category index (zero-based).
         * @return the label (possibly <code>null</code>).
         */
        public String generateLabel(CategoryDataset dataset, int series, int category) {
            String result = null;
            final Number value = dataset.getValue(series, category);
            if (value != null) {
                result = Util.double_formatter.format(value);
            }
            return result;
        }

        public String generateRowLabel(CategoryDataset arg0, int arg1) {
            return null;
        }

        public String generateColumnLabel(CategoryDataset arg0, int arg1) {
            return null;
        }
    }

    public StatisticView() {
        super();
        Util.statisticView = this;
        chartFrames = new Frame[6];
        this.loadData();
        Util.statisticView = this;
    }

    public void init(IViewSite site) throws PartInitException {
        setSite(site);
        setPartName(Messages.getString("StatisticView.name"));
    }

    private void loadData() {
        closeDays = this.getValidCloseOffDays();
        this.balanceSheetDatas = new ArrayList();
        this.incomeStatementDatas = new ArrayList();
        for (Iterator iter = closeDays.iterator(); iter.hasNext(); ) {
            String closeOffDay = (String) iter.next();
            Double[] bshDataArray = this.getBalanceSheetData(closeOffDay);
            Double[] istDataArray = this.getIncomeStatementData(closeOffDay);
            this.balanceSheetDatas.add(bshDataArray);
            this.incomeStatementDatas.add(istDataArray);
        }
    }

    public void createPartControl(Composite parent) {
        try {
            System.setProperty("sun.awt.noerasebackground", "true");
        } catch (NoSuchMethodError ignore) {
        }
        chartTabFolder = new CTabFolder(parent, SWT.TOP);
        chartTabFolder.setSimple(false);
        chartTabFolder.addSelectionListener(new SelectionListener() {

            public void widgetSelected(SelectionEvent arg0) {
            }

            public void widgetDefaultSelected(SelectionEvent arg0) {
            }
        });
        for (int i = 0; i < 6; i++) {
            CTabItem item = new CTabItem(chartTabFolder, SWT.NONE);
            item.setText(Messages.getString("ManageSubjectEditor.subject.type." + i));
            Composite chartComp = new Composite(chartTabFolder, SWT.NONE);
            chartComp.setLayout(new FillLayout());
            Composite awtComposite = new Composite(chartComp, SWT.EMBEDDED | SWT.NO_BACKGROUND);
            chartFrames[i] = SWT_AWT.new_Frame(awtComposite);
            chartTabFolder.setSelection(0);
            item.setControl(chartComp);
        }
        this.updateChart();
    }

    public void updateChart() {
        this.loadData();
        for (int i = 0; i < 6; i++) {
            chartFrames[i].removeAll();
            CategoryDataset dataset = null;
            Color color = Color.black;
            switch(i) {
                case 0:
                    dataset = this.createAssertDataset();
                    color = Color.green;
                    break;
                case 1:
                    dataset = this.createLiabilityDataset();
                    color = Color.red;
                    break;
                case 2:
                    dataset = this.createOwnerRightDataset();
                    color = Color.magenta;
                    break;
                case 3:
                    dataset = this.createIncomeDataset();
                    color = Color.green;
                    break;
                case 4:
                    dataset = this.createFeeDataset();
                    color = Color.red;
                    break;
                case 5:
                    dataset = this.createRetainedProfitsDataset();
                    color = Color.blue;
                    break;
            }
            JFreeChart chart = this.getStatisticChart("", dataset, color);
            ChartPanel cp = new ChartPanel(chart);
            chartFrames[i].add(cp);
            chartFrames[i].doLayout();
        }
    }

    /**
     * ��ݲ�ͬ����ݼ�������ͬ��ͳ��ͼ
     * 
     * @param title
     * @param dataset
     * @param lineColor
     * @return
     */
    private JFreeChart getStatisticChart(String title, CategoryDataset dataset, Color lineColor) {
        JFreeChart chart = ChartFactory.createLineChart(title, "", "", dataset, PlotOrientation.VERTICAL, true, true, false);
        chart.setBackgroundPaint(Color.white);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setRangeGridlinePaint(Color.gray);
        plot.setDomainGridlinePaint(Color.gray);
        plot.setDomainGridlinesVisible(true);
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createStandardTickUnits());
        rangeAxis.setAutoRangeIncludesZero(true);
        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setDrawOutlines(true);
        renderer.setBaseLinesVisible(true);
        renderer.setBaseShapesFilled(true);
        renderer.setShapesVisible(true);
        renderer.setSeriesPaint(0, lineColor);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setItemLabelGenerator(new LabelGenerator());
        renderer.setItemLabelFont(new Font("Serif", Font.PLAIN, 12));
        renderer.setItemLabelsVisible(true);
        return chart;
    }

    public void setFocus() {
    }

    public CategoryDataset createAssertDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String series1 = "�ʲ�";
        for (int i = 0; i < closeDays.size(); i++) {
            String closeOffDay = (String) closeDays.get(i);
            String closeOffDayShow = closeOffDay.substring(2, 6);
            Double[] bshDataArray = (Double[]) this.balanceSheetDatas.get(i);
            dataset.addValue(bshDataArray[0], series1, closeOffDayShow);
        }
        return dataset;
    }

    public CategoryDataset createLiabilityDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String series1 = "��ծ";
        for (int i = 0; i < closeDays.size(); i++) {
            String closeOffDay = (String) closeDays.get(i);
            String closeOffDayShow = closeOffDay.substring(2, 6);
            Double[] bshDataArray = (Double[]) this.balanceSheetDatas.get(i);
            dataset.addValue(bshDataArray[1], series1, closeOffDayShow);
        }
        return dataset;
    }

    public CategoryDataset createOwnerRightDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String series1 = "������Ȩ��";
        for (int i = 0; i < closeDays.size(); i++) {
            String closeOffDay = (String) closeDays.get(i);
            String closeOffDayShow = closeOffDay.substring(2, 6);
            Double[] bshDataArray = (Double[]) this.balanceSheetDatas.get(i);
            dataset.addValue(bshDataArray[2], series1, closeOffDayShow);
        }
        return dataset;
    }

    public CategoryDataset createIncomeDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String series1 = "����";
        for (int i = 0; i < closeDays.size(); i++) {
            String closeOffDay = (String) closeDays.get(i);
            String closeOffDayShow = closeOffDay.substring(2, 6);
            Double[] istDataArray = (Double[]) this.incomeStatementDatas.get(i);
            dataset.addValue(istDataArray[0], series1, closeOffDayShow);
        }
        return dataset;
    }

    public CategoryDataset createFeeDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String series1 = "����";
        for (int i = 0; i < closeDays.size(); i++) {
            String closeOffDay = (String) closeDays.get(i);
            String closeOffDayShow = closeOffDay.substring(2, 6);
            Double[] istDataArray = (Double[]) this.incomeStatementDatas.get(i);
            dataset.addValue(istDataArray[1], series1, closeOffDayShow);
        }
        return dataset;
    }

    public CategoryDataset createRetainedProfitsDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String series1 = "������";
        for (int i = 0; i < closeDays.size(); i++) {
            String closeOffDay = (String) closeDays.get(i);
            String closeOffDayShow = closeOffDay.substring(2, 6);
            Double[] istDataArray = (Double[]) this.incomeStatementDatas.get(i);
            double retainedProfits = istDataArray[0].doubleValue() - istDataArray[1].doubleValue();
            dataset.addValue(retainedProfits, series1, closeOffDayShow);
        }
        return dataset;
    }

    /**
     * ��ȡϵͳ���ʵ���Сʱ������ʱ��֮������н����յ��б?
     * 
     * @return
     */
    private List getValidCloseOffDays() {
        List closeOffDays = new ArrayList();
        try {
            String hql = "select min(registerTime),max(registerTime) from " + "com.widen.prima.finance.entites.SubjectBalanceBo where boaType=" + BoaType.CLOSEOFF.getValue();
            Collection result = Util.financeMgrService.findByHql(hql);
            Object[] dates = (Object[]) result.iterator().next();
            String minTimeString = (String) dates[0];
            String maxTimeString = (String) dates[1];
            if (minTimeString != null && maxTimeString != null) {
                Date minTime = Util.dateFormatter_code.parse(minTimeString);
                Date maxTime = Util.dateFormatter_code.parse(maxTimeString);
                int minYear = DateUtil.getYear(minTime);
                int minMonth = DateUtil.getMonth(minTime);
                int maxYear = DateUtil.getYear(maxTime);
                int maxMonth = DateUtil.getMonth(maxTime);
                for (int year = minYear; year <= maxYear; year++) {
                    int beginMonth = 0;
                    int endMonth = 0;
                    if (year == minYear && year < maxYear) {
                        beginMonth = minMonth;
                        endMonth = 12;
                    } else if (year == maxYear && year > minYear) {
                        beginMonth = 1;
                        endMonth = maxMonth;
                    } else if (minYear == year && year == maxYear) {
                        beginMonth = minMonth;
                        endMonth = maxMonth;
                    } else {
                        beginMonth = 1;
                        endMonth = 12;
                    }
                    for (int month = beginMonth; month <= endMonth; month++) {
                        Date closeOffDay = DateUtil.getEndTime(year, month);
                        String closeOfDayString = Util.dateFormatter_code.format(closeOffDay);
                        closeOffDays.add(closeOfDayString);
                    }
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return closeOffDays;
    }

    /**
     * ��ȡĳ�������յ��ʲ�ծ��ϼ���ݣ����ʲ��ܸ�ծ��������Ȩ���ܶ�
     * 
     * @param closeOffDay
     * @return
     */
    private Double[] getBalanceSheetData(String closeOffDay) {
        Double[] result = new Double[3];
        for (int i = 0; i < result.length; i++) {
            result[i] = new Double(0);
        }
        List params = new ArrayList();
        params.add(closeOffDay);
        Collection dataList = Util.financeMgrService.queryBalanceSheetData(params);
        for (Iterator iter = dataList.iterator(); iter.hasNext(); ) {
            Object[] oneRow = (Object[]) iter.next();
            int subjectType = ((SubjectType) oneRow[1]).getValue();
            double balanceMoney = 0;
            if (oneRow[4] != null) {
                balanceMoney = ((Double) oneRow[4]).doubleValue();
            }
            if (subjectType == SubjectType.ASSERT.getValue()) {
                result[0] = new Double(result[0].doubleValue() + balanceMoney);
            } else if (subjectType == SubjectType.LIABILITY.getValue()) {
                result[1] = new Double(result[1].doubleValue() + balanceMoney);
            } else if (subjectType == SubjectType.OWNER_RIGHTS.getValue()) {
                result[2] = new Double(result[2].doubleValue() + balanceMoney);
            }
        }
        return result;
    }

    /**
     * ��ȡĳ�������յ������ĺϼ���ݣ������롢�ܷ���
     * 
     * @param closeOffDay
     * @return
     */
    private Double[] getIncomeStatementData(String closeOffDay) {
        Double[] result = new Double[2];
        for (int i = 0; i < result.length; i++) {
            result[i] = new Double(0);
        }
        String beginTime = closeOffDay.substring(0, 6) + "01000000000";
        List params = new ArrayList();
        params.add(beginTime);
        params.add(closeOffDay);
        Collection dataList = Util.financeMgrService.queryIncomeStatementData(params);
        for (Iterator iter = dataList.iterator(); iter.hasNext(); ) {
            Object[] oneRow = (Object[]) iter.next();
            int subjectType = ((SubjectType) oneRow[1]).getValue();
            double balanceMoney = 0;
            if (oneRow[5] != null) {
                balanceMoney = ((Double) oneRow[5]).doubleValue();
            }
            if (subjectType == SubjectType.INCOME.getValue()) {
                result[0] = new Double(result[0].doubleValue() + balanceMoney);
            } else if (subjectType == SubjectType.FEE.getValue()) {
                result[1] = new Double(result[1].doubleValue() + balanceMoney);
            }
        }
        return result;
    }
}
