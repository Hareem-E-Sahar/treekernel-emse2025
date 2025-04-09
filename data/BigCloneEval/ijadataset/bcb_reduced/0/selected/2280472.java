package com.yerihyo.yeritools.swing;

import info.clearthought.layout.TableLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import com.yerihyo.yeritools.collections.CollectionsToolkit;
import com.yerihyo.yeritools.math.StatisticsToolkit;
import com.yerihyo.yeritools.swing.SwingToolkit.TestFrame;

public class TimelineBarToolkit {

    public static void main(String[] args) {
        test03();
    }

    public static class LabeledTimelineBarPanel extends JPanel {

        private static final long serialVersionUID = 1L;

        public LabeledTimelineBarPanel() {
        }

        public void loadBarPanel(TimelineFrame[] frameArray) {
            this.removeAll();
            double[] rowArray = new double[] { 100, TableLayout.FILL };
            double[] colArray = new double[frameArray.length];
            for (int i = 0; i < frameArray.length; i++) {
                colArray[i] = ((double) 1) / frameArray.length;
            }
            this.setLayout(new TableLayout(colArray, rowArray));
            for (int i = 0; i < frameArray.length; i++) {
                TimelineFrame frame = frameArray[i];
                TimelineBarPanel comp = new TimelineBarPanel(frame);
                this.add(new JLabel(Integer.toString(i)), comp);
            }
        }
    }

    @SuppressWarnings("unused")
    private static void test04() {
        List<Double> doubleList = new ArrayList<Double>();
        List<String> stringList = new ArrayList<String>();
        SectionLabels sectionLabels = new SectionLabels(new double[] { 2, 3, 5, 6, 14, 3, 2, 5, 7, 3 }, new String[] { "2", "3", "5", "6", "14", "3", "2", "5", "7", "3" });
        TimelineFrame timelineFrame = new TimelineFrame(0, 100);
        timelineFrame.setOrientation(Orientation.TOP_TO_BOTTOM);
        JPanel panel = createLabel(timelineFrame, sectionLabels, Orientation.BOTTOM_TO_TOP, null, null);
        SwingToolkit.createTestFrame(panel, new Dimension(800, 600)).showFrame();
    }

