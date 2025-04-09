package com.rbnb.plot;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.util.Hashtable;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import com.rbnb.utility.ToString;
import com.rbnb.utility.ByteConvert;
import com.rbnb.utility.KeyValueHash;

public class PlotContainer extends JComponent {

    public static final int ShortData = 1;

    public static final int FloatData = 2;

    public static final int DoubleData = 3;

    public static final int IntData = 4;

    private boolean fourByteIsFloat = true;

    private boolean beenPainted = false;

    private Dimension size = null;

    private String plottitle = null;

    private PlotTitle pTitle = null;

    private PlotAbscissa pAbscissa = null;

    private String absLabel = new String("");

    private PlotOrdinate pOrdinate = null;

    private PlotArea pArea = null;

    private int ordMode = FloatData;

    private boolean beenScaled = false;

    private double scaleMin = 0.0, scaleMax = 1.0;

    private int scaleDiv = 5;

    private boolean scaleAuto = true;

    private boolean scaleDecrease = false;

    private int displayMode;

    private String tableUnits = null;

    private double tableFirst = 0;

    private double tableLast = 0;

    private double tableMin = 0;

    private double tableMax = 0;

    private double tableAve = 0;

    private double tableStdDev = 0;

    private Dimension oldSize = new Dimension(0, 0);

    private boolean newData = true;

    private Image bufferImage = null;

    private FontMetrics fm = getFontMetrics(Environment.FONT12);

    private int lastUnknownPointSize = 0;

    private byte[] oldUserData = null;

    private float oldScale = 1;

    private float oldOffset = 0;

    private float preScale = 1;

    private float preOffset = 0;

    private boolean preScaleOffset = false;

    private Environment environment = null;

    private PosDurCubby posDurCubby = null;

    private Dimension prefSize = new Dimension(100, 100);

    private boolean showTitle = false;

    public PlotContainer(RegChannel rc, int dm, PosDurCubby pdc, Environment e) {
        this(rc, dm, pdc, e, false);
    }

    public PlotContainer(RegChannel rc, int dm, PosDurCubby pdc, Environment e, boolean showTitleI) {
        showTitle = showTitleI;
        plottitle = rc.name;
        displayMode = dm;
        environment = e;
        posDurCubby = pdc;
        if (environment.FOURBYTEASINTEGER) fourByteIsFloat = false;
        setLayout(new BorderLayout());
        add(pTitle = new PlotTitle(plottitle), BorderLayout.NORTH);
        pTitle.setVisible(showTitle);
        add(pAbscissa = new PlotAbscissa(absLabel, pdc, environment), BorderLayout.SOUTH);
        add(pOrdinate = new PlotOrdinate(pAbscissa), BorderLayout.WEST);
        add(pArea = new PlotArea(this, e.SCROLLGRIDLINES), BorderLayout.CENTER);
        if (displayMode != LayoutCubby.PlotMode) {
            pTitle.setVisible(false);
            pAbscissa.setVisible(false);
            pOrdinate.setVisible(false);
            pArea.setVisible(false);
        }
        if (environment.SCALE_DIV != -1) {
            scaleAuto = false;
            beenScaled = true;
            scaleMin = environment.SCALE_MIN;
            scaleMax = environment.SCALE_MAX;
            scaleDiv = environment.SCALE_DIV;
        }
        setOrdinate(scaleMin, scaleMax, scaleDiv);
        if (rc.isStaticUserData((short) 1)) {
            KeyValueHash kvpairs = new KeyValueHash(rc.staticUserData);
            String value = (String) kvpairs.get("scale");
            if (value != null) {
                preScale = (new Float(value)).floatValue();
                preScaleOffset = true;
            }
            value = (String) kvpairs.get("offset");
            if (value != null) {
                preOffset = (new Float(value)).floatValue();
                preScaleOffset = true;
            }
            value = (String) kvpairs.get("units");
            if (value != null) {
                pAbscissa.setOrdUnits(value);
            }
            tableUnits = value;
        } else if (rc.isStaticUserData((short) 2)) {
            fourByteIsFloat = false;
        }
    }

