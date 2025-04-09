package com.daffodilwoods.daffodildb.server.datasystem.btree;

import com.daffodilwoods.daffodildb.server.datasystem.indexsystem.*;
import com.daffodilwoods.daffodildb.server.datasystem.interfaces.*;
import com.daffodilwoods.daffodildb.server.datasystem.persistentsystem.*;
import com.daffodilwoods.daffodildb.server.sql99.utils.*;
import com.daffodilwoods.daffodildb.utils.*;
import com.daffodilwoods.daffodildb.utils.byteconverter.*;
import com.daffodilwoods.daffodildb.utils.comparator.*;
import com.daffodilwoods.database.resource.*;
import com.daffodilwoods.database.utility.*;

/**
 *
 * <p>Title: BTree</p>
 * <p>Description:BTree : BTree represents a complete Balanced Tree.Sometimes user of index system needs an iterator which can iterate on
 * corresponding data in order. So keeping this requirement in mind index system returns an iterator which
 * is known as IndexIterator.IndexIterator does it's job with the help of BTree.It gives all the
 * functionality which u can expect like insert,update,delete,seeking etc.
 </p>
 */
public class BTree implements _Index {

    /**
   * Comparator to compare BtreeKeys.Comparator is necessary for maintaing the elements in order.
   */
    private SuperComparator comparator;

    /**
   * Number of elements in BTree
   */
    private _IndexInformation indexInformation;

    /**
   * order of columns on which index is created
   */
    private boolean[] orderType;

    /**
   * whether duplicate key,value pairs can exist in BTree or not
   */
    public boolean DUPLICATEKEYSALLOWED = true;

    /**
   * characteristics of btree
   */
    private _BTreeCharacteristics btreeCharacteristics;

    /**
   * To provide new node or any existing node of Btree
   */
    private _NodeManager nodeManager;

    /**
   *
   */
    Object[] uniqueColumnReferences;

    public BTree(SuperComparator comparator0, _BTreeCharacteristics btreeCharacteristics0, _NodeManager nodeManger0) throws DException {
        comparator = comparator0;
        btreeCharacteristics = btreeCharacteristics0;
        nodeManager = nodeManger0;
        nodeManager.setBTree(this);
    }

    public _IndexInformation getIndexInformation() throws DException {
        return indexInformation;
    }

    /**
   * Sets IndexInformation of Btree
   *
   * @param indexInformation0 IndexInformation of Btree
   */
    public void setIndexInformation(_IndexInformation indexInformation0) throws DException {
        indexInformation = indexInformation0;
        orderType = indexInformation.getOrderOfColumns();
        uniqueColumnReferences = new Object[2];
        uniqueColumnReferences[0] = indexInformation.getColumnIndexes();
        uniqueColumnReferences[1] = indexInformation.getColumns();
    }

    /**
   * Facts And Terminology :
   * 1. BTree Node : Node in which new pair  should reside.
   * 2.If pair is the very first pair for this BTree then create a node newNode and
   * assign it as root node and insert in it.
   *
   * As we know that insert in btree is performed at ground level.So we must have to
   * locate the ground node first.For this starting from rootNode we move downwards
   * using binarysearch until we reach the appropriate ground node.
   *
   * Now create a new Btree Element with the key Value pair.
   *
   * Every Node has an array of it's elements.Elements in a node reside in the
   * appropriate order.Insert in a BTreeNode means place the element in elements
   * array at the appropriate location.
   *
   * In Case Of Breaking Of Node :
   * Some times BTree Node is full means It has no space for new pair.In this case these steps
   * are followed :
   *
   * 1. A new node is created by the BTree.
   * 2. Shift all the right half elements in new node from the node pair should reside.Right Half elements
   *    means all those elements which are at right side of middle element and middle element also.
   * 3. Compare the new pair with middle element.
   * 4. After the comparison insert the new pair in appropriate node whether in BTree Node or New Node.
   * 5. And Now insert the middle element in parent of BTree Node.
   *
   * Special handling is done in it for the nodes which are used in insert method
   * if any spliting occures than we set a flag false for the used nodes so that
   * they can't be removed while we are loading child nodes of splited non leaf node
   * than during updateNodeMapping nodes can be removed from the map .
   * Now after setting all information of that node its flag is reseted to true
   * so that it can be removed from map now.
   */
    public _IndexKey insert(_DatabaseUser user, Object key, Object value) throws DException {
        BTreeNode btreeNode = null;
        BTreeNode rootNode = null;
        BTreeKey btKey = null;
        int insertPosition = -1;
        if ((rootNode = nodeManager.getRootNode(user)) == null) {
            btreeNode = rootNode = nodeManager.getNewNode(user);
            BTreeElement element = new BTreeElement();
            element.setCurrentNode(btreeNode);
            rootNode.insertDummyElement(element);
            btreeNode.setLevel((short) 1);
            btKey = new BTreeKey(btreeNode, 0);
        } else {
            btKey = locateNode(user, key, value, rootNode);
            btreeNode = btKey.getNode();
        }
        BTreeNode newRootNode = rootNode;
        BTreeNode newNode = null;
        BTreeNode rightNode = null;
        int posa = btreeNode.insert(user, key, value, btKey.getPosition());
        if (posa != -1) {
            nodeManager.updateSizeAndBTreeInfo(user, true, rootNode);
            BTreeKey btreeKey = new BTreeKey();
            btreeKey.setNodeAndPosition(btreeNode, posa);
            return btreeKey;
        }
        BTreeKey temp = null;
        boolean flag = true;
        BTreeElement dummyElement, middleElement;
        BTreeNode parentNode;
        int splitPoint, cmp, pos;
        int testElementCount;
        while (posa == -1) {
            btreeNode.setIsNodeCanBeRemovedFromMap(false);
            newNode = nodeManager.getNewNode(user);
            newNode.setIsNodeCanBeRemovedFromMap(false);
            newNode.setLevel(btreeNode.getLevel());
            dummyElement = new BTreeElement();
            dummyElement.setCurrentNode(btreeNode);
            newNode.insertDummyElement(dummyElement);
            splitPoint = getSplitPoint(btreeNode);
            testElementCount = btreeNode.getElementCount();
            middleElement = btreeNode.getElement(splitPoint, user);
            shiftRightHalf(user, btreeNode, newNode, splitPoint);
            cmp = comparator.compare(middleElement.getKey(), key);
            pos = -1;
            if (!flag) {
                btreeNode.setLeafNode(false);
                newNode.setLeafNode(false);
            }
            pos = cmp > 0 ? btreeNode.insert(user, key, value, -1) : newNode.insert(user, key, value, -1);
            if (!flag) {
                BTreeNode nod = cmp > 0 ? btreeNode : newNode;
                BTreeElement bt = nod.getElement(pos, user);
                bt.updateChild(rightNode);
                rightNode.setIsNodeCanBeRemovedFromMap(true);
            }
            parentNode = btreeNode.getParentNode(user);
            boolean isParentNode = false;
            if (parentNode == null) {
                isParentNode = true;
                newRootNode = parentNode = nodeManager.getNewNode(user);
                newRootNode.setIsNodeCanBeRemovedFromMap(false);
                parentNode.setLevel((short) (btreeNode.getLevel() + 1));
                BTreeElement element = new BTreeElement();
                element.setCurrentNode(parentNode);
                element.setChild(btreeNode);
                parentNode.insertDummyElement(element);
                parentNode.setLeafNode(false);
            }
            if (flag) {
                flag = false;
                temp = new BTreeKey();
                if (cmp > 0) {
                    if (pos < 0) {
                        ;
                    }
                    temp.setNodeAndPosition(btreeNode, pos);
                } else temp.setNodeAndPosition(newNode, pos);
            }
            if (DUPLICATEKEYSALLOWED && !isParentNode) {
                insertPosition = parentNode.binarySearch(key);
            }
            key = middleElement.getKey();
            value = middleElement.getValue();
            btreeNode.setIsNodeCanBeRemovedFromMap(true);
            btreeNode = parentNode;
            posa = btreeNode.insert(user, key, value, isParentNode ? 0 : insertPosition);
            if (posa == -1) {
                rightNode = newNode;
                rightNode.setIsNodeCanBeRemovedFromMap(false);
            } else {
                BTreeElement ele = btreeNode.getElement(posa, user);
                ele.updateChild(newNode);
            }
            newNode.setIsNodeCanBeRemovedFromMap(true);
        }
        rootNode = newRootNode;
        newRootNode.setIsNodeCanBeRemovedFromMap(true);
        nodeManager.updateSizeAndBTreeInfo(user, true, rootNode);
        return temp;
    }

