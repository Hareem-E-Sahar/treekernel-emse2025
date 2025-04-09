package org.eclipse.gef.editpolicies;

import java.util.List;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Polyline;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.RectD2D;
import org.eclipse.draw2d.geometry.Transposer;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.requests.DropRequest;

/**
 * An EditPolicy for use with {@link org.eclipse.draw2d.FlowLayout}. This EditPolicy knows
 * how to map an <x,y> coordinate on the layout container to the appropriate index for the
 * operation being performed. It also shows target feedback consisting of an insertion
 * line at the appropriate location.
 * @since 2.0
 */
public abstract class FlowLayoutEditPolicy extends OrderedLayoutEditPolicy {

    private Polyline insertionLine;

    /**
 * @see LayoutEditPolicy#eraseLayoutTargetFeedback(Request)
 */
    protected void eraseLayoutTargetFeedback(Request request) {
        if (insertionLine != null) {
            removeFeedback(insertionLine);
            insertionLine = null;
        }
    }

    private RectD2D getAbsoluteBounds(GraphicalEditPart ep) {
        RectD2D bounds = ep.getFigure().getBounds().getCopy();
        ep.getFigure().translateToAbsolute(bounds);
        return bounds;
    }

    /**
 * @param request the Request
 * @return the index for the insertion reference
 */
    protected int getFeedbackIndexFor(Request request) {
        List children = getHost().getChildren();
        if (children.isEmpty()) return -1;
        Transposer transposer = new Transposer();
        transposer.setEnabled(!isHorizontal());
        Point p = transposer.t(getLocationFromRequest(request));
        int rowBottom = Integer.MIN_VALUE;
        int candidate = -1;
        for (int i = 0; i < children.size(); i++) {
            EditPart child = (EditPart) children.get(i);
            RectD2D rect = transposer.t(getAbsoluteBounds(((GraphicalEditPart) child)));
            if (rect.y > rowBottom) {
                if (p.y <= rowBottom) {
                    if (candidate == -1) candidate = i;
                    break;
                } else candidate = -1;
            }
            rowBottom = Math.max(rowBottom, rect.bottom());
            if (candidate == -1) {
                if (p.x <= rect.x + (rect.width / 2)) candidate = i;
            }
            if (candidate != -1) {
                if (p.y <= rowBottom) {
                    break;
                }
            }
        }
        return candidate;
    }

    /**
 * @see OrderedLayoutEditPolicy#getInsertionReference(Request)
 */
    protected EditPart getInsertionReference(Request request) {
        List children = getHost().getChildren();
        if (request.getType().equals(RequestConstants.REQ_CREATE)) {
            int i = getFeedbackIndexFor(request);
            if (i == -1) return null;
            return (EditPart) children.get(i);
        }
        int index = getFeedbackIndexFor(request);
        if (index != -1) {
            List selection = getHost().getViewer().getSelectedEditParts();
            do {
                EditPart editpart = (EditPart) children.get(index);
                if (!selection.contains(editpart)) return editpart;
            } while (++index < children.size());
        }
        return null;
    }

    /**
 * Lazily creates and returns a <code>Polyline</code> Figure for use as feedback.
 * @return a Polyline figure
 */
    protected Polyline getLineFeedback() {
        if (insertionLine == null) {
            insertionLine = new Polyline();
            insertionLine.setLineWidth(2);
            insertionLine.addPoint(new Point(0, 0));
            insertionLine.addPoint(new Point(10, 10));
            addFeedback(insertionLine);
        }
        return insertionLine;
    }

    private Point getLocationFromRequest(Request request) {
        return ((DropRequest) request).getLocation();
    }

    /**
 * @return <code>true</code> if the host's LayoutManager is in a horizontal orientation
 */
    protected boolean isHorizontal() {
        IFigure figure = ((GraphicalEditPart) getHost()).getContentPane();
        return ((FlowLayout) figure.getLayoutManager()).isHorizontal();
    }

    /**
 * Shows an insertion line if there is one or more current children.
 * @see LayoutEditPolicy#showLayoutTargetFeedback(Request)
 */
    protected void showLayoutTargetFeedback(Request request) {
        if (getHost().getChildren().size() == 0) return;
        Polyline fb = getLineFeedback();
        Transposer transposer = new Transposer();
        transposer.setEnabled(!isHorizontal());
        boolean before = true;
        int epIndex = getFeedbackIndexFor(request);
        RectD2D r = null;
        if (epIndex == -1) {
            before = false;
            epIndex = getHost().getChildren().size() - 1;
            EditPart editPart = (EditPart) getHost().getChildren().get(epIndex);
            r = transposer.t(getAbsoluteBounds((GraphicalEditPart) editPart));
        } else {
            EditPart editPart = (EditPart) getHost().getChildren().get(epIndex);
            r = transposer.t(getAbsoluteBounds((GraphicalEditPart) editPart));
            Point p = transposer.t(getLocationFromRequest(request));
            if (p.x <= r.x + (r.width / 2)) before = true; else {
                before = false;
                epIndex--;
                editPart = (EditPart) getHost().getChildren().get(epIndex);
                r = transposer.t(getAbsoluteBounds((GraphicalEditPart) editPart));
            }
        }
        int x = Integer.MIN_VALUE;
        if (before) {
            if (epIndex > 0) {
                RectD2D boxPrev = transposer.t(getAbsoluteBounds((GraphicalEditPart) getHost().getChildren().get(epIndex - 1)));
                int prevRight = boxPrev.right();
                if (prevRight < r.x) {
                    x = prevRight + (r.x - prevRight) / 2;
                } else if (prevRight == r.x) {
                    x = prevRight + 1;
                }
            }
            if (x == Integer.MIN_VALUE) {
                RectD2D parentBox = transposer.t(getAbsoluteBounds((GraphicalEditPart) getHost()));
                x = r.x - 5;
                if (x < parentBox.x) x = parentBox.x + (r.x - parentBox.x) / 2;
            }
        } else {
            RectD2D parentBox = transposer.t(getAbsoluteBounds((GraphicalEditPart) getHost()));
            int rRight = r.x + r.width;
            int pRight = parentBox.x + parentBox.width;
            x = rRight + 5;
            if (x > pRight) x = rRight + (pRight - rRight) / 2;
        }
        Point p1 = new Point(x, r.y - 4);
        p1 = transposer.t(p1);
        fb.translateToRelative(p1);
        Point p2 = new Point(x, r.y + r.height + 4);
        p2 = transposer.t(p2);
        fb.translateToRelative(p2);
        fb.setPoint(p1, 0);
        fb.setPoint(p2, 1);
    }
}
