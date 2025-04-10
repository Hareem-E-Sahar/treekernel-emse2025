package org.dengues.reports.editor.design.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.SharedCursors;
import org.eclipse.gef.tools.AbstractTool;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Display;

/**
 * Qiang.Zhang.Adolf@gmail.com class global comment. Detailled comment <br/>
 * 
 * $Id: Dengues.epf Qiang.Zhang.Adolf@gmail.com 2008-1-25 qiang.zhang $
 * 
 */
public class RootDragTracker extends AbstractTool implements DragTracker {

    protected void handleFinished() {
    }

    static final int TOGGLE_MODE = 1;

    static final int APPEND_MODE = 2;

    private int mode;

    private Figure marqueeRectangleFigure;

    private List allChildren = new ArrayList();

    private List selectedEditParts;

    private Request targetRequest;

    private static final Request MARQUEE_REQUEST = new Request(RequestConstants.REQ_SELECTION);

    /**
     * Creates a new MarqueeSelectionTool.
     */
    public RootDragTracker() {
        setDefaultCursor(SharedCursors.CROSS);
        setUnloadWhenFinished(false);
    }

    private List calculateNewSelection() {
        List newSelections = new ArrayList();
        List children = getAllChildren();
        for (int i = 0; i < children.size(); i++) {
            EditPart child = (EditPart) children.get(i);
            if (!child.isSelectable() || isInTable(child)) continue;
            IFigure figure = ((GraphicalEditPart) child).getFigure();
            Rectangle r = figure.getBounds().getCopy();
            figure.translateToAbsolute(r);
            if (getMarqueeSelectionRectangle().contains(r.getTopLeft()) && getMarqueeSelectionRectangle().contains(r.getBottomRight()) && figure.isShowing() && child.getTargetEditPart(MARQUEE_REQUEST) == child && isFigureVisible(figure)) newSelections.add(child);
        }
        return newSelections;
    }

    /**
     * Judges the editpart if in table
     * 
     * @param child
     * @return
     */
    private boolean isInTable(EditPart child) {
        return false;
    }

    private Request createTargetRequest() {
        return MARQUEE_REQUEST;
    }

    /**
     * Erases feedback if necessary and puts the tool into the terminal state.
     */
    public void deactivate() {
        if (isInState(STATE_DRAG_IN_PROGRESS)) {
            eraseMarqueeFeedback();
            eraseTargetFeedback();
        }
        super.deactivate();
        allChildren = new ArrayList();
        setState(STATE_TERMINAL);
    }

    private void eraseMarqueeFeedback() {
        if (marqueeRectangleFigure != null) {
            removeFeedback(marqueeRectangleFigure);
            marqueeRectangleFigure = null;
        }
    }

    private void eraseTargetFeedback() {
        if (selectedEditParts == null) return;
        ListIterator oldEditParts = selectedEditParts.listIterator();
        while (oldEditParts.hasNext()) {
            EditPart editPart = (EditPart) oldEditParts.next();
            editPart.eraseTargetFeedback(getTargetRequest());
        }
    }

    /**
     * Returns a list including all of the children of the edit part passed in.
     */
    private List getAllChildren(EditPart editPart, List allChildren) {
        List children = editPart.getChildren();
        for (int i = 0; i < children.size(); i++) {
            GraphicalEditPart child = (GraphicalEditPart) children.get(i);
            allChildren.add(child);
            getAllChildren(child, allChildren);
        }
        return allChildren;
    }

    /**
     * Return a vector including all of the children of the root editpart
     */
    private List getAllChildren() {
        if (allChildren.isEmpty()) allChildren = getAllChildren(getCurrentViewer().getRootEditPart(), new ArrayList());
        return allChildren;
    }

    /**
     * @see org.eclipse.gef.tools.AbstractTool#getCommandName()
     */
    protected String getCommandName() {
        return REQ_SELECTION;
    }

    /**
     * @see org.eclipse.gef.tools.AbstractTool#getDebugName()
     */
    protected String getDebugName() {
        return "Marquee Tool";
    }

    private IFigure getMarqueeFeedbackFigure() {
        if (marqueeRectangleFigure == null) {
            marqueeRectangleFigure = new MarqueeRectangleFigure();
            addFeedback(marqueeRectangleFigure);
        }
        return marqueeRectangleFigure;
    }

    private Rectangle getMarqueeSelectionRectangle() {
        return new Rectangle(getStartLocation(), getLocation());
    }

    private int getSelectionMode() {
        return mode;
    }

    private Request getTargetRequest() {
        if (targetRequest == null) targetRequest = createTargetRequest();
        return targetRequest;
    }

    /**
     * @see org.eclipse.gef.tools.AbstractTool#handleButtonDown(int)
     */
    protected boolean handleButtonDown(int button) {
        if (!isGraphicalViewer()) return true;
        if (button != 1) {
            setState(STATE_INVALID);
            handleInvalidInput();
        }
        if (stateTransition(STATE_INITIAL, STATE_DRAG_IN_PROGRESS)) {
            if (getCurrentInput().isControlKeyDown()) setSelectionMode(TOGGLE_MODE); else if (getCurrentInput().isShiftKeyDown()) setSelectionMode(APPEND_MODE);
        }
        return true;
    }

    /**
     * @see org.eclipse.gef.tools.AbstractTool#handleButtonUp(int)
     */
    protected boolean handleButtonUp(int button) {
        if (stateTransition(STATE_DRAG_IN_PROGRESS, STATE_TERMINAL)) {
            eraseTargetFeedback();
            eraseMarqueeFeedback();
            performMarqueeSelect();
        }
        handleFinished();
        return true;
    }

