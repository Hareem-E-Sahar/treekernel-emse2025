package pl.omtt.eclipse.ui.partitioner;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractTreeNode<T extends AbstractTreeNode<?>> {

    protected int DEBUG = 0;

    protected int fOffset = 0;

    protected int fLength = 0;

    protected List<T> fChildren;

    AbstractTreeNode() {
        fChildren = new ArrayList<T>();
    }

    public int getOffset() {
        return fOffset;
    }

    public int getLength() {
        return fLength;
    }

    public List<T> getChildren() {
        return fChildren;
    }

    protected T getFirstChild() {
        if (fChildren.size() == 0) return null;
        return fChildren.get(fChildren.size() - 1);
    }

    protected T getLastChild() {
        if (fChildren.size() == 0) return null;
        return fChildren.get(fChildren.size() - 1);
    }

    protected int findLowerChildPosition(int position) {
        int start = 0, end = fChildren.size() - 1;
        while (start < end) {
            int middlePosition = start + (end - start + 1) / 2;
            T middle = fChildren.get(middlePosition);
            if (position < middle.fOffset) {
                end = middlePosition - 1;
            } else if (position >= middle.fOffset + middle.fLength) {
                start = middlePosition;
            } else return middlePosition;
        }
        if (end < start || position < fChildren.get(start).fOffset) return -1; else return start;
    }

    protected int findUpperChildPosition(int position) {
        int start = 0, end = fChildren.size() - 1;
        while (start < end) {
            int middlePosition = start + (end - start) / 2;
            T middle = fChildren.get(middlePosition);
            if (position < middle.fOffset) {
                end = middlePosition;
            } else if (position >= middle.fOffset + middle.fLength) {
                start = middlePosition + 1;
            } else return middlePosition;
        }
        if (end < start || position > fChildren.get(start).getEnd()) return -1; else return start;
    }

    /**
	 * @see T#findUpperChild(int)
	 */
    protected final T findLowerChild(int position) {
        int lower = findLowerChildPosition(position);
        if (lower < 0) return null; else return fChildren.get(lower);
    }

    /**
	 * Finds child containing given position or, if such doesn't exist, a
	 * closest child with offset bigger than position.
	 * <p>
	 * <u>Important:</u> this method uses binary search algorithm.
	 * 
	 * @param position
	 * @return child containing position or the closest child with offset bigger
	 *         than position or null if both doesn't exist
	 */
    protected final T findUpperChild(int position) {
        int upper = findUpperChildPosition(position);
        if (upper < 0) return null; else return fChildren.get(upper);
    }

    protected int getEnd() {
        return fOffset + fLength - 1;
    }

    protected final boolean contains(int position) {
        return fOffset <= position && position < fOffset + fLength;
    }
}
