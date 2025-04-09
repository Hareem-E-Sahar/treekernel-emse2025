package ch.bbv.performancetests.tests;

import java.util.Random;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import ch.bbv.application.Application;
import ch.bbv.dog.DataObjectHandler;
import ch.bbv.performancetests.binarytree.*;

public class TreeUpdateTest {

    private static final double FRAC_MODIFY = 1 / 3d;

    private static final double FRAC_REMOVE = 1 / 10d;

    private static final double FRAC_ADD = 1 / 10d;

    private static final int[] treeDepths = { 6, 9, 13 };

    private Random randomVal, randomIdx;

    private boolean seriallyNumbered;

    private int numberCounter;

    public TreeUpdateTest() {
    }

    @Before
    public void init() {
        randomVal = new Random(System.currentTimeMillis());
        randomIdx = new Random(~System.currentTimeMillis());
        seriallyNumbered = true;
    }

    @Test
    public void test() {
        Log log = LogFactory.getLog(TreeUpdateTest.class);
        log.info("*** Tree Update Test ***");
        DataObjectHandler dataHandler = Application.getApplication().getDataObjectMgr().getDataObjectHandler();
        String dataSource = Application.getApplication().getDataObjectMgr().getDatasource();
        int nModify, nRemove, nAdd;
        for (int depth : treeDepths) {
            int nNodes = (((int) 1 << (depth + 1)) - 1);
            nModify = (int) Math.round(nNodes * FRAC_MODIFY);
            nRemove = (int) Math.round(nNodes * FRAC_REMOVE);
            nAdd = (int) Math.round(nNodes * FRAC_ADD);
            log.info("    Performing test with tree of depth " + depth + " (No. of nodes = " + nNodes + "):");
            log.info("       Creating tree...");
            Forest f = createTree(depth);
            log.info("       DONE");
            log.info("       Saving the newly created tree...");
            dataHandler.store(dataSource, Forest.class, f);
            log.info("       DONE");
            log.info("       Modifying " + nModify + " random node values...");
            modifyRandom(f, nModify);
            log.info("       DONE");
            log.info("       Trying to remove " + nRemove + " random nodes...");
            int nRemoved = removeRandom(f, nRemove);
            log.info("       DONE (actually removed " + nRemoved + " nodes)");
            log.info("       Adding " + nAdd + " nodes to tree...");
            add(f, nAdd);
            log.info("       DONE");
            log.info("       Saving the modified tree...");
            dataHandler.store(dataSource, Forest.class, f);
            log.info("       DONE");
        }
        log.info("*** Tree Update Test terminated ***");
    }

    /**
	 * Create a tree with the specified depth.
	 * @param depth the depth of the tree
	 * @param seriallyNumbered if true, the nodes are numbered serially. The order of
	 *                         their creation and therefore number (Value field) is in preorder odering
	 *  @returns the forest to which the root node of the newly created tree belongs to
	 */
    public Forest createTree(int depth) {
        Forest forest = new Forest();
        Node root = new Node();
        root.setValue(1);
        numberCounter = 1;
        addNode(root, depth, seriallyNumbered);
        forest.addTree(root);
        return forest;
    }

    /**
	 * Modify random entries in each tree within a forest.
	 * @param f the forest containing the trees whose nodes' values shall be modified
	 * @param nEntries the number of entries (i.e. nodes) in each tree to be modified
	 */
    public void modifyRandom(Forest f, int nEntries) {
        for (Node root : f.getTrees()) {
            for (int i = 0; i < nEntries; i++) {
                int nNodes = traverseTreePreorder(root, 0);
                int idx = randomIdx.nextInt(nNodes);
                Node n = getNodeByPreorderIdx(root, new int[] { idx });
                if (n != null) {
                    n.setValue(n.getValue() + 1);
                } else {
                    throw new IndexOutOfBoundsException("Random node modification");
                }
            }
        }
    }

    /**
	 * Add new nodes at random positions in each tree of a particular forest.
	 * @param f the forest containing the trees to which new nodes shall be added
	 * @param nEntries the number of entries (i.e. nodes) to be added
	 */
    public void add(Forest f, int nEntries) {
        for (Node root : f.getTrees()) {
            for (int i = 0; i < nEntries; i += 2) {
                int nNodes = traverseTreePreorder(root, 0);
                int idx = randomIdx.nextInt(nNodes);
                Node n = getNodeByPreorderIdx(root, new int[] { idx });
                if (n != null) {
                    Node nLeft = n.getLeft();
                    Node nRight = n.getRight();
                    Node tmpNode = new Node();
                    addNode(tmpNode, 1, seriallyNumbered);
                    n.setLeft(tmpNode.getLeft());
                    n.getLeft().setLeft(nLeft);
                    if (i < nEntries - 1) {
                        n.setRight(tmpNode.getRight());
                        n.getRight().setRight(nRight);
                    }
                } else {
                    throw new IndexOutOfBoundsException("Adding node");
                }
            }
        }
    }

