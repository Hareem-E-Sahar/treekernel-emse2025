package com.st.jhtmllogger.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import com.st.jhtmllogger.exception.OptionNotFoundException;

public class ZipMaker {

    public static boolean makeZip(final Shell shell, String folder) {
        String zipName;
        FileDialog dialog = new FileDialog(shell, SWT.SAVE);
        dialog.setFilterNames(new String[] { "Zip Files" });
        dialog.setFilterExtensions(new String[] { "*.zip", "*.*" });
        try {
            File f = new File(folder);
            File[] filenames = f.listFiles();
            if (filenames.length == 0) return false;
            dialog.setFilterPath(Options.getOptionValue(Options.LOGS_FOLDER));
            dialog.setFileName(f.getName() + ".zip");
            zipName = dialog.open();
            if (zipName == null || zipName.equals("")) {
                return false;
            }
            try {
                ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipName));
                zipRecursively(out, f, "");
                out.closeEntry();
                out.close();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (OptionNotFoundException e1) {
            e1.printStackTrace();
        }
        return false;
    }

    public static boolean zipSession(Shell shell) {
        String folder = Options.getOptionValue(Options.CURRENT_INSTANCE_FOLDER);
        return makeZip(shell, folder);
    }

    public static boolean zipFolder(Shell shell) {
        DirectoryDialog dialog = new DirectoryDialog(shell);
        dialog.setFilterPath(Options.getOptionValue(Options.LOGS_FOLDER));
        String folder = dialog.open();
        if (folder == null) {
            return false;
        }
        return makeZip(shell, folder);
    }

    private static void zipRecursively(ZipOutputStream zip, File file, String parentName) throws IOException {
        if (file.isFile()) {
            byte[] buf = new byte[1024];
            FileInputStream in = new FileInputStream(file);
            zip.putNextEntry(new ZipEntry(parentName + File.separator + file.getName()));
            int len;
            while ((len = in.read(buf)) > 0) {
                zip.write(buf, 0, len);
            }
            in.close();
        } else {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    zipRecursively(zip, f, parentName + File.separator + file.getName());
                }
            }
        }
    }
}
