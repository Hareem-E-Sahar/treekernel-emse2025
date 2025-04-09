package com.hifi.plugin.ui.components.smooth.table.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * RangeListSelectionModel - A list selection model implementation based on a list of ranges of selected image rowids.
 *
 * @author Created by Jasper Potts (01-Aug-2005)
 */
public class RangeListSelectionModel implements ListSelectionModel {

    private Range anchorLeadRange = new Range(-1, -1);

    private Range overallRange = new Range(-1, -1);

    private Range oldAnchorLeadRange = new Range(-1, -1);

    private Range oldOverallRange = new Range(-1, -1);

    /** Ordered list of selected ranges. First item in list is lowest selected range indexes */
    private List<Range> selectionRanges = new ArrayList<Range>();

    private boolean isAdjusting = false;

    private int selectionMode = MULTIPLE_INTERVAL_SELECTION;

    private Set<ListSelectionListener> listSelectionListeners = new CopyOnWriteArraySet<ListSelectionListener>();

    /**
     * Change the selection to be between index0 and index1 inclusive. If this represents a change to the current
     * selection, then notify each ListSelectionListener. Note that index0 doesn't have to be less than or equal to
     * index1.
     *
     * @param index0 one end of the interval.
     * @param index1 other end of the interval
     * @see #addListSelectionListener
     */
    public void setSelectionInterval(int index0, int index1) {
        updateOld();
        selectionRanges.clear();
        anchorLeadRange.set(index0, index1);
        overallRange.set(Math.min(index0, index1), Math.max(index0, index1));
        selectionRanges.add(new Range(overallRange));
        fireValueChanged(oldOverallRange.min, oldOverallRange.max, overallRange.min, overallRange.max, oldAnchorLeadRange.min, oldAnchorLeadRange.max);
    }

    /**
     * Change the selection to be the set union of the current selection and the indices between index0 and index1
     * inclusive.  If this represents a change to the current selection, then notify each ListSelectionListener. Note
     * that index0 doesn't have to be less than or equal to index1.
     *
     * @param index0 one end of the interval.
     * @param index1 other end of the interval
     * @see #addListSelectionListener
     */
    public void addSelectionInterval(int index0, int index1) {
        if (index0 == -1 || index1 == -1) return;
        updateOld();
        if (isSelectionEmpty()) {
            setSelectionInterval(index0, index1);
            return;
        }
        switch(selectionMode) {
            case SINGLE_SELECTION:
            case SINGLE_INTERVAL_SELECTION:
                setSelectionInterval(index0, index1);
                break;
            case MULTIPLE_INTERVAL_SELECTION:
                anchorLeadRange.set(index0, index1);
                int min = Math.min(index0, index1);
                int max = Math.max(index0, index1);
                if (min > overallRange.max) {
                    Range newRange = new Range(min, max);
                    selectionRanges.add(newRange);
                } else if (max < overallRange.min) {
                    Range newRange = new Range(min, max);
                    selectionRanges.add(0, newRange);
                } else {
                    List<Range> overlapedRanges = new ArrayList<Range>();
                    for (int i = 0; i < selectionRanges.size(); i++) {
                        Range selectionRange = selectionRanges.get(i);
                        if (max < (selectionRange.min - 1)) {
                            if (overlapedRanges.isEmpty()) {
                                Range newRange = new Range(min, max);
                                selectionRanges.add(i, newRange);
                            }
                            break;
                        } else if (min <= (selectionRange.max + 1)) {
                            overlapedRanges.add(selectionRange);
                        }
                    }
                    if (overlapedRanges.size() == 1) {
                        Range range = overlapedRanges.get(0);
                        if (min > range.min && max < range.max) {
                            return;
                        } else {
                            range.min = Math.min(min, range.min);
                            range.max = Math.max(max, range.max);
                        }
                    } else if (overlapedRanges.size() >= 2) {
                        Range newRange = new Range(Math.min(min, overlapedRanges.get(0).min), Math.max(max, overlapedRanges.get(overlapedRanges.size() - 1).max));
                        selectionRanges.add(selectionRanges.indexOf(overlapedRanges.get(0)), newRange);
                        selectionRanges.removeAll(overlapedRanges);
                    }
                }
                overallRange.min = Math.min(overallRange.min, min);
                overallRange.max = Math.max(overallRange.max, max);
                fireValueChanged(min, max, oldAnchorLeadRange.min, oldAnchorLeadRange.max);
                break;
        }
    }

