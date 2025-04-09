package com.simontuffs.eclipse.jarplug.popup.actions;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import com.simontuffs.eclipse.jarplug.views.JarPlug;

public class UpdateArchive implements IObjectActionDelegate {

    protected String archive = null;

    protected static boolean dontShow = false, dontShowBroken = false;

    protected static final boolean debug = false;

    /**
	 * Constructor for Action1.
	 */
    public UpdateArchive() {
        super();
    }

    /**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        if (debug) System.out.println("setActivePart(" + action + ", " + targetPart);
    }

    /**
	 * @see IActionDelegate#run(IAction)
	 */
    public void run(IAction action) {
        if (action.getId().endsWith("extractArchive")) {
            System.err.println("UpdateArchive.extractArchive not implemented");
        } else if (action.getId().endsWith("updateArchive")) {
            update(archive);
        }
    }

    public void update(String archive) {
        updateArchive(archive);
    }

    protected void updateArchive(String archive) {
        ProgressMonitorDialog progress = null;
        final List brokenFiles = new ArrayList();
        IPath outputPath = null;
        try {
            Workspace workspace = (Workspace) ResourcesPlugin.getWorkspace();
            IPath path = workspace.getRoot().getFullPath().append(archive);
            IProject project = JarPlug.getInstance().getCurrentProject();
            IPath projectPath = project.getFullPath();
            IPath last = path.removeFirstSegments(1);
            if (last.segment(0).equals(JarPlug.JARPLUG)) last = last.removeFirstSegments(1);
            String fileName = last.lastSegment();
            if (fileName.endsWith("." + JarPlug.WORK)) {
                last = last.removeLastSegments(1);
                int index = fileName.lastIndexOf("." + JarPlug.WORK);
                fileName = fileName.substring(0, index);
                last = last.append(fileName);
            }
            IPath inputPath = path;
            outputPath = projectPath.append(JarPlug.JARPLUG).append(last);
            IPath mergePath = projectPath.append(JarPlug.JARPLUG).append(last);
            if (!mergePath.lastSegment().endsWith("." + JarPlug.WORK)) mergePath = mergePath.addFileExtension(JarPlug.WORK);
            IFolder merge = (IFolder) workspace.newResource(mergePath, IResource.FOLDER);
            if (!merge.exists()) {
                int count = mergePath.segmentCount();
                mergePath = mergePath.addFileExtension(JarPlug.WORK);
                merge = (IFolder) workspace.newResource(mergePath, IResource.FOLDER);
            }
            for (int i = 1; i < outputPath.segmentCount() - 1; i++) {
                IFolder folder = (IFolder) workspace.newResource(outputPath.uptoSegment(i + 1), IResource.FOLDER);
                if (!folder.exists()) folder.create(true, true, null);
            }
            IFile source = (IFile) workspace.newResource(inputPath, IResource.FILE);
            boolean rename = false;
            if (inputPath.equals(outputPath)) {
                outputPath = outputPath.addFileExtension("new");
                rename = true;
            }
            IFile target = (IFile) workspace.newResource(outputPath, IResource.FILE);
            if (target.exists()) target.delete(true, true, null);
            IPath location = source.getLocation();
            if (location == null) location = source.getFullPath();
            ZipFile zip = new ZipFile(location.toFile());
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(target.getLocation().toFile()));
            if (debug) System.out.println("UpdateArchive.new ZipFile(" + location + " -> " + target.getLocation() + ")");
            progress = new ProgressMonitorDialog(new Shell());
            progress.open();
            IProgressMonitor monitor = progress.getProgressMonitor();
            int count = zip.size();
            if (debug) System.out.println("Updating " + count + " items");
            monitor.beginTask("Updating " + outputPath, count);
            Enumeration entries = zip.entries();
            Set written = new HashSet();
            ZipEntry entry = null;
            while (entries.hasMoreElements()) {
                entry = (ZipEntry) entries.nextElement();
                IPath entryPath = mergePath.append(entry.getName());
                IFile override = (IFile) workspace.newResource(entryPath, IResource.FILE);
                InputStream in = null;
                try {
                    if (override.exists() && !entryPath.toFile().isDirectory()) {
                        if (debug) System.out.println("Override: " + source + "/" + entry.getName());
                        in = override.getContents();
                        written.add(override);
                    } else if (!entry.isDirectory()) {
                        if (!JarPlug.getInstance().isDeleted(entry.getName())) {
                            in = zip.getInputStream(entry);
                        } else {
                            if (debug) System.out.println("Deleted " + entry.getName());
                        }
                    }
                    if (in != null) {
                        out.putNextEntry(new ZipEntry(entry.getName()));
                        copy(in, out);
                        out.closeEntry();
                    }
                } catch (ZipException zx) {
                    System.err.println("Problem writing entry " + entry + " to file " + location);
                    brokenFiles.add((entry == null ? "" + null : entry.getName()) + ": " + zx);
                } finally {
                    if (in != null) in.close();
                }
                monitor.worked(1);
            }
            zip.close();
            monitor.done();
            progress.close();
            progress = null;
            if (merge.exists()) {
                List members = new LinkedList(Arrays.asList(merge.members()));
                while (!members.isEmpty()) {
                    IResource member = (IResource) members.remove(0);
                    if (member instanceof IFolder) {
                        members.addAll(Arrays.asList(((IFolder) member).members()));
                        continue;
                    }
                    if (!written.contains(member) && !member.getLocation().toFile().isDirectory()) {
                        if (debug) System.out.println("Merging " + member);
                        String entryName = member.getFullPath().removeFirstSegments(merge.getFullPath().segmentCount()).toString();
                        out.putNextEntry(new ZipEntry(entryName));
                        IFile file = (IFile) workspace.newResource(member.getFullPath(), IResource.FILE);
                        InputStream in = file.getContents();
                        copy(in, out);
                        out.closeEntry();
                        in.close();
                    }
                }
            }
            out.close();
            target.refreshLocal(0, null);
            if (rename) {
                source.delete(true, true, null);
                outputPath = inputPath;
                target.move(outputPath, true, null);
                target = (IFile) workspace.newResource(outputPath, IResource.FILE);
            }
            JarPlug.getInstance().refresh(target);
            if (!dontShow) {
                Shell shell = new Shell();
                MessageDialogWithToggle dialog = MessageDialogWithToggle.openInformation(shell, "JarPlug", "New archive written to: " + outputPath, "Don't Show This Again", false, null, "dont.show.again");
                dontShow = dialog.getToggleState();
            }
        } catch (Exception x) {
            brokenFiles.add(outputPath + ": " + x);
            if (debug) x.printStackTrace();
            RuntimeException e = new RuntimeException("JarPlug: " + x, x);
            throw e;
        } finally {
            if (progress != null) progress.close();
            Shell shell = new Shell();
            if (!brokenFiles.isEmpty()) {
                if (!dontShowBroken) {
                    MessageDialogWithToggle dialog = new MessageDialogWithToggle(shell, "JarPlug", null, "Problems writing files to archive: " + outputPath, MessageDialog.ERROR, new String[] { IDialogConstants.OK_LABEL }, 0, "Don't Show This Again", false) {

                        protected Control createCustomArea(Composite parent) {
                            GridData gd = new GridData(GridData.FILL_HORIZONTAL);
                            gd.widthHint = 400;
                            gd.heightHint = 300;
                            Text text = new Text(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
                            text.setEditable(false);
                            text.setLayoutData(gd);
                            for (int i = 0; i < brokenFiles.size(); i++) {
                                text.append(brokenFiles.get(i) + "\n");
                            }
                            return text;
                        }
                    };
                    dialog.open();
                    dontShowBroken = dialog.getToggleState();
                }
            }
        }
    }

    public long copy(InputStream in, OutputStream out) throws IOException {
        byte buf[] = new byte[1024];
        int len = 0;
        long tot = 0;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
            tot += len;
        }
        return tot;
    }

    /**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
    public void selectionChanged(IAction action, ISelection $selection) {
        if ($selection instanceof StructuredSelection) {
            StructuredSelection selection = (StructuredSelection) $selection;
            Object first = selection.getFirstElement();
            if (first != null) {
                if (first instanceof IResource) {
                    IResource resource = (IResource) first;
                    archive = resource.getFullPath().toString();
                } else if (first instanceof IPackageFragmentRoot) {
                    IPackageFragmentRoot root = (IPackageFragmentRoot) first;
                    archive = root.getPath().toString();
                } else {
                    if (debug) System.out.println("selection=" + first + " (" + first.getClass().getName() + ")");
                }
            }
        }
    }
}