    public PlotContainer() {
    }

    public synchronized void getConfig(Hashtable ht, String prefix) {
        ht.put(prefix + "name", plottitle);
        if (scaleAuto && scaleDecrease) ht.put(prefix + "scaling", new String("auto_inc_dec")); else if (scaleAuto) ht.put(prefix + "scaling", new String("auto_inc")); else ht.put(prefix + "scaling", new String("manual"));
        ht.put(prefix + "divisions", Integer.toString(scaleDiv));
        ht.put(prefix + "min", Double.toString(scaleMin));
        ht.put(prefix + "max", Double.toString(scaleMax));
    }

    public synchronized void setConfig(Hashtable ht, String prefix) {
        if (ht.containsKey(prefix + "name")) {
            plottitle = (String) ht.get(prefix + "name");
            if (pTitle != null) pTitle.setTitle(plottitle);
        }
        if (ht.containsKey(prefix + "scaling")) {
            String scaleString = (String) ht.get(prefix + "scaling");
            if (scaleString.equals("auto_inc_dec")) {
                scaleAuto = true;
                scaleDecrease = true;
            } else if (scaleString.equals("auto_inc")) {
                scaleAuto = true;
                scaleDecrease = false;
            } else scaleAuto = false;
        }
        if (ht.containsKey(prefix + "divisions")) scaleDiv = Integer.parseInt((String) ht.get(prefix + "divisions"));
        if (ht.containsKey(prefix + "min")) scaleMin = (new Double((String) ht.get(prefix + "min"))).doubleValue();
        if (ht.containsKey(prefix + "max")) scaleMax = (new Double((String) ht.get(prefix + "max"))).doubleValue();
        beenScaled = true;
        setOrdinate(scaleMin, scaleMax, scaleDiv);
    }

    public void setDisplayMode(int dm) {
        displayMode = dm;
        if (displayMode == LayoutCubby.TableMode) {
            pTitle.setVisible(false);
            pAbscissa.setVisible(false);
            pOrdinate.setVisible(false);
            pArea.setVisible(false);
        } else if (displayMode == LayoutCubby.PlotMode) {
            pTitle.setVisible(showTitle);
            pAbscissa.setVisible(true);
            pOrdinate.setVisible(true);
            pArea.setVisible(true);
            invalidate();
            validate();
        } else System.out.println("PlotContainer.setDisplayMode: unknown mode: " + displayMode);
        repaint();
    }

    public Hashtable getParameters() {
        Hashtable ht = new Hashtable();
        ht.put("divisions", new Integer(scaleDiv));
        ht.put("min", new Double(scaleMin));
        ht.put("max", new Double(scaleMax));
        ht.put("autoscale", new Boolean(scaleAuto));
        ht.put("autodecrease", new Boolean(scaleDecrease));
        return ht;
    }

    public void setParameters(Hashtable ht) {
        scaleAuto = ((Boolean) ht.get("autoscale")).booleanValue();
        scaleDecrease = ((Boolean) ht.get("autodecrease")).booleanValue();
        scaleDiv = ((Integer) ht.get("divisions")).intValue();
        scaleMin = ((Double) ht.get("min")).doubleValue();
        scaleMax = ((Double) ht.get("max")).doubleValue();
        beenScaled = true;
        setOrdinate(scaleMin, scaleMax, scaleDiv);
    }

    public void setZoom(double start, double duration) {
        posDurCubby.setZoom(start, duration);
    }

    public void setAbscissa(Time duration) {
        synchronized (this) {
            beenPainted = false;
        }
        pAbscissa.setAbscissa(duration);
        pArea.setAbscissa(duration);
    }

    public void setOrdinate(double min, double max, int numLines) {
        synchronized (this) {
            beenPainted = false;
        }
        pOrdinate.setOrdinate(min, max, numLines);
        pArea.setOrdinate(min, max, numLines);
    }

