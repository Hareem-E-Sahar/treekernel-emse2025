package org.apache.fop.fo.flow;

import org.apache.fop.fo.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.*;
import org.apache.fop.datatypes.*;
import org.apache.fop.apps.FOPException;
import java.util.ArrayList;

public class Table extends FObj {

    public static class Maker extends FObj.Maker {

        public FObj make(FObj parent, PropertyList propertyList, String systemId, int line, int column) throws FOPException {
            return new Table(parent, propertyList, systemId, line, column);
        }
    }

    public static FObj.Maker maker() {
        return new Table.Maker();
    }

    private static final int MINCOLWIDTH = 10000;

    int breakBefore;

    int breakAfter;

    int spaceBefore;

    int spaceAfter;

    LengthRange ipd;

    int height;

    String id;

    TableHeader tableHeader = null;

    TableFooter tableFooter = null;

    boolean omitHeaderAtBreak = false;

    boolean omitFooterAtBreak = false;

    ArrayList columns = new ArrayList();

    int bodyCount = 0;

    private boolean bAutoLayout = false;

    private int contentWidth = 0;

    /** Optimum inline-progression-dimension */
    private int optIPD;

    /** Minimum inline-progression-dimension */
    private int minIPD;

    /** Maximum inline-progression-dimension */
    private int maxIPD;

    AreaContainer areaContainer;

    public Table(FObj parent, PropertyList propertyList, String systemId, int line, int column) {
        super(parent, propertyList, systemId, line, column);
    }

    public String getName() {
        return "fo:table";
    }

