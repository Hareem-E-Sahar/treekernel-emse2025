package org.xaware.help.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.ant.core.AntRunner;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.CopyFilesAndFoldersOperation;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.xaware.help.HelpPlugin;
import org.xaware.help.XAHelpLogger;
import org.xaware.help.examples.wizard.ExampleProjectDefinition;
import org.xaware.ide.xadev.XADesignerLogger;
import org.xaware.ide.xadev.XAServicesHandler;
import org.xaware.ide.xadev.XA_Designer_Plugin;
import org.xaware.ide.xadev.common.ControlFactory;
import org.xaware.ide.xadev.common.GlobalConstants;
import org.xaware.ide.xadev.common.ResourceUtils;
import org.xaware.ide.xadev.datamodel.ExampleProjectInfo;
import org.xaware.ide.xadev.gui.view.NavigatorView;
import org.xaware.ide.xadev.gui.view.XAProjectExplorer;
import org.xaware.shared.util.XAwareException;

/**
 * Utility class containing methods for creating example project.
 * 
 * @author blueAlly.
 * 
 */
public class ExampleProjectUtil {

    /** String constant for 'exampleHelp' */
    private static final String EXAMPLE_HELP = "exampleHelp";

    /** String constant for 'Failed to update .xawareproject file with example's context identifier' */
    private static final String HELP_UPDATE_ERROR = "Failed to update .xawareproject file with example's context identifier";

    /** String constant for 'cheatSheetId' element name*/
    private static final String CHEAT_SHEET_ID = "cheatSheetId";

    /** String constant for 'Failed to update .xawareproject file with cheatsheet identifier' */
    private static final String CHEAT_SHEET_UPDATE_ERROR = "Failed to update .xawareproject file with cheatsheets identifier";

    /**String constant to hold the Starter Project example project Id.*/
    private static final String STARTER_EXAMPLE_PROJ_ID = "org.xaware.ExampleProjectCreationWizard.starter";

    /**
     * Creates the project with the specified name.
     * 
     * @param projectName
     *            Name of project created in Eclipse workspace.
     * @param projectLocation
     *            String value representing project Location.
     * @param monitor
     *            IProgressMonitor instance.
     * @param configElement
     *            Instance of IConfigurationElement.
     * @param resourceFolder
     *            String
     * @param enableSyntaxChecking -
     *            boolean indicating syntax checking should be automatic or not
     * @param shell
     *            shell instance.
     * @param projDef
     *            project definition.
     * @param createProject
     *            boolean variable indicating whether to create a project or not. If it is true, creates the example
     *            project and updates its contents from example folder If it is false, means the project is already
     *            created, so it just updates its contents.s
     * 
     * @return true if project is successfully created, false otherwise.
     */
    public static boolean createExampleProject(final String projectName, final IPath projectLocation, IProgressMonitor progressMonitor, final IConfigurationElement configElement, final String resourceFolder, final boolean enableSyntaxChecking, final Shell shell, ExampleProjectDefinition projDef, boolean createProject) {
        return createExampleProject(projectName, projectLocation, progressMonitor, configElement, resourceFolder, enableSyntaxChecking, shell, projDef, createProject, true);
    }

