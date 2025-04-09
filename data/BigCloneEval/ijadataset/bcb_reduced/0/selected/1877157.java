package com.intersys.bio.paralogs.model;

import javax.persistence.Entity;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.jalapeno.annotations.Access;
import com.jalapeno.annotations.AccessType;

@Entity
@Access(type = AccessType.FIELD)
public class PTreeNode extends BaseTreeNode {

    public int pId = -1;

    public boolean isParalog;

    public SpeciesTreeNode species;

    public NodeType nodeType;

    public double minage;

    public double maxage;

    public PTreeNode(PTree tree) {
        super(tree);
    }

    public PTreeNode() {
    }

    public void calculateInnerSet(Map<BaseTreeNode, Set<String>> innerSets) {
        Set<String> innerSet = new HashSet<String>();
        if (isTerminal()) {
            innerSet.add(getSpecies());
        } else for (BaseTreeNode child : children) {
            PTreeNode node = (PTreeNode) child;
            node.calculateInnerSet(innerSets);
            innerSet.addAll(innerSets.get(node));
        }
        innerSets.put(this, innerSet);
    }

    public double distanceFromDuplication() {
        double d = 0;
        for (BaseTreeNode cur = this; ; cur = cur.parent) {
            if (cur == null) {
                d = -1;
                break;
            }
            PTreeNode node = (PTreeNode) cur;
            if (node.nodeType == NodeType.duplication) break;
            d += cur.length;
        }
        return d;
    }

    private void matchSpecies(Map<BaseTreeNode, Set<String>> innerSets) {
        if (species != null) {
            minage = maxage = species.age;
            return;
        }
        Dataset ds = tree.getDataset();
        Set<String> myset = innerSets.get(this);
        Map<BaseTreeNode, Set<String>> speciesSets = ds.getSpeciesTree().innerSets();
        SpeciesTree speciesTree = ds.getSpeciesTree();
        for (BaseTreeNode n : speciesTree.getNodes()) {
            SpeciesTreeNode sn = (SpeciesTreeNode) n;
            Set<String> nodeSet = speciesSets.get(sn);
            if (myset.equals(nodeSet)) {
                species = sn;
                minage = maxage = species.age;
                return;
            }
        }
        PTreeNode ancestor = (PTreeNode) parent;
        while (ancestor != null) {
            myset = innerSets.get(ancestor);
            for (BaseTreeNode n : speciesTree.getNodes()) {
                SpeciesTreeNode sn = (SpeciesTreeNode) n;
                Set<String> nodeSet = speciesSets.get(sn);
                if (myset.equals(nodeSet)) {
                    maxage = sn.age;
                    ancestor = null;
                    break;
                }
            }
            if (ancestor != null) ancestor = (PTreeNode) ancestor.parent;
        }
        minage = 0;
        for (BaseTreeNode n : children) {
            PTreeNode child = (PTreeNode) n;
            if (child.minage > minage) minage = child.minage;
        }
    }

    protected void calculateIsParalog(Map<BaseTreeNode, Set<String>> innerSets, Set<String> parentOuterSet) {
        Set<String> outerSet = new HashSet<String>(parentOuterSet);
        if (!isRoot()) for (BaseTreeNode sibling : parent.children) {
            if (this != sibling) {
                Set<String> siblingInnerSet = innerSets.get(sibling);
                outerSet.addAll(siblingInnerSet);
            }
        }
        length = -1;
        if (!isTerminal()) {
            for (BaseTreeNode ch : children) {
                PTreeNode child = (PTreeNode) ch;
                child.calculateIsParalog(innerSets, outerSet);
            }
            int size = children.size();
            nodeType = NodeType.unknown;
            for (int i = 0; i < size; i++) {
                PTreeNode child1 = (PTreeNode) children.get(i);
                Set<String> set1 = innerSets.get(child1);
                if (set1 == null) continue;
                for (int j = i + 1; j < size; j++) {
                    PTreeNode child2 = (PTreeNode) children.get(j);
                    Set<String> set2 = innerSets.get(child2);
                    Set<String> test = new HashSet<String>(set1);
                    test.retainAll(set2);
                    if (test.isEmpty()) {
                        if (nodeType == NodeType.unknown) nodeType = NodeType.speciation; else nodeType = NodeType.both;
                    } else {
                        if (nodeType == NodeType.unknown) nodeType = NodeType.duplication; else nodeType = NodeType.both;
                    }
                }
            }
        }
        matchSpecies(innerSets);
        if (!isTerminal()) {
            for (BaseTreeNode ch : children) {
                PTreeNode child = (PTreeNode) ch;
                double maxlen = maxage - child.minage;
                double minlen = minage - child.maxage;
                child.length = (maxlen + minlen) / 2;
            }
        }
        Set<String> innerSet = innerSets.get(this);
        outerSet.retainAll(innerSet);
        isParalog = !outerSet.isEmpty();
    }

    public String leafToString(boolean verbose) {
        if (verbose) if (pId >= 0) return tree.getProteins()[pId]; else return toString(); else return String.valueOf(pId);
    }

    protected String getSpecies() {
        return species.species;
    }
}
