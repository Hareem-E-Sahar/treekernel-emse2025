package AccordionDrawer;

import java.math.BigInteger;
import java.util.Hashtable;
import java.util.Vector;
import AccordionPowersetDrawer.Group;

public class DynSplitTree {

    DynSplitLine sentinel;

    DynSplitLine rootSentinel;

    public DynSplitLine min, max;

    public int size;

    static final int BLUE = 0;

    static final int RED = 1;

    static final int BLACK = -1;

    public static final double[] defaultMinStuckValue = { 0.015f, 0.001f };

    public static final double defaultMaxStuckValue = 0.999f;

    public boolean horizontal;

    double minStuckValue, maxStuckValue;

    public AccordionDrawer ad;

    public DynSplitLine[] nodeArray;

    public DynSplitTree(AccordionDrawer ad, boolean horizontal, double minStuckValue, double maxStuckValue) {
        sentinel = new DynSplitLine(sentinel);
        sentinel.leftChild = sentinel;
        sentinel.rightChild = sentinel;
        sentinel.parent = sentinel;
        sentinel.color = BLACK;
        sentinel.index = BigInteger.valueOf(-1);
        rootSentinel = new DynSplitLine(sentinel);
        rootSentinel.color = BLACK;
        rootSentinel.leftChild = sentinel;
        rootSentinel.rightChild = sentinel;
        rootSentinel.parent = null;
        rootSentinel.index = BigInteger.valueOf(-1);
        min = new DynSplitLine(Long.MAX_VALUE);
        max = new DynSplitLine(-1);
        size = 0;
        this.ad = ad;
        this.horizontal = horizontal;
        this.minStuckValue = minStuckValue;
        this.maxStuckValue = maxStuckValue;
    }

    public boolean isRoot(DynSplitLine cell) {
        return cell == rootSentinel.leftChild;
    }

    public DynSplitLine nodeExist(DynSplitLine node) {
        DynSplitLine current = rootSentinel.leftChild;
        while (current != sentinel) if (node.index.compareTo(current.index) == 0) return current; else if (node.index.compareTo(current.index) == 1) current = current.rightChild; else current = current.leftChild;
        return null;
    }

    public void delete(DynSplitLine current) {
        if (current.color == BLUE) {
            return;
        }
        if (current.leftChild == sentinel || current.rightChild == sentinel) {
            updateNodes(current);
            deleteLeaf(current);
        } else {
            DynSplitLine successor = current.getNext();
            if (successor.color == BLACK && successor.leftChild == sentinel && successor.rightChild == sentinel) successor = current.getPrev();
            updateNode(current, successor);
            deleteLeaf(successor);
            DynSplitLine Temp = current.rightChild;
            successor.rightChild = Temp;
            Temp.parent = successor;
            Temp = current.leftChild;
            successor.leftChild = Temp;
            Temp.parent = successor;
            Temp = current.parent;
            successor.parent = Temp;
            if (Temp.leftChild == current) Temp.leftChild = successor; else Temp.rightChild = successor;
            successor.color = current.color;
            successor.left = current.left;
            successor.right = current.right;
            successor.splitValue = current.splitValue;
            successor.defaultSplitValue = current.defaultSplitValue;
            successor.minLine = current.minLine;
            successor.maxLine = current.maxLine;
        }
        current.color = BLUE;
        return;
    }

