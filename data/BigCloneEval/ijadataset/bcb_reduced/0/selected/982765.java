package ui;

import discspy.concurrency.IBackgroundWorkHolder;
import discspy.Config;
import discspy.filesystemtree.FileInfo;
import discspy.graphics.FilesystemTreeGraphicWrapper;
import discspy.filesystemtree.ItemInfo;
import discspy.graphics.ItemInfoGraphicWrapper;
import discspy.concurrency.DiscspyWorker;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

/**
 *
 * @author michal
 */
public class DirectorySpacePanel extends JPanel {

    private FilesystemTreeGraphicWrapper tree;

    private int mouseDownX;

    private int mouseDownY;

    private IBackgroundWorkHolder getOwnerFrame() {
        return ((IBackgroundWorkHolder) SwingUtilities.getRoot(this));
    }

    private MainView getMainView() {
        return (MainView) SwingUtilities.getRoot(this);
    }

    public DirectorySpacePanel() {
        setBorder(BorderFactory.createLineBorder(Color.black));
        MouseAdapter adapter = new DirectorySpacePanelMouseAdapter();
        addMouseMotionListener(adapter);
        addMouseListener(adapter);
        addMouseWheelListener(adapter);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(250, 200);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setClip(new Rectangle(0, 0, getWidth(), getHeight()));
        if (getTree() != null) {
            getTree().paintAt(g);
        }
    }

    public FilesystemTreeGraphicWrapper getTree() {
        return tree;
    }

    public void setTree(FilesystemTreeGraphicWrapper tree) {
        this.tree = tree;
        this.tree.setMainRectangle(new Rectangle2D.Double(0, 0, this.getWidth(), this.getHeight()));
        repaint();
    }

    public void refreshTree() {
        if (tree != null) {
            tree.setMainRectangle(new Rectangle2D.Double(0, 0, this.getWidth(), this.getHeight()));
            tree.incrementLastRescaleToken();
            tree.calculateItemsPositions();
        }
    }

    public int getMouseDownX() {
        return mouseDownX;
    }

    public void setMouseDownX(int mouseDownX) {
        this.mouseDownX = mouseDownX;
    }

    public int getMouseDownY() {
        return mouseDownY;
    }

    public void setMouseDownY(int mouseDownY) {
        this.mouseDownY = mouseDownY;
    }