    /**
   * if btree is Default then  update the btree element
   * with new{newKey,newValue} pair
   * else Delete the old {key,value} pair and insert new pair.
   * if both old and new {key,value} pairs are same,do nothing.
   *
   * if both old and new {key,value} pairs are same,do nothing.
   *
   * @param user _DatabaseUser
   * @param oldKey old Btree key made of old values of index columns
   * @param newKey new Btree key made of new values of index columns
   * @param oldValue TableKey of Record  Before Updation in File
   * @param newValue TableKey of Record After Updation in File
   * @throws DException
   */
    public void update(_DatabaseUser user, Object oldKey, Object newKey, Object oldValue, Object newValue) throws DException {
        int cmp = comparator.compare(oldKey, newKey);
        if (cmp == 0) {
            if (oldValue == null && newValue == null) return;
            if (oldValue != null && oldValue.equals(newValue)) return;
        }
        BTreeNode rootNode = nodeManager.getRootNode(user);
        BTreeKey obj = searchNode(rootNode, oldKey, oldValue);
        BTreeKey updateKey = (BTreeKey) obj.clone();
        BTreeNode btreenode = obj.getNode();
        boolean flag = true;
        if (next1(obj)) {
            Object nextKey = obj.getKey();
            if (comparator.compare(nextKey, newKey) > 0 && updateKey.getNode().hashCode() == obj.getNode().hashCode()) {
                previous1(obj);
                flag = true;
            } else flag = false;
        }
        if (flag) {
            if (previous1(obj)) {
                Object previousKey = obj.getKey();
                if (comparator.compare(previousKey, newKey) < 0 && updateKey.getNode().hashCode() == obj.getNode().hashCode()) {
                    update(user, btreenode, updateKey, oldKey, oldValue, newKey, newValue);
                    return;
                }
            } else {
                update(user, btreenode, updateKey, oldKey, oldValue, newKey, newValue);
                return;
            }
        }
        delete(user, updateKey, rootNode);
        insert(user, newKey, newValue);
    }

    private void update(_DatabaseUser user, BTreeNode btreenode, BTreeKey obj, Object oldKey, Object oldValue, Object newKey, Object newValue) throws DException {
        btreenode = getNode(user, btreenode.getNodeKey());
        try {
            btreenode.update(null, obj, newKey, newValue);
        } catch (DException ex) {
            if (ex.getDseCode().equalsIgnoreCase("DSE2001")) {
                delete(user, oldKey, oldValue);
                insert(user, newKey, newValue);
            } else throw ex;
        }
    }

    /**
   * Searches the position of  {key,value} on Lowest level of Btree, If {key, value} pair found then
   * deletes element having that key value pair from btree.otherwise throws exception that key value
   * pair not found in Btree.
   *
   * @param key key of element which has to delete
   * @param value value of element which has to delete
   * @return BtreeKey which is deleted from Btree
   */
    public _IndexKey delete(_DatabaseUser user, Object key, Object value) throws DException {
        BTreeNode rootNode = nodeManager.getRootNode(user);
        BTreeKey obj = searchNode(rootNode, key, value);
        if (obj == null) {
            String we = "";
            Object key1 = null;
            try {
                CbCzufIboemfs[] handlers = nodeManager.getByteHandlers();
                key1 = getObject(key, handlers, nodeManager.getColumnTypes());
                we = P.print(key1);
            } catch (Exception E) {
                we = "" + key1;
            }
            obj = searchNode(rootNode, key, value);
            throw new DException("DSE2008", new Object[] { we, value });
        }
        return delete(user, obj, rootNode);
    }