    public synchronized void add(DynSplitLine newNode, int posCol) {
        if (newNode.color != BLUE) {
            return;
        }
        size++;
        if (newNode.index.compareTo(max.index) == 1) max = newNode;
        if (newNode.index.compareTo(min.index) == -1) min = newNode;
        newNode.color = RED;
        newNode.sentinel = sentinel;
        newNode.leftChild = sentinel;
        newNode.rightChild = sentinel;
        DynSplitLine current = rootSentinel.leftChild;
        if (current == sentinel) {
            rootSentinel.leftChild = newNode;
            newNode.parent = rootSentinel;
            rootSentinel.leftChild.maxLine = max;
            rootSentinel.leftChild.minLine = min;
        } else {
            int leftTurnCount = 0;
            int rightTurnCount = 0;
            do {
                if (newNode.index.compareTo(current.index) == -1) {
                    leftTurnCount++;
                    if (newNode.index.compareTo(current.minLine.index) == -1) current.minLine = newNode;
                    current.left++;
                    if (current.isLeftChild()) if (rightTurnCount == 0) {
                        if (current.splitValue == current.defaultSplitValue) current.splitValue = current.defaultSplitValue = (double) (current.left) / (double) (current.left + (current.right + 1f)); else current.defaultSplitValue = (double) (current.left) / (double) (current.left + (current.right + 1f));
                    } else {
                        if (current.splitValue == current.defaultSplitValue) current.splitValue = current.defaultSplitValue = (double) (current.left + 1f) / (double) ((current.left + 1f) + (current.right + 1f)); else current.defaultSplitValue = (double) (current.left + 1f) / (double) ((current.left + 1f) + (current.right + 1f));
                    } else if (leftTurnCount > 1) {
                        if (current.splitValue == current.defaultSplitValue) current.splitValue = current.defaultSplitValue = (double) (1f + current.left) / (double) ((1f + current.right) + (current.left + 1f)); else current.defaultSplitValue = (double) (1f + current.left) / (double) ((1f + current.right) + (current.left + 1f));
                    } else {
                        if (current.splitValue == current.defaultSplitValue) current.splitValue = current.defaultSplitValue = (double) (1f + current.left) / (double) ((1f + current.right) + current.left); else current.defaultSplitValue = (double) (1f + current.left) / (double) ((1f + current.right) + current.left);
                    }
                    if (current.leftChild == sentinel) {
                        current.leftChild = newNode;
                        newNode.parent = current;
                        break;
                    } else current = current.leftChild;
                } else {
                    rightTurnCount++;
                    if (newNode.index.compareTo(current.maxLine.index) == 1) current.maxLine = newNode;
                    current.right++;
                    if (!current.isLeftChild()) {
                        if (leftTurnCount == 0) {
                            if (current.splitValue == current.defaultSplitValue) current.splitValue = current.defaultSplitValue = (double) (current.left + 1f) / (double) ((current.left + 1f) + current.right); else current.defaultSplitValue = (double) (current.left + 1f) / (double) ((current.left + 1f) + current.right);
                        } else {
                            if (current.splitValue == current.defaultSplitValue) current.splitValue = current.defaultSplitValue = (double) (current.left + 1f) / (double) ((current.left + 1f) + (1f + current.right)); else current.defaultSplitValue = (double) (current.left + 1f) / (double) ((current.left + 1f) + (1f + current.right));
                        }
                    } else if (rightTurnCount > 1) {
                        if (current.splitValue == current.defaultSplitValue) current.splitValue = current.defaultSplitValue = (double) (current.left + 1f) / (double) ((1f + current.right) + (current.left + 1f)); else current.defaultSplitValue = (double) (current.left + 1f) / (double) ((1f + current.right) + (current.left + 1f));
                    } else {
                        if (current.splitValue == current.defaultSplitValue) current.splitValue = current.defaultSplitValue = (double) (current.left) / (double) ((1f + current.right) + current.left); else current.defaultSplitValue = (double) (current.left) / (double) ((1f + current.right) + current.left);
                    }
                    if (current.rightChild == sentinel) {
                        current.rightChild = newNode;
                        newNode.parent = current;
                        break;
                    } else current = current.rightChild;
                }
            } while (true);
            if (rootSentinel.leftChild.splitValue == rootSentinel.leftChild.defaultSplitValue) rootSentinel.leftChild.splitValue = rootSentinel.leftChild.defaultSplitValue = (double) rootSentinel.leftChild.left / (double) (rootSentinel.leftChild.left + rootSentinel.leftChild.right); else rootSentinel.leftChild.defaultSplitValue = (double) rootSentinel.leftChild.left / (double) (rootSentinel.leftChild.left + rootSentinel.leftChild.right);
            if (newNode.isLeftChild()) {
                newNode.maxLine = newNode.parent;
                newNode.minLine = newNode.parent.minLine;
            } else {
                newNode.minLine = newNode.parent;
                newNode.maxLine = newNode.parent.maxLine;
            }
            if (newNode == min) {
                newNode.splitValue = newNode.defaultSplitValue = 0;
                newNode.minLine = newNode;
            } else if (newNode == max) {
                newNode.splitValue = newNode.defaultSplitValue = 1;
                newNode.maxLine = newNode;
            } else newNode.splitValue = newNode.defaultSplitValue = 0.5;
            balance(current);
        }
        rootSentinel.leftChild.color = BLACK;
        return;
    }

    DynSplitLine getRoot() {
        return this.rootSentinel.leftChild;
    }

    private void balance(DynSplitLine current) {
        while (current.color != BLACK) {
            DynSplitLine cur_parent = current.parent;
            if (cur_parent.rightChild == current) {
                if (cur_parent.leftChild.color == RED) {
                    cur_parent.leftChild.color = BLACK;
                    current.color = BLACK;
                    cur_parent.color = RED;
                    current = cur_parent.parent;
                    continue;
                }
                if (current.leftChild.color == RED) {
                    current.rightSide_RightRotate();
                }
                cur_parent.leftRotate();
                if (cur_parent.color == RED && cur_parent.parent.color == BLACK) {
                    return;
                } else {
                    cur_parent.color = RED;
                    cur_parent.parent.color = BLACK;
                    return;
                }
            } else {
                if (cur_parent.rightChild.color == RED) {
                    cur_parent.rightChild.color = BLACK;
                    current.color = BLACK;
                    cur_parent.color = RED;
                    current = cur_parent.parent;
                    continue;
                }
                if (current.rightChild.color == RED) {
                    current.leftSide_LeftRotate();
                }
                cur_parent.rightRotate();
                if (cur_parent.color == RED && cur_parent.parent.color == BLACK) {
                    return;
                } else {
                    cur_parent.color = RED;
                    cur_parent.parent.color = BLACK;
                    return;
                }
            }
        }
        return;
    }