    /**
     * Change the selection to be the set difference of the current selection and the indices between index0 and index1
     * inclusive.  If this represents a change to the current selection, then notify each ListSelectionListener.  Note
     * that index0 doesn't have to be less than or equal to index1.
     *
     * @param index0 one end of the interval.
     * @param index1 other end of the interval
     * @see #addListSelectionListener
     */
    public void removeSelectionInterval(int index0, int index1) {
        int min = Math.min(index0, index1);
        int max = Math.max(index0, index1);
        anchorLeadRange.set(index0, index1);
        if (min > overallRange.max || max < overallRange.min) {
            return;
        }
        switch(selectionMode) {
            case SINGLE_SELECTION:
                int singleSelectedItem = overallRange.min;
                if (singleSelectedItem >= min && singleSelectedItem <= max) {
                    selectionRanges.clear();
                    overallRange.set(-1, -1);
                    fireValueChanged(singleSelectedItem, singleSelectedItem, isAdjusting);
                }
                break;
            case SINGLE_INTERVAL_SELECTION:
                Range singleSelection = new Range(overallRange);
                if (max < singleSelection.max && min > singleSelection.min) {
                    fireValueChanged(min, singleSelection.max, isAdjusting);
                } else if (max > singleSelection.max && min < singleSelection.min) {
                    selectionRanges.clear();
                    fireValueChanged(singleSelection.min, singleSelection.max, isAdjusting);
                } else if (max > singleSelection.max) {
                    fireValueChanged(min, singleSelection.max, isAdjusting);
                } else if (min < singleSelection.min) {
                    fireValueChanged(singleSelection.min, max, isAdjusting);
                }
                updateOverallRange();
                break;
            case MULTIPLE_INTERVAL_SELECTION:
                boolean haveMadeChange = false;
                for (Iterator<Range> iter = selectionRanges.iterator(); iter.hasNext(); ) {
                    Range selectionRange = iter.next();
                    if (min == selectionRange.min && max == selectionRange.max) {
                        iter.remove();
                        haveMadeChange = true;
                        break;
                    } else if (min > selectionRange.min && max < selectionRange.max) {
                        Range newRange = new Range(max + 1, selectionRange.max);
                        selectionRange.max = min - 1;
                        selectionRanges.add(selectionRanges.indexOf(selectionRange) + 1, newRange);
                        haveMadeChange = true;
                        break;
                    } else if (min <= selectionRange.min && max >= selectionRange.max) {
                        iter.remove();
                        haveMadeChange = true;
                    } else if (min <= selectionRange.max && min > selectionRange.min) {
                        selectionRange.max = min - 1;
                        haveMadeChange = true;
                    } else if (max <= selectionRange.max && max >= selectionRange.min) {
                        selectionRange.min = max + 1;
                        haveMadeChange = true;
                        break;
                    }
                }
                if (haveMadeChange) {
                    updateOverallRange();
                    fireValueChanged(min, max, isAdjusting);
                }
                break;
        }
    }

    /** Returns the first selected index or -1 if the selection is empty. */
    public int getMinSelectionIndex() {
        return isSelectionEmpty() ? -1 : overallRange.min;
    }

    /** Returns the last selected index or -1 if the selection is empty. */
    public int getMaxSelectionIndex() {
        return isSelectionEmpty() ? -1 : overallRange.max;
    }

    /** Returns true if the specified index is selected. */
    public boolean isSelectedIndex(int index) {
        if (index < overallRange.min || index > overallRange.max) return false;
        int first = 0;
        int last = selectionRanges.size() - 1;
        while (last >= first) {
            int m = (first + last) / 2;
            Range range = selectionRanges.get(m);
            if (index >= range.min && index <= range.max) {
                return true;
            }
            if (index > range.max) {
                first = m + 1;
            } else {
                last = m - 1;
            }
        }
        return false;
    }

