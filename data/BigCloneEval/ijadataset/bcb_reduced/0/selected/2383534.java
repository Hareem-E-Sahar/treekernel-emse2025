package jode.util;

import java.util.NoSuchElementException;
import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

/**
 * This class provides a red-black tree implementation of the SortedMap
 * interface.  Elements in the Map will be sorted by either a user-provided
 * Comparator object, or by the natural ordering of the keys.
 *
 * The algorithms are adopted from Corman, Leiserson,
 * and Rivest's <i>Introduction to Algorithms.<i>  In other words,
 * I cribbed from the same pseudocode as Sun.  <em>Any similarity
 * between my code and Sun's (if there is any -- I have never looked
 * at Sun's) is a result of this fact.</em>
 *
 * TreeMap guarantees O(log n) insertion and deletion of elements.  That 
 * being said, there is a large enough constant coefficient in front of 
 * that "log n" (overhead involved in keeping the tree 
 * balanced), that TreeMap may not be the best choice for small
 * collections.
 *
 * TreeMap is a part of the JDK1.2 Collections API.  Null keys are allowed
 * only if a Comparator is used which can deal with them.  Null values are 
 * always allowed.
 *
 * @author           Jon Zeppieri
 * @version          $Revision: 946 $
 * @modified         $Id: TreeMap.java 946 1999-06-28 11:08:46Z jochen $
 */
public class TreeMap extends AbstractMap implements SortedMap, Cloneable, Serializable {

    private static final int ENTRIES = 0;

    private static final int KEYS = 1;

    private static final int VALUES = 2;

    private static final int RED = -1;

    private static final int BLACK = 1;

    private static final RBNode NIL = new RBNode(null, null);

    /** The root node of this TreeMap */
    transient RBNode _oRoot;

    /** The size of this TreeMap */
    transient int _iSize;

    /** Number of modifications */
    transient int _iModCount;

    /** This TreeMap's comparator */
    Comparator comparator;

    static final long serialVersionUID = 919286545866124006L;

    /**
   * Instantiate a new TreeMap with no elements, using the keys'
   * natural ordering to sort.
   *
   * @see java.lang.Comparable
   */
    public TreeMap() {
        this((Comparator) null);
    }

    /**
   * Instantiate a new TreeMap with no elements, using the provided
   * comparator to sort.
   *
   * @param        oComparator        a Comparator object, used to sort 
   *                                  the keys of this SortedMap
   */
    public TreeMap(Comparator oComparator) {
        _oRoot = NIL;
        _iSize = 0;
        _iModCount = 0;
        comparator = oComparator;
    }

    /**
   * Instantiate a new TreeMap, initializing it with all of the
   * elements in the provided Map.  The elements will be sorted 
   * using the natural ordering of the keys.
   *
   * @param              oMap         a Map, whose keys will be put into
   *                                  this TreeMap
   *
   * @throws             ClassCastException     if the keys in the provided
   *                                            Map do not implement 
   *                                            Comparable
   *
   * @see                java.lang.Comparable
   */
    public TreeMap(Map oMap) {
        this((Comparator) null);
        putAll(oMap);
    }

    /** 
   * Instantiate a new TreeMap, initializing it with all of the
   * elements in the provided SortedMap.  The elements will be sorted 
   * using the same method as in the provided SortedMap.
   */
    public TreeMap(SortedMap oSortedMap) {
        this(oSortedMap.comparator());
        Map.Entry[] arEntries = new Map.Entry[oSortedMap.size()];
        Iterator itElements = oSortedMap.entrySet().iterator();
        int i = 0;
        while (itElements.hasNext()) arEntries[i++] = (Map.Entry) itElements.next();
        _iSize = i;
        putAllLinear(arEntries);
    }

    public void clear() {
        _oRoot = NIL;
        _iSize = 0;
        _iModCount++;
    }

    public Object clone() {
        TreeMap oClone;
        try {
            oClone = (TreeMap) super.clone();
            oClone._oRoot = _oRoot;
            oClone.comparator = comparator;
            oClone._iSize = _iSize;
            oClone._iModCount = 0;
        } catch (CloneNotSupportedException e) {
            oClone = null;
        }
        return oClone;
    }

    public Comparator comparator() {
        return comparator;
    }

    public boolean containsKey(Object oKey) {
        return (treeSearch(_oRoot, comparator, oKey) != NIL);
    }