    private void deleteLeaf(DynSplitLine node) {
        DynSplitLine node_parent = node.parent;
        boolean leftSide = (node_parent.leftChild == node);
        if (node.color == RED) {
            if (leftSide) {
                node_parent.leftChild = this.sentinel;
            } else {
                node_parent.rightChild = this.sentinel;
            }
            return;
        }
        if (node.leftChild != this.sentinel) {
            node.leftChild.color = BLACK;
            node.leftChild.parent = node_parent;
            if (leftSide) {
                node_parent.leftChild = node.leftChild;
            } else {
                node_parent.rightChild = node.leftChild;
            }
            return;
        }
        if (node.rightChild != this.sentinel) {
            node.rightChild.color = BLACK;
            node.rightChild.parent = node_parent;
            if (leftSide) {
                node_parent.leftChild = node.rightChild;
            } else {
                node_parent.rightChild = node.rightChild;
            }
            return;
        }
        if (leftSide) {
            node_parent.leftChild = this.sentinel;
        } else {
            node_parent.rightChild = this.sentinel;
        }
        DynSplitLine sibling = (leftSide) ? node_parent.rightChild : node_parent.leftChild;
        DynSplitLine current = node;
        while (current.color == BLACK && node_parent.parent != null) {
            if (sibling.color == RED) {
                node_parent.color = RED;
                sibling.color = BLACK;
                if (leftSide) {
                    node_parent.leftRotate();
                    sibling = node_parent.rightChild;
                } else {
                    node_parent.rightRotate();
                    sibling = node_parent.leftChild;
                }
                continue;
            }
            if (sibling.rightChild.color == BLACK && sibling.leftChild.color == BLACK) {
                if (sibling.color != RED) {
                    sibling.color = RED;
                }
                current = node_parent;
                node_parent = current.parent;
                leftSide = (node_parent.leftChild == current);
                sibling = (leftSide) ? node_parent.rightChild : node_parent.leftChild;
                continue;
            }
            if (leftSide) {
                if (sibling.rightChild.color == BLACK) {
                    sibling.rightSide_RightRotate();
                    sibling = node_parent.rightChild;
                }
                if (sibling.rightChild.color != BLACK || sibling.color != node_parent.color || node_parent.color != BLACK) {
                    sibling.rightChild.color = BLACK;
                    sibling.color = node_parent.color;
                    node_parent.color = BLACK;
                }
                current = node_parent;
                node_parent = current.parent;
                if (node_parent.leftChild == current) current.leftSide_LeftRotate(); else current.rightSide_LeftRotate();
                return;
            } else {
                if (sibling.leftChild.color == BLACK) {
                    sibling.leftSide_LeftRotate();
                    sibling = node_parent.leftChild;
                }
                if (sibling.leftChild.color != BLACK || sibling.color != node_parent.color || node_parent.color != BLACK) {
                    sibling.leftChild.color = BLACK;
                    sibling.color = node_parent.color;
                    node_parent.color = BLACK;
                }
                current = node_parent;
                node_parent = current.parent;
                if (node_parent.leftChild == current) current.leftSide_RightRotate(); else current.rightSide_RightRotate();
                return;
            }
        }
        current.color = BLACK;
        return;
    }

