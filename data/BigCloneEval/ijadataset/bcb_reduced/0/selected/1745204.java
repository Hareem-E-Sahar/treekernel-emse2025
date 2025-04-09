package net.sourceforge.rombrowser.roms;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import javax.swing.*;
import java.awt.*;
import javax.swing.tree.*;
import net.sourceforge.rombrowser.roms.*;
import net.sourceforge.rombrowser.gui.*;
import net.sourceforge.rombrowser.util.*;

public class ROMFileLoader {

    private static ProgressMonitor myPM;

    private static int currentProgress = 0;

    private ROMDatabase myKnownFiles = new ROMDatabase("filename");

    private ROMDatabase myROMInfo = new ROMDatabase("rom-CRC");

    public ROMFileLoader(File knownDatabase) {
        addKnownFileDatabase(knownDatabase);
    }

    public void addKnownFileDatabase(File knownDatabase) {
        if (knownDatabase.exists()) {
            try {
                DataFileParser.loadDataFile(knownDatabase, myKnownFiles);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    public ROMFileLoader() {
    }

    public Database getROMInfo() {
        return myROMInfo;
    }

    public Database getKnownFiles() {
        return myKnownFiles;
    }

    public void writeKnownFiles() {
        String s = System.getProperty("net.sourceforge.rombrowser.knownfiles");
        if (s != null) {
            FileOutputStream fos = null;
            GZIPOutputStream gos = null;
            try {
                fos = new FileOutputStream(s);
                gos = new GZIPOutputStream(fos);
                gos.write("<file-list>\n".getBytes());
                myKnownFiles.writeToStream(gos);
                gos.write("</file-list>\n".getBytes());
            } catch (Throwable t) {
                t.printStackTrace();
            } finally {
                try {
                    gos.close();
                } catch (Throwable t) {
                }
                try {
                    fos.close();
                } catch (Throwable t) {
                }
            }
        }
    }

    private void loadFiles(File root, int maxlevel) throws IOException {
        if ((root.isDirectory()) && (maxlevel != 0)) {
            File[] files = root.listFiles(ROMFile.getFileFilter());
            for (int x = 0; x < files.length; ++x) {
                loadFiles(files[x], maxlevel - 1);
            }
        } else {
            myPM.setNote(root.getName());
            myPM.setProgress(++currentProgress);
            ROMFile rf = new ROMFile(root.toString());
            MetaData md = myROMInfo.getEntry(rf.getProperty("rom-CRC").toUpperCase());
            if (md != null) {
                rf.addProperties(md);
                if ((!rf.getName().equals("rom-name")) && (Boolean.getBoolean("net.sourceforge.rombrowser.rename"))) {
                    ROMFile newrf = rf.renameAppropriately(Boolean.getBoolean("net.sourceforge.rombrowser.delete-while-renaming"));
                    if (newrf != rf) {
                        rf = newrf;
                        rf.addProperties(md);
                    }
                }
            }
            myKnownFiles.addEntry(rf);
        }
    }

    private static int countChildren(File root) {
        File[] files = root.listFiles();
        int ret_val = files.length;
        for (int x = 0; x < files.length; ++x) {
            if (files[x].isDirectory()) {
                ret_val = ret_val + countChildren(files[x]);
            }
        }
        return ret_val;
    }

    public void loadROMFiles(final File root) throws IOException {
        (new Thread() {

            public void run() {
                myPM = new ProgressMonitor(null, "Loading ROM Files", "", 0, countChildren(root));
                try {
                    loadFiles(root, -1);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                myPM.close();
            }
        }).start();
    }
}