    public boolean containsValue(Object oValue) {
        RBNode oNode = _oRoot;
        Object oCurrentValue;
        while (oNode != NIL) {
            oCurrentValue = oNode.getValue();
            if (((oValue == null) && (oCurrentValue == null)) || oValue.equals(oCurrentValue)) return true;
            oNode = treeSuccessor(oNode);
        }
        return false;
    }

    public Set entrySet() {
        return new TreeMapSet(this, ENTRIES);
    }

    public Object firstKey() {
        try {
            return treeMin(_oRoot).getKey();
        } catch (NullPointerException e) {
            throw new NoSuchElementException("TreeMap is empty");
        }
    }

    public Object get(Object oKey) {
        RBNode oNode = treeSearch(_oRoot, comparator, oKey);
        return (oNode != NIL) ? oNode.getValue() : null;
    }

    public SortedMap headMap(Object oToKey) {
        if (keyInClosedMaxRange(comparator, oToKey, null)) return new SubTreeMap(null, oToKey); else throw new IllegalArgumentException(getArgumentError("create a headMap", null, null));
    }

    public Set keySet() {
        return new TreeMapSet(this, KEYS);
    }

    public Object lastKey() {
        try {
            return treeMax(_oRoot).getKey();
        } catch (NullPointerException e) {
            throw new NoSuchElementException("TreeMap is empty");
        }
    }

    public Object put(Object oKey, Object oValue) {
        Map.Entry oEntry = rbInsert(this, comparator, new RBNode(oKey, oValue));
        if (oEntry == NIL) _iSize++;
        _iModCount++;
        return ((oEntry == NIL) ? null : oEntry.getValue());
    }

    public void putAll(Map oMap) {
        Iterator itEntries = oMap.entrySet().iterator();
        Map.Entry oEntry;
        while (itEntries.hasNext()) {
            oEntry = (Map.Entry) itEntries.next();
            put(oEntry.getKey(), oEntry.getValue());
        }
    }

    public Object remove(Object oKey) {
        RBNode oResult = treeSearch(_oRoot, comparator, oKey);
        if (oResult != NIL) {
            oResult = rbDelete(this, oResult);
            _iSize--;
            _iModCount++;
            return oResult.getValue();
        } else {
            return null;
        }
    }

    public int size() {
        return _iSize;
    }

    public SortedMap subMap(Object oFromKey, Object oToKey) {
        if (compare(comparator, oFromKey, oToKey) < 0) return new SubTreeMap(oFromKey, oToKey); else throw new IllegalArgumentException(getArgumentError("create a subMap", null, null));
    }

    public SortedMap tailMap(Object oFromKey) {
        if (keyInMinRange(comparator, oFromKey, null)) return new SubTreeMap(oFromKey, null); else throw new IllegalArgumentException(getArgumentError("create a tailMap", null, null));
    }

    public Collection values() {
        return new TreeMapCollection(this);
    }

    void putAllLinear(Map.Entry[] arEntries) {
        int iHeight;
        double dHeight;
        boolean boComplete;
        dHeight = Math.pow((double) arEntries.length, (double) 0.5);
        iHeight = (int) dHeight;
        boComplete = (dHeight == ((double) iHeight));
        _oRoot = buildTree(arEntries, iHeight, boComplete, 0, 0, arEntries.length);
    }

    private void writeObject(ObjectOutputStream oOut) throws IOException {
        RBNode oNode = treeMin(_oRoot);
        oOut.defaultWriteObject();
        oOut.writeInt(_iSize);
        while (oNode != NIL) {
            oOut.writeObject(oNode.getKey());
            oOut.writeObject(oNode.getValue());
            oNode = treeSuccessor(oNode);
        }
    }

    private void readObject(ObjectInputStream oIn) throws IOException, ClassNotFoundException {
        int i;
        Map.Entry[] arEntries;
        oIn.defaultReadObject();
        _iSize = oIn.readInt();
        _iModCount = 0;
        arEntries = new Map.Entry[_iSize];
        for (i = 0; i < _iSize; i++) arEntries[i] = new RBNode(oIn.readObject(), oIn.readObject());
        putAllLinear(arEntries);
    }

