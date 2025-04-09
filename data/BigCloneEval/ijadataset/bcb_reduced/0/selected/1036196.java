package sears.search.data;

/**
 * If elements added is not bigger or equals than the last one it do not correspond and it not added...
 *
 */
public class ListOfRow {

    private static final int INITIAL_CAPACITY = 10;

    private int size;

    private int[] list;

    /**
	 * Creates a new list with initial capacity set to default: 10
	 */
    public ListOfRow() {
        this(INITIAL_CAPACITY);
    }

    /**
	 * Creates a new empty list with capacity set to <tt>initialCapacity</tt>
	 * @param inititialCapacity	the initial capacity of the list
	 */
    public ListOfRow(int inititialCapacity) {
        if (inititialCapacity <= 0) {
            inititialCapacity = INITIAL_CAPACITY;
        }
        list = new int[inititialCapacity];
        size = 0;
    }

    /**
	 * Add a new item in the list
	 * Item must be positive and no have entry in the list
	 * @param indexOfOccurrence 	a positive <tt>int</tt>
	 * @return						true if <tt>indexOfOccurencce</tt> is added in the list, false if not
	 * 
	 * @throws 						IllegalArgumentException if <tt>indexOfOccurencce</tt> is negative
	 */
    public boolean add(int indexOfOccurrence) {
        if (indexOfOccurrence < 0) {
            throw new IllegalArgumentException("negative value is not permitted");
        }
        boolean isAdding = false;
        if (ensureUnicity(indexOfOccurrence) > -1) {
            ensureCapacity(size + 1);
            list[size++] = indexOfOccurrence;
            isAdding = true;
        }
        return isAdding;
    }

    /**
	 * Gets the row at index given in parameter
	 * @param index the index in the list
	 * @return		the row at <code>index</code>, -1 if the list is empty
	 * @throws 		IndexOutOfBoundsException if index is bigger than the list's capacity
	 */
    public int getRow(int index) {
        if (index >= size) {
            throw new IndexOutOfBoundsException("index is out of list's bounds");
        }
        if (index < 0) {
            index = 0;
        }
        int row = -1;
        if (!isEmpty()) {
            row = list[index];
        }
        return row;
    }

    /**
	 * Dichotomic search, gets the index of the row contained in the list
	 * @param row 	the row to search
	 * @return		the index of the <code>row</code> or -1 if the row is not in the list
	 */
    public int getIndexOfRow(int row) {
        if (row < 0) {
            throw new IllegalArgumentException("negative value is not a valid value for a row");
        }
        int middle = 0;
        boolean found = false;
        if (list != null && !isEmpty()) {
            int startAt = 0;
            int stopAt = size - 1;
            int result = -1;
            while (!found && startAt <= stopAt) {
                middle = (startAt + stopAt) / 2;
                result = list[middle];
                if (result == row) {
                    found = true;
                } else {
                    if (result > row) {
                        stopAt = middle - 1;
                    } else if (result < row) {
                        startAt = middle + 1;
                    }
                }
            }
        }
        if (!found) {
            middle = -1;
        }
        return middle;
    }

    /**
	 * Empty the list
	 */
    public void clear() {
        for (int i = 0; i < list.length; i++) {
            list[i] = -1;
        }
        size = 0;
    }

    /**
	 * Ensures the capacity of the list, extends it if needed
	 * @param minCapacity the capacity needed
	 */
    private void ensureCapacity(int minCapacity) {
        int oldCapacity = list.length;
        if (minCapacity > oldCapacity) {
            int[] oldData = list;
            int newCapacity = (oldCapacity * 3) / 2 + 1;
            if (newCapacity < minCapacity) newCapacity = minCapacity;
            list = new int[newCapacity];
            System.arraycopy(oldData, 0, list, 0, size);
        }
    }

    /**
	 * Ensures the unicity of element in the list.
	 * <br> the next added index must be bigger than the last one (and so it's not the same)
	 * @param index 	the index to add
	 * @return			the <code>index</code> given in parameters or -1 if it is not a valid index
	 */
    private int ensureUnicity(int index) {
        if (!isEmpty()) {
            if (index <= list[size - 1]) {
                index = -1;
            }
        }
        return index;
    }

    /**
	 * Tests if the list is empty or not
	 * @return	true if the list is empty, false if not
	 */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
	 * Gets the size of the list
	 * @return the size of the list
	 */
    public int size() {
        return size;
    }

    /**
	 * Returns a new <code>LoopIterator</code> object over the list
	 * @return an instance of <code>LoopIterator</code> for this list
	 * @see LoopIterator
	 */
    public LoopIterator iterator() {
        return new LoopIterator(this);
    }
}