    /**
   * Gets node from which key is to be deleted, deletes key from node and updates total btree size.
   *
   * @param key key which is to be deleted
   * @return btreekey which is deleted
   */
    public _IndexKey delete(_DatabaseUser user, _IndexKey key) throws DException {
        return delete(user, (BTreeKey) key, nodeManager.getRootNode(user));
    }

    /**
   * Used while insertin a new pair.It returns the appropriate leaf node in which this pair should reside.
   */
    private BTreeKey locateNode(_DatabaseUser user, Object key, Object value, BTreeNode rootNode) throws DException {
        BTreeNode node = rootNode;
        BTreeNode locatedNode = null;
        int pos = -1;
        do {
            if (node.getElementCount() < 2 && node.isLeaf()) {
                return new BTreeKey(node, 0);
            }
            locatedNode = node;
            pos = node.binarySearch(key);
            if (pos < 0) {
                pos = Math.abs(pos);
            }
            node = node.isLeaf() ? null : getNode(user, node.getChildNodeKey(pos));
        } while (node != null);
        return new BTreeKey(locatedNode, pos);
    }

    public void showBTree() throws DException {
        BTreeNode rootNode = nodeManager.getRootNode(null);
        if (rootNode == null || rootNode.getElementCount() == 1) {
            ;
            return;
        }
        showBTree(rootNode, 0);
    }

    private void showBTree(BTreeNode rootNode, int j) throws DException {
        if (rootNode == null) return;
        BTreeNode nod = rootNode.getParentNode(null);
        try {
        } catch (NullPointerException ex) {
        }
        if (rootNode.getElementCount() == 0) return;
        for (int i = 0; i < rootNode.getElementCount(); i++) {
            BTreeElement ele = rootNode.getElement(i, null);
            if (ele != null) {
                BTreeNode nodea = getNode(null, rootNode.getChildNodeKey(i));
                showBTree(nodea, j);
            }
        }
    }

    /**
   * In case of breaking of a node shifts the right half elements  into new node.and maintains linked
   * list among old and new node.
   *
   * @param oldNode old node from which elements has to shift
   * @param newNode new node in which elements has to insert
   * @param splitPoint splitpoint from where element of old node has to shift in new node.
   */
    private void shiftRightHalf(_DatabaseUser user, BTreeNode oldNode, BTreeNode newNode, int splitPoint) throws DException {
        int insertPosition = oldNode.isLeaf() ? splitPoint : splitPoint + 1;
        int deletePosition = splitPoint - 1;
        int endPosition = oldNode.getElementCount() - 1;
        newNode.insertRange(oldNode.getElements(), insertPosition, endPosition);
        if (!oldNode.isLeaf()) {
            BTreeElement ele;
            BTreeNode nd;
            for (int i = insertPosition; i <= endPosition; i++) {
                ele = oldNode.getElement(i, user);
                nd = nodeManager.getNode(user, ele.getChildNodeKey());
                if (nd != null) {
                    nd.setParentNode(newNode);
                }
            }
            ele = oldNode.getElement(splitPoint, user);
            nd = nodeManager.getNode(user, ele.getChildNodeKey());
            if (nd != null) {
                newNode.getElement(0, user).updateChild(nd);
            }
        }
        oldNode.deleteRange(0, deletePosition);
        BTreeNode nextNode = oldNode.getNextNode(user);
        if (nextNode != null) {
            newNode.setNextNode(nextNode);
            nextNode.setPreviousNode(newNode);
        }
        oldNode.setNextNode(newNode);
        newNode.setPreviousNode(oldNode);
    }

    /**
   * Method written below returns the position from where we have to split the node.
   *
   * Check the document "SplitPoint.doc"
   *
   * @param node : which is going to split
   * @return : position from where splitting begins
   */
    private int getSplitPoint(BTreeNode node) throws DException {
        int splitPoint = node.getSplitPoint();
        int startPosition = splitPoint;
        if (DUPLICATEKEYSALLOWED) {
            int count = node.getElementCount();
            Object key = node.getKey(startPosition);
            int a;
            int b;
            while (true) {
                a = startPosition + 1;
                if (a >= count) {
                    startPosition = splitPoint;
                    b = splitPoint - 1;
                    while (b > 0 && comparator.compare(key, node.getKey(b)) == 0) {
                        startPosition--;
                        b--;
                    }
                    if (startPosition == 1) {
                        return splitPoint;
                    }
                    return startPosition;
                }
                startPosition++;
                if (comparator.compare(key, node.getKey(a)) != 0) {
                    if (a == splitPoint + 1 && comparator.compare(key, node.getKey(splitPoint - 1)) != 0) {
                        return splitPoint;
                    }
                    return startPosition;
                }
            }
        }
        return startPosition;
    }

    /**
   * Return Names of Columns on which BTree is maintained
   */
    public String[] getColumnNames() throws DException {
        return indexInformation == null ? null : indexInformation.getColumns();
    }

    public int getHighestLevelofBtree() throws DException {
        return nodeManager.getRootNode(null).getLevel();
    }

    /**
   * returns comparator of BTree
   */
    public SuperComparator getComparator() {
        return comparator;
    }

    /**
   * Returns the first element of Btree which satisfies the condition.
   *
   * @param condition :
   * @return : BTreeElement if found any appropriate element otherwise null.
   */
    public Object seekFromTopRelative(_IndexPredicate[] condition) throws DException {
        if (getSize() == 0) return null;
        BTreeNode currentRootNode = nodeManager.getRootNode(null);
        return currentRootNode == null ? null : searchNode(currentRootNode, condition, new BTreeReader(btreeCharacteristics), true);
    }

    /**
   * Returns the last element of Btree which satisfies the condition.
   *
   * @param condition :
   * @return : BTreeElement if found any appropriate element otherwise null.
   */
    public Object seekFromBottomRelative(_IndexPredicate[] condition) throws DException {
        if (getSize() == 0) return null;
        BTreeNode currentRootNode = nodeManager.getRootNode(null);
        if (currentRootNode == null) return null;
        return searchNode(currentRootNode, condition, new BTreeReader(btreeCharacteristics), false);
    }

