package com.mockturtlesolutions.snifflib.guitools.components;

import javax.swing.tree.*;
import java.util.Comparator;

/**
Provides a tree model for render and selection of domain names.
*/
public class DomainTreeModel extends DefaultTreeModel {

    private Comparator comparator;

    public DomainTreeModel(TreeNode node, Comparator c) {
        super(node);
        this.comparator = c;
    }

    public DomainTreeModel(TreeNode node, boolean asksAllowsChildren, Comparator c) {
        super(node, asksAllowsChildren);
        this.comparator = c;
    }

    /**
	Inserts a domain name node into the tree.
	*/
    public void insertNodeInto(MutableTreeNode child, MutableTreeNode parent) {
        int index = findIndexFor(child, parent);
        super.insertNodeInto(child, parent, index);
    }

    /**
	Inserts a domain name node into the tree at position i.
	*/
    public void insertNodeInto(MutableTreeNode child, MutableTreeNode parent, int i) {
        this.insertNodeInto(child, parent);
    }

    /**
	Finds a domain name node in the tree.
	*/
    private int findIndexFor(MutableTreeNode child, MutableTreeNode parent) {
        int cc = parent.getChildCount();
        if (cc == 0) {
            return (0);
        }
        if (cc == 1) {
            return (comparator.compare(child, parent.getChildAt(0)) <= 0 ? 0 : 1);
        }
        return (this.findIndexFor(child, parent, 0, cc - 1));
    }

    /**
	Finds node 
	*/
    private int findIndexFor(MutableTreeNode child, MutableTreeNode parent, int i1, int i2) {
        if (i1 == i2) {
            return (comparator.compare(child, parent.getChildAt(i1)) <= 0 ? i1 : i1 + 1);
        }
        int half = (i1 + i2) / 2;
        if (comparator.compare(child, parent.getChildAt(half)) <= 0) {
            return (this.findIndexFor(child, parent, i1, half));
        }
        return (this.findIndexFor(child, parent, half + 1, i1));
    }
}
