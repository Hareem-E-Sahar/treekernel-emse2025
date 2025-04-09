package org.wtc.eclipse.platform.reset;

import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.util.ScreenCapture;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.wtc.eclipse.core.reset.IResetDaemon;
import org.wtc.eclipse.platform.PlatformActivator;
import org.wtc.eclipse.platform.conditions.JobExistsCondition;
import org.wtc.eclipse.platform.helpers.EclipseHelperFactory;
import org.wtc.eclipse.platform.helpers.IResourceHelper;
import org.wtc.eclipse.platform.helpers.IWorkbenchHelper;
import java.io.File;
import java.io.FilenameFilter;
import java.util.HashSet;
import java.util.Set;

/**
 * Reset daemon that makes sure all projects in the workspace have been copied into a
 * fixed location then removed from the workspace.
 */
public class ProjectResetDaemon implements IResetDaemon {

    private static final String[] TOFILTER = { "class", "jar", "JAR", "zip", "ZIP" };

    private final Set<String> FILTERED_EXTENSIONS;

    private final ProjectResetFileFilter _fileFilter;

    /**
     * Save the data members.
     */
    public ProjectResetDaemon() {
        FILTERED_EXTENSIONS = new HashSet<String>();
        for (String nextExt : TOFILTER) {
            FILTERED_EXTENSIONS.add(nextExt);
        }
        _fileFilter = new ProjectResetFileFilter();
    }

    /**
     * @see  org.wtc.eclipse.core.reset.IResetDaemon#resetWorkspace(com.windowtester.runtime.IUIContext,
     *       org.wtc.eclipse.core.reset.IResetDaemon.ResetContext)
     */
    public void resetWorkspace(final IUIContext ui, final ResetContext context) {
        IWorkbenchHelper workbench = EclipseHelperFactory.getWorkbenchHelper();
        workbench.saveAndWait(ui);
        workbench.waitForBuild(ui);
        ui.wait(new JobExistsCondition(null), 120000, 1000);
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        if (root == null) {
            return;
        }
        final IProject[] allProjects = root.getProjects();
        if ((allProjects == null) || (allProjects.length == 0)) {
            return;
        }
        File[] projectLocations = new File[allProjects.length];
        for (int i = 0; i < allProjects.length; i++) {
            projectLocations[i] = allProjects[i].getLocation().toFile();
        }
        try {
            IResourceHelper resources = EclipseHelperFactory.getResourceHelper();
            resources.createZipCopy(ui, context.getTestClassName(), projectLocations, _fileFilter);
        } catch (Throwable t) {
            ScreenCapture.createScreenCapture(context.getTestClassName() + "_ProjectResetDaemon");
            PlatformActivator.logException(t);
        } finally {
            IWorkspaceRunnable noResourceChangedEventsRunner = new IWorkspaceRunnable() {

                public void run(IProgressMonitor runnerMonitor) throws CoreException {
                    CoreException lastCE = null;
                    for (IProject nextProject : allProjects) {
                        try {
                            nextProject.close(runnerMonitor);
                        } catch (CoreException ce) {
                            PlatformActivator.logException(ce);
                            lastCE = ce;
                        }
                    }
                    if (lastCE != null) {
                        throw lastCE;
                    }
                }
            };
            boolean success = false;
            int retry = 0;
            while (!success && (retry < 5)) {
                try {
                    IWorkspace workspace = ResourcesPlugin.getWorkspace();
                    workspace.run(noResourceChangedEventsRunner, workspace.getRoot(), IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
                    success = true;
                } catch (CoreException ce) {
                    ui.pause(2000);
                    retry++;
                }
            }
            ui.pause(5000);
            workbench.waitForBuild(ui);
            ui.wait(new JobExistsCondition(null), 120000, 1000);
            noResourceChangedEventsRunner = new IWorkspaceRunnable() {

                public void run(IProgressMonitor runnerMonitor) throws CoreException {
                    CoreException lastCE = null;
                    for (IProject nextProject : allProjects) {
                        if (nextProject.exists()) {
                            try {
                                nextProject.delete(true, true, null);
                            } catch (CoreException ce) {
                                PlatformActivator.logException(ce);
                                lastCE = ce;
                            }
                        }
                    }
                    if (lastCE != null) {
                        throw lastCE;
                    }
                }
            };
            success = false;
            retry = 0;
            while (!success && (retry < 5)) {
                try {
                    IWorkspace workspace = ResourcesPlugin.getWorkspace();
                    workspace.run(noResourceChangedEventsRunner, workspace.getRoot(), IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
                    success = true;
                } catch (CoreException ce) {
                    ui.pause(2000);
                    retry++;
                }
            }
            ui.pause(3000);
            workbench.waitForBuild(ui);
            ui.wait(new JobExistsCondition(null), 120000, 1000);
        }
    }

    /**
     * File filter for file extensions.
     */
    private class ProjectResetFileFilter implements FilenameFilter {

        /**
         * @see  java.io.FilenameFilter#accept(java.io.File, java.lang.String)
         */
        public boolean accept(File file, String name) {
            boolean shouldFilter = false;
            IPath filePath = new Path(file.getAbsolutePath());
            String extension = filePath.getFileExtension();
            if (extension != null) {
                shouldFilter = FILTERED_EXTENSIONS.contains(extension);
            }
            return !shouldFilter;
        }
    }
}