    private int search(BTreeNode cluster, _IndexPredicate[] condition, _VariableValues reader, int low, int high, boolean flag) throws DException {
        if (condition == null) return flag ? low : high;
        int cmp = 0;
        int position = -1;
        int length = 0;
        for (int i = 0; i < condition.length && condition[i] != null; i++, length++) ;
        Object object = null;
        while (low <= high) {
            int mid = (low + high) / 2;
            object = cluster.getKey(mid);
            ((BTreeReader) reader).setValue(object);
            cmp = evaluate1(condition, reader, length);
            if (cmp > 0) low = mid + 1; else if (cmp < 0) high = mid - 1; else {
                cmp = evaluate(condition, reader, length);
                if (cmp == 0) position = mid;
                if (flag) high = mid - 1; else low = mid + 1;
            }
        }
        return position == -1 ? -(low - 1) : position;
    }

    /**
   * returns First BTreeKey having given indexkey otherwise null
   * @param indexKey key which has to seek
   * @return First BTreeKey having given indexkey otherwise null
   */
    public Object seek(Object indexKey) throws DException {
        if (getSize() == 0) return null;
        boolean isPartial = false;
        if (getColumnNames().length == 1) {
            if (indexKey instanceof Object[]) indexKey = ((Object[]) indexKey)[0];
        } else {
            isPartial = ((Object[]) indexKey).length != getColumnNames().length;
        }
        BTreeNode currentRootNode = nodeManager.getRootNode(null);
        if (currentRootNode == null || currentRootNode.isLeaf() && currentRootNode.getElementCount() < 2) return null;
        return searchNode(currentRootNode, indexKey, false, true, isPartial);
    }

    private int search(BTreeNode cluster, Object keyToSeek, int low, int high, boolean flag) throws DException {
        int cmp = 0;
        int position = -1;
        Object object = null;
        while (low <= high) {
            int mid = (low + high) / 2;
            object = cluster.getKey(mid);
            cmp = comparator.compare(keyToSeek, object);
            if (cmp > 0) low = mid + 1; else if (cmp < 0) high = mid - 1; else {
                if (cmp == 0) position = mid;
                if (flag) high = mid - 1; else low = mid + 1;
            }
        }
        return position == -1 ? -(low - 1) : position;
    }

    /**
   * returns Btreekey having given indexkey or if not found then it's next key . it searches the key
   * downwards from the given currentkey.If there is no key having value equal or greater than indexkey
   * then it returns null.
   *
   * @param key from where indexkey has to search
   * @param indexKey which has to search
   * @return Btreekey having given indexkey or if not found then it's next key
   * @throws DException
   */
    public Object seekFromTopRelative(Object key, Object indexKey) throws DException {
        if (getSize() == 0) return null;
        indexKey = getColumnNames().length == 1 && indexKey instanceof Object[] ? ((Object[]) indexKey)[0] : indexKey;
        BTreeKey currentKey = (BTreeKey) key;
        boolean flag = currentKey == null ? first(currentKey = new BTreeKey()) : next(currentKey);
        while (flag) {
            if (comparator.compare(currentKey.getKey(), indexKey) >= 0) return currentKey;
            flag = next(currentKey);
        }
        return null;
    }

    /**
   * returns Btreekey having given indexkey or if not found then it's previous key . it searches the key
   * upwards from the given currentkey.If there is no key having value equal or less than indexkey
   * then it returns null.
   *
   * @param key from where indexkey has to search
   * @param indexKey which has to search
   * @return BtreeKey having given indexkey or if not found then it's previous key
   */
    public Object seekFromBottomRelative(Object key, Object indexKey) throws DException {
        if (getSize() == 0) return null;
        indexKey = getColumnNames().length == 1 && indexKey instanceof Object[] ? ((Object[]) indexKey)[0] : indexKey;
        BTreeKey currentKey = (BTreeKey) key;
        boolean flag = currentKey == null ? last(currentKey = new BTreeKey()) : previous(currentKey);
        while (flag) {
            comparator.compare(currentKey.getKey(), indexKey);
            if (comparator.compare(currentKey.getKey(), indexKey) <= 0) {
                return currentKey;
            }
            flag = previous(currentKey);
        }
        return null;
    }

    private int evaluate(_IndexPredicate[] conditions, _VariableValues reader, int start) throws DException {
        int cmp = 0;
        for (int i = start, length = conditions.length; cmp == 0 && i < length; i++) {
            cmp = conditions[i] == null ? 0 : conditions[i].run(reader).hashCode();
            cmp = (orderType == null || orderType[i]) ? cmp : -cmp;
        }
        return cmp;
    }

    private int evaluate1(_IndexPredicate[] conditions, _VariableValues reader, int stopPosition) throws DException {
        int cmp = 0;
        for (int i = 0; cmp == 0 && i < stopPosition; i++) {
            cmp = conditions[i].run(reader).hashCode();
            cmp = (orderType == null || orderType[i]) ? cmp : -cmp;
        }
        return cmp;
    }

    /**
   * 1. if top is true
   *
   * returns first Btreekey having given indexkey or if not found then it's next key. If there is no key
   * having value equal or greater than indexkey then it returns null.
   * 1. if top is false
   *
   * returns last Btreekey having given indexkey or if not found then it's previous key. If there is no
   * key having value equal or less than indexkey then it returns null.
   *
   * @param indexKey which has to locate
   * @param top direction in which key has to locate
   * @return BtreeKey having given indexkey or if not found then it's next or previous key on the basis
   * of given variable top.
   */
    public _IndexKey locateKey(Object indexKey, boolean top) throws DException {
        if (getSize() == 0) return null;
        String[] columnmNames = getColumnNames();
        boolean isPartial = false;
        if (columnmNames.length == 1) {
            if (indexKey instanceof Object[]) indexKey = ((Object[]) indexKey)[0];
        } else {
            isPartial = ((Object[]) indexKey).length != columnmNames.length;
        }
        BTreeKey entry = null;
        BTreeNode currentRootNode = nodeManager.getRootNode(null);
        if (currentRootNode == null || currentRootNode.isLeaf() && currentRootNode.getElementCount() < 2) return null;
        entry = searchNode(currentRootNode, indexKey, true, top, isPartial);
        if (entry.getPosition() <= 0) return locateNode(entry, top);
        return entry;
    }

