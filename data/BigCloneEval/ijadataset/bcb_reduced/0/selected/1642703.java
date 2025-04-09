package net.rptools.lib.swing;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DragSourceMotionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;

public class ImagePanel extends JComponent implements Scrollable, DragGestureListener, DragSourceListener, MouseListener, DragSourceMotionListener {

    public enum SelectionMode {

        SINGLE, MULTIPLE, NONE
    }

    ;

    private ImagePanelModel model;

    private int gridSize = 50;

    private final int gridPadding = 5;

    private final Map<Rectangle, Integer> imageBoundsMap = new HashMap<Rectangle, Integer>();

    private boolean isDraggingEnabled = true;

    private boolean showCaptions = true;

    private boolean showImageBorder = false;

    private SelectionMode selectionMode = SelectionMode.NONE;

    private final List<Object> selectedIDList = new ArrayList<Object>();

    private final List<SelectionListener> selectionListenerList = new ArrayList<SelectionListener>();

    private int fontHeight;

    public ImagePanel() {
        DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
        addMouseListener(this);
        ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
        toolTipManager.registerComponent(this);
    }

    public void setGridSize(int size) {
        if (size < 25) {
            size = 25;
        }
        gridSize = size;
        revalidate();
        repaint();
    }

    public void setDraggingEnabled(boolean enabled) {
        isDraggingEnabled = enabled;
    }

    public void setSelectionMode(SelectionMode mode) {
        selectionMode = mode;
        selectedIDList.clear();
        repaint();
    }

    public void setShowCaptions(boolean enabled) {
        showCaptions = enabled;
        repaint();
    }

    public void setShowImageBorders(boolean enabled) {
        showImageBorder = enabled;
        repaint();
    }

    public ImagePanelModel getModel() {
        return model;
    }

