package org.neurpheus.collections.tree.linkedlist;

import java.util.ArrayList;
import java.util.List;
import org.neurpheus.collections.tree.TreeNode;

/**
 *
 * @author szkoleniowy
 */
public class LinkedListTreeNode implements TreeNode {

    private LinkedListPosition pos;

    public LinkedListTreeNode() {
        this.pos = null;
    }

    public LinkedListTreeNode(LinkedListPosition pos) {
        this.pos = pos;
    }

    protected LinkedListTreeUnit getUnit() {
        return pos.getUnit();
    }

    public Object getValue() {
        return new Integer(getUnit().getValueCode());
    }

    public void setValue(Object newValue) {
        throw new UnsupportedOperationException();
    }

    public boolean isLeaf() {
        return getUnit().isWordEnd();
    }

    /**
     *  Returns a readonly list of children nodes of this node.
     *
     * @return List of children nodes or empty list if this node is a leaf.
     */
    public List getChildren() {
        ArrayList result = new ArrayList();
        LinkedListTreeUnit unit = pos.getUnit();
        if (unit.isWordContinued()) {
            LinkedListPosition childPosition = pos.nextLevel();
            while (childPosition != null) {
                LinkedListTreeUnit childUnit = childPosition.getUnit();
                if (childUnit == null) {
                    System.out.println("error");
                }
                LinkedListTreeNode child = childUnit.isWordEnd() ? new LinkedListTreeLeaf(childPosition) : new LinkedListTreeNode(childPosition);
                result.add(child);
                childPosition = childPosition.nextChild();
            }
        }
        return result;
    }

    /**
     * Sets a new list of children nodes for this node.
     *
     * @param children A new list of children nodes of this node or empty list if this node is a leaf.
     */
    public void setChildren(List newChildren) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a child which contains specified key.
     *
     * @param key The transition key to the child.
     *
     * @return The child or null if there is no child contianing specified key.
     */
    public TreeNode getChild(Object key) {
        if (pos.isWordContinued()) {
            LinkedListPosition childPosition = pos.goToNextLevel();
            if (childPosition != null) {
                int keyValue = ((Integer) key).intValue();
                while (childPosition != null) {
                    int childValueCode = (int) childPosition.getValueCode();
                    if (childValueCode == keyValue) {
                        LinkedListTreeNode child = childPosition.isWordEnd() ? new LinkedListTreeLeaf(childPosition) : new LinkedListTreeNode(childPosition);
                        return child;
                    }
                    childPosition = childValueCode < keyValue ? childPosition.goToNextChild() : null;
                }
            }
        }
        return null;
    }

    public TreeNode getChild(final Object key, final TreeNode fromNode) {
        if (fromNode == null) {
            return getChild(key);
        } else {
            LinkedListPosition childPosition;
            childPosition = new LinkedListPosition(((LinkedListTreeNode) fromNode).pos);
            childPosition = childPosition.goToNextChild();
            int keyValue = ((Integer) key).intValue();
            while (childPosition != null) {
                int childValueCode = (int) childPosition.getValueCode();
                if (childValueCode == keyValue) {
                    LinkedListTreeNode child = childPosition.isWordEnd() ? new LinkedListTreeLeaf(childPosition) : new LinkedListTreeNode(childPosition);
                    return child;
                }
                childPosition = childValueCode < keyValue ? childPosition.goToNextChild() : null;
            }
            return null;
        }
    }

    /**
     * Returns the number of children nodes of this node.
     *
     * @return The number of children of this node.
     */
    public int getNumberOfChildren() {
        return getChildren().size();
    }

    /**
     * Returns a chid available at the given position in the ordered list of children nodes.
     * 
     * @param pos The index of child nodes on the nodes list.
     *
     * @return The child from the given position.
     */
    public TreeNode getChildAtPos(int index) {
        return (TreeNode) getChildren().get(index);
    }

    /**
     * Adds the given node to the end of a list of children nodes.
     * 
     * @param child The child node to add.
     */
    public void addChild(TreeNode child) {
        throw new UnsupportedOperationException();
    }

    /**
     * Adds the given node child
     * 
     * @param pos 
     * @param child 
     */
    public void addChild(int index, TreeNode child) {
        throw new UnsupportedOperationException();
    }

    /**
     * 
     * @param child 
     */
    public boolean removeChild(TreeNode child) {
        throw new UnsupportedOperationException();
    }