    /**
     * Creates the project with the specified name.
     * 
     * @param projectName
     *            Name of project created in Eclipse workspace.
     * @param projectLocation
     *            String value representing project Location.
     * @param monitor
     *            IProgressMonitor instance.
     * @param configElement
     *            Instance of IConfigurationElement.
     * @param resourceFolder
     *            String
     * @param enableSyntaxChecking -
     *            boolean indicating syntax checking should be automatic or not
     * @param shell
     *            shell instance.
     * @param projDef
     *            project definition.
     * @param createProject
     *            boolean variable indicating whether to create a project or not. If it is true, creates the example
     *            project and updates its contents from example folder If it is false, means the project is already
     *            created, so it just updates its contents.
     * @param fireProjectSpecifics fires the projects specifics like dynamic help, cheat sheet etc.           
     * 
     * @return true if project is successfully created, false otherwise.
     */
    public static boolean createExampleProject(String projectName, IPath projectLocation, IProgressMonitor progressMonitor, IConfigurationElement configElement, String resourceFolder, boolean enableSyntaxChecking, Shell shell, ExampleProjectDefinition projDef, boolean createProject, boolean fireProjectSpecifics) {
        if (createProject) {
            final String status = ControlFactory.createProject(projectName, projectLocation, progressMonitor, configElement, resourceFolder, enableSyntaxChecking);
            if ((status != null) && !status.trim().equals("")) {
                ControlFactory.showMessageDialog(status, "New XA-Designer Project", SWT.ICON_ERROR);
                return false;
            }
        }
        final IProject projectHandle = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
        final String exampleFolder = projDef.getFolder();
        final URL exampleUrl = FileLocator.find(HelpPlugin.getDefault().getBundle(), new Path(exampleFolder), null);
        copyExample(exampleUrl, projectHandle, shell);
        final File antBuildFile = new File(projectHandle.getLocation().toOSString() + "/build.xml");
        if (antBuildFile.exists()) {
            final IProgressMonitor monitor = new NullProgressMonitor();
            final AntRunner runner = new AntRunner();
            runner.setBuildFileLocation(antBuildFile.getAbsolutePath());
            runner.setArguments("-DXAWARE_HOME=\"" + System.getProperty("xaware.home") + "\"" + " -DProjectLocation=\"" + projectHandle.getLocation().toOSString() + "\" -DProjectLocPropSafe=\"" + projectHandle.getLocation().toOSString().replace('\\', '/') + "\" -logfile ./ant.log -verbose");
            try {
                runner.run(monitor);
                antBuildFile.delete();
                projectHandle.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
            } catch (final CoreException e) {
                XAHelpLogger.logError("Failed to run ANT example project preparation", e);
                return false;
            }
        }
        updateExampleElement(projectHandle, projDef.getHelpUrl(), EXAMPLE_HELP, HELP_UPDATE_ERROR);
        updateExampleElement(projectHandle, projDef.getCheatSheetId(), CHEAT_SHEET_ID, CHEAT_SHEET_UPDATE_ERROR);
        XAProjectExplorer.recordNewExampleProject(projectHandle, new ExampleProjectInfo(getQualifiedContextId(projDef.getHelpUrl()), projDef.getCheatSheetId()));
        if (projDef.getId().equals(STARTER_EXAMPLE_PROJ_ID)) evaluateDerbyServiceStatus();
        NavigatorView nv = XA_Designer_Plugin.getNavigatorView();
        nv.recalculateExampleProjectStatus(projectHandle);
        if (fireProjectSpecifics && XA_Designer_Plugin.getActivePage().isPartVisible(nv)) {
            try {
                XA_Designer_Plugin.getActivePage().showView(GlobalConstants.ID_HELP_VIEW, null, IWorkbenchPage.VIEW_VISIBLE);
            } catch (PartInitException e) {
            }
            nv.getXawareResNav().showDynamicHelp();
            nv.getXawareResNav().launchDerbyService();
        }
        return true;
    }

    /**
     * If this is a starter example project ensure that the status for derby server startup is evaluated.
     * 
     */
    private static void evaluateDerbyServiceStatus() {
        try {
            XAServicesHandler.getInstance().evaluateDerbyServiceStatus();
        } catch (CoreException exception) {
            XADesignerLogger.logError("Failed to evaluate the derby service status ", exception);
        } catch (XAwareException exception) {
            XADesignerLogger.logError("Failed to evaluate the derby service status ", exception);
        }
    }

