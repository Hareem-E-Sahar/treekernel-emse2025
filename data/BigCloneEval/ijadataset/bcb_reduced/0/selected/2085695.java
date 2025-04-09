package kr.ac.ssu.imc.whitehole.report.viewer.rdobjects.rdgraphs;

import java.awt.geom.*;
import java.awt.*;
import javax.swing.*;
import java.util.Vector;

public class RDGModel {

    private String queryName;

    private String seriesField;

    private String xAxisField;

    private String dataValueField;

    private String[][] sData;

    private boolean bHasFieldNames;

    private boolean bHasRowNames;

    private String sChartType = this.CT_COLUMN2D;

    private String sSeriesType = new String("��");

    private String sChartTitle;

    private String sXTitle;

    private String sYTitle;

    private String[] sReturn = null;

    private String[] xReturn = null;

    private double pieGap;

    private Point2D[] pieValuePosition = null;

    private Rectangle2D tChartRect, tSeriesRect, tChartTitleRect, tXTitleRect, tYTitleRect;

    private double yMaxValue, yMinValue, yInterval = 0.0;

    private double xMaxValue, xMinValue, xInterval = 0.0;

    private Color[] tChartObjectColor = { new Color(100, 100, 255), new Color(255, 100, 100), new Color(0xff, 0xec, 0x8b), new Color(200, 100, 255), new Color(255, 155, 0), new Color(0xbc, 0xce, 0x68), new Color(0x1c, 0x86, 0xee), new Color(0xee, 0xc5, 0x91), new Color(255, 100, 0), new Color(100, 200, 100), new Color(0x8b, 0xec, 0xff), new Color(0x68, 0xce, 0xbc), new Color(0xee, 0x86, 0x1c), new Color(0x91, 0xc5, 0xee), new Color(0, 100, 255), new Color(0, 155, 255), new Color(255, 100, 200), new Color(200, 100, 100), new Color(100, 100, 200), new Color(0xce, 0x68, 0xbc) };

    private double slope = 1.0;

    private float depthFor3DY = 20;

    private Line2D[] pieValueLine = null;

    private Font titleFont = new Font("����ü", Font.PLAIN, 15);

    private Color titleColor = new Color(Integer.parseInt("000000", 16));

    private Color bgColor = new Color(Integer.parseInt("dddddd", 16));

    private boolean bgTransparentOn = false;

    private Font labelFont = new Font("����ü", Font.PLAIN, 10);

    private int dataNum = -1;

    private boolean dataLabelOn = false;

    private boolean dataNumberOn = false;

    private boolean dataRotate = false;

    private String dataInOut = this.DIO_OUT;

    private Font dataLabelFont = new Font("����ü", Font.PLAIN, 11);

    private String dataPrefix = "", dataPostfix = "";

    private String dataForm = "";

    private int dataNum2 = -1;

    private Color[] dataColor2 = null;

    private boolean dataLabelOn2 = false;

    private Font dataLabelFont2 = new Font("����ü", Font.BOLD, 12);

    private String dataPrefix2 = "", dataPostfix2 = "";

    private String dataForm2 = "";

    private boolean fillOn = true;

    private Color[] fillColors = null;

    private boolean dotOn = false;

    private Color[] dotColors = null;

    private int[] dotSizes = null;

    private String[] dotTypes = null;

    private boolean lineOn = false;

    private Color[] lineColors = null;

    private float[] lineWidths;

    private String[] lineTypes = null;

    private int targetNum = 1;

    private String[] targetTexts = null;

    private Color[] targetColors = null;

    private Font targetFont = new Font("����ü", Font.PLAIN, 10);

    private double[] targetDatas = null;

    private double[][] targetEllips = null;

    private boolean xDataOn = true;

    private boolean xDataRotate = false;

    private boolean xGridOn = true;

    private String xForm = "";

    private boolean yDataOn = true;

    private boolean yGridOn = true;

    private String yForm = "";

    private boolean axisOn = true;

    private Color[] seriesColors;

    private String cDirection = VERTICAL;

    private String axisOpt;

    private int margin = 5;

    private String legendOpt = UP;

    private String legendOpt2 = OFF;

    private String avgLineColor;

    private String valuesLineColor;

    private String ValuesLineValue;

    private String[] sigmaLineColors;

    private String dataType;

    private boolean titleOn = true;

    public static final String CT_LINE2D = "��������";

    public static final String CT_COLUMN2D = "���� ������";

    public static final String CT_PIE2D = "����";

    public static final String CT_SCATTER2D = "�л���";

    public static final String CT_ACCUCOLUMN2D = "���� ���� ������";

    public static final String CT_REGION2D = "������";

    public static final String CT_HORICOLUMN2D = "���� ������";

    public static final String CT_LINE3D = "3D ��������";