    public _IndexKey insert(Object key, Object value) throws DException {
        return this.insert(null, key, value);
    }

    public void update(Object oldKey, Object newKey, Object oldValue, Object newValue) throws DException {
        update(null, oldKey, newKey, oldValue, newValue);
    }

    public _IndexKey delete(Object key, Object value) throws DException {
        return this.delete(null, key, value);
    }

    public Object seekAbsolute(Object key, Object indexKey) throws DException {
        if (getSize() == 0) return null;
        indexKey = getColumnNames().length == 1 && indexKey instanceof Object[] ? ((Object[]) indexKey)[0] : indexKey;
        BTreeKey currentKey = (BTreeKey) key;
        boolean flag = currentKey == null ? first(currentKey = new BTreeKey()) : next(currentKey);
        while (flag) {
            int comp = comparator.compare(currentKey.getKey(), indexKey);
            if (comp == 0) return currentKey; else flag = comp > 0 ? false : next(currentKey);
        }
        return null;
    }

    public int getSize() {
        return nodeManager.getSize();
    }

    /**
   * Sets first key of btree in parameter key, To get key first we have to reach on leftmost leaf node,
   * then first valid key of that node is required key, if there is no valid key in that node then first
   * valid key of next  node is required key, this process continues until we get key otherwise returns
   * false.
   *
   * @param key sets first key of btree in key
   * @return true if first key exists otherwise returns false
   */
    public boolean first(_IndexKey key0) throws DException {
        if (getSize() == 0) return false;
        BTreeKey key = (BTreeKey) key0;
        BTreeNode node = nodeManager.getRootNode(null);
        if (node == null) return false;
        while (!node.isLeaf()) {
            node = getNode(null, node.getChildNodeKey(0));
        }
        try {
            int position = node.getFirstValidPosition();
            key.setNodeAndPosition(node, position);
            return true;
        } catch (DException ex) {
            if (ex.getDseCode().equals("DSE2042") || ex.getDseCode().equals("DSE2043")) {
                node = node.getNextNode(null);
                while (node != null) {
                    try {
                        key.setNodeAndPosition(node, node.getFirstValidPosition());
                        return true;
                    } catch (DException ex1) {
                        node = node.getNextNode(null);
                    }
                }
                return false;
            }
            throw ex;
        }
    }

    /**
   * sets next key of given btreekey , first it checks that given key exists at given location or not.
   * if it exists then sets next valid key of this key in given key, if it does not exist then locates
   * this key in btree, locate return this key or its next key, if it finds that key at any other
   * location then returns its next key otherwise returns key got from locate key.
   * @param btreeKey current key whose next key has to set in btreekey
   * @return true if next key of current key exists otherwise returns false.
   */
    public boolean next(_IndexKey btreeKey0) throws DException {
        if (getSize() == 0) return false;
        BTreeKey btreeKey = (BTreeKey) btreeKey0;
        btreeKey.checkValidity();
        if (btreeKey.getPosition() == 0) {
            return next1(btreeKey);
        }
        Object oldKey = btreeKey.getOldkey();
        Object oldValue = btreeKey.getOldValue();
        BTreeElement element = null;
        Object newValue = null;
        try {
            BTreeNode node = btreeKey.getNode();
            if (node.isNodeRemovedFromMap()) node = nodeManager.getNode(null, node.getNodeKey());
            newValue = node.getValue(btreeKey.getPosition());
        } catch (DException de) {
            if (!de.getDseCode().equalsIgnoreCase("DSE2043")) throw de;
        }
        if (oldValue.equals(newValue)) {
            return next1(btreeKey);
        } else {
            BTreeKey btreeKeyAfterLocate = (BTreeKey) locateKey(oldKey, true);
            if (btreeKeyAfterLocate == null) {
                return false;
            }
            Object newKey = btreeKeyAfterLocate.getKey();
            newValue = btreeKeyAfterLocate.getValue();
            btreeKey.setNodeAndPosition(btreeKeyAfterLocate.getNode(), btreeKeyAfterLocate.getPosition());
            if (comparator.compare(oldKey, newKey) != 0) return true;
            BTreeKey clonedKey = (BTreeKey) btreeKey.clone();
            boolean foundOldRecord = false;
            while (comparator.compare(oldKey, newKey) == 0) {
                if (oldValue.equals(newValue)) {
                    foundOldRecord = true;
                    break;
                }
                if (next1(btreeKey) == false) break;
                newValue = btreeKey.getValue();
                newKey = btreeKey.getKey();
            }
            if (foundOldRecord) return next1(btreeKey); else {
                btreeKey.setNodeAndPosition(clonedKey.getNode(), clonedKey.getPosition());
                return true;
            }
        }
    }

    private boolean next1(BTreeKey key) throws DException {
        BTreeNode node = key.getNode();
        try {
            if (node.isNodeRemovedFromMap()) node = nodeManager.getNode(null, node.getNodeKey());
            int position = node.getNextValidPosition(key.getPosition());
            key.setNodeAndPosition(node, position);
            return true;
        } catch (DException de) {
            if (de.getDseCode().equals("DSE2042") || de.getDseCode().equals("DSE2043")) {
                node = node.getNextNode(null);
                while (node != null) {
                    try {
                        key.setNodeAndPosition(node, node.getFirstValidPosition());
                        return true;
                    } catch (DException ex) {
                        node = node.getNextNode(null);
                    }
                }
                return false;
            }
            throw de;
        }
    }

    /**
   * Sets last key of btree in parameter key, To get key first we have to reach on rightmost leaf node,
   * then last valid key of that node is required key, if there is no valid key in that node then last
   * valid key of previous  node is required key, this process continues until we get key otherwise
   * returns false.
   *
   * @param key sets last key of btree in key
   * @return true if last key exists otherwise returns false
   */
    public boolean last(_IndexKey key0) throws DException {
        if (getSize() == 0) return false;
        BTreeKey key = (BTreeKey) key0;
        BTreeNode node = nodeManager.getRootNode(null);
        if (node == null) return false;
        while (!node.isLeaf()) {
            node = getNode(null, node.getChildNodeKey(node.getElementCount() - 1));
        }
        try {
            int position = node.getLastValidPosition();
            key.setNodeAndPosition(node, position);
            return true;
        } catch (DException ex) {
            if (ex.getDseCode().equals("DSE2042") || ex.getDseCode().equals("DSE2043")) {
                node = node.getPreviousNode(null);
                while (node != null) {
                    try {
                        key.setNodeAndPosition(node, node.getLastValidPosition());
                        return true;
                    } catch (DException ex1) {
                        node = node.getPreviousNode(null);
                    }
                }
                return false;
            }
            throw ex;
        }
    }

