package com.yerihyo.yeritools.html;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
import com.yerihyo.yeritools.CalendarToolkit;
import com.yerihyo.yeritools.collections.CollectionsToolkit;
import com.yerihyo.yeritools.html.HTMLToolkit.HtmlBarChartItem.ItemComparator;
import com.yerihyo.yeritools.io.FileToolkit;
import com.yerihyo.yeritools.math.StatisticsToolkit.NumberValueComparator;
import com.yerihyo.yeritools.swing.TimelineBarToolkit.Orientation;
import com.yerihyo.yeritools.text.StringToolkit;

public class HTMLToolkit {

    public static CharSequence getParamValueString(String[][] paramValuePairs) {
        StringBuilder result = new StringBuilder();
        boolean isFirst = true;
        if (paramValuePairs == null) {
            return result;
        }
        for (String[] paramValuePair : paramValuePairs) {
            if (paramValuePair.length != 2) {
                throw new RuntimeException("Param-value should be pair!");
            }
            if (isFirst) {
                result.append("?");
                isFirst = false;
            } else {
                result.append("&");
            }
            result.append(paramValuePair[0]).append('=').append(paramValuePair[1]);
        }
        return result;
    }

    public static String generateAnchor(File root, File f) {
        String relativePath = FileToolkit.getRelativePath(root, f);
        String newPath = FileToolkit.fileSeparatorFromPlatformToSlash(relativePath);
        return generateAnchor(f.getName(), newPath);
    }

    public static String generateAnchor(String address) {
        return generateAnchor(address, address);
    }

    public static String generateAnchor(String name, String address) {
        return "<a href=\"" + address + "\">" + name + "</a>";
    }

    public static void main(String[] args) {
        test02();
    }

    private static String header = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\"\"http://www.w3.org/TR/html4/strict.dtd\">" + "<HTML>" + "	<HEAD><LINK href='index.css' rel='stylesheet' type='text/css'></HEAD>" + "	<BODY>";

    private static String footer = "</BODY></HTML>";