    public static final String CT_REGION3D = "3D ������";

    public static final String CT_COLUMN3D = "3D ���� ������";

    public static final String CT_HORICOLUMN3D = "3D ���� ������";

    public static final String CT_ACCUCOLUMN3D = "3D ���� ���� ������";

    public static final String CT_PIE3D = "3D ����";

    public static final String CT_BARNLINE2D = "bar&line";

    public static final String DIO_IN = "in";

    public static final String DIO_OUT = "out";

    public static final String AXIS_ON = "���߰�";

    public static final String AXIS_OFF = "������";

    public static final String AXIS_BG_OPAQUE = "���������";

    public static final String AXIS_ONLY_TEXT = "��ġ";

    public static final String OFF = "false";

    public static final String UP = "up";

    public static final String DOWN = "down";

    public static final String LEFT = "left";

    public static final String RIGHT = "right";

    public static final String LT_DOT = "����";

    public static final String LT_SOLID = "����";

    public static final String LT_SLASH = "�⼱";

    public static final String[] lineTypeItems = { LT_SOLID, LT_DOT, LT_SLASH };

    public static String[] sizeItems = { "1px", "2px", "3px", "4px", "5px", "6px", "7px", "8px", "9px", "10px", "11px", "12px" };

    public static final String DT_CIRCLE = "��";

    public static final String DT_SQUARE = "�簢��";

    public static final String DT_TRIANGLE = "�ﰢ��";

    public static final String[] dotTypeItems = { DT_CIRCLE, DT_SQUARE, DT_TRIANGLE };

    public static final String VERTICAL = "v";

    public static final String HORIZONTAL = "h";

    public static final String B_LINE_CT = "forCnL";

    public static final String CLC_BLACK = "black";

    public static final String CLC_RED = "red";

    public static final String CLC_YELLOW = "yellow";

    public static final String CLC_BLUE = "blue";

    public static final String CLC_GREEN = "green";

    public static final String CLC_CYAN = "cyan";

    public static final String CLC_PINK = "pink";

    public static final String CLC_ORANGE = "orange";

    public static final String CLC_MAGENTA = "magenta";

    public static final String CLC_WHITE = "white";

    public static final String CLC_LIGHTGRAY = "lightgray";

    public static final String CLC_GRAY = "gray";

    public static final String CLC_DARKGRAY = "darkgray";

    public static final String DF_TITLE_FONT = "����ü, bold, 20";

    public static final int DF_DOT_SIZE = 5;

    public static final float DF_LINE_WIDTH = 2.0f;

    public static final String DT_STATIC = "static";

    public static final String DT_DYNAMIC = "dynamic";

    public RDGModel(String[][] sData, String sChartType) {
        this.sData = sData;
        this.sChartType = sChartType;
        bHasFieldNames = false;
        bHasRowNames = false;
        sSeriesType = null;
        tChartRect = null;
        tSeriesRect = null;
        queryName = null;
        seriesField = null;
        xAxisField = null;
        dataValueField = null;
        this.prepareOtherColors();
    }

    public RDGModel(String[][] sData, String sChartType, boolean bHasFieldNames, boolean bHasRowNames) {
        this.sData = sData;
        this.sChartType = sChartType;
        this.bHasFieldNames = bHasFieldNames;
        this.bHasRowNames = bHasRowNames;
        sSeriesType = null;
        tChartRect = null;
        tSeriesRect = null;
        queryName = null;
        seriesField = null;
        xAxisField = null;
        dataValueField = null;
        this.prepareOtherColors();
    }

    public RDGModel() {
        bHasFieldNames = false;
        bHasRowNames = false;
        tChartRect = null;
        tSeriesRect = null;
        queryName = null;
        seriesField = null;
        xAxisField = null;
        dataValueField = null;
        this.prepareOtherColors();
    }

    public boolean isValid() {
        return (!(sData == null || sData.length <= 0 || sData[0] == null || sData[0].length <= 0));
    }

    public String getChartTitle() {
        return sChartTitle;
    }

    public void setChartTitle(String value) {
        sChartTitle = value;
    }

    public String getDataType() {
        return this.dataType;
    }

    public void setDataType(String dt) {
        this.dataType = dt;
    }

    public Color getTitleColor() {
        return this.titleColor;
    }

    public void setTitleColor(Color titleColor) {
        if (titleColor != null) {
            this.titleColor = titleColor;
        } else this.titleColor = Color.black;
    }

    public String getCDirection() {
        return this.cDirection;
    }

    public void setCDirection(String cDirection) {
        if (cDirection.equals("h")) this.cDirection = this.HORIZONTAL; else this.cDirection = this.VERTICAL;
    }

    public boolean getAxisOn() {
        return this.axisOn;
    }