    /**
   * sets previous key of given btreekey , first it checks that given key exists at given location or not.
   * if it exists then sets previous valid key of this key in given key, if it does not exist then
   * locates this key in btree, locate return this key or its previous key, if it finds that key at any
   * other location then returns its previous key otherwise returns key got from locate key.
   *
   * @param btreeKey current key whose previous key has to set in btreekey
   * @return true if previous key of current key exists otherwise returns false.
   */
    public boolean previous(_IndexKey btreeKey0) throws DException {
        if (getSize() == 0) return false;
        BTreeKey btreeKey = (BTreeKey) btreeKey0;
        Object oldKey = btreeKey.getOldkey();
        Object oldValue = btreeKey.getOldValue();
        Object newValue = null;
        try {
            BTreeNode node = btreeKey.getNode();
            if (node.isNodeRemovedFromMap()) node = nodeManager.getNode(null, node.getNodeKey());
            newValue = node.getValue(btreeKey.getPosition());
        } catch (DException de) {
            if (!de.getDseCode().equalsIgnoreCase("DSE2043")) throw de;
        }
        if (oldValue.equals(newValue)) return previous1(btreeKey); else {
            BTreeKey btreeKeyAfterLocate = (BTreeKey) locateKey(oldKey, false);
            if (btreeKeyAfterLocate == null) return false;
            Object newKey = btreeKeyAfterLocate.getKey();
            newValue = btreeKeyAfterLocate.getValue();
            btreeKey.setNodeAndPosition(btreeKeyAfterLocate.getNode(), btreeKeyAfterLocate.getPosition());
            if (comparator.compare(oldKey, newKey) != 0) return true;
            BTreeKey clonedKey = (BTreeKey) btreeKey.clone();
            boolean foundOldRecord = false;
            while (comparator.compare(oldKey, newKey) == 0) {
                if (oldValue.equals(newValue)) {
                    foundOldRecord = true;
                    break;
                }
                if (previous1(btreeKey) == false) break;
                newValue = btreeKey.getValue();
                newKey = btreeKey.getKey();
            }
            if (foundOldRecord) return previous1(btreeKey); else {
                btreeKey.setNodeAndPosition(clonedKey.getNode(), clonedKey.getPosition());
                return true;
            }
        }
    }

    private boolean previous1(BTreeKey key) throws DException {
        BTreeNode node = key.getNode();
        try {
            int position = 0;
            if (node.isNodeRemovedFromMap()) node = nodeManager.getNode(null, node.getNodeKey());
            position = node.getPreviousValidPosition(key.getPosition());
            key.setNodeAndPosition(node, position);
            return true;
        } catch (DException de) {
            if (de.getDseCode().equals("DSE2042") || de.getDseCode().equals("DSE2043")) {
                node = node.getPreviousNode(null);
                while (node != null) {
                    try {
                        key.setNodeAndPosition(node, node.getLastValidPosition());
                        return true;
                    } catch (DException ex) {
                        node = node.getPreviousNode(null);
                    }
                }
                return false;
            }
            throw de;
        }
    }

    public BTreeNode getRootNode() throws DException {
        return nodeManager.getRootNode(null);
    }

    public _NodeManager getNodeManager() throws DException {
        return nodeManager;
    }

    private BTreeKey getBTreeKey(BTreeNode node, int position) throws DException {
        BTreeKey bk = new BTreeKey();
        bk.setNodeAndPosition(node, position);
        return bk;
    }

    private static Object getObject(Object bb, CbCzufIboemfs[] handlers, int[] columnTypes) throws DException {
        if (bb instanceof BufferRange) {
            return ((BufferRange) bb).getNull() ? null : handlers[0].getObject((BufferRange) bb, columnTypes[0]);
        }
        BufferRange[] bytes = (BufferRange[]) bb;
        Object[] values = new Object[bytes.length];
        for (int i = 0; i < bytes.length; i++) values[i] = bytes[i] == null ? null : bytes[i].getNull() ? null : handlers[i].getObject(bytes[i], columnTypes[i]).getObject();
        return values;
    }

    protected BTreeNode getNode(_DatabaseUser user, Object childNodeKey) throws DException {
        return childNodeKey == null ? null : nodeManager.getNode(user, childNodeKey);
    }

    public Object[] getUniqueColumnReference() throws DException {
        return uniqueColumnReferences;
    }

    public int getTotalNumberOfClusterLoadedInMemory() {
        return ((FileNodeManager) nodeManager).getTotalSizeOfNodeMapAndWeakNodeMap();
    }

    public Object getObject(Object key) throws DException {
        CbCzufIboemfs[] handlers = nodeManager.getByteHandlers();
        return getObject(key, handlers, nodeManager.getColumnTypes());
    }

    protected void finalize123() {
    }

