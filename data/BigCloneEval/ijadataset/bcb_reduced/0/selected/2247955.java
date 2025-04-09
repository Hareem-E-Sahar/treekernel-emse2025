package org.dcopolis.util;

import java.util.*;
import java.lang.reflect.*;

/**
 * A data structure for representing rooted ordered trees.
 *
 * @author <a href="http://www.sultanik.com/" target="_blank">Evan A. Sultanik</a>
 */
@SuppressWarnings("unchecked")
public class Tree extends Graph<Tree> implements Vertex<Tree> {

    Tree parent;

    LinkedList<Tree> children;

    int depth;

    Tree leftSibling;

    LinkedList<Integer> depthSequence;

    int numNodes;

    String label;

    Hashtable<String, Tree> nodesByLabel;

    HashSet<Tree> vertices;

    /**
     * Constructs a new single node tree.
     */
    public Tree() {
        this((Tree) null);
    }

    public Tree getVertex() {
        return this;
    }

    public Set<Tree> getVertices() {
        return vertices;
    }

    /**
     * Constructs a new node as a child of an existing node.  If
     * <code>parent == null</code> then this node will be the root of
     * a single node tree.
     */
    public Tree(Tree parent) {
        this.parent = parent;
        label = null;
        depthSequence = null;
        leftSibling = null;
        if (parent != null) {
            if (!parent.children.isEmpty()) leftSibling = parent.children.getLast();
            parent.children.add(this);
            nodesByLabel = parent.nodesByLabel;
            vertices = parent.vertices;
            depth = parent.depth + 1;
            parent.resetDepthSequence();
            parent.addToSize(1);
        } else {
            depth = 0;
            nodesByLabel = new Hashtable<String, Tree>();
            vertices = new HashSet<Tree>();
        }
        if (getLabel() != null) nodesByLabel.put(getLabel(), this);
        vertices.add(this);
        children = new LinkedList<Tree>();
        numNodes = 1;
    }

    /**
     * Constructs a new tree from an adjacency matrix representation.
     * Note that it is assumed the adjacency represents a tree; this
     * is not checked.  Passing in an adjacency matrix representing a
     * cyclic graph will result in unexpected consequences, possibly
     * including&mdash;but not limited to&mdash;the apocalypse.
     */
    public Tree(boolean adjacency[][]) {
        this(adjacency, 0, null, new HashSet<Integer>());
    }

    private Tree(boolean adjacency[][], int myIdx, Tree parent, HashSet<Integer> addedNodes) {
        this(parent);
        addedNodes.add(myIdx);
        for (int i = 0; i < adjacency[myIdx].length; i++) if (adjacency[myIdx][i] && !addedNodes.contains(i)) new Tree(adjacency, i, this, addedNodes);
    }

    /**
     * Assigns automatically generated unique labels to the unlabeled
     * nodes in this subtree.
     */
    public void assignLabels() {
        HashSet<String> alreadyUsed = new HashSet<String>();
        for (Tree t : this) if (t.getLabel() != null) alreadyUsed.add(t.getLabel());
        assignLabels(new int[] { 0 }, alreadyUsed);
    }