    public int layout(Area area) throws FOPException {
        if (this.marker == BREAK_AFTER) {
            return Status.OK;
        }
        if (this.marker == START) {
            AccessibilityProps mAccProps = propMgr.getAccessibilityProps();
            AuralProps mAurProps = propMgr.getAuralProps();
            BorderAndPadding bap = propMgr.getBorderAndPadding();
            BackgroundProps bProps = propMgr.getBackgroundProps();
            MarginProps mProps = propMgr.getMarginProps();
            RelativePositionProps mRelProps = propMgr.getRelativePositionProps();
            this.breakBefore = this.properties.get("break-before").getEnum();
            this.breakAfter = this.properties.get("break-after").getEnum();
            this.spaceBefore = this.properties.get("space-before.optimum").getLength().mvalue();
            this.spaceAfter = this.properties.get("space-after.optimum").getLength().mvalue();
            this.ipd = this.properties.get("inline-progression-dimension").getLengthRange();
            this.height = this.properties.get("height").getLength().mvalue();
            this.bAutoLayout = (this.properties.get("table-layout").getEnum() == TableLayout.AUTO);
            this.id = this.properties.get("id").getString();
            this.omitHeaderAtBreak = this.properties.get("table-omit-header-at-break").getEnum() == TableOmitHeaderAtBreak.TRUE;
            this.omitFooterAtBreak = this.properties.get("table-omit-footer-at-break").getEnum() == TableOmitFooterAtBreak.TRUE;
            if (area instanceof BlockArea) {
                area.end();
            }
            if (this.areaContainer == null) {
                try {
                    area.getIDReferences().createID(id);
                } catch (FOPException e) {
                    if (!e.isLocationSet()) {
                        e.setLocation(systemId, line, column);
                    }
                    throw e;
                }
            }
            this.marker = 0;
            if (breakBefore == BreakBefore.PAGE) {
                return Status.FORCE_PAGE_BREAK;
            }
            if (breakBefore == BreakBefore.ODD_PAGE) {
                return Status.FORCE_PAGE_BREAK_ODD;
            }
            if (breakBefore == BreakBefore.EVEN_PAGE) {
                return Status.FORCE_PAGE_BREAK_EVEN;
            }
        }
        if ((spaceBefore != 0) && (this.marker == 0)) {
            area.addDisplaySpace(spaceBefore);
        }
        if (marker == 0 && areaContainer == null) {
            area.getIDReferences().configureID(id, area);
        }
        int spaceLeft = area.spaceLeft();
        this.areaContainer = new AreaContainer(propMgr.getFontState(area.getFontInfo()), 0, 0, area.getAllocationWidth(), area.spaceLeft(), Position.STATIC);
        areaContainer.foCreator = this;
        areaContainer.setPage(area.getPage());
        areaContainer.setParent(area);
        areaContainer.setBackground(propMgr.getBackgroundProps());
        areaContainer.setBorderAndPadding(propMgr.getBorderAndPadding());
        areaContainer.start();
        areaContainer.setAbsoluteHeight(area.getAbsoluteHeight());
        areaContainer.setIDReferences(area.getIDReferences());
        boolean addedHeader = false;
        boolean addedFooter = false;
        int numChildren = this.children.size();
        if (columns.size() == 0) {
            findColumns(areaContainer);
            if (this.bAutoLayout) {
                log.warn("table-layout=auto is not supported, using fixed!");
            }
            this.contentWidth = calcFixedColumnWidths(areaContainer.getAllocationWidth());
        }
        areaContainer.setAllocationWidth(this.contentWidth);
        layoutColumns(areaContainer);
        for (int i = this.marker; i < numChildren; i++) {
            FONode fo = (FONode) children.get(i);
            if (fo instanceof Marker) {
                ((Marker) fo).layout(area);
                continue;
            }
            if (fo instanceof TableHeader) {
                if (columns.size() == 0) {
                    log.warn("current implementation of tables requires a table-column for each column, indicating column-width");
                    return Status.OK;
                }
                tableHeader = (TableHeader) fo;
                tableHeader.setColumns(columns);
            } else if (fo instanceof TableFooter) {
                if (columns.size() == 0) {
                    log.warn("current implementation of tables requires a table-column for each column, indicating column-width");
                    return Status.OK;
                }
                tableFooter = (TableFooter) fo;
                tableFooter.setColumns(columns);
            } else if (fo instanceof TableBody) {
                if (columns.size() == 0) {
                    log.warn("current implementation of tables requires a table-column for each column, indicating column-width");
                    return Status.OK;
                }
                int status;
                if (tableHeader != null && !addedHeader) {
                    if (Status.isIncomplete((status = tableHeader.layout(areaContainer)))) {
                        tableHeader.resetMarker();
                        return Status.AREA_FULL_NONE;
                    }
                    addedHeader = true;
                    tableHeader.resetMarker();
                    area.setMaxHeight(area.getMaxHeight() - spaceLeft + this.areaContainer.getMaxHeight());
                }
                if (tableFooter != null && !this.omitFooterAtBreak && !addedFooter) {
                    if (Status.isIncomplete((status = tableFooter.layout(areaContainer)))) {
                        return Status.AREA_FULL_NONE;
                    }
                    addedFooter = true;
                    tableFooter.resetMarker();
                }
                ((TableBody) fo).setColumns(columns);
                if (Status.isIncomplete((status = fo.layout(areaContainer)))) {
                    this.marker = i;
                    if (bodyCount == 0 && status == Status.AREA_FULL_NONE) {
                        if (tableHeader != null) tableHeader.removeLayout(areaContainer);
                        if (tableFooter != null) tableFooter.removeLayout(areaContainer);
                        resetMarker();
                    }
                    if (areaContainer.getContentHeight() > 0) {
                        area.addChild(areaContainer);
                        area.increaseHeight(areaContainer.getHeight());
                        if (this.omitHeaderAtBreak) {
                            tableHeader = null;
                        }
                        if (tableFooter != null && !this.omitFooterAtBreak) {
                            ((TableBody) fo).setYPosition(tableFooter.getYPosition());
                            tableFooter.setYPosition(tableFooter.getYPosition() + ((TableBody) fo).getHeight());
                        }
                        setupColumnHeights();
                        status = Status.AREA_FULL_SOME;
                        this.areasGenerated++;
                    }
                    return status;
                } else {
                    bodyCount++;
                }
                area.setMaxHeight(area.getMaxHeight() - spaceLeft + this.areaContainer.getMaxHeight());
                if (tableFooter != null && !this.omitFooterAtBreak) {
                    ((TableBody) fo).setYPosition(tableFooter.getYPosition());
                    tableFooter.setYPosition(tableFooter.getYPosition() + ((TableBody) fo).getHeight());
                }
            }
        }
        this.areasGenerated++;
        if (tableFooter != null && this.omitFooterAtBreak) {
            if (Status.isIncomplete(tableFooter.layout(areaContainer))) {
                log.warn("footer could not fit on page, moving last body row to next page");
                area.addChild(areaContainer);
                area.increaseHeight(areaContainer.getHeight());
                if (this.omitHeaderAtBreak) {
                    tableHeader = null;
                }
                tableFooter.removeLayout(areaContainer);
                tableFooter.resetMarker();
                return Status.AREA_FULL_SOME;
            }
        }
        if (height != 0) areaContainer.setHeight(height);
        setupColumnHeights();
        areaContainer.end();
        area.addChild(areaContainer);
        area.increaseHeight(areaContainer.getHeight());
        if (spaceAfter != 0) {
            area.addDisplaySpace(spaceAfter);
        }
        if (area instanceof BlockArea) {
            area.start();
        }
        if (breakAfter == BreakAfter.PAGE) {
            this.marker = BREAK_AFTER;
            return Status.FORCE_PAGE_BREAK;
        }
        if (breakAfter == BreakAfter.ODD_PAGE) {
            this.marker = BREAK_AFTER;
            return Status.FORCE_PAGE_BREAK_ODD;
        }
        if (breakAfter == BreakAfter.EVEN_PAGE) {
            this.marker = BREAK_AFTER;
            return Status.FORCE_PAGE_BREAK_EVEN;
        }
        return Status.OK;
    }

