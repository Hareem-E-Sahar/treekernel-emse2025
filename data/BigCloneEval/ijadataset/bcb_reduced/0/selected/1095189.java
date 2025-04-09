package org.benetech.collections;

import static org.benetech.collections.Tree.ListDirection.BREADTH;
import static org.benetech.collections.Tree.ListDirection.DEPTH;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.benetech.beans.Filter;

/**
 * A tree data structure. Each node in the tree may have a single content element of type T, multiple meta content
 * elements of type V (with keys of U), and multiple child trees. Note that this is not synchronized, so care must
 * be taken when using trees with threads (generally, prevent simultaneous mutation and accessor calls).
 *
 * @author Reuben Firmin
 * @author John Brugge
 *
 * @param <T> The type of the primary content
 * @param <U> The key for the meta content
 * @param <V> The type of the meta content
 */
public class Tree<T, U, V> implements Collection<Tree<T, U, V>> {

    private final List<Tree<T, U, V>> children = new LinkedList<Tree<T, U, V>>();

    private final ListMap<U, TreeMetadata<U, V>> metaContent = new ListMap<U, TreeMetadata<U, V>>();

    private Tree<T, U, V> parent;

    private T content;

    private int size = 1;

    private int maxDepth;

    private List<Tree<T, U, V>> unfilteredDepthList;

    private List<Tree<T, U, V>> unfilteredBreadthList;

    private List<T> contentDepthList;

    private List<T> contentBreadthList;

    private Tree<T, U, V> topOfTree;

    private final Map<Integer, List<Tree<T, U, V>>> childrenAtDepth = new HashMap<Integer, List<Tree<T, U, V>>>();

    /** Map of child indices */
    private final Map<Tree<T, U, V>, Integer> childIndex = new HashMap<Tree<T, U, V>, Integer>();

    /** Direction that a list should be derived from this tree */
    public enum ListDirection {

        BREADTH, DEPTH
    }

    /**
     * Default constructor.
     */
    public Tree() {
    }

    /**
     * Constructor specifying content.
     * @param content never null
     */
    public Tree(final T content) {
        this.content = content;
    }

    /**
     * Invalidate the cached copies of various lists.
     */
    private void invalidate() {
        unfilteredDepthList = null;
        unfilteredBreadthList = null;
        contentDepthList = null;
        contentBreadthList = null;
        childrenAtDepth.clear();
        childIndex.clear();
    }

    /**
     * Return the children belonging to this tree node.
     * @return Never null
     */
    public final List<? extends Tree<T, U, V>> getChildren() {
        return children;
    }

    /**
     * Return the first child with the specified content.
     * @param content The content to match, based on an equals comparison
     * @return Null if no match is found
     */
    public final Tree<T, U, V> getChildWithContent(final T content) {
        final List<Tree<T, U, V>> children = (List<Tree<T, U, V>>) toList(ListDirection.DEPTH);
        for (Tree<T, U, V> child : children) {
            if (child.getContent().equals(content)) {
                return child;
            }
        }
        return null;
    }

    /**
     * Return the meta content belonging to this tree node.
     * @return Never null
     */
    public final ListMap<U, TreeMetadata<U, V>> getMetaContent() {
        return metaContent;
    }

    /**
     * Return the list of TreeMetadata mapped against this key.
     * @param metaContentKey the key content was mapped to
     * @return Null if the key isn't mapped
     */
    public final List<TreeMetadata<U, V>> getMetadataContent(final U metaContentKey) {
        return metaContent.get(metaContentKey);
    }

    /**
     * Return the first metacontent mapped to this key. Useful when only one is known to be mapped.
     * @param metaContentKey The key that the metacontent is mapped to.
     * @return Null if the key isn't mapped
     */
    public final V getFirstMetaContent(final U metaContentKey) {
        final List<TreeMetadata<U, V>> content = getMetadataContent(metaContentKey);
        if (content != null && content.size() > 0) {
            return content.get(0).getValue();
        }
        return null;
    }

    /**
     * The content of this tree node.
     * @return May be null
     */
    public T getContent() {
        return content;
    }

    /**
     * Return the parent of this tree node.
     * @return Null if this is the top of the tree.
     */
    public final Tree<T, U, V> getParent() {
        return parent;
    }