    public void setAxisOn(String axisOn) {
        if (axisOn.equalsIgnoreCase("false")) this.axisOn = false; else this.axisOn = true;
    }

    public void setPosition(String position) {
    }

    public void setSize(String size) {
    }

    public Color getBgColor() {
        return this.bgColor;
    }

    public void setBgColor(Color bgColor) {
        if (bgColor != null) this.bgColor = bgColor; else this.bgColor = null;
    }

    public boolean isBgTransparentsOn() {
        return this.bgTransparentOn;
    }

    public void setBgTransparentOn(String bgTransparentOn) {
        if (bgTransparentOn.equalsIgnoreCase("true")) this.bgTransparentOn = true; else this.bgTransparentOn = false;
    }

    public int getDataNum() {
        return this.dataNum;
    }

    public void setDataNum(String dataNum) {
        try {
            this.dataNum = Integer.parseInt(dataNum);
        } catch (Exception ex) {
            this.dataNum = -1;
        }
    }

    public Color[] getChartObjectColor() {
        return tChartObjectColor;
    }

    public void setChartObjectColor(Color[] value) {
        tChartObjectColor = value;
    }

    public int getDataNum2() {
        return this.dataNum2;
    }

    public void setDataNum2(String dataNum) {
        try {
            this.dataNum2 = Integer.parseInt(dataNum);
        } catch (Exception ex) {
            this.dataNum2 = -1;
        }
    }

    public Color[] getDataColors2() {
        return this.dataColor2;
    }

    public void setDataColors2(String[] dataColor) {
        this.dataColor2 = new Color[dataColor.length];
        for (int i = 0; i < dataColor.length; i++) {
            try {
                this.dataColor2[i] = new Color(Integer.parseInt(dataColor[i], 16));
            } catch (Exception ex) {
            }
        }
    }

    public boolean isDataLabelOn() {
        return this.dataLabelOn;
    }

    public void setDataLabelOn(String dataLabelOn) {
        if (dataLabelOn.equalsIgnoreCase("true")) this.dataLabelOn = true; else this.dataLabelOn = false;
    }

    public boolean isDataNumberOn() {
        return this.dataNumberOn;
    }

    public void setDataNumberOn(String dataNumberOn) {
        if (dataNumberOn.equalsIgnoreCase("true")) this.dataNumberOn = true; else this.dataNumberOn = false;
    }

    public String getDataInOut() {
        return this.dataInOut;
    }

    public void setDataInOut(String inOut) {
        if (inOut.equalsIgnoreCase("in")) this.dataInOut = this.DIO_IN; else this.dataInOut = this.DIO_OUT;
    }

    public boolean isDataRotate() {
        return this.dataRotate;
    }

    public void setDataRotate(String dataRotate) {
        if (dataRotate.equalsIgnoreCase("true")) this.dataRotate = true; else this.dataRotate = false;
    }

    public Font getDataLabelFont() {
        return this.dataLabelFont;
    }

    public void setDataLabelFont(Font dataLabelFont) {
        this.dataLabelFont = dataLabelFont;
    }

    public String getDataPrefix() {
        return this.dataPrefix;
    }

    public void setDataPrefix(String dataPrefix) {
        this.dataPrefix = dataPrefix;
    }

    public String getDataPostfix() {
        return this.dataPostfix;
    }

    public void setDataPostfix(String dataPostfix) {
        this.dataPostfix = dataPostfix;
    }

    public String getDataForm() {
        return this.dataForm;
    }

    public void setDataForm(String dataForm) {
        this.dataForm = dataForm;
    }

    public Font getDataLabelFont2() {
        return this.dataLabelFont2;
    }

    public void setDataLabelFont2(Font dataLabelFont) {
        this.dataLabelFont2 = dataLabelFont;
    }

    public String getDataPrefix2() {
        return this.dataPrefix2;
    }

    public void setDataPrefix2(String dataPrefix) {
        this.dataPrefix2 = dataPrefix;
    }

    public String getDataPostfix2() {
        return this.dataPostfix2;
    }

    public void setDataPostfix2(String dataPostfix) {
        this.dataPostfix2 = dataPostfix;
    }

    public String getDataForm2() {
        return this.dataForm2;
    }

    public void setDataForm2(String dataForm) {
        this.dataForm2 = dataForm;
    }

    public boolean isFillOn() {
        return this.fillOn;
    }

    public void setFillOn(String fillOn) {
        if (fillOn.equalsIgnoreCase("true")) this.fillOn = true; else this.fillOn = false;
    }

    public Color[] getFillColors() {
        return this.fillColors;
    }

    public void setFillColors(String[] fillColors) {
        this.fillColors = new Color[fillColors.length];
        for (int i = 0; i < fillColors.length; i++) {
            try {
                this.fillColors[i] = new Color(Integer.parseInt(fillColors[i], 16));
            } catch (Exception ex) {
            }
        }
    }