    private static final RBNode buildTree(Map.Entry[] arEntries, int iHeight, boolean boComplete, int iCurrentTier, int iStart, int iStop) {
        RBNode oNewTree;
        int iRootIndex;
        if (iStart == iStop) {
            return NIL;
        } else {
            iRootIndex = (iStop + iStart) / 2;
            oNewTree = new RBNode(arEntries[iRootIndex].getKey(), arEntries[iRootIndex].getValue());
            oNewTree._oLeft = buildTree(arEntries, iHeight, boComplete, (iCurrentTier + 1), iStart, iRootIndex);
            oNewTree._oRight = buildTree(arEntries, iHeight, boComplete, (iCurrentTier + 1), (iRootIndex + 1), iStop);
            if ((!boComplete) && ((iHeight % 2) == 1) && (iCurrentTier >= (iHeight - 2))) oNewTree._iColor = (iCurrentTier == (iHeight - 1)) ? RED : BLACK; else oNewTree._iColor = ((iCurrentTier % 2) == 1) ? RED : BLACK;
            if (oNewTree._oLeft != NIL) oNewTree._oLeft._oParent = oNewTree;
            if (oNewTree._oRight != NIL) oNewTree._oRight._oParent = oNewTree;
            return oNewTree;
        }
    }

    static final int compare(Comparator oComparator, Object oOne, Object oTwo) {
        return ((oComparator == null) ? ((Comparable) oOne).compareTo(oTwo) : oComparator.compare(oOne, oTwo));
    }

    static final boolean keyInMinRange(Comparator oComparator, Object oKey, Object oMinKey) {
        return ((oMinKey == null) || (compare(oComparator, oMinKey, oKey) <= 0));
    }

    static final boolean keyInMaxRange(Comparator oComparator, Object oKey, Object oMaxKey) {
        return ((oMaxKey == null) || (compare(oComparator, oMaxKey, oKey) > 0));
    }

    static final boolean keyInClosedMaxRange(Comparator oComparator, Object oKey, Object oMaxKey) {
        return ((oMaxKey == null) || (compare(oComparator, oMaxKey, oKey) >= 0));
    }

    static final boolean keyInRange(Comparator oComparator, Object oKey, Object oMinKey, Object oMaxKey) {
        return (keyInMinRange(oComparator, oKey, oMinKey) && keyInMaxRange(oComparator, oKey, oMaxKey));
    }

    static final boolean keyInClosedRange(Comparator oComparator, Object oKey, Object oMinKey, Object oMaxKey) {
        return (keyInMinRange(oComparator, oKey, oMinKey) && keyInClosedMaxRange(oComparator, oKey, oMaxKey));
    }

    static final RBNode treeSearch(RBNode oRoot, Comparator oComparator, Object oKey) {
        int iCompareResult;
        while (oRoot != NIL) {
            iCompareResult = compare(oComparator, oKey, oRoot.getKey());
            if (iCompareResult == 0) return oRoot; else if (iCompareResult < 0) oRoot = oRoot._oLeft; else oRoot = oRoot._oRight;
        }
        return oRoot;
    }

    static final RBNode treeMin(RBNode oRoot) {
        while (oRoot._oLeft != NIL) oRoot = oRoot._oLeft;
        return oRoot;
    }

    static final RBNode treeMinConstrained(RBNode oRoot, Comparator oComparator, Object oMinKey, Object oMaxKey) {
        int iCompare;
        RBNode oCurrent;
        do {
            oCurrent = oRoot;
            iCompare = compare(oComparator, oMinKey, oCurrent.getKey());
            if (iCompare == 0) return oRoot; else oRoot = (iCompare < 0) ? oCurrent._oLeft : oCurrent._oRight;
        } while (oRoot != NIL);
        if (iCompare > 0) oCurrent = treeSuccessor(oCurrent);
        return oCurrent;
    }

    static final RBNode treeMax(RBNode oRoot) {
        while (oRoot._oRight != NIL) oRoot = oRoot._oRight;
        return oRoot;
    }

    static final RBNode treeMaxConstrained(RBNode oRoot, Comparator oComparator, Object oMinKey, Object oMaxKey) {
        int iCompare;
        RBNode oCurrent;
        do {
            oCurrent = oRoot;
            iCompare = compare(oComparator, oMaxKey, oCurrent.getKey());
            if (iCompare == 0) return oRoot; else oRoot = (iCompare < 0) ? oCurrent._oLeft : oCurrent._oRight;
        } while (oRoot != NIL);
        if (iCompare < 0) oCurrent = treePredecessor(oCurrent);
        return oCurrent;
    }

