package com.ibm.realtime.flexotask.editor.dialogs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import com.ibm.realtime.flexotask.editor.EEditPlugin;
import com.ibm.realtime.flexotask.editor.FlexotaskClasspathInitializer;
import com.ibm.realtime.flexotask.editor.model.GlobalTiming;
import com.ibm.realtime.flexotask.editor.model.LibraryProvider;

/**
 * Export a Flexotask Runtime based contributed libraries, including a special NativeRuntime provider
 */
public class RuntimeExportWizard extends Wizard implements IExportWizard {

    /** The array of RuntimeProviders configured in this Eclipse instance */
    private static RuntimeProvider[] runtimeProviders;

    /** The extension-point id for RuntimeProviders */
    private static final String RUNTIME_PROVIDER_EXTENSION = "com.ibm.realtime.flexotask.editor.RuntimeProvider";

    /** Maximum size of a data transfer buffer */
    private static final int BUFFER_THRESHHOLD = 1024 * 1024;

    /** The one page of this wizard */
    RuntimeExportWizardPage page;

    public void addPages() {
        int vmBridges = 0;
        StringBuilder builder = new StringBuilder("This runtime will contain:\n");
        RuntimeProvider[] providers = getRuntimeProviders();
        for (int i = 0; i < providers.length; i++) {
            builder.append("  ").append(providers[i].getDescription()).append("\n");
            if (providers[i].isVMBridge()) {
                vmBridges++;
            }
        }
        builder.append("  Standard Java libraries for the Flexotask API\n");
        LibraryProvider[] libraries = GlobalTiming.getAllProviders();
        for (int i = 0; i < libraries.length; i++) {
            builder.append("  ").append(libraries[i].getDescription()).append("\n");
        }
        if (vmBridges != 1) {
            boolean ok;
            if (vmBridges == 0) {
                ok = MessageDialog.openConfirm(getShell(), "Problem Detected", "No providers of real-time VM behavior have been registered; generated runtime will not be real-time");
            } else {
                ok = MessageDialog.openConfirm(getShell(), "Problem Detected", "There are " + vmBridges + " providers of real-time VM behavior (current system expects only one).  Results may be inconsistent");
            }
            if (!ok) {
                return;
            }
        }
        page = new RuntimeExportWizardPage(builder.toString());
        addPage(page);
    }

    public void init(IWorkbench workbench, IStructuredSelection selection) {
        setNeedsProgressMonitor(true);
    }