    public boolean isDotOn() {
        return this.dotOn;
    }

    public void setDotOn(String dotOn) {
        if (dotOn.equalsIgnoreCase("true")) this.dotOn = true; else this.dotOn = false;
    }

    public Color[] getDotColors() {
        return this.dotColors;
    }

    public void setDotColors(String[] dotColors) {
        this.dotColors = new Color[dotColors.length];
        for (int i = 0; i < dotColors.length; i++) {
            try {
                this.dotColors[i] = new Color(Integer.parseInt(dotColors[i], 16));
            } catch (Exception ex) {
            }
        }
    }

    public void setDotColors(Color[] colors) {
        this.dotColors = colors;
    }

    public int[] getDotSizes() {
        return this.dotSizes;
    }

    public void setDotSizes(int[] dotSizes) {
        this.dotSizes = dotSizes;
    }

    public void setDotSize(int dotSize) {
        this.dotSizes = new int[this.sData.length];
        for (int i = 0; i < this.dotSizes.length; i++) this.dotSizes[i] = dotSize;
    }

    public String[] getDotTypes() {
        return this.dotTypes;
    }

    public void setDotTypes(String[] dotTypes) {
        this.dotTypes = dotTypes;
    }

    public void setDotTypes(String dotType) {
        String[] dTypes = new String[this.sData.length];
        for (int i = 0; i < dTypes.length; i++) dTypes[i] = dotType;
        this.dotTypes = dTypes;
    }

    public boolean isLineOn() {
        return this.lineOn;
    }

    public void setLineOn(String lineOn) {
        if (lineOn.equalsIgnoreCase("true")) this.lineOn = true; else this.lineOn = false;
    }

    public Color[] getLineColors() {
        return this.lineColors;
    }

    public void setLineColors(String[] lineColors) {
        this.lineColors = new Color[lineColors.length];
        for (int i = 0; i < lineColors.length; i++) {
            try {
                this.lineColors[i] = new Color(Integer.parseInt(lineColors[i], 16));
            } catch (Exception ex) {
            }
        }
    }

    public void setLineColors(Color[] clr) {
        this.lineColors = clr;
    }

    public float[] getLineWidths() {
        return this.lineWidths;
    }

    public void setLineWidths(String[] lineWidths) {
        this.lineWidths = new float[lineWidths.length];
        for (int i = 0; i < lineWidths.length; i++) {
            try {
                this.lineWidths[i] = Float.parseFloat(lineWidths[i]);
            } catch (Exception ex) {
                this.lineWidths[i] = 1.0f;
            }
        }
    }

    public void setLineWidths(int lineWidth) {
        float[] lWidths = new float[this.sData.length];
        for (int i = 0; i < lWidths.length; i++) lWidths[i] = (float) lineWidth;
        this.lineWidths = lWidths;
    }

    public String[] getLineTypes() {
        return this.lineTypes;
    }

    public void setLineTypes(String[] lineTypes) {
        this.lineTypes = lineTypes;
    }

    public void setLineTypes(String lineType) {
        String[] lTypes = new String[this.sData.length];
        for (int i = 0; i < lTypes.length; i++) {
            lTypes[i] = lineType;
        }
        this.lineTypes = lTypes;
    }

    public int getTargetNum() {
        return this.targetNum;
    }

    public void setTargetNum(String targetNum) {
        try {
            this.targetNum = Integer.parseInt(targetNum);
        } catch (Exception ex) {
            this.targetNum = 1;
        }
    }

    public String[] getTargetTexts() {
        return this.targetTexts;
    }

    public void setTargetTexts(String[] targetTexts) {
        this.targetTexts = targetTexts;
    }

    public Color[] getTargetColors() {
        return this.targetColors;
    }

    public void setTargetColors(String[] targetColors) {
        this.targetColors = new Color[targetColors.length];
        for (int i = 0; i < targetColors.length; i++) {
            try {
                this.targetColors[i] = new Color(Integer.parseInt(targetColors[i], 16));
            } catch (Exception ex) {
            }
        }
    }

    public void setTargetColors(Color[] targetColors) {
        this.targetColors = targetColors;
    }

    public Font getTargetFont() {
        return this.targetFont;
    }

    public void setTargetFont(Font targetFont) {
        this.targetFont = targetFont;
    }

    public double[] getTargetDatas() {
        return this.targetDatas;
    }

    public void setTargetDatas(String[] targetDatas) {
        if (targetDatas == null) {
            this.targetDatas = null;
            return;
        }
        this.targetDatas = new double[targetDatas.length];
        for (int i = 0; i < this.targetDatas.length; i++) this.targetDatas[i] = Double.parseDouble(targetDatas[i]);
    }

