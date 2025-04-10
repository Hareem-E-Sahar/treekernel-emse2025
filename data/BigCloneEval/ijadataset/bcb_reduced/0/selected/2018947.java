package org.eclipse.gef.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.internal.GEFMessages;

/** 
 * An action which selects all edit parts in the active workbench part.
 */
public class SelectAllAction extends Action {

    private IWorkbenchPart part;

    /**
 * Constructs a <code>SelectAllAction</code> and associates it with the given
 * part.
 * @param part The workbench part associated with this SelectAllAction
 */
    public SelectAllAction(IWorkbenchPart part) {
        this.part = part;
        setText(GEFMessages.SelectAllAction_Label);
        setToolTipText(GEFMessages.SelectAllAction_Tooltip);
        setId(ActionFactory.SELECT_ALL.getId());
    }

    /**
 * Selects all edit parts in the active workbench part.
 */
    public void run() {
        GraphicalViewer viewer = (GraphicalViewer) part.getAdapter(GraphicalViewer.class);
        if (viewer != null) viewer.setSelection(new StructuredSelection(viewer.getContents().getChildren()));
    }
}