    /**
     * Return the first index argument from the most recent call to setSelectionInterval(), addSelectionInterval() or
     * removeSelectionInterval(). The most recent index0 is considered the "anchor" and the most recent index1 is
     * considered the "lead".  Some interfaces display these indices specially, e.g. Windows95 displays the lead index
     * with a dotted yellow outline.
     *
     * @see #getLeadSelectionIndex
     * @see #setSelectionInterval
     * @see #addSelectionInterval
     */
    public int getAnchorSelectionIndex() {
        return anchorLeadRange.min;
    }

    /**
     * Set the anchor selection index.
     *
     * @see #getAnchorSelectionIndex
     */
    public void setAnchorSelectionIndex(int index) {
        int oldIndex = anchorLeadRange.min;
        anchorLeadRange.min = index;
        fireValueChanged(oldIndex, anchorLeadRange.min);
    }

    /**
     * Return the second index argument from the most recent call to setSelectionInterval(), addSelectionInterval() or
     * removeSelectionInterval().
     *
     * @see #getAnchorSelectionIndex
     * @see #setSelectionInterval
     * @see #addSelectionInterval
     */
    public int getLeadSelectionIndex() {
        return anchorLeadRange.max;
    }

    /**
     * Set the lead selection index.
     *
     * @see #getLeadSelectionIndex
     */
    public void setLeadSelectionIndex(int index) {
        int oldIndex = anchorLeadRange.max;
        anchorLeadRange.max = index;
        fireValueChanged(oldIndex, anchorLeadRange.max);
    }

    /**
     * Change the selection to the empty set.  If this represents a change to the current selection then notify each
     * ListSelectionListener.
     *
     * @see #addListSelectionListener
     */
    public void clearSelection() {
        Range oldRange = new Range(overallRange);
        overallRange.set(-1, -1);
        selectionRanges.clear();
        fireValueChanged(oldRange, isAdjusting);
    }

    /** Returns true if no indices are selected. */
    public boolean isSelectionEmpty() {
        return selectionRanges.isEmpty();
    }

    /**
     * Insert length indices beginning before/after index.  This is typically called to sync the selection model with a
     * corresponding change in the data model.
     */
    public void insertIndexInterval(int index, int length, boolean before) {
        if (anchorLeadRange.max > index || (before && anchorLeadRange.max == index)) {
            anchorLeadRange.max = anchorLeadRange.max + length;
        }
        if (anchorLeadRange.min > index || (before && anchorLeadRange.min == index)) {
            anchorLeadRange.min = anchorLeadRange.min + length;
        }
        if (index > overallRange.max) {
            return;
        }
        for (Range range : selectionRanges) {
            if (range.min > index) range.min += length;
            if (range.max >= index) range.max += length;
        }
        updateOverallRange();
        fireValueChanged(overallRange, isAdjusting);
    }

    /**
     * Remove the indices in the interval index0,index1 (inclusive) from the selection model.  This is typically called
     * to sync the selection model width a corresponding change in the data model.
     */
    public void removeIndexInterval(int index0, int index1) {
        int min = Math.min(index0, index1);
        int max = Math.max(index0, index1);
        int length = (max - min) + 1;
        if (anchorLeadRange.max == 0 && min == 0) {
        } else if (anchorLeadRange.max > max) {
            anchorLeadRange.max = anchorLeadRange.max - length;
        } else if (anchorLeadRange.max >= min) {
            anchorLeadRange.max = min - 1;
        }
        if (anchorLeadRange.min == 0 && min == 0) {
        } else if (anchorLeadRange.min > max) {
            anchorLeadRange.min = anchorLeadRange.min - length;
        } else if (anchorLeadRange.min >= min) {
            anchorLeadRange.min = min - 1;
        }
        if (min > overallRange.max) {
            return;
        } else if (max < overallRange.min) {
            for (Range range : selectionRanges) {
                range.min -= length;
                range.max -= length;
            }
            updateOverallRange();
            return;
        } else if (overallRange.min >= min && overallRange.max <= max) {
            selectionRanges.clear();
            updateOverallRange();
        } else {
            for (Iterator<Range> iter = selectionRanges.iterator(); iter.hasNext(); ) {
                Range range = iter.next();
                if (range.min >= min && range.max <= max) {
                    iter.remove();
                } else if (min > range.min && max < range.max) {
                    range.max -= length;
                } else if (range.max < min) {
                } else if (range.min > max) {
                    range.min -= length;
                    range.max -= length;
                } else if (range.min >= min) {
                    range.min = min;
                    range.max -= length;
                } else if (range.max <= max) {
                    range.max = min - 1;
                }
            }
            updateOverallRange();
        }
        fireValueChanged(overallRange, isAdjusting);
    }

