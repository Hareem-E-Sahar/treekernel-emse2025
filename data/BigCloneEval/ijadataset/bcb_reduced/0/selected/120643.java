package com.javable.dataview.actions;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import com.javable.dataview.ResourceManager;
import com.javable.utils.ExceptionDialog;
import com.javable.utils.ExtensionFileFilter;

public class ExportResAction extends com.javable.utils.ActionSupport implements java.beans.PropertyChangeListener {

    private java.io.File dir;

    private java.io.File file;

    private JFileChooser filechooser;

    private com.javable.dataview.DataFrame ifr = null;

    private com.javable.dataview.DataDesktop desktop;

    /**
     * Creates new action
     * 
     * @param d desktop
     */
    public ExportResAction(com.javable.dataview.DataDesktop d) {
        this();
        desktop = d;
        desktop.addPropertyChangeListener(this);
    }

    /**
     * Creates new action
     */
    public ExportResAction() {
        super(ResourceManager.getResource("Export_Results..."), ResourceManager.getResource("Export_results"), ResourceManager.getResource("export_image"));
        dir = new java.io.File(this.getClass().getResource(ResourceManager.getResource("res_dir")).getFile());
    }

    /**
     * Action interface implementation
     * 
     * @param evt event
     */
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        filechooser = new JFileChooser(dir);
        filechooser.setDialogTitle(ResourceManager.getResource("Export_Results"));
        filechooser.resetChoosableFileFilters();
        filechooser.setAcceptAllFileFilterUsed(true);
        com.javable.utils.ExtensionFileFilter asciiFilter = new com.javable.utils.ExtensionFileFilter(ResourceManager.getResource("ascii_extension"), ResourceManager.getResource("ASCII_Files"));
        filechooser.addChoosableFileFilter(asciiFilter);
        com.javable.utils.ExtensionFileFilter atfFilter = new com.javable.utils.ExtensionFileFilter(ResourceManager.getResource("atf_extension"), ResourceManager.getResource("ATF_Files"));
        filechooser.addChoosableFileFilter(atfFilter);
        com.javable.utils.ExtensionFileFilter netcdfFilter = new com.javable.utils.ExtensionFileFilter(ResourceManager.getResource("nc_extension"), ResourceManager.getResource("NetCDF_Files"));
        filechooser.addChoosableFileFilter(netcdfFilter);
        com.javable.utils.ExtensionFileFilter pngFilter = new com.javable.utils.ExtensionFileFilter(ResourceManager.getResource("png_extension"), ResourceManager.getResource("PNG_Files"));
        filechooser.addChoosableFileFilter(pngFilter);
        filechooser.setFileFilter(asciiFilter);
        int retVal = filechooser.showSaveDialog(desktop);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            file = filechooser.getSelectedFile();
            dir = filechooser.getCurrentDirectory();
            if (file.getPath() == null) return;
            ExtensionFileFilter filter = (ExtensionFileFilter) filechooser.getFileFilter();
            if (filter.getExtension(file) == null) file = new java.io.File(file.getAbsolutePath() + "." + filter.getExtension());
            if (file.exists()) {
                int n = JOptionPane.showConfirmDialog(desktop, ResourceManager.getResource("Overwrite_file_") + file.getPath() + " ?", ResourceManager.getResource("File_already_exists"), JOptionPane.YES_NO_OPTION);
                if (n == JOptionPane.NO_OPTION) return;
            }
            ifr = (com.javable.dataview.DataFrame) desktop.getActiveFrame();
            if (ifr != null) {
                com.javable.utils.SwingWorker worker = new com.javable.utils.SwingWorker() {

                    public Object construct() {
                        try {
                            ExtensionFileFilter filter = (ExtensionFileFilter) filechooser.getFileFilter();
                            if (filter.getExtension(file).equals(ResourceManager.getResource("res_extension"))) ifr.getSelectedContent().exportASCII(file.getPath()); else if (filter.getExtension(file).equals(ResourceManager.getResource("atf_extension"))) ifr.getSelectedContent().exportATF(file.getPath()); else if (filter.getExtension(file).equals(ResourceManager.getResource("nc_extension"))) ifr.getSelectedContent().exportNetCDF(file.getPath()); else if (filter.getExtension(file).equals(ResourceManager.getResource("png_extension"))) ifr.getSelectedContent().exportPNG(file.getPath()); else {
                                ifr.getSelectedContent().exportASCII(file.getPath());
                            }
                        } catch (Exception e) {
                            ExceptionDialog.showExceptionDialog(ResourceManager.getResource("Error"), ResourceManager.getResource("Error_saving_results_file."), e);
                        }
                        return ResourceManager.getResource("Done");
                    }

                    public void finished() {
                        desktop.getTasks().removeTask();
                    }
                };
                desktop.getTasks().addTask(ResourceManager.getResource("Export_Results") + " " + file.getPath());
                worker.start();
            }
        }
    }

    /**
     * Reflects change in the property (disables/enables) action
     * 
     * @param evt event
     */
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("frame_control")) this.setEnabled(((Boolean) evt.getNewValue()).booleanValue());
    }
}