    private static final char LABEL_CHARS[] = new char[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

    private static int[] getNextLabel(int prevLabel[]) {
        int i;
        i = prevLabel.length - 1;
        while (++prevLabel[i] >= LABEL_CHARS.length) {
            prevLabel[i--] = 0;
            if (i <= 0) {
                int newLabel[] = new int[prevLabel.length + 1];
                for (i = 0; i < newLabel.length; i++) newLabel[i] = 0;
                return newLabel;
            }
        }
        return prevLabel;
    }

    private void assignLabels(int nextLabel[], HashSet<String> alreadyUsed) {
        if (getLabel() == null) {
            String lbl = null;
            do {
                lbl = "";
                for (int i : nextLabel) lbl += LABEL_CHARS[i];
                nextLabel = getNextLabel(nextLabel);
            } while (alreadyUsed.contains(lbl));
            setLabel(lbl);
        }
        for (Tree c : children) c.assignLabels(nextLabel, alreadyUsed);
    }

    /**
     * Constructs a new tree from a string representation.  This is
     * equivalent to <code>Tree(null, tree)</code>.
     *
     * @see Tree#Tree(Tree, String);
     */
    public Tree(String tree) {
        this(null, tree);
    }

    /**
     * Constructs a new tree as a subtree of an existing tree from a
     * string representation.  The syntax for the string is given by
     * the following BNF grammar:<br />
<pre>
&lt;Tree&gt; ::= &lt;Node&gt; | "[" &lt;Children&gt; "]" | ""
&lt;Children&gt; ::= &lt;Tree&gt; | &lt;Tree&gt;&lt;Children&gt;
&lt;Node&gt; ::= "[" &lt;Optional-Whitespace&gt; "]" | &lt;Non-Whitespace-Non-Brace-Character&gt;
</pre>

     * For example, both <code>"[]"</code> and <code>"X"</code> will
     * produce a single node tree.  <code>"[[][]]"</code> will produce
     * a binary tree of three nodes, two of which are children of the
     * root.  <code>"[X X]"</code> is another way of representing
     * <code>"[[][]]"</code>.  The "X" and braces notations can also
     * be intermixed; for example: <code>"[X []]"</code>.
     */
    public Tree(Tree parent, String tree) {
        this(parent);
        char c[] = tree.toCharArray();
        int numBraces = 0;
        boolean foundOpeningBrace = false;
        String currentChild = "";
        for (int i = 0; i < c.length; i++) {
            if (c[i] == '[') {
                foundOpeningBrace = true;
                numBraces++;
                if (numBraces == 2) {
                    currentChild = "[";
                } else if (numBraces > 2) {
                    currentChild += "[";
                }
            } else if (c[i] == ']') {
                numBraces--;
                if (numBraces == 1) {
                    currentChild += "]";
                    new Tree(this, currentChild);
                    currentChild = "";
                } else if (numBraces > 1) {
                    currentChild += "]";
                }
            } else if (numBraces == 1 && c[i] != ' ' && c[i] != '\t') {
                new Tree(this);
            } else {
                currentChild += c[i];
            }
            if (foundOpeningBrace && numBraces <= 0) break;
        }
    }

    /**
     * Constructs a new tree from a preorder depth sequence.  This is
     * equivalent to <code>Tree(null, ordering)</code>.
     *
     * @see Tree#Tree(Tree,int)
     */
    public Tree(int[] ordering) {
        this(null, ordering);
    }

    public void setLabel(String label) {
        this.label = label;
        if (label != null) nodesByLabel.put(label, this);
    }

    public Tree getNodeByLabel(String label) {
        return nodesByLabel.get(label);
    }

    public String getLabel() {
        return label;
    }

    /**
     * Constructs a new subtree from a preorder depth sequence.  For
     * example, the ordering <code>[0, 1, 2, 3, 1, 2]</code> will
     * produce the following tree:<br />
<pre>
  X
 / \
X   X
|   |
X   X
|
X
</pre>
     */
    public Tree(Tree parent, int[] ordering) {
        this(parent, ordering, 0);
    }

    /**
     * Cosntructs a randomly generated rooted tree uniformly chosen
     * from the set of all rooted trees of <code>n</code> nodes.  Note
     * that, to ensure uniformity, this function will generate
     * <em>all</em> trees of <code>n</code> nodes in the process,
     * which will my take a ridiculously long amount of time for large
     * <code>n</code>.
     */
    public static Tree randomTree(int n) {
        return randomTree(n, new Random());
    }

    /**
     * Cosntructs a randomly generated rooted tree uniformly chosen
     * from the set of all rooted trees of <code>n</code> nodes.  Note
     * that, to ensure uniformity, this function will generate
     * <em>all</em> trees of <code>n</code> nodes in the process,
     * which will my take a ridiculously long amount of time for large
     * <code>n</code>.
     */
    public static Tree randomTree(int n, Random rand) {
        LinkedList<Tree> trees = RootedTreeGenerator.findAllChildren(n);
        return trees.get(rand.nextInt(trees.size()));
    }

    /**
     * Constructs a randomly generated tree with <code>n</code> nodes
     * and an average of <code>d</code> children per node.
     */
    public static Tree randomTree(int n, double d) {
        return randomTree(null, n, n, d, new Random());
    }

    /**
     * Returns the average depth of nodes in this subtree.
     */
    public double getAverageDepth() {
        long sum = 0;
        long numNodes = 1;
        LinkedList<Tree> queue = new LinkedList<Tree>(children);
        Tree lastParent = this;
        long currentDepth = 1;
        while (!queue.isEmpty()) {
            numNodes++;
            Tree vo = queue.removeFirst();
            if (vo.getParent() != lastParent) {
                currentDepth++;
                lastParent = vo.getParent();
            }
            sum += currentDepth;
            for (Tree v : vo.getChildren()) queue.addLast(v);
        }
        return (double) sum / (double) numNodes;
    }

    /**
     * Returns the average number of children of nodes in this
     * subtree.
     */
    public double getAverageNumChildren() {
        long sum = 0;
        long numNodes = 0;
        for (Tree t : this) {
            sum += t.getChildren().size();
            numNodes++;
        }
        return (double) sum / (double) numNodes;
    }

    /**
     * Constructs a randomly generated tree with <code>n</code> nodes
     * and an average of <code>d</code> children per node.
     */
    public static Tree randomTree(int n, double d, Random rand) {
        return randomTree(null, n, n, d, rand);
    }

    private static Tree randomTree(Tree parent, int minN, int maxN, double d, Random rand) {
        if (maxN <= 0) return null;
        Tree root = new Tree(parent);
        if (maxN == 1) return root; else if (maxN == 2) {
            new Tree(root);
            return root;
        }
        int currentSize = 1;
        if (d < 1.0) d = 1.0;
        int numChildren = (int) (rand.nextDouble() * d + 0.5);
        if (numChildren < minN - currentSize) numChildren = minN - currentSize; else if (numChildren > maxN - currentSize) numChildren = maxN - currentSize;
        int numCreatedChildren = 0;
        while (currentSize < maxN && (currentSize < minN || numCreatedChildren < numChildren)) {
            int numChildrenLeft = numChildren - numCreatedChildren;
            if (numChildrenLeft <= 0) numChildrenLeft = 1;
            int nodesPerChild = (minN - currentSize) / numChildrenLeft;
            Tree child = randomTree(root, nodesPerChild, maxN - currentSize, d, rand);
            if (child != null) {
                numCreatedChildren++;
                currentSize += child.size();
            }
        }
        return root;
    }

    public int calculateEditDistance(Tree tree) {
        return calculateEditDistance(tree, false);
    }

    /**
     * Calculates the edit distance between this tree and the given
     * tree.  Edit distance is the minimum number of parent-child
     * relationships that must be modified in either tree for the
     * trees to become isomorphic.  Both trees are assumed to be
     * labeled.
     */
    public int calculateEditDistance(Tree tree, boolean debug) {
        int edits = 0;
        HashSet<String> labels = new HashSet<String>();
        for (Tree v : getVertices()) if (v.getLabel() != null) labels.add(v.getLabel());
        for (Tree v : tree.getVertices()) if (v.getLabel() != null) labels.add(v.getLabel());
        if (debug) System.out.println("Labels: " + labels);
        for (String l : labels) {
            if (debug) System.out.println("Label: " + l);
            Tree v1 = getNodeByLabel(l);
            Tree v2 = tree.getNodeByLabel(l);
            if (debug) {
                System.out.println("v1 = " + v1);
                System.out.println("v2 = " + v2);
            }
            if ((v1 != null && v2 == null) || (v1 == null && v2 != null)) edits++; else {
                Tree p1 = v1.getParent();
                Tree p2 = v2.getParent();
                if (debug) {
                    System.out.println("p1 = " + p1);
                    System.out.println("p2 = " + p2);
                }
                if ((p1 == null && p2 != null) || (p1 != null && p2 == null) || (p1 != null && p2 != null && !p1.getLabel().equals(p2.getLabel()))) edits++;
            }
        }
        return edits;
    }

    /**
     * Generates the adjacency matrix of a random, undirected tree.
     *
     * @param n number of vertices
     * @param random a {@link java.util.Random} object using which to
     * generate the random edges.
     *
     * @return the adjacency matrix representation of the graph, or
     * <code>null</code> if <code>n&le;0</code>.  The diagonal of the
     * adjacency matrix will always be <code>false</code>
     * (<em>i.e.</em> the graph will have no loops).
     */
    public static boolean[][] randomTreeAdj(int n, Random random) {
        if (n <= 0) return null;
        boolean g[][] = new boolean[n][n];
        for (int i = 0; i < n; i++) for (int j = 0; j < n; j++) g[i][j] = false;
        int firstVertex = random.nextInt(n);
        ArrayList<Integer> tree = new ArrayList<Integer>();
        tree.add(new Integer(firstVertex));
        ArrayList<Integer> frontier = new ArrayList<Integer>();
        for (int i = 0; i < n; i++) if (i != firstVertex) frontier.add(new Integer(i));
        while (!frontier.isEmpty()) {
            int parent = random.nextInt(tree.size());
            int child = random.nextInt(frontier.size());
            int i = tree.get(parent).intValue();
            int j = frontier.get(child).intValue();
            tree.add(frontier.get(child));
            frontier.remove(child);
            g[i][j] = true;
            g[j][i] = true;
        }
        return g;
    }

    private Tree(Tree parent, int[] ordering, int offset) {
        this(parent);
        if (offset == 0) {
            int depthOffset = getDepth() - ordering[0];
            if (depthOffset != 0) {
                int oldOrdering[] = ordering;
                ordering = new int[oldOrdering.length];
                for (int i = 1; i < ordering.length; i++) ordering[i] = oldOrdering[i] + depthOffset;
            }
        }
        if (++offset >= ordering.length) return;
        if (ordering[offset] > getDepth() + 1) throw new IllegalArgumentException("Invalid depth sequence at index " + offset + " of the sequence " + printSequence(ordering)); else if (ordering[offset] > getDepth()) {
            new Tree(this, ordering, offset);
        } else {
            int generationsBack = getDepth() - ordering[offset];
            Tree newParent = null;
            for (int i = 0; i <= generationsBack; i++) {
                if (i == 0) newParent = parent; else newParent = newParent.getParent();
                if (newParent == null) throw new IllegalArgumentException("Invalid ancestor depth at index " + offset + " of the sequence " + printSequence(ordering));
            }
            new Tree(newParent, ordering, offset);
        }
    }

    static String printSequence(int[] seq) {
        String s = "[";
        for (int i = 0; i < seq.length; i++) {
            if (i > 0) s += ", ";
            s += Integer.toString(seq[i]);
        }
        return s + "]";
    }

    private void resetDepthSequence() {
        resetDepthSequenceUp();
        resetDepthSequenceDown();
    }

    private void resetDepthSequenceUp() {
        depthSequence = null;
        if (!isRoot()) parent.resetDepthSequenceUp();
    }

    private void resetDepthSequenceDown() {
        depthSequence = null;
        if (!isLeaf()) for (Tree c : children) c.resetDepthSequenceDown();
    }

    /**
     * Returns the sequence of depths of nodes in this subtree from a
     * preorder traversal.
     */
    public LinkedList<Integer> getDepthSequence() {
        return getDepthSequence(new LinkedList<Integer>());
    }

    private LinkedList<Integer> getDepthSequence(LinkedList<Integer> seq) {
        if (depthSequence == null) {
            depthSequence = new LinkedList<Integer>();
            depthSequence.add(getDepth());
            for (Tree child : children) child.getDepthSequence(depthSequence);
        }
        seq.addAll(depthSequence);
        return seq;
    }

    public void setParent(Tree parent) {
        if (!isRoot()) {
            ListIterator<Tree> iter = getParent().children.listIterator();
            Tree next = null;
            Tree prev = null;
            while (iter.hasNext() && next == null) {
                Tree t = iter.next();
                if (prev == this) next = t; else prev = t;
            }
            getParent().children.remove(this);
            for (Tree t : this) {
                if (t.getLabel() != null) getParent().nodesByLabel.remove(t.getLabel());
                getParent().vertices.remove(t);
            }
            if (next != null) next.leftSibling = leftSibling;
            getParent().resetDepthSequence();
            getParent().addToSize(0 - size());
            this.parent = null;
            addToDepth(0 - depth);
        }
        parent.addChild(this);
    }

    public void addChild(Tree tree) throws IllegalArgumentException {
        if (!tree.isRoot()) throw new IllegalArgumentException("Only the root of a tree may be added to another node as a child.");
        if (children.isEmpty()) tree.leftSibling = null; else tree.leftSibling = children.getLast();
        children.add(tree);
        tree.parent = this;
        tree.addToDepth(depth + 1);
        addToSize(tree.size());
        resetDepthSequence();
        for (Tree t : tree) {
            if (t.getLabel() != null) {
                nodesByLabel.put(t.getLabel(), t);
                t.nodesByLabel = nodesByLabel;
            }
            vertices.add(t);
            t.vertices = vertices;
        }
    }

    public Tree getLeftSibling() {
        return leftSibling;
    }

    /**
     * Returns the number of nodes in the subtree rooted at this node.
     */
    public int size() {
        return numNodes;
    }

    private void addToSize(int n) {
        numNodes += n;
        if (!isRoot()) getParent().addToSize(n);
    }

    private void addToDepth(int d) {
        depth += d;
        for (Tree child : children) child.addToDepth(d);
    }

    private static class DFSIterator implements Iterator<Tree> {

        Stack<Tree> stack;

        public DFSIterator(Tree tree) {
            stack = new Stack<Tree>();
            stack.push(tree);
        }

        public boolean hasNext() {
            return !stack.isEmpty();
        }

        public Tree next() {
            Tree t = stack.pop();
            for (Tree child : t.children) stack.push(child);
            return t;
        }

        public void remove() {
            throw new UnsupportedOperationException("You cannot remove nodes from a tree!");
        }
    }

    public Iterator<Tree> iterator() {
        return new DFSIterator(this);
    }

    public boolean isRoot() {
        return parent == null;
    }

    public boolean isLeaf() {
        return children == null || children.isEmpty();
    }

    public Tree getRoot() {
        if (isRoot()) return this; else return parent.getRoot();
    }

    /**
     * Returns the depth of this node.
     */
    public int getDepth() {
        return depth;
    }

    /**
     * Returns the depth of the deepest node in the subtree rooted at
     * this node.
     */
    public int getMaximumDepth() {
        int max = getDepth();
        for (Tree c : getChildren()) {
            int d = c.getMaximumDepth();
            if (d > max) max = d;
        }
        return max;
    }

    /**
     * Returns the adjacency matrix for the induced graph of this tree.
     */
    public boolean[][] calculateInducedGraph() {
        if (!isRoot()) return getRoot().calculateInducedGraph();
        boolean g[][] = new boolean[size()][size()];
        for (int i = 0; i < size(); i++) for (int j = 0; j < size(); j++) g[i][j] = false;
        Hashtable<Tree, Integer> ordering = new Hashtable<Tree, Integer>();
        Hashtable<Integer, Tree> nodesByOrder = new Hashtable<Integer, Tree>();
        int order = 0;
        LinkedList<Tree> queue = new LinkedList<Tree>();
        queue.addLast(this);
        while (!queue.isEmpty()) {
            Tree tree = queue.removeFirst();
            ordering.put(tree, order);
            nodesByOrder.put(order++, tree);
            for (Tree child : tree.children) queue.addLast(child);
        }
        for (int i = order - 1; i >= 0; i--) {
            Tree node = nodesByOrder.get(i);
            Tree parent = node.getParent();
            if (parent != null) g[ordering.get(parent)][i] = true;
        }
        return g;
    }

    /**
     * Calculates the induced width of this tree
     */
    public int calculateInducedWidth() {
        boolean g[][] = calculateInducedGraph();
        int max = 0;
        for (int i = 0; i < g.length; i++) {
            int count = 0;
            for (int j = 0; j < g.length; j++) if (g[j][i]) count++;
            if (count > max) max = count;
        }
        return max;
    }

    public <T extends Tree> T cloneToType(Class<T> treeclass) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, SecurityException, NoSuchMethodException {
        return cloneToType(null, treeclass);
    }

    public Tree clone() {
        try {
            return cloneToType((Class<? extends Tree>) this.getClass());
        } catch (Exception e) {
            e.printStackTrace();
            return clone(null);
        }
    }

    public Tree getParent() {
        return parent;
    }

    public LinkedList<Tree> getChildren() {
        return children;
    }

    public boolean isConnected() {
        return true;
    }

    public Set<Tree> getNeighbors() {
        HashSet<Tree> n = new HashSet<Tree>(getChildren());
        if (!isRoot()) n.add(getParent());
        return n;
    }

    private <T extends Tree> T cloneToType(T newParent, Class<T> treeclass) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, SecurityException, NoSuchMethodException {
        if (treeclass == null || treeclass.equals(this.getClass())) return (T) clone(newParent);
        Constructor<T> cons = treeclass.getConstructor(treeclass);
        T ret = cons.newInstance(newParent);
        for (Tree child : children) child.cloneToType(ret, treeclass);
        if (getLabel() != null) ret.setLabel(getLabel());
        return ret;
    }

    private Tree clone(Tree newParent) {
        Tree ret = new Tree(newParent);
        for (Tree child : children) child.clone(ret);
        if (getLabel() != null) ret.setLabel(getLabel());
        return ret;
    }

    /**
     * Returns the maximum depth in the tree.
     */
    private int assignLevels(Hashtable<Integer, LinkedList<Tree>> levels) {
        int ourDepth = getDepth();
        LinkedList<Tree> children = levels.get(ourDepth);
        if (children == null) {
            children = new LinkedList<Tree>();
            levels.put(ourDepth, children);
        }
        children.add(this);
        int maxDepth = ourDepth;
        for (Tree child : this.children) {
            int d = child.assignLevels(levels);
            if (d > maxDepth) maxDepth = d;
        }
        return maxDepth;
    }

    public Tree getLeftmostLeaf() {
        if (isLeaf()) return this; else return children.getFirst().getLeftmostLeaf();
    }

    public Tree getRightmostLeaf() {
        if (isLeaf()) return this; else return children.getLast().getRightmostLeaf();
    }

    /**
     * Returns a list of vertices <i>r<sub>i</sub></i> for 0 &le;
     * <i>i</i> &le; <i>k</i> where <i>r</i><sub>0</sub> is this node,
     * <i>r<sub>k</sub></i> is a leaf, and <i>r<sub>i</i>+1</sub> is
     * the rightmost child of <i>r<sub>i</sub></i>.
     */
    public LinkedList<Tree> getRightmostPath() {
        return getRightmostPath(new LinkedList<Tree>());
    }

    /**
     * Returns the node that would be returned previous to this node
     * in a preorder (DFS) traversal of this tree.  <code>null</code>
     * is returned if and only if <code>this.isRoot()</code>.  This
     * function runs in worst case linear time with respect to the
     * depth of the tree and best case constant time.  I'm guessing
     * that it will run in average case logarithmic time with respect
     * to the number of nodes in the tree, but I don't care to do the
     * calculation at the moment.
     */
    public Tree getPreorderNeighbor() {
        if (isRoot()) return null; else if (getParent().getChildren().getFirst() == this) return getParent(); else return getLeftSibling().getLeftmostLeaf();
    }

    LinkedList<Tree> getRightmostPath(LinkedList<Tree> list) {
        list.add(this);
        if (children.isEmpty()) return list; else return children.getLast().getRightmostPath(list);
    }

    /**
     * Returns a list of vertices <i>r<sub>i</sub></i> for 0 &le;
     * <i>i</i> &le; <i>k</i> where <i>r</i><sub>0</sub> is this node,
     * <i>r<sub>k</sub></i> is a leaf, and <i>r<sub>i</i>+1</sub> is
     * the leftmost child of <i>r<sub>i</sub></i>.
     */
    public LinkedList<Tree> getLeftmostPath() {
        return getLeftmostPath(new LinkedList<Tree>());
    }

    private LinkedList<Tree> getLeftmostPath(LinkedList<Tree> list) {
        list.add(this);
        if (children.isEmpty()) return list; else return children.getFirst().getLeftmostPath(list);
    }

    private static class Tuple implements Comparable<Tuple> {

        private int values[];

        int hc = 0;

        public Tuple(int maxDepth) {
            values = new int[maxDepth + 1];
            for (int i = 0; i <= maxDepth; i++) {
                values[i] = 0;
            }
            hc = 0;
        }

        public void addComponent(int value) {
            values[value]++;
            hc += value;
        }

        public int hashCode() {
            return hc;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Tuple)) return false;
            Tuple t = (Tuple) o;
            if (t.values.length != values.length) return false;
            for (int i = 0; i < values.length; i++) if (values[i] != t.values[i]) return false;
            return true;
        }