    /**
     * The parent of this tree. Automatically called when this tree is added as a child to another.
     * @param parent May be null, if this tree is the top
     */
    public final void setParent(final Tree parent) {
        this.parent = parent;
        this.topOfTree = null;
    }

    /**
     * Add a child to this tree node.
     * @param child Should not be null
     */
    public final void addChild(final Tree<T, U, V> child) {
        insertChild(children.size(), child);
    }

    /**
     * Inserts a child at the beginning of the list of children.
     * @param child Should not be null
     */
    public final void insertChild(final Tree<T, U, V> child) {
        insertChild(0, child);
    }

    /**
     * Private method that allows insertion of a child to this node.
     * @param index The index to insert at
     * @param child Should not be null
     * @throws IllegalArgumentException if the element is the same as the collection
     */
    private void insertChild(final int index, final Tree<T, U, V> child) {
        if (this.equals(child)) {
            throw new IllegalArgumentException("Cannot add a tree to itself");
        }
        children.add(index, child);
        child.setParent(this);
        incrementSizeAndDepth(child.getSize(), child.getMaxDepth() + 1);
    }

    /**
     * Increment the size and depth. Called by adding a child, recursively upwards.
     * @param childIncSize The child tree's incremental size.
     * @param depth The depth that we're counting.
     */
    private void incrementSizeAndDepth(final int childIncSize, final int depth) {
        invalidate();
        size += childIncSize;
        if (maxDepth < depth) {
            maxDepth = depth;
        }
        if (parent != null) {
            parent.incrementSizeAndDepth(childIncSize, depth + 1);
        }
    }

    /**
     * Decrements the size and depth. Called when removing a child.
     * @param childDecSize The child tree's decrement size.
     */
    private void decrementSizeAndDepth(final int childDecSize) {
        invalidate();
        size -= childDecSize;
        maxDepth = calcMaxDepth();
        if (parent != null) {
            parent.decrementSizeAndDepth(childDecSize);
        }
    }

    /**
     * Calculates how deep this tree is.
     * @return the max depth of all children, zero if none
     */
    private int calcMaxDepth() {
        int depth = 0;
        if (children.isEmpty()) {
            return depth;
        } else {
            depth++;
            for (Tree child : children) {
                final int childDepth = child.calcMaxDepth();
                if (childDepth + 1 > depth) {
                    depth = childDepth + 1;
                }
            }
        }
        return depth;
    }

    /**
     * Set the content of this tree node.
     * @param content May be null
     */
    public final void setContent(final T content) {
        this.content = content;
    }

    /**
     * Add meta content to this tree node, for this key. Note that multiple meta content can be mapped to the same key.
     * @param key The meta content key
     * @param value The meta content value
     */
    public final void addMetaContent(final U key, final V value) {
        this.metaContent.put(key, new TreeMetadata(key, value));
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        if (getContent() != null) {
            return getContent().toString();
        }
        return "null";
    }

    /**
     * Convert tree to a list, filtering only a certain set of nodes. Results of filtering are not cached. Returns a
     * depth first list.
     * @param filter never null
     * @return Filtered list representation of the tree
     */
    public final List<? extends Tree> toList(final Filter<? super Tree, ?> filter) {
        final List<? extends Tree> masterList = this.toList(ListDirection.DEPTH);
        final List<Tree> filterList = new ArrayList<Tree>(this.getSize());
        for (Tree tree : masterList) {
            final Tree[] eArray = (Tree[]) Array.newInstance(getClass(), 1);
            eArray[0] = tree;
            if (filter.accept(eArray)) {
                filterList.add(tree);
            }
        }
        return filterList;
    }

    /**
     * Return the tree flattened to a list, either depth first or breadth first.
     * @param direction The direction to flatten in
     * @return Never null
     */
    public final List<? extends Tree<T, U, V>> toList(final ListDirection direction) {
        switch(direction) {
            case DEPTH:
                if (unfilteredDepthList == null) {
                    final List<Tree<T, U, V>> depthList = new ArrayList<Tree<T, U, V>>(this.getSize());
                    buildDepthFirstList(depthList, this);
                    unfilteredDepthList = depthList;
                    return depthList;
                }
                return unfilteredDepthList;
            case BREADTH:
                if (unfilteredBreadthList == null) {
                    final List<Tree<T, U, V>> breadthList = new ArrayList<Tree<T, U, V>>(this.getSize());
                    buildBreadthFirstList(breadthList, this);
                    unfilteredBreadthList = breadthList;
                    return breadthList;
                }
            default:
                throw new UnsupportedOperationException("Direction: " + direction + " not supported");
        }
    }

