package org.velma.plots;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.image.MemoryImageSource;
import java.util.Hashtable;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EtchedBorder;
import org.velma.data.MSASelection;
import org.velma.data.msa.MSA;
import org.velma.data.msa.Sequence;
import org.velma.tools.AlignmentToolkit;
import org.velma.tools.SubstitutionMatrixFactory;
import com.lowagie.text.pdf.PdfContentByte;

/**
 * This view displays a similarity matrix for all sequences in the alignment.
 * Its shown as a raster, with each pixel representing a pair of sequences and
 * the color indicating how similar those two sequences are. Colors along the
 * diagonal should be consistent, since all sequences are equally similar to
 * themselves.
 * 
 * @author Andy Walsh
 * @author Hyun Kyu Shim
 * 
 */
public class AlignSeqDotPlot extends InterSeqPlotPanel {

    private static final long serialVersionUID = -2080245844268982086L;

    private MemoryImageSource dotplot;

    private Image image;

    private int alignmentSize;

    private class DotPlotGenerator implements Runnable {

        private MSA msa;

        private String matrixName;

        private double minVal = Double.POSITIVE_INFINITY;

        private double maxVal = Double.NEGATIVE_INFINITY;

        private JPanel glassPane;

        private JProgressBar progressBar;

        public DotPlotGenerator(MSA msa, String matrixName, JPanel glassPane, JProgressBar progressBar) {
            super();
            this.msa = msa;
            this.matrixName = matrixName;
            this.glassPane = glassPane;
            this.progressBar = progressBar;
        }

        public void run() {
            try {
                double simmat[][] = calculateSimilarityMatrix(msa, matrixName);
                int pix[] = calculateColorArray(simmat, msa.getPosCount());
                dotplot = new MemoryImageSource(simmat.length, simmat.length, pix, 0, simmat.length);
                done();
            } catch (Exception e) {
                e.printStackTrace();
            } catch (Error e) {
                e.printStackTrace();
            }
        }

        private int[] calculateColorArray(double valuematrix[][], int max) {
            int pix[] = new int[valuematrix.length * valuematrix.length];
            Color minColor = Color.RED;
            Color maxColor = Color.BLUE;
            float minHue = Color.RGBtoHSB(minColor.getRed(), minColor.getGreen(), minColor.getBlue(), null)[0];
            float maxHue = Color.RGBtoHSB(maxColor.getRed(), maxColor.getGreen(), maxColor.getBlue(), null)[0];
            float diffHue = Math.abs(maxHue - minHue);
            float minVal = (float) this.minVal;
            float diffVal = ((float) maxVal) - minVal;
            minHue = Math.min(minHue, maxHue);
            int index = 0;
            for (int i = 0; i < valuematrix.length; i++) {
                for (int j = 0; j < valuematrix.length; j++) pix[index++] = Color.HSBtoRGB(((float) (valuematrix[i][j] - minVal) / diffVal) * diffHue + minHue, 1f, 1f);
            }
            return pix;
        }

        private double[][] calculateSimilarityMatrix(MSA ma, String matrixName) {
            SubstitutionMatrixFactory factory = SubstitutionMatrixFactory.getSubstitutionMatrixFactory();
            double simmat[][] = new double[ma.getSeqCount()][ma.getSeqCount()];
            double maxk = (double) (simmat.length + 1.0) * ((double) simmat.length / 2.0);
            Hashtable<Character, Integer> letter2index = SubstitutionMatrixFactory.getSymbolMap();
            Short matrix[][] = factory.getMatrix(matrixName);
            Short matrix2[][] = new Short[matrix.length][matrix[matrix.length - 1].length];
            for (int i = 0; i < matrix2.length; i++) for (int j = 0; j < matrix2[i].length; j++) if (j <= i) matrix2[i][j] = matrix[i][j]; else matrix2[i][j] = matrix[j][i];
            int k = 0;
            Sequence seqi;
            for (int i = 0; i < simmat.length; i++) {
                seqi = ma.getSequence(i);
                simmat[i][i] = (int) AlignmentToolkit.scorePair(seqi, seqi, 10, 10, matrix2, letter2index);
                if (++k % 250 == 0) progressBar.setValue((int) ((double) k / maxk * 100.0));
            }
            Sequence seqj;
            for (int i = 0; i < simmat.length; i++) {
                seqi = ma.getSequence(i);
                for (int j = i + 1; j < simmat.length; j++) {
                    seqj = ma.getSequence(j);
                    simmat[i][j] = (int) AlignmentToolkit.scorePair(seqi, seqj, 10, 10, matrix2, letter2index) / Math.sqrt(simmat[i][i] * simmat[j][j]);
                    simmat[j][i] = simmat[i][j];
                    if (simmat[i][j] < minVal) minVal = simmat[i][j];
                    if (simmat[i][j] > maxVal) maxVal = simmat[i][j];
                    if (++k % 250 == 0) {
                        progressBar.setValue((int) ((double) k / maxk * 100.0));
                    }
                }
            }
            for (int i = 0; i < simmat.length; i++) simmat[i][i] = 1;
            if (1 > maxVal) maxVal = 1;
            return simmat;
        }

