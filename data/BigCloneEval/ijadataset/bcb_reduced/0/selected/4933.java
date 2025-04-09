package net.sf.accolorhelper.ui;

import static net.sf.accolorhelper.ui.CellConstants.BORDER_WIDTH;
import static net.sf.accolorhelper.ui.CellConstants.CELL_SPACING;
import static net.sf.accolorhelper.ui.CellConstants.OUTER_CELL_OFFSET;
import static net.sf.accolorhelper.ui.CellConstants.OUTER_CELL_SIZE;
import static net.sf.accolorhelper.ui.CellConstants.SELECTION_CELL_SIZE;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;

public class ValueTableComponent extends JComponent {

    public ValueTableComponent(int[][] pValues, ValueValidator pValidator, boolean[][] pSelection) {
        super();
        aValues = pValues;
        aValidator = pValidator;
        aSelection = pSelection;
        addMouseListener(new CellMouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent pEvent) {
                if (isInCell(pEvent.getX(), pEvent.getY())) {
                    int lHorizontalCellIndex, lVerticalCellIndex;
                    lHorizontalCellIndex = getCellIndex(pEvent.getX());
                    lVerticalCellIndex = getCellIndex(pEvent.getY());
                    if (lHorizontalCellIndex < aValues.length && lVerticalCellIndex < aValues.length) {
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
        return new Dimension(CELL_SPACING * aValues.length + BORDER_WIDTH, CELL_SPACING * aValues.length + BORDER_WIDTH);
    }

    @Override
    public Dimension getPreferredSize() {
        return getMinimumSize();
    }

    @Override
    public void paint(Graphics pGraphics) {
        Color lForegroundColor;
        Font lInvalidFont, lValidFont;
        int lOuterValueIndex, lInnerValueIndex;
        lForegroundColor = pGraphics.getColor();
        lInvalidFont = pGraphics.getFont();
        lValidFont = lInvalidFont.deriveFont(Font.BOLD, (int) (lInvalidFont.getSize() * 1.3));
        for (lOuterValueIndex = 0; lOuterValueIndex < aValues.length; lOuterValueIndex += 1) {
            for (lInnerValueIndex = 0; lInnerValueIndex < aValues.length; lInnerValueIndex += 1) {
                String lText;
                FontMetrics lFontMetrics;
                int lTextX, lTextY;
                if (aSelection[lOuterValueIndex][lInnerValueIndex]) {
                    pGraphics.fillRect(BORDER_WIDTH + lOuterValueIndex * CELL_SPACING, BORDER_WIDTH + lInnerValueIndex * CELL_SPACING, SELECTION_CELL_SIZE, SELECTION_CELL_SIZE);
                    pGraphics.setColor(getBackground());
                    pGraphics.fillRect(BORDER_WIDTH + lOuterValueIndex * CELL_SPACING + OUTER_CELL_OFFSET - 1, BORDER_WIDTH + lInnerValueIndex * CELL_SPACING + OUTER_CELL_OFFSET - 1, OUTER_CELL_SIZE + 2, OUTER_CELL_SIZE + 2);
                    pGraphics.setColor(lForegroundColor);
                }
                if (aValidator.isValid(aValues[lOuterValueIndex][lInnerValueIndex])) {
                    pGraphics.setFont(lValidFont);
                } else {
                    pGraphics.setFont(lInvalidFont);
                }
                lText = String.valueOf(aValues[lOuterValueIndex][lInnerValueIndex]);
                lFontMetrics = pGraphics.getFontMetrics();
                lTextX = (OUTER_CELL_SIZE - (int) (lFontMetrics.getStringBounds(lText, pGraphics).getWidth())) / 2;
                lTextY = (OUTER_CELL_SIZE - lFontMetrics.getHeight()) / 2 + lFontMetrics.getAscent();
                pGraphics.drawString(lText, BORDER_WIDTH + lOuterValueIndex * CELL_SPACING + OUTER_CELL_OFFSET + lTextX, BORDER_WIDTH + lInnerValueIndex * CELL_SPACING + OUTER_CELL_OFFSET + lTextY);
            }
        }
    }

    public void setValues(int[][] pBrightnessDifference, boolean[][] pSelection) {
        aValues = pBrightnessDifference;
        aSelection = pSelection;
        invalidate();
        repaint();
    }

    private static final long serialVersionUID = 1L;

    private int[][] aValues;

    private ValueValidator aValidator;

    private boolean[][] aSelection;
}
