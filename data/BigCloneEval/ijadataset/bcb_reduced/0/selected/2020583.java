package org.eml.MMAX2.annotation.query;

import java.util.ArrayList;
import org.eml.MMAX2.api.QueryResultListAPI;

public class MMAX2QueryResultList extends ArrayList implements QueryResultListAPI {

    private int indexToUse = 0;

    private boolean indexSetByUser = false;

    private ArrayList attributeNamesToDisplay = null;

    private String command = "";

    /** Creates a new instance of MMAX2QueryResultList of width 1 */
    public MMAX2QueryResultList(ArrayList elements) {
        super();
        int len = elements.size();
        for (int z = 0; z < len; z++) {
            if (elements.get(z) instanceof MMAX2QueryResultTupleElement) {
                add(new MMAX2QueryResultTuple((MMAX2QueryResultTupleElement) elements.get(z)));
            } else {
                add(new MMAX2QueryResultTuple(new MMAX2QueryResultTupleElement(elements.get(z))));
            }
        }
    }

    /** Creates a new empty instance of MMAX2QueryResultList */
    public MMAX2QueryResultList() {
        super();
    }

    /** This constructor returns a clone of MMAX2QueryResultList toClone. */
    public MMAX2QueryResultList(MMAX2QueryResultList toClone) {
        super();
        int len = toClone.size();
        for (int z = 0; z < len; z++) {
            add(new MMAX2QueryResultTuple((MMAX2QueryResultTuple) toClone.get(z)));
        }
    }

    /** This constructor returns a MMAX2QueryResultList with 1-element tuples copied from position index from oldList*/
    public MMAX2QueryResultList(MMAX2QueryResultList oldList, int index) {
        super();
        int len = oldList.size();
        MMAX2QueryResultTupleElement elem = null;
        for (int z = 0; z < len; z++) {
            elem = ((MMAX2QueryResultTuple) oldList.get(z)).getValueAt(index);
            add(new MMAX2QueryResultTuple(elem));
        }
    }

    public final int getResultSize() {
        return size();
    }

    public final int getElementIndexBeforeDiscoursePosition(int discPos) {
        int low = 0;
        int hi = size() - 1;
        int midpt = 0;
        int temp = 0;
        while (low <= hi) {
            midpt = (low + hi) / 2;
            if (isIndexSetByUser()) {
                temp = getElementAtIndexFromColumnToUse(midpt).getLeftmostDiscoursePosition();
            } else {
                temp = getTupleAtIndex(midpt).getLeftmostDiscoursePosition();
            }
            if (discPos == temp) {
                if (isIndexSetByUser()) {
                    int tempMidPt = midpt;
                    while (tempMidPt > 0 && getElementAtIndexFromColumnToUse(tempMidPt).getLeftmostDiscoursePosition() == temp) {
                        tempMidPt--;
                    }
                    midpt = tempMidPt;
                } else {
                    int tempMidPt = midpt;
                    while (tempMidPt > 0 && getTupleAtIndex(tempMidPt).getLeftmostDiscoursePosition() == temp) {
                        tempMidPt--;
                    }
                    midpt = tempMidPt;
                }
                return midpt;
            } else if (discPos < temp) {
                hi = midpt - 1;
            } else {
                low = midpt + 1;
            }
        }
        if (isIndexSetByUser()) {
            int tempMidPt = midpt;
            while (tempMidPt > 0 && getElementAtIndexFromColumnToUse(tempMidPt).getLeftmostDiscoursePosition() == temp) {
                tempMidPt--;
            }
            midpt = tempMidPt;
        } else {
            int tempMidPt = midpt;
            while (tempMidPt > 0 && getTupleAtIndex(tempMidPt).getLeftmostDiscoursePosition() == temp) {
                tempMidPt--;
            }
            midpt = tempMidPt;
        }
        return midpt;
    }

    public final void setCommand(String _command) {
        System.err.println("Setting command to " + _command);
        command = _command;
    }

    public final String getCommand() {
        return command;
    }

    public final void setAttributeNamesToDisplay(ArrayList list) {
        attributeNamesToDisplay = list;
    }

    public final ArrayList getAttributeNamesToDisplay() {
        return attributeNamesToDisplay;
    }

    /** This method returns true if this ResultList  was accessed via a variable which had a column specifier, false otherwise. */
    public final boolean isIndexSetByUser() {
        return indexSetByUser;
    }

    public final void setIndexSetByUser() {
        indexSetByUser = true;
    }

    public final int getWidth() {
        return getMaximumWidth();
    }

    public final int getMaximumWidth() {
        int result = -1;
        for (int z = 0; z < size(); z++) {
            if (((MMAX2QueryResultTuple) this.get(z)).getWidth() > result) {
                result = ((MMAX2QueryResultTuple) this.get(z)).getWidth();
            }
        }
        return result;
    }