    private void updateNodes(DynSplitLine newNode) {
        int leftTurnCount = 0;
        int rightTurnCount = 0;
        DynSplitLine current = rootSentinel.leftChild;
        DynSplitLine next;
        if (newNode.getNext() != null) next = newNode.getNext(); else next = null;
        DynSplitLine prev;
        if (newNode.getPrev() != null) prev = newNode.getPrev(); else prev = null;
        boolean foundNewNode = false;
        do {
            if (newNode.index.compareTo(current.index) == -1) {
                leftTurnCount++;
                if (newNode == current.minLine) current.minLine = next;
                current.left--;
                if (current.isLeftChild()) if (rightTurnCount == 0) {
                    current.splitValue = current.defaultSplitValue = (double) (current.left) / (double) (current.left + (current.right + 1f));
                } else {
                    current.splitValue = current.defaultSplitValue = (double) (current.left + 1f) / (double) ((current.left + 1f) + (current.right + 1f));
                } else if (leftTurnCount > 1) {
                    current.splitValue = current.defaultSplitValue = (double) (1f + current.left) / (double) ((1f + current.right) + (current.left + 1f));
                } else {
                    current.splitValue = current.defaultSplitValue = (double) (1f + current.left) / (double) ((1f + current.right) + current.left);
                }
                if (current.leftChild == newNode) {
                    break;
                } else current = current.leftChild;
            } else if (newNode.index.compareTo(current.index) == 1) {
                rightTurnCount++;
                if (newNode == current.maxLine) current.maxLine = prev;
                current.right--;
                if (!current.isLeftChild()) {
                    if (leftTurnCount == 0) {
                        current.splitValue = current.defaultSplitValue = (double) (current.left + 1f) / (double) ((current.left + 1f) + current.right);
                    } else {
                        current.splitValue = current.defaultSplitValue = (double) (current.left + 1f) / (double) ((current.left + 1f) + (1f + current.right));
                    }
                } else if (rightTurnCount > 1) {
                    current.splitValue = current.defaultSplitValue = (double) (current.left + 1f) / (double) ((1f + current.right) + (current.left + 1f));
                } else {
                    current.splitValue = current.defaultSplitValue = (double) (current.left) / (double) ((1f + current.right) + current.left);
                }
                if (current.rightChild == newNode) {
                    break;
                } else current = current.rightChild;
            } else break;
        } while (true);
        if (newNode.leftChild != this.sentinel) {
            current = newNode.leftChild;
        } else {
            current = newNode.rightChild;
        }
        boolean left = newNode.isLeftChild();
        while (current != this.sentinel) {
            if (left) {
                leftTurnCount++;
                if (newNode == current.minLine) if (next != null) current.minLine = prev; else current.minLine = newNode.getMinChild();
                if (current.isLeftChild()) if (rightTurnCount == 0) {
                    current.splitValue = current.defaultSplitValue = (double) (current.left) / (double) (current.left + (current.right + 1f));
                } else {
                    current.splitValue = current.defaultSplitValue = (double) (current.left + 1f) / (double) ((current.left + 1f) + (current.right + 1f));
                } else if (leftTurnCount > 1) {
                    current.splitValue = current.defaultSplitValue = (double) (1f + current.left) / (double) ((1f + current.right) + (current.left + 1f));
                } else {
                    current.splitValue = current.defaultSplitValue = (double) (1f + current.left) / (double) ((1f + current.right) + current.left);
                }
                if (current.leftChild == this.sentinel) {
                    break;
                } else current = current.leftChild;
            } else {
                rightTurnCount++;
                if (newNode == current.maxLine) if (prev != null) current.maxLine = next; else current.maxLine = newNode.getMaxChild();
                if (!current.isLeftChild()) {
                    if (leftTurnCount == 0) {
                        current.splitValue = current.defaultSplitValue = (double) (current.left + 1f) / (double) ((current.left + 1f) + current.right);
                    } else {
                        current.splitValue = current.defaultSplitValue = (double) (current.left + 1f) / (double) ((current.left + 1f) + (1f + current.right));
                    }
                } else if (rightTurnCount > 1) {
                    current.splitValue = current.defaultSplitValue = (double) (current.left + 1f) / (double) ((1f + current.right) + (current.left + 1f));
                } else {
                    current.splitValue = current.defaultSplitValue = (double) (current.left) / (double) ((1f + current.right) + current.left);
                }
                if (current.rightChild == this.sentinel) {
                    break;
                } else current = current.rightChild;
            }
        }
        rootSentinel.leftChild.splitValue = rootSentinel.leftChild.defaultSplitValue = (double) rootSentinel.leftChild.left / (double) (rootSentinel.leftChild.left + rootSentinel.leftChild.right);
    }

    private void updateNode(DynSplitLine newNode, DynSplitLine successor) {
        int leftTurnCount = 0;
        int rightTurnCount = 0;
        DynSplitLine current = rootSentinel.leftChild;
        boolean foundNewNode = false;
        do {
            if (successor.index.compareTo(current.index) == -1) {
                leftTurnCount++;
                if (foundNewNode) {
                    if (current.minLine == newNode) current.minLine = successor;
                }
                current.left--;
                if (current.isLeftChild()) if (rightTurnCount == 0) {
                    current.splitValue = current.defaultSplitValue = (double) (current.left) / (double) (current.left + (current.right + 1f));
                } else {
                    current.splitValue = current.defaultSplitValue = (double) (current.left + 1f) / (double) ((current.left + 1f) + (current.right + 1f));
                } else if (leftTurnCount > 1) {
                    current.splitValue = current.defaultSplitValue = (double) (1f + current.left) / (double) ((1f + current.right) + (current.left + 1f));
                } else {
                    current.splitValue = current.defaultSplitValue = (double) (1f + current.left) / (double) ((1f + current.right) + current.left);
                }
                if (current == newNode) foundNewNode = true;
                if (current.leftChild == successor) break; else current = current.leftChild;
            } else if (successor.index.compareTo(current.index) == 1) {
                rightTurnCount++;
                if (foundNewNode) {
                    if (current.maxLine == newNode) current.maxLine = successor;
                }
                current.right--;
                if (!current.isLeftChild()) {
                    if (leftTurnCount == 0) {
                        current.splitValue = current.defaultSplitValue = (double) (current.left + 1f) / (double) ((current.left + 1f) + current.right);
                    } else {
                        current.splitValue = current.defaultSplitValue = (double) (current.left + 1f) / (double) ((current.left + 1f) + (1f + current.right));
                    }
                } else if (rightTurnCount > 1) {
                    current.splitValue = current.defaultSplitValue = (double) (current.left + 1f) / (double) ((1f + current.right) + (current.left + 1f));
                } else {
                    current.splitValue = current.defaultSplitValue = (double) (current.left) / (double) ((1f + current.right) + current.left);
                }
                if (current == newNode) foundNewNode = true;
                if (current.rightChild == successor) break; else current = current.rightChild;
            } else break;
        } while (true);
        DynSplitLine node;
        if (newNode.index.compareTo(successor.index) == 1) {
            node = newNode.rightChild;
            while (node != this.sentinel) {
                node.minLine = successor;
                node = node.leftChild;
            }
        } else {
            node = newNode.leftChild;
            while (node != this.sentinel) {
                node.maxLine = successor;
                node = node.rightChild;
            }
        }
        rootSentinel.leftChild.splitValue = rootSentinel.leftChild.defaultSplitValue = (double) rootSentinel.leftChild.left / (double) (rootSentinel.leftChild.left + rootSentinel.leftChild.right);
        min = this.rootSentinel.leftChild.minLine;
        max = this.rootSentinel.leftChild.maxLine;
    }