    /**
     * Update the .xawareproject file with the example's help context.
     * 
     * @param project
     *            IProject instance.
     * @param contextId
     *            dynamic help context id.
     * @param elemName Name of the child element to be added to the resource file root element.
     * @param errMsg error message specific to the element being added.            
     */
    private static void updateExampleElement(final IProject project, final String id, String elemName, String errMsg) {
        if (id == null || "".equals(id)) {
            return;
        }
        final File resourcePathFile = ResourceUtils.getResourceFile(project.getName());
        final SAXBuilder sb = new SAXBuilder();
        if (resourcePathFile.exists()) {
            try {
                final Document document = sb.build(resourcePathFile);
                Element elem = document.getRootElement().getChild(elemName);
                if (elem == null) {
                    elem = new Element(elemName);
                    document.getRootElement().addContent(elem);
                }
                String elemText = "";
                if (elemName.equals(EXAMPLE_HELP)) elemText = getQualifiedContextId(id); else elemText = id;
                elem.setText(elemText);
                final XMLOutputter outputter = new XMLOutputter();
                outputter.output(document, new FileOutputStream(resourcePathFile));
            } catch (final JDOMException exception) {
                XADesignerLogger.logError("Failed to parse XML in file " + resourcePathFile.getAbsolutePath(), exception);
            } catch (final FileNotFoundException e) {
                XAHelpLogger.logError(errMsg, e);
            } catch (final IOException e) {
                XAHelpLogger.logError(errMsg, e);
            }
        }
    }

    /**
     * Add help plugin's name to context string if it isn't already there
     * 
     * @param context
     *            dynamic help context id.
     * 
     * @return context id qualified with help plug-in name.
     */
    private static String getQualifiedContextId(final String context) {
        String qualifiedContext = context;
        final String helpPluginName = HelpPlugin.getDefault().getPluginID();
        if (!qualifiedContext.startsWith(helpPluginName)) {
            qualifiedContext = helpPluginName + "." + context;
        }
        return qualifiedContext;
    }

    /**
     * Copy from the source path to the target all files and folders in the example.
     * 
     * @param sourceUrl
     *            url of the source folder.
     * @param targetProj
     *            project instance.
     * @param shell
     *            shell instance.
     */
    private static void copyExample(final URL sourceUrl, final IProject targetProj, final Shell shell) {
        try {
            final String sourceFile = FileLocator.toFileURL(sourceUrl).getFile();
            final CopyFilesAndFoldersOperation copyOper = new CopyFilesAndFoldersOperation(shell);
            final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
            copyExampleResources(root, sourceFile, targetProj, copyOper);
        } catch (final IOException e) {
            XAHelpLogger.logError("Failed to convert URL:" + sourceUrl, e);
        }
    }

    /**
     * Recursively copies files form the source folder to the target project.
     * 
     * @param root
     *            workspace root
     * @param source
     *            source file location.
     * @param target
     *            target project
     * @param copyOper
     *            CopyFilesAndFoldersOperation instance.
     */
    private static void copyExampleResources(final IWorkspaceRoot root, final String source, final IContainer target, final CopyFilesAndFoldersOperation copyOper) {
        try {
            target.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
        } catch (final CoreException e) {
            XAHelpLogger.logError("Failed to refresh project " + target.getName() + " before initializing with example's files and folders", e);
        }
        String adjustedSource = source;
        if (adjustedSource.startsWith("/")) {
            adjustedSource = adjustedSource.substring(1);
        }
        if (!adjustedSource.endsWith("/")) {
            adjustedSource += "/";
        }
        final File base = new File(adjustedSource);
        if (base.isDirectory()) {
            String copyCandidates[] = base.list();
            final List<String> ccList = new ArrayList<String>(copyCandidates.length);
            for (int i = 0; i < copyCandidates.length; i++) {
                if (!copyCandidates[i].startsWith(".")) {
                    final IResource res = target.findMember(copyCandidates[i]);
                    if (res != null) {
                        if (res instanceof IContainer) {
                            copyExampleResources(root, adjustedSource + copyCandidates[i], (IContainer) res, copyOper);
                        } else if (res instanceof IFile) {
                            ccList.add(adjustedSource + copyCandidates[i]);
                        }
                    } else {
                        ccList.add(adjustedSource + copyCandidates[i]);
                    }
                }
            }
            if (ccList.size() > 0) {
                copyCandidates = new String[ccList.size()];
                copyCandidates = ccList.toArray(copyCandidates);
                copyOper.copyFiles(copyCandidates, target);
            }
        } else if (base.isFile()) {
            final String baseList[] = new String[1];
            baseList[0] = base.getAbsolutePath();
            copyOper.copyFiles(baseList, target);
        }
    }
}
