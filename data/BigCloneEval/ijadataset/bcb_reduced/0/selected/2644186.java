package simpledb.GUI;

import java.awt.*;
import javax.swing.*;
import java.util.*;

public class DisplayPanel extends JPanel {

    private TreeNode<JTextArea> root;

    private int height;

    public DisplayPanel() {
        super(null);
    }

    public DisplayPanel(TreeNode<JTextArea> root) {
        setRoot(root);
        setPreferredSize(new Dimension(1, 1));
        setLayout(null);
    }

    public TreeNode<JTextArea> getRoot() {
        return root;
    }

    public void setRoot(TreeNode<JTextArea> newRoot) {
        root = newRoot;
        if (root != null) {
            root.getData().setSize(root.getData().getPreferredSize());
        } else {
            removeAll();
            height = 0;
        }
        repaint();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (root != null) {
            refresh(g);
        }
        setPreferredSize(new Dimension((int) getPreferredSize().getWidth(), height));
        revalidate();
    }

    private void refresh(Graphics g) {
        removeAll();
        int sep = (getWidth() - getRoot().getData().getWidth()) / 2;
        int x = sep;
        int y = 10;
        addTree(g);
    }

    private void addTree(Graphics g) {
        Vector<LinkedList<TreeNode<JTextArea>>> levels = doBreadthFirst();
        int y = 0;
        for (int i = 0; i < levels.size(); i++) {
            LinkedList<TreeNode<JTextArea>> level = levels.get(i);
            adjustLevelSizes(level);
            int prevMaxHeight = 0;
            if (i > 0) {
                LinkedList<TreeNode<JTextArea>> prevLevel = levels.get(i - 1);
                prevMaxHeight = (int) getMaxSize(prevLevel).getHeight();
            }
            int nodesInLevel = level.size();
            int totalLevelWidth = 0;
            for (TreeNode<JTextArea> node : level) {
                totalLevelWidth += node.getData().getWidth();
            }
            int sep = (getWidth() - totalLevelWidth) / (nodesInLevel + 1);
            int x = 0;
            y += prevMaxHeight + 20;
            for (TreeNode<JTextArea> node : level) {
                int nodeWidth = node.getData().getWidth();
                if (node.getParent() != null && node.getParent().getChildren().size() == 1) {
                    int parentX = node.getParent().getData().getX();
                    int parentWidth = node.getParent().getData().getWidth();
                    x = parentX + (parentWidth - nodeWidth) / 2;
                } else {
                    x += sep;
                }
                Component data = node.getData();
                data.setLocation(x, y);
                if (i > 0) {
                    Component parentData = node.getParent().getData();
                    int linkX1 = parentData.getX() + parentData.getWidth() / 2;
                    int linkY1 = parentData.getY() + parentData.getHeight() / 2;
                    int linkX2 = data.getX() + data.getWidth() / 2;
                    int linkY2 = data.getY() + data.getHeight() / 2;
                    g.drawLine(linkX1, linkY1, linkX2, linkY2);
                }
                add(data);
                x += nodeWidth;
                height = y + data.getHeight() + 20;
            }
        }
    }

    private void normalize(LinkedList<TreeNode<JTextArea>> level) {
        Dimension maxDim = getMaxSize(level);
        for (TreeNode<JTextArea> node : level) {
            node.getData().setSize(maxDim);
        }
    }

    private Dimension getMaxSize(LinkedList<TreeNode<JTextArea>> level) {
        int maxWidth = 0, maxHeight = 0;
        for (TreeNode<JTextArea> node : level) {
            int nodeWidth = node.getData().getWidth();
            int nodeHeight = node.getData().getHeight();
            if (nodeWidth > maxWidth) {
                maxWidth = nodeWidth;
            }
            if (nodeHeight > maxHeight) {
                maxHeight = nodeHeight;
            }
        }
        return new Dimension(maxWidth, maxHeight);
    }

    private void adjustLevelSizes(LinkedList<TreeNode<JTextArea>> level) {
        for (TreeNode<JTextArea> node : level) {
            node.getData().setSize(node.getData().getPreferredSize());
        }
    }

    private Vector<LinkedList<TreeNode<JTextArea>>> doBreadthFirst() {
        Vector<LinkedList<TreeNode<JTextArea>>> levels = new Vector<LinkedList<TreeNode<JTextArea>>>();
        LinkedList<TreeNode<JTextArea>> level = new LinkedList<TreeNode<JTextArea>>();
        level.add(getRoot());
        while (!level.isEmpty()) {
            levels.add(level);
            level = (LinkedList<TreeNode<JTextArea>>) level.clone();
            int count = level.size();
            for (int i = 0; i < count; i++) {
                TreeNode<JTextArea> front = level.removeFirst();
                if (front.hasChildren()) {
                    Vector<TreeNode<JTextArea>> children = front.getChildren();
                    for (TreeNode<JTextArea> child : children) {
                        level.addLast(child);
                    }
                }
            }
        }
        return levels;
    }
}