    /**
	 * Remove nodes at random postions in each tree of a particular forest.
	 * @param f the forest containing the trees from which to remove nodes
	 * @param nEntries the number of entries (i.e. nodes) to be removed (at most)
	 * @returns the actual number of nodes that have been removed
	 */
    public int removeRandom(Forest f, int nEntries) {
        int nRemoved = 0;
        for (Node root : f.getTrees()) {
            for (int i = 0; i < nEntries; i++) {
                int nNodes = traverseTreePreorder(root, 0);
                if (nNodes <= 1) {
                    break;
                }
                int idx = 1 + randomIdx.nextInt(nNodes - 1);
                Node n = getNodeByPreorderIdx(root, new int[] { idx });
                Node parent = getParentOfPreorderIdx(root, null, new int[] { idx });
                if ((n != null) && (parent != null)) {
                    if (parent.getLeft() == n) {
                        parent.setLeft(null);
                    } else {
                        parent.setRight(null);
                    }
                    nRemoved++;
                }
            }
        }
        return nRemoved;
    }

    /**
	 * Constructs a binary tree.
	 * @param node the root node
	 * @param depth the depth of the tree
	 */
    private void addNode(Node node, int depth, boolean seriallyNumbered) {
        if (depth < 1) {
            return;
        }
        Node left = new Node();
        if (seriallyNumbered) {
            left.setValue(++numberCounter);
        } else {
            left.setValue(randomVal.nextInt(1000));
        }
        node.setLeft(left);
        addNode(left, depth - 1, seriallyNumbered);
        Node right = new Node();
        if (seriallyNumbered) {
            right.setValue(++numberCounter);
        } else {
            right.setValue(randomVal.nextInt(1000));
        }
        node.setRight(right);
        addNode(right, depth - 1, seriallyNumbered);
    }

    /**
	 * Traverses the tree in preorder ordering.
	 * @param node the current Node
	 * @param count the current count
	 * @returns the number of traversed nodes
	 */
    private int traverseTreePreorder(Node node, int count) {
        count++;
        if (node.getLeft() != null) {
            count = traverseTreePreorder(node.getLeft(), count);
        }
        if (node.getRight() != null) {
            count = traverseTreePreorder(node.getRight(), count);
        }
        return count;
    }

    /**
	 * Gets the node with the specified index in the tree. The tree nodes are
	 * traversed in preorder, the root having index 0.
	 * @param node the curent node
	 * @param value the index of the node to be returned (value is not preserved!!!)
	 * @returns a reference to the node with the specified index in preorder search mode
	 */
    private Node getNodeByPreorderIdx(Node node, int[] idx) {
        if (idx[0] <= 0) {
            return node;
        } else {
            idx[0]--;
            if (node.getLeft() != null) {
                Node n = getNodeByPreorderIdx(node.getLeft(), idx);
                if (n != null) {
                    return n;
                }
            }
            if (node.getRight() != null) {
                Node n = getNodeByPreorderIdx(node.getRight(), idx);
                if (n != null) {
                    return n;
                }
            }
        }
        return null;
    }

    /**
	 * Gets the parent of the node with the specified index in the tree. The tree nodes are
	 * traversed in preorder, the root having index 0.
	 * @param node the current node
	 * @param parent the parent of the current node
	 * @param value the index of the node whose parent is to be returned (value is not preserved!!!)
	 *  @returns a reference to the parent node or <value>null< value> if the parent was not found
	 */
    private Node getParentOfPreorderIdx(Node node, Node parent, int[] idx) {
        if (idx[0] <= 0) {
            return parent;
        } else {
            idx[0]--;
            if (node.getLeft() != null) {
                Node n = getParentOfPreorderIdx(node.getLeft(), node, idx);
                if (n != null) {
                    return n;
                }
            }
            if (node.getRight() != null) {
                Node n = getParentOfPreorderIdx(node.getRight(), node, idx);
                if (n != null) {
                    return n;
                }
            }
        }
        return null;
    }
}
