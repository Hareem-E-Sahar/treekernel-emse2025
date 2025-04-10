package org.eclipse.gef.ui.actions;

import org.eclipse.swt.SWT;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.internal.GEFMessages;
import org.eclipse.gef.print.PrintGraphicalViewerOperation;

/**
 * @author hudsonr
 * @since 2.1
 */
public class PrintAction extends WorkbenchPartAction {

    /**
 * Constructor for PrintAction.
 * @param part The workbench part associated with this PrintAction
 */
    public PrintAction(IWorkbenchPart part) {
        super(part);
    }

    /**
 * @see org.eclipse.gef.ui.actions.WorkbenchPartAction#calculateEnabled()
 */
    protected boolean calculateEnabled() {
        PrinterData[] printers = Printer.getPrinterList();
        return printers != null && printers.length > 0;
    }

    /**
 * @see org.eclipse.gef.ui.actions.EditorPartAction#init()
 */
    protected void init() {
        super.init();
        setText(GEFMessages.PrintAction_Label);
        setToolTipText(GEFMessages.PrintAction_Tooltip);
        setId(ActionFactory.PRINT.getId());
    }

    /**
 * @see org.eclipse.jface.action.Action#run()
 */
    public void run() {
        GraphicalViewer viewer;
        viewer = (GraphicalViewer) getWorkbenchPart().getAdapter(GraphicalViewer.class);
        PrintDialog dialog = new PrintDialog(viewer.getControl().getShell(), SWT.NULL);
        PrinterData data = dialog.open();
        if (data != null) {
            PrintGraphicalViewerOperation op = new PrintGraphicalViewerOperation(new Printer(data), viewer);
            op.run(getWorkbenchPart().getTitle());
        }
    }
}