    public void setRoot(final ItemInfoGraphicWrapper newRoot) {
        tree.setRootNode(newRoot);
        tree.setDisplayOffsetX(0);
        tree.setDisplayOffsetY(0);
        tree.setDisplayScaleFactor(1);
        final Rectangle2D.Double prev = newRoot.getRectangle();
        if (prev == null) {
            tree.setMainRectangle(new Rectangle2D.Double(0, 0, this.getWidth(), this.getHeight()));
            tree.calculateItemsPositions();
            repaint();
            return;
        }
        tree.incrementLastRescaleToken();
        DiscspyWorker w = new ChangeRootAnimationWorker(getOwnerFrame(), "setRoot", newRoot, prev);
        try {
            w.start();
        } catch (Exception ex) {
            Logger.getLogger(DirectorySpacePanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void restoreRoot() {
        if (getTree() == null) {
            return;
        }
        ItemInfoGraphicWrapper root = getTree().getRootNode();
        while (root.getParent() != null) {
            root = root.getParent();
        }
        setRoot(root);
    }

    public void upOneLevel() {
        if (getTree() == null) {
            return;
        }
        ItemInfoGraphicWrapper root = getTree().getRootNode();
        if (root.getParent() != null) {
            setRoot(root.getParent());
        }
    }

    ItemInfoGraphicWrapper getRoot() {
        return tree.getRootNode();
    }

    /**
     * 
     */
    private class DirectorySpacePanelMouseAdapter extends MouseAdapter {

        public DirectorySpacePanelMouseAdapter() {
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            if (!isEnabled()) {
                return;
            }
            if (getTree() != null) {
                getTree().setMousePoint(e.getPoint());
                repaint();
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if (!isEnabled()) {
                return;
            }
            if (getTree() != null) {
                getTree().setMousePoint(new Point(-100, -100));
                repaint();
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (!isEnabled()) {
                return;
            }
            FilesystemTreeGraphicWrapper tree = getTree();
            if (tree == null) {
                return;
            }
            int offsetX = e.getX() - getMouseDownX();
            int offsetY = e.getY() - getMouseDownY();
            setMouseDownX(e.getX());
            setMouseDownY(e.getY());
            tree.setDisplayOffsetX(tree.getDisplayOffsetX() + offsetX);
            tree.setDisplayOffsetY(tree.getDisplayOffsetY() + offsetY);
            repaint();
        }

        private void updateTreeSelectedNode(FilesystemTreeGraphicWrapper tree) {
            tree.setSelectedNode(tree.getHighlightedNode());
            if (tree.getHighlightedNode().getItem().isDirectory()) {
                setRoot(tree.getHighlightedNode());
            }
        }

        private ArrayList<ItemInfo> getItemsForSelectionPath() {
            ArrayList<ItemInfo> list = new ArrayList<ItemInfo>();
            ItemInfo cur = tree.getSelectedNode().getItem();
            while (cur != null) {
                list.add(0, cur);
                cur = cur.getParent();
            }
            return list;
        }

        /**
         * Tries to execute system open command on selected item.
         * @param tree Tree where item has been selected.
         * @param e MouseEvent that caused opening attempt
         */
        private void tryExecuteSystemCommand(FilesystemTreeGraphicWrapper tree, MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON2) {
                try {
                    if (Desktop.isDesktopSupported() && tree.getSelectedNode().getItem() instanceof FileInfo) {
                        FileInfo info = (FileInfo) tree.getSelectedNode().getItem();
                        Desktop.getDesktop().open(info.getFile());
                    }
                } catch (IOException ex) {
                    Logger.getLogger(DirectorySpacePanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (!isEnabled()) {
                return;
            }
            if (getTree() == null || getTree().getHighlightedNode() == null) {
                return;
            }
            FilesystemTreeGraphicWrapper tree = getTree();
            MainView mv = getMainView();
            updateTreeSelectedNode(tree);
            expandSelectedPath(mv);
            repaint();
            tryExecuteSystemCommand(tree, e);
        }

        /**
         * Expands MainView's left tree to make selected item visible.
         * 
         * @param mv
         */
        private void expandSelectedPath(MainView mv) {
            ArrayList<ItemInfo> list = getItemsForSelectionPath();
            TreePath selectionPath = new TreePath(list.toArray());
            mv.getLeftTree().setSelectionPath(selectionPath);
            mv.getLeftTree().scrollPathToVisible(selectionPath);
            list.remove(list.size() - 1);
            if (list.size() > 0) {
                selectionPath = new TreePath(list.toArray());
                mv.getLeftTree().expandPath(selectionPath);
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (!isEnabled()) {
                return;
            }
            setMouseDownX(e.getX());
            setMouseDownY(e.getY());
        }

        /**
         * Performs scaling on mouse wheel move.
         * @param e
         */
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            if (!isEnabled()) {
                return;
            }
            FilesystemTreeGraphicWrapper tree = getTree();
            if (tree == null) {
                return;
            }
            int amount = e.getWheelRotation() * (-1);
            double scale = tree.getDisplayScaleFactor();
            double scaledAmount = (double) amount * scale / (10.0);
            double scaledAmountInverted = scaledAmount * (-1.0);
            double rx = ((double) e.getX() - tree.getDisplayOffsetX()) / tree.getDisplayScaleFactor();
            double ry = ((double) e.getY() - tree.getDisplayOffsetY()) / tree.getDisplayScaleFactor();
            double offsetX = rx * scaledAmountInverted + tree.getDisplayOffsetX();
            double offsetY = ry * scaledAmountInverted + tree.getDisplayOffsetY();
            tree.setDisplayScaleFactor(tree.getDisplayScaleFactor() + scaledAmount);
            tree.setDisplayOffsetX(offsetX);
            tree.setDisplayOffsetY(offsetY);
            tree.calculateItemsPositions();
            repaint();
        }
    }

    /**
     * BackgroundWorker that performs animation of filesystem tree's root change.
     */
    private class ChangeRootAnimationWorker extends DiscspyWorker {

        /**
         * Number of animation steps.
         */
        private final int numSteps;

        /**
         * New root set at tree.
         */
        private final ItemInfoGraphicWrapper newRoot;

        /**
         * Horizontal position value that new root's graphical representation
         * changes during each animation step.
         */
        private final double stepX;

        /**
         * Vertical position value that new root's graphical representation
         * changes during each animation step.
         */
        private final double stepY;

        /**
         * Width value that new root's graphical representation
         * changes during each animation step.
         */
        private final double stepWidth;

        /**
         * Height value that new root's graphical representation
         * changes during each animation step.
         */
        private final double stepHeight;

        /**
         * Creates new instance of ChangeRootAnimationWorker.
         * 
         * @param hld Holder that will hold animation.
         * @param description Description of background worker.
         * @param newRoot New root node that was set to tree
         * @param prev Previous (before root change) rectangle of new root's
         * graphical representation.
         */
        public ChangeRootAnimationWorker(IBackgroundWorkHolder hld, String description, ItemInfoGraphicWrapper newRoot, Rectangle2D.Double prev) {
            super(hld, description);
            numSteps = Config.getInstance().getEnlargeAnimationSteps();
            stepX = (0 - prev.getX()) / numSteps;
            stepY = (0 - prev.getY()) / numSteps;
            stepWidth = (getWidth() - prev.getWidth()) / numSteps;
            stepHeight = (getHeight() - prev.getHeight()) / numSteps;
            this.newRoot = newRoot;
        }

        /**
         * Performs animation.
         * @return Always null.
         */
        @Override
        protected Object doInBackground() {
            try {
                for (int i = 0; i < numSteps; i++) {
                    Thread.sleep(Config.getInstance().getEnlargeAnimationDelay());
                    Rectangle2D.Double cur = newRoot.getRectangle();
                    cur.x = cur.x + stepX;
                    cur.y = cur.y + stepY;
                    cur.width = cur.width + stepWidth;
                    cur.height = cur.height + stepHeight;
                    tree.setMainRectangle(cur);
                    tree.incrementLastRescaleToken();
                    repaint();
                }
            } catch (InterruptedException e) {
                return null;
            }
            return null;
        }

        /**
         * Schedules final repaint action on event dispatch thread.
         */
        @Override
        public void done() {
            super.done();
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    tree.incrementLastRescaleToken();
                    repaint();
                }
            });
        }
    }
}