    static RBNode lowerBound(RBNode oRoot, Comparator oComparator, Object oMinKey, Object oMaxKey) {
        return ((oMinKey != null) ? treeMinConstrained(oRoot, oComparator, oMinKey, oMaxKey) : treeMin(oRoot));
    }

    static RBNode upperBound(RBNode oRoot, Comparator oComparator, Object oMinKey, Object oMaxKey) {
        return ((oMaxKey != null) ? treeMaxConstrained(oRoot, oComparator, oMinKey, oMaxKey) : NIL);
    }

    static final RBNode treeSuccessor(RBNode oNode) {
        RBNode oParent;
        if (oNode._oRight != NIL) return treeMin(oNode._oRight);
        oParent = oNode._oParent;
        while ((oParent != NIL) && (oNode == oParent._oRight)) {
            oNode = oParent;
            oParent = oParent._oParent;
        }
        return oParent;
    }

    static final RBNode treePredecessor(RBNode oNode) {
        RBNode oParent;
        if (oNode._oLeft != NIL) return treeMax(oNode._oLeft);
        oParent = oNode._oParent;
        while ((oParent != NIL) && (oNode == oParent._oLeft)) {
            oNode = oParent;
            oParent = oParent._oParent;
        }
        return oParent;
    }

    static final String getArgumentError(String stType, Object oMinKey, Object oMaxKey) {
        return ("Attempt to " + stType + " outside of range [" + oMinKey.toString() + ", " + oMaxKey.toString() + ").");
    }

    private static final RBNode treeInsert(TreeMap oTree, Comparator oComparator, RBNode oNewNode) {
        int iCompareResult;
        Object oNewKey = oNewNode.getKey();
        RBNode oParent = NIL;
        RBNode oRoot = oTree._oRoot;
        RBNode oResult;
        while (oRoot != NIL) {
            oParent = oRoot;
            iCompareResult = compare(oComparator, oNewKey, oRoot.getKey());
            if (iCompareResult == 0) {
                oResult = new RBNode(oRoot.getKey(), oRoot.getValue());
                oRoot.key = oNewNode.key;
                oRoot.value = oNewNode.value;
                return oResult;
            } else {
                oRoot = (iCompareResult < 0) ? oRoot._oLeft : oRoot._oRight;
            }
        }
        oNewNode._oParent = oParent;
        if (oParent == NIL) oTree._oRoot = oNewNode; else if (compare(oComparator, oNewKey, oParent.getKey()) < 0) oParent._oLeft = oNewNode; else oParent._oRight = oNewNode;
        return oRoot;
    }

    private static final void leftRotate(TreeMap oTree, RBNode oNode) {
        RBNode oChild = oNode._oRight;
        oNode._oRight = oChild._oLeft;
        if (oChild._oLeft != NIL) oChild._oLeft._oParent = oNode;
        oChild._oParent = oNode._oParent;
        if (oNode._oParent == NIL) oTree._oRoot = oChild; else if (oNode == oNode._oParent._oLeft) oNode._oParent._oLeft = oChild; else oNode._oParent._oRight = oChild;
        oChild._oLeft = oNode;
        oNode._oParent = oChild;
    }

    private static final void rightRotate(TreeMap oTree, RBNode oNode) {
        RBNode oChild = oNode._oLeft;
        oNode._oLeft = oChild._oRight;
        if (oChild._oRight != NIL) oChild._oRight._oParent = oNode;
        oChild._oParent = oNode._oParent;
        if (oNode._oParent == NIL) oTree._oRoot = oChild; else if (oNode == oNode._oParent._oRight) oNode._oParent._oRight = oChild; else oNode._oParent._oLeft = oChild;
        oChild._oRight = oNode;
        oNode._oParent = oChild;
    }

