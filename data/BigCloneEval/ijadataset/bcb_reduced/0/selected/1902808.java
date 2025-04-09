package org.apache.fop.fo.flow;

import org.apache.fop.fo.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.*;
import org.apache.fop.apps.FOPException;

public class TableCell extends FObj {

    public static class Maker extends FObj.Maker {

        public FObj make(FObj parent, PropertyList propertyList, String systemId, int line, int column) throws FOPException {
            return new TableCell(parent, propertyList, systemId, line, column);
        }
    }

    public static FObj.Maker maker() {
        return new TableCell.Maker();
    }

    String id;

    int numColumnsSpanned;

    int numRowsSpanned;

    int iColNumber = -1;

    /**
     * Offset of content rectangle in inline-progression-direction,
     * relative to table.
     */
    protected int startOffset;

    /**
     * Dimension of allocation rectangle in inline-progression-direction,
     * determined by the width of the column(s) occupied by the cell
     */
    protected int width;

    /**
     * Offset of content rectangle, in block-progression-direction,
     * relative to the row.
     */
    protected int beforeOffset = 0;

    /**
     * Offset of content rectangle, in inline-progression-direction,
     * relative to the column start edge.
     */
    protected int startAdjust = 0;

    /**
     * Adjust to theoretical column width to obtain content width
     * relative to the column start edge.
     */
    protected int widthAdjust = 0;

    protected int borderHeight = 0;

    /**
     * Minimum ontent height of cell.
     */
    protected int minCellHeight = 0;

    protected int height = 0;

    protected int top;

    protected int verticalAlign;

    protected boolean bRelativeAlign = false;

    boolean bSepBorders = true;

    /**
     * Set to true if all content completely laid out.
     */
    boolean bDone = false;

    /**
     * Border separation value in the block-progression dimension.
     * Used in calculating cells height.
     */
    int m_borderSeparation = 0;

    AreaContainer cellArea;

    public TableCell(FObj parent, PropertyList propertyList, String systemId, int line, int column) throws FOPException {
        super(parent, propertyList, systemId, line, column);
        if (!(parent instanceof TableRow)) {
            throw new FOPException("A table cell must be child of fo:table-row," + " not " + parent.getName(), systemId, line, column);
        }
        doSetup();
    }

    public String getName() {
        return "fo:table-cell";
    }