    public double[][] getTargetEllips() {
        return this.targetEllips;
    }

    public void setTargetEllips(double[][] targetEllips) {
        this.targetEllips = targetEllips;
    }

    public boolean isXDataOn() {
        return this.xDataOn;
    }

    public void setXDataOn(String xDataOn) {
        if (xDataOn.equals("true")) this.xDataOn = true; else this.xDataOn = false;
    }

    public boolean getXDataOn() {
        return this.xDataOn;
    }

    public void setYDataOn(String yDataOn) {
        if (yDataOn.equals("true")) this.yDataOn = true; else this.yDataOn = false;
    }

    public boolean getYDataOn() {
        return this.yDataOn;
    }

    public boolean isXDataRotate() {
        return this.xDataRotate;
    }

    public void setXDataRotate(String xDataRotate) {
        if (xDataRotate.equalsIgnoreCase("true")) this.xDataRotate = true; else this.xDataRotate = false;
    }

    public boolean isXGridOn() {
        return this.xGridOn;
    }

    public void setXGridOn(String xGridOn) {
        if (xGridOn.equalsIgnoreCase("true")) this.xGridOn = true; else this.xGridOn = false;
    }

    public String getXForm() {
        return this.xForm;
    }

    public void setXForm(String xForm) {
        this.xForm = xForm;
    }

    public boolean isYGridOn() {
        return this.yGridOn;
    }

    public String getYForm() {
        return this.yForm;
    }

    public void setYForm(String yForm) {
        this.yForm = yForm;
    }

    public void setYGridOn(String yGridOn) {
        if (yGridOn.equalsIgnoreCase("true")) this.yGridOn = true; else this.yGridOn = false;
    }

    public Font getTitleFont() {
        return this.titleFont;
    }

    public void setTitleFont(Font titleFont) {
        this.titleFont = titleFont;
    }

    public Font getLabelFont() {
        return this.labelFont;
    }

    public void setLabelFont(Font labelFont) {
        this.labelFont = labelFont;
    }

    public String getLegendOpt() {
        return this.legendOpt;
    }

    public void setLegendOpt(String legendOpt) {
        this.legendOpt = legendOpt;
    }

    public String getLegendOpt2() {
        return this.legendOpt2;
    }

    public void setLegendOpt2(String legendOpt2) {
        this.legendOpt2 = legendOpt2;
    }

    public String getXTitle() {
        return sXTitle;
    }

    public void setXTitle(String value) {
        sXTitle = value;
    }

    public String getYTitle() {
        return sYTitle;
    }

    public void setYTitle(String value) {
        sYTitle = value;
    }

    public String[] getColumnNames() {
        if (!isValid()) return xReturn;
        if (xReturn != null) return xReturn;
        int nCols = getColumnCount();
        xReturn = new String[nCols];
        if (bHasFieldNames) {
            for (int i = 0; i < nCols; i++) xReturn[i] = (sData[0][i] == null) ? " " : sData[0][i];
        } else {
            for (int i = 0; i < nCols; i++) xReturn[i] = Integer.toString(i + 1);
        }
        return xReturn;
    }

    public String getGraphType() {
        return sChartType;
    }

    public void setGraphType(String sChartType) {
        this.sChartType = sChartType;
    }

    public String[] getRowNames() {
        if (!isValid()) return sReturn;
        if (sReturn != null) return sReturn;
        int nRows = getRowCount();
        sReturn = new String[nRows];
        if (bHasRowNames) {
            for (int i = 0; i < nRows; i++) sReturn[i] = (sData[i][0] == null) ? " " : sData[i][0];
        } else {
            for (int i = 0; i < nRows; i++) sReturn[i] = "�迭" + Integer.toString(i + 1);
        }
        return sReturn;
    }

    public Vector getCListItems() {
        Vector res = new Vector();
        res.add("����");
        res.add("�� ����");
        res.add("������1");
        res.add("������2");
        if (bHasRowNames) {
            for (int i = 0; i < getRowCount(); i++) res.add((sData[i][0] == null) ? " " : sData[i][0]);
            for (int i = 0; i < getRowCount(); i++) res.add((sData[i][0] == null) ? " " : sData[i][0] + " line");
            for (int i = 0; i < getRowCount(); i++) res.add((sData[i][0] == null) ? " " : sData[i][0] + " dot");
        } else {
            for (int i = 0; i < getRowCount(); i++) res.add("�迭 " + Integer.toString(i + 1));
            for (int i = 0; i < getRowCount(); i++) res.add("�迭 " + Integer.toString(i + 1) + " line");
            for (int i = 0; i < getRowCount(); i++) res.add("�迭 " + Integer.toString(i + 1) + " dot");
        }
        return res;
    }

    public String getAxisOpt() {
        return this.axisOpt;
    }

