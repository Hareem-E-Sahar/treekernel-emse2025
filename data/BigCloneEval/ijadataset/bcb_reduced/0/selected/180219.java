package com.daffodilwoods.daffodildb.server.datasystem.btree;

import com.daffodilwoods.daffodildb.server.datasystem.indexsystem.*;
import com.daffodilwoods.daffodildb.server.datasystem.interfaces.*;
import com.daffodilwoods.daffodildb.utils.byteconverter.*;
import com.daffodilwoods.daffodildb.utils.comparator.*;
import com.daffodilwoods.database.resource.*;
import com.daffodilwoods.database.utility.P;

/**
 *
 * <p>Title: BTreeNode</p>
 * <p>Description: BTreeNode : BTreeNode represents a single node of Balanced Tree.A Btree can have any
 * number of nodes.Node Size of a BTree is constant which is decided at the time of database creation.
 * All the nodes are managed by the BTree with which they are related.
 *
 * Must Know :
 * 1.	Every Node can contain 2^n-1 elements ( Optimized result ) at most.
 * 2.	Every node except root node has a parent node and a parent element.
 * 3.	Every node has a dummy element which is the very first element of the node.
 * 4.	Node can not have any left or right child.</p>
 */
public class BTreeNode {

    private _Index btree;

    /**
     * memory node or file Node
     */
    private _Node node;

    private _NodeManager nodeManager;

    private BTreeElement parentElement;

    private boolean isNodeRemovedFromMap = false;

    public BTreeNode(_Index btree, _Node node1) throws DException {
        this.btree = btree;
        node = node1;
        nodeManager = ((BTree) btree).getNodeManager();
    }

    public void insertDummyElement(BTreeElement element) throws DException {
        element.setPosition(0);
        node.insertDummyElement(element);
    }

    /**
     * insert key,value pair in this node at the proper position.
     */
    public int insert(_DatabaseUser user, Object key, Object value, int pos) throws DException {
        try {
            int position = pos == -1 ? binarySearch(key) : pos;
            if (position < 0) position = Math.abs(position);
            position++;
            node.updateNode(position, false);
            BTreeElement btreeElement = createBTreeElement(user, key, value, null, null);
            btreeElement.setCurrentNode(this);
            btreeElement.setPosition(position);
            node.insert(position, btreeElement, true);
            node.updateElementCount(true);
            return position;
        } catch (DException de) {
            if (de.getDseCode().equals("DSE2044")) {
                return -1;
            }
            throw de;
        }
    }

    Object getKey() {
        return this;
    }

    public void delete(_DatabaseUser user, int position) throws DException {
        node.delete(position);
    }

    public void update(_DatabaseUser user, BTreeKey btreekey, Object newKey, Object newValue) throws DException {
        node.updateBtree(btreekey, newKey, newValue);
    }

    /**
     * Returns parentNode of this node
     */
    public BTreeNode getParentNode(_DatabaseUser user) throws DException {
        Object nodeKey = node.getParentNode(user);
        return nodeKey == null ? null : nodeManager.getNode(user, nodeKey);
    }

    /**
     * Returns element at required position of Node
     *
     * @param index Position on which element From node is required
     * @return element at required position of Node
     */
    public BTreeElement getElement(int index, _DatabaseUser user) throws DException {
        BTreeElement ele = node.getElement(index);
        ele.setCurrentNode(this);
        ele.setPosition(index);
        return ele;
    }

    public int binarySearch(Object key) throws DException {
        return binarySearch(key, 1, node.getElementCount() - 1);
    }

    private int binarySearch(Object key, int low, int high) throws DException {
        int position = -1;
        SuperComparator comparator = btree.getComparator();
        while (low <= high) {
            int mid = (low + high) / 2;
            int cmp;
            cmp = comparator.compare(getKey(mid), key);
            if (cmp < 0) low = mid + 1; else if (cmp > 0) high = mid - 1; else {
                position = mid;
                if (!btree.getDuplicateAllowed()) break;
                low = mid + 1;
            }
        }
        return position == -1 ? -(low - 1) : position;
    }

    public String toString() {
        try {
            int count = getElementCount();
            String str = getNodeKey() + " ISLEAF NODE :: " + isLeaf() + " Element Count " + count;
            for (int i = 0; i < count; i++) {
                BTreeElement element = getElement(i, null);
                str += "[" + element + "]";
            }
            str += " prev node :: " + (getPreviousNode(null) == null ? null : getPreviousNode(null).getNodeKey());
            str += " next node :: " + (getNextNode(null) == null ? null : getNextNode(null).getNodeKey());
            return str;
        } catch (DException ex) {
        }
        return null;
    }

    SuperComparator getComparator() {
        return btree.getComparator();
    }

    public short getLevel() {
        return node.getLevel();
    }

    public void setLevel(short level0) throws DException {
        node.setLevel(level0);
    }

