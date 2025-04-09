package edu.iastate.aurora.struct;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import edu.iastate.aurora.supertree.InputTripletSumQuerier;
import edu.iastate.aurora.treevisitor.ClusterCollector;
import edu.iastate.aurora.treevisitor.NewickTreeStringConstructor;
import edu.iastate.aurora.treevisitor.PhyloTreeRenamer;
import edu.iastate.aurora.treevisitor.TreeOrderAssigner;
import edu.iastate.aurora.util.ArrayUtil;
import edu.iastate.aurora.util.SetUtil;

public class PhyloTree<T> {

    private String m_Name;

    private PhyloTreeNode<T> m_Root;

    private PhyloTree(String name) {
        m_Name = name;
        m_Root = new PhyloTreeNode<T>();
    }

    public String getName() {
        return m_Name;
    }

    public void setName(String name) {
        m_Name = name;
    }

    public PhyloTreeNode<T> getRoot() {
        return m_Root;
    }

    public void removeNode(PhyloTreeNode<T> subtreeRoot) {
        subtreeRoot.remove();
        if (m_Root.getNumberOfChildren() == 1) {
            PhyloTreeNode<T> newRoot = m_Root.getChild(0);
            newRoot.remove();
            m_Root = newRoot;
        }
    }

    public PhyloTreeNode<T> insertNodeAbove(PhyloTreeNode<T> subtreeRoot, PhyloTreeNode<T> targetNode) {
        PhyloTreeNode<T> newNode = targetNode.insertAbove(subtreeRoot);
        if (targetNode == m_Root) {
            m_Root = newNode;
        }
        return newNode;
    }

    public List<PhyloTreeNode<T>> extractPath(PhyloTreeNode<T> descendent, PhyloTreeNode<T> ancestor) {
        List<PhyloTreeNode<T>> path = new ArrayList<PhyloTreeNode<T>>();
        if (!ancestor.isAncestorOf(descendent)) {
            return path;
        }
        PhyloTreeNode<T> currentNode = descendent;
        while (currentNode != ancestor) {
            path.add(currentNode);
            currentNode = currentNode.getParent();
        }
        path.add(currentNode);
        return path;
    }

    public String toNewickTree() {
        NewickTreeStringConstructor<T> visitor = new NewickTreeStringConstructor<T>();
        m_Root.traversePostOrder(visitor);
        return visitor.getNewickTreeString();
    }

    public List<PhyloTreeNode<T>> cacheAndGetPostOrder() {
        TreeOrderAssigner<T> visitor = new TreeOrderAssigner<T>();
        m_Root.traversePostOrder(visitor);
        return visitor.getOrder();
    }

    public void cacheClusters() {
        ClusterCollector<T> visitor = new ClusterCollector<T>();
        m_Root.traversePostOrder(visitor);
    }

    public static PhyloTree<String> parseNewickTree(String label, String newickTree) throws ParseException {
        PhyloTree<String> tree = new PhyloTree<String>(label);
        PhyloTreeNode<String> currentNode = tree.getRoot();
        StringBuffer currentName = new StringBuffer();
        newickTree = newickTree.trim();
        if (newickTree.indexOf('(') == -1 && newickTree.indexOf(',') == -1 && newickTree.indexOf(')') == -1) {
            extractName(currentNode, newickTree);
        } else {
            char[] ch = newickTree.toCharArray();
            for (int i = 0; i < ch.length; i++) {
                if (ch[i] == '(') {
                    currentNode = currentNode.addChild();
                    currentName.setLength(0);
                } else if (ch[i] == ')') {
                    extractName(currentNode, currentName.toString());
                    currentName.setLength(0);
                    currentNode = currentNode.getParent();
                    if (currentNode == null) {
                        throw new ParseException("Invalid tree: " + newickTree, i);
                    }
                } else if (ch[i] == ',') {
                    extractName(currentNode, currentName.toString());
                    currentName.setLength(0);
                    currentNode = currentNode.getParent();
                    if (currentNode == null) {
                        throw new ParseException("Invalid tree: " + newickTree, i);
                    }
                    currentNode = currentNode.addChild();
                } else {
                    currentName.append(ch[i]);
                }
            }
        }
        tree.cacheClusters();
        return tree;
    }

    private static void extractName(PhyloTreeNode<String> currentNode, String name) {
        name = name.trim();
        if (name.indexOf(':') != -1) {
            name = name.substring(0, name.indexOf(':'));
        }
        currentNode.setName(name);
    }

    public static PhyloTree<Integer> makeRandomBinaryTree(String label, int taxaSize) {
        PhyloTree<Integer> tree = new PhyloTree<Integer>(label);
        List<PhyloTreeNode<Integer>> nodes = new ArrayList<PhyloTreeNode<Integer>>();
        PhyloTreeNode<Integer> root = tree.getRoot();
        root.setName(0);
        nodes.add(root);
        Random random = new Random();
        for (int i = 1; i < taxaSize; i++) {
            PhyloTreeNode<Integer> newLeaf = new PhyloTreeNode<Integer>();
            newLeaf.setName(i);
            int v = random.nextInt(nodes.size());
            PhyloTreeNode<Integer> newInternal = tree.insertNodeAbove(newLeaf, nodes.get(v));
            if (random.nextBoolean()) {
                newInternal.rotateChildren();
            }
            nodes.add(newInternal);
            nodes.add(newLeaf);
        }
        tree.cacheClusters();
        return tree;
    }