    public void setAxisOpt(String axisOpt) {
        this.axisOpt = axisOpt;
    }

    public void setChartDimention(String boundsData) {
    }

    public void setAvgLineColor(String avgLineColor) {
        if (avgLineColor != null) this.avgLineColor = avgLineColor; else this.avgLineColor = this.CLC_BLACK;
    }

    public String getAvgLineColor() {
        return this.avgLineColor;
    }

    public String getValsLineColor() {
        return this.valuesLineColor;
    }

    public void setValsLineColor(String valsLineColor) {
        this.valuesLineColor = valsLineColor;
    }

    public int getColumnCount() {
        return (isValid()) ? sData[0].length : -1;
    }

    public int getRowCount() {
        return (isValid()) ? sData.length : -1;
    }

    public String[][] getRawData() {
        return sData;
    }

    public double[][] getDataByDouble() {
        double[][] fReturn = null;
        if (!isValid()) return fReturn;
        int nRows;
        if (this.sChartType.equals(CT_SCATTER2D) && this.sData.length < 2) nRows = 2; else nRows = sData.length;
        int nCols = sData[0].length;
        fReturn = new double[nRows][nCols];
        if (this.sChartType.equals(CT_SCATTER2D) && this.sData.length < 2) {
            for (int j = 0; j < nCols; j++) {
                fReturn[0][j] = j + 1;
                try {
                    fReturn[0][j] = Double.parseDouble((sData[0][j] == null) ? "0" : sData[0][j]);
                } catch (NumberFormatException ex) {
                    fReturn[0][j] = 0.0;
                }
            }
        } else for (int i = 0; i < nRows; i++) for (int j = 0; j < nCols; j++) {
            try {
                fReturn[i][j] = Double.parseDouble((sData[i][j] == null) ? "0" : sData[i][j]);
            } catch (NumberFormatException ex) {
                fReturn[i][j] = 0.0;
            }
        }
        return fReturn;
    }

    public Rectangle2D getChartRect() {
        return this.tChartRect;
    }

    public void setChartRect(Rectangle2D chartRect) {
        this.tChartRect = chartRect;
    }

    public Rectangle2D getChartTitleRect() {
        return tChartTitleRect;
    }

    public void setChartTitleRect(Rectangle2D chartTitleRect) {
        this.tChartTitleRect = chartTitleRect;
    }

    public Rectangle2D getXTitleRect() {
        return tXTitleRect;
    }

    public void setXTitleRect(Rectangle2D xTitleRect) {
        this.tXTitleRect = xTitleRect;
    }

    public Rectangle2D getYTitleRect() {
        return tYTitleRect;
    }

    public void setYTitleRect(Rectangle2D yTitleRect) {
        this.tYTitleRect = yTitleRect;
    }

    public Rectangle2D getSeriesRect() {
        return tSeriesRect;
    }

    public void setSeriesRect(Rectangle2D seriesRect) {
        this.tSeriesRect = seriesRect;
    }

    public double getSlope() {
        return slope;
    }

    public void setSlope(double value) {
        slope = value;
    }

    public float getDepthFor3DY() {
        return depthFor3DY;
    }

    public void setDepthFor3DY(float value) {
        depthFor3DY = value;
    }

    public void setPieGap(double value) {
        pieGap = value;
    }

    public double getPieGap() {
        return pieGap;
    }

    public Point2D[] getPieValuePosition() {
        return pieValuePosition;
    }

    public void setPieValuePosition(Point2D[] value) {
        pieValuePosition = value;
    }

    public Line2D[] getPieValueLine() {
        return pieValueLine;
    }

    public void setPieValueLine(Line2D[] value) {
        pieValueLine = value;
    }

    public double getYMaxValue() {
        return yMaxValue;
    }

    public void setYMaxValue(double value) {
        yMaxValue = value;
    }

    public double getYMinValue() {
        return yMinValue;
    }

    public void setYMinValue(double value) {
        yMinValue = value;
    }

    public double getYInterval() {
        return yInterval;
    }

    public void setYInterval(double value) {
        yInterval = value;
    }

    public double getXMaxValue() {
        return xMaxValue;
    }

    public void setXMaxValue(double value) {
        xMaxValue = value;
    }

    public double getXMinValue() {
        return xMinValue;
    }

    public void setXMinValue(double value) {
        xMinValue = value;
    }

    public double getXInterval() {
        return xInterval;
    }

    public void setXInterval(double value) {
        xInterval = value;
    }

    public void setRowNames(String[] value) {
        sReturn = value;
    }

    public void setColumnNames(String[] values) {
        xReturn = values;
    }

    public boolean getTitleOn() {
        return this.titleOn;
    }

    public void setTitleOn(boolean titleOn) {
        this.titleOn = titleOn;
    }