        public void done() {
            glassPane.removeAll();
            glassPane.setVisible(false);
        }
    }

    public AlignSeqDotPlot(MSA msa, MSASelection selection, String viewName, String[] keyChain, Color selectionBoxColor, String matrixName, JPanel glassPane) {
        super(selection, viewName, keyChain, selectionBoxColor);
        this.alignmentSize = msa.getSeqCount();
        this.xAxis = false;
        this.yAxis = false;
        calculateBounds();
        final JProgressBar progressBar = new JProgressBar(0, 100);
        DotPlotGenerator task = new DotPlotGenerator(msa, matrixName, glassPane, progressBar);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setString("Calculating similarity matrix...");
        GridBagConstraints progressBarConstraints = new GridBagConstraints();
        JPanel borderPanel = new JPanel();
        borderPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        borderPanel.add(progressBar);
        glassPane.removeAll();
        glassPane.add(borderPanel, progressBarConstraints);
        glassPane.setVisible(true);
        glassPane.validate();
        new Thread(task).start();
    }

    public void calcSelectedSequences() {
        selection.clearSelection(MSASelection.MODE_SEQUENCE);
        boolean[] selectedSequences = selection.getSeqSelected();
        float lowX = graphToPlotX(Math.min(boxStartX, boxStopX)), highX = graphToPlotX(Math.max(boxStartX, boxStopX)), lowY = graphToPlotY(Math.min(boxStartY, boxStopY)), highY = graphToPlotY(Math.max(boxStartY, boxStopY));
        boolean inBox = false;
        for (int i = 0; i < selectedSequences.length; i++) {
            inBox = (i + 1 >= lowX && i <= highX) || (i + 1 >= lowY && i <= highY);
            selectedSequences[i] = inBox ? !selectedSequences[i] : selectedSequences[i] && addToSelection;
        }
    }

    /**
	 * all views have y increasing bottom up
	 * but this view is calculate from matrix
	 * so, its calculated from up down
	 */
    @Override
    protected float graphToPlotY(int y) {
        return yMin + diffY * (y - yTopBorder) / ySpan;
    }

    @Override
    protected int plotToGraphY(float y) {
        if (diffY == 0) return ySpan / 2;
        return Math.round((float) ySpan * (y - yMin) / diffY) + yTopBorder;
    }

    protected void calculateBounds() {
        xMin = 0;
        xMax = alignmentSize;
        yMin = 0;
        yMax = alignmentSize;
        xAbove0 = true;
        xBelow0 = false;
        yAbove0 = true;
        yBelow0 = false;
        xLeftBorder = 3;
        xRightBorder = 3;
        yTopBorder = 3;
        yBottomBorder = 3;
        diffX = xMax - xMin;
        diffY = yMax - yMin;
    }

    public void saveToDataFile(String filename) {
    }

    public void paint(Graphics g) {
        calculateSpan();
        if (this.hasFocus()) ((java.awt.Graphics2D) g).setBackground(Color.WHITE);
        g.clearRect(0, 0, getWidth(), getHeight());
        if (dotplot != null) {
            if (image == null) image = createImage(dotplot);
            g.drawImage(image, xLeftBorder, yTopBorder, xSpan, ySpan, this);
        }
        if (isSelecting) paintSelectionBox(g);
        g.dispose();
    }

    public void paintPDF(PdfContentByte cb) {
    }
}