    private static final RBNode rbInsert(TreeMap oTree, Comparator oComparator, RBNode oNode) {
        RBNode oUncle;
        RBNode oResult = treeInsert(oTree, oComparator, oNode);
        if (oResult == NIL) {
            oNode._iColor = RED;
            while ((oNode != oTree._oRoot) && (oNode._oParent._iColor == RED)) {
                if (oNode._oParent == oNode._oParent._oParent._oRight) {
                    oUncle = oNode._oParent._oParent._oRight;
                    if (oUncle._iColor == RED) {
                        oNode._oParent._iColor = BLACK;
                        oUncle._iColor = BLACK;
                        oNode._oParent._oParent._iColor = RED;
                        oNode = oNode._oParent._oParent;
                    } else {
                        if (oNode == oNode._oParent._oRight) {
                            oNode = oNode._oParent;
                            leftRotate(oTree, oNode);
                        }
                        oNode._oParent._iColor = BLACK;
                        oNode._oParent._oParent._iColor = RED;
                        rightRotate(oTree, oNode._oParent._oParent);
                    }
                } else {
                    oUncle = oNode._oParent._oParent._oLeft;
                    if (oUncle._iColor == RED) {
                        oNode._oParent._iColor = BLACK;
                        oUncle._iColor = BLACK;
                        oNode._oParent._oParent._iColor = RED;
                        oNode = oNode._oParent._oParent;
                    } else {
                        if (oNode == oNode._oParent._oLeft) {
                            oNode = oNode._oParent;
                            rightRotate(oTree, oNode);
                        }
                        oNode._oParent._iColor = BLACK;
                        oNode._oParent._oParent._iColor = RED;
                        leftRotate(oTree, oNode._oParent._oParent);
                    }
                }
            }
        }
        oTree._oRoot._iColor = BLACK;
        return oResult;
    }

    private static final RBNode rbDelete(TreeMap oTree, RBNode oNode) {
        RBNode oSplice;
        RBNode oChild;
        RBNode oSentinelParent = NIL;
        RBNode oResult = oNode;
        oSplice = (((oNode._oLeft == NIL) || (oNode._oRight == NIL)) ? oNode : treeSuccessor(oNode));
        oChild = (oSplice._oLeft != NIL) ? oSplice._oLeft : oSplice._oRight;
        oChild._oParent = oSplice._oParent;
        if (oSplice._oParent == NIL) oTree._oRoot = oChild; else if (oSplice == oSplice._oParent._oLeft) oSplice._oParent._oLeft = oChild; else oSplice._oParent._oRight = oChild;
        if (oSplice != oNode) {
            oResult = new RBNode(oNode.getKey(), oNode.getValue());
            oNode.key = oSplice.key;
            oNode.value = oSplice.value;
        }
        if (oSplice._iColor == BLACK) rbDeleteFixup(oTree, oChild);
        return oResult;
    }

    private static final void rbDeleteFixup(TreeMap oTree, RBNode oNode) {
        RBNode oSibling;
        while ((oNode != oTree._oRoot) && (oNode._iColor == BLACK)) {
            if (oNode == oNode._oParent._oLeft) {
                oSibling = oNode._oParent._oRight;
                if (oSibling._iColor == RED) {
                    oSibling._iColor = BLACK;
                    oNode._oParent._iColor = RED;
                    leftRotate(oTree, oNode._oParent);
                    oSibling = oNode._oParent._oRight;
                }
                if ((oSibling._oLeft._iColor == BLACK) && (oSibling._oRight._iColor == BLACK)) {
                    oSibling._iColor = RED;
                    oNode = oNode._oParent;
                } else {
                    if (oSibling._oRight._iColor == BLACK) {
                        oSibling._oLeft._iColor = BLACK;
                        oSibling._iColor = RED;
                        rightRotate(oTree, oSibling);
                        oSibling = oNode._oParent._oRight;
                    }
                    oSibling._iColor = oNode._oParent._iColor;
                    oNode._oParent._iColor = BLACK;
                    oSibling._oRight._iColor = BLACK;
                    leftRotate(oTree, oNode._oParent);
                    oNode = oTree._oRoot;
                }
            } else {
                oSibling = oNode._oParent._oLeft;
                if (oSibling._iColor == RED) {
                    oSibling._iColor = BLACK;
                    oNode._oParent._iColor = RED;
                    rightRotate(oTree, oNode._oParent);
                    oSibling = oNode._oParent._oLeft;
                }
                if ((oSibling._oRight._iColor == BLACK) && (oSibling._oLeft._iColor == BLACK)) {
                    oSibling._iColor = RED;
                    oNode = oNode._oParent;
                } else {
                    if (oSibling._oLeft._iColor == BLACK) {
                        oSibling._oRight._iColor = BLACK;
                        oSibling._iColor = RED;
                        leftRotate(oTree, oSibling);
                        oSibling = oNode._oParent._oLeft;
                    }
                    oSibling._iColor = oNode._oParent._iColor;
                    oNode._oParent._iColor = BLACK;
                    oSibling._oLeft._iColor = BLACK;
                    rightRotate(oTree, oNode._oParent);
                    oNode = oTree._oRoot;
                }
            }
        }
        oNode._iColor = BLACK;
    }