    /**
     * @see org.eclipse.gef.tools.AbstractTool#handleDragInProgress()
     */
    protected boolean handleDragInProgress() {
        if (isInState(STATE_DRAG | STATE_DRAG_IN_PROGRESS)) {
            showMarqueeFeedback();
            eraseTargetFeedback();
            selectedEditParts = calculateNewSelection();
            showTargetFeedback();
        }
        return true;
    }

    /**
     * @see org.eclipse.gef.tools.AbstractTool#handleFocusLost()
     */
    protected boolean handleFocusLost() {
        if (isInState(STATE_DRAG | STATE_DRAG_IN_PROGRESS)) {
            handleFinished();
            return true;
        }
        return false;
    }

    /**
     * This method is called when mouse or keyboard input is invalid and erases the feedback.
     * 
     * @return <code>true</code>
     */
    protected boolean handleInvalidInput() {
        eraseTargetFeedback();
        eraseMarqueeFeedback();
        return true;
    }

    /**
     * Handles high-level processing of a key down event. KeyEvents are forwarded to the current viewer's
     * {@link KeyHandler}, via {@link KeyHandler#keyPressed(KeyEvent)}.
     * 
     * @see AbstractTool#handleKeyDown(KeyEvent)
     */
    protected boolean handleKeyDown(KeyEvent e) {
        if (super.handleKeyDown(e)) return true;
        if (getCurrentViewer().getKeyHandler() != null && getCurrentViewer().getKeyHandler().keyPressed(e)) return true;
        return false;
    }

    private boolean isFigureVisible(IFigure fig) {
        Rectangle figBounds = fig.getBounds().getCopy();
        IFigure walker = fig.getParent();
        while (!figBounds.isEmpty() && walker != null) {
            walker.translateToParent(figBounds);
            figBounds.intersect(walker.getBounds());
            walker = walker.getParent();
        }
        return !figBounds.isEmpty();
    }

    private boolean isGraphicalViewer() {
        return getCurrentViewer() instanceof GraphicalViewer;
    }

    private void performMarqueeSelect() {
        EditPartViewer viewer = getCurrentViewer();
        List newSelections = calculateNewSelection();
        if (getSelectionMode() == APPEND_MODE) {
            for (int i = 0; i < newSelections.size(); i++) {
                EditPart editPart = (EditPart) newSelections.get(i);
                viewer.appendSelection(editPart);
            }
        } else if (getSelectionMode() == TOGGLE_MODE) {
            List selected = new ArrayList(viewer.getSelectedEditParts());
            for (int i = 0; i < newSelections.size(); i++) {
                EditPart editPart = (EditPart) newSelections.get(i);
                if (editPart.getSelected() != EditPart.SELECTED_NONE) selected.remove(editPart); else selected.add(editPart);
            }
            viewer.setSelection(new StructuredSelection(selected));
        } else {
            viewer.setSelection(new StructuredSelection(newSelections));
        }
    }

    /**
     * @see org.eclipse.gef.Tool#setViewer(org.eclipse.gef.EditPartViewer)
     */
    public void setViewer(EditPartViewer viewer) {
        if (viewer == getCurrentViewer()) return;
        super.setViewer(viewer);
        if (viewer instanceof GraphicalViewer) setDefaultCursor(SharedCursors.CROSS); else setDefaultCursor(SharedCursors.NO);
    }

    private void setSelectionMode(int mode) {
        this.mode = mode;
    }

    private void showMarqueeFeedback() {
        Rectangle rect = getMarqueeSelectionRectangle().getCopy();
        getMarqueeFeedbackFigure().translateToRelative(rect);
        getMarqueeFeedbackFigure().setBounds(rect);
    }

    private void showTargetFeedback() {
        for (int i = 0; i < selectedEditParts.size(); i++) {
            EditPart editPart = (EditPart) selectedEditParts.get(i);
            editPart.showTargetFeedback(getTargetRequest());
        }
    }

    class MarqueeRectangleFigure extends Figure {

        private int offset = 0;

        private boolean schedulePaint = true;

        private static final int DELAY = 110;

        /**
         * @see org.eclipse.draw2d.Figure#paintFigure(org.eclipse.draw2d.Graphics)
         */
        protected void paintFigure(Graphics graphics) {
            Rectangle bounds = getBounds().getCopy();
            graphics.translate(getLocation());
            graphics.setXORMode(true);
            graphics.setForegroundColor(ColorConstants.white);
            graphics.setBackgroundColor(ColorConstants.black);
            graphics.setLineStyle(Graphics.LINE_DOT);
            int[] points = new int[6];
            points[0] = 0 + offset;
            points[1] = 0;
            points[2] = bounds.width - 1;
            points[3] = 0;
            points[4] = bounds.width - 1;
            points[5] = bounds.height - 1;
            graphics.drawPolyline(points);
            points[0] = 0;
            points[1] = 0 + offset;
            points[2] = 0;
            points[3] = bounds.height - 1;
            points[4] = bounds.width - 1;
            points[5] = bounds.height - 1;
            graphics.drawPolyline(points);
            graphics.translate(getLocation().getNegated());
            if (schedulePaint) {
                Display.getCurrent().timerExec(DELAY, new Runnable() {

                    public void run() {
                        offset++;
                        if (offset > 5) offset = 0;
                        schedulePaint = true;
                        repaint();
                    }
                });
            }
            schedulePaint = false;
        }
    }
}
