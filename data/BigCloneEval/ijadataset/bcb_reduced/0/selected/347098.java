package edu.whitman.halfway.util;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import org.apache.log4j.Logger;
import cern.colt.list.IntArrayList;

/** Display text (usually a single character) into each cell of a
 * grid.
 */
public class MatrixTextGridPainter implements GridPainter {

    private static Logger log = Logger.getLogger(MatrixTextGridPainter.class);

    int rows, cols;

    IntArrayList rowIdx = new IntArrayList();

    IntArrayList colIdx = new IntArrayList();

    ArrayList text = new ArrayList();

    String fontName = "SansSerif";

    int fontStyle = Font.PLAIN;

    public MatrixTextGridPainter() {
    }

    public void setTextMatrix(String[][] str) {
        rowIdx.clear();
        colIdx.clear();
        text.clear();
        cols = str[0].length;
        rows = str.length;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (str[r][c] != null) {
                    rowIdx.add(r);
                    colIdx.add(c);
                    text.add(str[r][c]);
                }
            }
        }
        if (log.isInfoEnabled()) log.info("setTextMatrix, rows=" + rows + ", cols=" + cols);
    }

    protected Font fontThatFitsAll(Graphics2D context, double maxWidth, double maxHeight) {
        if (log.isInfoEnabled()) log.info("looking for font that fits.");
        if (maxWidth <= 0 || maxHeight <= 0) return null;
        int big = 128;
        int small = 4;
        Font best = null;
        best = testFont(small, context, maxWidth, maxHeight);
        if (best == null) return null;
        Font f = testFont(big, context, maxWidth, maxHeight);
        if (f != null) return f;
        int t = (big + small) / 2;
        while (t != small) {
            f = testFont(t, context, maxWidth, maxHeight);
            if (f != null) {
                small = t;
                best = f;
            } else {
                big = t;
            }
            t = (big + small) / 2;
        }
        return best;
    }

    /** Returns the font if the font of that size works, otherwise null */
    protected Font testFont(int size, Graphics2D context, double maxWidth, double maxHeight) {
        Font f = new Font(fontName, fontStyle, size);
        for (int i = 0; i < rowIdx.size(); i++) {
            FontMetrics fm = context.getFontMetrics(f);
            Rectangle2D bounds = fm.getStringBounds((String) text.get(i), context);
            if (bounds.getWidth() > maxWidth || bounds.getHeight() > maxHeight) {
                return null;
            }
        }
        return f;
    }

    protected void drawMatrix(Graphics2D g, Grid grid) {
        Rectangle paintRec = new Rectangle();
        paintRec.width = (int) grid.side;
        paintRec.height = (int) grid.side;
        Rectangle clipRect = g.getClipBounds();
        int margin = (int) Math.max(4, 0.18 * grid.side);
        Font f = fontThatFitsAll(g, grid.side - margin, grid.side - margin);
        if (f != null) {
            Composite defComp = g.getComposite();
            if (log.isInfoEnabled()) log.info("Font that fits: " + f);
            g.setFont(f);
            FontMetrics fm = g.getFontMetrics(f);
            g.setColor(Color.white);
            g.setXORMode(Color.black);
            for (int i = 0; i < rowIdx.size(); i++) {
                int row = rowIdx.get(i);
                int col = colIdx.get(i);
                String drawText = (String) text.get(i);
                paintRec.x = (int) Math.round(col * grid.side);
                paintRec.y = (int) Math.round(row * grid.side);
                if (clipRect == null || clipRect.intersects(paintRec)) {
                    Rectangle2D textSize = fm.getStringBounds(drawText, g);
                    int x = (paintRec.width - (int) textSize.getWidth()) / 2;
                    int y = (paintRec.height - (int) textSize.getHeight()) / 2 + fm.getAscent();
                    g.drawString(drawText, paintRec.x + x, paintRec.y + y);
                }
            }
            g.setComposite(defComp);
        } else {
            log.info("No font that fits.");
        }
    }

    private AffineTransform origTrans;

    /** Paints the matrix on the given graphics.  The rectangle
     * grid.gridRect should define the region in which the whole matrix
     * is drawn.  If it doesn't all need to be drawn this can be
     * specified using the user clipping bounds (see
     * setClipBounds). */
    public void paintGrid(Graphics2D g, Grid grid) {
        if (text != null) {
            if (rows != grid.rows || cols != grid.cols) {
                throw new IllegalArgumentException("Invalid matrix/grid size, grid = " + grid);
            }
            origTrans = ((Graphics2D) g).getTransform();
            g.translate(grid.gridRect.x, grid.gridRect.y);
            drawMatrix(g, grid);
            g.translate(-grid.gridRect.x, -grid.gridRect.y);
            ((Graphics2D) g).setTransform(origTrans);
        } else {
        }
    }
}
