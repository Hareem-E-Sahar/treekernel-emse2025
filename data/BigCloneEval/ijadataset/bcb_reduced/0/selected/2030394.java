package net.sf.accolorhelper.ui;

import static net.sf.accolorhelper.ui.CellConstants.BORDER_WIDTH;
import static net.sf.accolorhelper.ui.CellConstants.CELL_SPACING;
import static net.sf.accolorhelper.ui.CellConstants.INNER_CELL_OFFSET;
import static net.sf.accolorhelper.ui.CellConstants.INNER_CELL_SIZE;
import static net.sf.accolorhelper.ui.CellConstants.OUTER_CELL_OFFSET;
import static net.sf.accolorhelper.ui.CellConstants.OUTER_CELL_SIZE;
import static net.sf.accolorhelper.ui.CellConstants.SELECTION_CELL_SIZE;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;

public class ColorCombinationsComponent extends JComponent {

    public ColorCombinationsComponent(Color[] pColors, boolean[][] pSelection) {
        super();
        aColors = pColors;
        aSelection = pSelection;
        addMouseListener(new CellMouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent pEvent) {
                if (isInCell(pEvent.getX(), pEvent.getY())) {
                    int lHorizontalCellIndex, lVerticalCellIndex;
                    lHorizontalCellIndex = getCellIndex(pEvent.getX());
                    lVerticalCellIndex = getCellIndex(pEvent.getY());
                    if (lHorizontalCellIndex < aColors.length && lVerticalCellIndex < aColors.length) {
                        aSelection[lHorizontalCellIndex][lVerticalCellIndex] = !aSelection[lHorizontalCellIndex][lVerticalCellIndex];
                        aSelection[lVerticalCellIndex][lHorizontalCellIndex] = aSelection[lHorizontalCellIndex][lVerticalCellIndex];
                        repaint();
                    }
                }
            }
        });
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(CELL_SPACING * aColors.length + BORDER_WIDTH, CELL_SPACING * aColors.length + BORDER_WIDTH);
    }

    @Override
    public Dimension getPreferredSize() {
        return getMinimumSize();
    }

    @Override
    public void paint(Graphics pGraphics) {
        Color lForegroundColor;
        int lX, lY;
        lForegroundColor = pGraphics.getColor();
        for (lX = 0; lX < aColors.length; lX += 1) {
            for (lY = 0; lY < aColors.length; lY += 1) {
                if (aSelection[lX][lY]) {
                    pGraphics.setColor(lForegroundColor);
                    pGraphics.fillRect(BORDER_WIDTH + lX * CELL_SPACING, BORDER_WIDTH + lY * CELL_SPACING, SELECTION_CELL_SIZE, SELECTION_CELL_SIZE);
                    pGraphics.setColor(getBackground());
                    pGraphics.drawRect(BORDER_WIDTH + lX * CELL_SPACING + OUTER_CELL_OFFSET - 1, BORDER_WIDTH + lY * CELL_SPACING + OUTER_CELL_OFFSET - 1, OUTER_CELL_SIZE + 1, OUTER_CELL_SIZE + 1);
                }
                pGraphics.setColor(aColors[lX]);
                pGraphics.fillRect(BORDER_WIDTH + lX * CELL_SPACING + OUTER_CELL_OFFSET, BORDER_WIDTH + lY * CELL_SPACING + OUTER_CELL_OFFSET, OUTER_CELL_SIZE, OUTER_CELL_SIZE);
                pGraphics.setColor(aColors[lY]);
                pGraphics.fillRect(BORDER_WIDTH + lX * CELL_SPACING + INNER_CELL_OFFSET, BORDER_WIDTH + lY * CELL_SPACING + INNER_CELL_OFFSET, INNER_CELL_SIZE, INNER_CELL_SIZE);
            }
        }
    }

    public void setColors(Color[] pColors, boolean[][] pSelection) {
        aColors = pColors;
        aSelection = pSelection;
        invalidate();
        repaint();
    }

    private static final long serialVersionUID = 1L;

    private Color[] aColors;

    private boolean[][] aSelection;
}