    private static class RBNode extends BasicMapEntry implements Map.Entry {

        int _iColor;

        RBNode _oLeft;

        RBNode _oRight;

        RBNode _oParent;

        RBNode(Object oKey, Object oValue) {
            super(oKey, oValue);
            _oLeft = NIL;
            _oRight = NIL;
            _oParent = NIL;
            _iColor = BLACK;
        }
    }

    private class TreeMapSet extends AbstractSet implements Set {

        SortedMap _oMap;

        int _iType;

        TreeMapSet(SortedMap oMap, int iType) {
            _oMap = oMap;
            _iType = iType;
        }

        /**
     * adding an element is unsupported; this method simply 
     * throws an exception 
     *
     * @throws       UnsupportedOperationException
     */
        public boolean add(Object oObject) throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        /**
     * adding an element is unsupported; this method simply throws 
     * an exception 
     *
     * @throws       UnsupportedOperationException
     */
        public boolean addAll(Collection oCollection) throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        /**
     * clears the backing TreeMap; this is a prime example of an 
     * overridden implementation which is far more efficient than 
     * its superclass implementation (which uses an iterator
     * and is O(n) -- this is an O(1) call)
     */
        public void clear() {
            _oMap.clear();
        }

        /**
     * returns true if the supplied object is contained by this Set
     *
     * @param     oObject  an Object being testing to see if it is in this Set
     */
        public boolean contains(Object oObject) {
            Map.Entry oEntry;
            Object oKey;
            Object oInputValue;
            Object oMapValue;
            if (_iType == KEYS) {
                return _oMap.containsKey(oObject);
            } else if (oObject instanceof Map.Entry) {
                oEntry = (Map.Entry) oObject;
                oKey = oEntry.getKey();
                if (_oMap.containsKey(oKey)) {
                    oInputValue = oEntry.getValue();
                    oMapValue = _oMap.get(oKey);
                    return ((oInputValue == null) ? (oMapValue == null) : (oInputValue.equals(oMapValue)));
                }
            }
            return false;
        }

        /** 
     * returns true if the backing Map is empty (which is the only 
     * case either a KEYS Set or an ENTRIES Set would be empty)
     */
        public boolean isEmpty() {
            return _oMap.isEmpty();
        }

        /**
     * removes the supplied Object from the Set
     *
     * @param      o       the Object to be removed
     */
        public boolean remove(Object oObject) {
            if (_iType == KEYS) return (_oMap.remove(oObject) != null); else return (oObject instanceof Map.Entry) ? (_oMap.remove(((Map.Entry) oObject).getKey()) != null) : false;
        }

        /** returns the size of this Set (always equal to the size of 
     *  the backing Hashtable) */
        public int size() {
            return _oMap.size();
        }

        /** returns an Iterator over the elements in this set */
        public Iterator iterator() {
            return new TreeMapIterator(_oMap, _iType);
        }
    }

    private class TreeMapCollection extends AbstractCollection implements Collection {

        private SortedMap _oMap;

        TreeMapCollection(SortedMap oMap) {
            _oMap = oMap;
        }

        /** 
     * adding elements is not supported by this Collection;
     * this method merely throws an exception
     *
     * @throws     UnsupportedOperationException
     */
        public boolean add(Object oObject) throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        /** 
     * adding elements is not supported by this Collection;
     * this method merely throws an exception
     *
     * @throws     UnsupportedOperationException
     */
        public boolean addAll(Collection oCollection) throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        /** removes all elements from this Collection (and from the 
     *  backing TreeMap) */
        public void clear() {
            _oMap.clear();
        }

        /** 
     * returns true if this Collection contains at least one Object 
     * which equals() the supplied Object
     *
     * @param         o        the Object to compare against those in the Set
     */
        public boolean contains(Object oObject) {
            return _oMap.containsValue(oObject);
        }

        /** returns true IFF the Collection has no elements */
        public boolean isEmpty() {
            return _oMap.isEmpty();
        }

        /** returns the size of this Collection */
        public int size() {
            return _oMap.size();
        }