    private boolean byteArrayCompare(byte[] a, byte[] b) {
        if (a == null || b == null) return false;
        if (a.length != b.length) return false;
        for (int i = 0; i < a.length; i++) if (a[i] != b[i]) return false;
        return true;
    }

    public void setChannelData(Channel ch, Time start) {
        KeyValueHash kvpairs = null;
        boolean scaleOffset = false;
        float scale = 1, offset = 0;
        synchronized (this) {
            beenPainted = false;
        }
        if (preScaleOffset) {
            scale = preScale;
            offset = preOffset;
            scaleOffset = true;
        }
        if (ch.channelUserDataType == 1) {
            if (byteArrayCompare(oldUserData, ch.channelUserData)) {
                scaleOffset = true;
                scale = oldScale;
                offset = oldOffset;
            } else {
                kvpairs = new KeyValueHash(ch.channelUserData);
                String value = (String) kvpairs.get("scale");
                if (value != null) {
                    scale = (new Float(value)).floatValue();
                    scaleOffset = true;
                }
                value = (String) kvpairs.get("offset");
                if (value != null) {
                    offset = (new Float(value)).floatValue();
                    scaleOffset = true;
                }
                value = (String) kvpairs.get("units");
                if (value != null) {
                    pAbscissa.setOrdUnits(value);
                }
                tableUnits = value;
            }
            oldUserData = ch.channelUserData;
            oldScale = scale;
            oldOffset = offset;
        } else if (ch.channelUserDataType == 2) {
            fourByteIsFloat = false;
        }
        double[] absData = null;
        if (ch.numberOfPoints < 1) {
            pArea.setNoData();
            pAbscissa.setStartNum(start, 0);
            return;
        }
        try {
            if (displayMode != LayoutCubby.TableMode) {
                absData = ch.timeStamp.getTimesDouble(0, ch.numberOfPoints);
            }
        } catch (Exception e) {
            System.out.println("timesDouble exception:");
            e.printStackTrace();
        }
        short[] ordShortData = null;
        int[] ordIntData = null;
        float[] ordFloatData = null;
        double[] ordDoubleData = null;
        if (ch.isInt16) {
            boolean byteSwap = false;
            if (ch.byteOrder == Channel.LSB) byteSwap = true;
            ordShortData = ch.getDataInt16();
            if (scaleOffset) {
                ordFloatData = new float[ordShortData.length];
                for (int i = 0; i < ordFloatData.length; i++) {
                    ordFloatData[i] = scale * ordShortData[i] + offset;
                }
                ordMode = FloatData;
            } else {
                ordMode = ShortData;
                newData = true;
            }
        } else if (ch.isFloat32) {
            boolean byteSwap = false;
            if (ch.byteOrder == Channel.LSB) byteSwap = true;
            ordFloatData = ch.getDataFloat32();
            ordMode = FloatData;
            if (scaleOffset) for (int i = 0; i < ordFloatData.length; i++) {
                ordFloatData[i] = scale * ordFloatData[i] + offset;
            }
        } else if (ch.isInt32) {
            boolean byteSwap = false;
            if (ch.byteOrder == Channel.LSB) byteSwap = true;
            ordIntData = ch.getDataInt32();
            ordMode = IntData;
            if (scaleOffset) {
                ordFloatData = new float[ordIntData.length];
                for (int i = 0; i < ordFloatData.length; i++) {
                    ordFloatData[i] = scale * ordIntData[i] + offset;
                }
                ordMode = FloatData;
            }
        } else if (ch.isInt8) {
            byte[] temp = ch.getDataInt8();
            if (scaleOffset) {
                ordFloatData = new float[temp.length];
                for (int i = 0; i < ordFloatData.length; i++) {
                    ordFloatData[i] = scale * temp[i] + offset;
                }
                ordMode = FloatData;
            } else {
                ordShortData = new short[temp.length];
                for (int i = 0; i < temp.length; ++i) ordShortData[i] = (short) temp[i];
                ordMode = ShortData;
                newData = true;
            }
        } else if (ch.isFloat64) {
            boolean byteSwap = false;
            if (ch.byteOrder == Channel.LSB) byteSwap = true;
            ordDoubleData = ch.getDataFloat64();
            ordMode = DoubleData;
            if (scaleOffset) for (int i = 0; i < ordDoubleData.length; i++) {
                ordDoubleData[i] = scale * ordDoubleData[i] + offset;
            }
        } else if (ch.isByteArray && "audio/basic".equals(ch.getMimeType())) {
            int length = 0;
            byte[][] dba = ch.getDataByteArray();
            for (int ii = 0; ii < dba.length; ++ii) length += dba[ii].length;
            if (scaleOffset) {
                ordFloatData = new float[length];
                int c = 0;
                for (int ii = 0; ii < dba.length; ++ii) for (int iii = 0; iii < dba[ii].length; ++iii) ordFloatData[c++] = scale * (dba[ii][iii] & 0xff) + offset;
                ordMode = FloatData;
            } else {
                ordShortData = new short[length];
                int c = 0;
                for (int ii = 0; ii < dba.length; ++ii) for (int iii = 0; iii < dba[ii].length; ++iii) ordShortData[c++] = (short) (dba[ii][iii] & 0xff);
                ordMode = ShortData;
                newData = true;
            }
            double[] times = new double[length];
            int c = 0;
            for (int ii = 0; ii < dba.length - 1; ++ii) {
                for (int iii = 0; iii < dba[ii].length; ++iii) {
                    times[c] = absData[ii] + (absData[ii + 1] - absData[ii]) * iii / dba[ii].length;
                    ++c;
                }
            }
            for (int iii = 0; iii < dba[dba.length - 1].length; ++iii) {
                times[c] = absData[absData.length - 1] + iii / 8000.0;
                ++c;
            }
            absData = times;
        } else {
            if (ch.pointSize != lastUnknownPointSize) {
                lastUnknownPointSize = ch.pointSize;
                System.err.println("PlotContainer.setChannelData: unknown type in channel " + ch.channelName);
            }
            pArea.setNoData();
            pAbscissa.setStartNum(start, 0);
            return;
        }
        double min = 0, max = 0, sum = 0, sumsq = 0;
        if (ordMode == DoubleData) {
            if (scaleAuto || displayMode == LayoutCubby.TableMode) {
                min = ordDoubleData[0];
                max = min;
                if (displayMode == LayoutCubby.TableMode) {
                    sum = min;
                    sumsq = min * min;
                }
                for (int i = 1; i < ordDoubleData.length; i++) {
                    if (ordDoubleData[i] < min) min = ordDoubleData[i]; else if (ordDoubleData[i] > max) max = ordDoubleData[i];
                    if (displayMode == LayoutCubby.TableMode) {
                        sum += ordDoubleData[i];
                        sumsq += ordDoubleData[i] * ordDoubleData[i];
                    }
                }
                if (displayMode != LayoutCubby.TableMode) {
                    if (scaleDecrease || !beenScaled) autoScale(min, max); else if ((min < scaleMin) || (max > scaleMax)) autoScale(min, max);
                }
            }
            if (displayMode == LayoutCubby.TableMode) {
                tableFirst = ordDoubleData[0];
                tableLast = ordDoubleData[ordDoubleData.length - 1];
                tableMin = min;
                tableMax = max;
                tableAve = sum / ordDoubleData.length;
                tableStdDev = Math.sqrt((ordDoubleData.length * sumsq - sum * sum) / (ordDoubleData.length * ordDoubleData.length));
            } else if (displayMode == LayoutCubby.PlotMode) {
                pArea.setDoubleData(absData, ordDoubleData, start.getDoubleValue());
                pAbscissa.setStartNum(start, ordDoubleData.length);
            }
        } else if (ordMode == FloatData) {
            if (scaleAuto || displayMode == LayoutCubby.TableMode) {
                float minF = ordFloatData[0];
                float maxF = minF;
                if (displayMode == LayoutCubby.TableMode) {
                    sum = minF;
                    sumsq = minF * minF;
                }
                for (int i = 1; i < ordFloatData.length; i++) {
                    if (ordFloatData[i] < minF) minF = ordFloatData[i]; else if (ordFloatData[i] > maxF) maxF = ordFloatData[i];
                    if (displayMode == LayoutCubby.TableMode) {
                        sum += ordFloatData[i];
                        sumsq += ordFloatData[i] * ordFloatData[i];
                    }
                }
                try {
                    min = (new Double(Float.toString(minF))).doubleValue();
                    max = (new Double(Float.toString(maxF))).doubleValue();
                } catch (NumberFormatException e) {
                    min = minF;
                    max = maxF;
                }
                if (displayMode != LayoutCubby.TableMode) {
                    if (scaleDecrease || !beenScaled) autoScale(min, max); else if ((min < scaleMin) || (max > scaleMax)) autoScale(min, max);
                }
            }
            if (displayMode == LayoutCubby.TableMode) {
                tableFirst = ordFloatData[0];
                tableLast = ordFloatData[ordFloatData.length - 1];
                tableMin = min;
                tableMax = max;
                tableAve = sum / ordFloatData.length;
                tableStdDev = Math.sqrt((ordFloatData.length * sumsq - sum * sum) / (ordFloatData.length * ordFloatData.length));
            } else if (displayMode == LayoutCubby.PlotMode) {
                pArea.setFloatData(absData, ordFloatData, start.getDoubleValue());
                pAbscissa.setStartNum(start, ordFloatData.length);
            }
        } else if (ordMode == IntData) {
            if (scaleAuto || displayMode == LayoutCubby.TableMode) {
                min = ordIntData[0];
                max = min;
                if (displayMode == LayoutCubby.TableMode) {
                    sum = min;
                    sumsq = min * min;
                }
                for (int i = 1; i < ordIntData.length; i++) {
                    if (ordIntData[i] < min) min = ordIntData[i]; else if (ordIntData[i] > max) max = ordIntData[i];
                    if (displayMode == LayoutCubby.TableMode) {
                        sum += ordIntData[i];
                        sumsq += ordIntData[i] * ordIntData[i];
                    }
                }
                if (displayMode != LayoutCubby.TableMode) {
                    if (scaleDecrease || !beenScaled) autoScale(min, max); else if ((min < scaleMin) || (max > scaleMax)) autoScale(min, max);
                }
            }
            if (displayMode == LayoutCubby.TableMode) {
                tableFirst = ordIntData[0];
                tableLast = ordIntData[ordIntData.length - 1];
                tableMin = min;
                tableMax = max;
                tableAve = sum / ordIntData.length;
                tableStdDev = Math.sqrt((ordIntData.length * sumsq - sum * sum) / (ordIntData.length * ordIntData.length));
            } else if (displayMode == LayoutCubby.PlotMode) {
                pArea.setIntData(absData, ordIntData, start.getDoubleValue());
                pAbscissa.setStartNum(start, ordIntData.length);
            }
        } else if (ordMode == ShortData) {
            if (scaleAuto || displayMode == LayoutCubby.TableMode) {
                min = ordShortData[0];
                max = min;
                if (displayMode == LayoutCubby.TableMode) {
                    sum = min;
                    sumsq = min * min;
                }
                for (int i = 1; i < ordShortData.length; i++) {
                    if (ordShortData[i] < min) min = ordShortData[i];
                    if (ordShortData[i] > max) max = ordShortData[i];
                    if (displayMode == LayoutCubby.TableMode) {
                        sum += ordShortData[i];
                        sumsq += ordShortData[i] * ordShortData[i];
                    }
                }
                if (displayMode != LayoutCubby.TableMode) {
                    if (scaleDecrease || !beenScaled) autoScale(min, max); else if ((min < scaleMin) || (max > scaleMax)) autoScale(min, max);
                }
            }
            if (displayMode == LayoutCubby.TableMode) {
                tableFirst = ordShortData[0];
                tableLast = ordShortData[ordShortData.length - 1];
                tableMin = min;
                tableMax = max;
                tableAve = sum / ordShortData.length;
                tableStdDev = Math.sqrt((ordShortData.length * sumsq - sum * sum) / (ordShortData.length * ordShortData.length));
            } else if (displayMode == LayoutCubby.PlotMode) {
                pArea.setShortData(absData, ordShortData, start.getDoubleValue());
                pAbscissa.setStartNum(start, ordShortData.length);
            }
        } else System.out.println("PlotContainer.setChannelData: unknown ordMode " + ordMode);
        newData = true;
        repaint();
    }