    public void setSData(String[][] sData) {
        this.sData = sData;
    }

    public String[][] getSData() {
        return this.sData;
    }

    private void prepareOtherColors() {
        this.lineColors = new Color[this.tChartObjectColor.length];
        this.dotColors = new Color[this.tChartObjectColor.length];
        for (int i = 0; i < this.tChartObjectColor.length; i++) {
            this.lineColors[i] = this.tChartObjectColor[i].darker();
            this.lineColors[i] = this.lineColors[i].darker();
            this.lineColors[i] = this.lineColors[i].darker();
            this.dotColors[i] = this.tChartObjectColor[i].darker();
        }
        this.targetColors = new Color[2];
        this.targetColors[0] = Color.red;
        this.targetColors[1] = Color.blue;
    }

    public void setColor(Color[] tgtColors, int index, Color color) {
        tgtColors[index] = color;
    }

    public void setSeriesType(String msg) {
        sSeriesType = msg;
    }

    public String getSeriesType() {
        return this.sSeriesType;
    }

    public boolean hasColumnNames() {
        return bHasFieldNames;
    }

    public void setHasColumnNames(boolean has) {
        this.bHasFieldNames = has;
    }

    public boolean hasRowNames() {
        return bHasRowNames;
    }

    public void setHasRowNames(boolean has) {
        this.bHasRowNames = has;
    }

    public void setQueryName(String queryName) {
        this.queryName = queryName;
    }

    public void setSeriesField(String seriesField) {
        this.seriesField = seriesField;
    }

    public void setXAxisField(String xAxisField) {
        this.xAxisField = xAxisField;
    }

    public void setDataValueField(String dataValueField) {
        this.dataValueField = dataValueField;
    }

    public String getQueryName() {
        return queryName;
    }

    public String getSeriesField() {
        return seriesField;
    }

    public String getXAxisField() {
        return xAxisField;
    }

    public String getDataValueField() {
        return dataValueField;
    }

    public static double[][] derivateDoubles(String xDParam) {
        double[][] resultDoubles;
        try {
            String tempStr = new String(xDParam);
            Vector tempVec = new Vector();
            Vector resultVec = new Vector();
            tempVec = split1(xDParam);
            for (int i = 0; i < tempVec.size(); i++) {
                resultVec.add(split2((String) tempVec.get(i), ','));
            }
            resultDoubles = new double[resultVec.size()][((Vector) resultVec.get(0)).size()];
            for (int i = 0; i < resultVec.size(); i++) {
                for (int j = 0; j < ((Vector) resultVec.get(0)).size(); j++) {
                    resultDoubles[i][j] = Double.parseDouble(((String) ((Vector) (resultVec.get(i))).get(j)).trim());
                }
            }
            return resultDoubles;
        } catch (Exception ex) {
            return resultDoubles = null;
        }
    }

    public static int[][] derivateIntArrays(String xDParam) {
        String tempStr = new String(xDParam);
        Vector tempVec = new Vector();
        Vector resultVec = new Vector();
        tempVec = split1(xDParam);
        for (int i = 0; i < tempVec.size(); i++) {
            resultVec.add(split2((String) tempVec.get(i), ','));
        }
        int[][] resultInts;
        resultInts = new int[resultVec.size()][((Vector) resultVec.get(0)).size()];
        for (int i = 0; i < resultVec.size(); i++) {
            for (int j = 0; j < ((Vector) resultVec.get(0)).size(); j++) {
                resultInts[i][j] = Integer.parseInt(((String) ((Vector) (resultVec.get(i))).get(j)).trim());
            }
        }
        return resultInts;
    }

    public static int[] derivateIntArray(String param) {
        Vector tempVec = new Vector();
        tempVec = split2(param, ',');
        int[] resultInts;
        resultInts = new int[tempVec.size()];
        for (int i = 0; i < tempVec.size(); i++) {
            resultInts[i] = Integer.parseInt(((String) tempVec.get(i)).trim());
        }
        return resultInts;
    }

    public static String[] derivateString(String yDParam) {
        Vector tempVec = new Vector();
        if (yDParam == null) return null;
        tempVec = split2(yDParam, ',');
        String[] yData = new String[tempVec.size()];
        for (int i = 0; i < tempVec.size(); i++) {
            yData[i] = (((String) tempVec.get(i)).trim());
        }
        return yData;
    }

    public static Vector split2(String inputParam, char splitter) {
        int startIndex, endIndex;
        String tempString = inputParam;
        String searchedStr = new String();
        Vector splittedVec = new Vector();
        while (true) {
            if ((endIndex = tempString.indexOf(splitter)) == -1) {
                splittedVec.add(tempString);
                break;
            }
            searchedStr = tempString.substring(0, endIndex);
            splittedVec.add(searchedStr);
            tempString = tempString.substring(endIndex + 1);
        }
        return splittedVec;
    }

