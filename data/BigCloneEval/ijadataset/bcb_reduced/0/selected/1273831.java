package ru.sitekeeper.cpn;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import ru.sitekeeper.cpn.gui.PersistentFileChooser;
import ru.sitekeeper.cpn.gui.StatusDialog;

/**
 * 
 * @author $Author: alx27 $
 * @version $Id: ConflictFinder.java,v 1.1 2008/08/03 08:04:47 alx27 Exp $
 *
 */
public class ConflictFinder {

    private static final int THRESHOLD_CLASSES = 5000;

    private static final long THRESHOLD_BYTES = 1 * 1024 * 1024;

    private File[] filesAndDirs = new File[0];

    private long spaceWasted = 0;

    private StatusDialog sd;

    private boolean completed = false;

    private int totalClasses;

    private int totalJars;

    private String sb;

    private boolean conflictsDetected;

    public ConflictFinder(File[] files, StatusDialog _sd) {
        filesAndDirs = files;
        this.sd = _sd;
    }

    public synchronized void findConflicts() {
        completed = false;
        if (sd != null) sd.setTextThrottled("Collecting jars...");
        Log.log("1");
        Collection<File> allFoundJars = new HashSet<File>();
        for (int i = 0; i < filesAndDirs.length; i++) {
            Utils.collectJars(allFoundJars, filesAndDirs[i]);
        }
        totalJars = allFoundJars.size();
        if (Thread.currentThread().isInterrupted()) return;
        if (sd != null) sd.setTextThrottled("Collecting classes...");
        Log.log("2");
        final Map<String, Collection<File>> classToJarsMap = Utils.collectClasses(allFoundJars);
        allFoundJars = null;
        totalClasses = classToJarsMap.size();
        Log.log("Total classes: " + totalClasses);
        if (Thread.currentThread().isInterrupted()) return;
        if (sd != null) sd.setTextThrottled("Detecting conflicts...");
        Log.log("3");
        Utils.removeNotConflicting(classToJarsMap);
        conflictsDetected = !classToJarsMap.isEmpty();
        Log.log("Conflicting classes: " + classToJarsMap.size());
        if (Thread.currentThread().isInterrupted()) return;
        Log.log("4");
        Collection<File> conflictingJars = Utils.detectJars(classToJarsMap);
        Log.log("5");
        if (Thread.currentThread().isInterrupted()) return;
        spaceWasted = Utils.getSpace(classToJarsMap);
        Log.log("6");
        completed = true;
        Log.log("7");
        if (sd != null) sd.setTextThrottled("Preparing output... " + classToJarsMap.size());
        try {
            if (classToJarsMap.size() > THRESHOLD_CLASSES) {
                produceOutputInFile(classToJarsMap, conflictingJars);
            } else {
                produceOutputInMemory(classToJarsMap, conflictingJars);
            }
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(sd, "Too many conflicts found, not enough memory", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(sd, "Can't produce a list of conflicts due to " + e, "Error", JOptionPane.ERROR_MESSAGE);
        }
        Log.log("Text data length: " + (sb != null ? sb.length() : 0));
        Log.log("8");
    }

    private void produceOutputInFile(final Map<String, Collection<File>> classToJarsMap, Collection<File> conflictingJars) throws IOException {
        Log.log("produceOutputInFile");
        File tempFile = File.createTempFile("cpn", ".txt");
        Log.log("Temp file " + tempFile.getAbsolutePath());
        tempFile.deleteOnExit();
        BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile));
        prepareOutput(bw, classToJarsMap, conflictingJars);
        bw.flush();
        bw.close();
        bw = null;
        classToJarsMap.clear();
        conflictingJars.clear();
        long size = tempFile.length();
        Log.log("File size " + size);
        if (size > THRESHOLD_BYTES) {
            long sizeM = size / (1024 * 1024);
            final String OPTION_OPEN = "Open with default application";
            final String OPTION_DONT_OPEN = "Don't open";
            final String OPTION_SAVE = "Save...";
            JOptionPane jop = new JOptionPane("Report file is huge, about " + sizeM + "M", JOptionPane.WARNING_MESSAGE, 0, null, new String[] { OPTION_OPEN, OPTION_DONT_OPEN, OPTION_SAVE }, OPTION_OPEN);
            JDialog dlg = jop.createDialog(sd, "Warning");
            dlg.setVisible(true);
            Object selectedValue = jop.getValue();
            if (selectedValue != null) {
                if (OPTION_OPEN.equals(selectedValue)) {
                    if (Desktop.isDesktopSupported()) {
                        try {
                            Desktop.getDesktop().open(tempFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (OPTION_SAVE.equals(selectedValue)) {
                    PersistentFileChooser fc = PersistentFileChooser.getFC(PersistentFileChooser.PREFIX_CONFLICT_FINDER_REPORT);
                    fc.setDialogType(JFileChooser.SAVE_DIALOG);
                    fc.setDialogTitle("Save report");
                    fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                    fc.setMultiSelectionEnabled(false);
                    int returnVal = fc.showOpenDialog(sd);
                    fc.save();
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File targetFile = fc.getSelectedFile();
                        tempFile.renameTo(targetFile);
                    } else {
                        tempFile.delete();
                    }
                }
            }
        } else {
            StringBuilder sbld = new StringBuilder((int) size);
            BufferedReader br = new BufferedReader(new FileReader(tempFile));
            String line = null;
            while ((line = br.readLine()) != null) {
                sbld.append(line + "\n");
            }
            br.close();
            br = null;
            sb = sbld.toString();
            sbld = null;
            tempFile.delete();
        }
    }

    private void produceOutputInMemory(final Map<String, Collection<File>> classToJarsMap, Collection<File> conflictingJars) {
        Log.log("produceOutputInMemory");
        ByteArrayOutputStream baos = new ByteArrayOutputStream(classToJarsMap.size() * 100);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(baos));
        try {
            prepareOutput(bw, classToJarsMap, conflictingJars);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            JOptionPane.showMessageDialog(sd, "Too many conflicts found, not enough memory", "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                bw.flush();
                bw.close();
                baos.flush();
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            sb = baos.toString();
            bw = null;
            baos = null;
        }
    }

    public boolean conflictsDetected() {
        return conflictsDetected;
    }

    private void prepareOutput(final Writer writer, final Map<String, Collection<File>> conflictingClasses, final Collection<File> conflictingJars) throws IOException {
        writer.write("jars: " + totalJars + Utils.LINEBREAK);
        writer.write("classes: " + totalClasses + Utils.LINEBREAK);
        writer.write("conflicting classes: " + conflictingClasses.size() + Utils.LINEBREAK + Utils.LINEBREAK);
        outputConflicts(conflictingClasses, writer);
        writer.write(Utils.LINEBREAK + "conflicting jars: " + conflictingJars.size() + Utils.LINEBREAK + Utils.LINEBREAK);
        writer.write(Utils.outputJars(conflictingJars) + Utils.LINEBREAK + Utils.LINEBREAK);
        writer.write("Space wasted (approximately), bytes: " + spaceWasted + Utils.LINEBREAK);
    }

    public String getLog() {
        return sb;
    }

    private static void outputConflicts(final Map<String, Collection<File>> conflictingClasses, final Writer writer) throws IOException {
        Log.log("Classes: " + conflictingClasses.size());
        for (Iterator<String> it = conflictingClasses.keySet().iterator(); it.hasNext(); ) {
            String classname = it.next();
            writer.write("Class: " + classname + Utils.LINEBREAK);
            Collection<File> jars = conflictingClasses.get(classname);
            for (File jarfile : jars) {
                writer.write("JAR: " + jarfile.getAbsolutePath() + Utils.LINEBREAK);
            }
            writer.write(Utils.LINEBREAK);
            it.remove();
        }
    }

    public void doTheStuff() {
        if (sd != null) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    sd.setVisible(true);
                }
            });
        }
        findConflicts();
        if (sd != null) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    sd.setVisible(false);
                }
            });
        }
    }

    public boolean isCompleted() {
        return completed;
    }
}