    public DynSplitLine[] toArray() {
        DynSplitLine[] list = new DynSplitLine[size];
        addCelltoListRecurse(rootSentinel.leftChild, list, true, size);
        return list;
    }

    private void addCelltoListRecurse(DynSplitLine node, DynSplitLine[] list, boolean leftChild, int parentIndex) {
        if (node == sentinel) return;
        int index;
        if (leftChild) {
            index = parentIndex - node.right - 1;
        } else {
            index = parentIndex + node.left + 1;
        }
        list[index] = node;
        addCelltoListRecurse(node.leftChild, list, true, index);
        addCelltoListRecurse(node.rightChild, list, false, index);
    }

    public DynSplitLine getRootCell() {
        if (getRoot() != sentinel) return getRoot(); else return null;
    }

    public DynSplitLine getSplitCell(double screenPosition) {
        if (screenPosition < minStuckValue || screenPosition > maxStuckValue) {
            return null;
        }
        if (getRoot() == sentinel) return null;
        DynSplitLine currentCell = getRoot();
        DynSplitLine returnCell = currentCell;
        boolean rightFlag = false;
        while (currentCell != this.sentinel) {
            returnCell = currentCell;
            rightFlag = false;
            if (screenPosition == currentCell.absoluteValue) return currentCell; else if (screenPosition < currentCell.absoluteValue) currentCell = currentCell.leftChild; else {
                currentCell = currentCell.rightChild;
                rightFlag = true;
            }
        }
        if (!rightFlag) return returnCell; else return returnCell.getNext();
    }

    public synchronized double computePlaceThisFrame(DynSplitLine cell) {
        try {
            if (cell == null) return -1;
            int frameNum = ad.frameNum;
            if (frameNum <= cell.computedFrame) return cell.absoluteValue;
            cell.computedFrame = frameNum;
            if (cell == min) {
                cell.absoluteValue = minStuckValue;
                return cell.absoluteValue;
            } else if (cell == max) {
                cell.absoluteValue = maxStuckValue;
                return cell.absoluteValue;
            }
            if (!isRoot(cell)) {
                DynSplitLine parent = cell.parent;
                computePlaceThisFrame(parent);
                if (cell.isLeftChild()) {
                    DynSplitLine minLine = parent.minLine;
                    computePlaceThisFrame(minLine);
                    double parMinPosition = minLine == min ? minStuckValue : (minLine).absoluteValue;
                    double parSplitPosition = parent.absoluteValue;
                    double range = parSplitPosition - parMinPosition;
                    cell.absoluteValue = parMinPosition + range * cell.splitValue;
                } else {
                    DynSplitLine maxLine = parent.maxLine;
                    computePlaceThisFrame(maxLine);
                    double parMaxPosition = maxLine == max ? maxStuckValue : (maxLine).absoluteValue;
                    double parSplitPosition = parent.absoluteValue;
                    double range = parMaxPosition - parSplitPosition;
                    cell.absoluteValue = parSplitPosition + range * cell.splitValue;
                }
            } else {
                double myMinPosition = minStuckValue;
                double myMaxPosition = maxStuckValue;
                double range = myMaxPosition - myMinPosition;
                cell.absoluteValue = myMinPosition + range * cell.splitValue;
            }
            return cell.absoluteValue;
        } catch (Exception e) {
            System.err.println("Null in ComputerPlaceThisFrame, ignored!");
            return 0;
        }
    }