    public static Vector split1(String array) {
        int startIndex;
        int endIndex;
        String tempString = new String(array);
        String searchedStr = new String();
        Vector splittedVec = new Vector();
        while (true) {
            if ((startIndex = tempString.indexOf('{')) == -1) break;
            endIndex = tempString.indexOf('}');
            searchedStr = tempString.substring(startIndex + 1, endIndex);
            splittedVec.add(searchedStr);
            tempString = tempString.substring(endIndex + 1);
        }
        return splittedVec;
    }

    public static Font derivateFont(String[] fontData) {
        Font resultFont;
        try {
            String name = fontData[0];
            int size = Integer.parseInt(fontData[2]);
            int style;
            if (fontData[1].equalsIgnoreCase("bold")) style = Font.BOLD; else if (fontData[1].equalsIgnoreCase("italic")) style = Font.ITALIC; else if (fontData[1].equalsIgnoreCase("roman_baseline")) style = Font.ROMAN_BASELINE; else if (fontData[1].equalsIgnoreCase("center_baseline")) style = Font.CENTER_BASELINE; else if (fontData[1].equalsIgnoreCase("truetype_font")) style = Font.TRUETYPE_FONT; else if (fontData[1].equalsIgnoreCase("hanging_baseline")) style = Font.HANGING_BASELINE; else if (fontData[1].equalsIgnoreCase("plain")) style = Font.PLAIN; else style = Font.PLAIN;
            resultFont = new Font(name, style, size);
            return resultFont;
        } catch (Exception ex) {
            return resultFont = new Font("����", Font.PLAIN, 12);
        }
    }

    public static Color derivateColor(String colorVal) {
        Color resultColor;
        if (colorVal.equalsIgnoreCase(RDGModel.CLC_BLUE)) resultColor = Color.blue; else if (colorVal.equalsIgnoreCase(RDGModel.CLC_CYAN)) resultColor = Color.cyan; else if (colorVal.equalsIgnoreCase(RDGModel.CLC_GREEN)) resultColor = Color.green; else if (colorVal.equalsIgnoreCase(RDGModel.CLC_MAGENTA)) resultColor = Color.magenta; else if (colorVal.equalsIgnoreCase(RDGModel.CLC_ORANGE)) resultColor = Color.orange; else if (colorVal.equalsIgnoreCase(RDGModel.CLC_PINK)) resultColor = Color.pink; else if (colorVal.equalsIgnoreCase(RDGModel.CLC_RED)) resultColor = Color.red; else if (colorVal.equalsIgnoreCase(RDGModel.CLC_YELLOW)) resultColor = Color.yellow; else if (colorVal.equalsIgnoreCase(RDGModel.CLC_GRAY)) resultColor = Color.gray; else if (colorVal.equalsIgnoreCase(RDGModel.CLC_DARKGRAY)) resultColor = Color.darkGray; else if (colorVal.equalsIgnoreCase(RDGModel.CLC_LIGHTGRAY)) resultColor = Color.lightGray; else if (colorVal.equalsIgnoreCase(RDGModel.CLC_WHITE)) resultColor = Color.white; else resultColor = Color.black;
        return resultColor;
    }

    public static String applyNumberForm(String in, String form) {
        String out;
        int pIndex = form.indexOf('.');
        if (pIndex == form.length() - 1) return out = Math.round(new Double(in).doubleValue()) + "";
        int rearLength = form.length() - pIndex - 1;
        if (in.indexOf('.') == -1) {
            char[] rear = new char[rearLength];
            for (int i = 0; i < rear.length; i++) rear[i] = '0';
            out = in + "." + new String(rear);
        } else {
            if (in.indexOf('.') + rearLength < in.length()) out = in.substring(0, in.indexOf('.')) + in.substring(in.indexOf('.'), in.indexOf('.') + rearLength + 1); else {
                char[] addZeroz = new char[in.indexOf('.') + rearLength - in.length() + 1];
                for (int i = 0; i < addZeroz.length; i++) addZeroz[i] = '0';
                out = in + new String(addZeroz);
            }
        }
        return out;
    }

    public static String applyDefaultForm(String in) {
        String out;
        int pIndex = in.indexOf('.');
        String defaultForm = "#.";
        for (int i = pIndex + 1; i < in.length(); i++) {
            defaultForm += "#";
            if (in.charAt(i) != '0') break;
        }
        out = RDGModel.applyNumberForm(in, defaultForm + "#");
        return out;
    }

    public void changeSData() {
        String[][] fTempData = new String[sData[0].length][sData.length];
        for (int i = 0; i < sData.length; i++) for (int j = 0; j < sData[0].length; j++) fTempData[j][i] = sData[i][j];
        sData = fTempData;
    }
}