    /**
     * Helper method for {@link #toList()}. The node is added at the head of the list.
     * @param list The list being built
     * @param node The node to add
     */
    private void buildDepthFirstList(final List<Tree<T, U, V>> list, final Tree<T, U, V> node) {
        list.add(node);
        for (Tree child : node.getChildren()) {
            buildDepthFirstList(list, child);
        }
    }

    /**
     * Helper method for {@link #toList()}.
     * @param list The list being built
     * @param node The node to add
     */
    private void buildBreadthFirstList(final List<Tree<T, U, V>> list, final Tree<T, U, V> node) {
        for (int i = 0; i < node.getMaxDepth(); i++) {
            for (Tree<T, U, V> child : node.getChildrenAtDepth(i)) {
                list.add(child);
            }
        }
    }

    /**
     * Behaves in the same way as {@link #toList()}, except the output list consists of only the content of the tree
     * nodes. Results are cached, so it isn't expensive to repeatedly call.
     * @param direction Direction to flatten the tree in
     * @return List of content of this tree structure.
     */
    public final List<T> toContentList(final ListDirection direction) {
        if (direction == ListDirection.BREADTH && contentBreadthList != null) {
            return contentBreadthList;
        }
        if (direction == ListDirection.DEPTH && contentDepthList != null) {
            return contentDepthList;
        }
        final List<? extends Tree<T, U, V>> flattenedTree = toList(direction);
        final List<T> out = new ArrayList(this.getSize());
        for (Tree<T, U, V> node : flattenedTree) {
            out.add(node.getContent());
        }
        if (direction == ListDirection.BREADTH) {
            contentBreadthList = out;
        } else if (direction == ListDirection.DEPTH) {
            contentDepthList = out;
        }
        return out;
    }

    /**
    * Behaves in the same way as {@link #toList()}, except the output list consists of only the content of the tree
    * nodes. Does not cache the converted list, but relies on internally cached master (non-content) list, so not
    * overly expensive. Applies filters, simultaneously.
	* @param direction The direction of the list flattening - breadth or depth.
    * @param filters The filters to apply, in the order that they should be run in. If the first filter fails,
    * the second won't be run
	* @return List of content of this tree structure.
    */
    public final List<T> toContentList(final ListDirection direction, final Filter<? super T, ?>... filters) {
        final List<T> flattenedTree = toContentList(direction);
        final List<T> out = new ArrayList(this.getSize());
        for (T node : flattenedTree) {
            if (node != null) {
                final T[] eArray = (T[]) Array.newInstance(getContentClass(), 1);
                eArray[0] = node;
                boolean allAccept = true;
                for (Filter filter : filters) {
                    allAccept = allAccept && filter.accept(eArray);
                }
                if (allAccept) {
                    out.add(node);
                }
            }
        }
        return out;
    }

    /**
	 * Get the size of this tree, i.e. the number of children to the roots.
	 *
	 * @return at least one, including itself
	 */
    public final int getSize() {
        return size;
    }

    /**
     * Get the maximum depth of the tree. A tree without children has a depth of 0.
     * @return the depth.
     */
    public final int getMaxDepth() {
        return maxDepth;
    }

    /**
     * Get the list of child trees at the given depth in this tree. If 0 is selected, only the root will be returned.
     * Otherwise, the tree will be walked breadth first until the given depth is reached, and the set of trees across
     * that span returned. If the tree cannot be descended to the requested depth, or a negative depth is given, null
     * will be returned. This caches its results after the first call, so may be called repeatedly, efficiently.
     * @param depth Depth to return children at
     * @return perhaps null; use {@link #getMaxDepth()} before calling this to check
     */
    public final List<? extends Tree<T, U, V>> getChildrenAtDepth(final int depth) {
        if (childrenAtDepth.get(depth) == null) {
            if (depth == 0) {
                final List<Tree<T, U, V>> list = new LinkedList<Tree<T, U, V>>();
                list.add(this);
                childrenAtDepth.put(depth, list);
            } else {
                childrenAtDepth.put(depth, getChildrenAtDepth(depth, this));
            }
        }
        return childrenAtDepth.get(depth);
    }

