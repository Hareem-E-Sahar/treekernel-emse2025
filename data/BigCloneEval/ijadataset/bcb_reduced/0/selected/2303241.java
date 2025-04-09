package com.googlecode.gaal.vis.impl;

import java.io.IOException;
import java.util.List;
import com.googlecode.gaal.vis.api.EdgeStyle;
import com.googlecode.gaal.vis.api.VectorDrawing;

public class TikzVectorDrawing implements VectorDrawing {

    private static final String ARROW_STYLES = "arrownode/.style={double arrow, draw, text width=2cm, text centered},\n" + "sarrownode/.style={single arrow, draw, text width=1cm, text centered},\n";

    private static final String VECTOR_STYLES = "vecnode/.style={midway,sloped,above},\n";

    private static final String VECTOR_FORMAT = "\\draw[%s] (0,0) -- (%.2f,%.2f) node [%s] {%s};\n";

    private static final String ANGLE_FORMAT = "\\filldraw[fill=green!20,draw=green!50!black] (0,0) -- (%.2f:3mm) arc (%.2f:%.2f:3mm) -- cycle;\n" + "\\draw (%.2f:2mm) node[green!50!black] {%s};\n";

    private static final String GRID = "\\draw[step=1cm,gray,very thin] (-0.05,-0.05) grid (%.2f,%.2f);\n";

    private static final String TICKS = "\\foreach \\x in {%s}\n" + "\\draw (\\x cm,1pt) -- (\\x cm,-1pt) node[anchor=north] {$\\x$};\n" + "\\foreach \\y in {%s}\n" + "\\draw (1pt,\\y cm) -- (-1pt,\\y cm) node[anchor=east] {$\\y$};\n";

    private static final String NUM_PAR_DOC = "\\node (d%d) at (%.2f,%.2f) [arrownode] {$d_%d$};\n" + "\\draw (d%d.west) node[left=0.1 of d%d, srcnode]{%s};\n" + "\\draw (d%d.east) node[right=0.1 of d%d, dstnode]{%s};\n";

    private static final String PAR_DOC = "\\node (d%d) at (%.2f,%.2f) [arrownode] {};\n" + "\\draw (d%d.west) node[left=0.1 of d%d, srcnode]{%s};\n" + "\\draw (d%d.east) node[right=0.1 of d%d, dstnode]{%s};\n";

    private static final String NUM_DOC = "\\node (d%d) at (%.2f,%.2f) [dstnode]{%s};\n" + "\\draw (d%d.west) node[left=0.1 of d%d, sarrownode] {$d_%d$};\n";

    private static final String DOC = "\\draw (%.2f,%.2f) node[dstnode]{%s};\n";

    private static final String HEADER = "[scale=%.2f,\n";

    private static final String FOOTER = "};\n";

    private static final String MATRIX_HEADER = "\\matrix[row sep=1,column sep=1, nodes={scale=0.7,fill=blue!20,minimum size=6mm}] {\n";

    private int docWidth = 3;

    private final Appendable output;

    private final String caption;

    private final String label;

    private final String placement;

    private final double scale;

    private final double documentDistance;

    private final StringBuilder systemBuffer = new StringBuilder();

    private final StringBuilder vectorBuffer = new StringBuilder();

    private final StringBuilder docBuffer = new StringBuilder();

    private final StringBuilder matrixBuffer = new StringBuilder();

    private double maxX;

    private double maxY;

    private int docCounter = 1;

    public TikzVectorDrawing(Appendable output, String caption, String label, double scale, double documentDistance) {
        this(output, caption, label, "ht!", scale, documentDistance);
    }

    public TikzVectorDrawing(Appendable output, String caption, String label, String placement, double scale, double documentDistance) {
        this.output = output;
        this.caption = caption;
        this.label = label;
        this.placement = placement;
        this.scale = scale;
        this.documentDistance = documentDistance;
    }

    private void drawCoordinateSystem() {
        systemBuffer.append(String.format(GRID, Math.ceil(maxX) + 0.05, Math.ceil(maxY) + 0.05));
        systemBuffer.append(String.format(TICKS, toTickList((int) Math.ceil(maxX)), toTickList((int) Math.ceil(maxY))));
        appendVector(systemBuffer, Math.ceil(maxX) + 0.1, 0, "$d_1$", "right", TikzConstants.BLACK_VECTOR);
        appendVector(systemBuffer, 0, Math.ceil(maxY) + 0.1, "$d_2$", "above", TikzConstants.BLACK_VECTOR);
    }

    @Override
    public void drawVector(int x, int y, String label, EdgeStyle style) {
        appendVector(vectorBuffer, x, y, label, "vecnode", style);
        if (x > maxX) maxX = x;
        if (y > maxY) maxY = y;
    }