    /**
   * Simple search method which searches the key(which is always full) and value pair in the btree,first it starts searching from the root node
   * and move down to the leaf node.
   *
   * If the key is found in the leaf node it returns its position which is +ve integer value
   * and then it matches the value for that key and if it's value also matches it returns the located key
   * bt if the value doesn't match then searches the key in the pervious nodeand checks it value also if it matches
   * and returns the key if it satisfies both the criteria else searches in the next node accordingly and returns
   * respective key.
   * Case:If leaf node doesn't contains the required key:then in this case the position returned by binary search is
   * a -ve integer value and if the postion is negative it returns null.
   * @param rootNode BTreeNode rootNode of the given btree
   * @param key Object key to be searched in btree
   * @param value Object value to be found in btree
   * @return BTreeKey btree key with the node and position of the required key.
   */
    private BTreeKey searchNode(BTreeNode rootNode, Object key, Object value) throws DException {
        BTreeNode node = rootNode;
        BTreeNode locatedNode = null;
        int pos = -1;
        int valToRet = -1;
        do {
            if (node.getElementCount() < 2 && node.isLeaf()) return null;
            locatedNode = node;
            pos = node.binarySearch(key);
            valToRet = pos;
            if (pos < 0) {
                pos = Math.abs(pos);
            }
            node = node.isLeaf() ? null : getNode(null, node.getChildNodeKey(pos));
        } while (node != null);
        if (valToRet < 0) return null;
        BTreeKey btreeKey = getBTreeKey(locatedNode, valToRet);
        if (!DUPLICATEKEYSALLOWED) return btreeKey;
        if (value.equals(btreeKey.getValue())) return btreeKey;
        BTreeKey searchKey = (BTreeKey) btreeKey.clone();
        while (next1(btreeKey)) {
            Object nextKey = btreeKey.getKey();
            if ((comparator.compare(nextKey, key) == 0)) {
                if (value.equals(btreeKey.getValue())) {
                    return btreeKey;
                }
            } else break;
        }
        btreeKey = (BTreeKey) searchKey.clone();
        while (previous1(btreeKey)) {
            Object previousKey = btreeKey.getKey();
            if (comparator.compare(previousKey, key) == 0) {
                if (value.equals(btreeKey.getValue())) {
                    return btreeKey;
                }
            } else break;
        }
        return searchKey;
    }

    /**
   * Searches the key in the btree,first it strats searching from the root node
   * and move down to the leaf node.
   *
   * If the key is found in the leaf node it returns its position which is +ve integer value
   * and if the top variale is set to true then it moves to the previous node till the
   * same key is encountered and returns the previous most key as it is the top most key else if
   * same key is not there in the previous node then it breaks the loop and the loacted key is returned.
   *
   * if top is false then it searches for the same key in the next node and similarly continues searching in
   * the next nodes till the same key is encounterd and returns the right most key as it is the bottom most key else if
   * same key is not there in the next node then it breaks the loop and the loacted key is returned.
   *
   * Case:If leaf node doesn't contains the required key:then in this case the position returned by binary search is
   * a -ve integer value.
   * Top true:In this case also searching is done in the previous node first
   * but if no maching key is find in the previous node then it
   * searches in the next node for that key and else returns a new BTreeKey with the located node nad -ve position value
   * if locate is true else returns null
   *
   * Top false:In this case also searching is done in the next node first
   * but if no maching key is find in the next node then it
   * searches in the previous node for that key and else returns a new BTreeKey with the located node nad -ve position value
   * if locate is true else returns null.
   *
   * @param rootNode BTreeNode rootNode of the given btree
   * @param key Object key to be searched in btree
   * @param locate boolean if the exact key is not found then return the next key or previous key if locate
   *                       is true else return null
   * @param top boolean boolean which states if the key to searches shd be top most
   * of bottom most
   * @param isPartial boolean key to be searched is full or partial
   * @return BTreeKey btree key with the node and position of the required key.
   */
    private BTreeKey searchNode(BTreeNode rootNode, Object key, boolean locate, boolean top, boolean isPartial) throws DException {
        BTreeNode node = rootNode;
        BTreeNode locatedNode = null;
        int pos = -1;
        int valToRet = -1;
        do {
            locatedNode = node;
            pos = search(node, key, 1, node.getElementCount() - 1, top);
            valToRet = pos;
            if (pos < 0) {
                pos = Math.abs(pos);
            }
            node = node.isLeaf() ? null : getNode(null, node.getChildNodeKey(pos));
        } while (node != null);
        if (!isPartial && valToRet < 0) {
            return locate ? new BTreeKey(locatedNode, valToRet) : null;
        } else if (valToRet > 0) {
            BTreeKey btreeKey = getBTreeKey(locatedNode, valToRet);
            BTreeKey searchKey = (BTreeKey) btreeKey.clone();
            if (top) {
                while (previous1(btreeKey)) {
                    Object previousKey = btreeKey.getKey();
                    if (!(comparator.compare(previousKey, key) == 0)) {
                        next1(btreeKey);
                        return btreeKey;
                    }
                }
                return btreeKey;
            } else {
                btreeKey = (BTreeKey) searchKey.clone();
                while (next1(btreeKey)) {
                    Object nextKey = btreeKey.getKey();
                    if (!(comparator.compare(nextKey, key) == 0)) {
                        previous1(btreeKey);
                        return btreeKey;
                    }
                }
                return btreeKey;
            }
        } else {
            BTreeKey btreeKey = getBTreeKey(locatedNode, Math.abs(valToRet));
            BTreeKey searchKey = (BTreeKey) btreeKey.clone();
            BTreeKey locatedKey = null;
            if (top) {
                while (previous1(btreeKey)) {
                    Object previousKey = btreeKey.getKey();
                    if (comparator.compare(previousKey, key) != 0) break; else locatedKey = (BTreeKey) btreeKey.clone();
                }
                if (locatedKey != null) return locatedKey;
                btreeKey = (BTreeKey) searchKey.clone();
                if (next1(btreeKey)) {
                    Object nextKey = btreeKey.getKey();
                    if (comparator.compare(nextKey, key) == 0) return btreeKey;
                }
                return locate ? new BTreeKey(locatedNode, valToRet) : null;
            } else {
                while (next1(btreeKey)) {
                    Object nextKey = btreeKey.getKey();
                    if (comparator.compare(nextKey, key) != 0) break; else locatedKey = (BTreeKey) btreeKey.clone();
                }
                if (locatedKey != null) return locatedKey;
                btreeKey = (BTreeKey) searchKey.clone();
                if (previous1(btreeKey)) {
                    Object previousKey = btreeKey.getKey();
                    if (comparator.compare(previousKey, key) == 0) return btreeKey;
                }
                return locate ? new BTreeKey(locatedNode, valToRet) : null;
            }
        }
    }

