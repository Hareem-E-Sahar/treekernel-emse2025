package com.jameslow.limegreen;

import java.io.*;
import java.net.*;
import java.util.jar.Manifest;
import org.eclipse.core.resources.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.*;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.*;

public class NewProject extends Wizard implements INewWizard {

    private WizardNewProjectCreationPage _pageOne;

    private String PROJECT_TYPE, PROJECT_NAME, WIZARD_NAME, WIZARD_TITLE, PROJECT_DESC;

    public NewProject() {
        PROJECT_TYPE = getManifestName();
        PROJECT_NAME = PROJECT_TYPE + " Project";
        WIZARD_NAME = PROJECT_NAME + " Wizard";
        WIZARD_TITLE = "New " + PROJECT_NAME;
        PROJECT_DESC = "Create a " + PROJECT_NAME;
        setWindowTitle(WIZARD_TITLE);
    }

    public String getManifestName() {
        String name = "Custom";
        try {
            Manifest manifest = new Manifest(getClass().getResourceAsStream("/META-INF/MANIFEST.MF"));
            name = manifest.getMainAttributes().getValue("Bundle-Name");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return name;
    }

    public String getTemplatePath() {
        return "/template";
    }

    public File getTemplateDir() {
        URL url = getClass().getResource(getTemplatePath());
        if ("file".compareTo(url.getProtocol()) != 0) {
            try {
                url = org.eclipse.core.runtime.FileLocator.resolve(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File file = new File(url.getPath());
        return file;
    }

    public void init(IWorkbench workbench, IStructuredSelection selection) {
    }

    public void addPages() {
        super.addPages();
        _pageOne = new WizardNewProjectCreationPage(WIZARD_NAME);
        _pageOne.setTitle(PROJECT_NAME);
        _pageOne.setDescription(PROJECT_DESC);
        addPage(_pageOne);
    }

    public void createProject(String projectName, URI location) {
        IProject newProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
        if (!newProject.exists()) {
            URI projectLocation = location;
            IProjectDescription desc = newProject.getWorkspace().newProjectDescription(newProject.getName());
            if (location != null && ResourcesPlugin.getWorkspace().getRoot().getLocationURI().equals(location)) {
                projectLocation = null;
            }
            desc.setLocationURI(projectLocation);
            try {
                newProject.create(desc, null);
                if (!newProject.isOpen()) {
                    newProject.open(null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean performFinish() {
        File template = getTemplateDir();
        URI location = null;
        if (!_pageOne.useDefaults()) {
            location = _pageOne.getLocationURI();
        }
        createProject(_pageOne.getProjectName(), location);
        if (location == null) {
            location = _pageOne.getLocationURI();
        }
        try {
            copyFiles(template, new File(location));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public void copyFiles(File src, File dest) throws IOException {
        if (!src.exists()) {
            throw new IOException("copyFiles: Can not find source: " + src.getAbsolutePath() + ".");
        } else if (!src.canRead()) {
            throw new IOException("copyFiles: No right to source: " + src.getAbsolutePath() + ".");
        }
        if (src.isDirectory()) {
            if (!dest.exists()) {
                if (!dest.mkdirs()) {
                    throw new IOException("copyFiles: Could not create direcotry: " + dest.getAbsolutePath() + ".");
                }
            }
            String list[] = src.list();
            for (int i = 0; i < list.length; i++) {
                File dest1 = new File(dest, list[i]);
                File src1 = new File(src, list[i]);
                copyFiles(src1, dest1);
            }
        } else {
            FileInputStream fin = null;
            FileOutputStream fout = null;
            byte[] buffer = new byte[4096];
            int bytesRead;
            try {
                fin = new FileInputStream(src);
                fout = new FileOutputStream(dest);
                while ((bytesRead = fin.read(buffer)) >= 0) {
                    fout.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                IOException wrapper = new IOException("copyFiles: Unable to copy file: " + src.getAbsolutePath() + "to" + dest.getAbsolutePath() + ".");
                wrapper.initCause(e);
                wrapper.setStackTrace(e.getStackTrace());
                throw wrapper;
            } finally {
                if (fin != null) {
                    fin.close();
                }
                if (fout != null) {
                    fout.close();
                }
            }
        }
    }
}