    protected void setupColumnHeights() {
        for (int i = 0; i < columns.size(); i++) {
            TableColumn c = (TableColumn) columns.get(i);
            if (c != null) {
                c.setHeight(areaContainer.getContentHeight());
            }
        }
    }

    private void findColumns(Area areaContainer) throws FOPException {
        int nextColumnNumber = 1;
        for (int i = 0; i < children.size(); i++) {
            FONode fo = (FONode) children.get(i);
            if (fo instanceof TableColumn) {
                TableColumn c = (TableColumn) fo;
                c.doSetup(areaContainer);
                int numColumnsRepeated = c.getNumColumnsRepeated();
                int currentColumnNumber = c.getColumnNumber();
                if (currentColumnNumber == 0) {
                    currentColumnNumber = nextColumnNumber;
                }
                if (currentColumnNumber + numColumnsRepeated > columns.size()) {
                    columns.ensureCapacity(currentColumnNumber + numColumnsRepeated);
                }
                for (int j = 0; j < numColumnsRepeated; j++) {
                    if (currentColumnNumber <= columns.size()) {
                        if (columns.get(currentColumnNumber - 1) != null) {
                            log.warn("More than one column object assigned " + "to column " + currentColumnNumber);
                        }
                        columns.set(currentColumnNumber - 1, c);
                    } else {
                        columns.add(currentColumnNumber - 1, c);
                    }
                    currentColumnNumber++;
                }
                nextColumnNumber = currentColumnNumber;
            }
        }
    }