    private void autoScale(double min, double max) {
        double powmin = 0, powmax = 0, pow = 0, lower = 0, upper = 0;
        int num = 0;
        double realmin = min;
        double realmax = max;
        double thisScaleMin = scaleMin;
        double thisScaleMax = scaleMax;
        double offset = 0;
        boolean rescale = true;
        int rescaleCount = 0;
        while (rescale) {
            double thisMin = min;
            double thisMax = max;
            if (min == 0 && max == 0 && offset == 0) {
                setOrdinate(0., 1., 5);
                return;
            } else if (min == 0 && max == 0) {
                pow = 0;
                min = -1e-2;
                max = 1e-2;
            } else if (min == 0) pow = Math.floor(Math.log(Math.abs(max)) / Math.log(10)); else if (max == 0) pow = Math.floor(Math.log(Math.abs(min)) / Math.log(10)); else {
                powmin = Math.floor(Math.log(Math.abs(min)) / Math.log(10));
                if (beenScaled && !scaleDecrease) powmin = Math.max(powmin, Math.floor(Math.log(Math.abs(thisScaleMin)) / Math.log(10)));
                powmax = Math.floor(Math.log(Math.abs(max)) / Math.log(10));
                if (beenScaled && !scaleDecrease) powmax = Math.max(powmax, Math.floor(Math.log(Math.abs(thisScaleMax)) / Math.log(10)));
                pow = Math.max(powmin, powmax);
            }
            if (min == max) {
                min -= Math.abs(min / 10);
                max += Math.abs(max / 10);
            }
            min = min / Math.pow(10.0, pow);
            max = max / Math.pow(10.0, pow);
            if (min != 0) min -= min * 1e-4;
            if (max != 0) max -= max * 1e-4;
            if (min >= 0) {
                if (min < 1) lower = 0.; else if (min < 2) lower = 1. * Math.pow(10.0, pow); else if (min < 5) lower = 2. * Math.pow(10.0, pow); else lower = 5. * Math.pow(10.0, pow);
            } else {
                if (min >= -1) lower = -1. * Math.pow(10.0, pow); else if (min >= -2) lower = -2. * Math.pow(10.0, pow); else if (min >= -5) lower = -5. * Math.pow(10.0, pow); else lower = -1. * Math.pow(10.0, pow + 1);
            }
            if (beenScaled && !scaleDecrease) lower = Math.min(lower, thisScaleMin);
            if (max > 0) {
                if (max > 5) upper = Math.pow(10.0, pow + 1); else if (max > 2) upper = 5. * Math.pow(10.0, pow); else if (max > 1) upper = 2. * Math.pow(10.0, pow); else upper = Math.pow(10.0, pow);
            } else {
                if (max > -1) upper = 0.; else if (max > -2) upper = -1. * Math.pow(10.0, pow); else if (max > -5) upper = -2. * Math.pow(10.0, pow); else upper = -5. * Math.pow(10.0, pow);
            }
            if (beenScaled && !scaleDecrease) upper = Math.max(upper, thisScaleMax);
            num = (int) Math.round((upper - lower) / Math.pow(10.0, pow));
            if (num < 3) num *= 4; else if (num < 5) num *= 2; else if (num > 10 && num % 2 == 0) num /= 2; else if (num == 15) num = 6;
            boolean doZoom = true;
            if (beenScaled && !scaleDecrease) doZoom = false;
            if (rescaleCount++ >= 2) doZoom = false;
            if ((upper - lower) / (realmax - realmin) < 10) doZoom = false;
            if (doZoom) {
                double ave = (thisMax + thisMin) / 2;
                double[] gridline = new double[num + 1];
                double gridSpace = (upper - lower) / num;
                double delta = Double.MAX_VALUE;
                int gridnum = 0;
                for (int i = 0; i < num + 1; i++) {
                    gridline[i] = lower + i * gridSpace;
                    double thisDelta = Math.abs(ave - gridline[i]);
                    if (thisDelta < delta) {
                        delta = thisDelta;
                        gridnum = i;
                    }
                }
                min = thisMin - gridline[gridnum];
                max = thisMax - gridline[gridnum];
                thisScaleMin -= gridline[gridnum];
                thisScaleMax -= gridline[gridnum];
                offset += gridline[gridnum];
                if (rescaleCount > 2) rescale = false;
            } else {
                rescale = false;
            }
        }
        lower += offset;
        upper += offset;
        beenScaled = true;
        scaleMin = lower;
        scaleMax = upper;
        scaleDiv = num;
        setOrdinate(scaleMin, scaleMax, scaleDiv);
    }