    @Override
    public void drawAngle(double start, double end, String label) {
        double mid = (start + end) / 2;
        vectorBuffer.append(String.format(ANGLE_FORMAT, start, start, end, mid, label));
    }

    @Override
    public void drawDocument(String source, String target, boolean showNumber) {
        if (showNumber) appendNumberedDoc(docBuffer, maxX / 2, -(documentDistance * docCounter), docCounter++, source, target); else appendDoc(docBuffer, maxX / 2, -(documentDistance * docCounter), docCounter++, source, target);
    }

    @Override
    public void drawDocument(String text, boolean showArrow) {
        if (showArrow) appendNumberedDoc(docBuffer, maxX / 2, -(documentDistance * docCounter), docCounter++, text); else appendDoc(docBuffer, maxX / 2, -(documentDistance * docCounter), text);
    }

    @Override
    public void drawMatrix(List<String> rowLabels, List<String> columnLabels, int[][] values) {
        int rowLabelLength = maxStringLength(rowLabels) * 6;
        int columnLabelLength = maxStringLength(columnLabels) * 6;
        matrixBuffer.append(MATRIX_HEADER);
        for (String label : columnLabels) {
            matrixBuffer.append('&');
            matrixBuffer.append(String.format("\\node [fill=red!20,rotate=90, minimum width=%d] {%s};\n", columnLabelLength, label));
        }
        matrixBuffer.append("\\\\\n");
        for (int i = 0; i < values.length; i++) {
            matrixBuffer.append(String.format("\\node [fill=orange!20,minimum width=%d] {%s};\n", rowLabelLength, rowLabels.get(i)));
            for (int j = 0; j < values[i].length; j++) {
                matrixBuffer.append('&');
                matrixBuffer.append(String.format("\\node {%d};\n", values[i][j]));
            }
            matrixBuffer.append("\\\\\n");
        }
        matrixBuffer.append(FOOTER);
    }

    private void appendVector(StringBuilder buffer, double x, double y, String label, String labelStyle, EdgeStyle style) {
        buffer.append(String.format(VECTOR_FORMAT, style, x, y, labelStyle, label));
    }

    private void appendNumberedDoc(StringBuilder buffer, double x, double y, int number, String source, String target) {
        buffer.append(String.format(NUM_PAR_DOC, number, x, y, number, number, number, source, number, number, target));
    }

    private void appendDoc(StringBuilder buffer, double x, double y, int number, String source, String target) {
        buffer.append(String.format(PAR_DOC, number, x, y, number, number, source, number, number, target));
    }

    private void appendNumberedDoc(StringBuilder buffer, double x, double y, int number, String text) {
        buffer.append(String.format(NUM_DOC, number, x, y, text, number, number, number));
    }

    private void appendDoc(StringBuilder buffer, double x, double y, String text) {
        buffer.append(String.format(DOC, x, y, text));
    }

    @Override
    public void flush() throws IOException {
        drawCoordinateSystem();
        if (caption != null && placement != null) output.append(String.format(AbstractTikzDrawing.FIGURE_HEADER, placement));
        output.append(AbstractTikzDrawing.TIKZ_HEADER);
        output.append(String.format(HEADER, scale));
        if (vectorBuffer.length() != 0) {
            output.append(VECTOR_STYLES);
        }
        if (docBuffer.length() != 0) {
            output.append(ARROW_STYLES);
            output.append(docStyles());
        }
        output.append("]\n{\n");
        if (vectorBuffer.length() != 0) {
            output.append(systemBuffer);
            output.append(vectorBuffer);
        }
        output.append(matrixBuffer);
        output.append(docBuffer);
        output.append(FOOTER);
        output.append(AbstractTikzDrawing.TIKZ_FOOTER);
        if (caption != null && label != null) output.append(String.format(AbstractTikzDrawing.FIGURE_FOOTER, caption, label));
    }

    private String docStyles() {
        return String.format("srcnode/.style={anchor=east,copy shadow={opacity=.5},fill=blue!20,draw=blue,text width=%dcm,text justified},\n" + "dstnode/.style={anchor=west,copy shadow={opacity=.5},fill=green!20,draw=green,text width=%dcm,text justified},\n", docWidth, docWidth);
    }

    private static String toTickList(int n) {
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (int i = 1; i <= n; i++) {
            if (isFirst) isFirst = false; else sb.append(',');
            sb.append(i);
        }
        return sb.toString();
    }

    public void setDocumentWidth(int docWidth) {
        this.docWidth = docWidth;
    }

    public static int maxStringLength(List<String> list) {
        int max = 0;
        for (String string : list) {
            if (string.length() > max) max = string.length();
        }
        return max;
    }
}