    private int calcFixedColumnWidths(int maxAllocationWidth) {
        int nextColumnNumber = 1;
        int iEmptyCols = 0;
        double dTblUnits = 0.0;
        int iFixedWidth = 0;
        double dWidthFactor = 0.0;
        double dUnitLength = 0.0;
        double tuMin = 100000.0;
        for (int i = 0; i < columns.size(); i++) {
            TableColumn c = (TableColumn) columns.get(i);
            if (c == null) {
                log.warn("No table-column specification for column " + nextColumnNumber);
                iEmptyCols++;
            } else {
                Length colLength = c.getColumnWidthAsLength();
                double tu = colLength.getTableUnits();
                if (tu > 0 && tu < tuMin && colLength.mvalue() == 0) {
                    tuMin = tu;
                }
                dTblUnits += tu;
                iFixedWidth += colLength.mvalue();
            }
            nextColumnNumber++;
        }
        setIPD((dTblUnits > 0.0), maxAllocationWidth);
        if (dTblUnits > 0.0) {
            int iProportionalWidth = 0;
            if (this.optIPD > iFixedWidth) {
                iProportionalWidth = this.optIPD - iFixedWidth;
            } else if (this.maxIPD > iFixedWidth) {
                iProportionalWidth = this.maxIPD - iFixedWidth;
            } else {
                iProportionalWidth = maxAllocationWidth - iFixedWidth;
            }
            if (iProportionalWidth > 0) {
                dUnitLength = ((double) iProportionalWidth) / dTblUnits;
            } else {
                log.error("Sum of fixed column widths " + iFixedWidth + " greater than maximum available IPD " + maxAllocationWidth + "; no space for " + dTblUnits + " proportional units.");
                dUnitLength = MINCOLWIDTH / tuMin;
            }
        } else {
            int iTableWidth = iFixedWidth;
            if (this.minIPD > iFixedWidth) {
                iTableWidth = this.minIPD;
                dWidthFactor = (double) this.minIPD / (double) iFixedWidth;
            } else if (this.maxIPD < iFixedWidth) {
                log.warn("Sum of fixed column widths " + iFixedWidth + " greater than maximum specified IPD " + this.maxIPD);
            } else if (this.optIPD != -1 && iFixedWidth != this.optIPD) {
                log.warn("Sum of fixed column widths " + iFixedWidth + " differs from specified optimum IPD " + this.optIPD);
            }
        }
        int offset = 0;
        for (int i = 0; i < columns.size(); i++) {
            TableColumn c = (TableColumn) columns.get(i);
            if (c != null) {
                c.setColumnOffset(offset);
                Length l = c.getColumnWidthAsLength();
                if (dUnitLength > 0) {
                    l.resolveTableUnit(dUnitLength);
                }
                int colWidth = l.mvalue();
                if (colWidth <= 0) {
                    log.warn("Zero-width table column!");
                }
                if (dWidthFactor > 0.0) {
                    colWidth *= dWidthFactor;
                }
                c.setColumnWidth(colWidth);
                offset += colWidth;
            }
        }
        return offset;
    }

    private void layoutColumns(Area tableArea) throws FOPException {
        for (int i = 0; i < columns.size(); i++) {
            TableColumn c = (TableColumn) columns.get(i);
            if (c != null) {
                c.layout(tableArea);
            }
        }
    }

    public int getAreaHeight() {
        return areaContainer.getHeight();
    }

    /**
     * Return the content width of the boxes generated by this table FO.
     */
    public int getContentWidth() {
        if (areaContainer != null) return areaContainer.getContentWidth(); else return 0;
    }

    /**
     * Initialize table inline-progression-properties values
     */
    private void setIPD(boolean bHasProportionalUnits, int maxAllocIPD) {
        boolean bMaxIsSpecified = !this.ipd.getMaximum().getLength().isAuto();
        if (bMaxIsSpecified) {
            this.maxIPD = ipd.getMaximum().getLength().mvalue();
        } else {
            this.maxIPD = maxAllocIPD;
        }
        if (ipd.getOptimum().getLength().isAuto()) {
            this.optIPD = -1;
        } else {
            this.optIPD = ipd.getMaximum().getLength().mvalue();
        }
        if (ipd.getMinimum().getLength().isAuto()) {
            this.minIPD = -1;
        } else {
            this.minIPD = ipd.getMinimum().getLength().mvalue();
        }
        if (bHasProportionalUnits && this.optIPD < 0) {
            if (this.minIPD > 0) {
                if (bMaxIsSpecified) {
                    this.optIPD = (minIPD + maxIPD) / 2;
                } else {
                    this.optIPD = this.minIPD;
                }
            } else if (bMaxIsSpecified) {
                this.optIPD = this.maxIPD;
            } else {
                log.error("At least one of minimum, optimum, or maximum " + "IPD must be specified on table.");
                this.optIPD = this.maxIPD;
            }
        }
    }
}