    public void update(Graphics g) {
        System.out.println("PlotContainer.update()");
        paint(g);
    }

    public void paint(Graphics g) {
        if (displayMode == LayoutCubby.PlotMode) {
        } else if (displayMode == LayoutCubby.TableMode) {
            boolean newSize = false;
            Dimension size = getSize();
            if (size.width != oldSize.width || size.height != oldSize.height) {
                newSize = true;
                oldSize.width = size.width;
                oldSize.height = size.height;
            }
            if (newSize || newData) {
                if (newSize) bufferImage = createImage(size.width, size.height);
                Graphics bi = bufferImage.getGraphics();
                if (!newSize) bi.clearRect(0, 0, size.width - 1, size.height - 1);
                try {
                    int block = size.width / 7;
                    int fh = fm.getAscent();
                    int dh = fh;
                    int cw = fm.charWidth('0');
                    int fw = block / cw - 1;
                    if (fw > 12) fw = 12;
                    String number = null;
                    bi.setColor(Color.white);
                    for (int i = 1; i < 7; i += 2) {
                        bi.fillRect(i * block, 0, block, size.height);
                    }
                    bi.setColor(Color.lightGray);
                    for (int i = 0; i < 7; i += 2) {
                        bi.fillRect(i * block, 0, block, size.height);
                    }
                    bi.setColor(Color.black);
                    bi.setFont(Environment.FONT12);
                    if (tableUnits != null) bi.drawString(tableUnits, 0, dh);
                    number = ToString.toString("%-*" + fw + "g", tableFirst);
                    bi.drawString(number, block, dh);
                    number = ToString.toString("%-*" + fw + "g", tableLast);
                    bi.drawString(number, 2 * block, dh);
                    number = ToString.toString("%-*" + fw + "g", tableMin);
                    bi.drawString(number, 3 * block, dh);
                    number = ToString.toString("%-*" + fw + "g", tableMax);
                    bi.drawString(number, 4 * block, dh);
                    number = ToString.toString("%-*" + fw + "g", tableAve);
                    bi.drawString(number, 5 * block, dh);
                    number = ToString.toString("%-*" + fw + "g", tableStdDev);
                    bi.drawString(number, 6 * block, dh);
                } catch (Exception e) {
                    System.out.println("PlotContainer.paint: exception " + e);
                    e.printStackTrace();
                }
                newData = false;
            }
            g.drawImage(bufferImage, 0, 0, null);
        }
        super.paint(g);
        synchronized (this) {
            beenPainted = true;
        }
    }

    public synchronized boolean hasBeenPainted() {
        if (displayMode == LayoutCubby.TableMode) repaint();
        return beenPainted;
    }
}
