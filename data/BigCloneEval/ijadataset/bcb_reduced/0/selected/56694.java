package com.isa.jump.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import com.vividsolutions.jump.io.datasource.DataSourceQuery;
import com.vividsolutions.jump.util.FileUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CreateBackupPlugIn extends AbstractPlugIn {

    private static JFileChooser fileChooser;

    public void initialize(PlugInContext context) throws Exception {
        fileChooser = GUIUtil.createJFileChooserWithOverwritePrompting();
        fileChooser.setDialogTitle("Save Backup");
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        fileChooser.setMultiSelectionEnabled(false);
        GUIUtil.removeChoosableFileFilters(fileChooser);
        FileFilter fileFilter1 = GUIUtil.createFileFilter("ZIP Files", new String[] { "zip" });
        fileChooser.addChoosableFileFilter(fileFilter1);
        fileChooser.setFileFilter(fileFilter1);
        context.getFeatureInstaller().addMainMenuItem(this, new String[] { "File" }, "Create Backup" + "{pos:8}", false, null, SaveAllPlugIn.createEnableCheck(context.getWorkbenchContext()));
    }

    public boolean execute(PlugInContext context) throws Exception {
        WorkbenchContext workbenchContext = context.getWorkbenchContext();
        if (JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(workbenchContext.getLayerViewPanel())) {
            String zipFileName = fileChooser.getSelectedFile().getPath();
            Collection layerCollection = (Collection) workbenchContext.getLayerNamePanel().getLayerManager().getLayers();
            List filesToZip = new ArrayList();
            try {
                for (Iterator l = layerCollection.iterator(); l.hasNext(); ) {
                    Layer layer = (Layer) l.next();
                    if (layer.hasReadableDataSource()) {
                        DataSourceQuery dsq = layer.getDataSourceQuery();
                        String fname = "";
                        Object fnameObj = dsq.getDataSource().getProperties().get("File");
                        if (fnameObj != null) {
                            fname = fnameObj.toString();
                            if (new File(fname).exists()) {
                                filesToZip.add(fname);
                            }
                        }
                    }
                    if (filesToZip.size() > 0) {
                        byte[] buffer = new byte[18024];
                        try {
                            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName));
                            out.setLevel(Deflater.DEFAULT_COMPRESSION);
                            for (int i = 0; i < filesToZip.size(); i++) {
                                System.out.println(i);
                                FileInputStream in = new FileInputStream((String) filesToZip.get(i));
                                out.putNextEntry(new ZipEntry((String) filesToZip.get(i)));
                                int len;
                                while ((len = in.read(buffer)) > 0) {
                                    out.write(buffer, 0, len);
                                }
                                out.closeEntry();
                                in.close();
                            }
                            out.close();
                        } catch (IllegalArgumentException iae) {
                            iae.printStackTrace();
                        } catch (FileNotFoundException fnfe) {
                            fnfe.printStackTrace();
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                    }
                }
            } catch (Exception e) {
                context.getWorkbenchFrame().getOutputFrame().createNewDocument();
                context.getWorkbenchFrame().warnUser("Error: see output window");
                context.getWorkbenchFrame().getOutputFrame().addText("CreateBackupPlugIn Exception:" + e.toString());
                return false;
            }
        }
        return true;
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createTaskWindowMustBeActiveCheck()).add(checkFactory.createAtLeastNLayersMustExistCheck(1));
    }
}
