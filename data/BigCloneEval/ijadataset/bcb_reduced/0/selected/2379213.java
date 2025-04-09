package sears.search.data;

/**
 * Manages <tt>ListOfRow</tt> structure with loop iteration
 */
public class LoopIterator {

    private ListOfRow listOfRow;

    private int index;

    /**
	 * Instantiate a new object
	 * @param aListOfRow	the list on which iterate
	 * @throws NullPointerException if <tt>aListOfRow</tt> is null
	 */
    public LoopIterator(ListOfRow aListOfRow) {
        if (aListOfRow == null) {
            throw new NullPointerException("cannot iterate on a null object");
        }
        listOfRow = aListOfRow;
        index = -1;
    }

    /**
	 * <p>
	 * Gets the nearest element before means: 
	 * <br> gets the first smaller element before the element before the one given
	 * on parameters <br>if this element is not in the list, else it is return
	 * </p>
	 * <p>
	 * Method's algorithm uses is the <i>dichotomic</i> search
	 * </p>
	 * @param row	the element 
	 * @return		<tt>row</tt> if it is in the list or the first smaller element before it
	 * @throws 		IllegalArgumentException if <tt>row</tt> is out of the bounds of the elements contained in the list
	 */
    public int getTheNearestElementBefore(int row) {
        if (row < 0) {
            throw new IllegalArgumentException("negative value is not a valid value for a row");
        }
        int rowBefore = -1;
        if (!listOfRow.isEmpty()) {
            int currentIndex = index;
            try {
                int startAt = 0;
                int stopAt = listOfRow.size() - 1;
                int middle = 1;
                boolean rowInTheList = false;
                while (!rowInTheList && startAt <= stopAt) {
                    middle = (startAt + stopAt) / 2;
                    rowBefore = listOfRow.getRow(middle);
                    rowInTheList = (rowBefore == row);
                    if (rowBefore > row) {
                        stopAt = middle - 1;
                    } else if (rowBefore < row) {
                        startAt = middle + 1;
                    }
                }
                index = middle;
                if (!rowInTheList) {
                    if (rowBefore > row) {
                        rowBefore = getPreviousElement();
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                index = currentIndex;
                rowBefore = -1;
            }
        }
        return rowBefore;
    }

    /**
	 * @param row	the row
	 * @return		the nearest element after
	 * @throws 		IllegalArgumentException if <tt>row</tt> is out of the bounds of the elements contained in the list
	 */
    public int getTheNearestElementAfter(int row) {
        if (row < 0) {
            throw new IllegalArgumentException("negative value is not a valid value for a row");
        }
        int rowAfter = -1;
        if (!listOfRow.isEmpty()) {
            int currentIndex = index;
            try {
                int startAt = 0;
                int stopAt = listOfRow.size() - 1;
                int middle = 0;
                boolean rowInTheList = false;
                while (!rowInTheList && startAt <= stopAt) {
                    middle = (startAt + stopAt) / 2;
                    rowAfter = listOfRow.getRow(middle);
                    rowInTheList = (rowAfter == row);
                    if (rowAfter > row) {
                        stopAt = middle - 1;
                    } else if (rowAfter < row) {
                        startAt = middle + 1;
                    }
                }
                index = middle;
                if (!rowInTheList) {
                    if (rowAfter < row) {
                        rowAfter = getNextElement();
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                index = currentIndex;
                rowAfter = -1;
            }
        }
        return rowAfter;
    }

    /**
	 * Returns the next positive element of the list
	 * @return	the next element in the list, if the list is empty -1 is return
	 */
    public int getNextElement() {
        return getNextElement(index);
    }

    /**
	 * returns the next element at index given in parameters
	 */
    private int getNextElement(int startIndex) {
        index = startIndex;
        int row = -1;
        if (!listOfRow.isEmpty()) {
            row = listOfRow.getRow(getNextIndex());
        }
        return row;
    }

    /**
	 * Returns the previous positive element of the list
	 * @return	the previous element in the list, if the list is empty -1 is return
	 */
    public int getPreviousElement() {
        return getPreviousElement(index);
    }

    private int getPreviousElement(int startIndex) {
        index = startIndex;
        int row = -1;
        if (!listOfRow.isEmpty()) {
            row = listOfRow.getRow(getPreviousIndex());
        }
        return row;
    }

    /**
	 * Gets the next index, ensures that the loop iteration is done.
	 * <br> Increments the current <tt>index</tt>
	 * @return the incremented current index
	 */
    private int getNextIndex() {
        if (index + 1 >= listOfRow.size()) {
            index = -1;
        }
        index++;
        return index;
    }

    /**
	 * Gets the previous index, ensures that the loop iteration is done.
	 * <br> Decrements the current <tt>index</tt>
	 * @return the decremented current index
	 */
    private int getPreviousIndex() {
        if (index - 1 < 0) {
            index = listOfRow.size();
        }
        index--;
        return index;
    }

    /**
	 * SAFE METHOD, do not change iteration state
	 * @return the current element
	 */
    public int getCurrentElement() {
        return listOfRow.getRow(index);
    }

    /**
	 * SAFE METHOD, do not change iteration state
	 * @return the first element of the list
	 */
    public int getFirstElement() {
        return listOfRow.getRow(0);
    }

    /**
	 * SAFE METHOD, do not change iteration state
	 * @return the last element of the list
	 */
    public int getLastElement() {
        return listOfRow.getRow(listOfRow.size() - 1);
    }

    /**
	 * SAFE METHOD, do not change iteration state
	 * @return the index of the current element in the list
	 */
    public int getIndexOfTheCurrentElement() {
        return index;
    }
}