    public final int getIndexToUse() {
        return indexToUse;
    }

    public final void setIndexToUse(int index) {
        if (indexToUse < getWidth()) {
            indexToUse = index;
            indexSetByUser = true;
        } else {
            System.err.println("Error: Cannot set indexToUse to " + index + "! Defaulting to 0");
            toDefaultIndexToUse();
        }
    }

    public final void toDefaultIndexToUse() {
        indexToUse = 0;
        indexSetByUser = false;
    }

    /** This method adds a new copy of the the supplied tuple to this. It is used for creating the results
         of 'filter' queries, i.e. in which only the first tuple in a match is retained in the result. 
         Therefore, discontinuity is inherited to the copy. Also, the outer discourse positions are
         simply copied from input tuple. */
    public final void addSingleTuple(MMAX2QueryResultTuple tuple1) {
        MMAX2QueryResultTuple tuple = new MMAX2QueryResultTuple();
        for (int a = 0; a < tuple1.getWidth(); a++) {
            tuple.add(tuple1.getValueAt(a));
        }
        if (tuple1.isDiscontinuous()) {
            tuple.setDiscontinuous();
        }
        tuple.setOuterDiscoursePositions(tuple1.getLeftmostDiscoursePosition(), tuple1.getRightmostDiscoursePosition());
        add(tuple);
    }

    /** This method merges two result tuples into a new one, and adds the new tuple to this. */
    public final void mergeAndAdd(MMAX2QueryResultTuple tuple1, MMAX2QueryResultTuple tuple2, int mergeMode) {
        MMAX2QueryResultTuple tuple = new MMAX2QueryResultTuple();
        for (int a = 0; a < tuple1.getWidth(); a++) {
            tuple.add(tuple1.getValueAt(a));
        }
        for (int a = 0; a < tuple2.getWidth(); a++) {
            tuple.add(tuple2.getValueAt(a));
        }
        int a_start = tuple1.getLeftmostDiscoursePosition();
        int a_end = tuple1.getRightmostDiscoursePosition();
        int b_start = tuple2.getLeftmostDiscoursePosition();
        int b_end = tuple2.getRightmostDiscoursePosition();
        if (mergeMode == Constants.AStart_AEnd) {
            tuple.setOuterDiscoursePositions(a_start, a_end);
            if (tuple1.isDiscontinuous()) {
                tuple.setDiscontinuous();
            }
        } else if (mergeMode == Constants.AStart_BEnd) {
            tuple.setOuterDiscoursePositions(a_start, b_end);
            if (tuple1.isDiscontinuous() || tuple2.isDiscontinuous()) {
                tuple.setDiscontinuous();
            }
        } else if (mergeMode == Constants.BStart_AEnd) {
            tuple.setOuterDiscoursePositions(b_start, a_end);
        } else {
            System.err.println("Error: Invalid merge mode!");
        }
        add(tuple);
    }

    /** This method returns the MMAX2QueryResultTuple at list position index. */
    public final MMAX2QueryResultTuple getTupleAtIndex(int index) {
        return ((MMAX2QueryResultTuple) get(index));
    }

    /** This method returns from the MMAX2QueryResultTuple at list position index the Element in 
        the column this.indexToUse. */
    public final MMAX2QueryResultTupleElement getElementAtIndexFromColumnToUse(int index) {
        return ((MMAX2QueryResultTuple) get(index)).getValueAt(indexToUse);
    }

    /** This method returns from the MMAX2QueryResultTuple at list position index the Markable in 
        the column column. */
    public final MMAX2QueryResultTupleElement getElementAtIndexFromColumn(int index, int column) {
        return ((MMAX2QueryResultTuple) get(index)).getValueAt(column);
    }

    /** This method returns the numerical index of the column in which markables from MarkableLevel name are stored.
        It returns -1 if either the name does not exist or if it is ambiguous, i.e. not unique within this list. */
    public final int getColumnIndexByColumnName(String name) {
        int index = -1;
        boolean found = false;
        MMAX2QueryResultTuple firstTuple = null;
        if (size() > 0) {
            firstTuple = (MMAX2QueryResultTuple) get(0);
            int width = getWidth();
            for (int z = 0; z < width; z++) {
                if (firstTuple.getMarkableLevelNameAtColumnIndex(z).equalsIgnoreCase(name)) {
                    if (found == true) {
                        System.out.println("Column identifier '" + name + "' is ambiguous!");
                        index = -1;
                        break;
                    } else {
                        found = true;
                        index = z;
                    }
                }
            }
        }
        return index;
    }

    public final void dump() {
        for (int o = 0; o < size(); o++) {
            System.out.println(((MMAX2QueryResultTuple) get(o)).toString());
        }
    }
}
