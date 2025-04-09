package org.armedbear.j;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public final class SidebarBufferTree extends SidebarTree implements Constants, NavigationComponent, ActionListener, KeyListener, MouseListener, MouseMotionListener, PreferencesChangeListener, DragGestureListener, DragSourceListener, DropTargetListener {

    private JPopupMenu popup;

    private int updateFlag;

    private DefaultMutableTreeNode rootNode;

    private Sidebar sidebar;

    private DragSource dragSource;

    private static DragSourceContext dragSourceContext;

    private Buffer draggedBuffer = null;

    private int draggedBufferRow = -1;

    private int scrollY = 0;

    private Runnable scroller = null;

    private static final long[] WAIT_TIMES = new long[] { 200, 175, 150, 125, 100, 75, 50, 37, 25, 15, 10, 5 };

    private boolean alpha = false;

    private boolean reorder = false;

    public SidebarBufferTree(Sidebar sidebar) {
        super(null);
        this.sidebar = sidebar;
        setCellRenderer(new SidebarTreeCellRenderer(sidebar));
        setFocusTraversalKeysEnabled(false);
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        setToolTipText("");
        setRootVisible(false);
        dragSource = DragSource.getDefaultDragSource();
        DragGestureRecognizer dgr = dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
        dgr.setSourceActions(dgr.getSourceActions() & ~InputEvent.BUTTON3_MASK);
        new DropTarget(this, this);
        Preferences p = Editor.preferences();
        if (p != null) {
            updatePreferences(p);
            p.addPreferencesChangeListener(this);
        } else Debug.bug();
    }

    private void initializeTreeStructure() {
        final ArrayList arrayList = new ArrayList();
        for (BufferIterator it = new BufferIterator(); it.hasNext(); ) {
            Buffer buf = it.nextBuffer();
            if (buf.isPrimary()) {
                arrayList.add(buf);
                Buffer secondary = buf.getSecondary();
                if (secondary != null) arrayList.add(secondary);
            }
        }
        rootNode = new DefaultMutableTreeNode("");
        buildTreeFromList(rootNode, arrayList);
        setModel(new DefaultTreeModel(rootNode));
    }

    private void updatePreferences(Preferences p) {
        alpha = p.getBooleanProperty(Property.SORT_BUFFER_LIST);
        if (alpha) reorder = false; else reorder = p.getIntegerProperty(Property.REORDER_BUFFERS) > 1;
    }

    private void buildTreeFromList(DefaultMutableTreeNode node, List buffers) {
        Iterator iter = buffers.iterator();
        while (iter.hasNext()) {
            Object next = iter.next();
            if (next instanceof Buffer) {
                Buffer buffer = (Buffer) next;
                node.add(new DefaultMutableTreeNode(buffer));
            }
        }
    }

    public synchronized void setUpdateFlag(int mask) {
        updateFlag |= mask;
    }

    public synchronized void updateBufferList() {
        if (!SwingUtilities.isEventDispatchThread()) Debug.bug();
        if ((updateFlag & SIDEBAR_BUFFER_LIST_CHANGED) != 0) {
            refresh();
            updateFlag |= SIDEBAR_REPAINT_BUFFER_LIST;
        }
        if ((updateFlag & SIDEBAR_MODIFIED_BUFFER_COUNT) != 0) {
            updateFlag |= SIDEBAR_REPAINT_BUFFER_LIST;
            updateLabel();
        }
        Buffer buffer = sidebar.getEditor().getBuffer();
        if (buffer != getSelectedBuffer()) setSelectedBuffer(buffer); else scrollPathToVisible(getSelectionPath());
        boolean repaint = (updateFlag & SIDEBAR_REPAINT_BUFFER_LIST) != 0;
        updateFlag = 0;
        if (repaint) {
            repaint();
        }
    }

    public Buffer getSelectedBuffer() {
        if (getSelectionCount() > 0) return getBufferFromPath(getSelectionPath()); else return null;
    }

    public Buffer[] getSelectedBuffers() {
        int numSelected = getSelectionCount();
        if (numSelected <= 0) return new Buffer[0];
        Buffer[] buffers = new Buffer[numSelected];
        TreePath[] paths = getSelectionPaths();
        for (int i = 0; i < paths.length; i++) buffers[i] = getBufferFromPath(paths[i]);
        return buffers;
    }

    public int[] getSelectionRows() {
        int[] rows = super.getSelectionRows();
        if (rows == null) rows = new int[0];
        return rows;
    }

    public int getSelectionRow() {
        int rows[] = getSelectionRows();
        if (rows.length > 0) return rows[0];
        return -1;
    }

    private Buffer getBufferFromPath(TreePath path) {
        TreeNode endNode = (TreeNode) path.getLastPathComponent();
        if (endNode instanceof DefaultMutableTreeNode) {
            Object obj = ((DefaultMutableTreeNode) endNode).getUserObject();
            if (obj instanceof Buffer) return (Buffer) obj;
        }
        return null;
    }

    public void setSelectedBuffer(Buffer buffer) {
        DefaultMutableTreeNode node = findNodeForObject(buffer);
        if (node != null) {
            TreePath path = new TreePath(node.getPath());
            setSelectionPath(path);
            scrollPathToVisible(path);
        }
    }

    public void scrollPathToVisible(TreePath path) {
        if (path != null) {
            makeVisible(path);
            Rectangle bounds = getPathBounds(path);
            if (bounds != null) {
                bounds.y = Math.max(bounds.y - 17, 0);
                bounds.height = bounds.height + 34;
                scrollRectToVisible(bounds);
                if (accessibleContext != null) ((AccessibleJTree) accessibleContext).fireVisibleDataPropertyChange();
            }
        }
    }

    public String getLabelText() {
        int total = 0;
        int modified = 0;
        for (BufferIterator it = new BufferIterator(); it.hasNext(); ) {
            ++total;
            if (it.nextBuffer().isModified()) ++modified;
        }
        FastStringBuffer sb = new FastStringBuffer("Buffers");
        sb.append(" (");
        sb.append(total);
        if (modified > 0) {
            sb.append("; ");
            sb.append(modified);
            sb.append(" modified");
        }
        sb.append(')');
        return sb.toString();
    }

    private synchronized void updateLabel() {
        sidebar.setBufferListLabelText(getLabelText());
        updateFlag &= ~SIDEBAR_MODIFIED_BUFFER_COUNT;
    }

    private final Runnable refreshRunnable = new Runnable() {

        public void run() {
            if ((updateFlag & SIDEBAR_MODIFIED_BUFFER_COUNT) != 0) updateLabel();
            initializeTreeStructure();
        }
    };

    public void refresh() {
        if (SwingUtilities.isEventDispatchThread()) refreshRunnable.run(); else SwingUtilities.invokeLater(refreshRunnable);
    }

    public void updatePosition() {
    }

    private void switchToBuffer() {
        Buffer buffer = getSelectedBuffer();
        if (buffer != null) {
            Editor editor = sidebar.getEditor();
            if (Editor.preferences().getIntegerProperty(Property.REORDER_BUFFERS) > 1) editor.makeNext(buffer);
            editor.switchToBuffer(buffer);
            Editor.currentEditor().setFocusToDisplay();
            editor.updateDisplay();
        }
    }

    private void closeBuffers(Buffer[] array) {
        Editor editor = sidebar.getEditor();
        for (int i = 0; i < array.length; i++) editor.maybeKillBuffer(array[i]);
        EditorIterator iter = new EditorIterator();
        while (iter.hasNext()) iter.nextEditor().updateDisplay();
        for (int i = 0; i < Editor.getFrameCount(); i++) {
            Frame frame = Editor.getFrame(i);
            Sidebar sidebar = frame.getSidebar();
            if (sidebar != null) {
                sidebar.setUpdateFlag(SIDEBAR_BUFFER_LIST_ALL);
                SidebarBufferTree sidebarBufferTree = sidebar.getBufferTree();
                if (sidebarBufferTree != null) sidebarBufferTree.updateBufferList();
            }
        }
    }

    private void closeSelectedBuffers() {
        int index = -1;
        int[] rows = getSelectionRows();
        if (rows.length > 0) index = rows[0];
        Buffer[] array = getSelectedBuffers();
        closeBuffers(array);
        if (index >= 0) {
            if (index > getRowCount() - 1) index = getRowCount() - 1;
            setSelectionRow(index);
            scrollRowToVisible(index);
        }
    }

    private void saveSelectedBuffers() {
        Editor editor = sidebar.getEditor();
        Buffer[] array = getSelectedBuffers();
        for (int i = 0; i < array.length; i++) editor.save(array[i]);
        Sidebar.repaintBufferListInAllFrames();
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command.equals("close")) closeSelectedBuffers(); else if (command.equals("save")) saveSelectedBuffers();
    }

    private void showPopup(Component c, int x, int y) {
        popup = new JPopupMenu();
        TreePath path = getPathForLocation(x, y);
        String bufferName = null;
        if (path != null) {
            Object last = path.getLastPathComponent();
            Object value = ((DefaultMutableTreeNode) last).getUserObject();
            if (value instanceof Buffer) {
                bufferName = last.toString();
            }
        }
        JMenuItem menuItem = new JMenuItem("Save");
        menuItem.setActionCommand("save");
        menuItem.addActionListener(this);
        popup.add(menuItem);
        menuItem = new JMenuItem("Close");
        menuItem.setActionCommand("close");
        menuItem.addActionListener(this);
        popup.add(menuItem);
        popup.pack();
        popup.show(c, x, y);
    }

    public String getToolTipText(MouseEvent e) {
        String text = null;
        Point p = e.getPoint();
        int index = getRowForLocation(p.x, p.y);
        if (index >= 0 && index < getRowCount()) {
            TreePath path = getPathForRow(index);
            Buffer buffer = getBufferFromPath(path);
            if (buffer == null) return null;
            File file = buffer.getFile();
            if (file != null) text = file.isRemote() ? file.netPath() : file.canonicalPath(); else text = buffer.getTitle();
        }
        return text;
    }

    public synchronized void preferencesChanged() {
        Preferences p = Editor.preferences();
        if (p != null) {
            updatePreferences(p);
        } else {
            Debug.bug();
        }
    }

    private DefaultMutableTreeNode findNodeForObject(Object userObj) {
        if (rootNode != null) {
            Enumeration enumeration = rootNode.breadthFirstEnumeration();
            while (enumeration.hasMoreElements()) {
                Object next = enumeration.nextElement();
                if (next instanceof DefaultMutableTreeNode) {
                    Object obj = ((DefaultMutableTreeNode) next).getUserObject();
                    if (userObj.equals(obj)) return (DefaultMutableTreeNode) next;
                }
            }
        }
        return null;
    }

    public void keyPressed(KeyEvent e) {
        final int keyCode = e.getKeyCode();
        final int modifiers = e.getModifiers() & 0x0f;
        final Editor editor = sidebar.getEditor();
        switch(keyCode) {
            case KeyEvent.VK_SHIFT:
            case KeyEvent.VK_CONTROL:
            case KeyEvent.VK_ALT:
            case KeyEvent.VK_META:
                return;
            case KeyEvent.VK_ENTER:
                e.consume();
                switchToBuffer();
                if (modifiers == KeyEvent.ALT_MASK) editor.toggleSidebar();
                return;
            case KeyEvent.VK_TAB:
                e.consume();
                if (modifiers == 0) {
                    if (sidebar.getBottomComponent() != null) {
                        sidebar.setBuffer();
                        editor.setFocus((JComponent) sidebar.getBottomComponent());
                    }
                }
                return;
            case KeyEvent.VK_ESCAPE:
                e.consume();
                if (popup != null && popup.isVisible()) {
                    popup.setVisible(false);
                    popup = null;
                } else {
                    sidebar.setBuffer();
                    editor.setFocusToDisplay();
                }
                return;
            case KeyEvent.VK_DELETE:
                e.consume();
                closeSelectedBuffers();
                return;
        }
        editor.getDispatcher().setEnabled(false);
    }

    public void keyReleased(KeyEvent e) {
        e.consume();
        sidebar.getEditor().getDispatcher().setEnabled(true);
    }

    public void keyTyped(KeyEvent e) {
        e.consume();
    }

    public void mousePressed(MouseEvent e) {
        Editor editor = sidebar.getEditor();
        editor.ensureActive();
        int modifiers = e.getModifiers() & 0x1f;
        Point p = e.getPoint();
        if (modifiers == InputEvent.BUTTON1_MASK || modifiers == InputEvent.BUTTON2_MASK) {
            setSelectionRow(getRowForLocation(p.x, p.y));
            paintImmediately(0, 0, getWidth(), getHeight());
            switchToBuffer();
        } else if (modifiers == InputEvent.BUTTON3_MASK) {
            setSelectionRow(getRowForLocation(p.x, p.y));
            sidebar.getFrame().setFocus(this);
            paintImmediately(0, 0, getWidth(), getHeight());
        } else {
            sidebar.getFrame().setFocus(this);
        }
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
        int modifiers = e.getModifiers() & 0x1f;
        Point p = e.getPoint();
        if (modifiers == InputEvent.BUTTON1_MASK || modifiers == InputEvent.BUTTON2_MASK) {
            setSelectionRow(getRowForLocation(p.x, p.y));
            paintImmediately(0, 0, getWidth(), getHeight());
            switchToBuffer();
        } else if (e.getModifiers() == InputEvent.BUTTON3_MASK) {
            showPopup(e.getComponent(), e.getX(), e.getY());
        }
    }

    public void mouseMoved(MouseEvent e) {
        String text = getToolTipText(e);
        if (text == null) text = "";
        sidebar.getFrame().setStatusText(text);
    }

    public void mouseEntered(MouseEvent e) {
        setCursor(Cursor.getDefaultCursor());
    }

    public void mouseExited(MouseEvent e) {
        final Frame frame = sidebar.getFrame();
        final StatusBar statusBar = frame.getStatusBar();
        if (statusBar != null) {
            statusBar.setText(null);
            statusBar.repaintNow();
        }
        ToolTipManager.sharedInstance().setEnabled(false);
        ToolTipManager.sharedInstance().setEnabled(true);
        if (popup == null || !popup.isVisible()) {
            if (frame.getFocusedComponent() == this) {
                Editor editor = sidebar.getEditor();
                sidebar.setBuffer();
                editor.setFocusToDisplay();
            }
        }
    }

    public void mouseDragged(MouseEvent e) {
    }

    public void dragEnter(DropTargetDragEvent event) {
        if (alpha || reorder) return;
        stopScroll();
        if (Platform.isPlatformUnix() && dragSourceContext != null) {
            Cursor c = Dispatcher.getCursorForAction(event.getDropAction());
            dragSourceContext.setCursor(c);
        }
    }

    public void dragExit(DropTargetEvent e) {
        if (alpha || reorder) return;
        setSelectionRow(draggedBufferRow);
        if (Platform.isPlatformUnix() && dragSourceContext != null) dragSourceContext.setCursor(Dispatcher.getCursorForAction(-1));
    }

    public void dragOver(DropTargetDragEvent event) {
        if (alpha || reorder) return;
        if (event.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            Point pt = event.getLocation();
            int row = getRowForLocation(pt.x, pt.y);
            if (row == -1) setSelectionRow(draggedBufferRow); else setSelectionRow(row);
        }
    }

    public void drop(DropTargetDropEvent event) {
        if (alpha || reorder) return;
        Transferable t = event.getTransferable();
        if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor) && draggedBuffer != null) {
            BufferList bufList = Editor.getBufferList();
            Buffer movedTo = getSelectedBuffer();
            if (movedTo.isSecondary()) {
                setSelectionRow(draggedBufferRow);
                event.rejectDrop();
                return;
            }
            int index = bufList.indexOf(movedTo);
            if (index >= 0) {
                bufList.move(draggedBuffer, index);
                refresh();
                setSelectionRow(index);
            }
        } else event.rejectDrop();
    }

    public void dropActionChanged(DropTargetDragEvent event) {
    }

    public void dragGestureRecognized(DragGestureEvent event) {
        if (alpha || reorder) return;
        Buffer buf = getSelectedBuffer();
        if (buf != null && !buf.isSecondary()) {
            String name = buf.getFileNameForDisplay();
            Transferable transferable = new BufferSelection(new java.io.File(name));
            draggedBuffer = buf;
            draggedBufferRow = getSelectionRow();
            int action = event.getDragAction();
            Cursor cursor = null;
            if (Platform.isPlatformUnix()) {
                cursor = Dispatcher.getCursorForAction(action);
            }
            dragSource.startDrag(event, cursor, transferable, this);
            dragSourceContext = null;
        }
    }

    public void dragDropEnd(DragSourceDropEvent event) {
        draggedBuffer = null;
        draggedBufferRow = -1;
        stopScroll();
        if (alpha || reorder) return;
        int selectionRow = getSelectionRow();
        if (selectionRow >= 0) scrollRowToVisible(selectionRow);
    }

    public void dragEnter(DragSourceDragEvent event) {
        if (alpha || reorder) return;
        stopScroll();
        if (Platform.isPlatformUnix()) {
            DragSourceContext dsc = event.getDragSourceContext();
            dsc.setCursor(Dispatcher.getCursorForAction(event.getDropAction()));
        }
    }

    public void dragOver(DragSourceDragEvent event) {
        if (alpha || reorder) return;
        if (Platform.isPlatformMacOSX()) {
            Component parent = getParent();
            if (!(parent instanceof JViewport)) {
                stopScroll();
                return;
            }
            JViewport viewport = (JViewport) parent;
            Rectangle rect = viewport.getViewRect();
            Point frameLoc = Editor.getCurrentFrame().getLocationOnScreen();
            Point loc = viewport.getLocationOnScreen();
            int x = event.getX() - frameLoc.x + loc.x;
            int y = event.getY() + frameLoc.y - loc.y;
            if ((y < 0 || y > rect.height) && (x >= 0 && x <= rect.width)) {
                x += loc.x;
                y += loc.y;
                DragSourceEvent dse = new DragSourceEvent(event.getDragSourceContext(), x, y);
                dragExit(dse);
            } else stopScroll();
        }
    }

    public void dropActionChanged(DragSourceDragEvent event) {
    }

    public void dragExit(DragSourceEvent event) {
        if (alpha || reorder) return;
        DragSourceContext dsc = event.getDragSourceContext();
        if (Platform.isPlatformUnix()) dsc.setCursor(Dispatcher.getCursorForAction(-1));
        dragSourceContext = dsc;
        Component parent = getParent();
        if (!(parent instanceof JViewport)) {
            stopScroll();
            return;
        }
        JViewport viewport = (JViewport) parent;
        Rectangle rect = viewport.getViewRect();
        int x = event.getX();
        int y = event.getY();
        Point p = getLocationOnScreen();
        x -= p.x + rect.x;
        y -= p.y + rect.y;
        int rowFudge = (getRowBounds(0).height + 1) / 2;
        int yDiff = getSize().height - rect.height - rect.y;
        if (x < 0 || x >= rect.width || (y < 0 && rect.y < rowFudge) || (y > 0 && yDiff < rowFudge)) {
            stopScroll();
            return;
        }
        if (y < 0) {
            scrollY = (y - 1) / 2;
        } else {
            y -= rect.height;
            scrollY = (y + 2) / 2;
        }
        startScroll();
    }

    private synchronized void startScroll() {
        if (scroller == null) {
            scroller = new TreeScroller();
            Thread scroll = new Thread(scroller, "SidebarBufferTree scroller");
            scroll.setDaemon(true);
            scroll.setPriority(Thread.MIN_PRIORITY);
            scroll.start();
        }
    }

    private synchronized void stopScroll() {
        scrollY = 0;
        scroller = null;
    }

    private class TreeScroller implements Runnable {

        public void run() {
            Component parent = getParent();
            if (!(parent instanceof JViewport)) {
                return;
            }
            final JViewport viewport = (JViewport) parent;
            while (scrollY != 0) {
                if (viewport == null) break;
                Rectangle bounds = getRowBounds(0);
                if (bounds == null) break;
                int scrollInc = bounds.height;
                int diffY;
                final Rectangle rect = viewport.getViewRect();
                final Point pos = viewport.getViewPosition();
                if (scrollY < 0) {
                    pos.y = Math.max(pos.y - scrollInc, 0);
                    if (pos.y == 0) scrollY = 0;
                } else {
                    int h = getSize().height;
                    int yMax = h - rect.height;
                    pos.y = Math.min(pos.y + scrollInc, yMax);
                    if (pos.y == yMax) scrollY = 0;
                }
                Runnable r = new Runnable() {

                    public void run() {
                        if (viewport != null && pos != null) {
                            viewport.setViewPosition(pos);
                            repaint();
                        }
                    }
                };
                SwingUtilities.invokeLater(r);
                int absY = Math.abs(scrollY);
                int waitIndex = Math.min(absY, WAIT_TIMES.length - 1);
                try {
                    Thread.sleep(WAIT_TIMES[waitIndex]);
                } catch (InterruptedException ex) {
                }
            }
        }
    }

    private static class SidebarTreeCellRenderer extends JLabel implements TreeCellRenderer {

        private static final Color textForeground = UIManager.getColor("Tree.textForeground");

        private static final Color textBackground = UIManager.getColor("Tree.textBackground");

        private static final Color selectionForeground = UIManager.getColor("Tree.selectionForeground");

        private static final Color selectionBackground = UIManager.getColor("Tree.selectionBackground");

        private static final Color noFocusSelectionBackground = new Color(208, 208, 208);

        private static final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

        private final Sidebar sidebar;

        public SidebarTreeCellRenderer(Sidebar sidebar) {
            super();
            this.sidebar = sidebar;
            setOpaque(true);
        }

        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Object userObject = null;
            if (value instanceof DefaultMutableTreeNode) userObject = ((DefaultMutableTreeNode) value).getUserObject();
            Border innerBorder = null;
            if (userObject instanceof Buffer) {
                setText(userObject.toString());
                Buffer buffer = (Buffer) userObject;
                setIcon(buffer.getIcon());
                if (buffer.isSecondary()) innerBorder = new EmptyBorder(0, 10, 0, 0);
            } else setIcon(null);
            Frame frame = sidebar.getFrame();
            if (selected) {
                if (frame.isActive() && tree.hasFocus()) setBackground(selectionBackground); else setBackground(noFocusSelectionBackground);
                setForeground(selectionForeground);
            } else {
                setBackground(textBackground);
                setForeground(textForeground);
            }
            setEnabled(tree.isEnabled());
            setFont(tree.getFont());
            Border outerBorder;
            if (hasFocus) outerBorder = UIManager.getBorder("List.focusCellHighlightBorder"); else outerBorder = noFocusBorder;
            setBorder(new CompoundBorder(outerBorder, innerBorder));
            return this;
        }
    }

    private class BufferSelection implements Transferable {

        private List fileList = null;

        private final DataFlavor[] flavors = new DataFlavor[] { DataFlavor.javaFileListFlavor };

        public BufferSelection(java.io.File file) {
            fileList = new ArrayList(1);
            fileList.add(file);
        }

        public Object getTransferData(DataFlavor flavor) {
            List retList = new ArrayList(1);
            if (flavor == DataFlavor.javaFileListFlavor) retList.add(fileList.get(0));
            return retList;
        }

        public DataFlavor[] getTransferDataFlavors() {
            return flavors;
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            for (int i = 0; i < flavors.length; i++) {
                if (flavor == flavors[i]) return true;
            }
            return false;
        }
    }
}