    /**
     * 
     * @param pos 
     * @return 
     */
    public TreeNode removeChild(int index) {
        throw new UnsupportedOperationException();
    }

    public int replaceChild(TreeNode fromNode, TreeNode toNode) {
        throw new UnsupportedOperationException();
    }

    public void clear() {
        pos.dispose();
        pos = null;
    }

    public static boolean OLD_SOLUTION = false;

    /**
     * Returns a child which contains specified key.
     *
     * @param key The transition key to the child.
     *
     * @return The child or null if there is no child contianing specified key.
     */
    public TreeNode getChild(final Object key, final int[] stack, int stackPos) {
        if (OLD_SOLUTION) {
            if (pos.isWordContinued()) {
                LinkedListPosition childPosition = pos.goToNextLevel();
                if (childPosition != null) {
                    int keyValue = ((Integer) key).intValue();
                    while (childPosition != null) {
                        int childValueCode = childPosition.getValueCode();
                        if (childValueCode == keyValue) {
                            LinkedListTreeNode child = childPosition.isWordEnd() ? new LinkedListTreeLeaf(childPosition) : new LinkedListTreeNode(childPosition);
                            return child;
                        }
                        childPosition = childValueCode < keyValue ? childPosition.goToNextChild() : null;
                    }
                }
            }
            return null;
        } else {
            final LinkedListTreeUnitArray units = pos.getUnitArray();
            final int unitsSize = units.size();
            final int startStackPos = stackPos;
            int p = pos.getPos();
            int fp = units.getFastIndex(p);
            int nested = pos.getNested() ? 1 : 0;
            int unitsToRead = pos.getUnitsToRead();
            LinkedListPosition returnPos = pos.getReturnPos();
            while (p < unitsSize && units.isAbsolutePointerFast(fp)) {
                if (nested == 0 || unitsToRead > 1) {
                    stack[++stackPos] = p + 1;
                    stack[++stackPos] = nested;
                    stack[++stackPos] = unitsToRead - 1;
                }
                unitsToRead = units.getValueCodeFast(fp);
                nested = unitsToRead != 0 ? 1 : 0;
                p = units.getDistanceFast(fp);
                fp = units.getFastIndex(p);
            }
            if (units.isWordContinuedFast(fp)) {
                int cint = ((Integer) key).intValue();
                if (nested == 1 && unitsToRead <= 1) {
                    if (stackPos <= startStackPos) {
                        if (returnPos != null) {
                            unitsToRead = returnPos.getUnitsToRead();
                            nested = returnPos.getNested() ? 1 : 0;
                            p = returnPos.getPos();
                            returnPos = returnPos.getReturnPos();
                        } else {
                            return null;
                        }
                    } else {
                        unitsToRead = stack[stackPos--];
                        nested = stack[stackPos--];
                        p = stack[stackPos--];
                    }
                } else {
                    ++p;
                    --unitsToRead;
                }
                fp = units.getFastIndex(p);
                boolean found = false;
                while (!found) {
                    while (p < unitsSize && units.isAbsolutePointerFast(fp)) {
                        if (nested == 0 || unitsToRead > 1) {
                            stack[++stackPos] = p + 1;
                            stack[++stackPos] = nested;
                            stack[++stackPos] = unitsToRead - 1;
                        }
                        unitsToRead = units.getValueCodeFast(fp);
                        nested = unitsToRead != 0 ? 1 : 0;
                        p = units.getDistanceFast(fp);
                        fp = units.getFastIndex(p);
                    }
                    int vc = units.getValueCodeFast(fp);
                    if (vc == cint) {
                        LinkedListPosition retPos = returnPos == null ? null : new LinkedListPosition(returnPos);
                        LinkedListPosition position = new LinkedListPosition(units, p, retPos, unitsToRead, nested == 1);
                        LinkedListTreeNode child = units.isWordEndFast(fp) ? new LinkedListTreeLeaf(position) : new LinkedListTreeNode(position);
                        position.setAbsProcessed(true);
                        while (stackPos > startStackPos) {
                            unitsToRead = stack[stackPos--];
                            nested = stack[stackPos--];
                            p = stack[stackPos--];
                            LinkedListPosition tmp = new LinkedListPosition(units, p, retPos, unitsToRead, nested == 1);
                            position.setReturnPos(tmp);
                            position = tmp;
                        }
                        return child;
                    } else if (vc > cint) {
                        return null;
                    } else {
                        int d = units.getDistanceFast(fp);
                        if (d > 0) {
                            int target = p + d;
                            if (nested == 1 && unitsToRead > 0 && target >= p + unitsToRead) {
                                if (stackPos <= startStackPos) {
                                    if (returnPos != null) {
                                        unitsToRead = returnPos.getUnitsToRead();
                                        nested = returnPos.getNested() ? 1 : 0;
                                        p = returnPos.getPos();
                                        returnPos = returnPos.getReturnPos();
                                    } else {
                                        return null;
                                    }
                                } else {
                                    unitsToRead = stack[stackPos--];
                                    nested = stack[stackPos--];
                                    p = stack[stackPos--];
                                }
                            } else {
                                p = target;
                                unitsToRead = unitsToRead - d;
                            }
                            fp = units.getFastIndex(p);
                        } else {
                            return null;
                        }
                    }
                }
            }
            return null;
        }
    }