    public boolean performFinish() {
        final File destination = page.getDestination();
        page.saveSettings();
        if (destination.exists()) {
            if (!page.shouldOverwrite() && !MessageDialog.openConfirm(getShell(), "Should overwrite?", destination.getAbsolutePath() + " exists.  Replace with new contents?")) {
                return false;
            }
        }
        final boolean isDirectory = page.isDirectory();
        WorkspaceModifyOperation op = new WorkspaceModifyOperation() {

            protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
                monitor.beginTask("Exporting Runtime", 100);
                if (destination.exists()) {
                    if (!deleteDestination(destination)) {
                        monitor.worked(100);
                        MessageDialog.openError(getShell(), "Could not delete", destination.getAbsolutePath() + " could not be deleted");
                        return;
                    }
                }
                monitor.worked(10);
                if (monitor.isCanceled()) {
                    return;
                }
                exportRuntime(getShell(), destination, isDirectory, monitor);
            }
        };
        try {
            getContainer().run(true, true, op);
        } catch (Throwable e) {
            handleException(getShell(), e);
        }
        return true;
    }

    /**
	 * Remove a destination file or directory if possible
	 * @param destination the file or directory to remove
	 * @return true if removal was successful, false on error
	 */
    private boolean deleteDestination(File destination) {
        if (!destination.canWrite()) {
            return false;
        }
        if (destination.isFile()) {
            return destination.delete();
        }
        File[] children = destination.listFiles();
        for (int i = 0; i < children.length; i++) {
            if (!deleteDestination(children[i])) {
                return false;
            }
        }
        return destination.delete();
    }

    /**
	 * Export a runtime for use in a real-time VM.  At entry the destination is guaranteed not to exist
	 * @param shell the shell to use for any error messages or prompts
	 * @param destination the destination of the export
	 * @param isDirectory true if the export should be to a directory, false if to a zip file
	 * @param monitor the progress monitor to report progress
	 * @return true iff the export was successful, false otherwise (diagnostic will have been issued)
	 */
    public static boolean exportRuntime(Shell shell, File destination, boolean isDirectory, IProgressMonitor monitor) {
        OutputStream output = null;
        try {
            ExportController controller = isDirectory ? new DirectoryExportController(destination) : new ZipFileExportController(destination);
            RuntimeProvider[] providers = getRuntimeProviders();
            List<IClasspathEntry> accumulator = new ArrayList<IClasspathEntry>();
            GlobalTiming.addToClasspath(accumulator);
            int units = providers.length + accumulator.size() + 2;
            units = (int) (90.0 / units);
            for (RuntimeProvider runtimeProvider : providers) {
                ZipFile contribution = new ZipFile(runtimeProvider.contribute());
                if (contribution != null) {
                    output = transferZipFile(contribution, controller, output, monitor);
                }
                if (monitor.isCanceled()) {
                    return false;
                }
                monitor.worked(units);
            }
            monitor.subTask("flexotask.jar");
            output = transferFile(getBestAvailableJar(FlexotaskClasspathInitializer.FLEXOTASK_PLUGIN, "flexotask.jar"), controller, output, "lib/flexotask.jar");
            if (monitor.isCanceled()) {
                return false;
            }
            monitor.worked(units);
            monitor.subTask("realtimeAnalysis.jar");
            output = transferFile(getBestAvailableJar(FlexotaskClasspathInitializer.ANALYSIS_PLUGIN, "realtimeAnalysis.jar"), controller, output, "lib/realtimeAnalysis.jar");
            if (monitor.isCanceled()) {
                return false;
            }
            monitor.worked(units);
            for (IClasspathEntry classpathEntry : accumulator) {
                File toTransfer;
                String transferName;
                IPath path = classpathEntry.getPath();
                switch(classpathEntry.getEntryKind()) {
                    case IClasspathEntry.CPE_LIBRARY:
                        transferName = path.lastSegment();
                        if (FlexotaskClasspathInitializer.DEBUG_FILE.equals(transferName)) {
                            transferName = path.removeLastSegments(1).lastSegment() + ".jar";
                        }
                        toTransfer = new File(path.toString());
                        break;
                    case IClasspathEntry.CPE_PROJECT:
                        transferName = path.lastSegment() + ".jar";
                        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(classpathEntry.getPath().toString());
                        File location = project.getLocation().toFile();
                        toTransfer = new File(location, FlexotaskClasspathInitializer.DEBUG_FILE);
                        break;
                    default:
                        throw new IllegalStateException("Unexpected classpath entry type during export: " + classpathEntry);
                }
                monitor.subTask(transferName);
                output = transferFile(toTransfer, controller, output, "lib/ext/" + transferName);
                if (monitor.isCanceled()) {
                    return false;
                }
                monitor.worked(units);
            }
            output.close();
            return true;
        } catch (Throwable e) {
            handleException(shell, e);
            return false;
        }
    }

    /**
	 * Find the best available copy of a standard jar for a plugin, either from the plugin itself, or from the "debug.zip" of its
	 *   project
	 * @param pluginID the ID of the plugin
	 * @param jarToFind the name of the jar to find there
	 * @return the best available copy of jar
	 * @throws CoreException if thrown by the addPluginClasspath utility
	 * @throws IllegalStateException if no suitable copy could be found
	 */
    private static File getBestAvailableJar(String pluginID, String jarToFind) throws CoreException {
        List<IClasspathEntry> accumulator = new ArrayList<IClasspathEntry>();
        FlexotaskClasspathInitializer.addPluginClasspath(accumulator, pluginID);
        for (IClasspathEntry classpathEntry : accumulator) {
            switch(classpathEntry.getEntryKind()) {
                case IClasspathEntry.CPE_LIBRARY:
                    IPath path = classpathEntry.getPath();
                    if (jarToFind.equals(path.lastSegment()) || FlexotaskClasspathInitializer.DEBUG_FILE.equals(path.lastSegment())) {
                        return path.toFile();
                    }
                    break;
                case IClasspathEntry.CPE_PROJECT:
                    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(classpathEntry.getPath().toString());
                    File location = project.getLocation().toFile();
                    return new File(location, FlexotaskClasspathInitializer.DEBUG_FILE);
            }
        }
        throw new IllegalStateException("No usable copy of " + jarToFind + " was found");
    }

    /**
	 * Utility to find all the RuntimeProviders in the system
	 * @return an array containing all the RuntimeProviders in the system
	 */
    private static RuntimeProvider[] getRuntimeProviders() {
        if (runtimeProviders == null) {
            IExtensionRegistry registry = Platform.getExtensionRegistry();
            IExtensionPoint extensionPoint = registry.getExtensionPoint(RUNTIME_PROVIDER_EXTENSION);
            if (extensionPoint == null) {
                runtimeProviders = new RuntimeProvider[0];
            } else {
                IConfigurationElement points[] = extensionPoint.getConfigurationElements();
                List<RuntimeProvider> runtimeList = new ArrayList<RuntimeProvider>();
                for (int i = 0; i < points.length; i++) {
                    IConfigurationElement point = points[i];
                    RuntimeProvider rtp;
                    try {
                        rtp = (RuntimeProvider) point.createExecutableExtension("class");
                        runtimeList.add(rtp);
                    } catch (CoreException e) {
                    }
                }
                runtimeProviders = (RuntimeProvider[]) runtimeList.toArray(new RuntimeProvider[runtimeList.size()]);
            }
        }
        return runtimeProviders;
    }

    /**
	 * Handle an exception that occurs in this operation
	 * @param shell the shell to use
	 * @param exception the exception that occurred
	 */
    private static void handleException(final Shell shell, Throwable exception) {
        if (exception instanceof InvocationTargetException) {
            exception = ((InvocationTargetException) exception).getTargetException();
        }
        IStatus status = null;
        if (exception instanceof CoreException) {
            status = ((CoreException) exception).getStatus();
        }
        if (status == null) {
            status = new Status(Status.ERROR, EEditPlugin.ID, exception.getMessage(), exception);
        }
        ResourcesPlugin.getPlugin().getLog().log(status);
        final IStatus istatus = status;
        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

            public void run() {
                ErrorDialog.openError(shell, "Error during export", istatus.getMessage(), istatus);
            }
        });
    }

    /**
	 * Transfer a file to the destination
	 * @param file the file to transfer
	 * @param controller the ExportController for the destination
	 * @param output the OutputStream from the previous export step or null if this is the first step
	 * @param targetName the name that the logical file should have in the destination
	 * @return the OutputStream that was used for this export step
	 * @throws IOException 
	 * @throws CoreException
	 */
    private static OutputStream transferFile(File file, ExportController controller, OutputStream output, String targetName) throws IOException, CoreException {
        InputStream input = new FileInputStream(file);
        return transferStream(input, input.available(), controller, output, targetName, 0);
    }

    /**
	 * Transfer an input stream to the destination
	 * @param input the input stream to transfer
	 * @param length the length of the input stream
	 * @param controller the ExportController for the destination
	 * @param output the OutputStream from the previous export step or null if this is the first step
	 * @param targetName the name that the logical file should have in the destination
	 * @param mode the unix mode for the output if non-zero, ignored if zero
	 * @return the OutputStream that was used for this export step
	 * @throws IOException 
	 * @throws CoreException 
	 */
    private static OutputStream transferStream(InputStream input, int length, ExportController controller, OutputStream output, String targetName, int mode) throws IOException, CoreException {
        output = controller.getOutputStream(output, targetName, mode);
        int remaining = length;
        byte[] buffer;
        if (remaining > BUFFER_THRESHHOLD) {
            buffer = new byte[BUFFER_THRESHHOLD];
        } else {
            buffer = new byte[remaining];
        }
        while (remaining > 0) {
            int toRead = remaining > buffer.length ? buffer.length : remaining;
            int amount = input.read(buffer, 0, toRead);
            output.write(buffer, 0, amount);
            remaining -= amount;
        }
        input.close();
        return output;
    }

    /**
	 * Transfer the contents of a zip file (file by file) to the destination
	 * @param zipFile the zip file to transfer
	 * @param controller the ExportController for the destination
	 * @param output the OutputStream from the previous export step or null if this is the first step
	 * @param monitor progress monitor on which to report subtasks
	 * @return the last OutputStream that was used for this export step
	 * @throws IOException
	 * @throws CoreException
	 */
    private static OutputStream transferZipFile(ZipFile zipFile, ExportController controller, OutputStream output, IProgressMonitor monitor) throws IOException, CoreException {
        Enumeration<?> entries = zipFile.getEntries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            if (entry.isDirectory()) {
                continue;
            }
            monitor.subTask(entry.getName());
            int mode = entry.getUnixMode();
            InputStream input = zipFile.getInputStream(entry);
            output = transferStream(input, (int) entry.getSize(), controller, output, entry.getName(), mode);
        }
        zipFile.close();
        return output;
    }

    /**
	 * An ExportController implementation for the case where the output is a directory containing files
	 *   and subdirectories
	 */
    private static final class DirectoryExportController implements ExportController {

        /** The base directory */
        private File baseDirectory;

        /** The local file system */
        private static final IFileSystem fileSystem = EFS.getLocalFileSystem();

        /** The currently open IFileStore iff the executable bit should be set, otherwise null */
        private IFileStore currentlyOpen;

        /**
		 * Create a new DirectoryExportController
		 * @param baseDirectory the base directory of the export area
		 */
        DirectoryExportController(File baseDirectory) {
            this.baseDirectory = baseDirectory;
            baseDirectory.mkdir();
        }

        public OutputStream getOutputStream(OutputStream former, String newFile, int mode) throws IOException, CoreException {
            if (former != null) {
                former.close();
                if (currentlyOpen != null) {
                    IFileInfo info = EFS.createFileInfo();
                    info.setAttribute(EFS.ATTRIBUTE_EXECUTABLE, true);
                    currentlyOpen.putInfo(info, EFS.SET_ATTRIBUTES, null);
                }
            }
            File toCreate = new File(baseDirectory, newFile);
            toCreate.getParentFile().mkdirs();
            IFileStore target = fileSystem.fromLocalFile(toCreate);
            OutputStream ans = target.openOutputStream(0, null);
            currentlyOpen = ((mode & 0111) != 0) ? target : null;
            return ans;
        }
    }

    /**
	 * An interface to hide the difference between zipfile and directory/file output
	 */
    private static interface ExportController {

        /**
		 * Get the output stream for exporting a logical file (which either becomes a file or a
		 *   entry in a zip file)
		 * @param former the former OutputStream or null if this is the first file to be exported
		 * @param newFile the path name of the new logical file to be added
		 * @param mode unix mode if non-zero, ignored if zero
		 * @return a new (or perhaps the same) OutputStream, positioned to accept the new logical file
		 * @throws IOException on I/O errors detected by non-Eclipse components
		 * @throws CoreException on errors detected by Eclipse components
		 */
        OutputStream getOutputStream(OutputStream former, String newFile, int mode) throws IOException, CoreException;
    }

    /**
	 * An ExportController implementation for the case where the output is a zip file
	 */
    private static final class ZipFileExportController implements ExportController {

        private ZipOutputStream output;

        /**
		 * Create a new ZipFileExportController
		 * @param zipFile the file to create (does not yet exist) 
		 * @throws FileNotFoundException 
		 */
        public ZipFileExportController(File zipFile) throws FileNotFoundException {
            output = new ZipOutputStream(new FileOutputStream(zipFile));
        }

        public OutputStream getOutputStream(OutputStream former, String newFile, int mode) throws IOException {
            if (former != null) {
                assert former == output;
                output.closeEntry();
            }
            ZipEntry newEntry = new ZipEntry("flexotask-runtime/" + newFile);
            if (mode != 0) {
                newEntry.setUnixMode(mode);
            }
            output.putNextEntry(newEntry);
            return output;
        }
    }
}