    BTreeElement createBTreeElement(_DatabaseUser user, Object key, Object value, BTreeNode leftNode, BTreeNode rightNode) throws DException {
        return node.createElement(node.isLeafNode(), user, key, value, leftNode, rightNode);
    }

    public int getElementCount() throws DException {
        return node.getElementCount();
    }

    public int getSplitPoint() throws DException {
        return node.getSplitPoint();
    }

    public void insertRange(Object elements, int startPosition, int endPosition) throws DException {
        node.insertRange(elements, startPosition, endPosition);
    }

    public void deleteRange(int startPosition, int endPosition) throws DException {
        node.deleteRange(startPosition, endPosition);
    }

    public Object getElements() throws DException {
        return node.getElements();
    }

    public int getFirstValidPosition() throws DException {
        if (node.getElementCount() > 1) return 1; else throw StaticExceptions.NO_VALID_KEY;
    }

    /**
     * returns next valid position in this node from given position if this node does not have any valid
     * position after given position then it throws an Exception.
     * @param position after which next valid position from node has to give
     * @return next valid position in node from given position
     * @throws DException if no valid position exists after given position
     */
    public int getNextValidPosition(int position) throws DException {
        int nextvalidPosition = position + 1;
        if (nextvalidPosition < node.getElementCount()) return nextvalidPosition; else throw StaticExceptions.NO_VALID_KEY;
    }

    /**
     * Returns last valid position in this node, if there is no element in node then it throws DException
     * @return last valid position in this node
     * @throws DException there is no valid position if there is no element in node
     */
    public int getLastValidPosition() throws DException {
        int elementCount = node.getElementCount();
        if (elementCount > 1) return elementCount - 1; else throw StaticExceptions.NO_VALID_KEY;
    }

    /**
     * returns previous valid position in this node from given position if this node does not have any
     * valid position before given position then it throws an Exception.
     *
     * @param position after which previous valid position from node has to give
     * @return previous valid position in node from given position
     * @throws DException if no valid position exists before given position
     */
    public int getPreviousValidPosition(int position) throws DException {
        int previousPosition = position - 1;
        if (previousPosition > 0) return previousPosition; else throw StaticExceptions.NO_VALID_KEY;
    }

    /**
     * returns next node of this node if exists otherwise returns false
     * @param user
     * @return next node of this node if exists otherwise returns false
     */
    public BTreeNode getNextNode(_DatabaseUser user) throws DException {
        Object nextNode = node.getNextNode(user);
        return nextNode == null ? null : nodeManager.getNode(user, nextNode);
    }

    /**
     * returns previous node of this node if exists otherwise returns false
     * @param user
     * @return previous node of this node if exists otherwise returns false
     */
    public BTreeNode getPreviousNode(_DatabaseUser user) throws DException {
        Object nodeKey = node.getPreviousNode(user);
        return nodeKey == null ? null : nodeManager.getNode(user, nodeKey);
    }

    public Object getNodeKey() throws DException {
        return node.getNodeKey();
    }

    public void setNextNode(BTreeNode node0) throws DException {
        node.setNextNode(node0);
    }

    public void setPreviousNode(BTreeNode node0) throws DException {
        node.setPreviousNode(node0);
    }

    public void setLeafNode(boolean flag) throws DException {
        node.setLeafNode(flag);
    }

    public boolean isLeaf() throws DException {
        return node.isLeafNode();
    }

    public void setParentNode(BTreeNode node0) throws DException {
        node.setParentNode(node0);
    }

    public void updateChild(int position, Object key) throws DException {
        node.updateChild(position, key);
    }

    public _Node getNode() {
        return node;
    }

    CbCzufIboemfs[] getByteHandlers() throws DException {
        return nodeManager.getByteHandlers();
    }

    public Object getKey(int index) throws DException {
        return node.getKey(index);
    }

    public Object getChildNodeKey(int index) throws DException {
        return node.getChildNodeKey(index);
    }

    public Object getValue(int index) throws DException {
        return node.getValue(index);
    }

    public int[] getColumnTypes() throws DException {
        return nodeManager.getColumnTypes();
    }

    public void finalize1() {
        if (node instanceof FixedFileNode || node instanceof VariableFileNode) ;
    }

    public void setParentElement(BTreeElement ele0) throws DException {
        parentElement = ele0;
    }

    public FileNodeManager getNodeManager() {
        return (FileNodeManager) nodeManager;
    }

    public void clearReferences() {
        if (parentElement != null) {
            parentElement.clearChild();
            parentElement = null;
        }
    }

    public boolean isNodeRemovedFromMap() {
        return isNodeRemovedFromMap;
    }

    public void setFlagForNodeRemovedFromMap() {
        isNodeRemovedFromMap = true;
    }

    public void setIsNodeCanBeRemovedFromMap(boolean isNodeCanBeRemovedFromMap) {
        node.setIsNodeCanBeRemovedFromMap(isNodeCanBeRemovedFromMap);
    }

    public boolean isNodeCanBeRemovedFromMap() {
        return node.isNodeCanBeRemovedFromMap();
    }
}