    /**
     * Recursive function to return children at a specified depth.
     * @param depth The depth to iterate to
     * @param tree The tree to iterate over
     * @return The list of children at the specified depth, perhaps null
     */
    private List<Tree<T, U, V>> getChildrenAtDepth(final int depth, final Tree<T, U, V> tree) {
        List<Tree<T, U, V>> list = null;
        if (depth == 1) {
            list = new LinkedList<Tree<T, U, V>>();
            for (Tree<T, U, V> child : tree.getChildren()) {
                list.add(child);
            }
        } else {
            for (Tree<T, U, V> child : tree.getChildren()) {
                final List<Tree<T, U, V>> childList = getChildrenAtDepth(depth - 1, child);
                if (childList != null) {
                    if (list == null) {
                        list = childList;
                    } else {
                        list.addAll(childList);
                    }
                }
            }
        }
        return list;
    }

    /**
     * Get the tree at the top of the hierarchy.
     * @return Never null
     */
    public final Tree<T, U, V> getTopOfTree() {
        if (topOfTree == null) {
            Tree<T, U, V> parent = this;
            Tree<T, U, V> pointer = this;
            while (pointer != null) {
                pointer = pointer.getParent();
                if (pointer == null) {
                    topOfTree = parent;
                    break;
                } else {
                    parent = pointer;
                }
            }
        }
        return topOfTree;
    }

    /**
     * Get the distance from this tree to the other, assuming they are in the same hierarchy. Returns negative if
     * the other appears "before" this tree (based on a depth first flattening), positive if it's "after", and zero
     * if they are the same.
     * @param other The other tree to find the distance from
     * @return null if the other is not in the same hierarchy
     */
    public final Integer getDistanceFrom(final Tree<T, U, V> other) {
        return getTopOfTree().getChildIndex(other) - getTopOfTree().getChildIndex(this);
    }

    /**
     * Get the index of the given child, assuming that the tree has been flattened depth first. Results are cached
     * for efficiency. 0 for itself, 1st child has index of 1.
     * @param child The child whose index to look up
     * @return Never null.
     */
    private Integer getChildIndex(final Tree<T, U, V> child) {
        Integer index = childIndex.get(child);
        if (index == null) {
            final List<? extends Tree<T, U, V>> list = toList(ListDirection.DEPTH);
            for (int i = 0; i < list.size(); i++) {
                childIndex.put(list.get(i), Integer.valueOf(i));
            }
            index = childIndex.get(child);
        }
        return index;
    }

    /**
     * Return a "chart" of the sizes at the given depths of the tree. The size at the 0th index of the chart is always
     * 1 (i.e. this tree); the size at the 1st, the total number of children of this tree; the size at the 2nd, the
     * total number of grandchildren of this tree, etc.
     * @param tree
     * @return Never null
     */
    public final int[] getDepthChart() {
        final int[] depthSizes = new int[getMaxDepth() + 1];
        depthSizes[0] = 1;
        for (int i = 1; i < depthSizes.length; i++) {
            final List<? extends Tree<T, U, V>> children = getChildrenAtDepth(i - 1);
            int sumDirectChildren = 0;
            for (Tree child : children) {
                sumDirectChildren += child.getChildren().size();
            }
            depthSizes[i] = sumDirectChildren;
        }
        return depthSizes;
    }

    /**
     * Get the depth of this tree relative to the top of the tree. 0 if this is the top.
     * @return The depth.
     */
    public final int getDepthRelativeToTopOfTree() {
        int i = 0;
        Tree<T, U, V> parent = getParent();
        while (parent != null) {
            i++;
            parent = parent.getParent();
        }
        return i;
    }

    /**
     * Return true if this tree is a child (at any depth) of the other tree.
     * @param other The tree that might be an ancestor
     * @return true if so
     */
    public final boolean childOf(final Tree<T, U, V> other) {
        if (getParent() == null) {
            return false;
        } else if (getParent().equals(other)) {
            return true;
        } else {
            return getParent().childOf(other);
        }
    }