    public static <T> PhyloTree<T> makeCherry(String label, T taxumA, T taxumB) {
        PhyloTree<T> tree = new PhyloTree<T>(label);
        PhyloTreeNode<T> root = tree.getRoot();
        root.addLeaf(taxumA);
        root.addLeaf(taxumB);
        return tree;
    }

    public static PhyloTree<Integer> makeRandomLeafAddingTree(String label, int taxaSize, InputTripletSumQuerier inputTripletSum) {
        RandomList<Integer> remainingTaxa = RandomList.makeSequence(taxaSize);
        int taxum1 = remainingTaxa.removeRandom();
        int taxum2 = remainingTaxa.removeRandom();
        PhyloTree<Integer> tree = makeCherry(label, taxum1, taxum2);
        tree.cacheClusters();
        while (!remainingTaxa.isEmpty()) {
            int newTaxum = remainingTaxa.removeRandom();
            PhyloTreeNode<Integer> bestNode = getBestNodeToInsert(tree, newTaxum, inputTripletSum);
            tree.insertNodeAbove(new PhyloTreeNode<Integer>(newTaxum), bestNode);
            tree.cacheClusters();
        }
        return tree;
    }

    private static PhyloTreeNode<Integer> getBestNodeToInsert(PhyloTree<Integer> tree, int newTaxum, InputTripletSumQuerier inputTripletSum) {
        List<PhyloTreeNode<Integer>> postOrder = tree.cacheAndGetPostOrder();
        int[] tripletSumForNode = new int[postOrder.size()];
        for (int i = 0; i < tripletSumForNode.length; i++) {
            tripletSumForNode[i] = calculateTripletSum(tree, postOrder.get(i), newTaxum, inputTripletSum);
        }
        int bestNode = ArrayUtil.maxIndex(tripletSumForNode);
        return postOrder.get(bestNode);
    }

    private static int calculateTripletSum(PhyloTree<Integer> tree, PhyloTreeNode<Integer> insertAbove, int newTaxum, InputTripletSumQuerier inputTripletSum) {
        Set<Integer> clusterBelowSet = insertAbove.getCluster();
        Set<Integer> clusterAboveSet = SetUtil.minus(tree.getRoot().getCluster(), clusterBelowSet);
        int[] clusterBelow = SetUtil.toArray(clusterBelowSet);
        int[] clusterAbove = SetUtil.toArray(clusterAboveSet);
        int tripletSum = 0;
        tripletSum += calculateTripletSumWhenNewTaxumIsDistant(clusterBelow, newTaxum, inputTripletSum);
        tripletSum += calculateTripletSumWhenNewTaxumIsDistant(clusterAbove, newTaxum, inputTripletSum);
        tripletSum += calculateTripletSumWhenNewTaxumIsNotDistant(clusterBelow, clusterAbove, newTaxum, inputTripletSum);
        return tripletSum;
    }

    private static int calculateTripletSumWhenNewTaxumIsDistant(int[] otherTaxa, int newTaxum, InputTripletSumQuerier inputTripletSum) {
        int sum = 0;
        for (int i = 0; i < otherTaxa.length - 1; i++) {
            for (int j = i + 1; j < otherTaxa.length; j++) {
                sum += inputTripletSum.get(otherTaxa[i], otherTaxa[j], newTaxum);
            }
        }
        return sum;
    }

    private static int calculateTripletSumWhenNewTaxumIsNotDistant(int[] closeTaxa, int[] distantTaxa, int newTaxum, InputTripletSumQuerier inputTripletSum) {
        int sum = 0;
        for (int i = 0; i < closeTaxa.length; i++) {
            for (int j = 0; j < distantTaxa.length; j++) {
                sum += inputTripletSum.get(closeTaxa[i], newTaxum, distantTaxa[j]);
            }
        }
        return sum;
    }

    public static PhyloTree<Integer> makeBinaryBalancedTree(String label, int taxaSize) {
        PhyloTree<Integer> tree = new PhyloTree<Integer>(label);
        PhyloTreeNode<Integer> currentNode = tree.getRoot();
        makeBinaryBalancedTree(currentNode, 0, taxaSize);
        tree.cacheClusters();
        return tree;
    }

    private static void makeBinaryBalancedTree(PhyloTreeNode<Integer> root, int begin, int end) {
        if (begin + 1 == end) {
            root.setName(begin);
            return;
        }
        int mid = (begin + end) / 2;
        if (mid > begin) {
            makeBinaryBalancedTree(root.addChild(), begin, mid);
        }
        if (end > mid) {
            makeBinaryBalancedTree(root.addChild(), mid, end);
        }
    }

    public static PhyloTree<Integer> convertToInt(PhyloTree<String> tree, Map<String, Integer> map) {
        PhyloTree<Integer> newTree = new PhyloTree<Integer>(tree.getName());
        PhyloTreeRenamer<String, Integer> visitor = new PhyloTreeRenamer<String, Integer>(newTree.getRoot(), map);
        tree.getRoot().traversePostOrder(visitor);
        newTree.cacheClusters();
        return newTree;
    }

    public static PhyloTree<String> convertToStr(PhyloTree<Integer> tree, Map<Integer, String> map) {
        PhyloTree<String> newTree = new PhyloTree<String>(tree.getName());
        PhyloTreeRenamer<Integer, String> visitor = new PhyloTreeRenamer<Integer, String>(newTree.getRoot(), map);
        tree.getRoot().traversePostOrder(visitor);
        newTree.cacheClusters();
        return newTree;
    }
}