    /**
     * This property is true if upcoming changes to the value of the model should be considered a single event. For
     * example if the model is being updated in response to a user drag, the value of the valueIsAdjusting property will
     * be set to true when the drag is initiated and be set to false when the drag is finished.  This property allows
     * listeners to to update only when a change has been finalized, rather than always handling all of the intermediate
     * values.
     *
     * @param valueIsAdjusting The new value of the property.
     * @see #getValueIsAdjusting
     */
    public void setValueIsAdjusting(boolean valueIsAdjusting) {
        isAdjusting = valueIsAdjusting;
    }

    /**
     * Returns true if the value is undergoing a series of changes.
     *
     * @return true if the value is currently adjusting
     * @see #setValueIsAdjusting
     */
    public boolean getValueIsAdjusting() {
        return isAdjusting;
    }

    /**
     * Set the selection mode. The following selectionMode values are allowed: <ul> <li> <code>SINGLE_SELECTION</code>
     * Only one list index can be selected at a time.  In this mode the setSelectionInterval and addSelectionInterval
     * methods are equivalent, and only the second index argument (the "lead index") is used. <li>
     * <code>SINGLE_INTERVAL_SELECTION</code> One contiguous index interval can be selected at a time. In this mode
     * setSelectionInterval and addSelectionInterval are equivalent. <li> <code>MULTIPLE_INTERVAL_SELECTION</code> In
     * this mode, there's no restriction on what can be selected. </ul>
     *
     * @see #getSelectionMode
     */
    public void setSelectionMode(int selectionMode) {
        switch(selectionMode) {
            case SINGLE_SELECTION:
            case SINGLE_INTERVAL_SELECTION:
            case MULTIPLE_INTERVAL_SELECTION:
                this.selectionMode = selectionMode;
                break;
            default:
                throw new IllegalArgumentException("invalid selectionMode");
        }
    }

    /**
     * Returns the current selection mode.
     *
     * @return The value of the selectionMode property.
     * @see #setSelectionMode
     */
    public int getSelectionMode() {
        return selectionMode;
    }

    /**
     * Add a listener to the list that's notified each time a change to the selection occurs.
     *
     * @param x the ListSelectionListener
     * @see #removeListSelectionListener
     * @see #setSelectionInterval
     * @see #addSelectionInterval
     * @see #removeSelectionInterval
     * @see #clearSelection
     * @see #insertIndexInterval
     * @see #removeIndexInterval
     */
    public void addListSelectionListener(ListSelectionListener x) {
        listSelectionListeners.add(x);
    }

    /**
     * Remove a listener from the list that's notified each time a change to the selection occurs.
     *
     * @param x the ListSelectionListener
     * @see #addListSelectionListener
     */
    public void removeListSelectionListener(ListSelectionListener x) {
        listSelectionListeners.remove(x);
    }

    /**
     * Fire event with union range of m_oOldOverallRange,m_oOldAnchorLeadRange and m_oOverallRange
     *  
     * @param values array of indexes that habe changed
     */
    protected void fireValueChanged(int... values) {
        int min = Integer.MAX_VALUE;
        int max = 0;
        for (int i = 0; i < values.length; i++) {
            int value = values[i];
            if (value < min) min = value;
            if (value > max) max = value;
        }
        fireValueChanged(min, max, isAdjusting);
    }

    /**
     * @param range       The range of index for which the selection has changed
     * @param isAdjusting true if this is the final change in a series of adjustments
     * @see javax.swing.event.EventListenerList
     */
    protected void fireValueChanged(Range range, boolean isAdjusting) {
        fireValueChanged(range.min, range.max, isAdjusting);
    }