        /** returns an Iterator over the elements in this Collection */
        public Iterator iterator() {
            return new TreeMapIterator(_oMap, VALUES);
        }
    }

    private class TreeMapIterator implements Iterator {

        SortedMap _oMap;

        int _iType;

        int _iKnownMods;

        RBNode _oFirst;

        RBNode _oLast;

        RBNode _oPrev;

        TreeMapIterator(SortedMap oMap, int iType) {
            TreeMap oBackingMap = TreeMap.this;
            _oMap = oMap;
            _iType = iType;
            _iKnownMods = oBackingMap._iModCount;
            _oPrev = NIL;
            if (_oMap.isEmpty()) {
                _oFirst = NIL;
            } else {
                _oFirst = TreeMap.treeSearch(oBackingMap._oRoot, oBackingMap.comparator, _oMap.firstKey());
                _oLast = TreeMap.treeSearch(oBackingMap._oRoot, oBackingMap.comparator, _oMap.lastKey());
            }
        }

        /** 
     * Stuart Ballard's code:  if the backing TreeMap has been altered 
     * through anything but <i>this</i> Iterator's <pre>remove()</pre> 
     * method, we will give up right here, rather than risking undefined 
     * behavior
     *
     * @throws    ConcurrentModificationException
     */
        private void checkMod() {
            if (_iKnownMods < TreeMap.this._iModCount) throw new ConcurrentModificationException();
        }

        public boolean hasNext() {
            checkMod();
            return (_oFirst != NIL);
        }

        public Object next() {
            checkMod();
            RBNode oResult = _oFirst;
            if (oResult == NIL) throw new NoSuchElementException(); else if (oResult == _oLast) _oFirst = NIL; else _oFirst = TreeMap.treeSuccessor(_oFirst);
            _oPrev = oResult;
            return ((_iType == KEYS) ? oResult.getKey() : ((_iType == VALUES) ? oResult.getValue() : oResult));
        }

        public void remove() {
            checkMod();
            Object oKey;
            if (_oPrev == NIL) {
                throw new IllegalStateException("No previous call to next(), " + "or remove() has already been " + "called on this iteration.");
            } else {
                oKey = _oPrev.getKey();
                if (_oMap.containsKey(oKey)) {
                    _oMap.remove(oKey);
                    _iKnownMods++;
                }
                _oPrev = NIL;
            }
        }
    }

    private class SubTreeMap extends AbstractMap implements SortedMap {

        Object _oMinKey;

        Object _oMaxKey;

        SubTreeMap(Object oMinKey, Object oMaxKey) {
            _oMinKey = oMinKey;
            _oMaxKey = oMaxKey;
        }

        public void clear() {
            Object oMaxKey;
            RBNode oMin = TreeMap.lowerBound(TreeMap.this._oRoot, TreeMap.this.comparator, _oMinKey, _oMaxKey);
            RBNode oMax = TreeMap.upperBound(TreeMap.this._oRoot, TreeMap.this.comparator, _oMinKey, _oMaxKey);
            oMaxKey = oMax.getKey();
            while ((oMin != NIL) && ((oMax == NIL) || (TreeMap.compare(TreeMap.this.comparator, oMin.getKey(), oMaxKey) < 0))) {
                TreeMap.this.remove(oMin.getKey());
                oMin = TreeMap.treeSuccessor(oMin);
            }
        }

        public boolean containsKey(Object oKey) {
            return (keyInRange(TreeMap.this.comparator, oKey, _oMinKey, _oMaxKey) && TreeMap.this.containsKey(oKey));
        }

        public boolean containsValue(Object oValue) {
            Object oCurrentValue;
            Object oMaxKey;
            RBNode oMin = TreeMap.lowerBound(TreeMap.this._oRoot, TreeMap.this.comparator, _oMinKey, _oMaxKey);
            RBNode oMax = TreeMap.upperBound(TreeMap.this._oRoot, TreeMap.this.comparator, _oMinKey, _oMaxKey);
            oMaxKey = oMax.getKey();
            while ((oMin != NIL) && ((oMax == NIL) || (TreeMap.compare(TreeMap.this.comparator, oMin.getKey(), oMaxKey) < 0))) {
                oCurrentValue = oMin.getValue();
                if (((oValue == null) && (oCurrentValue == null)) || oValue.equals(oCurrentValue)) return true;
                oMin = treeSuccessor(oMin);
            }
            return false;
        }