    private static void test02() {
        File outFolder = new File("out/testw/");
        outFolder.mkdirs();
        File ofile = new File(outFolder, "index.html");
        StringBuilder content = new StringBuilder();
        content.append(header);
        DividedCell dividedCell = new DividedCell();
        dividedCell.setPixelLength(800);
        dividedCell.setOrientation(Orientation.TOP_TO_BOTTOM);
        DividedCellItem item1 = new DividedCellItem();
        item1.setLength(100);
        item1.setContent("HH");
        dividedCell.addItem(item1);
        DividedCellItem item2 = new DividedCellItem();
        item2.setLength(200);
        item2.setContent("KL");
        dividedCell.addItem(item2);
        content.append(dividedCell.toHtml());
        content.append(footer);
        try {
            FileToolkit.writeTo(ofile, content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unused")
    private static void test01() {
        HtmlTimelineBar htmlTimelineBar = new HtmlTimelineBar();
        htmlTimelineBar.addItem("class", "f", 200);
        htmlTimelineBar.addItem("class", "g", 400);
        htmlTimelineBar.addItem("class", "f", 500);
        htmlTimelineBar.setOrientation(Orientation.LEFT_TO_RIGHT);
        htmlTimelineBar.setLength(600);
        htmlTimelineBar.setBreadth(10);
        System.out.println(htmlTimelineBar.toHTML());
    }

    public static interface HtmlBarChartItem<T> {

        String getHtmlClass();

        String getHtmlId();

        T getValue();

        String toString();

        public static class ItemComparator<T> implements Comparator<HtmlBarChartItem<? extends T>> {

            private Comparator<? super T> c;

            public ItemComparator(Comparator<? super T> c) {
                this.c = c;
            }

            @Override
            public int compare(HtmlBarChartItem<? extends T> o1, HtmlBarChartItem<? extends T> o2) {
                return c.compare(o1.getValue(), o2.getValue());
            }
        }

        ;
    }

    public static class DefaultHtmlBarChartItem<T> implements HtmlBarChartItem<T> {

        private String htmlClass;

        private String htmlId;

        private T value;

        public DefaultHtmlBarChartItem(String htmlClass, String htmlId, T value) {
            this.htmlClass = htmlClass;
            this.value = value;
        }

        public String getHtmlClass() {
            return htmlClass;
        }

        public String getHtmlId() {
            return htmlId;
        }

        public T getValue() {
            return value;
        }

        public String toString() {
            return "{" + htmlClass + "-" + htmlId + ",something}";
        }
    }

    public static String getTROption(Orientation orientation, int breadth) {
        StringBuilder builder = new StringBuilder();
        if (orientation.isHorizontal()) {
            builder.append(" height='");
            builder.append(breadth).append("' ");
        } else if (orientation.isVertical()) {
            builder.append(" width='");
            builder.append(breadth).append("' ");
        } else {
            builder.append("");
        }
        return builder.toString();
    }

    public static String getTDOption(Orientation orientation, int breadth, int realCellWidth) {
        StringBuilder builder = new StringBuilder();
        if (orientation.isHorizontal()) {
            builder.append(" width='").append(realCellWidth).append("' ");
        } else if (orientation.isVertical()) {
            builder.append(" height='").append(realCellWidth).append("' ");
            builder.append(" width='").append(breadth).append("' ");
        } else {
            builder.append("");
        }
        return builder.toString();
    }

    public static String getLengthTag(Orientation orientation) {
        if (orientation.isHorizontal()) {
            return "width";
        } else {
            return "height";
        }
    }

    public static String getBreadthTag(Orientation orientation) {
        if (orientation.isHorizontal()) {
            return "height";
        } else {
            return "width";
        }
    }

    public static class HtmlTimelineBar {

        private int length = 800;

        private int breadth = 10;

        private double valueLowerBound = 0;

        private double valueUpperBound = 0;

        private int preferredCellThickness = 3;

        private List<HtmlBarChartItem<? extends Number>> itemList = new ArrayList<HtmlBarChartItem<? extends Number>>();

        private Orientation orientation = Orientation.LEFT_TO_RIGHT;

        public HtmlTimelineBar() {
        }

        public void addItem(String htmlClass, String htmlId, Number number) {
            itemList.add(new DefaultHtmlBarChartItem<Number>(htmlClass, htmlId, number));
        }

        public void addItem(HtmlBarChartItem<? extends Number> item) {
            itemList.add(item);
        }

        public void addItemList(String htmlClass, String htmlId, List<Number> numberList) {
            for (Number number : numberList) {
                HtmlBarChartItem<Number> item = new DefaultHtmlBarChartItem<Number>(htmlClass, htmlId, number);
                this.itemList.add(item);
            }
        }

        public void addItemList(List<? extends HtmlBarChartItem<Number>> itemList) {
            this.itemList.addAll(itemList);
        }

        public double getValueLowerBound() {
            Comparator<HtmlBarChartItem<? extends Number>> c = new ItemComparator<Number>(new NumberValueComparator());
            int minValueIndex = CollectionsToolkit.getMinMaxIndex(itemList, c)[0];
            double minValue = itemList.get(minValueIndex).getValue().doubleValue();
            if (minValue < valueLowerBound) {
                valueLowerBound = minValue;
            }
            return valueLowerBound;
        }

        public double getValueUpperBound() {
            Comparator<HtmlBarChartItem<? extends Number>> c = new ItemComparator<Number>(new NumberValueComparator());
            int maxValueIndex = CollectionsToolkit.getMinMaxIndex(itemList, c)[1];
            double maxValue = itemList.get(maxValueIndex).getValue().doubleValue();
            if (maxValue > valueUpperBound) {
                valueUpperBound = maxValue;
            }
            return valueUpperBound;
        }

        private int getPixelPosition(double value) {
            double valueLowerBound = getValueLowerBound();
            double valueUpperBound = getValueUpperBound();
            return ((int) Math.ceil(this.getLength() * value / (valueUpperBound - valueLowerBound)));
        }

        public HtmlCellBar makeCellList(List<? extends HtmlBarChartItem<? extends Number>> itemList) {
            HtmlCellBar htmlCellBar = new HtmlCellBar();
            if (itemList == null || itemList.size() == 0) {
                htmlCellBar.addHtmlCell(new DefaultHtmlCell(0, this.getLength(), null, null));
                return htmlCellBar;
            }
            HtmlBarChartItem<? extends Number> currentItem = null;
            HtmlBarChartItem<? extends Number> postItem = itemList.get(0);
            int currentPosition = Integer.MIN_VALUE;
            int postPosition = this.getPixelPosition(postItem.getValue().doubleValue());
            int preferredCellThickness = this.getPreferredCellThickness();
            int preferredCellLeftThickness = preferredCellThickness / 2;
            int preferredCellRightThickness = (preferredCellThickness + 1) / 2;
            int prevRightPosition = 0;
            for (int i = 0; i < itemList.size(); i++) {
                currentItem = postItem;
                if (i < itemList.size() - 1) {
                    postItem = itemList.get(i + 1);
                } else {
                    postItem = null;
                }
                currentPosition = postPosition;
                if (postItem != null) {
                    double postValue = postItem.getValue().doubleValue();
                    postPosition = this.getPixelPosition(postValue);
                } else {
                    postPosition = Integer.MAX_VALUE;
                }
                int currentLeftPosition = Math.max(prevRightPosition, currentPosition - preferredCellLeftThickness);
                int currentRightPosition;
                if (i == itemList.size() - 1) {
                    currentRightPosition = Math.min(currentPosition + preferredCellRightThickness, this.getLength());
                } else {
                    int postLeftPosition = postPosition - preferredCellLeftThickness;
                    int realCellRightThickness;
                    if (currentPosition + preferredCellRightThickness > postLeftPosition) {
                        realCellRightThickness = (postPosition - currentPosition + 1) / 2;
                    } else {
                        realCellRightThickness = preferredCellRightThickness;
                    }
                    currentRightPosition = currentPosition + realCellRightThickness;
                }
                if (currentLeftPosition - prevRightPosition > 0) {
                    DefaultHtmlCell emptyCell = new DefaultHtmlCell();
                    emptyCell.setStart(prevRightPosition);
                    emptyCell.setEnd(currentLeftPosition);
                    htmlCellBar.addHtmlCell(emptyCell);
                }
                DefaultHtmlCell realCell = new DefaultHtmlCell();
                realCell.setHtmlClass(currentItem.getHtmlClass());
                realCell.setHtmlId(currentItem.getHtmlId());
                realCell.setStart(currentLeftPosition);
                realCell.setEnd(currentRightPosition);
                htmlCellBar.addHtmlCell(realCell);
                prevRightPosition = currentRightPosition;
            }
            htmlCellBar.addHtmlCell(new DefaultHtmlCell(prevRightPosition, this.getLength(), null, null));
            return htmlCellBar;
        }

        public String toHTML() {
            Comparator<HtmlBarChartItem<? extends Number>> c = new ItemComparator<Number>(new NumberValueComparator());
            Collections.sort(this.itemList, c);
            Orientation orientation = this.getOrientation();
            HtmlCellBar htmlCellBar = makeCellList(this.itemList);
            htmlCellBar.setOrientation(orientation);
            htmlCellBar.setPixelLength(this.getLength());
            htmlCellBar.setTableClassName("timeline");
            return htmlCellBar.toHTML();
        }

        public void setValueLowerBound(double valueLowerBound) {
            this.valueLowerBound = valueLowerBound;
        }

        public void setValueUpperBound(double valueUpperBound) {
            this.valueUpperBound = valueUpperBound;
        }

        public int getPreferredCellThickness() {
            return preferredCellThickness;
        }

        public void setPreferredCellThickness(int cellWidth) {
            this.preferredCellThickness = cellWidth;
        }

        public Orientation getOrientation() {
            return orientation;
        }

        public void setOrientation(Orientation orientaion) {
            this.orientation = orientaion;
        }

        public int getLength() {
            return length;
        }

        public int getBreadth() {
            return breadth;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public void setBreadth(int breadth) {
            this.breadth = breadth;
        }
    }

    public static interface HTMLable {

        public String toHTML();
    }

    public static class ImageTag implements HTMLable {

        private URI imageURI;

        public ImageTag(File socialIcon) {
            imageURI = socialIcon.toURI();
        }

        public ImageTag(String string) {
            try {
                imageURI = new URI(string);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(e);
            }
        }

        public URI getImageURI() {
            return imageURI;
        }

        public void setImageURI(URI imageURI) {
            this.imageURI = imageURI;
        }

        @Override
        public String toHTML() {
            StringBuilder builder = new StringBuilder();
            builder.append("<IMG src='");
            String path = this.getImageURI().getPath();
            builder.append(path);
            builder.append("'>");
            return builder.toString();
        }
    }

    public static interface HtmlCell {

        Number getStart();

        Number getEnd();

        String getHtmlClass();

        String getHtmlId();

        double getLength();

        int getPixelLength(HtmlCellBar htmlCellBar);

        String getContent();
    }

    public static class DefaultHtmlCell implements HtmlCell {

        private Number start;

        private Number end;

        private String htmlClass;

        private String htmlId;

        private String content;

        public void setContent(String content) {
            this.content = content;
        }

        public String getContent() {
            return content;
        }

        public String toString() {
            return "(" + start + "," + end + ")";
        }

        public DefaultHtmlCell() {
        }

        public DefaultHtmlCell(Number start, Number end, String htmlClass, String htmlId) {
            this.setStart(start);
            this.setEnd(end);
            this.setHtmlClass(htmlClass);
            this.setHtmlId(htmlId);
        }

        public Number getStart() {
            return start;
        }

        public void setStart(Number start) {
            this.start = start;
        }

        public Number getEnd() {
            return end;
        }

        public void setEnd(Number end) {
            this.end = end;
        }

        public String getHtmlClass() {
            return htmlClass;
        }

        public void setHtmlClass(String htmlClass) {
            this.htmlClass = htmlClass;
        }

        public String getHtmlId() {
            return htmlId;
        }

        public void setHtmlId(String htmlId) {
            this.htmlId = htmlId;
        }

        public int getPixelLength(HtmlCellBar htmlCellBar) {
            double totalLength = htmlCellBar.getTotalLength();
            double start = this.getStart().doubleValue() * htmlCellBar.getPixelLength() / totalLength;
            double end = this.getEnd().doubleValue() * htmlCellBar.getPixelLength() / totalLength;
            return (int) (end - start);
        }

        public double getLength() {
            return this.getEnd().doubleValue() - this.getStart().doubleValue();
        }
    }

    public static class HtmlCellBar {

        private List<HtmlCell> htmlCellList = new ArrayList<HtmlCell>();

        private Orientation orientation;

        private int breadth = 10;

        private int pixelLength = 800;

        public double getTotalLength() {
            double totalLength = 0;
            for (HtmlCell htmlCell : htmlCellList) {
                totalLength += htmlCell.getLength();
            }
            return totalLength;
        }

        public int getPixelLength() {
            return pixelLength;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append('[');
            for (HtmlCell htmlCell : this.getHtmlCellList()) {
                builder.append('(');
                builder.append(htmlCell.getStart());
                builder.append(',');
                builder.append(htmlCell.getEnd());
                builder.append(')');
            }
            builder.append(']');
            return builder.toString();
        }

        public void setPixelLength(int pixelLength) {
            this.pixelLength = pixelLength;
        }

        public void addHtmlCell(HtmlCell htmlCell) {
            this.htmlCellList.add(htmlCell);
        }

        public void removeHtmlCell(HtmlCell htmlCell) {
            this.htmlCellList.remove(htmlCell);
        }

        public void addHtmlCellCollection(Collection<? extends HtmlCell> htmlCellCollection) {
            this.htmlCellList.addAll(htmlCellCollection);
        }

        private String tableClassName;

        private String tableIdName;

        public String getTableClassName() {
            return tableClassName;
        }

        public void setTableClassName(String tableClassName) {
            this.tableClassName = tableClassName;
        }

        public String getTableIdName() {
            return tableIdName;
        }

        public void setTableIdName(String tableIdName) {
            this.tableIdName = tableIdName;
        }

        public String toHTML() {
            StringBuilder builder = new StringBuilder();
            builder.append(StringToolkit.newLine()).append("<!-- START OF HtmlTimelineBar -->").append(StringToolkit.newLine());
            builder.append("<TABLE ");
            builder.append(" class='").append(this.getTableClassName()).append("' ");
            builder.append(" id='").append(this.getTableIdName()).append("' ");
            builder.append(" cellpadding='0' cellspacing='0' ");
            builder.append(">");
            Orientation orientation = this.getOrientation();
            int breadth = this.getBreadth();
            if (orientation.isHorizontal()) {
                builder.append("<TR ").append(getTROption(orientation, breadth)).append(">").append(StringToolkit.newLine());
            }
            List<HtmlCell> htmlCellList = this.getHtmlCellList();
            for (int i = 0; i < htmlCellList.size(); i++) {
                HtmlCell htmlCell = htmlCellList.get(i);
                if (orientation.isVertical()) {
                    builder.append("<TR ").append(getTROption(orientation, breadth)).append(">").append(StringToolkit.newLine());
                }
                builder.append("<TD ");
                builder.append(getTDOption(orientation, breadth, htmlCell.getPixelLength(this)));
                String htmlClass = htmlCell.getHtmlClass();
                if (htmlClass != null) {
                    builder.append(" class='").append(htmlClass).append("' ");
                }
                String id = htmlCell.getHtmlId();
                if (id != null) {
                    builder.append(" id='").append(id).append("' ");
                }
                builder.append(">");
                String cellContent = htmlCell.getContent();
                if (!(cellContent == null || cellContent.length() == 0)) {
                    builder.append(cellContent);
                }
                builder.append("</TD>").append(StringToolkit.newLine());
                if (orientation.isVertical()) {
                    builder.append("</TR>").append(StringToolkit.newLine());
                }
            }
            if (orientation.isHorizontal()) {
                builder.append("</TR>");
            }
            builder.append("</TABLE>").append(StringToolkit.newLine());
            return builder.toString();
        }

        public List<HtmlCell> getHtmlCellList() {
            return htmlCellList;
        }

        public Orientation getOrientation() {
            return orientation;
        }

        public int getBreadth() {
            return breadth;
        }

        public void setOrientation(Orientation orientation) {
            this.orientation = orientation;
        }

        public void setBreadth(int breadth) {
            this.breadth = breadth;
        }
    }

    public static class DividedCellItem {

        private double length;

        private String content;

        public double getLength() {
            return length;
        }

        public void setLength(double length) {
            this.length = length;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getContent() {
            return content;
        }
    }

    public static class DividedCell {

        private List<DividedCellItem> itemList = new ArrayList<DividedCellItem>();

        private int dividerLength = 2;

        private int breadth = 2;

        private int pixelLength = 800;

        private Orientation orientation = Orientation.TOP_TO_BOTTOM;

        private String lineCellClass;

        private String lineCellId;

        public void addItem(DividedCellItem item) {
            itemList.add(item);
        }

        private double getTotalLength() {
            double total = 0;
            for (DividedCellItem item : itemList) {
                double itemLength = item.getLength();
                total += itemLength;
            }
            return total;
        }

        private DefaultHtmlCell makeLineCell(int prevPixelEndPosition) {
            DefaultHtmlCell lineCell = new DefaultHtmlCell();
            lineCell.setStart(prevPixelEndPosition);
            prevPixelEndPosition += this.getDividerLength();
            lineCell.setEnd(prevPixelEndPosition);
            lineCell.setHtmlClass(this.getLineCellClass());
            lineCell.setHtmlId(this.getLineCellId());
            return lineCell;
        }

        public String toHtml() {
            HtmlCellBar cellBar = new HtmlCellBar();
            cellBar.setOrientation(this.getOrientation());
            cellBar.setBreadth(this.getBreadth());
            cellBar.setPixelLength(this.getPixelLength());
            double totalLength = this.getTotalLength();
            double lengthSum = 0;
            int prevPixelEndPosition = 0;
            int itemListSize = itemList.size();
            DefaultHtmlCell lineCell = this.makeLineCell(prevPixelEndPosition);
            prevPixelEndPosition += this.getDividerLength();
            cellBar.addHtmlCell(lineCell);
            for (int i = 0; i < itemListSize; i++) {
                DividedCellItem dcItem = itemList.get(i);
                double currentLength = dcItem.getLength();
                lengthSum += currentLength;
                int currentPixelEndPosition = (int) (lengthSum * this.getPixelLength() / totalLength);
                if (i == itemListSize - 1) {
                    currentPixelEndPosition -= this.getDividerLength();
                } else {
                    currentPixelEndPosition -= this.getDividerLength() / 2;
                }
                if (currentPixelEndPosition <= prevPixelEndPosition) {
                    continue;
                }
                DefaultHtmlCell contentCell = new DefaultHtmlCell();
                contentCell.setStart(prevPixelEndPosition);
                contentCell.setEnd(currentPixelEndPosition);
                contentCell.setContent(dcItem.getContent());
                contentCell.setHtmlId("content");
                cellBar.addHtmlCell(contentCell);
                prevPixelEndPosition = currentPixelEndPosition;
                lineCell = this.makeLineCell(prevPixelEndPosition);
                prevPixelEndPosition += this.getDividerLength();
                cellBar.addHtmlCell(lineCell);
            }
            return cellBar.toHTML();
        }

        public int getDividerLength() {
            return dividerLength;
        }

        public int getBreadth() {
            return breadth;
        }

        public Orientation getOrientation() {
            return orientation;
        }

        public int getPixelLength() {
            return pixelLength;
        }

        public void setPixelLength(int pixelLength) {
            this.pixelLength = pixelLength;
        }

        public void setOrientation(Orientation orientation) {
            this.orientation = orientation;
        }

        public String getLineCellId() {
            return lineCellId;
        }

        public void setLineCellId(String lineCellId) {
            this.lineCellId = lineCellId;
        }

        public String getLineCellClass() {
            return lineCellClass;
        }

        public void setLineCellClass(String lineCellClass) {
            this.lineCellClass = lineCellClass;
        }
    }

    public static String createCalendar(Calendar date) {
        CalendarToolkit.truncate(date, GregorianCalendar.DAY_OF_MONTH);
        StringBuilder builder = new StringBuilder();
        builder.append("<TABLE>");
        builder.append("<TR>");
        builder.append("<TD>S</TD>");
        builder.append("<TD>M</TD>");
        builder.append("<TD>T</TD>");
        builder.append("<TD>W</TD>");
        builder.append("<TD>T</TD>");
        builder.append("<TD>F</TD>");
        builder.append("<TD>S</TD>");
        builder.append("</TR>");
        builder.append("</TABLE>");
        return builder.toString();
    }

    public static String makeVerticalString(String string) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            if (i != 0) {
                builder.append("<br>").append(StringToolkit.newLine());
            }
            builder.append(string.charAt(i));
        }
        return builder.toString();
    }

    public static void stripHtmlTags(File sourceFile, File destFile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(sourceFile));
        PrintWriter writer = new PrintWriter(new FileWriter(destFile));
        String oneline = null;
        while ((oneline = reader.readLine()) != null) {
            String newOneline = oneline.replaceAll("\\<.*?>", StringToolkit.newLine()).trim();
            if (newOneline.length() == 0) {
                continue;
            }
            writer.println(newOneline);
        }
        reader.close();
        writer.close();
    }
}
