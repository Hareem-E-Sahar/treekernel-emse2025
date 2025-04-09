package bpmetrics.business;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.eclipse.core.internal.resources.File;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.part.FileEditorInput;
import Metadata.BpxlType;
import Metadata.DateType;
import Metadata.DocumentRoot;
import Metadata.InstanceType;
import Metadata.MetadataPackage;
import Metadata.ProcessType;
import Metadata.impl.MetadataFactoryImpl;
import Metadata.impl.MetadataPackageImpl;
import Metadata.util.MetadataResourceFactoryImpl;
import Metadata.util.MetadataResourceImpl;

public class BpxlCommon {

    public static boolean openBpelEditor(IWorkbenchPartSite site, BpelEditorInput editorInput) {
        ContainerSelectionDialog dialog = new ContainerSelectionDialog(site.getShell(), ResourcesPlugin.getWorkspace().getRoot(), true, "Select folder to import BPEL file");
        dialog.create();
        if (dialog.open() == dialog.OK) {
            Object[] path = (Object[]) dialog.getResult();
            try {
                editorInput.initializeFile((IPath) path[0]);
                FileEditorInput fei = new FileEditorInput(editorInput.getBpelFile()) {

                    @Override
                    public boolean equals(Object obj) {
                        if (obj instanceof BpelEditorInput) return false; else return super.equals(obj);
                    }
                };
                site.getPage().openEditor(fei, "org.eclipse.bpel.ui.bpeleditor");
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    public static java.io.File openExportDialog(IWorkbenchPartSite site) {
        FileDialog dialog = new FileDialog(site.getShell(), SWT.SAVE);
        dialog.setFilterExtensions(new String[] { "*.bpxl" });
        dialog.setFilterNames(new String[] { "Business process metrics" });
        String path = dialog.open();
        if (path != null) {
            if (!path.trim().toLowerCase().endsWith(".bpxl")) path += ".bpxl";
            return new java.io.File(path);
        }
        return null;
    }

    public static String getProjectName(IPath path) {
        String pathOS = path.toOSString().substring(java.io.File.separator.length()) + java.io.File.separator;
        return pathOS.substring(0, pathOS.indexOf(java.io.File.separator));
    }

    public static String getPathInProject(IPath path) {
        String pathOS = path.toOSString().substring(java.io.File.separator.length()) + java.io.File.separator;
        return pathOS.substring(pathOS.indexOf(java.io.File.separator));
    }

    public static String getAbsolutePath(IPath path) {
        return ResourcesPlugin.getWorkspace().getRoot().getProject(getProjectName(path)).getLocationURI().getPath() + getPathInProject(path);
    }

    public static boolean exportConfiguration(BpxlBPELProcess[] procs, java.io.File path) {
        HashMap<String, Boolean> filename = new HashMap<String, Boolean>();
        MetadataFactoryImpl mfi = new MetadataFactoryImpl();
        MetadataResourceFactoryImpl mrfi = new MetadataResourceFactoryImpl();
        Resource res = (MetadataResourceImpl) mrfi.createResource(null);
        for (BpxlBPELProcess process : procs) {
            int idx = 1;
            String processFilename = process.getName() + ".bpel";
            while (filename.containsKey(processFilename)) processFilename = process.getName() + "_conf" + (idx++) + ".bpel";
            filename.put(processFilename, Boolean.TRUE);
            ProcessType processType = mfi.createProcessType();
            processType.setFilename(processFilename);
            processType.setName(process.getName());
            BpxlType bpxlType = mfi.createBpxlType();
            bpxlType.setConfigurationName(process.getConfiguration().getLabel());
            bpxlType.setConfigurationType(process.getConfiguration().getClass().getSimpleName());
            bpxlType.setDate(new Date().getTime() + "");
            bpxlType.getProcess().add(processType);
            DocumentRoot root = mfi.createDocumentRoot();
            root.setBpxl(bpxlType);
            try {
                ZipOutputStream out = new ZipOutputStream(new FileOutputStream(path));
                BpxlProcessInstance[] pInstances = process.getConfiguration().getProcessInstances(process);
                for (int i = 0; i < pInstances.length; i++) {
                    String instanceFilename = processFilename + ".pid." + pInstances[i].getPid() + "";
                    DateType dateType = mfi.createDateType();
                    dateType.setStarted(pInstances[i].getStart().getTime() + "");
                    dateType.setEnded(pInstances[i].getStop().getTime() + "");
                    InstanceType instanceType = mfi.createInstanceType();
                    instanceType.setFilename(instanceFilename);
                    instanceType.setPid(pInstances[i].getPid() + "");
                    instanceType.setState(pInstances[i].getState() + "");
                    instanceType.setDate(dateType);
                    processType.getInstance().add(instanceType);
                    out.putNextEntry(new ZipEntry(instanceFilename));
                    out.write(pInstances[i].getLog().getBytes());
                    out.closeEntry();
                }
                out.putNextEntry(new ZipEntry("metadata.xml"));
                res.getContents().add(root);
                res.save(out, null);
                out.putNextEntry(new ZipEntry(processFilename));
                out.write(process.getBpel().getBytes());
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static BpxlConfiguration importConfiguration(java.io.File path) {
        return new BpxlFileConfiguration(path);
    }
}
