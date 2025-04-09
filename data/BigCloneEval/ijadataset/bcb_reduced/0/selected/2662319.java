package me.buick.util.jmeter.snmpvisualizers;

import java.awt.AWTEvent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Locale;
import javax.accessibility.AccessibleRole;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import static me.buick.util.jmeter.snmpvisualizers.SNMPFormatter.*;
import static me.buick.util.jmeter.snmpvisualizers.SNMPResources.*;

public class SNMPPlotter extends JComponent {

    public static enum Unit {

        NONE, BYTES, KBYTES, PERCENT, MIlLISECONDS, PERSECOND
    }

    static final int[] rangeValues = { 1, 5, 10, 30, 1 * 60, 2 * 60, 3 * 60, 6 * 60, 12 * 60, 1 * 24 * 60, 7 * 24 * 60, 1 * 31 * 24 * 60, 3 * 31 * 24 * 60, 6 * 31 * 24 * 60, 366 * 24 * 60, -1 };

    static final long SECOND = 1000;

    static final long MINUTE = 60 * SECOND;

    static final long HOUR = 60 * MINUTE;

    static final long DAY = 24 * HOUR;

    static final Color bgColor = new Color(250, 250, 250);

    static final Color defaultColor = Color.blue.darker();

    static final int ARRAY_SIZE_INCREMENT = 4000;

    private static Stroke dashedStroke;

    private TimeStamps times = new TimeStamps();

    private ArrayList<Sequence> seqs = new ArrayList<Sequence>();

    private JPopupMenu popupMenu;

    private JMenu timeRangeMenu;

    private JRadioButtonMenuItem[] menuRBs;

    private JMenuItem saveAsMI;

    private JFileChooser saveFC;

    private int viewRange = -1;

    private Unit unit;

    private Border border = null;

    private Rectangle r = new Rectangle(1, 1, 1, 1);

    private Font smallFont = null;

    private int topMargin = 10;

    private int bottomMargin = 45;

    private int leftMargin = 65;

    private int rightMargin = 70;

    public SNMPPlotter() {
        this(Unit.NONE);
    }