    /**
     * Return true if this tree is an ancestor of the other tree.
     * @param other The tree that might be a child
     * @return true if so
     */
    public final boolean ancestorOf(final Tree<T, U, V> other) {
        if (getChildren().contains(other)) {
            return true;
        } else {
            for (Tree<T, U, V> child : getChildren()) {
                if (child.ancestorOf(other)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Given a list of marker nodes, sequential to the hiearchy of this tree, flatten this tree and return a list of
     * lists demarcated by the markers. Note that this will not necessarily return a comprehensive list of the entire
     * tree if the first marker occurs deeper in the hierarchy than this tree. The last list will be from the last
     * marker to the end of the tree.
     * @param markers Marker nodes used to demarcate the tree. If null, just return tolist
     * @return Never null
     */
    public final List<List<? extends Tree<T, U, V>>> toListAndSplit(final List<? extends Tree<T, U, V>> markers) {
        final List<List<? extends Tree<T, U, V>>> output = new LinkedList<List<? extends Tree<T, U, V>>>();
        final List<? extends Tree<T, U, V>> master = this.toList(ListDirection.DEPTH);
        if (markers == null) {
            output.add(master);
        } else {
            boolean copying = false;
            int markerIndex = 0;
            List<Tree<T, U, V>> currentSegment = null;
            for (Tree<T, U, V> candidate : master) {
                if (markerIndex < markers.size() && markers.get(markerIndex).equals(candidate)) {
                    copying = true;
                    currentSegment = new LinkedList<Tree<T, U, V>>();
                    output.add(currentSegment);
                    markerIndex++;
                }
                if (copying) {
                    currentSegment.add(candidate);
                }
            }
        }
        return output;
    }

    /**
     * Adds the specified element to the collection.
     * @param o the element to add
     * @return true if the element was added
     */
    public final boolean add(final Tree<T, U, V> o) {
        if (o == null) {
            throw new NullPointerException("Cannot add null objects to this collection");
        } else if (o.equals(this)) {
            throw new IllegalArgumentException("Cannot add self to the collection");
        } else {
            addChild(o);
        }
        return true;
    }

    /**
     * Adds each of the elements of c as children of this Tree.
     * Each of the elements must itself be a Tree, and null objects are not supported.
     * @param c a Collection containing elements to be added to the Tree
     * @return true if all of the elements were added
     */
    public final boolean addAll(final Collection<? extends Tree<T, U, V>> c) {
        if (c == null) {
            throw new NullPointerException("Cannot add null collections to this collection");
        } else {
            for (Tree<T, U, V> o : c) {
                add(o);
            }
        }
        return true;
    }

    /**
     * Removes all elements from this Tree.
     * It retains the root node, and clears the parent relationship of all child nodes.
     */
    public final void clear() {
        for (Tree child : getChildren()) {
            child.localClear();
        }
        children.clear();
        maxDepth = 0;
        size = 1;
        invalidate();
    }

    /**
     * Clears just this tree. Helper method for clear, to prevent unnecessary recursion.
     */
    private void localClear() {
        for (Tree child : getChildren()) {
            child.localClear();
        }
        children.clear();
        maxDepth = 0;
        size = 1;
        parent = null;
        invalidate();
    }

    /**
     * Returns true if this Tree contains the specified Tree.
     * @param o the Tree whose presence is to be tested
     * @return true if the Tree contains o
     */
    public final boolean contains(final Object o) {
        boolean result = false;
        if (o == null) {
            throw new NullPointerException("Collection does now allow null values");
        } else if (this.equals(o)) {
            result = true;
        } else {
            for (Tree child : children) {
                if (child.contains(o)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Returns true if each element of c is contained in this Tree.
     * Null objects are not supported.
     * @param c a Collection of elements to test for inclusion
     * @return true if this Tree contains all of the elements of c
     */
    public final boolean containsAll(final Collection c) {
        boolean result = true;
        if (c == null) {
            throw new NullPointerException("Collection does not allow null values");
        } else {
            for (Object o : c) {
                if (!contains(o)) {
                    result = false;
                }
            }
        }
        return result;
    }

    /**
     * Returns true if this Tree has no child elements.
     * @return true if this Tree has no child elements
     */
    public final boolean isEmpty() {
        return children.isEmpty();
    }

    /**
     * Returns an iterator over the elements in the tree.
     * The elements are returned in a depth-first ordering.
     * @return an Iterator over the elements of the Tree
     */
    public final Iterator<Tree<T, U, V>> iterator() {
        return ((List<Tree<T, U, V>>) toList(ListDirection.DEPTH)).iterator();
    }

    /**
     * Removes the first instance of the given element from the Tree, if it is found.
     * @param o the element to be removed from the Tree
     * @return true if the element was removed
     */
    public final boolean remove(final Object o) {
        boolean result = false;
        if (o == null) {
            throw new NullPointerException("Collection does not support null values");
        } else if (children.contains(o)) {
            children.remove(o);
            final Tree child = (Tree) o;
            child.setParent(null);
            decrementSizeAndDepth(child.getSize());
            result = true;
        } else {
            for (Tree child : children) {
                result = child.remove(o);
                if (result) {
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Removes each element of the specified Collection from the Tree.
     * @param c a Collection of objects to remove from the Tree
     * @return true if any of the elements were removed
     */
    public final boolean removeAll(final Collection c) {
        boolean result = false;
        if (c == null) {
            throw new NullPointerException("Collection does not support null values");
        } else {
            for (Object o : c) {
                if (remove(o)) {
                    result = true;
                }
            }
        }
        return result;
    }

    /**
     * Retain only those elements in the Tree that are found in the specified Collection.
     * This also retains the elements' parents so that a Tree structure is preserved.
     * The effect can also be described as removing all elements from this Tree that are
     * not found in the specified Collection.
     * @param c the elements that are to be retained
     * @return true if the Tree was changed as a result of the action
     */
    public final boolean retainAll(final Collection c) {
        boolean result = false;
        if (c == null) {
            throw new NullPointerException("Collection does not support null values");
        } else {
            final List<Tree> removeList = new LinkedList<Tree>();
            collectRemovals(c, removeList);
            if (removeList.size() > 0) {
                removeAll(removeList);
                result = true;
            }
        }
        return result;
    }

    /**
     * Helper method for collectRemovals.
     * @param c The collection we're removing from
     * @param removeList The list being built
     */
    private void collectRemovals(final Collection c, final List removeList) {
        if (!this.containsAny(c)) {
            if (getParent() != null) {
                removeList.add(this);
            }
        } else if (!c.contains(this)) {
            for (Tree child : children) {
                child.collectRemovals(c, removeList);
            }
        }
    }

    /**
     * True if this tree contains any in the collection.
     * @param c Collection of objects to look for
     * @return True if one or more are found within this tree
     */
    public final boolean containsAny(final Collection c) {
        boolean result = false;
        for (Object o : c) {
            if (contains(o)) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * Returns the size of this Tree as the number of nodes.
     * @return at least one (the root node itself)
     */
    public final int size() {
        return getSize();
    }

    /**
     * Returns an array containing all of the elements of this Tree.
     * The order of the elements is the same as that returned by its iterator.
     * @return an array containing all of the elements of this Tree
     */
    public final Object[] toArray() {
        return toList(ListDirection.DEPTH).toArray();
    }

    /**
     * {@inheritDoc}
     */
    public final Object[] toArray(final Object[] a) {
        if (a == null) {
            throw new NullPointerException("Array store cannot be null");
        }
        Object[] out;
        if (a.length < size) {
            out = (Object[]) Array.newInstance(a.getClass().getComponentType(), size);
        } else {
            for (int i = size; i < a.length; i++) {
                a[i] = null;
            }
            out = a;
        }
        System.arraycopy(toArray(), 0, out, 0, size);
        return out;
    }

    /**
     * @Override
     * {@inheritDoc}
     */
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((content == null) ? 0 : content.hashCode());
        result = prime * result + maxDepth;
        result = prime * result + ((parent == null) ? 0 : parent.hashCode());
        result = prime * result + size;
        return result;
    }

    /**
     * Return the class of the content used by this tree. This is useful for toArray() etc.
     * @return never null
     */
    public final Class getContentClass() {
        return content.getClass();
    }
}