    public synchronized double computePlaceThisFrameDo(DynSplitLine cell) {
        cell.computedFrame = ad.frameNum;
        if (cell == min) {
            cell.absoluteValue = minStuckValue;
            return cell.absoluteValue;
        } else if (cell == max) {
            cell.absoluteValue = maxStuckValue;
            return cell.absoluteValue;
        }
        if (!isRoot(cell)) {
            DynSplitLine parent = cell.parent;
            computePlaceThisFrame(parent);
            if (cell.isLeftChild()) {
                DynSplitLine minLine = parent.minLine;
                computePlaceThisFrame(minLine);
                double parMinPosition = minLine == min ? minStuckValue : (minLine).absoluteValue;
                double parSplitPosition = parent.absoluteValue;
                double range = parSplitPosition - parMinPosition;
                cell.absoluteValue = parMinPosition + range * cell.splitValue;
            } else {
                DynSplitLine maxLine = parent.maxLine;
                computePlaceThisFrame(maxLine);
                double parMaxPosition = maxLine == max ? maxStuckValue : (maxLine).absoluteValue;
                double parSplitPosition = parent.absoluteValue;
                double range = parMaxPosition - parSplitPosition;
                cell.absoluteValue = parSplitPosition + range * cell.splitValue;
            }
        } else {
            double myMinPosition = minStuckValue;
            double myMaxPosition = maxStuckValue;
            double range = myMaxPosition - myMinPosition;
            cell.absoluteValue = myMinPosition + range * cell.splitValue;
        }
        return cell.absoluteValue;
    }

    private int getIndexForRangeSplit(DynSplitLine[] splitIndices, DynSplitLine root, int min, int max) {
        if (root == splitIndices[min] || root == splitIndices[max]) {
        }
        int mid = (min + max) / 2;
        while (min + 1 < max && splitIndices[mid] != root) {
            if (splitIndices[mid].index.compareTo(root.index) == -1) min = mid; else max = mid;
            mid = (min + max) / 2;
        }
        mid = (min + max) / 2;
        return mid;
    }

    public void moveLine(DynSplitLine dragLine, int dragPixelEnd, DynSplitLine staticLine, int numSteps, Hashtable newToMove) {
        ad.frameNum++;
        if (dragLine == staticLine) return;
        int xy = horizontal ? AccordionDrawer.X : AccordionDrawer.Y;
        if (dragLine == min || dragLine == max) return;
        DynSplitLine[] range = { min, dragLine, staticLine, max };
        double dragEnd = (double) ad.s2w(dragPixelEnd, xy);
        if (dragEnd < minStuckValue + ad.minContextPeriphery || dragEnd > maxStuckValue - ad.minContextPeriphery) return;
        double staticEnd = computePlaceThisFrame(staticLine);
        double dragStart = computePlaceThisFrame(dragLine);
        double[] startValue = { minStuckValue, dragStart, staticEnd, maxStuckValue };
        double[] endValue = { minStuckValue, dragEnd, staticEnd, maxStuckValue };
        if (dragLine.index.compareTo(staticLine.index) == 1) {
            range[1] = staticLine;
            endValue[1] = staticEnd;
            startValue[1] = staticEnd;
            range[2] = dragLine;
            endValue[2] = dragEnd;
            startValue[2] = dragStart;
        }
        int min = 0, max = range.length - 1;
        while (range[min] == range[min + 1]) min++;
        while (range[max] == range[max - 1]) max--;
        resizeRanges(range, startValue, endValue, numSteps, getRoot(), min, max, newToMove);
    }

    private void resizeRanges(DynSplitLine[] splitIndices, double[] startValues, double[] endValues, int numSteps, DynSplitLine currRoot, int min, int max, Hashtable newToMove) {
        if (max <= min) {
            return;
        }
        if (max - min <= 1) {
            return;
        }
        int index = getIndexForRangeSplit(splitIndices, currRoot, min, max);
        int xy = horizontal ? AccordionDrawer.X : AccordionDrawer.Y;
        double rootStartSize = startValues[max] - startValues[min];
        double rootEndSize = endValues[max] - endValues[min];
        if (rootStartSize < 0 || rootEndSize < 0) {
            System.out.println("this is probably a synchronization problem");
            return;
        }
        if (splitIndices[index] == currRoot) {
            SplitTransition st = new SplitTransition(this, currRoot, (endValues[index] - endValues[min]) / (rootEndSize), numSteps);
            newToMove.put(st.getHashKey(), st);
            resizeRanges(splitIndices, startValues, endValues, numSteps, getSplitRoot(splitIndices[min], splitIndices[index], splitIndices[max], true), min, index, newToMove);
            resizeRanges(splitIndices, startValues, endValues, numSteps, getSplitRoot(splitIndices[min], splitIndices[index], splitIndices[max], false), index, max, newToMove);
        } else {
            double rootOldLocation = computePlaceThisFrame(currRoot);
            double rootOldRangeSize = startValues[index + 1] - startValues[index];
            double rootNewRangeSize = endValues[index + 1] - endValues[index];
            if (rootNewRangeSize > 1 || rootNewRangeSize < 0) System.out.println("bad new root range size");
            double rootRatio = rootNewRangeSize / rootOldRangeSize;
            double rootNewSplitTemp = rootRatio * (rootOldLocation - startValues[index]) + endValues[index];
            double rootNewSplit = (rootNewSplitTemp - endValues[min]) / (rootEndSize);
            if (rootNewSplit > 1 || rootNewSplit < 0) System.out.println("bad new root split");
            double rootNewLocation = rootNewSplit * rootEndSize + endValues[min];
            if (rootNewLocation > 1 || rootNewLocation < 0) System.out.println("bad new root location");
            SplitTransition st = new SplitTransition(this, currRoot, rootNewSplit, numSteps);
            newToMove.put(st.getHashKey(), st);
            double tempEnd = endValues[index + 1];
            double tempStart = startValues[index + 1];
            DynSplitLine tempLine = splitIndices[index + 1];
            endValues[index + 1] = rootNewLocation;
            startValues[index + 1] = rootOldLocation;
            splitIndices[index + 1] = currRoot;
            if (rootNewLocation > 1 || rootNewLocation < 0) System.out.println("bad new root location");
            resizeRanges(splitIndices, startValues, endValues, numSteps, getSplitRoot(splitIndices[min], splitIndices[index + 1], splitIndices[max], true), min, index + 1, newToMove);
            endValues[index + 1] = tempEnd;
            startValues[index + 1] = tempStart;
            splitIndices[index + 1] = tempLine;
            tempEnd = endValues[index];
            tempStart = startValues[index];
            tempLine = splitIndices[index];
            endValues[index] = rootNewLocation;
            startValues[index] = rootOldLocation;
            splitIndices[index] = currRoot;
            if (rootNewLocation > 1 || rootNewLocation < 0) System.out.println("bad new root location");
            resizeRanges(splitIndices, startValues, endValues, numSteps, getSplitRoot(splitIndices[min], splitIndices[index], splitIndices[max], false), index, max, newToMove);
            endValues[index] = tempEnd;
            startValues[index] = tempStart;
            splitIndices[index] = tempLine;
        }
    }