    public SNMPPlotter(Unit unit) {
        this.unit = unit;
        enableEvents(AWTEvent.MOUSE_EVENT_MASK);
        addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent e) {
                if (getParent() instanceof SNMPPlotterPanel) {
                    getParent().requestFocusInWindow();
                }
            }
        });
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public SNMPPlotter.Sequence createSequence(String key, String name, Color color, boolean isPlotted) {
        Sequence seq = getSequence(key);
        if (seq == null) {
            seq = new Sequence(key);
        }
        seq.name = name;
        seq.color = (color != null) ? color : defaultColor;
        seq.isPlotted = isPlotted;
        seqs.add(seq);
        return seq;
    }

    public void setUseDashedTransitions(String key, boolean b) {
        Sequence seq = getSequence(key);
        if (seq != null) {
            seq.transitionStroke = b ? getDashedStroke() : null;
        }
    }

    public void setIsPlotted(String key, boolean isPlotted) {
        Sequence seq = getSequence(key);
        if (seq != null) {
            seq.isPlotted = isPlotted;
        }
    }

    public synchronized void addValues(long time, long... values) {
        assert (values.length == seqs.size());
        times.add(time);
        for (int i = 0; i < values.length; i++) {
            seqs.get(i).add(values[i]);
        }
        repaint();
    }

    private Sequence getSequence(String key) {
        for (Sequence seq : seqs) {
            if (seq.key.equals(key)) {
                return seq;
            }
        }
        return null;
    }

    /**
	 * @return the displayed time range in minutes, or -1 for all data
	 */
    public int getViewRange() {
        return viewRange;
    }

    /**
	 * @param minutes
	 *            the displayed time range in minutes, or -1 to diaplay all data
	 */
    public void setViewRange(int minutes) {
        if (minutes != viewRange) {
            int oldValue = viewRange;
            viewRange = minutes;
            firePropertyChange("viewRange", oldValue, viewRange);
            if (popupMenu != null) {
                for (int i = 0; i < menuRBs.length; i++) {
                    if (rangeValues[i] == viewRange) {
                        menuRBs[i].setSelected(true);
                        break;
                    }
                }
            }
            repaint();
        }
    }

    private void saveDataToFile(File file) {
        try {
            PrintStream out = new PrintStream(new FileOutputStream(file));
            out.print("Time");
            for (Sequence seq : seqs) {
                out.print("," + seq.name);
            }
            out.println();
            if (seqs.size() > 0 && seqs.get(0).size > 0) {
                for (int i = 0; i < seqs.get(0).size; i++) {
                    double excelTime = toExcelTime(times.time(i));
                    out.print(String.format(Locale.ENGLISH, "%.6f", excelTime));
                    for (Sequence seq : seqs) {
                        out.print("," + seq.value(i));
                    }
                    out.println();
                }
            }
            out.close();
            JOptionPane.showMessageDialog(this, getText("FileChooser.savedFile", file.getAbsolutePath(), file.length()));
        } catch (IOException ex) {
            String msg = ex.getLocalizedMessage();
            String path = file.getAbsolutePath();
            if (msg.startsWith(path)) {
                msg = msg.substring(path.length()).trim();
            }
            JOptionPane.showMessageDialog(this, getText("FileChooser.saveFailed.message", path, msg), getText("FileChooser.saveFailed.title"), JOptionPane.ERROR_MESSAGE);
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Color oldColor = g.getColor();
        Font oldFont = g.getFont();
        Color fg = getForeground();
        Color bg = getBackground();
        boolean bgIsLight = (bg.getRed() > 200 && bg.getGreen() > 200 && bg.getBlue() > 200);
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (smallFont == null) {
            smallFont = oldFont.deriveFont(9.0F);
        }
        r.x = leftMargin - 5;
        r.y = topMargin - 8;
        r.width = getWidth() - leftMargin - rightMargin;
        r.height = getHeight() - topMargin - bottomMargin + 16;
        if (border == null) {
            border = new BevelBorder(BevelBorder.LOWERED, getBackground().brighter().brighter(), getBackground().brighter(), getBackground().darker().darker(), getBackground().darker());
        }
        border.paintBorder(this, g, r.x, r.y, r.width, r.height);
        g.setColor(bgColor);
        g.fillRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4);
        g.setColor(oldColor);
        long tMin = Long.MAX_VALUE;
        long tMax = Long.MIN_VALUE;
        long vMin = Long.MAX_VALUE;
        long vMax = 1;
        int w = getWidth() - rightMargin - leftMargin - 10;
        int h = getHeight() - topMargin - bottomMargin;
        if (times.size > 1) {
            tMin = Math.min(tMin, times.time(0));
            tMax = Math.max(tMax, times.time(times.size - 1));
        }
        long viewRangeMS;
        if (viewRange > 0) {
            viewRangeMS = viewRange * MINUTE;
        } else {
            viewRangeMS = tMax - tMin;
        }
        for (Sequence seq : seqs) {
            if (seq.size > 0) {
                for (int i = 0; i < seq.size; i++) {
                    if (seq.size == 1 || times.time(i) >= tMax - viewRangeMS) {
                        long val = seq.value(i);
                        if (val > Long.MIN_VALUE) {
                            vMax = Math.max(vMax, val);
                            vMin = Math.min(vMin, val);
                        }
                    }
                }
            } else {
                vMin = 0L;
            }
            if (unit == Unit.BYTES || unit == Unit.MIlLISECONDS || unit == Unit.PERSECOND || !seq.isPlotted) {
                break;
            }
        }
        vMax = normalizeMax(vMax);
        if (vMin > 0) {
            if (vMax / vMin > 4) {
                vMin = 0;
            } else {
                vMin = normalizeMin(vMin);
            }
        }
        g.setColor(fg);
        int x = leftMargin - 18;
        int y = topMargin;
        FontMetrics fm = g.getFontMetrics();
        {
            String sMin = getSizeString(vMin, vMax);
            String sMax = getSizeString(vMax, vMax);
            int xMin = x - 6 - fm.stringWidth(sMin);
            int xMax = x - 6 - fm.stringWidth(sMax);
            if (checkLeftMargin(xMin) || checkLeftMargin(xMax)) {
                return;
            }
            g.drawString(sMax, xMax, y + 4);
            g.drawString(sMin, xMin, y + h + 4);
        }
        g.drawLine(x, y, x, y + h);
        g.drawLine(x - 5, y, x + 5, y);
        g.drawLine(x - 5, y + h, x + 5, y + h);
        int n = 5;
        if (("" + vMax).startsWith("2")) {
            n = 4;
        } else if (("" + vMax).startsWith("3")) {
            n = 6;
        } else if (("" + vMax).startsWith("4")) {
            n = 4;
        } else if (("" + vMax).startsWith("6")) {
            n = 6;
        } else if (("" + vMax).startsWith("7")) {
            n = 7;
        } else if (("" + vMax).startsWith("8")) {
            n = 8;
        } else if (("" + vMax).startsWith("9")) {
            n = 3;
        }
        for (int i = 0; i < n; i++) {
            long v = i * vMax / n;
            if (v > vMin) {
                y = topMargin + h - (int) (h * (v - vMin) / (vMax - vMin));
                g.drawLine(x - 2, y, x + 2, y);
                String s = getSizeString(v, vMax);
                int sx = x - 6 - fm.stringWidth(s);
                if (y < topMargin + h - 13) {
                    if (checkLeftMargin(sx)) {
                        return;
                    }
                    g.drawString(s, sx, y + 4);
                }
                g.setColor(Color.lightGray);
                g.drawLine(r.x + 4, y, r.x + r.width - 4, y);
                g.setColor(fg);
            }
        }
        x = leftMargin;
        y = topMargin + h + 15;
        g.drawLine(x, y, x + w, y);
        if (tMax > 0) {
            long tz = timeDF.getTimeZone().getOffset(tMax);
            long tickInterval = calculateTickInterval(w, 40, viewRangeMS);
            if (tickInterval > 3 * HOUR) {
                tickInterval = calculateTickInterval(w, 80, viewRangeMS);
            }
            long t0 = tickInterval - (tMax - viewRangeMS + tz) % tickInterval;
            while (t0 < viewRangeMS) {
                x = leftMargin + (int) (w * t0 / viewRangeMS);
                g.drawLine(x, y - 2, x, y + 2);
                long t = tMax - viewRangeMS + t0;
                String str = formatClockTime(t);
                g.drawString(str, x, y + 16);
                if ((t + tz) % (1 * DAY) == 0) {
                    str = formatDate(t);
                    g.drawString(str, x, y + 27);
                }
                g.setColor(Color.lightGray);
                g.drawLine(x, topMargin, x, topMargin + h);
                g.setColor(fg);
                t0 += tickInterval;
            }
        }
        int start = 0;
        int nValues = 0;
        int nLists = seqs.size();
        if (nLists > 0) {
            nValues = seqs.get(0).size;
        }
        if (nValues == 0) {
            g.setColor(oldColor);
            return;
        } else {
            Sequence seq = seqs.get(0);
            for (int p = 0; p < seq.size; p++) {
                if (times.time(p) >= tMax - viewRangeMS) {
                    start = p;
                    break;
                }
            }
        }
        int pointsPerPixel = (nValues - start) / w;
        if (pointsPerPixel < 4) {
            pointsPerPixel = 1;
        }
        for (int i = nLists - 1; i >= 0; i--) {
            int x0 = leftMargin;
            int y0 = topMargin + h + 1;
            Sequence seq = seqs.get(i);
            if (seq.isPlotted && seq.size > 0) {
                for (int pass = 0; pass < 2; pass++) {
                    g.setColor((pass == 0) ? Color.white : seq.color);
                    int x1 = -1;
                    long v1 = -1;
                    for (int p = start; p < nValues; p += pointsPerPixel) {
                        if (pointsPerPixel > 1 && p >= nValues - pointsPerPixel) {
                            p = nValues - 1;
                        }
                        int x2 = (viewRangeMS == 0L) ? 0 : (int) (w * (times.time(p) - (tMax - viewRangeMS)) / viewRangeMS);
                        long v2 = seq.value(p);
                        if (v2 >= vMin && v2 <= vMax) {
                            int y2 = (int) (h * (v2 - vMin) / (vMax - vMin));
                            if (x1 >= 0 && v1 >= vMin && v1 <= vMax) {
                                int y1 = (int) (h * (v1 - vMin) / (vMax - vMin));
                                if (y1 == y2) {
                                    g.fillRect(x0 + x1, y0 - y1 - pass, x2 - x1, 1);
                                } else {
                                    Graphics2D g2d = (Graphics2D) g;
                                    Stroke oldStroke = null;
                                    if (seq.transitionStroke != null) {
                                        oldStroke = g2d.getStroke();
                                        g2d.setStroke(seq.transitionStroke);
                                    }
                                    g.drawLine(x0 + x1, y0 - y1 - pass, x0 + x2, y0 - y2 - pass);
                                    if (oldStroke != null) {
                                        g2d.setStroke(oldStroke);
                                    }
                                }
                            } else if (seq.size == 1) {
                                g.fillRect(x0, y0 - y2 - pass, w, 1);
                            }
                        }
                        x1 = x2;
                        v1 = v2;
                    }
                }
                long v = seq.value(seq.size - 1);
                if (v >= vMin && v <= vMax) {
                    if (bgIsLight) {
                        g.setColor(seq.color);
                    } else {
                        g.setColor(fg);
                    }
                    x = r.x + r.width + 2;
                    y = topMargin + h - (int) (h * (v - vMin) / (vMax - vMin));
                    g.fillPolygon(new int[] { x + 2, x + 6, x + 6 }, new int[] { y, y + 3, y - 3 }, 3);
                }
                g.setColor(fg);
            }
        }
        int[] valueStringSlots = new int[nLists];
        for (int i = 0; i < nLists; i++) valueStringSlots[i] = -1;
        for (int i = 0; i < nLists; i++) {
            Sequence seq = seqs.get(i);
            if (seq.isPlotted && seq.size > 0) {
                long v = seq.value(seq.size - 1);
                if (v >= vMin && v <= vMax) {
                    x = r.x + r.width + 2;
                    y = topMargin + h - (int) (h * (v - vMin) / (vMax - vMin));
                    int y2 = getValueStringSlot(valueStringSlots, y, 2 * 10, i);
                    g.setFont(smallFont);
                    if (bgIsLight) {
                        g.setColor(seq.color);
                    } else {
                        g.setColor(fg);
                    }
                    String curValue = (unit == Unit.PERCENT) ? (v + "%") : String.format("%,d", v);
                    int valWidth = fm.stringWidth(curValue);
                    String legend = seq.name;
                    int legendWidth = fm.stringWidth(legend);
                    if (checkRightMargin(valWidth) || checkRightMargin(legendWidth)) {
                        return;
                    }
                    g.drawString(legend, x + 17, Math.min(topMargin + h, y2 + 3 - 10));
                    g.drawString(curValue, x + 17, Math.min(topMargin + h + 10, y2 + 3));
                    if (y2 > y + 3) {
                        g.drawLine(x + 9, y + 2, x + 14, y2);
                    } else if (y2 < y - 3) {
                        g.drawLine(x + 9, y - 2, x + 14, y2);
                    }
                }
                g.setFont(oldFont);
                g.setColor(fg);
            }
        }
        g.setColor(oldColor);
    }

    private boolean checkLeftMargin(int x) {
        if (x < 2) {
            leftMargin += (2 - x);
            repaint();
            return true;
        }
        return false;
    }

    private boolean checkRightMargin(int w) {
        if (w + 2 > rightMargin) {
            rightMargin = (w + 2);
            repaint();
            return true;
        }
        return false;
    }

    private int getValueStringSlot(int[] slots, int y, int h, int i) {
        for (int s = 0; s < slots.length; s++) {
            if (slots[s] >= y && slots[s] < y + h) {
                if (slots[s] > h) {
                    return getValueStringSlot(slots, slots[s] - h, h, i);
                } else {
                    return getValueStringSlot(slots, slots[s] + h, h, i);
                }
            } else if (y >= h && slots[s] > y - h && slots[s] < y) {
                return getValueStringSlot(slots, slots[s] + h, h, i);
            }
        }
        slots[i] = y;
        return y;
    }

    private long calculateTickInterval(int w, int hGap, long viewRangeMS) {
        long tickInterval = viewRangeMS * hGap / w;
        if (tickInterval < 1 * MINUTE) {
            tickInterval = 1 * MINUTE;
        } else if (tickInterval < 5 * MINUTE) {
            tickInterval = 5 * MINUTE;
        } else if (tickInterval < 10 * MINUTE) {
            tickInterval = 10 * MINUTE;
        } else if (tickInterval < 30 * MINUTE) {
            tickInterval = 30 * MINUTE;
        } else if (tickInterval < 1 * HOUR) {
            tickInterval = 1 * HOUR;
        } else if (tickInterval < 3 * HOUR) {
            tickInterval = 3 * HOUR;
        } else if (tickInterval < 6 * HOUR) {
            tickInterval = 6 * HOUR;
        } else if (tickInterval < 12 * HOUR) {
            tickInterval = 12 * HOUR;
        } else if (tickInterval < 1 * DAY) {
            tickInterval = 1 * DAY;
        } else {
            tickInterval = normalizeMax(tickInterval / DAY) * DAY;
        }
        return tickInterval;
    }

    private long normalizeMin(long l) {
        int exp = (int) Math.log10((double) l);
        long multiple = (long) Math.pow(10.0, exp);
        int i = (int) (l / multiple);
        return i * multiple;
    }

    private long normalizeMax(long l) {
        int exp = (int) Math.log10((double) l);
        long multiple = (long) Math.pow(10.0, exp);
        int i = (int) (l / multiple);
        l = (i + 1) * multiple;
        return l;
    }

    private String getSizeString(long v, long vMax) {
        String s;
        switch(unit) {
            case BYTES:
                s = formatBytes(v, vMax);
                break;
            case KBYTES:
                s = formatSNMPMemUsage(v, vMax, false);
                break;
            case PERCENT:
                s = v + "%";
                break;
            case MIlLISECONDS:
                s = v + "ms";
                break;
            case PERSECOND:
                s = v + "/sec";
                break;
            default:
                s = String.format("%,d", v);
        }
        return s;
    }

    private static synchronized Stroke getDashedStroke() {
        if (dashedStroke == null) {
            dashedStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[] { 2.0f, 3.0f }, 0.0f);
        }
        return dashedStroke;
    }

    private static Object extendArray(Object a1) {
        int n = Array.getLength(a1);
        Object a2 = Array.newInstance(a1.getClass().getComponentType(), n + ARRAY_SIZE_INCREMENT);
        System.arraycopy(a1, 0, a2, 0, n);
        return a2;
    }

    private static class TimeStamps {

        long[] offsets = new long[0];

        int[] indices = new int[0];

        int[] rtimes = new int[ARRAY_SIZE_INCREMENT];

        int size = 0;

        /**
		 * Returns the time stamp for index i
		 */
        public long time(int i) {
            long offset = 0;
            for (int j = indices.length - 1; j >= 0; j--) {
                if (i >= indices[j]) {
                    offset = offsets[j];
                    break;
                }
            }
            return offset + rtimes[i];
        }

        public void add(long time) {
            int n = offsets.length;
            if (n == 0 || time - offsets[n - 1] > Integer.MAX_VALUE) {
                long[] offsetsTmp = new long[n + 1];
                System.arraycopy(offsets, 0, offsetsTmp, 0, n);
                offsets = offsetsTmp;
                offsets[n] = time;
                int[] indicesTmp = new int[n + 1];
                System.arraycopy(indices, 0, indicesTmp, 0, n);
                indices = indicesTmp;
                indices[n] = size;
            }
            if (rtimes.length == size) {
                rtimes = (int[]) extendArray(rtimes);
            }
            rtimes[size] = (int) (time - offsets[offsets.length - 1]);
            size++;
        }

        public void clean() {
            offsets = new long[0];
            indices = new int[0];
            rtimes = new int[ARRAY_SIZE_INCREMENT];
            size = 0;
        }
    }

    static class Sequence {

        String key;

        String name;

        Color color;

        boolean isPlotted;

        Stroke transitionStroke = null;

        Object values = new byte[ARRAY_SIZE_INCREMENT + (int) (Math.random() * 100)];

        int size = 0;

        public Sequence(String key) {
            this.key = key;
        }

        /**
		 * Returns the value at index i
		 */
        public long value(int i) {
            return Array.getLong(values, i);
        }

        public void add(long value) {
            if ((values instanceof byte[] || values instanceof short[] || values instanceof int[]) && value > Integer.MAX_VALUE) {
                long[] la = new long[Array.getLength(values)];
                for (int i = 0; i < size; i++) {
                    la[i] = Array.getLong(values, i);
                }
                values = la;
            } else if ((values instanceof byte[] || values instanceof short[]) && value > Short.MAX_VALUE) {
                int[] ia = new int[Array.getLength(values)];
                for (int i = 0; i < size; i++) {
                    ia[i] = Array.getInt(values, i);
                }
                values = ia;
            } else if (values instanceof byte[] && value > Byte.MAX_VALUE) {
                short[] sa = new short[Array.getLength(values)];
                for (int i = 0; i < size; i++) {
                    sa[i] = Array.getShort(values, i);
                }
                values = sa;
            }
            if (Array.getLength(values) == size) {
                values = extendArray(values);
            }
            if (values instanceof long[]) {
                ((long[]) values)[size] = value;
            } else if (values instanceof int[]) {
                ((int[]) values)[size] = (int) value;
            } else if (values instanceof short[]) {
                ((short[]) values)[size] = (short) value;
            } else {
                ((byte[]) values)[size] = (byte) value;
            }
            size++;
        }
    }

    long getValue() {
        return 0;
    }

    long getLastTimeStamp() {
        return times.time(times.size - 1);
    }

    long getLastValue(String key) {
        Sequence seq = getSequence(key);
        return (seq != null && seq.size > 0) ? seq.value(seq.size - 1) : 0L;
    }

    protected class AccessiblePlotter extends AccessibleJComponent {

        protected AccessiblePlotter() {
            setAccessibleName(getText("Plotter.accessibleName"));
        }

        public String getAccessibleName() {
            String name = super.getAccessibleName();
            if (seqs.size() > 0 && seqs.get(0).size > 0) {
                String keyValueList = "";
                for (Sequence seq : seqs) {
                    if (seq.isPlotted) {
                        String value = "null";
                        if (seq.size > 0) {
                            value = "" + seq.value(seq.size - 1);
                            if (unit == Unit.PERCENT) {
                                value += "%";
                            } else if (unit == Unit.BYTES) {
                                value = getText("Size Bytes", seq.value(seq.size - 1));
                            }
                        }
                        keyValueList += getText("Plotter.accessibleName.keyAndValue", seq.key, value);
                    }
                }
                name += "\n" + keyValueList + ".";
            } else {
                name += "\n" + getText("Plotter.accessibleName.noData");
            }
            return name;
        }

        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.CANVAS;
        }
    }

    public synchronized void clearData() {
        times.clean();
        seqs.clear();
        repaint();
    }
}