    /**
   * Searches the key with the given condition in the btree,first it strats searching from the root node
   * and move down to the leaf node.
   *
   * If the key is found in the leaf node it returns its position which is +ve integer value
   * and if the top variale is set to true then it moves to the previous node till the
   * same key is encountered and returns the previous most key as it is the top most key else if
   * same key is not there in the previous node then it breaks the loop and the loacted key is returned.
   *
   * if top is false then it searches for the same key in the next node and similarly continues searching in
   * the next nodes till the same key is encounterd and returns the right most key as it is the bottom most key else if
   * same key is not there in the next node then it breaks the loop and the loacted key is returned.
   *
   * Case:If leaf node doesn't contains the required key:then in this case the postion returned by binary search is
   * a -ve integer value.
   * Top true:In this case also searching is done in the previous node first
   * but if no maching key is find in the previous node then it
   * searches in the next node for that key and else returns null.
   * Top false:In this case also searching is done in the next node first
   * but if no maching key is find in the next node then it
   * searches in the previous node for that key and else returns null.
   *
   * @param rootNode BTreeNode rootNode
   * @param condition _IndexPredicate[] condition for searching the key in btree
   * @param top boolean boolean which states if the key to searches shd be top most
   * of bottom most
   * @return BTreeKey btree key with the node and position of the required key.
   */
    private BTreeKey searchNode(BTreeNode rootNode, _IndexPredicate[] condition, _VariableValues reader, boolean top) throws DException {
        if (rootNode == null || (rootNode.getElementCount() < 2 && rootNode.isLeaf())) {
            return null;
        }
        BTreeNode node = rootNode;
        BTreeNode locatedNode = null;
        int pos = -1;
        int valToRet = -1;
        do {
            locatedNode = node;
            pos = search(node, condition, reader, 1, node.getElementCount() - 1, top);
            valToRet = pos;
            if (pos < 0) {
                pos = Math.abs(pos);
            }
            node = node.isLeaf() ? null : getNode(null, node.getChildNodeKey(pos));
        } while (node != null);
        boolean isPartial = condition.length != getColumnNames().length;
        if (!isPartial && valToRet < 0) return null; else if (valToRet > 0) {
            BTreeKey btreeKey = getBTreeKey(locatedNode, valToRet);
            BTreeKey searchKey = (BTreeKey) btreeKey.clone();
            if (top) {
                while (previous1(btreeKey)) {
                    Object previousKey = btreeKey.getKey();
                    ((BTreeReader) reader).setValue(previousKey);
                    if (evaluate(condition, reader, 0) != 0) {
                        next1(btreeKey);
                        return btreeKey;
                    }
                }
                return btreeKey;
            } else {
                btreeKey = (BTreeKey) searchKey.clone();
                while (next1(btreeKey)) {
                    Object nextKey = btreeKey.getKey();
                    ((BTreeReader) reader).setValue(nextKey);
                    if (evaluate(condition, reader, 0) != 0) {
                        previous1(btreeKey);
                        return btreeKey;
                    }
                }
                return btreeKey;
            }
        } else {
            BTreeKey btreeKey = getBTreeKey(locatedNode, Math.abs(valToRet));
            BTreeKey searchKey = (BTreeKey) btreeKey.clone();
            BTreeKey locatedKey = null;
            if (top) {
                while (previous1(btreeKey)) {
                    Object previousKey = btreeKey.getKey();
                    ((BTreeReader) reader).setValue(previousKey);
                    if (evaluate(condition, reader, 0) != 0) break; else {
                        locatedKey = (BTreeKey) btreeKey.clone();
                    }
                }
                if (locatedKey != null) return locatedKey;
                btreeKey = (BTreeKey) searchKey.clone();
                if (next1(btreeKey)) {
                    Object nextKey = btreeKey.getKey();
                    ((BTreeReader) reader).setValue(nextKey);
                    if (evaluate(condition, reader, 0) == 0) return btreeKey;
                }
                return null;
            } else {
                while (next1(btreeKey)) {
                    Object nextKey = btreeKey.getKey();
                    ((BTreeReader) reader).setValue(nextKey);
                    if (evaluate(condition, reader, 0) != 0) break; else locatedKey = (BTreeKey) btreeKey.clone();
                }
                if (locatedKey != null) return locatedKey;
                btreeKey = (BTreeKey) searchKey.clone();
                if (previous1(btreeKey)) {
                    Object previousKey = btreeKey.getKey();
                    ((BTreeReader) reader).setValue(previousKey);
                    if (evaluate(condition, reader, 0) == 0) return btreeKey;
                }
                return null;
            }
        }
    }

    /**
   * used in case of next and previous  methods
   * returns the next btreeKey if it exists else returns null in case top = true
   * else if top=false returns the previous btreeKey if it exists else return null
   * @param entry BTreeKey key returned from search node method with
   * -ve or 0 position
   * @param top boolean boolean which states if the key to searches shd be top most
   * of bottom most
   * @return BTreeKey which satisfies the condition or null if no match found
   */
    private BTreeKey locateNode(BTreeKey entry, boolean top) throws DException {
        int position = entry.getPosition();
        position = Math.abs(position);
        BTreeNode node = entry.getNode();
        if (node.isNodeRemovedFromMap()) node = nodeManager.getNode(null, node.getNodeKey());
        entry.setNodeAndPosition(node, position);
        if (top) return next1(entry) ? entry : null; else return entry.getPosition() == 0 ? previous1(entry) ? entry : null : entry;
    }

    private BTreeKey delete(_DatabaseUser user, BTreeKey key, BTreeNode rootNode) throws DException {
        BTreeNode node = key.getNode();
        node = getNode(user, node.getNodeKey());
        node.delete(user, key.getPosition());
        nodeManager.updateSizeAndBTreeInfo(user, false, rootNode);
        return key;
    }

    public void setDuplicateAllowed(boolean flag) {
        DUPLICATEKEYSALLOWED = flag;
    }

    public void releaseResource(_DatabaseUser user, boolean releaseCompletely) throws DException {
        nodeManager.releaseResource(user, releaseCompletely);
    }

    public boolean getDuplicateAllowed() {
        return DUPLICATEKEYSALLOWED;
    }

    public _IndexKey keyInstance() {
        return new BTreeKey();
    }

    public BTreeNavigator getNavigator() {
        return new BTreeNavigatorCurrent(this);
    }

    public void setSize(int size0) {
        nodeManager.setSize(size0);
    }
}
