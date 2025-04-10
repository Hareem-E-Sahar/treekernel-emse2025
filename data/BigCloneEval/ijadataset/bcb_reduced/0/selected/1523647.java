package org.remus.infomngmnt.internal.file.commands;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.remus.InformationUnit;
import org.eclipse.remus.core.model.InformationStructureRead;
import org.eclipse.remus.ui.handlerutil.InformationHandlerUtil;
import org.eclipse.remus.ui.progress.CancelableRunnable;
import org.eclipse.remus.util.InformationUtil;
import org.eclipse.remus.util.StatusCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.remus.infomngmnt.file.Activator;
import org.remus.infomngmnt.file.Messages;

/**
 * @author Tom Seidel <tom.seidel@remus-software.org>
 */
public class SaveFileOnDiskHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        final List<InformationUnit> informationUnitsFromExecutionEvent = InformationHandlerUtil.getInformationUnitsFromExecutionEvent(event);
        Shell activeShell = HandlerUtil.getActiveShell(event);
        DirectoryDialog fd = new DirectoryDialog(activeShell, SWT.SAVE);
        String section = Activator.getDefault().getDialogSettings().get("lastExportSection");
        fd.setFilterPath(section);
        final String open = fd.open();
        if (open != null) {
            Activator.getDefault().getDialogSettings().put("lastExportSection", open);
            CancelableRunnable runnable = new CancelableRunnable() {

                @Override
                protected IStatus runCancelableRunnable(IProgressMonitor monitor) {
                    IStatus returnValue = Status.OK_STATUS;
                    monitor.beginTask(NLS.bind(Messages.SaveFileOnDiskHandler_SavingFiles, open), informationUnitsFromExecutionEvent.size());
                    for (InformationUnit informationUnit : informationUnitsFromExecutionEvent) {
                        if (!monitor.isCanceled()) {
                            monitor.setTaskName(NLS.bind(Messages.SaveFileOnDiskHandler_Saving, informationUnit.getLabel()));
                            InformationStructureRead read = InformationStructureRead.newSession(informationUnit);
                            read.getValueByNodeId(Activator.FILENAME);
                            IFile binaryReferenceFile = InformationUtil.getBinaryReferenceFile(informationUnit);
                            FileWriter writer = null;
                            try {
                                if (binaryReferenceFile != null) {
                                    File file = new File(open, (String) read.getValueByNodeId(Activator.FILENAME));
                                    InputStream contents = binaryReferenceFile.getContents();
                                    writer = new FileWriter(file);
                                    IOUtils.copy(contents, writer);
                                    monitor.worked(1);
                                }
                            } catch (Exception e) {
                                returnValue = StatusCreator.newStatus(NLS.bind(Messages.SaveFileOnDiskHandler_ErrorSaving, informationUnit.getLabel(), e));
                                break;
                            } finally {
                                if (writer != null) {
                                    try {
                                        writer.flush();
                                        writer.close();
                                    } catch (IOException e) {
                                    }
                                }
                            }
                        }
                    }
                    return returnValue;
                }
            };
            ProgressMonitorDialog progressMonitorDialog = new ProgressMonitorDialog(activeShell);
            try {
                progressMonitorDialog.run(true, true, runnable);
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof CoreException) {
                    ErrorDialog.openError(activeShell, Messages.SaveFileOnDiskHandler_ErrorSaving2, Messages.SaveFileOnDiskHandler_ErrorSaving2, ((CoreException) e.getCause()).getStatus());
                } else {
                    ErrorDialog.openError(activeShell, Messages.SaveFileOnDiskHandler_ErrorSaving2, Messages.SaveFileOnDiskHandler_ErrorSaving2, StatusCreator.newStatus(Messages.SaveFileOnDiskHandler_ErrorSaving3, e));
                }
            } catch (InterruptedException e) {
            }
        }
        return null;
    }
}