    private static boolean createMin = false;

    private static boolean createMax = false;

    private int[] createIndexRanges(Group group) {
        int minOffset = 0;
        createMin = false;
        createMax = false;
        Vector items = new Vector();
        int counter = 0;
        for (int i = 0; i < group.width; i++) {
            int bb = i;
        }
        int rangeSize = items.size();
        if (((Integer) items.elementAt(0)).intValue() != 0) {
            rangeSize++;
            createMin = true;
            minOffset = 1;
        }
        if (((Integer) items.elementAt(items.size() - 1)).intValue() != group.width - 1) {
            rangeSize++;
            createMax = true;
        }
        Object[] indice;
        indice = items.toArray();
        int[] returnIndice = new int[rangeSize];
        for (int i = 0; i < indice.length; i++) {
            returnIndice[i + minOffset] = ((Integer) indice[i]).intValue();
        }
        if (createMin) returnIndice[0] = 0;
        if (createMax) returnIndice[returnIndice.length - 1] = group.width - 1;
        return returnIndice;
    }

    private double getTotalExtent(double[] extents) {
        double returnValue = 0;
        for (int i = 0; i < extents.length; i++) {
            returnValue += extents[i];
        }
        return returnValue;
    }

    public void resizeForest(Group group, int numSteps, Hashtable newToMove, double inflateIncr) {
        int frameNum = ad.frameNum;
        if (group.size() == 0) return;
        int[] ranges = createIndexRanges(group);
        DynSplitLine[] lines = new DynSplitLine[ranges.length];
        for (int i = 0; i < ranges.length; i++) lines[i] = nodeArray[ranges[i]];
        if (ranges == null) return;
        int leng = ranges.length / 2;
        double stuckRangeSize = maxStuckValue - minStuckValue;
        double[] startValues = new double[ranges.length];
        double[] endValues = new double[ranges.length];
        double[] extent = group.getSizesOfAllRanges(this, frameNum);
        double oldTotalExtent = getTotalExtent(extent);
        int rangesInPeriphery = (createMin ? 0 : 1) + (createMax ? 0 : 1);
        double foo = ad.minContextPeriphery * (2 - rangesInPeriphery);
        double bar = (ranges.length - 1) * ad.minContextInside;
        double minGrowSize = stuckRangeSize - ad.minContextPeriphery * (2 - rangesInPeriphery) - (leng - 1) * ad.minContextInside;
        if (oldTotalExtent >= minGrowSize) {
            System.out.println("Too much squishing, not going to grow (maybe too many ranges? " + leng + ")");
            return;
        }
        double minShrinkSize = stuckRangeSize - ad.minContextPeriphery * rangesInPeriphery - leng * ad.minContextInside;
        double oldTotalNonExtent = stuckRangeSize - oldTotalExtent;
        for (int i = 0; i < ranges.length; i++) {
            startValues[i] = nodeArray[ranges[i]].absoluteValue;
        }
        endValues[0] = startValues[0];
        endValues[ranges.length - 1] = startValues[ranges.length - 1];
        int numRealRanges = extent.length;
        int numRealNonRanges = numRealRanges - 1;
        if (createMin) numRealNonRanges++;
        if (createMax) numRealNonRanges++;
        int startAt = 0;
        int endAt = ranges.length - 1;
        {
            if (oldTotalExtent + inflateIncr > minGrowSize) inflateIncr = minGrowSize - oldTotalExtent;
            double newTotalExtent = oldTotalExtent + inflateIncr;
            double newTotalNonExtent = oldTotalNonExtent - inflateIncr;
            double totalExtentRatio = newTotalExtent / oldTotalExtent;
            double totalNonExtentRatio = newTotalNonExtent / oldTotalNonExtent;
            double firstRange = startValues[startAt + 1] - startValues[startAt];
            double lastRange = startValues[endAt] - startValues[endAt - 1];
            if (createMin) {
                if (firstRange < ad.minContextPeriphery) {
                    System.out.println("Area before first range might be squished too small");
                    endValues[startAt + 1] = startValues[startAt + 1];
                } else {
                    endValues[startAt + 1] = startValues[startAt] + firstRange * totalNonExtentRatio;
                    if (endValues[startAt + 1] - endValues[startAt] < ad.minContextPeriphery) endValues[startAt + 1] = endValues[startAt] + (double) ad.minContextPeriphery;
                }
                startAt++;
            }
            if (createMax) {
                if (lastRange < ad.minContextPeriphery) {
                    System.out.println("Area after last range might be squished too small");
                    endValues[endAt - 1] = startValues[endAt - 1];
                } else {
                    endValues[endAt - 1] = startValues[endAt] - lastRange * totalNonExtentRatio;
                    if (endValues[endAt] - endValues[endAt - 1] < ad.minContextPeriphery) endValues[endAt - 1] = endValues[endAt] - (double) ad.minContextPeriphery;
                }
                endAt--;
            }
            for (int i = startAt; i <= endAt - 2; i += 2) {
                endValues[i + 1] = startValues[i + 1] - startValues[i];
                endValues[i + 1] *= totalExtentRatio;
                endValues[i + 1] += endValues[i];
                endValues[i + 2] = startValues[i + 2] - startValues[i + 1];
                endValues[i + 2] *= totalNonExtentRatio;
                endValues[i + 2] += endValues[i + 1];
                if (endValues[i + 2] - endValues[i + 1] < ad.minContextInside) {
                    double halfAddBack = ((startValues[i + 2] - startValues[i + 1]) - ad.minContextInside) / 2;
                    if (halfAddBack > 0) {
                        endValues[i + 1] = startValues[i + 1] + halfAddBack;
                        endValues[i + 2] = startValues[i + 2] - halfAddBack;
                    } else {
                        System.out.println("Not moving, min context inside is too small: " + (endValues[i + 1] - endValues[i]) + " " + (endValues[i + 3] - endValues[i + 2]));
                        endValues[i + 1] = startValues[i + 1];
                        endValues[i + 2] = startValues[i + 2];
                        System.out.println("Real: " + (endValues[i + 1] - endValues[i]) + " " + (endValues[i + 3] - endValues[i + 2]));
                    }
                }
            }
        }
        int min = 0, max = lines.length - 1;
        resizeRanges(lines, startValues, endValues, numSteps, this.rootSentinel.leftChild, min, max, newToMove);
    }