    public void setModel(ImagePanelModel model) {
        this.model = model;
        revalidate();
        JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, this);
        if (scrollPane != null) {
            scrollPane.revalidate();
            scrollPane.repaint();
        }
    }

    public List<Object> getSelectedIds() {
        List<Object> list = new ArrayList<Object>();
        list.addAll(selectedIDList);
        return list;
    }

    public void clearSelection() {
        selectedIDList.clear();
        fireSelectionEvent();
    }

    public void addSelectionListener(SelectionListener listener) {
        selectionListenerList.add(listener);
    }

    public void removeSelectionListener(SelectionListener listener) {
        selectionListenerList.remove(listener);
    }

    @Override
    public boolean isOpaque() {
        return true;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Rectangle clipBounds = g.getClipBounds();
        Dimension size = getSize();
        FontMetrics fm = g.getFontMetrics();
        fontHeight = fm.getHeight();
        g.setColor(getBackground());
        g.fillRect(0, 0, size.width, size.height);
        if (model == null) {
            return;
        }
        imageBoundsMap.clear();
        int x = gridPadding;
        int y = gridPadding;
        for (int i = 0; i < model.getImageCount(); i++) {
            Image image = model.getImage(i);
            Rectangle bounds = new Rectangle(x, y, gridSize, gridSize);
            imageBoundsMap.put(bounds, i);
            Paint paint = model.getBackground(i);
            if (paint != null) {
                ((Graphics2D) g).setPaint(paint);
                g.fillRect(x - 2, y - 2, gridSize + 4, gridSize + 4);
            }
            if (image != null && bounds.intersects(clipBounds)) {
                Dimension dim = constrainSize(image, gridSize);
                g.drawImage(image, x + (gridSize - dim.width) / 2, y + (gridSize - dim.height) / 2, dim.width, dim.height, this);
                if (showImageBorder) {
                    g.setColor(Color.black);
                    g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
                }
            }
            if (selectedIDList.contains(model.getID(i))) {
                ImageBorder.RED.paintAround((Graphics2D) g, bounds.x, bounds.y, bounds.width, bounds.height);
            }
            Image[] decorations = model.getDecorations(i);
            if (decorations != null) {
                int offx = x;
                int offy = y + gridSize;
                int rowHeight = 0;
                for (Image decoration : decorations) {
                    g.drawImage(decoration, offx, offy - decoration.getHeight(null), this);
                    rowHeight = Math.max(rowHeight, decoration.getHeight(null));
                    offx += decoration.getWidth(null);
                    if (offx > gridSize) {
                        offx = x;
                        offy -= rowHeight + 2;
                        rowHeight = 0;
                    }
                }
            }
            if (showCaptions) {
                String caption = model.getCaption(i);
                if (caption != null) {
                    boolean nameTooLong = false;
                    int strWidth = fm.stringWidth(caption);
                    while (strWidth > bounds.width) {
                        nameTooLong = true;
                        caption = caption.substring(0, caption.length() - 1);
                        strWidth = fm.stringWidth(caption + "...");
                    }
                    if (nameTooLong) {
                        caption += "...";
                    }
                    int cx = x + (gridSize - strWidth) / 2;
                    int cy = y + gridSize + fm.getHeight();
                    g.setColor(getForeground());
                    g.drawString(caption, cx, cy);
                }
            }
            x += gridSize + gridPadding;
            if (x > size.width - gridPadding - gridSize) {
                x = gridPadding;
                y += gridSize + gridPadding;
                if (showCaptions) {
                    y += fontHeight;
                }
            }
        }
    }

    protected int getIndex(int x, int y) {
        for (Entry<Rectangle, Integer> entry : imageBoundsMap.entrySet()) {
            if (entry.getKey().contains(x, y)) {
                return entry.getValue();
            }
        }
        return -1;
    }

    protected Object getImageIDAt(int x, int y) {
        return model != null ? model.getID(getIndex(x, y)) : null;
    }

    protected void fireSelectionEvent() {
        List<Object> selectionList = Collections.unmodifiableList(selectedIDList);
        for (int i = 0; i < selectionListenerList.size(); i++) {
            selectionListenerList.get(i).selectionPerformed(selectionList);
        }
    }

    private Dimension constrainSize(Image image, int size) {
        int imageWidth = image.getWidth(this);
        int imageHeight = image.getHeight(this);
        if (imageWidth == imageHeight) {
            return new Dimension(size, size);
        }
        int width = 0;
        int height = 0;
        if (imageWidth > imageHeight) {
            width = size;
            height = (int) (imageHeight * ((float) size) / imageWidth);
        } else {
            height = size;
            width = (int) (imageWidth * ((float) size) / imageHeight);
        }
        return new Dimension(width, height);
    }

    @Override
    public Dimension getPreferredSize() {
        int width = getSize().width;
        int rowCount = 0;
        if (width < gridSize + gridPadding * 2) {
            rowCount = model != null ? model.getImageCount() : 0;
        } else {
            rowCount = (int) (model != null ? Math.ceil(model.getImageCount() / Math.floor(width / (gridSize + gridPadding))) : 0);
        }
        int height = (model != null ? rowCount * (gridSize + gridPadding + fontHeight) + gridPadding : gridSize + gridPadding);
        return new Dimension(width, height);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    protected int getImageIndexAt(int x, int y) {
        for (Rectangle rect : imageBoundsMap.keySet()) {
            if (rect.contains(x, y)) {
                return imageBoundsMap.get(rect);
            }
        }
        return -1;
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        if (getModel() == null) {
            return null;
        }
        return getModel().getCaption(getIndex(event.getX(), event.getY()));
    }

    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return (gridSize + gridPadding) * 2;
    }

    public boolean getScrollableTracksViewportHeight() {
        Dimension parentSize = SwingUtilities.getAncestorOfClass(JScrollPane.class, this).getSize();
        return getPreferredSize().height < parentSize.height;
    }

    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return gridSize / 4;
    }

    public void dragGestureRecognized(DragGestureEvent dge) {
        if (model == null || !isDraggingEnabled) {
            return;
        }
        int index = getImageIndexAt(dge.getDragOrigin().x, dge.getDragOrigin().y);
        if (index < 0) {
            return;
        }
        Transferable transferable = model.getTransferable(index);
        if (transferable == null) {
            return;
        }
        dge.startDrag(getDragCursor(), transferable, this);
        DragSource.getDefaultDragSource().addDragSourceMotionListener(this);
    }

    protected Cursor getDragCursor() {
        return null;
    }

    public void dragDropEnd(DragSourceDropEvent dsde) {
        DragSource.getDefaultDragSource().removeDragSourceMotionListener(this);
    }

    public void dragEnter(DragSourceDragEvent dsde) {
    }

    public void dragExit(DragSourceEvent dse) {
    }

    public void dragOver(DragSourceDragEvent dsde) {
    }

    public void dropActionChanged(DragSourceDragEvent dsde) {
    }

    public void dragMouseMoved(DragSourceDragEvent dsde) {
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        if (selectionMode == SelectionMode.NONE) {
            return;
        }
        Object imageID = getImageIDAt(e.getX(), e.getY());
        if (!SwingUtil.isControlDown(e) || selectionMode == SelectionMode.SINGLE) {
            selectedIDList.clear();
        }
        if (imageID != null) {
            selectedIDList.add(imageID);
            repaint();
        }
        fireSelectionEvent();
    }

    public void mouseReleased(MouseEvent e) {
    }
}