    @SuppressWarnings("unused")
    private static void test04_2() {
        class Test implements Runnable {

            private RotateableLabel panel;

            public Test(RotateableLabel panel) {
                this.panel = panel;
            }

            public void run() {
                Orientation[] orientationValueArray = Orientation.values();
                for (int i = 0; true; i++) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    int index = i % orientationValueArray.length;
                    Orientation currentOrientation = orientationValueArray[index];
                    panel.setOrientation(currentOrientation);
                    panel.repaint();
                }
            }
        }
        ;
        RotateableLabel panel = new RotateableLabel();
        panel.setText("Hello");
        SwingToolkit.createTestFrame(panel, new Dimension(400, 300)).showFrame();
        Orientation[] orientationValueArray = Orientation.values();
        int[] hAlignmentArray = new int[] { SwingConstants.LEFT, SwingConstants.CENTER, SwingConstants.RIGHT };
        int[] vAlignmentArray = new int[] { SwingConstants.BOTTOM, SwingConstants.CENTER, SwingConstants.TOP };
        for (int i = 0; true; i++) {
            for (int hAlignment : hAlignmentArray) {
                for (int vAlignment : vAlignmentArray) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    int index = i % orientationValueArray.length;
                    Orientation currentOrientation = orientationValueArray[index];
                    panel.setOrientation(currentOrientation);
                    panel.setHorizontalAlignment(hAlignment);
                    panel.setVerticalAlignment(vAlignment);
                    panel.repaint();
                }
            }
        }
    }

    @SuppressWarnings("unused")
    private static void test03() {
        TimelineFrame frame = new TimelineFrame(0, 100);
        frame.setOrientation(Orientation.TOP_TO_BOTTOM);
        frame.setDefaultCellColor(new Color(0x999999));
        frame.setFrameColor(null);
        TimelineBarPanel markBarComponent = new TimelineBarPanel(frame);
        markBarComponent.setPreferredBreadth(50);
        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(null);
        labelPanel.setBackground(Color.white);
        Map<Double, String> tickMap = createTmpTickMap();
        Component ruler = createRuler(frame, tickMap, 5);
        TestFrame testFrame = SwingToolkit.createTestFrame(ruler, new Dimension(300, 800));
        testFrame.showFrame();
    }

    @SuppressWarnings("unused")
    private static void test02() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.LINE_AXIS));
        contentPanel.add(Box.createHorizontalStrut(3));
        contentPanel.add(createTimelineBarPanel(10, 20, 40));
        contentPanel.add(Box.createHorizontalStrut(3));
        contentPanel.add(createTimelineBarPanel(20, 40, 60));
        contentPanel.add(Box.createHorizontalGlue());
        TestFrame testFrame = SwingToolkit.createTestFrame(contentPanel, new Dimension(1024, 768));
        testFrame.showFrame();
    }

    @SuppressWarnings("unused")
    private static void test01() {
        TimelineBarPanel markBarComponent = createTimelineBarPanel(20, 40, 50);
        SwingToolkit.createTestFrame(markBarComponent, new Dimension(100, 1024)).showFrame();
    }

    public static class RotateableLabel extends JPanel {

        private static final long serialVersionUID = 1L;

        private String text = "";

        private Orientation orientation = Orientation.LEFT_TO_RIGHT;

        private int horizontalAlignment = SwingConstants.LEFT;

        private int verticalAlignment = SwingConstants.BOTTOM;

        private Font font = new Font("verdana", Font.PLAIN, 10);

        private Color color = new Color(0x999999);

        public Font getFont() {
            return font;
        }

        public void setFont(Font font) {
            this.font = font;
        }

        public Color getColor() {
            return color;
        }

        public void setColor(Color color) {
            this.color = color;
        }

        public RotateableLabel() {
        }

        public RotateableLabel(String text) {
            this.setText(text);
        }

        public void paintChildren(Graphics g) {
            super.paintChildren(g);
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            int horizontalAlignment = this.getHorizontalAlignment();
            int verticalAlignment = this.getVerticalAlignment();
            Orientation orientation = this.getOrientation();
            String text = this.getText();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(this.getFont());
            g2.setColor(this.getColor());
            Rectangle bounds = new Rectangle(0, 0, this.getWidth(), this.getHeight());
            Point startPoint = SwingToolkit.getStartPoint(bounds, orientation, horizontalAlignment, verticalAlignment);
            SwingToolkit.drawString(startPoint, text, g2, orientation.getRotationDegree(), horizontalAlignment, verticalAlignment);
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public int getHorizontalAlignment() {
            return horizontalAlignment;
        }

        public void setHorizontalAlignment(int horizontalAlignment) {
            this.horizontalAlignment = horizontalAlignment;
        }

        public Orientation getOrientation() {
            return orientation;
        }

        public void setOrientation(Orientation orientation) {
            this.orientation = orientation;
        }

        public int getVerticalAlignment() {
            return verticalAlignment;
        }

        public void setVerticalAlignment(int verticalAlignment) {
            this.verticalAlignment = verticalAlignment;
        }
    }

    public static Map<Double, String> createTmpTickMap() {
        SortedMap<Double, String> returnMap = new TreeMap<Double, String>();
        int[] tickPositionArray = new int[] { 0, 8, 10, 15, 16, 20, 25, 30, 41, 50, 51, 53, 59, 60, 63, 65, 69, 70, 71, 72, 75, 77, 79, 82, 88, 100 };
        for (int i = 0; i < tickPositionArray.length; i++) {
            int tickPosition = tickPositionArray[i];
            returnMap.put((double) tickPosition, Integer.toString(i + 1));
        }
        return returnMap;
    }

    public static class SectionLabels {

        private double[] sectionLengthArray;

        private String[] sectionLabelArray;

        public SectionLabels(double[] sectionLengthArray, String[] sectionLabelArray) {
            CollectionsToolkit.assertArrayLength(sectionLengthArray, sectionLabelArray);
            this.sectionLengthArray = sectionLengthArray;
            this.sectionLabelArray = sectionLabelArray;
        }

        public double[] getSectionLengthArray() {
            return sectionLengthArray;
        }

        public String[] getSectionLabelArray() {
            return sectionLabelArray;
        }
    }

    public static TimelineBarPanel createLabel(TimelineFrame frame, SectionLabels sectionLabels, Orientation textOrientation, Font font, Color color) {
        double[] lengthArray = sectionLabels.getSectionLengthArray();
        String[] labelArray = sectionLabels.getSectionLabelArray();
        double[] proportionalArray = StatisticsToolkit.makeNormalProportionalArray(lengthArray);
        TimelineFrame newFrame = frame.cloneShellOnly();
        newFrame.setFrameColor(null);
        double cummulativeValue = 0;
        if (frame.getOrientation() != Orientation.TOP_TO_BOTTOM && frame.getOrientation() != Orientation.LEFT_TO_RIGHT) {
            throw new UnsupportedOperationException();
        }
        newFrame.addStringColorPair("line", new Color(0x999999));
        for (int j = 0; j < proportionalArray.length; j++) {
            TimelinePositionItem lineItem = new TimelinePositionItem(cummulativeValue, 1);
            lineItem.setKey("line");
            lineItem.setBreadthPercentage(0.5);
            newFrame.addTimelineItem(lineItem);
            double value = frame.getValue(proportionalArray[j]);
            double center = cummulativeValue + (value / 2);
            TimelinePositionItem textItem = new TimelinePositionItem(center, 1);
            StringContent stringContent = new StringContent(labelArray[j], textOrientation);
            if (font != null) {
                stringContent.setFont(font);
            }
            if (color != null) {
                stringContent.setColor(color);
            }
            textItem.setStringContent(stringContent);
            newFrame.addTimelineItem(textItem);
            cummulativeValue += value;
        }
        TimelinePositionItem lineItem = new TimelinePositionItem(cummulativeValue, 1);
        lineItem.setKey("line");
        newFrame.addTimelineItem(lineItem);
        TimelineBarPanel markBarComponent = new TimelineBarPanel(newFrame);
        markBarComponent.setOpaque(false);
        SwingToolkit.fixWidth(markBarComponent, 15);
        return markBarComponent;
    }

    public static interface Indicator {

        public Color getColor();

        public String getId();

        public String getLabel();
    }

    protected static class LabelColorIndicator implements Indicator {

        private String s;

        private Color c;

        public LabelColorIndicator(String s, Color c) {
            this.s = s;
            this.c = c;
        }

        @Override
        public Color getColor() {
            return c;
        }

        @Override
        public String getId() {
            return s;
        }

        @Override
        public String getLabel() {
            return s;
        }
    }

    public static Map<String, Indicator> createIndicatorMap(Map<String, Color> colorMap) {
        Map<String, Indicator> indicatorMap = new HashMap<String, Indicator>();
        for (String key : colorMap.keySet()) {
            indicatorMap.put(key, new LabelColorIndicator(key, colorMap.get(key)));
        }
        return indicatorMap;
    }

    public static JPanel createRuler(TimelineFrame frame, Map<Double, String> tickMap, int labelInterval) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, getOrthogonalBoxLayout(frame.getOrientation())));
        for (int i = 0; i < 2; i++) {
            TimelineFrame newFrame = frame.cloneShellOnly();
            newFrame.setFrameColor(null);
            if (i == 0) {
                newFrame.setDefaultCellColor(new Color(0x999999));
            }
            Set<Double> keySet = tickMap.keySet();
            Iterator<Double> iterator = keySet.iterator();
            int j = 0;
            for (j = 0; iterator.hasNext(); j++) {
                double tickPosition = iterator.next().doubleValue();
                TimelinePositionItem item = new TimelinePositionItem(tickPosition, 1);
                if (i == 0) {
                    item.setKey("default");
                } else if (j == 0 || (j + 1) % labelInterval == 0 || j == keySet.size() - 1) {
                    item.setKey(null);
                    StringContent stringContent = new StringContent(tickMap.get(tickPosition), Orientation.LEFT_TO_RIGHT);
                    stringContent.setColor(new Color(0x999999));
                    item.setStringContent(stringContent);
                }
                newFrame.addTimelineItem(item);
            }
            TimelineBarPanel markBarComponent = new TimelineBarPanel(newFrame);
            markBarComponent.setOpaque(false);
            if (i == 0) {
                markBarComponent.setPreferredBreadth(5);
            } else {
                markBarComponent.setPreferredBreadth(20);
            }
            panel.add(markBarComponent);
        }
        return panel;
    }

    public static int getOrthogonalBoxLayout(Orientation orientation) {
        if (orientation.isHorizontal()) {
            return BoxLayout.PAGE_AXIS;
        } else {
            return BoxLayout.LINE_AXIS;
        }
    }

    private static TimelineBarPanel createTimelineBarPanel(int x, int y, int z) {
        TimelineFrame frame = new TimelineFrame(0, 100);
        frame.setOrientation(Orientation.TOP_TO_BOTTOM);
        frame.setDefaultCellColor(new Color(0xffcccc));
        TimelineItem item = new TimelineCellItem(x, y, "i");
        frame.addTimelineItem(item);
        TimelinePositionItem item2 = new TimelinePositionItem(z, 2);
        item2.setKey("i");
        frame.addTimelineItem(item2);
        TimelineBarPanel markBarComponent = new TimelineBarPanel(frame);
        return markBarComponent;
    }

    public static enum Orientation {

        LEFT_TO_RIGHT, TOP_TO_BOTTOM, RIGHT_TO_LEFT, BOTTOM_TO_TOP;

        public boolean isHorizontal() {
            return this == LEFT_TO_RIGHT || this == RIGHT_TO_LEFT;
        }

        public boolean isVertical() {
            return !isHorizontal();
        }

        public double getRotationDegree() {
            return this.ordinal() * Math.PI / 2;
        }

        public int getCWTurnCount() {
            return this.ordinal();
        }

        public int getCCWTurnCount() {
            return (4 - getCWTurnCount()) % 4;
        }
    }

    ;

    public static interface TimelineUI {

        int getPixelLength();
    }

    public static class StringContent {

        private Orientation orientation;

        private String string;

        private Color color = Color.black;

        private Font font = null;

        public Color getColor() {
            return color;
        }

        public void setColor(Color color) {
            this.color = color;
        }

        public StringContent() {
        }

        public StringContent(String string, Orientation orientation) {
            this.string = string;
            this.orientation = orientation;
        }

        public Orientation getOrientation() {
            return orientation;
        }

        public void setOrientation(Orientation orientation) {
            this.orientation = orientation;
        }

        public String getString() {
            return string;
        }

        public void setString(String string) {
            this.string = string;
        }

        public void drawSelf(Point center, Dimension size, Graphics2D g) {
            Color originalColor = g.getColor();
            Font originalFont = g.getFont();
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            String text = this.getString();
            Orientation orientation = this.getOrientation();
            Dimension fontBoxSize = SwingToolkit.getFontBoxSize(g.getFontMetrics(), text, true);
            if (orientation.isVertical()) {
                fontBoxSize = SwingToolkit.flipDimension(fontBoxSize);
            }
            Rectangle outerBox = new Rectangle(new Point(0, 0), size);
            Point newCenter = SwingToolkit.adjustCenterToFitIntoOuterBox(center, outerBox, fontBoxSize);
            g.setColor(this.getColor());
            Font font = this.getFont();
            if (font != null) {
                g.setFont(font);
            }
            SwingToolkit.drawString(newCenter, text, g, orientation.getRotationDegree(), SwingConstants.CENTER, SwingConstants.CENTER);
            g.setFont(originalFont);
            g.setColor(originalColor);
        }

        public Font getFont() {
            return font;
        }

        public void setFont(Font font) {
            this.font = font;
        }
    }

    public static interface TimelineItem {

        String getKey();

        void drawSelf(TimelineUI ui, TimelineFrame timelineFrame, Dimension size, Graphics2D g);
    }

    public static class TimelinePositionItem implements TimelineItem {

        private double center;

        private int pixelLength;

        private String key;

        private double breadthPercentage = 1;

        private StringContent stringContent;

        public String toString() {
            return "[" + center + "/" + pixelLength + "]";
        }

        public TimelinePositionItem(double center, int pixelLength) {
            this.center = center;
            this.pixelLength = pixelLength;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public int getPixelStart(TimelineUI ui, TimelineFrame frame) {
            int centerPixelPosition = getPixelCenter(ui, frame);
            return centerPixelPosition - pixelLength / 2;
        }

        public int getPixelCenter(TimelineUI ui, TimelineFrame frame) {
            return getPixelPosition(ui, frame, this.getCenter());
        }

        public int getPixelEnd(TimelineUI ui, TimelineFrame frame) {
            int centerPixelPosition = getPixelCenter(ui, frame);
            return centerPixelPosition + (pixelLength + 1) / 2;
        }

        public double getCenter() {
            return center;
        }

        public int getPixelLength() {
            return pixelLength;
        }

        @Override
        public void drawSelf(TimelineUI ui, TimelineFrame timelineFrame, Dimension size, Graphics2D g) {
            int pixelStart = this.getPixelStart(ui, timelineFrame);
            int pixelEnd = this.getPixelEnd(ui, timelineFrame);
            int length = pixelEnd - pixelStart;
            Rectangle rectangle = null;
            double breadthPercentage = this.getBreadthPercentage();
            if (timelineFrame.getOrientation().isHorizontal()) {
                double[] yStartEnd = StatisticsToolkit.shrinkRegionStartEnd(0, size.height, breadthPercentage, SwingConstants.CENTER);
                rectangle = new Rectangle(pixelStart, (int) yStartEnd[0], length, (int) (yStartEnd[1] - yStartEnd[0]));
            } else {
                double[] xStartEnd = StatisticsToolkit.shrinkRegionStartEnd(0, size.width, breadthPercentage, SwingConstants.CENTER);
                rectangle = new Rectangle((int) xStartEnd[0], pixelStart, (int) (xStartEnd[1] - xStartEnd[0]), length);
            }
            Color cellColor = timelineFrame.getItemColor(this);
            if (cellColor != null) {
                g.setColor(cellColor);
                g.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
            }
            StringContent stringContent = this.getStringContent();
            if (stringContent != null) {
                int pixelCenter = this.getPixelCenter(ui, timelineFrame);
                Point center;
                if (timelineFrame.getOrientation().isHorizontal()) {
                    center = new Point(pixelCenter, size.height / 2);
                } else {
                    center = new Point(size.width / 2, pixelCenter);
                }
                stringContent.drawSelf(center, size, g);
            }
        }

        public StringContent getStringContent() {
            return stringContent;
        }

        public void setStringContent(StringContent stringContent) {
            this.stringContent = stringContent;
        }

        public double getBreadthPercentage() {
            return breadthPercentage;
        }

        public void setBreadthPercentage(double breadthPercentage) {
            this.breadthPercentage = breadthPercentage;
        }
    }

    public static class TimelineCellItem implements TimelineItem {

        private double center;

        private double length;

        private StringContent stringContent;

        private String key;

        public TimelineCellItem(double start, double end, String key) {
            this.setStartEndValue(start, end);
            this.setKey(key);
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public void setStartEndValue(double start, double end) {
            StatisticsToolkit.checkValueIncreasing(start, end);
            this.center = (start + end) / 2;
            this.length = (end - start);
        }

        public void setCenterLength(double center, double length) {
            this.center = center;
            this.length = length;
        }

        public int getPixelEnd(TimelineUI ui, TimelineFrame frame) {
            return getPixelPosition(ui, frame, this.getEnd());
        }

        public int getPixelStart(TimelineUI ui, TimelineFrame frame) {
            double start = this.getStart();
            int pixelStart = getPixelPosition(ui, frame, start);
            return pixelStart;
        }

        public int getPixelCenter(TimelineUI ui, TimelineFrame frame) {
            double center = this.getCenter();
            int pixelCenter = getPixelPosition(ui, frame, center);
            return pixelCenter;
        }

        public double getCenter() {
            return center;
        }

        public double getLength() {
            return length;
        }

        public double getStart() {
            return this.getCenter() - this.getLength() / 2;
        }

        public double getEnd() {
            return this.getCenter() + this.getLength() / 2;
        }

        @Override
        public void drawSelf(TimelineUI ui, TimelineFrame timelineFrame, Dimension size, Graphics2D g) {
            int pixelStart = this.getPixelStart(ui, timelineFrame);
            int pixelEnd = this.getPixelEnd(ui, timelineFrame);
            int length = pixelEnd - pixelStart;
            Rectangle rectangle = null;
            if (timelineFrame.getOrientation().isHorizontal()) {
                rectangle = new Rectangle(pixelStart, 0, length, size.height - 1);
            } else {
                rectangle = new Rectangle(0, pixelStart, size.width - 1, length);
            }
            Color cellColor = timelineFrame.getItemColor(this);
            if (cellColor != null) {
                g.setColor(cellColor);
                g.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
            }
            StringContent stringContent = this.getStringContent();
            if (stringContent != null) {
                int pixelCenter = this.getPixelCenter(ui, timelineFrame);
                Point center;
                if (timelineFrame.getOrientation().isHorizontal()) {
                    center = new Point(pixelCenter, size.height / 2);
                } else {
                    center = new Point(size.width / 2, pixelCenter);
                }
                stringContent.drawSelf(center, size, g);
            }
        }

        public StringContent getStringContent() {
            return stringContent;
        }

        public void setStringContent(StringContent stringContent) {
            this.stringContent = stringContent;
        }
    }

    public static int getPixelPosition(TimelineUI ui, TimelineFrame frame, double value) {
        int pixelLength = ui.getPixelLength();
        double proportion = frame.getProportion(value);
        if (proportion == 1) {
            proportion -= frame.getValueDifference() * 10E-6;
        }
        return (int) (proportion * pixelLength);
    }

    public static class TimelineFrame {

        private Color frameColor = new Color(0x999999);

        private double startValue;

        private double endValue;

        private int eventID = 1;

        private Map<String, Color> stringColorPairMap = new HashMap<String, Color>();

        private Orientation orientation = Orientation.LEFT_TO_RIGHT;

        private Color defaultCellColor = new Color(0x666666);

        private List<TimelineItem> timelineItemList = new ArrayList<TimelineItem>();

        private List<ActionListener> listenerList = new ArrayList<ActionListener>();

        public TimelineFrame cloneShellOnly() {
            TimelineFrame timelineFrame = new TimelineFrame(this.getStartValue(), this.getEndValue());
            timelineFrame.setStringColorPairMap(this.getStringColorPairMap());
            timelineFrame.setOrientation(this.getOrientation());
            timelineFrame.setDefaultCellColor(this.getDefaultCellColor());
            return timelineFrame;
        }

        public double getValueDifference() {
            return this.getEndValue() - this.getStartValue();
        }

        public double getValue(double proportion) {
            return this.getValueDifference() * proportion + this.getStartValue();
        }

        public void addActionListener(ActionListener listener) {
            listenerList.add(listener);
        }

        protected void fireActionEvent() {
            ActionEvent event = new ActionEvent(this, eventID++, "");
            for (ActionListener listener : listenerList) {
                listener.actionPerformed(event);
            }
        }

        public Map<String, Color> getStringColorPairMap() {
            return stringColorPairMap;
        }

        public void setStringColorPairMap(Map<? extends String, ? extends Color> stringColorPairMap) {
            this.stringColorPairMap = new HashMap<String, Color>(stringColorPairMap);
            fireActionEvent();
        }

        public void addStringColorPair(String s, Color c) {
            this.stringColorPairMap.put(s, c);
            fireActionEvent();
        }

        public void addTimelineItem(TimelineItem item) {
            this.timelineItemList.add(item);
        }

        public TimelineFrame(double startValue, double endValue) {
            this.startValue = startValue;
            this.endValue = endValue;
        }

        public double getProportion(double value) {
            double startValue = this.getStartValue();
            double endValue = this.getEndValue();
            return (value - startValue) / (endValue - startValue);
        }

        public Color getFrameColor() {
            return frameColor;
        }

        public void setFrameColor(Color frameColor) {
            this.frameColor = frameColor;
            fireActionEvent();
        }

        public double getStartValue() {
            return startValue;
        }

        public void setStartValue(double startValue) {
            this.startValue = startValue;
            fireActionEvent();
        }

        public double getEndValue() {
            return endValue;
        }

        public void setEndValue(double endValue) {
            this.endValue = endValue;
            fireActionEvent();
        }

        public List<TimelineItem> getTimelineItemList() {
            return timelineItemList;
        }

        public Orientation getOrientation() {
            return orientation;
        }

        public void setOrientation(Orientation orientation) {
            this.orientation = orientation;
            fireActionEvent();
        }

        private Color getItemColor(TimelineItem item) {
            Map<String, Color> stringColorPairMap = this.getStringColorPairMap();
            String key = item.getKey();
            if (key == null) {
                return null;
            }
            Color color = stringColorPairMap.get(key);
            if (color == null) {
                return this.getDefaultCellColor();
            } else {
                return color;
            }
        }

        public Color getDefaultCellColor() {
            return defaultCellColor;
        }

        public void setDefaultCellColor(Color defaultCellColor) {
            this.defaultCellColor = defaultCellColor;
            fireActionEvent();
        }
    }

    public static class TimelineBarPanel extends JPanel implements TimelineUI {

        private static final long serialVersionUID = 1L;

        private TimelineFrame timelineFrame;

        public TimelineBarPanel(TimelineFrame timelineFrame) {
            yeriInit();
            this.initTimelineFrame(timelineFrame);
        }

        private void yeriInit() {
            this.setBackground(Color.WHITE);
            this.setOpaque(true);
        }

        public TimelineFrame getTimelineFrame() {
            return timelineFrame;
        }

        protected void initTimelineFrame(TimelineFrame timelineFrame) {
            this.timelineFrame = timelineFrame;
            Color frameColor = timelineFrame.getFrameColor();
            if (frameColor == null) {
                this.setBorder(null);
            } else {
                Border border = BorderFactory.createLineBorder(frameColor, 1);
                this.setBorder(border);
            }
            this.setPreferredBreadth(10);
        }

        public void setPreferredBreadth(int preferredBreadth) {
            if (this.getTimelineFrame().getOrientation().isHorizontal()) {
                SwingToolkit.fixHeight(this, preferredBreadth);
            } else {
                SwingToolkit.fixWidth(this, preferredBreadth);
            }
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            TimelineFrame timelineFrame = this.getTimelineFrame();
            Dimension size = getSize();
            for (TimelineItem item : timelineFrame.getTimelineItemList()) {
                if (item == null) {
                    continue;
                }
                item.drawSelf(this, timelineFrame, size, g2);
            }
        }

        @Override
        public int getPixelLength() {
            if (this.getTimelineFrame().getOrientation().isHorizontal()) {
                return this.getWidth();
            } else {
                return this.getHeight();
            }
        }

        public int getPreferredBreadth() {
            if (this.getTimelineFrame().getOrientation().isHorizontal()) {
                return this.getMaximumSize().height;
            } else {
                return this.getMaximumSize().width;
            }
        }
    }
}
