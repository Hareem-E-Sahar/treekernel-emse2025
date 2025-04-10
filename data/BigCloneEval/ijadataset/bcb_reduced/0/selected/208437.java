package jshm.internal.patcher;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.*;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.error.ErrorInfo;
import jshm.logging.FileFormatter;
import jshm.util.Properties;

/**
 * This class serves to replace files in an existing jar with the new patched
 * files.
 * 
 * @author Tim Mullin
 * 
 */
public class Patcher {

    static class PatchEntry {

        String name;

        InputStream inStream;

        public PatchEntry(String name, InputStream inStream) {
            this.name = name;
            this.inStream = inStream;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            String oname = null;
            if (o instanceof PatchEntry) oname = ((PatchEntry) o).name; else if (o instanceof String) oname = (String) o; else return false;
            return this.name.equals(oname);
        }
    }

    static final int FILE_LOCK_LIMIT = 10;

    static Properties props = new Properties();

    static File patchJarFile = null;

    static File progJarFile = null;

    static PatcherGui gui = null;

    static final Logger LOG = Logger.getLogger(Patcher.class.getName());

    static boolean unattended = false;

    static boolean restartJshm = false;

    public static void main(String[] args) throws Exception {
        for (String s : args) {
            if (s.equals("?") || s.equals("-?") || s.equals("/?") || s.equalsIgnoreCase("-h") || s.equalsIgnoreCase("-help")) {
                System.out.println("Usage: java -jar JSHManager-patch-a.b.c-to-x.y.z.r.jar [-u] [-r] [-h]\n" + "    -u|-unattended    Automatically close the patcher after completion\n" + "    -r|-restartjshm   Automatically restart JSHManager after completion\n" + "    -h|-help          Display this message");
                System.exit(0);
            } else if (s.equalsIgnoreCase("-u") || s.equalsIgnoreCase("-unattended")) {
                unattended = true;
                LOG.finer("Got unattended switch");
            } else if (s.equalsIgnoreCase("-r") || s.equalsIgnoreCase("-restartjshm")) {
                restartJshm = true;
                LOG.finer("Got restart jshm switch");
            }
        }
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }
        try {
            try {
                progJarFile = getProgJarFile().getAbsoluteFile();
                SwingUtilities.invokeAndWait(new Runnable() {

                    public void run() {
                        gui = new PatcherGui();
                        gui.setLocationRelativeTo(null);
                        gui.setVisible(true);
                    }
                });
                configLogging();
                log("Current folder is " + progJarFile.getParentFile().getCanonicalPath());
                log("Checking JSHManager.jar...");
                int progJarFileWriteTestCount = 0;
                while (isFileLockedReadOnly(progJarFile) && progJarFileWriteTestCount < FILE_LOCK_LIMIT) {
                    progJarFileWriteTestCount++;
                    log("JSHmanager.jar is locked, waiting for it to become available (" + progJarFileWriteTestCount + ")");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                }
                if (progJarFileWriteTestCount == FILE_LOCK_LIMIT) {
                    log("JSHmanager.jar is still locked, patching cannot continue.");
                    log("Ensure that JSHManager is not running and try again.", 1, 1);
                } else {
                    JarLoader.addFileToClasspath(progJarFile);
                    log("Checking patch file...");
                    String url = Patcher.class.getResource("/jshm/internal/patcher/Patcher.class").toExternalForm();
                    File f = getJarFileFromURL(url);
                    if (null == f) {
                        url = Patcher.class.getResource("/jshm/internal/patcher/PatchTest.jar").toExternalForm();
                        f = new File(URI.create(url));
                    }
                    patchJarFile = f;
                    log("Using patch file " + patchJarFile.getCanonicalPath());
                    JarFile patchJar = new JarFile(f);
                    JarEntry propsEntry = patchJar.getJarEntry("jshm/internal/patcher/Patcher.properties");
                    if (null != propsEntry) {
                        props.load(patchJar.getInputStream(propsEntry));
                    }
                    log("Loaded " + props.keySet().size() + " patcher properties");
                    for (Object key : props.keySet()) {
                        log("  " + key + "=" + props.get(key));
                    }
                    log("Patching JSHManager.jar...");
                    addFilesToExistingZip(progJarFile, getPatchEntries(patchJar, "jar/"), Arrays.asList(props.getArray("jardelete")));
                    log("Patching main directory...");
                    patchMainDir(progJarFile.getParentFile(), getPatchEntries(patchJar, "folder/"), Arrays.asList(props.getArray("folderdelete")));
                    log("Patching complete.", 1, 1);
                    if (restartJshm) {
                        log("Restarting JSHManager...");
                        jshm.util.Exec.execNoOutput(progJarFile.getAbsoluteFile().getParentFile(), "java", "-jar", progJarFile.getName());
                    }
                }
                gui.setClosedEnabled(true);
                if (unattended) {
                    LOG.finer("Disposing GUI due to unattended switch");
                    gui.dispose();
                    System.exit(0);
                }
            } catch (Throwable t) {
                LOG.log(Level.SEVERE, "Unknown error while patching", t);
                if (!unattended) {
                    ErrorInfo ei = new org.jdesktop.swingx.error.ErrorInfo("Error", "Unknown error while patching", null, null, t, null, null);
                    JXErrorPane.showDialog(null, ei);
                }
                gui.dispose();
                System.exit(-1);
            }
        } catch (Throwable t) {
            LOG.log(Level.SEVERE, "Unknown error while patching", t);
            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            if (!unattended) {
                JOptionPane.showMessageDialog(null, sw.toString(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            gui.dispose();
            System.exit(-1);
        }
    }

    static void configLogging() throws Exception {
        Handler h = new ListLogHandler(gui, Level.ALL);
        LOG.addHandler(h);
        File logDir = new File(progJarFile.getParentFile(), "data/logs");
        logDir.mkdirs();
        h = new FileHandler(logDir.getAbsolutePath() + "/Patcher.txt");
        h.setLevel(Level.ALL);
        h.setFormatter(new FileFormatter());
        LOG.addHandler(h);
    }

    static void log(String msg) {
        log(msg, -1, -1);
    }

    static void log(String msg, int cur, int max) {
        LOG.info(msg);
        gui.setProgress(msg, cur, max);
    }

    public static File getProgJarFile() throws Exception {
        File f = new File("JSHManager.jar");
        if (!f.exists()) {
            JOptionPane.showMessageDialog(null, "JSHManager.jar is required but was not found.\n" + "Please locate it so that it can be patched.", "Error", JOptionPane.WARNING_MESSAGE);
            final JFileChooser fc = new JFileChooser();
            fc.setCurrentDirectory(new File("."));
            fc.setAcceptAllFileFilterUsed(false);
            fc.setFileFilter(new FileFilter() {

                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().equals("JSHManager.jar");
                }

                @Override
                public String getDescription() {
                    return "JSHManager.jar";
                }
            });
            int ret = fc.showDialog(null, null);
            if (ret != JFileChooser.APPROVE_OPTION) {
                JOptionPane.showMessageDialog(null, "JSHManager Patcher was canceled.", "", JOptionPane.INFORMATION_MESSAGE);
                System.exit(0);
            }
            f = fc.getSelectedFile();
        }
        return f;
    }

    public static List<PatchEntry> getPatchEntries(final JarFile patchJar, final String prefix) throws IOException {
        List<PatchEntry> ret = new ArrayList<PatchEntry>();
        Enumeration<JarEntry> en = patchJar.entries();
        while (en.hasMoreElements()) {
            JarEntry cur = en.nextElement();
            if (cur.isDirectory()) continue;
            if (cur.getName().startsWith(prefix)) {
                ret.add(new PatchEntry(cur.getName().substring(prefix.length()), patchJar.getInputStream(cur)));
            }
        }
        return ret;
    }

    public static void patchMainDir(final File dir, final List<PatchEntry> entries, final List<String> delete) throws Exception {
        if (!dir.isDirectory()) throw new IllegalArgumentException("dir is not a directory: " + dir.getCanonicalPath());
        int i = 0;
        for (String s : delete) {
            File cur = new File(dir, s);
            log(String.format("Deleting %s (%s of %s)", s, i + 1, delete.size()));
            cur.delete();
            i++;
        }
        i = 0;
        for (PatchEntry e : entries) {
            File cur = new File(dir, e.name);
            log(String.format("Patching %s (%s of %s)", e.name, i + 1, entries.size()), i, entries.size());
            JarLoader.copy(e.inStream, cur);
            i++;
        }
    }

    public static void addFilesToExistingZip(final File zipFile, final List<PatchEntry> entries, final List<String> delete) throws IOException {
        File tempFile = File.createTempFile(zipFile.getName(), null);
        tempFile.delete();
        boolean renameOk = zipFile.renameTo(tempFile);
        if (!renameOk) {
            throw new RuntimeException("could not rename the file " + zipFile.getAbsolutePath() + " to " + tempFile.getAbsolutePath());
        }
        byte[] buf = new byte[1024];
        ZipInputStream zin = new ZipInputStream(new FileInputStream(tempFile));
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
        ZipEntry entry = zin.getNextEntry();
        int unchangedCount = 0;
        int deletedCount = 0;
        while (entry != null) {
            String name = entry.getName();
            boolean notInFiles = true;
            for (PatchEntry e : entries) {
                if (e.name.equals(name)) {
                    notInFiles = false;
                    break;
                }
            }
            if (notInFiles && delete.contains(name)) {
                deletedCount++;
                log(String.format("Deleting %s (%s of %s)", name, deletedCount, delete.size()));
                notInFiles = false;
            }
            if (notInFiles) {
                unchangedCount++;
                gui.setProgress(String.format("Copying unchanged files (%s so far)...", unchangedCount));
                out.putNextEntry(new ZipEntry(name));
                int len;
                while ((len = zin.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
            entry = zin.getNextEntry();
        }
        zin.close();
        int i = 0;
        for (PatchEntry e : entries) {
            log(String.format("Patching %s (%s of %s)", e.name, i + 1, entries.size()), i, entries.size());
            InputStream in = e.inStream;
            out.putNextEntry(new ZipEntry(e.name));
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.closeEntry();
            in.close();
            i++;
        }
        out.close();
        tempFile.delete();
    }

    public static boolean isFileLockedReadOnly(File file) {
        try {
            RandomAccessFile test = new RandomAccessFile(file, "rw");
            test.close();
            return false;
        } catch (Exception e) {
            System.out.println("locked? " + e);
            return true;
        }
    }

    public static File getJarFileFromURL(String url_str) {
        if (url_str.startsWith("jar:file:")) {
            url_str = url_str.replaceAll(" ", "%20");
            if (!url_str.startsWith("jar:file:/")) {
                url_str = "jar:file:/".concat(url_str.substring(9));
            }
            try {
                int posPling = url_str.lastIndexOf('!');
                String jarName = url_str.substring(4, posPling);
                URI uri = URI.create(jarName);
                File jar = new File(uri);
                return (jar);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return (null);
    }
}