    public void setStartOffset(int offset) {
        startOffset = offset;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getColumnNumber() {
        return iColNumber;
    }

    public int getNumColumnsSpanned() {
        return numColumnsSpanned;
    }

    public int getNumRowsSpanned() {
        return numRowsSpanned;
    }

    public void doSetup() {
        AccessibilityProps mAccProps = propMgr.getAccessibilityProps();
        AuralProps mAurProps = propMgr.getAuralProps();
        BorderAndPadding bap = propMgr.getBorderAndPadding();
        BackgroundProps bProps = propMgr.getBackgroundProps();
        RelativePositionProps mRelProps = propMgr.getRelativePositionProps();
        this.iColNumber = properties.get("column-number").getNumber().intValue();
        if (iColNumber < 0) {
            iColNumber = 0;
        }
        this.numColumnsSpanned = this.properties.get("number-columns-spanned").getNumber().intValue();
        if (numColumnsSpanned < 1) {
            numColumnsSpanned = 1;
        }
        this.numRowsSpanned = this.properties.get("number-rows-spanned").getNumber().intValue();
        if (numRowsSpanned < 1) {
            numRowsSpanned = 1;
        }
        this.id = this.properties.get("id").getString();
        bSepBorders = (this.properties.get("border-collapse").getEnum() == BorderCollapse.SEPARATE);
        calcBorders(propMgr.getBorderAndPadding());
        verticalAlign = this.properties.get("display-align").getEnum();
        if (verticalAlign == DisplayAlign.AUTO) {
            bRelativeAlign = true;
            verticalAlign = this.properties.get("relative-align").getEnum();
        } else bRelativeAlign = false;
        this.minCellHeight = this.properties.get("height").getLength().mvalue();
    }

    public int layout(Area area) throws FOPException {
        int originalAbsoluteHeight = area.getAbsoluteHeight();
        if (this.marker == BREAK_AFTER) {
            return Status.OK;
        }
        if (this.marker == START) {
            try {
                area.getIDReferences().createID(id);
            } catch (FOPException e) {
                if (!e.isLocationSet()) {
                    e.setLocation(systemId, line, column);
                }
                throw e;
            }
            this.marker = 0;
            this.bDone = false;
        }
        if (marker == 0) {
            area.getIDReferences().configureID(id, area);
        }
        int spaceLeft = area.spaceLeft() - m_borderSeparation;
        this.cellArea = new AreaContainer(propMgr.getFontState(area.getFontInfo()), startOffset + startAdjust, beforeOffset, width - widthAdjust, spaceLeft, Position.RELATIVE);
        cellArea.foCreator = this;
        cellArea.setPage(area.getPage());
        cellArea.setParent(area);
        try {
            cellArea.setBorderAndPadding((BorderAndPadding) propMgr.getBorderAndPadding().clone());
        } catch (CloneNotSupportedException e) {
            System.err.println("Can't clone BorderAndPadding: " + e);
            cellArea.setBorderAndPadding(propMgr.getBorderAndPadding());
        }
        cellArea.setBackground(propMgr.getBackgroundProps());
        cellArea.start();
        cellArea.setAbsoluteHeight(area.getAbsoluteHeight());
        cellArea.setIDReferences(area.getIDReferences());
        cellArea.setTableCellXOffset(startOffset + startAdjust);
        int numChildren = this.children.size();
        for (int i = this.marker; bDone == false && i < numChildren; i++) {
            FObj fo = (FObj) children.get(i);
            fo.setIsInTableCell();
            fo.forceWidth(width);
            this.marker = i;
            int status;
            if (Status.isIncomplete((status = fo.layout(cellArea)))) {
                if ((i == 0) && (status == Status.AREA_FULL_NONE)) {
                    return Status.AREA_FULL_NONE;
                } else {
                    area.addChild(cellArea);
                    return Status.AREA_FULL_SOME;
                }
            }
            area.setMaxHeight(area.getMaxHeight() - spaceLeft + this.cellArea.getMaxHeight());
        }
        this.bDone = true;
        cellArea.end();
        area.addChild(cellArea);
        if (minCellHeight > cellArea.getContentHeight()) {
            cellArea.setHeight(minCellHeight);
        }
        height = cellArea.getHeight();
        top = cellArea.getCurrentYPosition();
        return Status.OK;
    }

    /**
     * Return the allocation height of the cell area.
     * Note: called by TableRow.
     * We adjust the actual allocation height of the area by the value
     * of border separation (for separate borders) or border height
     * adjustment for collapse style (because current scheme makes cell
     * overestimate the allocation height).
     */
    public int getHeight() {
        return cellArea.getHeight() + m_borderSeparation - borderHeight;
    }

    /**
     * Set the final size of cell content rectangles to the actual row height
     * and to vertically align the actual content within the cell rectangle.
     * @param h Height of this row in the grid  which is based on
     * the allocation height of all the cells in the row, including any
     * border separation values.
     */
    public void setRowHeight(int h) {
        int delta = h - getHeight();
        if (bRelativeAlign) {
            cellArea.increaseHeight(delta);
        } else if (delta > 0) {
            BorderAndPadding cellBP = cellArea.getBorderAndPadding();
            switch(verticalAlign) {
                case DisplayAlign.CENTER:
                    cellArea.shiftYPosition(delta / 2);
                    cellBP.setPaddingLength(BorderAndPadding.TOP, cellBP.getPaddingTop(false) + delta / 2);
                    cellBP.setPaddingLength(BorderAndPadding.BOTTOM, cellBP.getPaddingBottom(false) + delta - delta / 2);
                    break;
                case DisplayAlign.AFTER:
                    cellBP.setPaddingLength(BorderAndPadding.TOP, cellBP.getPaddingTop(false) + delta);
                    cellArea.shiftYPosition(delta);
                    break;
                case DisplayAlign.BEFORE:
                    cellBP.setPaddingLength(BorderAndPadding.BOTTOM, cellBP.getPaddingBottom(false) + delta);
                default:
                    break;
            }
        }
    }

    /**
     * Calculate cell border and padding, including offset of content
     * rectangle from the theoretical grid position.
     */
    private void calcBorders(BorderAndPadding bp) {
        if (this.bSepBorders) {
            int iSep = properties.get("border-separation.inline-progression-direction").getLength().mvalue();
            int iSpacing = properties.get("border-spacing.inline-progression-direction").getLength().mvalue();
            if (iSpacing > iSep) iSep = iSpacing;
            this.startAdjust = iSep / 2 + bp.getBorderLeftWidth(false) + bp.getPaddingLeft(false);
            this.widthAdjust = startAdjust + iSep - iSep / 2 + bp.getBorderRightWidth(false) + bp.getPaddingRight(false);
            m_borderSeparation = properties.get("border-separation.block-progression-direction").getLength().mvalue();
            int m_borderSpacing = properties.get("border-spacing.block-progression-direction").getLength().mvalue();
            if (m_borderSpacing > m_borderSeparation) m_borderSeparation = m_borderSpacing;
            this.beforeOffset = m_borderSeparation / 2 + bp.getBorderTopWidth(false) + bp.getPaddingTop(false);
        } else {
            int borderStart = bp.getBorderLeftWidth(false);
            int borderEnd = bp.getBorderRightWidth(false);
            int borderBefore = bp.getBorderTopWidth(false);
            int borderAfter = bp.getBorderBottomWidth(false);
            this.startAdjust = borderStart / 2 + bp.getPaddingLeft(false);
            this.widthAdjust = startAdjust + borderEnd / 2 + bp.getPaddingRight(false);
            this.beforeOffset = borderBefore / 2 + bp.getPaddingTop(false);
            this.borderHeight = (borderBefore + borderAfter) / 2;
        }
    }
}
