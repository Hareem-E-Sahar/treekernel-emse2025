package org.pr.usesystemdesktop.open;

import java.io.File;
import java.io.IOException;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

public class OpenWithDesktopAction extends CookieAction {

    protected static final java.awt.Desktop _desktop = java.awt.Desktop.getDesktop();

    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes != null) {
            if (activatedNodes.length == 1) {
                DataObject dataObject = activatedNodes[0].getLookup().lookup(DataObject.class);
                if (dataObject.getPrimaryFile() != null) {
                    FileObject obj = dataObject.getPrimaryFile();
                    File file = FileUtil.toFile(obj);
                    if (file != null && file.exists()) {
                        try {
                            if (obj.isData()) {
                                try {
                                    _desktop.edit(file);
                                } catch (IOException ex) {
                                    _desktop.open(file);
                                }
                            } else {
                                _desktop.open(file);
                            }
                            String message = NbBundle.getMessage(OpenWithDesktopAction.class, "CTL_OpenWithDesktopAction.opening.text", obj.getPath());
                            StatusDisplayer.getDefault().setStatusText(message);
                        } catch (IOException ex) {
                            String message = NbBundle.getMessage(OpenWithDesktopAction.class, "CTL_OpenWithDesktopAction.opening.io.error", obj.getPath());
                            StatusDisplayer.getDefault().setStatusText(message);
                            NotifyDescriptor.Exception exp = new NotifyDescriptor.Exception(ex, message);
                            DialogDisplayer.getDefault().notify(exp);
                        }
                    } else {
                        String message = NbBundle.getMessage(OpenWithDesktopAction.class, "CTL_OpenWithDesktopAction.opening.text", obj.getPath());
                        StatusDisplayer.getDefault().setStatusText(message);
                        NotifyDescriptor.Message msg = new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notify(msg);
                    }
                }
            }
        }
    }

    protected int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }

    public String getName() {
        return NbBundle.getMessage(OpenWithDesktopAction.class, "CTL_OpenWithDesktopAction");
    }

    protected Class[] cookieClasses() {
        return new Class[] { DataObject.class };
    }

    @Override
    protected void initialize() {
        super.initialize();
        putValue("noIconInMenu", Boolean.TRUE);
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        boolean _enable = false;
        if (isDesktopActionEnabled()) {
            if (activatedNodes != null) {
                if (activatedNodes.length == 1) {
                    DataObject dataObject = activatedNodes[0].getLookup().lookup(DataObject.class);
                    if (dataObject != null) {
                        FileObject obj = dataObject.getPrimaryFile();
                        if (obj != null) {
                            File file = FileUtil.toFile(obj);
                            _enable = file != null;
                        }
                    }
                }
            }
        }
        return _enable;
    }

    protected final boolean isDesktopEnabled() {
        return java.awt.Desktop.isDesktopSupported();
    }

    /**
     * Child classes should over-ride this method to ensure that the related Desktop.Action is supported.
     * @return 
     */
    protected boolean isDesktopActionEnabled() {
        boolean desktopActionEnabled = isDesktopEnabled();
        if (desktopActionEnabled) {
            desktopActionEnabled = java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.OPEN) && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.EDIT);
        }
        return desktopActionEnabled;
    }
}
