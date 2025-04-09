package game.report.srobjects;

import game.report.LATEXReportRenderer;
import game.report.SRLATEXRenderer;
import java.util.Set;

public class SRTableRendererLATEX implements ISRObjectRenderer {

    protected SRTable srTable;

    protected SRLATEXRenderer srlatexRenderer;

    private boolean tableTranspose = LATEXReportRenderer.configureData.getLatex().isTableTranspose();

    private int minColumnCountToTranspose = LATEXReportRenderer.configureData.getLatex().getMinColumnCountToTranspose();

    private int maxRowLength = LATEXReportRenderer.configureData.getLatex().getMaxRowLength();

    private int maxCellLength = LATEXReportRenderer.configureData.getLatex().getMaxCellLength();

    private double fixColumnWidth = LATEXReportRenderer.configureData.getLatex().getFixColumnWidth();

    private boolean tableRotate = LATEXReportRenderer.configureData.getLatex().isTableRotate();

    private Double roundTo = LATEXReportRenderer.configureData.getLatex().getTableNumberRounding();

    private boolean floatTables = LATEXReportRenderer.configureData.getLatex().isFloatTables();

    public SRTableRendererLATEX(SRTable srTable, SRLATEXRenderer srlatexRenderer) {
        this.srTable = srTable;
        this.srlatexRenderer = srlatexRenderer;
    }

    public void render() {
        String[][] data = srTable.getAsMatrix();
        boolean transposed = false;
        if (data[0].length > data.length && data[0].length > minColumnCountToTranspose) {
            if (tableTranspose == true) {
                data = transpose(data);
                transposed = true;
                System.out.println("latex report renderer: transposed data in table " + srTable.getCaption() + ".");
            }
        }
        int totalLength = getTotalLength(data);
        srlatexRenderer.append("\n");
        srlatexRenderer.append("\\pagebreak[1] \n");
        if (floatTables == true) srlatexRenderer.append("\\begin{table}[h]\n");
        srlatexRenderer.append("\\begin{center}{\n");
        if ((totalLength > maxRowLength) && (tableRotate == true)) srlatexRenderer.append("\\resizebox{!}{22cm}{\n" + "\\rotatebox{90}{\n");
        srlatexRenderer.append("\\begin{tabular}{");
        boolean header;
        for (int r = 0; r < data[0].length; r++) {
            if (srTable.isHeaderFirstColumn() && r == 0) srlatexRenderer.append("|c"); else {
                if (getMaxLengthOfColumn(data, r) > maxCellLength) {
                    srlatexRenderer.append("|p{" + fixColumnWidth + "cm}");
                } else srlatexRenderer.append("|l");
            }
        }
        srlatexRenderer.append("|}");
        srlatexRenderer.append("\\hline \n");
        for (int r = 0; r < data.length; r++) {
            for (int c = 0; c < data[r].length; c++) {
                header = (r == 0 && srTable.isHeaderFirstRow()) || (c == 0 && srTable.isHeaderFirstColumn());
                if (transposed == true) srlatexRenderer.append(applyAttributes(data[r][c], srTable.getAttributes(c, r))); else srlatexRenderer.append(applyAttributes(data[r][c], srTable.getAttributes(r, c)));
                if (c == data[r].length - 1) break;
                srlatexRenderer.append(header ? " & " : " & ");
            }
            srlatexRenderer.append(" \\\\ \n");
            if (srTable.isHeaderFirstRow() && r == 0) srlatexRenderer.append("\\hline \\hline \n"); else srlatexRenderer.append("\\hline \n");
        }
        srlatexRenderer.append("\\end{tabular}\n");
        if (totalLength > maxRowLength) srlatexRenderer.append("}}\n");
        if (floatTables == true) srlatexRenderer.append("\\caption{" + srTable.getCaption() + "}\n");
        srlatexRenderer.append("} \\end{center}\n");
        if (floatTables == true) srlatexRenderer.append("\\end{table}\n");
        if (floatTables == false) srlatexRenderer.append("" + "\\begin{center} \n " + "Table: " + srTable.getCaption() + "\n " + "\\end{center}\n");
    }

    private String applyAttributes(String text, Set<TextAttribute> attributes) {
        String newText = text;
        if (text.contains(".") == true) {
            try {
                Double number = Double.parseDouble(text);
                {
                    number = Math.round(number * roundTo) / roundTo;
                    text = number.toString();
                }
            } catch (Exception e) {
            }
        }
        newText = LATEXReportRenderer.toLatexText(text);
        if (attributes.contains(TextAttribute.BOLD)) {
            newText = "\\textbf{" + newText + "} ";
        }
        if (attributes.contains(TextAttribute.ITALIC)) {
            newText = "\\textit{" + newText + "} ";
        }
        return newText;
    }

    int getMaxLengthOfColumn(String data[][], int column) {
        int max = 0;
        for (int i = 0; i < data.length; i++) {
            int length = data[i][column].length();
            if (length > max) max = length;
        }
        return max;
    }

    int getTotalLength(String data[][]) {
        int maxLength = 0;
        int totalLength = 0;
        for (int i = 0; i < data[0].length; i++) {
            for (int j = 0; j < data.length; j++) {
                if (data[j][i].length() >= maxCellLength) {
                    maxLength = maxCellLength;
                    continue;
                }
                if (data[j][i].length() > maxLength) maxLength = data[j][i].length();
            }
            totalLength += maxLength;
            maxLength = 0;
        }
        return totalLength;
    }

    String[][] transpose(String[][] data) {
        int r = data.length;
        int c = data[0].length;
        String matrix[][] = new String[c][r];
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) {
                matrix[j][i] = data[i][j];
            }
        }
        return matrix;
    }
}