    private DynSplitLine getSplitRoot(DynSplitLine min, DynSplitLine split, DynSplitLine max, boolean left) {
        if (left) {
            if (split.leftChild != this.sentinel) return split.leftChild; else return min;
        } else {
            if (split.rightChild != this.sentinel) return split.rightChild; else return max;
        }
    }

    public void resetSplitValue(DynSplitLine cell) {
        DynSplitLine current = cell;
        if (current != this.sentinel) {
            current.splitValue = current.defaultSplitValue;
            resetSplitValue(current.leftChild);
            resetSplitValue(current.rightChild);
        }
    }

    public void computeSplitValue(int frameNum) {
        rootSentinel.leftChild.computedFrame = frameNum;
        rootSentinel.leftChild.absoluteValue = rootSentinel.leftChild.splitValue;
        min.absoluteValue = this.minStuckValue;
        max.absoluteValue = this.maxStuckValue;
        computeSplitValueRecurse(rootSentinel.leftChild.leftChild, true, frameNum);
        computeSplitValueRecurse(rootSentinel.leftChild.rightChild, false, frameNum);
    }

    private void computeSplitValueRecurse(DynSplitLine line, boolean left, int frameNum) {
        line.computedFrame = frameNum;
        if (line == this.sentinel) return;
        if (left) {
            line.absoluteValue = line.parent.minLine.absoluteValue + (line.parent.absoluteValue - line.parent.minLine.absoluteValue) * line.splitValue;
        } else {
            line.absoluteValue = (line.parent.maxLine.absoluteValue - line.parent.absoluteValue) * line.splitValue + line.parent.absoluteValue;
        }
        computeSplitValueRecurse(line.leftChild, true, frameNum);
        computeSplitValueRecurse(line.rightChild, false, frameNum);
    }
}
