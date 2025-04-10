package org.eclipse.gef.internal.ui.rulers;

import java.util.List;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.AccessibleEditPart;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editparts.AccessibleGraphicalEditPart;
import org.eclipse.gef.editparts.LayerManager;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.editpolicies.SelectionEditPolicy;
import org.eclipse.gef.internal.GEFMessages;
import org.eclipse.gef.requests.SelectionRequest;
import org.eclipse.gef.rulers.RulerChangeListener;
import org.eclipse.gef.rulers.RulerProvider;

/**
* @author Pratik Shah
* @since 3.0
*/
public class RulerEditPart extends AbstractGraphicalEditPart {

    protected GraphicalViewer diagramViewer;

    private AccessibleEditPart accPart;

    private RulerProvider rulerProvider;

    private boolean horizontal;

    private RulerChangeListener listener = new RulerChangeListener.Stub() {

        public void notifyGuideReparented(Object guide) {
            handleGuideReparented(guide);
        }

        public void notifyUnitsChanged(int newUnit) {
            handleUnitsChanged(newUnit);
        }
    };

    public RulerEditPart(Object model) {
        setModel(model);
    }

    public void activate() {
        getRulerProvider().addRulerChangeListener(listener);
        getRulerFigure().setZoomManager(getZoomManager());
        super.activate();
    }

    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, new RulerSelectionPolicy());
    }

    protected IFigure createFigure() {
        RulerFigure ruler = new RulerFigure(isHorizontal(), getRulerProvider().getUnit());
        if (ruler.getUnit() == RulerProvider.UNIT_PIXELS) ruler.setInterval(100, 2);
        return ruler;
    }

    public void deactivate() {
        super.deactivate();
        getRulerProvider().removeRulerChangeListener(listener);
        rulerProvider = null;
        getRulerFigure().setZoomManager(null);
    }

    protected AccessibleEditPart getAccessibleEditPart() {
        if (accPart == null) accPart = new AccessibleGraphicalEditPart(this) {

            public void getName(AccessibleEvent e) {
                e.result = isHorizontal() ? GEFMessages.Ruler_Horizontal_Label : GEFMessages.Ruler_Vertical_Label;
            }

            public void getDescription(AccessibleEvent e) {
                e.result = GEFMessages.Ruler_Desc;
            }
        };
        return accPart;
    }

    /**
 * Returns the GraphicalViewer associated with the diagram.
 * 
 * @return graphical viewer associated with the diagram.
 */
    protected GraphicalViewer getDiagramViewer() {
        return diagramViewer;
    }

    public DragTracker getDragTracker(Request request) {
        if (request.getType().equals(REQ_SELECTION) && ((SelectionRequest) request).getLastButtonPressed() != 1) {
            return null;
        }
        return new RulerDragTracker(this);
    }

    public IFigure getGuideLayer() {
        LayerManager lm = (LayerManager) diagramViewer.getEditPartRegistry().get(LayerManager.ID);
        if (lm != null) return lm.getLayer(LayerConstants.GUIDE_LAYER);
        return null;
    }

    protected List getModelChildren() {
        return getRulerProvider().getGuides();
    }

    protected RulerFigure getRulerFigure() {
        return (RulerFigure) getFigure();
    }

    public RulerProvider getRulerProvider() {
        return rulerProvider;
    }

    public EditPart getTargetEditPart(Request request) {
        if (request.getType().equals(REQ_MOVE)) {
            return this;
        } else {
            return super.getTargetEditPart(request);
        }
    }

    public ZoomManager getZoomManager() {
        return (ZoomManager) diagramViewer.getProperty(ZoomManager.class.toString());
    }

    public void handleGuideReparented(Object guide) {
        refreshChildren();
        EditPart guidePart = (EditPart) getViewer().getEditPartRegistry().get(guide);
        if (guidePart != null) {
            getViewer().select(guidePart);
        }
    }

    public void handleUnitsChanged(int newUnit) {
        getRulerFigure().setUnit(newUnit);
        if (newUnit == RulerProvider.UNIT_PIXELS) getRulerFigure().setInterval(100, 2); else getRulerFigure().setInterval(0, 0);
    }

    public boolean isHorizontal() {
        return horizontal;
    }

    public void setParent(EditPart parent) {
        super.setParent(parent);
        if (getParent() != null && diagramViewer == null) {
            diagramViewer = (GraphicalViewer) getViewer().getProperty(GraphicalViewer.class.toString());
            RulerProvider hProvider = (RulerProvider) diagramViewer.getProperty(RulerProvider.PROPERTY_HORIZONTAL_RULER);
            if (hProvider != null && hProvider.getRuler() == getModel()) {
                rulerProvider = hProvider;
                horizontal = true;
            } else {
                rulerProvider = (RulerProvider) diagramViewer.getProperty(RulerProvider.PROPERTY_VERTICAL_RULER);
            }
        }
    }

    public static class RulerSelectionPolicy extends SelectionEditPolicy {

        protected void hideFocus() {
            ((RulerFigure) getHostFigure()).setDrawFocus(false);
        }

        protected void hideSelection() {
            ((RulerFigure) getHostFigure()).setDrawFocus(false);
        }

        protected void showFocus() {
            ((RulerFigure) getHostFigure()).setDrawFocus(true);
        }

        protected void showSelection() {
            ((RulerFigure) getHostFigure()).setDrawFocus(true);
        }
    }
}