    /**
     * @param firstIndex  the first index in the interval
     * @param lastIndex   the last index in the interval
     * @param isAdjusting true if this is the final change in a series of adjustments
     * @see javax.swing.event.EventListenerList
     */
    protected void fireValueChanged(int firstIndex, int lastIndex, boolean isAdjusting) {
        ListSelectionEvent e = null;
        for (ListSelectionListener listener : listSelectionListeners) {
            if (e == null) {
                e = new ListSelectionEvent(this, firstIndex, lastIndex, isAdjusting);
            }
            listener.valueChanged(e);
        }
    }

    /**
     * Generate sql where statements for all of the ranges and append to buffer buf. Example: "(rowid>=2 and rowid<=4)
     * or (rowid>=2 and rowid<=4)".
     *
     * @param rowNumColumnName The name of the column that contains the row number eg.""+ROWID_COL_NAME+""
     * @param buf              The buffer to append the where expressions to
     */
    public void generateSqlWhereClause(String rowNumColumnName, StringBuilder buf) {
        for (Iterator<Range> iter = selectionRanges.iterator(); iter.hasNext(); ) {
            Range range = iter.next();
            buf.append(" (").append(rowNumColumnName).append(">=").append(range.min).append(" and ").append(rowNumColumnName).append("<=").append(range.max).append(") ");
            if (iter.hasNext()) buf.append("or");
        }
    }

    /**
     * Get the number of indexes that are selected
     *
     * @return Number of selected indexes
     */
    public int getSelectedIndexCount() {
        int count = 0;
        for (Range range : selectionRanges) {
            count += (range.max - range.min) + 1;
        }
        return count;
    }

    /**
     * Get the number of selected index within the given inclusive range
     *
     * @param start The start of range (is included in range)
     * @param end   The end of range (is included in range)
     * @return The total selected indexs in range
     */
    public int getSelectedCountInRange(int start, int end) {
        if (end < overallRange.min || start > overallRange.max) return 0;
        int selectedCount = 0;
        for (Range range : selectionRanges) {
            int overlapMin = Math.max(start, range.min);
            int overlapMax = Math.min(end, range.max);
            if (overlapMax > overlapMin) selectedCount += (overlapMax - overlapMin) + 1;
        }
        return selectedCount;
    }

    private void updateOld() {
        oldAnchorLeadRange.min = anchorLeadRange.min;
        oldAnchorLeadRange.max = anchorLeadRange.max;
        oldOverallRange.min = overallRange.min;
        oldOverallRange.max = overallRange.max;
    }

    private void updateOverallRange() {
        if (selectionRanges.isEmpty()) {
            overallRange.set(-1, -1);
        } else {
            overallRange.min = selectionRanges.get(0).min;
            overallRange.max = selectionRanges.get(selectionRanges.size() - 1).max;
        }
    }

    protected int getNumberOfRanges() {
        return selectionRanges.size();
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("RangeListSelectionModel[");
        buf.append("A=");
        buf.append(getAnchorSelectionIndex());
        buf.append(", ");
        buf.append("L=");
        buf.append(getLeadSelectionIndex());
        buf.append(", ");
        buf.append("O=");
        buf.append(overallRange);
        buf.append(", [");
        for (Iterator<Range> iter = selectionRanges.iterator(); iter.hasNext(); ) {
            Range range = iter.next();
            buf.append(range);
            if (iter.hasNext()) buf.append(", ");
        }
        buf.append("]]");
        return buf.toString();
    }

    /** Struct data class that represents a range of selected image rowids. It is inclusive from "min" to "max" */
    private static class Range {

        public int min;

        public int max;

        public Range() {
        }

        public Range(int min, int max) {
            set(min, max);
        }

        public Range(Range range) {
            min = range.min;
            max = range.max;
        }

        public void set(int min, int max) {
            this.min = min;
            this.max = max;
        }

        public void setAny(int a, int b) {
            min = Math.min(a, b);
            max = Math.max(a, b);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final Range range = (Range) o;
            if (max != range.max) return false;
            if (min != range.min) return false;
            return true;
        }

        @Override
        public int hashCode() {
            int result;
            result = min;
            result = 29 * result + max;
            return result;
        }

        @Override
        public String toString() {
            return "(" + min + "->" + max + ")";
        }
    }
}
