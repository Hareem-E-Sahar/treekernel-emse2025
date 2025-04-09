package it.schedesoftware.old;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.*;
import java.io.File;
import java.util.zip.*;
import java.io.*;
import javax.swing.*;
import javax.swing.JFileChooser.*;
import it.schedesoftware.old.AlertBox;
import it.schedesoftware.old.FiltroFileDat;

/**
 *
 * @author andrea
 */
public class DataBackup {

    private AlertBox alert = new AlertBox("ERRORE");

    private File[] cont;

    private File dir;

    private File zipFileName;

    private JFrame parent;

    /** Creates a new instance of DataBackup */
    public DataBackup() {
        dir = new File("dat/");
        makeBackup();
    }

    private void makeBackup() {
        try {
            FiltroFileDat filter = new FiltroFileDat("zip");
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(filter);
            fc.addChoosableFileFilter(filter);
            fc.showSaveDialog(parent);
            if (fc.getSelectedFile().exists()) {
                alert.avviso("File Esistente.");
                return;
            }
            zipFileName = new File(controlzipFile(fc.getSelectedFile().getAbsolutePath()));
        } catch (Exception exc) {
        }
        File[] listFiles = dir.listFiles();
        System.out.println(dir.getAbsolutePath());
        System.out.println(zipFileName.getAbsolutePath());
        int dirLength = listFiles.length;
        byte[] buffer = new byte[18024];
        try {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName.getAbsolutePath()));
            out.setLevel(Deflater.DEFAULT_COMPRESSION);
            for (int i = 0; i < dirLength; i++) {
                if (listFiles[i].isDirectory()) continue;
                FileInputStream in = new FileInputStream(listFiles[i].getAbsolutePath());
                out.putNextEntry(new ZipEntry(listFiles[i].getName()));
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
                out.closeEntry();
                in.close();
            }
            out.close();
        } catch (Exception exc) {
            System.out.println("errore");
        }
    }

    private String controlzipFile(String s) {
        char c[];
        int i, l;
        c = s.toCharArray();
        l = c.length;
        if (l > 4 && c[l - 4] == '.' && c[l - 3] == 'z' && c[l - 2] == 'i' && c[l - 1] == 'p') return s;
        s = s + ".zip";
        return s;
    }
}