        public Object get(Object oKey) {
            if (keyInRange(TreeMap.this.comparator, oKey, _oMinKey, _oMaxKey)) return TreeMap.this.get(oKey); else return null;
        }

        public Object put(Object oKey, Object oValue) {
            if (keyInRange(TreeMap.this.comparator, oKey, _oMinKey, _oMaxKey)) return TreeMap.this.put(oKey, oValue); else throw new IllegalArgumentException(getArgumentError("insert an entry", _oMinKey, _oMaxKey));
        }

        public void putAll(Map oMap) {
            Map.Entry oEntry;
            Iterator itElements = oMap.entrySet().iterator();
            while (itElements.hasNext()) {
                oEntry = (Map.Entry) itElements.next();
                put(oEntry.getKey(), oEntry.getValue());
            }
        }

        public Object remove(Object oKey) {
            if (keyInRange(TreeMap.this.comparator, oKey, _oMinKey, _oMaxKey)) return TreeMap.this.remove(oKey); else throw new IllegalArgumentException(getArgumentError("remove an entry", _oMinKey, _oMaxKey));
        }

        public int size() {
            int iCount = 0;
            Object oMaxKey;
            RBNode oMin = TreeMap.lowerBound(TreeMap.this._oRoot, TreeMap.this.comparator, _oMinKey, _oMaxKey);
            RBNode oMax = TreeMap.upperBound(TreeMap.this._oRoot, TreeMap.this.comparator, _oMinKey, _oMaxKey);
            oMaxKey = oMax.getKey();
            while ((oMin != NIL) && ((oMax == NIL) || (TreeMap.compare(TreeMap.this.comparator, oMin.getKey(), oMaxKey) < 0))) {
                iCount++;
                oMin = TreeMap.treeSuccessor(oMin);
            }
            return iCount;
        }

        public Set entrySet() {
            return new TreeMapSet(this, ENTRIES);
        }

        public Set keySet() {
            return new TreeMapSet(this, KEYS);
        }

        public Collection values() {
            return new TreeMapCollection(this);
        }

        public Comparator comparator() {
            return TreeMap.this.comparator;
        }

        public Object firstKey() {
            RBNode oFirst = TreeMap.lowerBound(TreeMap.this._oRoot, TreeMap.this.comparator, _oMinKey, _oMaxKey);
            return (oFirst != NIL) ? oFirst.getKey() : null;
        }

        public Object lastKey() {
            RBNode oLast;
            if (_oMaxKey == null) {
                oLast = TreeMap.treeMax(TreeMap.this._oRoot);
                return (oLast != NIL) ? oLast.getKey() : null;
            } else {
                oLast = TreeMap.treeMaxConstrained(TreeMap.this._oRoot, TreeMap.this.comparator, _oMinKey, _oMaxKey);
                return (oLast != NIL) ? TreeMap.treePredecessor(oLast).getKey() : null;
            }
        }

        public SortedMap subMap(Object oFromKey, Object oToKey) {
            if ((compare(comparator, oFromKey, oToKey) < 0) && keyInMinRange(TreeMap.this.comparator, oFromKey, _oMinKey) && keyInClosedMaxRange(TreeMap.this.comparator, oFromKey, _oMaxKey) && keyInMinRange(TreeMap.this.comparator, oToKey, _oMinKey) && keyInClosedMaxRange(TreeMap.this.comparator, oToKey, _oMaxKey)) return new SubTreeMap(oFromKey, oToKey); else throw new IllegalArgumentException(getArgumentError("create a subMap", _oMinKey, _oMaxKey));
        }

        public SortedMap headMap(Object oToKey) {
            if (keyInMinRange(TreeMap.this.comparator, oToKey, _oMinKey) && keyInClosedMaxRange(TreeMap.this.comparator, oToKey, _oMaxKey)) return new SubTreeMap(_oMinKey, oToKey); else throw new IllegalArgumentException(getArgumentError("create a subMap", _oMinKey, _oMaxKey));
        }

        public SortedMap tailMap(Object oFromKey) {
            if (keyInMinRange(TreeMap.this.comparator, oFromKey, _oMinKey) && keyInClosedMaxRange(TreeMap.this.comparator, oFromKey, _oMaxKey)) return new SubTreeMap(oFromKey, _oMaxKey); else throw new IllegalArgumentException(getArgumentError("create a subMap", _oMinKey, _oMaxKey));
        }
    }
}