    public Object getData(String str, int[] stack, int stackPos) {
        int startStackPos = stackPos;
        LinkedListTreeUnitArray units = pos.getUnitArray();
        int unitsSize = units.size();
        int p = pos.getPos();
        int fp = units.getFastIndex(p);
        int nested = pos.getNested() ? 1 : 0;
        int unitsToRead = pos.getUnitsToRead();
        for (int i = 0; i < str.length(); i++) {
            int cint = (int) str.charAt(i);
            while (p < unitsSize && units.isAbsolutePointerFast(fp)) {
                if (nested == 0 || unitsToRead > 1) {
                    stack[++stackPos] = p + 1;
                    stack[++stackPos] = nested;
                    stack[++stackPos] = unitsToRead - 1;
                }
                unitsToRead = units.getValueCodeFast(fp);
                nested = unitsToRead != 0 ? 1 : 0;
                p = units.getDistanceFast(fp);
                fp = units.getFastIndex(p);
            }
            if (units.isWordContinuedFast(fp)) {
                if (nested == 1 && unitsToRead <= 1) {
                    if (stackPos <= startStackPos) {
                        LinkedListPosition returnPos = pos.getReturnPos();
                        if (returnPos != null) {
                            unitsToRead = returnPos.getUnitsToRead();
                            nested = returnPos.getNested() ? 1 : 0;
                            p = returnPos.getPos();
                        } else {
                            return null;
                        }
                    } else {
                        unitsToRead = stack[stackPos--];
                        nested = stack[stackPos--];
                        p = stack[stackPos--];
                    }
                } else {
                    ++p;
                    --unitsToRead;
                }
                fp = units.getFastIndex(p);
            } else {
                return null;
            }
            boolean found = false;
            while (!found) {
                while (p < unitsSize && units.isAbsolutePointerFast(fp)) {
                    if (nested == 0 || unitsToRead > 1) {
                        stack[++stackPos] = p + 1;
                        stack[++stackPos] = nested;
                        stack[++stackPos] = unitsToRead - 1;
                    }
                    unitsToRead = units.getValueCodeFast(fp);
                    nested = unitsToRead != 0 ? 1 : 0;
                    p = units.getDistanceFast(fp);
                    fp = units.getFastIndex(p);
                }
                int vc = units.getValueCodeFast(fp);
                if (vc == cint) {
                    found = true;
                } else if (vc > cint) {
                    return null;
                } else {
                    int d = units.getDistanceFast(fp);
                    if (d > 0) {
                        int target = p + d;
                        if (nested == 1 && unitsToRead > 0 && target >= p + unitsToRead) {
                            if (stackPos <= startStackPos) {
                                LinkedListPosition returnPos = pos.getReturnPos();
                                if (returnPos != null) {
                                    unitsToRead = returnPos.getUnitsToRead();
                                    nested = returnPos.getNested() ? 1 : 0;
                                    p = returnPos.getPos();
                                    returnPos = returnPos.getReturnPos();
                                } else {
                                    return null;
                                }
                            } else {
                                unitsToRead = stack[stackPos--];
                                nested = stack[stackPos--];
                                p = stack[stackPos--];
                            }
                        } else {
                            p = target;
                            unitsToRead = unitsToRead - d;
                        }
                        fp = units.getFastIndex(p);
                    } else {
                        return null;
                    }
                }
            }
        }
        if (units.isWordEndFast(fp)) {
            int dataCode = units.getDataCodeFast(fp);
            return new Integer(dataCode);
        }
        return null;
    }
}
