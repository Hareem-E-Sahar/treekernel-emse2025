package org.sourcejammer.client.gui.action;

import java.awt.event.ActionEvent;
import javax.swing.*;
import org.sourcejammer.client.gui.*;
import org.sourcejammer.client.gui.dialog.RestoreNameDialog;
import org.sourcejammer.project.view.NodeName;
import java.util.Vector;
import java.util.Enumeration;

/**
 * Title:        SourceJammer v 0.1.0
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author Robert MacGrogan
 * @version $Revision: 1.3 $
 */
public class RestoreRemovedAction extends AbstractAction {

    public RestoreRemovedAction() {
    }

    public void actionPerformed(ActionEvent ev) {
        Object obj = ev.getSource();
        if (obj instanceof RemovedItem[]) {
            RemovedItem[] deleteItems = (RemovedItem[]) obj;
            Vector vecItems = new Vector();
            for (int i = 0; i < deleteItems.length; i++) {
                if (vecItems.size() > 0) {
                    int iSelection = deleteItems[i].getNumber();
                    int iBeginSearchRange = 0;
                    int iEndSearchRange = vecItems.size() - 1;
                    while (iEndSearchRange != iBeginSearchRange) {
                        int iRange = (iEndSearchRange - iBeginSearchRange) + 1;
                        int iCheckPoint = iBeginSearchRange + ((iRange / 2));
                        int iItemNumber = ((RemovedItem) vecItems.get(iCheckPoint)).getNumber();
                        if (iSelection < iItemNumber) {
                            iBeginSearchRange = iCheckPoint;
                        } else {
                            iEndSearchRange = iCheckPoint - 1;
                        }
                    }
                    if (((RemovedItem) vecItems.get(iBeginSearchRange)).getNumber() < iSelection) {
                        vecItems.add(iBeginSearchRange, deleteItems[i]);
                    } else {
                        vecItems.add(iBeginSearchRange + 1, deleteItems[i]);
                    }
                } else {
                    vecItems.add(deleteItems[i]);
                }
            }
            CommandCentral oCommand = CommandCentral.getInstance();
            NodeName ndParent = oCommand.getCurrentTreeNode().getNodeName();
            boolean bSkipRestoreNameDialog = false;
            String sNewName = null;
            Enumeration enmItems = vecItems.elements();
            while (enmItems.hasMoreElements()) {
                RemovedItem item = (RemovedItem) enmItems.nextElement();
                sNewName = null;
                if (!bSkipRestoreNameDialog) {
                    RestoreNameDialog dialog = new RestoreNameDialog(item.getName());
                    int iAction = dialog.showDialog(oCommand.getRootAppFrame());
                    if (iAction == RestoreNameDialog.CANCEL_BUTTON_CLICKED || iAction == RestoreNameDialog.DIALOG_CLOSED) {
                        break;
                    }
                    sNewName = dialog.getNewName();
                    bSkipRestoreNameDialog = dialog.getUseExistingNameForAll();
                }
                oCommand.restoreRemoved(ndParent, item.getNumber(), sNewName);
            }
            ActionCentral.getInstance().fireAction(ActionCentral.act_REFRESH_PROJECT);
        }
    }
}