        public int compareTo(Tuple t) {
            return hc - t.hc;
        }
    }

    /**
     * Returns a representation of the subtree rooted at this node as
     * the sequence of node depths produced by a preorder (DFS)
     * traversal.
     *
     * @see Tree#getDepthSequence()
     */
    public int[] toDepthSequence() {
        LinkedList<Integer> dseq = getDepthSequence();
        int seq[] = new int[dseq.size()];
        int i = 0;
        for (Integer j : dseq) seq[i++] = j.intValue();
        return seq;
    }

    /**
     * Calculates the index of the rightmost child of the node at
     * index <code>i</code>.  This will run in linear time with
     * respect to the number of children of <code>i</code>.
     *
     * @param depthSequence is the depth sequence representation of a
     * tree.
     *
     * @param i is the index of a node in the depth sequence.
     *
     * @return the index of the rightmost child of node
     * <code>i</code>, or -1 if node <code>i</code> has no children.
     */
    public static int getRightmostChildIndex(int depthSequence[], int i) {
        if (i < 0 || i >= depthSequence.length) return -1;
        int d = depthSequence[i];
        int c = -1;
        for (int j = i + 1; j < depthSequence.length; j++) {
            if (depthSequence[j] < d) break; else if (depthSequence[j] == d + 1) c = j;
        }
        return c;
    }

    /**
     * Returns the path from node <code>i</code> to the rightmost leaf
     * in the subtree rooted at <code>i</code>.  This will run in
     * worst case linear time with respect to
     * <code>depthSequence.length</code>.
     *
     * @param depthSequence is the depth sequence representation of a
     * tree.
     * @param i is the index of a node in the depth sequence.
     * @see Tree#getRightmostPath()
     * @return the path from <code>i</code> to the rightmost leaf in
     * the subtree rooted at <code>i</code> as a sequence of node
     * indexes.
     */
    public static int[] getRightmostPath(int depthSequence[], int i) {
        int c = i;
        LinkedList<Integer> path = new LinkedList<Integer>();
        path.add(c);
        while ((c = getRightmostChildIndex(depthSequence, c)) >= 0) path.add(c);
        int p[] = new int[path.size()];
        c = 0;
        for (Integer j : path) p[c++] = j.intValue();
        return p;
    }

    /**
     * Returns the index of the rightmost leaf in the subtree rooted
     * at node <code>i</code>.  The return value equals <code>i</code>
     * if and only if <code>i</code> is a leaf.  This function is
     * useful for returning the index of the last node in the subtree
     * rooted at <code>i</code>.
     */
    public static int getRightmostLeafIndex(int depthSequence[], int i) {
        int j = getRightmostChildIndex(depthSequence, i);
        if (j >= 0) return getRightmostLeafIndex(depthSequence, j); else return i;
    }

    /**
     * Returns the depth sequence produced by adding a new node as the
     * rightmost child of the node at index <code>i</code>.
     */
    public static int[] addChild(int depthSequence[], int i) {
        int newSeq[] = new int[depthSequence.length + 1];
        int newIdx = getRightmostChildIndex(depthSequence, i) + 1;
        if (newIdx <= 0) newIdx = i + 1;
        for (int j = 0; j < newSeq.length; j++) {
            if (j < newIdx) newSeq[j] = depthSequence[j]; else if (j == newIdx) newSeq[j] = depthSequence[i] + 1; else newSeq[j] = depthSequence[j - 1];
        }
        return newSeq;
    }

    /**
     * Checks whether or not the subtree rooted at this node is a
     * path.
     */
    public boolean isPath() {
        if (isLeaf()) return true; else if (getChildren().size() == 1) return getChildren().getFirst().isPath(); else return false;
    }

    /**
     * Removes one of our children.  <code>child</code> and the
     * subtree rooted at it will become a tree completely disjoint
     * from <code>this</code>.
     *
     * @see Tree#detatchFromParent()
     */
    public void removeChild(Tree child) {
        if (!children.remove(child)) return;
        child.parent = null;
        child.nodesByLabel = new Hashtable<String, Tree>();
        child.vertices = new HashSet<Tree>();
        for (Tree t : child) {
            t.nodesByLabel = child.nodesByLabel;
            if (t.getLabel() != null) {
                nodesByLabel.remove(t.getLabel());
                t.nodesByLabel.put(t.getLabel(), t);
            }
            vertices.remove(t);
            t.vertices = child.vertices;
            t.vertices.add(t);
        }
        addToSize(0 - child.size());
        child.addToDepth(0 - child.getDepth());
        resetDepthSequence();
        child.resetDepthSequence();
    }

    /**
     * Detaches this node from its parent, producing a separate tree
     * rooted at <code>this</code>.  This is equivalent to
     * <code>getParent().removeChild(this)</code>.
     *
     * @see Tree#removeChild(Tree)
     */
    public void detachFromParent() {
        if (!isRoot()) getParent().removeChild(this);
    }

    /**
     * Returns the index of the left sibling of a node in a tree that
     * is represented by a depth sequence.
     *
     * @return the left sibling's index in the depth sequence or -1 if
     * node <code>i</code> is the leftmost child.
     */
    public static int getLeftSibling(int depthSequence[], int i) {
        if (i <= 0) return -1;
        for (int j = i - 1; j >= 0; j--) {
            if (depthSequence[j] == depthSequence[i]) return j; else if (depthSequence[j] < depthSequence[i]) break;
        }
        return -1;
    }

    private static class TreeData {

        public Hashtable<Integer, LinkedList<Tree>> levels;

        public LinkedList<Tree> l;

        public int maxDepth;

        public Hashtable<Tree, Tuple> tuples;

        public Hashtable<Tree, Integer> integers;

        public TreeData(Tree tree) {
            levels = new Hashtable<Integer, LinkedList<Tree>>();
            maxDepth = tree.assignLevels(levels);
            l = new LinkedList<Tree>(levels.get(maxDepth));
            tuples = new Hashtable<Tree, Tuple>();
            integers = new Hashtable<Tree, Integer>();
        }
    }

    public boolean isIsomorphic(Tree tree) {
        TreeData td1 = new TreeData(this);
        TreeData td2 = new TreeData(tree);
        if (td1.maxDepth != td2.maxDepth) return false;
        for (int i = td1.maxDepth; i >= 0; i--) {
            for (int j = 0; j < 2; j++) {
                TreeData td = (j == 0 ? td1 : td2);
                for (Tree v : td.l) {
                    Integer vi;
                    vi = td.integers.get(v);
                    if (vi == null) {
                        vi = 0;
                        td.integers.put(v, vi);
                    }
                    if (v.getParent() != null) {
                        Tuple parentTuple = td.tuples.get(v.getParent());
                        if (parentTuple == null) parentTuple = new Tuple(td.maxDepth);
                        td.tuples.remove(parentTuple);
                        parentTuple.addComponent(vi);
                        td.tuples.put(v.getParent(), parentTuple);
                    }
                }
            }
            PriorityQueue<Tuple> s1 = new PriorityQueue<Tuple>();
            PriorityQueue<Tuple> s2 = new PriorityQueue<Tuple>();
            TreeMap<Tuple, LinkedList<Tree>> sh1 = new TreeMap<Tuple, LinkedList<Tree>>();
            TreeMap<Tuple, LinkedList<Tree>> sh2 = new TreeMap<Tuple, LinkedList<Tree>>();
            LinkedList<Tree> leaves1 = new LinkedList<Tree>();
            LinkedList<Tree> leaves2 = new LinkedList<Tree>();
            for (int j = 0; j < 2; j++) {
                TreeData td = (j == 0 ? td1 : td2);
                for (Tree v : td.levels.get(i)) {
                    if (v.isLeaf()) {
                        if (j == 0) leaves1.add(v); else leaves2.add(v);
                        continue;
                    }
                    Tuple t = td.tuples.get(v);
                    if (t == null) throw new RuntimeException("Assertion Fail: node does not have a tuple yet!");
                    if (j == 0) {
                        s1.add(t);
                        LinkedList<Tree> l = sh1.get(t);
                        if (l == null) {
                            l = new LinkedList<Tree>();
                            sh1.put(t, l);
                        }
                        l.add(v);
                    } else {
                        s2.add(t);
                        LinkedList<Tree> l = sh2.get(t);
                        if (l == null) {
                            l = new LinkedList<Tree>();
                            sh2.put(t, l);
                        }
                        l.add(v);
                    }
                }
            }
            if (s1.size() != s2.size()) return false;
            while (!s1.isEmpty() && !s2.isEmpty()) {
                Tuple t1 = s1.poll();
                Tuple t2 = s2.poll();
                if (!t1.equals(t2)) return false;
            }
            for (int j = 0; j < 2; j++) {
                TreeData td = (j == 0 ? td1 : td2);
                td.l = new LinkedList<Tree>();
                int k = 1;
                for (Tuple t : (j == 0 ? sh1.keySet() : sh2.keySet())) {
                    for (Tree v : (j == 0 ? sh1.get(t) : sh2.get(t))) {
                        td.integers.put(v, k);
                        td.l.add(v);
                    }
                    k++;
                }
                for (Tree leaf : (j == 0 ? leaves1 : leaves2)) td.l.add(leaf);
            }
        }
        return td1.integers.get(this).equals(td2.integers.get(tree));
    }

    public int numLeaves() {
        if (isLeaf()) return 1;
        int sum = 0;
        for (Tree c : children) sum += c.numLeaves();
        return sum;
    }

    public int maxDepth() {
        if (isLeaf()) return 0;
        int max = 1;
        for (Tree c : children) {
            int m = c.maxDepth() + 1;
            if (m > max) max = m;
        }
        return max;
    }

    public String prettyPrint() {
        String lines[] = new String[(maxDepth() + 1) * 2 - 1];
        prettyPrintInternal(lines);
        String s = "";
        for (int i = 0; i < lines.length; i++) s += (lines[i] == null ? "" : lines[i]) + (i < lines.length - 1 ? "\n" : "");
        return s;
    }

    private int prettyPrintInternal(String[] lines) {
        int myLine = depth * 2;
        if (lines[myLine] == null) lines[myLine] = "";
        String lbl = getLabel();
        if (lbl == null) lbl = "X";
        if (isLeaf()) {
            if (lines[myLine].equals("")) lines[myLine] = lbl; else lines[myLine] += " " + lbl;
            return lines[myLine].length() - 1;
        }
        int childOffsets[] = new int[children.size()];
        int minChildOffset = childOffsets.length + 1;
        int maxChildOffset = 0;
        int i = 0;
        for (Tree c : children) {
            int o = c.prettyPrintInternal(lines);
            childOffsets[i] = o;
            if (o < minChildOffset) minChildOffset = o;
            if (o > maxChildOffset) maxChildOffset = o;
            i++;
        }
        int myOffset = minChildOffset + (maxChildOffset - minChildOffset) / 2;
        while (lines[myLine].length() < myOffset) lines[myLine] += " ";
        lines[myLine] += lbl;
        myOffset = lines[myLine].length() - 1;
        for (i = 0; i < childOffsets.length; i++) {
            char d = '|';
            if (childOffsets[i] < myOffset) d = '/'; else if (childOffsets[i] > myOffset) d = '\\';
            if (lines[myLine + 1] == null) lines[myLine + 1] = "";
            while (lines[myLine + 1].length() < childOffsets[i]) lines[myLine + 1] += " ";
            lines[myLine + 1] += d;
        }
        return myOffset;
    }

    public String toString() {
        String s = "[ " + (getLabel() == null ? "X" : getLabel());
        for (Tree child : children) s += " " + child.toString();
        return s + " ]";
    }

    public static void main(String[] args) throws Exception {
        int seq[] = new int[] { 0, 1, 2, 3, 3, 2 };
        Tree t = new Tree(seq);
        System.out.println(t.prettyPrint());
        System.out.println("Size: " + t.size());
        System.out.println("Depth Sequence: " + printSequence(t.toDepthSequence()));
        System.out.println(printSequence(getRightmostPath(t.toDepthSequence(), 0)));
        System.out.println("Node 5's left sibling index: " + getLeftSibling(seq, 5));
    }
}
