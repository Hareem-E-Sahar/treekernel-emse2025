package net.sf.fileexchange.ui;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import net.sf.fileexchange.api.FilesUploadedByOthers;
import net.sf.fileexchange.api.Model;
import net.sf.fileexchange.api.FilesUploadedByOthers.Entry;

public class OpenContainingFolderActionListener implements ActionListener {

    private final Model model;

    private final JTable table;

    OpenContainingFolderActionListener(Model model, JTable table) {
        this.model = model;
        this.table = table;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        FilesUploadedByOthers files = model.getFilesUploadedByOthers();
        synchronized (files) {
            int index = table.getSelectedRow();
            final Entry entry = files.get(index);
            boolean supported = Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Action.OPEN);
            if (!supported) {
                JOptionPane.showConfirmDialog(table, "Not supported on your plattform");
                return;
            }
            try {
                Desktop.getDesktop().open(entry.getPath().getParentFile());
            } catch (IOException e) {
                JOptionPane.showConfirmDialog(table, "Can't open folder: " + e.getMessage());
            }
        }
    }
}
